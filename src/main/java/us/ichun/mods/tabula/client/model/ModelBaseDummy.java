package us.ichun.mods.tabula.client.model;

import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelBox;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.client.renderer.GLAllocation;
import net.minecraft.entity.Entity;
import us.ichun.mods.tabula.common.project.ProjectInfo;
import us.ichun.mods.tabula.common.project.components.CubeInfo;

import java.util.ArrayList;

public class ModelBaseDummy extends ModelBase
{
    public ProjectInfo parent;
    public ArrayList<CubeInfo> cubes = new ArrayList<CubeInfo>();

    public ModelBaseDummy(ProjectInfo par)
    {
        parent = par;
        textureHeight = parent.textureHeight;
        textureWidth = parent.textureWidth;
    }

    @Override
    public void render(Entity ent, float f, float f1, float f2, float f3, float f4, float f5)
    {
    }

    public void render(float f5)
    {
        for(int i = 0; i < cubes.size(); i++)
        {
            CubeInfo info = cubes.get(i);
            if(info.modelCube != null)
            {
                info.modelCube.render(f5);
            }
        }
    }

    public void createModelFromCubeInfo(CubeInfo info)
    {
        cubes.add(info);

        info.modelCube = new ModelRenderer(this, info.txOffset[0], info.txOffset[1]);
        info.modelCube.addBox((float)info.offset[0], (float)info.offset[1], (float)info.offset[2], info.dimensions[0], info.dimensions[1], info.dimensions[2]);
        info.modelCube.setRotationPoint((float)info.position[0], (float)info.position[1], (float)info.position[2]);
        info.modelCube.rotateAngleX = (float)Math.toRadians(info.rotation[0]);
        info.modelCube.rotateAngleY = (float)Math.toRadians(info.rotation[1]);
        info.modelCube.rotateAngleZ = (float)Math.toRadians(info.rotation[2]);
    }

    public void removeCubeInfo(CubeInfo info)
    {
        deleteModelDisplayList(info);
        cubes.remove(info);
    }

    private void deleteModelDisplayList(CubeInfo info)//Done to free up Graphics memory
    {
        for(CubeInfo info1 : info.children)
        {
            deleteModelDisplayList(info1);
        }
        if(info.modelCube != null && info.modelCube.compiled)
        {
            GLAllocation.deleteDisplayLists(info.modelCube.displayList);
            info.modelCube = null;
        }
    }
}
