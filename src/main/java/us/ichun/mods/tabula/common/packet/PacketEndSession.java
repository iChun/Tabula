package us.ichun.mods.tabula.common.packet;

import cpw.mods.fml.client.FMLClientHandler;
import cpw.mods.fml.common.network.ByteBufUtils;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import ichun.common.core.network.AbstractPacket;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.StatCollector;
import us.ichun.mods.tabula.client.gui.GuiWorkspace;
import us.ichun.mods.tabula.client.mainframe.core.ProjectHelper;
import us.ichun.mods.tabula.common.Tabula;
import us.ichun.mods.tabula.common.tileentity.TileEntityTabulaRasa;

public class PacketEndSession extends AbstractPacket
{

    public String host;
    public int x;
    public int y;
    public int z;
    public boolean crashed;

    public PacketEndSession(){}

    public PacketEndSession(String name, int i, int j, int k, boolean crash)
    {
        host = name;
        x = i;
        y = j;
        z = k;
        crashed = crash;
    }

    @Override
    public void writeTo(ByteBuf buffer, Side side)
    {
        ByteBufUtils.writeUTF8String(buffer, host);
        buffer.writeInt(x);
        buffer.writeInt(y);
        buffer.writeInt(z);
        buffer.writeBoolean(crashed);
    }

    @Override
    public void readFrom(ByteBuf buffer, Side side)
    {
        host = ByteBufUtils.readUTF8String(buffer);
        x = buffer.readInt();
        y = buffer.readInt();
        z = buffer.readInt();
        crashed = buffer.readBoolean();
    }

    @Override
    public void execute(Side side, EntityPlayer player)
    {
        if(side.isServer())
        {
            TileEntity te = player.worldObj.getTileEntity(x, y, z);
            if(te instanceof TileEntityTabulaRasa)
            {
                TileEntityTabulaRasa tr = (TileEntityTabulaRasa)te;
                if(tr.host.equals(host))
                {
                    tr.terminateSession(false);
                }
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
        ProjectHelper.addSystemMessage(StatCollector.translateToLocalFormatted(crashed ? "system.cannotReachHost" : "system.sessionEnded", host));
        Minecraft mc = Minecraft.getMinecraft();
        if(mc.currentScreen instanceof GuiWorkspace && ((GuiWorkspace)mc.currentScreen).host.equals(host))
        {
            ((GuiWorkspace)mc.currentScreen).sessionEnded = true;
        }
    }
}
