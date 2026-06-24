package com.goo.modified.common.events;


import com.goo.goo_lib.common.registry.TextEffects;
import com.goo.goo_lib.utils.text.EffectType;
import com.goo.modified.common.Modified;
import com.goo.modified.common.ModifiedCommands;
import com.goo.modified.utils.Modifier;
import com.goo.modified.utils.Tier;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.server.packs.repository.PackSource;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.AddPackFindersEvent;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.registries.DataPackRegistryEvent;
import net.neoforged.neoforge.registries.RegistryManager;
import net.neoforged.neoforge.server.ServerLifecycleHooks;

@EventBusSubscriber(modid = Modified.MOD_ID)
public class ModRegistryEvents {
    public static final ResourceKey<Registry<Modifier>> MODIFIER_REGISTRY_KEY =
            ResourceKey.createRegistryKey(ResourceLocation.withDefaultNamespace("modifiers"));
    public static final ResourceKey<Registry<Tier>> TIER_REGISTRY_KEY =
            ResourceKey.createRegistryKey(ResourceLocation.withDefaultNamespace("tiers"));

    @SubscribeEvent
    public static void onRegisterCommands(RegisterCommandsEvent event) {
        ModifiedCommands.register(event.getDispatcher());
    }

    @SubscribeEvent
    public static void registerDataPackRegistries(DataPackRegistryEvent.NewRegistry event) {
        // 1. This registers your other custom registry
        event.dataPackRegistry(MODIFIER_REGISTRY_KEY, Modifier.CODEC, Modifier.CODEC);

        event.dataPackRegistry(
                TIER_REGISTRY_KEY,
                Tier.CODEC, // Used to parse incoming datapack files
                Tier.CODEC  // Used to transmit serialized data to connecting clients
        );
    }

    @SubscribeEvent
    public static void addPackFinders(AddPackFindersEvent event) {
        event.addPackFinders(
                ResourceLocation.fromNamespaceAndPath(Modified.MOD_ID, "default_settings"),
                PackType.SERVER_DATA,
                Component.literal("Default settings for the Modified! mod"),
                PackSource.BUILT_IN,
                false,
                Pack.Position.BOTTOM
        );
    }
}
