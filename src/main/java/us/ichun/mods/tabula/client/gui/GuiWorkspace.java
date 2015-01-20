package us.ichun.mods.tabula.client.gui;

import com.google.common.io.ByteStreams;
import com.google.gson.Gson;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.StatCollector;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;
import org.lwjgl.util.glu.Project;
import us.ichun.mods.ichunutil.client.gui.window.IWorkspace;
import us.ichun.mods.ichunutil.client.gui.window.Window;
import us.ichun.mods.ichunutil.client.gui.window.element.Element;
import us.ichun.mods.ichunutil.client.gui.window.element.ElementMinimize;
import us.ichun.mods.ichunutil.client.gui.window.element.ElementToggle;
import us.ichun.mods.ichunutil.client.render.RendererHelper;
import us.ichun.mods.ichunutil.common.core.util.IOUtil;
import us.ichun.mods.ichunutil.common.module.tabula.common.project.ProjectInfo;
import us.ichun.mods.ichunutil.common.module.tabula.common.project.components.Animation;
import us.ichun.mods.ichunutil.common.module.tabula.common.project.components.AnimationComponent;
import us.ichun.mods.ichunutil.common.module.tabula.common.project.components.CubeGroup;
import us.ichun.mods.ichunutil.common.module.tabula.common.project.components.CubeInfo;
import us.ichun.mods.tabula.client.core.ModelSelector;
import us.ichun.mods.tabula.client.core.ResourceHelper;
import us.ichun.mods.tabula.client.gui.window.*;
import us.ichun.mods.tabula.client.gui.window.element.ElementListTree;
import us.ichun.mods.tabula.client.mainframe.core.ProjectHelper;
import us.ichun.mods.tabula.common.Tabula;
import us.ichun.mods.tabula.common.packet.PacketEndSession;
import us.ichun.mods.tabula.common.packet.PacketGenericMethod;
import us.ichun.mods.tabula.common.packet.PacketRemoveListener;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Map;

public class GuiWorkspace extends IWorkspace
{
    public float renderTick;

    public int oriScale;
    public final boolean remoteSession;
    public boolean isEditor;
    public boolean sessionEnded;
    public String host;
    public int hostX;
    public int hostY;
    public int hostZ;

    public ResourceLocation grid16 = new ResourceLocation("tabula", "textures/workspace/grid16.png");
    public ResourceLocation orientationBase = new ResourceLocation("tabula", "textures/workspace/orientationBase.png");

    public ArrayList<String> editors = new ArrayList<String>();
    public ArrayList<String> listeners = new ArrayList<String>();

    public boolean keyDeleteDown;
    public boolean keyXDown;
    public boolean keyCDown;
    public boolean keyVDown;
    public boolean keyZDown;
    public boolean keyYDown;
    public boolean keySDown;
    public boolean keyNDown;
    public boolean keyODown;
    public boolean keyHomeDown;
    public boolean keyEndDown;
    public boolean keyTabDown;

    public Object cubeCopied;

    public WindowProjectSelection projectManager;
    public WindowControls windowControls;
    public WindowTexture windowTexture;
    public WindowModelTree windowModelTree;
    public WindowChat windowChat;
    public WindowAnimate windowAnimate;

    public boolean init;

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

    public int controlDrag = -1;
    public int controlDragX;
    public int controlDragY;

    public boolean wantToExit;

    public boolean openNextNewProject;

    private ModelSelector modelSelector = new ModelSelector(this, 256);

    public GuiWorkspace(int scale, boolean remote, boolean editing, String name, int i, int j, int k)
    {
        levels = new ArrayList<ArrayList<Window>>() {{
            add(0, new ArrayList<Window>()); // dock left
            add(1, new ArrayList<Window>()); // dock right
            add(2, new ArrayList<Window>()); // dock btm
            add(3, new ArrayList<Window>()); // dock top
            add(4, new ArrayList<Window>()); // chat level
        }};

        oriScale = scale;

        File defaultTheme = new File(ResourceHelper.getThemesDir(), "default.json");

        Theme theme = new Theme();

        try
        {
            InputStream con = new FileInputStream(defaultTheme);
            String data = new String(ByteStreams.toByteArray(con));
            con.close();

            Theme.loadTheme(theme, (new Gson()).fromJson(data, Theme.class));
        }
        catch(Exception e)
        {
            Tabula.console("Error reading default theme!", true);
            e.printStackTrace();
        }

        String fav = Tabula.config.getString("favTheme");
        if(!(fav.isEmpty() || fav.equalsIgnoreCase("default")))
        {
            File userTheme = new File(ResourceHelper.getThemesDir(), fav + ".json");

            if(userTheme.exists())
            {
                try
                {
                    InputStream con = new FileInputStream(userTheme);
                    String data = new String(ByteStreams.toByteArray(con));
                    con.close();

                    Theme.loadTheme(theme, (new Gson()).fromJson(data, Theme.class));
                }
                catch(Exception e)
                {
                    Tabula.console("Error reading preferred theme!", true);
                    e.printStackTrace();
                }
            }
            else
            {
                Tabula.console("Preferred theme file does not exist!", true);
            }
        }

        currentTheme = theme;
        remoteSession = remote;
        isEditor = editing;
        host = name;
        hostX = i;
        hostY = j;
        hostZ = k;

        //        renderBlocks = new RenderBlocks();

        levels.get(3).add(new WindowTopDock(this, 0, 0, width, 20, 20, 20));
        projectManager = new WindowProjectSelection(this, 0, 0, width, 20, 20, 20);
        levels.get(3).add(projectManager);
    }

    @Override
    public void initGui()
    {
        super.initGui();
        if(!init)
        {
            windowControls = new WindowControls(this, width / 2 - 80, height / 2 - 125, 162, 295, 162, 295);
            windowTexture = new WindowTexture(this, width / 2 - 53, height / 2 - 100, 106, 100, 106, 88);
            windowModelTree = new WindowModelTree(this, width / 2 - 53, height / 2 - 125, 106, 250, 106, 250);
            windowAnimate = new WindowAnimate(this, 0, 0, 100, 100, 100, 100);
            addToDock(0, windowControls);
            addToDock(1, windowTexture);
            addToDock(1, windowModelTree);
            addToDock(2, windowAnimate);
            windowAnimate.toggleMinimize();

            windowChat = new WindowChat(this, -1000, -1000, 250, 180, 162, 50);
            levels.get(4).add(windowChat);

            if(host != null)
            {
                if(host.equals(Minecraft.getMinecraft().getSession().getUsername()))
                {
                    ProjectHelper.addSystemMessage(StatCollector.translateToLocal("system.hosting"));
                }
                else
                {
                    ProjectHelper.addSystemMessage(StatCollector.translateToLocalFormatted("system.hostingOther", host));
                }
                windowChat.toggleVisibility();

                String[] chatSettings = Tabula.config.getString("chatWindow").split(":");
                if(chatSettings.length == 8)
                {
                    try
                    {
                        windowChat.posX = Integer.parseInt(chatSettings[0]);
                        windowChat.posY = Integer.parseInt(chatSettings[1]);
                        windowChat.width = Integer.parseInt(chatSettings[2]);
                        windowChat.height = Integer.parseInt(chatSettings[3]);
                        if(Integer.parseInt(chatSettings[4]) >= 0)
                        {
                            addToDock(Integer.parseInt(chatSettings[4]), windowChat);
                        }
                        if(Boolean.parseBoolean(chatSettings[5]))
                        {
                            windowChat.toggleMinimize();
                        }
                        windowChat.oriWidth = Integer.parseInt(chatSettings[6]);
                        windowChat.oriHeight = Integer.parseInt(chatSettings[7]);
                    }
                    catch(Exception ignored){}
                    windowChat.resized();
                }
            }
            init = true;
        }
    }

    @Override
    public void updateScreen()
    {
        if(!init)
        {
            return;
        }
        super.updateScreen();
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
            for(Animation anim : proj.anims)
            {
                anim.update();
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
                        if(window instanceof WindowSaveBeforeClosing || window instanceof WindowSaveAs)
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
            else
            {
                this.mc.displayGuiScreen((GuiScreen)null);
                this.mc.setIngameFocus();
            }
        }
    }

    @Override
    public boolean canClickOnElement(Window window, Element element)
    {
        return !(projectManager.projects.isEmpty() && !window.interactableWhileNoProjects() && !(element instanceof ElementMinimize));
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float renderTick)
    {
        this.renderTick = renderTick;
        if(!init)
        {
            return;
        }
        //TODO a reset all windows button for people who "accidentally" drag the window out of the screen
        //TODO Do not close window if it is docked?
        Minecraft mc = Minecraft.getMinecraft();

        GlStateManager.pushMatrix();
        GlStateManager.enableCull();
        GlStateManager.enableDepth();
        GlStateManager.enableNormalize();
        GlStateManager.depthFunc(GL11.GL_LEQUAL);
        GlStateManager.depthMask(true);

        if(cameraFov < 15F)
        {
            cameraFov = 15F;
        }
        else if(cameraFov > 160F)
        {
            cameraFov = 160F;
        }

        ScaledResolution resolution = new ScaledResolution(mc, mc.displayWidth, mc.displayHeight);
        GlStateManager.matrixMode(GL11.GL_PROJECTION);
        GlStateManager.loadIdentity();
        Project.gluPerspective(cameraFov, (float)(resolution.getScaledWidth_double() / resolution.getScaledHeight_double()), 1F, 10000F);
        GlStateManager.matrixMode(GL11.GL_MODELVIEW);
        GlStateManager.loadIdentity();
        GlStateManager.clearColor(0F, 0F, 0F, 255F);
        GlStateManager.clear(GL11.GL_COLOR_BUFFER_BIT);

        if (Mouse.isButtonDown(0) && !mouseLeftDown) {
            modelSelector.onClick(mouseX, mouseY);
        }

        GlStateManager.clearColor((float)currentTheme.workspaceBackground[0] / 255F, (float)currentTheme.workspaceBackground[1] / 255F, (float)currentTheme.workspaceBackground[2] / 255F, 255F);
        GlStateManager.clear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);

        renderWorkspace(mouseX, mouseY, renderTick);

        GlStateManager.clear(GL11.GL_DEPTH_BUFFER_BIT);

        boolean onWindow = drawWindows(mouseX, mouseY);

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

        updateElementHovered(mouseX, mouseY, scroll);

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
                    paste(GuiScreen.isShiftKeyDown(), true);
                }
                if(Keyboard.isKeyDown(Keyboard.KEY_Z) && !keyZDown || Keyboard.isKeyDown(Keyboard.KEY_Y) && !keyYDown)
                {
                    switchState(Keyboard.isKeyDown(Keyboard.KEY_Y) || !GuiScreen.isShiftKeyDown());
                }
                if(Keyboard.isKeyDown(Keyboard.KEY_S) && !keySDown)
                {
                    if(GuiScreen.isShiftKeyDown())
                    {
                        this.addWindowOnTop(new WindowSaveAs(this, 0, 0, 200, 100, 200, 100, false).putInMiddleOfScreen());
                    }
                    else
                    {
                        save(false);
                    }
                }
                if(Keyboard.isKeyDown(Keyboard.KEY_N) && !keyNDown)
                {
                    this.addWindowOnTop(new WindowNewProject(this, this.width / 2 - 100, this.height / 2 - 80, 200, 160, 200, 160).putInMiddleOfScreen());
                }
                if(Keyboard.isKeyDown(Keyboard.KEY_O) && !keyODown)
                {
                    this.addWindowOnTop(new WindowOpenProject(this, this.width / 2 - 130, this.height / 2 - 160, 260, 320, 240, 160).putInMiddleOfScreen());
                }
                if(Keyboard.isKeyDown(Keyboard.KEY_TAB) && !keyTabDown)
                {
                    if(!projectManager.projects.isEmpty())
                    {
                        projectManager.selectedProject++;
                        if(projectManager.selectedProject >= projectManager.projects.size())
                        {
                            projectManager.selectedProject = 0;
                        }
                        projectManager.changeProject(projectManager.selectedProject);
                    }
                }
            }
            else if(Keyboard.isKeyDown(Keyboard.KEY_TAB) && host != null)
            {
                GlStateManager.pushMatrix();
                GlStateManager.translate(0F, 0F, 20F * (levels.size() + 1));

                ArrayList<String> listenersList = remoteSession ? listeners : new ArrayList<String>(Tabula.proxy.tickHandlerClient.mainframe.listeners.keySet());
                ArrayList<String> editorsList = remoteSession ? editors : Tabula.proxy.tickHandlerClient.mainframe.editors;
                listenersList.removeAll(editorsList);

                int tabHeight = (fontRendererObj.FONT_HEIGHT + 2) * (listenersList.size() + editorsList.size() + 1) + 3 + 2; // border


                RendererHelper.drawColourOnScreen(currentTheme.windowBorder[0], currentTheme.windowBorder[1], currentTheme.windowBorder[2], 255, width / 2 - 101, (height - tabHeight) / 2 - 1, 202, tabHeight + 2    , 0);
                RendererHelper.drawColourOnScreen(currentTheme.windowBackground[0], currentTheme.windowBackground[1], currentTheme.windowBackground[2], 255, width / 2 - 100, (height - tabHeight) / 2, 200, tabHeight, 0);
                RendererHelper.drawColourOnScreen(currentTheme.windowBorder[0], currentTheme.windowBorder[1], currentTheme.windowBorder[2], 255, width / 2 - 101, (height - tabHeight) / 2 - 1 + fontRendererObj.FONT_HEIGHT + 2 + 3, 202, 1, 0);

                fontRendererObj.drawString(StatCollector.translateToLocal("system.playersInSession"), width / 2 - 101 + 4, (height - tabHeight) / 2 - 1 + 3, currentTheme.getAsHex(currentTheme.font), false);

                for(int i = 0; i < editorsList.size(); i++)
                {
                    fontRendererObj.drawString(editorsList.get(i), width / 2 - 101 + 4, (height - tabHeight) / 2 - 1 + 3 + (fontRendererObj.FONT_HEIGHT + 2) + 4, currentTheme.getAsHex(currentTheme.font), false);
                    fontRendererObj.drawString(i == 0 ? StatCollector.translateToLocal("system.host") : StatCollector.translateToLocal("system.editor"), width / 2 + 101 - 4 - fontRendererObj.getStringWidth(i == 0 ? StatCollector.translateToLocal("system.host") : StatCollector.translateToLocal("system.editor")), (height - tabHeight) / 2 - 1 + 3 + (fontRendererObj.FONT_HEIGHT + 2) + 4, currentTheme.getAsHex(currentTheme.font), false);
                    GlStateManager.translate(0F, (fontRendererObj.FONT_HEIGHT + 2), 0);
                }
                for(int i = 0; i < listenersList.size(); i++)
                {
                    fontRendererObj.drawString(listenersList.get(i), width / 2 - 101 + 4, (height - tabHeight) / 2 - 1 + 3 + (fontRendererObj.FONT_HEIGHT + 2) + 4, currentTheme.getAsHex(currentTheme.font), false);
                    GlStateManager.translate(0F, (fontRendererObj.FONT_HEIGHT + 2), 0);
                }

                GlStateManager.popMatrix();
            }
            if(Keyboard.isKeyDown(Keyboard.KEY_HOME) && !keyHomeDown || !Keyboard.isKeyDown(Keyboard.KEY_HOME) && keyHomeDown)
            {
                windowAnimate.timeline.setCurrentPos(0);
                windowAnimate.timeline.focusOnTicker();
            }
            if(Keyboard.isKeyDown(Keyboard.KEY_END) && !keyEndDown || !Keyboard.isKeyDown(Keyboard.KEY_END) && keyEndDown)
            {
                windowAnimate.timeline.setCurrentPos(0);
                if(!windowAnimate.animList.selectedIdentifier.isEmpty())
                {
                    Animation anim = (Animation)windowAnimate.animList.getObjectByIdentifier(windowAnimate.animList.selectedIdentifier);
                    if(anim != null)
                    {
                        windowAnimate.timeline.setCurrentPos(anim.getLength());
                    }
                }
                windowAnimate.timeline.focusOnTicker();
            }
        }

        GlStateManager.popMatrix();

        updateKeyStates();

        updateWindowDragged(mouseX, mouseY);

        updateElementDragged(mouseX, mouseY);

        if(controlDrag >= 0)
        {
            if(Mouse.isButtonDown(0))
            {
                ArrayList<ElementListTree.Tree> trees = windowModelTree.modelList.trees;

                for (ElementListTree.Tree tree : trees)
                {
                    if (tree.selected && tree.attachedObject instanceof CubeInfo)
                    {
                        CubeInfo cube = (CubeInfo)tree.attachedObject;

                        boolean changed = false;

                        int changeAmt = Math.abs(controlDragY - mouseY) < Math.abs(mouseX - controlDragX) ? mouseX - controlDragX : controlDragY - mouseY;

                        if(controlDrag < 3)
                        {
                            changed = true;
                            if(controlDrag == 2)
                            {
                                cube.rotation[1] += changeAmt * 1D / cameraZoom;
                            }
                            else if(controlDrag == 1)
                            {
                                cube.rotation[2] += changeAmt * 1D / cameraZoom;
                            }
                            else if(controlDrag == 0)
                            {
                                cube.rotation[0] += changeAmt * 1D / cameraZoom;
                            }
                            controlDragX = mouseX;
                            controlDragY = mouseY;
                        }
                        else if(Math.abs(changeAmt) > 6)
                        {
                            int dimChange = (int)Math.round(changeAmt / ((float)6 * cameraZoom));
                            if(controlDrag <= 4)
                            {
                                if(cube.dimensions[1] + dimChange >= 0)
                                {
                                    cube.dimensions[1] += dimChange;
                                    if(controlDrag == 4)
                                    {
                                        cube.offset[1] -= dimChange;
                                    }
                                }
                            }
                            else if(controlDrag <= 6)
                            {
                                if(cube.dimensions[0] + dimChange >= 0)
                                {
                                    cube.dimensions[0] += dimChange;
                                    if(controlDrag == 6)
                                    {
                                        cube.offset[0] -= dimChange;
                                    }
                                }
                            }
                            else if(controlDrag <= 8)
                            {
                                if(cube.dimensions[2] + dimChange >= 0)
                                {
                                    cube.dimensions[2] += dimChange;
                                    if(controlDrag == 8)
                                    {
                                        cube.offset[2] -= dimChange;
                                    }
                                }
                            }
                            changed = true;
                            controlDragX = mouseX;
                            controlDragY = mouseY;
                        }

                        if(changed)
                        {
                            Gson gson = new Gson();
                            String s = gson.toJson(cube);
                            if(!this.remoteSession)
                            {
                                Tabula.proxy.tickHandlerClient.mainframe.updateCube(this.projectManager.projects.get(this.projectManager.selectedProject).identifier, s, this.windowAnimate.animList.selectedIdentifier, this.windowAnimate.timeline.selectedIdentifier, this.windowAnimate.timeline.getCurrentPos());
                            }
                            else if(!this.sessionEnded && this.isEditor)
                            {
                                Tabula.channel.sendToServer(new PacketGenericMethod(this.host, "updateCube", this.projectManager.projects.get(this.projectManager.selectedProject).identifier, s, this.windowAnimate.animList.selectedIdentifier, this.windowAnimate.timeline.selectedIdentifier, this.windowAnimate.timeline.getCurrentPos()));
                            }
                        }

                        break;
                    }
                }

            }
            else
            {
                controlDrag = -1;
            }
        }

        GlStateManager.matrixMode(GL11.GL_PROJECTION);
        GlStateManager.loadIdentity();
        GlStateManager.ortho(0.0D, resolution.getScaledWidth_double(), resolution.getScaledHeight_double(), 0.0D, 1000.0D, 3000.0D);
        GlStateManager.matrixMode(GL11.GL_MODELVIEW);
        GlStateManager.loadIdentity();

        if(false && projectManager.selectedProject != -1 && !Keyboard.isKeyDown(Keyboard.KEY_TAB))
        {
            GlStateManager.matrixMode(GL11.GL_PROJECTION);
            GlStateManager.loadIdentity();
            Project.gluPerspective(cameraFov, (float)(resolution.getScaledWidth_double() / resolution.getScaledHeight_double()), 1F, 10000F);
            GlStateManager.matrixMode(GL11.GL_MODELVIEW);
            GlStateManager.loadIdentity();
            GlStateManager.clearColor(100F, 100F, 100F, 255F);
            GlStateManager.clear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);

            modelSelector = new ModelSelector(this, 20);

            ArrayList<ElementListTree.Tree> trees = windowModelTree.modelList.trees;

            boolean stop = false;
            for (ElementListTree.Tree tree : trees)
            {
                if (tree.selected && tree.attachedObject instanceof CubeInfo)
                {
                    CubeInfo cube = (CubeInfo)tree.attachedObject;

                    modelSelector.fakeRenderSelectedCube(cube);

                    stop = true;
                    break;
                }
            }

            if(!stop)
            {
                modelSelector.fakeRender();
            }

            GlStateManager.matrixMode(GL11.GL_PROJECTION);
            GlStateManager.loadIdentity();
            GlStateManager.ortho(0.0D, resolution.getScaledWidth_double(), resolution.getScaledHeight_double(), 0.0D, 1000.0D, 3000.0D);
            GlStateManager.matrixMode(GL11.GL_MODELVIEW);
            GlStateManager.loadIdentity();
        }
    }

    @Override
    public void updateKeyStates()
    {
        super.updateKeyStates();

        keyDeleteDown = Keyboard.isKeyDown(Keyboard.KEY_DELETE);
        keyXDown = Keyboard.isKeyDown(Keyboard.KEY_X);
        keyCDown = Keyboard.isKeyDown(Keyboard.KEY_C);
        keyVDown = Keyboard.isKeyDown(Keyboard.KEY_V);
        keyZDown = Keyboard.isKeyDown(Keyboard.KEY_Z);
        keyYDown = Keyboard.isKeyDown(Keyboard.KEY_Y);
        keySDown = Keyboard.isKeyDown(Keyboard.KEY_S);
        keyNDown = Keyboard.isKeyDown(Keyboard.KEY_N);
        keyODown = Keyboard.isKeyDown(Keyboard.KEY_O);
        keyHomeDown = Keyboard.isKeyDown(Keyboard.KEY_HOME);
        keyEndDown = Keyboard.isKeyDown(Keyboard.KEY_END);
        keyTabDown = Keyboard.isKeyDown(Keyboard.KEY_TAB);
    }

    public void applyCamera() {
        float scale = 100F;
        GlStateManager.scale(scale, scale, scale);
        GlStateManager.translate(0F, -1.75F, -9F);
        GlStateManager.scale(cameraZoom, cameraZoom, cameraZoom);
        GlStateManager.scale(-1.0F, -1.0F, 1.0F);
        GlStateManager.translate(cameraOffsetX, cameraOffsetY, 0.0F);
        GlStateManager.rotate(-15F + cameraPitch + 180F, 1.0F, 0.0F, 0.0F);
        GlStateManager.rotate(-38F + cameraYaw, 0.0F, 1.0F, 0.0F);
    }

    public void applyModelTranslation() {
        GlStateManager.translate(0.0F, 2.0005F, 0.0F);
        GlStateManager.scale(-1.0F, -1.0F, 1.0F);
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

        GlStateManager.pushMatrix();

        GlStateManager.enableLighting();

        Tessellator tessellator = Tessellator.getInstance();
        WorldRenderer worldRenderer = tessellator.getWorldRenderer();

        worldRenderer.setBrightness(15728880);

        RenderHelper.enableGUIStandardItemLighting();

        int ii = 15728880;
        int jj = ii % 65536;
        int kk = ii / 65536;
        OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, (float)jj / 1.0F, (float)kk / 1.0F);

        applyCamera();

        Block block = Block.getBlockFromName(((Theme)currentTheme).workspaceBlock.block);
        int meta = ((Theme)currentTheme).workspaceBlock.metadata;

        if(block == null)
        {
            block = Blocks.planks;
        }

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
            ItemStack spoof = new ItemStack(block, 1, meta);
            IBlockState state = block.getStateFromMeta(meta);
            RendererHelper.renderBakedModel(mc.getBlockRendererDispatcher().getBlockModelShapes().getModelForState(state), -1, spoof);
        }

        GlStateManager.enableBlend();
        GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);

        if(projectManager.selectedProject != -1)
        {
            GlStateManager.pushMatrix();
            GlStateManager.disableCull();
            applyModelTranslation();
            GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
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

            boolean renderRotationPoint = Tabula.config.getInt("renderRotationPoint") == 1;

            applyModelAnimations();

            GlStateManager.scale(1D / info.scale[0], 1D / info.scale[1], 1D / info.scale[2]);
            if(windowTexture.imageId != -1)
            {
                GlStateManager.bindTexture(windowTexture.imageId);

                info.model.render(0.0625F, selected, hidden, cameraZoom, true, 0, renderRotationPoint, Tabula.config.getInt("renderModelControls") == 1);
                info.model.render(0.0625F, selected, hidden, cameraZoom, true, 1, renderRotationPoint, Tabula.config.getInt("renderModelControls") == 1);
            }
            else
            {
                GlStateManager.disableTexture2D();
                GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);

                info.model.render(0.0625F, selected, hidden, cameraZoom, false, 0, renderRotationPoint, Tabula.config.getInt("renderModelControls") == 1);
                info.model.render(0.0625F, selected, hidden, cameraZoom, false, 1, renderRotationPoint, Tabula.config.getInt("renderModelControls") == 1);

                GlStateManager.enableTexture2D();
            }
            GlStateManager.enableCull();
            GlStateManager.popMatrix();

            resetModelAnimations();
        }

        if(Tabula.config.getInt("renderGrid") == 1)
        {
            GlStateManager.color(1.0F, 1.0F, 1.0F, 0.5F);
            Minecraft.getMinecraft().getTextureManager().bindTexture(grid16);
            double dist = 0.125D;
            double pX = -3.495D - dist;
            double pY = 0.500125D;
            double pZ = -3.495D - dist;
            double w = 7 + (dist * 2);
            double l = 7 + (dist * 2);
            worldRenderer.startDrawingQuads();
            worldRenderer.addVertexWithUV(pX, pY, pZ + l, -0.125D, 7.125D);
            worldRenderer.addVertexWithUV(pX + w, pY, pZ + l, 7.125D, 7.125D);
            worldRenderer.addVertexWithUV(pX + w, pY, pZ, 7.125D, -0.125D);
            worldRenderer.addVertexWithUV(pX, pY, pZ, -0.125D, -0.125D);
            worldRenderer.addVertexWithUV(pX + w, pY, pZ + l, 7.125D, 7.125D);
            worldRenderer.addVertexWithUV(pX, pY, pZ + l, -0.125D, 7.125D);
            worldRenderer.addVertexWithUV(pX, pY, pZ, -0.125D, -0.125D);
            worldRenderer.addVertexWithUV(pX + w, pY, pZ, 7.125D, -0.125D);
            tessellator.draw();
        }

        GlStateManager.popMatrix();

        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);

        ScaledResolution resolution = new ScaledResolution(mc, mc.displayWidth, mc.displayHeight);
        GlStateManager.matrixMode(GL11.GL_PROJECTION);
        GlStateManager.loadIdentity();
        GlStateManager.ortho(0.0D, resolution.getScaledWidth_double(), resolution.getScaledHeight_double(), 0.0D, -5000.0D, 5000.0D);
        GlStateManager.matrixMode(GL11.GL_MODELVIEW);
        GlStateManager.loadIdentity();

        GlStateManager.pushMatrix();

        block = Blocks.furnace;
        //        Block block = Blocks.diamond_ore;

        Minecraft.getMinecraft().renderEngine.bindTexture(TextureMap.locationBlocksTexture);

        GlStateManager.translate(width - (levels.get(1).isEmpty() ? 15F : 15F + levels.get(1).get(0).width), height - 15F - windowAnimate.getHeight(), 3000F);
        float scale = 15F;
        GlStateManager.scale(scale, scale, scale);
        GlStateManager.scale(-1.0F, 1.0F, 1.0F);
        GlStateManager.rotate(-15F + cameraPitch + 180F, 1.0F, 0.0F, 0.0F);
        GlStateManager.rotate(-38F + cameraYaw, 0.0F, 1.0F, 0.0F);

        GlStateManager.pushMatrix();
        GlStateManager.scale(2.0F, 2.0F, 2.0F);
        mc.getRenderItem().renderItemModel(new ItemStack(block, 1));
        GlStateManager.popMatrix();

        GlStateManager.enableBlend();
        GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);

        GlStateManager.color(1.0F, 1.0F, 1.0F, 0.5F);
        Minecraft.getMinecraft().getTextureManager().bindTexture(orientationBase);
        double dist = 0.125D;
        double pX = -1D - dist;
        double pY = -0.500125D;
        double pZ = -1D - dist;
        double w = 2 + (dist * 2);
        double l = 2 + (dist * 2);
        worldRenderer.startDrawingQuads();
        worldRenderer.addVertexWithUV(pX, pY, pZ + l, 0.0D, 1.0D);
        worldRenderer.addVertexWithUV(pX + w, pY, pZ + l, 1.0D, 1.0D);
        worldRenderer.addVertexWithUV(pX + w, pY, pZ, 1.0D, 0.0D);
        worldRenderer.addVertexWithUV(pX	  , pY, pZ, 0.0D, 0.0D);
        worldRenderer.addVertexWithUV(pX + w, pY, pZ + l, 1.0D, 1.0D);
        worldRenderer.addVertexWithUV(pX	  , pY, pZ + l, 0.0D, 1.0D);
        worldRenderer.addVertexWithUV(pX	  , pY, pZ, 0.0D, 0.0D);
        worldRenderer.addVertexWithUV(pX + w, pY, pZ, 1.0D, 0.0D);
        tessellator.draw();
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);

        GlStateManager.disableBlend();

        RenderHelper.disableStandardItemLighting();

        GlStateManager.disableLighting();

        GlStateManager.popMatrix();
    }

    //only group parts. not actual individual cubes
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

    private static void addElementsForSelection(CubeGroup group, ArrayList<CubeInfo> selected)
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

    public static void addElementsForHiding(CubeGroup group, ArrayList<CubeInfo> selected)
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

    private static void addElementsForSelection(CubeInfo cube, ArrayList<CubeInfo> selected)
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

    public void paste(boolean inPlace, boolean withChildren)
    {
        if(cubeCopied instanceof CubeInfo)
        {
            Gson gson = new Gson();
            String s = gson.toJson(cubeCopied);
            if(!remoteSession)
            {
                Tabula.proxy.tickHandlerClient.mainframe.createNewCube(this.projectManager.projects.get(this.projectManager.selectedProject).identifier, s, inPlace, withChildren);
            }
            else if(!sessionEnded && isEditor)
            {
                Tabula.channel.sendToServer(new PacketGenericMethod(host, "createNewCube", this.projectManager.projects.get(this.projectManager.selectedProject).identifier, s, inPlace, withChildren));
            }
        }
        else if(cubeCopied instanceof CubeGroup)
        {
            if(!remoteSession)
            {
                Tabula.proxy.tickHandlerClient.mainframe.copyGroupTo(this.projectManager.projects.get(this.projectManager.selectedProject).identifier, ((CubeGroup)cubeCopied).identifier, inPlace);
            }
            else
            {
                Tabula.channel.sendToServer(new PacketGenericMethod(host, "copyGroupTo", this.projectManager.projects.get(this.projectManager.selectedProject).identifier, ((CubeGroup)cubeCopied).identifier, inPlace));
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
            String md5 = IOUtil.getMD5Checksum(proj.saveFile);
            if(proj.saveFile != null && proj.saveFile.exists() && md5 != null && md5.equals(proj.saveFileMd5))
            {
                if(ProjectInfo.saveProject(proj, proj.saveFile))
                {
                    proj.saveFileMd5 = IOUtil.getMD5Checksum(proj.saveFile);
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
            if(!remoteSession)
            {
                Tabula.proxy.tickHandlerClient.mainframe.switchState(projectManager.projects.get(projectManager.selectedProject).identifier, undo);
            }
            else if(!sessionEnded && isEditor)
            {
                Tabula.channel.sendToServer(new PacketGenericMethod(host, "switchState", projectManager.projects.get(projectManager.selectedProject).identifier, undo));
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

    public void applyModelAnimations()
    {
        if(projectManager.selectedProject != -1)
        {
            ProjectInfo info = projectManager.projects.get(projectManager.selectedProject);

            ArrayList<CubeInfo> allCubes = info.getAllCubes();

            for(Animation anim : info.anims)
            {
                if(anim.identifier.equalsIgnoreCase(windowAnimate.animList.selectedIdentifier))
                {
                    for(Map.Entry<String, ArrayList<AnimationComponent>> e : anim.sets.entrySet())
                    {
                        for(CubeInfo cube : allCubes)
                        {
                            if(cube.identifier.equals(e.getKey()))
                            {
                                ArrayList<AnimationComponent> components = e.getValue();
                                Collections.sort(components);

                                for(AnimationComponent comp : components)
                                {
                                    if(!comp.hidden)
                                    {
                                        comp.animate(cube, anim.playTime + (anim.playing ? renderTick : 0));
                                    }
                                }
                            }
                        }
                    }
                    break;
                }
            }
        }
    }

    public void resetModelAnimations()
    {
        if(projectManager.selectedProject != -1)
        {
            ProjectInfo info = projectManager.projects.get(projectManager.selectedProject);

            ArrayList<CubeInfo> allCubes = info.getAllCubes();

            for(Animation anim : info.anims)
            {
                if(anim.identifier.equalsIgnoreCase(windowAnimate.animList.selectedIdentifier))
                {
                    for(Map.Entry<String, ArrayList<AnimationComponent>> e : anim.sets.entrySet())
                    {
                        for(CubeInfo cube : allCubes)
                        {
                            if(cube.identifier.equals(e.getKey()))
                            {
                                ArrayList<AnimationComponent> components = e.getValue();
                                Collections.sort(components);

                                for(AnimationComponent comp : components)
                                {
                                    if(!comp.hidden)
                                    {
                                        comp.reset(cube, anim.playTime + (anim.playing ? renderTick : 0));
                                    }
                                }
                            }
                        }
                    }
                    break;
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
            if(host != null && !sessionEnded)
            {
                Tabula.channel.sendToServer(new PacketEndSession(host, hostX, hostY, hostZ, false));
            }
            if(init)
            {
                Tabula.proxy.tickHandlerClient.mainframe.shutdown();
            }
        }
        else
        {
            Tabula.channel.sendToServer(new PacketRemoveListener(host, Minecraft.getMinecraft().getSession().getUsername()));
        }
        if(host != null)
        {
            StringBuilder sb = new StringBuilder();

            sb.append(windowChat.posX);
            sb.append(":");
            sb.append(windowChat.posY);
            sb.append(":");
            sb.append(windowChat.width);
            sb.append(":");
            sb.append(windowChat.height);
            sb.append(":");
            sb.append(windowChat.docked);
            sb.append(":");
            sb.append(windowChat.minimized);
            sb.append(":");
            sb.append(windowChat.oriWidth);
            sb.append(":");
            sb.append(windowChat.oriHeight);

            Tabula.config.get("chatWindow").set(sb.toString());
            Tabula.config.save();
        }
    }

    @Override
    public boolean doesGuiPauseGame()
    {
        return false;
    }
}
