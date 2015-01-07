package us.ichun.mods.tabula.client.gui.window.element;

import org.lwjgl.input.Mouse;
import us.ichun.mods.ichunutil.client.render.RendererHelper;
import us.ichun.mods.tabula.client.gui.Theme;
import us.ichun.mods.tabula.client.gui.window.Window;

public class ElementCheckBox extends ElementToggle
{
    public ElementCheckBox(Window window, int x, int y, int ID, boolean igMin, int sideH, int sideV, String Tooltip, boolean state)
    {
        super(window, x, y, 9, 9, ID, igMin, sideH, sideV, "", Tooltip, state);
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
        if(toggledState)
        {
            parent.workspace.getFontRenderer().drawString("X", getPosX() + 2, getPosY() + height - (height / 2) - (parent.workspace.getFontRenderer().FONT_HEIGHT / 2), Theme.getAsHex(!toggledState ? Theme.instance.elementButtonToggleHover : Theme.instance.font), false);
        }
    }
}
