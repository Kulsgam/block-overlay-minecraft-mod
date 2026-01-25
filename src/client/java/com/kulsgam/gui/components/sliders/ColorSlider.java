package com.kulsgam.gui.components.sliders;

import java.awt.Color;
import java.util.function.DoubleConsumer;
import net.minecraft.client.gui.DrawContext;

public class ColorSlider extends BaseSlider {
    public ColorSlider(int x, int y, int width, int height, String label, double min, double max, double value, DoubleConsumer onChange) {
        super(x, y, width, height, label, min, max, value, onChange);
    }

    @Override
    protected void drawBackground(DrawContext context) {
        for (int i = 0; i < getWidth(); i++) {
            float hue = (float) i / (float) getWidth();
            int rgb = Color.getHSBColor(hue, 1.0f, 1.0f).getRGB();
            context.fill(getX() + i, getY(), getX() + i + 1, getY() + getHeight(), rgb);
        }
    }

    public Color getColor() {
        return Color.getHSBColor((float) getValueInt() / 360.0f, 1.0f, 1.0f);
    }
}
