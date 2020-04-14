package me.ichun.mods.tabula.old.common.packet;

import io.netty.buffer.ByteBuf;
import me.ichun.mods.ichunutil.common.core.network.AbstractPacket;
import me.ichun.mods.tabula.old.client.gui.GuiWorkspace;
import me.ichun.mods.tabula.old.common.Tabula;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.ArrayList;
import java.util.Set;

public class PacketListenersList extends AbstractPacket
{
    public String listener;
    public ArrayList<String> editors;
    public ArrayList<String> listeners;

    public PacketListenersList(){}

    public PacketListenersList(String listener, ArrayList<String> editors, Set<String> listeners)
    {
        this.listener = listener;
        this.editors = new ArrayList<>(editors);
        this.listeners = new ArrayList<>(listeners);
    }

    @Override
    public void writeTo(ByteBuf buffer)
    {
        ByteBufUtils.writeUTF8String(buffer, listener);
        buffer.writeInt(editors.size());
        for(String editor : editors)
        {
            ByteBufUtils.writeUTF8String(buffer, editor);
        }
        buffer.writeInt(listeners.size());
        for(String listener1 : listeners)
        {
            ByteBufUtils.writeUTF8String(buffer, listener1);
        }
    }

    @Override
    public void readFrom(ByteBuf buffer)
    {
        listener = ByteBufUtils.readUTF8String(buffer);
        editors = new ArrayList<>();
        int eSize = buffer.readInt();
        for(int i = 0; i < eSize; i++)
        {
            editors.add(ByteBufUtils.readUTF8String(buffer));
        }
        listeners = new ArrayList<>();
        int lSize = buffer.readInt();
        for(int i = 0; i < lSize; i++)
        {
            listeners.add(ByteBufUtils.readUTF8String(buffer));
        }
    }

    @Override
    public void execute(Side side, EntityPlayer player)
    {
        if(side.isServer())
        {
            EntityPlayerMP listener1 = FMLCommonHandler.instance().getMinecraftServerInstance().getPlayerList().getPlayerByUsername(listener);
            if(listener1 != null)
            {
                Tabula.channel.sendTo(this, listener1);
            }
        }
        else
        {
            handleClient();
        }
    }

    @Override
    public Side receivingSide()
    {
        return null;
    }

    @SideOnly(Side.CLIENT)
    public void handleClient()
    {
        Minecraft mc = Minecraft.getMinecraft();
        if(mc.currentScreen instanceof GuiWorkspace)
        {
            GuiWorkspace workspace = (GuiWorkspace)mc.currentScreen;
            workspace.editors = editors;
            workspace.listeners = listeners;
        }
    }
}
