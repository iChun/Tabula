package us.ichun.mods.tabula.gui.window.element;

import ichun.client.render.RendererHelper;
import net.minecraft.util.StatCollector;
import us.ichun.mods.tabula.gui.window.Window;
import us.ichun.mods.tabula.gui.window.WindowTabs;

public class ElementWindow extends Element
{
    public Window mountedWindow;
    public int oriWidth;
    public int oriHeight;

    public ElementWindow(Window window, int x, int y, int w, int h, int ID, Window mount)
    {
        super(window, x, y, w, h, ID, true);
        mountedWindow = mount;
        oriWidth = mountedWindow.width;
        oriHeight = mountedWindow.height;
    }

    @Override
    public void draw(int mouseX, int mouseY, boolean hover)
    {
        String titleToRender = StatCollector.translateToLocal(mountedWindow.titleLocale);
        while(titleToRender.length() > 1 && parent.workspace.getFontRenderer().getStringWidth(titleToRender) > width )
        {
            if(titleToRender.endsWith("... "))
            {
                titleToRender = titleToRender.substring(0, titleToRender.length() - 5) + "... ";
            }
            else
            {
                titleToRender = titleToRender.substring(0, titleToRender.length() - 1) + "... ";
            }
        }
        parent.workspace.getFontRenderer().drawString(titleToRender, parent.posX + posX + 4, parent.posY + posY + 3, 0xffffff, false);

        mountedWindow.posX = parent.posX;
        mountedWindow.posY = parent.posY;
        mountedWindow.width = parent.width;
        mountedWindow.height = parent.height;

        //TODO if selected tab, draw window. Right now I don't have anything in the window so there's nothing to draw.
        if(parent instanceof WindowTabs)
        {
            WindowTabs tab = (WindowTabs)parent;
            if(tab.minimized && id != tab.tabs.size() - 1)
            {
                RendererHelper.drawColourOnScreen(100, 100, 100, 255, getPosX() + width + 1, getPosY() + 1, 1, height, 0);
            }
            if(tab.selectedTab == id && !tab.minimized)
            {
                RendererHelper.drawColourOnScreen(150, 150, 150, 255, getPosX(), getPosY() + 1 + height, width + 3, 2, 0);
            }
            else if(hover)
            {
                RendererHelper.drawColourOnScreen(0xffffff, 255, getPosX() + 2, getPosY() + 1, width, 1, 0);
                RendererHelper.drawColourOnScreen(0xffffff, 255, getPosX() + 2, getPosY() + 1, 1, height, 0);
                RendererHelper.drawColourOnScreen(0xffffff, 255, getPosX() + 2, getPosY() + 1 + height - 1, width, 1, 0);
                RendererHelper.drawColourOnScreen(0xffffff, 255, getPosX() + width + 1, getPosY() + 1, 1, height, 0);
            }
        }
    }

    @Override
    public void resized()
    {
        if(parent instanceof WindowTabs)
        {
            WindowTabs tab = (WindowTabs)parent;
            int space = tab.getWidth() - (tab.BORDER_SIZE * 2) - tab.workspace.getFontRenderer().getStringWidth("  _");
            int totalSpace = 0;
            for(ElementWindow tab1 : tab.tabs)
            {
                totalSpace += tab.workspace.getFontRenderer().getStringWidth(StatCollector.translateToLocal(tab1.mountedWindow.titleLocale) + " ");
            }
            if(totalSpace > space)
            {
                posX = tab.BORDER_SIZE + (id * (space / tab.tabs.size()));
                posY = 0;
                width = space / tab.tabs.size();
                height = 12;
            }
            else
            {
                posX = tab.BORDER_SIZE;
                for(int i = 0; i < id; i++)
                {
                    posX += tab.workspace.getFontRenderer().getStringWidth(StatCollector.translateToLocal(tab.tabs.get(i).mountedWindow.titleLocale) + " ");
                }
                posY = 0;
                width = tab.workspace.getFontRenderer().getStringWidth(StatCollector.translateToLocal(mountedWindow.titleLocale) + " ");
                height = 12;
            }
        }
    }

    @Override
    public String tooltip()
    {
        String titleToRender = StatCollector.translateToLocal(mountedWindow.titleLocale);
        if(parent.workspace.getFontRenderer().getStringWidth(titleToRender) > width)
        {
            return mountedWindow.titleLocale;
        }
        return null; //return null for no tooltip. This is localized.
    }

    @Override
    public boolean onClick(int mouseX, int mouseY, int id)
    {
        if(id == 0)
        {
            if(parent instanceof WindowTabs)
            {
                ((WindowTabs)parent).selectedTab = this.id;
                if(parent.minimized)
                {
                    parent.toggleMinimize();
                }
            }
        }
        return true;
    }
}
