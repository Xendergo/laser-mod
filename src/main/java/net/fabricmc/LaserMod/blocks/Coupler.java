package net.fabricmc.LaserMod.blocks;

import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Material;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.Properties;
import net.minecraft.util.math.Direction;

public class Coupler extends Block {
  public Coupler() {
    super(FabricBlockSettings.of(Material.METAL).hardness(3.0f).resistance(1200.0f).nonOpaque().luminance((state) -> (Boolean)state.get(Properties.LIT) ? 15 : 0).solidBlock((a, b, c) -> false));
    setDefaultState(this.stateManager.getDefaultState().with(Properties.FACING, Direction.NORTH).with(Properties.LIT, false));
  }

  public BlockState getPlacementState(ItemPlacementContext ctx) {
    return (BlockState)this.getDefaultState().with(Properties.FACING, ctx.getPlayerLookDirection());
  }

  @Override
  protected void appendProperties(StateManager.Builder<Block, BlockState> stateManager) {
    stateManager.add(Properties.FACING).add(Properties.LIT);
  }
}
