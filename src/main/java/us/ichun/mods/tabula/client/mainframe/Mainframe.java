package us.ichun.mods.tabula.client.mainframe;

import com.google.gson.Gson;
import net.minecraft.client.Minecraft;
import org.apache.commons.lang3.RandomStringUtils;
import us.ichun.mods.tabula.client.mainframe.core.ProjectHelper;
import us.ichun.mods.tabula.common.Tabula;
import us.ichun.module.tabula.client.model.ModelInfo;
import us.ichun.module.tabula.common.project.ProjectInfo;
import us.ichun.module.tabula.common.project.components.CubeGroup;
import us.ichun.module.tabula.common.project.components.CubeInfo;

import java.awt.image.BufferedImage;
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

    public int age;

    public void tick()
    {
        age++;
        for(ProjectInfo proj : projects)
        {
            if(age - proj.lastState > 40)//2 second idle before saving a state.
            {
                String state = proj.getAsJson();
                if(proj.states.isEmpty() || !proj.states.get(proj.states.size() - 1).equals(state) && !proj.states.contains(state))
                {
                    if(proj.switchState != -1 && proj.switchState < proj.states.size() - 1)
                    {
                        while(proj.states.size() > proj.switchState + 1)
                        {
                            proj.states.remove(proj.states.size() - 1);
                        }
                    }
                    proj.states.add(state);
                    while(proj.states.size() > 200)
                    {
                        proj.states.remove(0);//max 200 states
                    }
                    proj.switchState = -1;
                }
                proj.lastState = age;//state has been checked and updated;
            }
        }
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
            if(id.toString().replaceAll("-", "").equals("deadbeefdeadbeefdeadbeefdeadbeef") || id.toString().replaceAll("-", "").equals(Minecraft.getMinecraft().getSession().getPlayerID().replaceAll("-", "")))
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
            if(id.toString().replaceAll("-", "").equals("deadbeefdeadbeefdeadbeefdeadbeef") || id.toString().replaceAll("-", "").equals(Minecraft.getMinecraft().getSession().getPlayerID().replaceAll("-", "")))
            {
                ProjectHelper.removeProjectFromManager(ident);
            }
        }
    }

    public void streamProject(ProjectInfo project)
    {
        project.lastState = age;//Update lastState because of an action.
        allowEditing = false;
        for(UUID id : listeners)
        {
            //TODO stream to other listeners
            if(id.toString().replaceAll("-", "").equals("deadbeefdeadbeefdeadbeefdeadbeef") || id.toString().replaceAll("-", "").equals(Minecraft.getMinecraft().getSession().getPlayerID().replaceAll("-", "")))
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
            if(id.toString().replaceAll("-", "").equals("deadbeefdeadbeefdeadbeefdeadbeef") || id.toString().replaceAll("-", "").equals(Minecraft.getMinecraft().getSession().getPlayerID().replaceAll("-", "")))
            {
                ProjectHelper.updateProjectTexture(ident, bufferedImage);
            }
        }
        allowEditing = true;
    }

    public void switchState(String projIdent, boolean undo)//undo/redo
    {
        for(int k = 0; k < projects.size(); k++)
        {
            ProjectInfo proj = projects.get(k);
            if(proj.identifier.equals(projIdent))
            {
                String state = proj.getAsJson();
                for(int i = 0; i < proj.states.size(); i++)
                {
                    String storedState = proj.states.get(i);
                    if(storedState.equals(state))
                    {
                        if(undo && i == 0 || !undo && i == proj.states.size() - 1)//you can't undo when you're the first state or redo when you're the final state
                        {
                            return;
                        }
                        String wantedState = proj.states.get(undo ? i - 1 : i + 1);
                        ProjectInfo newProj = ((new Gson()).fromJson(wantedState, ProjectInfo.class));
                        newProj.inherit(proj);
                        projects.remove(k);
                        projects.add(k, newProj);

                        newProj.switchState = i;

                        streamProject(newProj);

                        break;
                    }
                }
            }
        }
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
            removeFromCube(newParent, dragged, cube);
        }
        for(CubeGroup group : info.cubeGroups)
        {
            removeFromGroup(newParent, dragged, group);
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
            removeFromCube(newParent, dragged, group1);
        }

        if(cube.getChildren().contains(dragged) && cube != newParent)
        {
            cube.removeChild((CubeInfo)dragged);
        }
    }

    public void removeFromGroup(Object newParent, Object dragged, CubeGroup group)
    {
        for(CubeInfo cube : group.cubes)
        {
            removeFromCube(newParent, dragged, cube);
        }
        for(CubeGroup group1 : group.cubeGroups)
        {
            removeFromGroup(newParent, dragged, group1);
        }
        if(group.cubes.contains(dragged) && group != newParent)
        {
            group.cubes.remove(dragged);
        }
        if(group.cubeGroups.contains(dragged) && group != newParent)
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

    public void reidentifyChildren(ArrayList<CubeInfo> children, ProjectInfo info)
    {
        for(CubeInfo cube : children)
        {
            cube.identifier = RandomStringUtils.randomAscii(IDENTIFIER_LENGTH);
            if(info != null)
            {
                info.cubeCount++;
            }
            reidentifyChildren(cube.getChildren(), info);
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

                reidentifyChildren(cube.getChildren(), info);

                streamProject(info);
            }
        }
    }

    public void copyGroupTo(String projIdent, String groupIdent, boolean inPlace)
    {
        CubeGroup group = null;
        ProjectInfo project = null;
        for(ProjectInfo info : projects)
        {
            if(info.identifier.equals(projIdent))
            {
                project = info;
            }
            if(group == null)
            {
                group = (CubeGroup)info.getObjectByIdent(groupIdent);
            }
        }
        if(project != null && group != null)
        {
            //can't just add, groups and cubes have unique identifiers.
            CubeGroup group1 = new CubeGroup(group.name);
            project.cubeGroups.add(group1);
            cloneGroups(group, group1, inPlace);

            streamProject(project);
        }
    }

    public void cloneGroups(CubeGroup ori, CubeGroup clone, boolean inPlace)
    {
        clone.txMirror = ori.txMirror;
        clone.hidden = ori.hidden;
        for(int i = 0; i < ori.cubeGroups.size(); i++)
        {
            CubeGroup group = ori.cubeGroups.get(i);
            CubeGroup group1 = new CubeGroup(group.name);
            clone.cubeGroups.add(group1);
            cloneGroups(group, group1, inPlace);
        }
        for(int i = 0; i < ori.cubes.size(); i++)
        {
            CubeInfo cube = ori.cubes.get(i);
            CubeInfo cube1 = new CubeInfo(cube.name);
            clone.cubes.add(cube1);
            cloneCube(cube, cube1, inPlace);
        }
    }

    public void cloneCube(CubeInfo ori, CubeInfo clone, boolean inPlace)
    {
        for(int i = 0; i < 3; i++)
        {
            if(inPlace)
            {
                clone.position[i] = ori.position[i];
            }
            clone.dimensions[i] = ori.dimensions[i];
            clone.offset[i] = ori.offset[i];
            clone.scale[i] = ori.scale[i];
            clone.rotation[i] = ori.rotation[i];
        }
        clone.txOffset[0] = ori.txOffset[0];
        clone.txOffset[1] = ori.txOffset[1];
        clone.txMirror = ori.txMirror;
        clone.hidden = ori.hidden;
        clone.parentIdentifier = ori.parentIdentifier;

        for(int i = 0; i < ori.getChildren().size(); i++)
        {
            CubeInfo cube = ori.getChildren().get(i);
            CubeInfo cube1 = new CubeInfo(cube.name);
            clone.addChild(cube1);
            cloneCube(cube, cube1, inPlace);
        }
    }

    public void setGroupVisibility(String projIdent, String groupIdent, boolean hidden)
    {
        for(ProjectInfo info : projects)
        {
            if(info.identifier.equals(projIdent))
            {
                CubeGroup group = (CubeGroup)info.getObjectByIdent(groupIdent);

                if(group != null)
                {
                    group.hidden = hidden;
                }

                streamProject(info);
            }
        }
    }

    public void updateGroup(String projIdent, String groupIdent, String name, double[] pos, double[] offset, double[] scale, int[] txOffset, double[] rot, boolean mirror, double mcScale)
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
                        updateGroupPieces(info1, pos, offset, scale, txOffset, rot, mirror, mcScale);
                        break;
                    }
                }
                if(!found)
                {
                    updateGroupInCubeGroups(groupIdent, proj.cubeGroups, name, pos, offset, scale, txOffset, rot, mirror, mcScale);
                }

                streamProject(proj);
            }
        }
    }

    public void updateGroupInCubeGroups(String groupIdent, ArrayList<CubeGroup> groups, String name, double[] pos, double[] offset, double[] scale, int[] txOffset, double[] rot, boolean mirror, double mcScale)
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
                    updateGroupPieces(info1, pos, offset, scale, txOffset, rot, mirror, mcScale);
                    break;
                }
            }
            updateGroupInCubeGroups(groupIdent, proj.cubeGroups, name, pos, offset, scale, txOffset, rot, mirror, mcScale);
        }
    }

    public void updateGroupPieces(CubeGroup group, double[] pos, double[] offset, double[] scale, int[] txOffset, double[] rot, boolean mirror, double mcScale)
    {
        for(CubeGroup group1 : group.cubeGroups)
        {
            updateGroupPieces(group1, pos, offset, scale, txOffset, rot, mirror, mcScale);
        }
        for(CubeInfo cube : group.cubes)
        {
            for(int i = 0; i < 3; i++)
            {
                cube.position[i] += pos[i];
                cube.offset[i] += offset[i];
                cube.scale[i] *= scale[i];
                cube.rotation[i] += rot[i];
            }
            cube.txOffset[0] += txOffset[0];
            cube.txOffset[1] += txOffset[1];
            cube.txMirror = mirror;
            cube.mcScale += mcScale;
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

    public void deleteObject(String ident, String cubeIdent)
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
                    deleteObjectInCubeGroups(cubeIdent, proj.cubeGroups);

                    CubeInfo cube = (CubeInfo)proj.getObjectByIdent(cubeIdent);
                    if(cube != null && cube.parentIdentifier != null)
                    {
                        CubeInfo info1 = (CubeInfo)proj.getObjectByIdent(cube.parentIdentifier);
                        if(info1 != null) //null check it just in case something strange happens again.
                        {
                            info1.removeChild(cube);
                        }
                    }
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

    public void deleteObjectInCubeGroups(String ident, ArrayList<CubeGroup> groups)
    {
        for(int j = groups.size() - 1; j >= 0; j--)
        {
            CubeGroup proj = groups.get(j);
            if(proj.identifier.equals(ident))
            {
                groups.remove(j);
                break;
            }
            for(int i = 0; i < proj.cubes.size(); i++)
            {
                CubeInfo info1 = proj.cubes.get(i);
                if(info1.identifier.equals(ident))
                {
                    proj.cubes.remove(i);
                    break;
                }
            }
            deleteObjectInCubeGroups(ident, proj.cubeGroups);
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
