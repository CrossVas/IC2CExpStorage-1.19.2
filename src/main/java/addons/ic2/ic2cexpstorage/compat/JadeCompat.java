package addons.ic2.ic2cexpstorage.compat;

import addons.ic2.ic2cexpstorage.tiles.XPStorageBlockEntity;
import ic2.jadeplugin.base.JadeCommonHandler;
import ic2.jadeplugin.base.JadeHelper;
import ic2.jadeplugin.base.interfaces.IInfoProvider;
import ic2.jadeplugin.helpers.TextFormatter;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.entity.BlockEntity;

public class JadeCompat {

    public static final XPStorageInfo XP_STORAGE_INFO = new XPStorageInfo();

    public static void init() {
        JadeCommonHandler.THIS.registerProviders(XP_STORAGE_INFO);
    }

    public static class XPStorageInfo implements IInfoProvider {

        @Override
        public void addInfo(JadeHelper helper, BlockEntity blockEntity, Player player) {
            if (blockEntity instanceof XPStorageBlockEntity xpStorageBlock) {
                helper.maxIn(xpStorageBlock.getMaxInput());
                helper.usage(xpStorageBlock.getEnergyUsage());
                helper.text(TextFormatter.GREEN.translate("ic2.probe.machine.xp", xpStorageBlock.getXpStorage()));
            }
        }
    }
}
