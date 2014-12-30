package us.ichun.mods.tabula.client.model;

import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.entity.Entity;
import org.lwjgl.opengl.GL11;

/**
 * Wax Tablet - iChun
 * Created using Tabula 4.1.0
 */
public class ModelWaxTablet extends ModelBase {
    public double[] modelScale = new double[] { 2.0D, 2.0D, 2.0D };
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

    public ModelWaxTablet() {
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
    }

    public void render(float f5) {
        GL11.glPushMatrix();
        GL11.glScaled(1D / modelScale[0], 1D / modelScale[1], 1D / modelScale[2]);
        this.bBindBottom.render(f5);
        this.lBorderBack.render(f5);
        this.bBindRight.render(f5);
        this.fBindLeft.render(f5);
        this.fBindTop.render(f5);
        this.lBorderFront.render(f5);
        this.rBorderR.render(f5);
        this.bBindLeft.render(f5);
        this.lBorderL.render(f5);
        this.rPage.render(f5);
        this.rBorderBack.render(f5);
        this.fBindBottom.render(f5);
        this.bBindTop.render(f5);
        this.fBindRight.render(f5);
        this.lPage.render(f5);
        this.rBorderFront.render(f5);
        this.lBorderR.render(f5);
        this.rBorderL.render(f5);
        GL11.glPopMatrix();
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
