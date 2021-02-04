package net.fabricmc.LaserMod.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.fabricmc.LaserMod.blocks.Laser;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.ComparatorBlock;
import net.minecraft.state.property.Properties;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.World;

@Mixin(ComparatorBlock.class)
public class ComparatorPower {
	@Inject(at = @At("TAIL"), method = "update()V")
	protected void update(World world, BlockPos pos, BlockState state, CallbackInfo info) {
		Block block = world.getBlockState(pos.offset(state.get(Properties.HORIZONTAL_FACING), -1)).getBlock();
		if (Registry.BLOCK.getId(block).toString().equals("lasermod:laser")) {
			((Laser)block).updateComparatorPower(world.getEmittedRedstonePower(pos, state.get(Properties.HORIZONTAL_FACING)));
		}
	}
}
