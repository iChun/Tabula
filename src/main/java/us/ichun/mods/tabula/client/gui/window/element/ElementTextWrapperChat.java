package us.ichun.mods.tabula.client.gui.window.element;

import net.minecraft.client.renderer.GlStateManager;
import us.ichun.mods.tabula.client.gui.Theme;
import us.ichun.mods.tabula.client.gui.window.Window;

import java.util.List;

public class ElementTextWrapperChat extends ElementTextWrapper
{
    public ElementTextWrapperChat(Window window, int x, int y, int w, int h, int ID, boolean igMin)
    {
        super(window, x, y, w, h, ID, igMin, true);
    }

    @Override
    public void drawText(int mouseX, int mouseY, boolean hover)
    {
        GlStateManager.pushMatrix();
        for(int i = 0; i < text.size(); i++)
        {
            String msg = text.get(i);

            List list = parent.workspace.getFontRenderer().listFormattedStringToWidth(msg, width - 12);

            boolean onName = true;
            for(int j = 0; j < list.size(); j++)
            {
                String[] strings = ((String)list.get(j)).split(": ");
                parent.workspace.getFontRenderer().drawString(strings[0], getPosX() + (j == 0 ? 4 : 12), getPosY() + 4, onName ? getRandomColourForName(strings[0]) : Theme.getAsHex(Theme.instance.fontChat), false);
                if(strings.length > 1)
                {
                    onName = false;
                    parent.workspace.getFontRenderer().drawString(": " + strings[1], getPosX() + parent.workspace.getFontRenderer().getStringWidth(strings[0]) + 4, getPosY() + 4, Theme.getAsHex(Theme.instance.fontChat), false);
                }
                GlStateManager.translate(0F, parent.workspace.getFontRenderer().FONT_HEIGHT + 2, 0F);
            }
        }
        GlStateManager.popMatrix();
    }

    public int getRandomColourForName(String s)
    {
        if(s.equalsIgnoreCase("System"))
        {
            return 0xffcc00;
        }
        else
        {
            return Math.abs(s.hashCode()) & 0xffffff;
        }
    }
}
