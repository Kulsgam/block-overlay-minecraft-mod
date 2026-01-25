package com.kulsgam.gui.components.buttons;

import com.kulsgam.utils.Animator;
import com.kulsgam.utils.GuiUtils;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.gui.screen.narration.NarrationPart;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;

import java.util.List;

public abstract class TooltipButton extends ClickableWidget {
    protected final List<Text> tooltips;
    private final Animator animator = new Animator(200.0);
    private boolean prevHovered;
    private boolean animateIn;

    protected TooltipButton(int x, int y, int width, int height, Text message, List<Text> tooltips) {
        super(x, y, width, height, message);
        this.tooltips = tooltips;
    }

    @Override
    protected void renderWidget(DrawContext context, int mouseX, int mouseY, float delta) {
        boolean hovered = this.isHovered();

        if (hovered && !prevHovered) {
            animateIn = true;
            animator.reset();
        } else if (!hovered && prevHovered) {
            animateIn = false;
            animator.reset();
        }

        prevHovered = hovered;

        double scale = animator.getValue(0.1, 1.0, animateIn, false);

        if (hovered || animateIn || scale > 0.1) {
            context.getMatrices().pushMatrix();
            context.getMatrices().translate(mouseX, mouseY);
            context.getMatrices().scale((float) scale, (float) scale);
            GuiUtils.drawTooltip(context, tooltips, 0, 0);
            context.getMatrices().popMatrix();
        }
    }

    @Override
    protected void appendClickableNarrations(NarrationMessageBuilder builder) {
        builder.put(NarrationPart.TITLE, getMessage());
    }
}
