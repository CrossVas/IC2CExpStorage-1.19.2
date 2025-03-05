package ic2.addons.xpstorage.compat.jade;

import ic2.jadeplugin.base.JadeCommonHandler;

public class JadeHandler {

    public JadeHandler() {}

    public static void init() {
        JadeCommonHandler.THIS.registerProviders(XPStorageInfo.INSTANCE);
    }
}
