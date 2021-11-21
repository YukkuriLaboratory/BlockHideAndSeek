package com.iduki.blockhideandseekmod.command;

public class TeamBlocks {
    /*public static void registerCommands() {
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
        if (!self.giveItemStack(new ItemStack(Items.RED_CONCRETE, 64))) {
            throw new SimpleCommandExceptionType(new TranslatableText("inventory.isfull")).create();
        }
        if (!self.giveItemStack(new ItemStack(Items.BLUE_CONCRETE, 64))) {
            throw new SimpleCommandExceptionType(new TranslatableText("inventory.isfull")).create();
        }
        if (!self.giveItemStack(new ItemStack(Items.GRAY_CONCRETE, 64))) {
            throw new SimpleCommandExceptionType(new TranslatableText("inventory.isfull")).create();
        }

        return 1;
    }*/

}
