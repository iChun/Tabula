package me.ichun.mods.tabula.client.model;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import me.ichun.mods.ichunutil.client.model.ModelTabula;
import net.minecraft.client.renderer.model.Model;
import net.minecraft.client.renderer.model.ModelRenderer;

/**
 * Created using Tabula 5.0.0
 */
public class ModelVoxel extends Model
{
    public ModelRenderer shape1;

    public ModelVoxel()
    {
        super(rl -> ModelTabula.RENDER_MODEL_COMPASS_FLAT);
        this.textureWidth = 4;
        this.textureHeight = 2;
        this.shape1 = new ModelRenderer(this, 0, 0);
        this.shape1.setRotationPoint(0.0F, 0.0F, 0.0F);
        this.shape1.addBox(-0.5F, -0.5F, -0.5F, 1, 1, 1, 0.0F);
    }

    @Override
    public void render(MatrixStack matrixStackIn, IVertexBuilder bufferIn, int packedLightIn, int packedOverlayIn, float red, float green, float blue, float alpha)
    {
        shape1.render(matrixStackIn, bufferIn, packedLightIn, packedOverlayIn, red, green, blue, alpha);
    }
}
