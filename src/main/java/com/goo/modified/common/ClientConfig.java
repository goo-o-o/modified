package com.goo.modified.common;

import net.neoforged.neoforge.common.ModConfigSpec;

// An example config class. This is not required, but it's a good idea to have one to keep your config organized.
// Demonstrates how to use Neo's config APIs
public class ClientConfig {
    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();


    public static final ModConfigSpec.BooleanValue RENDER_SUNBURST_RAYS = BUILDER
            .comment("Whether a to render Sunburst rays behind items")
            .define("render_sunburst_rays", true);
    public static final ModConfigSpec.IntValue SUNBURST_RAY_COUNT = BUILDER
            .comment("How many Sunburst rays to render")
            .defineInRange("sunburst_ray_count", 10, 1, 64);
    public static final ModConfigSpec.DoubleValue SUNBURST_RAY_LENGTH = BUILDER
            .comment("How long should each Sunburst ray be")
            .defineInRange("sunburst_ray_length", 0.75, 0.01, 10.0F);
    public static final ModConfigSpec.DoubleValue SUNBURST_RAY_WIDTH = BUILDER
            .comment("Whether a to render Sunburst rays behind items")
            .defineInRange("sunburst_ray_width", 0.2, 0.01, 1.0F);

    public static final ModConfigSpec.BooleanValue REQUIRE_KEYBIND_TO_SHOW_RARITY = BUILDER
            .comment("Whether a keybind is required to show the Rarity")
            .define("require_keybind_to_show_rarity", false);

    public static final ModConfigSpec.BooleanValue REQUIRE_KEYBIND_TO_SHOW_STATS = BUILDER
            .comment("Whether a keybind is required to show additional Stats")
            .define("require_keybind_to_show_stats", true);

    public static final ModConfigSpec.BooleanValue COLOR_ITEM_NAME_WITH_RARITY = BUILDER
            .comment("Whether the Item's name should be colored by its Rarity")
            .define("color_item_name_with_rarity", true);

    public static final ModConfigSpec SPEC = BUILDER.build();

}
