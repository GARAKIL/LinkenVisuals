package dev.linkenvisuals.mixin;

import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

/** Exposes the vanilla placeholder text so the LINKEN field skin can render it. */
@Mixin(TextFieldWidget.class)
public interface TextFieldWidgetAccessor {
    @Accessor("placeholder")
    Text linkenvisuals$placeholder();
}
