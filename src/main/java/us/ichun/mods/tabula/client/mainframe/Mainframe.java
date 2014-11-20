package us.ichun.mods.tabula.client.mainframe;

import com.google.gson.Gson;
import net.minecraft.client.Minecraft;
import org.apache.commons.lang3.RandomStringUtils;
import us.ichun.mods.tabula.client.mainframe.core.ProjectHelper;
import us.ichun.module.tabula.client.model.ModelInfo;
import us.ichun.module.tabula.common.project.ProjectInfo;
import us.ichun.mods.tabula.Tabula;
import us.ichun.module.tabula.common.project.components.CubeGroup;
import us.ichun.module.tabula.common.project.components.CubeInfo;

import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.UUID;

//This is the class that holds all the info of the workspace and handles UI input from everyone.
//The player hosting this doesn't edit this directly, he has his own workspace and whatever he does to the workspace there changes things here, which are sent back to him.
public class Mainframe
{
    public static final int IDENTIFIER_LENGTH = ProjectInfo.IDENTIFIER_LENGTH;

    public ArrayList<UUID> listeners = new ArrayList<UUID>();
    public ArrayList<UUID> editors = new ArrayList<UUID>();

    public static final int projVersion = 1; //TODO change this everytime loading changes.

    public boolean allowEditing;

    public ArrayList<ProjectInfo> projects = new ArrayList<ProjectInfo>(); //each workspace tab should be a project.

    public void Mainframe()
    {
        allowEditing = true;
    }

    public void loadEmptyProject(String name, String author, int txWidth, int txHeight)
    {
        ProjectInfo projectInfo = new ProjectInfo(name, author);
        projectInfo.projVersion = projVersion;

        projectInfo.identifier = RandomStringUtils.randomAscii(IDENTIFIER_LENGTH);

        projectInfo.textureWidth = txWidth;
        projectInfo.textureHeight = txHeight;

        projects.add(projectInfo);

        streamProject(projectInfo);
    }

    public void editProject(String ident, String name, String author, int txWidth, int txHeight)
    {
        for(ProjectInfo info : projects)
        {
            if(info.identifier.equals(ident))
            {
                info.modelName = name;
                info.authorName = author;
                info.textureWidth = txWidth;
                info.textureHeight = txHeight;
                streamProject(info);
            }
        }
    }

    public void closeProject(String ident)
    {
        for(int i = projects.size() - 1; i >= 0; i--)
        {
            ProjectInfo info = projects.get(i);
            if(info.identifier.equals(ident))
            {
                streamProjectClosure(ident);
            }
        }
    }

    public void sendChat(String name, String message)
    {
        for(UUID id : listeners)
        {
            //TODO stream to other listeners
            if(id.toString().replaceAll("-", "").equals(Minecraft.getMinecraft().getSession().getPlayerID().replaceAll("-", "")))
            {
                ProjectHelper.receiveChat(name + ": " + message);
            }
        }
    }

    public void streamProjectClosure(String ident)
    {
        for(UUID id : listeners)
        {
            //TODO stream to other listeners
            if(id.toString().replaceAll("-", "").equals(Minecraft.getMinecraft().getSession().getPlayerID().replaceAll("-", "")))
            {
                ProjectHelper.removeProjectFromManager(ident);
            }
        }
    }

    public void streamProject(ProjectInfo project)
    {
        allowEditing = false;
        for(UUID id : listeners)
        {
            //TODO stream to other listeners
            if(id.toString().replaceAll("-", "").equals(Minecraft.getMinecraft().getSession().getPlayerID().replaceAll("-", "")))
            {
                ProjectHelper.addProjectToManager(ProjectHelper.createProjectFromJsonHost(project.identifier, project.getAsJson()));
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

    public void importProject(String ident, String projectString, BufferedImage image)
    {
        ProjectInfo project = ((new Gson()).fromJson(projectString, ProjectInfo.class));

        project.identifier = RandomStringUtils.randomAscii(IDENTIFIER_LENGTH);

        if(project.projVersion != projVersion)
        {
            repairProject(project);
        }

        projects.add(project);

        project.bufferedTexture = image;

        for(ProjectInfo info : projects)
        {
            if(info.identifier.equals(ident))
            {
                info.cubes.addAll(project.cubes);
                info.cubeGroups.addAll(info.cubeGroups);

                if(project.bufferedTexture != null)
                {
                    info.textureWidth = project.textureWidth;
                    info.textureHeight = project.textureHeight;
                    info.bufferedTexture = project.bufferedTexture;

                    streamProject(info);

                    streamProjectTexture(info.identifier, info.bufferedTexture);
                }
                else
                {
                    streamProject(info);
                }
            }
        }
    }

    public void openProject(String projectString, BufferedImage image)
    {
        ProjectInfo project = ((new Gson()).fromJson(projectString, ProjectInfo.class));

        project.identifier = RandomStringUtils.randomAscii(IDENTIFIER_LENGTH);

        if(project.projVersion != projVersion)
        {
            repairProject(project);
        }

        projects.add(project);

        project.bufferedTexture = image;

        streamProject(project);

        streamProjectTexture(project.identifier, project.bufferedTexture);
    }

    public void loadTexture(String ident, BufferedImage image, boolean updateDims)
    {
        for(ProjectInfo info : projects)
        {
            if(info.identifier.equals(ident))
            {
                boolean changed = false;
                info.bufferedTexture = image;
                if(info.bufferedTexture != null && !(info.textureWidth == info.bufferedTexture.getWidth() && info.textureHeight == info.bufferedTexture.getHeight()) && updateDims)
                {
                    changed = true;
                    info.textureWidth = info.bufferedTexture.getWidth();
                    info.textureHeight = info.bufferedTexture.getHeight();
                }
                if(changed)
                {
                    streamProject(info);
                }
                streamProjectTexture(info.identifier, info.bufferedTexture);
            }
        }
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

            projectInfo.identifier = RandomStringUtils.randomAscii(IDENTIFIER_LENGTH);

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
            streamProject(projectInfo);
            if(streamTexture)
            {
                streamProjectTexture(projectInfo.identifier, projectInfo.bufferedTexture);
            }
        }
    }

    public void dragOnto(String projIdent, String draggedOntoIdent, String draggedIdent)
    {
        for(ProjectInfo info : projects)
        {
            if(info.identifier.equals(projIdent))
            {
                Object draggedOnto = info.getObjectByIdent(draggedOntoIdent);
                Object dragged = info.getObjectByIdent(draggedIdent);

                //HANDLE.
                //Cube on Group
                if(dragged instanceof CubeInfo && draggedOnto instanceof CubeGroup && !((CubeGroup)draggedOnto).cubes.contains(dragged))
                {
                        ((CubeGroup)draggedOnto).cubes.add((CubeInfo)dragged);
                }
                //Group on Group
                else if(dragged instanceof CubeGroup && draggedOnto instanceof CubeGroup && !((CubeGroup)draggedOnto).cubeGroups.contains(dragged))
                {
                    ((CubeGroup)draggedOnto).cubeGroups.add((CubeGroup)dragged);
                }
                //Cube on Cube
                else if(dragged instanceof CubeInfo && draggedOnto instanceof CubeInfo && !((CubeInfo)draggedOnto).getChildren().contains(dragged))
                {
                    ((CubeInfo)draggedOnto).addChild((CubeInfo)dragged);
                }
                childProtectiveServices(info, draggedOnto, dragged);

                streamProject(info);
            }
        }
    }

    public void childProtectiveServices(ProjectInfo info, Object newParent, Object dragged)
    {
        if(info.cubes.contains(dragged))
        {
            info.cubes.remove(dragged);
        }
        if(info.cubeGroups.contains(dragged))
        {
            info.cubeGroups.remove(dragged);
        }
        for(CubeInfo cube : info.cubes)
        {
            if(cube != newParent)
            {
                removeFromCube(newParent, dragged, cube);
            }
        }
        for(CubeGroup group : info.cubeGroups)
        {
            if(group != newParent)
            {
                removeFromGroup(newParent, dragged, group);
            }
        }
        if(newParent == null)
        {
            if(dragged instanceof CubeInfo)
            {
                info.cubes.add((CubeInfo)dragged);
            }
            else if(dragged instanceof CubeGroup)
            {
                info.cubeGroups.add((CubeGroup)dragged);
            }
        }
    }

    public void removeFromCube(Object newParent, Object dragged, CubeInfo cube)
    {
        for(CubeInfo group1 : cube.getChildren())
        {
            if(group1 != newParent)
            {
                removeFromCube(newParent, dragged, group1);
            }
        }

        if(cube.getChildren().contains(dragged))
        {
            cube.removeChild((CubeInfo)dragged);
        }
    }

    public void removeFromGroup(Object newParent, Object dragged, CubeGroup group)
    {
        for(CubeInfo cube : group.cubes)
        {
            if(cube != newParent)
            {
                removeFromCube(newParent, dragged, cube);
            }
        }
        for(CubeGroup group1 : group.cubeGroups)
        {
            if(group1 != newParent)
            {
                removeFromGroup(newParent, dragged, group1);
            }
        }
        if(group.cubes.contains(dragged))
        {
            group.cubes.remove(dragged);
        }
        if(group.cubeGroups.contains(dragged))
        {
            group.cubeGroups.remove(dragged);
        }
    }

    public void createNewGroup(String ident)
    {
        for(ProjectInfo info : projects)
        {
            if(info.identifier.equals(ident))
            {
                info.createNewGroup();
                streamProject(info);
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
                streamProject(info);
            }
        }
    }

    public void createNewCube(String ident, String json, boolean inPlace)
    {
        for(ProjectInfo info : projects)
        {
            if(info.identifier.equals(ident))
            {
                CubeInfo cube = ((new Gson()).fromJson(json, CubeInfo.class));

                cube.identifier = RandomStringUtils.randomAscii(IDENTIFIER_LENGTH);

                if(!inPlace)
                {
                    cube.position = new double[3];
                    cube.offset = new double[3];
                    cube.rotation = new double[3];
                }

                info.cubeCount++;
                info.cubes.add(cube);

                streamProject(info);
            }
        }
    }

    public void updateGroup(String projIdent, String groupIdent, String name, double[] pos, double[] offset, double[] scale, int[] txOffset, double[] rot, boolean mirror)
    {
        for(ProjectInfo proj : projects)
        {
            if(proj.identifier.equals(projIdent))
            {
                boolean found = false;
                for(int i = 0; i < proj.cubeGroups.size(); i++)
                {
                    CubeGroup info1 = proj.cubeGroups.get(i);
                    if(info1.identifier.equals(groupIdent))
                    {
                        found = true;
                        info1.name = name;
                        updateGroupPieces(info1, pos, offset, scale, txOffset, rot, mirror);
                        break;
                    }
                }
                if(!found)
                {
                    updateGroupInCubeGroups(groupIdent, proj.cubeGroups, name, pos, offset, scale, txOffset, rot, mirror);
                }

                streamProject(proj);
            }
        }
    }

    public void updateGroupInCubeGroups(String groupIdent, ArrayList<CubeGroup> groups, String name, double[] pos, double[] offset, double[] scale, int[] txOffset, double[] rot, boolean mirror)
    {
        for(int j = 0; j < groups.size(); j++)
        {
            CubeGroup proj = groups.get(j);
            for(int i = 0; i < proj.cubeGroups.size(); i++)
            {
                CubeGroup info1 = proj.cubeGroups.get(i);
                if(info1.identifier.equals(groupIdent))
                {
                    info1.name = name;
                    updateGroupPieces(info1, pos, offset, scale, txOffset, rot, mirror);
                    break;
                }
            }
            updateGroupInCubeGroups(groupIdent, proj.cubeGroups, name, pos, offset, scale, txOffset, rot, mirror);
        }
    }

    public void updateGroupPieces(CubeGroup group, double[] pos, double[] offset, double[] scale, int[] txOffset, double[] rot, boolean mirror)
    {
        for(CubeGroup group1 : group.cubeGroups)
        {
            updateGroupPieces(group1, pos, offset, scale, txOffset, rot, mirror);
        }
        for(CubeInfo cube : group.cubes)
        {
            for(int i = 0; i < 3; i++)
            {
                cube.position[i] += pos[i];
                cube.offset[i] += offset[i];
                cube.scale[i] += scale[i];
                cube.rotation[i] += rot[i];
            }
            cube.txOffset[0] += txOffset[0];
            cube.txOffset[1] += txOffset[1];
            cube.txMirror = mirror;
        }
    }

    public void updateCube(String ident, String cubeInfo)
    {
        for(ProjectInfo proj : projects)
        {
            if(proj.identifier.equals(ident))
            {
                CubeInfo info = ((new Gson()).fromJson(cubeInfo, CubeInfo.class));

                if(info.parentIdentifier != null)
                {
                    CubeInfo info2 = (CubeInfo)proj.getObjectByIdent(info.parentIdentifier);
                    if(info2 != null)
                    {
                        for(int i = 0; i < info2.getChildren().size(); i++)
                        {
                            CubeInfo info1 = info2.getChildren().get(i);
                            if(info1.identifier.equals(info.identifier))
                            {
                                info2.getChildren().remove(i);
                                info2.getChildren().add(i, info);
                                break;
                            }
                        }
                    }
                }
                else
                {
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
                }

                streamProject(proj);
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

                streamProject(proj);
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

    //repairs projects which have been outdated or something. idk
    public void repairProject(ProjectInfo project)
    {
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
        //TODO get name from the UUID...? how?
    }

    //TODO do a "maybe the host has crashed" inform to the clients
    public void shutdown()
    {
        //TODO tell listeners that you're shutting down;
        if(Tabula.proxy.tickHandlerClient.mainframe == this)
        {
            Tabula.proxy.tickHandlerClient.mainframe = null;
        }
    }
}
