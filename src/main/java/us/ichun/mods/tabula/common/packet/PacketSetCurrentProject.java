package us.ichun.mods.tabula.common.packet;

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
import us.ichun.mods.tabula.common.tileentity.TileEntityTabulaRasa;

public class PacketSetCurrentProject extends AbstractPacket
{

    public String host;
    public int x;
    public int y;
    public int z;
    public String ident;

    public PacketSetCurrentProject(){}

    public PacketSetCurrentProject(String name, int i, int j, int k, String identt)
    {
        host = name;
        x = i;
        y = j;
        z = k;
        ident = identt;
    }

    @Override
    public void writeTo(ByteBuf buffer, Side side)
    {
        ByteBufUtils.writeUTF8String(buffer, host);
        buffer.writeInt(x);
        buffer.writeInt(y);
        buffer.writeInt(z);
        ByteBufUtils.writeUTF8String(buffer, ident);
    }

    @Override
    public void readFrom(ByteBuf buffer, Side side)
    {
        host = ByteBufUtils.readUTF8String(buffer);
        x = buffer.readInt();
        y = buffer.readInt();
        z = buffer.readInt();
        ident = ByteBufUtils.readUTF8String(buffer);
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
                if(tr.host.equals(player.getCommandSenderName()))
                {
                    tr.currentProj = ident;
                    tr.needProjectUpdate = true;
                    tr.needTextureUpdate = true;
                    tr.getWorldObj().markBlockForUpdate(tr.xCoord, tr.yCoord, tr.zCoord);
                }
            }
        }
    }
}
