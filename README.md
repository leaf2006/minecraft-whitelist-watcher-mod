# Minecraft Whitelist Watcher
**A mod for server that can monitor changes to the Minecraft server whitelist and immediately reload the whitelist when it changes.Based on Fabric**

>[!NOTE]
> > This repo is still under development; the current version is unstable but usable.

## What it does

For me, this mod is meant to work with another plugin I developed,<a href="https://github.com/leaf2006/nonebot-plugin-mc-whitelist-controller">nonebot-plugin-mc-whitelist-controller</a>. This plugin allows users to register whitelists on social platforms like QQ by sending messages, and then this mod enables real-time updates for the modified whitelists.

Of course, setting up a scheduled task on the server to automatically execute the */whitelist reload* command can achieve a similar effect, but if there is a mod that allows the server to monitor changes to the whitelist and automatically reload, it would be smarter and more efficient for the server. This mod is designed to achieve exactly that.

## Platform

- Minecraft versions: 1.21.7 only
- Fabric Loader: >= 0.17.2
- Fabric API: 0.129.0+1.21.7

**I understand that this version has very strict requirements, but I have not yet completed all the work. This is merely a test version. I will compile more versions as soon as possible, as it is not a difficult task since the code remains the same.**

<div align="center">

Copyright © Leaf developer 2023-2026.
This product is under the MIT open source agreement

</div>