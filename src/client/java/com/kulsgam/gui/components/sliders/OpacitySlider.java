package com.kulsgam.gui.components.sliders;

import java.util.function.DoubleConsumer;
import com.kulsgam.utils.ColorUtils;
import net.minecraft.client.gui.DrawContext;

public class OpacitySlider extends BaseSlider {
    private int color;

    public OpacitySlider(int x, int y, int width, int height, String label, double min, double max, double value, DoubleConsumer onChange) {
        super(x, y, width, height, label, min, max, value, onChange);
    }

    public void setColor(int color) {
        this.color = color;
    }

    @Override
    protected void drawBackground(DrawContext context) {
        for (int i = 0; i < getWidth(); i++) {
            double alpha = (double) i / (double) getWidth();
            int rgb = ColorUtils.setAlpha(color, alpha);
            context.fill(getX() + i, getY(), getX() + i + 1, getY() + getHeight(), rgb);
        }
    }

    @Override
    protected String getDisplayValue() {
        return String.format("%.2f", getValue());
    }
}
