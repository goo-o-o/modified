package com.goo.modified.utils;

import com.goo.goo_lib.utils.attribute.AttributeContainer;
import com.goo.modified.common.Modified;
import com.goo.modified.common.events.ModRegistryEvents;
import com.goo.modified.common.registry.ModifiedComponents;
import net.minecraft.ChatFormatting;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.neoforged.neoforge.common.extensions.IAttributeExtension;
import net.neoforged.neoforge.common.util.AttributeUtil;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class ModifierUtils {
    private static final RandomSource RANDOM = RandomSource.create();

    public static List<Component> createModifierTooltipLines(Modifier modifier, @Nullable Tier tier, TooltipFlag flag, RegistryAccess registryAccess) {
        // Optimization: Initialize the ArrayList with an explicit initial capacity guess.
        // modifier.attributeContainers().size() + 1 prevents the internal array from resizing itself mid-loop.
        List<Component> lines = new ArrayList<>(modifier.attributeContainers().size() + 1);

        var modifierRegistry = registryAccess.registryOrThrow(ModRegistryEvents.MODIFIER_REGISTRY_KEY);
        ResourceLocation modifierId = modifierRegistry.getKey(modifier);
        if (modifierId == null) return lines;

        lines.add(Component.translatable(modifierId.getNamespace() + ".modifier." + modifierId.getPath()));

        double statMultiplier = tier != null ? tier.statMultiplier() : 1;

        // attributes
        for (AttributeContainer container : modifier.attributeContainers()) {
            double value = container.value() * statMultiplier;
            Attribute attr = container.attribute().value();
            AttributeModifier.Operation op = container.operation();

            MutableComponent valueComp = attr.toValueComponent(op, value, flag);
            String key = value > 0 ? "neoforge.modifier.plus" : "neoforge.modifier.take";
            ChatFormatting color = attr.getStyle(value > 0);

            Component attrDesc = Component.translatable(attr.getDescriptionId());
            MutableComponent attributeLine = Component.translatable(key, valueComp, attrDesc).withStyle(color);
            attributeLine.append(attr.getDebugInfo(new AttributeModifier(AttributeUtil.FAKE_MERGED_ID, value, op), flag));

            lines.add(Component.literal("  ").append(attributeLine));
        }

        return lines;
    }

    public static boolean canRerollModifier(Player player) {
        return player.experienceLevel >= 3 || player.isCreative();
    }

    public static boolean rerollModifier(ItemStack stack, int index) {
        if (stack.isEmpty() || index < 0) return false;

        RegistryAccess registryAccess = Modified.getSafeRegistryAccess();
        if (registryAccess == null) return false;

        Tier tier = TierUtils.getTier(registryAccess, stack);
        int maxSlots = (tier != null) ? tier.slots() : 0;

        List<ResourceLocation> currentIds = getModifierIds(stack);
        if (index >= maxSlots) return false;

        Registry<Modifier> modifierRegistry = registryAccess.registryOrThrow(ModRegistryEvents.MODIFIER_REGISTRY_KEY);

        List<ResourceLocation> availableIds = modifierRegistry.keySet().stream()
                .filter(id -> !currentIds.contains(id))
                .filter(id -> {
                    Modifier mod = modifierRegistry.get(id);
                    return mod != null && mod.isValidForItem(stack.getItemHolder());
                })
                .toList();
        if (availableIds.isEmpty()) return false;
        ResourceLocation selectedNewId = availableIds.get(RANDOM.nextInt(availableIds.size()));

        List<ResourceLocation> updatedIds = new ArrayList<>(currentIds);
        if (index >= updatedIds.size()) {
            updatedIds.add(selectedNewId); // add
        } else {
            updatedIds.set(index, selectedNewId); // overwrite
        }

        stack.set(ModifiedComponents.ITEM_MODIFIERS.get(), updatedIds);
        return true;
    }

    public static boolean addModifier(ItemStack stack, ResourceLocation modifierId) {
        if (stack.isEmpty() || modifierId == null) return false;

        RegistryAccess registryAccess = Modified.getSafeRegistryAccess();
        if (registryAccess == null) return false;

        Registry<Modifier> modifierRegistry = registryAccess.registryOrThrow(ModRegistryEvents.MODIFIER_REGISTRY_KEY);
        Modifier modifier = modifierRegistry.get(modifierId);
        if (modifier == null || !modifier.isValidForItem(stack.getItemHolder())) return false;

        Tier tier = TierUtils.getTier(registryAccess, stack);
        int maxSlots = (tier != null) ? tier.slots() : 0;

        List<ResourceLocation> modifierIds = getModifierIds(stack);
        if (modifierIds.size() >= maxSlots || modifierIds.contains(modifierId)) return false;

        List<ResourceLocation> updatedIds = new ArrayList<>(modifierIds);
        updatedIds.add(modifierId);
        stack.set(ModifiedComponents.ITEM_MODIFIERS.get(), updatedIds);

        return true;
    }

    public static void forceAddModifier(ItemStack stack, ResourceLocation... modifierIds) {
        if (stack.isEmpty() || modifierIds == null || modifierIds.length == 0) return;

        RegistryAccess registryAccess = Modified.getSafeRegistryAccess();
        if (registryAccess == null) return;

        Registry<Modifier> modifierRegistry = registryAccess.registryOrThrow(ModRegistryEvents.MODIFIER_REGISTRY_KEY);
        List<ResourceLocation> updatedIds = new ArrayList<>(getModifierIds(stack));

        for (ResourceLocation id : modifierIds) {
            if (id == null || !modifierRegistry.containsKey(id)) continue;
            if (!updatedIds.contains(id)) {
                updatedIds.add(id);
            }
        }

        stack.set(ModifiedComponents.ITEM_MODIFIERS.get(), updatedIds);
        stack.remove(DataComponents.ATTRIBUTE_MODIFIERS);
    }

    public static boolean removeModifier(ItemStack stack, ResourceLocation modifierId) {
        if (stack.isEmpty() || modifierId == null) return false;

        List<ResourceLocation> modifierIds = getModifierIds(stack);
        if (!modifierIds.contains(modifierId)) return false;


        List<ResourceLocation> updatedIds = new ArrayList<>(modifierIds);
        updatedIds.remove(modifierId);

        if (updatedIds.isEmpty()) {
            stack.remove(ModifiedComponents.ITEM_MODIFIERS.get());
        } else {
            stack.set(ModifiedComponents.ITEM_MODIFIERS.get(), updatedIds);
        }

        return true;
    }

    public static List<ResourceLocation> getModifierIds(ItemStack stack) {
        if (stack.isEmpty()) return List.of();
        List<ResourceLocation> modifierIds = stack.get(ModifiedComponents.ITEM_MODIFIERS.get());
        return modifierIds != null ? modifierIds : List.of();
    }

    public static List<Modifier> getModifiers(ItemStack stack) {
        List<Modifier> foundModifiers = new ArrayList<>();
        if (stack.isEmpty()) return foundModifiers;

        List<ResourceLocation> modifierIds = getModifierIds(stack);
        if (modifierIds.isEmpty()) return foundModifiers;

        RegistryAccess registryAccess = Modified.getSafeRegistryAccess();
        if (registryAccess == null) return foundModifiers;

        Registry<Modifier> modifierRegistry = registryAccess.registryOrThrow(ModRegistryEvents.MODIFIER_REGISTRY_KEY);

        for (ResourceLocation id : modifierIds) {
            Modifier modifier = modifierRegistry.get(id);
            if (modifier != null) {
                foundModifiers.add(modifier);
            }
        }

        return foundModifiers;
    }

}