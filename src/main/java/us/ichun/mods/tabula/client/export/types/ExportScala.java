package us.ichun.mods.tabula.client.export.types;

import org.apache.commons.io.FileUtils;
import us.ichun.mods.tabula.Tabula;
import us.ichun.mods.tabula.client.core.ResourceHelper;
import us.ichun.mods.tabula.client.gui.GuiWorkspace;
import us.ichun.mods.tabula.client.gui.window.WindowExportScala;
import us.ichun.module.tabula.common.project.ProjectInfo;
import us.ichun.module.tabula.common.project.components.CubeInfo;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Scala model exporter
 *
 * @author anti344
 * @version 1.0
 */

public class ExportScala extends Exporter
{
    public ExportScala()
    {
        super("export.scalaObject.name");
    }

    @Override
    public boolean export(ProjectInfo info, Object... params)
    {
        File file = new File(ResourceHelper.getExportsDir(), params[1] + ".scala");

        StringBuilder sb = new StringBuilder();

        ArrayList<CubeInfo> allCubes = info.getAllCubes();

        HashMap<CubeInfo, String> cubeFieldMap = new HashMap<CubeInfo, String>();

        boolean scale = false;

        for(CubeInfo cube : allCubes)
        {
            if(!(cube.scale[0] == 1.0D && cube.scale[1] == 1.0D && cube.scale[2] == 1.0D))
            {
                scale = true;
                break;
            }
        }

        sb.append("package ").append(params[0]).append("\n\n");
        sb.append("import net.minecraft.client.model.ModelBase\n");
        sb.append("import net.minecraft.client.model.ModelRenderer\n");
        sb.append("import net.minecraft.entity.Entity\n");
        if(scale)
        {
            sb.append("\nimport org.lwjgl.opengl.GL11._\n");
        }
        sb.append("\n");
        sb.append("/**\n");
        sb.append(" * ").append(info.modelName).append(" - ").append(info.authorName).append("\n");
        sb.append(" * Created using Tabula ").append(Tabula.version).append("\n");
        sb.append(" */\n");
        sb.append("object ").append(params[1]).append(" extends ModelBase{\n");
        for(CubeInfo cube : allCubes)
        {
            int count = 0;
            while(count == 0 && cubeFieldMap.containsValue(getFieldName(cube)) || count != 0 && cubeFieldMap.containsValue(getFieldName(cube) + "_" + count))
            {
                count++;
            }
            String fieldName = getFieldName(cube);
            if(count > 0)
            {
                fieldName = fieldName + "_" + count;
            }
            cubeFieldMap.put(cube, fieldName);
        }
        sb.append("  textureWidth = ").append(info.textureWidth).append("\n");
        sb.append("  textureHeight = ").append(info.textureHeight).append("\n\n");
        HashMap<CubeInfo, CubeInfo> parentMap = new HashMap<CubeInfo, CubeInfo>();
        for(Map.Entry<CubeInfo, String> e : cubeFieldMap.entrySet())
        {
            CubeInfo cube = e.getKey();
            String field = e.getValue();
            sb.append("  val ").append(field).append(": ModelRenderer = new ModelRenderer(this, ").append(cube.txOffset[0]).append(", ").append(cube.txOffset[1]).append(")\n");
            sb.append("    .rotationPoint(").append(cube.position[0]).append("F, ").append(cube.position[1]).append("F, ").append(cube.position[2]).append("F)\n");
            if(!(cube.rotation[0] == 0.0D && cube.rotation[1] == 0.0D && cube.rotation[2] == 0.0D))
            {
                sb.append("    .rotate(").append(Math.toRadians(cube.rotation[0])).append("F, ").append(Math.toRadians(cube.rotation[1])).append("F, ").append(Math.toRadians(cube.rotation[2])).append("F)\n");
            }
            sb.append("    .box(").append(cube.offset[0]).append("F, ").append(cube.offset[1]).append("F, ").append(cube.offset[2]).append("F, ").append(cube.dimensions[0]).append(", ").append(cube.dimensions[1]).append(", ").append(cube.dimensions[2]).append(")\n");
            if(cube.txMirror)
            {
                sb.append("    .mirror()\n");
            }
            for(CubeInfo child : cube.getChildren())
            {
                parentMap.put(child, cube);
            }
        }
        if(!parentMap.isEmpty())
        {
            sb.append("\n");
            for(Map.Entry<CubeInfo, String> e : cubeFieldMap.entrySet())
            {
                CubeInfo cube = e.getKey();
                String field = e.getValue();
                if(parentMap.get(cube) != null)
                {
                    sb.append("  ").append(cubeFieldMap.get(parentMap.get(cube))).append(".addChild(").append(field).append(")\n");
                }
            }
            for(Map.Entry<CubeInfo, CubeInfo> e : parentMap.entrySet())
            {
                cubeFieldMap.remove(e.getKey());
            }
        }
        sb.append("\n  override def render(entity: Entity, f: Float, f1: Float, f2: Float, f3: Float, f4: Float, f5: Float) = {\n");
        for(Map.Entry<CubeInfo, String> e : cubeFieldMap.entrySet())
        {
            CubeInfo cube = e.getKey();
            sb.append("    ").append(e.getValue());
            if(!(cube.scale[0] == 1.0D && cube.scale[1] == 1.0D && cube.scale[2] == 1.0D))
            {
                sb.append(".rotatedRender(f5)(").append(cube.scale[0]).append("D, ").append(cube.scale[1]).append("D, ").append(cube.scale[2]).append("D)\n");
            }
            else
            {
                sb.append(".render(f5)\n");
            }
        }
        sb.append("  }\n\n");
        sb.append("  /**\n");
        sb.append("   * This is an implicit mutator class from Tabula to set properties of model parts(useful for big models)\n");
        sb.append("   */\n");
        sb.append("  implicit class ModelMutator(model: ModelRenderer){\n\n");
        sb.append("    def mirrored(): ModelRenderer = {\n");
        sb.append("      model.mirror = true\n");
        sb.append("      model\n");
        sb.append("    }\n\n");
        sb.append("    def rotationPoint(x: Float, y: Float, z: Float): ModelRenderer = {\n");
        sb.append("      model.setRotationPoint(x, y, z)\n");
        sb.append("      model\n");
        sb.append("    }\n\n");
        sb.append("    def rotate(x: Float, y: Float, z: Float): ModelRenderer = {\n");
        sb.append("      model.rotateAngleX = x\n");
        sb.append("      model.rotateAngleY = y\n");
        sb.append("      model.rotateAngleZ = z\n");
        sb.append("      model\n");
        sb.append("    }\n\n");
        sb.append("    def box(x: Float, y: Float, z: Float, w: Int, h: Int, d: Int): ModelRenderer = {\n");
        sb.append("      model.addBox(x, y, z, w, h, d)\n");
        sb.append("      model\n");
        sb.append("    }\n");
        if(scale)
        {
            sb.append("\n    def rotatedRender(f5: Float)(scaleX: Double, scaleY: Double, scaleZ: Double) = {\n");
            sb.append("      glPushMatrix()\n");
            sb.append("      glTranslatef(model.offsetX, model.offsetY, model.offsetZ)\n");
            sb.append("      glTranslatef(model.rotationPointX * f5, model.rotationPointY * f5, model.rotationPointZ * f5)\n");
            sb.append("      glScaled(scaleX, scaleY, scaleZ)\n");
            sb.append("      glTranslatef(-model.offsetX, -model.offsetY, -model.offsetZ)\n");
            sb.append("      glTranslatef(-model.rotationPointX * f5, -model.rotationPointY * f5, -model.rotationPointZ * f5)\n");
            sb.append("      model.render(f5)\n");
            sb.append("      glPopMatrix()\n");
            sb.append("    }\n");
        }
        sb.append("  }\n");
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

    private String getFieldName(CubeInfo cube)
    {
        return cube.name.replaceAll("[^A-Za-z0-9_$]", "");
    }

    @Override
    public boolean override(GuiWorkspace workspace)
    {
        workspace.addWindowOnTop(new WindowExportScala(workspace, workspace.width / 2 - 100, workspace.height / 2 - 80, 200, 160, 200, 160).putInMiddleOfScreen());
        return true;
    }
}
