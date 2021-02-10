package net.fabricmc.LaserMod.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.fabricmc.LaserMod.LaserStorage;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerWorld;

@Mixin(MinecraftServer.class)
public abstract class ServerMixin {
  @Shadow protected abstract ServerWorld getOverworld();

  // Clear the laser data at the start of a tick so it can be regenerated
  @Inject(at = @At("HEAD"), method = "tick")
  private void tick(CallbackInfo info) {
    LaserStorage.clear();
  }

  // If the laser data changed, send the new data to the clients
  @Inject(at = @At("TAIL"), method = "tick")
  private void tickEnd(CallbackInfo info) {
    boolean changed = LaserStorage.checkChanged();
    if (changed) {
      LaserStorage.sendLaserData(getOverworld().getServer());
    }
  }
}
