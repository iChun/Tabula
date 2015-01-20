package us.ichun.mods.tabula.common.packet;

import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockPos;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import us.ichun.mods.ichunutil.common.core.network.AbstractPacket;
import us.ichun.mods.tabula.client.gui.GuiWorkspace;
import us.ichun.mods.tabula.common.Tabula;
import us.ichun.mods.tabula.common.tileentity.TileEntityTabulaRasa;

public class PacketPingAlive extends AbstractPacket
{

    public String host;
    public int x;
    public int y;
    public int z;

    public PacketPingAlive(){}

    public PacketPingAlive(String name, int i, int j, int k)
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
        if(side.isServer())
        {
            TileEntity te = player.worldObj.getTileEntity(new BlockPos(x, y, z));
            if(te instanceof TileEntityTabulaRasa)
            {
                ((TileEntityTabulaRasa)te).pingTime = 0;
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
        Minecraft mc = Minecraft.getMinecraft();
        if(host.equals(mc.getSession().getUsername()) && mc.currentScreen instanceof GuiWorkspace)
        {
            GuiWorkspace workspace = (GuiWorkspace)mc.currentScreen;
            if(((GuiWorkspace)workspace).hostX == x && ((GuiWorkspace)workspace).hostY == y && ((GuiWorkspace)workspace).hostZ == z)
            {
                Tabula.channel.sendToServer(this);
            }
        }
    }
}
