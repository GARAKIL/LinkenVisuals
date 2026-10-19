package dev.linkenvisuals.render.state;

/** Duck interface implemented on {@code ItemEntityRenderState} by its mixin. */
public interface ItemPhysicsRenderState {
    boolean linkenvisuals$isOnGround();

    void linkenvisuals$setOnGround(boolean onGround);

    float linkenvisuals$getYRotation();

    void linkenvisuals$setYRotation(float rotation);

    boolean linkenvisuals$hasAdditionalOffset();

    void linkenvisuals$setAdditionalOffset(boolean additionalOffset);
}
