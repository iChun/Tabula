package us.ichun.mods.tabula.client.gui.window;

import net.minecraft.util.StatCollector;
import us.ichun.mods.tabula.client.core.ResourceHelper;
import us.ichun.mods.tabula.client.gui.GuiWorkspace;
import us.ichun.mods.tabula.client.gui.Theme;
import us.ichun.mods.tabula.client.gui.window.element.Element;
import us.ichun.mods.tabula.client.gui.window.element.ElementButton;
import us.ichun.mods.tabula.client.gui.window.element.ElementListTree;
import us.ichun.mods.tabula.common.Tabula;
import us.ichun.module.tabula.client.formats.ImportList;
import us.ichun.module.tabula.common.project.ProjectInfo;

import java.io.File;
import java.util.ArrayList;

public class WindowOpenProject extends Window
{
    public ElementListTree modelList;

    public File openingFile;
    public String openingJson;

    public WindowOpenProject(GuiWorkspace parent, int x, int y, int w, int h, int minW, int minH)
    {
        super(parent, x, y, w, h, minW, minH, "window.open.title", true);

        elements.add(new ElementButton(this, width - 140, height - 22, 60, 16, 1, false, 1, 1, "element.button.ok"));
        elements.add(new ElementButton(this, width - 70, height - 22, 60, 16, 0, false, 1, 1, "element.button.cancel"));
        modelList = new ElementListTree(this, BORDER_SIZE + 1, BORDER_SIZE + 1 + 10, width - (BORDER_SIZE * 2 + 2), height - BORDER_SIZE - 22 - 16, 3, false, false);
        elements.add(modelList);

        ArrayList<File> files = new ArrayList<File>();

        File[] textures = ResourceHelper.getSaveDir().listFiles();

        for(File file : textures)
        {
            if(!file.isDirectory() && ImportList.isFileSupported(file))
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
    public void draw(int mouseX, int mouseY) //4 pixel border?
    {
        super.draw(mouseX, mouseY);
        if(!minimized && openingFile != null)
        {
            workspace.getFontRenderer().drawString(StatCollector.translateToLocal("window.open.opening"), posX + 11, posY + height - 18, Theme.getAsHex(Theme.instance.font), false);
        }
    }

    @Override
    public void elementTriggered(Element element)
    {
        if(element.id == 0)
        {
            workspace.removeWindow(this, true);
        }
        if((element.id == 1 || element.id == 3) && openingFile == null)
        {
            for(int i = 0; i < modelList.trees.size(); i++)
            {
                ElementListTree.Tree tree = modelList.trees.get(i);
                if(tree.selected)
                {
                    if(workspace.windowDragged == this)
                    {
                        workspace.windowDragged = null;
                    }
                    ProjectInfo project = ImportList.createProjectFromFile((File)tree.attachedObject);
                    if(project == null)
                    {
                        workspace.addWindowOnTop(new WindowPopup(workspace, 0, 0, 180, 80, 180, 80, "window.open.failed").putInMiddleOfScreen());
                    }
                    else
                    {
                        project.repair();

                        openingFile = (File)tree.attachedObject;
                        openingJson = project.getAsJson();
                        if(workspace.remoteSession)
                        {
                            //TODO this
                        }
                        else
                        {
                            Tabula.proxy.tickHandlerClient.mainframe.openProject(openingJson, project.bufferedTexture);
                        }
                    }
                    break;
                }
            }
        }
    }
}
