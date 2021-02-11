package net.fabricmc.LaserMod.blocks;

import net.fabricmc.LaserMod.LaserMod;
import net.fabricmc.LaserMod.LaserStorage;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.state.property.Properties;
import net.minecraft.util.Tickable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;

public class LaserEntity extends BlockEntity implements Tickable {
  /* Using λ for wavelength
  [0-1): microwave
  [1-2): infrared
  [2-14): visible
  [14-15): UV
  [15-16): γ rays
  */
  public float λ = 7;

  /* Using p for power
  Power can be between 0 & 15 like redstone
  */
  public float p = 0;

  public LaserEntity() {
    super(LaserMod.LaserEntityData);
  }

  @Override
  public CompoundTag toTag(CompoundTag tag) {
    super.toTag(tag);

    tag.putFloat("λ", λ);
    tag.putFloat("p", p);

    return tag;
  }

  @Override
  public void fromTag(BlockState state, CompoundTag tag) {
    super.fromTag(state, tag);
    λ = tag.getFloat("λ");
    p = tag.getFloat("p");
  }

  public void updateLaserData(float newΛ, float newP) {
    λ = newΛ;
    p = newP;
  }

  public void tick() {
    if (world.isClient()) return;

    if (p != 0) {
      marchLaser(this.pos, this.world.getBlockState(this.pos).get(Properties.FACING), p);
    }
  }

  public void marchLaser(BlockPos pos, Direction dir, float power) {
    BlockState blockState = world.getBlockState(pos);
    power += 0.25;
    boolean start = true;

    while (!blockState.isSolidBlock(world, pos) && world.isChunkLoaded(pos) && !World.isOutOfBuildLimitVertically(pos) && power > 0) {
      power -= 0.25;
      LaserStorage.setAtPos(world, pos, power, λ, dir, start, false);

      if (start) start = false;

      pos = pos.offset(dir, 1);
      blockState = world.getBlockState(pos);

      if (blockState.getBlock() instanceof Lens) {
        Direction facing = blockState.get(Properties.FACING);
        if (dir.equals(facing.getOpposite())) {
          for (int i = 0; i < 6; i++) {
            if (!directions[i].equals(facing)) {
              marchLaser(pos, directions[i], power/5);
            }
          }
        } else {
          marchLaser(pos, facing, power);
        }
        break;
      }
    }

    LaserStorage.setAtPos(world, pos, power, λ, dir, false, true);
  }

  private static Direction[] directions = {Direction.NORTH, Direction.SOUTH, Direction.EAST, Direction.WEST, Direction.UP, Direction.DOWN};
}
