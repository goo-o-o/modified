package com.goo.modified.mixin;

import com.goo.modified.common.CommonConfig;
import com.goo.modified.utils.ModifierUtils;
import com.goo.modified.utils.Tier;
import com.goo.modified.utils.TierUtils;
import net.minecraft.core.RegistryAccess;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.CraftingMenu;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.common.Tags;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import com.llamalad7.mixinextras.sugar.Local;

@Mixin(CraftingMenu.class)
public class CraftingMenuMixin {

    @Inject(
            method = "quickMoveStack(Lnet/minecraft/world/entity/player/Player;I)Lnet/minecraft/world/item/ItemStack;",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/inventory/CraftingMenu;moveItemStackTo(Lnet/minecraft/world/item/ItemStack;IIZ)Z"
            )
    )
    private void onBatchCraftItemTransfer(Player player, int slotIndex, CallbackInfoReturnable<ItemStack> cir, @Local(ordinal = 1) ItemStack stack) {
        if (!player.level().isClientSide() && stack != null && !stack.isEmpty()) {
            if (stack.is(Tags.Items.TOOLS) || stack.is(Tags.Items.ARMORS)) {
                RegistryAccess registryAccess = player.level().registryAccess();
                TierUtils.rollAndAddTier(registryAccess, stack, CommonConfig.USE_VANILLA_LUCK.get() ? player.getLuck() : 1);
                Tier tier = TierUtils.getTier(registryAccess, stack);
                int slots = tier.slots();
                if (slots > 0) {
                    int toRoll = Mth.nextInt(player.level().random, 0, slots);
                    for (int i = 0; i < toRoll; i++) {
                        ModifierUtils.rerollModifier(stack, i);
                    }
                }
            }
        }
    }
}