package us.ichun.mods.tabula.client.core;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.apache.commons.io.FileUtils;
import us.ichun.mods.tabula.client.gui.Theme;

import java.io.File;
import java.io.IOException;

public class ResourceHelper
{
    private static File workRoot;
    private static File saveDir;
    private static File autosaveDir;
    private static File texturesDir;
    private static File exportsDir;
    private static File themesDir;
    private static File configDir;
    //TODO do i need a configs dir?

    public static void init()
    {
        workRoot = new File(ichun.common.core.util.ResourceHelper.getModsFolder(), "tabula");
        saveDir = new File(workRoot, "saves");
        autosaveDir = new File(workRoot, "autosave");
        texturesDir = new File(workRoot, "textures");
        exportsDir = new File(workRoot, "export");
        themesDir = new File(workRoot, "themes");
        configDir = new File(workRoot, "config");

        saveDir.mkdirs();
        autosaveDir.mkdirs();
        texturesDir.mkdirs();
        exportsDir.mkdirs();
        themesDir.mkdirs();
        configDir.mkdirs();

        File defaultTheme = new File(themesDir, "default.json");
        if(!defaultTheme.exists())
        {
            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            String jsonOutput = gson.toJson(Theme.instance);

            try
            {
                FileUtils.writeStringToFile(defaultTheme, jsonOutput);
            }
            catch(IOException e)
            {
                e.printStackTrace();
            }
        }
    }

    public static File getWorkRoot()
    {
        return workRoot;
    }

    public static File getSaveDir()
    {
        return saveDir;
    }

    public static File getAutosaveDir()
    {
        return autosaveDir;
    }

    public static File getTexturesDir()
    {
        return texturesDir;
    }

    public static File getExportsDir()
    {
        return exportsDir;
    }

    public static File getThemesDir()
    {
        return themesDir;
    }

    public static File getConfigDir()
    {
        return configDir;
    }

}
