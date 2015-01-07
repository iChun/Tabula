package us.ichun.mods.tabula.client.gui.window.element;

import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.ResourceLocation;
import us.ichun.mods.ichunutil.client.render.RendererHelper;
import us.ichun.mods.tabula.client.gui.window.Window;

public class ElementButtonTextured extends ElementButton
{
    public ResourceLocation txLocation;

    public ElementButtonTextured(Window window, int x, int y, int w, int h, int ID, boolean igMin, int sideH, int sideV, String title, ResourceLocation loc)
    {
        super(window, x, y, w, h, ID, igMin, sideH, sideV, title);
        txLocation = loc;
    }

    public ElementButtonTextured(Window window, int x, int y, int ID, boolean igMin, int sideH, int sideV, String title, ResourceLocation loc)
    {
        super(window, x, y, 20, 20, ID, igMin, sideH, sideV, title);
        txLocation = loc;
    }

    @Override
    public void draw(int mouseX, int mouseY, boolean hover)
    {
        super.draw(mouseX, mouseY, hover);
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        RendererHelper.drawTextureOnScreen(txLocation, getPosX() + 2, getPosY() + 2, width - 4, height - 4, 0);
    }

    @Override
    public String tooltip()
    {
        return text;
    }
}
