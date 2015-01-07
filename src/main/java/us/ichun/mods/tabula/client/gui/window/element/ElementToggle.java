package us.ichun.mods.tabula.client.gui.window.element;

import net.minecraft.util.StatCollector;
import org.lwjgl.input.Mouse;
import us.ichun.mods.ichunutil.client.render.RendererHelper;
import us.ichun.mods.tabula.client.gui.Theme;
import us.ichun.mods.tabula.client.gui.window.Window;

public class ElementToggle extends Element
{
    public int anchorH;//0 = left, 1 = right, 2 = middle
    public int spaceH;
    public int anchorV;
    public int spaceV;
    public String text;
    public String tooltip;
    public boolean toggledState;

    public ElementToggle(Window window, int x, int y, int w, int h, int ID, boolean igMin, int sideH, int sideV, String Text, String Tooltip, boolean state)
    {
        super(window, x, y, w, h, ID, igMin);
        anchorH = sideH;
        switch(anchorH)
        {
            case 0: spaceH = posX; break;
            case 1: spaceH = parent.width - posX - width; break;
        }

        anchorV = sideV;
        switch(anchorV)
        {
            case 0: spaceV = posY; break;
            case 1: spaceV = parent.height - posY - height; break;
        }
        text = Text;
        tooltip = Tooltip;
        toggledState = state;
    }

    @Override
    public void draw(int mouseX, int mouseY, boolean hover)
    {
        RendererHelper.drawColourOnScreen(Theme.instance.elementButtonBorder[0], Theme.instance.elementButtonBorder[1], Theme.instance.elementButtonBorder[2], 255, getPosX(), getPosY(), width, height, 0);
        if(hover)
        {
            if(Mouse.isButtonDown(0))
            {
                RendererHelper.drawColourOnScreen(Theme.instance.elementButtonClick[0], Theme.instance.elementButtonClick[1], Theme.instance.elementButtonClick[2], 255, getPosX() + 1, getPosY() + 1, width - 2, height - 2, 0);
            }
            else
            {
                RendererHelper.drawColourOnScreen(Theme.instance.elementButtonBackgroundHover[0], Theme.instance.elementButtonBackgroundHover[1], Theme.instance.elementButtonBackgroundHover[2], 255, getPosX() + 1, getPosY() + 1, width - 2, height - 2, 0);
            }
        }
        else
        {
            if(toggledState)
            {
                RendererHelper.drawColourOnScreen(Theme.instance.elementButtonToggle[0], Theme.instance.elementButtonToggle[1], Theme.instance.elementButtonToggle[2], 255, getPosX() + 1, getPosY() + 1, width - 2, height - 2, 0);
            }
            else
            {
                RendererHelper.drawColourOnScreen(Theme.instance.elementButtonBackgroundInactive[0], Theme.instance.elementButtonBackgroundInactive[1], Theme.instance.elementButtonBackgroundInactive[2], 255, getPosX() + 1, getPosY() + 1, width - 2, height - 2, 0);
            }
        }
        parent.workspace.getFontRenderer().drawString(StatCollector.translateToLocal(text), getPosX() + (width / 2) - (parent.workspace.getFontRenderer().getStringWidth(StatCollector.translateToLocal(text)) / 2), getPosY() + height - (height / 2) - (parent.workspace.getFontRenderer().FONT_HEIGHT / 2), Theme.getAsHex(!toggledState ? Theme.instance.elementButtonToggleHover : Theme.instance.font), false);
    }

    @Override
    public boolean onClick(int mouseX, int mouseY, int id)
    {
        toggledState = !toggledState;
        parent.elementTriggered(this);
        return true;
    }

    @Override
    public void resized()
    {
        switch(anchorH)
        {
            case 0: posX = spaceH; break;
            case 1: posX = parent.width - spaceH - width; break;
            case 2: posX = (parent.width / 2) - (width / 2); break;
        }
        switch(anchorV)
        {
            case 0: posY = spaceV; break;
            case 1: posY = parent.height - spaceV - height; break;
            case 2: posY = (parent.height / 2) - (height / 2); break;
        }
    }

    public String tooltip()
    {
        return tooltip; //return null for no tooltip. This is localized.
    }
}
