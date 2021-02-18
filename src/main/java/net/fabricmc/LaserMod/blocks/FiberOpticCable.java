package net.fabricmc.LaserMod.blocks;

import net.fabricmc.LaserMod.fiberOpticUpdates;
import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Material;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.DirectionProperty;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;

public class FiberOpticCable extends Block {
  public static DirectionProperty Input = DirectionProperty.of("input", Direction.NORTH, Direction.EAST, Direction.SOUTH, Direction.WEST, Direction.UP, Direction.DOWN);
  public static DirectionProperty Output = DirectionProperty.of("output", Direction.NORTH, Direction.EAST, Direction.SOUTH, Direction.WEST, Direction.UP, Direction.DOWN);

  public FiberOpticCable() {
    super(FabricBlockSettings.of(Material.METAL).hardness(3.0f).resistance(1200.0f).nonOpaque().solidBlock((a, b, c) -> false));
    setDefaultState(this.stateManager.getDefaultState().with(Output, Direction.NORTH).with(Input, Direction.SOUTH));
  }

  public BlockState getPlacementState(ItemPlacementContext ctx) {
    return (BlockState)this.getDefaultState().with(Output, ctx.getPlayerLookDirection()).with(Input, ctx.getSide());
  }

  @Override
  protected void appendProperties(StateManager.Builder<Block, BlockState> stateManager) {
    stateManager.add(Input).add(Output);
  } 

  @Override
  public void onStateReplaced(BlockState state, World world, BlockPos pos, BlockState newState, boolean moved) {
    super.onStateReplaced(state, world, pos, newState, moved);

    if (world.isClient()) return;

    System.out.println(state);
    System.out.println(pos);
    System.out.println(moved);

    BlockPos end = fiberOpticUpdates.getOtherSide(pos, world, state);
    Block endStateBlock = world.getBlockState(end).getBlock();
    
    if (endStateBlock instanceof Coupler) { // If it's not a coupler, then the other end wouldn't have been able to send a laser anyways
      ((Coupler)endStateBlock).fiberOpticUpdate(world, end);
    }
  }

  @Override
  public void onBlockAdded(BlockState state, World world, BlockPos pos, BlockState oldState, boolean notify) {
    super.onBlockAdded(state, world, pos, oldState, notify);

    if (world.isClient()) return;

    BlockPos end = fiberOpticUpdates.getOtherSide(pos, world);
    Block block = world.getBlockState(end).getBlock();
    if (block instanceof Coupler) { // If it's not a coupler, then the other end wouldn't be able to send a laser anyways
      ((Coupler)block).fiberOpticUpdate(world, end);
    }
  }
}
