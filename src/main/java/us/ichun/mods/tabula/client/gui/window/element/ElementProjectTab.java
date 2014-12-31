package us.ichun.mods.tabula.client.gui.window.element;

import ichun.client.render.RendererHelper;
import us.ichun.mods.tabula.client.gui.Theme;
import us.ichun.mods.tabula.client.gui.window.Window;
import us.ichun.mods.tabula.client.gui.window.WindowProjectSelection;
import us.ichun.module.tabula.common.project.ProjectInfo;

public class ElementProjectTab extends Element
{
    public ProjectInfo info;
    public boolean changed;

    public ElementProjectTab(Window window, int x, int y, int w, int h, int ID, ProjectInfo inf)
    {
        super(window, x, y, w, h, ID, true);
        info = inf;
        changed = false;
    }

    @Override
    public void draw(int mouseX, int mouseY, boolean hover)
    {
        WindowProjectSelection proj = (WindowProjectSelection)parent;
        if(id != proj.projects.size() - 1)
        {
            RendererHelper.drawColourOnScreen(Theme.instance.tabSideInactive[0], Theme.instance.tabSideInactive[1], Theme.instance.tabSideInactive[2], 255, getPosX() + width - 1, getPosY() + 1, 1, height, 0);
        }
        if(proj.selectedProject == id)
        {
            RendererHelper.drawColourOnScreen(Theme.instance.elementProjectTabActive[0], Theme.instance.elementProjectTabActive[1], Theme.instance.elementProjectTabActive[2], 255, getPosX(), getPosY(), width - 1, height, 0);
        }
        else if(hover)
        {
            RendererHelper.drawColourOnScreen(Theme.instance.elementProjectTabHover[0], Theme.instance.elementProjectTabHover[1], Theme.instance.elementProjectTabHover[2], 255, getPosX(), getPosY(), width - 1, height, 0);
        }

        String titleToRender = info.modelName;
        while(titleToRender.length() > 1 && parent.workspace.getFontRenderer().getStringWidth(titleToRender) > width - (parent.workspace.remoteSession ? 2 : 11) )
        {
            if(titleToRender.startsWith("... "))
            {
                break;
            }
            if(titleToRender.endsWith("... "))
            {
                titleToRender = titleToRender.substring(0, titleToRender.length() - 5) + "... ";
            }
            else
            {
                titleToRender = titleToRender.substring(0, titleToRender.length() - 1) + "... ";
            }
        }
        parent.workspace.getFontRenderer().drawString(titleToRender, parent.posX + posX + 4, parent.posY + posY + 3, Theme.getAsHex(changed? Theme.instance.elementProjectTabFontChanges : Theme.instance.elementProjectTabFont), false);
        if(!parent.workspace.remoteSession)
        {
            parent.workspace.getFontRenderer().drawString("X", parent.posX + posX + width - 8, parent.posY + posY + 3, Theme.getAsHex(Theme.instance.elementProjectTabFont), false);
        }
    }

    @Override
    public void resized()
    {
        WindowProjectSelection tab = (WindowProjectSelection)parent;
        int space = tab.getWidth();
        int totalSpace = 0;
        for(ProjectInfo tab1 : tab.projects)
        {
            totalSpace += tab.workspace.getFontRenderer().getStringWidth(" " + tab1.modelName + (parent.workspace.remoteSession ? " " : " X "));
        }
        if(totalSpace > space)
        {
            posX = (id * (space / tab.projects.size()));
            posY = 0;
            width = space / tab.projects.size();
            height = 12;
        }
        else
        {
            posX = 0;
            for(int i = 0; i < id; i++)
            {
                posX += tab.workspace.getFontRenderer().getStringWidth(" " + tab.projects.get(i).modelName + (parent.workspace.remoteSession ? " " : " X "));
            }
            posY = 0;
            width = tab.workspace.getFontRenderer().getStringWidth(" " + info.modelName + (parent.workspace.remoteSession ? " " : " X "));
            height = 12;
        }
    }

    @Override
    public String tooltip()
    {
        //        String titleToRender = StatCollector.translateToLocal(mountedWindow.titleLocale);
        //        if(parent.workspace.getFontRenderer().getStringWidth(titleToRender) > width)
        //        {
        //            return mountedWindow.titleLocale;
        //        }
        String tooltip = info.modelName + " - " + info.authorName;
        if(info.saveFile != null)
        {
            tooltip = tooltip + " - " + info.saveFile.getName();
        }
        return tooltip; //return null for no tooltip. This is localized.
    }

    @Override
    public boolean onClick(int mouseX, int mouseY, int id)
    {
        if(id == 0 || id == 2)
        {
            ((WindowProjectSelection)parent).changeProject(this.id);
            if(!parent.workspace.remoteSession && (mouseX + parent.posX > getPosX() + width - 9 || id == 2))
            {
                parent.workspace.closeProject(((WindowProjectSelection)parent).projects.get(this.id));
            }
        }
        return false;
    }
}
