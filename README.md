# Introduction
This repository is for the plugin UnitedUtils. It contains several miscellaneous addons for the Minecraft Server United Lands that are too minor to warrant an entire plugin.

# Installation
Unfortunately, this plugin is built around and specifically for United Lands and is not designed for use on other server. The code is mainly available for accessibility and learning for those who are curious, but it is possible to compile and install. 

There are no hard dependencies required to run this plugin. PlaceholderAPI is optional to parse certain placeholders. 

Most features are fully configurable and can be toggled on or off depending on your use case. 

# Permissions
This plugin only has two permission nodes, `united.utils.player` and `united.utils.admin`, the .player node will currently only allow them to use the info commands (outlined below). .admin permission node gives Staff permission to reload the plugin config file. 

# Modules

This plugin is split into several 'modules' as there is not one clear function it provides. Each module is configurable and (usually) toggleable. 

## Explosion Manager

This module handles explosion PvP nerfs such as crystal, anchor, and beds. Each one can be enabled and disabled, so you can only nerf the relevant ones to you, and the damage reduction is configurable to your liking. So, it is possible to completely disable certain damage sources, or just reduce it by a set amount.

## Void Protection

This module stops players from falling into the void in certain worlds (useful if you have a spawn world) and sends them back to the spawn world.

## Info Commands

Included in the plugin are some built-in, common commands that can be configured to display information relevant to your server. Currently available commands are:
- Map
- Discord
- Wiki
- Shop
- Greylist (Links to some forum posts)
- Toptime (Top playtime leaderboard, currently uses CMI placeholders but these can be swapped to whatever you use in the config)
- Other miscellaneous commands that aren't relevant to other servers. 

# Suggestions and Improvements
This serves as my first real plugin with actual use cases and essentially a compilation of little exercises I'm doing to practice. Feedback is more than welcome if you have suggestions on how to further improve the plugin, either optimisation-wise or simply making it more accessible for other servers. Feel free to either open an issue here or contact me via Discord (Litning11).
