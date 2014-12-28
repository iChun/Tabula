package us.ichun.mods.tabula.client.gui.window.element;

import us.ichun.mods.tabula.client.gui.window.Window;

public abstract class Element
{
    public final Window parent;
    public int posX;
    public int posY;
    public int width;
    public int height;
    public int id;
    public boolean ignoreMinimized;

    public Element(Window window, int x, int y, int w, int h, int ID, boolean igMin)
    {
        parent = window;
        posX = x;
        posY = y;
        width = w;
        height = h;
        id = ID;
        ignoreMinimized = igMin;
    }

    public void update(){}

    public abstract void draw(int mouseX, int mouseY, boolean hover);

    public boolean mouseInBoundary(int mouseX, int mouseY)
    {
        return mouseX >= this.posX && mouseX <= this.posX + this.width && mouseY >= this.posY && mouseY <= this.posY + this.height;
    }

    public boolean onClick(int mouseX, int mouseY, int id)
    {
        return false;//return true for elements that has input eg typing
    }

    public void selected(){}

    public void deselected(){}

    public void resized(){}

    public void triggerInput()
    {
        parent.elementTriggered(this);
    }

    public void keyInput(char c, int key){}

    public boolean mouseScroll(int mouseX, int mouseY, int k)
    {
        return false;//return true to say you're interacted with
    }

    public String tooltip()
    {
        return null; //return null for no tooltip. This is localized.
    }

    public final int getPosX()
    {
        return parent.posX + posX;
    }

    public final int getPosY()
    {
        return parent.posY + posY;
    }
}
