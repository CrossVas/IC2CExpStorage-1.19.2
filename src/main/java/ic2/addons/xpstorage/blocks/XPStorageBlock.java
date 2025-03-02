package ic2.addons.xpstorage.blocks;

import ic2.addons.xpstorage.Refs;
import ic2.addons.xpstorage.tiles.XPStorageBlockEntity;
import ic2.core.block.base.drops.IBlockDropProvider;
import ic2.core.block.base.tiles.BaseTileEntity;
import ic2.core.block.machines.BaseMachineBlock;
import ic2.core.platform.registries.IC2Blocks;
import ic2.core.platform.rendering.features.ITextureProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

public class XPStorageBlock extends BaseMachineBlock {

    public XPStorageBlock(BlockEntityType<? extends BaseTileEntity> type) {
        super("xp_storage", IBlockDropProvider.SELF_OR_ADV_MACHINE, ITextureProvider.noState(Refs.ID, "machine/lv/xpstorage"), type);
        IC2Blocks.registerBlock(this);
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand handIn, BlockHitResult hit) {
        if (handIn == InteractionHand.OFF_HAND) {
            return InteractionResult.PASS;
        } else {
            ItemStack heldStack = player.getItemInHand(handIn);
            BlockEntity blockEntity = level.getBlockEntity(pos);
            if (heldStack.is(Items.GLASS_BOTTLE)) {
                if (blockEntity instanceof XPStorageBlockEntity xpStorageBlock) {
                    int xpDrop = 3 + level.random.nextInt(5) + level.random.nextInt(5);
                    if (xpDrop <= xpStorageBlock.getXpStorage()) {
                        xpStorageBlock.xpStorage -= xpDrop;
                        xpStorageBlock.updateGuiField("xpStorage");
                        heldStack.shrink(1);
                        player.getInventory().add(new ItemStack(Items.EXPERIENCE_BOTTLE));
                    }
                }
            } else {
                return super.use(state, level, pos, player, handIn, hit);
            }
        }
        return InteractionResult.PASS;
    }
}
