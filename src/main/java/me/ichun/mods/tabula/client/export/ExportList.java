package me.ichun.mods.tabula.client.export;

import me.ichun.mods.ichunutil.common.module.tabula.formats.types.Exporter;
import me.ichun.mods.tabula.client.export.types.*;

import java.util.Comparator;
import java.util.TreeMap;

public final class ExportList
{
    public static final TreeMap<String, Exporter> EXPORTERS = new TreeMap<String, Exporter>(Comparator.naturalOrder()) {{
        put("blockJson", new ExportBlockJson());
        put("handInfo", new ExportHandInfo());
        put("headInfo", new ExportHeadInfo());
        put("javaClass", new ExportJava());
        put("projectTexture", new ExportProjectTexture());
        put("textureMap", new ExportTextureMap());
    }};
}
