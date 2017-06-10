package me.ichun.mods.tabula.common.core;

import me.ichun.mods.ichunutil.common.module.tabula.project.ProjectInfo;
import me.ichun.mods.tabula.client.mainframe.core.ProjectHelper;
import me.ichun.mods.tabula.common.Tabula;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiMainMenu;
import net.minecraft.client.renderer.texture.TextureUtil;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.network.FMLNetworkEvent;
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
                        intersects = btn.intersects(new Rectangle(button.xPosition, button.yPosition, button.width, button.height));
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
}
