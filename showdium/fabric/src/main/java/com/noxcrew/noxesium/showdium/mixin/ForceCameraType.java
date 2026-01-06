package com.noxcrew.noxesium.showdium.mixin;

import com.noxcrew.noxesium.api.component.GameComponents;
import com.noxcrew.noxesium.showdium.registry.ShowdiumGameComponent;
import net.minecraft.client.CameraType;
import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Minecraft.class)
public class ForceCameraType {
    @Inject(method = "tick", at = @At("HEAD"))
    private void onTick(CallbackInfo ci) {
        Minecraft mc = (Minecraft) (Object) this;
        if (GameComponents.getInstance().noxesium$hasComponent(ShowdiumGameComponent.ForcePerspective)) {
            Integer perspective = GameComponents.getInstance()
                    .noxesium$getComponentOr(ShowdiumGameComponent.ForcePerspective, () -> -1);
            switch (perspective) {
                case 0:
                    mc.options.setCameraType(CameraType.FIRST_PERSON);
                    break;
                case 1:
                    mc.options.setCameraType(CameraType.THIRD_PERSON_BACK);
                    break;
                case 2:
                    mc.options.setCameraType(CameraType.THIRD_PERSON_FRONT);
                    break;
            }
        }
    }
}
