package me.ichun.mods.tabula.common;

import me.ichun.mods.ichunutil.common.network.PacketChannel;
import me.ichun.mods.tabula.client.core.ConfigClient;
import me.ichun.mods.tabula.client.core.EventHandlerClient;
import me.ichun.mods.tabula.client.core.ResourceHelper;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.ExtensionPoint;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod(Tabula.MOD_ID)
public class Tabula
{
    public static final String MOD_ID = "tabula";
    public static final String MOD_NAME = "Tabula";

    public static final Logger LOGGER = LogManager.getLogger();

    public static ConfigClient configClient;

    public static EventHandlerClient eventHandlerClient;

    public static PacketChannel channel;

    public Tabula()
    {
        DistExecutor.runWhenOn(Dist.CLIENT, () -> () -> {
            configClient = new ConfigClient().init();
            MinecraftForge.EVENT_BUS.register(eventHandlerClient = new EventHandlerClient());
            ModLoadingContext.get().registerExtensionPoint(ExtensionPoint.CONFIGGUIFACTORY, () -> me.ichun.mods.ichunutil.client.core.EventHandlerClient::getConfigGui);
            ResourceHelper.init();
        });
    }

}
