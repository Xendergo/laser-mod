package net.fabricmc.LaserMod.blocks;

import net.fabricmc.LaserMod.LaserDamageSource;
import net.fabricmc.LaserMod.LaserMod;
import net.fabricmc.LaserMod.LaserStorage;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.state.property.Properties;
import net.minecraft.util.Tickable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;

public class LaserEntity extends BlockEntity implements Tickable {
  public static DamageSource Lased = LaserDamageSource.LASER_DAMAGE_SOURCE;
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
    λ = remap(newΛ, 0, 15, 0, 16);
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

      java.util.List<LivingEntity> entitiesHit = world.getEntitiesByClass(LivingEntity.class, new Box(pos), (a) -> true);
      if (entitiesHit.size() > 0) {
        if (λ > 14) {
          for (LivingEntity entity : entitiesHit) {
            entity.damage(Lased, remap(λ, 14, 16, 0, 6) * remap(power, 0, 16, 0, 1));
          }
        }
        break;
      }

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

  private static float remap(float v, float min1, float max1, float min2, float max2) {
    return (v - min1) * (max2 - min2) / (max1 - min1) + min2;
  }

  private static Direction[] directions = {Direction.NORTH, Direction.SOUTH, Direction.EAST, Direction.WEST, Direction.UP, Direction.DOWN};
}
