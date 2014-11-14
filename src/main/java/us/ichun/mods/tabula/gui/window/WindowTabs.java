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
                tabs.add(windowTabs.tabs.get(i));
                elements.add(windowTabs.tabs.get(i));
                if(i == windowTabs.selectedTab && select)
                {
                    selectedTab = tabs.size() - 1;
                }
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
        }
        resized();
    }
}
