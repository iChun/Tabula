package us.ichun.mods.tabula.common.tileentity;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.S35PacketUpdateTileEntity;
import net.minecraft.server.gui.IUpdatePlayerListBox;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import us.ichun.mods.tabula.client.gui.GuiWorkspace;
import us.ichun.mods.tabula.common.Tabula;
import us.ichun.mods.tabula.common.packet.PacketEndSession;
import us.ichun.mods.tabula.common.packet.PacketPingAlive;
import us.ichun.mods.tabula.common.packet.PacketRequestProject;

import java.util.ArrayList;

public class TileEntityTabulaRasa extends TileEntity
        implements IUpdatePlayerListBox
{
    public int side;
    public String host;
    public ArrayList<String> listeners;
    public int pingTime;

    public int updateTimeout;
    public String currentProj;
    public boolean needTextureUpdate;
    public boolean needProjectUpdate;

    public int age;

    public TileEntityTabulaRasa()
    {
        host = "";
        listeners = new ArrayList<String>();
        currentProj = "";
    }

    @Override
    public void update()
    {
        age++;
        if(!host.isEmpty())
        {
            if(!worldObj.isRemote)
            {
                pingTime++;
                if(pingTime > 150 || FMLCommonHandler.instance().getMinecraftServerInstance().getConfigurationManager().getPlayerByUsername(host) == null)
                {
                    terminateSession(true);
                }
                else if(pingTime == 25)
                {
                    //send ping packet
                    Tabula.channel.sendToPlayer(new PacketPingAlive(host, pos.getX(), pos.getY(), pos.getZ()), FMLCommonHandler.instance().getMinecraftServerInstance().getConfigurationManager().getPlayerByUsername(host));
                }

                if(updateTimeout > 0)
                {
                    updateTimeout--;
                    if(updateTimeout == 0)
                    {
                        needProjectUpdate = needTextureUpdate = false;
                    }
                }
            }
            else
            {
                clientCheck();
            }
        }
    }

    @SideOnly(Side.CLIENT)
    public void clientCheck()
    {
        Minecraft mc = Minecraft.getMinecraft();
        if(mc.currentScreen instanceof GuiWorkspace)
        {
            GuiWorkspace workspace = (GuiWorkspace)mc.currentScreen;
            if(workspace.host.equals(host))
            {
                return;
            }
        }

        if(needProjectUpdate)
        {
            needProjectUpdate = false;
            if(!currentProj.isEmpty())
            {
                Tabula.channel.sendToServer(new PacketRequestProject(host, Minecraft.getMinecraft().getSession().getUsername(), currentProj, false));
            }
        }
        if(needTextureUpdate)
        {
            needTextureUpdate = false;
            if(!currentProj.isEmpty())
            {
                Tabula.channel.sendToServer(new PacketRequestProject(host, Minecraft.getMinecraft().getSession().getUsername(), currentProj, true));
            }
        }
    }

    public void terminateSession(boolean crashed)
    {
        for(String listener : listeners)
        {
            EntityPlayerMP player = FMLCommonHandler.instance().getMinecraftServerInstance().getConfigurationManager().getPlayerByUsername(listener);
            if(player != null)
            {
                Tabula.channel.sendToPlayer(new PacketEndSession(host, getPos().getX(), getPos().getY(), getPos().getZ(), crashed), player);
            }
        }

        pingTime = 0;
        host = "";
        currentProj = "";
        listeners.clear();

        worldObj.markBlockForUpdate(getPos());
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void onDataPacket(NetworkManager net, S35PacketUpdateTileEntity pkt)
    {
        readFromNBT(pkt.getNbtCompound());

        worldObj.markBlockForUpdate(getPos());
    }

    @Override
    public Packet getDescriptionPacket()
    {
        NBTTagCompound tag = new NBTTagCompound();
        writeToNBT(tag);
        return new S35PacketUpdateTileEntity(pos, 0, tag);
    }

    @Override
    public void writeToNBT(NBTTagCompound tag)
    {
        super.writeToNBT(tag);
        tag.setInteger("side", side);
        tag.setString("host", host);
        tag.setString("currentProj", currentProj);
        tag.setBoolean("needProjectUpdate", needProjectUpdate);
        tag.setBoolean("needTextureUpdate", needTextureUpdate);
    }

    @Override
    public void readFromNBT(NBTTagCompound tag)
    {
        super.readFromNBT(tag);
        side = tag.getInteger("side");
        host = tag.getString("host");
        currentProj = tag.getString("currentProj");
        needProjectUpdate = tag.getBoolean("needProjectUpdate");
        needTextureUpdate = tag.getBoolean("needTextureUpdate");
    }

    @SideOnly(Side.CLIENT)
    @Override
    public AxisAlignedBB getRenderBoundingBox()
    {
        return new AxisAlignedBB(getPos(), getPos().add(1, 1, 1));
    }
}
