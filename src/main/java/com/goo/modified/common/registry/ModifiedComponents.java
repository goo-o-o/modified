package com.goo.modified.common.registry;


import com.goo.modified.common.Modified;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.List;
import java.util.function.Supplier;

public class ModifiedComponents {
    public static final DeferredRegister<DataComponentType<?>> COMPONENTS =
            DeferredRegister.create(Registries.DATA_COMPONENT_TYPE, Modified.MOD_ID);

    // 2. Register the specific component that holds your active Data Pack Modifier IDs
    // This maps to a JSON/NBT array list of ResourceLocations on the item stack: e.g., ["modified:sharp", "custom_pack:god_speed"]
    public static final Supplier<DataComponentType<List<ResourceLocation>>> ITEM_MODIFIERS =
            COMPONENTS.register("item_modifiers", () -> DataComponentType.<List<ResourceLocation>>builder()
                    // persistent() tells Minecraft to serialize this data to the item's NBT/Save file
                    .persistent(ResourceLocation.CODEC.listOf())
                    // networkSynchronized() ensures the data syncs flawlessly from server to client for tooltips/client-side attributes
                    .build());
}
