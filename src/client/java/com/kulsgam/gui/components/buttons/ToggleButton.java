package com.kulsgam.gui.components.buttons;

import net.minecraft.client.gui.Click;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.Text;
import net.minecraft.client.MinecraftClient;

import java.util.Arrays;
import java.util.function.Consumer;

public class ToggleButton extends TooltipButton {
    private boolean value;
    private final String label;
    private final Consumer<Boolean> onToggle;

    public ToggleButton(int x, int y, int width, int height, boolean value, String label, Consumer<Boolean> onToggle, String... tooltips) {
        super(x, y, width, height, Text.literal(label), Arrays.stream(tooltips)
                .map(s -> (Text) Text.literal(s))
                .toList());
        this.value = value;
        this.label = label;
        this.onToggle = onToggle;
        updateMessage();
    }

    @Override
    public void onClick(Click click, boolean doubled) {
        value = !value;
        updateMessage();
        if (onToggle != null) {
            onToggle.accept(value);
        }
    }

    public boolean getValue() {
        return value;
    }

    public void setValue(boolean value) {
        this.value = value;
        updateMessage();
    }

    private void updateMessage() {
        setMessage(Text.literal(label + ": " + (value ? "On" : "Off")));
    }

    @Override
    protected void renderWidget(DrawContext context, int mouseX, int mouseY, float delta) {
        int color = value ? 0xFF4CAF50 : 0xFFB71C1C;
        if (this.isHovered()) {
            color = value ? 0xFF66BB6A : 0xFFEF5350;
        }
        context.fill(getX(), getY(), getX() + getWidth(), getY() + getHeight(), color);
        context.drawCenteredTextWithShadow(MinecraftClient.getInstance().textRenderer, getMessage(), getX() + getWidth() / 2, getY() + (getHeight() - 8) / 2, 0xFFFFFFFF);
    }
}
