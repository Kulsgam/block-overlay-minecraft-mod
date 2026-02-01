package com.kulsgam.config;

import java.awt.Color;

import com.kulsgam.BlockOverlayClient;
import com.kulsgam.utils.ColorUtils;

public class ColorSetting {
    public int hue = 0;
    public int rgb = Color.WHITE.getRGB();
    public double opacity = 1.0;

    public int getColor(boolean shaderEnabled) {
        return ColorUtils.setAlpha(rgb, shaderEnabled ? opacity * BlockOverlayClient.shaderOpacityMultiplier : opacity);
    }

    void validate() {
        hue = Math.max(0, Math.min(360, hue));
        if (opacity < 0.07) {
            opacity = 0.07;
        }
        if (opacity > 1.0) {
            opacity = 1.0;
        }
    }
}
