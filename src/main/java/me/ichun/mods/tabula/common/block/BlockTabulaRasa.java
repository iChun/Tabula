package me.ichun.mods.tabula.common.block;

import me.ichun.mods.tabula.common.tileentity.TileEntityTabulaRasa;
import net.minecraft.block.Block;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorldReader;
import net.minecraft.world.World;

import javax.annotation.Nullable;

public class BlockTabulaRasa extends Block
{
    public static final VoxelShape SHAPE = Block.makeCuboidShape(2.0D, 0.0D, 2.0D, 14.0D, 2.0D, 14.0D);

    public BlockTabulaRasa()
    {
        super(Block.Properties.create(Material.MISCELLANEOUS)
//                .doesNotBlockMovement()
                .hardnessAndResistance(0.0F)
                .sound(SoundType.WOOD)
        );
    }

    @Override
    public boolean hasTileEntity(BlockState state)
    {
        return true;
    }

    @Nullable
    @Override
    public TileEntity createTileEntity(BlockState state, IBlockReader world)
    {
        return new TileEntityTabulaRasa();
    }

    @Override
    public boolean canSpawnInBlock() {
        return true;
    }

    @Override
    public BlockRenderType getRenderType(BlockState state) {
        return BlockRenderType.ENTITYBLOCK_ANIMATED;
    }

    @Override
    public void onBlockPlacedBy(World world, BlockPos pos, BlockState state, @Nullable LivingEntity living, ItemStack stack)
    {
        super.onBlockPlacedBy(world, pos, state, living, stack);
        TileEntity te = world.getTileEntity(pos);
        if(te instanceof TileEntityTabulaRasa && living != null)
        {
            TileEntityTabulaRasa tr = (TileEntityTabulaRasa)te;
            tr.facing = Direction.fromAngle(living.rotationYaw);
        }
    }

    @Override
    public ActionResultType onBlockActivated(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand handIn, BlockRayTraceResult hit)
    {
//        if(world.isRemote)
//        {
//            Tabula.channel.sendToServer(new PacketRequestSession(pos.getX(), pos.getY(), pos.getZ()));
//        }
        return ActionResultType.PASS;
    }

    @Override
    public float getBlockHardness(BlockState blockState, IBlockReader world, BlockPos pos)
    {
        TileEntity te = world.getTileEntity(pos);
        if(te instanceof TileEntityTabulaRasa)
        {
            TileEntityTabulaRasa tr = (TileEntityTabulaRasa)te;
            if(!tr.host.isEmpty())
            {
                return -1.0F;
            }
        }
        return this.blockHardness;
    }

    @Override
    public VoxelShape getShape(BlockState state, IBlockReader worldIn, BlockPos pos, ISelectionContext context) {
        return SHAPE;
    }

    @Override
    public boolean isValidPosition(BlockState state, IWorldReader worldIn, BlockPos pos) {
        return hasSolidSideOnTop(worldIn, pos.down());
    }

    @Override
    public void neighborChanged(BlockState state, World world, BlockPos pos, Block block, BlockPos fromPos, boolean isMoving)
    {
        if (!world.isRemote)
        {
            TileEntity te = world.getTileEntity(pos);
            if(te instanceof TileEntityTabulaRasa)
            {
                TileEntityTabulaRasa tr = (TileEntityTabulaRasa)te;
                if(!tr.host.isEmpty())
                {
                    return;
                }
            }
            if(!state.isValidPosition(world, pos))
            {
                world.removeBlock(pos, false);
                spawnDrops(state, world, pos);
//                spawnAsEntity(world, pos, new ItemStack(Tabula.blockTabulaRasa, 1));
            }
        }
    }
}
