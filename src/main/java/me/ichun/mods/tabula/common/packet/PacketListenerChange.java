package me.ichun.mods.tabula.common.packet;

import me.ichun.mods.ichunutil.common.network.AbstractPacket;
import me.ichun.mods.tabula.client.gui.WorkspaceTabula;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.network.NetworkEvent;

public class PacketListenerChange extends AbstractPacket
{
    public String listener;
    public boolean add;

    public PacketListenerChange(){}

    public PacketListenerChange(String listener, boolean add)
    {
        this.listener = listener;
        this.add = add;
    }

    @Override
    public void writeTo(PacketBuffer buf)
    {
        buf.writeString(listener);
        buf.writeBoolean(add);
    }

    @Override
    public void readFrom(PacketBuffer buf)
    {
        listener = buf.readString();
        add = buf.readBoolean();
    }

    @Override
    public void process(NetworkEvent.Context context)
    {
        handleClient();
    }

    @OnlyIn(Dist.CLIENT)
    public void handleClient()
    {
        Screen screen = Minecraft.getInstance().currentScreen;
        if(screen instanceof WorkspaceTabula)
        {
            WorkspaceTabula workspace = (WorkspaceTabula)screen;

            workspace.mainframe.listenerChange(listener, add);
        }
    }
}
