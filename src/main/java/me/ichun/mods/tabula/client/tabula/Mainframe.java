package me.ichun.mods.tabula.client.tabula;

import me.ichun.mods.ichunutil.client.gui.bns.window.Window;
import me.ichun.mods.ichunutil.client.gui.bns.window.constraint.Constraint;
import me.ichun.mods.ichunutil.client.gui.bns.window.view.element.ElementList;
import me.ichun.mods.ichunutil.common.module.tabula.project.Identifiable;
import me.ichun.mods.ichunutil.common.module.tabula.project.Project;
import me.ichun.mods.ichunutil.common.util.IOUtil;
import me.ichun.mods.tabula.client.core.ResourceHelper;
import me.ichun.mods.tabula.client.gui.IProjectInfo;
import me.ichun.mods.tabula.client.gui.WorkspaceTabula;
import me.ichun.mods.tabula.client.gui.window.WindowModelTree;
import me.ichun.mods.tabula.client.gui.window.WindowTexture;
import me.ichun.mods.tabula.common.Tabula;
import me.ichun.mods.tabula.common.packet.PacketKillSession;
import net.minecraft.util.Util;
import net.minecraft.util.math.BlockPos;

import javax.annotation.Nonnull;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

public class Mainframe
{
    public ArrayList<ProjectInfo> projects = new ArrayList<>();

    //only on the master.
    public HashSet<String> listeners = new HashSet<>();
    public HashSet<String> editors = new HashSet<>();

    private boolean isMaster;
    private boolean canEdit;
    public String master; //who is the master
    public boolean sessionEnded;
    public BlockPos origin; //only set if it's multiplayer
    public int lastPing;

    public Camera defaultCam = new Camera();
    public int activeView = -1;

    private WorkspaceTabula workspace;

    public Mainframe(String master)
    {
        this.isMaster = false;
        this.canEdit = false;
        this.master = master;
    }

    public Mainframe setMaster()
    {
        isMaster = true;
        canEdit = true;
        return this;
    }

    public Mainframe setCanEdit()
    {
        canEdit = true;
        return this;
    }

    public Mainframe setOrigin(BlockPos pos)
    {
        this.origin = pos;
        return this;
    }

    public boolean getIsMaster()
    {
        return isMaster;
    }

    public boolean getCanEdit()
    {
        return canEdit;
    }

    public void setWorkspace(WorkspaceTabula workspace)
    {
        this.workspace = workspace;
    }

    public void tick()
    {
        for(ProjectInfo project : projects)
        {
            project.tick();
        }
        defaultCam.tick();

        lastPing++;
        //TODO if lastPing > 600 (30 secs)
    }

    public void shutdown()
    {
        if(origin != null && !sessionEnded) //if we're on MP
        {
            Tabula.channel.sendToServer(new PacketKillSession(origin));
        }
    }

    //INPUT FROM CLIENT
    public void openProject(Project project) //when opened using the UI
    {
        //add the project
        ProjectInfo info = new ProjectInfo(this, project);
        projects.add(info);

        //switch to view the active project
        activeView = projects.size() - 1;

        //this is the first project you've opened.
        if(projects.size() == 1 && workspace.getWindowType(WindowTexture.class) == null) // first project
        {
            Window<?> window = new WindowTexture(workspace);
            workspace.addToDock(window, Constraint.Property.Type.RIGHT);
            workspace.addToDocked(window, new WindowModelTree(workspace));
        }

        //Notify!
        workspace.setCurrentProject(info);
        workspace.projectChanged(IProjectInfo.ChangeType.PROJECTS);
        workspace.projectChanged(IProjectInfo.ChangeType.PROJECT);
    }

    public void editProject(Project project) //edited in the UI
    {
        ProjectInfo info = getProjectInfoForProject(project);
        if(info != null)
        {
            info.markProjectDirty();
            workspace.projectChanged(IProjectInfo.ChangeType.PROJECT);
        }
    }

    public void importProject(@Nonnull Project project, boolean texture)
    {
        ProjectInfo info = getActiveProject();
        if(info != null)
        {
            info.project.importProject(project, texture);
            if(texture)
            {
                info.textureFile = null;
                info.textureFileMd5 = null;
                workspace.projectChanged(IProjectInfo.ChangeType.TEXTURE);
            }
            info.markProjectDirty();
            workspace.projectChanged(IProjectInfo.ChangeType.PARTS);
        }
    }

    public void closeProject(ProjectInfo info)
    {
        boolean currentProject = info == getActiveProject();
        if(currentProject)
        {
            activeView--;
            if(activeView < 0 && !projects.isEmpty())
            {
                activeView = 0;
            }
            info.project.destroy();
            projects.remove(info);
        }
        workspace.setCurrentProject(getActiveProject());
        workspace.projectChanged(IProjectInfo.ChangeType.PROJECTS);

        if(currentProject)
        {
            workspace.projectChanged(IProjectInfo.ChangeType.PROJECT);
        }
    }

    public void setActiveProject(ProjectInfo info)
    {
        for(int i = 0; i < projects.size(); i++)
        {
            if(projects.get(i) == info)
            {
                activeView = i;
                workspace.setCurrentProject(info);
                workspace.projectChanged(IProjectInfo.ChangeType.PROJECT);
            }
        }
    }

    public void addPart(ProjectInfo info, Identifiable<?> parent, Project.Part part)
    {
        if(info != null)
        {
            info.project.addPart(parent, part);
            info.markProjectDirty();
            workspace.projectChanged(IProjectInfo.ChangeType.PARTS);
        }
    }

    public void addBox(ProjectInfo info, Identifiable<?> parent, Project.Part.Box box)
    {
        if(info != null)
        {
            info.project.addBox(parent, box);
            info.markProjectDirty();
            workspace.projectChanged(IProjectInfo.ChangeType.PARTS);
        }
    }

    public void delete(ProjectInfo info, Identifiable<?> child) //parent should not be null
    {
        if(info != null)
        {
            info.project.delete(child);
            info.markProjectDirty();
            workspace.projectChanged(IProjectInfo.ChangeType.PARTS);
        }
    }

    public void updatePart(Project.Part part)
    {
        ProjectInfo info = getProjectInfoForProject(part.markDirty());
        if(info != null)
        {
            info.markProjectDirty();
            workspace.projectChanged(IProjectInfo.ChangeType.PARTS);
        }
    }

    public void updateBox(Project.Part.Box box)
    {
        ProjectInfo info = getProjectInfoForProject(box.markDirty());
        if(info != null)
        {
            info.markProjectDirty();
            workspace.projectChanged(IProjectInfo.ChangeType.PARTS);
        }
    }

    public void setImage(ProjectInfo info, BufferedImage image)
    {
        if(info != null)
        {
            info.project.setBufferedTexture(image);
            info.markProjectDirty();
            workspace.projectChanged(IProjectInfo.ChangeType.TEXTURE);
        }
    }

    public void handleDragged(Identifiable<?> object, Identifiable<?> object1)
    {
        Project.Part draggedOnto = null;
        if(object1 instanceof Project.Part) // the item we dragged onto
        {
            draggedOnto = ((Project.Part)object1);
        }
        else if(object1 instanceof Project.Part.Box)
        {
            draggedOnto = ((Project.Part)((Project.Part.Box)object1).parent);
        }

        if(draggedOnto != null)
        {
            if(object instanceof Project.Part)
            {
                Project.Part part = (Project.Part)object;
                part.parent.disown(part);
                draggedOnto.adopt(part);
            }
            else if(object instanceof Project.Part.Box)
            {
                Project.Part.Box box = (Project.Part.Box)object;
                box.parent.disown(box);
                draggedOnto.adopt(box);
            }
            Project project = draggedOnto.getProject();
            if(getActiveProject() != null && project == getActiveProject().project)
            {
                getActiveProject().markProjectDirty();
                workspace.projectChanged(IProjectInfo.ChangeType.PARTS);
            }
        }
    }

    public void handleRearrange(List<ElementList.Item<?>> items, Identifiable<?> child, int oldIndex)
    {
        Project project = child.getProject();
        if(getActiveProject() != null && project == getActiveProject().project)
        {
            Identifiable<?> lastItem = null;
            for(int i = 0; i < items.size(); i++)
            {
                ElementList.Item<?> item = items.get(i);
                if(item.getObject() == child)
                {
                    //we found it!
                    if(i == items.size() - 1)//we're the last object.
                    {
                        lastItem = null;
                    }
                    break;
                }
                else
                {
                    lastItem = (Identifiable<?>)item.getObject();
                }
            }

            if(lastItem == null) // attach to the project
            {
                if(child instanceof Project.Part.Box)
                {
                    child.parent.parent.disown(child.parent);

                    if(items.get(0).getObject() == child) // first
                    {
                        project.parts.add(0, (Project.Part)child.parent);
                        child.parent.parent = project;
                    }
                    else
                    {
                        project.adopt(child.parent);
                    }
                }
                else
                {
                    child.parent.disown(child);

                    if(items.get(0).getObject() == child) // first
                    {
                        project.parts.add(0, (Project.Part)child);
                        child.parent = project;
                    }
                    else
                    {
                        project.adopt(child);
                    }
                }
            }
            else
            {
                project.rearrange(lastItem, child);
            }

            ProjectInfo info = getProjectInfoForProject(project);
            if(info != null)
            {
                info.markProjectDirty();
                workspace.projectChanged(IProjectInfo.ChangeType.PARTS);
            }
        }
    }

    public void changeState(ProjectInfo info, boolean redo)
    {
        if(redo)
        {
            info.redo();
        }
        else
        {
            info.undo();
        }
        workspace.projectChanged(IProjectInfo.ChangeType.PROJECT);
        workspace.projectChanged(IProjectInfo.ChangeType.PARTS);
        workspace.projectChanged(IProjectInfo.ChangeType.TEXTURE);
    }
    //END INPUT STUFF

    //CONNECTION STUFF
    //HOST STUFF
    public void listenerChange(String listener, boolean add)
    {
        //TODO post to chat
        if(add)
        {
            listeners.add(listener);
        }
        else
        {
            listeners.remove(listener);
        }
    }

    public void sendChat(String s)
    {
        //TODO this;
    }

    //END CONNECTION STUFF


    //LOCAL
    public Camera getCamera()
    {
        ProjectInfo info = getActiveProject();
        return info != null ? info.camera : defaultCam;
    }

    public ProjectInfo getActiveProject()
    {
        if(activeView >= 0 && activeView < projects.size())
        {
            return projects.get(activeView);
        }
        return null;
    }

    public ProjectInfo getProjectWithIdentifier(String ident)
    {
        for(ProjectInfo info : projects)
        {
            Identifiable<?> id = info.project.getById(ident);
            if(id != null)
            {
                return info;
            }
        }
        return null;
    }

    public ProjectInfo getProjectInfoForProject(Project project)
    {
        for(ProjectInfo info : projects)
        {
            if(info.project == project)
            {
                return info;
            }
        }
        return null;
    }

    public static class ProjectInfo
    {
        @Nonnull
        private final Mainframe mainframe;
        @Nonnull
        public Project project;
        @Nonnull
        public final Camera camera;

        private Project.Part selectedPart;
        private Project.Part.Box selectedBox;

        public File textureFile;
        public String textureFileMd5;
        public boolean hideTexture;

        public Project ghostProject;
        public float ghostOpacity;

        public int stateCooldown;
        public ArrayList<State> states = new ArrayList<>();
        public int stateIndex = -1;

        public int autosaveTimer;

        public ProjectInfo(@Nonnull Mainframe mainframe, Project project)
        {
            this.mainframe = mainframe;
            this.project = project;
            this.camera = new Camera();

            if(project.textureFile != null && project.textureFileMd5 != null)
            {
                File file = new File(ResourceHelper.getTexturesDir().toFile(), project.textureFile);
                if(file.exists())
                {
                    String md5 = IOUtil.getMD5Checksum(file);
                    if(md5 != null && md5.equals(project.textureFileMd5)) //oh hey we found our save file.
                    {
                        textureFile = file;
                        textureFileMd5 = md5;

                        BufferedImage image = null;
                        try
                        {
                            image = ImageIO.read(textureFile);
                        }
                        catch(IOException ignored){}

                        if(image != null)
                        {
                            //don't call setImage from the mainframe. We don't want to mark dirty.
                            project.setBufferedTexture(image);
                            mainframe.workspace.projectChanged(IProjectInfo.ChangeType.TEXTURE);
                        }
                    }
                }
            }
        }

        public void tick()
        {
            camera.tick();
            if(stateCooldown > 0)
            {
                stateCooldown--;
                if(stateCooldown == 0)
                {
                    if(states.size() > stateIndex + 1)
                    {
                        states.subList(stateIndex + 1, states.size()).clear();
                    }

                    states.add(new State(Project.SIMPLE_GSON.toJson(project), project.getBufferedTexture()));
                    while(states.size() > Tabula.configClient.maximumUndoStates)
                    {
                        states.remove(0);
                    }
                    stateIndex = states.size() - 1; //put state at max
                }
            }
            if(!Tabula.configClient.disableAutosaves)
            {
                autosaveTimer++;
                if(autosaveTimer > 5 * 60 * 20) //5 minutes
                {
                    autosaveTimer = 0;

                    if(stateIndex >= 0 && stateIndex < states.size())
                    {
                        State state = states.get(stateIndex);
                        if(!state.autosaved)
                        {
                            state.autosaved = true;

                            File file = new File(ResourceHelper.getAutosaveDir().toFile(), project.name + "-Autosave-" + Util.millisecondsSinceEpoch() + ".tbl");

                            File projFile = project.saveFile;
                            boolean projDirt = project.isDirty;
                            if(project.save(file))
                            {
                                project.saveFile = projFile;
                                project.isDirty = projDirt;

                                //get the last few files in this name, delete them off.
                                long timestamp = 0;
                                File oldestAutosave = null;
                                int count = 0;
                                File[] files = ResourceHelper.getAutosaveDir().toFile().listFiles();
                                for(File save : files)
                                {
                                    if(!save.isDirectory() && save.getName().endsWith(".tbl") && save.getName().startsWith(project.name + "-Autosave-"))
                                    {
                                        count++;
                                        String stamp = save.getName().substring((project.name + "-Autosave-").length(), save.getName().length() - 4);//remove the ".tbl"
                                        try
                                        {
                                            long time = Long.parseLong(stamp);
                                            if(time < timestamp || timestamp == 0)
                                            {
                                                timestamp = time;
                                                oldestAutosave = save;
                                            }
                                        }
                                        catch(NumberFormatException e)
                                        {
                                        }
                                    }
                                }
                                if(oldestAutosave != null && count > 10)
                                {
                                    oldestAutosave.delete();
                                }
                            }
                        }
                    }
                }
            }
        }

        public void markProjectDirty()
        {
            project.markDirty();
            //SAVE STATE
            if(stateCooldown <= 0)
            {
                stateCooldown = 40; //2 seconds?

                if(states.size() > stateIndex + 1)
                {
                    states.subList(stateIndex + 1, states.size()).clear();
                }
            }
        }

        public void createState()
        {
            State state = new State(Project.SIMPLE_GSON.toJson(project), project.getBufferedTexture());
            if(states.isEmpty() || !states.get(states.size() - 1).equals(state))
            {
                states.add(state);
                while(states.size() > Tabula.configClient.maximumUndoStates)
                {
                    states.remove(0);
                }
                stateIndex = states.size() - 1; //put state at max
            }
        }

        public void undo()
        {
            if(stateIndex > 0)
            {
                stateIndex--;
                setProjectToState(stateIndex);
            }
        }

        public void redo()
        {
            if(stateIndex < states.size() - 1)
            {
                stateIndex++;
                setProjectToState(stateIndex);
            }
        }

        private void setProjectToState(int index)
        {
            State state = states.get(index);
            Project project = Project.SIMPLE_GSON.fromJson(state.project, Project.class);
            if(project != null)
            {
                state.autosaved = false;

                this.project.transferTransients(project);
                project.setBufferedTexture(state.image);

                //DO NOT CALL DESTROY.
                this.project = project;
                this.project.adoptChildren();

                selectPart(null); // this selects a null box for us anyway
            }
        }

        public void addPart(Identifiable<?> parent, Project.Part part)
        {
            mainframe.addPart(this, parent, part);
        }

        public void addBox(Identifiable<?> parent, Project.Part.Box box)
        {
            mainframe.addBox(this, parent, box);
        }

        public void delete(Identifiable<?> child)
        {
            mainframe.delete(this, child);
        }

        public Project.Part getSelectedPart()
        {
            return selectedPart;
        }

        public Project.Part.Box getSelectedBox()
        {
            return selectedBox;
        }

        public void selectPart(Project.Part part)
        {
            if(part == null) //deselect the box first
            {
                selectBox(null);
            }

            selectedPart = part;

            mainframe.workspace.projectChanged(IProjectInfo.ChangeType.PARTS);
        }

        public void selectBox(Project.Part.Box box)
        {
            selectedBox = box;

            mainframe.workspace.projectChanged(IProjectInfo.ChangeType.PARTS);
        }

        public void setGhostProject(Project project, float ghostOpacity)
        {
            if(ghostProject != null && ghostProject != project)
            {
                ghostProject.destroy();
            }
            this.ghostProject = project;
            this.ghostOpacity = ghostOpacity;
        }

        public static class State
        {
            public final String project;
            public final BufferedImage image;
            public boolean autosaved;

            public State(String project, BufferedImage image)
            {
                this.project = project;
                this.image = image;
            }

            @Override
            public boolean equals(Object obj)
            {
                return obj instanceof State && ((State)obj).project.equals(project) && ((State)obj).image == image;
            }
        }
    }

    public static class Camera
    {
        public float fov = 30F;
        public float zoom = 1F;
        public float x = 0F;
        public float y = 0F;
        public float yaw = 0F;
        public float pitch = 0F;

        public float rendFov = fov, rendZoom = zoom, rendX = x, rendY = y, rendYaw = yaw, rendPitch = pitch;
        public float rendFovPrev = rendFov, rendZoomPrev = rendZoom, rendXPrev = rendX, rendYPrev = rendY, rendYawPrev = rendYaw, rendPitchPrev = rendPitch;


        public void tick()
        {
            rendFovPrev = rendFov;
            rendZoomPrev = rendZoom;
            rendXPrev = rendX;
            rendYPrev = rendY;
            rendYawPrev = rendYaw;
            rendPitchPrev = rendPitch;

            float mag = 0.4F;
            rendFov += (fov - rendFov) * mag;
            rendZoom += (zoom - rendZoom) * mag;
            rendX += (x - rendX) * mag;
            rendY += (y - rendY) * mag;
            rendYaw += (yaw - rendYaw) * mag;
            rendPitch += (pitch - rendPitch) * mag;
        }

        public void correct()
        {
            if(zoom < 0.05F)
            {
                zoom = 0.05F;
            }
            else if(zoom > 15F)
            {
                zoom = 15F;
            }
            if(fov < 15F)
            {
                fov = 15F;
            }
            else if(fov > 160F)
            {
                fov = 160F;
            }
        }
    }

}
