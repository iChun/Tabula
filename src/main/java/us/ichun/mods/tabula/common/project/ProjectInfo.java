package us.ichun.mods.tabula.common.project;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
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
