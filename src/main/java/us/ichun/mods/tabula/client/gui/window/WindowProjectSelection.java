package us.ichun.mods.tabula.client.gui.window;

import us.ichun.mods.ichunutil.client.gui.window.IWorkspace;
import us.ichun.mods.ichunutil.client.gui.window.element.Element;
import us.ichun.mods.ichunutil.client.render.RendererHelper;
import us.ichun.mods.ichunutil.common.module.tabula.common.project.ProjectInfo;
import us.ichun.mods.ichunutil.common.module.tabula.common.project.components.Animation;
import us.ichun.mods.ichunutil.common.module.tabula.common.project.components.CubeGroup;
import us.ichun.mods.ichunutil.common.module.tabula.common.project.components.CubeInfo;
import us.ichun.mods.tabula.client.gui.GuiWorkspace;
import us.ichun.mods.tabula.client.gui.window.element.ElementListTree;
import us.ichun.mods.tabula.client.gui.window.element.ElementProjectTab;
import us.ichun.mods.tabula.common.Tabula;
import us.ichun.mods.tabula.common.packet.PacketSetCurrentProject;

import java.util.ArrayList;

public class WindowProjectSelection extends WindowTopDock
{
    public ArrayList<ProjectInfo> projects = new ArrayList<ProjectInfo>();
    public int selectedProject;

    public WindowProjectSelection(IWorkspace parent, int x, int y, int w, int h, int minW, int minH)
    {
        super(parent, x, y, w, h, minW, minH);

        elements.clear();

        selectedProject = -1;
    }

    @Override
    public void elementTriggered(Element element)
    {
        selectedProject = element.id;
    }

    @Override
    public void draw(int mouseX, int mouseY) //4 pixel border?
    {
        if(projects.isEmpty() || width <= 0)
        {
            return;
        }
        super.draw(mouseX, mouseY);
        RendererHelper.drawColourOnScreen(workspace.currentTheme.tabSideInactive[0], workspace.currentTheme.tabSideInactive[1], workspace.currentTheme.tabSideInactive[2], 255, posX, posY, width, 1, 0);
    }

    @Override
    public void resized()
    {
        for(Element element : elements)
        {
            element.resized();
        }
        if(!workspace.levels.get(0).isEmpty())
        {
            posX = workspace.levels.get(0).get(0).width - 2;
        }
        else
        {
            posX = 0;
        }
        posY = workspace.TOP_DOCK_HEIGHT + 1;
        if(!workspace.levels.get(1).isEmpty())
        {
            width = workspace.width - posX - workspace.levels.get(1).get(0).width + 2;
        }
        else
        {
            width = workspace.width - posX;
        }
        height = 12;
    }

    @Override
    public void shutdown()
    {
        for(ProjectInfo project : projects)
        {
            project.destroy();
        }
    }

    public void removeProject(String ident)
    {
        for(int i = projects.size() - 1; i >= 0; i--)
        {
            ProjectInfo project = projects.get(i);
            if(project.identifier.equals(ident))
            {
                project.destroy();
                projects.remove(i);
                if(i == selectedProject || selectedProject == projects.size())
                {
                    selectedProject--;
                    if(selectedProject < 0 && !projects.isEmpty())
                    {
                        selectedProject = 0;
                    }
                }
                changeProject(selectedProject);
                break;
            }
        }

        ArrayList<Element> els = new ArrayList<Element>(elements);
        for(int i = projects.size() - 1; i >= 0; i--)
        {
            ProjectInfo project = projects.get(i);
            for(Element e : elements)
            {
                if(e instanceof ElementProjectTab)
                {
                    ElementProjectTab tab = (ElementProjectTab)e;
                    if(tab.info.identifier.equals(project.identifier))
                    {
                        tab.id = i;
                        els.remove(e);
                    }
                }
            }
        }

        for(int i = els.size() - 1; i >= 0; i--)
        {
            if(els.get(i) instanceof ElementProjectTab)
            {
                elements.remove(els.get(i));
            }
        }
        if(selectedProject >= 0)
        {
            updateModelTree(projects.get(selectedProject));
        }
        else
        {
            ((GuiWorkspace)workspace).windowModelTree.modelList.trees.clear();
            ((GuiWorkspace)workspace).windowAnimate.animList.trees.clear();
            ((GuiWorkspace)workspace).windowControls.selectedObject = null;
            ((GuiWorkspace)workspace).windowControls.refresh = true;
        }

        resized();
    }

    public void updateProject(ProjectInfo info)
    {
        boolean updateModels = false;
        boolean projExisted = false;
        for(int i = 0; i < projects.size(); i++)
        {
            ProjectInfo info1 = projects.get(i);
            if(info1.identifier.equals(info.identifier))
            {
                info.cloneFrom(info1);
                info1.destroy();
                projects.remove(i);
                projects.add(i, info);
                ((ElementProjectTab)elements.get(i)).info = info;
                if(i != selectedProject)
                {
                    ((ElementProjectTab)elements.get(i)).changed = true;
                }
                else
                {
                    updateModels = true;
                }
                for(Animation anim : info1.anims)
                {
                    for(Animation anim1 : info.anims)
                    {
                        if(anim1.identifier.equals(anim.identifier))
                        {
                            anim1.playTime = anim.playTime;
                            anim1.playing = anim.playing;
                        }
                    }
                }
                projExisted = true;
            }
        }
        if(!projExisted)
        {
            if(projects.isEmpty())
            {
                info.cameraFov = ((GuiWorkspace)workspace).cameraFov;
                info.cameraZoom = ((GuiWorkspace)workspace).cameraZoom;
                info.cameraYaw = ((GuiWorkspace)workspace).cameraYaw;
                info.cameraPitch = ((GuiWorkspace)workspace).cameraPitch;
                info.cameraOffsetX = ((GuiWorkspace)workspace).cameraOffsetX;
                info.cameraOffsetY = ((GuiWorkspace)workspace).cameraOffsetY;
            }
            info.lastAutosave = ((GuiWorkspace)workspace).liveTime;
            projects.add(info);
            elements.add(new ElementProjectTab(this, 0, 0, 10, 10, elements.size(), info));
            if(projects.size() == 1 || ((GuiWorkspace)workspace).openNextNewProject)
            {
                ((GuiWorkspace)workspace).openNextNewProject = false;
                changeProject(projects.size() - 1);
            }
        }

        if(updateModels)
        {
            updateModelTree(info);
        }

        info.initClient();

        resized();
    }

    public void updateModelTree(ProjectInfo info)
    {
        ElementListTree modelList = ((GuiWorkspace)workspace).windowModelTree.modelList;
        modelList.trees.clear();

        for(int k = 0; k < info.cubeGroups.size(); k++)
        {
            modelList.createTree(null, info.cubeGroups.get(k), 13, 0, true, false);
            createTreeForGroup(info.cubeGroups.get(k), modelList, 1);
        }
        for(int k = 0; k < info.cubes.size(); k++)
        {
            modelList.createTree(null, info.cubes.get(k), 13, 0, true, false);
            createTreeForCube(info.cubes.get(k), modelList, 1);
        }

        if(!modelList.selectedIdentifier.isEmpty())
        {
            boolean found = false;
            for(int k = 0; k < modelList.trees.size(); k++)
            {
                if(modelList.trees.get(k).attachedObject instanceof CubeGroup && ((CubeGroup)modelList.trees.get(k).attachedObject).identifier.equals(modelList.selectedIdentifier))
                {
                    found = true;
                    modelList.trees.get(k).selected = true;
                    modelList.clickElement(modelList.trees.get(k).attachedObject);
                }
                else if(modelList.trees.get(k).attachedObject instanceof CubeInfo && ((CubeInfo)modelList.trees.get(k).attachedObject).identifier.equals(modelList.selectedIdentifier))
                {
                    found = true;
                    modelList.trees.get(k).selected = true;
                    modelList.clickElement(modelList.trees.get(k).attachedObject);
                }
            }
            if(!found)
            {
                modelList.selectedIdentifier = "";
            }
        }
        if(modelList.selectedIdentifier.isEmpty())
        {
            ((GuiWorkspace)workspace).windowControls.selectedObject = null;
            ((GuiWorkspace)workspace).windowControls.refresh = true;
        }

        ElementListTree animList = ((GuiWorkspace)workspace).windowAnimate.animList;
        animList.trees.clear();

        for(int i = 0; i < info.anims.size(); i++)
        {
            animList.createTree(null, info.anims.get(i), 13, 0, false, false);
        }

        if(!animList.selectedIdentifier.isEmpty())
        {
            boolean found = false;
            for(int k = 0; k < animList.trees.size(); k++)
            {
                if(animList.trees.get(k).attachedObject instanceof Animation && ((Animation)animList.trees.get(k).attachedObject).identifier.equals(animList.selectedIdentifier))
                {
                    found = true;
                    animList.trees.get(k).selected = true;
                    animList.clickElement(animList.trees.get(k).attachedObject);
                }
            }
            if(!found)
            {
                animList.selectedIdentifier = "";
            }
        }

    }

    public void createTreeForGroup(CubeGroup group, ElementListTree modelList, int attachLevel)
    {
        for(int k = 0; k < group.cubeGroups.size(); k++)
        {
            modelList.createTree(null, group.cubeGroups.get(k), 13, attachLevel, true, false);
            createTreeForGroup(group.cubeGroups.get(k), modelList, attachLevel + 1);
        }
        for(int k = 0; k < group.cubes.size(); k++)
        {
            modelList.createTree(null, group.cubes.get(k), 13, attachLevel, true, false);
            createTreeForCube(group.cubes.get(k), modelList, attachLevel + 1);
        }
    }

    public void createTreeForCube(CubeInfo group, ElementListTree modelList, int attachLevel)
    {
        for(int k = 0; k < group.getChildren().size(); k++)
        {
            modelList.createTree(null, group.getChildren().get(k), 13, attachLevel, true, false);
            createTreeForCube(group.getChildren().get(k), modelList, attachLevel + 1);
        }
    }

    public void changeProject(ProjectInfo info)
    {
        for(int i = 0; i < projects.size(); i++)
        {
            if(projects.get(i) == info)
            {
                changeProject(i);
                return;
            }
        }
    }

    public void changeProject(int i)
    {
        if(selectedProject != -1)
        {
            ProjectInfo info = projects.get(selectedProject);
            info.cameraFov = ((GuiWorkspace)workspace).cameraFov;
            info.cameraZoom = ((GuiWorkspace)workspace).cameraZoom;
            info.cameraYaw = ((GuiWorkspace)workspace).cameraYaw;
            info.cameraPitch = ((GuiWorkspace)workspace).cameraPitch;
            info.cameraOffsetX = ((GuiWorkspace)workspace).cameraOffsetX;
            info.cameraOffsetY = ((GuiWorkspace)workspace).cameraOffsetY;
        }
        selectedProject = i;
        if(selectedProject != -1)
        {
            ProjectInfo info = projects.get(selectedProject);
            ((GuiWorkspace)workspace).cameraFov = info.cameraFov;
            ((GuiWorkspace)workspace).cameraZoom = info.cameraZoom;
            ((GuiWorkspace)workspace).cameraYaw = info.cameraYaw;
            ((GuiWorkspace)workspace).cameraPitch = info.cameraPitch;
            ((GuiWorkspace)workspace).cameraOffsetX = info.cameraOffsetX;
            ((GuiWorkspace)workspace).cameraOffsetY = info.cameraOffsetY;

            ((ElementProjectTab)elements.get(i)).changed = false;

            updateModelTree(info);

            if(!((GuiWorkspace)workspace).remoteSession && ((GuiWorkspace)workspace).host != null)
            {
                Tabula.channel.sendToServer(new PacketSetCurrentProject(((GuiWorkspace)workspace).host, ((GuiWorkspace)workspace).hostX, ((GuiWorkspace)workspace).hostY, ((GuiWorkspace)workspace).hostZ, info.identifier));
            }
        }
        else
        {
            ((GuiWorkspace)workspace).windowAnimate.animList.selectedIdentifier = "";
            ((GuiWorkspace)workspace).windowAnimate.timeline.selectedIdentifier = "";
            ((GuiWorkspace)workspace).cameraZoom = 1.0F;
            ((GuiWorkspace)workspace).cameraYaw = 0.0F;
            ((GuiWorkspace)workspace).cameraPitch = 0.0F;
            ((GuiWorkspace)workspace).cameraOffsetX = 0.0F;
            ((GuiWorkspace)workspace).cameraOffsetY = 0.0F;

            if(!((GuiWorkspace)workspace).remoteSession && ((GuiWorkspace)workspace).host != null)
            {
                Tabula.channel.sendToServer(new PacketSetCurrentProject(((GuiWorkspace)workspace).host, ((GuiWorkspace)workspace).hostX, ((GuiWorkspace)workspace).hostY, ((GuiWorkspace)workspace).hostZ, ""));
            }
        }

        ((GuiWorkspace)workspace).windowModelTree.modelList.sliderProg = 0.0F;
    }

    @Override
    public int getHeight()
    {
        return 12;
    }
}
