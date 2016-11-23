package us.ichun.mods.tabula.common.packet;

import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.relauncher.Side;
import me.ichun.mods.ichunutil.common.core.network.AbstractPacket;
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
    public void writeTo(ByteBuf buffer)
    {
        ByteBufUtils.writeUTF8String(buffer, host);
        ByteBufUtils.writeUTF8String(buffer, listener);
        ByteBufUtils.writeUTF8String(buffer, message);
    }

    @Override
    public void readFrom(ByteBuf buffer)
    {
        host = ByteBufUtils.readUTF8String(buffer);
        listener = ByteBufUtils.readUTF8String(buffer);
        message = ByteBufUtils.readUTF8String(buffer);
    }

    @Override
    public AbstractPacket execute(Side side, EntityPlayer player)
    {
        if(side.isServer())
        {
            EntityPlayerMP listener1 = FMLCommonHandler.instance().getMinecraftServerInstance().getConfigurationManager().getPlayerByUsername(listener);
            if(listener1 != null)
            {
                Tabula.channel.sendToPlayer(this, listener1);
            }
        }
        else
        {
            ProjectHelper.receiveChat(message);
        }
    }
}
