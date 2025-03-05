package ic2.addons.xpstorage.compat.jade;

import ic2.addons.xpstorage.tiles.XPStorageBlockEntity;
import ic2.jadeplugin.base.JadeHelper;
import ic2.jadeplugin.base.interfaces.IInfoProvider;
import ic2.jadeplugin.helpers.TextFormatter;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.entity.BlockEntity;

public class XPStorageInfo implements IInfoProvider {

    public static final XPStorageInfo INSTANCE = new XPStorageInfo();

    @Override
    public void addInfo(JadeHelper helper, BlockEntity blockEntity, Player player) {
        if (blockEntity instanceof XPStorageBlockEntity xpStorageBlock) {
            helper.maxIn(xpStorageBlock.getMaxInput());
            helper.usage(xpStorageBlock.getEnergyUsage());
            helper.text(TextFormatter.GREEN.translate("ic2.probe.machine.xp", xpStorageBlock.getXpStorage()));
            helper.addTankInfo(xpStorageBlock);
        }
    }
}
