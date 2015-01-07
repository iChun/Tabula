package us.ichun.mods.tabula.client.gui.window.element;

import net.minecraft.client.renderer.GlStateManager;
import us.ichun.mods.ichunutil.client.render.RendererHelper;
import us.ichun.mods.tabula.client.gui.Theme;
import us.ichun.mods.tabula.client.gui.window.Window;
import us.ichun.mods.tabula.client.gui.window.WindowAnimate;

public class ElementMinimize extends Element
{
    public ElementMinimize(Window window, int x, int y, int w, int h, int id)
    {
        super(window, x, y, w, h, id, true);
    }

    @Override
    public void draw(int mouseX, int mouseY, boolean hover)
    {
        if(parent.docked >= 0 && parent.docked != 2)
        {
            return;
        }
        if(hover)
        {
            RendererHelper.drawColourOnScreen(Theme.getAsHex(Theme.instance.font), 255, getPosX() - 0.5D, getPosY(), width, 1, 0);
            RendererHelper.drawColourOnScreen(Theme.getAsHex(Theme.instance.font), 255, getPosX() - 0.5D, getPosY(), 1, height, 0);
            RendererHelper.drawColourOnScreen(Theme.getAsHex(Theme.instance.font), 255, getPosX() - 0.5D, getPosY() + height - 1, width, 1, 0);
            RendererHelper.drawColourOnScreen(Theme.getAsHex(Theme.instance.font), 255, getPosX() + width - 1  - 0.5D, getPosY(), 1, height, 0);
        }
        GlStateManager.pushMatrix();
        float scale = 2F;
        GlStateManager.scale(scale, scale, scale);
        if(parent.minimized && !(parent instanceof WindowAnimate) || !parent.minimized && parent instanceof WindowAnimate)
        {
            parent.workspace.getFontRenderer().drawString("\u25BC", (int)((float)(getPosX() + 2) / scale), (int)((float)(getPosY() - 2) / scale), Theme.getAsHex(Theme.instance.font), false); //down arrow
        }
        else
        {
            parent.workspace.getFontRenderer().drawString("\u25B2", (int)((float)(getPosX() + 2) / scale), (int)((float)(getPosY() - 2) / scale), Theme.getAsHex(Theme.instance.font), false); //up arrow
        }
        GlStateManager.popMatrix();
    }

    @Override
    public void resized()
    {
        posX = parent.width - 13;
        posY = 2;
    }

    @Override
    public String tooltip()
    {
        if(parent.docked < 0 || parent.docked == 2)
        {
            if(!parent.minimized)
            {
                return "element.minimize";
            }
            else
            {
                return "element.expand";
            }
        }
        return null;
    }

    @Override
    public boolean onClick(int mouseX, int mouseY, int id)
    {
        if(id == 0 && (parent.docked < 0 || parent.docked == 2))
        {
            parent.toggleMinimize();
        }
        return true;
    }
}
