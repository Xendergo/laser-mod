package net.fabricmc.LaserMod;

import net.fabricmc.LaserMod.blocks.Lens;
import net.fabricmc.LaserMod.blocks.BeamSplitter;
import net.fabricmc.LaserMod.blocks.Laser;
import net.fabricmc.LaserMod.blocks.LaserDetector;
import net.fabricmc.LaserMod.blocks.LaserEntity;
import net.fabricmc.LaserMod.blocks.FiberOpticCable;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.block.Block;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemGroup;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

public class LaserMod implements ModInitializer {
	public static final Block Lens = new Lens();
	public static final Block Laser = new Laser();
	public static final Block LaserDetector = new LaserDetector();
	public static final Block BeamSplitter = new BeamSplitter();
	public static final Block FiberOpticCable = new FiberOpticCable();

	public static BlockEntityType<LaserEntity> LaserEntityData;
	
	@Override
	public void onInitialize() {
		ClientPlayNetworking.registerGlobalReceiver(NetworkingIdentifiers.LaserStorage, (client, handler, buf, responseSender) -> {
			LaserStorageClient.processLaserData(buf.readIntArray());
		});
		
		ServerPlayNetworking.registerGlobalReceiver(NetworkingIdentifiers.NudgeFrequency, (server, player, handler, buf, responseSender) -> {
			LaserStorage.handleNudge(server, player, buf);
		});

		System.out.println("Laser mod initialized, registering blocks");

		Registry.register(Registry.BLOCK, new Identifier("lasermod", "lens"), Lens);
		Registry.register(Registry.ITEM, new Identifier("lasermod", "lens"), new BlockItem(Lens, new FabricItemSettings().group(ItemGroup.REDSTONE)));
		BlockRenderLayerMap.INSTANCE.putBlock(Lens, RenderLayer.getCutout());

		Registry.register(Registry.BLOCK, new Identifier("lasermod", "laser"), Laser);
		Registry.register(Registry.ITEM, new Identifier("lasermod", "laser"), new BlockItem(Laser, new FabricItemSettings().group(ItemGroup.REDSTONE)));
		LaserEntityData = Registry.register(Registry.BLOCK_ENTITY_TYPE, "lasermod:laser", BlockEntityType.Builder.create(LaserEntity::new, Laser).build(null));
		BlockRenderLayerMap.INSTANCE.putBlock(Laser, RenderLayer.getCutout());

		Registry.register(Registry.BLOCK, new Identifier("lasermod", "laserdetector"), LaserDetector);
		Registry.register(Registry.ITEM, new Identifier("lasermod", "laserdetector"), new BlockItem(LaserDetector, new FabricItemSettings().group(ItemGroup.REDSTONE)));
		BlockRenderLayerMap.INSTANCE.putBlock(LaserDetector, RenderLayer.getCutout());

		Registry.register(Registry.BLOCK, new Identifier("lasermod", "beamsplitter"), BeamSplitter);
		Registry.register(Registry.ITEM, new Identifier("lasermod", "beamsplitter"), new BlockItem(BeamSplitter, new FabricItemSettings().group(ItemGroup.REDSTONE)));
		BlockRenderLayerMap.INSTANCE.putBlock(BeamSplitter, RenderLayer.getCutout());

		Registry.register(Registry.BLOCK, new Identifier("lasermod", "fiberopticcable"), FiberOpticCable);
		Registry.register(Registry.ITEM, new Identifier("lasermod", "fiberopticcable"), new BlockItem(FiberOpticCable, new FabricItemSettings().group(ItemGroup.REDSTONE)));

		System.out.println("Laser mod blocks are registered, registering keybinds");

		keybinds.registerKeybinds();
	}
}
