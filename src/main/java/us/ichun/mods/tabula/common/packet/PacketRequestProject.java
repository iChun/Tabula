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
import us.ichun.module.tabula.common.project.ProjectInfo;

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
    public void writeTo(ByteBuf buffer, Side side)
    {
        ByteBufUtils.writeUTF8String(buffer, host);
        ByteBufUtils.writeUTF8String(buffer, listener);
        ByteBufUtils.writeUTF8String(buffer, ident);
        buffer.writeBoolean(isTexture);
    }

    @Override
    public void readFrom(ByteBuf buffer, Side side)
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
