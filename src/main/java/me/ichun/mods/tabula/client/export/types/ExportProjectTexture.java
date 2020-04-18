package me.ichun.mods.tabula.client.export.types;

import me.ichun.mods.ichunutil.common.module.tabula.formats.types.Exporter;
import me.ichun.mods.ichunutil.common.module.tabula.project.Project;
import me.ichun.mods.tabula.client.core.ResourceHelper;
import net.minecraft.client.resources.I18n;

import javax.imageio.ImageIO;
import java.io.File;
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
        if(project.getBufferedTexture() == null)
        {
            return false;
        }

        File file = new File(ResourceHelper.getExportsDir().toFile(), project.name + "-texture.png");

        try
        {
            ImageIO.write(project.getBufferedTexture(), "png", file);
            return true;
        }
        catch (IOException ioexception)
        {
            ioexception.printStackTrace();
            return false;
        }
    }
}
