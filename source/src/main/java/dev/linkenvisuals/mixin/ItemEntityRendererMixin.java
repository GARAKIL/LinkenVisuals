package dev.linkenvisuals.mixin;

import dev.linkenvisuals.module.Modules;
import dev.linkenvisuals.module.impl.ItemPhysicsModule;
import dev.linkenvisuals.render.state.ItemPhysicsRenderState;
import dev.linkenvisuals.util.ItemPhysicsMath;
import net.minecraft.client.render.command.OrderedRenderCommandQueue;
import net.minecraft.client.render.entity.ItemEntityRenderer;
import net.minecraft.client.render.entity.state.ItemEntityRenderState;
import net.minecraft.client.render.state.CameraRenderState;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.RotationAxis;
import net.minecraft.util.math.random.Random;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * ItemPhysics renderer: replaces vanilla's upright item with a flat, tumbling
 * item. The stack layout (depth stacks, random spread) is delegated to
 * vanilla's static {@code ItemEntityRenderer.render(...)} helper, so only the
 * outer transform differs.
 */
@Mixin(ItemEntityRenderer.class)
public abstract class ItemEntityRendererMixin {
    @Shadow
    @Final
    private Random random;

    @Inject(method = "updateRenderState(Lnet/minecraft/entity/ItemEntity;"
        + "Lnet/minecraft/client/render/entity/state/ItemEntityRenderState;F)V",
        at = @At("TAIL"))
    private void linkenvisuals$capturePhysics(net.minecraft.entity.ItemEntity entity,
                                              ItemEntityRenderState state, float tickDelta,
                                              CallbackInfo ci) {
        ItemPhysicsModule.captureEntityState(entity, state);
    }

    @Inject(method = "render(Lnet/minecraft/client/render/entity/state/ItemEntityRenderState;"
        + "Lnet/minecraft/client/util/math/MatrixStack;"
        + "Lnet/minecraft/client/render/command/OrderedRenderCommandQueue;"
        + "Lnet/minecraft/client/render/state/CameraRenderState;)V",
        at = @At("HEAD"), cancellable = true)
    private void linkenvisuals$renderPhysics(ItemEntityRenderState state, MatrixStack matrices,
                                             OrderedRenderCommandQueue queue,
                                             CameraRenderState cameraState, CallbackInfo ci) {
        ItemPhysicsModule module = Modules.itemPhysics();
        if (module == null || !module.enabled() || state.itemRenderState.isEmpty()
            || state.age < 1.0f) {
            return;
        }

        ItemPhysicsRenderState physics = (ItemPhysicsRenderState) state;
        boolean sideLit = state.itemRenderState.isSideLit();
        Box box = state.itemRenderState.getModelBoundingBox();
        boolean hasDepth = box.getLengthZ() > 0.0625;

        matrices.push();
        random.setSeed(state.seed);
        matrices.multiply(RotationAxis.POSITIVE_X.rotation((float) Math.PI / 2.0f));
        matrices.multiply(RotationAxis.POSITIVE_Z.rotation(physics.linkenvisuals$getYRotation()));

        matrices.translate(0.0,
            ItemPhysicsMath.groundOffsetY(sideLit),
            ItemPhysicsMath.groundOffsetZ(sideLit, physics.linkenvisuals$hasAdditionalOffset(),
                state.uniqueOffset));
        if (hasDepth) {
            matrices.translate(0.0, 0.2f, 0.0);
        }
        matrices.multiply(RotationAxis.POSITIVE_Y.rotation(module.xRotation(state, physics)));
        if (hasDepth) {
            matrices.translate(0.0, -0.2f, 0.0);
        }

        float scale = module.scaleValue();
        matrices.scale(scale, scale, scale);
        ItemEntityRenderer.render(matrices, queue, state.light, state, random, box);
        matrices.pop();

        // The shadow pieces are skipped together with the vanilla render pass,
        // matching the donor behavior (flat items do not cast the upright shadow).
        ci.cancel();
    }
}
