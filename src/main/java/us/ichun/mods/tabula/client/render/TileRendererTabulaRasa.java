package us.ichun.mods.tabula.client.render;

import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.texture.TextureUtil;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;
import us.ichun.mods.ichunutil.common.module.tabula.common.project.ProjectInfo;
import us.ichun.mods.ichunutil.common.module.tabula.common.project.components.CubeGroup;
import us.ichun.mods.ichunutil.common.module.tabula.common.project.components.CubeInfo;
import us.ichun.mods.tabula.client.gui.GuiWorkspace;
import us.ichun.mods.tabula.client.mainframe.core.ProjectHelper;
import us.ichun.mods.tabula.client.model.ModelWaxTablet;
import us.ichun.mods.tabula.common.tileentity.TileEntityTabulaRasa;

import java.util.ArrayList;

public class TileRendererTabulaRasa extends TileEntitySpecialRenderer
{
    public ModelWaxTablet model;
    public final ResourceLocation txModel = new ResourceLocation("tabula", "textures/model/tabulaRasa.png");

    public TileRendererTabulaRasa()
    {
        model = new ModelWaxTablet();
    }

    public void renderTabulaRasa(TileEntityTabulaRasa tr, double d, double d1, double d2, float f, int destroyState)
    {
        GlStateManager.pushMatrix();

        GlStateManager.translate(d + 0.5D, d1 + 1 / 16D, d2 + 0.5D);
        GlStateManager.scale(-1F, -1F, 1F);

        GlStateManager.rotate((tr.side * 90F), 0.0F, 1.0F, 0.0F);

        if (destroyState >= 0)
        {
            this.bindTexture(DESTROY_STAGES[destroyState]);
            GlStateManager.matrixMode(GL11.GL_TEXTURE);
            GlStateManager.pushMatrix();
            GlStateManager.scale(4.0F, 2.0F, 1.0F);
            GlStateManager.translate(0.0625F, 0.0625F, 0.0625F);
            GlStateManager.matrixMode(GL11.GL_MODELVIEW);
        }
        else
        {
            bindTexture(txModel);
        }

        model.render(0.0625F);

        if(!tr.host.isEmpty() && !tr.currentProj.isEmpty())
        {
            ProjectInfo info = ProjectHelper.projects.get(tr.currentProj);
            if(info != null && !info.destroyed)
            {
                if(info.model == null)
                {
                    info.initClient();
                }

                ArrayList<CubeInfo> hidden = new ArrayList<CubeInfo>();
                for(CubeGroup group1 : info.cubeGroups)
                {
                    GuiWorkspace.addElementsForHiding(group1, hidden);
                }

                GlStateManager.pushMatrix();

                GlStateManager.enableBlend();
                GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);

                GlStateManager.disableCull();

                GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);

                float scale = 0.3F;

                float size = info.getMaximumSize();
                if(size != 0)
                {
                    scale = 0.3F * 16F / size;
                }

                GlStateManager.rotate((tr.age + f) / 2F, 0.0F, 1.0F, 0.0F);

                GlStateManager.translate(0.0F, -0.6F + (-0.075F * (float)Math.sin((tr.age + f) / 10F)), 0.0F);

                GlStateManager.scale(scale, scale, scale);

                GlStateManager.scale(1D / info.scale[0], 1D / info.scale[1], 1D / info.scale[2]);

                if(info.bufferedTexture != null)
                {
                    Integer id = ProjectHelper.projectTextureIDs.get(info.bufferedTexture);
                    if(id == null)
                    {
                        id = TextureUtil.uploadTextureImage(TextureUtil.glGenTextures(), info.bufferedTexture);
                        ProjectHelper.projectTextureIDs.put(info.bufferedTexture, id);
                    }

                    GlStateManager.bindTexture(id);

                    info.model.render(0.0625F, new ArrayList<CubeInfo>(), hidden, 1.0F, true, 1, false, true);
                }
                else
                {
                    GlStateManager.disableTexture2D();
                    GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);

                    info.model.render(0.0625F, new ArrayList<CubeInfo>(), hidden, 1.0F, false, 1, false, true);

                    GlStateManager.enableTexture2D();
                }

                GlStateManager.enableCull();
                GlStateManager.popMatrix();
            }
        }

        if (destroyState >= 0)
        {
            GlStateManager.matrixMode(GL11.GL_TEXTURE);
            GlStateManager.popMatrix();
            GlStateManager.matrixMode(GL11.GL_MODELVIEW);
        }

        GlStateManager.popMatrix();
    }

    @Override
    public void renderTileEntityAt(TileEntity tileentity, double d0, double d1, double d2, float f, int destroyState)
    {
        renderTabulaRasa((TileEntityTabulaRasa)tileentity, d0, d1, d2, f, destroyState);
    }
}
