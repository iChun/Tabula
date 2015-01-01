package us.ichun.mods.tabula.client.gui.window;

import ichun.common.core.network.PacketHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.util.ResourceLocation;
import us.ichun.mods.tabula.client.gui.GuiWorkspace;
import us.ichun.mods.tabula.client.gui.window.element.*;
import us.ichun.mods.tabula.common.Tabula;
import us.ichun.mods.tabula.common.packet.PacketChat;

public class WindowChat extends Window
{
    public int wasDocked;

    public ElementTextWrapperChat chatHolder;

    public WindowChat(GuiWorkspace parent, int x, int y, int w, int h, int minW, int minH)
    {
        super(parent, x, y, w, h, minW, minH, "window.chat.title", true);
        wasDocked = -1;

        elements.add(new ElementTextInput(this, 5, height - BORDER_SIZE - 14, width - 10, 12, 0, "window.chat.textbox", 200));
        chatHolder = new ElementTextWrapperChat(this, 5, BORDER_SIZE + 1 + 12, width - 10, (height - BORDER_SIZE - 14) - (BORDER_SIZE + 1 + 12) - 2, 2, false);
        chatHolder.sliderProg = 1.0D;
        elements.add(chatHolder);
    }

    public void toggleVisibility()
    {
        if(posX < -1 || posX > workspace.width || posY < 0 || posY > workspace.height)
        {
            if(wasDocked >= 0)
            {
                workspace.addToDock(wasDocked, this);

                if(posY > workspace.height)
                {
                    wasDocked = -1;
                    workspace.removeFromDock(this);

                    posX = (workspace.width - width) / 2;
                    posY = (workspace.height - height) / 2;
                }
            }
            else
            {
                posX = (workspace.width - width) / 2;
                posY = (workspace.height - height) / 2;
            }
        }
        else
        {
            wasDocked = docked;
            if(docked >= 0)
            {
                workspace.removeFromDock(this);
            }
            posX = -1000;
            posY = -1000;
        }
        this.resized();
    }

    @Override
    public void elementTriggered(Element element)
    {
        if(element.id == 0)
        {
            ElementTextInput text = (ElementTextInput)element;
            if(!text.textField.getText().isEmpty())
            {
                if(!workspace.remoteSession)
                {
                    Tabula.proxy.tickHandlerClient.mainframe.sendChat(Minecraft.getMinecraft().getSession().getUsername(), text.textField.getText());
                }
                else if(!workspace.sessionEnded)
                {
                    PacketHandler.sendToServer(Tabula.channels, new PacketChat(workspace.host, Minecraft.getMinecraft().getSession().getUsername(), text.textField.getText()));
                }
                text.textField.setText("");
            }
        }
    }

    @Override
    public void resized()
    {
        for(Element e : elements)
        {
            if(e.id == 0)
            {
                e.posY = height - BORDER_SIZE - 14;
            }
        }
        super.resized();
    }
}
