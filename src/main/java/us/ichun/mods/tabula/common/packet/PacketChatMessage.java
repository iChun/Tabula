package us.ichun.mods.tabula.common.packet;

import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.network.ByteBufUtils;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import ichun.common.core.network.AbstractPacket;
import ichun.common.core.network.PacketHandler;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import us.ichun.mods.tabula.client.mainframe.core.ProjectHelper;
import us.ichun.mods.tabula.common.Tabula;

public class PacketChatMessage extends AbstractPacket
{
    public String host;
    public String listener;
    public String message;

    public PacketChatMessage(){}

    public PacketChatMessage(String host, String listener, String message)
    {
        this.host = host;
        this.listener = listener;
        this.message = message;
    }

    @Override
    public void writeTo(ByteBuf buffer, Side side)
    {
        ByteBufUtils.writeUTF8String(buffer, host);
        ByteBufUtils.writeUTF8String(buffer, listener);
        ByteBufUtils.writeUTF8String(buffer, message);
    }

    @Override
    public void readFrom(ByteBuf buffer, Side side)
    {
        host = ByteBufUtils.readUTF8String(buffer);
        listener = ByteBufUtils.readUTF8String(buffer);
        message = ByteBufUtils.readUTF8String(buffer);
    }

    @Override
    public void execute(Side side, EntityPlayer player)
    {
        if(side.isServer())
        {
            EntityPlayerMP listener1 = FMLCommonHandler.instance().getMinecraftServerInstance().getConfigurationManager().func_152612_a(listener);
            if(listener1 != null)
            {
                PacketHandler.sendToPlayer(Tabula.channels, this, listener1);
            }
        }
        else
        {
            ProjectHelper.receiveChat(message);
        }
    }
}
