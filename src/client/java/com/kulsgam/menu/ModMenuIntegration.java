package com.kulsgam.menu;

import com.kulsgam.BlockOverlayClient;
import com.kulsgam.gui.BlockOverlayScreen;
import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import net.minecraft.client.gui.screen.Screen;

public class ModMenuIntegration implements ModMenuApi {
    @Override
    public ConfigScreenFactory<Screen> getModConfigScreenFactory() {
        return parent -> new BlockOverlayScreen(BlockOverlayClient.instance.getConfig());
    }
}
