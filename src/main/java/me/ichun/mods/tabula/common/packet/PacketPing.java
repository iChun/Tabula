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

public class PacketPing extends AbstractPacket
{
    public BlockPos pos;

    public PacketPing(){}

    public PacketPing(BlockPos pos)
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
            if(context.getDirection().getReceptionSide().isServer()) // server getting a pong from the host
            {
                ServerPlayerEntity player = context.getSender();
                World world = player.world;
                TileEntity tileEntity = world.getTileEntity(pos);
                if(tileEntity instanceof TileEntityTabulaRasa)
                {
                    TileEntityTabulaRasa tabulaRasa = (TileEntityTabulaRasa)tileEntity;
                    tabulaRasa.lastPing = 0;
                }
            }
            else
            {
                pong();
            }
        });
    }

    @OnlyIn(Dist.CLIENT)
    public void pong()
    {
        Screen screen = Minecraft.getInstance().currentScreen;
        if(screen instanceof WorkspaceTabula)
        {
            WorkspaceTabula workspace = (WorkspaceTabula)screen;
            workspace.mainframe.lastPing = 0;

            if(workspace.mainframe.getIsMaster()) // we are the master. pong.
            {
                Tabula.channel.sendToServer(new PacketPing(pos));
            }
        }
    }
}
