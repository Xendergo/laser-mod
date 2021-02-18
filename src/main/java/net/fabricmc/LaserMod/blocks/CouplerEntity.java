package net.fabricmc.LaserMod.blocks;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import net.fabricmc.LaserMod.LaserMod;
import net.minecraft.block.BlockState;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.state.property.Properties;
import net.minecraft.util.Tickable;

public class CouplerEntity extends LaserEmitter implements Tickable {
  public List<float[]> lasersOut = new LinkedList<float[]>();
  public CouplerEntity() {
    super(LaserMod.CouplerEntityData);
  }

  @Override
  public void fromTag(BlockState state, CompoundTag tag) {
    super.fromTag(state, tag);

    int[] data = tag.getIntArray("data");

    for (int i = 0; i < data.length; i+=2) {
      lasersOut.add(new float[] {Float.intBitsToFloat(data[i]), Float.intBitsToFloat(data[i+1])});
    }
  }

  @Override
  public CompoundTag toTag(CompoundTag tag) {
    List<Integer> out = new ArrayList<Integer>(lasersOut.size()*2);

    for (float[] laser : lasersOut) {
      out.add(Float.floatToIntBits(laser[0]));
      out.add(Float.floatToIntBits(laser[1]));
    }

    tag.putIntArray("data", out);

    return tag;
  }

  public void tick() {
    if (world.isClient()) return;
    for (float[] laser : lasersOut) {
      marchLaser(pos, world.getBlockState(pos).get(Properties.FACING).getOpposite(), laser[0], laser[1]);
    }
  }
}
