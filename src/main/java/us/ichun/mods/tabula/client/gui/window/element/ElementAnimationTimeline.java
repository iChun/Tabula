package us.ichun.mods.tabula.client.gui.window.element;

import ichun.client.render.RendererHelper;
import org.lwjgl.opengl.GL11;
import us.ichun.mods.tabula.client.gui.Theme;
import us.ichun.mods.tabula.client.gui.window.Window;

public class ElementAnimationTimeline extends Element
{

    public int tickWidth = 5;

    public ElementAnimationTimeline(Window window, int x, int y, int w, int h, int ID)
    {
        super(window, x, y, w, h, ID, false);
    }

    @Override
    public void draw(int mouseX, int mouseY, boolean hover)
    {
//        RendererHelper.drawColourOnScreen(Theme.instance.elementTreeBorder[0], Theme.instance.elementTreeBorder[1], Theme.instance.elementTreeBorder[2], 255, getPosX() + 1, getPosY() + 1, width - 2, height - 2, 0);
        RendererHelper.drawColourOnScreen(Theme.instance.elementTreeBorder[0], Theme.instance.elementTreeBorder[1], Theme.instance.elementTreeBorder[2], 255, getPosX() + 100, getPosY(), 1, height, 0);

        RendererHelper.startGlScissor(getPosX() + 101, getPosY() - 1, width - 101, height + 3);

        //Timeline
        RendererHelper.drawColourOnScreen(Theme.instance.elementTreeItemBorder[0], Theme.instance.elementTreeItemBorder[1], Theme.instance.elementTreeItemBorder[2], 255, getPosX() + 101, getPosY() + height - 19, width - 101, 20, 0);

        int tick = 0;
        int timeOffX = 0;
        while(getPosX() + 100 + timeOffX < parent.posX + parent.width) //TODO scroll stuff
        {
            if(tick % 5 == 0)
            {
                RendererHelper.drawColourOnScreen(Theme.instance.elementTreeScrollBarBorder[0], Theme.instance.elementTreeScrollBarBorder[1], Theme.instance.elementTreeScrollBarBorder[2], 255, getPosX() + 100 + timeOffX, getPosY() + height - 19, 2, 7, 0);
                GL11.glPushMatrix();
                float scale = 0.5F;
                GL11.glScalef(scale, scale, scale);
                parent.workspace.getFontRenderer().drawString(Integer.toString(tick), (int)((getPosX() + 103 + timeOffX) / scale), (int)((getPosY() + height - 17) / scale), Theme.getAsHex(Theme.instance.fontDim), false);
                GL11.glPopMatrix();
            }
            else
            {
                RendererHelper.drawColourOnScreen(Theme.instance.elementTreeScrollBarBorder[0], Theme.instance.elementTreeScrollBarBorder[1], Theme.instance.elementTreeScrollBarBorder[2], 255, getPosX() + 100 + timeOffX, getPosY() + height - 19, 2, 2, 0);
            }
            tick++;
            timeOffX += tickWidth;
        }

        if(parent.isTab)
        {
            RendererHelper.startGlScissor(parent.posX + 1, parent.posY + 1 + 12, parent.getWidth() - 2, parent.getHeight() - 2 - 12);
        }
        else
        {
            RendererHelper.startGlScissor(parent.posX + 1, parent.posY + 1, parent.getWidth() - 2, parent.getHeight() - 2);
        }
    }

    @Override
    public boolean mouseScroll(int mouseX, int mouseY, int k)
    {
        return false;
    }

        @Override
    public void resized()
    {
        posX = 101;
        width = parent.width - posX - 1;
        posY = parent.BORDER_SIZE + 10;
        height = parent.height - posY - 1;
    }
}
