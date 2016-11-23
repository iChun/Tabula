package us.ichun.mods.tabula.common.packet;

import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.relauncher.Side;
import me.ichun.mods.ichunutil.common.core.network.AbstractPacket;
import us.ichun.mods.tabula.common.Tabula;

public class PacketRequestHeartbeat extends AbstractPacket
{
    public String host;
    public String listener;

    public PacketRequestHeartbeat(){}

    public PacketRequestHeartbeat(String host, String listener)
    {
        this.host = host;
        this.listener = listener;
    }

    @Override
    public void writeTo(ByteBuf buffer)
    {
        ByteBufUtils.writeUTF8String(buffer, host);
        ByteBufUtils.writeUTF8String(buffer, listener);
    }

    @Override
    public void readFrom(ByteBuf buffer)
    {
        host = ByteBufUtils.readUTF8String(buffer);
        listener = ByteBufUtils.readUTF8String(buffer);
    }

    @Override
    public AbstractPacket execute(Side side, EntityPlayer player)
    {
        if(side.isServer())
        {
            EntityPlayerMP listening = FMLCommonHandler.instance().getMinecraftServerInstance().getConfigurationManager().getPlayerByUsername(listener);
            if(listening != null)
            {
                Tabula.channel.sendToPlayer(this, listening);
            }
        }
        else
        {
            Tabula.channel.sendToServer(new PacketHeartbeat(host, listener));
        }
    }
}
