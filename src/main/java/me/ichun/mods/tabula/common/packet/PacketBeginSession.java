package me.ichun.mods.tabula.common.packet;

import io.netty.buffer.ByteBuf;
import me.ichun.mods.ichunutil.common.core.network.AbstractPacket;
import me.ichun.mods.tabula.client.gui.GuiWorkspace;
import me.ichun.mods.tabula.common.Tabula;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.fml.client.FMLClientHandler;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class PacketBeginSession extends AbstractPacket
{
    public String host;
    public int x;
    public int y;
    public int z;

    public PacketBeginSession(){}

    public PacketBeginSession(String name, int i, int j, int k)
    {
        host = name;
        x = i;
        y = j;
        z = k;
    }

    @Override
    public void writeTo(ByteBuf buffer)
    {
        ByteBufUtils.writeUTF8String(buffer, host);
        buffer.writeInt(x);
        buffer.writeInt(y);
        buffer.writeInt(z);
    }

    @Override
    public void readFrom(ByteBuf buffer)
    {
        host = ByteBufUtils.readUTF8String(buffer);
        x = buffer.readInt();
        y = buffer.readInt();
        z = buffer.readInt();
    }

    @Override
    public AbstractPacket execute(Side side, EntityPlayer player)
    {
        handleClient();
        return null;
    }

    @Override
    public Side receivingSide()
    {
        return Side.CLIENT;
    }

    @SideOnly(Side.CLIENT)
    public void handleClient()
    {
        Minecraft mc = Minecraft.getMinecraft();
        if(host.equals(mc.getSession().getUsername()))
        {
            Tabula.proxy.tickHandlerClient.initializeMainframe(host, x, y, z);
        }
        else
        {
            int oriScale = mc.gameSettings.guiScale;
            mc.gameSettings.guiScale = mc.gameSettings.guiScale == 1 ? 1 : 2;
            FMLClientHandler.instance().showGuiScreen(new GuiWorkspace(oriScale, true, false, host, x, y, z));
        }
    }
}
