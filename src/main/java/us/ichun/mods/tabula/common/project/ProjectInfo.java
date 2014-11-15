package us.ichun.mods.tabula.common.project;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.File;
import java.util.ArrayList;

public class ProjectInfo
{
    public transient String identifier;
    public transient File saveFile;

    public String modelName;
    public String authorName;
    public String projVersion;

    public ProjectInfo(String name, String author)
    {
        modelName = name;
        authorName = author;
    }

    public String getAsJson()
    {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
//        Gson gson = new Gson();

        return gson.toJson(this);
    }

    //TODO texture size?
}
