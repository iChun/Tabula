package me.ichun.mods.tabula.common.packet;

import io.netty.buffer.ByteBuf;
import me.ichun.mods.ichunutil.common.core.network.AbstractPacket;
import me.ichun.mods.tabula.common.Tabula;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class PacketProjectFragmentFromClient extends AbstractPacket
{
    public String host;
    public String projectIdentifier;
    public boolean isImport;
    public int projSize;
    public byte packetTotal;
    public byte packetNumber;
    public int fileSize;
    public byte[] data;

    public PacketProjectFragmentFromClient(){}

    public PacketProjectFragmentFromClient(String hoster, String name, boolean isImporting, int projectSize, int pktTotal, int pktNum, int fSize, byte[] dataArray)
    {
        host = hoster;
        projectIdentifier = name;
        isImport = isImporting;
        projSize = projectSize;
        packetTotal = (byte)pktTotal;
        packetNumber = (byte)pktNum;
        fileSize = fSize;
        data = dataArray;
    }

    @Override
    public void writeTo(ByteBuf buffer)
    {
        ByteBufUtils.writeUTF8String(buffer, host);
        ByteBufUtils.writeUTF8String(buffer, projectIdentifier);
        buffer.writeBoolean(isImport);
        buffer.writeInt(projSize);
        buffer.writeByte(packetTotal);
        buffer.writeByte(packetNumber);
        buffer.writeInt(fileSize);
        buffer.writeBytes(data);
    }

    @Override
    public void readFrom(ByteBuf buffer)
    {
        host = ByteBufUtils.readUTF8String(buffer);
        projectIdentifier = ByteBufUtils.readUTF8String(buffer);
        isImport = buffer.readBoolean();
        projSize = buffer.readInt();
        packetTotal = buffer.readByte();
        packetNumber = buffer.readByte();
        fileSize = buffer.readInt();

        data = new byte[fileSize];

        buffer.readBytes(data);
    }

    @Override
    public AbstractPacket execute(Side side, EntityPlayer player)
    {
        if(side.isServer())
        {
            EntityPlayerMP hoster = FMLCommonHandler.instance().getMinecraftServerInstance().getPlayerList().getPlayerByUsername(host);
            if(hoster != null)
            {
                Tabula.channel.sendTo(this, hoster);
            }
        }
        else
        {
            handleClient();
        }
        return null;
    }

    @Override
    public Side receivingSide()
    {
        return null;
    }

    @SideOnly(Side.CLIENT)
    public void handleClient()
    {
        if(Tabula.proxy.tickHandlerClient.mainframe != null && Minecraft.getMinecraft().getSession().getUsername().equals(host))
        {
            Tabula.proxy.tickHandlerClient.mainframe.receiveProjectData(projectIdentifier, isImport, projSize, packetTotal, packetNumber, data);
        }
    }
}
