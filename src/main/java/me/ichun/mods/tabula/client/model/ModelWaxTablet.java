package me.ichun.mods.tabula.client.model;

import com.google.common.collect.ImmutableList;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.model.Model;
import net.minecraft.client.renderer.model.ModelRenderer;

import java.util.List;

/**
 * Wax Tablet - iChun
 * Created using Tabula 4.1.0
 * Updated whilst writing Tabula 8.0.0 :)
 */
public class ModelWaxTablet extends Model
{
    public float[] modelScale = new float[] { 2.0F, 2.0F, 2.0F };
    public ModelRenderer bBindTop;
    public ModelRenderer bBindLeft;
    public ModelRenderer bBindRight;
    public ModelRenderer bBindBottom;
    public ModelRenderer fBindTop;
    public ModelRenderer fBindLeft;
    public ModelRenderer fBindRight;
    public ModelRenderer fBindBottom;
    public ModelRenderer lBorderL;
    public ModelRenderer lBorderR;
    public ModelRenderer lBorderFront;
    public ModelRenderer lBorderBack;
    public ModelRenderer lPage;
    public ModelRenderer rBorderL;
    public ModelRenderer rBorderR;
    public ModelRenderer rBorderFront;
    public ModelRenderer rBorderBack;
    public ModelRenderer rPage;
    public List<ModelRenderer> modelRenderers;

    public ModelWaxTablet() {
        super(RenderType::getEntityCutoutNoCull);
        this.textureWidth = 64;
        this.textureHeight = 32;
        this.bBindBottom = new ModelRenderer(this, 0, 0);
        this.bBindBottom.setRotationPoint(-1.0F, 0.8999999999999986F, 3.0F);
        this.bBindBottom.addBox(0.0F, 0.0F, 0.0F, 2, 1, 1, 0.0F);
        this.lBorderBack = new ModelRenderer(this, 0, 15);
        this.lBorderBack.setRotationPoint(-9.5F, 0.0F, 7.0F);
        this.lBorderBack.addBox(0.0F, 0.0F, 0.0F, 7, 2, 2, 0.0F);
        this.bBindRight = new ModelRenderer(this, 0, 2);
        this.bBindRight.setRotationPoint(1.0F, -1.2000000000000028F, 3.0F);
        this.bBindRight.addBox(0.0F, 0.0F, 0.0F, 1, 2, 1, 0.0F);
        this.fBindLeft = new ModelRenderer(this, 0, 2);
        this.fBindLeft.setRotationPoint(-2.0F, -1.2000000000000028F, -4.0F);
        this.fBindLeft.addBox(0.0F, 0.0F, 0.0F, 1, 2, 1, 0.0F);
        this.fBindTop = new ModelRenderer(this, 0, 0);
        this.fBindTop.setRotationPoint(-1.0F, -1.2000000000000028F, -4.0F);
        this.fBindTop.addBox(0.0F, 0.0F, 0.0F, 2, 1, 1, 0.0F);
        this.lBorderFront = new ModelRenderer(this, 0, 15);
        this.lBorderFront.setRotationPoint(-9.5F, 0.0F, -9.0F);
        this.lBorderFront.addBox(0.0F, 0.0F, 0.0F, 7, 2, 2, 0.0F);
        this.rBorderR = new ModelRenderer(this, 24, 0);
        this.rBorderR.setRotationPoint(9.5F, 0.0F, -9.0F);
        this.rBorderR.addBox(0.0F, 0.0F, 0.0F, 2, 2, 18, 0.0F);
        this.bBindLeft = new ModelRenderer(this, 0, 2);
        this.bBindLeft.setRotationPoint(-2.0F, -1.2000000000000028F, 3.0F);
        this.bBindLeft.addBox(0.0F, 0.0F, 0.0F, 1, 2, 1, 0.0F);
        this.lBorderL = new ModelRenderer(this, 24, 0);
        this.lBorderL.setRotationPoint(-11.5F, 0.0F, -9.0F);
        this.lBorderL.addBox(0.0F, 0.0F, 0.0F, 2, 2, 18, 0.0F);
        this.rPage = new ModelRenderer(this, -8, 0);
        this.rPage.setRotationPoint(2.5F, 1.0F, -7.0F);
        this.rPage.addBox(0.0F, 0.0F, 0.0F, 7, 1, 14, 0.0F);
        this.rBorderBack = new ModelRenderer(this, 0, 15);
        this.rBorderBack.setRotationPoint(2.5F, 0.0F, 7.0F);
        this.rBorderBack.addBox(0.0F, 0.0F, 0.0F, 7, 2, 2, 0.0F);
        this.fBindBottom = new ModelRenderer(this, 0, 0);
        this.fBindBottom.setRotationPoint(-1.0F, 0.8999999999999986F, -4.0F);
        this.fBindBottom.addBox(0.0F, 0.0F, 0.0F, 2, 1, 1, 0.0F);
        this.bBindTop = new ModelRenderer(this, 0, 0);
        this.bBindTop.setRotationPoint(-1.0F, -1.2000000000000028F, 3.0F);
        this.bBindTop.addBox(0.0F, 0.0F, 0.0F, 2, 1, 1, 0.0F);
        this.fBindRight = new ModelRenderer(this, 0, 2);
        this.fBindRight.setRotationPoint(1.0F, -1.2000000000000028F, -4.0F);
        this.fBindRight.addBox(0.0F, 0.0F, 0.0F, 1, 2, 1, 0.0F);
        this.lPage = new ModelRenderer(this, -8, 0);
        this.lPage.setRotationPoint(-9.5F, 1.0F, -7.0F);
        this.lPage.addBox(0.0F, 0.0F, 0.0F, 7, 1, 14, 0.0F);
        this.rBorderFront = new ModelRenderer(this, 0, 15);
        this.rBorderFront.setRotationPoint(2.5F, 0.0F, -9.0F);
        this.rBorderFront.addBox(0.0F, 0.0F, 0.0F, 7, 2, 2, 0.0F);
        this.lBorderR = new ModelRenderer(this, 2, 2);
        this.lBorderR.setRotationPoint(-2.5F, 0.0F, -9.0F);
        this.lBorderR.addBox(0.0F, 0.0F, 0.0F, 2, 2, 18, 0.0F);
        this.rBorderL = new ModelRenderer(this, 2, 2);
        this.rBorderL.setRotationPoint(0.5F, 0.0F, -9.0F);
        this.rBorderL.addBox(0.0F, 0.0F, 0.0F, 2, 2, 18, 0.0F);

        modelRenderers = ImmutableList.of(
                this.bBindBottom,
                this.lBorderBack,
                this.bBindRight,
                this.fBindLeft,
                this.fBindTop,
                this.lBorderFront,
                this.rBorderR,
                this.bBindLeft,
                this.lBorderL,
                this.rPage,
                this.rBorderBack,
                this.fBindBottom,
                this.bBindTop,
                this.fBindRight,
                this.lPage,
                this.rBorderFront,
                this.lBorderR,
                this.rBorderL);
    }

    @Override
    public void render(MatrixStack matrixStackIn, IVertexBuilder bufferIn, int packedLightIn, int packedOverlayIn, float red, float green, float blue, float alpha)
    {
        matrixStackIn.scale(1F / modelScale[0], 1F / modelScale[1], 1F / modelScale[2]);
        modelRenderers.forEach(modelRenderer -> {
            modelRenderer.render(matrixStackIn, bufferIn, packedLightIn, packedOverlayIn, red, green, blue, alpha);
        });
    }
}
