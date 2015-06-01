package us.ichun.mods.tabula.client.export.types;

import us.ichun.mods.ichunutil.common.module.tabula.client.formats.types.Exporter;
import us.ichun.mods.ichunutil.common.module.tabula.common.project.ProjectInfo;
import us.ichun.mods.ichunutil.common.module.tabula.common.project.components.CubeInfo;
import us.ichun.mods.tabula.client.core.ResourceHelper;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

public class ExportTextureMap extends Exporter
{
    public ExportTextureMap()
    {
        super("export.textureMap.name");
    }

    @Override
    public boolean export(ProjectInfo info, Object... params)
    {
        File file = new File(ResourceHelper.getExportsDir(), info.modelName + "-texturemap.png");

        BufferedImage tmp = new BufferedImage(info.textureWidth, info.textureHeight, BufferedImage.TYPE_INT_ARGB);

        ArrayList<CubeInfo> cubes = info.getAllCubes();

        for(CubeInfo cube : cubes)
        {
            for(int i = 0; i < cube.dimensions[0] || i == 0 && cube.dimensions[0] == 0; i++)
            {
                for(int j = 0; j < cube.dimensions[1] || j == 0 && cube.dimensions[1] == 0; j++)
                {
                    for(int k = 0; k < cube.dimensions[2] || k == 0 && cube.dimensions[2] == 0; k++)
                    {
                        if(withinBounds(tmp, cube.txOffset[0] + k, cube.txOffset[1] + cube.dimensions[2] + j) && cube.dimensions[2] > 0 && cube.dimensions[1] > 0)
                        {
                            tmp.setRGB(cube.txOffset[0] + k, cube.txOffset[1] + cube.dimensions[2] + j, 0xffff0000); //red
                        }
                        if(withinBounds(tmp, cube.txOffset[0] + cube.dimensions[2] + i, cube.txOffset[1] + cube.dimensions[2] + j) && cube.dimensions[0] > 0 && cube.dimensions[1] > 0)
                        {
                            tmp.setRGB(cube.txOffset[0] + cube.dimensions[2] + i, cube.txOffset[1] + cube.dimensions[2] + j, 0xff0000ff); //blue
                        }
                        if(withinBounds(tmp, cube.txOffset[0] + cube.dimensions[2] + cube.dimensions[0] + k, cube.txOffset[1] + cube.dimensions[2] + j) && cube.dimensions[2] > 0 && cube.dimensions[1] > 0)
                        {
                            tmp.setRGB(cube.txOffset[0] + cube.dimensions[2] + cube.dimensions[0] + k, cube.txOffset[1] + cube.dimensions[2] + j, 0xffaa0000); //dark red
                        }
                        if(withinBounds(tmp, cube.txOffset[0] + cube.dimensions[2] + cube.dimensions[0] + cube.dimensions[2] + i, cube.txOffset[1] + cube.dimensions[2] + j) && cube.dimensions[0] > 0 && cube.dimensions[1] > 0)
                        {
                            tmp.setRGB(cube.txOffset[0] + cube.dimensions[2] + cube.dimensions[0] + cube.dimensions[2] + i, cube.txOffset[1] + cube.dimensions[2] + j, 0xff0000aa); //dark blue
                        }
                        if(withinBounds(tmp, cube.txOffset[0] + cube.dimensions[2] + i, cube.txOffset[1] + k) && cube.dimensions[0] > 0 && cube.dimensions[2] > 0)
                        {
                            tmp.setRGB(cube.txOffset[0] + cube.dimensions[2] + i, cube.txOffset[1] + k, 0xff00ff00); //green
                        }
                        if(withinBounds(tmp, cube.txOffset[0] + cube.dimensions[2] + cube.dimensions[0] + i, cube.txOffset[1] + k) && cube.dimensions[0] > 0 && cube.dimensions[2] > 0)
                        {
                            tmp.setRGB(cube.txOffset[0] + cube.dimensions[2] + cube.dimensions[0] + i, cube.txOffset[1] + k, 0xff00aa00); //dark green
                        }
                    }
                }
            }
            //            RendererHelper.drawColourOnScreen(255, 0, 0, alpha, cube.txOffset[0], cube.txOffset[1] + cube.dimensions[2], cube.dimensions[2], cube.dimensions[1], 0D);
            //            RendererHelper.drawColourOnScreen(0, 0, 255, alpha, cube.txOffset[0] + cube.dimensions[2], cube.txOffset[1] + cube.dimensions[2], cube.dimensions[0], cube.dimensions[1], 0D);
            //            RendererHelper.drawColourOnScreen(170, 0, 0, alpha, cube.txOffset[0] + cube.dimensions[2] + cube.dimensions[0], cube.txOffset[1] + cube.dimensions[2], cube.dimensions[2], cube.dimensions[1], 0D);
            //            RendererHelper.drawColourOnScreen(0, 0, 170, alpha, cube.txOffset[0] + cube.dimensions[2] + cube.dimensions[0] + cube.dimensions[2], cube.txOffset[1] + cube.dimensions[2], cube.dimensions[0], cube.dimensions[1], 0D);
            //            RendererHelper.drawColourOnScreen(0, 255, 0, alpha, cube.txOffset[0] + cube.dimensions[2], cube.txOffset[1], cube.dimensions[0], cube.dimensions[2], 0D);
            //            RendererHelper.drawColourOnScreen(0, 170, 0, alpha, cube.txOffset[0] + cube.dimensions[2] + cube.dimensions[0], cube.txOffset[1], cube.dimensions[0], cube.dimensions[2], 0D);
        }

        try
        {
            ImageIO.write(tmp, "png", file);
            return true;
        }
        catch (IOException ioexception)
        {
            ioexception.printStackTrace();
            return false;
        }
    }

    public boolean withinBounds(BufferedImage img, int x, int y)
    {
        return x >= 0 && x < img.getWidth() && y >= 0 && y < img.getHeight();
    }
}
