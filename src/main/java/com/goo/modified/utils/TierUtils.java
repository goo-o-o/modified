package com.goo.modified.utils;

import com.goo.modified.common.CommonConfig;
import com.goo.modified.common.events.ModRegistryEvents;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

public class TierUtils {
    private static final RandomSource random = RandomSource.create();

    /**
     * Pipeline Core: Filters the live registry down to only the Tiers that accept this specific item.
     * Sequentially sorted by the tier's index configuration.
     */
    public static List<Map.Entry<ResourceLocation, Tier>> getAvailableTiers(RegistryAccess registryAccess, ItemStack stack) {
        if (stack.isEmpty()) return List.of();

        Registry<Tier> tierRegistry = registryAccess.registryOrThrow(ModRegistryEvents.TIER_REGISTRY_KEY);
        Holder<Item> itemHolder = stack.getItemHolder();

        return tierRegistry.entrySet().stream()
                .filter(entry -> entry.getValue().isValidForItem(itemHolder)) // Run the new tag/item checks
                .map(entry -> Map.entry(entry.getKey().location(), entry.getValue()))
                .sorted(Comparator.comparingInt(entry -> entry.getValue().index()))
                .toList();
    }

    public static int getLevelRequirementForAscend(ItemStack stack, Player player) {
        Tier tier = TierUtils.getTier(player.level().registryAccess(), stack);
        if (tier == null) return 0;
        int required = tier.requiredXpLevel();
        return Mth.ceil(required * CommonConfig.ASCEND_XP_COST_MULTIPLIER.getAsDouble());
    }

    public static boolean canAscend(ItemStack stack, Player player) {
        return !stack.isEmpty() && !isMaxTier(player.level().registryAccess(), stack) &&
                (player.experienceLevel >= getLevelRequirementForAscend(stack, player) || player.isCreative());
    }

    public static boolean tryAscendAndConsumeXp(ItemStack stack, Player player) {
        if (canAscend(stack, player)) {
            if (ascendTier(player.level().registryAccess(), stack)) {
                player.giveExperienceLevels(-3); // always take 3 levels
                return true;
            }
        }
        return false;
    }

    /**
     * Rolls a dynamic tier out of the validated pool and applies it directly to the item stack.
     */
    public static void rollAndAddTier(RegistryAccess registryAccess, ItemStack stack, float luck) {
        ResourceLocation rolledId = rollTier(registryAccess, stack, luck * CommonConfig.LUCK_MULTIPLIER.getAsDouble());
        if (rolledId != null) {
            addTier(stack, rolledId);
        }
    }

    public static Tier getTier(RegistryAccess registryAccess, ItemStack stack) {
        if (stack == null || stack.isEmpty()) return null;

        ResourceLocation id = getTierId(stack);
        if (id == null) return null;

        Registry<Tier> tierRegistry = registryAccess.registryOrThrow(ModRegistryEvents.TIER_REGISTRY_KEY);
        return tierRegistry.get(id);
    }

    public static ResourceLocation getTierId(ItemStack stack) {
        CustomData customData = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY);
        if (customData.isEmpty()) return null;

        CompoundTag tag = customData.copyTag();
        if (!tag.contains("tier")) return null;

        return ResourceLocation.tryParse(tag.getString("tier"));
    }

    /**
     * Clean Add Tier: Forced execution with zero criteria validation.
     * Simply overwrites the registry ID path stored on the NBT stack data layer.
     */
    public static boolean addTier(ItemStack stack, ResourceLocation tierId) {
        if (stack.isEmpty() || tierId == null) return false;

        CustomData customData = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY);
        CompoundTag tag = customData.copyTag();
        tag.putString("tier", tierId.toString());
        stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
        return true;
    }

    /**
     * Dynamically loads validated active tiers matching item criteria,
     * evaluating math weights relative to player luck properties.
     */
    public static ResourceLocation rollTier(RegistryAccess registryAccess, ItemStack stack, double luck) {
        List<Map.Entry<ResourceLocation, Tier>> validEntries = getAvailableTiers(registryAccess, stack);

        if (validEntries.isEmpty()) {
            return null;
        }

        double totalWeight = 0.0;
        List<Double> dynamicWeights = new ArrayList<>();

        for (Map.Entry<ResourceLocation, Tier> entry : validEntries) {
            Tier tier = entry.getValue();

            double luckMultiplier = Math.pow(1.1, luck * tier.index());
            double adjustedWeight = tier.baseWeight() * luckMultiplier;

            dynamicWeights.add(adjustedWeight);
            totalWeight += adjustedWeight;
        }

        if (totalWeight <= 0.0) {
            return validEntries.getFirst().getKey();
        }

        double roll = random.nextDouble() * totalWeight;
        double cumulativeWeight = 0.0;

        for (int i = 0; i < validEntries.size(); i++) {
            cumulativeWeight += dynamicWeights.get(i);
            if (roll <= cumulativeWeight) {
                return validEntries.get(i).getKey();
            }
        }

        return validEntries.getFirst().getKey();
    }

    /**
     * Finds the index position of the item's current tier relative to the VALIDATED subset pool.
     */
    public static int getCurrentTierIndex(RegistryAccess registryAccess, ItemStack stack) {
        ResourceLocation currentId = getTierId(stack);
        if (currentId == null) return -1;

        List<Map.Entry<ResourceLocation, Tier>> validEntries = getAvailableTiers(registryAccess, stack);
        for (int i = 0; i < validEntries.size(); i++) {
            if (validEntries.get(i).getKey().equals(currentId)) {
                return i;
            }
        }
        return -1;
    }

    /**
     * Calculates available remaining ascensions strictly bounded by what tiers match this item.
     */
    public static int getTiersRemaining(RegistryAccess registryAccess, ItemStack stack) {
        List<Map.Entry<ResourceLocation, Tier>> validEntries = getAvailableTiers(registryAccess, stack);
        if (validEntries.isEmpty()) return 0;

        ResourceLocation currentId = getTierId(stack);
        if (currentId == null) {
            return validEntries.size();
        }

        int currentIndex = getCurrentTierIndex(registryAccess, stack);
        if (currentIndex == -1) return 0; // Guard against an item holding a tier it shouldn't have access to

        return (validEntries.size() - 1) - currentIndex;
    }

    public static boolean isMaxTier(RegistryAccess registryAccess, ItemStack stack) {
        return getTierId(stack) != null && getTiersRemaining(registryAccess, stack) == 0;
    }

    /**
     * Progresses an item to its next valid contextual chronological layer step.
     */
    public static boolean ascendTier(RegistryAccess registryAccess, ItemStack stack) {
        if (stack.isEmpty()) return false;

        List<Map.Entry<ResourceLocation, Tier>> validEntries = getAvailableTiers(registryAccess, stack);
        if (validEntries.isEmpty()) return false;

        ResourceLocation currentId = getTierId(stack);

        // 1. Initialize if no tier exists using the lowest matching tier entry
        if (currentId == null) {
            addTier(stack, validEntries.getFirst().getKey());
            return true;
        }

        // 2. Ceiling protection check
        if (isMaxTier(registryAccess, stack)) {
            return false;
        }

        // 3. Increment index step bounded by the item's valid pool
        int currentIndex = getCurrentTierIndex(registryAccess, stack);
        int nextIndex = currentIndex + 1;

        if (nextIndex < validEntries.size()) {
            addTier(stack, validEntries.get(nextIndex).getKey());
            return true;
        }

        return false;
    }
}