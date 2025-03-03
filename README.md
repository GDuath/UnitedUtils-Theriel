# Introduction
This repository is for the plugin UnitedUtils. It contains several miscellaneous addons for the Minecraft Server United Lands that are too minor to warrant an entire plugin.

# Installation
Unfortunately, this plugin is built around and specifically for United Lands and is not designed for use on other server. The code is mainly available for accessibility and learning for those who are curious, but it is possible to compile and install. 

Most features are fully configurable and can be toggled on or off depending on your use case. 

## Dependencies

This plugin requires a few dependencies to function: 
* [Towny](https://github.com/TownyAdvanced/Towny) 
* [MapTowny](https://github.com/TownyAdvanced/MapTowny)
* [CMI Vault](https://www.zrips.net/faq/) (Found under 'Extra Resources')

# Permissions
This plugin only has two permission nodes, `united.utils.player` and `united.utils.admin`, the .player node will currently only allow them to use the info commands (outlined below). .admin permission node gives Staff permission to reload the plugin config file. 

# Modules

This plugin is split into several 'modules' as there is not one clear function it provides. Each module is configurable and (usually) toggleable. 

## Border Wrapper
This module handles map wrapping, this means teleporting players from one edge of the map to another to create the experience of travelling around a globe. 

It currently replicates East-West, North-North, and South-South travel that accurately mimics that of a globe. The borders are currently hard-coded but should work for any 1:500 scale map of Earth. 

## Explosion Manager

This module handles explosion PvP nerfs such as crystal, anchor, and beds. Each one can be enabled and disabled, so you can only nerf the relevant ones to you, and the damage reduction is configurable to your liking. So, it is possible to completely disable certain damage sources, or just reduce it by a set amount.

## Portal Manager

The portal manager module was made to disable Nether portals on the Earth map and instead linking them to temporary 'resource' worlds that reset periodically. This was to create a vanilla experience when interacting with the resource worlds as the overworld, Nether, and End all link as expected in the base game, while also leaving the spawn and Earth worlds untouched.

In the module is also a configurable 'spawn portal' zone that when defined can force a player to run a command when they enter. This is useful for allowing new players to run directly into them and send them straight into gameplay. 

## Random Teleport

The RTP module functions much like many standard plugins of similar function, except for one additional feature: Regional teleports. Here you can define zones in one world to have teleports to, allowing for much finer control over where a player chooses to go. This works best for maps with well-defined areas - such as Earth's continents.

## Void Protection

This module stops players from falling into the void in certain worlds (useful if you have a spawn world) and sends them back to the spawn world.

## Wiki Map Links

This module parses some placeholders set in MapTowny's config to dynamically adjust town tooltip info boxes and allows click through links to an external wiki page of the same name. An example can be found [here](https://map.unitedlands.net).

## Info Commands

Included in the plugin are some built-in, common commands that can be configured to display information relevant to your server. Currently available commands are:
- Map
- Discord
- Wiki
- Shop
- Greylist (Links to some forum posts)
- Other miscellaneous commands that aren't relevant to other servers. 

# Suggestions and Improvements
This serves as my first real plugin with actual use cases and essentially a compilation of little exercises I'm doing to practice. Feedback is more than welcome if you have suggestions on how to further improve the plugin, either optimisation-wise or simply making it more accessible for other servers. Feel free to either open an issue here or contact me via Discord (Litning11).
