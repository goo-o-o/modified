package com.goo.modified.common;

import net.neoforged.neoforge.common.ModConfigSpec;

// An example config class. This is not required, but it's a good idea to have one to keep your config organized.
// Demonstrates how to use Neo's config APIs
public class CommonConfig {
    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();

    public static final ModConfigSpec.BooleanValue USE_VANILLA_LUCK = BUILDER
            .comment("Whether to use the player's Luck attribute when rolling for Tiers (defaults to a Luck value of 1 if disabled)")
            .define("use_vanilla_luck", true);

    public static final ModConfigSpec.DoubleValue LUCK_MULTIPLIER = BUILDER
            .comment("A global multiplier for all Tier rolls")
            .defineInRange("luck_multiplier", 1D, -Integer.MAX_VALUE, Integer.MAX_VALUE);

    public static final ModConfigSpec.DoubleValue ASCEND_XP_COST_MULTIPLIER = BUILDER
            .comment("XP cost multiplier for Ascending Tiers")
            .defineInRange("ascend_xp_cost_multiplier", 1, 0.1, 100);


    public static final ModConfigSpec SPEC = BUILDER.build();

}
