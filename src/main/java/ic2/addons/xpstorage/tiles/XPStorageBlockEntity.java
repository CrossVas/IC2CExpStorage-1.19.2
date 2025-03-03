package ic2.addons.xpstorage.tiles;

import ic2.addons.xpstorage.IC2CExpStorageData;
import ic2.addons.xpstorage.containers.XPStorageContainer;
import ic2.addons.xpstorage.containers.comp.XPStorageComponent;
import ic2.api.network.buffer.NetworkInfo;
import ic2.api.util.DirectionList;
import ic2.core.block.base.cache.TileCache;
import ic2.core.block.base.features.ITickListener;
import ic2.core.block.base.tiles.BaseElectricTileEntity;
import ic2.core.block.machines.containers.mv.ChunkloaderContainer;
import ic2.core.block.machines.tiles.hv.ElectricEnchanterTileEntity;
import ic2.core.inventory.base.ITileGui;
import ic2.core.inventory.container.IC2Container;
import ic2.core.platform.registries.IC2Tags;
import ic2.core.utils.helpers.EnchantUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.tags.TagKey;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.fluids.capability.templates.FluidTank;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class XPStorageBlockEntity extends BaseElectricTileEntity implements ITileGui, ITickListener {

    TagKey<Fluid> EXPERIENCE = IC2Tags.createForgeFluidTag("experience");
    FluidTank fluidTank = new FluidTank(1000, fluidStack -> fluidStack.getFluid().is(EXPERIENCE)); // keep it equal to 1 bucket

    @NetworkInfo
    public int xpStorage = 0;
    protected int energyUsage = 0;
    TileCache<ElectricEnchanterTileEntity> ENCHANTER;

    private List<Player> lastPlayers = new ArrayList<>();

    public XPStorageBlockEntity(BlockPos pos, BlockState state) {
        super(pos, state, 0, 512, 10000);
        this.addCapability(ForgeCapabilities.FLUID_HANDLER, this.fluidTank);
        this.addGuiFields("xpStorage");
        this.ENCHANTER = new TileCache<>(this, DirectionList.ALL, ElectricEnchanterTileEntity.class);
        this.addCaches(this.ENCHANTER);
    }

    @Override
    protected void onCachesUpdated() {
        if (!this.ENCHANTER.isEmpty()) {
            this.addToTick();
        }
    }

    public int calculateEnergyUsage(int storage) {
        int divisor = (storage > 1000000) ? 5000 : 1000; // Adjusts dynamically
        return Math.max(1, Math.min(512, storage / divisor));
    }

    public ElectricEnchanterTileEntity getValidEnchanter() {
        for (Direction direction : this.ENCHANTER) {
            ElectricEnchanterTileEntity enchanter = this.ENCHANTER.getHandler(direction);
            if (enchanter != null) {
                return enchanter;
            }
        }
        return null;
    }

    @Override
    public void onTick() {
        // change to active when storing xp
        if (this.getXpStorage() > 0) {
            this.setActive(true);
        }

        // use energy when active
        if (this.isActive()) {
            this.useEnergy(this.getEnergyUsage());
        }

        if (!this.fluidTank.isEmpty()) {
            int fluidAmount = this.fluidTank.getFluidAmount();
            if (fluidAmount >= 500) {
                this.xpStorage += 1;
                this.fluidTank.getFluid().shrink(1);
                this.updateGuiField("xpStorage");
            }
        }

        // check for Electric Enchanter every 2 seconds
        if (this.clock(40)) {
            this.energyUsage = calculateEnergyUsage(this.getXpStorage());
            ElectricEnchanterTileEntity enchanter = getValidEnchanter();
            if (enchanter != null) {
                int storedXP = enchanter.storedExperience;
                if (storedXP < 1000) {
                    int needed = 1000 - storedXP;
                    int offer = Math.min(needed, this.getXpStorage());
                    this.xpStorage -= offer;
                    this.updateGuiField("xpStorage");
                    enchanter.storedExperience += offer;
                    enchanter.updateGuiField("storedExperience");
                }
            }
        }

        // we update the players list every ~2 seconds
        // if the players isn't around for a longer time, the list gets updated, adding a delay when starting the drain action - ok
        // the drain stop is handled later based on bounding box, rather than on players list, this ensures the draining action is stopped the moment players is out of the box - ok
        // Thanks: Speiger - IC2C Dev.
        AABB drainArea = new AABB(this.getBlockPos()).inflate(0, 2, 0);
        if (this.clock(40)) {
            // gather players
            this.lastPlayers = this.level.getEntitiesOfClass(Player.class, drainArea, player -> player.isAlive() && !player.isSpectator() && player.totalExperience > 0);
        }

        // drain player's xp
        if (!this.lastPlayers.isEmpty()) {
            for (Player player : this.lastPlayers) {
                if (player.getBoundingBox().intersects(drainArea)) {
                    int xpLevel = player.totalExperience;
                    int drain = Math.min(100, xpLevel);
                    this.xpStorage += EnchantUtil.drainExperience(player, drain);
                    this.updateGuiField("xpStorage");
                }
            }
        }

        // handle energy comparator
        this.handleComparators();
    }

    @Override
    public boolean supportsNotify() {
        return false;
    }

    @Override
    public boolean allowsUI() { // slot filter
        return false;
    }

    @Override
    public BlockEntityType<?> createType() {
        return IC2CExpStorageData.XP_STORAGE;
    }

    @Override
    public void saveAdditional(CompoundTag compound) {
        super.saveAdditional(compound);
        compound.putInt("xpStorage", this.xpStorage);
        compound.put("fluidTank", this.fluidTank.writeToNBT(new CompoundTag()));
    }

    @Override
    public void load(CompoundTag compound) {
        super.load(compound);
        this.xpStorage = compound.getInt("xpStorage");
        this.fluidTank.readFromNBT(compound.getCompound("fluidTank"));
    }

    public int getXpStorage() {
        return this.xpStorage;
    }

    public int getEnergyUsage() {
        return this.energyUsage;
    }

    @Override
    public void onGuiFieldChanged(Set<String> fields, Player player) {
        super.onGuiFieldChanged(fields, player);
        if (fields.contains("xpStorage")) {
            this.resyncGUI();
        }
    }

    @OnlyIn(Dist.CLIENT)
    public void resyncGUI() {
        AbstractContainerMenu c = Minecraft.getInstance().player.containerMenu;
        if (c instanceof ChunkloaderContainer container) {
            XPStorageComponent comp = container.getComponent(XPStorageComponent.class);
            if (comp != null) {
                comp.onSync();
            }
        }
    }

    @Override
    public void onClientDataReceived(Player entity, int key, int value) {
        super.onClientDataReceived(entity, key, value);
        switch (key) {
            case 1:
                if (value < 0) {
                    int removing = Math.min(-value, this.xpStorage);
                    if (!entity.isCreative()) {
                        entity.giveExperiencePoints(removing);
                    }

                    entity.onEnchantmentPerformed(ItemStack.EMPTY, 0);
                    this.xpStorage -= removing;
                    this.updateGuiField("xpStorage");
                } else {
                    this.xpStorage += EnchantUtil.drainExperience(entity, Math.min(Integer.MAX_VALUE - this.xpStorage, value));
                    this.updateGuiField("xpStorage");
                }
                break;
            case 2:
                if (value == 1) {
                    int removing = this.xpStorage;
                    if (!entity.isCreative()) entity.giveExperiencePoints(removing);
                    entity.onEnchantmentPerformed(ItemStack.EMPTY, 0);
                    this.xpStorage -= removing;
                    this.updateGuiField("xpStorage");
                } else if (value == -1) {
                    this.xpStorage += EnchantUtil.drainExperience(entity, entity.totalExperience);
                    this.updateGuiField("xpStorage");
                }
        }
    }

    @Override
    public IC2Container createContainer(Player player, InteractionHand interactionHand, Direction direction, int i) {
        return new XPStorageContainer(this, player, i);
    }
}
