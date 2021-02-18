package net.fabricmc.LaserMod;

import net.fabricmc.LaserMod.blocks.FiberOpticCable;
import net.minecraft.block.BlockState;
import net.minecraft.state.property.Properties;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;

public class fiberOpticUpdates {
  public static BlockPos getOtherSide(BlockPos pos, World world, BlockState state) {
    Direction facing;
    
    try {
      facing = state.get(Properties.FACING);
    } catch (Exception e) {
      facing = state.get(FiberOpticCable.Input).getOpposite();
    }

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

    return pos;
  }

  public static BlockPos getOtherSide(BlockPos pos, World world) {
    return getOtherSide(pos, world, world.getBlockState(pos));
  }
}
