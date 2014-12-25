package us.ichun.mods.tabula.client.gui.window;

import us.ichun.mods.tabula.client.gui.GuiWorkspace;

public class WindowAnimate extends Window
{
    public WindowAnimate(GuiWorkspace parent, int x, int y, int w, int h, int minW, int minH)
    {
        super(parent, x, y, w, h, minW, minH, "window.animate.title", true);
    }

    @Override
    public boolean interactableWhileNoProjects()
    {
        return false;
    }
}
