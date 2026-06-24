package com.goo.modified.common.loot;


import com.goo.modified.utils.ModifierUtils;
import com.goo.modified.utils.Tier;
import com.goo.modified.utils.TierUtils;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.core.RegistryAccess;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.neoforged.neoforge.common.Tags;
import net.neoforged.neoforge.common.loot.IGlobalLootModifier;
import net.neoforged.neoforge.common.loot.LootModifier;
import org.jetbrains.annotations.NotNull;

public class ItemModifierLootModifier extends LootModifier {

    // Codec needed for NeoForge to recognize your modifier from JSON definitions
    public static final MapCodec<ItemModifierLootModifier> CODEC = RecordCodecBuilder.mapCodec(inst ->
            LootModifier.codecStart(inst).apply(inst, ItemModifierLootModifier::new)
    );

    public ItemModifierLootModifier(LootItemCondition[] conditionsIn) {
        super(conditionsIn);
    }

    @Override
    protected @NotNull ObjectArrayList<ItemStack> doApply(ObjectArrayList<ItemStack> generatedLoot, LootContext context) {
        RegistryAccess registryAccess = context.getLevel().registryAccess();
        for (ItemStack stack : generatedLoot) {
            if (!stack.isEmpty()) {
                if (stack.is(Tags.Items.TOOLS) || stack.is(Tags.Items.ARMORS)) {
                    TierUtils.rollAndAddTier(registryAccess, stack, 0);
                    Tier tier = TierUtils.getTier(registryAccess, stack);
                    int slots = tier.slots();
                    if (slots > 0) {
                        int toRoll = Mth.nextInt(context.getRandom(), 0, slots);
                        for (int i = 0; i < toRoll; i++) {
                            ModifierUtils.rerollModifier(stack, i);
                        }
                    }
                }
            }
        }
        return generatedLoot;
    }

    @Override
    public @NotNull MapCodec<? extends IGlobalLootModifier> codec() {
        return CODEC;
    }
}