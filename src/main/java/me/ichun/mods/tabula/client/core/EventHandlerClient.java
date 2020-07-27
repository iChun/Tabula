package me.ichun.mods.tabula.client.core;

import me.ichun.mods.tabula.client.gui.WorkspaceTabula;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.MainMenuScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.Widget;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.util.text.ChatType;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.client.event.ClientChatReceivedEvent;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.awt.*;

public class EventHandlerClient
{
    @SubscribeEvent(priority = EventPriority.LOW)
    public void onInitGuiPost(GuiScreenEvent.InitGuiEvent.Post event)
    {
        if(event.getGui() instanceof MainMenuScreen)
        {
            int offsetX = 0;
            int offsetY = 0; //24?
            int btnX = event.getGui().width / 2 - 124 + offsetX;
            int btnY = event.getGui().height / 4 + 48 + 24 * 2 + offsetY;
            for(int i = 0; i < event.getWidgetList().size(); i++)
            {
                Widget button = event.getWidgetList().get(i);
                if(button instanceof Button && button.getMessage() instanceof TranslationTextComponent && ((TranslationTextComponent)button.getMessage()).getKey().equals("fml.menu.mods"))
                {
                    btnY = button.y;
                    break;
                }
            }
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
                for(int i = 0; i < event.getWidgetList().size(); i++)
                {
                    Widget button = event.getWidgetList().get(i);
                    if(!intersects)
                    {
                        intersects = btn.intersects(new Rectangle(button.x, button.y, button.getWidth(), button.getHeight()));
                    }
                }

                if(!intersects)
                {
                    break;
                }

                btnX -= 24; // move to the left to try and find a free space.
            }

            event.addWidget(new Button(btnX, btnY, 20, 20, new StringTextComponent("T"), button -> {
                WorkspaceTabula screen = WorkspaceTabula.create(Minecraft.getInstance().getSession().getUsername());
                Minecraft.getInstance().displayGuiScreen(screen);
            }));
        }
    }

    @SubscribeEvent(priority = EventPriority.LOW)
    public void onClientChatReceived(ClientChatReceivedEvent event)
    {
        if(event.getType().equals(ChatType.CHAT) || event.getType().equals(ChatType.SYSTEM))
        {
            Screen screen = Minecraft.getInstance().currentScreen;
            if(screen instanceof WorkspaceTabula)
            {
                WorkspaceTabula workspace = (WorkspaceTabula)screen;
                workspace.mainframe.receiveChat(event.getMessage().getString(), false);
            }
        }
    }
}
