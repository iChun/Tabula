package me.ichun.mods.tabula.old.client.mainframe.core;

import com.google.gson.Gson;
import me.ichun.mods.ichunutil.client.gui.window.Window;
import me.ichun.mods.ichunutil.common.core.util.IOUtil;
import me.ichun.mods.ichunutil.common.module.tabula.project.ProjectInfo;
import me.ichun.mods.tabula.old.client.gui.GuiWorkspace;
import me.ichun.mods.tabula.old.client.gui.window.WindowOpenProject;
import me.ichun.mods.tabula.old.common.Tabula;
import me.ichun.mods.tabula.old.common.packet.PacketProjectFragment;
import me.ichun.mods.tabula.old.common.packet.PacketProjectFragmentFromClient;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.PositionedSoundRecord;
import net.minecraft.init.SoundEvents;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;

public class ProjectHelper
{
    public static HashMap<String, ArrayList<byte[]>> projectParts = new HashMap<>();
    public static HashMap<String, ArrayList<byte[]>> projectTextureParts = new HashMap<>();

    public static HashMap<String, ProjectInfo> projects = new HashMap<>();
    public static HashMap<String, BufferedImage> projectTextures = new HashMap<>();
    public static HashMap<BufferedImage, Integer> projectTextureIDs = new HashMap<>();

    public static ArrayList<BufferedImage> texturesToClear = new ArrayList<>();
    public static ArrayList<ProjectInfo> projectsToDestroy = new ArrayList<>();

    public static ProjectInfo createProjectFromJson(String ident, String s)
    {
        Gson gson = new Gson();
        ProjectInfo info = gson.fromJson(s, ProjectInfo.class);
        info.identifier = ident;
        return info;
    }

    @SideOnly(Side.CLIENT)
    public static ProjectInfo createProjectFromJsonHost(String ident, String s)//TODO is this for opening just for host or....?
    {
        Gson gson = new Gson();
        ProjectInfo info = gson.fromJson(s, ProjectInfo.class);
        info.identifier = ident;

        Minecraft mc = Minecraft.getMinecraft();
        if(mc.currentScreen instanceof GuiWorkspace)
        {
            GuiWorkspace workspace = (GuiWorkspace)mc.currentScreen;
            for(int i = workspace.levels.size() - 1; i >= 0; i--)
            {
                for(int j = workspace.levels.get(i).size() - 1; j >= 0; j--)
                {
                    Window window = workspace.levels.get(i).get(j);
                    if(window instanceof WindowOpenProject)
                    {
                        if(((WindowOpenProject)window).openingJson != null && ((WindowOpenProject)window).openingJson.equals(s))
                        {
                            info.saveFile = ((WindowOpenProject)window).openingFile;
                            info.saveFileMd5 = IOUtil.getMD5Checksum(info.saveFile);
                            info.saved = true;

                            window.workspace.removeWindow(window);
                            break;
                        }
                    }
                }
            }
        }
        return info;
    }

    @SideOnly(Side.CLIENT)
    public static void addProjectToManager(ProjectInfo project)
    {
        Minecraft mc = Minecraft.getMinecraft();
        if(mc.currentScreen instanceof GuiWorkspace)
        {
            GuiWorkspace workspace = (GuiWorkspace)mc.currentScreen;
            workspace.projectManager.updateProject(project);
        }
    }

    @SideOnly(Side.CLIENT)
    public static void removeProjectFromManager(String ident)
    {
        Minecraft mc = Minecraft.getMinecraft();
        if(mc.currentScreen instanceof GuiWorkspace)
        {
            GuiWorkspace workspace = (GuiWorkspace)mc.currentScreen;
            workspace.projectManager.removeProject(ident);
        }
    }

    @SideOnly(Side.CLIENT)
    public static void updateProjectTexture(String ident, BufferedImage image)
    {
        Minecraft mc = Minecraft.getMinecraft();
        if(mc.currentScreen instanceof GuiWorkspace)
        {
            GuiWorkspace workspace = (GuiWorkspace)mc.currentScreen;
            for(ProjectInfo project : workspace.projectManager.projects)
            {
                if(project.identifier.equals(ident))
                {
                    if(project.ignoreNextImage)
                    {
                        project.ignoreNextImage = false; //received texture you've just set;
                    }
                    else if(project.textureFile != null)
                    {
                        project.textureFile = null; //texture file updated for other reasons.. no longer yours. Stop listening.
                    }
                    project.bufferedTexture = image;
                }
            }
        }
    }

    public static boolean receiveProjectData(boolean fromClient, String projectIdentifier, boolean isTexture, boolean updateDims, byte packetTotal, byte packetNumber, byte[] data) // return true if only the project data is different?
    {
        boolean flag = false;
        if(packetNumber == -1)
        {
            if(projectTextures.get(projectIdentifier) != null)
            {
                flag = true;
                Integer id = ProjectHelper.projectTextureIDs.get(projectTextures.get(projectIdentifier));
                if(id != null && !ProjectHelper.texturesToClear.contains(projectTextures.get(projectIdentifier)))
                {
                    ProjectHelper.texturesToClear.add(projectTextures.get(projectIdentifier));
                }
            }
            projectTextures.remove(projectIdentifier);
            Tabula.proxy.updateProject(fromClient, projectIdentifier, true, updateDims);
        }
        else
        {
            HashMap<String, ArrayList<byte[]>> map = isTexture ? projectTextureParts : projectParts;
            ArrayList<byte[]> byteArray = map.get(projectIdentifier);
            if(byteArray == null)
            {
                byteArray = new ArrayList<>();

                map.put(projectIdentifier, byteArray);

                for(int i = 0; i < packetTotal; i++)
                {
                    byteArray.add(new byte[0]);
                }
            }

            byteArray.set(packetNumber, data);

            boolean hasAllInfo = true;

            for(byte[] byteList : byteArray)
            {
                if(byteList.length == 0)
                {
                    hasAllInfo = false;
                }
            }

            if(hasAllInfo)
            {
                int size = 0;

                for(byte[] aByteArray1 : byteArray)
                {
                    size += aByteArray1.length;
                }

                byte[] bytes = new byte[size];

                int index = 0;

                for(byte[] aByteArray : byteArray)
                {
                    System.arraycopy(aByteArray, 0, bytes, index, aByteArray.length);
                    index += aByteArray.length;
                }

                try
                {
                    if(isTexture)
                    {
                        InputStream is = new ByteArrayInputStream(bytes);
                        BufferedImage img = ImageIO.read(is);
                        if(projectTextures.get(projectIdentifier) == null)
                        {
                            flag = true;
                        }
                        else if(!IOUtil.areBufferedImagesEqual(projectTextures.get(projectIdentifier), img))
                        {
                            flag = true;
                            Integer id = ProjectHelper.projectTextureIDs.get(projectTextures.get(projectIdentifier));
                            if(id != null && !ProjectHelper.texturesToClear.contains(projectTextures.get(projectIdentifier)))
                            {
                                ProjectHelper.texturesToClear.add(projectTextures.get(projectIdentifier));
                            }
                        }
                        projectTextures.put(projectIdentifier, img);
                        if(projects.get(projectIdentifier) != null)
                        {
                            projects.get(projectIdentifier).bufferedTexture = img;
                        }
                        Tabula.proxy.updateProject(fromClient, projectIdentifier, true, updateDims);
                    }
                    else
                    {
                        String json = new String(bytes, "UTF-8");
                        ProjectInfo proj = createProjectFromJson(projectIdentifier, json);
                        if(projects.get(projectIdentifier) == null)
                        {
                            flag = true;
                        }
                        else if(!projects.get(projectIdentifier).getAsJson().equals(proj.getAsJson()))
                        {
                            flag = true;
                            if(!ProjectHelper.projectsToDestroy.contains(projects.get(projectIdentifier)))
                            {
                                ProjectHelper.projectsToDestroy.add(projects.get(projectIdentifier));
                            }
                        }
                        projects.put(projectIdentifier, proj);
                        proj.bufferedTexture = projectTextures.get(projectIdentifier);
                        Tabula.proxy.updateProject(fromClient, projectIdentifier, false, updateDims);
                    }
                }
                catch(IOException ignored)
                {
                }
                catch(Exception e)
                {
                    Tabula.LOGGER.warn("Error reading project sent through server!");
                    e.printStackTrace();
                }

                map.remove(projectIdentifier);
            }
        }
        return flag;
    }

    @SideOnly(Side.CLIENT)
    public static void receiveChat(String message)
    {
        Minecraft mc = Minecraft.getMinecraft();
        if(mc.currentScreen instanceof GuiWorkspace)
        {
            GuiWorkspace workspace = (GuiWorkspace)mc.currentScreen;
            workspace.windowChat.chatHolder.text.add(message);
            if(Tabula.config.chatSound == 1)
            {
                mc.getSoundHandler().playSound(PositionedSoundRecord.getMasterRecord(SoundEvents.ENTITY_ARROW_HIT_PLAYER, 1.0F));
            }
        }
    }

    @SideOnly(Side.CLIENT)
    public static void addSystemMessage(String message)
    {
        receiveChat("System: " + message);
    }

    public static void sendProjectToServer(String host, String ident, ProjectInfo proj, boolean isImport)
    {
        byte[] projBytes = proj.getAsJson().getBytes();
        byte[] imgBytes = new byte[0];
        if(proj.bufferedTexture != null)
        {
            try
            {
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                ImageIO.write(proj.bufferedTexture, "png", baos);
                imgBytes = baos.toByteArray();
            }
            catch(IOException ignored){}
        }

        byte[] collective = new byte[projBytes.length + imgBytes.length];

        System.arraycopy(projBytes, 0, collective, 0, projBytes.length);
        System.arraycopy(imgBytes, 0, collective, projBytes.length, imgBytes.length);

        final int maxFile = 31000; //smaller packet cause I'm worried about too much info carried over from the bloat vs hat info.

        int fileSize = collective.length;

        int packetsToSend = (int)Math.ceil((float)fileSize / (float)maxFile);

        int packetCount = 0;
        int offset = 0;
        while(fileSize > 0)
        {
            byte[] fileBytes = new byte[fileSize > maxFile ? maxFile : fileSize];
            int index = 0;
            while(index < fileBytes.length) //from index 0 to 31999
            {
                fileBytes[index] = collective[index + offset];
                index++;
            }

            Tabula.channel.sendToServer(new PacketProjectFragmentFromClient(host, ident, isImport, projBytes.length, packetsToSend, packetCount, fileSize > maxFile ? maxFile : fileSize, fileBytes));

            packetCount++;
            fileSize -= 32000;
            offset += index;
        }
    }

    public static void sendTextureToServer(String host, String projectIdent, boolean updateDims, BufferedImage image)
    {
        byte[] data = null;

        try
        {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(image, "png", baos);
            data = baos.toByteArray();
        }
        catch(IOException e){return;}

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
                x = workspace.hostX;
                y = workspace.hostY;
                z = workspace.hostZ;
            }

            Tabula.channel.sendToServer(new PacketProjectFragment(x, y, z, true, host, Minecraft.getMinecraft().getSession().getUsername(), projectIdent, true, updateDims, false, packetsToSend, packetCount, fileSize > maxFile ? maxFile : fileSize, fileBytes));

            packetCount++;
            fileSize -= 32000;
            offset += index;
        }
    }
}
