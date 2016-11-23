package us.ichun.mods.tabula.common.packet;

import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import me.ichun.mods.ichunutil.common.core.network.AbstractPacket;
import us.ichun.mods.tabula.common.Tabula;

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
        for(int i = 0; i < meta.size(); i++)
        {
            ByteBufUtils.writeUTF8String(buffer, meta.get(i));
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
        meta = new ArrayList<String>();
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
            EntityPlayerMP hoster = FMLCommonHandler.instance().getMinecraftServerInstance().getConfigurationManager().getPlayerByUsername(host);
            if(hoster != null)
            {
                Tabula.channel.sendToPlayer(this, hoster);
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
            Tabula.proxy.tickHandlerClient.mainframe.setProjectMetadata(projIdent, objIdent, meta, isProj);
        }
    }
}
