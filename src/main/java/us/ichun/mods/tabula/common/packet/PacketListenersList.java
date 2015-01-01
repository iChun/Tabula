package us.ichun.mods.tabula.common.packet;

import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.network.ByteBufUtils;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import ichun.common.core.network.AbstractPacket;
import ichun.common.core.network.PacketHandler;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import us.ichun.mods.tabula.client.gui.GuiWorkspace;
import us.ichun.mods.tabula.common.Tabula;

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
        this.editors = new ArrayList<String>(editors);
        this.listeners = new ArrayList<String>(listeners);
    }

    @Override
    public void writeTo(ByteBuf buffer, Side side)
    {
        ByteBufUtils.writeUTF8String(buffer, listener);
        buffer.writeInt(editors.size());
        for(int i = 0; i < editors.size(); i++)
        {
            ByteBufUtils.writeUTF8String(buffer, editors.get(i));
        }
        buffer.writeInt(listeners.size());
        for(int i = 0; i < listeners.size(); i++)
        {
            ByteBufUtils.writeUTF8String(buffer, listeners.get(i));
        }
    }

    @Override
    public void readFrom(ByteBuf buffer, Side side)
    {
        listener = ByteBufUtils.readUTF8String(buffer);
        editors = new ArrayList<String>();
        int eSize = buffer.readInt();
        for(int i = 0; i < eSize; i++)
        {
            editors.add(ByteBufUtils.readUTF8String(buffer));
        }
        listeners = new ArrayList<String>();
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
            EntityPlayerMP listener1 = FMLCommonHandler.instance().getMinecraftServerInstance().getConfigurationManager().func_152612_a(listener);
            if(listener1 != null)
            {
                PacketHandler.sendToPlayer(Tabula.channels, this, listener1);
            }
        }
        else
        {
            handleClient();
        }
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
