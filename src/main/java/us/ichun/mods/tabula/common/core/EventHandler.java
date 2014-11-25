package us.ichun.mods.tabula.common.core;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiMainMenu;
import net.minecraftforge.client.event.GuiScreenEvent;
import us.ichun.mods.tabula.Tabula;

import java.awt.*;

public class EventHandler
{
    public static final int TABULA_BUTTON_ID = "Tabula".hashCode();

    @SideOnly(Side.CLIENT)
    @SubscribeEvent
    public void onInitGuiPost(GuiScreenEvent.InitGuiEvent.Post event)
    {
        if(event.gui instanceof GuiMainMenu)
        {
            int offsetX = 0;
            int offsetY = 0; //24?
            int btnX = event.gui.width / 2 - 124 + offsetX;
            int btnY = event.gui.height / 4 + 48 + 24 * 2 + offsetY;
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
                        btnX = event.gui.width / 2 - 124 + offsetX;
                        btnY = event.gui.height / 4 + 48 + 24 * 2 + offsetY;
                    }
                }

                Rectangle btn = new Rectangle(btnX, btnY, 20, 20);//Thanks to heldplayer for this.
                boolean intersects = false;
                for(int i = 0; i < event.gui.buttonList.size(); i++)
                {
                    GuiButton button = (GuiButton)event.gui.buttonList.get(i);
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

            event.gui.buttonList.add(new GuiButton(TABULA_BUTTON_ID, btnX, btnY, 20, 20, "T"));
        }
    }

    @SideOnly(Side.CLIENT)
    @SubscribeEvent
    public void onButtonPressPre(GuiScreenEvent.ActionPerformedEvent.Pre event)
    {
        if(event.gui instanceof GuiMainMenu && event.button.id == TABULA_BUTTON_ID)
        {
            Tabula.proxy.tickHandlerClient.initializeMainframe();
            event.setCanceled(true);
        }
    }
}
