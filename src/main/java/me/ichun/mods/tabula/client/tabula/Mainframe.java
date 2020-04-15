package me.ichun.mods.tabula.client.tabula;

import me.ichun.mods.ichunutil.client.gui.bns.window.Window;
import me.ichun.mods.ichunutil.client.gui.bns.window.constraint.Constraint;
import me.ichun.mods.ichunutil.common.module.tabula.project.Identifiable;
import me.ichun.mods.ichunutil.common.module.tabula.project.Project;
import me.ichun.mods.tabula.client.gui.IProjectInfo;
import me.ichun.mods.tabula.client.gui.WorkspaceTabula;
import me.ichun.mods.tabula.client.gui.window.WindowModelTree;
import me.ichun.mods.tabula.client.gui.window.WindowTexture;

import javax.annotation.Nonnull;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;

public class Mainframe
{
    public ArrayList<ProjectInfo> projects = new ArrayList<>();

    //only on the master.
    public ArrayList<String> listeners = new ArrayList<>();
    public ArrayList<String> editors = new ArrayList<>();

    public boolean isMaster;
    public boolean canEdit;
    public String master; //who is the master
    public boolean sessionEnded;

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
    }

    //CONNECTION STUFF
    //INPUT FROM CLIENT
    public void openProject(Project project) //when opened using the UI
    {
        //TODO what to do when you're not the master??
        //TODO send project to listeners

        //add the project
        ProjectInfo info = new ProjectInfo(this, project);
        projects.add(info);

        //switch to view the active project
        activeView = projects.size() - 1;

        //this is the first project you've opened.
        if(projects.size() == 1) // first project
        {
            Window<?> window = new WindowTexture(workspace);
            workspace.addToDock(window, Constraint.Property.Type.RIGHT);
            workspace.addToDocked(window, new WindowModelTree(workspace));
        }

        //Notify!
        workspace.projectChanged(IProjectInfo.ChangeType.PROJECTS);
        workspace.setCurrentProject(info);
    }

    public void editProject(Project project) //edited in the UI
    {
        //TODO streamline changes
    }

    public void addPart(ProjectInfo info, Identifiable<?> parent)
    {
        if(info != null)
        {
            info.project.addPart(parent); //TODO this receives an object
            workspace.projectChanged(IProjectInfo.ChangeType.PARTS);
        }
    }

    public void addBox(ProjectInfo info, Identifiable<?> parent)
    {
        if(info != null)
        {
            info.project.addBox(parent); //TODO this receives an object
            workspace.projectChanged(IProjectInfo.ChangeType.PARTS); //TODO change box?
        }
    }

    public void delete(ProjectInfo info, Identifiable<?> child) //parent should not be null
    {
        if(info != null)
        {
            info.project.delete(child); //TODO this receives an object
            workspace.projectChanged(IProjectInfo.ChangeType.PARTS); //TODO change box?
        }
    }

    public void updatePart(Project.Part part)
    {
        //TODO find the project this is from
        //TODO regenerate the model
        //TODO should we be refreshing everything every time???
        part.markDirty();
        workspace.projectChanged(IProjectInfo.ChangeType.PARTS); //TODO change box?
    }

    public void updateBox(Project.Part.Box box)
    {
        //TODO this
        //TODO regenerate the model
        //TODO should we be refreshing everything every time???
        box.markDirty();
        workspace.projectChanged(IProjectInfo.ChangeType.PARTS); //TODO change box?
    }

    //WHEN SHOULD WE SEND OUT SERVER STUFF?

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

    public static class ProjectInfo
    {
        @Nonnull
        private final Mainframe mainframe;
        @Nonnull
        public final Project project;
        @Nonnull
        public final Camera camera;

        public BufferedImage bufferedTexture;
        public int bufferedTextureId = -1;

        public ProjectInfo(@Nonnull Mainframe mainframe, Project project)
        {
            this.mainframe = mainframe;
            this.project = project;
            this.camera = new Camera();
        }

        public void tick()  //TODO autosaves?
        {
            camera.tick();
        }

        public void addPart(Identifiable<?> parent)
        {
            mainframe.addPart(this, parent);
        }

        public void addBox(Identifiable<?> parent)
        {
            mainframe.addBox(this, parent);
        }

        public void delete(Identifiable<?> child)
        {
            mainframe.delete(this, child);
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
