package us.ichun.mods.tabula.client.model;

import ichun.client.model.ModelHelper;
import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.util.ResourceLocation;

import java.util.HashMap;

public class ModelInfo
{
    public final ResourceLocation texture;
    public final ModelBase modelParent;
    public final HashMap<String, ModelRenderer> modelList;
    public final Class clz;

    public ModelInfo(ResourceLocation texture, ModelBase modelParent, Class clz)
    {
        this.texture = texture;
        this.modelParent = modelParent;
        this.modelList = ModelHelper.getModelCubesWithNames(modelParent);
        this.clz = clz;
    }
}
