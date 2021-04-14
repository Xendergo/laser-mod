// Reading malilib was a massive lifesaver in making this: https://github.com/maruohon/malilib/blob/arne_1.16.x/src/main/java/fi/dy/masa/malilib/render/RenderUtils.java

package net.fabricmc.LaserMod;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;

import org.lwjgl.opengl.GL11;

import net.fabricmc.LaserMod.Sounds.LaserSound;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents.AfterTranslucent;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents.BeforeEntities;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.client.sound.SoundManager;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.util.math.Vector3f;
import net.minecraft.sound.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Quaternion;
import net.minecraft.util.math.Vec2f;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3i;

public class LaserRenderer implements AfterTranslucent {
  public static double laserWidth = 0.25;
  private static double laserDist = (1 - laserWidth) / 2;

  public void afterTranslucent(WorldRenderContext ctx) {
    RenderSystem.pushMatrix();
    RenderSystem.multMatrix(ctx.matrixStack().peek().getModel());

    MinecraftClient mc = MinecraftClient.getInstance();
    SoundManager soundManager = mc.getSoundManager();

    GlStateManager.enableDepthTest();
    GlStateManager.depthMask(false);
    GlStateManager.disableAlphaTest();

    if (mc.player != null) {
      synchronized (LaserStorageClient.laserList) {
        Vec3d cameraPos = mc.gameRenderer.getCamera().getPos();
        Vec3i cameraPosInt = new Vec3i(cameraPos.x, cameraPos.y, cameraPos.z);
        List<LaserData> sorted = LaserStorageClient.laserList.stream().filter(x -> x.lasers.size() > 0).sorted((a, b) -> b.pos.getManhattanDistance(cameraPosInt) - a.pos.getManhattanDistance(cameraPosInt)).collect(Collectors.toList());
        
        if (!mc.isPaused() && sorted.size() > 0) {
          soundManager.play(new PositionedSoundInstance(LaserSound.LaserSound, SoundCategory.AMBIENT, 1, 1, sorted.get(sorted.size()-1).pos));
        }
  
        for (LaserData laserSet : sorted) {
  
          for (int dir : directions) {
            List<int[]> lasersInDir = laserSet.lasers.stream().filter((v) -> v[2] >> 2 == dir).collect(Collectors.toList());
            ArrayList<int[]> colorList = new ArrayList<int[]>();
            for (int[] laser : lasersInDir) {
              colorList.add(calcualteColor(laser[0], laser[1]));
            }
  
            int[] color = new int[] {0, 0, 0, 0};
  
            for (int[] colorArr : colorList) {
              color[0] += colorArr[0];
            }
  
            color[0] = Math.min(color[0], 255);
  
            for (int[] colorArr : colorList) {
              float toMultBy = (float)colorArr[0]/(float)color[0];
  
              color[1] += colorArr[1] * toMultBy;
              color[2] += colorArr[2] * toMultBy;
              color[3] += colorArr[3] * toMultBy;
            }
  
            for (int[] laser : lasersInDir) {
  
              drawLaser(laserSet.pos, Direction.byId(dir), color, mc, (laser[2] & 1) == 1, (laser[2] & 2) == 2, cameraPos);
            }
          }
        }
      }
    }

    GlStateManager.depthMask(true);
    GlStateManager.disableDepthTest();
    GlStateManager.enableAlphaTest();

    RenderSystem.popMatrix();
  }

  private static void drawLaser(BlockPos pos, Direction dir, int[] color, MinecraftClient mc, boolean start, boolean end, Vec3d cameraPos) {
    if (start && end) return; // If it's both the start and end of a laser, something went terribly wrong

    float a = (float) Math.min(color[0], 255) / 255.0F;
    if (a == 0) return; // Don't bother rendering anything you can't see

    float r = (float) Math.min(color[1], 255) / 255.0F;
    float g = (float) Math.min(color[2], 255) / 255.0F;
    float b = (float) Math.min(color[3], 255) / 255.0F;

    // If it's the end of a laser, reverse the direction and draw as if it's the start of a laser
    if (end) {
      start = true;
      dir = dir.getOpposite();
    }

    RenderSystem.disableLighting();
    RenderSystem.disableTexture();
    RenderSystem.enableBlend();
    RenderSystem.disableCull();
    
    color(r, g, b, a);

    Quaternion rotation = getRotation(dir);
    
    if (start) {
      drawRect(new Vec3d(pos.getX() + laserDist + laserWidth, pos.getY() + laserDist, pos.getZ() + 0.5), new Vec2f(0.5F, (float)laserWidth), Direction.WEST, cameraPos, rotation);
      drawRect(new Vec3d(pos.getX() + laserDist, pos.getY() + laserDist, pos.getZ() + 0.5), new Vec2f(0.5F, (float)laserWidth), Direction.WEST, cameraPos, rotation);
      drawRect(new Vec3d(pos.getX() + laserDist, pos.getY() + laserDist, pos.getZ() + 0.5), new Vec2f((float)laserWidth, 0.5F), Direction.DOWN, cameraPos, rotation);
      drawRect(new Vec3d(pos.getX() + laserDist, pos.getY() - laserDist + 1, pos.getZ() + 0.5), new Vec2f((float)laserWidth, 0.5F), Direction.DOWN, cameraPos, rotation);
    
      drawRect(new Vec3d(pos.getX() + laserDist + laserWidth, pos.getY() + laserDist, pos.getZ() + 0.5), new Vec2f((float)laserWidth, (float)laserWidth), Direction.NORTH, cameraPos, rotation);
    } else {
      drawRect(new Vec3d(pos.getX() - laserDist + 1, pos.getY() + laserDist, pos.getZ()), new Vec2f(1F, (float)laserWidth), Direction.WEST, cameraPos, rotation);
      drawRect(new Vec3d(pos.getX() + laserDist, pos.getY() + laserDist, pos.getZ()), new Vec2f(1F, (float)laserWidth), Direction.WEST, cameraPos, rotation);
      drawRect(new Vec3d(pos.getX() + laserDist, pos.getY() + laserDist, pos.getZ()), new Vec2f((float)laserWidth, 1F), Direction.DOWN, cameraPos, rotation);
      drawRect(new Vec3d(pos.getX() + laserDist, pos.getY() - laserDist + 1, pos.getZ()), new Vec2f((float)laserWidth, 1F), Direction.DOWN, cameraPos, rotation);
    }

    RenderSystem.enableCull();
    RenderSystem.enableTexture();
    RenderSystem.enableLighting();
    RenderSystem.disableBlend();
  }
  
  private static void drawRect(Vec3d vertex1, Vec2f scale, Direction face, Vec3d cameraPos, Quaternion rotation) {
    MatrixStack matrixStackTemp = new MatrixStack();

    RenderSystem.pushMatrix();
    
    double x = vertex1.x - cameraPos.x;
    double y = vertex1.y - cameraPos.y;
    double z = vertex1.z - cameraPos.z;

    double x2 = Math.floor(vertex1.x) - vertex1.x + 0.5;
    double y2 = Math.floor(vertex1.y) - vertex1.y + 0.5;
    double z2 = Math.floor(vertex1.z) - vertex1.z + 0.5;
    
    overlayTranslations(x, y, z, x2, y2, z2, face, matrixStackTemp, rotation);
    RenderSystem.multMatrix(matrixStackTemp.peek().getModel());

    Tessellator tessellator = Tessellator.getInstance();
    BufferBuilder buffer = tessellator.getBuffer();

    buffer.begin(GL11.GL_QUADS, VertexFormats.POSITION);

    buffer.vertex(x, y, z).next();
    buffer.vertex(x + scale.x, y, z).next();
    buffer.vertex(x + scale.x, y + scale.y, z).next();
    buffer.vertex(x, y + scale.y, z).next();

    tessellator.draw();

    RenderSystem.popMatrix();
  }

  private static void color(float r, float g, float b, float a)
  {
    RenderSystem.color4f(r, g, b, a);
  }

  private static void overlayTranslations(double x, double y, double z, double x2, double y2, double z2, Direction side, MatrixStack matrixStack, Quaternion rotation)
  {
    matrixStack.translate(x + x2, y + y2, z + z2);
    
    matrixStack.multiply(rotation);
    matrixStack.translate(-x2, -y2, -z2);

    matrixStack.multiply(getRotation(side));

    matrixStack.translate(-x, -y, -z);
  }

  private static Quaternion getRotation(Direction side) {
    switch (side)
    {
      case DOWN:
        return Vector3f.POSITIVE_X.getDegreesQuaternion(90f);
      case UP:
        return Vector3f.POSITIVE_X.getDegreesQuaternion(-90f);
      case NORTH:
        return Vector3f.POSITIVE_Y.getDegreesQuaternion(180f);
      case SOUTH:
        return Quaternion.IDENTITY;
      case WEST:
        return Vector3f.POSITIVE_Y.getDegreesQuaternion(-90f);
      case EAST:
        return Vector3f.POSITIVE_Y.getDegreesQuaternion(90f);
      default:
        return Quaternion.IDENTITY;
    }
  }

  private static int[] calcualteColor(int powerBits, int λBits) {
    float power = Float.intBitsToFloat(powerBits) / 16;
    float λ = Float.intBitsToFloat(λBits);

    int a = 0;
    int r = 0;
    int g = 0;
    int b = 0;

    if (λ > 15) {
      r = 255;
      g = 255;
      b = 255;
      a = (int)remap(λ, 15, 16, 0, 128);
    } else if (λ > 14.25) {

    } else if (λ > 14) {
      r = 127;
      g = 0;
      b = 255;
      a = (int)remap(λ, 14, 14.25F, 255, 0);
    } else if (λ > 2) {
      int[] colors = hslToRgb(remap(λ, 2, 14, 0, 0.75F), 1, 0.5F);
      r = colors[0];
      g = colors[1];
      b = colors[2];
      a = 255;
    } else if (λ > 1.75) {
      r = 255;
      g = 0;
      b = 0;
      a = (int)remap(λ, 1.75F, 2, 0, 255);
    } else if (λ > 1) {

    } else {
      r = 255;
      g = 64;
      b = 0;
      a = (int)remap(λ, 0, 1, 128, 0);
    }

    if (power < 0) power = 0;

    return new int[] {(int)(a * power), r, g, b};
  }

  private static float remap(float v, float min1, float max1, float min2, float max2) {
    return (v - min1) * (max2 - min2) / (max1 - min1) + min2;
  }

   /**
   * https://stackoverflow.com/questions/2353211/hsl-to-rgb-color-conversion
   * 
   * Converts an HSL color value to RGB. Conversion formula
   * adapted from http://en.wikipedia.org/wiki/HSL_color_space.
   * Assumes h, s, and l are contained in the set [0, 1] and
   * returns r, g, and b in the set [0, 255].
   *
   * @param h       The hue
   * @param s       The saturation
   * @param l       The lightness
   * @return int array, the RGB representation
   */
  private static int[] hslToRgb(float h, float s, float l){
    float r, g, b;

    if (s == 0f) {
        r = g = b = l; // achromatic
    } else {
        float q = l < 0.5f ? l * (1 + s) : l + s - l * s;
        float p = 2 * l - q;
        r = hueToRgb(p, q, h + 1f/3f);
        g = hueToRgb(p, q, h);
        b = hueToRgb(p, q, h - 1f/3f);
    }
    int[] rgb = {to255(r), to255(g), to255(b)};
    return rgb;
  }
  private static int to255(float v) { return (int)Math.min(255,256*v); }

  /** Helper method that converts hue to rgb */
  private static float hueToRgb(float p, float q, float t) {
    if (t < 0f)
        t += 1f;
    if (t > 1f)
        t -= 1f;
    if (t < 1f/6f)
        return p + (q - p) * 6f * t;
    if (t < 1f/2f)
        return q;
    if (t < 2f/3f)
        return p + (q - p) * (2f/3f - t) * 6f;
    return p;
  }

  private static int[] directions = new int[] {Direction.NORTH.getId(), Direction.SOUTH.getId(), Direction.EAST.getId(), Direction.WEST.getId(), Direction.UP.getId(), Direction.DOWN.getId()};
}
