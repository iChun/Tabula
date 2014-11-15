package us.ichun.mods.tabula.client.mainframe;

import us.ichun.mods.tabula.common.project.ProjectInfo;
import us.ichun.mods.tabula.common.Tabula;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;

//This is the class that holds all the info of the workspace and handles UI input from everyone.
public class Mainframe
{
    public HashMap<UUID, String> listeners = new HashMap<UUID, String>();
    public HashMap<UUID, String> editors = new HashMap<UUID, String>();

    public boolean allowEditing;

    public ArrayList<ProjectInfo> projects = new ArrayList<ProjectInfo>(); //each workspace tab should be a project.

    public void Mainframe()
    {
        allowEditing = true;
    }

    public void loadEmptyProject(String name, String author)
    {
        ProjectInfo projectInfo = new ProjectInfo(name, author);
        projectInfo.projVersion = "1.0.0"; //TODO change this everytime loading changes.

        projects.add(projectInfo);
    }

    public void loadProject(File file)
    {
        //TODO load .tbl files?
    }

    public void shutdown()
    {
        //TODO tell listeners that you're shutting down;
        if(Tabula.proxy.tickHandlerClient.mainframe == this)
        {
            Tabula.proxy.tickHandlerClient.mainframe = null;
        }
    }
}
