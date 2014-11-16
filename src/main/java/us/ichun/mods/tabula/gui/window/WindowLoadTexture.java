package us.ichun.mods.tabula.gui.window;

import ichun.common.core.util.MD5Checksum;
import javafx.scene.control.Tab;
import us.ichun.mods.tabula.client.core.ResourceHelper;
import us.ichun.mods.tabula.client.model.ModelInfo;
import us.ichun.mods.tabula.client.model.ModelList;
import us.ichun.mods.tabula.common.Tabula;
import us.ichun.mods.tabula.common.project.ProjectInfo;
import us.ichun.mods.tabula.gui.GuiWorkspace;
import us.ichun.mods.tabula.gui.window.element.Element;
import us.ichun.mods.tabula.gui.window.element.ElementButton;
import us.ichun.mods.tabula.gui.window.element.ElementListTree;
import us.ichun.mods.tabula.gui.window.element.ElementToggle;

import java.io.File;
import java.util.ArrayList;

public class WindowLoadTexture extends Window
{
    public ElementListTree modelList;

    public WindowLoadTexture(GuiWorkspace parent, int x, int y, int w, int h, int minW, int minH)
    {
        super(parent, x, y, w, h, minW, minH, "window.loadTexture.title", true);

        elements.add(new ElementButton(this, width - 140, height - 22, 60, 16, 1, false, 1, 1, "element.button.ok"));
        elements.add(new ElementButton(this, width - 70, height - 22, 60, 16, 0, false, 1, 1, "element.button.cancel"));
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

    public void elementTriggered(Element element)
    {
        if(element.id == 0)
        {
            workspace.removeWindow(this, true);
        }
        if(element.id == 1)
        {
            if(!workspace.projectManager.projects.isEmpty())
            {
                ProjectInfo info = workspace.projectManager.projects.get(workspace.projectManager.selectedProject);

                boolean found = false;

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

                            Tabula.proxy.tickHandlerClient.mainframe.loadTexture(info.identifier, info.textureFile);
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
