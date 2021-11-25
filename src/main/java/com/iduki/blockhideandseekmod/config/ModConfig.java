package com.iduki.blockhideandseekmod.config;

import com.iduki.blockhideandseekmod.BlockHideAndSeekMod;
import me.lortseam.completeconfig.api.ConfigContainer;
import me.lortseam.completeconfig.api.ConfigEntries;
import me.lortseam.completeconfig.api.ConfigEntry;
import me.lortseam.completeconfig.api.ConfigGroup;
import me.lortseam.completeconfig.data.Config;

public class ModConfig extends Config implements ConfigContainer {

    public ModConfig() {
        super(BlockHideAndSeekMod.MOD_ID);
    }

    @Transitive
    @ConfigEntries
    public static class SystemConfig implements ConfigGroup {
        @ConfigEntry(comment = "鬼の最大人数")
        public static int seekerLimit = 1;

        @Transitive
        @ConfigEntries
        public static class Times implements ConfigGroup {
            @ConfigEntry(comment = "陣営の投票時間(単位:秒)")
            public static int voteTime = 30;
            /**
             * 準備時間
             * 単位: 秒
             */
            @ConfigEntry(comment = "準備時間(単位:秒)")
            public static int prepareTime = 10;

            /**
             * 制限時間
             * 単位: 秒
             */
            public static int playTime = 10;

            /**
             * 擬態にかかる時間
             * 単位: 秒
             */
            public static int hideWaitTime;
        }
    }

    @Transitive
    @ConfigEntries
    public static class ItemConfig implements ConfigGroup {
        /**
         * 鬼が持つ探知用アイテム
         */
        @Transitive
        @ConfigEntries
        public static class ItemDetecter implements ConfigGroup {
            @ConfigEntry(comment = "使用クールタイム")
            public static int coolTime = 200;
            @ConfigEntry(comment = "ダメージ量")
            public static int damageAmount = 50;
        }

        /**
         * 鬼が持つスキャン用アイテム
         */
        @Transitive
        @ConfigEntries
        public static class ItemScanner implements ConfigGroup {

            @ConfigEntry(comment = "使用クールタイム")
            public static int coolTime = 300;

            @ConfigEntry(comment = "捜索半径")
            public static double scanLength = 15;
        }
    }

}
