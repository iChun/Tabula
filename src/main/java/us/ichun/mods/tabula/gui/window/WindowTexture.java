package us.ichun.mods.tabula.gui.window;

import net.minecraft.util.ResourceLocation;
import us.ichun.mods.tabula.gui.GuiWorkspace;
import us.ichun.mods.tabula.gui.window.element.Element;
import us.ichun.mods.tabula.gui.window.element.ElementButtonTextured;
import us.ichun.mods.tabula.gui.window.element.ElementToggle;

import java.io.File;

public class WindowTexture extends Window
{
    public int listenTime;
    public File textureFile;
    public boolean textureRemote;//received from server, if not,

    //TODO buffered image id + buffered image

    public WindowTexture(GuiWorkspace parent, int x, int y, int w, int h, int minW, int minH)
    {
        super(parent, x, y, w, h, minW, minH, "window.texture.title", true);

        elements.add(new ElementToggle(this, width - BORDER_SIZE - 100, height - BORDER_SIZE - 20, 60, 20, 0, false, 1, 1, "window.texture.listenTexture", "window.texture.listenTextureFull", true));
        elements.add(new ElementButtonTextured(this, width - BORDER_SIZE - 40, height - BORDER_SIZE - 20, 1, false, 1, 1, "window.texture.loadTexture", new ResourceLocation("tabula", "textures/icon/newTexture.png")));
        elements.add(new ElementButtonTextured(this, width - BORDER_SIZE - 20, height - BORDER_SIZE - 20, 2, false, 1, 1, "window.texture.clearTexture", new ResourceLocation("tabula", "textures/icon/clearTexture.png")));
    }

    @Override
    public void draw(int mouseX, int mouseY) //4 pixel border?
    {
        super.draw(mouseX, mouseY);
    }

    @Override
    public boolean interactableWhileNoProjects()
    {
        return false;
    }

    @Override
    public void update()
    {
        super.update();
        listenTime++;
        if(listenTime > 20)
        {
            listenTime = 0;
            if(!textureRemote && textureFile != null)
            {
                //TODO texture listening
            }
        }
    }
}
