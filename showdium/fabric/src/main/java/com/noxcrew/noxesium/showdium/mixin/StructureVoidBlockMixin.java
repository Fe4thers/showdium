package com.noxcrew.noxesium.showdium.mixin;

import com.noxcrew.noxesium.api.component.GameComponents;
import com.noxcrew.noxesium.showdium.registry.ShowdiumGameComponent;
import net.minecraft.client.Minecraft; // Import Minecraft
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.StructureVoidBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(StructureVoidBlock.class)
public class StructureVoidBlockMixin {

    @Inject(method = "getShape", at = @At("HEAD"), cancellable = true)
    private void makeShapeSolid(
            BlockState state,
            BlockGetter world,
            BlockPos pos,
            CollisionContext context,
            CallbackInfoReturnable<VoxelShape> cir) {
        if (Minecraft.getInstance() == null || Minecraft.getInstance().level == null) {
            return;
        }

        if (GameComponents.getInstance().noxesium$hasComponent(ShowdiumGameComponent.StructureVoidsWithCollision)) {
            cir.setReturnValue(Shapes.block());
        }
    }
}
