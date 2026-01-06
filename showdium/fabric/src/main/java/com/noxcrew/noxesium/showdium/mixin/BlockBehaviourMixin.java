package com.noxcrew.noxesium.showdium.mixin;

import com.noxcrew.noxesium.api.component.GameComponents;
import com.noxcrew.noxesium.showdium.registry.ShowdiumGameComponent;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.StructureVoidBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.EntityCollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BlockBehaviour.class)
public abstract class BlockBehaviourMixin {

    @Inject(method = "getCollisionShape", at = @At("HEAD"), cancellable = true)
    private void overrideStructureVoidCollisionCheck(
            BlockState state,
            BlockGetter world,
            BlockPos pos,
            CollisionContext context,
            CallbackInfoReturnable<VoxelShape> cir) {
        if (Minecraft.getInstance() == null || Minecraft.getInstance().level == null) {
            return;
        }

        if (GameComponents.getInstance().noxesium$hasComponent(ShowdiumGameComponent.StructureVoidsWithCollision)) {
            if (state.getBlock() instanceof StructureVoidBlock) {
                if (context instanceof EntityCollisionContext entityContext
                        && entityContext.getEntity() instanceof Player) {
                    cir.setReturnValue(state.getShape(world, pos, context));
                } else {
                    cir.setReturnValue(Shapes.empty());
                }
            }
        }
    }
}
