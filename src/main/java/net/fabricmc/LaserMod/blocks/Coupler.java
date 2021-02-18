package net.fabricmc.LaserMod.blocks;

import java.util.List;
import java.util.stream.Collectors;

import net.fabricmc.LaserMod.LaserStorage;
import net.fabricmc.LaserMod.fiberOpticUpdates;
import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;
import net.minecraft.block.Block;
import net.minecraft.block.BlockEntityProvider;
import net.minecraft.block.BlockState;
import net.minecraft.block.Material;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.piston.PistonBehavior;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemStack;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.Properties;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;

public class Coupler extends Block implements BlockEntityProvider {
  public Coupler() {
    super(FabricBlockSettings.of(Material.METAL).hardness(3.0f).resistance(1200.0f).nonOpaque().luminance((state) -> (Boolean)state.get(Properties.LIT) ? 15 : 0).solidBlock((a, b, c) -> false));
    setDefaultState(this.stateManager.getDefaultState().with(Properties.FACING, Direction.NORTH).with(Properties.LIT, false));
  }

  public BlockState getPlacementState(ItemPlacementContext ctx) {
    return (BlockState)this.getDefaultState().with(Properties.FACING, ctx.getPlayerLookDirection());
  }

  public void onPlaced(World world, BlockPos pos, BlockState state, LivingEntity placer, ItemStack itemStack) {
    super.onPlaced(world, pos, state, placer, itemStack);

    if (!world.isClient()) {
      fiberOpticUpdate(world, pos);
    }
  }

  @Override
  protected void appendProperties(StateManager.Builder<Block, BlockState> stateManager) {
    stateManager.add(Properties.FACING).add(Properties.LIT);
  }

  @Override
  public PistonBehavior getPistonBehavior(BlockState state) {
    return PistonBehavior.BLOCK;
  }

  @Override
  public BlockEntity createBlockEntity(BlockView world) {
    return new CouplerEntity();
  }

  public void laserUpdate(BlockState state, ServerWorld world, BlockPos pos) {
    laserUpdate(state, (World)world, pos);
  }

  public void laserUpdate(BlockState state, World world, BlockPos pos) {
    Direction facing = state.get(Properties.FACING);
    world.setBlockState(pos, (BlockState)state.with(Properties.LIT, LaserStorage.laserPowerAtSpot(pos, facing, world) != 0), 2);
  
    int dirId = facing.getId();
    List<float[]> lasers = LaserStorage.lasersAtPos(world, pos).stream().filter(x -> x[2] >> 2 == dirId).map(x -> new float[] {Float.intBitsToFloat(x[0]), Float.intBitsToFloat(x[1])}).collect(Collectors.toList());

    pos = ((CouplerEntity)world.getBlockEntity(pos)).endPos;
    if (pos != null && world.getBlockState(pos).getBlock() instanceof Coupler) {
      ((CouplerEntity)world.getBlockEntity(pos)).lasersOut = lasers;
    }
  }

  public void fiberOpticUpdate(World world, BlockPos pos) {
    BlockPos endPos = fiberOpticUpdates.getOtherSide(pos, world);

    BlockState state = world.getBlockState(endPos);

    CouplerEntity coupler = ((CouplerEntity)world.getBlockEntity(pos));
    if (state.getBlock() instanceof Coupler) {
      ((CouplerEntity)world.getBlockEntity(endPos)).setEndPos(pos);
      coupler.setEndPos(endPos);
      laserUpdate(state, world, endPos);
      laserUpdate(world.getBlockState(pos), world, pos);
    } else {
      if (coupler.endPos != null && world.getBlockEntity(coupler.endPos) != null) {
        ((CouplerEntity)world.getBlockEntity(coupler.endPos)).setEndPos(null);
      }
      coupler.setEndPos(null);
    }
  }
}
