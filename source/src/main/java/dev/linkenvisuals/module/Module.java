package dev.linkenvisuals.module;

import dev.linkenvisuals.clickgui.Category;
import dev.linkenvisuals.clickgui.ModuleInfo;
import dev.linkenvisuals.clickgui.setting.Setting;

import java.util.List;

/**
 * Object-based module (Phase 4+). Owns its {@link ModuleInfo} and its settings
 * as instance state. The info is built internally so the enable/disable
 * lifecycle fires on every user toggle path (ClickGUI, keybinds) — GUI code
 * toggles via {@code info().toggle()} directly.
 *
 * <p>New modules (Phase 5 donor ports) extend this. Legacy static modules in
 * {@link dev.linkenvisuals.clickgui.ModuleRegistry} are untouched.
 */
public abstract class Module {
    private final String id;
    private final ModuleInfo info;

    protected Module(String id, String name, String description, Category category,
                     List<Setting> leftSettings, List<Setting> rightSettings) {
        this.id = id;
        this.info = new ModuleInfo(name, description, category, leftSettings, rightSettings, this::handleToggle);
    }

    private void handleToggle(boolean enabled) {
        if (enabled) {
            onEnable();
        } else {
            onDisable();
        }
    }

    /** Stable lookup/config id, e.g. {@code "waypoints"}. */
    public final String id() {
        return id;
    }

    /** GUI descriptor shared with the ClickGUI, keybinds and config serialization. */
    public final ModuleInfo info() {
        return info;
    }

    public final boolean enabled() {
        return info.enabled();
    }

    /**
     * Fired on user toggle only — not on silent config restore
     * ({@code info().toggleSilently(...)}), matching legacy module semantics.
     */
    protected void onEnable() {
    }

    /** Fired on user toggle only, see {@link #onEnable()}. */
    protected void onDisable() {
    }
}
