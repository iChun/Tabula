package me.ichun.mods.tabula.common.core;

import me.ichun.mods.ichunutil.common.module.tabula.project.ProjectInfo;
import me.ichun.mods.tabula.client.mainframe.core.ProjectHelper;
import me.ichun.mods.tabula.common.Tabula;
import me.ichun.mods.tabula.common.block.BlockTabulaRasa;
import me.ichun.mods.tabula.common.tileentity.TileEntityTabulaRasa;
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
        if(event.getGui() instanceof GuiMainMenu)
        {
            int offsetX = 0;
            int offsetY = 0; //24?
            int btnX = event.getGui().width / 2 - 124 + offsetX;
            int btnY = event.getGui().height / 4 + 48 + 24 * 2 + offsetY;
            while(true)
            {
                if(btnX < 0)
                {
                    if(offsetY <= -48) //give up
                    {
                        btnX = 0;
                        btnY = 0;
                        break;
                    }
                    else
                    {
                        offsetX = 0;
                        offsetY -= 24;
                        btnX = event.getGui().width / 2 - 124 + offsetX;
                        btnY = event.getGui().height / 4 + 48 + 24 * 2 + offsetY;
                    }
                }

                Rectangle btn = new Rectangle(btnX, btnY, 20, 20);//Thanks to heldplayer for this.
                boolean intersects = false;
                for(int i = 0; i < event.getGui().buttonList.size(); i++)
                {
                    GuiButton button = event.getGui().buttonList.get(i);
                    if(!intersects)
                    {
                        intersects = btn.intersects(new Rectangle(button.x, button.y, button.width, button.height));
                    }
                }

                if(!intersects)
                {
                    break;
                }

                btnX -= 24; // move to the left to try and find a free space.
            }

            event.getGui().buttonList.add(new GuiButton(TABULA_BUTTON_ID, btnX, btnY, 20, 20, "T"));
        }
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
        Tabula.blockTabulaRasa = (new BlockTabulaRasa(Material.CIRCUITS)).setHardness(0.0F).setCreativeTab(CreativeTabs.DECORATIONS).setRegistryName(new ResourceLocation("tabula", "tabularasa")).setTranslationKey("tabula.block.tabularasa");
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
