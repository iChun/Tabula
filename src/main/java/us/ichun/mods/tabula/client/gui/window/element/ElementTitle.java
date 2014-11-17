package us.ichun.mods.tabula.client.gui.window.element;

import net.minecraft.util.StatCollector;
import us.ichun.mods.tabula.client.gui.window.Window;

public class ElementTitle extends Element
{
    public ElementTitle(Window window, int x, int y, int w, int h, int ID)
    {
        super(window, x, y, w, h, ID, true);
    }

    @Override
    public void draw(int mouseX, int mouseY, boolean hover)
    {
    }

    @Override
    public void resized()
    {
        posX = 0;
        posY = 0;
        width = parent.width - 13;
        height = 13;
    }

    @Override
    public String tooltip()
    {
        String titleToRender = StatCollector.translateToLocal(parent.titleLocale);
        if(parent.workspace.getFontRenderer().getStringWidth(titleToRender) > parent.getWidth() - (parent.BORDER_SIZE * 2) - parent.workspace.getFontRenderer().getStringWidth("  _"))
        {
            return parent.titleLocale;
        }
        return null;
    }
}
