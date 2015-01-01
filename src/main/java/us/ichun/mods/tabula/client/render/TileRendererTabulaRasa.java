package us.ichun.mods.tabula.client.render;

import net.minecraft.client.renderer.texture.TextureUtil;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;
import us.ichun.mods.tabula.client.gui.GuiWorkspace;
import us.ichun.mods.tabula.client.gui.window.element.ElementListTree;
import us.ichun.mods.tabula.client.mainframe.core.ProjectHelper;
import us.ichun.mods.tabula.client.model.ModelWaxTablet;
import us.ichun.mods.tabula.common.tileentity.TileEntityTabulaRasa;
import us.ichun.module.tabula.common.project.ProjectInfo;
import us.ichun.module.tabula.common.project.components.CubeGroup;
import us.ichun.module.tabula.common.project.components.CubeInfo;

import java.util.ArrayList;

public class TileRendererTabulaRasa extends TileEntitySpecialRenderer
{
    public ModelWaxTablet model;
    public final ResourceLocation txModel = new ResourceLocation("tabula", "textures/model/tabulaRasa.png");

    public TileRendererTabulaRasa()
    {
        model = new ModelWaxTablet();
    }

    public void renderTabulaRasa(TileEntityTabulaRasa tr, double d, double d1, double d2, float f)
    {
        GL11.glPushMatrix();

        GL11.glTranslated(d + 0.5D, d1 + 1/16D, d2 + 0.5D);
        GL11.glScalef(-1F, -1F, 1F);

        GL11.glRotatef((tr.side * 90F), 0.0F, 1.0F, 0.0F);

        bindTexture(txModel);
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

                GL11.glPushMatrix();

                GL11.glEnable(GL11.GL_BLEND);
                GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);

                GL11.glDisable(GL11.GL_CULL_FACE);

                GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);

                float scale = 0.3F;

                float size = info.getMaximumSize();
                if(size != 0)
                {
                    scale = 0.3F * 16F / size;
                }

                GL11.glRotatef((tr.age + f) / 2F, 0.0F, 1.0F, 0.0F);

                GL11.glTranslatef(0.0F, -0.6F + (-0.075F * (float)Math.sin((tr.age + f) / 10F)), 0.0F);

                GL11.glScalef(scale, scale, scale);

                GL11.glScaled(1D / info.scale[0], 1D / info.scale[1], 1D / info.scale[2]);

                if(info.bufferedTexture != null)
                {
                    Integer id = ProjectHelper.projectTextureIDs.get(info.bufferedTexture);
                    if(id == null)
                    {
                        id = TextureUtil.uploadTextureImage(TextureUtil.glGenTextures(), info.bufferedTexture);
                        ProjectHelper.projectTextureIDs.put(info.bufferedTexture, id);
                    }

                    GL11.glBindTexture(GL11.GL_TEXTURE_2D, id);

                    info.model.render(0.0625F, new ArrayList<CubeInfo>(), hidden, 1.0F, true, 1, false);
                }
                else
                {
                    GL11.glDisable(GL11.GL_TEXTURE_2D);
                    GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);

                    info.model.render(0.0625F, new ArrayList<CubeInfo>(), hidden, 1.0F, false, 1, false);

                    GL11.glEnable(GL11.GL_TEXTURE_2D);
                }

                GL11.glEnable(GL11.GL_CULL_FACE);
                GL11.glPopMatrix();
            }
        }

        GL11.glPopMatrix();
    }

    @Override
    public void renderTileEntityAt(TileEntity tileentity, double d0, double d1, double d2, float f)
    {
        renderTabulaRasa((TileEntityTabulaRasa)tileentity, d0, d1, d2, f);
    }
}
