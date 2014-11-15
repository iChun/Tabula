package us.ichun.mods.tabula.gui.window.element;

import ichun.client.render.RendererHelper;
import net.minecraft.util.StatCollector;
import org.lwjgl.input.Mouse;
import us.ichun.mods.tabula.gui.Theme;
import us.ichun.mods.tabula.gui.window.Window;

public class ElementButton extends Element
{
    public int anchor;//0 = left, 1 = right, 2 = middle
    public int space;
    public String text;

    public ElementButton(Window window, int x, int y, int w, int h, int ID, boolean igMin, int side, String Text)
    {
        super(window, x, y, w, h, ID, igMin);
        anchor = side;
        switch(anchor)
        {
            case 0: space = posX; break;
            case 1: space = parent.width - posX - width; break;
        }
        text = Text;
    }

    @Override
    public void draw(int mouseX, int mouseY, boolean hover)
    {
        RendererHelper.drawColourOnScreen(Theme.elementButtonBorder[0], Theme.elementButtonBorder[1], Theme.elementButtonBorder[2], 255, getPosX(), getPosY(), width, height, 0);
        if(hover)
        {
            if(Mouse.isButtonDown(0))
            {
                RendererHelper.drawColourOnScreen(Theme.elementButtonClick[0], Theme.elementButtonClick[1], Theme.elementButtonClick[2], 255, getPosX() + 1, getPosY() + 1, width - 2, height - 2, 0);
            }
            else
            {
                RendererHelper.drawColourOnScreen(Theme.elementButtonBackgroundHover[0], Theme.elementButtonBackgroundHover[1], Theme.elementButtonBackgroundHover[2], 255, getPosX() + 1, getPosY() + 1, width - 2, height - 2, 0);
            }
        }
        else
        {
            RendererHelper.drawColourOnScreen(Theme.elementButtonBackgroundInactive[0], Theme.elementButtonBackgroundInactive[1], Theme.elementButtonBackgroundInactive[2], 255, getPosX() + 1, getPosY() + 1, width - 2, height - 2, 0);
        }
        parent.workspace.getFontRenderer().drawString(StatCollector.translateToLocal(text), getPosX() + (width / 2) - (parent.workspace.getFontRenderer().getStringWidth(StatCollector.translateToLocal(text)) / 2), getPosY() + height - (height / 2) - (parent.workspace.getFontRenderer().FONT_HEIGHT / 2), Theme.getAsHex(Theme.font), false);
    }

    @Override
    public boolean onClick(int mouseX, int mouseY, int id)
    {
        parent.elementTriggered(this);
        return true;
    }

    @Override
    public void resized()
    {
        switch(anchor)
        {
            case 0: posX = space; break;
            case 1: posX = parent.width - space - width; break;
            case 2: posX = (parent.width / 2) - (width / 2); break;
        }
    }
}
