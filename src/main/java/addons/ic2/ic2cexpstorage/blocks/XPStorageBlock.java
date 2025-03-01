package addons.ic2.ic2cexpstorage.blocks;

import addons.ic2.ic2cexpstorage.Refs;
import ic2.core.block.base.drops.IBlockDropProvider;
import ic2.core.block.base.tiles.BaseTileEntity;
import ic2.core.block.machines.BaseMachineBlock;
import ic2.core.platform.registries.IC2Blocks;
import ic2.core.platform.rendering.features.ITextureProvider;
import net.minecraft.world.level.block.entity.BlockEntityType;

public class XPStorageBlock extends BaseMachineBlock {

    public XPStorageBlock(BlockEntityType<? extends BaseTileEntity> type) {
        super("xp_storage", IBlockDropProvider.SELF_OR_ADV_MACHINE, ITextureProvider.noState(Refs.ID, "machine/lv/xpstorage"), type);
        IC2Blocks.registerBlock(this);
    }
}
