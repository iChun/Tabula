package us.ichun.mods.tabula.client.export;

import us.ichun.mods.tabula.client.export.types.*;

import java.util.ArrayList;

public final class ExportList
{
    public static final ArrayList<Exporter> exportTypes = new ArrayList<Exporter>() {{
        add(new ExportTextureMap());
        add(new ExportJava());
        add(new ExportScala());
        add(new ExportProjectTexture());
    }};
}
