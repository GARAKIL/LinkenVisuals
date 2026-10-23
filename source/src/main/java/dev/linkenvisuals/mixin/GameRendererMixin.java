package dev.linkenvisuals.mixin;

import dev.linkenvisuals.hands.HandEditor;
import dev.linkenvisuals.hands.HandOutline;
import dev.linkenvisuals.clickgui.ModuleRegistry;
import dev.linkenvisuals.render.ProjectionMath;
import dev.linkenvisuals.module.Modules;
import dev.linkenvisuals.module.impl.ZoomModule;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.GameRenderer;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(GameRenderer.class)
public abstract class GameRendererMixin {
    @Shadow
    public abstract Matrix4f getBasicProjectionMatrix(float fov);

    @Shadow
    private float getFov(Camera camera, float tickProgress, boolean changingFov) {
        throw new AssertionError();
    }

    @Shadow
    public abstract Camera getCamera();

    @Inject(method = "renderHand", at = @At("HEAD"))
    private void linkenvisuals$beforeHands(float tickProgress, boolean sleeping,
                                            Matrix4f positionMatrix, CallbackInfo ci) {
        if (HandEditor.active() && getCamera() != null) {
            HandEditor.setFov(getFov(getCamera(), tickProgress, true));
            HandEditor.update();
        }
        HandOutline.captureBefore();
    }

    @Inject(method = "renderWorld", at = @At("TAIL"))
    private void linkenvisuals$afterHands(CallbackInfo ci) {
        HandOutline.composite();
    }

    @Inject(method = "getBasicProjectionMatrix", at = @At("RETURN"), cancellable = true)
    private void linkenvisuals$aspectRatio(float fov, CallbackInfoReturnable<Matrix4f> cir) {
        var module = ModuleRegistry.aspectRatio();
        if (module == null || !module.enabled()) {
            return;
        }
        var setting = ModuleRegistry.aspectRatioValue();
        if (setting == null) {
            return;
        }
        cir.setReturnValue(ProjectionMath.withAspect(cir.getReturnValue(), setting.value()));
    }

    @Inject(method = "getFov", at = @At("RETURN"), cancellable = true)
    private void linkenvisuals$onGetFov(Camera camera, float tickDelta, boolean changingFov,
                                         CallbackInfoReturnable<Float> cir) {
        ZoomModule module = Modules.zoom();
        if (module == null || !module.enabled()) {
            return;
        }
        float multiplier = module.getFovMultiplier(tickDelta);
        if (multiplier != 1.0f) {
            cir.setReturnValue(cir.getReturnValueF() * multiplier);
        }
    }
}
