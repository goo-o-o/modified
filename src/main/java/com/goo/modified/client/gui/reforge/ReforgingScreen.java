package com.goo.modified.client.gui.reforge;

import com.goo.modified.common.Modified;
import com.goo.modified.utils.Modifier;
import com.goo.modified.utils.ModifierUtils;
import com.goo.modified.utils.Tier;
import com.goo.modified.utils.TierUtils;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class ReforgingScreen extends AbstractContainerScreen<ReforgingMenu> {
    private static final ResourceLocation TEXTURE = Modified.loc("textures/gui/container/reforging_anvil.png");
    static final ResourceLocation[] LEVEL_SPRITES = new ResourceLocation[]{
            ResourceLocation.withDefaultNamespace("container/enchanting_table/level_3_disabled"),
            ResourceLocation.withDefaultNamespace("container/enchanting_table/level_3")
    };

    private final Inventory playerInventory;
    private ReforgeSlotList slotList;
    private Button ascendButton;
    // caches
    private int cachedPlayerXp = -1;
    private boolean cachedCreativeMode = false;
    private ItemStack cachedStack = ItemStack.EMPTY;

    public ReforgingScreen(ReforgingMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
        this.playerInventory = playerInventory;
        this.imageWidth = 256;
        this.imageHeight = 256;
        this.titleLabelX = 8;
        this.titleLabelY = 6;
        this.inventoryLabelX = 8;
        this.inventoryLabelY = 162;
    }

    @Override
    protected void init() {
        super.init();

        this.ascendButton = this.addRenderableWidget(Button.builder(
                        Component.translatable("block." + Modified.MOD_ID + ".reforging_anvil.ascend"),
                        button -> this.handleAscendPressed())
                .bounds(this.leftPos + 7, this.topPos + 140, 162, 18)
                .build());
        this.ascendButton.active = false;

        this.slotList = new ReforgeSlotList(minecraft, 160, 95, this.topPos + 40, 19);
        this.slotList.setRenderHeader(false, 0);
        this.slotList.setX(this.leftPos + 8);
        this.addRenderableWidget(this.slotList);

        this.cachedStack = ItemStack.EMPTY;
    }
    @Override
    protected void containerTick() {
        ItemStack currentStack = this.menu.getSlot(0).getItem();
        Player player = this.playerInventory.player;
        this.ascendButton.active = TierUtils.canAscend(currentStack, player);

        // Capture current player state variables
        int currentXp = player.experienceLevel;
        boolean isCreative = player.isCreative();

        // check if item changed or if player xp changed or if they changed game modes
        if (ItemStack.matches(this.cachedStack, currentStack) &&
                ItemStack.isSameItemSameComponents(this.cachedStack, currentStack) &&
                this.cachedPlayerXp == currentXp &&
                this.cachedCreativeMode == isCreative) {
            return;
        }

        // update cache variables
        this.cachedStack = currentStack.copy();
        this.cachedPlayerXp = currentXp;
        this.cachedCreativeMode = isCreative;

        // reset tooltip
        this.ascendButton.setTooltip(null);

        if (!isCreative) {
            int requiredLevels = TierUtils.getLevelRequirementForAscend(currentStack, player);
            boolean isMaxTier = TierUtils.isMaxTier(player.level().registryAccess(), currentStack);
            if (isMaxTier) {
                ascendButton.setTooltip(Tooltip.create(Component.translatable("gui." + Modified.MOD_ID + ".max_tier_hint").withStyle(ChatFormatting.RED)));
            } else if (currentXp < requiredLevels) {
                ascendButton.setTooltip(Tooltip.create(Component.translatable("container.enchant.level.requirement", requiredLevels).withStyle(ChatFormatting.RED)));
            }
        }

        this.slotList.clearEntries();

        if (currentStack.isEmpty()) {
            this.ascendButton.active = false;
            return;
        }

        Tier currentTier = TierUtils.getTier(Modified.getSafeRegistryAccess(), currentStack);
        if (currentTier != null) {
            List<Modifier> modifiers = ModifierUtils.getModifiers(currentStack);
            int totalSlots = currentTier.slots();

            for (int i = 0; i < modifiers.size(); i++) {
                this.slotList.addEntry(new ReforgeSlotRowWidget(i, modifiers.get(i), currentTier, this.menu.containerId, player));
            }

            for (int i = modifiers.size(); i < totalSlots; i++) {
                this.slotList.addEntry(new ReforgeSlotRowWidget(i, null, currentTier, this.menu.containerId, player));
            }
        }
    }

    private void handleAscendPressed() {
        if (this.minecraft != null && this.minecraft.gameMode != null) {
            this.minecraft.gameMode.handleInventoryButtonClick(this.menu.containerId, 0);
            this.cachedStack = ItemStack.EMPTY; // Force sync loop execution on next frame
            this.ascendButton.setFocused(false);
            this.setFocused(null);
        }
    }

    @Override
    public void render(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(guiGraphics, mouseX, mouseY, partialTick);
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        this.renderTooltip(guiGraphics, mouseX, mouseY);

        guiGraphics.blitSprite(LEVEL_SPRITES[this.ascendButton.active ? 1 : 0], this.ascendButton.getRight() - 17, this.ascendButton.getY() + 1, 16, 16);
        guiGraphics.blitSprite(LEVEL_SPRITES[1], this.leftPos + 9, this.topPos + 24, 16, 16);
    }

    @Override
    protected void renderBg(@NotNull GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.setShaderTexture(0, TEXTURE);
        guiGraphics.blit(TEXTURE, this.leftPos, this.topPos, 0, 0, this.imageWidth, this.imageHeight);
    }
}