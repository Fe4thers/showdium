package com.noxcrew.noxesium.showdium.mixin;

import com.noxcrew.noxesium.api.component.GameComponents;
import com.noxcrew.noxesium.showdium.registry.ShowdiumGameComponent;
import java.util.Arrays;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BlockBehaviour.class)
public class BlockInteractionMixin {

    @Inject(
            method =
                    "useItemOn(Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/entity/player/Player;Lnet/minecraft/world/InteractionHand;Lnet/minecraft/world/phys/BlockHitResult;)Lnet/minecraft/world/InteractionResult;",
            at = @At("HEAD"),
            cancellable = true)
    private void onUseItemOn(
            net.minecraft.world.item.ItemStack stack,
            BlockState state,
            Level level,
            BlockPos pos,
            Player player,
            InteractionHand hand,
            BlockHitResult hitResult,
            CallbackInfoReturnable<InteractionResult> cir) {
        HandleInteraction(state, player, cir);
    }

    @Inject(
            method =
                    "useWithoutItem(Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/entity/player/Player;Lnet/minecraft/world/phys/BlockHitResult;)Lnet/minecraft/world/InteractionResult;",
            at = @At("HEAD"),
            cancellable = true)
    private void onUseWithoutItem(
            BlockState state,
            Level level,
            BlockPos pos,
            Player player,
            BlockHitResult hitResult,
            CallbackInfoReturnable<InteractionResult> cir) {
        HandleInteraction(state, player, cir);
    }

    @Unique
    private void HandleInteraction(BlockState state, Player player, CallbackInfoReturnable<InteractionResult> cir) {
        String blockId = state.getBlock().getDescriptionId();
        String prefix = "block.minecraft.";

        if (blockId.startsWith(prefix)) {
            String cleanedId = blockId.substring(prefix.length());
            String noxesiumComponent = GameComponents.getInstance()
                    .noxesium$getComponentOr(ShowdiumGameComponent.NoBlockInteractions, () -> "");
            String[] parts = noxesiumComponent.split(","); // Split by the comma separator
            List<String> list = Arrays.asList(parts);
            if (list.contains(cleanedId)) {
                cir.setReturnValue(InteractionResult.FAIL);
            }
        }
    }
}
