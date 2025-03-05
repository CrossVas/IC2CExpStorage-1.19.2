package ic2.addons.xpstorage.utils;

import com.mojang.blaze3d.vertex.PoseStack;
import ic2.addons.xpstorage.Refs;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.client.gui.ScreenUtils;
import org.jetbrains.annotations.NotNull;

public class CustomButton extends Button {

    protected ResourceLocation TEXTURE;

    public CustomButton(int pX, int pY, Variant variant, Component pMessage, OnPress pOnPress) {
        super(pX, pY, variant.pUWidth, variant.pVHeight, pMessage, pOnPress);
        this.TEXTURE = new ResourceLocation(Refs.ID, "textures/gui/button.png");
    }

    @Override
    public void renderButton(@NotNull PoseStack pPoseStack, int pMouseX, int pMouseY, float pPartialTick) {
        Minecraft minecraft = Minecraft.getInstance();
        Font font = minecraft.font;
        int k = this.getYImage(this.isHoveredOrFocused());
        ScreenUtils.blitWithBorder(pPoseStack, TEXTURE, this.x, this.y, 0, k * 15, this.width, this.height, 98, 15, 1, 1, 1, 2, this.getBlitOffset());
        this.renderBg(pPoseStack, minecraft, pMouseX, pMouseY);

        Component buttonText = this.getMessage();
        int strWidth = font.width(buttonText);
        int ellipsisWidth = font.width("...");

        if (strWidth > width - 6 && strWidth > ellipsisWidth)
            buttonText = Component.literal(font.substrByWidth(buttonText, width - 6 - ellipsisWidth).getString() + "...");

        drawCenteredString(pPoseStack, font, buttonText, this.x + this.width / 2, this.y + (this.height - 8) / 2, getFGColor());
    }

    public enum Variant {
        SMALL(26, 15),
        LONG(72, 15);

        final int pUWidth;
        final int pVHeight;

        Variant(int pUWidth, int pVHeight) {
            this.pUWidth = pUWidth;
            this.pVHeight = pVHeight;
        }
    }
}
