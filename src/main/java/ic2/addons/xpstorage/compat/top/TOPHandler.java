package ic2.addons.xpstorage.compat.top;

import ic2.addons.xpstorage.tiles.XPStorageBlockEntity;
import ic2.probeplugin.base.ProbePlugin;

public class TOPHandler {

    public TOPHandler() {}

    public static void init() {
        ProbePlugin.register(XPStorageBlockEntity.class, TOPXPStorageInfo.INSTANCE);
    }
}
