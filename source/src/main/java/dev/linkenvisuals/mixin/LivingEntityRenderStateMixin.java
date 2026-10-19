package dev.linkenvisuals.mixin;

import dev.linkenvisuals.render.state.HitColorRenderState;
import net.minecraft.client.render.entity.state.LivingEntityRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(LivingEntityRenderState.class)
public abstract class LivingEntityRenderStateMixin implements HitColorRenderState {
    @Unique
    private boolean linkenvisuals$hitColorPlayer;
    @Unique
    private float linkenvisuals$hitColorStrength;

    @Override
    public void linkenvisuals$setHitColorPlayer(boolean player) {
        linkenvisuals$hitColorPlayer = player;
    }

    @Override
    public boolean linkenvisuals$isHitColorPlayer() {
        return linkenvisuals$hitColorPlayer;
    }

    @Override
    public void linkenvisuals$setHitColorStrength(float strength) {
        linkenvisuals$hitColorStrength = strength;
    }

    @Override
    public float linkenvisuals$getHitColorStrength() {
        return linkenvisuals$hitColorStrength;
    }
}
