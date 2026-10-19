package dev.linkenvisuals.hands;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import dev.linkenvisuals.util.AtomicFiles;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.util.Arm;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

public final class HandPose {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path FILE =
        FabricLoader.getInstance().getConfigDir().resolve("linkenvisuals_hands.json");

    private static Stored stored = new Stored();

    private HandPose() {
    }

    public static float x(Arm arm) {
        return arm == Arm.RIGHT ? stored.rightX : stored.leftX;
    }

    public static float y(Arm arm) {
        return arm == Arm.RIGHT ? stored.rightY : stored.leftY;
    }

    public static float z(Arm arm) {
        return arm == Arm.RIGHT ? stored.rightZ : stored.leftZ;
    }

    public static float scale(Arm arm) {
        return arm == Arm.RIGHT ? stored.rightScaleX : stored.leftScaleX;
    }

    public static float scaleX(Arm arm) {
        return arm == Arm.RIGHT ? stored.rightScaleX : stored.leftScaleX;
    }

    public static float scaleY(Arm arm) {
        return arm == Arm.RIGHT ? stored.rightScaleY : stored.leftScaleY;
    }

    public static float scaleZ(Arm arm) {
        return arm == Arm.RIGHT ? stored.rightScaleZ : stored.leftScaleZ;
    }

    public static boolean moved(Arm arm) {
        return Math.abs(x(arm)) > 0.0001f || Math.abs(y(arm)) > 0.0001f
            || Math.abs(z(arm)) > 0.0001f || Math.abs(scaleX(arm) - 1.0f) > 0.0001f
            || Math.abs(scaleY(arm) - 1.0f) > 0.0001f || Math.abs(scaleZ(arm) - 1.0f) > 0.0001f;
    }

    public static void move(Arm arm, float dx, float dy, float dz) {
        if (arm == Arm.RIGHT) {
            stored.rightX = clampReach(stored.rightX + dx);
            stored.rightY = clampReach(stored.rightY + dy);
            stored.rightZ = clampDepth(stored.rightZ + dz);
        } else {
            stored.leftX = clampReach(stored.leftX + dx);
            stored.leftY = clampReach(stored.leftY + dy);
            stored.leftZ = clampDepth(stored.leftZ + dz);
        }
    }

    public static void resize(Arm arm, float amount) {
        if (arm == Arm.RIGHT) {
            stored.rightScaleX = clampScale(stored.rightScaleX * amount);
            stored.rightScaleY = clampScale(stored.rightScaleY * amount);
            stored.rightScaleZ = clampScale(stored.rightScaleZ * amount);
        } else {
            stored.leftScaleX = clampScale(stored.leftScaleX * amount);
            stored.leftScaleY = clampScale(stored.leftScaleY * amount);
            stored.leftScaleZ = clampScale(stored.leftScaleZ * amount);
        }
    }

    public static void resizeAxis(Arm arm, int axis, float amount) {
        if (arm == Arm.RIGHT) {
            if (axis == 0) stored.rightScaleX = clampScale(stored.rightScaleX * amount);
            else if (axis == 1) stored.rightScaleY = clampScale(stored.rightScaleY * amount);
            else stored.rightScaleZ = clampScale(stored.rightScaleZ * amount);
        } else {
            if (axis == 0) stored.leftScaleX = clampScale(stored.leftScaleX * amount);
            else if (axis == 1) stored.leftScaleY = clampScale(stored.leftScaleY * amount);
            else stored.leftScaleZ = clampScale(stored.leftScaleZ * amount);
        }
    }

    public static void reset(Arm arm) {
        if (arm == Arm.RIGHT) {
            stored.rightX = 0.0f;
            stored.rightY = 0.0f;
            stored.rightZ = 0.0f;
            stored.rightScaleX = 1.0f;
            stored.rightScaleY = 1.0f;
            stored.rightScaleZ = 1.0f;
        } else {
            stored.leftX = 0.0f;
            stored.leftY = 0.0f;
            stored.leftZ = 0.0f;
            stored.leftScaleX = 1.0f;
            stored.leftScaleY = 1.0f;
            stored.leftScaleZ = 1.0f;
        }
    }

    private static float clampReach(float value) {
        return HandPoseMath.clampReach(value);
    }

    private static float clampDepth(float value) {
        return HandPoseMath.clampDepth(value);
    }

    private static float clampScale(float value) {
        return HandPoseMath.clampScale(value);
    }

    public static void load() {
        if (!Files.isRegularFile(FILE)) {
            return;
        }
        try (Reader reader = Files.newBufferedReader(FILE, StandardCharsets.UTF_8)) {
            Stored read = GSON.fromJson(reader, Stored.class);
            if (read != null) {
                stored = read;

                stored.rightX = clampReach(stored.rightX);
                stored.rightY = clampReach(stored.rightY);
                stored.rightZ = clampDepth(stored.rightZ);
                stored.rightScaleX = clampScale(stored.rightScaleX);
                stored.rightScaleY = clampScale(stored.rightScaleY);
                stored.rightScaleZ = clampScale(stored.rightScaleZ);
                stored.leftX = clampReach(stored.leftX);
                stored.leftY = clampReach(stored.leftY);
                stored.leftZ = clampDepth(stored.leftZ);
                stored.leftScaleX = clampScale(stored.leftScaleX);
                stored.leftScaleY = clampScale(stored.leftScaleY);
                stored.leftScaleZ = clampScale(stored.leftScaleZ);
                // Migrate old uniform scale.
                if (stored.rightScale != 1.0f) {
                    stored.rightScaleX = stored.rightScaleY = stored.rightScaleZ = clampScale(stored.rightScale);
                    stored.rightScale = 1.0f;
                }
                if (stored.leftScale != 1.0f) {
                    stored.leftScaleX = stored.leftScaleY = stored.leftScaleZ = clampScale(stored.leftScale);
                    stored.leftScale = 1.0f;
                }
            }
        } catch (IOException | RuntimeException e) {
            dev.linkenvisuals.LinkenVisuals.LOGGER.warn("Failed to load hand pose, using defaults: {}", e.getMessage());
        }
    }

    public static void mirror() {
        stored.leftX = -stored.rightX;
        stored.leftY = stored.rightY;
        stored.leftZ = stored.rightZ;
        stored.leftScaleX = stored.rightScaleX;
        stored.leftScaleY = stored.rightScaleY;
        stored.leftScaleZ = stored.rightScaleZ;
    }

    public static void save() {
        try (Writer writer = AtomicFiles.openWriter(FILE)) {
            GSON.toJson(stored, writer);
        } catch (IOException | RuntimeException e) {
            dev.linkenvisuals.LinkenVisuals.LOGGER.error("Failed to save hand pose: {}", e.getMessage());
        }
    }

    private static final class Stored {
        float rightX;
        float rightY;
        float rightZ;
        float rightScale = 1.0f; // legacy, migrated to per-axis
        float rightScaleX = 1.0f;
        float rightScaleY = 1.0f;
        float rightScaleZ = 1.0f;
        float leftX;
        float leftY;
        float leftZ;
        float leftScale = 1.0f; // legacy, migrated to per-axis
        float leftScaleX = 1.0f;
        float leftScaleY = 1.0f;
        float leftScaleZ = 1.0f;
    }
}
