package me.ichun.mods.tabula.common.packet;

import io.netty.buffer.ByteBuf;
import me.ichun.mods.ichunutil.common.core.network.AbstractPacket;
import me.ichun.mods.tabula.common.Tabula;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class PacketHeartbeat extends AbstractPacket
{
    public String host;
    public String listener;

    public PacketHeartbeat(){}

    public PacketHeartbeat(String host, String listener)
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
            EntityPlayerMP hoster = FMLCommonHandler.instance().getMinecraftServerInstance().getPlayerList().getPlayerByUsername(host);
            if(hoster != null)
            {
                Tabula.channel.sendTo(this, hoster);
            }
        }
        else
        {
            handleClient();
        }
        return null;
    }

    @Override
    public Side receivingSide()
    {
        return null;
    }

    @SideOnly(Side.CLIENT)
    public void handleClient()
    {
        if(Tabula.proxy.tickHandlerClient.mainframe != null && Minecraft.getMinecraft().getSession().getUsername().equals(host))
        {
            Tabula.proxy.tickHandlerClient.mainframe.listeners.put(listener, 0);
        }
    }
}
