package dev.linkenvisuals.render.state;

/**
 * Duck interface implemented on {@code LivingEntityRenderState} by
 * {@code LivingEntityRenderStateMixin}. The render state is rebuilt every frame,
 * so the strength lives on the state (not on the entity) and is advanced once
 * per {@code updateRenderState} call.
 */
public interface HitColorRenderState {
    void linkenvisuals$setHitColorPlayer(boolean player);

    boolean linkenvisuals$isHitColorPlayer();

    void linkenvisuals$setHitColorStrength(float strength);

    float linkenvisuals$getHitColorStrength();
}
