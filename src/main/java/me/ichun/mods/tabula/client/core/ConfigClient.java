package me.ichun.mods.tabula.client.core;

import me.ichun.mods.ichunutil.common.config.ConfigBase;
import me.ichun.mods.ichunutil.common.config.annotations.CategoryDivider;
import me.ichun.mods.ichunutil.common.config.annotations.Prop;
import me.ichun.mods.tabula.common.Tabula;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;

public class ConfigClient extends ConfigBase
{
    @CategoryDivider(name = "clientOnly")
    @Prop(min = -1, max = 8)
    public int forceGuiScale = 2;

    @Prop
    public boolean animateImports = true;

    @Prop(min = 0, max = 10)
    public int guiMaxDecimals = 2;

    @Prop
    public boolean renderWorkspaceBlock = true;

    @Prop
    public boolean renderWorkspaceGrid = true;

    @Prop
    public double workspaceGridSize = 7.25D;

    @Prop
    public boolean ignoreOldTabulaWarning = false;

    @Prop
    public boolean disableAutosaves = true;

    @Prop(min = 0, max = 200)
    public int maximumUndoStates = 40;

    @CategoryDivider(name = "multiplayer")
    @Prop
    public boolean chatSound = true;

    @Prop
    public boolean allowEveryoneToEdit = true;

    @Prop
    public List<String> editors = new ArrayList<>();


    @Nonnull
    @Override
    public String getModId()
    {
        return Tabula.MOD_ID;
    }

    @Nonnull
    @Override
    public String getConfigName()
    {
        return Tabula.MOD_NAME;
    }
}
