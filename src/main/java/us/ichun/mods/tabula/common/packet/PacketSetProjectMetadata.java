package us.ichun.mods.tabula.common.packet;

import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import us.ichun.mods.ichunutil.common.core.network.AbstractPacket;
import us.ichun.mods.tabula.client.mainframe.Mainframe;
import us.ichun.mods.tabula.common.Tabula;

import java.util.ArrayList;

public class PacketSetProjectMetadata extends AbstractPacket
{
    public String host;
    public String ident;
    public ArrayList<String> meta;

    public PacketSetProjectMetadata(){}

    public PacketSetProjectMetadata(String host, String ident, ArrayList<String> meta)
    {
        this.host = host;
        this.ident = ident;
        this.meta = meta;
    }

    @Override
    public void writeTo(ByteBuf buffer, Side side)
    {
        ByteBufUtils.writeUTF8String(buffer, host);
        ByteBufUtils.writeUTF8String(buffer, ident);
        buffer.writeInt(meta.size());
        for(int i = 0; i < meta.size(); i++)
        {
            ByteBufUtils.writeUTF8String(buffer, meta.get(i));
        }
    }

    @Override
    public void readFrom(ByteBuf buffer, Side side)
    {
        host = ByteBufUtils.readUTF8String(buffer);
        ident = ByteBufUtils.readUTF8String(buffer);
        int length = buffer.readInt();
        meta = new ArrayList<String>();
        for(int i = 0; i < length; i++)
        {
            meta.add(ByteBufUtils.readUTF8String(buffer));
        }
    }

    @Override
    public void execute(Side side, EntityPlayer player)
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
            Tabula.proxy.tickHandlerClient.mainframe.setProjectMetadata(ident, meta);
        }
    }
}
