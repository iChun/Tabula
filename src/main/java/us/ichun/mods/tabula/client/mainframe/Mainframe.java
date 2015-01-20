package us.ichun.mods.tabula.client.mainframe;

import com.google.gson.Gson;
import net.minecraft.client.Minecraft;
import net.minecraft.util.MathHelper;
import net.minecraft.util.StatCollector;
import org.apache.commons.lang3.RandomStringUtils;
import us.ichun.mods.ichunutil.common.core.util.IOUtil;
import us.ichun.mods.ichunutil.common.module.tabula.client.model.ModelInfo;
import us.ichun.mods.ichunutil.common.module.tabula.common.project.ProjectInfo;
import us.ichun.mods.ichunutil.common.module.tabula.common.project.components.Animation;
import us.ichun.mods.ichunutil.common.module.tabula.common.project.components.AnimationComponent;
import us.ichun.mods.ichunutil.common.module.tabula.common.project.components.CubeGroup;
import us.ichun.mods.ichunutil.common.module.tabula.common.project.components.CubeInfo;
import us.ichun.mods.tabula.client.gui.GuiWorkspace;
import us.ichun.mods.tabula.client.mainframe.core.ProjectHelper;
import us.ichun.mods.tabula.common.Tabula;
import us.ichun.mods.tabula.common.packet.*;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;

//This is the class that holds all the info of the workspace and handles UI input from everyone.
//The player hosting this doesn't edit this directly, he has his own workspace and whatever he does to the workspace there changes things here, which are sent back to him.
public class Mainframe
{
    public static final int IDENTIFIER_LENGTH = ProjectInfo.IDENTIFIER_LENGTH;

    public HashMap<String, Integer> listeners = new HashMap<String, Integer>();
    public ArrayList<String> editors = new ArrayList<String>();

    public boolean allowEditing;

    public ArrayList<ProjectInfo> projects = new ArrayList<ProjectInfo>(); //each workspace tab should be a project.

    public HashMap<String, ArrayList<byte[]>> projectParts = new HashMap<String, ArrayList<byte[]>>();

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
                        while(proj.states.size() > proj.switchState)
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
        Iterator<Map.Entry<String, Integer>> ite = listeners.entrySet().iterator();
        while(ite.hasNext())
        {
            Map.Entry<String, Integer> e = ite.next();
            if(!e.getKey().equals(Minecraft.getMinecraft().getSession().getUsername()))
            {
                e.setValue(e.getValue() + 1);
                if(e.getValue() == 25)
                {
                    Tabula.channel.sendToServer(new PacketRequestHeartbeat(Minecraft.getMinecraft().getSession().getUsername(), e.getKey()));
                }
                if(e.getValue() > 150)
                {
                    ite.remove();
                    sendChat("System", StatCollector.translateToLocalFormatted("system.timeout", e.getKey()));
                }
            }
        }
    }

    public void loadEmptyProject(String name, String author, int txWidth, int txHeight, double scaleX, double scaleY, double scaleZ)
    {
        ProjectInfo projectInfo = new ProjectInfo(name, author);
        projectInfo.projVersion = ProjectInfo.PROJ_VERSION;

        projectInfo.identifier = RandomStringUtils.randomAscii(IDENTIFIER_LENGTH);

        projectInfo.textureWidth = txWidth;
        projectInfo.textureHeight = txHeight;

        projectInfo.scale = new double[] { scaleX, scaleY, scaleZ };

        projects.add(projectInfo);

        streamProject(projectInfo);
    }

    public void editProject(String ident, String name, String author, int txWidth, int txHeight, double scaleX, double scaleY, double scaleZ)
    {
        for(ProjectInfo info : projects)
        {
            if(info.identifier.equals(ident))
            {
                info.modelName = name;
                info.authorName = author;
                info.textureWidth = txWidth;
                info.textureHeight = txHeight;
                info.scale = new double[] { scaleX, scaleY, scaleZ };
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
                boolean flag = true;
                Minecraft mc = Minecraft.getMinecraft();
                if(mc.currentScreen instanceof GuiWorkspace)
                {
                    GuiWorkspace workspace = (GuiWorkspace)mc.currentScreen;
                    flag = !workspace.wantToExit;
                }
                if(flag)
                {
                    streamProjectClosure(ident);
                }

                projects.remove(i);
            }
        }
    }

    public void sendChat(String name, String message)
    {
        for(String id : listeners.keySet())
        {
            //            if(id.toString().replaceAll("-", "").equals("deadbeefdeadbeefdeadbeefdeadbeef") || id.toString().replaceAll("-", "").equals(Minecraft.getMinecraft().getSession().getPlayerID().replaceAll("-", "")))
            if(id.equals(Minecraft.getMinecraft().getSession().getUsername()))
            {
                ProjectHelper.receiveChat(name + ": " + message);
            }
            else
            {
                Tabula.channel.sendToServer(new PacketChatMessage(Minecraft.getMinecraft().getSession().getUsername(), id, name + ": " + message));
            }
        }
    }

    public void streamProjectClosure(String ident)
    {
        for(String id : listeners.keySet())
        {
            //            if(id.toString().replaceAll("-", "").equals("deadbeefdeadbeefdeadbeefdeadbeef") || id.toString().replaceAll("-", "").equals(Minecraft.getMinecraft().getSession().getPlayerID().replaceAll("-", "")))
            if(id.equals(Minecraft.getMinecraft().getSession().getUsername()))
            {
                ProjectHelper.removeProjectFromManager(ident);
            }
            else
            {
                Tabula.channel.sendToServer(new PacketCloseProject(Minecraft.getMinecraft().getSession().getUsername(), id, ident));
            }
        }
    }

    public void streamProject(ProjectInfo project)
    {
        project.lastState = age;//Update lastState because of an action.
        allowEditing = false;
        for(String id : listeners.keySet())
        {
            //            if(id.toString().replaceAll("-", "").equals("deadbeefdeadbeefdeadbeefdeadbeef") || id.toString().replaceAll("-", "").equals(Minecraft.getMinecraft().getSession().getPlayerID().replaceAll("-", "")))
            if(id.equals(Minecraft.getMinecraft().getSession().getUsername()))
            {
                ProjectHelper.addProjectToManager(ProjectHelper.createProjectFromJsonHost(project.identifier, project.getAsJson()));
            }
            else
            {
                boolean isCurrent = false;
                Minecraft mc = Minecraft.getMinecraft();
                if(mc.currentScreen instanceof GuiWorkspace)
                {
                    GuiWorkspace workspace = (GuiWorkspace)mc.currentScreen;
                    if(!((GuiWorkspace)workspace).projectManager.projects.isEmpty())
                    {
                        isCurrent = project.identifier.equals(((GuiWorkspace)workspace).projectManager.projects.get(((GuiWorkspace)workspace).projectManager.selectedProject).identifier);
                    }
                }
                streamProjectToListener(id, project, isCurrent);
            }
        }

        if(listeners.size() == 1)
        {
            Minecraft mc = Minecraft.getMinecraft();
            if(mc.currentScreen instanceof GuiWorkspace)
            {
                GuiWorkspace workspace = (GuiWorkspace)mc.currentScreen;
                if(!((GuiWorkspace)workspace).remoteSession && ((GuiWorkspace)workspace).host != null)
                {
                    Tabula.channel.sendToServer(new PacketSetCurrentProject(((GuiWorkspace)workspace).host, ((GuiWorkspace)workspace).hostX, ((GuiWorkspace)workspace).hostY, ((GuiWorkspace)workspace).hostZ, project.identifier));
                }
            }
        }
        allowEditing = true;
    }

    public void streamProjectToListener(String id, ProjectInfo proj, boolean currentProj)
    {
        sendData(id, proj.identifier, proj.getAsJson().getBytes(), false, currentProj);
    }

    public void streamProjectTexture(String ident, BufferedImage bufferedImage)
    {
        allowEditing = false;
        for(String id : listeners.keySet())
        {
            //            if(id.toString().replaceAll("-", "").equals("deadbeefdeadbeefdeadbeefdeadbeef") || id.toString().replaceAll("-", "").equals(Minecraft.getMinecraft().getSession().getPlayerID().replaceAll("-", "")))
            if(id.equals(Minecraft.getMinecraft().getSession().getUsername()))
            {
                ProjectHelper.updateProjectTexture(ident, bufferedImage);
            }
            else
            {
                boolean isCurrent = false;
                Minecraft mc = Minecraft.getMinecraft();
                if(mc.currentScreen instanceof GuiWorkspace)
                {
                    GuiWorkspace workspace = (GuiWorkspace)mc.currentScreen;
                    if(!((GuiWorkspace)workspace).projectManager.projects.isEmpty())
                    {
                        isCurrent = ident.equals(((GuiWorkspace)workspace).projectManager.projects.get(((GuiWorkspace)workspace).projectManager.selectedProject).identifier);
                    }
                }
                streamProjectTextureToListener(id, ident, bufferedImage, isCurrent);
            }
        }
        if(listeners.size() == 1)
        {
            Minecraft mc = Minecraft.getMinecraft();
            if(mc.currentScreen instanceof GuiWorkspace)
            {
                GuiWorkspace workspace = (GuiWorkspace)mc.currentScreen;
                if(!((GuiWorkspace)workspace).remoteSession && ((GuiWorkspace)workspace).host != null)
                {
                    Tabula.channel.sendToServer(new PacketSetCurrentProject(((GuiWorkspace)workspace).host, ((GuiWorkspace)workspace).hostX, ((GuiWorkspace)workspace).hostY, ((GuiWorkspace)workspace).hostZ, ident));
                }
            }
        }
        allowEditing = true;
    }

    public void streamProjectTextureToListener(String id, String ident, BufferedImage img, boolean currentProj)
    {
        if(img == null)
        {
            int x = -1;
            int y = -1;
            int z = -1;

            Minecraft mc = Minecraft.getMinecraft();
            if(mc.currentScreen instanceof GuiWorkspace)
            {
                GuiWorkspace workspace = (GuiWorkspace)mc.currentScreen;
                x = ((GuiWorkspace)workspace).hostX;
                y = ((GuiWorkspace)workspace).hostY;
                z = ((GuiWorkspace)workspace).hostZ;
            }

            Tabula.channel.sendToServer(new PacketProjectFragment(x, y, z, false, Minecraft.getMinecraft().getSession().getUsername(), id, ident, true, false, currentProj, 1, -1, 0, new byte[0]));
        }
        else
        {
            try
            {
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                ImageIO.write(img, "png", baos);
                sendData(id, ident, baos.toByteArray(), true, currentProj);
            }
            catch(IOException ignored){}
        }
    }

    public void sendData(String id, String projectIdent, byte[] data, boolean isTexture, boolean isCurrentProject)
    {
        final int maxFile = 31000; //smaller packet cause I'm worried about too much info carried over from the bloat vs hat info.

        int fileSize = data.length;

        int packetsToSend = (int)Math.ceil((float)fileSize / (float)maxFile);

        int packetCount = 0;
        int offset = 0;
        while(fileSize > 0)
        {
            byte[] fileBytes = new byte[fileSize > maxFile ? maxFile : fileSize];
            int index = 0;
            while(index < fileBytes.length) //from index 0 to 31999
            {
                fileBytes[index] = data[index + offset];
                index++;
            }

            int x = -1;
            int y = -1;
            int z = -1;

            Minecraft mc = Minecraft.getMinecraft();
            if(mc.currentScreen instanceof GuiWorkspace)
            {
                GuiWorkspace workspace = (GuiWorkspace)mc.currentScreen;
                x = ((GuiWorkspace)workspace).hostX;
                y = ((GuiWorkspace)workspace).hostY;
                z = ((GuiWorkspace)workspace).hostZ;
            }

            Tabula.channel.sendToServer(new PacketProjectFragment(x, y, z, false, Minecraft.getMinecraft().getSession().getUsername(), id, projectIdent, isTexture, false, isCurrentProject, packetsToSend, packetCount, fileSize > maxFile ? maxFile : fileSize, fileBytes));

            packetCount++;
            fileSize -= 32000;
            offset += index;
        }
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

        project.repair();

        project.identifier = RandomStringUtils.randomAscii(IDENTIFIER_LENGTH);

        projects.add(project);

        project.bufferedTexture = image;

        for(ProjectInfo info : projects)
        {
            if(info.identifier.equals(ident))
            {
                info.cubes.addAll(project.cubes);
                info.cubeGroups.addAll(project.cubeGroups);

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

    public void overrideProject(String projectIdent, String projectJson, BufferedImage image) // replaces an entire project with this, or creates a new one.
    {
        ProjectInfo project = ((new Gson()).fromJson(projectJson, ProjectInfo.class));

        project.repair();

        BufferedImage oriImage = null;

        boolean flag = true;
        if(!projectIdent.isEmpty())
        {
            for(int i = 0; i < projects.size(); i++)
            {
                ProjectInfo proj = projects.get(i);
                if(proj.identifier.equals(projectIdent))
                {
                    oriImage = proj.bufferedTexture;
                    int txW = project.textureWidth;
                    int txH = project.textureHeight;
                    project.inherit(proj);
                    project.textureWidth = txW;
                    project.textureHeight = txH;
                    project.bufferedTexture = image;
                    projects.remove(i);
                    projects.add(i, project);
                    flag = false;
                    break;
                }
            }
        }

        if(flag)
        {
            project.identifier = RandomStringUtils.randomAscii(IDENTIFIER_LENGTH);

            projects.add(project);

            project.bufferedTexture = image;
        }

        streamProject(project);

        if(!IOUtil.areBufferedImagesEqual(oriImage, project.bufferedTexture))
        {
            streamProjectTexture(project.identifier, project.bufferedTexture);
        }
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

    private void importModel(String ident, ModelInfo model, boolean texture)
    {
        ProjectInfo projectInfo = null;
        if(ident.isEmpty())
        {
            projectInfo = new ProjectInfo(model.modelParent.getClass().getSimpleName(), "Either Mojang or a mod author");
            projectInfo.projVersion = ProjectInfo.PROJ_VERSION;

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
                    if(((CubeInfo)draggedOnto).parentIdentifier != null && ((CubeInfo)draggedOnto).parentIdentifier.equals(((CubeInfo)dragged).identifier))
                    {
                        ((CubeInfo)dragged).removeChild((CubeInfo)draggedOnto);
                        childProtectiveServices(info, null, draggedOnto);
                    }
                    ((CubeInfo)draggedOnto).addChild((CubeInfo)dragged);
                }
                childProtectiveServices(info, draggedOnto, dragged);

                streamProject(info);
            }
        }
    }

    public void createNewAnimation(String ident, String name, boolean loops)
    {
        for(ProjectInfo info : projects)
        {
            if(info.identifier.equals(ident))
            {
                info.createNewAnimation(name, loops);
                streamProject(info);
            }
        }
    }

    public void editAnimation(String ident, String animIdent, String name, boolean loops)
    {
        for(ProjectInfo info : projects)
        {
            if(info.identifier.equals(ident))
            {
                for(Animation anim : info.anims)
                {
                    if(anim.identifier.equals(animIdent))
                    {
                        anim.name = name;
                        anim.loops = loops;
                        streamProject(info);
                    }
                }
            }
        }
    }

    public void deleteAnimation(String ident, String animIdent)
    {
        for(ProjectInfo info : projects)
        {
            if(info.identifier.equals(ident))
            {
                for(int i = info.anims.size() - 1; i >= 0; i--)
                {
                    if(info.anims.get(i).identifier.equals(animIdent))
                    {
                        info.anims.remove(i);
                        streamProject(info);
                    }
                }
            }
        }
    }

    public void createNewAnimComponent(String ident, String animIdent, String cubeIdent, String name, int animLength, int startPos)
    {
        for(ProjectInfo info : projects)
        {
            if(info.identifier.equals(ident))
            {
                for(Animation anim : info.anims)
                {
                    if(anim.identifier.equals(animIdent))
                    {
                        anim.createAnimComponent(cubeIdent, name, animLength, startPos);
                        streamProject(info);
                    }
                }
            }
        }
    }

    public void editAnimComponent(String ident, String animIdent, String compIdent, String name, int animLength, int startPos)
    {
        for(ProjectInfo info : projects)
        {
            if(info.identifier.equals(ident))
            {
                for(Animation anim : info.anims)
                {
                    if(anim.identifier.equals(animIdent))
                    {
                        for(Map.Entry<String, ArrayList<AnimationComponent>> e : anim.sets.entrySet())
                        {
                            for(AnimationComponent comp : e.getValue())
                            {
                                if(comp.identifier.equals(compIdent))
                                {
                                    comp.name = name;
                                    comp.length = animLength;
                                    comp.startKey = startPos;
                                    streamProject(info);
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    public void deleteAnimComponent(String ident, String animIdent, String compIdent)
    {
        for(ProjectInfo info : projects)
        {
            if(info.identifier.equals(ident))
            {
                for(Animation anim : info.anims)
                {
                    if(anim.identifier.equals(animIdent))
                    {
                        Iterator<Map.Entry<String, ArrayList<AnimationComponent>>> ite = anim.sets.entrySet().iterator();
                        while(ite.hasNext())
                        {
                            Map.Entry<String, ArrayList<AnimationComponent>> e = ite.next();
                            if(e.getKey().equals(compIdent))
                            {
                                ite.remove();
                                streamProject(info);
                            }
                            else
                            {
                                for(int i = e.getValue().size() - 1; i >= 0; i--)
                                {
                                    if(e.getValue().get(i).identifier.equals(compIdent))
                                    {
                                        e.getValue().remove(i);
                                        if(e.getValue().isEmpty())
                                        {
                                            ite.remove();
                                        }
                                        streamProject(info);
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    public void splitAnimComponent(String ident, String animIdent, String compIdent, int currentPos)
    {
        for(ProjectInfo info : projects)
        {
            if(info.identifier.equals(ident))
            {
                for(Animation anim : info.anims)
                {
                    if(anim.identifier.equals(animIdent))
                    {
                        String groupIdent = null;
                        AnimationComponent comp = null;
                        for(Map.Entry<String, ArrayList<AnimationComponent>> e : anim.sets.entrySet())
                        {
                            for(AnimationComponent c : e.getValue())
                            {
                                if(c.identifier.equalsIgnoreCase(compIdent))
                                {
                                    comp = c;
                                    groupIdent = e.getKey();
                                    break;
                                }
                            }
                        }
                        if(groupIdent != null && currentPos > comp.startKey && currentPos < comp.startKey + comp.length )
                        {
                            AnimationComponent split1 = new AnimationComponent(comp.name + "_1", currentPos - comp.startKey, comp.startKey);
                            AnimationComponent split2 = new AnimationComponent(comp.name + "_2", (comp.startKey + comp.length) - currentPos, currentPos);

                            split1.posOffset = comp.posOffset;
                            split1.rotOffset = comp.rotOffset;
                            split1.scaleOffset = comp.scaleOffset;
                            split1.opacityOffset = comp.opacityOffset;

                            float prog = MathHelper.clamp_float((currentPos - comp.startKey) / (float)comp.length, 0F, 1F);
                            float mag = prog;
                            if(comp.getProgressionCurve() != null)
                            {
                                mag = MathHelper.clamp_float((float)comp.getProgressionCurve().value(prog), 0.0F, 1.0F);
                            }
                            for(int i = 0; i < 3; i++)
                            {
                                split1.posChange[i] = comp.posChange[i] * mag;
                                split1.rotChange[i] = comp.rotChange[i] * mag;
                                split1.scaleChange[i] = comp.scaleChange[i] * mag;
                            }
                            split1.opacityChange = comp.opacityChange * mag;

                            for(int i = 0; i < 3; i++)
                            {
                                split2.posChange[i] = comp.posChange[i] - split1.posChange[i];
                                split2.rotChange[i] = comp.rotChange[i] - split1.rotChange[i];
                                split2.scaleChange[i] = comp.scaleChange[i] - split1.scaleChange[i];
                            }
                            split2.opacityChange = comp.opacityChange - split1.opacityChange;

                            anim.sets.get(groupIdent).remove(comp);
                            anim.sets.get(groupIdent).add(split1);
                            anim.sets.get(groupIdent).add(split2);

                            streamProject(info);
                        }
                    }
                }
            }
        }
    }

    public void toggleAnimComponentVisibility(String ident, String animIdent, String compIdent)
    {
        for(ProjectInfo info : projects)
        {
            if(info.identifier.equals(ident))
            {
                for(Animation anim : info.anims)
                {
                    if(anim.identifier.equals(animIdent))
                    {
                        for(Map.Entry<String, ArrayList<AnimationComponent>> e : anim.sets.entrySet())
                        {
                            for(AnimationComponent comp : e.getValue())
                            {
                                if(comp.identifier.equals(compIdent))
                                {
                                    comp.hidden = !comp.hidden;
                                    streamProject(info);
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    public void resetAnimCompProgCoord(String ident, String animIdent, String compIdent)
    {
        for(ProjectInfo info : projects)
        {
            if(info.identifier.equals(ident))
            {
                for(Animation anim : info.anims)
                {
                    if(anim.identifier.equals(animIdent))
                    {
                        for(Map.Entry<String, ArrayList<AnimationComponent>> e : anim.sets.entrySet())
                        {
                            for(AnimationComponent comp : e.getValue())
                            {
                                if(comp.identifier.equals(compIdent))
                                {
                                    comp.progressionCoords = null;
                                    comp.progressionCurve = null;
                                    streamProject(info);
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    public void moveAnimCompProgCoord(String ident, String animIdent, String compIdent, double oldX, double oldY, double newX, double newY)
    {
        for(ProjectInfo info : projects)
        {
            if(info.identifier.equals(ident))
            {
                for(Animation anim : info.anims)
                {
                    if(anim.identifier.equals(animIdent))
                    {
                        for(Map.Entry<String, ArrayList<AnimationComponent>> e : anim.sets.entrySet())
                        {
                            for(AnimationComponent comp : e.getValue())
                            {
                                if(comp.identifier.equals(compIdent))
                                {
                                    if(oldX < 0 || oldY < 0)
                                    {
                                        comp.addProgressionCoords(newX, newY);
                                    }
                                    else if(newX < 0 || newY < 0)
                                    {
                                        comp.removeProgressionCoords(oldX, oldY);
                                    }
                                    else
                                    {
                                        comp.moveProgressionCoords(oldX, oldY, newX, newY);
                                    }
                                    streamProject(info);
                                }
                            }
                        }
                    }
                }
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

    public void createNewCube(String ident, String json, boolean inPlace, boolean withChildren)
    {
        for(ProjectInfo info : projects)
        {
            if(info.identifier.equals(ident))
            {
                CubeInfo cube = ((new Gson()).fromJson(json, CubeInfo.class));

                cube.parentIdentifier = null;
                cube.identifier = RandomStringUtils.randomAscii(IDENTIFIER_LENGTH);

                if(!inPlace)
                {
                    cube.position = new double[3];
                    cube.offset = new double[3];
                    cube.rotation = new double[3];
                }

                info.cubeCount++;
                info.cubes.add(cube);

                if(withChildren)
                {
                    reidentifyChildren(cube.getChildren(), info);
                }
                else
                {
                    cube.getChildren().clear();
                }

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

    public void updateGroup(String projIdent, String groupIdent, String name, double[] pos, double[] offset, double[] scale, int[] txOffset, double[] rot, boolean mirror, double mcScale, double opacity)
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
                        updateGroupPieces(info1, pos, offset, scale, txOffset, rot, mirror, mcScale, opacity);
                        break;
                    }
                }
                if(!found)
                {
                    updateGroupInCubeGroups(groupIdent, proj.cubeGroups, name, pos, offset, scale, txOffset, rot, mirror, mcScale, opacity);
                }

                streamProject(proj);
            }
        }
    }

    public void updateGroupInCubeGroups(String groupIdent, ArrayList<CubeGroup> groups, String name, double[] pos, double[] offset, double[] scale, int[] txOffset, double[] rot, boolean mirror, double mcScale, double opacity)
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
                    updateGroupPieces(info1, pos, offset, scale, txOffset, rot, mirror, mcScale, opacity);
                    break;
                }
            }
            updateGroupInCubeGroups(groupIdent, proj.cubeGroups, name, pos, offset, scale, txOffset, rot, mirror, mcScale, opacity);
        }
    }

    public void updateGroupPieces(CubeGroup group, double[] pos, double[] offset, double[] scale, int[] txOffset, double[] rot, boolean mirror, double mcScale, double opacity)
    {
        for(CubeGroup group1 : group.cubeGroups)
        {
            updateGroupPieces(group1, pos, offset, scale, txOffset, rot, mirror, mcScale, opacity);
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
            cube.opacity = opacity;
        }
    }

    public void updateCube(String ident, String cubeInfo, String animIdent, String compIdent, int pos)
    {
        for(ProjectInfo proj : projects)
        {
            if(proj.identifier.equals(ident))
            {
                CubeInfo info = ((new Gson()).fromJson(cubeInfo, CubeInfo.class));

                boolean editCube = compIdent.isEmpty();

                if(!animIdent.isEmpty())
                {
                    for(Animation anim : proj.anims)
                    {
                        if(anim.identifier.equalsIgnoreCase(animIdent))
                        {
                            ArrayList<AnimationComponent> animationComponents = anim.sets.get(info.identifier);
                            if(animationComponents != null)
                            {
                                Collections.sort(animationComponents);

                                CubeInfo ori = (CubeInfo)proj.getObjectByIdent(info.identifier);

                                if(ori != null)
                                {
                                    AnimationComponent selected = null;
                                    for(AnimationComponent comp : animationComponents)
                                    {
                                        if(!comp.hidden)
                                        {
                                            comp.animate(ori, pos);
                                            if(comp.identifier.equals(compIdent))
                                            {
                                                selected = comp;
                                            }
                                        }
                                    }

                                    double[] posChange = new double[3];
                                    double[] rotChange = new double[3];
                                    double[] scaleChange = new double[3];
                                    double opacityChange = 0.0D;

                                    if(selected != null && (selected.startKey == pos || selected.startKey + selected.length == pos))
                                    {
                                        for(int i = 0; i < 3; i++)
                                        {
                                            posChange[i] = info.position[i] - ori.position[i];
                                            rotChange[i] = info.rotation[i] - ori.rotation[i];
                                            scaleChange[i] = info.scale[i] - ori.scale[i];
                                        }
                                        opacityChange = info.opacity - ori.opacity;
                                    }

                                    for(AnimationComponent comp : animationComponents)
                                    {
                                        if(!comp.hidden)
                                        {
                                            comp.reset(ori, pos);
                                        }
                                    }

                                    if(selected != null)
                                    {
                                        if(selected.startKey == pos)
                                        {
                                            for(int i = 0; i < 3; i++)
                                            {
                                                selected.posOffset[i] += posChange[i];
                                                selected.rotOffset[i] += rotChange[i];
                                                selected.scaleOffset[i] += scaleChange[i];
                                            }
                                            selected.opacityOffset += opacityChange;
                                        }
                                        else if(selected.startKey + selected.length == pos)
                                        {
                                            for(int i = 0; i < 3; i++)
                                            {
                                                selected.posChange[i] += posChange[i];
                                                selected.rotChange[i] += rotChange[i];
                                                selected.scaleChange[i] += scaleChange[i];
                                            }
                                            selected.opacityChange += opacityChange;
                                        }
                                    }
                                }
                            }
                            break;
                        }
                    }
                }

                if(editCube)
                {
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

                for(Animation anim : proj.anims)
                {
                    anim.sets.remove(cubeIdent);
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

    public void addListener(String id, boolean isEditor)
    {
        if(!listeners.containsKey(id))
        {
            listeners.put(id, 0);
            if(!id.equals(Minecraft.getMinecraft().getSession().getUsername()))
            {
                sendChat("System", StatCollector.translateToLocalFormatted("system.joinedSession", id));
                for(int i = 0; i < projects.size(); i++)
                {
                    ProjectInfo proj = projects.get(i);
                    streamProjectToListener(id, proj, false);
                    streamProjectTextureToListener(id, proj.identifier, proj.bufferedTexture, false);
                }
            }
        }
        if(isEditor && !editors.contains(id))
        {
            editors.add(id);
            if(!id.equals(Minecraft.getMinecraft().getSession().getUsername()))
            {
                sendChat("System", StatCollector.translateToLocalFormatted("system.isEditor", id));
            }
        }
        if(editors.contains(id) && !id.equals(Minecraft.getMinecraft().getSession().getUsername()))
        {
            Tabula.channel.sendToServer(new PacketIsEditor(Minecraft.getMinecraft().getSession().getUsername(), id, true));
        }
        updateListenersList();
    }

    public void removeListener(String id)
    {
        if(listeners.containsKey(id))
        {
            listeners.remove(id);
            sendChat("System", StatCollector.translateToLocalFormatted("system.leftSession", id));

            updateListenersList();
        }
    }

    public void addEditor(String id)
    {
        if(!editors.contains(id))
        {
            editors.add(id);
            sendChat("System", StatCollector.translateToLocalFormatted("system.addEditor", Minecraft.getMinecraft().getSession().getUsername(), id));
            Tabula.channel.sendToServer(new PacketIsEditor(Minecraft.getMinecraft().getSession().getUsername(), id, true));

            String[] editors = Tabula.config.getString("editors").split(", *");
            ArrayList<String> editorArray = new ArrayList<String>();
            for(String s : editors)
            {
                if(!s.isEmpty() && !editorArray.contains(s))
                {
                    editorArray.add(s);
                }
            }

            if(!editorArray.contains(id))
            {
                editorArray.add(id);
            }

            StringBuilder sb = new StringBuilder();
            for(int i = 0; i < editorArray.size(); i++)
            {
                sb.append(sb);
                if(i != editorArray.size() - 1)
                {
                    sb.append(", ");
                }
            }

            Tabula.config.get("editors").set(sb.toString());
            Tabula.config.save();

            updateListenersList();
        }
        else
        {
            ProjectHelper.receiveChat("System: " + StatCollector.translateToLocalFormatted("system.alreadyEditor", id));
        }
    }

    public void removeEditor(String id)
    {
        if(editors.contains(id))
        {
            editors.remove(id);
            sendChat("System", StatCollector.translateToLocalFormatted("system.removeEditor", Minecraft.getMinecraft().getSession().getUsername(), id));
            Tabula.channel.sendToServer(new PacketIsEditor(Minecraft.getMinecraft().getSession().getUsername(), id, false));

            String[] editors = Tabula.config.getString("editors").split(", *");
            ArrayList<String> editorArray = new ArrayList<String>();
            for(String s : editors)
            {
                if(!s.isEmpty() && !editorArray.contains(s))
                {
                    editorArray.add(s);
                }
            }

            if(editorArray.contains(id))
            {
                editorArray.remove(id);
            }

            StringBuilder sb = new StringBuilder();
            for(int i = 0; i < editorArray.size(); i++)
            {
                sb.append(sb);
                if(i != editorArray.size() - 1)
                {
                    sb.append(", ");
                }
            }

            Tabula.config.get("editors").set(sb.toString());
            Tabula.config.save();

            updateListenersList();
        }
        else
        {
            ProjectHelper.receiveChat("System: " + StatCollector.translateToLocalFormatted("system.notEditor", id));
        }
    }

    public void updateListenersList()
    {
        for(String id : listeners.keySet())
        {
            if(!id.equals(Minecraft.getMinecraft().getSession().getUsername()))
            {
                Tabula.channel.sendToServer(new PacketListenersList(id, editors, listeners.keySet()));
            }
        }
    }

    public boolean isEditor(String id)
    {
        String[] editors = Tabula.config.getString("editors").split(", *");
        for(String editor : editors)
        {
            if(!editor.isEmpty())
            {
                return editor.equals(id);
            }
        }
        return Tabula.config.getInt("allowEveryoneToEdit") == 1 || id.equals(Minecraft.getMinecraft().getSession().getUsername());
    }

    public void shutdown()
    {
        if(Tabula.proxy.tickHandlerClient.mainframe == this)
        {
            Tabula.proxy.tickHandlerClient.mainframe = null;
        }
    }

    public void receiveProjectData(String projectIdentifier, boolean isImport, int projectSize, byte packetTotal, byte packetNumber, byte[] data) // return true if only the project data is different?
    {
        ArrayList<byte[]> byteArray = projectParts.get(projectIdentifier);
        if(byteArray == null)
        {
            byteArray = new ArrayList<byte[]>();

            projectParts.put(projectIdentifier, byteArray);

            for(int i = 0; i < packetTotal; i++)
            {
                byteArray.add(new byte[0]);
            }
        }

        byteArray.set(packetNumber, data);

        boolean hasAllInfo = true;

        for(int i = 0; i < byteArray.size(); i++)
        {
            byte[] byteList = byteArray.get(i);
            if(byteList.length == 0)
            {
                hasAllInfo = false;
            }
        }

        if(hasAllInfo)
        {
            int size = 0;

            for(int i = 0; i < byteArray.size(); i++)
            {
                size += byteArray.get(i).length;
            }

            byte[] bytes = new byte[size];

            int index = 0;

            for(int i = 0; i < byteArray.size(); i++)
            {
                System.arraycopy(byteArray.get(i), 0, bytes, index, byteArray.get(i).length);
                index += byteArray.get(i).length;
            }

            try
            {
                byte[] projBytes = new byte[projectSize];
                System.arraycopy(bytes, 0, projBytes, 0, projectSize);

                String json = new String(projBytes, "UTF-8");
                ProjectInfo proj = ProjectHelper.createProjectFromJson(projectIdentifier, json);

                BufferedImage projImage = null;
                if(bytes.length > projectSize)
                {
                    byte[] imgBytes = new byte[bytes.length - projectSize];
                    System.arraycopy(bytes, projectSize, imgBytes, 0, imgBytes.length);

                    InputStream is = new ByteArrayInputStream(imgBytes);
                    projImage = ImageIO.read(is);
                }

                if(isImport)
                {
                    importProject(projectIdentifier, proj.getAsJson(), projImage);
                }
                else
                {
                    overrideProject(projectIdentifier, proj.getAsJson(), projImage);
                }
            }
            catch(IOException ignored)
            {
            }
            catch(Exception e)
            {
                Tabula.console("Error reading project sent from client!", true);
                e.printStackTrace();
            }

            projectParts.remove(projectIdentifier);
        }
    }
}
