package dev.linkenvisuals.clickgui;

import dev.linkenvisuals.clickgui.setting.SliderSetting;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class SliderSettingTest {

    @Test
    void integerStepsFormatWithoutFraction() {
        SliderSetting slider = new SliderSetting("Radius", 0.0f, 10.0f, 1.0f, 4.0f, null);

        assertEquals("4", slider.format(4.0f));
        assertEquals("4", slider.format(4.4f));
    }

    @Test
    void fractionalStepsUseRootLocaleAndSuffix() {
        SliderSetting slider = new SliderSetting("Opacity", 0.0f, 100.0f, 0.05f, 80.0f, "%");

        assertEquals("4.50%", slider.format(4.5f));
        assertEquals("80.00%", slider.display());
    }

    @Test
    void setFractionClampsAndQuantizes() {
        SliderSetting slider = new SliderSetting("Radius", 0.0f, 10.0f, 1.0f, 4.0f, null);

        slider.setFraction(2.0f);
        assertEquals(10.0f, slider.value(), 1.0e-4f);
        slider.setFraction(-1.0f);
        assertEquals(0.0f, slider.value(), 1.0e-4f);
    }
}
