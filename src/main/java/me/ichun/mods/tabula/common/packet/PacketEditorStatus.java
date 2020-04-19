package me.ichun.mods.tabula.common.packet;

import me.ichun.mods.ichunutil.common.network.AbstractPacket;
import me.ichun.mods.tabula.client.gui.WorkspaceTabula;
import me.ichun.mods.tabula.common.Tabula;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.List;

public class PacketEditorStatus extends AbstractPacket
{
    public String editor;
    public boolean status;

    public PacketEditorStatus(){}

    public PacketEditorStatus(String editor, boolean status)
    {
        this.editor = editor;
        this.status = status;
    }

    @Override
    public void writeTo(PacketBuffer buf)
    {
        buf.writeString(editor);
        buf.writeBoolean(status);
    }

    @Override
    public void readFrom(PacketBuffer buf)
    {
        editor = buf.readString(32767);
        status = buf.readBoolean();
    }

    @Override
    public void process(NetworkEvent.Context context)
    {
        context.enqueueWork(() -> {
            if(context.getDirection().getReceptionSide().isServer()) // server
            {
                ServerPlayerEntity player = context.getSender();
                World world = player.world;
                List<? extends PlayerEntity> players = world.getPlayers();
                for(PlayerEntity playerEntity : players)
                {
                    if(playerEntity.getName().getUnformattedComponentText().equalsIgnoreCase(editor))
                    {
                        Tabula.channel.sendTo(new PacketEditorStatus(editor, status), (ServerPlayerEntity)playerEntity);
                        break;
                    }
                }
            }
            else
            {
                handleClient();
            }
        });
    }

    @OnlyIn(Dist.CLIENT)
    public void handleClient()
    {
        Screen screen = Minecraft.getInstance().currentScreen;
        if(screen instanceof WorkspaceTabula)
        {
            WorkspaceTabula workspace = (WorkspaceTabula)screen;

            workspace.mainframe.setCanEdit(status);
        }
    }
}
