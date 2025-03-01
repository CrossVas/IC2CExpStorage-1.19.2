package addons.ic2.ic2cexpstorage.containers.comp;

import addons.ic2.ic2cexpstorage.tiles.XPStorageBlockEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import ic2.core.inventory.gui.IC2Screen;
import ic2.core.inventory.gui.components.GuiWidget;
import ic2.core.inventory.gui.components.base.ToolTipButton;
import ic2.core.utils.math.geometry.Box2i;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.Set;

public class XPStorageComponent extends GuiWidget {

    XPStorageBlockEntity TILE;
    int lastXpStorage;

    public XPStorageComponent(XPStorageBlockEntity tile) {
        super(Box2i.EMPTY_BOX);
        this.TILE = tile;
    }

    @Override
    protected void addRequests(Set<ActionRequest> set) {
        set.add(ActionRequest.GUI_INIT);
        set.add(ActionRequest.GUI_TICK);
        set.add(ActionRequest.DRAW_FOREGROUND);
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public void init(IC2Screen gui) {
        int x = gui.getGuiLeft();
        int y = gui.getGuiTop();
        gui.addRenderableWidget(0, new ToolTipButton(x + 28, y + 40, 16, 16, this.string("-"), (T) -> this.exp(false)));
        gui.addRenderableWidget(1, new ToolTipButton(x + 75, y + 40, 16, 16, this.string("+"), (T) -> this.exp(true)));
        gui.addRenderableWidget(2, new ToolTipButton(x + 26, y + 81, 70, 13, this.translate("info.xp_storage.store"), pButton -> this.transfer(false)));
        gui.addRenderableWidget(3, new ToolTipButton(x + 26, y + 95, 70, 13, this.translate("info.xp_storage.take"), pButton -> this.transfer(true)));
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public void tick(IC2Screen gui) {
        boolean active = this.TILE.hasEnergy(this.TILE.getEnergyUsage());
        gui.getButton(0).active = active && this.TILE.getXpStorage() > 0;
        gui.getButton(1).active = active;
        gui.getButton(2).active = active;
        gui.getButton(3).active = active;
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public void drawForeground(PoseStack matrix, int mouseX, int mouseY) {
        Font font = gui.getFont();
        Component text = this.string("" + this.TILE.getXpStorage()).withStyle(ChatFormatting.GREEN);
        int xPos = 60 - font.width(text.getVisualOrderText()) / 2;
        int yPos = 60;
        font.drawShadow(matrix, text, xPos, yPos, 0);
    }

    private void transfer(boolean take) {
        this.TILE.sendToServer(2, take ? 1 : -1);
    }

    private void exp(boolean consume) {
        this.TILE.sendToServer(1, (consume ? 1 : -1) * this.getType());
    }

    @OnlyIn(Dist.CLIENT)
    public int getType() {
        if (Screen.hasControlDown()) {
            return 1000;
        } else if (Screen.hasShiftDown()) {
            return 100;
        } else {
            return Screen.hasAltDown() ? 10 : 1;
        }
    }

    public void onSync() {
        this.lastXpStorage = this.TILE.getXpStorage();
    }
}
