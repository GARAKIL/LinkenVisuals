package dev.linkenvisuals.mixin;

import dev.linkenvisuals.i18n.I18n;
import dev.linkenvisuals.ui.editor.MainMenuEditorScreen;
import dev.linkenvisuals.ui.style.MenuStyle;
import dev.linkenvisuals.ui.style.StyleManager;
import dev.linkenvisuals.ui.style.StyleRouting;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.option.GameOptionsScreen;
import net.minecraft.client.gui.screen.option.VideoOptionsScreen;
import net.minecraft.client.gui.tooltip.Tooltip;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.DirectionalLayoutWidget;
import net.minecraft.client.gui.widget.ThreePartsLayoutWidget;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GameOptionsScreen.class)
public abstract class GameOptionsScreenMixin extends Screen {
    @Shadow @Final public ThreePartsLayoutWidget layout;

    protected GameOptionsScreenMixin(Text title) {
        super(title);
    }

    @Inject(method = "initFooter", at = @At("HEAD"), cancellable = true)
    private void linkenvisuals$addVideoFooter(CallbackInfo ci) {
        if (!((Object) this instanceof VideoOptionsScreen)) {
            return;
        }

        ci.cancel();
        this.layout.setFooterHeight(60);

        DirectionalLayoutWidget footerLayout = DirectionalLayoutWidget.vertical();
        footerLayout.spacing(4);

        DirectionalLayoutWidget row1 = DirectionalLayoutWidget.horizontal();
        row1.spacing(8);

        // 1. Cycle UI style: Beta -> Vanilla -> Linken -> Beta.
        // StyleManager owns the preset decision (LINKEN is fixed and keeps the editable config intact).
        ButtonWidget toggleBtn = ButtonWidget.builder(linkenvisuals$getToggleMessage(), btn -> {
            MenuStyle next = StyleRouting.nextStyle(StyleManager.getStyle());
            StyleManager.setStyle(next);
            btn.setMessage(linkenvisuals$getToggleMessage());
        }).width(150).build();
        toggleBtn.setTooltip(Tooltip.of(Text.literal(I18n.get("linkenvisuals.video.style_toggle_tooltip"))));
        row1.add(toggleBtn);

        // 2. Menu Settings & Style Editor button (Bright Green)
        ButtonWidget editorBtn = ButtonWidget.builder(
                Text.literal("★ " + I18n.get("linkenvisuals.video.editor_btn")).formatted(Formatting.GREEN),
                btn -> {
                    MinecraftClient client = MinecraftClient.getInstance();
                    if (client != null) {
                        client.setScreen(new MainMenuEditorScreen(this, false));
                    }
                }
        ).width(150).build();
        editorBtn.setTooltip(Tooltip.of(Text.literal(I18n.get("linkenvisuals.video.editor_tooltip"))));
        row1.add(editorBtn);

        footerLayout.add(row1, p -> p.alignHorizontalCenter());

        // 3. Centered Done button
        ButtonWidget doneBtn = ButtonWidget.builder(ScreenTexts.DONE, btn -> this.close()).width(200).build();
        footerLayout.add(doneBtn, p -> p.alignHorizontalCenter());

        this.layout.addFooter(footerLayout);
    }

    @Unique
    private static Text linkenvisuals$getToggleMessage() {
        MenuStyle style = StyleManager.getStyle();
        String key = switch (style) {
            case BETA -> "linkenvisuals.video.style_custom";
            case VANILLA -> "linkenvisuals.video.style_vanilla";
            case LINKEN -> "linkenvisuals.video.style_linken";
        };
        return Text.literal("★ " + I18n.get(key)).formatted(Formatting.GREEN);
    }
}
