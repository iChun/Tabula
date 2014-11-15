package us.ichun.mods.tabula.common.project.components;

import java.util.ArrayList;

public class CubeInfo
{
    public String name;
    public int[] dimensions = new int[3];
    public double[] txOffset = new double[2];

    public double[] position = new double[3];
    public double[] offset = new double[3];
    public double[] rotation = new double[3];

    public double[] scale = new double[3];

    public ArrayList<CubeInfo> children = new ArrayList<CubeInfo>();
}
