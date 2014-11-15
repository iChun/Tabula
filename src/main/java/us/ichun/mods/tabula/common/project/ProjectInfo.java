package us.ichun.mods.tabula.common.project;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
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

    public String modelName;
    public String authorName;
    public String projVersion;

    public int textureWidth = 64;
    public int textureHeight = 32;

    public ArrayList<CubeInfo> cubes;
    public ArrayList<CubeGroup> cubeGroups;

    public ProjectInfo(String name, String author)
    {
        modelName = name;
        authorName = author;

        cubes = new ArrayList<CubeInfo>();
        cubeGroups = new ArrayList<CubeGroup>();
    }

    public String getAsJson()
    {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
//        Gson gson = new Gson();

        return gson.toJson(this);
    }

    public void createNewCube()
    {
        cubes.add(new CubeInfo("shape" + Integer.toString(cubes.size() + 1)));
    }

    //TODO texture size?
}
