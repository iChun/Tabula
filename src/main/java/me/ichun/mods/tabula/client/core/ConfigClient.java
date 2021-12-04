package me.ichun.mods.tabula.client.core;

import me.ichun.mods.ichunutil.common.config.ConfigBase;
import me.ichun.mods.ichunutil.common.config.annotations.CategoryDivider;
import me.ichun.mods.ichunutil.common.config.annotations.Prop;
import me.ichun.mods.tabula.common.Tabula;
import net.minecraftforge.fml.config.ModConfig;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;

public class ConfigClient extends ConfigBase
{
    @CategoryDivider(name = "clientOnly")
    @Prop(min = -1, max = 8)
    public int forceGuiScale = 2;

    public boolean animateImports = true;

    public boolean addEntityTags = true;

    public boolean readEmptyNbt = true;

    @Prop(min = 0, max = 10)
    public int guiMaxDecimals = 2;

    public boolean renderWorkspaceBlock = true;

    public boolean renderWorkspaceGrid = true;

    public double workspaceGridSize = 7.25D;

    public boolean ignoreOldTabulaWarning = false;

    public boolean disableAutosaves = false;

    @Prop(min = 0, max = 200)
    public int maximumUndoStates = 40;

    @CategoryDivider(name = "multiplayer")
    public boolean chatSound = true;

    public boolean allowEveryoneToEdit = true;

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

    @Nonnull
    @Override
    public ModConfig.Type getConfigType()
    {
        return ModConfig.Type.CLIENT;
    }
}
