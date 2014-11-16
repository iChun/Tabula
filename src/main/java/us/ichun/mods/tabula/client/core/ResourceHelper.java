package us.ichun.mods.tabula.client.core;

import java.io.File;

public class ResourceHelper
{
    private static File workRoot;
    private static File saveDir;
    private static File autosaveDir;
    private static File texturesDir;
    private static File exportsDir;

    public static void init()
    {
        workRoot = new File(ichun.common.core.util.ResourceHelper.getModsFolder(), "tabula");
        saveDir = new File(workRoot, "saves");
        autosaveDir = new File(workRoot, "autosave");
        texturesDir = new File(workRoot, "textures");
        exportsDir = new File(workRoot, "export");

        saveDir.mkdirs();
        autosaveDir.mkdirs();
        texturesDir.mkdirs();
        exportsDir.mkdirs();
    }
}
