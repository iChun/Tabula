package me.ichun.mods.tabula.common.block;

import me.ichun.mods.tabula.common.Tabula;
import me.ichun.mods.tabula.common.packet.PacketRequestSession;
import me.ichun.mods.tabula.common.tileentity.TileEntityTabulaRasa;
import net.minecraft.block.Block;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

public class BlockTabulaRasa extends Block
        implements ITileEntityProvider
{
    public static final AxisAlignedBB BLOCK_AABB = new AxisAlignedBB(0.125F, 0.0F, 0.125F, 1.0F - 0.125F, 0.125F, 1.0F - 0.125F);

    public BlockTabulaRasa(Material mat)
    {
        super(mat);
        setSoundType(SoundType.WOOD);
    }

    @Override
    public TileEntity createNewTileEntity(World p_149915_1_, int p_149915_2_)
    {
        return new TileEntityTabulaRasa();
    }


    @Override
    public boolean isFullCube(IBlockState state)
    {
        return false;
    }

    @Override
    public boolean isNormalCube(IBlockState state, IBlockAccess world, BlockPos pos)
    {
        return false;
    }

    @Override
    public boolean isOpaqueCube(IBlockState state)
    {
        return false;
    }

    @Override
    public boolean isCollidable()
    {
        return true;
    }

    @Override
    public void onBlockPlacedBy(World world, BlockPos pos, IBlockState state, EntityLivingBase living, ItemStack stack)
    {
        super.onBlockPlacedBy(world, pos, state, living, stack);
        TileEntity te = world.getTileEntity(pos);
        if(te instanceof TileEntityTabulaRasa)
        {
            TileEntityTabulaRasa tr = (TileEntityTabulaRasa)te;
            tr.side = MathHelper.floor((double)(living.rotationYaw * 4.0F / 360.0F) + 0.5D) & 3;
        }
    }

    @Override
    public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer player, EnumHand hand, EnumFacing side, float hitX, float hitY, float hitZ)
    {
        if(world.isRemote)
        {
            Tabula.channel.sendToServer(new PacketRequestSession(pos.getX(), pos.getY(), pos.getZ()));
        }
        return true;
    }

    @Override
    public float getBlockHardness(IBlockState blockState, World world, BlockPos pos)
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
    public AxisAlignedBB getCollisionBoundingBox(IBlockState blockState, IBlockAccess worldIn, BlockPos pos)
    {
        return null;
    }

    @Override
    public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess source, BlockPos pos)
    {
        return BLOCK_AABB;
    }

    @Override
    public boolean canPlaceBlockAt(World world, BlockPos pos)
    {
        return world.isSideSolid(pos.add(0, -1, 0), EnumFacing.UP, false);
    }

    @Override
    public void neighborChanged(IBlockState state, World world, BlockPos pos, Block block, BlockPos fromPos)
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
        if(!world.isSideSolid(pos.add(0, -1, 0), EnumFacing.UP, false))
        {
            world.setBlockToAir(pos);
            spawnAsEntity(world, pos, new ItemStack(Tabula.blockTabulaRasa, 1));
        }
    }
}
