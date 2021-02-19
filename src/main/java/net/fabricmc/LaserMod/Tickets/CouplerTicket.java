package net.fabricmc.LaserMod.Tickets;

import net.minecraft.server.world.ChunkTicketType;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3i;

public class CouplerTicket {
  public static final ChunkTicketType<BlockPos> COUPLER = ChunkTicketType.create("coupler", Vec3i::compareTo, 600);
}
