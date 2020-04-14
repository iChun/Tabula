package me.ichun.mods.tabula.old.common.core;

import me.ichun.mods.ichunutil.common.core.network.PacketChannel;
import me.ichun.mods.tabula.old.client.core.TickHandlerClient;
import me.ichun.mods.tabula.old.common.Tabula;
import me.ichun.mods.tabula.common.packet.*;
import me.ichun.mods.tabula.old.common.packet.*;

public class ProxyCommon
{
    public TickHandlerClient tickHandlerClient;

    public void preInit()
    {
        Tabula.channel = new PacketChannel(Tabula.MOD_NAME, PacketRequestSession.class, PacketBeginSession.class, PacketEndSession.class, PacketAddListener.class, PacketRemoveListener.class,
                PacketChat.class, PacketChatMessage.class, PacketPingAlive.class, PacketIsEditor.class, PacketRequestHeartbeat.class,
                PacketHeartbeat.class, PacketProjectFragment.class, PacketCloseProject.class, PacketRequestProject.class, PacketSetCurrentProject.class,
                PacketGenericMethod.class, PacketProjectFragmentFromClient.class, PacketClearTexture.class, PacketListenersList.class, PacketSetProjectMetadata.class);
    }

    public void init(){}

    public void postInit(){}

    public void updateProject(boolean fromClient, String ident, boolean isTexture, boolean updateDims){}
}
