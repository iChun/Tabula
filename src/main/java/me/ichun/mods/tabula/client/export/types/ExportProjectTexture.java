package me.ichun.mods.tabula.client.export.types;

import me.ichun.mods.ichunutil.common.module.tabula.formats.types.Exporter;
import me.ichun.mods.ichunutil.common.module.tabula.project.ProjectInfo;
import me.ichun.mods.tabula.client.core.ResourceHelper;

import javax.imageio.ImageIO;
import java.io.File;
import java.io.IOException;

public class ExportProjectTexture extends Exporter
{
    public ExportProjectTexture()
    {
        super("export.projTexture.name");
    }

    @Override
    public boolean export(ProjectInfo info, Object... params)
    {
        if(info.bufferedTexture == null)
        {
            return false;
        }

        File file = new File(ResourceHelper.getExportsDir(), info.modelName + "-texture.png");

        try
        {
            ImageIO.write(info.bufferedTexture, "png", file);
            return true;
        }
        catch (IOException ioexception)
        {
            ioexception.printStackTrace();
            return false;
        }
    }

    @Override
    public boolean localizable()
    {
        return true;
    }
}
