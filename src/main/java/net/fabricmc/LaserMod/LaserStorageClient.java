package net.fabricmc.LaserMod;

import java.util.*;

import net.minecraft.util.math.BlockPos;

public class LaserStorageClient {
  public static HashMap<Long, ArrayList<int[]>> lasers = new HashMap<Long, ArrayList<int[]>>();

  public static ArrayList<int[]> getAtPos(BlockPos pos) {
    if (lasers.containsKey(pos.asLong())) {
      return lasers.get(pos.asLong());
    } else {
      return new ArrayList<int[]>();
    }
  }

  public static int lightAtPos(BlockPos pos) {
    ArrayList<int[]> lasersAtPos = getAtPos(pos);

    if (lasersAtPos.size() == 0) {
      return 0;
    } else {
      float max = 0;
      for (int[] laser : lasersAtPos) {
        float v = Float.intBitsToFloat(laser[0]);

        if (v > max) {
          max = v;
        }
      }

      return Math.min((int)max, 15);
    }
  }

  public static void clear() {
    for (long key : lasers.keySet()) {

      ArrayList<int[]> temp = lasers.get(key);
      temp.clear();

      lasers.put(key, temp);
    }
  }

  public static void processLaserData(int[] data) {
    clear();

    int i = 0;

    while (i < data.length) {
      long key = (long)data[i] + (((long)data[i+1]) << 32);
      i += 2;
      int length = data[i];
      i++;

      ArrayList<int[]> toAdd = new ArrayList<int[]>();

      for (int j = 0; j < length; j++) {
        toAdd.add(new int[] {data[i], data[i+1], data[i+2]});

        i+=3;
      }

      lasers.put(key, toAdd);
    }

    System.out.println(lasers);
  }
}
