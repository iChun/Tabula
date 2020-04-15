package me.ichun.mods.tabula.client.gui;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import me.ichun.mods.ichunutil.client.gui.bns.Workspace;
import me.ichun.mods.ichunutil.client.gui.bns.window.Fragment;
import me.ichun.mods.ichunutil.client.gui.bns.window.Window;
import me.ichun.mods.ichunutil.client.gui.bns.window.WindowDock;
import me.ichun.mods.ichunutil.client.gui.bns.window.WindowPopup;
import me.ichun.mods.ichunutil.client.gui.bns.window.constraint.Constraint;
import me.ichun.mods.ichunutil.client.gui.bns.window.view.element.ElementToggle;
import me.ichun.mods.ichunutil.client.render.RenderHelper;
import me.ichun.mods.ichunutil.common.iChunUtil;
import me.ichun.mods.ichunutil.common.module.tabula.project.Identifiable;
import me.ichun.mods.ichunutil.common.module.tabula.project.Project;
import me.ichun.mods.tabula.client.core.ResourceHelper;
import me.ichun.mods.tabula.client.gui.window.*;
import me.ichun.mods.tabula.client.tabula.Mainframe;
import me.ichun.mods.tabula.common.Tabula;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.Matrix4f;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.resources.I18n;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TranslationTextComponent;
import org.lwjgl.opengl.GL11;

import java.util.List;

public class WorkspaceTabula extends Workspace
    implements IProjectInfo
{
    public static final ResourceLocation TEX_GRID_16 = new ResourceLocation("tabula", "textures/workspace/grid16.png");
    public static final ResourceLocation TEX_COMPASS_BASE = new ResourceLocation("tabula", "textures/workspace/orientationbase.png");

    public final Mainframe mainframe;
    public final int oriScale;
    public final WindowToolbar windowToolbar;

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
        Window<?> window = getWindowType(WindowProjectNavigator.class);
        if(window == null)
        {
            window = new WindowProjectNavigator(this);
            addToDock(window, Constraint.Property.Type.TOP);
            window.init();
        }

        List<Window<?>> children = children();
        children.stream().filter(child -> child instanceof IProjectInfo).forEach(child -> ((IProjectInfo)child).setCurrentProject(info));
    }

    public boolean selectPart(Project.Part part) //TODO select the first box when you click on this.... if there is a box
    {
        Window<?> window = getWindowType(WindowPartInfo.class);
        if(window == null)
        {
            window = new WindowPartInfo(this);
            addToDock(window, Constraint.Property.Type.LEFT);
            window.init();
        }

        return ((WindowPartInfo)window).selectPart(part);
    }

    public void selectBox(Project.Part.Box box)
    {
        if(box != null)
        {
            Identifiable<?> id = mainframe.getActiveProject().project.getById(box.parentIdent);
            if(id instanceof Project.Part)
            {
                selectPart((Project.Part)id);

                //we've selected the parent. we can show the selector now.
                Window<?> window = getWindowType(WindowBoxInfo.class);
                if(window == null)
                {
                    Window<?> partInfo = getWindowType(WindowPartInfo.class);
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

                ((WindowBoxInfo)window).selectBox(box);
                return;
            }
        }

        //box is null, or box has no part parent. Deselect.
        Window<?> window = getWindowType(WindowBoxInfo.class);
        if(window != null)
        {
            ((WindowBoxInfo)window).selectBox(null);
        }
    }

    @Override
    protected void init()
    {
        super.init();

        Project project = new Project();
        project.name = "MyFirstModel";
        project.author = "sugar tits";
        project.texWidth = 64;
        project.texHeight = 32;
        this.mainframe.openProject(project);

        this.mainframe.getActiveProject().addPart(null);
    }

    @Override
    public void tick()
    {
        super.tick();
        mainframe.tick();
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

        renderCompass();

        renderWindows(mouseX, mouseY, partialTick);

        renderTooltip(mouseX, mouseY, partialTick);

        resetBackground();

        RenderSystem.enableAlphaTest();
        RenderSystem.depthMask(false);
        RenderSystem.disableDepthTest();
    }

    @Override
    public void renderBackground()
    {
//        if(renderMinecraftStyle())
//        {
//            this.renderBackground(0);
//        }
//        else
        {
            RenderSystem.clearColor((float)getTheme().workspaceBackground[0] / 255F, (float)getTheme().workspaceBackground[1] / 255F, (float)getTheme().workspaceBackground[2] / 255F, 255F);
            RenderSystem.clear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT, Minecraft.IS_RUNNING_ON_MAC);
        }

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

    public void renderCompass()
    {
        Block block = Blocks.FURNACE;

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
        Minecraft mc = Minecraft.getInstance();
        super.onClose();
        if(oriScale != mc.gameSettings.guiScale)
        {
            mc.gameSettings.guiScale = oriScale;
            mc.updateWindowSize();
        }
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
