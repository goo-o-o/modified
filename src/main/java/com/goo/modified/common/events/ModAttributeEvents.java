package com.goo.modified.common.events;

import com.goo.goo_lib.utils.attribute.AttributeContainer;
import com.goo.modified.common.registry.ModifiedComponents;
import com.goo.modified.utils.*;
import com.goo.modified.common.Modified;
import com.goo.modified.utils.Tier;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.*;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.common.Tags;
import net.neoforged.neoforge.event.ItemAttributeModifierEvent;
import net.neoforged.neoforge.event.entity.EntityAttributeModificationEvent;

import java.util.List;

@EventBusSubscriber(modid = Modified.MOD_ID)
public class ModAttributeEvents {


    @SubscribeEvent
    public static void modifyItemAttributes(ItemAttributeModifierEvent event) {
        ItemStack stack = event.getItemStack();
        if (stack.isEmpty()) return;

        List<ResourceLocation> activeModifierIds = stack.get(ModifiedComponents.ITEM_MODIFIERS.get());
        if (activeModifierIds == null || activeModifierIds.isEmpty()) return;

        Tier tier = TierUtils.getTier(Modified.getSafeRegistryAccess(), stack);
        if (tier == null) return;

        RegistryAccess registryAccess = Modified.getSafeRegistryAccess();
        if (registryAccess == null) return;

        Registry<Modifier> modifierRegistry = registryAccess.registryOrThrow(ModRegistryEvents.MODIFIER_REGISTRY_KEY);

        for (ResourceLocation modifierId : activeModifierIds) {
            Modifier modifier = modifierRegistry.get(modifierId);
            if (modifier == null) continue;

            EquipmentSlotGroup targetSlotGroup = getCorrectSlotGroup(stack);

            for (AttributeContainer container : modifier.attributeContainers()) {
                ResourceLocation uniqueModId = ResourceLocation.fromNamespaceAndPath(
                        Modified.MOD_ID,
                        modifierId.getPath() + "_" + targetSlotGroup.getSerializedName()
                );

                AttributeModifier vanillaModifier = new AttributeModifier(
                        uniqueModId,
                        container.value() * tier.statMultiplier(),
                        container.operation()
                );

                event.addModifier(container.attribute(), vanillaModifier, targetSlotGroup);
            }
        }
    }


    /**
     * Helper method to automatically categorize items into their active EquipmentSlotGroups
     */
    public static EquipmentSlotGroup getCorrectSlotGroup(ItemStack stack) {
        Item item = stack.getItem();

        if (stack.is(Tags.Items.ARMORS) || item instanceof ArmorItem) {
            return EquipmentSlotGroup.bySlot(((ArmorItem) item).getType().getSlot());
        }
        return EquipmentSlotGroup.HAND;
    }
}