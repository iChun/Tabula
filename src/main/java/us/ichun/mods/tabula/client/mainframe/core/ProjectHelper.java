package us.ichun.mods.tabula.client.mainframe.core;

import com.google.gson.Gson;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import ichun.common.core.util.IOUtil;
import ichun.common.core.util.MD5Checksum;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.PositionedSoundRecord;
import net.minecraft.client.renderer.texture.TextureUtil;
import net.minecraft.util.ResourceLocation;
import org.apache.commons.lang3.ArrayUtils;
import us.ichun.mods.tabula.client.gui.GuiWorkspace;
import us.ichun.mods.tabula.client.gui.window.Window;
import us.ichun.mods.tabula.client.gui.window.WindowOpenProject;
import us.ichun.mods.tabula.common.Tabula;
import us.ichun.module.tabula.common.project.ProjectInfo;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

public class ProjectHelper
{
    public static HashMap<String, ArrayList<byte[]>> projectParts = new HashMap<String, ArrayList<byte[]>>();
    public static HashMap<String, ArrayList<byte[]>> projectTextureParts = new HashMap<String, ArrayList<byte[]>>();

    public static HashMap<String, ProjectInfo> projects = new HashMap<String, ProjectInfo>();
    public static HashMap<String, BufferedImage> projectTextures = new HashMap<String, BufferedImage>();
    public static HashMap<BufferedImage, Integer> projectTextureIDs = new HashMap<BufferedImage, Integer>();

    public static ArrayList<BufferedImage> texturesToClear = new ArrayList<BufferedImage>();
    public static ArrayList<ProjectInfo> projectsToDestroy = new ArrayList<ProjectInfo>();

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
                            info.saveFileMd5 = MD5Checksum.getMD5Checksum(info.saveFile);
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

    public static boolean receiveProjectData(String projectIdentifier, boolean isTexture, byte packetTotal, byte packetNumber, byte[] data) // return true if only the project data is different?
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
            Tabula.proxy.updateProject(projectIdentifier, true);
        }
        else
        {
            HashMap<String, ArrayList<byte[]>> map = isTexture ? projectTextureParts : projectParts;
            ArrayList<byte[]> byteArray = map.get(projectIdentifier);
            if(byteArray == null)
            {
                byteArray = new ArrayList<byte[]>();

                map.put(projectIdentifier, byteArray);

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
                        Tabula.proxy.updateProject(projectIdentifier, true);
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
                        Tabula.proxy.updateProject(projectIdentifier, false);
                    }
                }
                catch(IOException ignored)
                {
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
            if(Tabula.config.getInt("chatSound") == 1)
            {
                mc.getSoundHandler().playSound(PositionedSoundRecord.func_147674_a(new ResourceLocation("random.successful_hit"), 1.0F));
            }
        }
    }

    @SideOnly(Side.CLIENT)
    public static void addSystemMessage(String message)
    {
        receiveChat("System: " + message);
    }
}
