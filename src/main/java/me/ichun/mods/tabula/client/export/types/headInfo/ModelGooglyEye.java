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
    public ModelRenderer cornea1;
    public ModelRenderer cornea2;
    public ModelRenderer cornea3;
    public ModelRenderer cornea4;
    public ModelRenderer cornea5;
    public ModelRenderer cornea6;
    public ModelRenderer[] iris = new ModelRenderer[3];

    public ModelGooglyEye()
    {
        super(RenderType::getEntityCutout);

        this.textureWidth = 64;
        this.textureHeight = 32;

        this.cornea1 = new ModelRenderer(this, 0, 0);
        this.cornea1.setRotationPoint(0.0F, 0.0F, 0.0F);
        this.cornea1.addBox(-0.5F, -1.865F, -1.03F, 1, 3.73F, 1, 0.0F);

        this.cornea2 = new ModelRenderer(this, 0, 0);
        this.cornea2.setRotationPoint(0.0F, 0.0F, 0.0F);
        this.cornea2.addBox(-0.5F, -1.865F, -1.0F, 1, 3.73F, 1, 0.0F);
        this.setRotateAngle(cornea2, 0.0F, -0.0F, 0.5235987755982988F);

        this.cornea3 = new ModelRenderer(this, 0, 0);
        this.cornea3.setRotationPoint(0.0F, 0.0F, 0.0F);
        this.cornea3.addBox(-0.5F, -1.865F, -1.02F, 1, 3.73F, 1, 0.0F);
        this.setRotateAngle(cornea3, 0.0F, 0.0F, 1.0471975511965976F);

        this.cornea4 = new ModelRenderer(this, 0, 0);
        this.cornea4.setRotationPoint(0.0F, 0.0F, 0.0F);
        this.cornea4.addBox(-0.5F, -1.865F, -0.99F, 1, 3.73F, 1, 0.0F);
        this.setRotateAngle(cornea4, 0.0F, -0.0F, 1.5707963267948966F);

        this.cornea5 = new ModelRenderer(this, 0, 0);
        this.cornea5.setRotationPoint(0.0F, 0.0F, 0.0F);
        this.cornea5.addBox(-0.5F, -1.865F, -1.01F, 1, 3.73F, 1, 0.0F);
        this.setRotateAngle(cornea5, 0.0F, -0.0F, 2.0943951023931953F);

        this.cornea6 = new ModelRenderer(this, 0, 0);
        this.cornea6.setRotationPoint(0.0F, 0.0F, 0.0F);
        this.cornea6.addBox(-0.5F, -1.865F, -0.98F, 1, 3.73F, 1, 0.0F);
        this.setRotateAngle(cornea6, 0.0F, -0.0F, 2.6179938779914944F);

        iris[0] = new ModelRenderer(this, 0, 0);
        iris[0].setRotationPoint(0.0F, 0.0F, 0.0F);
        iris[0].addBox(-0.5F, -0.8665F, -1.51F, 1, 1.733F, 1, 0.0F);
        this.setRotateAngle(iris[0], 0.0F, -0.0F, 2.0943951023931953F);

        iris[1] = new ModelRenderer(this, 0, 0);
        iris[1].setRotationPoint(0.0F, 0.0F, 0.0F);
        iris[1].addBox(-0.5F, -0.8665F, -1.5F, 1, 1.733F, 1, 0.0F);
        this.setRotateAngle(iris[1], 0.0F, -0.0F, 1.0471975511965976F);

        iris[2] = new ModelRenderer(this, 0, 0);
        iris[2].setRotationPoint(0.0F, 0.0F, 0.0F);
        iris[2].addBox(-0.5F, -0.8665F, -1.49F, 1, 1.733F, 1, 0.0F);
    }

    @Override
    public void render(MatrixStack matrixStackIn, IVertexBuilder bufferIn, int packedLightIn, int packedOverlayIn, float red, float green, float blue, float alpha){}

    public void renderCornea(MatrixStack matrixStackIn, IVertexBuilder bufferIn, int packedLightIn, int packedOverlayIn, float red, float green, float blue, float alpha)
    {
        this.cornea1.render(matrixStackIn, bufferIn, packedLightIn, packedOverlayIn, red, green, blue, alpha);
        this.cornea2.render(matrixStackIn, bufferIn, packedLightIn, packedOverlayIn, red, green, blue, alpha);
        this.cornea3.render(matrixStackIn, bufferIn, packedLightIn, packedOverlayIn, red, green, blue, alpha);
        this.cornea4.render(matrixStackIn, bufferIn, packedLightIn, packedOverlayIn, red, green, blue, alpha);
        this.cornea5.render(matrixStackIn, bufferIn, packedLightIn, packedOverlayIn, red, green, blue, alpha);
        this.cornea6.render(matrixStackIn, bufferIn, packedLightIn, packedOverlayIn, red, green, blue, alpha);
    }

    public void renderIris(MatrixStack matrixStackIn, IVertexBuilder bufferIn, int packedLightIn, int packedOverlayIn, float red, float green, float blue, float alpha)
    {
        for(int i = 0; i < iris.length; i++)
        {
            iris[i].render(matrixStackIn, bufferIn, packedLightIn, packedOverlayIn, red, green, blue, alpha);
        }
    }

    public void moveIris(float x, float y, float pupilSize)
    {
        //pupilSize is not for scaling.
        float shiftFactor = (1.45F - pupilSize * 0.525F) / pupilSize;
        float rotX = -x * shiftFactor;// * (float)Math.cos(Math.toRadians((y / 1F) * 90F));
        float rotY = -y * shiftFactor * (float)Math.cos(Math.toRadians(x * 90F));

        for(int i = 0; i < iris.length; i++)
        {
            iris[i].rotationPointX = rotX;
            iris[i].rotationPointY = rotY;
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
