package us.ichun.mods.tabula.client.gui.window.element;

import ichun.client.render.RendererHelper;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.util.MathHelper;
import org.lwjgl.input.Mouse;
import us.ichun.mods.tabula.client.gui.Theme;
import us.ichun.mods.tabula.client.gui.window.Window;

public class ElementHoriSlider extends Element
{
    public double prevSliderProg;
    public double sliderProg;
    public int spacerL;
    public int spacerR;
    public String tooltip;

    public ElementHoriSlider(Window window, int x, int y, int w, int ID, boolean igMin, String title)
    {
        super(window, x, y, w, 12, ID, igMin);

        sliderProg = 1.0D;
        spacerL = x;
        spacerR = parent.width - x - width;
        tooltip = title;
    }

    @Override
    public void draw(int mouseX, int mouseY, boolean hover)
    {
        double x1 = getPosX() + 4;
        double x2 = x1 + width - 8;
        double y1 = getPosY() + 5;
        double y2 = y1 + height - 10;
        RendererHelper.drawColourOnScreen(Theme.instance.elementTreeBorder[0], Theme.instance.elementTreeBorder[1], Theme.instance.elementTreeBorder[2], 255, x1, y1, (x2 - x1), (y2 - y1), 0);

        RendererHelper.drawColourOnScreen(Theme.instance.elementTreeBorder[0], Theme.instance.elementTreeBorder[1], Theme.instance.elementTreeBorder[2], 255, getPosX() + (x2 - x1) * sliderProg, getPosY(), 8, height, 0);
        RendererHelper.drawColourOnScreen(Theme.instance.elementTreeScrollBar[0], Theme.instance.elementTreeScrollBar[1], Theme.instance.elementTreeScrollBar[2], 255, getPosX() + (x2 - x1) * sliderProg + 1, getPosY() + 1, 6, height - 2, 0);

        if(parent.workspace.elementDragged == this && Mouse.isButtonDown(0) && mouseX >= posX && mouseX <= posX + width && mouseY >= posY && mouseY <= posY + height)
        {
            double sx1 = posX + 4;
            double sx2 = posX + width - 8;
            sliderProg = MathHelper.clamp_double((double)(mouseX - sx1) / (double)(sx2 - sx1), 0.0D, 1.0D);

            if(sliderProg != prevSliderProg)
            {
                parent.elementTriggered(this);
            }
        }
        prevSliderProg = sliderProg;
    }

    @Override
    public boolean mouseScroll(int mouseX, int mouseY, int k)
    {
        sliderProg = MathHelper.clamp_double(sliderProg + (GuiScreen.isShiftKeyDown() ? k * 10 : k) * 0.001D, 0.0D, 1.0D);
        if(sliderProg != prevSliderProg)
        {
            parent.elementTriggered(this);
        }
        return true;
    }

        @Override
    public boolean onClick(int mouseX, int mouseY, int id)
    {
        return id == 0;//return true for elements that has input eg typing
    }

    @Override
    public void resized()
    {
        posX = spacerL;
        width = parent.width - posX - spacerR;
    }

    @Override
    public String tooltip()
    {
        return tooltip; //return null for no tooltip. This is localized.
    }
}
