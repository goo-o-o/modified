package com.goo.modified.mixin;

import com.goo.goo_lib.utils.text.StyleEffectUtils;
import com.goo.modified.common.ClientConfig;
import com.goo.modified.common.CommonConfig;
import com.goo.modified.common.Modified;
import com.goo.modified.utils.ModifierUtils;
import com.goo.modified.utils.Tier;
import com.goo.modified.utils.TierUtils;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.common.Tags;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import javax.annotation.Nullable;

@Mixin(ItemStack.class)

public class ItemStackMixin {


    @Inject(method = "onCraftedBySystem", at = @At("HEAD"))
    private void onCraftedBySystem(Level level, CallbackInfo ci) {
        ItemStack stack = (ItemStack) (Object) this;
        if (!level.isClientSide() && stack != null && !stack.isEmpty()) {
            if (stack.is(Tags.Items.TOOLS) || stack.is(Tags.Items.ARMORS)) {
                RegistryAccess registryAccess = level.registryAccess();
                TierUtils.rollAndAddTier(registryAccess, stack, 0);
                Tier tier = TierUtils.getTier(registryAccess, stack);
                int slots = tier.slots();
                if (slots > 0) {
                    int toRoll = Mth.nextInt(level.random, 0, slots);
                    for (int i = 0; i < toRoll; i++) {
                        ModifierUtils.rerollModifier(stack, i);
                    }
                }
            }
        }
    }

    @Inject(method = "onCraftedBy", at = @At("HEAD"))
    private void onCraftedBy(Level level, Player player, int amount, CallbackInfo ci) {
        ItemStack stack = (ItemStack) (Object) this;
        if (!level.isClientSide() && stack != null && !stack.isEmpty()) {
            if (stack.is(Tags.Items.TOOLS) || stack.is(Tags.Items.ARMORS)) {
                RegistryAccess registryAccess = level.registryAccess();
                TierUtils.rollAndAddTier(registryAccess, stack, CommonConfig.USE_VANILLA_LUCK.get() ? player.getLuck() : 1);
                Tier tier = TierUtils.getTier(registryAccess, stack);
                int slots = tier.slots();
                if (slots > 0) {
                    int toRoll = Mth.nextInt(level.random, 0, slots);
                    for (int i = 0; i < toRoll; i++) {
                        ModifierUtils.rerollModifier(stack, i);
                    }
                }
            }
        }
    }

    @ModifyReturnValue(method = "getHoverName", at = @At("RETURN"))
    private Component modifyHoverName(Component original) {
        if (ClientConfig.COLOR_ITEM_NAME_WITH_RARITY.isTrue()) {
            ItemStack itemStack = (ItemStack) (Object) this;
            Tier tier = TierUtils.getTier(Modified.getSafeRegistryAccess(), itemStack);
            if (tier != null) {
                MutableComponent styled = original.copy();
                StyleEffectUtils.withEffects(styled, tier.textEffects());
                return styled;
            }
        }
        return original;
    }
}
