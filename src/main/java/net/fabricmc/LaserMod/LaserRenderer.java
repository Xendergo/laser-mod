// Reading malilib was a massive lifesaver in making this: https://github.com/maruohon/malilib/blob/arne_1.16.x/src/main/java/fi/dy/masa/malilib/render/RenderUtils.java

package net.fabricmc.LaserMod;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;

import org.lwjgl.opengl.GL11;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.util.math.Vector3f;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;

public class LaserRenderer {
  public static void render() {
    MinecraftClient mc = MinecraftClient.getInstance();
    if (mc.player != null) {
      // drawRect(10, 100, 50, 50, 0xFFFFFFFF, 0);
      drawLaser(new BlockPos(0, 100, 0), new BlockPos(1, 100, 0), 0xFFFFFFFF, mc);
    }
  }

  private static void drawLaser(BlockPos start, BlockPos end, int color, MinecraftClient mc) {
    
    RenderSystem.depthMask(false);
    RenderSystem.disableLighting();
    RenderSystem.disableCull();
    RenderSystem.disableTexture();
    
    setupBlend();
    
    float a = (float) (color >> 24 & 255) / 255.0F;
    float r = (float) (color >> 16 & 255) / 255.0F;
    float g = (float) (color >>  8 & 255) / 255.0F;
    float b = (float) (color & 255) / 255.0F;
    
    RenderSystem.pushMatrix();
    
    MatrixStack matrixStackTemp = new MatrixStack();
    
    Vec3d cameraPos = mc.gameRenderer.getCamera().getPos();
    
    double x = start.getX() + 0.5d - cameraPos.x;
    double y = start.getY() + 0.5d - cameraPos.y;
    double z = start.getZ() + 0.5d - cameraPos.z;
    
    overlayTranslations(x, y, z, Direction.NORTH, mc.getCameraEntity().getHorizontalFacing(), matrixStackTemp);
    RenderSystem.multMatrix(matrixStackTemp.peek().getModel());

    Tessellator tessellator = Tessellator.getInstance();
    BufferBuilder buffer = tessellator.getBuffer();

    color(r, g, b, a);

    buffer.begin(GL11.GL_QUADS, VertexFormats.POSITION);

    buffer.vertex(x - 0.5, y - 0.5, z).next();
    buffer.vertex(x + 0.5, y - 0.5, z).next();
    buffer.vertex(x + 0.5, y + 0.5, z).next();
    buffer.vertex(x - 0.5, y + 0.5, z).next();

    tessellator.draw();

    RenderSystem.popMatrix();

    RenderSystem.enableTexture();
    RenderSystem.disableBlend();
    RenderSystem.enableCull();
    RenderSystem.depthMask(true);

  }

  public static void color(float r, float g, float b, float a)
  {
     RenderSystem.color4f(r, g, b, a);
  }

  private static void overlayTranslations(double x, double y, double z, Direction side, Direction playerFacing, MatrixStack matrixStack)
  {
    matrixStack.translate(x, y, z);

    switch (side)
    {
      case DOWN:
        matrixStack.multiply(Vector3f.POSITIVE_Y.getDegreesQuaternion(180f - playerFacing.asRotation()));
        matrixStack.multiply(Vector3f.POSITIVE_X.getDegreesQuaternion(90f));
        break;
      case UP:
        matrixStack.multiply(Vector3f.POSITIVE_Y.getDegreesQuaternion(180f - playerFacing.asRotation()));
        matrixStack.multiply(Vector3f.POSITIVE_X.getDegreesQuaternion(-90f));
        break;
      case NORTH:
        matrixStack.multiply(Vector3f.POSITIVE_Y.getDegreesQuaternion(180f));
        break;
      case SOUTH:
        break;
      case WEST:
        matrixStack.multiply(Vector3f.POSITIVE_Y.getDegreesQuaternion(-90f));
        break;
      case EAST:
        matrixStack.multiply(Vector3f.POSITIVE_Y.getDegreesQuaternion(90f));
        break;
    }

    matrixStack.translate(-x, -y, -z + 0.510);
  }

  private static void setupBlend()
  {
    RenderSystem.enableBlend();
    RenderSystem.blendFuncSeparate(GlStateManager.SrcFactor.SRC_ALPHA, GlStateManager.DstFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SrcFactor.ONE, GlStateManager.DstFactor.ZERO);
  }
}
