package me.ichun.mods.tabula.old.common.core;

import me.ichun.mods.ichunutil.common.module.tabula.project.ProjectInfo;
import me.ichun.mods.tabula.old.client.mainframe.core.ProjectHelper;
import me.ichun.mods.tabula.old.common.Tabula;
import me.ichun.mods.tabula.old.common.block.BlockTabulaRasa;
import me.ichun.mods.tabula.old.common.tileentity.TileEntityTabulaRasa;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiMainMenu;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.renderer.texture.TextureUtil;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.network.FMLNetworkEvent;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.awt.*;

public class EventHandler
{
    public static final int TABULA_BUTTON_ID = Tabula.MOD_NAME.hashCode();

    @SideOnly(Side.CLIENT)
    @SubscribeEvent(priority = EventPriority.LOW)
    public void onInitGuiPost(GuiScreenEvent.InitGuiEvent.Post event)
    {
    }

    @SideOnly(Side.CLIENT)
    @SubscribeEvent
    public void onButtonPressPre(GuiScreenEvent.ActionPerformedEvent.Pre event)
    {
        if(event.getGui() instanceof GuiMainMenu && event.getButton().id == TABULA_BUTTON_ID)
        {
            Tabula.proxy.tickHandlerClient.initializeMainframe(null, -1, -1, -1);
            event.setCanceled(true);
        }
    }

    @SideOnly(Side.CLIENT)
    @SubscribeEvent
    public void onClientConnect(FMLNetworkEvent.ClientConnectedToServerEvent event)
    {
        ProjectHelper.projectParts.clear();
        ProjectHelper.projectTextureParts.clear();

        for(ProjectInfo proj : ProjectHelper.projects.values())
        {
            proj.destroy();
        }

        for(Integer id : ProjectHelper.projectTextureIDs.values())
        {
            TextureUtil.deleteTexture(id);
        }

        ProjectHelper.projects.clear();
        ProjectHelper.projectTextures.clear();
        ProjectHelper.projectTextureIDs.clear();
    }

    @SubscribeEvent
    public void onRegisterBlock(RegistryEvent.Register<Block> event)
    {
        Tabula.blockTabulaRasa = (new BlockTabulaRasa(Material.CIRCUITS)).setHardness(0.0F)
                .setCreativeTab(CreativeTabs.DECORATIONS)
                .setRegistryName(new ResourceLocation("tabula", "tabularasa"))
                .setTranslationKey("tabula.block.tabularasa");

        event.getRegistry().register(Tabula.blockTabulaRasa);

        GameRegistry.registerTileEntity(TileEntityTabulaRasa.class, new ResourceLocation("tabula", "tabula_rasa"));
    }

    @SubscribeEvent
    public void onRegisterItem(RegistryEvent.Register<Item> event)
    {
        event.getRegistry().register(new ItemBlock(Tabula.blockTabulaRasa).setRegistryName(Tabula.blockTabulaRasa.getRegistryName()));
    }

    @SideOnly(Side.CLIENT)
    @SubscribeEvent
    public void onModelRegistry(ModelRegistryEvent event)
    {
        ModelLoader.setCustomModelResourceLocation(Item.getItemFromBlock(Tabula.blockTabulaRasa), 0, new ModelResourceLocation("tabula:tabularasa", "inventory"));
    }
}
