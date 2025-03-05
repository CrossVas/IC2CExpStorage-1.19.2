package ic2.addons.xpstorage.blocks;

import ic2.addons.xpstorage.Refs;
import ic2.addons.xpstorage.tiles.XPStorageBlockEntity;
import ic2.core.block.base.drops.IBlockDropProvider;
import ic2.core.block.base.tiles.BaseTileEntity;
import ic2.core.block.machines.BaseMachineBlock;
import ic2.core.platform.registries.IC2Blocks;
import ic2.core.platform.rendering.features.ITextureProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

public class XPStorageBlock extends BaseMachineBlock {

    public XPStorageBlock(BlockEntityType<? extends BaseTileEntity> type) {
        super("xp_storage", IBlockDropProvider.SELF_OR_STABLE_MACHINE, ITextureProvider.noState(Refs.ID, "machine/hv/xpstorage"), type);
        IC2Blocks.registerBlock(this);
    }

    @Override
    public boolean canRemoveBlock(BlockState state, Level level, BlockPos pos, Player player) {
        BlockEntity blockEntity = level.getBlockEntity(pos);
        if (blockEntity instanceof XPStorageBlockEntity xpStorageBlock) {
            return xpStorageBlock.getXpStorage() <= 0;
        } else return false;
    }

    @Override
    public boolean canSetFacing(BlockState state, Level world, BlockPos pos, Player player, Direction side) {
        return false;
    }
}
