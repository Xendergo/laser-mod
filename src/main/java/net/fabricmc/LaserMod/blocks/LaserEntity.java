package net.fabricmc.LaserMod.blocks;

import net.fabricmc.LaserMod.LaserMod;
import net.minecraft.block.BlockEntityProvider;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.nbt.CompoundTag;

public class LaserEntity extends BlockEntity {
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

    System.out.print(λ);
    System.out.print(", ");
    System.out.println(p);
  }
}
