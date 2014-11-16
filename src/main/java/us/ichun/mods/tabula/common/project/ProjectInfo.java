package us.ichun.mods.tabula.common.project;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.minecraft.client.model.ModelQuadruped;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.client.renderer.entity.RenderCow;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.entity.RenderPig;
import net.minecraft.client.renderer.entity.RendererLivingEntity;
import net.minecraft.entity.passive.EntityCow;
import net.minecraft.entity.passive.EntityPig;
import us.ichun.mods.tabula.client.model.ModelBaseDummy;
import us.ichun.mods.tabula.common.project.components.CubeGroup;
import us.ichun.mods.tabula.common.project.components.CubeInfo;

import java.io.File;
import java.util.ArrayList;

public class ProjectInfo
{
    public transient String identifier;
    public transient File saveFile;

    public transient float cameraZoom = 1.0F;
    public transient float cameraYaw;
    public transient float cameraPitch;
    public transient float cameraOffsetX;
    public transient float cameraOffsetY;

    public transient ModelBaseDummy model;

    public String modelName;
    public String authorName;
    public String projVersion;

    public int textureWidth = 64;
    public int textureHeight = 32;

    public ArrayList<CubeGroup> cubeGroups;
    public ArrayList<CubeInfo> cubes;

    public ProjectInfo()
    {
        cameraZoom = 1.0F;
    }

    public ProjectInfo(String name, String author)
    {
        this();
        modelName = name;
        authorName = author;

        cubeGroups = new ArrayList<CubeGroup>();
        cubes = new ArrayList<CubeInfo>();
    }

    public String getAsJson()
    {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
//        Gson gson = new Gson();

        return gson.toJson(this);
    }

    public void initClient()
    {
        model = new ModelBaseDummy(this);
        for(int i = 0 ; i < cubes.size(); i++)
        {
            model.createModelFromCubeInfo(cubes.get(i));
        }
//        final RenderCow render = ((RenderCow)RenderManager.instance.getEntityClassRenderObject(EntityCow.class));
//        ArrayList<ModelRenderer> boxes = new ArrayList<ModelRenderer>() {{
//            add(((ModelQuadruped)render.mainModel).body);
//            add(((ModelQuadruped)render.mainModel).head);
//            add(((ModelQuadruped)render.mainModel).leg1);
//            add(((ModelQuadruped)render.mainModel).leg2);
//            add(((ModelQuadruped)render.mainModel).leg3);
//            add(((ModelQuadruped)render.mainModel).leg4);
//        }};
//        for(int i = 0; i < boxes.size(); i++)
//        {
//            CubeInfo info = new CubeInfo("fake" + i);
//            info.modelCube = boxes.get(i);
//            model.cubes.add(info);
//        }
    }

    public void destroy()
    {
        if(model != null)
        {
            for(int i = model.cubes.size() - 1; i >= 0; i--)
            {
                model.removeCubeInfo(model.cubes.get(i));
            }
        }
    }

    public void createNewCube()
    {
        cubes.add(new CubeInfo("shape" + Integer.toString(cubes.size() + 1)));
    }

    public void cloneFrom(ProjectInfo info)
    {
        //TODO link the textures together.
        this.cameraZoom = info.cameraZoom;
        this.cameraYaw = info.cameraYaw;
        this.cameraPitch = info.cameraPitch;
        this.cameraOffsetX = info.cameraOffsetX;
        this.cameraOffsetY = info.cameraOffsetY;
    }

    //TODO texture size?
}
