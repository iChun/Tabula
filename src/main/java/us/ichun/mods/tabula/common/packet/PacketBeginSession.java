package us.ichun.mods.tabula.common.packet;

import cpw.mods.fml.client.FMLClientHandler;
import cpw.mods.fml.common.network.ByteBufUtils;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import ichun.common.core.network.AbstractPacket;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import us.ichun.mods.tabula.client.gui.GuiWorkspace;
import us.ichun.mods.tabula.common.Tabula;

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
    public void writeTo(ByteBuf buffer, Side side)
    {
        ByteBufUtils.writeUTF8String(buffer, host);
        buffer.writeInt(x);
        buffer.writeInt(y);
        buffer.writeInt(z);
    }

    @Override
    public void readFrom(ByteBuf buffer, Side side)
    {
        host = ByteBufUtils.readUTF8String(buffer);
        x = buffer.readInt();
        y = buffer.readInt();
        z = buffer.readInt();
    }

    @Override
    public void execute(Side side, EntityPlayer player)
    {
        handleClient();
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
