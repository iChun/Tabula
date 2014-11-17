package us.ichun.mods.tabula.gui.window;

import net.minecraft.util.StatCollector;
import us.ichun.mods.tabula.client.model.ModelInfo;
import us.ichun.mods.tabula.client.model.ModelList;
import us.ichun.mods.tabula.common.Tabula;
import us.ichun.mods.tabula.gui.GuiWorkspace;
import us.ichun.mods.tabula.gui.Theme;
import us.ichun.mods.tabula.gui.window.element.*;

public class WindowImport extends Window
{
    public ElementListTree modelList;

    public WindowImport(GuiWorkspace parent, int x, int y, int w, int h, int minW, int minH)
    {
        super(parent, x, y, w, h, minW, minH, "window.importMC.title", true);

        elements.add(new ElementButton(this, width - 140, height - 22, 60, 16, 1, false, 1, 1, "element.button.ok"));
        elements.add(new ElementButton(this, width - 70, height - 22, 60, 16, 0, false, 1, 1, "element.button.cancel"));
        elements.add(new ElementToggle(this, 8, height - 22, 60, 16, 2, false, 0, 1, "window.import.texture", "window.import.textureFull", true));
        elements.add(new ElementToggle(this, 74, height - 22, 60, 16, 4, false, 0, 1, "window.importMC.newProject", "window.importMC.newProjectFull", true));
        modelList = new ElementListTree(this, BORDER_SIZE + 1, BORDER_SIZE + 1 + 10, width - (BORDER_SIZE * 2 + 2), height - BORDER_SIZE - 22 - 16, 3, false, false);
        elements.add(modelList);

        for(ModelInfo model : ModelList.models)
        {
            modelList.createTree(null, model, 13, 0, false, false);
        }
    }

    @Override
    public void draw(int mouseX, int mouseY)
    {
        super.draw(mouseX, mouseY);
    }

    @Override
    public void elementTriggered(Element element)
    {
        if(element.id == 0)
        {
            workspace.removeWindow(this, true);
        }
        if(element.id == 1)
        {
            boolean found = false;

            boolean texture = false;
            boolean newProj = false;
            for(Element e : elements)
            {
                if(e.id == 2)
                {
                    texture = ((ElementToggle)e).toggledState;
                }
                else if(e.id == 4)
                {
                    newProj = ((ElementToggle)e).toggledState;
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
                        Tabula.proxy.tickHandlerClient.mainframe.importModel(workspace.projectManager.selectedProject == -1 || newProj ? "" : workspace.projectManager.projects.get(workspace.projectManager.selectedProject).identifier, (ModelInfo)tree.attachedObject, texture);
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
