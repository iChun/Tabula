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
import us.ichun.mods.tabula.common.Tabula;

public class PacketCreateNewAnimation extends AbstractPacket
{
    public String host;
    public String projIdent;
    public String animName;
    public boolean loop;

    public PacketCreateNewAnimation(){}

    public PacketCreateNewAnimation(String host, String projIdent, String animName, boolean loop)
    {
        this.host = host;
        this.projIdent = projIdent;
        this.animName = animName;
        this.loop = loop;
    }

    @Override
    public void writeTo(ByteBuf buffer, Side side)
    {
        ByteBufUtils.writeUTF8String(buffer, host);
        ByteBufUtils.writeUTF8String(buffer, projIdent);
        ByteBufUtils.writeUTF8String(buffer, animName);
        buffer.writeBoolean(loop);
    }

    @Override
    public void readFrom(ByteBuf buffer, Side side)
    {
        host = ByteBufUtils.readUTF8String(buffer);
        projIdent = ByteBufUtils.readUTF8String(buffer);
        animName = ByteBufUtils.readUTF8String(buffer);
        loop = buffer.readBoolean();
    }

    @Override
    public void execute(Side side, EntityPlayer player)
    {
        if(side.isServer())
        {
            EntityPlayerMP hoster = FMLCommonHandler.instance().getMinecraftServerInstance().getConfigurationManager().func_152612_a(host);
            if(hoster != null)
            {
                PacketHandler.sendToPlayer(Tabula.channels, this, hoster);
            }
        }
        else
        {
            handleClient();
        }
    }

    @SideOnly(Side.CLIENT)
    public void handleClient()
    {
        if(Tabula.proxy.tickHandlerClient.mainframe != null && Minecraft.getMinecraft().getSession().getUsername().equals(host))
        {
            Tabula.proxy.tickHandlerClient.mainframe.createNewAnimation(projIdent, animName, loop);
        }
    }
}
