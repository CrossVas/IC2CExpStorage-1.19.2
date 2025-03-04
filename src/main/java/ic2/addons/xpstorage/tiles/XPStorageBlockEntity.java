package ic2.addons.xpstorage.tiles;

import ic2.addons.xpstorage.IC2CExpStorageData;
import ic2.addons.xpstorage.containers.XPStorageContainer;
import ic2.api.network.buffer.NetworkInfo;
import ic2.api.util.DirectionList;
import ic2.core.IC2;
import ic2.core.block.base.cache.TileCache;
import ic2.core.block.base.features.IClickable;
import ic2.core.block.base.features.ITickListener;
import ic2.core.block.base.features.IXPMachine;
import ic2.core.block.base.tiles.BaseElectricTileEntity;
import ic2.core.block.base.tiles.BaseLinkingTileEntity;
import ic2.core.block.base.tiles.impls.machine.single.BasicMachineTileEntity;
import ic2.core.block.machines.tiles.hv.ElectricEnchanterTileEntity;
import ic2.core.inventory.base.ITileGui;
import ic2.core.inventory.container.IC2Container;
import ic2.core.inventory.filter.SetItemFilter;
import ic2.core.platform.player.PlayerHandler;
import ic2.core.platform.registries.IC2Items;
import ic2.core.platform.registries.IC2Tags;
import ic2.core.utils.helpers.EnchantUtil;
import ic2.core.utils.helpers.StackUtil;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.TagKey;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.fluids.capability.templates.FluidTank;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class XPStorageBlockEntity extends BaseElectricTileEntity implements ITileGui, ITickListener, IClickable {

    TagKey<Fluid> EXPERIENCE = IC2Tags.createForgeFluidTag("experience");
    FluidTank fluidTank = new FluidTank(1000, fluidStack -> fluidStack.getFluid().is(EXPERIENCE)); // keep it equal to 1 bucket

    @NetworkInfo
    public int xpStorage = 0;
    @NetworkInfo
    public int energyUsage = 0;
    TileCache<ElectricEnchanterTileEntity> ENCHANTER;
    TileCache<BasicMachineTileEntity> BASIC_XP_PROVIDER;
    TileCache<BaseLinkingTileEntity> MULTI_XP_PROVIDER;

    private List<Player> lastPlayers = new ArrayList<>();
    private List<ExperienceOrb> orbs = new ObjectArrayList<>();

    public XPStorageBlockEntity(BlockPos pos, BlockState state) {
        super(pos, state, 0, 512, 10000);
        this.addCapability(ForgeCapabilities.FLUID_HANDLER, this.fluidTank);
        this.addGuiFields("xpStorage", "energyUsage");
        this.ENCHANTER = new TileCache<>(this, DirectionList.ALL, ElectricEnchanterTileEntity.class);
        this.BASIC_XP_PROVIDER = new TileCache<>(this, DirectionList.ALL, BasicMachineTileEntity.class);
        this.MULTI_XP_PROVIDER = new TileCache<>(this, DirectionList.ALL, BaseLinkingTileEntity.class);
        this.addCaches(this.ENCHANTER);
        this.addCaches(this.BASIC_XP_PROVIDER);
        this.addCaches(this.MULTI_XP_PROVIDER);
    }

    @Override
    protected void onCachesUpdated() {
        if (!this.ENCHANTER.isEmpty() || !this.BASIC_XP_PROVIDER.isEmpty() || !this.MULTI_XP_PROVIDER.isEmpty()) {
            this.addToTick();
        }
    }

    public void calculateEnergyUsage(int storage) {
        int divisor = (storage > 1000000) ? 5000 : 1000; // Adjusts dynamically
        this.energyUsage = Math.max(1, Math.min(512, storage / divisor));
        this.updateGuiField("energyUsage");
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

    public BasicMachineTileEntity getValidBaseXPProvider() {
        for (Direction direction : this.BASIC_XP_PROVIDER) {
            BasicMachineTileEntity machine = this.BASIC_XP_PROVIDER.getHandler(direction);
            if (machine != null) {
                return machine;
            }
        }

        return null;
    }

    public BaseLinkingTileEntity getValidMultiXPProvider() {
        for (Direction direction : this.MULTI_XP_PROVIDER) {
            BaseLinkingTileEntity linkingTileEntity = this.MULTI_XP_PROVIDER.getHandler(direction);
            if (linkingTileEntity != null) {
                return linkingTileEntity;
            }
        }
        return null;
    }

    @Override
    public void onTick() {
        calculateEnergyUsage(this.getXpStorage());
        // change to active when storing xp
        if (this.hasEnergy(this.getEnergyUsage())) {
            if (this.getXpStorage() > 0) {
                this.setActive(true);
            }

            // use energy when active
            if (this.isActive()) {
                this.useEnergy(this.getEnergyUsage());
            }

            if (!this.fluidTank.isEmpty()) {
                int fluidAmount = this.fluidTank.getFluidAmount();
                if (fluidAmount >= 20) {
                    this.xpStorage += 1;
                    this.fluidTank.getFluid().shrink(20);
                    this.updateGuiField("xpStorage");
                }
            }

            // check for Electric Enchanter every 2 seconds
            if (this.clock(40)) {
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

            if (this.clock(200)) {
                IXPMachine xpProvider = getValidBaseXPProvider();
                BaseLinkingTileEntity linkingTileEntity = getValidMultiXPProvider();
                if (xpProvider != null) {
                    int storedXP = xpProvider.getCreatedXP(true);
                    this.xpStorage += storedXP;
                    this.updateGuiField("xpStorage");
                }
                if (linkingTileEntity != null) {
                    BlockEntity master = linkingTileEntity.getMaster();
                    if (master instanceof IXPMachine ixpMachine) {
                        int storedXP = ixpMachine.getCreatedXP(true);
                        this.xpStorage += storedXP;
                        this.updateGuiField("xpStorage");
                    }
                }
            }

            // we update the players list every ~2 seconds
            // if the players isn't around for a longer time, the list gets updated, adding a delay when starting the drain action - ok
            // the drain stop is handled later based on bounding box, rather than on players list, this ensures the draining action is stopped the moment players is out of the box - ok
            // Thanks: Speiger - IC2C Dev.
            AABB collectArea = new AABB(this.getBlockPos()).inflate(4);
            AABB drainArea = new AABB(this.getBlockPos()).inflate(0, 2, 0);
            if (this.clock(40)) {
                // gather players
                this.lastPlayers = this.level.getEntitiesOfClass(Player.class, drainArea, player -> player.isAlive() && !player.isSpectator() && player.totalExperience > 0);
                // gather orbs
                orbs = level.getEntities(EntityType.EXPERIENCE_ORB, collectArea,
                        orb -> orb.isAlive() && (!level.isClientSide || orb.tickCount > 1)
                                && !orb.getPersistentData().contains("PreventRemoteMovement"));
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

            if (!orbs.isEmpty()) {
                int totalXp = 0;
                Iterator<ExperienceOrb> iterator = orbs.iterator();
                while (iterator.hasNext()) {
                    ExperienceOrb orb = iterator.next();
                    if (orb.getBoundingBox().intersects(collectArea)) {
                        int xpAmount = orb.getValue();
                        totalXp += xpAmount;
                        orb.discard();
                        iterator.remove();
                    }
                }

                if (totalXp > 0) {
                    this.xpStorage += totalXp;
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

    @Override
    public boolean onRightClick(Player player, InteractionHand interactionHand, Direction direction, BlockHitResult blockHitResult) {
        if (interactionHand == InteractionHand.OFF_HAND) return false;
        PlayerHandler handler = PlayerHandler.getHandler(player);
        boolean altKey = handler.altKeyDown;

        RandomSource random = player.getRandom();
        ItemStack heldStack = player.getItemInHand(interactionHand);
        boolean actionPerformed = false;

        int totalAttempts = altKey ? Math.min(64, heldStack.getCount()) : 1;
        int totalTransfer = 0;
        int finished = 0;

        // filter held stack
        // we either have experience bottle or glass bottle
        if (heldStack.is(Items.EXPERIENCE_BOTTLE) || heldStack.is(Items.GLASS_BOTTLE)) {
            // check for one case: we check for extraction, which uses glass bottle
            // true for XP extraction, false for insertion
            boolean isExtracting = heldStack.is(Items.GLASS_BOTTLE);
            for (int i = 0; i < totalAttempts; i++) {
                int xpPoints = 3 + random.nextInt(5) + random.nextInt(5);
                if (isExtracting && totalTransfer + xpPoints > this.getXpStorage()) break; // break if in extraction mode and we have need more than we actually have
                totalTransfer += xpPoints; // continue otherwise
                finished++; // finish the calculation
            }

            if (finished > 0) {
                StackUtil.addOrPop(player, player.blockPosition(), new ItemStack(isExtracting ? Items.EXPERIENCE_BOTTLE : Items.GLASS_BOTTLE, finished)); // return the corresponding item
                this.xpStorage += isExtracting ? -totalTransfer : totalTransfer; // if my math is correct, this should work
                actionPerformed = true;
            }
        }

        if (actionPerformed) {
            if (!player.isCreative()) heldStack.shrink(finished);
            this.updateGuiField("xpStorage");
            player.getLevel().playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.EXPERIENCE_ORB_PICKUP, SoundSource.NEUTRAL, 0.1F, 0.4F / (random.nextFloat() * 0.4F + 0.8F));
            return true;
        }

        if (WRENCHES.matches(heldStack)) {
            if (IC2.PLATFORM.isRendering()) {
                player.displayClientMessage(Component.literal("You really want to lose all of your XP?"), false);
            }
            return false;
        }
        return false;
    }

    SetItemFilter WRENCHES = new SetItemFilter(IC2Items.WRENCH, IC2Items.ELECTRIC_WRENCH, IC2Items.PRECISION_WRENCH);
}
