package us.ichun.mods.tabula.client.gui.window;

import ichun.common.core.util.MD5Checksum;
import us.ichun.mods.tabula.client.core.ResourceHelper;
import us.ichun.mods.tabula.client.gui.GuiWorkspace;
import us.ichun.mods.tabula.client.gui.window.element.Element;
import us.ichun.mods.tabula.client.gui.window.element.ElementButton;
import us.ichun.mods.tabula.client.gui.window.element.ElementListTree;
import us.ichun.mods.tabula.client.gui.window.element.ElementToggle;
import us.ichun.mods.tabula.common.Tabula;
import us.ichun.module.tabula.common.project.ProjectInfo;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

public class WindowLoadTexture extends Window
{
    public transient ElementListTree modelList;

    public WindowLoadTexture(GuiWorkspace parent, int x, int y, int w, int h, int minW, int minH)
    {
        super(parent, x, y, w, h, minW, minH, "window.loadTexture.title", true);

        elements.add(new ElementButton(this, width - 140, height - 22, 60, 16, 1, false, 1, 1, "element.button.ok"));
        elements.add(new ElementButton(this, width - 70, height - 22, 60, 16, 0, false, 1, 1, "element.button.cancel"));
        elements.add(new ElementToggle(this, 8, height - 22, 60, 16, 2, false, 0, 1, "window.loadTexture.updateTextureDimensions", "window.loadTexture.updateTextureDimensionsFull", true));
        modelList = new ElementListTree(this, BORDER_SIZE + 1, BORDER_SIZE + 1 + 10, width - (BORDER_SIZE * 2 + 2), height - BORDER_SIZE - 22 - 16, 3, false, false);
        elements.add(modelList);

        ArrayList<File> files = new ArrayList<File>();

        File[] textures = ResourceHelper.getTexturesDir().listFiles();

        for(File file : textures)
        {
            if(!file.isDirectory() && file.getName().endsWith(".png"))
            {
                files.add(file);
            }
        }

        for(File file : files)
        {
            modelList.createTree(null, file, 26, 0, false, false);
        }
    }

    @Override
    public void elementTriggered(Element element)
    {
        if(element.id == 0)
        {
            workspace.removeWindow(this, true);
        }
        if(element.id == 1 || element.id == 3)
        {
            if(!workspace.projectManager.projects.isEmpty())
            {
                ProjectInfo info = workspace.projectManager.projects.get(workspace.projectManager.selectedProject);

                boolean found = false;

                boolean texture = false;
                for(Element e : elements)
                {
                    if(e.id == 2)
                    {
                        texture = ((ElementToggle)e).toggledState;
                        break;
                    }
                }

                for(int i = 0; i < modelList.trees.size(); i++)
                {
                    ElementListTree.Tree tree = modelList.trees.get(i);
                    if(tree.selected)
                    {
                        if(workspace.remoteSession)
                        {
                            //TODO this
                        }
                        else
                        {
                            info.textureFile = (File)tree.attachedObject;
                            info.ignoreNextImage = true;
                            info.textureFileMd5 = MD5Checksum.getMD5Checksum(info.textureFile);
                            workspace.windowTexture.listenTime = 0;

                            BufferedImage image = null;
                            try
                            {
                                image = ImageIO.read(info.textureFile);
                            }
                            catch(IOException e)
                            {
                            }
                            Tabula.proxy.tickHandlerClient.mainframe.loadTexture(info.identifier, image, texture);
                        }
                        found = true;
                        break;
                    }
                }

                if(found)
                {
                    workspace.removeWindow(this, true);
                }
            }
        }
    }
}
