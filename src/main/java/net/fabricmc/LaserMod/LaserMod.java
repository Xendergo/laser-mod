package net.fabricmc.LaserMod;

import net.fabricmc.LaserMod.blocks.Lens;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap;
import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.minecraft.block.Block;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemGroup;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

public class LaserMod implements ModInitializer {
	public static final Block Lens = new Lens();
	
	@Override
	public void onInitialize() {
		// This code runs as soon as Minecraft is in a mod-load-ready state.
		// However, some things (like resources) may still be uninitialized.
		// Proceed with mild caution.

		System.out.println("Laser mod initialized, registering blocks");

		Registry.register(Registry.BLOCK, new Identifier("lasermod", "lens"), Lens);
		Registry.register(Registry.ITEM, new Identifier("lasermod", "lens"), new BlockItem(Lens, new FabricItemSettings().group(ItemGroup.REDSTONE)));
		BlockRenderLayerMap.INSTANCE.putBlock(Lens, RenderLayer.getCutout());
	}
}
