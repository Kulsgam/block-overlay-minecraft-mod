package com.kulsgam.gui.components.buttons;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.Text;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.Click;

import java.util.Arrays;
import java.util.function.IntConsumer;

public class MenuButton extends TooltipButton {
    private final String label;
    private final String[] options;
    private final IntConsumer onChange;
    private int index;

    public MenuButton(int x, int y, int width, int height, String label, int index, String[] options, IntConsumer onChange, String... tooltips) {
        super(x, y, width, height, Text.literal(label + options[index]), Arrays.stream(tooltips)
                .map(s -> (Text) Text.literal(s))
                .toList());
        this.label = label;
        this.options = options;
        this.index = index;
        this.onChange = onChange;
    }

    @Override
    public void onClick(Click click, boolean doubled) {
        index = (index + 1) % options.length;
        setMessage(Text.literal(label + options[index]));
        if (onChange != null) {
            onChange.accept(index);
        }
    }

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
        setMessage(Text.literal(label + options[index]));
    }

    @Override
    protected void renderWidget(DrawContext context, int mouseX, int mouseY, float delta) {
        int color = this.isHovered() ? 0xFFAAAAAA : 0xFF888888;
        context.fill(getX(), getY(), getX() + getWidth(), getY() + getHeight(), color);
        int textColor = 0xFFFFFFFF;
        context.drawCenteredTextWithShadow(MinecraftClient.getInstance() .textRenderer, getMessage(), getX() + getWidth() / 2, getY() + (getHeight() - 8) / 2, textColor);
    }
}
