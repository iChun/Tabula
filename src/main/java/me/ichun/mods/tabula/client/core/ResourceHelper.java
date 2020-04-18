package me.ichun.mods.tabula.client.core;

import me.ichun.mods.tabula.common.Tabula;
import net.minecraftforge.fml.loading.FMLPaths;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class ResourceHelper
{
    private static boolean init;

    private static Path workingDir;

    private static Path savesDir; //also used for imports
    private static Path texturesDir;
    private static Path exportsDir;
    private static Path autosaveDir;

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
}
