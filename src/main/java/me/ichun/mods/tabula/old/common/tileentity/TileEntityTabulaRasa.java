package me.ichun.mods.tabula.old.common.tileentity;

import me.ichun.mods.tabula.old.client.gui.GuiWorkspace;
import me.ichun.mods.tabula.old.common.Tabula;
import me.ichun.mods.tabula.old.common.packet.PacketEndSession;
import me.ichun.mods.tabula.old.common.packet.PacketPingAlive;
import me.ichun.mods.tabula.old.common.packet.PacketRequestProject;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.ArrayList;

public class TileEntityTabulaRasa extends TileEntity
        implements ITickable
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
        listeners = new ArrayList<>();
        currentProj = "";
    }

    @Override
    public void update()
    {
        age++;
        if(!host.isEmpty())
        {
            if(!world.isRemote)
            {
                pingTime++;
                if(pingTime > 150 || FMLCommonHandler.instance().getMinecraftServerInstance().getPlayerList().getPlayerByUsername(host) == null)
                {
                    terminateSession(true);
                }
                else if(pingTime == 25)
                {
                    //send ping packet
                    Tabula.channel.sendTo(new PacketPingAlive(host, pos.getX(), pos.getY(), pos.getZ()), FMLCommonHandler.instance().getMinecraftServerInstance().getPlayerList().getPlayerByUsername(host));
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
            EntityPlayerMP player = FMLCommonHandler.instance().getMinecraftServerInstance().getPlayerList().getPlayerByUsername(listener);
            if(player != null)
            {
                Tabula.channel.sendTo(new PacketEndSession(host, getPos().getX(), getPos().getY(), getPos().getZ(), crashed), player);
            }
        }

        pingTime = 0;
        host = "";
        currentProj = "";
        listeners.clear();

        IBlockState state = world.getBlockState(getPos());
        world.notifyBlockUpdate(getPos(), state, state, 3);
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void onDataPacket(NetworkManager net, SPacketUpdateTileEntity pkt)
    {
        readFromNBT(pkt.getNbtCompound());

        IBlockState state = world.getBlockState(getPos());
        world.notifyBlockUpdate(getPos(), state, state, 3);
    }

    @Override
    public NBTTagCompound getUpdateTag()
    {
        return this.writeToNBT(new NBTTagCompound());
    }

    @Override
    public SPacketUpdateTileEntity getUpdatePacket()
    {
        return new SPacketUpdateTileEntity(getPos(), 0, getUpdateTag());
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound tag)
    {
        super.writeToNBT(tag);
        tag.setInteger("side", side);
        tag.setString("host", host);
        tag.setString("currentProj", currentProj);
        tag.setBoolean("needProjectUpdate", needProjectUpdate);
        tag.setBoolean("needTextureUpdate", needTextureUpdate);
        return tag;
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
