package me.ichun.mods.tabula.client.export.types;

import me.ichun.mods.ichunutil.common.module.tabula.formats.types.Exporter;
import me.ichun.mods.ichunutil.common.module.tabula.project.Project;
import me.ichun.mods.tabula.client.core.ResourceHelper;
import net.minecraft.client.resources.I18n;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class ExportProjectTexture extends Exporter
{
    public ExportProjectTexture()
    {
        super(I18n.format("export.projTexture.name"));
    }

    @Override
    public String getId()
    {
        return "projectTexture";
    }

    @Override
    public boolean export(Project project, Object... params)
    {
        if(project.getTextureBytes() == null)
        {
            return false;
        }

        File file = new File(ResourceHelper.getExportsDir().toFile(), project.name + "-texture.png");

        try
        {
            FileOutputStream stream = new FileOutputStream(file);
            stream.write(project.getTextureBytes());
            stream.close();
            return true;
        }
        catch (IOException ioexception)
        {
            ioexception.printStackTrace();
            return false;
        }
    }
}
