package com.goo.modified.client.registry;

import com.goo.modified.common.Modified;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.neoforged.neoforge.client.settings.KeyConflictContext;
import org.lwjgl.glfw.GLFW;

public class ModifiedKeybinds {
    public static final String KEY_CATEGORIES_MODIFIED = "key.categories." + Modified.MOD_ID;

    public static final KeyMapping SHOW_RARITY = new KeyMapping(
            "key." + Modified.MOD_ID + ".show_rarity",
            KeyConflictContext.GUI,
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_LEFT_SHIFT,
            KEY_CATEGORIES_MODIFIED
    );

    public static final KeyMapping SHOW_STATS = new KeyMapping(
            "key." + Modified.MOD_ID + ".show_stats",
            KeyConflictContext.GUI,
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_LEFT_SHIFT,
            KEY_CATEGORIES_MODIFIED
    );
}