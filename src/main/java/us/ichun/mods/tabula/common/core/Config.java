package us.ichun.mods.tabula.common.core;

import us.ichun.mods.ichunutil.common.core.config.ConfigBase;
import us.ichun.mods.ichunutil.common.core.config.annotations.ConfigProp;
import us.ichun.mods.ichunutil.common.core.config.annotations.IntBool;

import java.io.File;

public class Config extends ConfigBase
{
    @ConfigProp
    public String favTheme = "default";

    @ConfigProp
    @IntBool
    public int renderRotationPoint = 1;

    @ConfigProp
    @IntBool
    public int renderWorkspaceBlock = 1;

    @ConfigProp
    @IntBool
    public int renderGrid = 1;

    @ConfigProp
    @IntBool
    public int renderModelControls = 1;

    @ConfigProp
    @IntBool
    public int animateImports = 1;

    @ConfigProp(category = "multiplayer")
    @IntBool
    public int chatSound = 1;

    @ConfigProp(category = "multiplayer")
    @IntBool
    public int allowEveryoneToEdit = 1;

    @ConfigProp(category = "multiplayer")
    public String editors = "";

    @ConfigProp(category = "others")
    public String chatWindow = "";

    @ConfigProp(category = "others", comment = "Temporary setting", nameOverride = "Animation Warning")
    @IntBool
    public int animationWarning = 0;

    public Config(File file, String... unhide)
    {
        super(file, unhide);
    }

    @Override
    public String getModId()
    {
        return "tabula";
    }

    @Override
    public String getModName()
    {
        return "Tabula";
    }
}
