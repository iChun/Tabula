package us.ichun.mods.tabula.client.export.types;

import us.ichun.mods.ichunutil.common.module.tabula.common.project.ProjectInfo;
import us.ichun.mods.tabula.client.gui.GuiWorkspace;

public abstract class Exporter
{
    public final String name;

    public Exporter(String name)
    {
        this.name = name;
    }

    public abstract boolean export(ProjectInfo info, Object...params);

    public boolean override(GuiWorkspace workspace)
    {
        return false;
    }
}
