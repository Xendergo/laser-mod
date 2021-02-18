package net.fabricmc.LaserMod.blocks;

import java.util.List;
import java.util.stream.Collectors;

import net.fabricmc.LaserMod.LaserStorage;
import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;
import net.minecraft.block.Block;
import net.minecraft.block.BlockEntityProvider;
import net.minecraft.block.BlockState;
import net.minecraft.block.Material;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.piston.PistonBehavior;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.Properties;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.BlockView;

public class Coupler extends Block implements BlockEntityProvider {
  public Coupler() {
    super(FabricBlockSettings.of(Material.METAL).hardness(3.0f).resistance(1200.0f).nonOpaque().luminance((state) -> (Boolean)state.get(Properties.LIT) ? 15 : 0).solidBlock((a, b, c) -> false));
    setDefaultState(this.stateManager.getDefaultState().with(Properties.FACING, Direction.NORTH).with(Properties.LIT, false));
  }

  public BlockState getPlacementState(ItemPlacementContext ctx) {
    return (BlockState)this.getDefaultState().with(Properties.FACING, ctx.getPlayerLookDirection());
  }

  @Override
  protected void appendProperties(StateManager.Builder<Block, BlockState> stateManager) {
    stateManager.add(Properties.FACING).add(Properties.LIT);
  }

  @Override
  public PistonBehavior getPistonBehavior(BlockState state) {
    return PistonBehavior.BLOCK;
  }

  @Override
  public BlockEntity createBlockEntity(BlockView world) {
    return new CouplerEntity();
  }

  public void laserUpdate(BlockState state, ServerWorld world, BlockPos pos) {
    Direction facing = state.get(Properties.FACING);
    world.setBlockState(pos, (BlockState)state.with(Properties.LIT, LaserStorage.laserPowerAtSpot(pos, facing, world) != 0), 2);
  
    int dirId = facing.getId();
    List<float[]> lasers = LaserStorage.lasersAtPos(world, pos).stream().filter(x -> x[2] >> 2 == dirId).map(x -> new float[] {Float.intBitsToFloat(x[0]), Float.intBitsToFloat(x[1])}).collect(Collectors.toList());
    BlockPos pPos = pos;
    pos = pos.offset(facing);
    state = world.getBlockState(pos);

    while (state.getBlock() instanceof FiberOpticCable && !state.get(FiberOpticCable.Input).equals(state.get(FiberOpticCable.Output)) && (state.get(FiberOpticCable.Input).equals(facing) || state.get(FiberOpticCable.Output).equals(facing))) {
      Direction input = state.get(FiberOpticCable.Input);
      Direction output = state.get(FiberOpticCable.Output);
      if (!pos.offset(input.getOpposite()).equals(pPos)) {
        facing = input.getOpposite();
      } else {
        facing = output.getOpposite();
      }

      pPos = pos;
      pos = pos.offset(facing);
      state = world.getBlockState(pos);
    }

    if (world.getBlockState(pos).getBlock() instanceof Coupler) {
      ((CouplerEntity)world.getBlockEntity(pos)).lasersOut = lasers;
    }
  }
}
