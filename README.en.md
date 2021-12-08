
<img align="left" src="src\main\resources\assets\blockhideandseekmod\icon.png" height="200">

<img alt="YouTube Channel Views" src="https://img.shields.io/youtube/channel/views/UCe0_ecxZmAQPRxRa0gM3KvQ?style=social">

<img src="http://cf.way2muchnoise.eu/blockhideandseekmod.svg">&nbsp;

<img src="https://img.shields.io/badge/dynamic/json?color=5da545&label=modrinth&prefix=downloads%20&query=downloads&url=https://api.modrinth.com/api/v1/mod/blockhideandseekmod&style=flat-square&logo=data:image/svg+xml;base64,PHN2ZyB4bWxucz0iaHR0cDovL3d3dy53My5vcmcvMjAwMC9zdmciIHZpZXdCb3g9IjAgMCAxMSAxMSIgd2lkdGg9IjE0LjY2NyIgaGVpZ2h0PSIxNC42NjciICB4bWxuczp2PSJodHRwczovL3ZlY3RhLmlvL25hbm8iPjxkZWZzPjxjbGlwUGF0aCBpZD0iQSI+PHBhdGggZD0iTTAgMGgxMXYxMUgweiIvPjwvY2xpcFBhdGg+PC9kZWZzPjxnIGNsaXAtcGF0aD0idXJsKCNBKSI+PHBhdGggZD0iTTEuMzA5IDcuODU3YTQuNjQgNC42NCAwIDAgMS0uNDYxLTEuMDYzSDBDLjU5MSA5LjIwNiAyLjc5NiAxMSA1LjQyMiAxMWMxLjk4MSAwIDMuNzIyLTEuMDIgNC43MTEtMi41NTZoMGwtLjc1LS4zNDVjLS44NTQgMS4yNjEtMi4zMSAyLjA5Mi0zLjk2MSAyLjA5MmE0Ljc4IDQuNzggMCAwIDEtMy4wMDUtMS4wNTVsMS44MDktMS40NzQuOTg0Ljg0NyAxLjkwNS0xLjAwM0w4LjE3NCA1LjgybC0uMzg0LS43ODYtMS4xMTYuNjM1LS41MTYuNjk0LS42MjYuMjM2LS44NzMtLjM4N2gwbC0uMjEzLS45MS4zNTUtLjU2Ljc4Ny0uMzcuODQ1LS45NTktLjcwMi0uNTEtMS44NzQuNzEzLTEuMzYyIDEuNjUxLjY0NSAxLjA5OC0xLjgzMSAxLjQ5MnptOS42MTQtMS40NEE1LjQ0IDUuNDQgMCAwIDAgMTEgNS41QzExIDIuNDY0IDguNTAxIDAgNS40MjIgMCAyLjc5NiAwIC41OTEgMS43OTQgMCA0LjIwNmguODQ4QzEuNDE5IDIuMjQ1IDMuMjUyLjgwOSA1LjQyMi44MDljMi42MjYgMCA0Ljc1OCAyLjEwMiA0Ljc1OCA0LjY5MSAwIC4xOS0uMDEyLjM3Ni0uMDM0LjU2bC43NzcuMzU3aDB6IiBmaWxsLXJ1bGU9ImV2ZW5vZGQiIGZpbGw9IiM1ZGE0MjYiLz48L2c+PC9zdmc+">&nbsp;

<img alt="GitHub branch checks state" src="https://img.shields.io/github/checks-status/YukkuriLaboratory/BlockHideAndSeek/1.17">&nbsp;

<img src="https://img.shields.io/badge/-Intellijidea-000000.svg?logo=intellijidea&style=plastic">&nbsp;

# BlockHideandSeekMod (English)/([Japanese](README.md))

The BlockHideAndSeekMod is a game that divides players into two camps: Hiders, who mimic blocks and hide behind them,
and Seekers, who seek them out and defeat them.

<span style="color: red; ">**!! This mod is only available in Japanese. !!**</span>

## Requirements

This mod is server-side only,You can have it installed on the client, though it will not have any effect.

**BLockHideandSeekMod
requires [Fabric Loader ](https://www.curseforge.com/linkout?remoteUrl=https%3a%2f%2ffabricmc.net%2fuse%2f),[Fabric
API](https://www.curseforge.com/minecraft/mc-mods/fabric-api),[Cloth Config API (
Fabric)](https://www.curseforge.com/minecraft/mc-mods/cloth-config)
and [Fabric Language Kotlin](https://www.curseforge.com/minecraft/mc-mods/fabric-language-kotlin).**

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

## License

This MOD is released under the [AGPL-3.0 License](https://github.com/YukkuriLaboratory/BlockHideAndSeek/blob/1.17/LICENSE).