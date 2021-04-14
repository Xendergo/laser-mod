package net.fabricmc.LaserMod.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.fabricmc.LaserMod.LaserStorageClient;
import net.minecraft.block.BlockState;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockRenderView;

@Mixin(WorldRenderer.class)
public abstract class RenderMixin {
  @Inject(at = @At("RETURN"), method = "getLightmapCoordinates(Lnet/minecraft/world/BlockRenderView;Lnet/minecraft/block/BlockState;Lnet/minecraft/util/math/BlockPos;)I", cancellable = true)
  private static void giveLaserLight(BlockRenderView world, BlockState state, BlockPos pos, CallbackInfoReturnable<Integer> info) {
    int laserLight = LaserStorageClient.lightAtPos(pos);
    int ret = info.getReturnValue();
    
    if ((ret >> 4 & 0xFFFF) < laserLight) {
      info.setReturnValue(ret & 0xFFF0000F | laserLight << 4);
    }
  }
}
