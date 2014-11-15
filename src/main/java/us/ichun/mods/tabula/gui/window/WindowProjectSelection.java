package us.ichun.mods.tabula.gui.window;

import ichun.client.render.RendererHelper;
import us.ichun.mods.tabula.common.project.ProjectInfo;
import us.ichun.mods.tabula.gui.GuiWorkspace;
import us.ichun.mods.tabula.gui.Theme;
import us.ichun.mods.tabula.gui.window.element.Element;
import us.ichun.mods.tabula.gui.window.element.ElementProjectTab;

import java.util.ArrayList;

public class WindowProjectSelection extends WindowTopDock
{
    public ArrayList<ProjectInfo> projects = new ArrayList<ProjectInfo>();
    public int selectedProject;

    public WindowProjectSelection(GuiWorkspace parent, int x, int y, int w, int h, int minW, int minH)
    {
        super(parent, x, y, w, h, minW, minH);

        elements.clear();
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
        RendererHelper.drawColourOnScreen(Theme.tabSideInactive[0], Theme.tabSideInactive[1], Theme.tabSideInactive[2], 255, posX, posY, width, 1, 0);
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
        posY = GuiWorkspace.TOP_DOCK_HEIGHT + 1;
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

    public void updateProject(ProjectInfo info)
    {
        boolean added = false;
        for(int i = 0; i < projects.size(); i++)
        {
            ProjectInfo info1 = projects.get(i);
            if(info1.identifier.equals(info.identifier))
            {
                //TODO link the textures together.
                projects.remove(i);
                projects.add(i, info);
                ((ElementProjectTab)elements.get(i)).info = info;
                ((ElementProjectTab)elements.get(i)).changed = true;
                added = true;
            }
        }
        if(!added)
        {
            projects.add(info);
            elements.add(new ElementProjectTab(this, 0, 0, 10, 10, elements.size(), info));
            changeProject(elements.size());
        }
        resized();
    }

    public void changeProject(int i)
    {
        selectedProject = i;
    }

    @Override
    public int getHeight()
    {
        return 12;
    }
}
