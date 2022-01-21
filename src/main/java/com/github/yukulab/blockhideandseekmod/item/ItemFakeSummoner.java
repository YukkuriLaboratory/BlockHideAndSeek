package com.github.yukulab.blockhideandseekmod.item;

import com.github.yukulab.blockhideandseekmod.BlockHideAndSeekMod;
import com.github.yukulab.blockhideandseekmod.config.ModConfig;
import com.github.yukulab.blockhideandseekmod.entity.BhasEntityTypes;
import com.github.yukulab.blockhideandseekmod.util.HideController;
import com.github.yukulab.blockhideandseekmod.util.HudDisplay;
import com.github.yukulab.blockhideandseekmod.util.TeamCreateAndDelete;
import com.github.yukulab.blockhideandseekmod.util.extention.ServerPlayerEntityKt;
import com.google.common.collect.Maps;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.FluidBlock;
import net.minecraft.entity.mob.ShulkerEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.*;
import net.minecraft.network.packet.s2c.play.BlockUpdateS2CPacket;
import net.minecraft.network.packet.s2c.play.CooldownUpdateS2CPacket;
import net.minecraft.scoreboard.Team;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.stat.Stats;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.RaycastContext;
import net.minecraft.world.World;
import net.minecraft.world.event.GameEvent;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Consumer;

import static net.minecraft.util.Formatting.YELLOW;

public class ItemFakeSummoner extends LoreItem implements ServerSideItem{
    private static final Item.Settings SETTINGS = new Item.Settings();

    public ItemFakeSummoner() {
        super(SETTINGS);
    }

    private static final Map<BlockPos, ShulkerEntity> fakeEntities = Maps.newHashMap();
    private static final Map<BlockPos, BlockState> decoyBlocks = Maps.newHashMap();
    private static final String ERROR_MESSAGE = "errormessage";

    @Override
    public Text getName() {
        return new LiteralText("フェイクサモナー");
    }

    @Override
    public List<Text> getLore() {
        return List.of(
                new LiteralText("右クリック: デコイブロックを設置します"),
                Text.of("デコイ消滅までの時間" + ModConfig.ItemConfig.ItemFakeSummoner.deleteTime + "秒"),
                Text.of("クールタイム" + ModConfig.ItemConfig.ItemFakeSummoner.cooltime)
        );
    }

    @Override
    public Item getVisualItem() {
        return Items.SHULKER_SHELL;
    }

    @Override
    public ActionResult useOnBlock(ItemUsageContext context) {
        var scoreboard = BlockHideAndSeekMod.SERVER.getScoreboard();
        var playerManager = BlockHideAndSeekMod.SERVER.getPlayerManager();
        Team team = scoreboard.getTeam("Decoy");
        if (team == null) {
            team = scoreboard.addTeam("Decoy");
            team.setColor(YELLOW);
        }

        var player = ((ServerPlayerEntity)context.getPlayer());
        World world = context.getWorld();
        if (!(world instanceof ServerWorld)) {
            return ActionResult.SUCCESS;
        }
        BlockPos blockPos = context.getBlockPos();
        var hiders = BlockHideAndSeekMod.SERVER.getPlayerManager().getPlayerList()
                .stream()
                .filter(p -> p.isTeamPlayer(TeamCreateAndDelete.getHiders()))
                .toList();

        if(world.getBlockState(blockPos.up()).isAir()) {
            if(HideController.getSelectedBlock(Objects.requireNonNull(player).getUuid()) != null) {
                var blockposUp = blockPos.up();
                if(fakeEntities.get(blockposUp) == null) {
                    setHighlight(blockposUp, hiders, entity -> {
                        entity.setCustomName(new LiteralText("デコイ"));
                        entity.setCustomNameVisible(true);
                    });
                    decoyBlocks.put(blockposUp, HideController.getSelectedBlock(Objects.requireNonNull(player).getUuid()));
                    var block = HideController.getSelectedBlock(player.getUuid());
                    var blockPacket = new BlockUpdateS2CPacket(blockposUp, block);
                    var playerTracker = ServerPlayerEntityKt.getPlayerTracker(player);
                    playerManager.getPlayerList()
                            .stream()
                            .peek(p -> p.networkHandler.sendPacket(blockPacket))
                            .filter(p -> p.getUuid() != player.getUuid())
                            .forEach(playerTracker::stopTracking);
                    var coolTime = getCoolTime();
                    player.getItemCooldownManager().set(this, coolTime);

                    player.networkHandler.sendPacket(new CooldownUpdateS2CPacket(getVisualItem(), coolTime));
                }
            }
            else {
                var message = new LiteralText("ブロックが選択されていません").setStyle(Style.EMPTY.withColor(Formatting.RED));
                HudDisplay.setActionBarText(player.getUuid(), ERROR_MESSAGE, message ,30L);
            }
        }
        return ActionResult.CONSUME;
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        var stack = user.getStackInHand(hand);

        BlockHitResult hitResult = SpawnEggItem.raycast(world, user, RaycastContext.FluidHandling.SOURCE_ONLY);
        if (((HitResult)hitResult).getType() != HitResult.Type.BLOCK) {
            return TypedActionResult.pass(stack);
        }

        if (!(world instanceof ServerWorld)) {
            return TypedActionResult.success(stack);
        }

        BlockPos blockPos = hitResult.getBlockPos();
        if (!(world.getBlockState(blockPos).getBlock() instanceof FluidBlock)) {
            return TypedActionResult.pass(stack);
        }
        if (!world.canPlayerModifyAt(user, blockPos) || !user.canPlaceOn(blockPos, hitResult.getSide(), stack)) {
            return TypedActionResult.fail(stack);
        }

        user.incrementStat(Stats.USED.getOrCreateStat(this));
        world.emitGameEvent(GameEvent.ENTITY_PLACE, user);

        return TypedActionResult.success(stack);
    }

    public static void setHighlight(BlockPos pos, List<ServerPlayerEntity> players, Consumer<ShulkerEntity> entityEditConsumer) {
        if (players.isEmpty()) {
            return;
        }
        var scoreboard = BlockHideAndSeekMod.SERVER.getScoreboard();

        var world = players.get(0).world;
        var entity = BhasEntityTypes.DECOY.create(world);
        if (entity == null) {
            BlockHideAndSeekMod.LOGGER.error("cannot get BlockHighlightEntity!!");
            return;
        }
        scoreboard.addPlayerToTeam(Objects.requireNonNull(entity).getEntityName(),scoreboard.getTeam("Decoy"));
        entityEditConsumer.accept(entity);
        entity.setPos(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5);
        world.spawnEntity(entity);
        fakeEntities.put(pos, entity);
    }

    public static void removeHighlight(BlockPos pos) {
        var entity = fakeEntities.remove(pos);
        decoyBlocks.remove(pos);
        if (entity == null) {
            return;
        }
        var packet = new BlockUpdateS2CPacket(pos, Blocks.AIR.getDefaultState());
        BlockHideAndSeekMod.SERVER.getPlayerManager().getPlayerList()
                .forEach(p -> p.networkHandler.sendPacket(packet));
        entity.discard();
    }

    public static void clearHighlight(){
        fakeEntities.forEach((key, value) -> value.discard());
        decoyBlocks.forEach((key, value) -> {
            var packet = new BlockUpdateS2CPacket(key, Blocks.AIR.getDefaultState());
            BlockHideAndSeekMod.SERVER.getPlayerManager().getPlayerList()
                    .forEach(p -> p.networkHandler.sendPacket(packet));
        });
        fakeEntities.clear();
        decoyBlocks.clear();
    }

    public static BlockState getDecoyState(BlockPos pos) {
        return decoyBlocks.get(pos);
    }

    public static Set<Map.Entry<BlockPos, BlockState>> getDecoyBlocks() {
        return decoyBlocks.entrySet();
    }

    private static int getCoolTime() {
        return ModConfig.ItemConfig.ItemFakeSummoner.cooltime;
    }
}
