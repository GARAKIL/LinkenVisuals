package dev.linkenvisuals.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import dev.linkenvisuals.module.impl.OptimizationModule;
import net.minecraft.client.render.Frustum;
import net.minecraft.client.render.entity.EntityRenderManager;
import net.minecraft.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(EntityRenderManager.class)
public abstract class OptimizationEntityCullMixin {
    @ModifyReturnValue(method = "shouldRender", at = @At("RETURN"))
    private boolean linkenvisuals$occlusionCull(boolean original, Entity entity, Frustum frustum,
                                                double camX, double camY, double camZ) {
        return OptimizationModule.shouldRenderEntity(original, entity, camX, camY, camZ);
    }
}
