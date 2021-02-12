package net.fabricmc.LaserMod.blocks;

import net.fabricmc.LaserMod.LaserStorage;
import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Material;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.Properties;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;

public class LaserDetector extends Block {
  public LaserDetector() {
    super(FabricBlockSettings.of(Material.METAL).hardness(3.0f).resistance(1200.0f).nonOpaque().luminance((state) -> (Boolean)state.get(Properties.LIT) ? 7 : 0).solidBlock((a, b, c) -> false));
    setDefaultState(this.stateManager.getDefaultState().with(Properties.FACING, Direction.NORTH).with(Properties.LIT, false));
  }

  public BlockState getPlacementState(ItemPlacementContext ctx) {
    return (BlockState)this.getDefaultState().with(Properties.FACING, ctx.getPlayerLookDirection());
  }

  @Override
  protected void appendProperties(StateManager.Builder<Block, BlockState> stateManager) {
    stateManager.add(Properties.FACING);
    stateManager.add(Properties.LIT);
  }

  public boolean emitsRedstonePower(BlockState state) {
    return true;
  }

  public void laserUpdate(BlockState state, World world, BlockPos pos) {
    world.setBlockState(pos, (BlockState)state.with(Properties.LIT, LaserStorage.laserPowerAtSpot(pos, state.get(Properties.FACING).getOpposite(), world) != 0), 2);
    
    world.updateNeighbors(pos, this);
    
    world.updateComparators(pos, this);
  }

  public void neighborUpdate(BlockState state, World world, BlockPos pos, Block block, BlockPos fromPos, boolean notify) {
    world.setBlockState(pos, (BlockState)state.with(Properties.LIT, LaserStorage.laserPowerAtSpot(pos, state.get(Properties.FACING).getOpposite(), world) != 0), 2);
  }

  public boolean hasComparatorOutput(BlockState state) {
    return true;
  }

  public int getComparatorOutput(BlockState state, World world, BlockPos pos) {
    return (int)Math.floor(remap(LaserStorage.laserFreqAtSpot(pos, state.get(Properties.FACING).getOpposite(), world), 0, 16, 0, 15));
  }

  public int getWeakRedstonePower(BlockState state, BlockView world, BlockPos pos, Direction direction) {
    return Math.min((int)Math.ceil(LaserStorage.laserPowerAtSpot(pos, state.get(Properties.FACING).getOpposite(), (World)world)), 15);
  }

  private static float remap(float v, float min1, float max1, float min2, float max2) {
    return (v - min1) * (max2 - min2) / (max1 - min1) + min2;
  }
}
