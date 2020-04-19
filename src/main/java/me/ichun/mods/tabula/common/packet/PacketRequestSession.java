package me.ichun.mods.tabula.common.packet;

import me.ichun.mods.ichunutil.common.network.AbstractPacket;
import me.ichun.mods.tabula.client.gui.WorkspaceTabula;
import me.ichun.mods.tabula.common.Tabula;
import me.ichun.mods.tabula.common.tileentity.TileEntityTabulaRasa;
import net.minecraft.block.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.network.NetworkEvent;

public class PacketRequestSession extends AbstractPacket
{
    public BlockPos pos;
    public String host;

    public PacketRequestSession(){}

    public PacketRequestSession(BlockPos pos, String host)
    {
        this.pos = pos;
        this.host = host;
    }

    @Override
    public void writeTo(PacketBuffer buf)
    {
        buf.writeBlockPos(pos);
        buf.writeString(host);
    }

    @Override
    public void readFrom(PacketBuffer buf)
    {
        pos = buf.readBlockPos();
        host = buf.readString(32767);
    }

    @Override
    public void process(NetworkEvent.Context context)
    {
        context.enqueueWork(() -> {
            if(context.getDirection().getReceptionSide().isServer()) // server getting a request
            {
                ServerPlayerEntity player = context.getSender();
                World world = player.world;
                TileEntity tileEntity = world.getTileEntity(pos);
                if(tileEntity instanceof TileEntityTabulaRasa)
                {
                    TileEntityTabulaRasa tabulaRasa = (TileEntityTabulaRasa)tileEntity;
                    tabulaRasa.listeners.add(player.getName().getUnformattedComponentText());
                    if(tabulaRasa.host.isEmpty())
                    {
                        tabulaRasa.host = player.getName().getUnformattedComponentText(); //you are now the host!
                        tabulaRasa.projectString = null;
                        tabulaRasa.projectImage = null;

                        BlockState state = world.getBlockState(pos);
                        world.notifyBlockUpdate(pos, state, state, 3);
                    }
                    else
                    {
                        ServerPlayerEntity host = tabulaRasa.getHost();
                        if(host != null)
                        {
                            Tabula.channel.sendTo(new PacketListenerChange(player.getName().getUnformattedComponentText(), true), host);
                        }
                    }

                    Tabula.channel.sendTo(new PacketRequestSession(pos, tabulaRasa.host), context.getSender());
                }
            }
            else //client receiving a reply
            {
                beginClientSession();
            }
        });
    }

    @OnlyIn(Dist.CLIENT)
    public void beginClientSession()
    {
        WorkspaceTabula workspace = WorkspaceTabula.create(host);
        workspace.mainframe.setOrigin(pos);
        Minecraft.getInstance().displayGuiScreen(workspace);
    }
}
