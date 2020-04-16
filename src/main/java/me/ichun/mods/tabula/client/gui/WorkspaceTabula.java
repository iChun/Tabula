package me.ichun.mods.tabula.client.gui;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import me.ichun.mods.ichunutil.client.gui.bns.Workspace;
import me.ichun.mods.ichunutil.client.gui.bns.window.*;
import me.ichun.mods.ichunutil.client.gui.bns.window.constraint.Constraint;
import me.ichun.mods.ichunutil.client.gui.bns.window.view.element.ElementToggle;
import me.ichun.mods.ichunutil.client.model.ModelHelper;
import me.ichun.mods.ichunutil.client.model.ModelTabula;
import me.ichun.mods.ichunutil.client.render.RenderHelper;
import me.ichun.mods.ichunutil.common.iChunUtil;
import me.ichun.mods.ichunutil.common.module.tabula.project.Project;
import me.ichun.mods.tabula.client.gui.window.*;
import me.ichun.mods.tabula.client.gui.window.popup.WindowSaveAs;
import me.ichun.mods.tabula.client.gui.window.popup.WindowSaveOverwrite;
import me.ichun.mods.tabula.client.tabula.Mainframe;
import me.ichun.mods.tabula.common.Tabula;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.Matrix4f;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.entity.model.SpiderModel;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.resources.I18n;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TranslationTextComponent;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;

import java.nio.FloatBuffer;
import java.util.ArrayList;
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

    public boolean selecting;
    public boolean closing;

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
            Window<?> window = getWindowType(WindowProjectNavigator.class);
            if(window == null)
            {
                window = new WindowProjectNavigator(this);
                addToDock(window, Constraint.Property.Type.TOP);
                window.init();
            }
        }

        List<Window<?>> children = children();
        children.stream().filter(child -> child instanceof IProjectInfo).forEach(child -> ((IProjectInfo)child).setCurrentProject(info));
    }

    public void closeProject(Mainframe.ProjectInfo projectInfo)
    {
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
                        WindowPopup.popup(this, 0.4D, 0.3D, I18n.format("window.saveAs.failed"), null);
                        closing = false; //disable closing (if we are)
                    }
                }
            }, workspace ->
            {
                projectInfo.project.isDirty = false;
                mainframe.closeProject(projectInfo);
            }, //do nothing. we're not saving.
                    workspace -> {
                        closing = false; //disable closing (if we are)
                    });
            window.setId("windowClosingPrompt");
            openWindowInCenter(window, 0.6D, 0.6D);
        }
        else
        {
            mainframe.closeProject(projectInfo);
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
                Window<?> window = getWindowType(WindowPartInfo.class);
                if(window == null)
                {
                    window = new WindowPartInfo(this);
                    addToDock(window, Constraint.Property.Type.LEFT);
                    window.init();
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
                Window<?> window = getWindowType(WindowBoxInfo.class);
                if(window == null)
                {
                    Window<?> partInfo = getWindowType(WindowPartInfo.class); //shouldn't be null, we just checked for it!
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


    @Override
    protected void init()
    {
        super.init();

        SpiderModel model = new SpiderModel();
        model.isChild = false;
        model.setRotationAngles(null, 0F, 0F ,0F, 0F, 0F);
        Project project = ModelHelper.convertModelToProject(model);

        //        Project project = new Project();
        //        project.name = "MyFirstModel";
        //        project.author = "sugar tits";
        //        project.texWidth = 64;
        //        project.texHeight = 32;
        this.mainframe.openProject(project);

        //        this.mainframe.getActiveProject().addPart(null);
    }

    @Override
    public void tick()
    {
        super.tick();
        mainframe.tick();

        if(closing)
        {
            if(!(getById("windowClosingPrompt") != null || getWindowType(WindowSaveAs.class) != null || getWindowType(WindowSaveOverwrite.class) != null) && !mainframe.projects.isEmpty())
            {
                closeProject(mainframe.getActiveProject());
            }
            onClose();
        }
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double distX, double distY)
    {
        return (this.getFocused() != null && this.isDragging()) && this.getFocused().mouseDragged(mouseX, mouseY, button, distX, distY);
    }

    @Override
    public void render(int mouseX, int mouseY, float partialTick)
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

        renderBackground();

        //Set up projection for workspace
        Mainframe.Camera cam = mainframe.getCamera();
        RenderSystem.matrixMode(GL11.GL_PROJECTION);
        RenderSystem.loadIdentity();
        RenderSystem.multMatrix(Matrix4f.perspective((cam.rendFovPrev + (cam.rendFov - cam.rendFovPrev) * partialTick), (float)minecraft.getMainWindow().getFramebufferWidth() / (float)minecraft.getMainWindow().getFramebufferHeight(), 1F, 10000F));
        RenderSystem.matrixMode(GL11.GL_MODELVIEW);
        RenderSystem.loadIdentity();

        RenderSystem.pushMatrix();

        renderWorkspace(mouseX, mouseY, partialTick);

        //Reset our perspective, and set up for drawing windows.
        RenderSystem.popMatrix();

        RenderSystem.matrixMode(GL11.GL_PROJECTION);
        RenderSystem.loadIdentity();
        RenderSystem.ortho(0.0D, minecraft.getMainWindow().getFramebufferWidth() / minecraft.getMainWindow().getGuiScaleFactor(), minecraft.getMainWindow().getFramebufferHeight() / minecraft.getMainWindow().getGuiScaleFactor(), 0.0D, -5000.0D, 5000.0D);
        RenderSystem.matrixMode(GL11.GL_MODELVIEW);
        RenderSystem.loadIdentity();

        renderCompass(partialTick);

        renderWindows(mouseX, mouseY, partialTick);

        renderTooltip(mouseX, mouseY, partialTick);

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
    }

    @Override
    public void renderBackground()
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

    public void renderWorkspace(int mouseX, int mouseY, float partialTick)
    {
        setupCamera(partialTick);

        //RENDER BLOCK
        Fragment<?> fragment = getById("buttonBlockToggle");
        if(fragment instanceof ElementToggle && ((ElementToggle<?>)fragment).toggleState) //render the block
        {
            //TODO fix themes
            //TODO change how this is done
            //TODO set the block from theme
            Block block = getTheme().block;
            if(block == null)
            {
                block = Blocks.SPRUCE_PLANKS;
            }

            net.minecraft.client.renderer.RenderHelper.setupGuiFlatDiffuseLighting();

            RenderHelper.renderBakedModel(Minecraft.getInstance().getBlockRendererDispatcher().getBlockModelShapes().getModel(block.getDefaultState()), new ItemStack(block));

            net.minecraft.client.renderer.RenderHelper.setupGui3DDiffuseLighting();
        }
        //END RENDER BLOCK

        renderModel(false);

        fragment = getById("buttonGridToggle");
        if(fragment instanceof ElementToggle && ((ElementToggle<?>)fragment).toggleState) //render the block
        {
            Minecraft.getInstance().getTextureManager().bindTexture(TEX_GRID_16);

            Tessellator tessellator = Tessellator.getInstance();
            BufferBuilder bufferbuilder = tessellator.getBuffer();
            int light1 = 15728880 >> 16 & 65535;
            int light2 = 15728880 & 65535;

            RenderSystem.enableBlend();
            RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);

            double dist = 0.125D;
            double pX = -3.495D - dist;
            double pY = 0.500125D;
            double pZ = -3.495D - dist;
            double w = 7 + (dist * 2);
            double l = 7 + (dist * 2);
            bufferbuilder.begin(7, DefaultVertexFormats.POSITION_COLOR_TEX_LIGHTMAP);
            bufferbuilder.pos(pX, pY, pZ + l).color(1.0F, 1.0F, 1.0F, 0.5F).tex(-0.125F, 7.125F).lightmap(light1, light2).endVertex();
            bufferbuilder.pos(pX + w, pY, pZ + l).color(1.0F, 1.0F, 1.0F, 0.5F).tex(7.125F, 7.125F).lightmap(light1, light2).endVertex();
            bufferbuilder.pos(pX + w, pY, pZ).color(1.0F, 1.0F, 1.0F, 0.5F).tex(7.125F, -0.125F).lightmap(light1, light2).endVertex();
            bufferbuilder.pos(pX, pY, pZ).color(1.0F, 1.0F, 1.0F, 0.5F).tex(-0.125F, -0.125F).lightmap(light1, light2).endVertex();
            bufferbuilder.pos(pX + w, pY, pZ + l).color(1.0F, 1.0F, 1.0F, 0.5F).tex(7.125F, 7.125F).lightmap(light1, light2).endVertex();
            bufferbuilder.pos(pX, pY, pZ + l).color(1.0F, 1.0F, 1.0F, 0.5F).tex(-0.125F, 7.125F).lightmap(light1, light2).endVertex();
            bufferbuilder.pos(pX, pY, pZ).color(1.0F, 1.0F, 1.0F, 0.5F).tex(-0.125F, -0.125F).lightmap(light1, light2).endVertex();
            bufferbuilder.pos(pX + w, pY, pZ).color(1.0F, 1.0F, 1.0F, 0.5F).tex(7.125F, -0.125F).lightmap(light1, light2).endVertex();
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
            RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);

            MatrixStack stack = new MatrixStack();

            stack.translate(0F, 2.0005F, 0F);
            stack.scale(-1F, -1F, 1F);

            if(selection)
            {
                info.project.getModel().renderForSelection(stack);
            }
            else
            {
                info.project.getModel().render(stack, info.getSelectedPart(), info.getSelectedBox());
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

    public void renderCompass(float partialTick)
    {
        RenderSystem.pushMatrix();

        Mainframe.Camera cam = mainframe.getCamera();

        double right = width;
        double bottom = height;
        for(Map.Entry<ArrayList<Window<?>>, Constraint.Property.Type> e : getDock().docked.entrySet())
        {
            for(Window<?> key : e.getKey())
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

        RenderSystem.pushMatrix();
        Block block = Blocks.FURNACE;
        net.minecraft.client.renderer.RenderHelper.setupGuiFlatDiffuseLighting();
        RenderHelper.renderBakedModel(Minecraft.getInstance().getBlockRendererDispatcher().getBlockModelShapes().getModel(block.getDefaultState()), new ItemStack(block));
        net.minecraft.client.renderer.RenderHelper.setupGui3DDiffuseLighting();
        RenderSystem.popMatrix();

        RenderSystem.enableBlend();
        RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);

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
        bufferbuilder.begin(7, DefaultVertexFormats.POSITION_COLOR_TEX_LIGHTMAP);
        bufferbuilder.pos(pX, pY, pZ + l).color(1.0F, 1.0F, 1.0F, 0.5F).tex(0.0F, 1.0F).lightmap(light1, light2).endVertex();
        bufferbuilder.pos(pX + w, pY, pZ + l).color(1.0F, 1.0F, 1.0F, 0.5F).tex(1.0F, 1.0F).lightmap(light1, light2).endVertex();
        bufferbuilder.pos(pX + w, pY, pZ).color(1.0F, 1.0F, 1.0F, 0.5F).tex(1.0F, 0.0F).lightmap(light1, light2).endVertex();
        bufferbuilder.pos(pX	  , pY, pZ).color(1.0F, 1.0F, 1.0F, 0.5F).tex(0.0F, 0.0F).lightmap(light1, light2).endVertex();
        bufferbuilder.pos(pX + w, pY, pZ + l).color(1.0F, 1.0F, 1.0F, 0.5F).tex(1.0F, 1.0F).lightmap(light1, light2).endVertex();
        bufferbuilder.pos(pX	  , pY, pZ + l).color(1.0F, 1.0F, 1.0F, 0.5F).tex(0.0F, 1.0F).lightmap(light1, light2).endVertex();
        bufferbuilder.pos(pX	  , pY, pZ).color(1.0F, 1.0F, 1.0F, 0.5F).tex(0.0F, 0.0F).lightmap(light1, light2).endVertex();
        bufferbuilder.pos(pX + w, pY, pZ).color(1.0F, 1.0F, 1.0F, 0.5F).tex(1.0F, 0.0F).lightmap(light1, light2).endVertex();
        tessellator.draw();

        RenderSystem.disableBlend();

        RenderSystem.popMatrix();
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
    public void onClose()
    {
        closing = true;
        if(canClose())
        {
            Minecraft mc = Minecraft.getInstance();
            super.onClose();
            if(oriScale != mc.gameSettings.guiScale)
            {
                mc.gameSettings.guiScale = oriScale;
                mc.updateWindowSize();
            }
        }
    }

    public boolean canClose()
    {
        return mainframe.projects.isEmpty();
    }

    public static WorkspaceTabula create(Screen lastScreen)
    {
        Minecraft mc = Minecraft.getInstance();
        WorkspaceTabula workspace = new WorkspaceTabula(lastScreen, new Mainframe(mc.getSession().getUsername()), mc.gameSettings.guiScale);
        if(Tabula.configClient.forceGuiScale >= 0)
        {
            mc.gameSettings.guiScale = Tabula.configClient.forceGuiScale;
            mc.updateWindowSize();
        }

        return workspace;
    }
}
