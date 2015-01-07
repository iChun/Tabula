package us.ichun.mods.tabula.client.gui.window;

import us.ichun.mods.ichunutil.common.module.tabula.client.model.ModelInfo;
import us.ichun.mods.ichunutil.common.module.tabula.client.model.ModelList;
import us.ichun.mods.ichunutil.common.module.tabula.common.project.ProjectInfo;
import us.ichun.mods.tabula.client.gui.GuiWorkspace;
import us.ichun.mods.tabula.client.gui.window.element.Element;
import us.ichun.mods.tabula.client.gui.window.element.ElementButton;
import us.ichun.mods.tabula.client.gui.window.element.ElementListTree;
import us.ichun.mods.tabula.client.gui.window.element.ElementToggle;
import us.ichun.mods.tabula.client.mainframe.core.ProjectHelper;
import us.ichun.mods.tabula.common.Tabula;

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
        if(element.id == 1 || element.id == 3)
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
                    ProjectInfo proj;
                    if(workspace.projectManager.selectedProject == -1 || newProj)
                    {
                        proj = new ProjectInfo(((ModelInfo)tree.attachedObject).modelParent.getClass().getSimpleName(), "Either Mojang or a mod author");
                        proj.projVersion = ProjectInfo.PROJ_VERSION;
                        proj.identifier = "";
                        workspace.openNextNewProject = true;
                    }
                    else
                    {
                        proj = workspace.projectManager.projects.get(workspace.projectManager.selectedProject);
                    }

                    proj.importModel(((ModelInfo)tree.attachedObject), texture);

                    if(!workspace.remoteSession)
                    {
                        Tabula.proxy.tickHandlerClient.mainframe.overrideProject(proj.identifier, proj.getAsJson(), proj.bufferedTexture);
                    }
                    else if(!workspace.sessionEnded && workspace.isEditor)
                    {
                        ProjectHelper.sendProjectToServer(workspace.host, proj.identifier, proj, false);
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
