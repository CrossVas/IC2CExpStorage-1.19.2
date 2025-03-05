package ic2.addons.xpstorage;

import ic2.addons.xpstorage.compat.jade.JadeHandler;
import ic2.addons.xpstorage.compat.top.TOPHandler;
import ic2.api.recipes.registries.IAdvancedCraftingManager;
import ic2.core.platform.recipes.misc.AdvRecipeRegistry;
import ic2.core.platform.registries.IC2Blocks;
import ic2.core.platform.registries.IC2Items;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegisterEvent;

@Mod(Refs.ID)
public class IC2CExpStorage {

    public IC2CExpStorage() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
        modEventBus.addListener(this::onRegister);
        modEventBus.addListener(this::commonLoad);
    }

    public void commonLoad(FMLCommonSetupEvent e) {
        if (ModList.get().isLoaded("ic2jadeplugin")) {
            JadeHandler.init();
        }
        if (ModList.get().isLoaded("theoneprobe")) {
            TOPHandler.init();
        }
        AdvRecipeRegistry.INSTANCE.registerListener(this::initRecipes);
    }

    public void onRegister(RegisterEvent e) {
        if (e.getRegistryKey().equals(ForgeRegistries.Keys.BLOCKS)) {
            IC2CExpStorageData.initTiles();
        }
    }

    public void initRecipes(IAdvancedCraftingManager manager) {
        manager.addShapedRecipe(new ResourceLocation(Refs.ID, "xp_storage"), new ItemStack(IC2CExpStorageData.XP_STORAGE_BLOCK),
                "APA", "CTC", "UMU",
                'A', IC2Items.INGOT_ALUMINIUM,
                'P', IC2Blocks.PICKUP_TUBE,
                'C', Items.END_CRYSTAL,
                'T', IC2Blocks.MACHINE_TANK,
                'U', IC2Items.EXP_COLLECTOR_UPGRADE,
                'M', IC2Blocks.STABILIZED_MACHINE_BLOCK);
    }
}
