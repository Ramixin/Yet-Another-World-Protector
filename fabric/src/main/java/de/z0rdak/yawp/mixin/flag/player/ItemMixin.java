package de.z0rdak.yawp.mixin.flag.player;

import de.z0rdak.yawp.api.FlagEvaluator;
import de.z0rdak.yawp.api.events.flag.FlagCheckRequest;
import de.z0rdak.yawp.platform.Services;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.component.BlockTransformer;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.component.BlockTransformers;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import static de.z0rdak.yawp.core.flag.RegionFlag.*;
import static de.z0rdak.yawp.handler.HandlerUtil.*;
import static de.z0rdak.yawp.api.MessageSender.sendFlagMsg;

@Mixin(Item.class)
public abstract class ItemMixin {

    @Inject(method = "useOn", at = @At(value = "HEAD"), cancellable = true, allow = 1)
    public void dispatchUseOn(UseOnContext context, CallbackInfoReturnable<InteractionResult> cir) {

        Holder<BlockTransformer> blockTransformerHolder = context.getItemInHand().get(DataComponents.BLOCK_TRANSFORMER);
        if(blockTransformerHolder == null) return;
        if(blockTransformerHolder.is(BlockTransformers.AXE))
            onUseAxeOnBlock(context, cir);
        else if(blockTransformerHolder.is(BlockTransformers.HOE))
            onUseHoeOnBlock(context, cir);
        else if(blockTransformerHolder.is(BlockTransformers.SHOVEL))
            onUseShovelOnBlock(context, cir);

    }

    @Unique
    private void onUseShovelOnBlock(UseOnContext context, CallbackInfoReturnable<InteractionResult> cir) {
        Level world = context.getLevel();
        BlockPos pos = context.getClickedPos();
        Player player = context.getPlayer();
        if (isServerSide(world) && player != null) {
            FlagCheckRequest checkEvent = new FlagCheckRequest(pos, TOOL_SECONDARY_USE, getDimKey(player), player);
            if (Services.FLAG_EVENT_DISPATCHER.post(checkEvent))
                return;
            FlagEvaluator.processCheck(checkEvent, deny -> {
                sendFlagMsg(deny);
                cir.setReturnValue(InteractionResult.PASS);
            });
            checkEvent = new FlagCheckRequest(pos, SHOVEL_PATH, getDimKey(player), player);
            if (Services.FLAG_EVENT_DISPATCHER.post(checkEvent))
                return;
            FlagEvaluator.processCheck(checkEvent, deny -> {
                sendFlagMsg(deny);
                cir.setReturnValue(InteractionResult.PASS);
            });
        }
    }

    @Unique
    private void onUseHoeOnBlock(UseOnContext context, CallbackInfoReturnable<InteractionResult> cir) {
        BlockPos pos = context.getClickedPos();
        Player player = context.getPlayer();
        if (isServerSide(context.getLevel())) {
            if (player != null) {
                FlagCheckRequest checkEvent = new FlagCheckRequest(pos, TOOL_SECONDARY_USE, getDimKey(player), player);
                if (Services.FLAG_EVENT_DISPATCHER.post(checkEvent)) {
                    return;
                }
                FlagEvaluator.processCheck(checkEvent, deny -> {
                    sendFlagMsg(deny);
                    cir.setReturnValue(InteractionResult.PASS);
                });
                checkEvent = new FlagCheckRequest(pos, HOE_TILL, getDimKey(player), player);
                if (Services.FLAG_EVENT_DISPATCHER.post(checkEvent)) {
                    return;
                }
                FlagEvaluator.processCheck(checkEvent, deny -> {
                    sendFlagMsg(deny);
                    cir.setReturnValue(InteractionResult.PASS);
                });
            }
        }
    }

    @Unique
    private static void onUseAxeOnBlock(UseOnContext context, CallbackInfoReturnable<InteractionResult> cir) {
        BlockPos pos = context.getClickedPos();
        Player player = context.getPlayer();
        if (isServerSide(context.getLevel()) && player != null) {
            FlagCheckRequest checkEvent = new FlagCheckRequest(pos, TOOL_SECONDARY_USE, getDimKey(context.getLevel()), player);
            if (Services.FLAG_EVENT_DISPATCHER.post(checkEvent)) {
                return;
            }
            FlagEvaluator.processCheck(checkEvent, deny -> {
                sendFlagMsg(deny);
                cir.setReturnValue(InteractionResult.PASS);
            });

            checkEvent = new FlagCheckRequest(pos, AXE_STRIP, getDimKey(context.getLevel()), player);
            if (Services.FLAG_EVENT_DISPATCHER.post(checkEvent)) {
                return;
            }
            FlagEvaluator.processCheck(checkEvent, deny -> {
                sendFlagMsg(deny);
                cir.setReturnValue(InteractionResult.PASS);
            });
        }
    }
}
