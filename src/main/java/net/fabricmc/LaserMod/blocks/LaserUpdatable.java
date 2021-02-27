package net.fabricmc.LaserMod.blocks;

import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public interface LaserUpdatable {
  abstract void laserUpdate(BlockState state, World world, BlockPos pos);
}
