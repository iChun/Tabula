package us.ichun.mods.tabula.gui.element;

import us.ichun.mods.tabula.gui.GuiWindow;

public abstract class Element
{
    public final GuiWindow parent;
    public int posX;
    public int posY;
    public int width;
    public int height;
    public int id;
    public boolean ignoreMinimized;

    public Element(GuiWindow window, int x, int y, int w, int h, int ID, boolean igMin)
    {
        parent = window;
        posX = x;
        posY = y;
        width = w;
        height = h;
        id = ID;
        ignoreMinimized = igMin;
    }

    public abstract void draw(int mouseX, int mouseY, boolean hover);

    public void onClick(int mouseX, int mouseY, int id)
    {
    }

    public void resized()
    {
    }

    public int getPosX()
    {
        return parent.posX + posX;
    }

    public int getPosY()
    {
        return parent.posY + posY;
    }
}
