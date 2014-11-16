package us.ichun.mods.tabula.gui.window;

import ichun.client.render.RendererHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.texture.TextureUtil;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;
import us.ichun.mods.tabula.common.Tabula;
import us.ichun.mods.tabula.common.project.ProjectInfo;
import us.ichun.mods.tabula.gui.GuiWorkspace;
import us.ichun.mods.tabula.gui.window.element.Element;
import us.ichun.mods.tabula.gui.window.element.ElementButtonTextured;
import us.ichun.mods.tabula.gui.window.element.ElementToggle;

import java.awt.image.BufferedImage;
import java.io.File;

public class WindowTexture extends Window
{
    public int listenTime;
    public File textureFile;
    public boolean textureRemote;//received from server, if not,

    public BufferedImage image;
    public int imageId = -1;
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
        if(!workspace.projectManager.projects.isEmpty())
        {
            ProjectInfo info = workspace.projectManager.projects.get(workspace.projectManager.selectedProject);
            double w = width - (BORDER_SIZE * 2);
            double h = height - BORDER_SIZE - 22 - 15;
            double rW = w / (double)info.textureWidth;
            double rH = h / (double)info.textureHeight;

            double max = Math.min(rW, rH);
            double offX = (w - (info.textureWidth * max)) / 2D;
            double offY = (h - (info.textureHeight * max)) / 2D;
            RendererHelper.drawColourOnScreen(200, 200, 200, 255, posX + BORDER_SIZE + offX, posY + offY + 15, (info.textureWidth * max), (info.textureHeight * max), 0D);

            ProjectInfo project = workspace.projectManager.projects.get(workspace.projectManager.selectedProject);
            if(project.bufferedTexture != image) //TODO try comparing image data to see if it's what you synced.
            {
                if(imageId != -1)
                {
                    TextureUtil.deleteTexture(imageId);
                    imageId = -1;
                }
                image = project.bufferedTexture;
                if(image != null)
                {
                    imageId = TextureUtil.uploadTextureImage(TextureUtil.glGenTextures(), image);
                }
            }

            if(image != null)
            {
                double pX = posX + BORDER_SIZE + offX;
                double pY = posY + offY + 15;
                double w1 = (info.textureWidth * max);
                double h1 = (info.textureHeight * max);

                GL11.glBindTexture(GL11.GL_TEXTURE_2D, imageId);
                Tessellator tessellator = Tessellator.instance;
                tessellator.startDrawingQuads();
                tessellator.addVertexWithUV(pX		, pY + h1	, 0, 0.0D, 1.0D);
                tessellator.addVertexWithUV(pX + w1, pY + h1	, 0, 1.0D, 1.0D);
                tessellator.addVertexWithUV(pX + w1, pY			, 0, 1.0D, 0.0D);
                tessellator.addVertexWithUV(pX		, pY			, 0, 0.0D, 0.0D);
                tessellator.draw();
                //TODO draw texture location overlay.
            }
        }
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

    @Override
    public void elementTriggered(Element element)
    {
        if(element.id == 2)
        {
            if(!workspace.projectManager.projects.isEmpty())
            {
                ProjectInfo info = workspace.projectManager.projects.get(workspace.projectManager.selectedProject);
                if(info.bufferedTexture != null)
                {
                    if(workspace.remoteSession)
                    {

                    }
                    else
                    {
                        Tabula.proxy.tickHandlerClient.mainframe.clearTexture(info.identifier);
                    }
                }
            }
        }
    }
}
