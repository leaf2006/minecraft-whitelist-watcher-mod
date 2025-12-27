package org.whitelist_watcher.whitelist_watcher;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.minecraft.server.MinecraftServer;
import net.minecraft.text.Text;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.*;
import java.util.concurrent.atomic.AtomicBoolean;

public class WhitelistWatcherMod implements ModInitializer {
    public static final String MOD_ID = "whitelist watcher";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    // 用于控制监听线程的运行状态
    private final AtomicBoolean isRunning = new AtomicBoolean(false);
    private Thread watcherThread;

    @Override
    public void onInitialize() {
        // 当服务器启动完成时，开启监听线程
        ServerLifecycleEvents.SERVER_STARTED.register(this::startWatcher);

        // 当服务器准备停止时，关闭监听线程，防止资源甚至进程残留
        ServerLifecycleEvents.SERVER_STOPPING.register(this::stopWatcher);
    }

    private void startWatcher(MinecraftServer server) {
        isRunning.set(true);

        watcherThread = new Thread(() -> {
            try (WatchService watchService = FileSystems.getDefault().newWatchService()) {
                // 获取服务器运行目录 (通常是 ".")
                Path serverDir = Paths.get(".");
                // 注册监听器，只监听 "修改" 事件
                serverDir.register(watchService, StandardWatchEventKinds.ENTRY_MODIFY);

                LOGGER.info("Whitelist Watcher started monitoring whitelist.json");

                while (isRunning.get()) {
                    WatchKey key;
                    try {
                        // 阻塞等待文件事件
                        key = watchService.take();
                    } catch (InterruptedException e) {
                        // 线程被中断（服务器关闭时）退出循环
                        return;
                    }

                    for (WatchEvent<?> event : key.pollEvents()) {
                        WatchEvent.Kind<?> kind = event.kind();

                        // 忽略溢出事件
                        if (kind == StandardWatchEventKinds.OVERFLOW) {
                            continue;
                        }

                        // 获取变化的文件名
                        // 几把AI想被我操了
                        @SuppressWarnings("unchecked")
                        WatchEvent<Path> ev = (WatchEvent<Path>) event;
                        Path filename = ev.context();

                        // 检查是否是 whitelist.json
                        if (filename.toString().equals("whitelist.json")) {
                            LOGGER.info("Detected change in whitelist.json!");

                            // !!! 关键点 !!!
                            // 不能在当前线程操作服务器，必须调度回主线程
                            server.execute(() -> {
                                try {
                                    // 方法A: 直接调用内部API刷新白名单 (推荐，更底层更干净)
                                    server.getPlayerManager().reloadWhitelist();
                                    LOGGER.info("Whitelist reloaded successfully via API.");

                                    // 通知控制台或OP (可选)
                                    server.sendMessage(Text.of("§e[Watcher] Whitelist updated and reloaded automatically."));

                                    // 方法B: 如果你非要执行具体命令字符串 (例如有其他插件依赖命令触发)
                                    // server.getCommandManager().executeWithPrefix(server.getCommandSource(), "/whitelist reload");

                                } catch (Exception e) {
                                    LOGGER.error("Failed to reload whitelist", e);
                                }
                            });
                        }
                    }

                    // 重置 key，如果返回 false 说明目录已不可访问（循环结束）
                    boolean valid = key.reset();
                    if (!valid) {
                        break;
                    }
                }
            } catch (IOException e) {
                LOGGER.error("Error setting up file watcher", e);
            }
        }, "Whitelist-Watcher-Thread");

        // 设置为守护线程，确保JVM关闭时它也会关闭
        watcherThread.setDaemon(true);
        watcherThread.start();
    }

    private void stopWatcher(MinecraftServer server) {
        isRunning.set(false);
        if (watcherThread != null) {
            watcherThread.interrupt(); // 中断阻塞的 take()
            LOGGER.info("Whitelist Watcher stopped.");
        }
    }
}