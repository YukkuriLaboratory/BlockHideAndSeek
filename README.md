# BlockHideandSeekMod

The BlockHideAndSeekMod is a game that divides players into two camps: Hiders, who mimic blocks and hide behind them,
and Seekers, who seek them out and defeat them.

## Requirements

This mod is server-side only,You can have it installed on the client, though it will not have any effect.

**BLockHideandSeekMod
requires [Fabric Loader ](https://www.curseforge.com/linkout?remoteUrl=https%3a%2f%2ffabricmc.net%2fuse%2f),[Fabric
API](https://www.curseforge.com/minecraft/mc-mods/fabric-api),[Cloth Config API (
Fabric)](https://www.curseforge.com/minecraft/mc-mods/cloth-config)
and [Fabric Language Kotlin](https://www.curseforge.com/minecraft/mc-mods/fabric-language-kotlin)**.

## Installation

1.17.1:Minecraft Fabric Loader, Fabric API,Cloth Config API (Fabric) and Fabric Language Kotlin.

## Command

`/bhas start` Start the game.*

`/bhas stop` Suspend the game.*

`/bhas settings <parameters>`  Change various parameters of the game.*

`/bhas reload` Reload Config changes.*

`/bhas rules <target>`  Displays the game rules, and distributes the rulebook to the target if the argument is
specified.*

`/bhas team <seeker|hider>`  Join the team specified in the argument, it is only valid during voting time, so players
who log in during voting time should use it!

*Operator privileges are required to execute the command, but the rules command can be executed by anyone with no
arguments.

## Game progress

<u>Votting time</u> The teaming will begin, please click on the team displayed in the chat to vote. Players who have not
yet voted will be forced to go into spectator mode.

<u>Preparation time</u> During the preparation time is the time for the Hider team to hide, the demon is deprived of
vision and cannot move.

<u>Main game time</u> If the Seeker team defeats all the Hider teams within the game time, the Seeker team wins, and if
the Hider team escapes until 0 seconds left, the Hider team wins.Hider who die during the game will go into spectator
mode.

