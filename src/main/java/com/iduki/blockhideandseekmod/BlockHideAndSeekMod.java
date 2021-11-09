package com.iduki.blockhideandseekmod;


import com.iduki.blockhideandseekmod.config.ModConfig;
import com.iduki.blockhideandseekmod.effect.HideEffect;
import com.terraformersmc.modmenu.util.mod.Mod;
import me.lortseam.completeconfig.api.ConfigContainer;
import me.lortseam.completeconfig.api.ConfigGroup;
import net.fabricmc.api.ModInitializer;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.item.Item;
import net.minecraft.block.Block;
import net.minecraft.block.Material;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemGroup;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import com.iduki.blockhideandseekmod.block.SampleBlock;


public class BlockHideAndSeekMod implements ModInitializer {
	// This logger is used to write text to the console and the log file.
	// It is considered best practice to use your mod id as the logger's name.
	// That way, it's clear which mod wrote info, warnings, and errors.
	public static final String MOD_ID = "blockhideandseekmod";
	public static final Logger LOGGER = LogManager.getLogger("modid");


	public static final Block SAMPLEBLOCK = new SampleBlock(FabricBlockSettings.of(Material.METAL).strength(0f));

	public static final StatusEffect EXP = new HideEffect();

	public static ModConfig CONFIG;

	@Override
	public void onInitialize() {
		// This code runs as soon as Minecraft is in a mod-load-ready state.
		// However, some things (like resources) may still be uninitialized.
		// Proceed with mild caution.

		//initialize config
		CONFIG = new ModConfig();
        CONFIG.load();

		//ブロック登録
		Registry.register(Registry.BLOCK, new Identifier(MOD_ID, "sample_block"), SAMPLEBLOCK);
		//ブロックアイテムの登録
		Registry.register(Registry.ITEM, new Identifier(MOD_ID, "sample_block"), new BlockItem(SAMPLEBLOCK, new Item.Settings().group(ItemGroup.MISC)));
        //ステータスエフェクト登録
		Registry.register(Registry.STATUS_EFFECT, new Identifier(MOD_ID, "hideeffect"), EXP);

		LOGGER.info("Hello Fabric world!");
	}
}
