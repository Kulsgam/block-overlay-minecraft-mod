package com.kulsgam.config;

import com.kulsgam.utils.ColorUtils;
import com.kulsgam.utils.EnumUtils;
import com.kulsgam.utils.enums.ColorMode;

public class RenderSettings {
    public final String name;
    public ColorSetting staticColor = new ColorSetting();
    public ColorSetting gradientStartColor = new ColorSetting();
    public ColorSetting gradientEndColor = new ColorSetting();
    public ColorSetting fadeStartColor = new ColorSetting();
    public ColorSetting fadeEndColor = new ColorSetting();
    public double chromaOpacity = 1.0;
    public ColorMode colorMode = ColorMode.STATIC;
    public boolean visible = true;
    public double fadeSpeed = 5.5;
    public double chromaSpeed = 5.5;

    public RenderSettings(String name) {
        this.name = name;
    }

    public int getHue(int index) {
        return switch (EnumUtils.fromName(ColorMode.class, colorMode.name())) {
            case STATIC -> staticColor.hue;
            case GRADIENT -> (index == 0 ? gradientStartColor.hue : gradientEndColor.hue);
            case FADE -> (index == 0 ? fadeStartColor.hue : fadeEndColor.hue);
            case CHROMA -> 0;
        };
    }

    public void setHue(int index, int hue) {
        switch (EnumUtils.fromName(ColorMode.class, colorMode.name())) {
            case STATIC -> staticColor.hue = hue;
            case GRADIENT -> {
                if (index == 0) {
                    gradientStartColor.hue = hue;
                } else {
                    gradientEndColor.hue = hue;
                }
            }
            case FADE -> {
                if (index == 0) {
                    fadeStartColor.hue = hue;
                } else {
                    fadeEndColor.hue = hue;
                }
            }
            case CHROMA -> {
            }
        }
    }

    public int getColor(int index) {
        return switch (EnumUtils.fromName(ColorMode.class, colorMode.name())) {
            case STATIC -> staticColor.rgb;
            case GRADIENT -> (index == 0 ? gradientStartColor.rgb : gradientEndColor.rgb);
            case FADE -> (index == 0 ? fadeStartColor.rgb : fadeEndColor.rgb);
            case CHROMA -> ColorUtils.getChroma(chromaSpeed);
        };
    }

    public void setColor(int index, int color) {
        switch (EnumUtils.fromName(ColorMode.class, colorMode.name())) {
            case STATIC -> staticColor.rgb = color;
            case GRADIENT -> {
                if (index == 0) {
                    gradientStartColor.rgb = color;
                } else {
                    gradientEndColor.rgb = color;
                }
            }
            case FADE -> {
                if (index == 0) {
                    fadeStartColor.rgb = color;
                } else {
                    fadeEndColor.rgb = color;
                }
            }
            case CHROMA -> {
            }
        }
    }

    public double getOpacity(int index) {
        return switch (EnumUtils.fromName(ColorMode.class, colorMode.name())) {
            case STATIC -> staticColor.opacity;
            case GRADIENT -> (index == 0 ? gradientStartColor.opacity : gradientEndColor.opacity);
            case FADE -> (index == 0 ? fadeStartColor.opacity : fadeEndColor.opacity);
            case CHROMA -> chromaOpacity;
        };
    }

    public void setOpacity(int index, double opacity) {
        switch (EnumUtils.fromName(ColorMode.class, colorMode.name())) {
            case STATIC -> staticColor.opacity = opacity;
            case GRADIENT -> {
                if (index == 0) {
                    gradientStartColor.opacity = opacity;
                } else {
                    gradientEndColor.opacity = opacity;
                }
            }
            case FADE -> {
                if (index == 0) {
                    fadeStartColor.opacity = opacity;
                } else {
                    fadeEndColor.opacity = opacity;
                }
            }
            case CHROMA -> chromaOpacity = opacity;
        }
    }

    public int getStart() {
        return switch (EnumUtils.fromName(ColorMode.class, colorMode.name())) {
            case STATIC -> staticColor.getColor();
            case GRADIENT -> gradientStartColor.getColor();
            case FADE -> {
                double percent = Math.sin(System.currentTimeMillis() / (1100.0 - fadeSpeed * 100.0)) * 0.5 + 0.5;
                yield ColorUtils.interpolate(fadeStartColor.getColor(), fadeEndColor.getColor(), percent);
            }
            case CHROMA -> ColorUtils.setAlpha(ColorUtils.getChroma(chromaSpeed), chromaOpacity);
        };
    }

    public int getEnd() {
        return switch (EnumUtils.fromName(ColorMode.class, colorMode.name())) {
            case STATIC, CHROMA -> getStart();
            case GRADIENT -> gradientEndColor.getColor();
            case FADE -> {
                double percent = Math.sin((System.currentTimeMillis() + 500L) / (1100.0 - fadeSpeed * 100.0)) * 0.5 + 0.5;
                yield ColorUtils.interpolate(fadeStartColor.getColor(), fadeEndColor.getColor(), percent);
            }
        };
    }

    void validate() {
        staticColor.validate();
        gradientStartColor.validate();
        gradientEndColor.validate();
        fadeStartColor.validate();
        fadeEndColor.validate();
        if (colorMode == null) {
            colorMode = ColorMode.STATIC;
        }
        if (chromaOpacity < 0.07) {
            chromaOpacity = 0.07;
        }
        if (chromaOpacity > 1.0) {
            chromaOpacity = 1.0;
        }
        fadeSpeed = clamp(fadeSpeed, 1.0, 10.0);
        chromaSpeed = clamp(chromaSpeed, 1.0, 10.0);
    }

    private static double clamp(double value, double min, double max) {
        return Math.max(min, Math.min(max, value));
    }
}
