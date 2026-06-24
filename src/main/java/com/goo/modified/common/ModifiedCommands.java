package com.goo.modified.common;

import com.goo.goo_lib.utils.text.StyleEffectUtils;
import com.goo.modified.common.events.ModRegistryEvents;
import com.goo.modified.utils.*;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.ResourceLocationArgument;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

import java.util.Optional;
import java.util.stream.Collectors;

public class ModifiedCommands {

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("modified")
                .requires(source -> source.hasPermission(2)) // Requires OP level 2
                .then(Commands.literal("modifier")
                        .then(Commands.literal("debug")
                                .executes(ModifiedCommands::printAllModifiers))
                        .then(Commands.literal("add")
                                .then(Commands.argument("modifier", ResourceLocationArgument.id())
                                        .suggests((ctx, builder) -> {
                                            var registries = ctx.getSource().registryAccess();
                                            var registry = registries.registry(ModRegistryEvents.MODIFIER_REGISTRY_KEY);

                                            return registry.map(modifiers -> SharedSuggestionProvider.suggestResource(modifiers.keySet(), builder)).orElseGet(builder::buildFuture);
                                        })
                                        .executes(c -> executeAddModifier(c, false))
                                        .then(Commands.argument("force", BoolArgumentType.bool())
                                                .executes(c -> executeAddModifier(c, BoolArgumentType.getBool(c, "force")))
                                        )
                                )
                        )
                        .then(Commands.literal("remove")
                                .then(Commands.argument("modifier", ResourceLocationArgument.id())
                                        .suggests((ctx, builder) -> {
                                            var source = ctx.getSource();

                                            if (source.getEntity() instanceof net.minecraft.world.entity.LivingEntity livingEntity) {
                                                var stack = livingEntity.getMainHandItem();

                                                var activeModifierIds = ModifierUtils.getModifierIds(stack);

                                                return SharedSuggestionProvider.suggestResource(activeModifierIds, builder);
                                            }

                                            return builder.buildFuture();
                                        })
                                        .executes(ModifiedCommands::executeRemoveModifier)
                                )
                        ))
                .then(Commands.literal("tier")
                        .then(Commands.literal("set")
                                .then(Commands.argument("tier", ResourceLocationArgument.id()) // Swapped to ResourceLocation id argument
                                        .suggests((ctx, builder) -> {
                                            var registries = ctx.getSource().registryAccess();
                                            var registry = registries.registry(ModRegistryEvents.TIER_REGISTRY_KEY);

                                            return registry.map(tiers -> SharedSuggestionProvider.suggestResource(tiers.keySet(), builder)).orElseGet(builder::buildFuture);
                                        })
                                        .executes(ModifiedCommands::executeSetTier)
                                )
                        )
                        .then(Commands.literal("ascend").executes(ModifiedCommands::executeAscendTier))
                )
        );
    }


    private static int printAllModifiers(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        CommandSourceStack source = context.getSource();
        Player player = source.getPlayerOrException();

        RegistryAccess registryAccess = source.getLevel().registryAccess();
        Optional<Registry<Modifier>> modifierRegistryOptional = registryAccess.registry(ModRegistryEvents.MODIFIER_REGISTRY_KEY);

        if (modifierRegistryOptional.isPresent()) {
            Registry<Modifier> modifierRegistry = modifierRegistryOptional.get();

            for (Modifier modifier : modifierRegistry) {
                ResourceLocation id = modifierRegistry.getKey(modifier);
                player.sendSystemMessage(Component.literal("=== Modifier: " + id + " ===").withStyle(ChatFormatting.GOLD));
                modifier.tagFilters().ifPresent(tagFilterList ->
                        player.sendSystemMessage(Component.literal("Tag Filters: " + tagFilterList.stream()
                                .map(tag -> tag.location().toString())
                                .collect(Collectors.joining(" : ")))));
                modifier.itemFilters().ifPresent(itemFilterList ->
                        player.sendSystemMessage(Component.literal("Item Filters: " + itemFilterList.stream()
                                .map(holder -> holder.unwrapKey()
                                        .map(key -> key.location().toString())
                                        .orElse("unknown"))
                                .collect(Collectors.joining(" : "))))
                );

                ModifierUtils.createModifierTooltipLines(modifier, null, TooltipFlag.NORMAL, registryAccess).forEach(player::sendSystemMessage);
            }

            return 1;
        }
        source.sendFailure(Component.literal("Modifier registry could not be found!"));
        return 0;
    }

    private static int executeAscendTier(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        CommandSourceStack source = context.getSource();
        Player player = source.getPlayerOrException();
        ItemStack stack = player.getMainHandItem();

        if (stack.isEmpty()) {
            source.sendFailure(Component.literal("You must be holding an item to set a Tier!"));
            return 0;
        }

        boolean success = TierUtils.ascendTier(source.registryAccess(), stack);
        if (success) {
            ResourceLocation tierId = TierUtils.getTierId(stack);
            var registry = source.registryAccess().registryOrThrow(ModRegistryEvents.TIER_REGISTRY_KEY);

            Tier tier = registry.get(tierId);
            if (tier != null) {
                MutableComponent currentTier = Component.translatable("modified.tier." + tier.name());
                StyleEffectUtils.withEffects(currentTier, tier.textEffects());
                source.sendSuccess(() -> Component.literal("Successfully set Tier to: ").append(currentTier), true);
            } else {
                source.sendSuccess(() -> Component.literal("Successfully set Tier to: " + tierId), true);
            }
            return 1;
        } else {
            source.sendFailure(Component.literal("Could not ascend Item!"));
            return 0;
        }
    }

    private static int executeSetTier(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        CommandSourceStack source = context.getSource();
        Player player = source.getPlayerOrException();
        ResourceLocation tierId = ResourceLocationArgument.getId(context, "tier");
        ItemStack stack = player.getMainHandItem();

        if (stack.isEmpty()) {
            source.sendFailure(Component.literal("You must be holding an item to set a Tier!"));
            return 0;
        }

        Registry<Tier> registry = source.registryAccess().registryOrThrow(ModRegistryEvents.TIER_REGISTRY_KEY);
        Tier tier = registry.get(tierId);

        if (!registry.containsKey(tierId)) {
            if (tier != null) {
                MutableComponent translatable = Component.translatable("modified.tier." + tier.name());
                StyleEffectUtils.withEffects(translatable, tier.textEffects());
                source.sendFailure(Component.literal("Unknown tier: " + translatable));
            } else {
                source.sendFailure(Component.literal("Unknown tier: " + tierId));
            }
            return 0;
        }

        boolean success = TierUtils.addTier(stack, tierId);

        if (success) {
            if (tier != null) {
                MutableComponent translatable = Component.translatable("modified.tier." + tier.name());
                StyleEffectUtils.withEffects(translatable, tier.textEffects());
                source.sendSuccess(() -> Component.literal("Successfully set Tier to: ").append(translatable), true);
            }
            source.sendSuccess(() -> Component.literal("Successfully set Tier to: " + tierId), true);
            return 1;
        } else {
            source.sendFailure(Component.literal("Could not set Tier!"));
            return 0;
        }
    }


    private static int executeAddModifier(CommandContext<CommandSourceStack> context, boolean force) throws CommandSyntaxException {
        CommandSourceStack source = context.getSource();
        Player player = source.getPlayerOrException();
        ResourceLocation input = ResourceLocationArgument.getId(context, "modifier");
        ItemStack stack = player.getMainHandItem();

        if (stack.isEmpty()) {
            source.sendFailure(Component.literal("You must be holding an item to add a modifier!"));
            return 0;
        }

        var modifierRegistryOptional = source.getLevel().registryAccess().registry(ModRegistryEvents.MODIFIER_REGISTRY_KEY);

        if (modifierRegistryOptional.isEmpty()) {
            source.sendFailure(Component.literal("Internal Error: The Modifier registry is not loaded or active in this level."));
            return 0;
        }

        var modifierRegistry = modifierRegistryOptional.get();
        Modifier modifier = modifierRegistry.get(input);

        if (modifier == null) {
            source.sendFailure(Component.literal("Unknown modifier: " + input));
            return 0;
        }

        boolean success;
        if (force) {
            ModifierUtils.forceAddModifier(stack, input);
            success = true;
        } else {
            success = ModifierUtils.addModifier(stack, input);
        }

        if (success) {
            source.sendSuccess(() -> Component.literal("Successfully added modifier: " + input + (force ? " (Forced)" : "")), true);
            return 1;
        } else {
            source.sendFailure(Component.literal("Could not add modifier! Check limits, duplicates, or item filters."));
            return 0;
        }
    }

    private static int executeRemoveModifier(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        CommandSourceStack source = context.getSource();
        Player player = source.getPlayerOrException();
        ResourceLocation input = ResourceLocationArgument.getId(context, "modifier");
        ItemStack stack = player.getMainHandItem();

        if (stack.isEmpty()) {
            source.sendFailure(Component.literal("You must be holding an item to remove a modifier!"));
            return 0;
        }

        Registry<Modifier> modifierRegistry = source.getLevel().registryAccess().registryOrThrow(ModRegistryEvents.MODIFIER_REGISTRY_KEY);
        Modifier modifier = modifierRegistry.get(input);

        if (modifier == null) {
            source.sendFailure(Component.literal("Unknown modifier: " + input));
            return 0;
        }

        boolean success = ModifierUtils.removeModifier(stack, input);

        if (success) {
            source.sendSuccess(() -> Component.literal("Successfully removed modifier: " + input), true);
            return 1;
        } else {
            source.sendFailure(Component.literal("That item doesn't have the modifier: " + input));
            return 0;
        }
    }
}