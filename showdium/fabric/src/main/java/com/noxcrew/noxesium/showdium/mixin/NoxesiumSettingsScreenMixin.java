package com.noxcrew.noxesium.showdium.mixin;

import com.noxcrew.noxesium.core.fabric.config.NoxesiumSettingsScreen;
import com.noxcrew.noxesium.showdium.config.ShowdiumOptionsScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.layouts.GridLayout;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Adds Showdium options to the Noxesium settings screen.
 */
@Mixin(NoxesiumSettingsScreen.class)
public abstract class NoxesiumSettingsScreenMixin {

    @Inject(method = "addToDeveloperTab", at = @At("TAIL"))
    public void addShowdiumOptions(GridLayout.RowHelper rowHelper, CallbackInfo ci) {
        NoxesiumSettingsScreen self = (NoxesiumSettingsScreen) (Object) this;

        rowHelper.addChild(Button.builder(
                        Component.translatable("showdium.options.open_settings"),
                        button -> Minecraft.getInstance().setScreenAndShow(new ShowdiumOptionsScreen(self)))
                .bounds(0, 0, 150, 20)
                .build());
    }
}
