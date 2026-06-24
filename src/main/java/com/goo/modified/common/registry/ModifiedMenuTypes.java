package com.goo.modified.common.registry;

import com.goo.modified.client.gui.reforge.ReforgingMenu;
import com.goo.modified.common.Modified;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.inventory.MenuType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModifiedMenuTypes {
    public static final DeferredRegister<MenuType<?>> MENUS =
        DeferredRegister.create(BuiltInRegistries.MENU, Modified.MOD_ID);

    public static final DeferredHolder<MenuType<?>, MenuType<ReforgingMenu>> REFORGING =
            MENUS.register("reforge", () -> new MenuType<>(ReforgingMenu::new, FeatureFlags.DEFAULT_FLAGS));

}