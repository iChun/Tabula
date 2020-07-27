package me.ichun.mods.tabula.client.gui;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import me.ichun.mods.ichunutil.client.gui.bns.Workspace;
import me.ichun.mods.ichunutil.client.gui.bns.window.*;
import me.ichun.mods.ichunutil.client.gui.bns.window.constraint.Constraint;
import me.ichun.mods.ichunutil.client.gui.bns.window.view.element.ElementToggle;
import me.ichun.mods.ichunutil.client.model.tabula.ModelTabula;
import me.ichun.mods.ichunutil.client.render.RenderHelper;
import me.ichun.mods.ichunutil.common.iChunUtil;
import me.ichun.mods.ichunutil.common.module.tabula.project.Identifiable;
import me.ichun.mods.ichunutil.common.module.tabula.project.Project;
import me.ichun.mods.tabula.client.gui.window.*;
import me.ichun.mods.tabula.client.gui.window.popup.WindowSaveAs;
import me.ichun.mods.tabula.client.gui.window.popup.WindowSaveOverwrite;
import me.ichun.mods.tabula.client.model.ModelVoxel;
import me.ichun.mods.tabula.client.tabula.Mainframe;
import me.ichun.mods.tabula.common.Tabula;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.resources.I18n;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.vector.Matrix4f;
import net.minecraft.util.text.TranslationTextComponent;
import org.lwjgl.BufferUtils;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.opengl.GL11;

import javax.annotation.Nonnull;
import java.nio.FloatBuffer;
import java.util.List;
import java.util.Map;

public class WorkspaceTabula extends Workspace
        implements IProjectInfo
{
    public static final ResourceLocation TEX_GRID_16 = new ResourceLocation("tabula", "textures/workspace/grid16.png");
    public static final ResourceLocation TEX_COMPASS_BASE = new ResourceLocation("tabula", "textures/workspace/orientationbase.png");

    public final Mainframe mainframe;
    public final int oriScale;
    public final WindowToolbar windowToolbar;

    public final ModelVoxel modelVoxel = new ModelVoxel();

    public boolean selecting;
    public boolean closing;

    public Identifiable<?> clipboard = null;

    private WorkspaceTabula(Screen lastScreen, Mainframe mainframe, int oriScale)
    {
        super(lastScreen, new TranslationTextComponent("tabula.name"), iChunUtil.configClient.guiStyleMinecraft);
        this.mainframe = mainframe;
        this.mainframe.setWorkspace(this);
        this.oriScale = oriScale;
        windows.add(new WindowInputReceiver(this));

        addToDock(windowToolbar = new WindowToolbar(this), Constraint.Property.Type.TOP);
        getDock().disableDock(Constraint.Property.Type.TOP);
    }

    @Override
    public void setCurrentProject(Mainframe.ProjectInfo info)
    {
        if(info != null)
        {
            Window<?> window = getByWindowType(WindowProjectNavigator.class);
            if(window == null)
            {
                window = new WindowProjectNavigator(this);
                addToDock(window, Constraint.Property.Type.TOP);
                window.init();
            }
        }

        List<Window<?>> children = getEventListeners();
        children.stream().filter(child -> child instanceof IProjectInfo).forEach(child -> ((IProjectInfo)child).setCurrentProject(info));
    }

    public void closeProject(Mainframe.ProjectInfo projectInfo)
    {
        if(closing && !mainframe.getIsMaster())
        {
            mainframe.closeProject(projectInfo, false);
            return;
        }

        if(projectInfo.project.isDirty) //interrupt with prompts
        {
            WindowYesNoCancel window = new WindowYesNoCancel(this, "window.notSaved.title", I18n.format("window.notSaved.save"), workspace -> {
                if(projectInfo.project.saveFile == null) //we've not saved before. no savefile
                {
                    openWindowInCenter(new WindowSaveAs(this, projectInfo.project), 0.4D, 0.4D);
                }
                else
                {
                    boolean save = projectInfo.project.save(projectInfo.project.saveFile);
                    if(save)
                    {
                        closeProject(projectInfo);
                    }
                    else
                    {
                        WindowPopup.popup(this, 0.4D, 140, null, I18n.format("window.saveAs.failed"));
                        closing = false; //disable closing (if we are)
                    }
                }
            }, workspace ->
            {
                projectInfo.project.isDirty = false;
                mainframe.closeProject(projectInfo, true);
            }, //do nothing. we're not saving.
                    workspace -> {
                        closing = false; //disable closing (if we are)
                    });
            window.setId("windowClosingPrompt");
            openWindowInCenter(window, 0.6D, 0.6D);
        }
        else
        {
            mainframe.closeProject(projectInfo, true);
        }
    }

    public void selectPart(Project.Part part)
    {
        Mainframe.ProjectInfo currentProject = mainframe.getActiveProject();

        if(currentProject != null)
        {
            if(part != null)
            {
                //check to see if WindowPartInfo exists, if not, add it.
                Window<?> window = getByWindowType(WindowPartInfo.class);
                if(window == null)
                {
                    window = new WindowPartInfo(this);
                    WindowChat chat = getByWindowType(WindowChat.class);
                    boolean chatDocked = false;
                    if(chat != null & isDocked(chat))
                    {
                        //remove the chat first
                        chatDocked = true;
                        removeFromDock(chat);
                    }
                    addToDock(window, Constraint.Property.Type.LEFT);
                    window.init();

                    if(chatDocked)
                    {
                        addToDock(chat, Constraint.Property.Type.BOTTOM);
                        chat.setHeight(95);
                        chat.constraint.apply();
                        chat.resize(Minecraft.getInstance(), chat.getParentWidth(), chat.getParentHeight());
                        chat.init();
                    }
                }
            }

            currentProject.selectPart(part);
        }
    }

    public void selectBox(Project.Part.Box box)
    {
        Mainframe.ProjectInfo currentProject = mainframe.getActiveProject();

        if(currentProject != null)
        {
            if(box != null && box.parent instanceof Project.Part)
            {
                //select this poor child's parent first
                selectPart((Project.Part)box.parent);

                //we've selected the parent
                //check to see if WindowBoxInfo exists, if not, add it.
                Window<?> window = getByWindowType(WindowBoxInfo.class);
                if(window == null)
                {
                    Window<?> partInfo = getByWindowType(WindowPartInfo.class); //shouldn't be null, we just checked for it!
                    window = new WindowBoxInfo(this);
                    if(isDocked(partInfo))
                    {
                        addToDocked(partInfo, window);
                    }
                    else
                    {
                        addToDock(window, Constraint.Property.Type.LEFT);
                    }
                    window.init();
                }

                currentProject.selectBox(box);
            }
            else //select null.
            {
                currentProject.selectPart(null);
                currentProject.selectBox(null);
            }
        }
    }

    public void updateChat()
    {
        Window<?> chat = getByWindowType(WindowChat.class);
        if(chat != null)
        {
            ((WindowChat)chat).updateChat();
        }
    }


    @Override
    protected void init()
    {
        super.init();

        if(!closing && Tabula.configClient.forceGuiScale >= 0 && minecraft.gameSettings.guiScale != Tabula.configClient.forceGuiScale)
        {
            minecraft.gameSettings.guiScale = Tabula.configClient.forceGuiScale;
            minecraft.updateWindowSize();
        }

        if(mainframe.projects.isEmpty())
        {
            windowToolbar.setCurrentProject(null);
        }
        if(mainframe.origin != null) //multiplayer
        {
            Window<?> chat = getByWindowType(WindowChat.class);
            if(chat == null)
            {
                chat = new WindowChat(this);
                addWindow(chat);
                chat.setPosX(-10000);
                chat.init();
            }
        }
    }

    @Override
    public void tick()
    {
        super.tick();
        mainframe.tick();

        if(closing)
        {
            if(!(getById("windowClosingPrompt") != null || getByWindowType(WindowSaveAs.class) != null || getByWindowType(WindowSaveOverwrite.class) != null) && !mainframe.projects.isEmpty())
            {
                closeProject(mainframe.getActiveProject());
            }
            closeScreen();
        }
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double distX, double distY)
    {
        return (this.getListener() != null && this.isDragging()) && this.getListener().mouseDragged(mouseX, mouseY, button, distX, distY);
    }

    @Override
    public void render(MatrixStack stack, int mouseX, int mouseY, float partialTick)
    {
        RenderSystem.enableAlphaTest();
        RenderSystem.enableCull();
        RenderSystem.enableDepthTest();
        RenderSystem.depthFunc(GL11.GL_LEQUAL);
        RenderSystem.depthMask(true);

        if(selecting)
        {
            findSelection(mouseX, mouseY, partialTick);
            selecting = false;
        }

        //        RenderSystem.pushMatrix();

        renderBackground(stack);

        //Set up projection for workspace
        Mainframe.Camera cam = mainframe.getCamera();
        RenderSystem.matrixMode(GL11.GL_PROJECTION);
        RenderSystem.loadIdentity();
        RenderSystem.multMatrix(Matrix4f.perspective((cam.rendFovPrev + (cam.rendFov - cam.rendFovPrev) * partialTick), (float)minecraft.getMainWindow().getFramebufferWidth() / (float)minecraft.getMainWindow().getFramebufferHeight(), 1F, 10000F));
        RenderSystem.matrixMode(GL11.GL_MODELVIEW);
        RenderSystem.loadIdentity();

        RenderSystem.pushMatrix();

        renderWorkspace(stack, mouseX, mouseY, partialTick);

        //Reset our perspective, and set up for drawing windows.
        RenderSystem.popMatrix();

        RenderSystem.matrixMode(GL11.GL_PROJECTION);
        RenderSystem.loadIdentity();
        RenderSystem.ortho(0.0D, minecraft.getMainWindow().getFramebufferWidth() / minecraft.getMainWindow().getGuiScaleFactor(), minecraft.getMainWindow().getFramebufferHeight() / minecraft.getMainWindow().getGuiScaleFactor(), 0.0D, -5000.0D, 5000.0D);
        RenderSystem.matrixMode(GL11.GL_MODELVIEW);
        RenderSystem.loadIdentity();

        renderCompass(stack, partialTick);

        renderWindows(stack, mouseX, mouseY, partialTick);

        renderTooltip(stack, mouseX, mouseY, partialTick);

        resetBackground();

        RenderSystem.enableAlphaTest();
        RenderSystem.depthMask(false);
        RenderSystem.disableDepthTest();
    }

    public void findSelection(int mouseX, int mouseY, float partialTick)
    {
        RenderSystem.clearColor(0F, 0F, 0F, 255F);
        RenderSystem.clear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT, Minecraft.IS_RUNNING_ON_MAC);

        RenderSystem.pushMatrix();


        //Set up projection for workspace
        Mainframe.Camera cam = mainframe.getCamera();
        RenderSystem.matrixMode(GL11.GL_PROJECTION);
        RenderSystem.loadIdentity();
        RenderSystem.multMatrix(Matrix4f.perspective((cam.rendFovPrev + (cam.rendFov - cam.rendFovPrev) * partialTick), (float)minecraft.getMainWindow().getFramebufferWidth() / (float)minecraft.getMainWindow().getFramebufferHeight(), 1F, 10000F));
        RenderSystem.matrixMode(GL11.GL_MODELVIEW);
        RenderSystem.loadIdentity();

        RenderSystem.pushMatrix();

        //from renderWorkspace
        setupCamera(partialTick);
        renderModel(true);

        Mainframe.ProjectInfo info = mainframe.getActiveProject();
        if(info != null)
        {
            FloatBuffer buffer = BufferUtils.createFloatBuffer(3);
            GL11.glReadPixels((int)minecraft.mouseHelper.getMouseX(), (int)(minecraft.getMainWindow().getHeight() - minecraft.mouseHelper.getMouseY()), 1, 1, GL11.GL_RGB, GL11.GL_FLOAT, buffer);

            ModelTabula model = info.project.getModel();
            Project.Part.Box box = model.getSelectedBox((int)(buffer.get() * 255), (int)(buffer.get() * 255), (int)(buffer.get() * 255));
            selectBox(box);
        }

        //Reset our perspective, and set up for drawing windows.
        RenderSystem.popMatrix();

        RenderSystem.matrixMode(GL11.GL_PROJECTION);
        RenderSystem.loadIdentity();
        RenderSystem.ortho(0.0D, minecraft.getMainWindow().getFramebufferWidth() / minecraft.getMainWindow().getGuiScaleFactor(), minecraft.getMainWindow().getFramebufferHeight() / minecraft.getMainWindow().getGuiScaleFactor(), 0.0D, -5000.0D, 5000.0D);
        RenderSystem.matrixMode(GL11.GL_MODELVIEW);
        RenderSystem.loadIdentity();

        RenderSystem.popMatrix();

        RenderSystem.pushMatrix();

        cam = mainframe.getCamera();

        double right = width;
        double bottom = height;
        for(Map.Entry<WindowDock.ArrayListHolder, Constraint.Property.Type> e : getDock().docked.entrySet())
        {
            for(Window<?> key : e.getKey().windows)
            {
                Constraint.Property.Type value = e.getValue();
                switch(value)
                {
                    case RIGHT:
                    {
                        if(key.getLeft() < right)
                        {
                            right = key.getLeft();
                        }
                        break;
                    }
                    case BOTTOM:
                    {
                        if(key.getTop() < bottom)
                        {
                            bottom = key.getTop();
                        }
                        break;
                    }
                }
            }
        }

        RenderSystem.translated(right - 20F, bottom - 20F, 3000F);
        float scale = 15F;
        RenderSystem.scalef(scale, scale, scale);
        RenderSystem.scalef(-1.0F, 1.0F, 1.0F);
        RenderSystem.rotatef(-15F + (cam.rendPitchPrev + (cam.rendPitch - cam.rendPitchPrev) * partialTick) + 180F, 1.0F, 0.0F, 0.0F);
        RenderSystem.rotatef(-38F + (cam.rendYawPrev + (cam.rendYaw - cam.rendYawPrev) * partialTick), 0.0F, 1.0F, 0.0F);

        IRenderTypeBuffer.Impl bufferSource = Minecraft.getInstance().getRenderTypeBuffers().getBufferSource();
        RenderType type = ModelTabula.RENDER_MODEL_COMPASS_FLAT;

        IVertexBuilder ivertexbuilder = bufferSource.getBuffer(type);

        MatrixStack stack = new MatrixStack();
        stack.scale(16F, 16F, 16F);

        modelVoxel.render(stack, ivertexbuilder, 15728880, OverlayTexture.NO_OVERLAY, 1F, 1F, 1F, 1F);

        bufferSource.finish();

        RenderSystem.popMatrix();

        FloatBuffer buffer = BufferUtils.createFloatBuffer(3);
        GL11.glReadPixels((int)minecraft.mouseHelper.getMouseX(), (int)(minecraft.getMainWindow().getHeight() - minecraft.mouseHelper.getMouseY()), 1, 1, GL11.GL_RGB, GL11.GL_FLOAT, buffer);

        int red = (int)(buffer.get() * 255);
        int green = (int)(buffer.get() * 255);
        int blue = (int)(buffer.get() * 255);

        if(blue > 0 && red == 255 && green == 255)
        {
            blue -= 250;
            if(blue <= 1)
            {
                cam.yaw = 0F;
            }
            else
            {
                cam.pitch = 0F;
            }
            switch(blue)
            {
                case 0:
                {
                    cam.pitch = 90F;
                    break;
                }
                case 1:
                {
                    cam.pitch = -90F;
                    break;
                }
                case 2:
                {
                    cam.yaw = 90F;
                    break;
                }
                case 3:
                {
                    cam.yaw = 270F;
                    break;
                }
                case 4:
                {
                    cam.yaw = 180F;
                    break;
                }
                case 5:
                {
                    cam.yaw = 0F;
                    break;
                }
            }
            cam.pitch += 15F;
            cam.yaw += 38F;
        }
    }

    @Override
    public void renderBackground(MatrixStack stack)
    {
        RenderSystem.clearColor((float)getTheme().workspaceBackground[0] / 255F, (float)getTheme().workspaceBackground[1] / 255F, (float)getTheme().workspaceBackground[2] / 255F, 255F);
        RenderSystem.clear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT, Minecraft.IS_RUNNING_ON_MAC);

        RenderSystem.pushMatrix();
    }

    @Override
    public void resetBackground()
    {
        RenderSystem.popMatrix();

        RenderSystem.matrixMode(GL11.GL_PROJECTION);
        RenderSystem.loadIdentity();
        RenderSystem.ortho(0.0D, minecraft.getMainWindow().getFramebufferWidth() / minecraft.getMainWindow().getGuiScaleFactor(), minecraft.getMainWindow().getFramebufferHeight() / minecraft.getMainWindow().getGuiScaleFactor(), 0.0D, 1000.0D, 3000.0D);
        RenderSystem.matrixMode(GL11.GL_MODELVIEW);
        RenderSystem.loadIdentity();
        RenderSystem.translatef(0.0F, 0.0F, -2000.0F);
    }

    @Override
    public WindowDock<? extends Workspace> getDock()
    {
        return (WindowDock<? extends Workspace>)windows.get(windows.size() - 2);
    }

    public void renderWorkspace(MatrixStack stack, int mouseX, int mouseY, float partialTick)
    {
        setupCamera(partialTick);

        RenderSystem.enableRescaleNormal();

        //RENDER BLOCK
        Fragment<?> fragment = getById("buttonBlockToggle");
        if(fragment instanceof ElementToggle && ((ElementToggle<?>)fragment).toggleState) //render the block
        {
            Block block = getTheme().block;
            if(block == null || block.getDefaultState().isAir())
            {
                block = Blocks.SPRUCE_PLANKS;
            }

            net.minecraft.client.renderer.RenderHelper.setupGuiFlatDiffuseLighting();

            RenderHelper.renderBakedModel(Minecraft.getInstance().getBlockRendererDispatcher().getBlockModelShapes().getModel(block.getDefaultState()), new ItemStack(block));

            net.minecraft.client.renderer.RenderHelper.setupGui3DDiffuseLighting();
        }
        //END RENDER BLOCK

        RenderSystem.enableDepthTest();
        RenderSystem.depthFunc(GL11.GL_LEQUAL);
        RenderSystem.depthMask(true);

        renderModel(false);

        RenderSystem.disableRescaleNormal();

        fragment = getById("buttonGridToggle");
        if(fragment instanceof ElementToggle && ((ElementToggle<?>)fragment).toggleState) //render the block
        {
            Minecraft.getInstance().getTextureManager().bindTexture(TEX_GRID_16);

            Tessellator tessellator = Tessellator.getInstance();
            BufferBuilder bufferbuilder = tessellator.getBuffer();
            int light1 = 15728880 >> 16 & 65535;
            int light2 = 15728880 & 65535;

            RenderSystem.enableBlend();
            RenderSystem.defaultBlendFunc();

            double dist = Tabula.configClient.workspaceGridSize;
            float halfDist = (float)(dist / 2);
            double pX = -(dist / 2) - 0.005D;
            double pY = 0.500125D;
            double pZ = -(dist / 2) - 0.005D;
            double w = dist;
            double l = dist;
            Matrix4f matrix = stack.getLast().getMatrix();
            bufferbuilder.begin(7, DefaultVertexFormats.POSITION_COLOR_TEX_LIGHTMAP);
            bufferbuilder.pos(matrix, (float)pX, (float)pY, (float)(pZ + l)).color(1.0F, 1.0F, 1.0F, 0.5F).tex((-halfDist + 0.5F), (halfDist + 0.5F)).lightmap(light1, light2).endVertex();
            bufferbuilder.pos(matrix, (float)(pX + w), (float)pY, (float)(pZ + l)).color(1.0F, 1.0F, 1.0F, 0.5F).tex((halfDist + 0.5F), (halfDist + 0.5F)).lightmap(light1, light2).endVertex();
            bufferbuilder.pos(matrix, (float)(pX + w), (float)pY, (float)pZ).color(1.0F, 1.0F, 1.0F, 0.5F).tex((halfDist + 0.5F), (-halfDist + 0.5F)).lightmap(light1, light2).endVertex();
            bufferbuilder.pos(matrix, (float)pX, (float)pY, (float)pZ).color(1.0F, 1.0F, 1.0F, 0.5F).tex((-halfDist + 0.5F), (-halfDist + 0.5F)).lightmap(light1, light2).endVertex();
            bufferbuilder.pos(matrix, (float)(pX + w), (float)pY, (float)(pZ + l)).color(1.0F, 1.0F, 1.0F, 0.5F).tex((halfDist + 0.5F), (halfDist + 0.5F)).lightmap(light1, light2).endVertex();
            bufferbuilder.pos(matrix, (float)pX, (float)pY, (float)(pZ + l)).color(1.0F, 1.0F, 1.0F, 0.5F).tex((-halfDist + 0.5F), (halfDist + 0.5F)).lightmap(light1, light2).endVertex();
            bufferbuilder.pos(matrix, (float)pX, (float)pY, (float)pZ).color(1.0F, 1.0F, 1.0F, 0.5F).tex((-halfDist + 0.5F), (-halfDist + 0.5F)).lightmap(light1, light2).endVertex();
            bufferbuilder.pos(matrix, (float)(pX + w), (float)pY, (float)pZ).color(1.0F, 1.0F, 1.0F, 0.5F).tex((halfDist + 0.5F), (-halfDist + 0.5F)).lightmap(light1, light2).endVertex();
            tessellator.draw();

            RenderSystem.disableBlend();
        }
    }

    public void renderModel(boolean selection)
    {
        Mainframe.ProjectInfo info = mainframe.getActiveProject();
        if(info != null)
        {
            net.minecraft.client.renderer.RenderHelper.setupGuiFlatDiffuseLighting();

            RenderSystem.enableRescaleNormal();
            RenderSystem.enableAlphaTest();
            RenderSystem.defaultAlphaFunc();
            RenderSystem.enableBlend();
            RenderSystem.defaultBlendFunc();

            MatrixStack stack = new MatrixStack(); //TODO do I have to use the one passed in render?

            stack.translate(0F, 2.0005F, 0F);
            stack.scale(-1F, -1F, 1F);

            stack.push();

            if(info.project.scaleX != 1.0F || info.project.scaleY != 1.0F || info.project.scaleZ != 1.0F)
            {
                stack.translate(0F, 1.5F, 0F);
                stack.scale(info.project.scaleX, info.project.scaleY, info.project.scaleZ);
                stack.translate(0F, -1.5F, 0F);
            }

            if(selection)
            {
                info.project.getModel().renderForSelection(stack);

                stack.pop();
            }
            else
            {
                info.project.getModel().render(stack, info.getSelectedPart(), info.getSelectedBox(), info.hideTexture, 1F);

                stack.pop();

                if(info.ghostProject != null)
                {
                    info.ghostProject.getModel().render(stack, null, null, false, info.ghostOpacity);
                }
            }

            net.minecraft.client.renderer.RenderHelper.setupGui3DDiffuseLighting();

            RenderSystem.enableDepthTest();
            RenderSystem.disableAlphaTest();
            RenderSystem.disableRescaleNormal();

            RenderSystem.enableTexture();
        }
    }

    public void setupCamera(float partialTick)
    {
        Mainframe.Camera cam = mainframe.getCamera();
        cam.correct();
        float scale = 100F;
        RenderSystem.scalef(scale, scale, scale);
        RenderSystem.translatef(0F, -1.75F, -9F);
        float zoom = cam.rendZoomPrev + (cam.rendZoom - cam.rendZoomPrev) * partialTick;
        RenderSystem.scalef(zoom, zoom, zoom);
        RenderSystem.scalef(-1.0F, -1.0F, 1.0F);
        RenderSystem.translatef((cam.rendXPrev + (cam.rendX - cam.rendXPrev) * partialTick), (cam.rendYPrev + (cam.rendY - cam.rendYPrev) * partialTick), 0.0F);
        RenderSystem.rotatef(-15F + (cam.rendPitchPrev + (cam.rendPitch - cam.rendPitchPrev) * partialTick) + 180F, 1.0F, 0.0F, 0.0F);
        RenderSystem.rotatef(-38F + (cam.rendYawPrev + (cam.rendYaw - cam.rendYawPrev) * partialTick), 0.0F, 1.0F, 0.0F);
    }

    public void renderCompass(MatrixStack stack, float partialTick)
    {
        RenderSystem.pushMatrix();

        Mainframe.Camera cam = mainframe.getCamera();

        double right = width;
        double bottom = height;
        for(Map.Entry<WindowDock.ArrayListHolder, Constraint.Property.Type> e : getDock().docked.entrySet())
        {
            for(Window<?> key : e.getKey().windows)
            {
                Constraint.Property.Type value = e.getValue();
                switch(value)
                {
                    case RIGHT:
                    {
                        if(key.getLeft() < right)
                        {
                            right = key.getLeft();
                        }
                        break;
                    }
                    case BOTTOM:
                    {
                        if(key.getTop() < bottom)
                        {
                            bottom = key.getTop();
                        }
                        break;
                    }
                }
            }
        }

        RenderSystem.translated(right - 20F, bottom - 20F, 3000F);
        float scale = 15F;
        RenderSystem.scalef(scale, scale, scale);
        RenderSystem.scalef(-1.0F, 1.0F, 1.0F);
        RenderSystem.rotatef(-15F + (cam.rendPitchPrev + (cam.rendPitch - cam.rendPitchPrev) * partialTick) + 180F, 1.0F, 0.0F, 0.0F);
        RenderSystem.rotatef(-38F + (cam.rendYawPrev + (cam.rendYaw - cam.rendYawPrev) * partialTick), 0.0F, 1.0F, 0.0F);

        RenderSystem.enableRescaleNormal();

        RenderSystem.pushMatrix();
        Block block = Blocks.FURNACE;
        net.minecraft.client.renderer.RenderHelper.setupGuiFlatDiffuseLighting();
        RenderHelper.renderBakedModel(Minecraft.getInstance().getBlockRendererDispatcher().getBlockModelShapes().getModel(block.getDefaultState()), new ItemStack(block));
        net.minecraft.client.renderer.RenderHelper.setupGui3DDiffuseLighting();
        RenderSystem.popMatrix();

        RenderSystem.disableRescaleNormal();

        RenderSystem.enableDepthTest();
        RenderSystem.depthFunc(GL11.GL_LEQUAL);
        RenderSystem.depthMask(true);

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();

        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder bufferbuilder = tessellator.getBuffer();
        int light1 = 15728880 >> 16 & 65535;
        int light2 = 15728880 & 65535;

        bindTexture(TEX_COMPASS_BASE);
        double dist = 0.125D;
        double pX = -1D - dist;
        double pY = -0.500125D;
        double pZ = -1D - dist;
        double w = 2 + (dist * 2);
        double l = 2 + (dist * 2);
        Matrix4f matrix = stack.getLast().getMatrix();
        bufferbuilder.begin(7, DefaultVertexFormats.POSITION_COLOR_TEX_LIGHTMAP);
        bufferbuilder.pos(matrix, (float)pX, (float)pY, (float)(pZ + l)).color(1.0F, 1.0F, 1.0F, 0.5F).tex(0.0F, 1.0F).lightmap(light1, light2).endVertex();
        bufferbuilder.pos(matrix, (float)(pX + w), (float)pY, (float)(pZ + l)).color(1.0F, 1.0F, 1.0F, 0.5F).tex(1.0F, 1.0F).lightmap(light1, light2).endVertex();
        bufferbuilder.pos(matrix, (float)(pX + w), (float)pY, (float)pZ).color(1.0F, 1.0F, 1.0F, 0.5F).tex(1.0F, 0.0F).lightmap(light1, light2).endVertex();
        bufferbuilder.pos(matrix, (float)pX	  , (float)pY, (float)pZ).color(1.0F, 1.0F, 1.0F, 0.5F).tex(0.0F, 0.0F).lightmap(light1, light2).endVertex();
        bufferbuilder.pos(matrix, (float)(pX + w), (float)pY, (float)(pZ + l)).color(1.0F, 1.0F, 1.0F, 0.5F).tex(1.0F, 1.0F).lightmap(light1, light2).endVertex();
        bufferbuilder.pos(matrix, (float)pX	  , (float)pY, (float)(pZ + l)).color(1.0F, 1.0F, 1.0F, 0.5F).tex(0.0F, 1.0F).lightmap(light1, light2).endVertex();
        bufferbuilder.pos(matrix, (float)pX	  , (float)pY, (float)pZ).color(1.0F, 1.0F, 1.0F, 0.5F).tex(0.0F, 0.0F).lightmap(light1, light2).endVertex();
        bufferbuilder.pos(matrix, (float)(pX + w), (float)pY, (float)pZ).color(1.0F, 1.0F, 1.0F, 0.5F).tex(1.0F, 0.0F).lightmap(light1, light2).endVertex();
        tessellator.draw();

        RenderSystem.disableBlend();

        RenderSystem.popMatrix();
    }

    @Override
    public boolean keyPressed(int keyCode, int p_keyPressed_2_, int p_keyPressed_3_)
    {
        if(Screen.hasControlDown())
        {
            if(keyCode == GLFW.GLFW_KEY_N)
            {
                windowToolbar.getCurrentView().openNewProject();
                return true;
            }
            else if(keyCode == GLFW.GLFW_KEY_O)
            {
                windowToolbar.getCurrentView().openOpenProject();
                return true;
            }
            else if(keyCode == GLFW.GLFW_KEY_S)
            {
                if(Screen.hasShiftDown())
                {
                    windowToolbar.getCurrentView().saveAsProject();
                    return true;
                }
                else
                {
                    windowToolbar.getCurrentView().saveProject();
                    return true;
                }
            }
            else if(keyCode == GLFW.GLFW_KEY_G)
            {
                windowToolbar.getCurrentView().openGhostModel();
                return true;
            }
            else if(keyCode == GLFW.GLFW_KEY_I)
            {
                if(Screen.hasShiftDown())
                {
                    windowToolbar.getCurrentView().openImportProject();
                    return true;
                }
                else
                {
                    windowToolbar.getCurrentView().openImportMCProject();
                    return true;
                }
            }
            else if(keyCode == GLFW.GLFW_KEY_E)
            {
                windowToolbar.getCurrentView().openExportProject();
                return true;
            }
            else if(keyCode == GLFW.GLFW_KEY_TAB)
            {
                if(Screen.hasShiftDown()) //in place
                {
                    mainframe.activeView--;
                    if(mainframe.activeView < 0)
                    {
                        mainframe.activeView = mainframe.projects.size() - 1;
                    }
                    if(mainframe.activeView >= 0)
                    {
                        setCurrentProject(mainframe.projects.get(mainframe.activeView));
                        projectChanged(IProjectInfo.ChangeType.PROJECT);
                    }
                    return true;
                }
                else
                {
                    mainframe.activeView++;
                    if(mainframe.activeView >= mainframe.projects.size())
                    {
                        mainframe.activeView = 0;
                    }
                    if(mainframe.activeView < mainframe.projects.size())
                    {
                        setCurrentProject(mainframe.projects.get(mainframe.activeView));
                        projectChanged(IProjectInfo.ChangeType.PROJECT);
                    }
                    return true;
                }
            }
            else if(keyCode == GLFW.GLFW_KEY_W)
            {
                Mainframe.ProjectInfo info = mainframe.getActiveProject();
                if(info != null)
                {
                    closeProject(info);
                }
                return true;
            }
            else if(getListener() instanceof WindowInputReceiver)
            {
                if(keyCode == GLFW.GLFW_KEY_Z)
                {
                    if(Screen.hasShiftDown())
                    {
                        windowToolbar.getCurrentView().redo();
                        return true;
                    }
                    else
                    {
                        windowToolbar.getCurrentView().undo();
                        return true;
                    }
                }
                else if(keyCode == GLFW.GLFW_KEY_Y)
                {
                    windowToolbar.getCurrentView().redo();
                    return true;
                }
                else if(keyCode == GLFW.GLFW_KEY_X)
                {
                    windowToolbar.getCurrentView().cut();
                    return true;
                }
                else if(keyCode == GLFW.GLFW_KEY_C)
                {
                    windowToolbar.getCurrentView().copy();
                    return true;
                }
                else if(keyCode == GLFW.GLFW_KEY_V)
                {
                    if(Screen.hasShiftDown()) //in place
                    {
                        windowToolbar.getCurrentView().paste(false, true);
                        return true;
                    }
                    else
                    {
                        windowToolbar.getCurrentView().paste(false, false);
                        return true;
                    }
                }
            }
        }
        else if((keyCode == GLFW.GLFW_KEY_DELETE || keyCode == GLFW.GLFW_KEY_KP_DECIMAL) && (getListener() instanceof WindowInputReceiver || getListener() instanceof WindowModelTree))
        {
            Window<?> window = getByWindowType(WindowModelTree.class);
            if(window != null)
            {
                ((WindowModelTree.ViewModelTree)window.currentView).delete();
                return true;
            }
        }
        return super.keyPressed(keyCode, p_keyPressed_2_, p_keyPressed_3_);
    }

    @Override
    public void addToDock(Window<?> window, Constraint.Property.Type type)
    {
        Fragment<?> nav = getById("windowNav");
        if(nav instanceof WindowProjectNavigator)
        {
            getDock().removeFromDock((Window<?>)nav); //bypass readding to windows
        }
        super.addToDock(window, type);
        if(nav instanceof WindowProjectNavigator)
        {
            super.addToDock((Window<?>)nav, Constraint.Property.Type.TOP);
        }
    }

    @Override
    public void closeScreen()
    {
        if(!windows.isEmpty())
        {
            for(Window<?> window : windows)
            {
                if(window.getClass().getName().contains("popup") || window instanceof WindowEditList) //I'm lazy ok
                {
                    removeWindow(window);
                    return;
                }
            }
        }

        closing = true;
        if(canClose())
        {
            super.closeScreen();
        }
    }

    @Override
    public void onClose()
    {
        super.onClose();

        mainframe.shutdown();
        if(oriScale != minecraft.gameSettings.guiScale)
        {
            minecraft.gameSettings.guiScale = oriScale;
            minecraft.updateWindowSize();
        }
    }

    public boolean canClose()
    {
        return mainframe.projects.isEmpty();
    }

    public static WorkspaceTabula create(@Nonnull String master)
    {
        Minecraft mc = Minecraft.getInstance();
        WorkspaceTabula workspace = new WorkspaceTabula(Minecraft.getInstance().currentScreen, new Mainframe(!master.isEmpty() ? master : mc.getSession().getUsername()), mc.gameSettings.guiScale);
        if(master.equals(mc.getSession().getUsername()))
        {
            workspace.mainframe.setMaster();
        }

        return workspace;
    }
}
