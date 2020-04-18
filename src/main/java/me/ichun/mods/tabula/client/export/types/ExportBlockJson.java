package me.ichun.mods.tabula.client.export.types;

import me.ichun.mods.ichunutil.client.gui.bns.Workspace;
import me.ichun.mods.ichunutil.common.module.tabula.formats.types.Exporter;
import me.ichun.mods.ichunutil.common.module.tabula.project.Project;
import me.ichun.mods.tabula.client.core.ResourceHelper;
import me.ichun.mods.tabula.client.gui.WorkspaceTabula;
import me.ichun.mods.tabula.client.gui.window.popup.WindowExportBlockJson;
import me.ichun.mods.tabula.common.Tabula;
import net.minecraft.client.resources.I18n;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class ExportBlockJson extends Exporter
{
    private int textureWidth;
    private int textureHeight;

    public ExportBlockJson()
    {
        super(I18n.format("export.blockjson.name"));
        errors = new StringBuilder();
    }

    @Override
    public String getId()
    {
        return "blockJson";
    }


    public StringBuilder errors;

    @Override
    public boolean override(Workspace workspace, Project project)
    {
        workspace.openWindowInCenter(new WindowExportBlockJson(((WorkspaceTabula)workspace), project), 0.6D, 0.6D);
        return true;
    }

    @Override
    public boolean export(Project project, Object... params)
    {
        Object modid = params[0];
        Object filename = params[1];
        Object texturename = params[2];
        boolean cornerAtZero = (Boolean) params[3];
        boolean relativeToBlock = (Boolean) params[4];
        int xAdjust = cornerAtZero ? 0 : 8;
        int yAdjust = relativeToBlock ? 24 : 0;
        int zAdjust = cornerAtZero ? 0 : 8;
        this.textureWidth = project.texWidth;
        this.textureHeight = project.texHeight;

        File file = new File(ResourceHelper.getExportsDir().toFile(), filename + ".json");

        StringBuilder sb = new StringBuilder();
        errors = new StringBuilder();

        ArrayList<Project.Part.Box> allCubes = project.getAllBoxes();

        HashMap<Project.Part.Box, String> cubeFieldMap = new HashMap<>();

        sb.append("{   \"__comment\": " + "\"" + project.name + " - " + project.author + ", ");
        sb.append("created using Tabula " + Tabula.VERSION + "\",\n");
        sb.append("    \"parent\": \"minecraft:block/block\",\n");
        sb.append("    \"ambientocclusion\": false,\n");
        sb.append("    \"textures\": {\n");
        sb.append("        \"particle\": \"" + modid + ":blocks/" + texturename + "\"\n");
        sb.append("    },\n");
        sb.append("    \"elements\": [\n");
        for(Project.Part.Box cube : allCubes)
        {
            int count = 0;
            while(count == 0 && cubeFieldMap.containsValue(getFieldName(cube)) || count != 0 && cubeFieldMap.containsValue(getFieldName(cube) + "_" + count))
            {
                count++;
            }
            String fieldName = getFieldName(cube);
            if(count != 0)
            {
                fieldName = fieldName + "_" + count;
            }
            cubeFieldMap.put(cube, fieldName);
        }
        for(Map.Entry<Project.Part.Box, String> e : cubeFieldMap.entrySet())
        {
            Project.Part.Box cube = e.getKey();
            String field = e.getValue();
            Project.Part parent = (Project.Part)cube.parent;
            //Obtain parent cube if there is one
            Project.Part parentCube = cube.parent.parent instanceof Project.Part ? (Project.Part) cube.parent.parent : null;
            //Extract data for the JSON
            int x = (int) parent.rotPX;
            int y = (int) -parent.rotPY;
            int z = (int) parent.rotPZ;
            //Add x, y and z of parent if there is one
            if(parentCube != null)
            {
                x += (int) parentCube.rotPX;
                y -= (int) parentCube.rotPY;
                z += (int) parentCube.rotPZ;
            }
            int xLen = (int)cube.dimX;
            int yLen = (int)cube.dimY;
            int zLen = (int)cube.dimZ;
            //x + (int) cube.offset[0] outputs a mirror image on the z axis
            int xstart = x - xLen - (int) cube.posX + xAdjust;
            int ystart = y - yLen - (int) cube.posY + yAdjust;
            int zstart = z + (int) cube.posZ + zAdjust;
            int u = parent.texOffX + cube.texOffX;
            int v = parent.texOffY + cube.texOffY;
            String axis;
            double angle;
            //Validate data for the JSON
            this.rangeCheck(field, "cube start x", xstart, -16, 32);
            this.rangeCheck(field, "cube start y", ystart, -16, 32);
            this.rangeCheck(field, "cube start z", zstart, -16, 32);
            //Lengths don't need validating because they are always integers in Tabula
            this.rangeCheck(field, "cube end x", xstart + xLen, -16, 32);
            this.rangeCheck(field, "cube end y", ystart + yLen, -16, 32);
            this.rangeCheck(field, "cube end z", zstart + zLen, -16, 32);
            double rotX = parent.rotAX;
            double rotY = parent.rotAY;
            double rotZ = parent.rotAZ;
            //Add rotation of parent if there is one
            if(parentCube != null)
            {
                rotX += parentCube.rotAX;
                rotY += parentCube.rotAY;
                rotZ += parentCube.rotAZ;
            }
            axis = this.getAxis(field, rotX, rotY, rotZ);
            angle = this.getAngle(field, axis, rotX, rotY, rotZ);
            //Build the JSON. On the bottom face I had to invert the u for some reason. On the top face, the u and v.
            //I also had to mirror the x and y rotations.
            sb.append("        {   \"__comment\": \"" + field + "\",\n");
            sb.append("            \"from\": [ " + xstart + ", " + ystart + ", " + zstart + " ],\n");
            sb.append("            \"to\": [ " + (xstart + xLen) + ", " + (ystart + yLen) + ", " + (zstart + zLen) + " ],\n");
            sb.append("            \"rotation\": {\n");
            sb.append("                \"origin\": [ " + (x + xAdjust) + ", " + (y + yAdjust) + ", " + (z + zAdjust) + " ],\n");
            sb.append("                \"axis\": \"" + axis + "\",\n");
            sb.append("                \"angle\": " + angle + ",\n");
            sb.append("                \"rescale\": false\n");
            sb.append("            },\n");
            sb.append("            \"shade\": true,\n");
            sb.append("            \"faces\": {\n");
            sb.append("                \"down\":  { \"uv\": [ "  + toU(u + zLen + xLen + xLen) + ", " + toV(v)              + ", " + toU(u + zLen + xLen)               + ", " + toV(v + zLen)        + " ], \"texture\": \"#particle\" },\n");
            sb.append("                \"up\":  { \"uv\": [ "    + toU(u + zLen + xLen)        + ", " + toV(v + zLen) + ", " + toU(u + zLen)                      + ", " + toV(v)     + " ], \"texture\": \"#particle\" },\n");
            sb.append("                \"north\":  { \"uv\": [ " + toU(u + zLen)               + ", " + toV(v + zLen) + ", " + toU(u + zLen + xLen)               + ", " + toV(v + zLen + yLen) + " ], \"texture\": \"#particle\" },\n");
            sb.append("                \"south\":  { \"uv\": [ " + toU(u + zLen + xLen + zLen) + ", " + toV(v + zLen) + ", " + toU(u + zLen + xLen + zLen + xLen) + ", " + toV(v + zLen + yLen) + " ], \"texture\": \"#particle\" },\n");
            sb.append("                \"west\":  { \"uv\": [ "  + toU(u + zLen + xLen)        + ", " + toV(v + zLen) + ", " + toU(u + zLen + xLen + zLen)        + ", " + toV(v + zLen + yLen) + " ], \"texture\": \"#particle\" },\n");
            sb.append("                \"east\":  { \"uv\": [ "  + toU(u)                            + ", " + toV(v + zLen) + ", " + toU(u + zLen)                      + ", " + toV(v + zLen + yLen) + " ], \"texture\": \"#particle\" }\n");
            sb.append("            }\n");
            sb.append("        },\n");
        }
        //Delete the last ",\n"
        sb.setLength(sb.length() - 2);
        sb.append("\n");
        sb.append("    ]\n");
        sb.append("}\n");
        try
        {
            if(errors.length() == 0) {
                FileUtils.writeStringToFile(file, sb.toString(), StandardCharsets.UTF_8);
                return true;
            }
            else {
                return false;
            }
        }
        catch(IOException e)
        {
            e.printStackTrace();
            return false;
        }
    }

    public String getFieldName(Project.Part.Box cube)
    {
        String name = cube.parent.name.replaceAll("[^A-Za-z0-9_$]", "");
        return name;
    }

    private void rangeCheck(String cubeName, String valueName, double value, double min, double max) {
        if(value < min) {
            this.logError(cubeName + ": " + valueName + " cannot be beyond " + min);
        }
        else if(value > max) {
            this.logError(cubeName + ": " + valueName + " cannot be beyond " + max);
        }
    }

    private void logError(String error) {
        if(this.errors.length() == 0) {
            errors.append("Export failed for the following reasons:\n");
        }
        errors.append(error + "\n");
    }

    private String getAxis(String cubeName, double x, double y, double z) {
        if(x != 0.0) {
            if(y == 0.0 && z == 0.0) {
                return "x";
            }
        } else if (y != 0.0) {
            if(z == 0.0) {
                return "y";
            }
        }
        else {
            return "z";
        }
        this.logError(cubeName + ": rotation can only be on one axis");
        return "z";
    }

    private double getAngle(String cubeName, String axis, double x, double y, double z) {
        switch(axis.charAt(0)) {
            case 'x':
                this.rangeCheck(cubeName, "X rotation", x, -45, 45);
                if(!(x == -45.0 || x == -22.5 || x == 0.0 || x == 22.5 || x == 45.0)) {
                    this.logError(cubeName + ": X rotation must be a multiple of 22.5");
                }
                else if(x == 0.0) {
                    return x;
                }
                return -x;
            case 'y':
                this.rangeCheck(cubeName, "Y rotation", y, -45, 45);
                if(!(y == -45.0 || y == -22.5 || y == 0.0 || y == 22.5 || y == 45.0)) {
                    this.logError(cubeName + ": Y rotation must be a multiple of 22.5");
                }
                else if(y == 0.0) {
                    return y;
                }
                return -y;
            case 'z':
                this.rangeCheck(cubeName, "Z rotation", z, -45, 45);
                if(!(z == -45.0 || z == -22.5 || z == 0.0 || z == 22.5 || z == 45.0)) {
                    this.logError(cubeName + ": Z rotation must be a multiple of 22.5");
                }
                return z;
            default:
                return z;
        }
    }

    private double toU(int oldU) {
        return oldU * 16.0 / (double) this.textureWidth;
    }

    private double toV(int oldV) {
        return oldV * 16.0 / (double) this.textureHeight;
    }
}