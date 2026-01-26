package com.kulsgam.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import com.kulsgam.utils.enums.ColorMode;
import com.kulsgam.utils.enums.RenderMode;
import org.slf4j.Logger;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;

public class BlockOverlayConfig {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    public RenderMode renderMode = RenderMode.VANILLA;
    public boolean persistence = false;
    public boolean depthless = false;
    public boolean barriers = false;
    public boolean hidePlants = false;
    public double thickness = 2.0;
    public RenderSettings fillRender = new RenderSettings("Fill");
    public RenderSettings outlineRender = new RenderSettings("Outline");

    private transient Path path;

    public static BlockOverlayConfig load(Path path, Logger logger) {
        BlockOverlayConfig config = null;
        if (Files.exists(path)) {
            try (Reader reader = Files.newBufferedReader(path)) {
                config = GSON.fromJson(reader, BlockOverlayConfig.class);
            } catch (IOException | JsonSyntaxException exception) {
                logger.error("Failed to read config", exception);
            }
        }
        if (config == null) {
            config = new BlockOverlayConfig();
        }
        config.path = path;
        config.validate();
        config.save(logger);
        return config;
    }

    private static double clamp(double value, double min, double max) {
        return Math.max(min, Math.min(max, value));
    }

    public boolean isFillEnabled() {
        return fillRender.visible;
    }

    public boolean isFillChromaEnabled() {
        return fillRender.colorMode == ColorMode.CHROMA;
    }

    public double getFillChromaSpeed() {
        return fillRender.chromaSpeed;
    }

    public double getFillOpacity(int index) {
        return fillRender.getOpacity(index);
    }

    public int getFillColor(int index) {
        return fillRender.getColor(index);
    }

    public boolean isOutlineChromaEnabled() {
        return outlineRender.colorMode == ColorMode.CHROMA;
    }

    public double getOutlineChromaSpeed() {
        return outlineRender.chromaSpeed;
    }

    public double getOutlineOpacity(int index) {
        return outlineRender.getOpacity(index);
    }

    public double getOutlineWidth() {
        return thickness;
    }

    public int getOutlineColor(int index) {
        return outlineRender.getColor(index);
    }

    public void save(Logger logger) {
        if (path == null) {
            return;
        }
        try {
            Files.createDirectories(path.getParent());
        } catch (IOException exception) {
            logger.error("Failed to create config directory", exception);
            return;
        }
        try (Writer writer = Files.newBufferedWriter(path)) {
            GSON.toJson(this, writer);
        } catch (IOException exception) {
            logger.error("Failed to save config", exception);
        }
    }

    private void validate() {
        if (renderMode == null) {
            renderMode = RenderMode.VANILLA;
        }
        thickness = clamp(thickness, 1.0, 10.0);
        if (fillRender == null) {
            fillRender = new RenderSettings("Fill");
        }
        if (outlineRender == null) {
            outlineRender = new RenderSettings("Outline");
        }
        fillRender.validate();
        outlineRender.validate();
    }
}
