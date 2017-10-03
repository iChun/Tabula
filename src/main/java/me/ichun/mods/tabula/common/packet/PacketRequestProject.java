package me.ichun.mods.tabula.common.packet;

import io.netty.buffer.ByteBuf;
import me.ichun.mods.ichunutil.common.core.network.AbstractPacket;
import me.ichun.mods.ichunutil.common.module.tabula.project.ProjectInfo;
import me.ichun.mods.tabula.common.Tabula;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class PacketRequestProject extends AbstractPacket
{
    public String host;
    public String listener;
    public String ident;
    public boolean isTexture;

    public PacketRequestProject(){}

    public PacketRequestProject(String host, String listener, String ident, boolean isTexture)
    {
        this.host = host;
        this.listener = listener;
        this.ident = ident;
        this.isTexture = isTexture;
    }

    @Override
    public void writeTo(ByteBuf buffer)
    {
        ByteBufUtils.writeUTF8String(buffer, host);
        ByteBufUtils.writeUTF8String(buffer, listener);
        ByteBufUtils.writeUTF8String(buffer, ident);
        buffer.writeBoolean(isTexture);
    }

    @Override
    public void readFrom(ByteBuf buffer)
    {
        host = ByteBufUtils.readUTF8String(buffer);
        listener = ByteBufUtils.readUTF8String(buffer);
        ident = ByteBufUtils.readUTF8String(buffer);
        isTexture = buffer.readBoolean();
    }

    @Override
    public void execute(Side side, EntityPlayer player)
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
            for(ProjectInfo project : Tabula.proxy.tickHandlerClient.mainframe.projects)
            {
                if(project.identifier.equals(ident))
                {
                    if(isTexture)
                    {
                        Tabula.proxy.tickHandlerClient.mainframe.streamProjectTextureToListener(listener, project.identifier, project.bufferedTexture, false);
                    }
                    else
                    {
                        Tabula.proxy.tickHandlerClient.mainframe.streamProjectToListener(listener, project, false);
                    }
                }
            }
        }
    }
}
