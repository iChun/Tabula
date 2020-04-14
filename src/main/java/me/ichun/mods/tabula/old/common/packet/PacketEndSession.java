package me.ichun.mods.tabula.old.common.packet;

import io.netty.buffer.ByteBuf;
import me.ichun.mods.ichunutil.common.core.network.AbstractPacket;
import me.ichun.mods.tabula.old.client.gui.GuiWorkspace;
import me.ichun.mods.tabula.old.client.mainframe.core.ProjectHelper;
import me.ichun.mods.tabula.old.common.tileentity.TileEntityTabulaRasa;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.translation.I18n;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

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
    public void writeTo(ByteBuf buffer)
    {
        ByteBufUtils.writeUTF8String(buffer, host);
        buffer.writeInt(x);
        buffer.writeInt(y);
        buffer.writeInt(z);
        buffer.writeBoolean(crashed);
    }

    @Override
    public void readFrom(ByteBuf buffer)
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
            TileEntity te = player.world.getTileEntity(new BlockPos(x, y, z));
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

    @Override
    public Side receivingSide()
    {
        return null;
    }

    @SideOnly(Side.CLIENT)
    public void handleClient()
    {
        ProjectHelper.addSystemMessage(I18n.translateToLocalFormatted(crashed ? "system.cannotReachHost" : "system.sessionEnded", host));
        Minecraft mc = Minecraft.getMinecraft();
        if(mc.currentScreen instanceof GuiWorkspace && ((GuiWorkspace)mc.currentScreen).host.equals(host))
        {
            ((GuiWorkspace)mc.currentScreen).sessionEnded = true;
        }
    }
}
