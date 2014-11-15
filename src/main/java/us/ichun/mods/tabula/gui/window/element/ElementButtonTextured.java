package us.ichun.mods.tabula.gui.window.element;

import ichun.client.render.RendererHelper;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;
import us.ichun.mods.tabula.gui.window.Window;

public class ElementButtonTextured extends ElementButton
{
    public ResourceLocation txLocation;

    public ElementButtonTextured(Window window, int x, int y, int w, int h, int ID, boolean igMin, int side, String title, ResourceLocation loc)
    {
        super(window, x, y, w, h, ID, igMin, side, title);
        txLocation = loc;
    }

    public ElementButtonTextured(Window window, int x, int y, int ID, boolean igMin, int side, String title, ResourceLocation loc)
    {
        super(window, x, y, 20, 20, ID, igMin, side, title);
        txLocation = loc;
    }

    @Override
    public void draw(int mouseX, int mouseY, boolean hover)
    {
        super.draw(mouseX, mouseY, hover);
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        RendererHelper.drawTextureOnScreen(txLocation, getPosX() + 2, getPosY() + 2, width - 4, height - 4, 0);
    }

    @Override
    public String tooltip()
    {
        return text;
    }
}
