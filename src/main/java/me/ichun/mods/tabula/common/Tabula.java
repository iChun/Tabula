package me.ichun.mods.tabula.common;

import me.ichun.mods.ichunutil.common.network.PacketChannel;
import me.ichun.mods.tabula.client.core.ConfigClient;
import me.ichun.mods.tabula.client.core.EventHandlerClient;
import me.ichun.mods.tabula.client.core.ResourceHelper;
import me.ichun.mods.tabula.client.render.TileRendererTabulaRasa;
import me.ichun.mods.tabula.common.block.BlockTabulaRasa;
import me.ichun.mods.tabula.common.packet.*;
import me.ichun.mods.tabula.common.tileentity.TileEntityTabulaRasa;
import net.minecraft.block.Block;
import net.minecraft.client.renderer.texture.AtlasTexture;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.ExtensionPoint;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

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

    public Tabula()
    {
        IEventBus bus = FMLJavaModLoadingContext.get().getModEventBus();

        DistExecutor.runWhenOn(Dist.CLIENT, () -> () -> {
            configClient = new ConfigClient().init();
            MinecraftForge.EVENT_BUS.register(eventHandlerClient = new EventHandlerClient());
            ModLoadingContext.get().registerExtensionPoint(ExtensionPoint.CONFIGGUIFACTORY, () -> me.ichun.mods.ichunutil.client.core.EventHandlerClient::getConfigGui);
            ResourceHelper.init();
            bus.addListener(this::onTextureStitchEvent);
        });

        Blocks.REGISTRY.register(bus);
        Items.REGISTRY.register(bus);
        TileEntityTypes.REGISTRY.register(bus);
        bus.addListener(this::onClientSetup);

        channel = new PacketChannel(new ResourceLocation(MOD_ID, "channel"), PROTOCOL,
                PacketRequestSession.class, PacketPing.class, PacketKillSession.class, PacketListenerChange.class, PacketChat.class, PacketEditorStatus.class, PacketProjectFragment.class
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

    public static class Blocks
    {
        private static final DeferredRegister<Block> REGISTRY = new DeferredRegister<>(ForgeRegistries.BLOCKS, MOD_ID);

        public static final RegistryObject<BlockTabulaRasa> TABULA_RASA = REGISTRY.register("tabularasa", BlockTabulaRasa::new);
    }

    public static class Items
    {
        private static final DeferredRegister<Item> REGISTRY = new DeferredRegister<>(ForgeRegistries.ITEMS, MOD_ID);

        public static final RegistryObject<BlockItem> TABULA_RASA = REGISTRY.register("tabularasa", () -> new BlockItem(Blocks.TABULA_RASA.get(), (new Item.Properties()).group(ItemGroup.DECORATIONS)));
    }

    public static class TileEntityTypes
    {
        private static final DeferredRegister<TileEntityType<?>> REGISTRY = new DeferredRegister<>(ForgeRegistries.TILE_ENTITIES, MOD_ID);

        public static final RegistryObject<TileEntityType<TileEntityTabulaRasa>> TABULA_RASA = REGISTRY.register("tabularasa", () -> TileEntityType.Builder.create(TileEntityTabulaRasa::new, Blocks.TABULA_RASA.get()).build(null));
    }
}
