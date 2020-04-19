package me.ichun.mods.tabula.common.packet;

import me.ichun.mods.ichunutil.common.network.AbstractPacket;
import me.ichun.mods.tabula.client.gui.WorkspaceTabula;
import me.ichun.mods.tabula.common.Tabula;
import me.ichun.mods.tabula.common.tileentity.TileEntityTabulaRasa;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.network.NetworkEvent;

public class PacketKillSession extends AbstractPacket
{
    public BlockPos pos;

    public PacketKillSession(){}

    public PacketKillSession(BlockPos pos)
    {
        this.pos = pos;
    }

    @Override
    public void writeTo(PacketBuffer buf)
    {
        buf.writeBlockPos(pos);
    }

    @Override
    public void readFrom(PacketBuffer buf)
    {
        pos = buf.readBlockPos();
    }

    @Override
    public void process(NetworkEvent.Context context)
    {
        context.enqueueWork(() -> {
            if(context.getDirection().getReceptionSide().isServer()) // server
            {
                ServerPlayerEntity player = context.getSender();
                World world = player.world;
                TileEntity tileEntity = world.getTileEntity(pos);
                if(tileEntity instanceof TileEntityTabulaRasa)
                {
                    TileEntityTabulaRasa tabulaRasa = (TileEntityTabulaRasa)tileEntity;
                    if(tabulaRasa.host.equals(player.getName().getUnformattedComponentText())) //we got a kill session order from the host.
                    {
                        tabulaRasa.listeners.remove(player.getName().getUnformattedComponentText()); // remove from listeners first
                        tabulaRasa.killSession();
                    }
                    else
                    {
                        ServerPlayerEntity host = tabulaRasa.getHost();
                        if(host != null)
                        {
                            Tabula.channel.sendTo(new PacketListenerChange(player.getName().getUnformattedComponentText(), false), host);
                        }
                    }
                }
            }
            else
            {
                killSession();
            }
        });
    }

    @OnlyIn(Dist.CLIENT)
    public void killSession()
    {
        Screen screen = Minecraft.getInstance().currentScreen;
        if(screen instanceof WorkspaceTabula)
        {
            WorkspaceTabula workspace = (WorkspaceTabula)screen;
            workspace.mainframe.sessionEnded = true;
        }
    }
}
