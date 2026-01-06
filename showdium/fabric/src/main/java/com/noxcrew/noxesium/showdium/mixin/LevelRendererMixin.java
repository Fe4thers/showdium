package com.noxcrew.noxesium.showdium.mixin;

import com.mojang.blaze3d.buffers.GpuBufferSlice;
import com.mojang.blaze3d.resource.GraphicsResourceAllocator;
import com.noxcrew.noxesium.showdium.pingsystem.RenderContext;
import net.minecraft.client.Camera;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.renderer.LevelRenderer;
import org.joml.Matrix4f;
import org.joml.Vector4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Mixin to capture render matrices during world rendering.
 * These matrices are needed for projecting ping positions to screen space.
 */
@Mixin(LevelRenderer.class)
public abstract class LevelRendererMixin {

    @Inject(
            method = "renderLevel",
            at =
                    @At(
                            value = "FIELD",
                            target =
                                    "Lnet/minecraft/client/renderer/LevelRenderer;minecraft:Lnet/minecraft/client/Minecraft;",
                            ordinal = 7,
                            shift = At.Shift.AFTER))
    private void captureRenderContext(
            GraphicsResourceAllocator graphicsResourceAllocator,
            DeltaTracker deltaTracker,
            boolean bl,
            Camera camera,
            Matrix4f matrix4f,
            Matrix4f matrix4f2,
            Matrix4f matrix4f3,
            GpuBufferSlice gpuBufferSlice,
            Vector4f vector4f,
            boolean bl2,
            CallbackInfo ci) {

        float tickDelta = deltaTracker.getGameTimeDeltaPartialTick(true);
        RenderContext.captureMatrices(matrix4f, matrix4f2, tickDelta, camera);
    }
}
