package com.goo.modified.common;

import com.goo.modified.client.ClientProxy;
import com.goo.modified.common.loot.ItemModifierLootModifier;
import com.goo.modified.common.registry.*;
import com.mojang.logging.LogUtils;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.event.server.ServerStartingEvent;
import net.neoforged.neoforge.server.ServerLifecycleHooks;
import org.slf4j.Logger;

// The value here should match an entry in the META-INF/neoforge.mods.toml file
@Mod(Modified.MOD_ID)
public class Modified {
    // Define mod id in a common place for everything to reference
    public static final String MOD_ID = "modified";
    // Directly reference a slf4j logger
    public static final Logger LOGGER = LogUtils.getLogger();

    // The constructor for the mod class is the first code that is run when your mod is loaded.
    // FML will recognize some parameter types like IEventBus or ModContainer and pass them in automatically.
    public Modified(IEventBus modEventBus, ModContainer modContainer) {

        ModifiedMenuTypes.MENUS.register(modEventBus);
        ModifiedBlocks.BLOCKS.register(modEventBus);
        ModifiedItems.ITEMS.register(modEventBus);
        ModifiedComponents.COMPONENTS.register(modEventBus);

        ModifiedLootModifiers.LOOT_MODIFIERS.register(modEventBus);
        ModifiedLootModifiers.LOOT_MODIFIERS.register("roll_modifiers", () -> ItemModifierLootModifier.CODEC);
        // Register our mod's ModConfigSpec so that FML can create and load the config file for us
        modContainer.registerConfig(ModConfig.Type.COMMON, CommonConfig.SPEC);
        modContainer.registerConfig(ModConfig.Type.CLIENT, ClientConfig.SPEC);
    }

    public static ResourceLocation loc(String path) {
        return ResourceLocation.fromNamespaceAndPath(Modified.MOD_ID, path);
    }

    public static RegistryAccess getSafeRegistryAccess() {
        if (FMLEnvironment.dist.isDedicatedServer()) {
            var server = ServerLifecycleHooks.getCurrentServer();
            return server != null ? server.registryAccess() : null;
        } else {
            // Client side fallback proxy
            return ClientProxy.getClientRegistryAccess();
        }
    }

}
