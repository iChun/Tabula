package me.ichun.mods.tabula.client.tabula;

import me.ichun.mods.ichunutil.client.gui.bns.window.Window;
import me.ichun.mods.ichunutil.client.gui.bns.window.WindowPopup;
import me.ichun.mods.ichunutil.client.gui.bns.window.constraint.Constraint;
import me.ichun.mods.ichunutil.client.gui.bns.window.view.element.ElementList;
import me.ichun.mods.ichunutil.client.gui.bns.window.view.element.ElementTextWrapper;
import me.ichun.mods.ichunutil.common.module.tabula.project.Identifiable;
import me.ichun.mods.ichunutil.common.module.tabula.project.Project;
import me.ichun.mods.ichunutil.common.util.IOUtil;
import me.ichun.mods.tabula.client.core.ResourceHelper;
import me.ichun.mods.tabula.client.gui.IProjectInfo;
import me.ichun.mods.tabula.client.gui.WorkspaceTabula;
import me.ichun.mods.tabula.client.gui.window.WindowModelTree;
import me.ichun.mods.tabula.client.gui.window.WindowTexture;
import me.ichun.mods.tabula.common.Tabula;
import me.ichun.mods.tabula.common.packet.PacketChat;
import me.ichun.mods.tabula.common.packet.PacketEditorStatus;
import me.ichun.mods.tabula.common.packet.PacketKillSession;
import me.ichun.mods.tabula.common.packet.PacketProjectFragment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.SimpleSound;
import net.minecraft.client.renderer.texture.NativeImage;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.Util;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.registries.ForgeRegistries;
import org.apache.commons.lang3.RandomStringUtils;

import javax.annotation.Nonnull;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
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
    public int mpAge;
    public String mpString;
    public byte[] mpImage;
    public int lastPing;
    public ArrayList<String> chatMessages = new ArrayList<>();

    public Camera defaultCam = new Camera();
    public int activeView = -1;

    private WorkspaceTabula workspace;

    public Mainframe(String master)
    {
        this.isMaster = false;
        this.canEdit = false;
        this.master = master;
        String s;
        if(master.equalsIgnoreCase(Minecraft.getInstance().getSession().getUsername()))
        {
            s = I18n.format("system.hosting");
            listeners.add(master);
            editors.add(master);
            editors.addAll(Tabula.configClient.editors);
        }
        else
        {
            s = I18n.format("system.hostingOther", master);
        }
        chatMessages.add(getChatStyleMessage("System", s).getString());
    }

    public Mainframe setMaster()
    {
        isMaster = true;
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

    public void setCanEdit(boolean flag)
    {
        canEdit = flag;
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

        if(origin != null && !sessionEnded && !Minecraft.getInstance().isGamePaused())
        {
            if(master.equals(Minecraft.getInstance().getSession().getUsername()))
            {
                mpAge++;
                if(mpAge % 100 == 0)
                {
                    ProjectInfo info = getActiveProject();
                    if(info != null)
                    {
                        String projString = Project.SIMPLE_GSON.toJson(info.project);
                        byte[] image = info.project.getTextureBytes();

                        if(!projString.equals(mpString))
                        {
                            mpString = projString;
                            sendContent("Server", "project", "", info.project, (byte)-1);
                        }
                        if(!Arrays.equals(image, mpImage))
                        {
                            mpImage = image;
                            sendContent("Server", "image", "", mpImage, (byte)-1);
                        }
                    }
                }
            }
            else
            {
                lastPing++;
                if(lastPing > 600)
                {
                    sessionEnded = true;
                    addSystemMessage(I18n.format("system.cannotReachHost", master), false);
                }
            }
        }
    }

    public void shutdown()
    {
        if(origin != null && !sessionEnded) //if we're on MP
        {
            if(getIsMaster())
            {
                sendSystemMessage(I18n.format("system.sessionEnded", master));
            }
            Tabula.channel.sendToServer(new PacketKillSession(origin));
        }
    }

    //INPUT FROM CLIENT
    public void openProject(Project project, boolean isUserInput) //when opened using the UI
    {
        if(isUserInput && !getCanEdit())
        {
            WindowPopup.popup(workspace, 0.4D, 140, w -> {}, I18n.format("system.notEditor", Minecraft.getInstance().getSession().getUsername()));
            return;
        }
        ProjectInfo otherProj = getProjectInfoForProject(project);
        if(otherProj != null)
        {
            if(otherProj.project.saveFile != project.saveFile)
            {
                project.witnessProtectionProgramme();
            }
            else
            {
                WindowPopup.popup(workspace, 0.4D, 140, w -> {}, I18n.format("system.projectAlreadyOpen"));
                return;
            }
        }

        //add the project
        ProjectInfo info = new ProjectInfo(this, project);
        projects.add(info);

        if(isUserInput || projects.size() == 1)
        {
            //switch to view the active project
            activeView = projects.size() - 1;
        }

        //this is the first project you've opened.
        if(projects.size() == 1 && workspace.getByWindowType(WindowTexture.class) == null) // first project
        {
            Window<?> window = new WindowTexture(workspace);
            workspace.addToDock(window, Constraint.Property.Type.RIGHT);
            workspace.addToDocked(window, new WindowModelTree(workspace));
        }

        //Notify!
        workspace.setCurrentProject(getActiveProject());
        workspace.projectChanged(IProjectInfo.ChangeType.PROJECTS);
        workspace.projectChanged(IProjectInfo.ChangeType.PROJECT);

        if(isUserInput && origin != null && !sessionEnded)
        {
            sendContent("", info.project.identifier, "", info.project, (byte)1);

            if(info.project.getTextureBytes() != null)
            {
                sendContent("", info.project.identifier, "", info.project.getTextureBytes(), (byte)0);
            }
        }
    }

    public void editProject(Project project) //edited in the UI
    {
        if(!getCanEdit())
        {
            WindowPopup.popup(workspace, 0.4D, 140, w -> {}, I18n.format("system.notEditor", Minecraft.getInstance().getSession().getUsername()));
            return;
        }

        ProjectInfo info = getProjectInfoForProject(project);
        if(info != null)
        {
            info.markProjectDirty();
            workspace.projectChanged(IProjectInfo.ChangeType.PROJECT);

            if(origin != null && !sessionEnded)
            {
                sendContent("", info.project.identifier, "", info.project, (byte)2);
            }
        }
    }

    public void importProject(@Nonnull Project project, boolean texture, boolean isUserInput)
    {
        if(isUserInput && !getCanEdit())
        {
            WindowPopup.popup(workspace, 0.4D, 140, w -> {}, I18n.format("system.notEditor", Minecraft.getInstance().getSession().getUsername()));
            return;
        }

        ProjectInfo info = getActiveProject();
        if(info != null)
        {
            info.project.importProject(project, texture);
            if(texture)
            {
                info.textureFile = null;
                info.textureFileMd5 = null;
                workspace.projectChanged(IProjectInfo.ChangeType.TEXTURE);

                if(isUserInput && origin != null && !sessionEnded && info.project.getTextureBytes() != null)
                {
                    sendContent("", info.project.identifier, "", info.project.getTextureBytes(), (byte)0);
                }
            }
            info.markProjectDirty();
            workspace.projectChanged(IProjectInfo.ChangeType.PARTS);

            if(isUserInput && origin != null && !sessionEnded)
            {
                sendContent("", info.project.identifier, "", info.project, (byte)3);
            }
        }
    }

    public void closeProject(ProjectInfo info, boolean isUserInput)
    {
        if(isUserInput && !getCanEdit())
        {
            WindowPopup.popup(workspace, 0.4D, 140, w -> {}, I18n.format("system.notEditor", Minecraft.getInstance().getSession().getUsername()));
            return;
        }

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

            if(isUserInput && origin != null && !sessionEnded)
            {
                sendContent("", info.project.identifier, "", "null", (byte)4);
            }
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

    public void addPart(ProjectInfo info, Identifiable<?> parent, Project.Part part, boolean isUserInput)
    {
        if(isUserInput && !getCanEdit())
        {
            WindowPopup.popup(workspace, 0.4D, 140, w -> {}, I18n.format("system.notEditor", Minecraft.getInstance().getSession().getUsername()));
            return;
        }

        if(info != null)
        {
            info.project.addPart(parent, part);
            info.markProjectDirty();
            workspace.projectChanged(IProjectInfo.ChangeType.PARTS);

            if(isUserInput && origin != null && !sessionEnded)
            {
                sendContent("", info.project.identifier, parent == null ? "" : parent.identifier, part, (byte)5);
            }
        }
    }

    public void addBox(ProjectInfo info, Identifiable<?> parent, Project.Part.Box box, boolean isUserInput)
    {
        if(isUserInput && !getCanEdit())
        {
            WindowPopup.popup(workspace, 0.4D, 140, w -> {}, I18n.format("system.notEditor", Minecraft.getInstance().getSession().getUsername()));
            return;
        }

        if(info != null)
        {
            Project.Part part = info.project.addBox(parent, box);
            info.markProjectDirty();
            workspace.projectChanged(IProjectInfo.ChangeType.PARTS);

            if(isUserInput && origin != null && !sessionEnded)
            {
                if(part != null)
                {
                    sendContent("", info.project.identifier, "", part, (byte)5);
                    sendContent("", info.project.identifier, part.identifier, box, (byte)6);
                }
                else
                {
                    sendContent("", info.project.identifier, parent.identifier, box, (byte)6);
                }
            }
        }
    }

    public void delete(ProjectInfo info, Identifiable<?> child, boolean isUserInput) //parent should not be null
    {
        if(isUserInput && !getCanEdit())
        {
            WindowPopup.popup(workspace, 0.4D, 140, w -> {}, I18n.format("system.notEditor", Minecraft.getInstance().getSession().getUsername()));
            return;
        }

        if(info != null)
        {
            info.project.delete(child);
            info.markProjectDirty();

            if(child instanceof Project.Part)
            {
                info.selectPart(null);
            }
            else if(child instanceof Project.Part.Box)
            {
                info.selectBox(null);
            }

            workspace.projectChanged(IProjectInfo.ChangeType.PARTS);

            if(isUserInput && origin != null && !sessionEnded)
            {
                sendContent("", info.project.identifier, child.identifier, "null", (byte)7);
            }
        }
    }

    public void updatePart(Project.Part part, boolean isUserInput)
    {
        if(isUserInput && !getCanEdit())
        {
            WindowPopup.popup(workspace, 0.4D, 140, w -> {}, I18n.format("system.notEditor", Minecraft.getInstance().getSession().getUsername()));
            return;
        }

        ProjectInfo info = getProjectInfoForProject(part.markDirty());
        if(info != null)
        {
            info.markProjectDirty();
            workspace.projectChanged(IProjectInfo.ChangeType.PARTS);

            if(isUserInput && origin != null && !sessionEnded)
            {
                sendContent("", info.project.identifier, part.parent.identifier, part, (byte)8);
            }
        }
    }

    public void updateBox(Project.Part.Box box, boolean isUserInput)
    {
        if(isUserInput && !getCanEdit())
        {
            WindowPopup.popup(workspace, 0.4D, 140, w -> {}, I18n.format("system.notEditor", Minecraft.getInstance().getSession().getUsername()));
            return;
        }

        ProjectInfo info = getProjectInfoForProject(box.markDirty());
        if(info != null)
        {
            info.markProjectDirty();
            workspace.projectChanged(IProjectInfo.ChangeType.PARTS);

            if(isUserInput && origin != null && !sessionEnded)
            {
                sendContent("", info.project.identifier, box.parent.identifier, box, (byte)9);
            }
        }
    }

    public void setImage(ProjectInfo info, byte[] imageBytes, boolean isUserInput)
    {
        if(isUserInput && !getCanEdit())
        {
            WindowPopup.popup(workspace, 0.4D, 140, w -> {}, I18n.format("system.notEditor", Minecraft.getInstance().getSession().getUsername()));
            return;
        }

        if(info != null)
        {
            info.project.setImageBytes(imageBytes);
            info.markProjectDirty();
            workspace.projectChanged(IProjectInfo.ChangeType.TEXTURE);

            if(isUserInput && origin != null && !sessionEnded)
            {
                sendContent("", info.project.identifier, "", info.project.getTextureBytes(), (byte)0);
            }
        }
    }

    public void handleDragged(Identifiable<?> object, Identifiable<?> object1)
    {
        if(!getCanEdit()) //this function is only called by user input
        {
            WindowPopup.popup(workspace, 0.4D, 140, w -> {}, I18n.format("system.notEditor", Minecraft.getInstance().getSession().getUsername()));
            return;
        }

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
            ProjectInfo info = getActiveProject();
            if(info != null && project == info.project)
            {
                info.markProjectDirty();

                if(origin != null && !sessionEnded)
                {
                    sendContent("", info.project.identifier, "", info.project, (byte)2);
                }
                workspace.projectChanged(IProjectInfo.ChangeType.PARTS);
            }
        }
    }

    public void handleRearrange(List<ElementList.Item<?>> items, Identifiable<?> child)
    {
        if(!getCanEdit()) //this function is only called by user input
        {
            WindowPopup.popup(workspace, 0.4D, 140, w -> {}, I18n.format("system.notEditor", Minecraft.getInstance().getSession().getUsername()));
            return;
        }

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
                if(origin != null && !sessionEnded)
                {
                    sendContent("", info.project.identifier, "", info.project, (byte)2);
                }
                workspace.projectChanged(IProjectInfo.ChangeType.PARTS);
            }
        }
    }

    public void changeState(ProjectInfo info, boolean redo)
    {
        if(!getCanEdit()) //this function is only called by user input
        {
            WindowPopup.popup(workspace, 0.4D, 140, w -> {}, I18n.format("system.notEditor", Minecraft.getInstance().getSession().getUsername()));
            return;
        }

        if(redo)
        {
            info.redo();
        }
        else
        {
            info.undo();
        }
        if(origin != null && !sessionEnded)
        {
            sendContent("", info.project.identifier, "", info.project, (byte)2);
        }
        workspace.projectChanged(IProjectInfo.ChangeType.PROJECT);
        workspace.projectChanged(IProjectInfo.ChangeType.PARTS);
        workspace.projectChanged(IProjectInfo.ChangeType.TEXTURE);
    }
    //END INPUT STUFF

    //CONNECTION STUFF
    //UPDATE STUFF
    public void sendContent(String directed, String projIdent, String secondaryIdent, Object o, byte func) //empty string for directed to send to all
    {
        byte type;
        byte[] data;
        if(func == 0 || func == -1 && projIdent.equals("image")) //sending out an image
        {
            data = (byte[])o;
            type = 0;
        }
        else //it's an identifiable
        {
            String obj = Project.SIMPLE_GSON.toJson(o);
            try
            {
                data = IOUtil.compress(obj);
            }
            catch(IOException e){return;}
            if(o instanceof Project)
            {
                type = 1;
            }
            else if(o instanceof Project.Part)
            {
                type = 2;
            }
            else if(o instanceof Project.Part.Box)
            {
                type = 3;
            }
            else if(o instanceof String)
            {
                type = 4;
            }
            else
            {
                return;
            }
        }
        //1 = open project

        final int maxFile = 31000; //smaller packet cause I'm worried about too much info carried over from the bloat vs hat info.

        String fileName = RandomStringUtils.randomAscii(Project.IDENTIFIER_LENGTH);
        if(data == null) //only when cancelling an image
        {
            Tabula.channel.sendToServer(new PacketProjectFragment(fileName, 1, 0, new byte[0], origin, directed, projIdent, secondaryIdent, type, func));
            return;
        }
        int fileSize = data.length;

        int packetsToSend = (int)Math.ceil((float)fileSize / (float)maxFile);

        int packetCount = 0;
        int offset = 0;
        while(fileSize > 0)
        {
            byte[] fileBytes = new byte[Math.min(fileSize, maxFile)];
            int index = 0;
            while(index < fileBytes.length) //from index 0 to 30999
            {
                fileBytes[index] = data[index + offset];
                index++;
            }

            Tabula.channel.sendToServer(new PacketProjectFragment(fileName, packetsToSend, packetCount, fileBytes, origin, directed, projIdent, secondaryIdent, type, func));

            packetCount++;
            fileSize -= fileBytes.length;
            offset += index;
        }

    }


    //HOST STUFF
    public void listenerChange(String listener, boolean add)
    {
        if(add)
        {
            listeners.add(listener);
            if(Tabula.configClient.allowEveryoneToEdit)
            {
                editors.add(listener);
            }

            sendSystemMessage(I18n.format("system.joinedSession", listener));
            addSystemMessage(I18n.format(editors.contains(listener) ? "system.isEditor" : "system.notEditor", listener), true);

            if(editors.contains(listener))
            {
                Tabula.channel.sendToServer(new PacketEditorStatus(listener, true));
            }

            projects.forEach(info -> {
                sendContent(listener, info.project.identifier, "", info.project, (byte)1);
                if(info.project.getTextureBytes() != null)
                {
                    sendContent(listener, info.project.identifier, "", info.project.getTextureBytes(), (byte)0);
                }
            });
        }
        else
        {
            listeners.remove(listener);
            sendSystemMessage(I18n.format("system.leftSession", listener));
        }
    }

    public void editorChange(String editor, boolean add)
    {
        if(add)
        {
            editors.add(editor);
            sendSystemMessage(I18n.format("system.addEditor", master, editor));
            Tabula.channel.sendToServer(new PacketEditorStatus(editor, true));
        }
        else
        {
            editors.remove(editor);
            sendSystemMessage(I18n.format("system.removeEditor", master, editor));
            Tabula.channel.sendToServer(new PacketEditorStatus(editor, false));
        }
    }

    public void receiveChat(String s, boolean silent)
    {
        if(!silent && Tabula.configClient.chatSound && !s.startsWith(Minecraft.getInstance().getSession().getUsername()))
        {
            SoundEvent sound = ForgeRegistries.SOUND_EVENTS.getValue(new ResourceLocation("entity.experience_orb.pickup"));
            if(sound != null)
            {
                Minecraft.getInstance().getSoundHandler().play(SimpleSound.master(sound, 1F));
            }
        }
        chatMessages.add(s);
        workspace.updateChat();
    }

    public void addSystemMessage(String s, boolean silent)
    {
        receiveChat(getChatStyleMessage("System", s).getString(), silent);
    }

    public void sendSystemMessage(String s)
    {
        if(origin != null && !sessionEnded)
        {
            Tabula.channel.sendToServer(new PacketChat(origin, getChatStyleMessage("System", s).getString()));
        }
    }

    public void sendChat(String s, boolean global)
    {
        if(origin != null && !sessionEnded)
        {
            if(global)
            {
                Minecraft.getInstance().player.sendChatMessage(s);
            }
            else
            {
                Tabula.channel.sendToServer(new PacketChat(origin, getChatStyleMessage(Minecraft.getInstance().player.getName().getUnformattedComponentText(), s).getString()));
            }
        }
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


    //GETTERS
    public ProjectInfo getObjectWithIdentifier(String ident)
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

    public ProjectInfo getProjectInfoByProjectIdentifier(String identifier)
    {
        for(int i = projects.size() - 1; i >= 0; i--)
        {
            ProjectInfo info = projects.get(i);
            if(info.project.identifier.equals(identifier))
            {
                return info;
            }
        }
        return null;
    }


    public ProjectInfo getProjectInfoForProject(Project project)
    {
        return getProjectInfoByProjectIdentifier(project.identifier);
    }

    public static StringTextComponent getChatStyleMessage(String name, String s)
    {
        StringTextComponent text = new StringTextComponent(ElementTextWrapper.getRandomTextFormattingColorForName(name) + name);
        text.appendSibling(new StringTextComponent(TextFormatting.WHITE + " : " + s));
        return text;
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

                        byte[] image = null;
                        try (NativeImage img = NativeImage.read(new FileInputStream(textureFile)))
                        {
                            image = img.getBytes();
                        }
                        catch(IOException ignored){}

                        if(image != null)
                        {
                            //don't call setImage from the mainframe. We don't want to mark dirty.
                            project.setImageBytes(image);
                            mainframe.workspace.projectChanged(IProjectInfo.ChangeType.TEXTURE);
                        }

                        textureFile = file;
                        textureFileMd5 = md5;
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

                    states.add(new State(Project.SIMPLE_GSON.toJson(project), project.getTextureBytes()));
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
            State state = new State(Project.SIMPLE_GSON.toJson(project), project.getTextureBytes());
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

                //DO NOT CALL DESTROY.
                this.project = project;
                this.project.adoptChildren();

                if(this.project.getTextureBytes() != state.image)
                {
                    mainframe.setImage(this, state.image, false);
                }

                selectPart(null); // this selects a null box for us anyway
            }
        }

        public void addPart(Identifiable<?> parent, Project.Part part)
        {
            mainframe.addPart(this, parent, part, true);
        }

        public void addBox(Identifiable<?> parent, Project.Part.Box box)
        {
            mainframe.addBox(this, parent, box, true);
        }

        public void delete(Identifiable<?> child)
        {
            mainframe.delete(this, child, true);
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
            public final byte[] image;
            public boolean autosaved;

            public State(String project, byte[] image)
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
