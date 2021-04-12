package net.fabricmc.LaserMod;

import java.util.*;
import java.util.stream.Collectors;

import net.minecraft.util.math.BlockPos;

public class LaserStorageClient {
  public static HashMap<Long, ArrayList<int[]>> lasers = new HashMap<Long, ArrayList<int[]>>();
  public static List<LaserData> laserList = new LinkedList<LaserData>();

  private static int[] packet;
  private static long[] packetRemoved;
  private static boolean packetClear;

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

      return Math.min((int)Math.ceil(max), 15);
    }
  }

  public static void clear() {
    for (long key : lasers.keySet()) {

      ArrayList<int[]> temp = lasers.get(key);
      temp.clear();

      lasers.put(key, temp);
    }
  }

  public static void processLaserData(int[] data, long[] removed, boolean clear) {
    packet = data;
    packetRemoved = removed;
    packetClear = clear;

    processLaserDataCallback();
  }

  public static void processLaserDataCallback() {
    synchronized (laserList) {    
      if (packetClear) {
        clear();
      }
  
      int i = 0;
  
      while (i < packet.length) {
        long key = ((long)packet[i] << 32 >>> 32) | (((long)packet[i+1]) << 32);
        i += 2;
        int length = packet[i];
        i++;
  
        ArrayList<int[]> toAdd = new ArrayList<int[]>();
  
        for (int j = 0; j < length; j++) {
          toAdd.add(new int[] {packet[i], packet[i+1], packet[i+2]});
  
          i+=3;
        }
  
        lasers.put(key, toAdd);
      }
  
      for (long pos : packetRemoved) {
        lasers.remove(pos);
      }
  
      laserList = lasers.entrySet().stream().map(x -> new LaserData(BlockPos.fromLong(x.getKey()), x.getValue())).collect(Collectors.toList());
    }
  }
}
