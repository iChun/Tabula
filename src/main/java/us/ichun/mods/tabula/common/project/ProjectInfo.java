package us.ichun.mods.tabula.common.project;

import java.io.File;
import java.util.ArrayList;

public class ProjectInfo
{
    public String modelName;
    public String authorName;
    public String projVersion;
    public File saveFile;

    public ProjectInfo(String name, String author)
    {
        modelName = name;
        authorName = author;
    }

    //TODO texture size?
}
