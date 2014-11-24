package us.ichun.mods.tabula.client.export.types;

import us.ichun.mods.tabula.client.core.ResourceHelper;
import us.ichun.module.tabula.common.project.ProjectInfo;
import us.ichun.module.tabula.common.project.components.CubeInfo;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

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
}
