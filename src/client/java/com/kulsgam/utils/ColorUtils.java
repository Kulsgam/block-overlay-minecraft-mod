package com.kulsgam.utils;

import java.awt.*;


public class ColorUtils {
    public static int setAlpha(int rgb, double alpha) {
        return rgb & 0xFFFFFF | (int) (alpha * 255.0) << 24;
    }

    public static int getChroma(double step) {
        double time = System.currentTimeMillis() % (18000.0 / step) / (18000.0 / step);
        return Color.getHSBColor((float) time, 1.0f, 1.0f).getRGB();
    }

    public static int interpolate(int first, int second, double percent) {
        Color color = new Color(first, true);
        Color color1 = new Color(second, true);
        double percent1 = 1.0 - percent;
        int red = (int) (color.getRed() * percent + color1.getRed() * percent1);
        int green = (int) (color.getGreen() * percent + color1.getGreen() * percent1);
        int blue = (int) (color.getBlue() * percent + color1.getBlue() * percent1);
        int alpha = (int) (color.getAlpha() * percent + color1.getAlpha() * percent1);
        return new Color(red, green, blue, alpha).getRGB();
    }
}
