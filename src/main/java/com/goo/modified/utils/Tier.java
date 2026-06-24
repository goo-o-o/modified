package com.goo.modified.utils; // Adjust package path to your layout


import com.goo.goo_lib.utils.text.EffectType;
import com.goo.goo_lib.utils.text.effect.base.ConfiguredEffect;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.common.Tags;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public record Tier(
        String name,
        Optional<List<TagKey<Item>>> tagFilters,
        Optional<List<Holder<Item>>> itemFilters,
        int index,
        float baseWeight,
        int slots,
        float statMultiplier,
        int requiredXpLevel,
        boolean renderRays,
        List<ConfiguredEffect<?>> textEffects
) {
    // It delays looking up the registry until the JSON files are actively read.
    public static final Codec<Tier> CODEC = Codec.lazyInitialized(() -> {
        // Query vanilla's master root directory layer safely during runtime JSON evaluation
        Registry<EffectType<?>> effectTypes = (Registry<EffectType<?>>) BuiltInRegistries.REGISTRY.get(EffectType.REGISTRY_KEY.location());

        if (effectTypes == null) {
            throw new IllegalStateException("Critical Failure: Text Effect Types registry was missing when parsing Tier JSON datasets!");
        }

        return createRecordCodec(effectTypes);
    });

    private static Codec<Tier> createRecordCodec(Registry<EffectType<?>> effectTypes) {
        return RecordCodecBuilder.create(instance -> instance.group(
                // 1. Defaults to "tools" if "name" is missing from the JSON
                Codec.STRING.optionalFieldOf("name", "tools").forGetter(Tier::name),
                TagKey.codec(Registries.ITEM).listOf().optionalFieldOf("tag_filters").forGetter(Tier::tagFilters),
                BuiltInRegistries.ITEM.holderByNameCodec().listOf().optionalFieldOf("item_filters").forGetter(Tier::itemFilters),
                Codec.INT.fieldOf("index").forGetter(Tier::index),
                Codec.FLOAT.fieldOf("baseWeight").forGetter(Tier::baseWeight),
                Codec.INT.fieldOf("slots").forGetter(Tier::slots),
                Codec.FLOAT.optionalFieldOf("statMultiplier", 1.0F).forGetter(Tier::statMultiplier),
                Codec.INT.optionalFieldOf("requiredXpLevel", 30).forGetter(Tier::requiredXpLevel),
                Codec.BOOL.optionalFieldOf("renderRays", false).forGetter(Tier::renderRays),
                ConfiguredEffect.codec(effectTypes).listOf().optionalFieldOf("effects", List.of()).forGetter(Tier::textEffects)
        ).apply(instance, Tier::new));
    }

    public boolean isValidForItem(Holder<Item> itemHolder) {
        // 1. Explicit item whitelist takes highest priority
        if (itemFilters.isPresent() && !itemFilters.get().isEmpty()) {
            for (Holder<Item> allowedItem : itemFilters.get()) {
                if (allowedItem.value() == itemHolder.value()) {
                    return true;
                }
            }
            // If item_filters is defined, and this item isn't in it, fail immediately
            return false;
        }

        // 2. Check tag filters if they exist
        if (tagFilters.isPresent() && !tagFilters.get().isEmpty()) {
            for (TagKey<Item> tag : tagFilters.get()) {
                if (itemHolder.is(tag)) {
                    return true;
                }
            }
            return false;
        }

        // 3. Fallback: If neither tags nor items are specified, default to vanilla NeoForge tools/armor tags
        return itemHolder.is(Tags.Items.TOOLS) || itemHolder.is(Tags.Items.ARMORS);
    }
}