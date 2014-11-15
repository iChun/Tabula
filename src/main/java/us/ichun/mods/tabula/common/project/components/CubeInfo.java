package us.ichun.mods.tabula.common.project.components;

import net.minecraft.client.model.ModelRenderer;

import java.util.ArrayList;

public class CubeInfo
{
    public CubeInfo(String name)
    {
        this.name = name;
        dimensions = new int[] { 1, 1, 1 };
    }

    public String name;
    public int[] dimensions = new int[3];

    public double[] position = new double[3];
    public double[] offset = new double[3];
    public double[] rotation = new double[3];

    public double[] scale = new double[3];

    public double[] txOffset = new double[2];
    public boolean txMirror = false;

    public ArrayList<CubeInfo> children = new ArrayList<CubeInfo>();

    public transient ModelRenderer modelCube;
}
