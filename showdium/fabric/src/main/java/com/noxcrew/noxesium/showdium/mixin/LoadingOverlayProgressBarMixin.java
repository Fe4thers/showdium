package com.noxcrew.noxesium.showdium.mixin;

import com.noxcrew.noxesium.api.component.GameComponents;
import com.noxcrew.noxesium.showdium.registry.ShowdiumGameComponent;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.LoadingOverlay;
import net.minecraft.util.ARGB;
import net.minecraft.util.Mth;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LoadingOverlay.class)
public abstract class LoadingOverlayProgressBarMixin {

    @Shadow
    private float currentProgress;

    @Inject(method = "drawProgressBar", at = @At("HEAD"), cancellable = true)
    private void onDrawProgressBar(
            GuiGraphics guiGraphics, int left, int top, int right, int bottom, float alpha, CallbackInfo ci) {
        if (!GameComponents.getInstance().noxesium$hasComponent(ShowdiumGameComponent.ShowdiumLoadingScreen)) {
            return; // let original run
        }
        ci.cancel(); // Cancel original

        int totalWidth = right - left;
        int totalHeight = bottom - top;

        // Calculate filled width based on actual progress (not alpha!)
        int filledWidth = Mth.ceil((float) totalWidth * this.currentProgress);

        // Alpha is for opacity/transparency
        int alphaInt = Math.round(alpha * 255.0f);

        // Background color (dark gray with alpha)
        int backgroundColor = ARGB.color(alphaInt, 40, 40, 40);

        // Progress color (yellow/gold with alpha)
        int progressColor = ARGB.color(alphaInt, 255, 215, 0);

        // Draw background bar
        guiGraphics.fill(left, top, right, bottom, backgroundColor);

        // Draw filled progress bar
        if (filledWidth > 0) {
            guiGraphics.fill(left, top, left + filledWidth, bottom, progressColor);
        }

        // Optional: Draw border (like vanilla does)
        int borderColor = ARGB.color(alphaInt, 243, 206, 40);
        guiGraphics.fill(left - 1, top - 1, right + 1, top, borderColor); // top border
        guiGraphics.fill(left - 1, bottom, right + 1, bottom + 1, borderColor); // bottom border
        guiGraphics.fill(left - 1, top, left, bottom, borderColor); // left border
        guiGraphics.fill(right, top, right + 1, bottom, borderColor); // right border
    }
}
