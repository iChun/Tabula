package us.ichun.mods.tabula.client.mainframe;

import net.minecraft.client.Minecraft;
import us.ichun.mods.tabula.client.mainframe.core.ProjectHelper;
import us.ichun.mods.tabula.common.project.ProjectInfo;
import us.ichun.mods.tabula.common.Tabula;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;

//This is the class that holds all the info of the workspace and handles UI input from everyone.
public class Mainframe
{

    public ArrayList<UUID> listeners = new ArrayList<UUID>();
    public ArrayList<UUID> editors = new ArrayList<UUID>();

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

        //TODO add a random identifier
        projectInfo.identifier = Long.toString(Minecraft.getSystemTime());

        projects.add(projectInfo);

        streamProject(projectInfo.identifier, projectInfo.getAsJson());

        //TODO inform listeners of new project.
    }

    public void loadProject(File file)
    {
        //TODO load .tbl files?
    }

    public void streamProject(String ident, String s)
    {
        allowEditing = false;
        for(UUID id : listeners)
        {
            //TODO stream to other listeners
            if(id.toString().replaceAll("-", "").equals(Minecraft.getMinecraft().getSession().getPlayerID().replaceAll("-", "")))
            {
                System.out.println("the hoster.");
                System.out.println(s);
                ProjectHelper.addProjectToManager(ProjectHelper.createProjectFromJson(ident, s));
            }
        }
        allowEditing = true;
    }

    public void createNewCube(String ident)
    {
        for(ProjectInfo info : projects)
        {
            if(info.identifier.equals(ident))
            {
                info.createNewCube();
                streamProject(info.identifier, info.getAsJson());
            }
        }
    }

    public void addListener(UUID id, boolean isEditor)
    {
        if(!listeners.contains(id))
        {
            listeners.add(id);
        }
        if(isEditor && !editors.contains(id))
        {
            editors.add(id);
        }
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
