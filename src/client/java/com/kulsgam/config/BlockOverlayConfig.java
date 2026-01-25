package com.kulsgam.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import com.kulsgam.utils.enums.RenderMode;
import org.slf4j.Logger;

public class BlockOverlayConfig {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    public RenderMode renderMode = RenderMode.VANILLA;
    public boolean persistence = false;
    public boolean depthless = false;
    public boolean barriers = false;
    public boolean hidePlants = false;
    public double thickness = 2.0;
    public RenderSettings overlayRender = new RenderSettings("Overlay");
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
        if (overlayRender == null) {
            overlayRender = new RenderSettings("Overlay");
        }
        if (outlineRender == null) {
            outlineRender = new RenderSettings("Outline");
        }
        overlayRender.validate();
        outlineRender.validate();
    }

    private static double clamp(double value, double min, double max) {
        return Math.max(min, Math.min(max, value));
    }
}
