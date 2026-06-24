package com.goo.modified.common.registry;

import com.goo.modified.common.Modified;
import com.goo.modified.common.blocks.ReforgingAnvilBlock;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class ModifiedBlocks {
    public static final DeferredRegister<Block> BLOCKS =
            DeferredRegister.create(BuiltInRegistries.BLOCK, Modified.MOD_ID);

    public static final Supplier<Block> REFORGING_ANVIL = BLOCKS.register("reforging_anvil",
            () -> new ReforgingAnvilBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.ANVIL).requiresCorrectToolForDrops()));

}
