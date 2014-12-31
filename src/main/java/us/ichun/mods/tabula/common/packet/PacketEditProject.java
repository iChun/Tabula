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
import us.ichun.mods.tabula.common.Tabula;

public class PacketEditProject extends AbstractPacket
{
    public String host;
    public String projIdent;
    public String projName;
    public String authName;
    public int txWidth;
    public int txHeight;
    public double scaleX;
    public double scaleY;
    public double scaleZ;

    public PacketEditProject(){}

    public PacketEditProject(String host, String projIdent, String projName, String authName, int txWidth, int txHeight, double scaleX, double scaleY, double scaleZ)
    {
        this.host = host;
        this.projIdent = projIdent;
        this.projName = projName;
        this.authName = authName;
        this.txWidth = txWidth;
        this.txHeight = txHeight;
        this.scaleX = scaleX;
        this.scaleY = scaleY;
        this.scaleZ = scaleZ;
    }

    @Override
    public void writeTo(ByteBuf buffer, Side side)
    {
        ByteBufUtils.writeUTF8String(buffer, host);
        ByteBufUtils.writeUTF8String(buffer, projIdent);
        ByteBufUtils.writeUTF8String(buffer, projName);
        ByteBufUtils.writeUTF8String(buffer, authName);
        buffer.writeInt(txWidth);
        buffer.writeInt(txHeight);
        buffer.writeDouble(scaleX);
        buffer.writeDouble(scaleY);
        buffer.writeDouble(scaleZ);
    }

    @Override
    public void readFrom(ByteBuf buffer, Side side)
    {
        host = ByteBufUtils.readUTF8String(buffer);
        projIdent = ByteBufUtils.readUTF8String(buffer);
        projName = ByteBufUtils.readUTF8String(buffer);
        authName = ByteBufUtils.readUTF8String(buffer);
        txWidth = buffer.readInt();
        txHeight = buffer.readInt();
        scaleX = buffer.readDouble();
        scaleY = buffer.readDouble();
        scaleZ = buffer.readDouble();
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
            Tabula.proxy.tickHandlerClient.mainframe.editProject(projIdent, projName, authName, txWidth, txHeight, scaleX, scaleY, scaleZ);
        }
    }
}
