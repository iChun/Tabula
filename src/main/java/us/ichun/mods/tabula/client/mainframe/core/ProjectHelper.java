package us.ichun.mods.tabula.client.mainframe.core;

import com.google.gson.Gson;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import ichun.common.iChunUtil;
import net.minecraft.client.Minecraft;
import us.ichun.mods.tabula.common.Tabula;
import us.ichun.mods.tabula.common.project.ProjectInfo;
import us.ichun.mods.tabula.gui.GuiWorkspace;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class ProjectHelper
{
    public static ProjectInfo createProjectFromJson(String ident, String s)
    {
        Gson gson = new Gson();
        ProjectInfo info = gson.fromJson(s, ProjectInfo.class);
        info.identifier = ident;
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

    @SideOnly(Side.CLIENT)
    public static void receiveChat(String message)
    {
        Minecraft mc = Minecraft.getMinecraft();
        if(mc.currentScreen instanceof GuiWorkspace)
        {
            GuiWorkspace workspace = (GuiWorkspace)mc.currentScreen;
            workspace.windowChat.chatHolder.text.add(message);
            //TODO play a sound
        }
    }

    @SideOnly(Side.CLIENT)
    public static boolean saveProject(ProjectInfo info, File file)
    {
        try
        {
            file.getParentFile().mkdirs();

            ZipOutputStream out = new ZipOutputStream(new FileOutputStream(file));
            out.setLevel(9);
            out.putNextEntry(new ZipEntry("model.json"));

            byte[] data = (new Gson()).toJson(info).getBytes();
            out.write(data, 0, data.length);
            out.closeEntry();

            if(info.bufferedTexture != null)
            {
                out.putNextEntry(new ZipEntry("texture.png"));
                ImageIO.write(info.bufferedTexture, "png", out);
            }
            out.closeEntry();

            out.close();
            return true;
        }
        catch(Exception e)
        {
            Tabula.console("Failed to save model: " + info.modelName, true);
            e.printStackTrace();
            return false;
        }
    }

}
