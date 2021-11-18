package com.iduki.blockhideandseekmod.game;

import com.iduki.blockhideandseekmod.BlockHideAndSeekMod;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.entity.boss.BossBar;
import net.minecraft.entity.boss.CommandBossBar;
import net.minecraft.server.PlayerManager;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.LiteralText;
import net.minecraft.util.Identifier;

import java.util.List;


/**
 * コマンドで/bhas startを実行した場合にHidersの準備時間(隠れる時間)からスタートしてSeekersの索敵時間に移行する。
 * 時間内までにHidersが全滅した場合はSeekersの勝利、制限時間までHidersが残っていた場合は残っているHidersの勝ち
 */

public class PreparationTimer {
//準備時間

    static int PreparationTime = 1200;
    static PlayerManager playerManager = BlockHideAndSeekMod.SERVER.getPlayerManager();
    static List<ServerPlayerEntity> players = playerManager.getPlayerList();

    public static void Preparationtime() {
        //startコマンドが呼び出されたタイミングで準備時間を初期化する
        PreparationTime = 1200;

    }


    public static void GameStarttime() {

    }


    static {
        var bossBar = new CommandBossBar(new Identifier(BlockHideAndSeekMod.MOD_ID), new LiteralText("準備時間"));
        bossBar.setMaxValue(PreparationTime);
        bossBar.setStyle(BossBar.Style.NOTCHED_20);

        ServerTickEvents.START_SERVER_TICK.register(server -> {

            bossBar.addPlayers(players);

            if (PreparationTime > 0) {
                bossBar.setVisible(true);
                PreparationTime = --PreparationTime;
                bossBar.setValue(PreparationTime);
            } else {
                bossBar.setVisible(false);
                GameStarttime();
            }
        });
    }

}