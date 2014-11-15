package us.ichun.mods.tabula.gui.window;

import net.minecraft.util.ResourceLocation;
import us.ichun.mods.tabula.gui.GuiWorkspace;
import us.ichun.mods.tabula.gui.window.element.Element;
import us.ichun.mods.tabula.gui.window.element.ElementButtonTextured;

public class WindowTopDock extends Window
{
    public static final int ID_NEW = 0;
    public static final int ID_OPEN = 1;
    public static final int ID_SAVE = 2;
    public static final int ID_SAVE_AS = 3;
    public static final int ID_IMPORT = 4;
    public static final int ID_EXPORT = 5;

    public WindowTopDock(GuiWorkspace parent, int x, int y, int w, int h, int minW, int minH)
    {
        super(parent, x, y, w, h, minW, minH, "", false);

        //ADD Elements
        elements.add(new ElementButtonTextured(this, 0, 0, ID_NEW, true, 0, "topdock.new", new ResourceLocation("tabula", "textures/icon/new.png")));
        elements.add(new ElementButtonTextured(this, 20, 0, ID_OPEN, true, 0, "topdock.open", new ResourceLocation("tabula", "textures/icon/open.png")));
        elements.add(new ElementButtonTextured(this, 40, 0, ID_SAVE, true, 0, "topdock.save", new ResourceLocation("tabula", "textures/icon/save.png")));
        elements.add(new ElementButtonTextured(this, 60, 0, ID_SAVE_AS, true, 0, "topdock.saveAs", new ResourceLocation("tabula", "textures/icon/saveAs.png")));
        elements.add(new ElementButtonTextured(this, 80, 0, ID_IMPORT, true, 0, "topdock.import", new ResourceLocation("tabula", "textures/icon/import.png")));
        elements.add(new ElementButtonTextured(this, 100, 0, ID_EXPORT, true, 0, "topdock.export", new ResourceLocation("tabula", "textures/icon/export.png")));
    }

    @Override
    public int clickedOnBorder(int mouseX, int mouseY, int id)//only left clicks
    {
        return 0;
    }

    @Override
    public boolean clickedOnTitle(int mouseX, int mouseY, int id)
    {
        return false;
    }

    @Override
    public void elementTriggered(Element element)
    {
    }

    @Override
    public void resized()
    {
        for(Element element : elements)
        {
            element.resized();
        }
        posX = 0;
        posY = 0;
        width = workspace.width;
        height = 20;
    }

    @Override
    public void toggleMinimize()
    {
    }

    @Override
    public int getHeight()
    {
        return 20;
    }

}
