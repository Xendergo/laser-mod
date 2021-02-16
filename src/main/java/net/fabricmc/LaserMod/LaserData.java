package net.fabricmc.LaserMod;

import java.util.ArrayList;

import net.minecraft.util.math.BlockPos;

public class LaserData {
  public BlockPos pos;
  public ArrayList<int[]> lasers;

  LaserData(BlockPos thisPos, ArrayList<int[]> thisLasers) {
    pos = thisPos;
    lasers = thisLasers;
  }
}
