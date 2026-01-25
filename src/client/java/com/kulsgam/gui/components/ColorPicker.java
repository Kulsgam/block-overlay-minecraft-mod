package com.kulsgam.gui.components;

import java.awt.Color;
import com.kulsgam.utils.GuiUtils;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.gui.Click;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.gui.screen.narration.NarrationPart;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.texture.NativeImageBackedTexture;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;

import java.util.function.IntConsumer;

public class ColorPicker extends ClickableWidget {
    private static final int SIZE = 200;

    private final MinecraftClient client;
    private NativeImage backgroundImage;
    private NativeImageBackedTexture texture;
    private final Identifier textureId;
    private final IntConsumer onChange;
    private boolean dragging;
    private int selectorX;
    private int selectorY;
    private int color;

    private static final Identifier COLOR_PICKER_TEXTURE_ID =
            Identifier.of("kulsgam", "blockoverlay_color_picker");

    public ColorPicker(int x, int y, int width, int height, Color hue, IntConsumer onChange) {
        super(x, y, width, height, Text.literal(""));
        this.client = MinecraftClient.getInstance();
        this.onChange = onChange;

        this.textureId = COLOR_PICKER_TEXTURE_ID;

        // start selector in top-left of widget by default (or center, your choice)
        this.selectorX = x;
        this.selectorY = y;

        setHue(hue);
    }

    @Override
    public void onClick(Click click, boolean doubled) {
        double mouseX = click.x();
        double mouseY = click.y();
        dragging = true;
        updateColor((int) mouseX, (int) mouseY);
    }

    @Override
    public void onRelease(Click click) {
        dragging = false;
    }

    @Override
    public void onDrag(Click click, double offsetX, double offsetY) {
        if (dragging) {
            double mouseX = click.x();
            double mouseY = click.y();
            updateColor((int) mouseX, (int) mouseY);
        }
    }

    @Override
    protected void renderWidget(DrawContext context, int mouseX, int mouseY, float delta) {
        if (!visible) {
            return;
        }
        if (textureId != null) {
            context.drawTexture(
                    RenderPipelines.GUI_TEXTURED,
                    textureId,
                    getX(),
                    getY(),
                    0.0f,
                    0.0f,
                    getWidth(),
                    getHeight(),
                    getWidth(),
                    getHeight()
            );
        }
        GuiUtils.drawHollowRect(context, selectorX, selectorY, selectorX + 5.0, selectorY + 5.0, 0.5, Color.BLACK.getRGB());
        GuiUtils.drawHollowRect(context, selectorX + 0.5, selectorY + 0.5, selectorX + 4.5, selectorY + 4.5, 0.5, Color.WHITE.getRGB());
        GuiUtils.drawHollowRect(context, getX(), getY(), getX() + getWidth(), getY() + getHeight(), 0.5, Color.BLACK.getRGB());
    }

    public int getColor() {
        return color;
    }

    public void setColor(int color) {
        float[] hsb = Color.RGBtoHSB((color >> 16) & 0xFF, (color >> 8) & 0xFF, color & 0xFF, null);
        selectorX = getX() + (int) (hsb[1] * getWidth());
        selectorY = getY() + (int) ((1.0f - hsb[2]) * getHeight());
        checkBounds();
        this.color = color;
    }

    public void setHue(Color hue) {
        backgroundImage = new NativeImage(SIZE, SIZE, false);
        for (int x = 0; x < SIZE; x++) {
            float saturation = (float) x / (float) SIZE;
            for (int y = 0; y < SIZE; y++) {
                float brightness = 1.0f - (float) y / (float) SIZE;
                int rgb = Color.getHSBColor(Color.RGBtoHSB(hue.getRed(), hue.getGreen(), hue.getBlue(), null)[0], saturation, brightness).getRGB();
                backgroundImage.setColor(x, y, toAbgr(rgb));
            }
        }
        if (texture == null) {
            texture = new NativeImageBackedTexture(
                    () -> "blockoverlay_color_picker",
                    backgroundImage
            );
            client.getTextureManager().registerTexture(textureId, texture);
        } else {
            texture.setImage(backgroundImage);
            texture.upload();
        }
    }

    private void updateColor(int mouseX, int mouseY) {
        // If we're not laid out yet, don't compute color.
        int w = getWidth();
        int h = getHeight();
        if (w <= 0 || h <= 0 || backgroundImage == null) {
            // Keep selector clamped to widget origin; color stays unchanged until first real render/click.
            selectorX = getX();
            selectorY = getY();
            return;
        }

        selectorX = mouseX - 3;
        selectorY = mouseY - 3;
        checkBounds();

        double scaleX = (double) SIZE / (double) w;
        double scaleY = (double) SIZE / (double) h;

        int x = MathHelper.clamp((int) ((selectorX - getX()) * scaleX), 0, SIZE - 1);
        int y = MathHelper.clamp((int) ((selectorY - getY()) * scaleY), 0, SIZE - 1);

        color = fromAbgr(backgroundImage.getColorArgb(x, y));
        if (onChange != null) {
            onChange.accept(color);
        }
    }

    private void checkBounds() {
        if (selectorX < getX()) {
            selectorX = getX();
        } else if (selectorX > getX() + getWidth() - 5) {
            selectorX = getX() + getWidth() - 5;
        }
        if (selectorY < getY()) {
            selectorY = getY();
        } else if (selectorY > getY() + getHeight() - 5) {
            selectorY = getY() + getHeight() - 5;
        }
    }

    private int toAbgr(int argb) {
        return applyAbgr(argb);
    }

    private int fromAbgr(int abgr) {
        return applyAbgr(abgr);
    }

    private int applyAbgr(int abgr) {
        int a = (abgr >> 24) & 0xFF;
        int b = (abgr >> 16) & 0xFF;
        int g = (abgr >> 8) & 0xFF;
        int r = abgr & 0xFF;
        return (a << 24) | (r << 16) | (g << 8) | b;
    }

    @Override
    protected void appendClickableNarrations(NarrationMessageBuilder builder) {
        builder.put(NarrationPart.TITLE, getMessage());
    }
}
