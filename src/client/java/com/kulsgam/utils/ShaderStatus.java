package com.kulsgam.utils;

import com.kulsgam.BlockOverlayClient;
import net.fabricmc.loader.api.FabricLoader;

import java.lang.reflect.Method;

public final class ShaderStatus {
    private ShaderStatus() {
        // utility class; no instances
    }

    public static boolean isIrisShadersEnabled() {
        if (!FabricLoader.getInstance().isModLoaded("iris")) {
            return false;
        }

        try {
            Class<?> irisApiClass =
                    Class.forName("net.irisshaders.iris.api.v0.IrisApi");

            Method getInstance = irisApiClass.getMethod("getInstance");
            Object irisApi = getInstance.invoke(null);

            Method isShaderPackInUse =
                    irisApiClass.getMethod("isShaderPackInUse");

            return (boolean) isShaderPackInUse.invoke(irisApi);
        } catch (Throwable t) {
            // Fail closed: assume shaders off if Iris misbehaves
            BlockOverlayClient.instance.getLogger().info("Failed to query Iris shader state", t);
            return false;
        }
    }
}
