package us.ichun.mods.tabula.client.export.types;

import us.ichun.mods.ichunutil.client.gui.window.IWorkspace;
import us.ichun.mods.ichunutil.client.gui.window.element.IListable;
import us.ichun.mods.ichunutil.common.module.tabula.common.project.ProjectInfo;

public abstract class Exporter implements IListable
{
    public final String name;

    public Exporter(String name)
    {
        this.name = name;
    }

    public abstract boolean export(ProjectInfo info, Object...params);

    public boolean override(IWorkspace workspace)
    {
        return false;
    }

    @Override
    public String getName()
    {
        return name;
    }
}
