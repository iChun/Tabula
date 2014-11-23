package us.ichun.mods.tabula.client.gui;

import com.google.gson.Gson;
import ichun.client.render.RendererHelper;
import ichun.common.core.util.MD5Checksum;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.init.Blocks;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.StatCollector;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;
import org.lwjgl.util.glu.Project;
import us.ichun.mods.tabula.Tabula;
import us.ichun.mods.tabula.client.core.ModelSelector;
import us.ichun.mods.tabula.client.core.ResourceHelper;
import us.ichun.mods.tabula.client.gui.window.*;
import us.ichun.mods.tabula.client.gui.window.element.Element;
import us.ichun.mods.tabula.client.gui.window.element.ElementListTree;
import us.ichun.mods.tabula.client.gui.window.element.ElementToggle;
import us.ichun.mods.tabula.client.gui.window.element.ElementWindow;
import us.ichun.module.tabula.common.project.ProjectInfo;
import us.ichun.module.tabula.common.project.components.CubeGroup;
import us.ichun.module.tabula.common.project.components.CubeInfo;

import java.io.File;
import java.util.ArrayList;

public class GuiWorkspace extends GuiScreen
{
    public int oriScale;
    public final boolean remoteSession;
    public boolean isEditor;

    public ResourceLocation grid16 = new ResourceLocation("tabula", "textures/workspace/grid16.png");
    public ResourceLocation orientationBase = new ResourceLocation("tabula", "textures/workspace/orientationBase.png");

    public ArrayList<ArrayList<Window>> levels = new ArrayList<ArrayList<Window>>() {{
        add(0, new ArrayList<Window>()); // dock left
        add(1, new ArrayList<Window>()); // dock right
        add(2, new ArrayList<Window>()); // dock btm
        add(3, new ArrayList<Window>()); // dock top
        add(4, new ArrayList<Window>()); // chat level
    }};

    public int oldWidth;
    public int oldHeight;

    public boolean mouseLeftDown;
    public boolean mouseRightDown;
    public boolean mouseMiddleDown;
    public boolean keyDeleteDown;
    public boolean keyXDown;
    public boolean keyCDown;
    public boolean keyVDown;
    public boolean keyZDown;
    public boolean keyYDown;

    public Object cubeCopied;

    public WindowProjectSelection projectManager;
    public WindowControls windowControls;
    public WindowTexture windowTexture;
    public WindowModelTree windowModelTree;
    public WindowChat windowChat;

    public Window windowDragged;
    public int dragType; //1 = title drag, 2 >= border drag.

    public Element elementHovered;
    public int hoverTime;
    public boolean hovering;

    public Element elementDragged;
    public int elementDragX;
    public int elementDragY;

    public Element elementSelected;

    public RenderBlocks renderBlocks;

    public boolean init;
    public int liveTime;
    public boolean resize;

    public float cameraZoom = 1.0F;
    public int cameraZoomInertia = 0;
    public float cameraZoomPerScroll = 0.05F;

    public float cameraFov = 30F;
    public int cameraFovInertia = 0;
    public float cameraFovPerScroll = 1F;

    public float cameraYaw;
    public float cameraPitch;
    public float cameraOffsetX;
    public float cameraOffsetY;

    public boolean clicked;
    public int prevMouseX;
    public int prevMouseY;

    public boolean wantToExit;

    public static final int VARIABLE_LEVEL = 4;
    public static final int TOP_DOCK_HEIGHT = 19;

    private ModelSelector modelSelector = new ModelSelector(this, 256);

    public GuiWorkspace(int scale, boolean remote, boolean editing)
    {
        oriScale = scale;
        remoteSession = remote;
        isEditor = editing;

        renderBlocks = new RenderBlocks();
    }

    @Override
    public void initGui()
    {
        super.initGui();
        if(!init)
        {
            init = true;

            windowControls = new WindowControls(this, width / 2 - 80, height / 2 - 125, 162, 270, 162, 270);
            windowTexture = new WindowTexture(this, width / 2 - 53, height / 2 - 100, 106, 100, 106, 88);
            windowModelTree = new WindowModelTree(this, width / 2 - 53, height / 2 - 125, 106, 250, 106, 250);
            addToDock(0, windowControls);
            addToDock(1, windowTexture);
            addToDock(1, windowModelTree);

            levels.get(3).add(new WindowTopDock(this, 0, 0, width, 20, 20, 20));
            projectManager = new WindowProjectSelection(this, 0, 0, width, 20, 20, 20);
            levels.get(3).add(projectManager);

            windowChat = new WindowChat(this, -1000, -1000, 250, 180, 162, 50);
            levels.get(4).add(windowChat);

//            Tabula.proxy.tickHandlerClient.mainframe.loadEmptyProject("New Project", "iChun? :O", 64, 32);
        }
        resize = true;
        screenResize();

        Keyboard.enableRepeatEvents(true);
    }

    @Override
    public void updateScreen()
    {
        if(resize)
        {
            resize = false;
            screenResize();
        }
        if(elementHovered != null)
        {
            hoverTime++;
        }
        for(int i = levels.size() - 1; i >= 0; i--)//clean up empty levels.
        {
            if(levels.get(i).isEmpty()&& i >= VARIABLE_LEVEL)
            {
                levels.remove(i);
            }
            else
            {
                for(int j = levels.get(i).size() - 1; j >= 0; j--)
                {
                    levels.get(i).get(j).update();
                }
            }
        }
        liveTime++;
        if(cameraFovInertia > 0)
        {
            cameraFovInertia--;
            cameraFov += cameraFovPerScroll * (double)cameraFovInertia / 10D;
        }
        else if(cameraFovInertia < 0)
        {
            cameraFovInertia++;
            cameraFov += cameraFovPerScroll * (double)cameraFovInertia / 10D;
        }
        if(cameraZoomInertia > 0)
        {
            cameraZoomInertia--;
            cameraZoom += cameraZoomPerScroll * ((cameraZoom < 1.0F) ? ((cameraZoom + 0.5F) / 1.5F) : 1.0F ) * ((double)cameraZoomInertia / 10D);
        }
        else if(cameraZoomInertia < 0)
        {
            cameraZoomInertia++;
            cameraZoom += cameraZoomPerScroll * ((cameraZoom < 1.0F) ? ((cameraZoom + 0.5F) / 1.5F) : 1.0F ) * ((double)cameraZoomInertia / 10D);
        }
        for(ProjectInfo proj : projectManager.projects)
        {
            if(liveTime - proj.lastAutosave > 20 * 60 * 5 && !proj.autosaved)
            {
                File file = new File(ResourceHelper.getAutosaveDir(), proj.modelName + "-TabulaAutosave-" + Minecraft.getSystemTime() + ".tbl");
                if(ProjectInfo.saveProject(proj, file))
                {
                    //get the last few files in this name, delete them off.
                    long timestamp = 0;
                    File oldestAutosave = null;
                    int count = 0;
                    File[] files = ResourceHelper.getAutosaveDir().listFiles();
                    for(File save : files)
                    {
                        if(!save.isDirectory() && save.getName().endsWith(".tbl") && save.getName().startsWith(proj.modelName + "-TabulaAutosave-"))
                        {
                            count++;
                            String stamp = save.getName().substring((proj.modelName + "-TabulaAutosave-").length(), save.getName().length() - 4);//remove the ".tbl"
                            try
                            {
                                long time = Long.parseLong(stamp);
                                if(time < timestamp || timestamp == 0)
                                {
                                    timestamp = time;
                                    oldestAutosave = save;
                                }
                            }
                            catch(NumberFormatException e)
                            {
                            }
                        }
                    }
                    if(oldestAutosave != null && count > 5)
                    {
                        oldestAutosave.delete();
                    }
                }
                proj.lastAutosave = liveTime;
                proj.autosaved = true;
            }
        }
        if(wantToExit)
        {
            if(!remoteSession)
            {
                boolean canClose = true;
                for(int i = levels.size() - 1; i >= 0 ; i--)
                {
                    for(int j = levels.get(i).size() - 1; j >= 0; j--)
                    {
                        Window window = levels.get(i).get(j);
                        if(window instanceof WindowSaveBeforeClosing)
                        {
                            canClose = false;
                        }
                    }
                }
                if(canClose)
                {
                    for(int i = projectManager.projects.size() - 1; i >= 0; i--)
                    {
                        ProjectInfo project = projectManager.projects.get(i);
                        closeProject(project);
                        if(!project.saved)
                        {
                            canClose = false;
                            break;
                        }
                        else
                        {
                            project.destroy();
                        }
                    }
                    if(canClose)
                    {
                        this.mc.displayGuiScreen((GuiScreen)null);
                        this.mc.setIngameFocus();
                    }
                }
            }
        }
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float f)
    {
        //TODO a reset all windows button for people who "accidentally" drag the window out of the screen
        //TODO Do not close window if it is docked?
        Minecraft mc = Minecraft.getMinecraft();

        GL11.glPushMatrix();
        GL11.glEnable(GL11.GL_CULL_FACE);
        GL11.glEnable(GL11.GL_DEPTH_TEST);
        GL11.glEnable(GL11.GL_NORMALIZE);
        GL11.glDepthFunc(GL11.GL_LEQUAL);
        GL11.glDepthMask(true);

        if(cameraFov < 15F)
        {
            cameraFov = 15F;
        }
        else if(cameraFov > 160F)
        {
            cameraFov = 160F;
        }

        ScaledResolution resolution = new ScaledResolution(mc, mc.displayWidth, mc.displayHeight);
        GL11.glMatrixMode(GL11.GL_PROJECTION);
        GL11.glLoadIdentity();
        Project.gluPerspective(cameraFov, (float)(resolution.getScaledWidth_double() / resolution.getScaledHeight_double()), 1F, 10000F);
        GL11.glMatrixMode(GL11.GL_MODELVIEW);
        GL11.glLoadIdentity();
        GL11.glClearColor((float)Theme.workspaceBackground[0] / 255F, (float)Theme.workspaceBackground[1] / 255F, (float)Theme.workspaceBackground[2] / 255F, 255F);
        GL11.glClear(GL11.GL_COLOR_BUFFER_BIT);

        if (Mouse.isButtonDown(0) && !mouseLeftDown) {
            modelSelector.onClick(mouseX, mouseY);
        }

        renderWorkspace(mouseX, mouseY, f);

        GL11.glMatrixMode(GL11.GL_PROJECTION);
        GL11.glLoadIdentity();
        GL11.glOrtho(0.0D, resolution.getScaledWidth_double(), resolution.getScaledHeight_double(), 0.0D, -5000.0D, 5000.0D);
        GL11.glMatrixMode(GL11.GL_MODELVIEW);
        GL11.glLoadIdentity();

        GL11.glClear(GL11.GL_DEPTH_BUFFER_BIT);

        hovering = false;
        boolean hasClicked = false;
        boolean onWindow = false;
        Element prevElementSelected = elementSelected;
        elementSelected = null;

        GL11.glTranslatef(0F, 0F, 1000F);
        for(int i = levels.size() - 1; i >= 0 ; i--)
        {
            for(int j = levels.get(i).size() - 1; j >= 0; j--)
            {
                Window window = levels.get(i).get(j);
                if(mouseX >= window.posX && mouseX <= window.posX + window.getWidth() && mouseY >= window.posY && mouseY <= window.posY + window.getHeight())
                {
                    onWindow = true;
                    if(!hasClicked && liveTime > 5)
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
        RendererHelper.endGlScissor();//end scissor in case any window does it incorrectly.

        if(!hasClicked)
        {
            if((Mouse.isButtonDown(0) && !mouseLeftDown || Mouse.isButtonDown(1) && !mouseRightDown || Mouse.isButtonDown(2) && !mouseMiddleDown) && prevElementSelected != null && !(mouseX >= prevElementSelected.getPosX() && mouseX <= prevElementSelected.getPosX() + prevElementSelected.width && mouseY >= prevElementSelected.getPosY() && mouseY <= prevElementSelected.getPosY() + prevElementSelected.height))
            {
                prevElementSelected.deselected();
            }
            else
            {
                elementSelected = prevElementSelected;
            }
        }
        else if(elementSelected != prevElementSelected)
        {
            if(elementSelected != null)
            {
                elementSelected.selected();
            }
            if(prevElementSelected != null)
            {
                prevElementSelected.deselected();
            }
        }
        else
        {
            elementSelected = prevElementSelected;
        }

        if(clicked)
        {
            if(GuiScreen.isShiftKeyDown() || Mouse.isButtonDown(2))
            {
                float factor = 0.0125F;
                cameraOffsetX += (prevMouseX - mouseX) * factor;
                cameraOffsetY -= (prevMouseY - mouseY) * factor;
            }
            else
            {
                if(GuiScreen.isCtrlKeyDown())
                {
                    float factor = 0.0125F;
                    cameraZoom += (prevMouseY - mouseY) * factor;
                }
                else
                {
                    float factor = 0.5F;
                    cameraYaw -= (prevMouseX - mouseX) * factor;
                    cameraPitch += (prevMouseY - mouseY) * factor;
                }
            }

            prevMouseX = mouseX;
            prevMouseY = mouseY;
        }
        int scroll = Mouse.getDWheel();
        if(!onWindow)
        {
            if(scroll != 0)
            {
                if(GuiScreen.isShiftKeyDown())
                {
                    cameraFov += cameraFovPerScroll * (scroll / 120F);
                    cameraFovInertia = scroll > 0 ? 10 : -10;
                }
                else
                {
                    cameraZoom += cameraZoomPerScroll * ((cameraZoom < 1.0F) ? ((cameraZoom + 0.5F) / 1.5F) : 1.0F) * (scroll / 120F);
                    cameraZoomInertia = scroll > 0 ? 10 : -10;
                }
            }

            if(Mouse.isButtonDown(1) && !mouseRightDown || Mouse.isButtonDown(2) && !mouseMiddleDown)
            {
                clicked = true;
                prevMouseX = mouseX;
                prevMouseY = mouseY;
            }
        }
        if(!Mouse.isButtonDown(1) && !Mouse.isButtonDown(2))
        {
            clicked = false;
        }

        if(!hovering)
        {
            elementHovered = null;
            hoverTime = 0;
        }
        else if(elementHovered != null)
        {
            boolean activated = false;
            if(scroll != 0)//scroll up
            {
                activated = elementHovered.mouseScroll(mouseX - elementHovered.parent.posX, mouseY - elementHovered.parent.posY, (int)Math.round(scroll / 120));
            }
            if(activated)
            {
                if(elementSelected != null)
                {
                    elementSelected.deselected();
                }
                elementHovered.onClick(mouseX - elementHovered.parent.posX, mouseY - elementHovered.parent.posY, 2);
                elementSelected = elementHovered;
            }
            if(hoverTime > 20 && elementHovered.tooltip() != null) //1s to draw tooltip
            {
                GL11.glTranslatef(0F, 0F, 20F * levels.size());
                String tooltip = StatCollector.translateToLocal(elementHovered.tooltip());
                int xOffset = 5;
                int yOffset = 20;
                int size = fontRendererObj.getStringWidth(tooltip) + ((Window.BORDER_SIZE - 1) * 2);
                if(width - mouseX < size)
                {
                    xOffset -= size - (width - mouseX) + 20;
                }
                if(height - (mouseY + 12 + yOffset) < 0)
                {
                    yOffset = -20;
                }
                RendererHelper.drawColourOnScreen(Theme.windowBorder[0], Theme.windowBorder[1], Theme.windowBorder[2], 255, mouseX + xOffset, mouseY + yOffset, fontRendererObj.getStringWidth(tooltip) + ((Window.BORDER_SIZE - 1) * 2), 12, 0);
                RendererHelper.drawColourOnScreen(Theme.windowBackground[0], Theme.windowBackground[1], Theme.windowBackground[2], 255, mouseX + xOffset + 1, mouseY + yOffset + 1, fontRendererObj.getStringWidth(tooltip) + ((Window.BORDER_SIZE - 1) * 2) - 2, 12 - 2, 0);
                fontRendererObj.drawString(tooltip, mouseX + xOffset + (Window.BORDER_SIZE - 1), mouseY + yOffset + (Window.BORDER_SIZE - 1), Theme.getAsHex(Theme.font), false);
                //            RendererHelper.drawColourOnScreen(34, 34, 34, 255, posX + BORDER_SIZE, posY + BORDER_SIZE, getWidth() - (BORDER_SIZE * 2), getHeight() - (BORDER_SIZE * 2), 0);
            }
        }

        if(elementSelected == null)
        {
            if(Keyboard.isKeyDown(Keyboard.KEY_DELETE) && !keyDeleteDown)
            {
                windowControls.selectedObject = null;
                windowControls.refresh = true;

                for(Element e : windowModelTree.elements)
                {
                    if(e.id == 2)
                    {
                        windowModelTree.elementTriggered(e);
                    }
                }
            }
            if(GuiScreen.isCtrlKeyDown())
            {
                if(Keyboard.isKeyDown(Keyboard.KEY_X) && !keyXDown)
                {
                    cut();
                }
                if(Keyboard.isKeyDown(Keyboard.KEY_C) && !keyCDown)
                {
                    copy();
                }
                if(Keyboard.isKeyDown(Keyboard.KEY_V) && !keyVDown && cubeCopied != null)
                {
                    paste(GuiScreen.isShiftKeyDown());
                }
                if(Keyboard.isKeyDown(Keyboard.KEY_Z) && !keyZDown || Keyboard.isKeyDown(Keyboard.KEY_Y) && !keyYDown)
                {
                    switchState(Keyboard.isKeyDown(Keyboard.KEY_Y) || !GuiScreen.isShiftKeyDown());
                }
            }
        }

        GL11.glPopMatrix();

        mouseLeftDown = Mouse.isButtonDown(0);
        mouseRightDown = Mouse.isButtonDown(1);
        mouseMiddleDown = Mouse.isButtonDown(2);
        keyDeleteDown = Keyboard.isKeyDown(Keyboard.KEY_DELETE);
        keyXDown = Keyboard.isKeyDown(Keyboard.KEY_X);
        keyCDown = Keyboard.isKeyDown(Keyboard.KEY_C);
        keyVDown = Keyboard.isKeyDown(Keyboard.KEY_V);
        keyZDown = Keyboard.isKeyDown(Keyboard.KEY_Z);
        keyYDown = Keyboard.isKeyDown(Keyboard.KEY_Y);

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
                    int moveX = windowDragged.clickX - (mouseX - windowDragged.posX);
                    int moveY = windowDragged.clickY - (mouseY - windowDragged.posY);
                    if(windowDragged.docked < 0)
                    {
                        windowDragged.posX -= moveX;
                        windowDragged.posY -= moveY;
                    }
                    else
                    {
                        if(Math.sqrt(moveX * moveX + moveY + moveY) > 5)
                        {
                            removeFromDock(windowDragged);
                            windowDragged.posX -= moveX;
                            windowDragged.posY -= moveY;
                        }
                    }


                    boolean tabbed = false;
                    for(int i = levels.size() - 1; i >= 0 ; i--)
                    {
                        for(int j = levels.get(i).size() - 1; j >= 0; j--)
                        {
                            Window window = levels.get(i).get(j);
                            //TODO if in dock....?
                            if(tabbed || window instanceof WindowTopDock || window == windowDragged)
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

                    if(mouseX <= 10)
                    {
                        addToDock(0, windowDragged);
                        windowDragged = null;
                    }
                    if(mouseX >= width - 10)
                    {
                        addToDock(1, windowDragged);
                        windowDragged = null;
                    }
                    if(mouseY >= height - 10)
                    {
                        addToDock(2, windowDragged);
                        windowDragged = null;
                    }

                    if(windowDragged != null)
                    {
                        windowDragged.resized();
                    }
                }
                if(dragType >= 2)
                {
                    int bordersClicked = dragType - 3;
                    if((bordersClicked & 1) == 1 && !((windowDragged.docked == 0 || windowDragged.docked == 1) && !levels.get(windowDragged.docked).isEmpty() && levels.get(windowDragged.docked).get(0) == windowDragged)) // top
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
                    if((bordersClicked >> 1 & 1) == 1 && windowDragged.docked != 0) // left
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
                    if((bordersClicked >> 3 & 1) == 1 && windowDragged.docked != 1) // right
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

                    if(windowDragged.docked >= 0)
                    {
                        redock(windowDragged.docked, windowDragged);
                    }
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
                if(elementDragged instanceof ElementWindow)
                {
                    ((WindowTabs)((ElementWindow)elementDragged).parent).detach((ElementWindow)elementDragged);

                    ElementWindow element = (ElementWindow)elementDragged;

                    windowDragged = element.mountedWindow;
                    windowDragged.docked = -1;
                    dragType = 1;

                    windowDragged.width = element.oriWidth;
                    windowDragged.height = element.oriHeight;

                    windowDragged.posX = mouseX - (windowDragged.getWidth() / 2);
                    windowDragged.posY = mouseY - 6;

                    windowDragged.resized();

                    elementDragged = null;
                }
            }
        }

        GL11.glMatrixMode(GL11.GL_PROJECTION);
        GL11.glLoadIdentity();
        GL11.glOrtho(0.0D, resolution.getScaledWidth_double(), resolution.getScaledHeight_double(), 0.0D, 1000.0D, 3000.0D);
        GL11.glMatrixMode(GL11.GL_MODELVIEW);
        GL11.glLoadIdentity();
    }

    public void applyCamera() {
        float scale = 100F;
        GL11.glScalef(scale, scale, scale);
        GL11.glTranslatef(0F, -1.75F, -9F);
        GL11.glScalef(cameraZoom, cameraZoom, cameraZoom);
        GL11.glScalef(-1.0F, -1.0F, 1.0F);
        GL11.glTranslatef(cameraOffsetX, cameraOffsetY, 0.0F);
        GL11.glRotatef(-15F + cameraPitch + 180F, 1.0F, 0.0F, 0.0F);
        GL11.glRotatef(-38F + cameraYaw, 0.0F, 1.0F, 0.0F);
    }

    public void applyModelTranslation() {
        GL11.glTranslatef(0.0F, 2.0005F, 0.0F);
        GL11.glScalef(-1.0F, -1.0F, 1.0F);
    }

    public void renderWorkspace(int mouseX, int mouseY, float f)
    {
        if(cameraZoom < 0.05F)
        {
            cameraZoom = 0.05F;
        }
        else if(cameraZoom > 15F)
        {
            cameraZoom = 15F;
        }

        Minecraft.getMinecraft().renderEngine.bindTexture(TextureMap.locationBlocksTexture);

        GL11.glPushMatrix();

        GL11.glEnable(GL11.GL_LIGHTING);

        Tessellator tessellator = Tessellator.instance;

        tessellator.setBrightness(15728880);

        RenderHelper.enableGUIStandardItemLighting();

        int ii = 15728880;
        int jj = ii % 65536;
        int kk = ii / 65536;
        OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, (float)jj / 1.0F, (float)kk / 1.0F);

        applyCamera();

        Block block = Blocks.planks;
        renderBlocks.setRenderBoundsFromBlock(block);
        renderBlocks.enableAO = false;

        boolean enableWood = false;

        for(int i = 0; i < levels.get(3).get(0).elements.size(); i++)
        {
            if(levels.get(3).get(0).elements.get(i).id == WindowTopDock.ID_WOOD)
            {
                ElementToggle toggle = (ElementToggle)levels.get(3).get(0).elements.get(i);
                enableWood = toggle.toggledState;
            }
        }

        if(enableWood)
        {
            tessellator.startDrawingQuads();
            tessellator.setNormal(0.0F, -1.0F, 0.0F);
            renderBlocks.renderFaceYNeg(block, -0.5D, -0.5D, -0.5D, renderBlocks.getBlockIconFromSideAndMetadata(block, 0, 1));
            tessellator.draw();
            tessellator.startDrawingQuads();
            tessellator.setNormal(0.0F, 1.0F, 0.0F);
            renderBlocks.renderFaceYPos(block, -0.5D, -0.5D, -0.5D, renderBlocks.getBlockIconFromSideAndMetadata(block, 1, 1));
            tessellator.draw();
            tessellator.startDrawingQuads();
            tessellator.setNormal(0.0F, 0.0F, -1.0F);
            renderBlocks.renderFaceZNeg(block, -0.5D, -0.5D, -0.5D, renderBlocks.getBlockIconFromSideAndMetadata(block, 2, 1));
            tessellator.draw();
            tessellator.startDrawingQuads();
            tessellator.setNormal(0.0F, 0.0F, 1.0F);
            renderBlocks.renderFaceZPos(block, -0.5D, -0.5D, -0.5D, renderBlocks.getBlockIconFromSideAndMetadata(block, 3, 1));
            tessellator.draw();
            tessellator.startDrawingQuads();
            tessellator.setNormal(-1.0F, 0.0F, 0.0F);
            renderBlocks.renderFaceXNeg(block, -0.5D, -0.5D, -0.5D, renderBlocks.getBlockIconFromSideAndMetadata(block, 4, 1));
            tessellator.draw();
            tessellator.startDrawingQuads();
            tessellator.setNormal(1.0F, 0.0F, 0.0F);
            renderBlocks.renderFaceXPos(block, -0.5D, -0.5D, -0.5D, renderBlocks.getBlockIconFromSideAndMetadata(block, 5, 1));
            tessellator.draw();
        }

        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);

        if(projectManager.selectedProject != -1)
        {
            GL11.glPushMatrix();
            GL11.glDisable(GL11.GL_CULL_FACE);
            applyModelTranslation();
            GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
            ProjectInfo info = projectManager.projects.get(projectManager.selectedProject);

            ArrayList<CubeInfo> selected = new ArrayList<CubeInfo>();
            for(ElementListTree.Tree tree : windowModelTree.modelList.trees)
            {
                if(tree.selected)
                {
                    if(tree.attachedObject instanceof CubeGroup)
                    {
                        addElementsForSelection((CubeGroup)tree.attachedObject, selected);
                    }
                    if(tree.attachedObject instanceof CubeInfo)
                    {
                        selected.add((CubeInfo)tree.attachedObject);
                    }
                }
            }
            ArrayList<CubeInfo> hidden = getHiddenElements();

            if(windowTexture.imageId != -1)
            {
                GL11.glBindTexture(GL11.GL_TEXTURE_2D, windowTexture.imageId);

                info.model.render(0.0625F, selected, hidden, cameraZoom, true, 0);
                info.model.render(0.0625F, selected, hidden, cameraZoom, true, 1);
            }
            else
            {
                GL11.glDisable(GL11.GL_TEXTURE_2D);
                GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);

                info.model.render(0.0625F, selected, hidden, cameraZoom, false, 0);
                info.model.render(0.0625F, selected, hidden, cameraZoom, false, 1);

                GL11.glEnable(GL11.GL_TEXTURE_2D);
            }
            GL11.glEnable(GL11.GL_CULL_FACE);
            GL11.glPopMatrix();
        }

        GL11.glColor4f(1.0F, 1.0F, 1.0F, 0.5F);
        Minecraft.getMinecraft().getTextureManager().bindTexture(grid16);
        double dist = 0.125D;
        double pX = -3.5D - dist;
        double pY = 0.500125D;
        double pZ = -3.5D - dist;
        double w = 7 + (dist * 2);
        double l = 7 + (dist * 2);
        tessellator.startDrawingQuads();
        tessellator.addVertexWithUV(pX	  , pY, pZ + l, -0.125D, 7.125D);
        tessellator.addVertexWithUV(pX + w, pY, pZ + l, 7.125D, 7.125D);
        tessellator.addVertexWithUV(pX + w, pY, pZ, 7.125D, -0.125D);
        tessellator.addVertexWithUV(pX	  , pY, pZ, -0.125D, -0.125D);
        tessellator.addVertexWithUV(pX + w, pY, pZ + l, 7.125D, 7.125D);
        tessellator.addVertexWithUV(pX	  , pY, pZ + l, -0.125D, 7.125D);
        tessellator.addVertexWithUV(pX	  , pY, pZ, -0.125D, -0.125D);
        tessellator.addVertexWithUV(pX + w, pY, pZ, 7.125D, -0.125D);
        tessellator.draw();

        GL11.glDisable(GL11.GL_BLEND);

        RenderHelper.disableStandardItemLighting();

        GL11.glDisable(GL11.GL_LIGHTING);

        GL11.glPopMatrix();

        GL11.glPushMatrix();

        block = Blocks.furnace;
        //        Block block = Blocks.diamond_ore;

        Minecraft.getMinecraft().renderEngine.bindTexture(TextureMap.locationBlocksTexture);

        GL11.glTranslatef(width - (levels.get(1).isEmpty() ? 15F : 15F + levels.get(1).get(0).width), height - 15F, 3000F);
        float scale = 15F;
        GL11.glScalef(scale, scale, scale);
        GL11.glScalef(-1.0F, 1.0F, 1.0F);
        GL11.glRotatef(-15F + cameraPitch + 180F, 1.0F, 0.0F, 0.0F);
        GL11.glRotatef(-38F + cameraYaw + 90F, 0.0F, 1.0F, 0.0F);

        renderBlocks.renderBlockAsItem(block, 0, 1.0F);

        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);

        GL11.glColor4f(1.0F, 1.0F, 1.0F, 0.5F);
        Minecraft.getMinecraft().getTextureManager().bindTexture(orientationBase);
        dist = 0.125D;
        pX = -1D - dist;
        pY = -0.500125D;
        pZ = -1D - dist;
        w = 2 + (dist * 2);
        l = 2 + (dist * 2);
        tessellator.startDrawingQuads();
        tessellator.addVertexWithUV(pX, pY, pZ + l, 0.0D, 1.0D);
        tessellator.addVertexWithUV(pX + w, pY, pZ + l, 1.0D, 1.0D);
        tessellator.addVertexWithUV(pX + w, pY, pZ, 1.0D, 0.0D);
        tessellator.addVertexWithUV(pX	  , pY, pZ, 0.0D, 0.0D);
        tessellator.addVertexWithUV(pX + w, pY, pZ + l, 1.0D, 1.0D);
        tessellator.addVertexWithUV(pX	  , pY, pZ + l, 0.0D, 1.0D);
        tessellator.addVertexWithUV(pX	  , pY, pZ, 0.0D, 0.0D);
        tessellator.addVertexWithUV(pX + w, pY, pZ, 1.0D, 0.0D);
        tessellator.draw();
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);

        GL11.glDisable(GL11.GL_BLEND);

        GL11.glPopMatrix();
    }

    public ArrayList<CubeInfo> getHiddenElements() {
        ArrayList<CubeInfo> hidden = new ArrayList<CubeInfo>();
        for(ElementListTree.Tree tree : windowModelTree.modelList.trees)
        {
            if(tree.attachedObject instanceof CubeGroup)
            {
                addElementsForHiding((CubeGroup)tree.attachedObject, hidden);
            }
        }

        return hidden;
    }

    private void addElementsForSelection(CubeGroup group, ArrayList<CubeInfo> selected)
    {
        for(CubeGroup group1 : group.cubeGroups)
        {
            addElementsForSelection(group1, selected);
        }
        for(CubeInfo info : group.cubes)
        {
            addElementsForSelection(info, selected);
        }
    }

    private void addElementsForHiding(CubeGroup group, ArrayList<CubeInfo> selected)
    {
        if(group.hidden)
        {
            for(CubeGroup group1 : group.cubeGroups)
            {
                addElementsForSelection(group1, selected);
            }
            for(CubeInfo info : group.cubes)
            {
                addElementsForSelection(info, selected);
            }
        }
        for(CubeGroup group1 : group.cubeGroups)
        {
            addElementsForHiding(group1, selected);
        }
    }

    private void addElementsForSelection(CubeInfo cube, ArrayList<CubeInfo> selected)
    {
        selected.add(cube);
        for(CubeInfo child : cube.getChildren())
        {
            addElementsForSelection(child, selected);
        }
    }

    public void cut()
    {
        windowControls.selectedObject = null;
        windowControls.refresh = true;

        copy();

        for(Element e : windowModelTree.elements)
        {
            if(e.id == 2)
            {
                windowModelTree.elementTriggered(e);
            }
        }
    }

    public void copy()
    {
        for(ElementListTree.Tree tree : windowModelTree.modelList.trees)
        {
            if(tree.selected)
            {
                if(tree.attachedObject instanceof CubeInfo || tree.attachedObject instanceof CubeGroup)
                {
                    cubeCopied = tree.attachedObject;
                }
                break;
            }
        }
    }

    public void paste(boolean inPlace)
    {
        if(cubeCopied instanceof CubeInfo)
        {
            Gson gson = new Gson();
            String s = gson.toJson(cubeCopied);
            if(this.remoteSession)
            {

            }
            else
            {
                Tabula.proxy.tickHandlerClient.mainframe.createNewCube(this.projectManager.projects.get(this.projectManager.selectedProject).identifier, s, inPlace);
            }
        }
        else if(cubeCopied instanceof CubeGroup)
        {
            if(this.remoteSession)
            {

            }
            else
            {
                Tabula.proxy.tickHandlerClient.mainframe.copyGroupTo(this.projectManager.projects.get(this.projectManager.selectedProject).identifier, ((CubeGroup)cubeCopied).identifier, inPlace);
            }
        }
    }

    public void save(boolean close)
    {
        if(!this.projectManager.projects.isEmpty())
        {
            ProjectInfo proj = this.projectManager.projects.get(this.projectManager.selectedProject);
            boolean saveAs = true;
            boolean error = false;
            if(proj.saveFile != null && proj.saveFile.exists() && MD5Checksum.getMD5Checksum(proj.saveFile).equals(proj.saveFileMd5))
            {
                if(ProjectInfo.saveProject(proj, proj.saveFile))
                {
                    proj.saveFileMd5 = MD5Checksum.getMD5Checksum(proj.saveFile);
                    saveAs = false;
                    if(close)
                    {
                        closeProject(proj);
                    }
                }
                else
                {
                    error = true;
                }
            }
            if(saveAs)
            {
                this.addWindowOnTop(new WindowSaveAs(this, this.width / 2 - 100, this.height / 2 - 80, 200, 100, 200, 100, close).putInMiddleOfScreen());
            }
            if(error)
            {
                this.addWindowOnTop(new WindowPopup(this, 0, 0, 180, 80, 180, 80, "window.saveAs.failed").putInMiddleOfScreen());
            }
        }
    }

    public void switchState(boolean undo)
    {
        if(!projectManager.projects.isEmpty())
        {
            if(remoteSession)
            {
                //TODO This
            }
            else
            {
                Tabula.proxy.tickHandlerClient.mainframe.switchState(projectManager.projects.get(projectManager.selectedProject).identifier, undo);
            }
        }
    }

    public void closeProject(ProjectInfo project)
    {
        if(!project.saved)
        {
            projectManager.changeProject(project);
            addWindowOnTop(new WindowSaveBeforeClosing(this, projectManager.projects.get(projectManager.selectedProject)).putInMiddleOfScreen());
        }
        else
        {
            Tabula.proxy.tickHandlerClient.mainframe.closeProject(project.identifier);
        }
    }

    public void addToDock(int dock, Window window)
    {
        //TODO docking to the lower mid
        if(window != null && window.docked < 0)
        {
            if(window.minimized)
            {
                window.toggleMinimize();
            }
            ArrayList<Window> docked = levels.get(dock);
            window.docked = dock;
            window.oriHeight = window.height;
            window.oriWidth = window.width;
            docked.add(window);
            for(int i = VARIABLE_LEVEL; i < levels.size(); i++)
            {
                levels.get(i).remove(window);
            }

            redock(dock, null);
        }
    }

    public void removeFromDock(Window window)
    {
        for(int i = 2; i >= 0; i--)
        {
            ArrayList<Window> docked = levels.get(i);
            for(int j = docked.size() - 1; j >= 0; j--)
            {
                Window window1 = docked.get(j);
                if(window1 == window)
                {
                    docked.remove(j);

                    redock(i, null);

                    break;
                }
            }
        }
        window.docked = -1;
        window.height = window.oriHeight;
        window.width = window.oriWidth;

        addWindowOnTop(window);

        window.resized();
    }

    public void redock(int dock, Window pref)
    {
        ArrayList<Window> docked = levels.get(dock);
        int prefInt = -2;
        if(pref != null)
        {
            for(int j = 0; j < docked.size(); j++)
            {
                if(docked.get(j) == pref)
                {
                    prefInt = j;
                }
            }
        }
        for(int j = 0; j < docked.size(); j++)
        {
            Window window = docked.get(j);
            if(dock == 0)
            {
                window.posX = -1;
            }
            else if(dock == 1)
            {
                window.posX = width - window.getWidth() + 1;
            }
            if(dock <= 1)
            {
                if(prefInt != -2)
                {
                    docked.get(0).width = docked.get(prefInt).width;
                }
                else
                {
                    if(j == 0)
                    {
                        window.posY = TOP_DOCK_HEIGHT;
                    }
                    else
                    {
                        window.width = docked.get(0).width;
                        window.posY = docked.get(j - 1).posY + (docked.get(j - 1).minimized ? 12 : (docked.get(j - 1).height + docked.get(j - 1).posY + 2 >= height) ? docked.get(j - 1).oriHeight : docked.get(j - 1).height);
                        docked.get(j - 1).height = window.posY - docked.get(j - 1).posY + 2;
                    }
                }
                if(j - 1 == prefInt)
                {
                    window.height += window.posY - (docked.get(j - 1).posY + docked.get(j - 1).height) + 2;
                    window.posY -= window.posY - (docked.get(j - 1).posY + docked.get(j - 1).height) + 2;
                }
                if(j + 1 == prefInt)
                {
                    window.height = docked.get(j + 1).posY - window.posY + 2;
                    if(window.height < window.minHeight + 2)
                    {
                        window.height = window.minHeight + 2;
                        docked.get(prefInt).posY = window.posY + window.height - 2;
                        windowDragged = null;
                        dragType = 0;
                    }
                }
                window.width = docked.get(0).width;
            }
            window.resized();
        }
        screenResize();
    }

    public void screenResize()
    {
        for(int i = 0; i <= 3; i++)
        {
            ArrayList<Window> docked = levels.get(i);
            for(int j = 0; j < docked.size(); j++)
            {
                Window window = docked.get(j);

                if(i == 0)
                {
                    window.posX = -1;
                }
                else if(i == 1)
                {
                    window.posX = width - window.getWidth() + 1;
                }
                if(j == docked.size() - 1)
                {
                    window.height = height - window.posY + 1;
                }

                if(window.posX == (oldWidth - window.width) / 2 && window.posY == (oldHeight - window.height) / 2)
                {
                    window.posX = (width - window.width) / 2;
                    window.posY = (height - window.height) / 2;
                }

                window.resized();
            }
        }

        for(int i = 4; i < levels.size(); i++)
        {
            ArrayList<Window> docked = levels.get(i);
            for(int j = 0; j < docked.size(); j++)
            {
                Window window = docked.get(j);

                if(window.posX == (oldWidth - window.width) / 2 && window.posY == (oldHeight - window.height) / 2)
                {
                    window.putInMiddleOfScreen();
                }
            }
        }

        oldWidth = width;
        oldHeight = height;
    }

    public void removeWindow(Window window, boolean checkTab)
    {
        for(int i = levels.size() - 1; i >= 0 ; i--)
        {
            for(int j = levels.get(i).size() - 1; j >= 0; j--)
            {
                Window window1 = levels.get(i).get(j);
                if(window1 instanceof WindowTabs && !(window instanceof WindowTabs) && checkTab)
                {
                    WindowTabs tabs = (WindowTabs)window1;
                    for(ElementWindow tab : tabs.tabs)
                    {
                        if(tab.mountedWindow == window)
                        {
                            Window win = WindowTabs.detach(tab);
                            win.docked = -1;

                            win.width = tab.oriWidth;
                            win.height = tab.oriHeight;

                            win.posX = (width / 2) - (win.width / 2);
                            win.posY = (height / 2) - (win.height / 2);

                            win.resized();

                            removeWindow(win);

                            return;
                        }
                    }
                }
                if(window1 == window)
                {
                    if(i < VARIABLE_LEVEL)
                    {
                        removeFromDock(window1);
                        removeWindow(window1, checkTab);
                    }
                    else
                    {
                        levels.get(i).remove(j);
                        if(levels.get(i).isEmpty())
                        {
                            levels.remove(i);
                        }
                    }
                    break;
                }
            }
        }
    }

    public void removeWindow(Window window)
    {
        removeWindow(window, false);
    }

    public void addWindowOnTop(Window window)
    {
        if(!window.allowMultipleInstances() && window.getClass() != Window.class)
        {
            for(int i = levels.size() - 1; i >= 0 ; i--)
            {
                for(int j = levels.get(i).size() - 1; j >= 0; j--)
                {
                    Window window1 = levels.get(i).get(j);
                    if(window == window1)
                    {
                        continue;
                    }
                    if(window1 instanceof WindowTabs)
                    {
                        WindowTabs tabs = (WindowTabs)window1;
                        for(ElementWindow tab : tabs.tabs)
                        {
                            if(tab.mountedWindow.getClass() == window.getClass())
                            {
                                Window win = WindowTabs.detach(tab);
                                win.docked = -1;

                                win.width = tab.oriWidth;
                                win.height = tab.oriHeight;

                                win.posX = (width / 2) - (win.width / 2);
                                win.posY = (height / 2) - (win.height / 2);

                                win.resized();

                                bringWindowToFront(win);
                                return;
                            }
                        }
                    }
                    else
                    {
                        if(window1.getClass() == window.getClass())
                        {
                            if(window1.docked >= 0)
                            {
                                removeFromDock(window1);
                            }
                            window1.docked = -1;

                            if(window1.height > height)
                            {
                                window1.height = window1.minHeight;
                            }
                            if(window1.width > width)
                            {
                                window1.width = window1.minWidth;
                            }

                            window1.posX = (width / 2) - (window1.width / 2);
                            window1.posY = (height / 2) - (window1.height / 2);

                            window1.resized();
                            bringWindowToFront(window1);
                            return;
                        }
                    }
                }
            }
        }
        ArrayList<Window> topLevel = new ArrayList<Window>();
        topLevel.add(window);
        levels.add(topLevel);
    }

    public void bringWindowToFront(Window window)
    {
        if(window instanceof WindowTopDock)
        {
            return;
        }
        for(int i = levels.size() - 1; i >= 0 ; i--)
        {
            for(int j = levels.get(i).size() - 1; j >= 0; j--)
            {
                Window window1 = levels.get(i).get(j);
                //TODO inform docking of change.
                if(window1 == window && window.docked < 0 && !(i == levels.size() - 1 && levels.get(i).size() == 1))
                {
                    ArrayList<Window> topLevel = new ArrayList<Window>();
                    topLevel.add(window1);
                    levels.get(i).remove(j);
                    if(levels.get(i).isEmpty() && i >= VARIABLE_LEVEL)
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
            boolean wantedToExit = wantToExit;
            wantToExit = true;
            if(wantedToExit)
            {
                for(int i = levels.size() - 1; i >= 0 ; i--)
                {
                    for(int j = levels.get(i).size() - 1; j >= 0; j--)
                    {
                        Window window = levels.get(i).get(j);
                        if(window instanceof WindowSaveBeforeClosing)
                        {
                            wantToExit = false;
                            removeWindow(window);
                            break;
                        }
                    }
                }
            }
        }
        else if(elementSelected != null)
        {
            elementSelected.keyInput(c, key);
        }
    }

    @Override
    public void onGuiClosed()
    {
        Keyboard.enableRepeatEvents(false);

        for(int i = levels.size() - 1; i >= 0; i--)
        {
            for(int j = levels.get(i).size() - 1; j >= 0; j--)
            {
                ((Window)levels.get(i).get(j)).shutdown();
            }
        }

        Minecraft.getMinecraft().gameSettings.guiScale = oriScale;
        if(!remoteSession)
        {
            Tabula.proxy.tickHandlerClient.mainframe.shutdown();
        }
    }

    public FontRenderer getFontRenderer()
    {
        return fontRendererObj;
    }
}
