package us.ichun.mods.tabula.gui;

import ichun.client.render.RendererHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.util.StatCollector;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;
import us.ichun.mods.tabula.gui.window.WindowTabs;
import us.ichun.mods.tabula.gui.window.element.Element;
import us.ichun.mods.tabula.gui.window.Window;
import us.ichun.mods.tabula.gui.window.element.ElementWindow;

import java.util.ArrayList;

public class GuiWorkspace extends GuiScreen
{
    public int oriScale;
    public ArrayList<ArrayList<Window>> levels = new ArrayList<ArrayList<Window>>() {{
        add(0, new ArrayList<Window>()); // dock left
        add(1, new ArrayList<Window>()); // dock right
        add(2, new ArrayList<Window>()); // dock btm
    }};

    public boolean mouseLeftDown;
    public boolean mouseRightDown;
    public boolean mouseMiddleDown;

    public Window windowDragged;
    public int dragType; //1 = title drag, 2 = border drag, 3 = corner drag;

    public Element elementHovered;
    public int hoverTime;
    public boolean hovering;

    public Element elementDragged;
    public int elementDragX;
    public int elementDragY;

    public static final int VARIABLE_LEVEL = 3;

    public GuiWorkspace(int scale)
    {
        oriScale = scale;
        levels.get(2).add(new Window(this, 20, 20, 200, 200, 40, 50, "menu.convertingLevel", true));
        levels.get(2).add(new Window(this, 700, 100, 300, 500, 100, 200, "menu.generatingTerrain", true));
        levels.get(2).add(new Window(this, 400, 200, 200, 300, 100, 200, "menu.loadingLevel", true));
    }

    @Override
    public void updateScreen()
    {
        if(elementHovered != null)
        {
            hoverTime++;
        }
        for(int i = levels.size() - 1; i >= VARIABLE_LEVEL; i--)//clean up empty levels.
        {
            if(levels.get(i).isEmpty())
            {
                levels.remove(i);
            }
        }
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float f)
    {
        //TODO update elements here
        //TODO docks...? Remember to draw upper dock first.
        //TODO a reset all windows button for people who "accidentally" drag the window out of the screen
        //TODO window tabs!
        GL11.glPushMatrix();
        GL11.glEnable(GL11.GL_DEPTH_TEST);
        GL11.glDepthFunc(GL11.GL_LEQUAL);
        GL11.glDepthMask(true);
        RendererHelper.drawColourOnScreen(204, 204, 204, 255, 0, 0, width, height, -1000D); //204 cause 0.8F * 255

        hovering = false;
        boolean hasClicked = false;
        for(int i = levels.size() - 1; i >= 0 ; i--)
        {
            for(int j = levels.get(i).size() - 1; j >= 0; j--)
            {
                Window window = levels.get(i).get(j);
                if(mouseX >= window.posX && mouseX <= window.posX + window.getWidth() && mouseY >= window.posY && mouseY <= window.posY + window.getHeight())
                {
                    if(!hasClicked)
                    {
                        if(Mouse.isButtonDown(0) && !mouseLeftDown)
                        {
                            windowDragged = window;
                            dragType = window.onClick(mouseX - window.posX, mouseY - window.posY, 0);
                            hasClicked = true;
                        }
                        if(Mouse.isButtonDown(1) && !mouseRightDown)
                        {
                            windowDragged = window;
                            dragType = window.onClick(mouseX - window.posX, mouseY - window.posY, 1);
                            hasClicked = true;
                        }
                        if(Mouse.isButtonDown(2) && !mouseMiddleDown)
                        {
                            windowDragged = window;
                            dragType = window.onClick(mouseX - window.posX, mouseY - window.posY, 2);
                            hasClicked = true;
                        }
                    }
                }
                window.draw(mouseX - window.posX, mouseY - window.posY);
            }
            GL11.glTranslatef(0F, 0F, -10F);
        }

        if(!hovering)
        {
            elementHovered = null;
            hoverTime = 0;
        }
        else if(elementHovered != null && hoverTime > 5 && elementHovered.tooltip() != null) //1s to draw tooltip
        {
            GL11.glTranslatef(0F, 0F, 20F * levels.size());
            String tooltip = StatCollector.translateToLocal(elementHovered.tooltip());
            int xOffset = 5;
            int yOffset = 20;
            RendererHelper.drawColourOnScreen(150, 150, 150, 255, mouseX + xOffset, mouseY + yOffset, fontRendererObj.getStringWidth(tooltip) + ((Window.BORDER_SIZE - 1) * 2), 12, 0);
            RendererHelper.drawColourOnScreen(34, 34, 34, 255, mouseX + xOffset + 1, mouseY + yOffset + 1, fontRendererObj.getStringWidth(tooltip) + ((Window.BORDER_SIZE - 1) * 2) - 2, 12 - 2, 0);
            fontRendererObj.drawString(tooltip, mouseX + xOffset + (Window.BORDER_SIZE - 1), mouseY + yOffset + (Window.BORDER_SIZE - 1), 0xffffff, false);
//            RendererHelper.drawColourOnScreen(34, 34, 34, 255, posX + BORDER_SIZE, posY + BORDER_SIZE, getWidth() - (BORDER_SIZE * 2), getHeight() - (BORDER_SIZE * 2), 0);
        }

        GL11.glPopMatrix();

        mouseLeftDown = Mouse.isButtonDown(0);
        mouseRightDown = Mouse.isButtonDown(1);
        mouseMiddleDown = Mouse.isButtonDown(2);

        if(windowDragged != null)
        {
            if(windowDragged.clickId == 0 && !mouseLeftDown || windowDragged.clickId == 1 && !mouseRightDown || windowDragged.clickId == 2 && !mouseMiddleDown)
            {
                windowDragged = null;
            }
            else
            {
                bringWindowToFront(windowDragged);
                if(dragType == 1) // moving the window
                {
                    windowDragged.posX -= windowDragged.clickX - (mouseX - windowDragged.posX);
                    windowDragged.posY -= windowDragged.clickY - (mouseY - windowDragged.posY);

                    boolean tabbed = false;
                    for(int i = levels.size() - 1; i >= 0 ; i--)
                    {
                        for(int j = levels.get(i).size() - 1; j >= 0; j--)
                        {
                            Window window = levels.get(i).get(j);
                            //TODO if in dock....?
                            if(tabbed || window == windowDragged)
                            {
                                continue;
                            }
                            if(mouseX - window.posX >= 0 && mouseX - window.posX <= window.getWidth() && mouseY - window.posY >= 0 && mouseY - window.posY <= 12)
                            {
                                WindowTabs tabs;
                                if(window instanceof WindowTabs)
                                {
                                    tabs = (WindowTabs)window;
                                }
                                else
                                {
                                    tabs = new WindowTabs(this, window);
                                }
                                tabs.addWindow(windowDragged, true);
                                levels.get(i).remove(j);
                                levels.get(i).add(j, tabs);
                                removeWindow(windowDragged);
                                windowDragged = null;
                                tabbed = true;
                            }
                        }
                    }
                }
                if(dragType >= 2)
                {
                    int bordersClicked = dragType - 3;
                    if((bordersClicked & 1) == 1) // top
                    {
                        windowDragged.height += windowDragged.clickY - (mouseY - windowDragged.posY);
                        windowDragged.posY -= windowDragged.clickY - (mouseY - windowDragged.posY);
                        if(windowDragged.getHeight() < windowDragged.minHeight)
                        {
                            int resize = windowDragged.getHeight() - windowDragged.minHeight;
                            windowDragged.posY += resize;
                            windowDragged.height -= resize;
                        }
                        else
                        {
                            windowDragged.clickY = mouseY - windowDragged.posY;
                        }
                    }
                    if((bordersClicked >> 1 & 1) == 1) // top
                    {
                        windowDragged.width += windowDragged.clickX - (mouseX - windowDragged.posX);
                        windowDragged.posX -= windowDragged.clickX - (mouseX - windowDragged.posX);
                        if(windowDragged.getWidth() < windowDragged.minWidth)
                        {
                            int resize = windowDragged.getWidth() - windowDragged.minWidth;
                            windowDragged.posX += resize;
                            windowDragged.width -= resize;
                        }
                        else
                        {
                            windowDragged.clickX = mouseX - windowDragged.posX;
                        }
                    }
                    if((bordersClicked >> 2 & 1) == 1) // bottom
                    {
                        windowDragged.height -= windowDragged.clickY - (mouseY - windowDragged.posY);
                        if(windowDragged.getHeight() < windowDragged.minHeight)
                        {
                            windowDragged.height = windowDragged.minHeight;
                        }
                        else
                        {
                            windowDragged.clickY = mouseY - windowDragged.posY;
                        }
                    }
                    if((bordersClicked >> 3 & 1) == 1) // bottom
                    {
                        windowDragged.width -= windowDragged.clickX - (mouseX - windowDragged.posX);
                        if(windowDragged.getWidth() < windowDragged.minWidth)
                        {
                            windowDragged.width = windowDragged.minWidth;
                        }
                        else
                        {
                            windowDragged.clickX = mouseX - windowDragged.posX;
                        }
                    }
                    windowDragged.resized();
                }
            }
        }

        if(elementDragged != null)
        {
            if(!mouseLeftDown)
            {
                elementDragged = null;
            }
            else if(!(mouseX - elementDragged.parent.posX >= 0 && mouseX - elementDragged.parent.posX <= elementDragged.parent.getWidth() && mouseY - elementDragged.parent.posY >= 0 && mouseY - elementDragged.parent.posY <= 12))
            {
                //TODO handle if tabs only left 1, handle if was currently selected tab, -1 selected tab or +1...?
                if(elementDragged instanceof ElementWindow)
                {
                    ElementWindow element = (ElementWindow)elementDragged;
                    //fix up the tabs.
                    WindowTabs tab = (WindowTabs)element.parent;
                    tab.tabs.remove(element);
                    tab.elements.remove(element);
                    if(tab.tabs.size() <= 1)
                    {
                        removeWindow(tab);
                        if(tab.tabs.size() == 1)
                        {
                            addWindowOnTop(tab.tabs.get(0).mountedWindow);
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

                    addWindowOnTop(element.mountedWindow);
                    windowDragged = element.mountedWindow;
                    dragType = 1;

                    windowDragged.width = element.oriWidth;
                    windowDragged.height = element.oriHeight;

                    windowDragged.posX = mouseX - (windowDragged.getWidth() / 2);
                    windowDragged.posY = mouseY - 6;

                    elementDragged = null;
                }
            }
        }
    }

    public void removeWindow(Window window)
    {
        for(int i = levels.size() - 1; i >= 0 ; i--)
        {
            for(int j = levels.get(i).size() - 1; j >= 0; j--)
            {
                Window window1 = levels.get(i).get(j);
                //TODO inform docking of change.
                if(window1 == window)
                {
                    levels.get(i).remove(j);
                    if(levels.get(i).isEmpty())
                    {
                        levels.remove(i);
                    }
                }
            }
        }
    }

    public void addWindowOnTop(Window window)
    {
        ArrayList<Window> topLevel = new ArrayList<Window>();
        topLevel.add(window);
        levels.add(topLevel);
    }

    public void bringWindowToFront(Window window)
    {
        for(int i = levels.size() - 1; i >= 0 ; i--)
        {
            for(int j = levels.get(i).size() - 1; j >= 0; j--)
            {
                Window window1 = levels.get(i).get(j);
                //TODO inform docking of change.
                if(window1 == window && !(i == levels.size() - 1 && levels.get(i).size() == 1))
                {
                    ArrayList<Window> topLevel = new ArrayList<Window>();
                    topLevel.add(window1);
                    levels.get(i).remove(j);
                    if(levels.get(i).isEmpty())
                    {
                        levels.remove(i);
                    }
                    levels.add(topLevel);
                }
            }
        }
    }

    @Override
    public void handleMouseInput(){} //Mouse handling is done in drawScreen

    @Override
    protected void keyTyped(char c, int key)
    {
        if (key == 1)
        {
            this.mc.displayGuiScreen((GuiScreen)null);
            this.mc.setIngameFocus();
        }
    }

    @Override
    public void onGuiClosed()
    {
        Minecraft.getMinecraft().gameSettings.guiScale = oriScale;
    }

    public FontRenderer getFontRenderer()
    {
        return fontRendererObj;
    }
}
