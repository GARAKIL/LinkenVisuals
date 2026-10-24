package dev.linkenvisuals.mixin;

import net.minecraft.client.gui.screen.world.CreateWorldScreen;
import net.minecraft.client.gui.widget.TabNavigationWidget;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

/** Exposes the vanilla tab rail to the live autotest so all three tabs can be captured. */
@Mixin(CreateWorldScreen.class)
public interface CreateWorldScreenAccessor {
    @Accessor("tabNavigation")
    TabNavigationWidget linkenvisuals$tabNavigation();
}
