package dev.linkenvisuals.render.state;

/**
 * Duck interface implemented on {@code PlayerEntityRenderState} by
 * {@code PlayerEntityRenderStateMixin}: the local player has the Rudy character selected.
 * The render state is rebuilt every frame, so the flag lives on the state (not on the entity).
 */
public interface RudyRenderState {
    boolean linkenvisuals$isRudy();

    void linkenvisuals$setRudy(boolean rudy);
}
