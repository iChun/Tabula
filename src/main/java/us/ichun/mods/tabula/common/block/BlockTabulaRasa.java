package us.ichun.mods.tabula.common.block;

import net.minecraft.block.Block;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.MathHelper;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import us.ichun.mods.tabula.common.Tabula;
import us.ichun.mods.tabula.common.packet.PacketRequestSession;
import us.ichun.mods.tabula.common.tileentity.TileEntityTabulaRasa;

public class BlockTabulaRasa extends Block
        implements ITileEntityProvider
{
    public BlockTabulaRasa(Material mat)
    {
        super(mat);
    }

    @Override
    public TileEntity createNewTileEntity(World p_149915_1_, int p_149915_2_)
    {
        return new TileEntityTabulaRasa();
    }

    @Override
    public boolean isOpaqueCube()
    {
        return false;
    }

    @Override
    public boolean isFullCube()
    {
        return false;
    }

    @Override
    public int getRenderType()
    {
        return 2;
    }

    @Override
    public void onBlockPlacedBy(World world, BlockPos pos, IBlockState state, EntityLivingBase living, ItemStack stack)
    {
        super.onBlockPlacedBy(world, pos, state, living, stack);
        TileEntity te = world.getTileEntity(pos);
        if(te instanceof TileEntityTabulaRasa)
        {
            TileEntityTabulaRasa tr = (TileEntityTabulaRasa)te;
            tr.side = MathHelper.floor_double((double)(living.rotationYaw * 4.0F / 360.0F) + 0.5D) & 3;
        }
    }

    @Override
    public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer player, EnumFacing side, float hitVecX, float hitVecY, float hitVecZ)
    {
        if(world.isRemote)
        {
            Tabula.channel.sendToServer(new PacketRequestSession(pos.getX(), pos.getY(), pos.getZ()));
        }
        return true;
    }

    @Override
    public float getBlockHardness(World world, BlockPos pos)
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
    public AxisAlignedBB getCollisionBoundingBox(World world_, BlockPos pos, IBlockState state)
    {
        return null;
    }

    @Override
    public void setBlockBoundsBasedOnState(IBlockAccess world, BlockPos pos)
    {
        float width = 0.125F;
        this.setBlockBounds(width, 0.0F, width, 1.0F - width, 0.125F, 1.0F - width);
    }

    @Override
    public boolean canPlaceBlockAt(World world, BlockPos pos)
    {
        return world.isSideSolid(pos.add(0, -1, 0), EnumFacing.UP, false);
    }

    @Override
    public void onNeighborBlockChange(World world, BlockPos pos, IBlockState state, Block block)
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
