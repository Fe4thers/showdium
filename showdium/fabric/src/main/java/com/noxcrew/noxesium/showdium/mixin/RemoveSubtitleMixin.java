package com.noxcrew.noxesium.showdium.mixin;

import com.noxcrew.noxesium.api.component.GameComponents;
import com.noxcrew.noxesium.showdium.registry.ShowdiumGameComponent;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.SubtitleOverlay;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(SubtitleOverlay.class)
public class RemoveSubtitleMixin {
    @Inject(method = "extractRenderState", at = @At("HEAD"), cancellable = true)
    private void onSetSubtitle(GuiGraphicsExtractor guiGraphics, CallbackInfo ci) {
        if (GameComponents.getInstance().noxesium$hasComponent(ShowdiumGameComponent.DisableSubtitles)) {
            ci.cancel();
        }
    }
}
