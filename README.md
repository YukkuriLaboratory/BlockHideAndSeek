# BlockHideandSeekMod (日本語)/([English](README.en.md))

BlockHideAndSeekModはブロックに擬態して隠れるミミック側(Hider)とそれを探し出して倒す鬼側(Seeker)陣営に分かれて遊ぶゲームです。

## Requirements

このMODはサーバーサイドのみで、クライアントにインストールしても効果はありません。

**BLockHideandSeekModには、[Fabric Loader](https://www.curseforge.com/linkout?remoteUrl=https%3a%2f%2ffabricmc.net%2fuse%2f)、[Fabric
API](https://www.curseforge.com/minecraft/mc-mods/fabric-api)、[Cloth Config API (
Fabric)](https://www.curseforge.com/minecraft/mc-mods/cloth-config)、[Fabric Language
Kotlin](https://www.curseforge.com/minecraft/mc-mods/fabric-language-kotlin) が必要です。**

## Command

`/bhas start` ゲームをスタートします。※

`/bhas stop` ゲームを中断します。※

`/bhas settings <parameters>`  各種パラメータを設定します。※

`/bhas reload` Configを再ロードします。※

`/bhas rules <target>`  ルールブックを入手します、引数が指定されている場合はルールブックをターゲットに配布します。※

`/bhas team <seeker|hider>`  引数で指定されたチームに参加します。このコマンドは投票期間中のみ有効なので、投票期間中にログインしたプレイヤーが使用してください。

※コマンドの実行にはOP権限が必要ですが、Rulesコマンドは引数無しなら誰でも実行できます。

## Game progress

<u>投票時間</u> チーム分けが始まりますので、チャットに表示されているチームをクリックして投票してください。時間終了まで投票していないプレイヤーは強制的に観戦モードになります。

<u>準備時間</u> 準備時間中は、ミミックが隠れる時間です。 この間鬼は視界が奪われ、動けなくなります。

<u>本戦時間</u> ゲーム時間内に鬼チームがミミックチームを全て倒せば鬼チームの勝利、ゲーム中に死亡したミミックは観戦モードになります。

## License

このMODは[AGPL-3.0 License](https://github.com/YukkuriLaboratory/BlockHideAndSeek/blob/1.17/LICENSE)のもとで公開されています