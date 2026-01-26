package com.kulsgam;

import com.kulsgam.config.BlockOverlayConfig;
import com.kulsgam.gui.BlockOverlayScreen;
import com.kulsgam.listeners.BlockOverlayListener;
import com.kulsgam.listeners.ShadersListener;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;

import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.literal;

public class BlockOverlayClient implements ClientModInitializer {
    public static final String MOD_ID = "block-overlay";
    public static final String NAME = "Block Overlay";
    public static final String VERSION = "1.0.0";

    public static BlockOverlayClient instance;

    private final Logger logger = LoggerFactory.getLogger(MOD_ID);
    private MinecraftClient client;
    private BlockOverlayConfig config;
    private ShadersListener shadersListener;
    private BlockOverlayListener blockOverlayListener;

    @Override
    public void onInitializeClient() {
        instance = this;
        client = MinecraftClient.getInstance();
        Path configPath = FabricLoader.getInstance().getConfigDir().resolve("blockoverlay.json");
        config = BlockOverlayConfig.load(configPath, logger);
        shadersListener = new ShadersListener(client, logger);
        blockOverlayListener = new BlockOverlayListener(client, config, shadersListener, logger);

        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) ->
                dispatcher.register(literal("blockoverlay").executes(context -> {
                    openScreen(new BlockOverlayScreen(config, blockOverlayListener));
                    return 1;
                }))
        );
    }

    private void openScreen(Screen screen) {
        client.execute(() -> client.setScreen(screen));
    }

    public Logger getLogger() {
        return logger;
    }

    public MinecraftClient getClient() {
        return client;
    }

    public BlockOverlayConfig getConfig() {
        return config;
    }

    public ShadersListener getShadersListener() {
        return shadersListener;
    }

    public BlockOverlayListener getBlockOverlayListener() {
        return blockOverlayListener;
    }
}
