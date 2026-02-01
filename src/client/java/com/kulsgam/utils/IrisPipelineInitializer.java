package com.kulsgam.utils;

import com.kulsgam.BlockOverlayClient;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import net.fabricmc.loader.api.FabricLoader;

import java.lang.reflect.Field;

import net.minecraft.client.gl.RenderPipelines;

import java.lang.reflect.Method;

public final class IrisPipelineInitializer {
    private IrisPipelineInitializer() {
        // utility class; no instances
    }

    public static void init() {
        if (!FabricLoader.getInstance().isModLoaded("iris")) {
            return;
        }

        try {
            Class<?> irisApiClass =
                    Class.forName("net.irisshaders.iris.api.v0.IrisApi");
            Class<?> irisProgramClass =
                    Class.forName("net.irisshaders.iris.api.v0.IrisProgram");

            Method getInstance = irisApiClass.getMethod("getInstance");
            Field irisProgramBASIC = irisProgramClass.getField("BASIC");

            Object irisApiInstance = getInstance.invoke(null);

            // assignPipeline(RenderPipeline pipeline, IrisProgram program)
            Method assignPipeline = irisApiClass.getMethod(
                    "assignPipeline",
                    RenderPipeline.class,
                    irisProgramClass
            );

            assignPipeline.invoke(irisApiInstance, RenderPipelines.DEBUG_QUADS, irisProgramBASIC.get(null));
        } catch (Throwable t) {
            // Fail closed: assume shaders off if Iris misbehaves
            BlockOverlayClient.instance.getLogger().info("Failed to initialize Iris pipelines: ", t);
        }
    }
}
