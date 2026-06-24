package com.goo.modified.client.events;


import com.goo.goo_lib.utils.text.StyleEffectUtils;
import com.goo.modified.client.registry.ModifiedKeybinds;
import com.goo.modified.common.ClientConfig;
import com.goo.modified.common.Modified;
import com.goo.modified.common.events.ModAttributeEvents;
import com.goo.modified.common.events.ModRegistryEvents;
import com.goo.modified.utils.Modifier;
import com.goo.modified.utils.ModifierUtils;
import com.goo.modified.utils.Tier;
import com.goo.modified.utils.TierUtils;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.AddAttributeTooltipsEvent;
import net.neoforged.neoforge.client.event.GatherSkippedAttributeTooltipsEvent;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;

import java.util.List;

@EventBusSubscriber(modid = Modified.MOD_ID, value = Dist.CLIENT)
public class TooltipEvents {

    @SubscribeEvent
    public static void onTooltipRender(ItemTooltipEvent event) {
        // 1. Thread safety: Only run keybind checks if we are physically on the client side
        if (event.getEntity() != null && event.getEntity().level().isClientSide()) {
            if (ClientConfig.REQUIRE_KEYBIND_TO_SHOW_RARITY.isTrue() &&
                    !InputConstants.isKeyDown(
                            Minecraft.getInstance().getWindow().getWindow(),
                            ModifiedKeybinds.SHOW_RARITY.getKey().getValue()
                    )) {
                return;
            }
        }

        // 2. Fetch the RegistryAccess safely from the context
        var level = event.getContext().level();
        if (level == null) return;
        var registryAccess = level.registryAccess();

        // 3. Optimize Lookups: Read NBT ID first (extremely fast primitive check)
        ResourceLocation tierId = TierUtils.getTierId(event.getItemStack());
        if (tierId == null) return;

        // 4. Fetch the data-driven Tier once
        var tierRegistry = registryAccess.registryOrThrow(ModRegistryEvents.TIER_REGISTRY_KEY);
        Tier tier = tierRegistry.get(tierId);

        // 5. Apply the tooltip component cleanly
        if (tier != null) {
            MutableComponent translatable = Component.translatable("modified.tier." + tier.name());
            StyleEffectUtils.withEffects(translatable, tier.textEffects());
            event.getToolTip().add(1, translatable);
        }
    }

    @SubscribeEvent
    public static void onGatherSkippedTooltips(GatherSkippedAttributeTooltipsEvent event) {
        ItemStack stack = event.getStack();

        List<ResourceLocation> modifierIds = ModifierUtils.getModifierIds(stack);
        if (modifierIds.isEmpty()) return;

        for (ResourceLocation modifierId : modifierIds) {

            ResourceLocation id = ResourceLocation.fromNamespaceAndPath(
                    Modified.MOD_ID,
                    modifierId.getPath() + "_" + ModAttributeEvents.getCorrectSlotGroup(stack).getSerializedName()
            );

            event.skipId(id);
        }
    }

    @SubscribeEvent
    public static void onAddAttributeTooltips(AddAttributeTooltipsEvent event) {
        if (!event.shouldShow()) return;

        ItemStack stack = event.getStack();
        List<Modifier> modifiers = ModifierUtils.getModifiers(stack);
        Level level = event.getContext().level();
        if (level == null) return;
        Tier tier = TierUtils.getTier(level.registryAccess(), stack);
        if (modifiers.isEmpty() || tier == null) return;

        if (ClientConfig.REQUIRE_KEYBIND_TO_SHOW_STATS.isTrue() && !InputConstants.isKeyDown(Minecraft.getInstance().getWindow().getWindow(), ModifiedKeybinds.SHOW_STATS.getKey().getValue())) {
            MutableComponent keyHint = Component.translatable(
                    "tooltip.modified.show_stats_hint",
                    Component.keybind(ModifiedKeybinds.SHOW_STATS.getName())
            ).withStyle(ChatFormatting.GRAY);

            event.addTooltipLines(keyHint);
            return;
        }

        RegistryAccess registryAccess = Modified.getSafeRegistryAccess();
        if (registryAccess == null) return;

        TooltipFlag isAdvanced = event.getContext().flag();

        for (Modifier modifier : modifiers) {
            List<Component> modifierTooltip = ModifierUtils.createModifierTooltipLines(modifier, tier, isAdvanced, registryAccess);
            modifierTooltip.forEach(event::addTooltipLines);
        }
    }
}
