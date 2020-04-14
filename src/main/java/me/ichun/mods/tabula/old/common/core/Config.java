package me.ichun.mods.tabula.old.common.core;

import me.ichun.mods.ichunutil.common.core.config.ConfigBase;
import me.ichun.mods.ichunutil.common.core.config.annotations.ConfigProp;
import me.ichun.mods.ichunutil.common.core.config.annotations.IntBool;
import me.ichun.mods.ichunutil.common.core.config.annotations.IntMinMax;
import me.ichun.mods.tabula.old.common.Tabula;

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
    public int swapPositionOffset = 0;

    @ConfigProp
    @IntBool
    public int animateImports = 1;

    @ConfigProp
    @IntMinMax(min = 0)
    public int tooltipTime = 20;

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

    public Config(File file)
    {
        super(file);
    }

    @Override
    public String getModId()
    {
        return Tabula.MOD_ID;
    }

    @Override
    public String getModName()
    {
        return "Tabula";
    }
}
