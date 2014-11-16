package us.ichun.mods.tabula.common.project;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.ModelBox;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.client.resources.IResource;
import us.ichun.mods.tabula.client.model.ModelBaseDummy;
import us.ichun.mods.tabula.client.model.ModelInfo;
import us.ichun.mods.tabula.common.project.components.CubeGroup;
import us.ichun.mods.tabula.common.project.components.CubeInfo;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Map;

public class ProjectInfo
{
    public transient String identifier;
    public transient File saveFile;

    public transient float cameraZoom = 1.0F;
    public transient float cameraYaw;
    public transient float cameraPitch;
    public transient float cameraOffsetX;
    public transient float cameraOffsetY;

    public transient boolean ignoreNextImage;
    public transient File textureFile;
    public transient String textureFileMd5;

    public transient BufferedImage bufferedTexture;

    public transient ModelBaseDummy model;

    public String modelName;
    public String authorName;
    public String projVersion;

    public int textureWidth = 64;
    public int textureHeight = 32;

    public ArrayList<CubeGroup> cubeGroups;
    public ArrayList<CubeInfo> cubes;

    public int cubeCount;

    public ProjectInfo()
    {
        cameraZoom = 1.0F;
    }

    public ProjectInfo(String name, String author)
    {
        this();
        modelName = name;
        authorName = author;

        cubeGroups = new ArrayList<CubeGroup>();
        cubes = new ArrayList<CubeInfo>();
    }

    public String getAsJson()
    {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        //        Gson gson = new Gson();

        return gson.toJson(this);
    }

    public void initClient()
    {
        model = new ModelBaseDummy(this);
        model.textureWidth = textureWidth;
        model.textureHeight = textureHeight;
        for(int i = 0 ; i < cubes.size(); i++)
        {
            //TODO remember to support child models.
            model.createModelFromCubeInfo(cubes.get(i));
        }
        //        final RenderCow render = ((RenderCow)RenderManager.instance.getEntityClassRenderObject(EntityCow.class));
        //        ArrayList<ModelRenderer> boxes = new ArrayList<ModelRenderer>() {{
        //            add(((ModelQuadruped)render.mainModel).body);
        //            add(((ModelQuadruped)render.mainModel).head);
        //            add(((ModelQuadruped)render.mainModel).leg1);
        //            add(((ModelQuadruped)render.mainModel).leg2);
        //            add(((ModelQuadruped)render.mainModel).leg3);
        //            add(((ModelQuadruped)render.mainModel).leg4);
        //        }};
        //        for(int i = 0; i < boxes.size(); i++)
        //        {
        //            CubeInfo info = new CubeInfo("fake" + i);
        //            info.modelCube = boxes.get(i);
        //            model.cubes.add(info);
        //        }
    }

    public void destroy()
    {
        if(model != null)
        {
            for(int i = model.cubes.size() - 1; i >= 0; i--)
            {
                model.removeCubeInfo(model.cubes.get(i));
            }
        }
    }

    public void createNewCube()
    {
        cubeCount++;
        cubes.add(new CubeInfo("shape" + Integer.toString(cubeCount)));
    }

    public boolean importModel(ModelInfo model, boolean texture)
    {
        for(Map.Entry<String, ModelRenderer> e : model.modelList.entrySet())
        {
            ModelRenderer rend = e.getValue();

            for(int j = 0; j < rend.cubeList.size(); j++)
            {
                CubeInfo info = new CubeInfo(e.getKey() + (rend.cubeList.size() == 1 ? "" : (" - " + j)));
                ModelBox box = (ModelBox)rend.cubeList.get(j);

                info.dimensions[0] = (int)Math.abs(box.posX2 - box.posX1);
                info.dimensions[1] = (int)Math.abs(box.posY2 - box.posY1);
                info.dimensions[2] = (int)Math.abs(box.posZ2 - box.posZ1);

                info.position[0] = rend.rotationPointX;
                info.position[1] = rend.rotationPointY;
                info.position[2] = rend.rotationPointZ;

                info.offset[0] = box.posX1;
                info.offset[1] = box.posY1;
                info.offset[2] = box.posZ1;

                info.rotation[0] = Math.toDegrees(rend.rotateAngleX);
                info.rotation[1] = Math.toDegrees(rend.rotateAngleY);
                info.rotation[2] = Math.toDegrees(rend.rotateAngleZ);

                info.scale[0] = info.scale[1] = info.scale[2] = 1.0F;

                info.txOffset[0] = rend.textureOffsetX;
                info.txOffset[1] = rend.textureOffsetY;

                info.txMirror = rend.mirror;

                cubeCount++;
                cubes.add(info);
            }
            //TODO handle children
        }
        if(texture)
        {
            if(model.texture != null)
            {
                InputStream inputstream = null;
                try
                {
                    IResource iresource = Minecraft.getMinecraft().mcResourceManager.getResource(model.texture);
                    inputstream = iresource.getInputStream();
                    bufferedTexture = ImageIO.read(inputstream);

                    if(bufferedTexture != null)
                    {
                        for(Map.Entry<String, ModelRenderer> e : model.modelList.entrySet())
                        {
                            ModelRenderer rend = e.getValue();

                            textureHeight = (int)rend.textureHeight;
                            textureWidth = (int)rend.textureWidth;

                            break;
                        }
                        return true;
                    }
                }
                catch(Exception e)
                {

                }
            }
            else
            {
                bufferedTexture = null;
                return true;
            }
        }
        return false;
    }

    public void cloneFrom(ProjectInfo info)
    {
        //TODO link the textures together.
        this.bufferedTexture = info.bufferedTexture;
        this.cameraZoom = info.cameraZoom;
        this.cameraYaw = info.cameraYaw;
        this.cameraPitch = info.cameraPitch;
        this.cameraOffsetX = info.cameraOffsetX;
        this.cameraOffsetY = info.cameraOffsetY;
    }

    //TODO texture size?
}
