package com.noxcrew.noxesium.showdium.mixin;

import com.noxcrew.noxesium.api.component.GameComponents;
import com.noxcrew.noxesium.showdium.registry.ShowdiumGameComponent;
import net.minecraft.client.CameraType;
import net.minecraft.client.Options;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Options.class)
public class ForceCameraOption {

    @Inject(method = "getCameraType", at = @At("HEAD"), cancellable = true)
    private void forceFirstPersonPerspective(CallbackInfoReturnable<CameraType> cir) {
        if (GameComponents.getInstance().noxesium$hasComponent(ShowdiumGameComponent.ForcePerspective)) {
            Integer perspective = GameComponents.getInstance()
                    .noxesium$getComponentOr(ShowdiumGameComponent.ForcePerspective, () -> -1);
            switch (perspective) {
                case 0:
                    cir.setReturnValue(CameraType.FIRST_PERSON);
                    break;
                case 1:
                    cir.setReturnValue(CameraType.THIRD_PERSON_BACK);
                    break;
                case 2:
                    cir.setReturnValue(CameraType.THIRD_PERSON_FRONT);
                    break;
            }
        }
    }
}
