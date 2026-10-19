package dev.linkenvisuals.mixin;

import dev.linkenvisuals.render.state.RudyRenderState;
import net.minecraft.client.render.entity.state.PlayerEntityRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(PlayerEntityRenderState.class)
public abstract class PlayerEntityRenderStateMixin implements RudyRenderState {
    @Unique
    private boolean linkenvisuals$rudy;

    @Override
    public boolean linkenvisuals$isRudy() {
        return linkenvisuals$rudy;
    }

    @Override
    public void linkenvisuals$setRudy(boolean rudy) {
        linkenvisuals$rudy = rudy;
    }
}
