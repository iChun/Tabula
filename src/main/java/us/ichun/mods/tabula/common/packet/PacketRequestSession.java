package us.ichun.mods.tabula.common.packet;

import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockPos;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.relauncher.Side;
import us.ichun.mods.ichunutil.common.core.network.AbstractPacket;
import us.ichun.mods.tabula.common.Tabula;
import us.ichun.mods.tabula.common.tileentity.TileEntityTabulaRasa;

public class PacketRequestSession extends AbstractPacket
{
    public int x;
    public int y;
    public int z;

    public PacketRequestSession(){}

    public PacketRequestSession(int i, int j, int k)
    {
        x = i;
        y = j;
        z = k;
    }

    @Override
    public void writeTo(ByteBuf buffer, Side side)
    {
        buffer.writeInt(x);
        buffer.writeInt(y);
        buffer.writeInt(z);
    }

    @Override
    public void readFrom(ByteBuf buffer, Side side)
    {
        x = buffer.readInt();
        y = buffer.readInt();
        z = buffer.readInt();
    }

    @Override
    public void execute(Side side, EntityPlayer player)
    {
        TileEntity te = player.worldObj.getTileEntity(new BlockPos(x, y, z));
        if(te instanceof TileEntityTabulaRasa)
        {
            TileEntityTabulaRasa tr = (TileEntityTabulaRasa)te;
            if(tr.host.isEmpty())
            {
                //Start new session with this player
                tr.host = player.getName();
                Tabula.channel.sendToPlayer(new PacketBeginSession(tr.host, x, y, z), player);
            }
            else if(!tr.host.equals(player.getName()))
            {
                EntityPlayerMP host = FMLCommonHandler.instance().getMinecraftServerInstance().getConfigurationManager().getPlayerByUsername(tr.host);
                //Connect to existing session
                if(host != null)
                {
                    if(!tr.listeners.contains(player.getName()))
                    {
                        tr.listeners.add(player.getName());
                    }
                    Tabula.channel.sendToPlayer(new PacketAddListener(tr.host, player.getName()), host);
                    Tabula.channel.sendToPlayer(new PacketBeginSession(tr.host, x, y, z), player);
                }
            }
        }
    }
}
