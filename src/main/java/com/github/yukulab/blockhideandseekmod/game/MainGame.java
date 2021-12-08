package com.github.yukulab.blockhideandseekmod.game;

import com.github.yukulab.blockhideandseekmod.BlockHideAndSeekMod;
import com.github.yukulab.blockhideandseekmod.config.ModConfig;
import com.github.yukulab.blockhideandseekmod.item.BhasItems;
import com.github.yukulab.blockhideandseekmod.util.HideController;
import com.github.yukulab.blockhideandseekmod.util.PlayerUtil;
import com.github.yukulab.blockhideandseekmod.util.TeamCreateAndDelete;
import com.github.yukulab.blockhideandseekmod.util.TeamPlayerListHeader;
import net.minecraft.entity.boss.BossBar;
import net.minecraft.entity.boss.ServerBossBar;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.network.packet.s2c.play.SubtitleS2CPacket;
import net.minecraft.network.packet.s2c.play.TitleS2CPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.GameMode;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.Duration;
import java.time.Instant;
import java.util.Objects;


/**
 * ゲーム時間の計測
 * ↓
 * ゲーム開始
 * ↓
 * Hidersが全滅した場合はSeekersの勝利
 * 時間までHidersが残っていた場合は残っているHidersの勝ち
 */
public class MainGame implements GameStatus {
    /**
     * 時間計測用
     * 準備時間
     */
    private static Instant ingameTime;


    //毎回クラス名入力するのがダルいので定数として扱う
    private static final MinecraftServer server = BlockHideAndSeekMod.SERVER;

    public MainGame() {
        ingameTime = Instant.now();
    }

    @NotNull
    @Override
    public ServerBossBar getProgressBar() {
        return new ServerBossBar(Text.of(""), BossBar.Color.BLUE, BossBar.Style.NOTCHED_20);
    }

    @Override
    public boolean onUpdate(@Nullable ServerBossBar progressBar) {
        var playerManager = server.getPlayerManager();

        TeamPlayerListHeader.TeamList();
        PlayerUtil.setMaxStamina();

        //制限時間(毎回入力するのがダルいので定数化．クラス内定数にしないのは途中でConfig変えられたりする可能性を考えているため)
        var gameTime = ModConfig.SystemConfig.Times.playTime;

        //現在の時間
        var now = Instant.now();

        //経過時間
        var currentTime = Duration.between(ingameTime, now);

        //残り時間
        var remainsTime = Duration.ofSeconds(gameTime).minus(currentTime);

        //ミミック陣営が0かどうかの確認
        var hiderTeam = TeamCreateAndDelete.getHiders();
        var mimicEmpty = hiderTeam == null || hiderTeam.getPlayerList().isEmpty();
        //ミミック陣営の人数が0のとき
        if (mimicEmpty) {
            var winMessage = new LiteralText("鬼陣営の勝利！").append(Text.of("\n"));
            playerManager.getPlayerList().forEach(player -> player.sendMessage(winMessage, false));

            //タイトルバーにGAMEOVERと表示
            var endMessage = new TitleS2CPacket(new LiteralText("     ").setStyle(Style.EMPTY.withFormatting(Formatting.UNDERLINE))
                    .append(new LiteralText("GAMEOVER").setStyle(Style.EMPTY.withFormatting(Formatting.UNDERLINE)))
                    .append(new LiteralText("     ").setStyle(Style.EMPTY.withFormatting(Formatting.UNDERLINE))));
            var endsubMessage = new SubtitleS2CPacket(new LiteralText("鬼陣営の勝利").setStyle(Style.EMPTY.withColor(Formatting.RED)));
            playerManager.getPlayerList().forEach(player -> player.networkHandler.sendPacket(endMessage));
            playerManager.getPlayerList().forEach(player -> player.networkHandler.sendPacket(endsubMessage));

            playerManager.getPlayerList().forEach(player -> player.playSound(SoundEvents.ENTITY_ENDER_DRAGON_GROWL, SoundCategory.PLAYERS, 1.0f, 1.0f));
            return true;
        }

        //残り時間が０以下のとき
        if (remainsTime.isNegative()) {
            var winMessage = new LiteralText("ミミック陣営の勝利！").append(Text.of("\n"));
            var winPlayers = hiderTeam.getPlayerList();
            var message = new LiteralText("生き残ったミミック").append(Text.of("\n")).append(new LiteralText(winPlayers.toString()).setStyle(Style.EMPTY.withColor(Formatting.GREEN)));
            playerManager.getPlayerList().forEach(player -> player.sendMessage(winMessage, false));
            playerManager.getPlayerList().forEach(player -> player.sendMessage(message, false));

            //タイトルバーにGAMEOVERと表示
            var endMessage = new TitleS2CPacket(new LiteralText("     ").setStyle(Style.EMPTY.withFormatting(Formatting.UNDERLINE))
                    .append(new LiteralText("GAMEOVER").setStyle(Style.EMPTY.withFormatting(Formatting.UNDERLINE)))
                    .append(new LiteralText("     ").setStyle(Style.EMPTY.withFormatting(Formatting.UNDERLINE))));
            var endsubMessage = new SubtitleS2CPacket(new LiteralText("ミミック陣営の勝利").setStyle(Style.EMPTY.withColor(Formatting.GREEN)));
            playerManager.getPlayerList().forEach(player -> player.networkHandler.sendPacket(endMessage));
            playerManager.getPlayerList().forEach(player -> player.networkHandler.sendPacket(endsubMessage));

            playerManager.getPlayerList().forEach(player -> player.playSound(SoundEvents.ENTITY_ENDER_DRAGON_GROWL, SoundCategory.PLAYERS, 1.0f, 1.0f));

            //残ったミミックを光らせたりする
            hiderTeam.getPlayerList()
                    .stream()
                    .map(playerManager::getPlayer)
                    .filter(Objects::nonNull)
                    .forEach(player -> player.addStatusEffect(new StatusEffectInstance(StatusEffects.GLOWING, 200, 200, false, false, false)));
            return true;
        }

        if (progressBar != null) {
            //ボスバーのテキストを更新
            var timeText = Text.of("残り時間: " + remainsTime.toSeconds() + "秒");
            progressBar.setName(timeText);
            //ゲージ更新
            progressBar.setPercent(MathHelper.clamp(remainsTime.toSeconds() / ((float) gameTime), 0, 1));
        }

        var isDisplayTime = Math.floor(currentTime.toMillis() / 100f / 5) % 2 == 0;

        if (currentTime.toSeconds() >= gameTime - 4 && isDisplayTime) {
            playerManager.getPlayerList().forEach(player -> player.playSound(SoundEvents.BLOCK_NOTE_BLOCK_HAT, SoundCategory.PLAYERS, 1.0f, 1.0f));
        }
        return false;
    }

    @Override
    public void onFinally() {
        TeamCreateAndDelete.deleteTeam();
        HideController.clearSelectors();
        server.getPlayerManager()
                .getPlayerList()
                .forEach(player -> {
                    player.changeGameMode(GameMode.SPECTATOR);
                    // 擬態解除(事故ることはないのでここで呼んじゃう)
                    HideController.cancelHiding(player);
                    // Modアイテムの削除
                    player.getInventory()
                            .remove(
                                    itemStack -> BhasItems.isModItem(itemStack.getItem()),
                                    64,
                                    player.playerScreenHandler.getCraftingInput()
                            );
                });
        TeamCreateAndDelete.deleteTeam();
        TeamPlayerListHeader.EmptyList();
    }

    @Override
    public void onSuspend() {

    }

    @Nullable
    @Override
    public GameStatus next() {
        return null;
    }
}
