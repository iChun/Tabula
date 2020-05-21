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

public class PacketChat extends AbstractPacket
{
    public BlockPos pos;
    public String chat;

    public PacketChat(){}

    public PacketChat(BlockPos pos, String chat)
    {
        this.pos = pos;
        this.chat = chat;
    }

    @Override
    public void writeTo(PacketBuffer buf)
    {
        buf.writeBlockPos(pos);
        buf.writeString(chat);
    }

    @Override
    public void readFrom(PacketBuffer buf)
    {
        pos = buf.readBlockPos();
        chat = readString(buf);
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
                    world.getPlayers().stream().filter(player1 -> tabulaRasa.listeners.contains(player1.getName().getUnformattedComponentText())).forEach(player1 -> Tabula.channel.sendTo(new PacketChat(this.pos, this.chat), (ServerPlayerEntity)player1));
                }
            }
            else
            {
                chat();
            }
        });
    }

    @OnlyIn(Dist.CLIENT)
    public void chat()
    {
        Screen screen = Minecraft.getInstance().currentScreen;
        if(screen instanceof WorkspaceTabula)
        {
            WorkspaceTabula workspace = (WorkspaceTabula)screen;
            workspace.mainframe.receiveChat(chat, false);
        }
    }
}
