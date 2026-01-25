package com.kulsgam.gui;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import com.kulsgam.BlockOverlayClient;
import com.kulsgam.config.BlockOverlayConfig;
import com.kulsgam.config.RenderSettings;
import com.kulsgam.gui.components.ColorPicker;
import com.kulsgam.gui.components.buttons.MenuButton;
import com.kulsgam.gui.components.buttons.ToggleButton;
import com.kulsgam.gui.components.sliders.BaseSlider;
import com.kulsgam.gui.components.sliders.ColorSlider;
import com.kulsgam.gui.components.sliders.OpacitySlider;
import com.kulsgam.listeners.BlockOverlayListener;
import com.kulsgam.utils.Animator;
import com.kulsgam.utils.ColorUtils;
import com.kulsgam.utils.EnumUtils;
import com.kulsgam.utils.GuiUtils;
import com.kulsgam.utils.enums.ColorMode;
import com.kulsgam.utils.enums.RenderMode;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.input.KeyInput;
import net.minecraft.text.Text;

public class BlockOverlayScreen extends Screen {
    private static int propertyRenderIndex;
    private static int gradientColorIndex;
    private static int fadeColorIndex;

    private final BlockOverlayConfig config;
    private final BlockOverlayListener blockOverlayListener;
    private final Animator animator = new Animator(350.0);
    private final List<ClickableWidget> leftComponents = new ArrayList<>();
    private final List<ClickableWidget> rightComponents = new ArrayList<>();

    private RenderSettings property;
    private ColorPicker colorPicker;
    private ColorSlider colorSlider;
    private OpacitySlider opacitySlider;
    private BaseSlider fadeSpeedSlider;
    private BaseSlider chromaSpeedSlider;
    private MenuButton gradientColorButton;
    private MenuButton fadeColorButton;
    private MenuButton colorModeButton;
    private MenuButton propertyButton;
    private BaseSlider thicknessSlider;
    private boolean closing;

    public BlockOverlayScreen(BlockOverlayConfig config, BlockOverlayListener blockOverlayListener) {
        super(Text.literal("Block Overlay"));
        this.config = config;
        this.blockOverlayListener = blockOverlayListener;
        this.property = propertyRenderIndex == 1 ? config.overlayRender : config.outlineRender;
        animator.reset();
    }

    @Override
    protected void init() {
        leftComponents.clear();
        rightComponents.clear();
        clearChildren();

        int x = 10;
        colorSlider = new ColorSlider(x, height / 2 + 2, 100, 20, "Hue", 0.0, 360.0, property.getHue(getColorIndex()),
            value -> updateColorSlider());
        leftComponents.add(colorSlider);

        colorPicker = new ColorPicker(x, height / 2 - 107, 100, 100, colorSlider.getColor(), value -> updateColorPicker());
        colorPicker.setColor(property.getColor(getColorIndex()));
        leftComponents.add(colorPicker);

        opacitySlider = new OpacitySlider(x, height / 2 + 30, 100, 20, "Opacity", 0.07, 1.0, property.getOpacity(getColorIndex()),
            value -> updateOpacitySlider());
        opacitySlider.setColor(colorPicker.getColor());
        leftComponents.add(opacitySlider);

        fadeSpeedSlider = new BaseSlider(x, height / 2 - 164, 100, 20, "Speed", 1.0, 10.0, property.fadeSpeed,
            value -> updateFadeSpeedSlider());
        leftComponents.add(fadeSpeedSlider);

        chromaSpeedSlider = new BaseSlider(x, height / 2 + 2, 100, 20, "Speed", 1.0, 10.0, property.chromaSpeed,
            value -> updateChromaSpeedSlider());
        leftComponents.add(chromaSpeedSlider);

        gradientColorButton = new MenuButton(x, height / 2 - 136, 100, 20,
            "Gradient: Color ", gradientColorIndex, new String[]{"1", "2"},
            index -> {
                gradientColorIndex = index;
                updateComponents();
            });
        leftComponents.add(gradientColorButton);

        fadeColorButton = new MenuButton(x, height / 2 - 136, 100, 20,
            "Fade: Color ", fadeColorIndex, new String[]{"1", "2"},
            index -> {
                fadeColorIndex = index;
                updateComponents();
            });
        leftComponents.add(fadeColorButton);

        colorModeButton = new MenuButton(x, height / 2 + 58, 100, 20, "Color: ",
            EnumUtils.getOrdinal(ColorMode.class, property.colorMode.name()),
            Arrays.stream(EnumUtils.getNames(ColorMode.class)).map(this::formatName).toArray(String[]::new),
            index -> {
                property.colorMode = ColorMode.values()[index];
                updateComponents();
            });
        leftComponents.add(colorModeButton);

        propertyButton = new MenuButton(x, height / 2 + 86, 100, 20, "Editing: ", propertyRenderIndex,
            new String[]{config.outlineRender.name, config.overlayRender.name},
            index -> {
                propertyRenderIndex = index;
                property = propertyRenderIndex == 1 ? config.overlayRender : config.outlineRender;
                fadeSpeedSlider.setValue(property.fadeSpeed);
                chromaSpeedSlider.setValue(property.chromaSpeed);
                colorModeButton.setIndex(EnumUtils.getOrdinal(ColorMode.class, property.colorMode.name()));
                updateComponents();
            });
        leftComponents.add(propertyButton);

        x = width - 110;
        MenuButton renderModeButton = new MenuButton(x, height / 2 - 96, 100, 20, "Render: ",
                EnumUtils.getOrdinal(RenderMode.class, config.renderMode.name()),
                Arrays.stream(EnumUtils.getNames(RenderMode.class)).map(this::formatName).toArray(String[]::new),
                index -> config.renderMode = RenderMode.values()[index]);
        rightComponents.add(renderModeButton);

        thicknessSlider = new BaseSlider(x, height / 2 - 68, 100, 20, "Outline Thickness", 1.0, 10.0,
            config.thickness, value -> updateThicknessSlider());
        rightComponents.add(thicknessSlider);

        ToggleButton outlineToggleButton = new ToggleButton(x, height / 2 - 40, 100, 20, config.outlineRender.visible,
                config.outlineRender.name, value -> config.outlineRender.visible = value, "Show", "the outline");
        rightComponents.add(outlineToggleButton);

        ToggleButton overlayToggleButton = new ToggleButton(x, height / 2 - 16, 100, 20, config.overlayRender.visible,
                config.overlayRender.name, value -> config.overlayRender.visible = value, "Show", "the overlay");
        rightComponents.add(overlayToggleButton);

        ToggleButton persistenceToggleButton = new ToggleButton(x, height / 2 + 8, 100, 20, config.persistence,
                "Persistent", value -> config.persistence = value, "Render in", "Adventure and", "Spectator mode");
        rightComponents.add(persistenceToggleButton);

        ToggleButton depthToggleButton = new ToggleButton(x, height / 2 + 32, 100, 20, config.depthless,
                "Depthless", value -> config.depthless = value, "Render", "without depth");
        rightComponents.add(depthToggleButton);

        ToggleButton barriersToggleButton = new ToggleButton(x, height / 2 + 56, 100, 20, config.barriers,
                "Barriers", value -> config.barriers = value, "Render on", "barrier blocks");
        rightComponents.add(barriersToggleButton);

        ToggleButton plantsToggleButton = new ToggleButton(x, height / 2 + 80, 100, 20, config.hidePlants,
                "Hide Plants", value -> config.hidePlants = value, "Prevent", "com/kulsgam/rendering", "on grass", "and flowers");
        rightComponents.add(plantsToggleButton);

        updateComponents();
        leftComponents.forEach(this::addDrawableChild);
        rightComponents.forEach(this::addDrawableChild);
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        float panelWidth = (float) (colorPicker.getWidth() + 20.0);
        float panelX = (float) animator.getValue(0.0, panelWidth, closing, false);

        context.getMatrices().pushMatrix();
        context.getMatrices().translate(-panelX, 0.0f);
        GuiUtils.drawRect(context, 0.0, 0.0, panelWidth, height, 0xBF000000);
        if (property.colorMode == ColorMode.CHROMA) {
            GuiUtils.drawRect(context, colorPicker.getX(), colorPicker.getY(),
                colorPicker.getX() + colorPicker.getWidth(),
                colorPicker.getY() + colorPicker.getHeight(),
                ColorUtils.getChroma(property.chromaSpeed));
            opacitySlider.setColor(ColorUtils.getChroma(property.chromaSpeed));
        }
        leftComponents.forEach(component -> component.render(context, mouseX, mouseY, delta));
        context.getMatrices().translate(panelX, 0.0f);
        context.getMatrices().translate(panelX, 0.0f);
        GuiUtils.drawRect(context, width - panelWidth, 0.0, width, height, 0xBF000000);
        rightComponents.forEach(component -> component.render(context, mouseX, mouseY, delta));
        context.getMatrices().translate(-panelX, 0.0f);
        context.getMatrices().scale(1.5f, 1.5f);
        double alpha = animator.getValue(0.1, 1.0, !closing, true);
        context.drawCenteredTextWithShadow(textRenderer, Text.literal("Block Overlay"), (int) ((width / 2.0) / 1.5),
            (int) ((height / 15.0) / 1.5), ColorUtils.setAlpha(Color.WHITE.getRGB(), alpha));
        context.getMatrices().popMatrix();

        if (closing && panelX == panelWidth) {
            MinecraftClient.getInstance().setScreen(null);
            blockOverlayListener.resetAnimation(true);
        }
    }

    @Override
    public boolean keyPressed(KeyInput keyInput) {
        if (keyInput.getKeycode() == 256 && !closing) {
            closing = true;
            animator.reset();
            blockOverlayListener.resetAnimation(true);
            return true;
        }
        return super.keyPressed(keyInput);
    }

    @Override
    public void removed() {
        config.save(BlockOverlayClient.instance.getLogger());
    }

    private String formatName(String name) {
        return name.substring(0, 1).toUpperCase(Locale.ROOT) + name.substring(1).toLowerCase(Locale.ROOT);
    }

    private void updateComponents() {
        colorSlider.setValue(property.getHue(getColorIndex()));
        colorPicker.setHue(colorSlider.getColor());
        colorPicker.setColor(property.getColor(getColorIndex()));
        opacitySlider.setValue(property.getOpacity(getColorIndex()));
        opacitySlider.setColor(colorPicker.getColor());
        gradientColorButton.setIndex(gradientColorIndex);
        fadeColorButton.setIndex(fadeColorIndex);
        colorModeButton.setIndex(EnumUtils.getOrdinal(ColorMode.class, property.colorMode.name()));
        propertyButton.setIndex(propertyRenderIndex);

        switch (property.colorMode) {
            case STATIC -> {
                colorPicker.visible = true;
                colorSlider.visible = true;
                fadeSpeedSlider.visible = false;
                chromaSpeedSlider.visible = false;
                gradientColorButton.visible = false;
                fadeColorButton.visible = false;
            }
            case GRADIENT -> {
                colorPicker.visible = true;
                colorSlider.visible = true;
                fadeSpeedSlider.visible = false;
                chromaSpeedSlider.visible = false;
                gradientColorButton.visible = true;
                fadeColorButton.visible = false;
            }
            case FADE -> {
                colorPicker.visible = true;
                colorSlider.visible = true;
                fadeSpeedSlider.visible = true;
                chromaSpeedSlider.visible = false;
                gradientColorButton.visible = false;
                fadeColorButton.visible = true;
            }
            case CHROMA -> {
                colorPicker.visible = false;
                colorSlider.visible = false;
                fadeSpeedSlider.visible = false;
                chromaSpeedSlider.visible = true;
                gradientColorButton.visible = false;
                fadeColorButton.visible = false;
            }
        }
    }

    private void updateColorSlider() {
        colorPicker.setHue(colorSlider.getColor());
        opacitySlider.setColor(colorPicker.getColor());
        property.setColor(getColorIndex(), colorPicker.getColor());
        property.setHue(getColorIndex(), colorSlider.getValueInt());
    }

    private void updateColorPicker() {
        opacitySlider.setColor(colorPicker.getColor());
        property.setColor(getColorIndex(), colorPicker.getColor());
    }

    private void updateOpacitySlider() {
        property.setOpacity(getColorIndex(), opacitySlider.getValue());
    }

    private void updateFadeSpeedSlider() {
        property.fadeSpeed = fadeSpeedSlider.getValue();
    }

    private void updateChromaSpeedSlider() {
        property.chromaSpeed = chromaSpeedSlider.getValue();
    }

    private void updateThicknessSlider() {
        config.thickness = thicknessSlider.getValue();
    }

    private int getColorIndex() {
        return property.colorMode == ColorMode.GRADIENT ? gradientColorIndex : fadeColorIndex;
    }
}
