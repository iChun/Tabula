package us.ichun.mods.tabula.client.gui.window.element;

import ichun.client.render.RendererHelper;
import net.minecraft.util.MathHelper;
import org.lwjgl.opengl.GL11;
import us.ichun.mods.tabula.client.gui.Theme;
import us.ichun.mods.tabula.client.gui.window.Window;

import java.util.ArrayList;
import java.util.List;

public class ElementTextWrapper extends Element
{
    public ArrayList<String> text = new ArrayList<String>();

    public double sliderProg;

    public boolean showBorder;

    public int spacerL;
    public int spacerR;
    public int spacerU;
    public int spacerD;

    public ElementTextWrapper(Window window, int x, int y, int w, int h, int ID, boolean igMin, boolean border)
    {
        super(window, x, y, w, h, ID, igMin);

        showBorder = border;

        spacerL = x;
        spacerR = parent.width - x - width;
        spacerU = y;
        spacerD = parent.height - y - height;
    }

    @Override
    public void draw(int mouseX, int mouseY, boolean hover)
    {
        RendererHelper.endGlScissor();
        RendererHelper.startGlScissor(getPosX(), getPosY(), width, height);

        GL11.glPushMatrix();
        int lineHeight = 0;

        for(int i = 0; i < text.size(); i++)
        {
            String msg = text.get(i);

            List list = parent.workspace.getFontRenderer().listFormattedStringToWidth(msg, width - 12);

            for(int j = 0; j < list.size(); j++)
            {
                lineHeight += parent.workspace.getFontRenderer().FONT_HEIGHT + 2;
            }
        }
        lineHeight += 4;

        if(lineHeight > height)
        {
            GL11.glTranslatef(0F, (float)((height - lineHeight) * sliderProg), 0F);
        }

        drawText(mouseX, mouseY, hover);

        GL11.glPopMatrix();

        RendererHelper.endGlScissor();
        if(parent.isTab)
        {
            RendererHelper.startGlScissor(parent.posX + 1, parent.posY + 1 + 12, parent.getWidth() - 2, parent.getHeight() - 2 - 12);
        }
        else
        {
            RendererHelper.startGlScissor(parent.posX + 1, parent.posY + 1, parent.getWidth() - 2, parent.getHeight() - 2);
        }

        if(showBorder)
        {
            int x1 = getPosX();
            int x2 = getPosX() + width;
            int y1 = getPosY();
            int y2 = getPosY() + height;

            RendererHelper.drawColourOnScreen(Theme.elementTreeBorder[0], Theme.elementTreeBorder[1], Theme.elementTreeBorder[2], 255, x1, y1, width, 1, 0);
            RendererHelper.drawColourOnScreen(Theme.elementTreeBorder[0], Theme.elementTreeBorder[1], Theme.elementTreeBorder[2], 255, x1, y1, 1, height, 0);
            RendererHelper.drawColourOnScreen(Theme.elementTreeBorder[0], Theme.elementTreeBorder[1], Theme.elementTreeBorder[2], 255, x1, y2- 1, width, 1, 0);
            RendererHelper.drawColourOnScreen(Theme.elementTreeBorder[0], Theme.elementTreeBorder[1], Theme.elementTreeBorder[2], 255, x2 - 1, y1, 1, height, 0);
        }
    }

    public void drawText(int mouseX, int mouseY, boolean hover)
    {
        for(int i = 0; i < text.size(); i++)
        {
            String msg = text.get(i);

            List list = parent.workspace.getFontRenderer().listFormattedStringToWidth(msg, width - 12);

            for(int j = 0; j < list.size(); j++)
            {
                if(j == 0)
                {
                    parent.workspace.getFontRenderer().drawString((String)list.get(j), getPosX() + 4, getPosY() + 4, Theme.getAsHex(Theme.font), false);
                }
                else
                {
                    parent.workspace.getFontRenderer().drawString((String)list.get(j), getPosX() + 12, getPosY() + 4, Theme.getAsHex(Theme.font), false);
                }
                GL11.glTranslatef(0F, parent.workspace.getFontRenderer().FONT_HEIGHT + 2, 0F);
            }
        }
    }

    @Override
    public void resized()
    {
        posX = spacerL;
        width = parent.width - posX - spacerR;
        posY = spacerU;
        height = parent.height - posY - spacerD;
    }

    @Override
    public boolean mouseScroll(int mouseX, int mouseY, int k)
    {
        if(!text.isEmpty())
        {
            sliderProg += (0.1D * MathHelper.clamp_double((height / (parent.workspace.getFontRenderer().FONT_HEIGHT + 2)) / text.size(), 1.0D, 0.0D)) * -k;
            sliderProg = MathHelper.clamp_double(sliderProg, 0.0D, 1.0D);
        }
        return false;//return true to say you're interacted with
    }

}
