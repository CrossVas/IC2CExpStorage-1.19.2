package ic2.addons.xpstorage.utils;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import ic2.addons.xpstorage.Refs;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

// TODO: Maybe we should make it dynamic?
public class CustomButton extends Button {

    protected ResourceLocation TEXTURE;
    private final Variant BUTTON_VARIANT;

    public CustomButton(int pX, int pY, Variant variant, Component pMessage, OnPress pOnPress) {
        super(pX, pY, variant.pUWidth, variant.pVHeight, pMessage, pOnPress);
        this.TEXTURE = new ResourceLocation(Refs.ID, "textures/gui/button.png");
        this.BUTTON_VARIANT = variant;
    }

    @Override
    public void renderButton(@NotNull PoseStack pPoseStack, int pMouseX, int pMouseY, float pPartialTick) {
        Minecraft minecraft = Minecraft.getInstance();
        Font font = minecraft.font;
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderTexture(0, TEXTURE);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, this.alpha);
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.enableDepthTest();
        if (!this.isHoveredOrFocused()) { // regular
            this.blit(pPoseStack, this.x, this.y, BUTTON_VARIANT.pU, BUTTON_VARIANT.pV, BUTTON_VARIANT.pUWidth, BUTTON_VARIANT.pVHeight);
        } else { // hover
            this.blit(pPoseStack, this.x, this.y, BUTTON_VARIANT.pUA, BUTTON_VARIANT.pVA, BUTTON_VARIANT.pUWidth, BUTTON_VARIANT.pVHeight);
        }
        font.drawShadow(pPoseStack, this.getMessage(), this.x + (this.width - font.width(this.getMessage().getVisualOrderText())) / 2, this.y + (this.height / 2) - font.lineHeight / 2, 0);
    }

    public enum Variant {
        SMALL(0, 0, 0, 15, 26, 15),
        LONG(26, 0, 26, 15, 72, 15);

        final int pU;
        final int pV;
        final int pUA;
        final int pVA;
        final int pUWidth;
        final int pVHeight;

        Variant(int pUOffset, int pVOffset, int pUA, int pVA, int pUWidth, int pVHeight) {
            this.pU = pUOffset;
            this.pV = pVOffset;
            this.pUA = pUA;
            this.pVA = pVA;
            this.pUWidth = pUWidth;
            this.pVHeight = pVHeight;
        }
    }
}
