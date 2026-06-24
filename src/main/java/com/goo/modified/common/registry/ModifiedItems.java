package com.goo.modified.common.registry;

import com.goo.modified.common.Modified;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.registries.DeferredRegister;
import java.util.function.Supplier;

public class ModifiedItems {
    public static final DeferredRegister<Item> ITEMS =
            DeferredRegister.create(BuiltInRegistries.ITEM, Modified.MOD_ID);

    public static final Supplier<Item> REFORGING_ANVIL_ITEM = ITEMS.register("reforging_anvil",
            () -> new BlockItem(ModifiedBlocks.REFORGING_ANVIL.get(), new Item.Properties()));
}