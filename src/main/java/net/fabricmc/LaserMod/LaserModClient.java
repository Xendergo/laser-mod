package net.fabricmc.LaserMod;

import net.fabricmc.LaserMod.Sounds.LaserSound;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.util.registry.Registry;

public class LaserModClient implements ClientModInitializer{

	@Override
	public void onInitializeClient() {
		ClientPlayNetworking.registerGlobalReceiver(NetworkingIdentifiers.LaserStorage, (client, handler, buf, responseSender) -> {
			LaserStorageClient.processLaserData(buf.readIntArray(), buf.readLongArray(null), buf.readBoolean());
		});

    BlockRenderLayerMap.INSTANCE.putBlock(LaserMod.Lens, RenderLayer.getCutout());
		BlockRenderLayerMap.INSTANCE.putBlock(LaserMod.Laser, RenderLayer.getCutout());
    BlockRenderLayerMap.INSTANCE.putBlock(LaserMod.LaserDetector, RenderLayer.getCutout());
    BlockRenderLayerMap.INSTANCE.putBlock(LaserMod.BeamSplitter, RenderLayer.getCutout());
		BlockRenderLayerMap.INSTANCE.putBlock(LaserMod.Coupler, RenderLayer.getCutout());

    System.out.println("Registering Laser mod keybinds");
    keybinds.registerKeybinds();

		System.out.println("Laser mod keybinds are registered, registering sounds");

		Registry.register(Registry.SOUND_EVENT, LaserSound.LaserSoundID, LaserSound.LaserSound);

		WorldRenderEvents.AFTER_TRANSLUCENT.register(new LaserRenderer());
	}
}
