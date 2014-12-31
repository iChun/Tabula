package us.ichun.mods.tabula.client.core;

import com.google.common.io.ByteStreams;
import com.google.gson.Gson;
import com.mojang.util.UUIDTypeAdapter;
import cpw.mods.fml.client.FMLClientHandler;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.PositionedSoundRecord;
import net.minecraft.client.gui.GuiMainMenu;
import net.minecraft.client.renderer.texture.TextureUtil;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.input.Keyboard;
import us.ichun.mods.tabula.client.gui.GuiWorkspace;
import us.ichun.mods.tabula.client.gui.Theme;
import us.ichun.mods.tabula.client.mainframe.Mainframe;
import us.ichun.mods.tabula.client.mainframe.core.ProjectHelper;
import us.ichun.mods.tabula.common.Tabula;
import us.ichun.module.tabula.common.project.ProjectInfo;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.*;

public class TickHandlerClient
{
    @SubscribeEvent
    public void onRenderTick(TickEvent.RenderTickEvent event)
    {
        if(event.phase == TickEvent.Phase.END)
        {
            Minecraft mc = Minecraft.getMinecraft();

            if(mc.currentScreen instanceof GuiMainMenu)
            {
                if(Keyboard.isKeyDown(Keyboard.KEY_T) && !keyTDown)
                {
                    initializeMainframe(null, -1, -1, -1);
                }
                keyTDown = Keyboard.isKeyDown(Keyboard.KEY_T);
            }
        }
    }

    @SubscribeEvent
    public void onClientTick(TickEvent.ClientTickEvent event)
    {
        if(event.phase == TickEvent.Phase.END)
        {
            if(mainframe != null)
            {
                mainframe.tick();
            }
            for(int i = ProjectHelper.texturesToClear.size() - 1; i >= 0; i--)
            {
                Integer id = ProjectHelper.projectTextureIDs.get(ProjectHelper.texturesToClear.get(i));
                if(id != null)
                {
                    TextureUtil.deleteTexture(id);
                }
                ProjectHelper.texturesToClear.remove(i);
            }
            for(int i = ProjectHelper.projectsToDestroy.size() - 1; i >= 0; i--)
            {
                ProjectHelper.projectsToDestroy.get(i).destroy();
                ProjectHelper.projectsToDestroy.remove(i);
            }
            Iterator<Map.Entry<String, ProjectInfo>> ite = projectsToUpdate.entrySet().iterator();
            for(Map.Entry<String, ProjectInfo> e : projectsToUpdate.entrySet())
            {
                ProjectHelper.addProjectToManager(ProjectHelper.createProjectFromJsonHost(e.getKey(), e.getValue().getAsJson()));
            }
            for(Map.Entry<String, BufferedImage> e : projectImagesToUpdate.entrySet())
            {
                ProjectHelper.updateProjectTexture(e.getKey(), e.getValue());
            }
            projectsToUpdate.clear();
            projectImagesToUpdate.clear();
        }
    }

    public void initializeMainframe(String name, int i, int j, int k)
    {
        Minecraft mc = Minecraft.getMinecraft();
        mc.getSoundHandler().playSound(PositionedSoundRecord.func_147674_a(new ResourceLocation("gui.button.press"), 1.0F));

        int oriScale = mc.gameSettings.guiScale;
        mc.gameSettings.guiScale = mc.gameSettings.guiScale == 1 ? 1 : 2;
        mainframe = new Mainframe();
        UUID uuid;
        try
        {
            uuid = UUIDTypeAdapter.fromString(mc.getSession().getPlayerID());
        }
        catch(IllegalArgumentException e)
        {
            uuid = UUIDTypeAdapter.fromString("deadbeef-dead-beef-dead-beefdeadbeef");
        }
        mainframe.addListener(mc.getSession().getUsername(), true);

        File defaultTheme = new File(ResourceHelper.getThemesDir(), "default.json");

        try
        {
            InputStream con = new FileInputStream(defaultTheme);
            String data = new String(ByteStreams.toByteArray(con));
            con.close();

            Theme.loadTheme((new Gson()).fromJson(data, Theme.class));
        }
        catch(Exception e)
        {
            Tabula.console("Error reading default theme!", true);
            e.printStackTrace();
        }

        String fav = Tabula.config.getString("favTheme");
        if(!(fav.isEmpty() || fav.equalsIgnoreCase("default")))
        {
            File userTheme = new File(ResourceHelper.getThemesDir(), fav + ".json");

            if(userTheme.exists())
            {
                try
                {
                    InputStream con = new FileInputStream(userTheme);
                    String data = new String(ByteStreams.toByteArray(con));
                    con.close();

                    Theme.loadTheme((new Gson()).fromJson(data, Theme.class));
                }
                catch(Exception e)
                {
                    Tabula.console("Error reading preferred theme!", true);
                    e.printStackTrace();
                }
            }
            else
            {
                Tabula.console("Preferred theme file does not exist!", true);
            }
        }

        FMLClientHandler.instance().showGuiScreen(new GuiWorkspace(oriScale, false, true, name, i, j, k));
    }

    public HashMap<String, ProjectInfo> projectsToUpdate = new HashMap<String, ProjectInfo>();
    public HashMap<String, BufferedImage> projectImagesToUpdate = new HashMap<String, BufferedImage>();

    public Mainframe mainframe;

    private boolean keyTDown;
}
