package us.ichun.mods.tabula.common.packet;

import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockPos;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.relauncher.Side;
import me.ichun.mods.ichunutil.common.core.network.AbstractPacket;
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
    public boolean updateDims;
    public boolean isCurrentProject;
    public byte packetTotal;
    public byte packetNumber;
    public int fileSize;
    public byte[] data;

    public PacketProjectFragment(){}

    public PacketProjectFragment(int i, int j, int k, boolean toHoster, String hoster, String listen, String name, boolean isTex, boolean dims, boolean isCur, int pktTotal, int pktNum, int fSize, byte[] dataArray)
    {
        x = i;
        y = j;
        z = k;
        toHost = toHoster;
        host = hoster;
        listener = listen;
        projectIdentifier = name;
        isTexture = isTex;
        updateDims = dims;
        isCurrentProject = isCur;
        packetTotal = (byte)pktTotal;
        packetNumber = (byte)pktNum;
        fileSize = fSize;
        data = dataArray;
    }

    @Override
    public void writeTo(ByteBuf buffer)
    {
        buffer.writeInt(x);
        buffer.writeInt(y);
        buffer.writeInt(z);
        buffer.writeBoolean(toHost);
        ByteBufUtils.writeUTF8String(buffer, host);
        ByteBufUtils.writeUTF8String(buffer, listener);
        ByteBufUtils.writeUTF8String(buffer, projectIdentifier);
        buffer.writeBoolean(isTexture);
        buffer.writeBoolean(updateDims);
        buffer.writeBoolean(isCurrentProject);
        buffer.writeByte(packetTotal);
        buffer.writeByte(packetNumber);
        buffer.writeInt(fileSize);
        buffer.writeBytes(data);
    }

    @Override
    public void readFrom(ByteBuf buffer)
    {
        x = buffer.readInt();
        y = buffer.readInt();
        z = buffer.readInt();
        toHost = buffer.readBoolean();
        host = ByteBufUtils.readUTF8String(buffer);
        listener = ByteBufUtils.readUTF8String(buffer);
        projectIdentifier = ByteBufUtils.readUTF8String(buffer);
        isTexture = buffer.readBoolean();
        updateDims = buffer.readBoolean();
        isCurrentProject = buffer.readBoolean();
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
            if(!toHost)
            {
                boolean changed = ProjectHelper.receiveProjectData(false, projectIdentifier, isTexture, updateDims, packetTotal, packetNumber, data);

                EntityPlayerMP listening = FMLCommonHandler.instance().getMinecraftServerInstance().getConfigurationManager().getPlayerByUsername(listener);
                if(listening != null)
                {
                    Tabula.channel.sendToPlayer(this, listening);
                }

                if(x != -1 && y != -1 && z != -1 && isCurrentProject && changed)
                {
                    TileEntity te = player.worldObj.getTileEntity(new BlockPos(x, y, z));
                    if(te instanceof TileEntityTabulaRasa)
                    {
                        TileEntityTabulaRasa tr = (TileEntityTabulaRasa)te;
                        tr.currentProj = projectIdentifier;
                        if(isTexture)
                        {
                            tr.needTextureUpdate = true;
                        }
                        else
                        {
                            tr.needProjectUpdate = true;
                        }
                        tr.updateTimeout = 3;
                        tr.getWorld().markBlockForUpdate(tr.getPos());
                    }
                }

            }
            else
            {
                EntityPlayerMP hoster = FMLCommonHandler.instance().getMinecraftServerInstance().getConfigurationManager().getPlayerByUsername(host);
                if(hoster != null)
                {
                    Tabula.channel.sendToPlayer(this, hoster);
                }
            }
        }
        else
        {
            ProjectHelper.receiveProjectData(!host.equals(listener), projectIdentifier, isTexture, updateDims, packetTotal, packetNumber, data);
        }
    }
}
