package net.fabricmc.LaserMod;

import java.util.*;

import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;

public class LaserStorage {
  /*
  The keys are long integers representing block positions
  each block position has a linked list representing every laser that passes through the block
  Each laser is represented as a linked list of integers
  First integer is the bits of a float representing the power
  Second integer is the bits of a float representing the frequency
  Third integer gives data about the laser, 1st bit represents whether the laser starts, 2nd is whether the laser ends here, next 3 bits are the id of the direction
      
  Different lasers in the same block in the same direction are rendered once, with the color in the middle (allows for fine control of color by combining multiple lasers with a lens)
  */

  public static HashMap<Long, ArrayList<int[]>> lasers = new HashMap<Long, ArrayList<int[]>>();
  public static HashMap<Long, ArrayList<int[]>> pLasers = new HashMap<Long, ArrayList<int[]>>();

  public static void setAtPos(BlockPos pos, float p, float λ, Direction dir, boolean start, boolean end) {
    int[] toSet = new int[3];

    toSet[0] = Float.floatToIntBits(p);
    toSet[1] = Float.floatToIntBits(λ);
    toSet[2] = (start ? 1 : 0) + (end ? 2 : 0) + (dir.getId() << 2);

    long posLong = pos.asLong();

    if (!lasers.containsKey(posLong)) {
      lasers.put(posLong, new ArrayList<int[]>());
    }

    lasers.get(posLong).add(toSet);
  }

  public static boolean laserAtPos(BlockPos pos) {
    long posLong = pos.asLong();

    if (!lasers.containsKey(posLong)) {
      return false;
    }

    return lasers.get(posLong).size() > 0;
  }

  public static void clear() {
    // Remove excess memory in pLasers
    for (long key : pLasers.keySet()) {
      if (!lasers.containsKey(key)) pLasers.remove(key);
    }

    // Swap the lists to avoid reallocating anything
    for (long key : lasers.keySet()) {
      if (!pLasers.containsKey(key)) pLasers.put(key, new ArrayList<int[]>());

      ArrayList<int[]> temp = pLasers.get(key);
      temp.clear();

      pLasers.put(key, lasers.get(key));

      lasers.put(key, temp);
    }
  }

  public static void removeUselessEntries() {
    for (long key : lasers.keySet()) {
      if (lasers.get(key).size() == 0) {
        lasers.remove(key);
      }
    }
  }

  public static boolean checkChanged() {
    Set<Long> keys = lasers.keySet();
    Set<Long> pKeys = pLasers.keySet();
    if (keys.containsAll(pKeys)) {
      for (long key : keys) {
        if (!lasers.get(key).equals(pLasers.get(key))) return true;
      }

      return false;
    }

    return true;
  }

  public static void sendLaserData(MinecraftServer server) {
    ArrayList<Integer> ints = new ArrayList<Integer>();
    for (long key : lasers.keySet()) {
      ints.add((int)(key & 0xFFFFFFFF));
      ints.add((int)(key >> 32));
      ints.add(lasers.get(key).size());
      for (int[] laserData : lasers.get(key)) {
        ints.add(laserData[0]);
        ints.add(laserData[1]);
        ints.add(laserData[2]);
      }
    }

    // Convert the integer list to a packet byte buffer
    PacketByteBuf buf = PacketByteBufs.create();
    buf.writeIntArray(Arrays.stream(ints.toArray(new Integer[ints.size()])).mapToInt(Integer::intValue).toArray());

    for (ServerPlayerEntity player : PlayerLookup.all(server)) {
      ServerPlayNetworking.send(player, NetworkingIdentifiers.LaserStorage, buf);
    }
    System.out.println("Sending data");
  }
}
