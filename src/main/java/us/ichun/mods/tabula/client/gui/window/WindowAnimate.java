package us.ichun.mods.tabula.client.gui.window;

import us.ichun.mods.tabula.client.gui.GuiWorkspace;

public class WindowAnimate extends Window
{
    public WindowAnimate(GuiWorkspace parent, int x, int y, int w, int h, int minW, int minH)
    {
        super(parent, x, y, w, h, minW, minH, "window.animate.title", true);
    }

    @Override
    public void draw(int mouseX, int mouseY) //4 pixel border?
    {
        if(width <= 0 || height <= 0)
        {
            return;
        }
        super.draw(mouseX, mouseY);
    }

    @Override
    public boolean interactableWhileNoProjects()
    {
        return false;
    }
}
