package com.iduki.blockhideandseekmod.config;

import com.iduki.blockhideandseekmod.BlockHideAndSeekMod;
import me.lortseam.completeconfig.api.ConfigContainer;
import me.lortseam.completeconfig.api.ConfigEntries;
import me.lortseam.completeconfig.api.ConfigGroup;
import me.lortseam.completeconfig.data.Config;

public class ModConfig extends Config implements ConfigContainer {

    public ModConfig() {
        super(BlockHideAndSeekMod.MOD_ID);
    }

    @Transitive
    @ConfigEntries
    public static class SystemConfig implements ConfigGroup {
        /**
         * 準備時間
         * 単位: 秒
         */
        public static int prepareTime;

        /**
         * 制限時間
         * 単位: 秒
         */
        public static int playTime;

        /**
         * 擬態にかかる時間
         * 単位: 秒
         */
        public static int hideWaitTime;
    }

    @Transitive
    @ConfigEntries
    public static class ItemConfig implements ConfigGroup {
        /**
         *  鬼が持つ探知用アイテム
         */
        @Transitive
        @ConfigEntries
        public static class ItemDetecter implements ConfigGroup {
            /**
             * 使用クールタイム
             */
            public static int coolTime;
            /**
             * ダメージ量
             */
            public static int damageAmount;
        }

        /**
         * 鬼が持つスキャン用アイテム
         */
        @Transitive
        @ConfigEntries
        public static class ItemScanner implements ConfigGroup {
            /**
             * 使用クールタイム
             */
            public static int coolTime;
            /**
             * 捜索半径
             */
            public static double scanLength;
        }
    }

}
