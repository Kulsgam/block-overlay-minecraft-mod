package com.kulsgam.gui.components.sliders;

import com.kulsgam.utils.GuiUtils;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.Click;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.gui.screen.narration.NarrationPart;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.text.Text;

import java.awt.Color;
import java.util.function.DoubleConsumer;

public class BaseSlider extends ClickableWidget {
    protected final double min;
    protected final double max;
    protected double value;
    protected boolean dragging;
    private final String label;
    private final DoubleConsumer onChange;

    public BaseSlider(int x, int y, int width, int height, String label, double min, double max, double value, DoubleConsumer onChange) {
        super(x, y, width, height, Text.literal(label));
        this.label = label;
        this.min = min;
        this.max = max;
        this.value = value;
        this.onChange = onChange;
    }

    public double getValue() {
        return value;
    }

    public int getValueInt() {
        return (int) Math.round(value);
    }

    public void setValue(double value) {
        this.value = clamp(value);
    }

    @Override
    public void onClick(Click click, boolean doubled) {
        updateValue(click.x());
        dragging = true;
    }

    @Override
    public void onRelease(Click click) {
        dragging = false;
    }

    @Override
    public void onDrag(Click click, double offsetX, double offsetY) {
        if (dragging) {
            updateValue(click.x());
        }
    }

    protected void updateValue(double mouseX) {
        double percent = (mouseX - (double) getX() - 4.0) / ((double) getWidth() - 8.0);
        percent = Math.max(0.0, Math.min(1.0, percent));
        value = min + (max - min) * percent;
        if (onChange != null) {
            onChange.accept(value);
        }
    }

    @Override
    protected void renderWidget(DrawContext context, int mouseX, int mouseY, float delta) {
        if (!this.visible) {
            return;
        }
        drawBackground(context);
        int knobX = getX() + (int) ((value - min) / (max - min) * ((double) getWidth() - 8.0));
        GuiUtils.drawHollowRect(context, knobX + 0.5, getY() + 0.5, knobX + 7.5, getY() + getHeight() - 0.5, 0.5, Color.BLACK.getRGB());
        GuiUtils.drawHollowRect(context, knobX + 1.0, getY() + 1.0, knobX + 7.0, getY() + getHeight() - 1.0, 0.5, Color.WHITE.getRGB());
        GuiUtils.drawHollowRect(context, getX(), getY(), getX() + getWidth(), getY() + getHeight(), 0.5, Color.BLACK.getRGB());
        context.drawCenteredTextWithShadow(MinecraftClient.getInstance().textRenderer, Text.literal(label + ": " + getDisplayValue()), getX() + getWidth() / 2, getY() + (getHeight() - 8) / 2, 0xFFFFFFFF);
    }

    protected String getDisplayValue() {
        return String.format("%.2f", value);
    }

    protected void drawBackground(DrawContext context) {
        context.fill(getX(), getY(), getX() + getWidth(), getY() + getHeight(), Color.GRAY.getRGB());
    }

    private double clamp(double value) {
        return Math.max(min, Math.min(max, value));
    }

    @Override
    protected void appendClickableNarrations(NarrationMessageBuilder builder) {
        builder.put(NarrationPart.TITLE, getMessage());
    }
}
