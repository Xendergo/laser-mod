package net.fabricmc.LaserMod.mixin;

import com.mojang.authlib.GameProfile;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.At;

import net.fabricmc.LaserMod.LaserStorage;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

@Mixin(ServerPlayerEntity.class)
public abstract class PlayerEntityMixin extends PlayerEntity {

  public PlayerEntityMixin(World world, BlockPos blockPos, float f, GameProfile gameProfile) {
      super(world, blockPos, f, gameProfile);
  }

  @Inject(method = "moveToWorld", at = @At("TAIL"))
  private void atChangeDimension(ServerWorld destination, CallbackInfoReturnable<Entity> cir) {
    LaserStorage.sendLaserData((ServerPlayerEntity) (Object)this);
  }
}
