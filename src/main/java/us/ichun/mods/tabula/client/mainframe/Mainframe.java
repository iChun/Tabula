package us.ichun.mods.tabula.client.mainframe;

import com.google.gson.Gson;
import net.minecraft.client.Minecraft;
import org.apache.commons.lang3.RandomStringUtils;
import us.ichun.mods.tabula.client.mainframe.core.ProjectHelper;
import us.ichun.mods.tabula.client.model.ModelInfo;
import us.ichun.mods.tabula.common.project.ProjectInfo;
import us.ichun.mods.tabula.common.Tabula;
import us.ichun.mods.tabula.common.project.components.CubeGroup;
import us.ichun.mods.tabula.common.project.components.CubeInfo;

import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;

//This is the class that holds all the info of the workspace and handles UI input from everyone.
public class Mainframe
{

    public ArrayList<UUID> listeners = new ArrayList<UUID>();
    public ArrayList<UUID> editors = new ArrayList<UUID>();

    public final String projVersion = "1.0.0";

    public boolean allowEditing;

    public ArrayList<ProjectInfo> projects = new ArrayList<ProjectInfo>(); //each workspace tab should be a project.

    public void Mainframe()
    {
        allowEditing = true;
    }

    public void loadEmptyProject(String name, String author)
    {
        ProjectInfo projectInfo = new ProjectInfo(name, author);
        projectInfo.projVersion = projVersion; //TODO change this everytime loading changes.

        projectInfo.identifier = RandomStringUtils.randomAscii(20);

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
                ProjectHelper.addProjectToManager(ProjectHelper.createProjectFromJson(ident, s));
            }
        }
        allowEditing = true;
    }

    public void streamProjectTexture(String ident, BufferedImage bufferedImage)
    {
        allowEditing = false;
        for(UUID id : listeners)
        {
            //TODO stream to other listeners
            if(id.toString().replaceAll("-", "").equals(Minecraft.getMinecraft().getSession().getPlayerID().replaceAll("-", "")))
            {
                ProjectHelper.updateProjectTexture(ident, bufferedImage);
            }
        }
        allowEditing = true;
    }

    public void clearTexture(String ident)
    {
        for(ProjectInfo info : projects)
        {
            if(info.identifier.equals(ident))
            {
                if(info.bufferedTexture != null)
                {
                    info.bufferedTexture = null;
                    streamProjectTexture(info.identifier, info.bufferedTexture);
                }
            }
        }
    }

    public void importModel(String ident, ModelInfo model, boolean texture)
    {
        ProjectInfo projectInfo = null;
        if(ident.isEmpty())
        {
            projectInfo = new ProjectInfo(model.modelParent.getClass().getSimpleName(), "Either Mojang or a mod author");
            projectInfo.projVersion = projVersion; //TODO change this everytime loading changes.

            projectInfo.identifier = RandomStringUtils.randomAscii(20);

            projects.add(projectInfo);
        }
        else
        {
            for(ProjectInfo info : projects)
            {
                if(info.identifier.equals(ident))
                {
                    projectInfo = info;
                }
            }
        }

        if(projectInfo != null)
        {
            boolean streamTexture = projectInfo.importModel(model, texture);
            streamProject(projectInfo.identifier, projectInfo.getAsJson());
            if(streamTexture)
            {
                streamProjectTexture(projectInfo.identifier, projectInfo.bufferedTexture);
            }
        }
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

    public void updateCube(String ident, String cubeInfo)
    {
        for(ProjectInfo proj : projects)
        {
            if(proj.identifier.equals(ident))
            {
                CubeInfo info = ((new Gson()).fromJson(cubeInfo, CubeInfo.class));
                boolean found = false;
                for(int i = 0; i < proj.cubes.size(); i++)
                {
                    CubeInfo info1 = proj.cubes.get(i);
                    if(info1.identifier.equals(info.identifier))
                    {
                        found = true;
                        proj.cubes.remove(i);
                        proj.cubes.add(i, info);
                        break;
                    }
                }
                if(!found)
                {
                    replaceCubeInCubeGroups(info, proj.cubeGroups);
                }

                streamProject(proj.identifier, proj.getAsJson());
            }
        }
    }

    public void deleteCube(String ident, String cubeIdent)
    {
        for(ProjectInfo proj : projects)
        {
            if(proj.identifier.equals(ident))
            {
                boolean found = false;
                for(int i = 0; i < proj.cubes.size(); i++)
                {
                    CubeInfo info1 = proj.cubes.get(i);
                    if(info1.identifier.equals(cubeIdent))
                    {
                        found = true;
                        proj.cubes.remove(i);
                        break;
                    }
                }
                if(!found)
                {
                    deleteCubeInCubeGroups(cubeIdent, proj.cubeGroups);
                }

                streamProject(proj.identifier, proj.getAsJson());
            }
        }
    }

    public void replaceCubeInCubeGroups(CubeInfo cube, ArrayList<CubeGroup> groups)
    {
        for(int j = 0; j < groups.size(); j++)
        {
            CubeGroup proj = groups.get(j);
            for(int i = 0; i < proj.cubes.size(); i++)
            {
                CubeInfo info1 = proj.cubes.get(i);
                if(info1.identifier.equals(cube.identifier))
                {
                    proj.cubes.remove(i);
                    proj.cubes.add(i, cube);
                    break;
                }
            }
            replaceCubeInCubeGroups(cube, proj.cubeGroups);
        }
    }

    public void deleteCubeInCubeGroups(String ident, ArrayList<CubeGroup> groups)
    {
        for(int j = 0; j < groups.size(); j++)
        {
            CubeGroup proj = groups.get(j);
            for(int i = 0; i < proj.cubes.size(); i++)
            {
                CubeInfo info1 = proj.cubes.get(i);
                if(info1.identifier.equals(ident))
                {
                    proj.cubes.remove(i);
                    break;
                }
            }
            deleteCubeInCubeGroups(ident, proj.cubeGroups);
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
