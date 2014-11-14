package us.ichun.mods.tabula.gui.window;

import us.ichun.mods.tabula.gui.GuiWorkspace;
import us.ichun.mods.tabula.gui.window.element.Element;
import us.ichun.mods.tabula.gui.window.element.ElementTitle;
import us.ichun.mods.tabula.gui.window.element.ElementWindow;

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
        }
        resized();
    }
}
