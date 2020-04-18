package me.ichun.mods.tabula.client.export;

import me.ichun.mods.ichunutil.common.module.tabula.formats.types.Exporter;
import me.ichun.mods.tabula.client.export.types.ExportBlockJson;
import me.ichun.mods.tabula.client.export.types.ExportJava;
import me.ichun.mods.tabula.client.export.types.ExportProjectTexture;
import me.ichun.mods.tabula.client.export.types.ExportTextureMap;

import java.util.HashMap;

public final class ExportList
{
    public static final HashMap<String, Exporter> EXPORTERS = new HashMap<String, Exporter>() {{
        put("textureMap", new ExportTextureMap());
        put("javaClass", new ExportJava());
        put("projectTexture", new ExportProjectTexture());
        put("blockJson", new ExportBlockJson());
    }};
}
