package net.fabricmc.LaserMod.blocks;

import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;
import net.minecraft.block.Block;
import net.minecraft.block.BlockEntityProvider;
import net.minecraft.block.BlockState;
import net.minecraft.block.Material;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.Properties;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.World;

public class Laser extends Block implements BlockEntityProvider, LaserUpdatable {
  public Laser() {
    super(FabricBlockSettings.of(Material.METAL).hardness(3.0f).resistance(1200.0f).nonOpaque().luminance((state) -> (Boolean)state.get(Properties.LIT) ? 15 : 0).solidBlock((a, b, c) -> false));
    setDefaultState(this.stateManager.getDefaultState().with(Properties.FACING, Direction.NORTH).with(Properties.LIT, false));  
  }

  public BlockState getPlacementState(ItemPlacementContext ctx) {
    BlockState state = (BlockState)this.getDefaultState().with(Properties.FACING, ctx.getPlayerLookDirection());
    return state;
  }

  @Override
  protected void appendProperties(StateManager.Builder<Block, BlockState> stateManager) {
    stateManager.add(Properties.FACING);
    stateManager.add(Properties.LIT);
  }

  public void neighborUpdate(BlockState state, World world, BlockPos pos, Block block, BlockPos fromPos, boolean notify) {
    BlockEntity entity = world.getBlockEntity(pos);

    if (entity instanceof LaserEntity) {
      LaserEntity laserEntity = (LaserEntity)entity;

      Direction[] directions = {Direction.NORTH, Direction.SOUTH, Direction.EAST, Direction.WEST, Direction.UP, Direction.DOWN};
      
      int newFreq = -1;
      int newPower = 0;
  
      for (int i = directions.length-1; i >= 0; i--) {
        if (directions[i] != state.get(Properties.FACING).getOpposite()) {
          BlockPos neighborPos = pos.offset(directions[i], -1);
          BlockState blockState = world.getBlockState(neighborPos);
          int power = world.getEmittedRedstonePower(neighborPos, directions[i].getOpposite());
    
          if (Registry.BLOCK.getId(blockState.getBlock()).toString().equals("minecraft:comparator") && blockState.get(Properties.HORIZONTAL_FACING) == directions[i].getOpposite()) {
            // Set the frequency
            newFreq = Math.max(newFreq, power);
          } else {
            // Set the power
            newPower = Math.max(newPower, power);
          }
        }
      }
      
      // Update the power & frequency
      world.setBlockState(pos, (BlockState)state.with(Properties.LIT, newPower != 0), 2);
      laserEntity.updateLaserData(newFreq, newPower);
    }
  }

  public void laserUpdate(BlockState state, World world, BlockPos pos) {
    world.updateComparators(pos, this);
  }

  public boolean hasComparatorOutput(BlockState state) {
    return true;
  }

  @Override
  public int getComparatorOutput(BlockState state, World world, BlockPos pos) {
    return ((LaserEntity)world.getBlockEntity(pos)).comparatorPower;
  }

  @Override
  public BlockEntity createBlockEntity(net.minecraft.world.BlockView world) {
    return new LaserEntity();
  }

  public void onBlockAdded(BlockState state, World world, BlockPos pos, BlockState oldState, boolean notify) {
    // Update when the block is placed
    world.updateNeighbor(pos, this, pos);
  }
}
