package me.ichun.mods.tabula.client.core;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import me.ichun.mods.ichunutil.client.gui.bns.Theme;
import me.ichun.mods.tabula.common.Tabula;
import net.minecraftforge.fml.loading.FMLPaths;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class ResourceHelper
{
    private static boolean init;

    private static Path workingDir;

    private static Path savesDir; //also used for imports
    private static Path texturesDir;
    private static Path exportsDir;
    private static Path autosaveDir;
    private static Path themesDir;

    public static void init()
    {
        if(!init)
        {
            init = true;

            try
            {
                workingDir = FMLPaths.MODSDIR.get().resolve(Tabula.MOD_ID);
                if(!Files.exists(workingDir)) Files.createDirectory(workingDir);

                savesDir = workingDir.resolve("saves");
                if(!Files.exists(savesDir)) Files.createDirectory(savesDir);

                texturesDir = workingDir.resolve("textures");
                if(!Files.exists(texturesDir)) Files.createDirectory(texturesDir);

                exportsDir = workingDir.resolve("export");
                if(!Files.exists(exportsDir)) Files.createDirectory(exportsDir);

                autosaveDir = workingDir.resolve("autosave");
                if(!Files.exists(autosaveDir)) Files.createDirectory(autosaveDir);

                themesDir = workingDir.resolve("themes");
                if(!Files.exists(themesDir)) Files.createDirectory(themesDir);

                //TODO move themes to iChunUtil?
                File defaultTheme = new File(themesDir.toFile(), "default.json");
                if(!defaultTheme.exists())
                {
                    Gson gson = new GsonBuilder().setPrettyPrinting().create();
                    String jsonOutput = gson.toJson(new Theme());

                    try
                    {
                        FileUtils.writeStringToFile(defaultTheme, jsonOutput, StandardCharsets.UTF_8);
                    }
                    catch(IOException e)
                    {
                        e.printStackTrace();
                    }
                }

                InputStream in = Tabula.class.getResourceAsStream("/themes.zip");
                if(in != null)
                {
                    ZipInputStream zipStream = new ZipInputStream(in);
                    ZipEntry entry = null;

                    while((entry = zipStream.getNextEntry()) != null)
                    {
                        File file = new File(themesDir.toFile(), entry.getName());
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
                Tabula.LOGGER.fatal("Error initialising resources!");
                e.printStackTrace();
            }
        }
    }

    public static Path getWorkingDir()
    {
        return workingDir;
    }

    public static Path getSavesDir()
    {
        return savesDir;
    }

    public static Path getTexturesDir()
    {
        return texturesDir;
    }

    public static Path getExportsDir()
    {
        return exportsDir;
    }

    public static Path getAutosaveDir()
    {
        return autosaveDir;
    }

    public static Path getThemesDir()
    {
        return themesDir;
    }
}
