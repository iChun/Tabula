package me.ichun.mods.tabula.client.export.types.headInfo;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.model.Model;
import net.minecraft.client.renderer.model.ModelRenderer;

/**
 * Googly Eyes - iChun
 * Created using Tabula 5.1.0
 * Taken from Googly Eyes 10.0.0
 */
public class ModelGooglyEye extends Model
{
    public ModelRenderer iris12;
    public ModelRenderer iris11;
    public ModelRenderer iris10;
    public ModelRenderer iris9;
    public ModelRenderer iris8;
    public ModelRenderer iris7;
    public ModelRenderer iris6;
    public ModelRenderer iris5;
    public ModelRenderer iris4;
    public ModelRenderer iris3;
    public ModelRenderer iris2;
    public ModelRenderer iris1;
    public ModelRenderer[] pupils = new ModelRenderer[6];

    public ModelGooglyEye()
    {
        super(RenderType::getEntityCutout);

        this.textureWidth = 64;
        this.textureHeight = 32;
        this.iris6 = new ModelRenderer(this, 0, 0);
        this.iris6.setRotationPoint(0.0F, 0.0F, 0.0F);
        this.iris6.addBox(-0.5F, -1.88F, -1.0F, 1, 3, 1, 0.0F);
        this.setRotateAngle(iris6, 0.0F, -0.0F, -3.141592653589793F);
        this.iris12 = new ModelRenderer(this, 0, 0);
        this.iris12.setRotationPoint(0.0F, 0.0F, 0.0F);
        this.iris12.addBox(-0.5F, -1.88F, -1.0F, 1, 3, 1, 0.0F);
        this.iris2 = new ModelRenderer(this, 0, 0);
        this.iris2.setRotationPoint(0.0F, 0.0F, 0.0F);
        this.iris2.addBox(-0.5F, -1.88F, -1.0F, 1, 3, 1, 0.0F);
        this.setRotateAngle(iris2, 0.0F, -0.0F, -1.0471975511965976F);
        this.iris1 = new ModelRenderer(this, 0, 0);
        this.iris1.setRotationPoint(0.0F, 0.0F, 0.0F);
        this.iris1.addBox(-0.5F, -1.88F, -1.0F, 1, 3, 1, 0.0F);
        this.setRotateAngle(iris1, 0.0F, -0.0F, -0.5235987755982988F);
        this.iris11 = new ModelRenderer(this, 0, 0);
        this.iris11.setRotationPoint(0.0F, 0.0F, 0.0F);
        this.iris11.addBox(-0.5F, -1.88F, -1.0F, 1, 3, 1, 0.0F);
        this.setRotateAngle(iris11, 0.0F, -0.0F, 0.5235987755982988F);
        pupils[0] = new ModelRenderer(this, 0, 0);
        pupils[0].setRotationPoint(0.0F, 0.0F, 0.0F);
        pupils[0].addBox(-0.5F, -0.88F, -1.5F, 1, 1, 1, 0.0F);
        this.setRotateAngle(pupils[0], 0.0F, -0.0F, -1.0471975511965976F);
        this.iris4 = new ModelRenderer(this, 0, 0);
        this.iris4.setRotationPoint(0.0F, 0.0F, 0.0F);
        this.iris4.addBox(-0.5F, -1.88F, -1.0F, 1, 3, 1, 0.0F);
        this.setRotateAngle(iris4, 0.0F, -0.0F, -2.0943951023931953F);
        pupils[3] = new ModelRenderer(this, 0, 0);
        pupils[3].setRotationPoint(0.0F, 0.0F, 0.0F);
        pupils[3].addBox(-0.5F, -0.88F, -1.5F, 1, 1, 1, 0.0F);
        this.setRotateAngle(pupils[3], 0.0F, -0.0F, 2.0943951023931953F);
        this.iris5 = new ModelRenderer(this, 0, 0);
        this.iris5.setRotationPoint(0.0F, 0.0F, 0.0F);
        this.iris5.addBox(-0.5F, -1.88F, -1.0F, 1, 3, 1, 0.0F);
        this.setRotateAngle(iris5, 0.0F, -0.0F, -2.6179938779914944F);
        this.iris7 = new ModelRenderer(this, 0, 0);
        this.iris7.setRotationPoint(0.0F, 0.0F, 0.0F);
        this.iris7.addBox(-0.5F, -1.88F, -1.0F, 1, 3, 1, 0.0F);
        this.setRotateAngle(iris7, 0.0F, -0.0F, 2.6179938779914944F);
        pupils[5] = new ModelRenderer(this, 0, 0);
        pupils[5].setRotationPoint(0.0F, 0.0F, 0.0F);
        pupils[5].addBox(-0.5F, -0.88F, -1.5F, 1, 1, 1, 0.0F);
        pupils[4] = new ModelRenderer(this, 0, 0);
        pupils[4].setRotationPoint(0.0F, 0.0F, 0.0F);
        pupils[4].addBox(-0.5F, -0.88F, -1.5F, 1, 1, 1, 0.0F);
        this.setRotateAngle(pupils[4], 0.0F, -0.0F, 1.0471975511965976F);
        this.iris3 = new ModelRenderer(this, 0, 0);
        this.iris3.setRotationPoint(0.0F, 0.0F, 0.0F);
        this.iris3.addBox(-0.5F, -1.88F, -1.0F, 1, 3, 1, 0.0F);
        this.setRotateAngle(iris3, 0.0F, -0.0F, -1.5707963267948966F);
        pupils[2] = new ModelRenderer(this, 0, 0);
        pupils[2].setRotationPoint(0.0F, 0.0F, 0.0F);
        pupils[2].addBox(-0.5F, -0.88F, -1.5F, 1, 1, 1, 0.0F);
        this.setRotateAngle(pupils[2], 0.0F, -0.0F, -3.141592653589793F);
        this.iris9 = new ModelRenderer(this, 0, 0);
        this.iris9.setRotationPoint(0.0F, 0.0F, 0.0F);
        this.iris9.addBox(-0.5F, -1.88F, -1.0F, 1, 3, 1, 0.0F);
        this.setRotateAngle(iris9, 0.0F, -0.0F, 1.5707963267948966F);
        pupils[1] = new ModelRenderer(this, 0, 0);
        pupils[1].setRotationPoint(0.0F, 0.0F, 0.0F);
        pupils[1].addBox(-0.5F, -0.88F, -1.5F, 1, 1, 1, 0.0F);
        this.setRotateAngle(pupils[1], 0.0F, -0.0F, -2.0943951023931953F);
        this.iris10 = new ModelRenderer(this, 0, 0);
        this.iris10.setRotationPoint(0.0F, 0.0F, 0.0F);
        this.iris10.addBox(-0.5F, -1.88F, -1.0F, 1, 3, 1, 0.0F);
        this.setRotateAngle(iris10, 0.0F, 0.0F, 1.0471975511965976F);
        this.iris8 = new ModelRenderer(this, 0, 0);
        this.iris8.setRotationPoint(0.0F, 0.0F, 0.0F);
        this.iris8.addBox(-0.5F, -1.88F, -1.0F, 1, 3, 1, 0.0F);
        this.setRotateAngle(iris8, 0.0F, -0.0F, 2.0943951023931953F);
    }

    @Override
    public void render(MatrixStack matrixStackIn, IVertexBuilder bufferIn, int packedLightIn, int packedOverlayIn, float red, float green, float blue, float alpha){}

    public void renderIris(MatrixStack matrixStackIn, IVertexBuilder bufferIn, int packedLightIn, int packedOverlayIn, float red, float green, float blue, float alpha)
    {
        this.iris1.render(matrixStackIn, bufferIn, packedLightIn, packedOverlayIn, red, green, blue, alpha);
        this.iris2.render(matrixStackIn, bufferIn, packedLightIn, packedOverlayIn, red, green, blue, alpha);
        this.iris3.render(matrixStackIn, bufferIn, packedLightIn, packedOverlayIn, red, green, blue, alpha);
        this.iris4.render(matrixStackIn, bufferIn, packedLightIn, packedOverlayIn, red, green, blue, alpha);
        this.iris5.render(matrixStackIn, bufferIn, packedLightIn, packedOverlayIn, red, green, blue, alpha);
        this.iris6.render(matrixStackIn, bufferIn, packedLightIn, packedOverlayIn, red, green, blue, alpha);
        this.iris7.render(matrixStackIn, bufferIn, packedLightIn, packedOverlayIn, red, green, blue, alpha);
        this.iris8.render(matrixStackIn, bufferIn, packedLightIn, packedOverlayIn, red, green, blue, alpha);
        this.iris9.render(matrixStackIn, bufferIn, packedLightIn, packedOverlayIn, red, green, blue, alpha);
        this.iris10.render(matrixStackIn, bufferIn, packedLightIn, packedOverlayIn, red, green, blue, alpha);
        this.iris11.render(matrixStackIn, bufferIn, packedLightIn, packedOverlayIn, red, green, blue, alpha);
        this.iris12.render(matrixStackIn, bufferIn, packedLightIn, packedOverlayIn, red, green, blue, alpha);
    }

    public void renderPupil(MatrixStack matrixStackIn, IVertexBuilder bufferIn, int packedLightIn, int packedOverlayIn, float red, float green, float blue, float alpha)
    {
        for(int i = 0; i < pupils.length; i++)
        {
            pupils[i].render(matrixStackIn, bufferIn, packedLightIn, packedOverlayIn, red, green, blue, alpha);
        }
    }

    public void movePupil(float x, float y, float pupilSize)
    {
        //irissize will never be used, entire eye is scaled.
        float shiftFactor = (1.45F - pupilSize * 0.525F) / pupilSize;
        for(int i = 0; i < pupils.length; i++)
        {
            pupils[i].rotationPointX = -x * shiftFactor;// * (float)Math.cos(Math.toRadians((y / 1F) * 90F));
            pupils[i].rotationPointY = -y * shiftFactor * (float)Math.cos(Math.toRadians(x * 90F));
        }
    }

    /**
     * This is a helper function from Tabula to set the rotation of model parts
     */
    public void setRotateAngle(ModelRenderer modelRenderer, float x, float y, float z) {
        modelRenderer.rotateAngleX = x;
        modelRenderer.rotateAngleY = y;
        modelRenderer.rotateAngleZ = z;
    }
}
