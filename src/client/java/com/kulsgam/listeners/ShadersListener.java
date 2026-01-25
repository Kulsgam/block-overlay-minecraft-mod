package com.kulsgam.listeners;

import net.minecraft.client.MinecraftClient;
import org.slf4j.Logger;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;

public class ShadersListener {
    private static final long CHECK_INTERVAL_MS = 2000L;

    private final MinecraftClient client;
    private final Logger logger;
    private boolean usingShaders;
    private long lastCheck;

    public ShadersListener(MinecraftClient client, Logger logger) {
        this.client = client;
        this.logger = logger;
        checkShaders();
    }

    public void tick() {
        long now = System.currentTimeMillis();
        if (now - lastCheck < CHECK_INTERVAL_MS) {
            return;
        }
        lastCheck = now;
        checkShaders();
    }

    private void checkShaders() {
        boolean shaders = false;
        File shadersConfig = new File(client.runDirectory, "optionsshaders.txt");
        if (shadersConfig.exists()) {
            try (BufferedReader reader = new BufferedReader(new FileReader(shadersConfig))) {
                String text;
                while ((text = reader.readLine()) != null) {
                    if (!text.startsWith("shaderPack=")) {
                        continue;
                    }
                    String[] split = text.split("=");
                    shaders = split.length > 1 && !split[1].equals("OFF");
                    break;
                }
            } catch (Exception exception) {
                logger.error("Shaders configuration", exception);
            }
        }
        usingShaders = shaders;
    }

    public boolean isUsingShaders() {
        return usingShaders;
    }
}
