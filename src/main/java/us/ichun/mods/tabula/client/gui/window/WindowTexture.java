package us.ichun.mods.tabula.client.gui.window;

import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.texture.TextureUtil;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.StatCollector;
import org.lwjgl.opengl.GL11;
import us.ichun.mods.ichunutil.client.gui.window.IWorkspace;
import us.ichun.mods.ichunutil.client.gui.window.Window;
import us.ichun.mods.ichunutil.client.gui.window.element.Element;
import us.ichun.mods.ichunutil.client.gui.window.element.ElementButtonTextured;
import us.ichun.mods.ichunutil.client.gui.window.element.ElementListTree;
import us.ichun.mods.ichunutil.client.gui.window.element.ElementToggle;
import us.ichun.mods.ichunutil.client.render.RendererHelper;
import us.ichun.mods.ichunutil.common.core.util.IOUtil;
import us.ichun.mods.ichunutil.common.module.tabula.common.project.ProjectInfo;
import us.ichun.mods.ichunutil.common.module.tabula.common.project.components.CubeInfo;
import us.ichun.mods.tabula.client.gui.GuiWorkspace;
import us.ichun.mods.tabula.client.gui.Theme;
import us.ichun.mods.tabula.client.mainframe.core.ProjectHelper;
import us.ichun.mods.tabula.common.Tabula;
import us.ichun.mods.tabula.common.packet.PacketClearTexture;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;

public class WindowTexture extends Window
{
    public int listenTime;

    public BufferedImage image;
    public int imageId = -1;

    public WindowTexture(IWorkspace parent, int x, int y, int w, int h, int minW, int minH)
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
        if(!((GuiWorkspace)workspace).projectManager.projects.isEmpty())
        {
            ProjectInfo project = ((GuiWorkspace)workspace).projectManager.projects.get(((GuiWorkspace)workspace).projectManager.selectedProject);
            double w = width - (BORDER_SIZE * 2);
            double h = height - BORDER_SIZE - 22 - 15 - 12;
            double rW = w / (double)project.textureWidth;
            double rH = h / (double)project.textureHeight;

            double max = Math.min(rW, rH);
            double offX = (w - (project.textureWidth * max)) / 2D;
            double offY = (h - (project.textureHeight * max)) / 2D;

            double pX = posX + BORDER_SIZE + offX;
            double pY = posY + offY + 15;
            double w1 = (project.textureWidth * max);
            double h1 = (project.textureHeight * max);

            RendererHelper.drawColourOnScreen(200, 200, 200, 255, pX, pY, w1, h1, 0D);

            if(project.bufferedTexture != image)
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
                GlStateManager.bindTexture(imageId);
                Tessellator tessellator = Tessellator.getInstance();
                WorldRenderer worldRenderer = tessellator.getWorldRenderer();
                worldRenderer.startDrawingQuads();
                worldRenderer.addVertexWithUV(pX		, pY + h1	, 0, 0.0D, 1.0D);
                worldRenderer.addVertexWithUV(pX + w1, pY + h1	, 0, 1.0D, 1.0D);
                worldRenderer.addVertexWithUV(pX + w1, pY			, 0, 1.0D, 0.0D);
                worldRenderer.addVertexWithUV(pX		, pY			, 0, 0.0D, 0.0D);
                tessellator.draw();
            }

            RendererHelper.endGlScissor();
            RendererHelper.startGlScissor((int)pX, (int)pY, (int)w1, (int)h1);

            GlStateManager.enableBlend();
            GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);

            GlStateManager.alphaFunc(GL11.GL_GREATER, 0.00625F);

            for(ElementListTree.Tree tree : ((GuiWorkspace)workspace).windowModelTree.modelList.trees)
            {
                if(tree.attachedObject instanceof CubeInfo)
                {
                    CubeInfo info = (CubeInfo)tree.attachedObject;
                    int alpha = tree.selected ? 125 : 25;
                    double ratio = (project.textureWidth / w1);
                    RendererHelper.drawColourOnScreen(255, 0, 0, alpha, pX + info.txOffset[0] / ratio, pY + info.txOffset[1] / ratio + info.dimensions[2] / ratio                                                                                               , info.dimensions[2] / ratio, info.dimensions[1] / ratio, 0D);
                    RendererHelper.drawColourOnScreen(0, 0, 255, alpha, pX + info.txOffset[0] / ratio + info.dimensions[2] / ratio, pY + info.txOffset[1] / ratio + info.dimensions[2] / ratio                                                                  , info.dimensions[0] / ratio, info.dimensions[1] / ratio, 0D);
                    RendererHelper.drawColourOnScreen(170, 0, 0, alpha, pX + info.txOffset[0] / ratio + info.dimensions[2] / ratio + info.dimensions[0] / ratio, pY + info.txOffset[1] / ratio + info.dimensions[2] / ratio                                     , info.dimensions[2] / ratio, info.dimensions[1] / ratio, 0D);
                    RendererHelper.drawColourOnScreen(0, 0, 170, alpha, pX + info.txOffset[0] / ratio + info.dimensions[2] / ratio + info.dimensions[0] / ratio  + info.dimensions[2] / ratio, pY + info.txOffset[1] / ratio + info.dimensions[2] / ratio       , info.dimensions[0] / ratio, info.dimensions[1] / ratio, 0D);
                    RendererHelper.drawColourOnScreen(0, 255, 0, alpha, pX + info.txOffset[0] / ratio + info.dimensions[2] / ratio, pY + info.txOffset[1] / ratio                                                                                               , info.dimensions[0] / ratio, info.dimensions[2] / ratio, 0D);
                    RendererHelper.drawColourOnScreen(0, 170, 0, alpha, pX + info.txOffset[0] / ratio + info.dimensions[2] / ratio + info.dimensions[0] / ratio, pY + info.txOffset[1] / ratio                                                                  , info.dimensions[0] / ratio, info.dimensions[2] / ratio, 0D);
                }
            }

            GlStateManager.alphaFunc(GL11.GL_GREATER, 0.1F);
            GlStateManager.disableBlend();

            RendererHelper.endGlScissor();
            if(this.isTab)
            {
                RendererHelper.startGlScissor(this.posX + 1, this.posY + 1 + 12, this.getWidth() - 2, this.getHeight() - 2 - 12);
            }
            else
            {
                RendererHelper.startGlScissor(this.posX + 1, this.posY + 1, this.getWidth() - 2, this.getHeight() - 2);
            }

            if(imageId == -1)
            {
                workspace.getFontRenderer().drawString(StatCollector.translateToLocal("window.texture.noTexture"), posX + 4, posY + height - BORDER_SIZE - 12 - 20, Theme.getAsHex(workspace.currentTheme.font), false);
            }
            else if(project.textureFile != null)
            {
                workspace.getFontRenderer().drawString(project.textureFile.getName(), posX + 4, posY + height - BORDER_SIZE - 12 - 20, Theme.getAsHex(workspace.currentTheme.font), false);
            }
            else
            {
                workspace.getFontRenderer().drawString(StatCollector.translateToLocal("window.texture.remoteTexture"), posX + 4, posY + height - BORDER_SIZE - 12 - 20, Theme.getAsHex(workspace.currentTheme.font), false);
            }
        }
    }

    @Override
    public void shutdown()
    {
        if(imageId != -1)
        {
            TextureUtil.deleteTexture(imageId);
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
        if(!((GuiWorkspace)workspace).projectManager.projects.isEmpty())
        {
            ProjectInfo info = ((GuiWorkspace)workspace).projectManager.projects.get(((GuiWorkspace)workspace).projectManager.selectedProject);

            listenTime++;
            if(listenTime > 20)
            {
                listenTime = 0;
                boolean shouldListen = false;
                for(Element e : elements)
                {
                    if(e.id == 0)
                    {
                        shouldListen = ((ElementToggle)e).toggledState;
                    }
                }
                if(shouldListen && info.textureFile != null && info.textureFile.exists())
                {
                    String md5 = IOUtil.getMD5Checksum(info.textureFile);
                    if(md5 != null && !md5.equals(info.textureFileMd5))
                    {
                        info.ignoreNextImage = true;
                        info.textureFileMd5 = md5;

                        BufferedImage image = null;
                        try
                        {
                            image = ImageIO.read(info.textureFile);
                        }
                        catch(IOException e)
                        {
                        }

                        if(!((GuiWorkspace)workspace).remoteSession)
                        {
                            Tabula.proxy.tickHandlerClient.mainframe.loadTexture(info.identifier, image, false);
                        }
                        else if(!((GuiWorkspace)workspace).sessionEnded && ((GuiWorkspace)workspace).isEditor)
                        {
                            ProjectHelper.sendTextureToServer(((GuiWorkspace)workspace).host, info.identifier, false, image);
                        }
                    }
                }
            }
        }
    }

    @Override
    public void elementTriggered(Element element)
    {
        if(element.id == 1)
        {
            workspace.addWindowOnTop(new WindowLoadTexture(workspace, workspace.width / 2 - 130, workspace.height / 2 - 160, 260, 320, 240, 160));
        }
        if(element.id == 2)
        {
            if(!((GuiWorkspace)workspace).projectManager.projects.isEmpty())
            {
                ProjectInfo info = ((GuiWorkspace)workspace).projectManager.projects.get(((GuiWorkspace)workspace).projectManager.selectedProject);
                if(info.bufferedTexture != null)
                {
                    if(!((GuiWorkspace)workspace).remoteSession)
                    {
                        Tabula.proxy.tickHandlerClient.mainframe.clearTexture(info.identifier);
                    }
                    else if(!((GuiWorkspace)workspace).sessionEnded && ((GuiWorkspace)workspace).isEditor)
                    {
                        Tabula.channel.sendToServer(new PacketClearTexture(((GuiWorkspace)workspace).host, info.identifier));
                    }
                }
            }
        }
    }
}
