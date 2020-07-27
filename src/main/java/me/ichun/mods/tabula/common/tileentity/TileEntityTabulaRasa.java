package me.ichun.mods.tabula.common.tileentity;

import me.ichun.mods.ichunutil.common.module.tabula.project.Project;
import me.ichun.mods.tabula.common.Tabula;
import me.ichun.mods.tabula.common.packet.PacketKillSession;
import me.ichun.mods.tabula.common.packet.PacketPing;
import me.ichun.mods.tabula.common.packet.PacketRequestProject;
import net.minecraft.block.BlockState;
import net.minecraft.client.renderer.texture.NativeImage;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SUpdateTileEntityPacket;
import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.HashSet;

public class TileEntityTabulaRasa extends TileEntity
        implements ITickableTileEntity
{
    public Direction facing;
    public String host;

    public HashSet<String> listeners = new HashSet<>();

    public int lastPing;

    public String projectString;
    public byte[] projectImage;

    @OnlyIn(Dist.CLIENT)
    public Project project;
    public int projectReq;

    public TileEntityTabulaRasa()
    {
        super(Tabula.TileEntityTypes.TABULA_RASA.get());
        facing = Direction.UP; //invalid
        host = "";
    }

    @Override
    public void tick()
    {
        if(!world.isRemote)
        {
            if(!host.isEmpty())
            {
                lastPing++;
                if(lastPing == 300) //15 seconds
                {
                    world.getPlayers().stream().filter(player -> listeners.contains(player.getName().getUnformattedComponentText())).forEach(player -> Tabula.channel.sendTo(new PacketPing(this.pos), (ServerPlayerEntity)player));
                }
                else if(lastPing == 600) //30 seconds
                {
                    killSession();
                }
            }
        }
        else
        {
            if(host != null && !host.isEmpty())
            {
                projectReq--;
            }
            else
            {
                if(project != null)
                {
                    project.destroy();
                }
                project = null;
                projectReq = 0;
            }
        }
    }

    public void requestProject()
    {
        Tabula.channel.sendToServer(new PacketRequestProject(pos));
    }

    public void killSession()
    {
        world.getPlayers().stream().filter(player -> listeners.contains(player.getName().getUnformattedComponentText())).forEach(player -> Tabula.channel.sendTo(new PacketKillSession(this.pos), (ServerPlayerEntity)player));

        host = "";
        listeners.clear();
        lastPing = 0;
        projectString = null;
        projectImage = null;

        BlockState state = world.getBlockState(pos);
        world.notifyBlockUpdate(pos, state, state, 3);
    }

    public ServerPlayerEntity getHost()
    {
        if(!host.isEmpty() && !world.isRemote)
        {
            return (ServerPlayerEntity)world.getPlayers().stream().filter(player -> player.getName().getUnformattedComponentText().equals(host)).findAny().get();
        }
        return null;
    }

    @Override
    public void onDataPacket(NetworkManager net, SUpdateTileEntityPacket pkt)
    {
        BlockState state = world.getBlockState(getPos());

        read(state, pkt.getNbtCompound());

        world.notifyBlockUpdate(getPos(), state, state, 3);
    }

    @Override
    public CompoundNBT getUpdateTag()
    {
        return this.write(new CompoundNBT());
    }

    @Override
    public SUpdateTileEntityPacket getUpdatePacket()
    {
        return new SUpdateTileEntityPacket(getPos(), 0, getUpdateTag());
    }

    @Override
    public CompoundNBT write(CompoundNBT tag)
    {
        super.write(tag);
        tag.putByte("facing", (byte)facing.getHorizontalIndex());
        tag.putString("host", host);
        return tag;
    }

    @Override
    public void read(BlockState state, CompoundNBT tag)
    {
        super.read(state, tag);
        facing = Direction.byHorizontalIndex(tag.getByte("facing"));
        host = tag.getString("host");
    }
}
