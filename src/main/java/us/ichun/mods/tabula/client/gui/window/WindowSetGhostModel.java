package us.ichun.mods.tabula.client.gui.window;

import us.ichun.mods.ichunutil.client.gui.window.IWorkspace;
import us.ichun.mods.ichunutil.client.gui.window.Window;
import us.ichun.mods.ichunutil.client.gui.window.WindowPopup;
import us.ichun.mods.ichunutil.client.gui.window.element.Element;
import us.ichun.mods.ichunutil.client.gui.window.element.ElementButton;
import us.ichun.mods.ichunutil.client.gui.window.element.ElementNumberInput;
import us.ichun.mods.ichunutil.client.gui.window.element.ElementToggle;
import us.ichun.mods.ichunutil.common.module.tabula.client.formats.ImportList;
import us.ichun.mods.ichunutil.common.module.tabula.common.project.ProjectInfo;
import us.ichun.mods.ichunutil.common.module.tabula.common.project.components.CubeInfo;
import us.ichun.mods.tabula.client.core.ResourceHelper;
import us.ichun.mods.tabula.client.gui.GuiWorkspace;
import us.ichun.mods.tabula.client.gui.window.element.ElementListTree;

import java.io.File;
import java.util.ArrayList;

public class WindowSetGhostModel extends Window
{
    public ElementListTree modelList;

    public WindowSetGhostModel(IWorkspace parent, int x, int y, int w, int h, int minW, int minH)
    {
        super(parent, x, y, w, h, minW, minH, "window.ghostModel.title", true);

        elements.add(new ElementButton(this, width - 140, height - 22, 60, 16, 1, false, 1, 1, "element.button.ok"));
        elements.add(new ElementButton(this, width - 70, height - 22, 60, 16, 0, false, 1, 1, "element.button.clear"));
        modelList = new ElementListTree(this, BORDER_SIZE + 1, BORDER_SIZE + 1 + 10, width - (BORDER_SIZE * 2 + 2), height - BORDER_SIZE - 22 - 16, 3, false, false);
        elements.add(modelList);
        elements.add(new ElementToggle(this, 10, height - 22, 60, 16, -1, false, 0, 1, "window.import.texture", "window.import.textureFull", true));
        elements.add(new ElementNumberInput(this, 75, height - 20, 40, 12, -1, "window.controls.opacity", 1, false, 0, 100, 20));

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
    public void elementTriggered(Element element)
    {
        if(element.id == 0)
        {
            if(((GuiWorkspace)workspace).hasOpenProject())
            {
                ((GuiWorkspace)workspace).getOpenProject().ghostModel = null;
                for(int i = 0; i < modelList.trees.size(); i++)
                {
                    ElementListTree.Tree tree = modelList.trees.get(i);
                    tree.selected = false;
                }
            }
        }
        if((element.id == 1 || element.id == 3))
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

                        if(((GuiWorkspace)workspace).hasOpenProject())
                        {
                            if(((GuiWorkspace)workspace).getOpenProject().ghostModel != null)
                            {
                                ((GuiWorkspace)workspace).getOpenProject().ghostModel.destroy();
                            }
                            ((GuiWorkspace)workspace).getOpenProject().ghostModel = project;
                            for(Element e : elements)
                            {
                                if(e instanceof ElementToggle)
                                {
                                    if(!((ElementToggle)e).toggledState)
                                    {
                                        project.bufferedTexture = null;
                                    }
                                }
                                else if(e instanceof ElementNumberInput)
                                {
                                    ArrayList<CubeInfo> cubes = project.getAllCubes();
                                    for(CubeInfo cube : cubes)
                                    {
                                        cube.opacity = Double.parseDouble(((ElementNumberInput)e).textFields.get(0).getText());
                                    }
                                }
                            }
                        }
                    }
                    break;
                }
            }

            workspace.removeWindow(this, true);
        }
    }
}
