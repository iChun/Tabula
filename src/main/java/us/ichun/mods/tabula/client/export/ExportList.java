package us.ichun.mods.tabula.client.export;

import us.ichun.mods.tabula.client.export.types.ExportTextureMap;
import us.ichun.mods.tabula.client.export.types.Exporter;

import java.util.ArrayList;

public final class ExportList
{
    public static ArrayList<Exporter> exportTypes = new ArrayList<Exporter>() {{
        add(new ExportTextureMap());
    }};
}
