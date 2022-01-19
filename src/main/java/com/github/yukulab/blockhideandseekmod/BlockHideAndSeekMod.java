package com.github.yukulab.blockhideandseekmod;

import com.github.yukulab.blockhideandseekmod.command.BHASCommands;
import com.github.yukulab.blockhideandseekmod.command.Settings;
import com.github.yukulab.blockhideandseekmod.config.ModConfig;
import com.github.yukulab.blockhideandseekmod.entity.BhasEntityTypes;
import com.github.yukulab.blockhideandseekmod.item.BhasItems;
import com.github.yukulab.blockhideandseekmod.util.CoroutineProvider;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.minecraft.server.MinecraftServer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


public class BlockHideAndSeekMod implements ModInitializer {
	// This logger is used to write text to the console and the log file.
	// It is considered best practice to use your mod id as the logger's name.
	// That way, it's clear which mod wrote info, warnings, and errors.
	public static final String MOD_ID = "blockhideandseekmod";
	public static final Logger LOGGER = LogManager.getLogger(MOD_ID);

	public static MinecraftServer SERVER;

	public static ModConfig CONFIG;

	@Override
	public void onInitialize() {
        // This code runs as soon as Minecraft is in a mod-load-ready state.
        // However, some things (like resources) may still be uninitialized.
        // Proceed with mild caution.

        // init coroutine
        CoroutineProvider.init();

        //initialize config
        CONFIG = new ModConfig();
        CONFIG.load();
        //item
        BhasItems.init();
        //Entity
        BhasEntityTypes.register();
        //コマンド登録
        BHASCommands.register();
        Settings.registerCommands();

		ServerLifecycleEvents.SERVER_STARTED.register(server -> SERVER = server);
	}
}
