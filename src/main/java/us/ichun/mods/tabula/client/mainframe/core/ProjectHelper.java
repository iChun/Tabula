package us.ichun.mods.tabula.client.mainframe.core;

import com.google.gson.Gson;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.Minecraft;
import us.ichun.mods.tabula.common.project.ProjectInfo;
import us.ichun.mods.tabula.gui.GuiWorkspace;

import java.awt.image.BufferedImage;

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
                    project.bufferedTexture = image;
                }
            }
        }
    }
}
