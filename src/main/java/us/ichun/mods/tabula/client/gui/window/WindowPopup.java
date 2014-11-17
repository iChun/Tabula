package us.ichun.mods.tabula.client.gui.window;

import net.minecraft.util.StatCollector;
import us.ichun.mods.tabula.client.gui.GuiWorkspace;
import us.ichun.mods.tabula.client.gui.Theme;
import us.ichun.mods.tabula.client.gui.window.element.Element;
import us.ichun.mods.tabula.client.gui.window.element.ElementButton;

public class WindowPopup extends Window
{
    public String message;

    public WindowPopup(GuiWorkspace parent, int x, int y, int w, int h, int minW, int minH, String msg)
    {
        super(parent, 0, 0, w, h, minW, minH, "window.popup.title", true);
        message = msg;

        elements.add(new ElementButton(this, width / 2 - 30, height - 25, 60, 16, 3, false, 2, 1, "element.button.ok"));
    }

    @Override
    public void draw(int mouseX, int mouseY)
    {
        super.draw(mouseX, mouseY);
        if(!minimized)
        {
            workspace.getFontRenderer().drawString(StatCollector.translateToLocal(message), posX + 11, posY + 20, Theme.getAsHex(Theme.font), false);
        }
    }

    @Override
    public void elementTriggered(Element element)
    {
        workspace.removeWindow(this, true);
    }
    }
