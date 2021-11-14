package com.iduki.blockhideandseekmod.command;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import net.fabricmc.fabric.api.command.v1.CommandRegistrationCallback;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.TranslatableText;

import static net.minecraft.server.command.CommandManager.literal;


public class TeamBlocks {
    public static void registerCommands() {
        CommandRegistrationCallback.EVENT.register(
                (dispatcher, dedicated) ->
                        dispatcher.register(
                                literal("bhas")
                                        .then(literal("teamblocks")
                                                .executes(TeamBlocks::giveTeamblocks)
                                        )
                                        .then(literal("tb")
                                                .executes(TeamBlocks::giveTeamblocks)
                                        )
                        )
        );
    }

    public static int giveTeamblocks(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        final ServerCommandSource source = context.getSource();
        //チーム分けブロックを渡す
        final PlayerEntity self = source.getPlayer();
        if (!self.giveItemStack(new ItemStack(Items.ORANGE_CONCRETE, 64))) {
            throw new SimpleCommandExceptionType(new TranslatableText("inventory.isfull")).create();
        }
        if (!self.giveItemStack(new ItemStack(Items.LIGHT_BLUE_CONCRETE, 64))) {
            throw new SimpleCommandExceptionType(new TranslatableText("inventory.isfull")).create();
        }

        return 1;
    }

}
