package net.fabricmc.LaserMod;

import java.text.DecimalFormat;
import java.util.*;

import net.fabricmc.LaserMod.blocks.LaserEntity;
import net.fabricmc.LaserMod.blocks.LaserUpdatable;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.block.Block;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.EntityType;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.world.RaycastContext;
import net.minecraft.world.World;

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

  public static HashMap<RegistryKey<World>, HashMap<Long, ArrayList<int[]>>> lasers = new HashMap<RegistryKey<World>, HashMap<Long, ArrayList<int[]>>>();
  public static HashMap<RegistryKey<World>, HashMap<Long, ArrayList<int[]>>> pLasers = new HashMap<RegistryKey<World>, HashMap<Long, ArrayList<int[]>>>();

  public static HashMap<RegistryKey<World>, HashSet<BlockPos>> toUpdate = new HashMap<RegistryKey<World>, HashSet<BlockPos>>();

  public static boolean useCurrent = false;

  private static DecimalFormat df = new DecimalFormat("#0.0");
  
  public static void setAtPos(World world, BlockPos pos, float p, float λ, Direction dir, boolean start, boolean end) {
    RegistryKey<World> regKey = world.getRegistryKey();

    int[] toSet = new int[3];

    toSet[0] = Float.floatToIntBits(p);
    toSet[1] = Float.floatToIntBits(λ);
    toSet[2] = (start ? 1 : 0) + (end ? 2 : 0) + (dir.getId() << 2);

    long posLong = pos.asLong();

    if (!lasers.containsKey(regKey)) {
      lasers.put(regKey, new HashMap<Long, ArrayList<int[]>>());
      pLasers.put(regKey, new HashMap<Long, ArrayList<int[]>>());
    }

    if (!lasers.get(regKey).containsKey(posLong)) {
      lasers.get(regKey).put(posLong, new ArrayList<int[]>());
    }

    lasers.get(regKey).get(posLong).add(toSet);
  }

  public static boolean laserAtPos(World world, BlockPos pos) {
    long posLong = pos.asLong();

    if (!pLasers.get(world.getRegistryKey()).containsKey(posLong)) {
      return false;
    }

    return pLasers.get(world.getRegistryKey()).get(posLong).size() > 0;
  }
  
  public static int lightAtPos(BlockPos pos, World world) {
    List<int[]> lasersAtPos = lasersAtPos(world, pos);

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

  public static List<int[]> lasersAtPos(World world, BlockPos pos) {
    List<int[]> out;
    if (useCurrent) {
      out = lasers.get(world.getRegistryKey()).get(pos.asLong());
    } else {
      out = pLasers.get(world.getRegistryKey()).get(pos.asLong());
    }

    if (out == null) {
      return new ArrayList<int[]>();
    } else {
      return out;
    }
  }

  public static float laserPowerAtSpot(BlockPos pos, Direction dir, World world) {
    int dirId = dir.getId();

    List<int[]> lasersAtPos = lasersAtPos(world, pos);

    try {
      if (lasersAtPos.size() == 0) {
        return 0;
      } else {
        float max = 0;
        for (int[] laser : lasersAtPos) {
          if (laser[2] >> 2 == dirId) {
            float v = Float.intBitsToFloat(laser[0]);
    
            if (v > max) {
              max = v;
            }
          }
        }

        return max;
      }
    } catch (Exception e) {
      return 0;
    }
  }

  public static float laserFreqAtSpot(BlockPos pos, Direction dir, World world) {
    int dirId = dir.getId();

    List<int[]> lasersAtPos = lasersAtPos(world, pos);

    try {
      if (lasersAtPos.size() == 0) {
        return 0;
      } else {
        float max = 0;
        float λ = 0;
        for (int[] laser : lasersAtPos) {
          if (laser[2] >> 2 == dirId) {
            float v = Float.intBitsToFloat(laser[0]);
    
            if (v > max) {
              max = v;
              λ = Float.intBitsToFloat(laser[1]);
            }
          }
        }

        return λ;
      }
    } catch (Exception e) {
      return 0;
    }
  }

  public static void clear() {
    useCurrent = false;
    for (RegistryKey<World> regKey : lasers.keySet()) {
      // Remove excess memory in pLasers
      LinkedList<Long> toRemove = new LinkedList<Long>();
      for (long key : pLasers.get(regKey).keySet()) {
        if (!lasers.get(regKey).containsKey(key)) toRemove.add(key);
      }
  
      for (long key : toRemove) {
        pLasers.get(regKey).remove(key);
      }
  
      // Swap the lists to avoid reallocating anything
      for (long key : lasers.get(regKey).keySet()) {
        if (!pLasers.get(regKey).containsKey(key)) pLasers.get(regKey).put(key, new ArrayList<int[]>());
  
        ArrayList<int[]> temp = pLasers.get(regKey).get(key);
        temp.clear();
  
        pLasers.get(regKey).put(key, lasers.get(regKey).get(key));
  
        lasers.get(regKey).put(key, temp);
      }
    }
  }

  private static void removeUselessEntries() {
    for (RegistryKey<World> regKey : lasers.keySet()) {
      LinkedList<Long> toRemove = new LinkedList<Long>();
      for (long key : lasers.get(regKey).keySet()) {
        if (lasers.get(regKey).get(key).size() == 0) {
          toRemove.add(key);
        }
      }

      for (long key : toRemove) {
        lasers.get(regKey).remove(key);
      }
    }
  }

  public static boolean lasersEqual(ArrayList<int[]> l1, ArrayList<int[]> l2) {
    if (l1.size() == l2.size()) {
      for (int i = l1.size()-1; i >= 0; i--) {
        if (!Arrays.equals(l1.get(i), l2.get(i))) return false;
      }
    } else {
      return false;
    }

    return true;
  }

  public static boolean checkChanged() {
    Set<RegistryKey<World>> dims = lasers.keySet();
    Set<RegistryKey<World>> pDims = lasers.keySet();

    if (dims.containsAll(pDims) && pDims.containsAll(dims)) {
      for (RegistryKey<World> regKey : dims) {
        Set<Long> keys = lasers.get(regKey).keySet();
        Set<Long> pKeys = pLasers.get(regKey).keySet();
        if (keys.containsAll(pKeys) && pKeys.containsAll(keys)) {
          for (long key : keys) {
            if (!lasersEqual(lasers.get(regKey).get(key), pLasers.get(regKey).get(key))) return true;
          }
        } else {
          return true;
        }
      }

      return false;
    }
    
    return true;
  }

  private static PacketByteBuf generatePacketBuf(HashMap<Long, ArrayList<int[]>> laserData) {
    ArrayList<Integer> ints = new ArrayList<Integer>();
    for (long key : laserData.keySet()) {
      ints.add((int)(key & 0xFFFFFFFF));
      ints.add((int)(key >>> 32));
      ints.add(laserData.get(key).size());
      for (int[] laserSectionData : laserData.get(key)) {
        ints.add(laserSectionData[0]);
        ints.add(laserSectionData[1]);
        ints.add(laserSectionData[2]);
      }
    }

    // Convert the integer list to a packet byte buffer
    PacketByteBuf buf = PacketByteBufs.create();
    buf.writeIntArray(Arrays.stream(ints.toArray(new Integer[ints.size()])).mapToInt(Integer::intValue).toArray());

    return buf;
  }

  public static void sendLaserData(MinecraftServer server) {
    useCurrent = true;

    removeUselessEntries();

    for (RegistryKey<World> regKey : lasers.keySet()) {
      ArrayList<BlockPos> toRemove = new ArrayList<BlockPos>();
      ServerWorld dimension = server.getWorld(regKey);
      if (toUpdate.containsKey(regKey)) {
        for (BlockPos posToUpdate : toUpdate.get(regKey)) {
          Block block = dimension.getBlockState(posToUpdate).getBlock();
          if (block instanceof LaserUpdatable) {
            ((LaserUpdatable)dimension.getBlockState(posToUpdate).getBlock()).laserUpdate(dimension.getBlockState(posToUpdate), dimension, posToUpdate);
          } else {
            toRemove.add(posToUpdate);
          }
        }

        for (BlockPos posToRemove : toRemove) {
          toUpdate.get(regKey).remove(posToRemove);
        }
      }

      HashMap<Long, ArrayList<int[]>> newLasers = new HashMap<Long, ArrayList<int[]>>(lasers.get(regKey));

      for (long key : pLasers.get(regKey).keySet()) {
        if (newLasers.containsKey(key) && lasersEqual(newLasers.get(key), pLasers.get(regKey).get(key))) {
          newLasers.remove(key);
        }
      }

      PacketByteBuf buf = generatePacketBuf(newLasers);

      Set<Long> removedLasers = pLasers.get(regKey).keySet();
      removedLasers.removeAll(lasers.get(regKey).keySet());
      buf.writeLongArray(removedLasers.stream().mapToLong(v -> v).toArray());

      buf.writeBoolean(false);

      for (ServerPlayerEntity player : PlayerLookup.world(dimension)) {
        ServerPlayNetworking.send(player, NetworkingIdentifiers.LaserStorage, buf);
      }
    }
  }

  public static void sendLaserData(ServerPlayerEntity player) {
    RegistryKey<World> regKey = player.getServerWorld().getRegistryKey();

    if (!lasers.containsKey(regKey)) {
      lasers.put(regKey, new HashMap<Long, ArrayList<int[]>>());
      pLasers.put(regKey, new HashMap<Long, ArrayList<int[]>>());
    }

    PacketByteBuf buf = generatePacketBuf(lasers.get(regKey));

    buf.writeLongArray(new long[0]);
    buf.writeBoolean(true);

    ServerPlayNetworking.send(player, NetworkingIdentifiers.LaserStorage, buf);
  }

  public static void handleNudge(MinecraftServer server, ServerPlayerEntity player, PacketByteBuf buf) {
    byte toAdjust = buf.readByte();
    server.execute(() -> {
      // https://github.com/gnembon/fabric-carpet/blob/0e0a4cc0f21f4a331512c0c005f81f329fabf422/src/main/java/carpet/helpers/Tracer.java#L29
  
      Vec3d pos = player.getCameraPosVec(1);
      Vec3d rotation = player.getRotationVec(1);
      Vec3d reachEnd = pos.add(rotation.x * 4.5, rotation.y * 4.5, rotation.z * 4.5);
      HitResult hit = player.world.raycast(new RaycastContext(pos, reachEnd, RaycastContext.ShapeType.OUTLINE, RaycastContext.FluidHandling.NONE, player));
      
      BlockEntity targeted = player.world.getBlockEntity(new BlockPos(hit.getPos().add(rotation.multiply(0.01))));
  
      if (targeted instanceof LaserEntity) {
        LaserEntity laserEntity = ((LaserEntity)targeted);
        laserEntity.adjustFrequency(toAdjust/10.0F);
        player.sendMessage(new TranslatableText("state.lasermod.setfrequency", df.format(laserEntity.λ)), false);
      }
    });
  }

  public static boolean checkEntityIntersection(EntityType<?> entity, BlockPos pos, ServerWorld world) {
    Box boundingBox = entity.createSimpleBoundingBox(pos.getX(), pos.getY(), pos.getZ());

    int minX = (int)Math.floor(boundingBox.minX);
    int maxX = (int)Math.ceil(boundingBox.maxX);
    int minY = (int)Math.floor(boundingBox.minY);
    int maxY = (int)Math.ceil(boundingBox.maxY);
    int minZ = (int)Math.floor(boundingBox.minZ);
    int maxZ = (int)Math.ceil(boundingBox.maxZ);

    RegistryKey<World> regKey = world.getRegistryKey();

    for (int i = minX; i < maxX; i++) {
      for (int j = minY; j < maxY; j++) {
        for (int k = minZ; k < maxZ; k++) {
          try {
            List<int[]> lasersAtPos = (useCurrent ? lasers : pLasers).get(regKey).get(new BlockPos(i, j, k).asLong());
            if (lasersAtPos != null && lasersAtPos.size() != 0) {
              return true;
            }
          } catch (Exception e) { // If there's a null pointer exception, that means there can't be any lasers there
            return true;
          }
        }
      }
    }

    return false;
  }
}
