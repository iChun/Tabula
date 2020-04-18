package me.ichun.mods.tabula.client.render;

import com.mojang.blaze3d.matrix.MatrixStack;
import me.ichun.mods.tabula.client.model.ModelWaxTablet;
import me.ichun.mods.tabula.common.tileentity.TileEntityTabulaRasa;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.Vector3f;
import net.minecraft.client.renderer.model.Material;
import net.minecraft.client.renderer.texture.AtlasTexture;
import net.minecraft.client.renderer.tileentity.TileEntityRenderer;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.util.ResourceLocation;

public class TileRendererTabulaRasa extends TileEntityRenderer<TileEntityTabulaRasa>
{
    public static final Material MATERIAL_MODEL = new Material(AtlasTexture.LOCATION_BLOCKS_TEXTURE, new ResourceLocation("tabula", "model/tabularasa"));
    public ModelWaxTablet model;

    public TileRendererTabulaRasa(TileEntityRendererDispatcher dispatcher)
    {
        super(dispatcher);
        model = new ModelWaxTablet();
    }


    @Override
    public void render(TileEntityTabulaRasa tr, float partialTicks, MatrixStack matrixStackIn, IRenderTypeBuffer bufferIn, int combinedLightIn, int combinedOverlayIn)
    {
        matrixStackIn.push();
        matrixStackIn.translate(0.5D, 0.0625D, 0.5D);
        matrixStackIn.scale(-1F, -1F, 1F);

        matrixStackIn.rotate(Vector3f.YP.rotationDegrees((tr.facing.getHorizontalIndex() * 90F)));

        model.render(matrixStackIn, MATERIAL_MODEL.getBuffer(bufferIn, RenderType::getEntityCutoutNoCull), combinedLightIn, combinedOverlayIn, 1F, 1F, 1F, 1F);

        matrixStackIn.pop();

        //        if(!tr.host.isEmpty() && !tr.currentProj.isEmpty())
        //        {
        //            ProjectInfo info = ProjectHelper.projects.get(tr.currentProj);
        //            if(info != null && !info.destroyed)
        //            {
        //                if(info.model == null)
        //                {
        //                    info.initClient();
        //                }
        //
        //                ArrayList<CubeInfo> hidden = new ArrayList<>();
        //                for(CubeGroup group1 : info.cubeGroups)
        //                {
        //                    GuiWorkspace.addElementsForHiding(group1, hidden);
        //                }
        //
        //                GlStateManager.pushMatrix();
        //
        //                GlStateManager.enableBlend();
        //                GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        //
        //                GlStateManager.disableCull();
        //
        //                GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        //
        //                float scale = 0.3F;
        //
        //                float size = info.getMaximumSize();
        //                if(size != 0)
        //                {
        //                    scale = 0.3F * 16F / size;
        //                }
        //
        //                GlStateManager.rotate((tr.age + f) / 2F, 0.0F, 1.0F, 0.0F);
        //
        //                GlStateManager.translate(0.0F, -0.6F + (-0.075F * (float)Math.sin((tr.age + f) / 10F)), 0.0F);
        //
        //                GlStateManager.scale(scale, scale, scale);
        //
        //                GlStateManager.scale(1D / info.scale[0], 1D / info.scale[1], 1D / info.scale[2]);
        //
        //                if(info.bufferedTexture != null)
        //                {
        //                    Integer id = ProjectHelper.projectTextureIDs.computeIfAbsent(info.bufferedTexture, k -> TextureUtil.uploadTextureImage(TextureUtil.glGenTextures(), info.bufferedTexture));
        //
        //                    GlStateManager.bindTexture(id);
        //
        //                    info.model.render(0.0625F, new ArrayList<>(), hidden, 1.0F, true, 1, false, true);
        //                }
        //                else
        //                {
        //                    GlStateManager.disableTexture2D();
        //                    GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        //
        //                    info.model.render(0.0625F, new ArrayList<>(), hidden, 1.0F, false, 1, false, true);
        //
        //                    GlStateManager.enableTexture2D();
        //                }
        //
        //                GlStateManager.enableCull();
        //                GlStateManager.popMatrix();
        //            }
        //        }

    }
}
