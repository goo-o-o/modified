package com.goo.modified.utils;

import com.goo.goo_lib.utils.attribute.AttributeContainer;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;

import java.util.List;
import java.util.Optional;

public record Modifier(
        Optional<List<TagKey<Item>>> tagFilters,
        Optional<List<Holder<Item>>> itemFilters,
        List<AttributeContainer> attributeContainers
) {
    // Codec used by Minecraft/NeoForge to automatically read/write data pack JSONs
    public static final Codec<Modifier> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            TagKey.codec(Registries.ITEM).listOf().optionalFieldOf("tag_filters").forGetter(Modifier::tagFilters),
            BuiltInRegistries.ITEM.holderByNameCodec().listOf().optionalFieldOf("item_filters").forGetter(Modifier::itemFilters),
            AttributeContainer.CODEC.listOf().fieldOf("attribute_containers").forGetter(Modifier::attributeContainers)
    ).apply(instance, Modifier::new));

    public boolean isValidForItem(Holder<Item> itemHolder) {
        // Prioritize item filters
        if (itemFilters.isPresent() && !itemFilters.get().isEmpty()) {
            for (Holder<Item> allowedItem : itemFilters.get()) {
                if (allowedItem.unwrapKey().equals(itemHolder.unwrapKey())) {
                    return true;
                }
            }
            return false;
        }

        if (tagFilters.isPresent() && !tagFilters.get().isEmpty()) {
            boolean matchesAnyTag = false;
            for (TagKey<Item> tag : tagFilters.get()) {
                if (itemHolder.is(tag)) {
                    matchesAnyTag = true;
                    break;
                }
            }
            return matchesAnyTag;
        }


        return true;
    }
}