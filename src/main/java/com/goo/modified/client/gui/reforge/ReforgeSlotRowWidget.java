package com.goo.modified.client.gui.reforge;

import com.goo.modified.common.Modified;
import com.goo.modified.common.events.ModRegistryEvents;
import com.goo.modified.utils.Modifier;
import com.goo.modified.utils.ModifierUtils;
import com.goo.modified.utils.Tier;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.ImageButton;
import net.minecraft.client.gui.components.ObjectSelectionList;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.components.WidgetSprites;
import net.minecraft.core.RegistryAccess;
import net.minecraft.locale.Language;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.TooltipFlag;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class ReforgeSlotRowWidget extends ObjectSelectionList.Entry<ReforgeSlotRowWidget> {
    private static final int WHITE = 16777215, GRAY = 12829635, DARK_GRAY = 7039851;

    @Nullable
    private final Modifier modifier;
    private final ImageButton mainButton;
    private final ImageButton rerollButton;
    private static final WidgetSprites REROLL_SPRITES = new WidgetSprites(
            Modified.loc("reroll"),
            Modified.loc("reroll_disabled"),
            Modified.loc("reroll_focused")
    );
    private static final WidgetSprites ROW_SPRITES = new WidgetSprites(
            Modified.loc("container/reforging_anvil/reforge_slot"),
            Modified.loc("container/reforging_anvil/reforge_slot_highlighted")
    );
    private static final WidgetSprites DISABLED_SPRITES = new WidgetSprites(
            Modified.loc("container/reforging_anvil/reforge_slot_disabled"),
            Modified.loc("container/reforging_anvil/reforge_slot_disabled")
    );

    public ReforgeSlotRowWidget(int slotIndex, @Nullable Modifier modifier, Tier tier, int containerId, Player player) {
        this.modifier = modifier;

        WidgetSprites chosenSprites = DISABLED_SPRITES;
        Component buttonLabel = Component.translatable("gui." + Modified.MOD_ID + ".empty_slot");
        Tooltip tooltip = null;

        if (modifier != null) {
            RegistryAccess registryAccess = Modified.getSafeRegistryAccess();
            if (registryAccess != null) {
                ResourceLocation modifierId = registryAccess.registryOrThrow(ModRegistryEvents.MODIFIER_REGISTRY_KEY).getKey(modifier);
                if (modifierId != null) {
                    chosenSprites = ROW_SPRITES;

                    String translationKey = modifierId.getNamespace() + ".modifier." + modifierId.getPath();
                    String translatedString = Language.getInstance().getOrDefault(translationKey);
                    String cleanText = ChatFormatting.stripFormatting(translatedString);

                    buttonLabel = Component.literal(cleanText);

                    List<Component> lines = ModifierUtils.createModifierTooltipLines(modifier, tier, TooltipFlag.NORMAL, registryAccess);
                    MutableComponent combinedComponent = Component.empty();
                    for (int i = 0; i < lines.size(); i++) {
                        combinedComponent.append(lines.get(i));
                        if (i < lines.size() - 1) {
                            combinedComponent.append("\n");
                        }
                    }
                    tooltip = Tooltip.create(combinedComponent);
                }
            }
        }

        this.mainButton = new ImageButton(20, 0, 141, 19, chosenSprites, button -> {
        }) {
            @Override
            public void renderWidget(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
                super.renderWidget(guiGraphics, mouseX, mouseY, partialTick);
                int i = modifier != null ? (isHovered() ? WHITE : GRAY) : (isHoveredOrFocused() ? GRAY : DARK_GRAY);
                this.renderString(guiGraphics, Minecraft.getInstance().font, i | Mth.ceil(this.alpha * 255.0F) << 24);
            }
        };
        this.mainButton.setMessage(buttonLabel);
        if (tooltip != null) {
            this.mainButton.setTooltip(tooltip);
        }
        this.mainButton.active = false;

        this.rerollButton = new ImageButton(0, 0, 19, 19, REROLL_SPRITES, button -> {
            if (Minecraft.getInstance().gameMode != null) {
                Minecraft.getInstance().gameMode.handleInventoryButtonClick(containerId, slotIndex + 1);
            }
        });
        this.rerollButton.setTooltip(Tooltip.create(Component.translatable("gui." + Modified.MOD_ID + ".reroll")));
        this.rerollButton.active = ModifierUtils.canRerollModifier(player);

    }

    @Override
    public void render(@NotNull GuiGraphics guiGraphics, int index, int top, int left, int width, int height, int mouseX, int mouseY, boolean isHovered, float partialTick) {
        if (this.mainButton != null) {
            this.rerollButton.setX(left);
            this.rerollButton.setY(top);

            this.mainButton.setX(left + 19); // 19 for reroll btn
            this.mainButton.setY(top);

            this.rerollButton.render(guiGraphics, mouseX, mouseY, partialTick);
            this.mainButton.render(guiGraphics, mouseX, mouseY, partialTick);
        }
    }

    @Override
    public @NotNull Component getNarration() {
        RegistryAccess registryAccess = Modified.getSafeRegistryAccess();
        if (registryAccess != null && modifier != null) {
            ResourceLocation modifierId = registryAccess.registryOrThrow(ModRegistryEvents.MODIFIER_REGISTRY_KEY).getKey(modifier);
            if (modifierId != null) {
                return Component.translatable(modifierId.getNamespace() + ".modifier." + modifierId.getPath());
            }
        }
        return Component.empty();
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        return this.rerollButton.mouseClicked(mouseX, mouseY, button) || this.mainButton.mouseClicked(mouseX, mouseY, button);
    }
}