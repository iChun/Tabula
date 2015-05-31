package us.ichun.mods.tabula.client.export;

import us.ichun.mods.tabula.client.export.types.ExportJava;
import us.ichun.mods.tabula.client.export.types.ExportProjectTexture;
import us.ichun.mods.tabula.client.export.types.ExportTextureMap;
import us.ichun.mods.ichunutil.common.module.tabula.client.formats.types.Exporter;

import java.util.ArrayList;

public final class ExportList
{
    public static final ArrayList<Exporter> exportTypes = new ArrayList<Exporter>() {{
        add(new ExportTextureMap());
        add(new ExportJava());
        add(new ExportProjectTexture());
    }};
}
