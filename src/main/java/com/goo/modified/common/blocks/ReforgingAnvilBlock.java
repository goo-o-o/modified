package com.goo.modified.common.blocks;

import com.goo.modified.client.gui.reforge.ReforgingMenu;
import com.goo.modified.common.Modified;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.CraftingTableBlock;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.NotNull;

public class ReforgingAnvilBlock extends CraftingTableBlock {
    public static final MapCodec<ReforgingAnvilBlock> CODEC = simpleCodec(ReforgingAnvilBlock::new);
    private static final Component CONTAINER_TITLE = Component.translatable("container." + Modified.MOD_ID + ".reforging_anvil");
    public static final DirectionProperty FACING = HorizontalDirectionalBlock.FACING;

    private static final VoxelShape SHAPE_NORTH = Shapes.or(
            Block.box(3, 0, 4, 13, 2, 12),
            Block.box(5, 2, 5, 11, 5, 11),
            Block.box(2, 5, 3, 19, 10, 13),
            Block.box(-3, 8, 3, 2, 10, 13)
    );

    // Flips X and Z coordinates for East-facing placement
    private static final VoxelShape SHAPE_EAST = Shapes.or(
            Block.box(4, 0, 3, 12, 2, 13),
            Block.box(5, 2, 5, 11, 5, 11),
            Block.box(3, 5, 2, 13, 10, 19),
            Block.box(3, 8, -3, 13, 10, 2)
    );

    // Mirrors North coordinates for South-facing placement
    private static final VoxelShape SHAPE_SOUTH = Shapes.or(
            Block.box(3, 0, 4, 13, 2, 12),
            Block.box(5, 2, 5, 11, 5, 11),
            Block.box(-3, 5, 3, 14, 10, 13),
            Block.box(14, 8, 3, 19, 10, 13)
    );

    // Flips X and Z coordinates for West-facing placement
    private static final VoxelShape SHAPE_WEST = Shapes.or(
            Block.box(4, 0, 3, 12, 2, 13),
            Block.box(5, 2, 5, 11, 5, 11),
            Block.box(3, 5, -3, 13, 10, 14),
            Block.box(3, 8, 14, 13, 10, 19)
    );

    @Override
    public @NotNull MapCodec<ReforgingAnvilBlock> codec() {
        return CODEC;
    }

    public ReforgingAnvilBlock(BlockBehaviour.Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.NORTH));
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return this.defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite());
    }
    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }

    @Override
    public @NotNull VoxelShape getShape(BlockState state, @NotNull BlockGetter level, @NotNull BlockPos pos, CollisionContext context) {
        return switch (state.getValue(FACING)) {
            case EAST -> SHAPE_EAST;
            case SOUTH -> SHAPE_SOUTH;
            case WEST -> SHAPE_WEST;
            default -> SHAPE_NORTH;
        };
    }

    @Override
    protected @NotNull MenuProvider getMenuProvider(BlockState state, Level level, BlockPos pos) {
        return new SimpleMenuProvider(
                (i, inventory, player) -> new ReforgingMenu(i, inventory, ContainerLevelAccess.create(level, pos)), CONTAINER_TITLE
        );
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        if (level.isClientSide) {
            return InteractionResult.SUCCESS;
        } else {
            player.openMenu(state.getMenuProvider(level, pos));
            return InteractionResult.CONSUME;
        }
    }
}
