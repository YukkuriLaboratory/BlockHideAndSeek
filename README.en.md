
<img align="left" src="src\main\resources\assets\blockhideandseekmod\icon.png" height="200">

<a href="https://www.youtube.com/channel/UCe0_ecxZmAQPRxRa0gM3KvQ"><img alt="YouTube Channel Views" src="https://img.shields.io/youtube/channel/views/UCe0_ecxZmAQPRxRa0gM3KvQ?style=social"></a>&nbsp;

<a href="https://www.curseforge.com/minecraft/mc-mods/blockhideandseekmod"><img src="http://cf.way2muchnoise.eu/blockhideandseekmod.svg"></a>&nbsp;

<a href="https://modrinth.com/mod/blockhideandseekmod"><img src="https://img.shields.io/badge/dynamic/json?color=5da545&label=modrinth&prefix=downloads%20&query=downloads&url=https://api.modrinth.com/api/v1/mod/blockhideandseekmod&style=flat&logo=data:image/svg+xml;base64,PHN2ZyB4bWxucz0iaHR0cDovL3d3dy53My5vcmcvMjAwMC9zdmciIHZpZXdCb3g9IjAgMCAxMSAxMSIgd2lkdGg9IjE0LjY2NyIgaGVpZ2h0PSIxNC42NjciICB4bWxuczp2PSJodHRwczovL3ZlY3RhLmlvL25hbm8iPjxkZWZzPjxjbGlwUGF0aCBpZD0iQSI+PHBhdGggZD0iTTAgMGgxMXYxMUgweiIvPjwvY2xpcFBhdGg+PC9kZWZzPjxnIGNsaXAtcGF0aD0idXJsKCNBKSI+PHBhdGggZD0iTTEuMzA5IDcuODU3YTQuNjQgNC42NCAwIDAgMS0uNDYxLTEuMDYzSDBDLjU5MSA5LjIwNiAyLjc5NiAxMSA1LjQyMiAxMWMxLjk4MSAwIDMuNzIyLTEuMDIgNC43MTEtMi41NTZoMGwtLjc1LS4zNDVjLS44NTQgMS4yNjEtMi4zMSAyLjA5Mi0zLjk2MSAyLjA5MmE0Ljc4IDQuNzggMCAwIDEtMy4wMDUtMS4wNTVsMS44MDktMS40NzQuOTg0Ljg0NyAxLjkwNS0xLjAwM0w4LjE3NCA1LjgybC0uMzg0LS43ODYtMS4xMTYuNjM1LS41MTYuNjk0LS42MjYuMjM2LS44NzMtLjM4N2gwbC0uMjEzLS45MS4zNTUtLjU2Ljc4Ny0uMzcuODQ1LS45NTktLjcwMi0uNTEtMS44NzQuNzEzLTEuMzYyIDEuNjUxLjY0NSAxLjA5OC0xLjgzMSAxLjQ5MnptOS42MTQtMS40NEE1LjQ0IDUuNDQgMCAwIDAgMTEgNS41QzExIDIuNDY0IDguNTAxIDAgNS40MjIgMCAyLjc5NiAwIC41OTEgMS43OTQgMCA0LjIwNmguODQ4QzEuNDE5IDIuMjQ1IDMuMjUyLjgwOSA1LjQyMi44MDljMi42MjYgMCA0Ljc1OCAyLjEwMiA0Ljc1OCA0LjY5MSAwIC4xOS0uMDEyLjM3Ni0uMDM0LjU2bC43NzcuMzU3aDB6IiBmaWxsLXJ1bGU9ImV2ZW5vZGQiIGZpbGw9IiM1ZGE0MjYiLz48L2c+PC9zdmc+"></a>&nbsp;

<img alt="Github release download total" src="https://img.shields.io/github/downloads/YukkuriLaboratory/BlockHideAndSeek/total">&nbsp;

<img alt="GitHub branch checks state" src="https://github.com/YukkuriLaboratory/BlockHideAndSeek/actions/workflows/build.yml/badge.svg">&nbsp;


# BlockHideAndSeekMod (English)/([Japanese](README.md))

BlockHideAndSeekMod is a competitive game played between Hiders who hide by morphing into blocks and Seekers who find and defeat the Hiders.

<span style="color: red; ">**!! This mod is only available in Japanese. !!**</span>

## Requirements

This mod is server-side only; you do not need to install this on client, and doing so will not have any effect.

**BlockHideandSeekMod
requires [Fabric Loader](https://www.curseforge.com/linkout?remoteUrl=https%3a%2f%2ffabricmc.net%2fuse%2f),
[Fabric API](https://www.curseforge.com/minecraft/mc-mods/fabric-api),
and [Fabric Language Kotlin](https://www.curseforge.com/minecraft/mc-mods/fabric-language-kotlin).**

## Command

`/bhas start` Starts the game.*

`/bhas stop` Suspends the game.*

`/bhas settings <parameters>`  Changes various parameters of the game.*

`/bhas reload` Reload config changes.*

`/bhas rules <target>`  Displays the game rules, and distributes the rulebook to the target if the argument is
specified.*

`/bhas team <seeker|hider>`  Joins the team specified in the argument.**

`/bhas restore (<force>)` Restore players inventory and gamemode from last played, and this process runs forcibly
if `<force>` is true**

*Operator privileges are required to execute the command, but the rules command could be ran by any players if without
argument.

** The command is only valid during voting period. The code is intended so that the players
who logged in during the voting period could join the team without the comment display.

## Game progress

<u>Voting Period:</u> The teaming will begin right after the game starts. The teams will be displayed on the chat, and
players can vote by clicking on the displayed team. Players who didn't vote will be forced to enter spectator mode.

<u>Preparation Period:</u> During the preparation period, the Seekers will be blinded and locked into place
so the Hiders will have a chance to hide.

<u>Main Game Period:</u> If the Seekers defeat all the Hiders within the time limit, the Seeker team wins. 
If any of the Hiders survive until time runs out, the Hider team wins. 
The defeated Hiders will enter spectator mode for the rest of the game.

## License

This MOD is released under the [LGPL-3.0 License](https://github.com/YukkuriLaboratory/BlockHideAndSeek/blob/1.18/LICENSE).
