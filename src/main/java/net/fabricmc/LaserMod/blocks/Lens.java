package net.fabricmc.LaserMod.blocks;

import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Material;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.Properties;
import net.minecraft.util.math.Direction;

public class Lens extends Block {
  public Lens() {
    super(FabricBlockSettings.of(Material.METAL).hardness(3.0f).resistance(1200.0f).nonOpaque());
    setDefaultState(this.stateManager.getDefaultState().with(Properties.FACING, Direction.NORTH));
  }

  public BlockState getPlacementState(ItemPlacementContext ctx) {
    return (BlockState)this.getDefaultState().with(Properties.FACING, ctx.getPlayerLookDirection());
  }

  @Override
  protected void appendProperties(StateManager.Builder<Block, BlockState> stateManager) {
    stateManager.add(Properties.FACING);
  }
}
