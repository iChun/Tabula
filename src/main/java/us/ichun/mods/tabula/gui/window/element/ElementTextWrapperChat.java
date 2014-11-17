package us.ichun.mods.tabula.gui.window.element;

import org.lwjgl.opengl.GL11;
import us.ichun.mods.tabula.gui.Theme;
import us.ichun.mods.tabula.gui.window.Window;

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
        GL11.glPushMatrix();
        for(int i = 0; i < text.size(); i++)
        {
            String msg = text.get(i);

            List list = parent.workspace.getFontRenderer().listFormattedStringToWidth(msg, width - 12);

            for(int j = 0; j < list.size(); j++)
            {
                if(j == 0)
                {
                    String[] strings = ((String)list.get(j)).split(": ");
                    parent.workspace.getFontRenderer().drawString(strings[0], getPosX() + 4, getPosY() + 4, getRandomColourForName(strings[0]), false);
                    parent.workspace.getFontRenderer().drawString(": " + strings[1], getPosX() + parent.workspace.getFontRenderer().getStringWidth(strings[0]) + 4, getPosY() + 4, Theme.getAsHex(Theme.font), false);
                }
                else
                {
                    parent.workspace.getFontRenderer().drawString((String)list.get(j), getPosX() + 12, getPosY() + 4, Theme.getAsHex(Theme.font), false);
                }
                GL11.glTranslatef(0F, parent.workspace.getFontRenderer().FONT_HEIGHT + 2, 0F);
            }
        }
        GL11.glPopMatrix();
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
