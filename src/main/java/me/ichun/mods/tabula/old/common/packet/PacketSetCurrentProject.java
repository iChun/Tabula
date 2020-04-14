package me.ichun.mods.tabula.old.common.packet;

import io.netty.buffer.ByteBuf;
import me.ichun.mods.ichunutil.common.core.network.AbstractPacket;
import me.ichun.mods.tabula.old.common.tileentity.TileEntityTabulaRasa;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.relauncher.Side;

public class PacketSetCurrentProject extends AbstractPacket
{

    public String host;
    public int x;
    public int y;
    public int z;
    public String ident;

    public PacketSetCurrentProject(){}

    public PacketSetCurrentProject(String name, int i, int j, int k, String identt)
    {
        host = name;
        x = i;
        y = j;
        z = k;
        ident = identt;
    }

    @Override
    public void writeTo(ByteBuf buffer)
    {
        ByteBufUtils.writeUTF8String(buffer, host);
        buffer.writeInt(x);
        buffer.writeInt(y);
        buffer.writeInt(z);
        ByteBufUtils.writeUTF8String(buffer, ident);
    }

    @Override
    public void readFrom(ByteBuf buffer)
    {
        host = ByteBufUtils.readUTF8String(buffer);
        x = buffer.readInt();
        y = buffer.readInt();
        z = buffer.readInt();
        ident = ByteBufUtils.readUTF8String(buffer);
    }

    @Override
    public void execute(Side side, EntityPlayer player)
    {
        if(side.isServer())
        {
            TileEntity te = player.world.getTileEntity(new BlockPos(x, y, z));
            if(te instanceof TileEntityTabulaRasa)
            {
                TileEntityTabulaRasa tr = (TileEntityTabulaRasa)te;
                if(tr.host.equals(player.getName()))
                {
                    tr.currentProj = ident;
                    tr.needProjectUpdate = true;
                    tr.needTextureUpdate = true;
                    IBlockState state = tr.getWorld().getBlockState(tr.getPos());
                    tr.getWorld().notifyBlockUpdate(tr.getPos(), state, state, 3);
                }
            }
        }
    }

    @Override
    public Side receivingSide()
    {
        return Side.SERVER;
    }
}
