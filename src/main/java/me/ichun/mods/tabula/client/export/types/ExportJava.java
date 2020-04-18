package me.ichun.mods.tabula.client.export.types;

import me.ichun.mods.ichunutil.client.gui.bns.Workspace;
import me.ichun.mods.ichunutil.common.module.tabula.formats.types.Exporter;
import me.ichun.mods.ichunutil.common.module.tabula.project.Identifiable;
import me.ichun.mods.ichunutil.common.module.tabula.project.Project;
import me.ichun.mods.tabula.client.core.ResourceHelper;
import me.ichun.mods.tabula.client.gui.WorkspaceTabula;
import me.ichun.mods.tabula.client.gui.window.popup.WindowExportJava;
import me.ichun.mods.tabula.common.Tabula;
import net.minecraft.client.resources.I18n;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class ExportJava extends Exporter
{
    public ExportJava()
    {
        super(I18n.format("export.javaClass.name"));
    }

    @Override
    public String getId()
    {
        return "javaClass";
    }

    @Override
    public boolean override(Workspace workspace, Project project)
    {
        workspace.openWindowInCenter(new WindowExportJava(((WorkspaceTabula)workspace), project), 0.6D, 0.6D);
        return true;
    }

    @Override
    public boolean export(Project project, Object... params)
    {
        File file = new File(ResourceHelper.getExportsDir().toFile(), params[1] + ".java");

        StringBuilder sb = new StringBuilder();

        ArrayList<Project.Part> allCubes = project.getAllParts();

        HashMap<Project.Part, String> cubeFieldMap = new HashMap<>();

        sb.append("package " + params[0] + ";\n\n");
        sb.append("import com.google.common.collect.ImmutableList;\n");
        sb.append("import com.mojang.blaze3d.matrix.MatrixStack;\n");
        sb.append("import com.mojang.blaze3d.vertex.IVertexBuilder;\n");
        sb.append("import net.minecraft.client.renderer.entity.model.EntityModel;\n");
        sb.append("import net.minecraft.client.renderer.model.ModelRenderer;\n");
        sb.append("import net.minecraft.entity.Entity;\n");
        sb.append("import net.minecraftforge.api.distmarker.Dist;\n");
        sb.append("import net.minecraftforge.api.distmarker.OnlyIn;\n");


        sb.append("\n");
        sb.append("/**\n");
        sb.append(" * " + project.name + " - " + project.author + "\n");
        sb.append(" * Created using Tabula " + Tabula.VERSION + "\n");
        sb.append(" */\n");
        sb.append("@OnlyIn(Dist.CLIENT)\n");
        sb.append("public class " + params[1] + "<T extends Entity> extends EntityModel<T> {\n");
        boolean hasProjectScale = !(project.scaleX == 1F && project.scaleY == 1F && project.scaleZ == 1F);
        if(hasProjectScale)
        {
            sb.append("    public float[] modelScale = new float[] { " + project.scaleX + "F, " + project.scaleY + "F, " + project.scaleZ + "F };\n");
        }
        for(Project.Part cube : allCubes)
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
            sb.append("    public ModelRenderer " + fieldName + ";\n");
        }
        sb.append("\n");
        sb.append("    public " + params[1] + "() {\n");
        sb.append("        this.textureWidth = " + project.texWidth + ";\n");
        sb.append("        this.textureHeight = " + project.texHeight + ";\n");
        HashMap<Project.Part, Project.Part> parentMap = new HashMap<>();
        StringBuilder childSb = new StringBuilder();
        for(Map.Entry<Project.Part, String> e : cubeFieldMap.entrySet())
        {
            Project.Part cube = e.getKey();
            for(Project.Part child : cube.children)
            {
                parentMap.put(child, cube);
            }
        }
        for(Map.Entry<Project.Part, String> e : cubeFieldMap.entrySet())
        {
            Project.Part cube = e.getKey();
            String field = e.getValue();
            sb.append("        this." + field + " = new ModelRenderer(this, " + cube.texOffX + ", " + cube.texOffY + ");\n");
            if(cube.mirror)
            {
                sb.append("        this." + field + ".mirror = true;\n");
            }
            sb.append("        this." + field + ".setRotationPoint(" + cube.rotPX + "F, " + cube.rotPY + "F, " + cube.rotPZ + "F);\n");
            for(Project.Part.Box box : cube.boxes)
            {
                sb.append("        this." + field + ".addBox(" + box.posX + "F, " + box.posY + "F, " + box.posZ + "F, " + box.dimX + "F, " + box.dimY + "F, " + box.dimZ + "F, " + box.expandX + "F, " + box.expandY + "F, "  + box.expandZ + "F);\n");
            }
            if(!(cube.rotAX == 0.0D && cube.rotAY == 0.0D && cube.rotAZ == 0.0D))
            {
                sb.append("        this.setRotateAngle(" + field + ", " + Math.toRadians(cube.rotAX) + "F, " + Math.toRadians(cube.rotAY) + "F, " + Math.toRadians(cube.rotAZ) + "F);\n");
            }
            for(Project.Part child : cube.children)
            {
                parentMap.put(child, cube);
            }
            if(parentMap.get(cube) != null)
            {
                childSb.append("        this." + cubeFieldMap.get(parentMap.get(cube)) + ".addChild(this." + field + ");\n");
            }
        }
        sb.append(childSb.toString());
        for(Map.Entry<Project.Part, Project.Part> e : parentMap.entrySet())
        {
            cubeFieldMap.remove(e.getKey());//removing it so children don't get called to render later on.
        }
        sb.append("    }\n\n");
        sb.append("    @Override\n");
        sb.append("    public void render(MatrixStack matrixStackIn, IVertexBuilder bufferIn, int packedLightIn, int packedOverlayIn, float red, float green, float blue, float alpha) { \n");

        if(hasProjectScale)
        {
            sb.append("        matrixStackIn.push();\n");
            sb.append("        matrixStackIn.scale(modelScale[0], modelScale[1], modelScale[2]);\n");
        }
        sb.append("        ImmutableList.of(");
        int i = 0;
        for(Map.Entry<Project.Part, String> e : cubeFieldMap.entrySet())
        {
            Project.Part cube = e.getKey();
            String field = e.getValue();
            sb.append("this." + field);
            if(i < cubeFieldMap.size() - 1)
            {
                sb.append(", ");
            }
            i++;
        }
        sb.append(").forEach((modelRenderer) -> { \n");
        sb.append("            modelRenderer.render(matrixStackIn, bufferIn, packedLightIn, packedOverlayIn, red, green, blue, alpha);\n");
        sb.append("        });\n");
        if(hasProjectScale)
        {
            sb.append("        matrixStackIn.pop();\n");
        }

        sb.append("    }\n\n");
        sb.append("    @Override\n");
        sb.append("    public void setRotationAngles(T entityIn, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {}\n\n");
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
            FileUtils.writeStringToFile(file, sb.toString(), StandardCharsets.UTF_8);
            return true;
        }
        catch(IOException e)
        {
            e.printStackTrace();
            return false;
        }
    }

    public String getFieldName(Identifiable<?> cube)
    {
        String name = cube.name.replaceAll("[^A-Za-z0-9_$]", "");
        return name;
    }
}
