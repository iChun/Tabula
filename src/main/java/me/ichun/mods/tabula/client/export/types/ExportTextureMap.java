package me.ichun.mods.tabula.client.export.types;

import me.ichun.mods.ichunutil.common.module.tabula.formats.types.Exporter;
import me.ichun.mods.ichunutil.common.module.tabula.project.Project;
import me.ichun.mods.tabula.client.core.ResourceHelper;
import net.minecraft.client.renderer.texture.NativeImage;
import net.minecraft.client.resources.I18n;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

public class ExportTextureMap extends Exporter
{
    public ExportTextureMap()
    {
        super(I18n.format("export.textureMap.name"));
    }

    @Override
    public String getId()
    {
        return "textureMap";
    }

    @Override
    public boolean export(Project project, Object... params)
    {
        File file = new File(ResourceHelper.getExportsDir().toFile(), project.name + "-texturemap.png");

        NativeImage tmp = new NativeImage(project.texWidth, project.texHeight, true);

        ArrayList<Project.Part.Box> cubes = project.getAllBoxes();

        for(Project.Part.Box cube : cubes)
        {
            for(int i = 0; i < (int)Math.ceil(cube.dimX) || i == 0 && (int)Math.ceil(cube.dimX) == 0; i++)
            {
                for(int j = 0; j < (int)Math.ceil(cube.dimY) || j == 0 && (int)Math.ceil(cube.dimY) == 0; j++)
                {
                    for(int k = 0; k < (int)Math.ceil(cube.dimZ) || k == 0 && (int)Math.ceil(cube.dimZ) == 0; k++)
                    {
                        if(withinBounds(tmp, (cube.texOffX + ((Project.Part)cube.parent).texOffX) + k, (cube.texOffY + ((Project.Part)cube.parent).texOffY) + (int)Math.ceil(cube.dimZ) + j) && (int)Math.ceil(cube.dimZ) > 0 && (int)Math.ceil(cube.dimY) > 0)
                        {
                            tmp.setPixelRGBA((cube.texOffX + ((Project.Part)cube.parent).texOffX) + k, (cube.texOffY + ((Project.Part)cube.parent).texOffY) + (int)Math.ceil(cube.dimZ) + j, 0xffff0000); //red
                        }
                        if(withinBounds(tmp, (cube.texOffX + ((Project.Part)cube.parent).texOffX) + (int)Math.ceil(cube.dimZ) + i, (cube.texOffY + ((Project.Part)cube.parent).texOffY) + (int)Math.ceil(cube.dimZ) + j) && (int)Math.ceil(cube.dimX) > 0 && (int)Math.ceil(cube.dimY) > 0)
                        {
                            tmp.setPixelRGBA((cube.texOffX + ((Project.Part)cube.parent).texOffX) + (int)Math.ceil(cube.dimZ) + i, (cube.texOffY + ((Project.Part)cube.parent).texOffY) + (int)Math.ceil(cube.dimZ) + j, 0xff0000ff); //blue
                        }
                        if(withinBounds(tmp, (cube.texOffX + ((Project.Part)cube.parent).texOffX) + (int)Math.ceil(cube.dimZ) + (int)Math.ceil(cube.dimX) + k, (cube.texOffY + ((Project.Part)cube.parent).texOffY) + (int)Math.ceil(cube.dimZ) + j) && (int)Math.ceil(cube.dimZ) > 0 && (int)Math.ceil(cube.dimY) > 0)
                        {
                            tmp.setPixelRGBA((cube.texOffX + ((Project.Part)cube.parent).texOffX) + (int)Math.ceil(cube.dimZ) + (int)Math.ceil(cube.dimX) + k, (cube.texOffY + ((Project.Part)cube.parent).texOffY) + (int)Math.ceil(cube.dimZ) + j, 0xffaa0000); //dark red
                        }
                        if(withinBounds(tmp, (cube.texOffX + ((Project.Part)cube.parent).texOffX) + (int)Math.ceil(cube.dimZ) + (int)Math.ceil(cube.dimX) + (int)Math.ceil(cube.dimZ) + i, (cube.texOffY + ((Project.Part)cube.parent).texOffY) + (int)Math.ceil(cube.dimZ) + j) && (int)Math.ceil(cube.dimX) > 0 && (int)Math.ceil(cube.dimY) > 0)
                        {
                            tmp.setPixelRGBA((cube.texOffX + ((Project.Part)cube.parent).texOffX) + (int)Math.ceil(cube.dimZ) + (int)Math.ceil(cube.dimX) + (int)Math.ceil(cube.dimZ) + i, (cube.texOffY + ((Project.Part)cube.parent).texOffY) + (int)Math.ceil(cube.dimZ) + j, 0xff0000aa); //dark blue
                        }
                        if(withinBounds(tmp, (cube.texOffX + ((Project.Part)cube.parent).texOffX) + (int)Math.ceil(cube.dimZ) + i, (cube.texOffY + ((Project.Part)cube.parent).texOffY) + k) && (int)Math.ceil(cube.dimX) > 0 && (int)Math.ceil(cube.dimZ) > 0)
                        {
                            tmp.setPixelRGBA((cube.texOffX + ((Project.Part)cube.parent).texOffX) + (int)Math.ceil(cube.dimZ) + i, (cube.texOffY + ((Project.Part)cube.parent).texOffY) + k, 0xff00ff00); //green
                        }
                        if(withinBounds(tmp, (cube.texOffX + ((Project.Part)cube.parent).texOffX) + (int)Math.ceil(cube.dimZ) + (int)Math.ceil(cube.dimX) + i, (cube.texOffY + ((Project.Part)cube.parent).texOffY) + k) && (int)Math.ceil(cube.dimX) > 0 && (int)Math.ceil(cube.dimZ) > 0)
                        {
                            tmp.setPixelRGBA((cube.texOffX + ((Project.Part)cube.parent).texOffX) + (int)Math.ceil(cube.dimZ) + (int)Math.ceil(cube.dimX) + i, (cube.texOffY + ((Project.Part)cube.parent).texOffY) + k, 0xff00aa00); //dark green
                        }
                    }
                }
            }
        }

        try
        {
            tmp.write(file);
            return true;
        }
        catch (IOException ioexception)
        {
            ioexception.printStackTrace();
            return false;
        }
        finally
        {
            tmp.close();
        }
    }

    public boolean withinBounds(NativeImage img, int x, int y)
    {
        return x >= 0 && x < img.getWidth() && y >= 0 && y < img.getHeight();
    }
}
