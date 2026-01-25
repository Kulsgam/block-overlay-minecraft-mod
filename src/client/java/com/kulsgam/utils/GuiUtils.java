package com.kulsgam.utils;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.Text;

import java.util.List;

public class GuiUtils {
    public static void drawRect(DrawContext context, double left, double top, double right, double bottom, int color) {
        context.fill((int) left, (int) top, (int) right, (int) bottom, color);
    }

    public static void drawHollowRect(DrawContext context, double left, double top, double right, double bottom, double thickness, int color) {
        drawRect(context, left, top, left + thickness, bottom, color);
        drawRect(context, left + thickness, top, right - thickness, top + thickness, color);
        drawRect(context, right - thickness, top, right, bottom, color);
        drawRect(context, left + thickness, bottom - thickness, right - thickness, bottom, color);
    }

    public static void drawTooltip(DrawContext context, List<Text> textLines, int mouseX, int mouseY) {
        if (textLines.isEmpty()) {
            return;
        }
        TextRenderer textRenderer = MinecraftClient.getInstance().textRenderer;
        context.drawTooltip(textRenderer, textLines, mouseX, mouseY);
    }
}
