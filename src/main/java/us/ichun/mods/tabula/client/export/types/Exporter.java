package us.ichun.mods.tabula.client.export.types;

import us.ichun.module.tabula.common.project.ProjectInfo;

public abstract class Exporter
{
    public final String name;

    public Exporter(String name)
    {
        this.name = name;
    }

    public abstract boolean export(ProjectInfo info, Object...params);
}
