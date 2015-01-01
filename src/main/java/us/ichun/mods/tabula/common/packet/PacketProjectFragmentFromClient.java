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
import net.minecraft.tileentity.TileEntity;
import us.ichun.mods.tabula.client.mainframe.core.ProjectHelper;
import us.ichun.mods.tabula.common.Tabula;
import us.ichun.mods.tabula.common.tileentity.TileEntityTabulaRasa;

public class PacketProjectFragmentFromClient extends AbstractPacket
{
    public String host;
    public String projectIdentifier;
    public int projSize;
    public byte packetTotal;
    public byte packetNumber;
    public int fileSize;
    public byte[] data;

    public PacketProjectFragmentFromClient(){}

    public PacketProjectFragmentFromClient(String hoster, String name, int projectSize, int pktTotal, int pktNum, int fSize, byte[] dataArray)
    {
        host = hoster;
        projectIdentifier = name;
        projSize = projectSize;
        packetTotal = (byte)pktTotal;
        packetNumber = (byte)pktNum;
        fileSize = fSize;
        data = dataArray;
    }

    @Override
    public void writeTo(ByteBuf buffer, Side side)
    {
        ByteBufUtils.writeUTF8String(buffer, host);
        ByteBufUtils.writeUTF8String(buffer, projectIdentifier);
        buffer.writeInt(projSize);
        buffer.writeByte(packetTotal);
        buffer.writeByte(packetNumber);
        buffer.writeInt(fileSize);
        buffer.writeBytes(data);
    }

    @Override
    public void readFrom(ByteBuf buffer, Side side)
    {
        host = ByteBufUtils.readUTF8String(buffer);
        projectIdentifier = ByteBufUtils.readUTF8String(buffer);
        projSize = buffer.readInt();
        packetTotal = buffer.readByte();
        packetNumber = buffer.readByte();
        fileSize = buffer.readInt();

        data = new byte[fileSize];

        buffer.readBytes(data);
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
            Tabula.proxy.tickHandlerClient.mainframe.receiveProjectData(projectIdentifier, projSize, packetTotal, packetNumber, data);
        }
    }
}
