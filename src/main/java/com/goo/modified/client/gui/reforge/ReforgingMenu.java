package com.goo.modified.client.gui.reforge;

import com.goo.modified.common.registry.ModifiedBlocks;
import com.goo.modified.common.registry.ModifiedMenuTypes;
import com.goo.modified.utils.ModifierUtils;
import com.goo.modified.utils.TierUtils;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

public class ReforgingMenu extends AbstractContainerMenu {
    private final ContainerLevelAccess access;
    private final Container reforgeContainer = new SimpleContainer(1);

    public ReforgingMenu(int containerId, Inventory playerInventory) {
        this(containerId, playerInventory, ContainerLevelAccess.NULL);
    }

    public ReforgingMenu(int containerId, Inventory playerInventory, ContainerLevelAccess access) {
        super(ModifiedMenuTypes.REFORGING.get(), containerId);
        this.access = access;

        this.addSlot(new Slot(this.reforgeContainer, 0, 80, 18) {
            @Override
            public boolean mayPlace(@NotNull ItemStack stack) {
                return !TierUtils.getAvailableTiers(playerInventory.player.registryAccess(), stack).isEmpty();
            }

            @Override
            public int getMaxStackSize() {
                return 1;
            }
        });

        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 9; j++) {
                this.addSlot(new Slot(playerInventory, j + i * 9 + 9, 8 + j * 18, 174 + i * 18));
            }
        }

        for (int k = 0; k < 9; k++) {
            this.addSlot(new Slot(playerInventory, k, 8 + k * 18, 232));
        }
    }

    @Override
    public boolean stillValid(@NotNull Player player) {
        return stillValid(this.access, player, ModifiedBlocks.REFORGING_ANVIL.get());
    }

    @Override
    public @NotNull ItemStack quickMoveStack(@NotNull Player player, int index) {
        ItemStack itemstack = ItemStack.EMPTY;
        Slot slot = this.slots.get(index);

        if (slot.hasItem()) {
            ItemStack sourceStack = slot.getItem();
            itemstack = sourceStack.copy();

            if (index == 0) {
                // Shift-clicking out of the Reforge Slot -> move into player inventory/hotbar
                if (!this.moveItemStackTo(sourceStack, 1, 37, true)) {
                    return ItemStack.EMPTY;
                }
            } else {
                // Shift-clicking from player inventory -> move into the Reforge Slot (index 0)
                if (!this.moveItemStackTo(sourceStack, 0, 1, false)) {
                    return ItemStack.EMPTY;
                }
            }

            if (sourceStack.isEmpty()) {
                slot.setByPlayer(ItemStack.EMPTY);
            } else {
                slot.setChanged();
            }

            if (sourceStack.getCount() == itemstack.getCount()) {
                return ItemStack.EMPTY;
            }

            slot.onTake(player, sourceStack);
        }

        return itemstack;
    }

    @Override
    public boolean clickMenuButton(Player player, int id) {
        ItemStack processingItem = this.getSlot(0).getItem();

        if (processingItem.isEmpty()) {
            return false; // Safely cancel if there is no item to modify
        }

        if (id == 0) {
            if (TierUtils.tryAscendAndConsumeXp(processingItem, player)) {
                this.getSlot(0).setChanged();

                access.execute((level, blockPos) -> level.playSound(null, blockPos, SoundEvents.ANVIL_USE, SoundSource.BLOCKS, 1.0F, level.random.nextFloat() * 0.1F + 0.9F));
                return true;
            }
        } else if (id > 0) {
            // Reverse the client-side offset to determine the actual target slot array index
            int targetSlotIndex = id - 1;
            if (ModifierUtils.canRerollModifier(player))
                if (ModifierUtils.rerollModifier(processingItem, targetSlotIndex)) {
                    player.giveExperienceLevels(-3);
                    this.getSlot(0).setChanged();

                    access.execute((level, blockPos) -> level.playSound(null, blockPos, SoundEvents.ENCHANTMENT_TABLE_USE, SoundSource.BLOCKS, 1.0F, level.random.nextFloat() * 0.1F + 0.9F));
                    return true;
                }
        }

        return false;
    }

    @Override
    public void removed(@NotNull Player player) {
        super.removed(player);
        this.clearContainer(player, this.reforgeContainer);
    }
}
