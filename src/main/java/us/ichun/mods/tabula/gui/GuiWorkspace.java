package us.ichun.mods.tabula.gui;

import ichun.client.render.RendererHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiScreen;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;

import java.util.ArrayList;

public class GuiWorkspace extends GuiScreen
{
    public int oriScale;
    private ArrayList<ArrayList<GuiWindow>> levels = new ArrayList<ArrayList<GuiWindow>>() {{
        add(0, new ArrayList<GuiWindow>()); // dock left
        add(1, new ArrayList<GuiWindow>()); // dock right
        add(2, new ArrayList<GuiWindow>()); // dock btm
    }};

    public boolean mouseLeftDown;
    public boolean mouseRightDown;
    public boolean mouseMiddleDown;

    public GuiWindow windowDragged;
    public int dragType; //1 = title drag, 2 = border drag, 3 = corner drag;

    public final int VARIABLE_LEVEL = 3;

    public GuiWorkspace(int scale)
    {
        oriScale = scale;
        levels.get(2).add(new GuiWindow(this, 20, 20, 200, 200, 40, 50, "menu.convertingLevel", true));
        levels.get(2).add(new GuiWindow(this, 700, 100, 300, 500, 100, 200, "menu.generatingTerrain", true));
        levels.get(2).add(new GuiWindow(this, 400, 200, 200, 300, 100, 200, "menu.loadingLevel", true));
    }

    @Override
    public void updateScreen()
    {
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float f)
    {
        //TODO update elements here
        //TODO draw elements here
        //TODO docks...? Remember to draw upper dock first.
        //TODO a reset all windows button for people who "accidentally" drag the window out of the screen
        GL11.glPushMatrix();
        GL11.glEnable(GL11.GL_DEPTH_TEST);
        GL11.glDepthFunc(GL11.GL_LEQUAL);
        GL11.glDepthMask(true);
        RendererHelper.drawColourOnScreen(204, 204, 204, 255, 0, 0, width, height, -1000D); //204 cause 0.8F * 255

        boolean hasClicked = false;
        for(int i = levels.size() - 1; i >= 0 ; i--)
        {
            for(int j = levels.get(i).size() - 1; j >= 0; j--)
            {
                GuiWindow window = levels.get(i).get(j);
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
                if(dragType == 1)
                {
                    windowDragged.posX -= windowDragged.clickX - (mouseX - windowDragged.posX);
                    windowDragged.posY -= windowDragged.clickY - (mouseY - windowDragged.posY);
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
    }

    public void bringWindowToFront(GuiWindow window)
    {
        for(int i = levels.size() - 1; i >= 0 ; i--)
        {
            for(int j = levels.get(i).size() - 1; j >= 0; j--)
            {
                GuiWindow window1 = levels.get(i).get(j);
                //TODO inform docking of change.
                if(window1 == window && !(i == levels.size() - 1 && levels.get(i).size() == 1))
                {
                    ArrayList<GuiWindow> topLevel = new ArrayList<GuiWindow>();
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
