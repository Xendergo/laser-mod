package net.fabricmc.LaserMod.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.Slice;

import net.fabricmc.LaserMod.LaserRenderer;
import net.fabricmc.LaserMod.LaserStorageClient;
import net.minecraft.block.BlockState;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockRenderView;

@Mixin(WorldRenderer.class)
public abstract class RenderMixin {
  // Again, maruohon saving my sanity: https://github.com/maruohon/malilib/blob/arne_1.16.x/src/main/java/fi/dy/masa/malilib/mixin/MixinWorldRenderer.java
  @Inject(method = "render",
          at = @At(value = "INVOKE", ordinal = 1,
                   target = "Lnet/minecraft/client/render/WorldRenderer;renderWeather(Lnet/minecraft/client/render/LightmapTextureManager;FDDD)V"))
  private void render(CallbackInfo info) {
    LaserRenderer.render();
  }

  @Inject(method = "render",
            slice = @Slice(from = @At(value = "FIELD", ordinal = 1, // start from the endDrawing() call
                                      target = "Lnet/minecraft/client/render/RenderPhase;WEATHER_TARGET:Lnet/minecraft/client/render/RenderPhase$Target;"),
                            to = @At(value = "INVOKE", ordinal = 1, // end at the second renderWeather call
                                     target = "Lnet/minecraft/client/render/WorldRenderer;renderWeather(Lnet/minecraft/client/render/LightmapTextureManager;FDDD)V")),
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/client/gl/ShaderEffect;render(F)V"))
  private void renderFabulous(CallbackInfo info) {
    LaserRenderer.render();
  }

  @Inject(at = @At("RETURN"), method = "getLightmapCoordinates(Lnet/minecraft/world/BlockRenderView;Lnet/minecraft/block/BlockState;Lnet/minecraft/util/math/BlockPos;)I", cancellable = true)
  private static void giveLaserLight(BlockRenderView world, BlockState state, BlockPos pos, CallbackInfoReturnable<Integer> info) {
    int laserLight = LaserStorageClient.lightAtPos(pos);
    int ret = info.getReturnValue();
    
    if ((ret >> 4 & 0xFFFF) < laserLight) {
      info.setReturnValue(ret & 0xFFF0000F | laserLight << 4);
    }
  }
}
