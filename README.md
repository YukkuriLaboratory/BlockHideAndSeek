
<img align="left" src="src\main\resources\assets\blockhideandseekmod\icon.png" height="200">

<a href="https://www.youtube.com/channel/UCe0_ecxZmAQPRxRa0gM3KvQ"><img alt="YouTube Channel Views" src="https://img.shields.io/youtube/channel/views/UCe0_ecxZmAQPRxRa0gM3KvQ?style=social"></a>&nbsp;

<a href="https://www.curseforge.com/minecraft/mc-mods/blockhideandseekmod"><img src="http://cf.way2muchnoise.eu/blockhideandseekmod.svg"></a>&nbsp;

<a href="https://modrinth.com/mod/blockhideandseekmod"><img src="https://img.shields.io/badge/dynamic/json?color=5da545&label=modrinth&prefix=downloads%20&query=downloads&url=https://api.modrinth.com/api/v1/mod/blockhideandseekmod&style=flat&logo=data:image/svg+xml;base64,PHN2ZyB4bWxucz0iaHR0cDovL3d3dy53My5vcmcvMjAwMC9zdmciIHZpZXdCb3g9IjAgMCAxMSAxMSIgd2lkdGg9IjE0LjY2NyIgaGVpZ2h0PSIxNC42NjciICB4bWxuczp2PSJodHRwczovL3ZlY3RhLmlvL25hbm8iPjxkZWZzPjxjbGlwUGF0aCBpZD0iQSI+PHBhdGggZD0iTTAgMGgxMXYxMUgweiIvPjwvY2xpcFBhdGg+PC9kZWZzPjxnIGNsaXAtcGF0aD0idXJsKCNBKSI+PHBhdGggZD0iTTEuMzA5IDcuODU3YTQuNjQgNC42NCAwIDAgMS0uNDYxLTEuMDYzSDBDLjU5MSA5LjIwNiAyLjc5NiAxMSA1LjQyMiAxMWMxLjk4MSAwIDMuNzIyLTEuMDIgNC43MTEtMi41NTZoMGwtLjc1LS4zNDVjLS44NTQgMS4yNjEtMi4zMSAyLjA5Mi0zLjk2MSAyLjA5MmE0Ljc4IDQuNzggMCAwIDEtMy4wMDUtMS4wNTVsMS44MDktMS40NzQuOTg0Ljg0NyAxLjkwNS0xLjAwM0w4LjE3NCA1LjgybC0uMzg0LS43ODYtMS4xMTYuNjM1LS41MTYuNjk0LS42MjYuMjM2LS44NzMtLjM4N2gwbC0uMjEzLS45MS4zNTUtLjU2Ljc4Ny0uMzcuODQ1LS45NTktLjcwMi0uNTEtMS44NzQuNzEzLTEuMzYyIDEuNjUxLjY0NSAxLjA5OC0xLjgzMSAxLjQ5MnptOS42MTQtMS40NEE1LjQ0IDUuNDQgMCAwIDAgMTEgNS41QzExIDIuNDY0IDguNTAxIDAgNS40MjIgMCAyLjc5NiAwIC41OTEgMS43OTQgMCA0LjIwNmguODQ4QzEuNDE5IDIuMjQ1IDMuMjUyLjgwOSA1LjQyMi44MDljMi42MjYgMCA0Ljc1OCAyLjEwMiA0Ljc1OCA0LjY5MSAwIC4xOS0uMDEyLjM3Ni0uMDM0LjU2bC43NzcuMzU3aDB6IiBmaWxsLXJ1bGU9ImV2ZW5vZGQiIGZpbGw9IiM1ZGE0MjYiLz48L2c+PC9zdmc+"></a>&nbsp;

<img alt="Github release download" src="https://img.shields.io/github/downloads/YukkuriLoboratory/BlockHIdeAndSeek/total.svg">&nbsp;

<img alt="GitHub branch checks state" src="https://img.shields.io/github/checks-status/YukkuriLaboratory/BlockHideAndSeek/1.18">&nbsp;


# BlockHideandSeekMod (日本語)/([English](README.en.md))

BlockHideAndSeekModはブロックに擬態して隠れるミミック側(Hider)とそれを探し出して倒す鬼側(Seeker)陣営に分かれて遊ぶゲームです。

## Requirements

このMODはサーバーサイド専用modです。クライアントでは動作しません。

**
BlockHideandSeekModには、[Fabric Loader](https://www.curseforge.com/linkout?remoteUrl=https%3a%2f%2ffabricmc.net%2fuse%2f)、[Fabric
API](https://www.curseforge.com/minecraft/mc-mods/fabric-api)、[Fabric Language
Kotlin](https://www.curseforge.com/minecraft/mc-mods/fabric-language-kotlin) が必要です。**

## Command

`/bhas start` ゲームをスタートします。※

`/bhas stop` ゲームを中断します。※

`/bhas settings <parameters>`  各種設定を変更します。※

`/bhas reload` Configをリロードします。※

`/bhas rules <target>`  ルールブックを入手します、対象が指定されている場合はルールブックをターゲットに配布します(Rulesコマンドは引数無しなら誰でも実行できます)※

`/bhas team <seeker|hider>`  引数で指定されたチームに参加します(投票時間中のみ有効)

`/bhas restore (<force>)` 最後にゲームを開始した時のインベントリとゲームモードを復元します。(`<force>`がtrueの際は強制的に実行します)※

※OP権限が必要

## Game progress

### [投票時間]

陣営の選択ができます。チャットに表示されているチームをクリックして投票してください。投票していないプレイヤーは強制的に観戦モードになります。

### [準備時間]

ミミックが隠れる時間です。 鬼は視界が奪われ、動けなくなります。

### [本戦時間] 
ゲーム時間内に鬼がミミックを全て倒せば鬼の勝利となり、ミミックはゲーム終了まで生き残れば勝利となります。ゲーム中に死亡したミミックは観戦モードになります。

## License

このMODは[AGPL-3.0 License](https://github.com/YukkuriLaboratory/BlockHideAndSeek/blob/1.17/LICENSE)のもとで公開されています
