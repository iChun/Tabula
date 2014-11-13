package us.ichun.mods.tabula.gui.element;

import ichun.client.render.RendererHelper;
import org.lwjgl.opengl.GL11;
import us.ichun.mods.tabula.gui.GuiWindow;

public class ElementMinimize extends Element
{
    public ElementMinimize(GuiWindow window, int x, int y, int w, int h, int id)
    {
        super(window, x, y, w, h, id, true);
    }

    @Override
    public void draw(int mouseX, int mouseY, boolean hover)
    {
        if(hover)
        {
            RendererHelper.drawColourOnScreen(0xffffff, 255, getPosX() - 0.5D, getPosY(), width, 1, 0);
            RendererHelper.drawColourOnScreen(0xffffff, 255, getPosX() - 0.5D, getPosY(), 1, height, 0);
            RendererHelper.drawColourOnScreen(0xffffff, 255, getPosX() - 0.5D, getPosY() + height - 1, width, 1, 0);
            RendererHelper.drawColourOnScreen(0xffffff, 255, getPosX() + width - 1  - 0.5D, getPosY(), 1, height, 0);
        }
        GL11.glPushMatrix();
        float scale = 2F;
        GL11.glScalef(scale, scale, scale);
        if(parent.minimized)
        {
            parent.workspace.getFontRenderer().drawString("\u25BC", (int)((float)(getPosX() + 2) / scale), (int)((float)(getPosY() - 2) / scale), 0xffffff, false); //down arrow
        }
        else
        {
            parent.workspace.getFontRenderer().drawString("\u25B2", (int)((float)(getPosX() + 2) / scale), (int)((float)(getPosY() - 2) / scale), 0xffffff, false); //up arrow
        }
        GL11.glPopMatrix();
    }

    public void resized()
    {
        posX = parent.width - 13;
        posY = 2;
    }

    @Override
    public void onClick(int mouseX, int mouseY, int id)
    {
        if(id == 0)
        {
            parent.toggleMinimize();
        }
    }
}
