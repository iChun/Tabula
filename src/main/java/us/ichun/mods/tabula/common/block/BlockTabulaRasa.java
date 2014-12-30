package us.ichun.mods.tabula.common.block;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import ichun.common.core.network.PacketHandler;
import net.minecraft.block.Block;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;
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
    public boolean renderAsNormalBlock()
    {
        return false;
    }

    @Override
    public int getRenderType()
    {
        return 8;
    }

    @Override
    public void onBlockPlacedBy(World world, int i, int j, int k, EntityLivingBase living, ItemStack stack)
    {
        super.onBlockPlacedBy(world, i, j, k, living, stack);
        TileEntity te = world.getTileEntity(i, j, k);
        if(te instanceof TileEntityTabulaRasa)
        {
            TileEntityTabulaRasa tr = (TileEntityTabulaRasa)te;
            tr.side = MathHelper.floor_double((double)(living.rotationYaw * 4.0F / 360.0F) + 0.5D) & 3;
        }
    }

    @Override
    public boolean onBlockActivated(World world, int x, int y, int z, EntityPlayer player, int side, float hitVecX, float hitVecY, float hitVecZ)
    {
        if(world.isRemote)
        {
            PacketHandler.sendToServer(Tabula.channels, new PacketRequestSession(x, y, z));
        }
        return true;
    }

    @Override
    public float getBlockHardness(World world, int i, int j, int k)
    {
        TileEntity te = world.getTileEntity(i, j, k);
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
    public AxisAlignedBB getCollisionBoundingBoxFromPool(World p_149668_1_, int p_149668_2_, int p_149668_3_, int p_149668_4_)
    {
        return null;
    }

    @Override
    public boolean canPlaceBlockAt(World world, int i, int j, int k)
    {
        return world.isSideSolid(i, j - 1, k, ForgeDirection.UP, false);
    }

    @Override
    public void onNeighborBlockChange(World world, int i, int j, int k, Block block)
    {
        TileEntity te = world.getTileEntity(i, j, k);
        if(te instanceof TileEntityTabulaRasa)
        {
            TileEntityTabulaRasa tr = (TileEntityTabulaRasa)te;
            if(!tr.host.isEmpty())
            {
                return;
            }
        }
        if(!world.isSideSolid(i, j - 1, k, ForgeDirection.UP, false))
        {
            world.setBlockToAir(i, j, k);
            dropBlockAsItem(world, i, j, k, new ItemStack(Tabula.blockTabulaRasa, 1));
        }
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void registerBlockIcons(IIconRegister iconRegister) {
        this.blockIcon = iconRegister.registerIcon("tabula:tabulaRasa");
    }
}
