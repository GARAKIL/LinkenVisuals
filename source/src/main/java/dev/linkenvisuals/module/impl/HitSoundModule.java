package dev.linkenvisuals.module.impl;

import dev.linkenvisuals.clickgui.Category;
import dev.linkenvisuals.clickgui.setting.BooleanSetting;
import dev.linkenvisuals.clickgui.setting.ModeSetting;
import dev.linkenvisuals.clickgui.setting.Setting;
import dev.linkenvisuals.module.Module;

import java.util.List;

/**
 * HitSound (Phase 5.7): plays a sound on hit.
 * Donor's {@code HitSound} (102 lines) simplified.
 */
public final class HitSoundModule extends Module {
    public final ModeSetting sound;
    public final BooleanSetting onlyCrit;

    public HitSoundModule() {
        this(new ModeSetting("Sound", List.of("Bell", "Click", "Pop"), 0),
             new BooleanSetting("Only Crit", false));
    }

    private HitSoundModule(ModeSetting sound, BooleanSetting onlyCrit) {
        super("hitsound", "Hit Sound", "sound on hit",
            Category.VISUAL,
            List.of(),
            List.<Setting>of(sound, onlyCrit));
        this.sound = sound;
        this.onlyCrit = onlyCrit;
    }
}
