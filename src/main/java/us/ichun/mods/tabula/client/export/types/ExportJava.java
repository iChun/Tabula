package us.ichun.mods.tabula.client.export.types;

import org.apache.commons.io.FileUtils;
import us.ichun.mods.ichunutil.client.gui.window.IWorkspace;
import us.ichun.mods.ichunutil.common.module.tabula.client.formats.types.Exporter;
import us.ichun.mods.ichunutil.common.module.tabula.common.project.ProjectInfo;
import us.ichun.mods.ichunutil.common.module.tabula.common.project.components.CubeInfo;
import us.ichun.mods.tabula.client.core.ResourceHelper;
import us.ichun.mods.tabula.client.gui.window.WindowExportJava;
import us.ichun.mods.tabula.common.Tabula;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class ExportJava extends Exporter
{
    public ExportJava()
    {
        super("export.javaClass.name");
    }

    @Override
    public boolean export(ProjectInfo info, Object... params)
    {
        File file = new File(ResourceHelper.getExportsDir(), params[1] + ".java");

        StringBuilder sb = new StringBuilder();

        ArrayList<CubeInfo> allCubes = info.getAllCubes();

        HashMap<CubeInfo, String> cubeFieldMap = new HashMap<CubeInfo, String>();

        sb.append("package " + params[0] + ";\n\n");
        sb.append("import net.minecraft.client.model.ModelBase;\n");
        sb.append("import net.minecraft.client.model.ModelRenderer;\n");
        sb.append("import net.minecraft.entity.Entity;\n");
        boolean addGl11 = false;
        for(CubeInfo cube : allCubes)
        {
            if(!(cube.scale[0] == 1.0D && cube.scale[1] == 1.0D && cube.scale[2] == 1.0D) || cube.opacity != 100D)
            {
                addGl11 = true;
                break;
            }
        }
        if(!(info.scale[0] == 1D && info.scale[1] == 1D && info.scale[2] == 1D))
        {
            addGl11 = true;
        }
        if(addGl11)
        {
            sb.append("import net.minecraft.client.renderer.GlStateManager;\n");
            sb.append("import org.lwjgl.opengl.GL11;\n");
        }
        sb.append("\n");
        sb.append("/**\n");
        sb.append(" * " + info.modelName + " - " + info.authorName + "\n");
        sb.append(" * Created using Tabula " + Tabula.VERSION + "\n");
        sb.append(" */\n");
        sb.append("public class " + params[1] + " extends ModelBase {\n");
        if(!(info.scale[0] == 1D && info.scale[1] == 1D && info.scale[2] == 1D))
        {
            sb.append("    public double[] modelScale = new double[] { " + info.scale[0] + "D, " + info.scale[1] + "D, " + info.scale[2] + "D };\n");
        }
        for(int i = 0; i < allCubes.size(); i++)
        {
            CubeInfo cube =  allCubes.get(i);
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
            sb.append("    public ModelRenderer " + fieldName + ";\n");
        }
        sb.append("\n");
        sb.append("    public " + params[1] + "() {\n");
        sb.append("        this.textureWidth = " + info.textureWidth + ";\n");
        sb.append("        this.textureHeight = " + info.textureHeight + ";\n");
        HashMap<CubeInfo, CubeInfo> parentMap = new HashMap<CubeInfo, CubeInfo>();
        StringBuilder childSb = new StringBuilder();
        for(Map.Entry<CubeInfo, String> e : cubeFieldMap.entrySet())
        {
            CubeInfo cube = e.getKey();
            for(CubeInfo child : cube.getChildren())
            {
                parentMap.put(child, cube);
            }
        }
        for(Map.Entry<CubeInfo, String> e : cubeFieldMap.entrySet())
        {
            CubeInfo cube = e.getKey();
            String field = e.getValue();
            sb.append("        this." + field + " = new ModelRenderer(this, " + cube.txOffset[0] + ", " + cube.txOffset[1] + ");\n");
            if(cube.txMirror)
            {
                sb.append("        this." + field + ".mirror = true;\n");
            }
            sb.append("        this." + field + ".setRotationPoint(" + cube.position[0] + "F, " + cube.position[1] + "F, " + cube.position[2] + "F);\n");
            sb.append("        this." + field + ".addBox(" + cube.offset[0] + "F, " + cube.offset[1] + "F, " + cube.offset[2] + "F, " + cube.dimensions[0] + ", " + cube.dimensions[1] + ", " + cube.dimensions[2] + ", " + cube.mcScale + "F);\n");
            if(!(cube.rotation[0] == 0.0D && cube.rotation[1] == 0.0D && cube.rotation[2] == 0.0D))
            {
                sb.append("        this.setRotateAngle(" + field + ", " + Math.toRadians(cube.rotation[0]) + "F, " + Math.toRadians(cube.rotation[1]) + "F, " + Math.toRadians(cube.rotation[2]) + "F);\n");
            }
            for(CubeInfo child : cube.getChildren())
            {
                parentMap.put(child, cube);
            }
            if(parentMap.get(cube) != null)
            {
                childSb.append("        this." + cubeFieldMap.get(parentMap.get(cube)) + ".addChild(this." + field + ");\n");
            }
        }
        sb.append(childSb.toString());
        for(Map.Entry<CubeInfo, CubeInfo> e : parentMap.entrySet())
        {
            cubeFieldMap.remove(e.getKey());//removing it so children don't get called to render later on.
        }
        sb.append("    }\n\n");
        sb.append("    @Override\n");
        sb.append("    public void render(Entity entity, float f, float f1, float f2, float f3, float f4, float f5) { \n");

        if(!(info.scale[0] == 1D && info.scale[1] == 1D && info.scale[2] == 1D))
        {
            sb.append("        GlStateManager.pushMatrix();\n");
            sb.append("        GlStateManager.scale(1D / modelScale[0], 1D / modelScale[1], 1D / modelScale[2]);\n");
        }
        for(Map.Entry<CubeInfo, String> e : cubeFieldMap.entrySet())
        {
            CubeInfo cube = e.getKey();
            String field = e.getValue();
            if(!(cube.scale[0] == 1.0D && cube.scale[1] == 1.0D && cube.scale[2] == 1.0D))
            {
                sb.append("        GlStateManager.pushMatrix();\n");
                sb.append("        GlStateManager.translate(this." + field + ".offsetX, this." + field + ".offsetY, this." + field + ".offsetZ);\n");
                sb.append("        GlStateManager.translate(this." + field + ".rotationPointX * f5, this." + field + ".rotationPointY * f5, this." + field + ".rotationPointZ * f5);\n");
                sb.append("        GlStateManager.scale(" + cube.scale[0] + "D, " + cube.scale[1] + "D, " + cube.scale[2] + "D);\n");
                sb.append("        GlStateManager.translate(-this." + field + ".offsetX, -this." + field + ".offsetY, -this." + field + ".offsetZ);\n");
                sb.append("        GlStateManager.translate(-this." + field + ".rotationPointX * f5, -this." + field + ".rotationPointY * f5, -this." + field + ".rotationPointZ * f5);\n");
            }
            if(cube.opacity != 100D)
            {
                sb.append("        GlStateManager.enableBlend();\n");
                sb.append("        GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);\n");
                sb.append("        GlStateManager.color(1.0F, 1.0F, 1.0F, " + (cube.opacity / 100D) + "F);\n");
            }
            sb.append("        this." + field + ".render(f5);\n");
            if(cube.opacity != 100D)
            {
                sb.append("        GlStateManager.disableBlend();\n");
            }
            if(!(cube.scale[0] == 1.0D && cube.scale[1] == 1.0D && cube.scale[2] == 1.0D))
            {
                sb.append("        GlStateManager.popMatrix();\n");
            }
        }
        if(!(info.scale[0] == 1D && info.scale[1] == 1D && info.scale[2] == 1D))
        {
            sb.append("        GlStateManager.popMatrix();\n");
        }

        sb.append("    }\n\n");
        sb.append("    /**\n");
        sb.append("     * This is a helper function from Tabula to set the rotation of model parts\n");
        sb.append("     */\n");
        sb.append("    public void setRotateAngle(ModelRenderer modelRenderer, float x, float y, float z) {\n");
        sb.append("        modelRenderer.rotateAngleX = x;\n");
        sb.append("        modelRenderer.rotateAngleY = y;\n");
        sb.append("        modelRenderer.rotateAngleZ = z;\n");
        sb.append("    }\n");
        sb.append("}\n");

        try
        {
            FileUtils.writeStringToFile(file, sb.toString());
            return true;
        }
        catch(IOException e)
        {
            e.printStackTrace();
            return false;
        }
    }

    public String getFieldName(CubeInfo cube)
    {
        String name = cube.name.replaceAll("[^A-Za-z0-9_$]", "");
        return name;
    }

    @Override
    public boolean override(IWorkspace workspace)
    {
        workspace.addWindowOnTop(new WindowExportJava(workspace, workspace.width / 2 - 100, workspace.height / 2 - 80, 200, 160, 200, 160).putInMiddleOfScreen());
        return true;
    }
}
