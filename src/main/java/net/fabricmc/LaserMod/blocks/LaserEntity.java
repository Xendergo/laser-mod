package net.fabricmc.LaserMod.blocks;

import java.util.HashSet;

import net.fabricmc.LaserMod.LaserMod;
import net.fabricmc.LaserMod.LaserStorage;
import net.minecraft.block.BlockState;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.state.property.Properties;
import net.minecraft.util.Tickable;
import net.minecraft.util.math.BlockPos;

public class LaserEntity extends LaserEmitter implements Tickable {
  /* Using λ for wavelength
  [0-1): microwave
  [1-2): infrared
  [2-14): visible
  [14-15): UV
  [15-16): γ rays
  */
  public float λ = 0;

  /* Using p for power
  Power can be between 0 & 15 like redstone
  */
  public float p = 0;

  public int comparatorPower = 0;

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
    markDirty();
    if (newΛ != -1) {
      λ = remap(newΛ, 0, 15, 0, 16);
    }

    p = newP;
  }

  public void adjustFrequency(float amt) {
    λ += amt;
    λ = Math.round(Math.min(Math.max(λ, 0), 16)*10)*0.1F;
  }

  public void tick() {
    if (world.isClient()) return;

    if (!LaserStorage.toUpdate.containsKey(world.getRegistryKey())) {
      LaserStorage.toUpdate.put(world.getRegistryKey(), new HashSet<BlockPos>());
    }
    LaserStorage.toUpdate.get(world.getRegistryKey()).add(pos);

    if (p == 0) {
      comparatorPower = 0;
    } else {
      comparatorPower = marchLaser(this.pos, this.world.getBlockState(this.pos).get(Properties.FACING), p, λ);
    }
  }
}
