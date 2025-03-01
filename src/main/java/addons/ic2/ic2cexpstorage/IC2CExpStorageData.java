package addons.ic2.ic2cexpstorage;

import addons.ic2.ic2cexpstorage.blocks.XPStorageBlock;
import addons.ic2.ic2cexpstorage.tiles.XPStorageBlockEntity;
import ic2.core.platform.registries.IC2Tiles;
import net.minecraft.world.level.block.entity.BlockEntityType;

public class IC2CExpStorageData {

    public static BlockEntityType<XPStorageBlockEntity> XP_STORAGE;
    public static XPStorageBlock XP_STORAGE_BLOCK;

    public static void initTiles() {
        XP_STORAGE = IC2Tiles.createTile("xp_storage", XPStorageBlockEntity::new);
        XP_STORAGE_BLOCK = new XPStorageBlock(XP_STORAGE);
    }
}
