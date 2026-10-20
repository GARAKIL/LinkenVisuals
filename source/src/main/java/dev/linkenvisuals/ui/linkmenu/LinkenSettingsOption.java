package dev.linkenvisuals.ui.linkmenu;

import java.util.function.BooleanSupplier;
import java.util.function.DoubleConsumer;
import java.util.function.DoubleSupplier;
import java.util.function.Supplier;

/**
 * One row in a LINKEN settings sub-page. Values are read and written through
 * the vanilla SimpleOption objects, so the screen owns presentation only.
 */
public final class LinkenSettingsOption {
    public enum Kind {
        TOGGLE, SLIDER, CYCLE, ACTION, DISPLAY
    }

    private final String label;
    private final Kind kind;
    private final Supplier<String> value;
    private final BooleanSupplier booleanValue;
    private final Runnable toggle;
    private final DoubleSupplier fraction;
    private final DoubleConsumer setFraction;
    private final Runnable cycle;
    private final Runnable action;

    private LinkenSettingsOption(String label, Kind kind, Supplier<String> value,
                                 BooleanSupplier booleanValue, Runnable toggle,
                                 DoubleSupplier fraction, DoubleConsumer setFraction,
                                 Runnable cycle, Runnable action) {
        this.label = label;
        this.kind = kind;
        this.value = value;
        this.booleanValue = booleanValue;
        this.toggle = toggle;
        this.fraction = fraction;
        this.setFraction = setFraction;
        this.cycle = cycle;
        this.action = action;
    }

    public static LinkenSettingsOption toggle(String label, BooleanSupplier get, Runnable toggle) {
        return new LinkenSettingsOption(label, Kind.TOGGLE,
            () -> get.getAsBoolean() ? "ON" : "OFF", get, toggle, null, null, null, null);
    }

    public static LinkenSettingsOption slider(String label, Supplier<String> value,
                                              DoubleSupplier fraction, DoubleConsumer setFraction) {
        return new LinkenSettingsOption(label, Kind.SLIDER, value, null, null,
            fraction, setFraction, null, null);
    }

    public static LinkenSettingsOption cycle(String label, Supplier<String> value, Runnable cycle) {
        return new LinkenSettingsOption(label, Kind.CYCLE, value, null, null,
            null, null, cycle, null);
    }

    public static LinkenSettingsOption action(String label, Supplier<String> value, Runnable action) {
        return new LinkenSettingsOption(label, Kind.ACTION, value, null, null,
            null, null, null, action);
    }

    public static LinkenSettingsOption display(String label, Supplier<String> value) {
        return new LinkenSettingsOption(label, Kind.DISPLAY, value, null, null,
            null, null, null, null);
    }

    public String label() {
        return label;
    }

    public Kind kind() {
        return kind;
    }

    public String value() {
        return value == null ? "" : value.get();
    }

    public boolean booleanValue() {
        return booleanValue != null && booleanValue.getAsBoolean();
    }

    public double fraction() {
        return fraction == null ? 0.0 : Math.max(0.0, Math.min(1.0, fraction.getAsDouble()));
    }

    public void toggle() {
        if (toggle != null) {
            toggle.run();
        }
    }

    public void setFraction(double value) {
        if (setFraction != null) {
            setFraction.accept(Math.max(0.0, Math.min(1.0, value)));
        }
    }

    public void cycle() {
        if (cycle != null) {
            cycle.run();
        }
    }

    public void run() {
        if (action != null) {
            action.run();
        }
    }
}
