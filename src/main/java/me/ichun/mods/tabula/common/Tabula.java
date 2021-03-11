package me.ichun.mods.tabula.common;

import me.ichun.mods.ichunutil.common.module.tabula.TabulaPlugin;
import me.ichun.mods.ichunutil.common.network.PacketChannel;
import me.ichun.mods.tabula.client.core.ConfigClient;
import me.ichun.mods.tabula.client.core.EventHandlerClient;
import me.ichun.mods.tabula.client.core.ResourceHelper;
import me.ichun.mods.tabula.client.render.TileRendererTabulaRasa;
import me.ichun.mods.tabula.common.block.BlockTabulaRasa;
import me.ichun.mods.tabula.common.packet.*;
import me.ichun.mods.tabula.common.tileentity.TileEntityTabulaRasa;
import net.minecraft.block.Block;
import net.minecraft.client.renderer.model.Model;
import net.minecraft.client.renderer.texture.AtlasTexture;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.*;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.InterModProcessEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLEnvironment;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.HashSet;

@Mod(Tabula.MOD_ID)
public class Tabula
{
    public static final String MOD_ID = "tabula";
    public static final String MOD_NAME = "Tabula";
    public static final String VERSION = "8.0.0";
    public static final String PROTOCOL = "1";

    public static final Logger LOGGER = LogManager.getLogger();

    public static ConfigClient configClient;

    public static EventHandlerClient eventHandlerClient;

    public static PacketChannel channel;

    public static HashSet<Class<? extends Model>> modelBlacklist = new HashSet<>();

    public Tabula()
    {
        IEventBus bus = FMLJavaModLoadingContext.get().getModEventBus();

        DistExecutor.runWhenOn(Dist.CLIENT, () -> () -> {
            configClient = new ConfigClient().init();
            MinecraftForge.EVENT_BUS.register(eventHandlerClient = new EventHandlerClient());
            ModLoadingContext.get().registerExtensionPoint(ExtensionPoint.CONFIGGUIFACTORY, () -> me.ichun.mods.ichunutil.client.core.EventHandlerClient::getConfigGui);
            ResourceHelper.init();
            bus.addListener(this::processIMC);
            bus.addListener(this::onTextureStitchEvent);
        });

        Blocks.REGISTRY.register(bus);
        Items.REGISTRY.register(bus);
        TileEntityTypes.REGISTRY.register(bus);
        bus.addListener(this::onClientSetup);

        channel = new PacketChannel(new ResourceLocation(MOD_ID, "channel"), PROTOCOL,
                PacketRequestSession.class, PacketPing.class, PacketKillSession.class, PacketListenerChange.class, PacketChat.class, PacketEditorStatus.class, PacketProjectFragment.class, PacketRequestProject.class
        );
    }

    private void onClientSetup(FMLClientSetupEvent event)
    {
        ClientRegistry.bindTileEntityRenderer(TileEntityTypes.TABULA_RASA.get(), TileRendererTabulaRasa::new);
    }

    private void onTextureStitchEvent(TextureStitchEvent.Pre event)
    {
        if(event.getMap().getTextureLocation() == AtlasTexture.LOCATION_BLOCKS_TEXTURE)
        {
            event.addSprite(new ResourceLocation("tabula", "model/tabularasa"));
        }
    }

    private void processIMC(InterModProcessEvent event)
    {
        event.getIMCStream(m -> m.equalsIgnoreCase("blacklist")).forEach(msg -> {
            Object o = msg.getMessageSupplier().get();
            Class<? extends Model> modelClz = null;
            if(o instanceof Class<?>)
            {
                if(Model.class.isAssignableFrom((Class<?>)o) && o != Model.class) //is an instance
                {
                    modelClz = (Class<? extends Model>)o;
                }
            }
            else if(o instanceof String)
            {
                try
                {
                    Class clz = Class.forName(o.toString());
                    if(Model.class.isAssignableFrom(clz)) //is an instance
                    {
                        modelClz = (Class<? extends Model>)o;
                    }
                }
                catch(ClassNotFoundException ignored){}
            }

            if(modelClz != null)
            {
                modelBlacklist.add(modelClz);
                LOGGER.info("Blacklisting {} as requested by {}", modelClz.getName(), msg.getSenderModId());
            }
        });
        event.getIMCStream(m -> m.equalsIgnoreCase("plugin")).forEach(msg -> {
            if(FMLEnvironment.dist.isClient())
            {
                handleClientIMC(msg);
            }
            else
            {
                LOGGER.warn("Received plugin IMC when we're not on a client: {}", msg.getSenderModId());
            }
        });
    }

    @OnlyIn(Dist.CLIENT)
    private void handleClientIMC(InterModComms.IMCMessage msg)
    {
        Object o = msg.getMessageSupplier().get();
        if(o instanceof TabulaPlugin)
        {
            eventHandlerClient.loadPlugin(msg.getSenderModId(), (TabulaPlugin)o);
        }
        else if(o instanceof Class<?>)
        {
            if(TabulaPlugin.class.isAssignableFrom((Class<?>)o))
            {
                try
                {
                    eventHandlerClient.loadPlugin(msg.getSenderModId(), ((Class<?extends TabulaPlugin>)o).newInstance());
                }
                catch(InstantiationException | IllegalAccessException e)
                {
                    Tabula.LOGGER.error("Error creating instance for {} from {}", ((Class<?extends TabulaPlugin>)o).getName(), msg.getSenderModId());
                    e.printStackTrace();
                }
            }
            else
            {
                LOGGER.error("Found invalid plugin class from {}: {}", msg.getSenderModId(), ((Class<?>)o).getName());
            }
        }
        else if(o instanceof String)
        {
            try
            {
                Class clz = Class.forName(o.toString());
                if(TabulaPlugin.class.isAssignableFrom((Class<?>)o))
                {
                    try
                    {
                        eventHandlerClient.loadPlugin(msg.getSenderModId(), ((Class<?extends TabulaPlugin>)clz).newInstance());
                    }
                    catch(InstantiationException | IllegalAccessException e)
                    {
                        Tabula.LOGGER.error("Error creating instance for {} from {}", ((Class<?extends TabulaPlugin>)clz).getName(), msg.getSenderModId());
                        e.printStackTrace();
                    }
                }
            }
            catch(ClassNotFoundException ignored)
            {
                LOGGER.error("Found invalid plugin class from {}: {}", msg.getSenderModId(), o.toString());
            }
        }
    }

    public static class Blocks
    {
        private static final DeferredRegister<Block> REGISTRY = DeferredRegister.create(ForgeRegistries.BLOCKS, MOD_ID);

        public static final RegistryObject<BlockTabulaRasa> TABULA_RASA = REGISTRY.register("tabularasa", BlockTabulaRasa::new);
    }

    public static class Items
    {
        private static final DeferredRegister<Item> REGISTRY = DeferredRegister.create(ForgeRegistries.ITEMS, MOD_ID);

        public static final RegistryObject<BlockItem> TABULA_RASA = REGISTRY.register("tabularasa", () -> new BlockItem(Blocks.TABULA_RASA.get(), (new Item.Properties()).group(ItemGroup.DECORATIONS)));
    }

    public static class TileEntityTypes
    {
        private static final DeferredRegister<TileEntityType<?>> REGISTRY = DeferredRegister.create(ForgeRegistries.TILE_ENTITIES, MOD_ID);

        public static final RegistryObject<TileEntityType<TileEntityTabulaRasa>> TABULA_RASA = REGISTRY.register("tabularasa", () -> TileEntityType.Builder.create(TileEntityTabulaRasa::new, Blocks.TABULA_RASA.get()).build(null));
    }
}
