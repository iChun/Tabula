package us.ichun.mods.tabula.common;

import net.minecraft.block.Block;
import net.minecraft.client.model.ModelBase;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLInterModComms;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import us.ichun.mods.ichunutil.common.core.Logger;
import us.ichun.mods.ichunutil.common.core.config.ConfigBase;
import us.ichun.mods.ichunutil.common.core.config.ConfigHandler;
import us.ichun.mods.ichunutil.common.core.network.PacketChannel;
import us.ichun.mods.ichunutil.common.core.updateChecker.ModVersionChecker;
import us.ichun.mods.ichunutil.common.core.updateChecker.ModVersionInfo;
import us.ichun.mods.ichunutil.common.iChunUtil;
import us.ichun.mods.ichunutil.common.module.tabula.client.model.ModelList;
import us.ichun.mods.tabula.client.core.ResourceHelper;
import us.ichun.mods.tabula.common.core.CommonProxy;
import us.ichun.mods.tabula.common.core.Config;
import us.ichun.mods.tabula.common.core.EventHandler;

import java.io.File;

@Mod(modid = Tabula.modName, name = Tabula.modName,
        version = Tabula.version,
        guiFactory = "us.ichun.mods.ichunutil.common.core.config.GenericModGuiFactory",
        dependencies = "required-after:iChunUtil@[" + iChunUtil.versionMC +".4.0," + (iChunUtil.versionMC + 1) + ".0.0)",
        acceptableRemoteVersions = "[" + iChunUtil.versionMC +".1.0," + iChunUtil.versionMC + ".2.0)"
)
public class Tabula
{
    public static final String modName = "Tabula";
    public static final String version = iChunUtil.versionMC + ".1.0";

    @Mod.Instance(Tabula.modName)
    public static Tabula instance;

    @SidedProxy(clientSide = "us.ichun.mods.tabula.client.core.ClientProxy", serverSide = "us.ichun.mods.tabula.common.core.CommonProxy")
    public static CommonProxy proxy;

    public static PacketChannel channel;

    public static final Logger logger = Logger.createLogger(Tabula.modName);

    public static Config config;

    public static Block blockTabulaRasa;

    @Mod.EventHandler
    public void preLoad(FMLPreInitializationEvent event)
    {
        proxy.preInit();

        if(FMLCommonHandler.instance().getEffectiveSide().isClient())
        {
            config = (Config)ConfigHandler.registerConfig(new Config(new File(ResourceHelper.getConfigDir(), "config.cfg")));
        }

        EventHandler handler = new EventHandler();
        FMLCommonHandler.instance().bus().register(handler);
        MinecraftForge.EVENT_BUS.register(handler);

        ModVersionChecker.register_iChunMod(new ModVersionInfo(modName, iChunUtil.versionOfMC, version, false));
    }

    @Mod.EventHandler
    public void load(FMLInitializationEvent event)
    {
        proxy.init();
    }

    @Mod.EventHandler
    public void postLoad(FMLPostInitializationEvent event)
    {
        proxy.postInit();
    }

    @Mod.EventHandler
    public void onIMCMessage(FMLInterModComms.IMCEvent event)
    {
        for(FMLInterModComms.IMCMessage message : event.getMessages())
        {
            if(message.key.equalsIgnoreCase("blacklist"))
            {
                try
                {
                    Class clz = Class.forName(message.getStringValue());
                    if(ModelBase.class.isAssignableFrom(clz))
                    {
                        if(ModelList.modelBlacklist.contains(clz))
                        {
                            Tabula.logger.warn(message.getStringValue() + " is already blacklisted");
                        }
                        else
                        {
                            ModelList.modelBlacklist.add(clz);
                            Tabula.logger.warn(message.getStringValue() + " blacklisted from Tabula's import list");
                        }
                    }
                    else
                    {
                        Tabula.logger.warn(message.getStringValue() + " is not a model class!");
                    }
                }
                catch(Exception e)
                {
                    Tabula.logger.warn("Could not find class " + message.getStringValue() + " for blacklist");
                }
            }
        }
    }
}
