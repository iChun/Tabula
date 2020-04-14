package me.ichun.mods.tabula.old.client.model;

import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.client.renderer.GLAllocation;

/**
 * Created using Tabula 5.0.0
 */
public class ModelVoxel extends ModelBase
{
    public ModelRenderer shape1;

    public ModelVoxel() {
        this.textureWidth = 4;
        this.textureHeight = 2;
        this.shape1 = new ModelRenderer(this, 0, 0);
        this.shape1.setRotationPoint(0.0F, 0.0F, 0.0F);
        this.shape1.addBox(-0.5F, -0.5F, -0.5F, 1, 1, 1, 0.0F);
    }

    public void render(float f5) {
        this.shape1.render(f5);
    }

    public void destroy()
    {
        if(shape1.compiled)
        {
            GLAllocation.deleteDisplayLists(shape1.displayList);
        }
    }
}
