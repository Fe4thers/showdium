package com.noxcrew.noxesium.showdium.mixin;

import com.noxcrew.noxesium.api.component.GameComponents;
import com.noxcrew.noxesium.showdium.registry.ShowdiumGameComponent;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.TrapDoorBlock;
import net.minecraft.world.level.block.piston.*;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(PistonMovingBlockEntity.class)
public class PistonPushMixin {

    @ModifyVariable(method = "moveCollidedEntities", at = @At(value = "STORE", ordinal = 0), ordinal = 0)
    private static boolean replaceBooleanBl(
            boolean originalBl,
            Level level,
            BlockPos blockPos,
            float f,
            PistonMovingBlockEntity pistonMovingBlockEntity) {
        String noxesiumComponent = GameComponents.getInstance()
                .noxesium$getComponentOr(ShowdiumGameComponent.SlimeBlockBlocks, () -> "empty");
        noxesiumComponent = noxesiumComponent.replace("empty", "");
        String[] parts = noxesiumComponent.split(",");
        List<Integer> list = Arrays.stream(parts)
                .filter(s -> !s.isEmpty())
                .mapToInt(Integer::parseInt)
                .boxed()
                .toList();

        if (pistonMovingBlockEntity.getMovedState().getBlock() instanceof PistonBaseBlock
                || pistonMovingBlockEntity.getMovedState().getBlock() instanceof PistonHeadBlock
                || pistonMovingBlockEntity.getMovedState().getBlock() instanceof MovingPistonBlock) {
            return false;
        }

        if ((pistonMovingBlockEntity.getMovedState().getBlock() instanceof SlabBlock
                        || pistonMovingBlockEntity.getMovedState().getBlock() instanceof StairBlock
                        || pistonMovingBlockEntity.getMovedState().getBlock() instanceof TrapDoorBlock)
                && list.contains(blockPos.getY())) {
            return false;
        }

        if (noxesiumComponent.equals("empty")) {
            return originalBl;
        }
        return true;
    }

    @Inject(
            method = "moveCollidedEntities",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Entity;setDeltaMovement(DDD)V"),
            cancellable = true,
            locals = LocalCapture.CAPTURE_FAILSOFT)
    private static void injectBeforeSetVelocity(
            Level level,
            BlockPos blockPos,
            float f,
            PistonMovingBlockEntity pistonMovingBlockEntity,
            CallbackInfo ci,
            Direction direction,
            double d,
            VoxelShape voxelShape,
            AABB aABB,
            List list,
            List list2,
            boolean bl,
            Iterator var12,
            Entity entity,
            Vec3 vec3,
            double e,
            double g,
            double h) {

        if (!customEntityCondition(entity)) {
            switch (direction.getAxis()) {
                case X -> e = (double) direction.getStepX();
                case Y -> g = (double) direction.getStepY();
                case Z -> h = (double) direction.getStepZ();
            }
            entity.setDeltaMovement(e, g, h);
            ci.cancel();
        }
    }

    private static boolean customEntityCondition(Entity entity) {
        String noxesiumComponent = GameComponents.getInstance()
                .noxesium$getComponentOr(ShowdiumGameComponent.SlimeBlockBlocks, () -> "empty");
        return entity.getType() == EntityType.PLAYER || !noxesiumComponent.equals("empty");
    }
}
