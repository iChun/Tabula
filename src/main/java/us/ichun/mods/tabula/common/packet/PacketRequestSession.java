package us.ichun.mods.tabula.common.packet;

import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.relauncher.Side;
import ichun.common.core.network.AbstractPacket;
import ichun.common.core.network.PacketHandler;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.tileentity.TileEntity;
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
        TileEntity te = player.worldObj.getTileEntity(x, y, z);
        if(te instanceof TileEntityTabulaRasa)
        {
            TileEntityTabulaRasa tr = (TileEntityTabulaRasa)te;
            if(tr.host.isEmpty())
            {
                //Start new session with this player
                tr.host = player.getCommandSenderName();
                PacketHandler.sendToPlayer(Tabula.channels, new PacketBeginSession(tr.host, x, y, z), player);
            }
            else if(!tr.host.equals(player.getCommandSenderName()))
            {
                EntityPlayerMP host = FMLCommonHandler.instance().getMinecraftServerInstance().getConfigurationManager().func_152612_a(tr.host);
                //Connect to existing session
                if(host != null)
                {
                    if(!tr.listeners.contains(player.getCommandSenderName()))
                    {
                        tr.listeners.add(player.getCommandSenderName());
                    }
                    PacketHandler.sendToPlayer(Tabula.channels, new PacketAddListener(tr.host, player.getCommandSenderName()), host);
                    PacketHandler.sendToPlayer(Tabula.channels, new PacketBeginSession(tr.host, x, y, z), player);
                }
            }
        }
    }
}
