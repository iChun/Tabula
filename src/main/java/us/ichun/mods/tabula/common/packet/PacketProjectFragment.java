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

public class PacketProjectFragment extends AbstractPacket
{
    public int x;
    public int y;
    public int z;
    public boolean toHost;
    public String host;
    public String listener;
    public String projectIdentifier;
    public boolean isTexture;
    public byte packetTotal;
    public byte packetNumber;
    public int fileSize;
    public byte[] data;

    public PacketProjectFragment(){}

    public PacketProjectFragment(int i, int j, int k, boolean toHoster, String hoster, String listen, String name, boolean isTex, int pktTotal, int pktNum, int fSize, byte[] dataArray)
    {
        x = i;
        y = j;
        z = k;
        toHost = toHoster;
        host = hoster;
        listener = listen;
        projectIdentifier = name;
        isTexture = isTex;
        packetTotal = (byte)pktTotal;
        packetNumber = (byte)pktNum;
        fileSize = fSize;
        data = dataArray;
    }

    @Override
    public void writeTo(ByteBuf buffer, Side side)
    {
        buffer.writeInt(x);
        buffer.writeInt(y);
        buffer.writeInt(z);
        buffer.writeBoolean(toHost);
        ByteBufUtils.writeUTF8String(buffer, host);
        ByteBufUtils.writeUTF8String(buffer, listener);
        ByteBufUtils.writeUTF8String(buffer, projectIdentifier);
        buffer.writeBoolean(isTexture);
        buffer.writeByte(packetTotal);
        buffer.writeByte(packetNumber);
        buffer.writeInt(fileSize);
        buffer.writeBytes(data);
    }

    @Override
    public void readFrom(ByteBuf buffer, Side side)
    {
        x = buffer.readInt();
        y = buffer.readInt();
        z = buffer.readInt();
        toHost = buffer.readBoolean();
        host = ByteBufUtils.readUTF8String(buffer);
        listener = ByteBufUtils.readUTF8String(buffer);
        projectIdentifier = ByteBufUtils.readUTF8String(buffer);
        isTexture = buffer.readBoolean();
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
            if(!toHost)
            {
//                if(x != -1 && y != -1 && z != -1)
                //                {
                //                    TileEntity te = player.worldObj.getTileEntity(x, y, z);
                //                    if(te instanceof TileEntityTabulaRasa)
                //                    {
                //                        TileEntityTabulaRasa tr = (TileEntityTabulaRasa)te;
                //                        //TODO something...? should i mark that the TE needs updating?
                //                    }
                //                }
                ProjectHelper.receiveProjectData(projectIdentifier, isTexture, packetTotal, packetNumber, data);

                EntityPlayerMP listening = FMLCommonHandler.instance().getMinecraftServerInstance().getConfigurationManager().func_152612_a(listener);
                if(listening != null)
                {
                    PacketHandler.sendToPlayer(Tabula.channels, this, listening);
                }
            }
            else
            {
                EntityPlayerMP hoster = FMLCommonHandler.instance().getMinecraftServerInstance().getConfigurationManager().func_152612_a(host);
                if(hoster != null)
                {
                    PacketHandler.sendToPlayer(Tabula.channels, this, hoster);
                }
            }
        }
        else
        {
            ProjectHelper.receiveProjectData(projectIdentifier, isTexture, packetTotal, packetNumber, data);
        }
    }
}
