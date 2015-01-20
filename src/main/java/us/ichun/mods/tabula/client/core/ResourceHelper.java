package us.ichun.mods.tabula.client.core;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.apache.commons.io.FileUtils;
import us.ichun.mods.tabula.client.gui.Theme;
import us.ichun.mods.tabula.common.Tabula;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class ResourceHelper
{
    private static File workRoot;
    private static File saveDir;
    private static File autosaveDir;
    private static File texturesDir;
    private static File exportsDir;
    private static File themesDir;
    private static File configDir;

    public static void init()
    {
        workRoot = new File(us.ichun.mods.ichunutil.common.core.util.ResourceHelper.getModsFolder(), "tabula");
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
            String jsonOutput = gson.toJson(new Theme());

            try
            {
                FileUtils.writeStringToFile(defaultTheme, jsonOutput);
            }
            catch(IOException e)
            {
                e.printStackTrace();
            }
        }

        try
        {
            InputStream in = Tabula.class.getResourceAsStream("/themes.zip");
            if(in != null)
            {
                ZipInputStream zipStream = new ZipInputStream(in);
                ZipEntry entry = null;

                while((entry = zipStream.getNextEntry()) != null)
                {
                    File file = new File(themesDir, entry.getName());
                    if(file.exists() && file.length() > 3L)
                    {
                        continue;
                    }
                    FileOutputStream out = new FileOutputStream(file);

                    byte[] buffer = new byte[8192];
                    int len;
                    while((len = zipStream.read(buffer)) != -1)
                    {
                        out.write(buffer, 0, len);
                    }
                    out.close();
                }
                zipStream.close();
            }
        }
        catch(IOException e)
        {
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
