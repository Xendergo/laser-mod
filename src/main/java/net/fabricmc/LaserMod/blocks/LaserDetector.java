package net.fabricmc.LaserMod.blocks;

import java.util.Random;

import net.fabricmc.LaserMod.LaserStorageClient;
import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Material;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.Properties;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.BlockView;

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

  @Override
  public void scheduledTick(BlockState state, ServerWorld world, BlockPos pos, Random random) {
    super.scheduledTick(state, world, pos, random);

    world.updateNeighbors(pos, this);

    world.setBlockState(pos, (BlockState)state.with(Properties.LIT, LaserStorageClient.laserPowerAtSpot(pos, state.get(Properties.FACING).getOpposite()) != 0), 2);
  }

  public int getWeakRedstonePower(BlockState state, BlockView world, BlockPos pos, Direction direction) {
    return Math.min((int)Math.ceil(LaserStorageClient.laserPowerAtSpot(pos, state.get(Properties.FACING).getOpposite())), 15);
  }
}
