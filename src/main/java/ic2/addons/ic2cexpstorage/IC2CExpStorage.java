package ic2.addons.ic2cexpstorage;

import ic2.addons.ic2cexpstorage.compat.JadeCompat;
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
            JadeCompat.init();
        }
    }

    public void onRegister(RegisterEvent e) {
        if (e.getRegistryKey().equals(ForgeRegistries.Keys.BLOCKS)) {
            IC2CExpStorageData.initTiles();
        }
    }
}
