package us.ichun.mods.tabula.client.gui.window;

import us.ichun.mods.tabula.client.gui.GuiWorkspace;
import us.ichun.mods.tabula.client.gui.window.element.ElementTitle;
import us.ichun.mods.tabula.client.gui.window.element.ElementWindow;

import java.util.ArrayList;

public class WindowTabs extends Window
{
    public ArrayList<ElementWindow> tabs = new ArrayList<ElementWindow>();
    public int selectedTab;

    public WindowTabs(GuiWorkspace parent, Window original)
    {
        super(parent, original.posX, original.posY, original.width, original.height, original.minWidth, original.minHeight, "", true);
        docked = original.docked;
        for(int i = 0; i < elements.size(); i++)
        {
            if(elements.get(i) instanceof ElementTitle)
            {
                elements.remove(i);
            }
        }
        addWindow(original, false);
    }

    @Override
    public int onClick(int mouseX, int mouseY, int id) //returns > 0 if clicked on title//border with LMB.
    {
        tabs.get(selectedTab).mountedWindow.onClick(mouseX, mouseY, id);
        return super.onClick(mouseX, mouseY, id);
    }

    public void addWindow(Window window, boolean select)
    {
        if(window instanceof WindowTabs)
        {
            WindowTabs windowTabs = (WindowTabs)window;
            for(int i = 0; i < windowTabs.tabs.size(); i++)
            {
                addWindow(windowTabs.tabs.get(i).mountedWindow, i == windowTabs.selectedTab && select);
            }
        }
        else
        {
            tabs.add(new ElementWindow(this, 0, 0, 10, 10, tabs.size(), window));
            elements.add(tabs.get(tabs.size() - 1));
            if(select)
            {
                selectedTab = tabs.size() - 1;
            }
            window.docked = docked;

            if(window.minWidth > minWidth)
            {
                minWidth = window.minWidth;
                if(width < minWidth)
                {
                    width = minWidth;
                }
            }
            if(window.minHeight > minHeight)
            {
                minHeight = window.minHeight;
                if(height < minHeight)
                {
                    height = minHeight;
                }
            }
            window.width = width;
            window.height = height;
        }
        resized();
    }

    public static Window detach(ElementWindow element)
    {
        //fix up the tabs.
        WindowTabs tab = (WindowTabs)element.parent;
        tab.tabs.remove(element);
        tab.elements.remove(element);
        if(tab.tabs.size() <= 1)
        {
            if(tab.docked >= 0)
            {
                for(int i = element.parent.workspace.levels.get(tab.docked).size() - 1; i >= 0; i--)
                {
                    Window w = element.parent.workspace.levels.get(tab.docked).get(i);
                    if(w == tab)
                    {
                        element.parent.workspace.levels.get(tab.docked).remove(i);
                        if(tab.tabs.size() == 1)
                        {
                            element.parent.workspace.levels.get(tab.docked).add(i, tab.tabs.get(0).mountedWindow);
                        }
                        break;
                    }
                }
            }
            else
            {
                element.parent.workspace.removeWindow(tab);
                if(tab.tabs.size() == 1)
                {
                    Window window = tab.tabs.get(0).mountedWindow;
                    element.parent.workspace.addWindowOnTop(window);
                    window.resized();
                }
            }
        }
        else
        {
            for(int i = 0; i < tab.tabs.size(); i++)
            {
                tab.tabs.get(i).id = i;
            }

            while(tab.selectedTab >= tab.tabs.size() && tab.selectedTab > 0)
            {
                tab.selectedTab--;
            }
            tab.resized();
        }

        element.parent.workspace.addWindowOnTop(element.mountedWindow);
        return element.mountedWindow;
    }

    public boolean allowMultipleInstances()
    {
        return true;
    }

}
