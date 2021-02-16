package net.fabricmc.LaserMod;

import org.lwjgl.glfw.GLFW;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.client.options.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.network.PacketByteBuf;

public class keybinds {
  private static String category = "category.lasermod";
  private static KeyBinding increase = KeyBindingHelper.registerKeyBinding(new KeyBinding("key.lasermod.increase", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_UP, category));
  private static KeyBinding decrease = KeyBindingHelper.registerKeyBinding(new KeyBinding("key.lasermod.decrease", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_DOWN, category));
  private static KeyBinding nudgeUp = KeyBindingHelper.registerKeyBinding(new KeyBinding("key.lasermod.nudgeup", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_RIGHT, category));
  private static KeyBinding nudgeDown = KeyBindingHelper.registerKeyBinding(new KeyBinding("key.lasermod.nudgedown", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_LEFT, category));
  
  public static void registerKeybinds() {
    
    ClientTickEvents.END_CLIENT_TICK.register(client -> {
      if (increase.wasPressed()) {
        PacketByteBuf msg = PacketByteBufs.create();
        msg.writeByte(10);
        ClientPlayNetworking.send(NetworkingIdentifiers.NudgeFrequency, msg);
      }
      
      if (decrease.wasPressed()) {
        PacketByteBuf msg = PacketByteBufs.create();
        msg.writeByte(-10);
        ClientPlayNetworking.send(NetworkingIdentifiers.NudgeFrequency, msg);
      }
      
      if (nudgeUp.wasPressed()) {
        PacketByteBuf msg = PacketByteBufs.create();
        msg.writeByte(1);
        ClientPlayNetworking.send(NetworkingIdentifiers.NudgeFrequency, msg);
      }
      
      if (nudgeDown.wasPressed()) {
        PacketByteBuf msg = PacketByteBufs.create();
        msg.writeByte(-1);
        ClientPlayNetworking.send(NetworkingIdentifiers.NudgeFrequency, msg);
      }
    });
  }
}
