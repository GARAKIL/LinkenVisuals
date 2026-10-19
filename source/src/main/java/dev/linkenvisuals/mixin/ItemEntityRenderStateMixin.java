package dev.linkenvisuals.mixin;

import dev.linkenvisuals.render.state.ItemPhysicsRenderState;
import net.minecraft.client.render.entity.state.ItemEntityRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(ItemEntityRenderState.class)
public abstract class ItemEntityRenderStateMixin implements ItemPhysicsRenderState {
    @Unique
    private boolean linkenvisuals$onGround;
    @Unique
    private float linkenvisuals$yRotation;
    @Unique
    private boolean linkenvisuals$additionalOffset;

    @Override
    public boolean linkenvisuals$isOnGround() {
        return linkenvisuals$onGround;
    }

    @Override
    public void linkenvisuals$setOnGround(boolean onGround) {
        linkenvisuals$onGround = onGround;
    }

    @Override
    public float linkenvisuals$getYRotation() {
        return linkenvisuals$yRotation;
    }

    @Override
    public void linkenvisuals$setYRotation(float rotation) {
        linkenvisuals$yRotation = rotation;
    }

    @Override
    public boolean linkenvisuals$hasAdditionalOffset() {
        return linkenvisuals$additionalOffset;
    }

    @Override
    public void linkenvisuals$setAdditionalOffset(boolean additionalOffset) {
        linkenvisuals$additionalOffset = additionalOffset;
    }
}
