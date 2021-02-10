package net.fabricmc.LaserMod.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.fabricmc.LaserMod.LaserStorage;
import net.minecraft.network.ClientConnection;
import net.minecraft.server.PlayerManager;
import net.minecraft.server.network.ServerPlayerEntity;

@Mixin(PlayerManager.class)
public class PlayerMixin {
  @Inject(at = @At("TAIL"), method = "onPlayerConnect")
  private void newPlayer(ClientConnection connection, ServerPlayerEntity player, CallbackInfo info) {
    LaserStorage.sendLaserData(player);
  }

  @Inject(at = @At("TAIL"), method = "respawnPlayer")
  private void playerRespawns(ServerPlayerEntity player, boolean alive, CallbackInfoReturnable<ServerPlayerEntity> info) {
    LaserStorage.sendLaserData(player);
  }
}
