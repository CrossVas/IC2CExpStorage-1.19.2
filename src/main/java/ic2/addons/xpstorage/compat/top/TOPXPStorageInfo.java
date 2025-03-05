package ic2.addons.xpstorage.compat.top;

import ic2.addons.xpstorage.tiles.XPStorageBlockEntity;
import ic2.api.energy.EnergyNet;
import ic2.probeplugin.base.ProbePluginHelper;
import ic2.probeplugin.info.ITileInfoComponent;
import ic2.probeplugin.override.IExpandedProbeInfo;
import ic2.probeplugin.override.components.Panel;
import ic2.probeplugin.styles.IC2Styles;
import mcjty.theoneprobe.api.IProbeInfo;
import net.minecraft.ChatFormatting;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;

public class TOPXPStorageInfo implements ITileInfoComponent<XPStorageBlockEntity> {

    public static final TOPXPStorageInfo INSTANCE = new TOPXPStorageInfo();

    @Override
    public void addInfo(IProbeInfo info, Player player, Direction direction, XPStorageBlockEntity xpStorageBlock) {
        Panel machine = new Panel(IC2Styles.OUTER_STYLE, Panel.Type.VERTICAL);
        IExpandedProbeInfo basicInfo = machine.vertical(IC2Styles.INNER_STYLE);
        basicInfo.text("ic2.probe.eu.tier.name", EnergyNet.INSTANCE.getDisplayTier(xpStorageBlock.getTier()));
        basicInfo.text("ic2.probe.eu.max_in.name", xpStorageBlock.getMaxInput());
        basicInfo.text("ic2.probe.eu.usage.name", xpStorageBlock.getEnergyUsage());
        basicInfo.text(Component.translatable("ic2.probe.machine.xp", xpStorageBlock.getXpStorage()).withStyle(ChatFormatting.GREEN));
        IExpandedProbeInfo bars = basicInfo.vertical().element(ProbePluginHelper.generateHiddenBar(xpStorageBlock));
        ProbePluginHelper.addTanks(xpStorageBlock, bars, false);
        this.addSecurely(info, 1, machine);
    }
}
