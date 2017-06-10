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

import java.util.ArrayList;

public class PacketSetProjectMetadata extends AbstractPacket
{
    public String host;
    public String projIdent;
    public String objIdent;
    public ArrayList<String> meta;
    public boolean isProj;

    public PacketSetProjectMetadata(){}

    public PacketSetProjectMetadata(String host, String projIdent, String objIdent, ArrayList<String> meta, boolean isProj)
    {
        this.host = host;
        this.projIdent = projIdent;
        this.objIdent = objIdent;
        this.meta = meta;
        this.isProj = isProj;
    }

    @Override
    public void writeTo(ByteBuf buffer)
    {
        ByteBufUtils.writeUTF8String(buffer, host);
        ByteBufUtils.writeUTF8String(buffer, projIdent);
        ByteBufUtils.writeUTF8String(buffer, objIdent);
        buffer.writeInt(meta.size());
        for(String aMeta : meta)
        {
            ByteBufUtils.writeUTF8String(buffer, aMeta);
        }
        buffer.writeBoolean(isProj);
    }

    @Override
    public void readFrom(ByteBuf buffer)
    {
        host = ByteBufUtils.readUTF8String(buffer);
        projIdent = ByteBufUtils.readUTF8String(buffer);
        objIdent = ByteBufUtils.readUTF8String(buffer);
        int length = buffer.readInt();
        meta = new ArrayList<>();
        for(int i = 0; i < length; i++)
        {
            meta.add(ByteBufUtils.readUTF8String(buffer));
        }
        isProj = buffer.readBoolean();
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
            Tabula.proxy.tickHandlerClient.mainframe.setProjectMetadata(projIdent, objIdent, meta, isProj);
        }
    }
}
