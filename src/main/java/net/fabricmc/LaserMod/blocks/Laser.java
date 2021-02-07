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

public class Laser extends Block implements BlockEntityProvider {
  public Laser() {
    super(FabricBlockSettings.of(Material.METAL).hardness(3.0f).resistance(1200.0f).nonOpaque());
    setDefaultState(this.stateManager.getDefaultState().with(Properties.FACING, Direction.NORTH));
  }

  public BlockState getPlacementState(ItemPlacementContext ctx) {
    return (BlockState)this.getDefaultState().with(Properties.FACING, ctx.getPlayerLookDirection());
  }

  @Override
  protected void appendProperties(StateManager.Builder<Block, BlockState> stateManager) {
    stateManager.add(Properties.FACING);
  }

  public void neighborUpdate(BlockState state, World world, BlockPos pos, Block block, BlockPos fromPos, boolean notify) {
    BlockEntity entity = world.getBlockEntity(pos);

    if (entity instanceof LaserEntity) {
      LaserEntity laserEntity = (LaserEntity)entity;

      Direction[] directions = {Direction.NORTH, Direction.SOUTH, Direction.EAST, Direction.WEST, Direction.UP, Direction.DOWN};
      
      int newFreq = 0;
      int newPower = 0;
  
      for (int i = directions.length-1; i >= 0; i--) {
        int power = world.getEmittedRedstonePower(pos, directions[i]);
        BlockPos neighborPos = pos.offset(directions[i], -1);
        BlockState blockState = world.getBlockState(neighborPos);
  
        if (Registry.BLOCK.getId(blockState.getBlock()).toString().equals("minecraft:comparator")) {
          // Set the frequency
          newFreq = Math.max(newFreq, power);
        } else {
          // Set the power
          newPower = Math.max(newPower, power);
        }
      }
      
      // Update the power & frequency
      laserEntity.updateLaserData(newFreq, newPower);
    }
  }

  @Override
  public BlockEntity createBlockEntity(net.minecraft.world.BlockView world) {
    return new LaserEntity();
  }
}
