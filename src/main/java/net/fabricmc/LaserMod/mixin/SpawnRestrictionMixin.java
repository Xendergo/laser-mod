package net.fabricmc.LaserMod.mixin;

import java.util.Random;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.At;

import net.fabricmc.LaserMod.LaserStorage;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.SpawnRestriction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.ServerWorldAccess;

@Mixin(SpawnRestriction.class)
public class SpawnRestrictionMixin {
  @Inject(at = @At("HEAD"), method = "canSpawn", cancellable = true)
  private static <T extends Entity> void noSpawningInLasers(EntityType<T> type, ServerWorldAccess serverWorldAccess, SpawnReason spawnReason, BlockPos pos, Random random, CallbackInfoReturnable<Boolean> info) {
    if (LaserStorage.checkEntityIntersection(type, pos, serverWorldAccess.toServerWorld())) {
      info.setReturnValue(false); // Stuff can't spawn in lasers
    }
  }
}
