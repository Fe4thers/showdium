package com.noxcrew.noxesium.showdium.mixin;

import com.noxcrew.noxesium.api.component.GameComponents;
import com.noxcrew.noxesium.showdium.registry.ShowdiumGameComponent;
import java.util.Optional;
import java.util.function.Consumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.LoadingOverlay;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.ReloadInstance;
import net.minecraft.util.ARGB;
import net.minecraft.util.Mth;
import net.minecraft.util.Util;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LoadingOverlay.class)
public abstract class LoadingOverlayMixin {

    @Unique
    private static final Identifier CUSTOM_BACKGROUND =
            Identifier.fromNamespaceAndPath("showdium", "loading_background.png");

    @Shadow
    @Final
    private Minecraft minecraft;

    @Shadow
    @Final
    private ReloadInstance reload;

    @Shadow
    @Final
    private Consumer<Optional<Throwable>> onFinish;

    @Shadow
    @Final
    private boolean fadeIn;

    @Shadow
    private float currentProgress;

    @Shadow
    private long fadeOutStart;

    @Shadow
    private long fadeInStart;

    @Inject(method = "render", at = @At("HEAD"), cancellable = true)
    private void renderCustomScreen(
            GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick, CallbackInfo ci) {
        if (!GameComponents.getInstance().noxesium$hasComponent(ShowdiumGameComponent.ShowdiumLoadingScreen)) {
            return;
        }
        ci.cancel();

        int screenWidth = guiGraphics.guiWidth();
        int screenHeight = guiGraphics.guiHeight();
        long currentTime = Util.getMillis();

        if (this.fadeIn && this.fadeInStart == -1L) this.fadeInStart = currentTime;
        float fadeOutSeconds = this.fadeOutStart > -1L ? (float) (currentTime - this.fadeOutStart) / 300.0f : -1.0f;
        float fadeInSeconds = this.fadeInStart > -1L ? (float) (currentTime - this.fadeInStart) / 200.0f : -1.0f;
        float alpha;

        if (fadeOutSeconds >= 1.0f) alpha = 1.0f - Mth.clamp(fadeOutSeconds - 1.0f, 0.0f, 1.0f);
        else if (this.fadeIn) alpha = Mth.clamp(fadeInSeconds, 0.0f, 1.0f);
        else alpha = 1.0f;

        guiGraphics.fill(0, 0, screenWidth, screenHeight, 0xFF000000);

        try {
            // Assuming you have this accessor interface implemented correctly
            ((IGuiGraphicsAccessor) guiGraphics)
                    .showdium_invokeInnerBlit(
                            net.minecraft.client.renderer.RenderPipelines.GUI_TEXTURED,
                            CUSTOM_BACKGROUND,
                            0,
                            screenWidth,
                            0,
                            screenHeight,
                            0.0f,
                            1.0f,
                            0.0f,
                            1.0f,
                            -1);
        } catch (Exception e) {
            guiGraphics.fill(0, 0, screenWidth, screenHeight, 0xFFFF00FF);
        }

        float fadeOverlayAlpha = 1.0f - alpha;
        int fadeOverlayColor = ARGB.color((int) (fadeOverlayAlpha * 255.0f), 0, 0, 0);
        guiGraphics.fill(0, 0, screenWidth, screenHeight, fadeOverlayColor);

        float actualProgress = this.reload.getActualProgress();
        this.currentProgress = Mth.clamp(this.currentProgress * 0.95f + actualProgress * 0.05f, 0.0f, 1.0f);
        if (fadeOutSeconds < 1.0f) {
            double d = Math.min((double) screenWidth * 0.75, (double) screenHeight) * 0.25;
            int r = (int) (d * 2.0);
            int progressBarY = (int) ((double) screenHeight * 0.8325);
            this.drawProgressBar(
                    guiGraphics, screenWidth / 2 - r, progressBarY - 5, screenWidth / 2 + r, progressBarY + 5, alpha);
        }
        if (fadeOutSeconds >= 2.0f) this.minecraft.setOverlay(null);
        if (this.fadeOutStart == -1L && this.reload.isDone() && (!this.fadeIn || fadeInSeconds >= 2.0f)) {
            try {
                this.reload.checkExceptions();
                this.onFinish.accept(Optional.empty());
            } catch (Throwable throwable) {
                this.onFinish.accept(Optional.of(throwable));
            }
            this.fadeOutStart = Util.getMillis();
            if (this.minecraft.screen != null) this.minecraft.screen.init(screenWidth, screenHeight);
        }
    }

    @Shadow
    private void drawProgressBar(GuiGraphics guiGraphics, int i, int j, int k, int l, float f) {
        throw new AssertionError();
    }
}
