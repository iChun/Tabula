package me.ichun.mods.tabula.client.core;

import me.ichun.mods.ichunutil.client.model.util.ModelHelper;
import me.ichun.mods.ichunutil.client.module.tabula.model.ModelInfo;
import me.ichun.mods.ichunutil.client.module.tabula.model.ModelList;
import me.ichun.mods.ichunutil.common.core.util.ObfHelper;
import me.ichun.mods.tabula.client.mainframe.core.ProjectHelper;
import me.ichun.mods.tabula.client.render.TileRendererTabulaRasa;
import me.ichun.mods.tabula.common.Tabula;
import me.ichun.mods.tabula.common.core.ProxyCommon;
import me.ichun.mods.tabula.common.tileentity.TileEntityTabulaRasa;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.ModelBase;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderEntity;
import net.minecraft.client.renderer.entity.RenderLivingBase;
import net.minecraft.client.renderer.entity.layers.LayerRenderer;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraft.world.WorldProviderSurface;
import net.minecraft.world.chunk.IChunkProvider;
import net.minecraft.world.storage.SaveHandlerMP;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.client.registry.RenderingRegistry;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;
import net.minecraftforge.fml.common.registry.EntityEntry;
import net.minecraftforge.fml.common.registry.ForgeRegistries;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.*;

public class ProxyClient extends ProxyCommon
{
    @Override
    public void preInit()
    {
        super.preInit();

        ResourceHelper.init();

        ClientRegistry.bindTileEntitySpecialRenderer(TileEntityTabulaRasa.class, new TileRendererTabulaRasa());

        tickHandlerClient = new TickHandlerClient();
        FMLCommonHandler.instance().bus().register(tickHandlerClient);
    }

    @Override
    public void postInit()
    {
        super.postInit();

        ArrayList<Class<? extends Entity>> compatibleEntities = new ArrayList<>();

        for(EntityEntry entry : ForgeRegistries.ENTITIES.getValues())
        {
            Class clz = entry.getEntityClass();
            if(EntityLivingBase.class.isAssignableFrom(clz) && !compatibleEntities.contains(clz))
            {
                compatibleEntities.add(clz);
            }
        }

        HashMap<Class, Render> renders = new HashMap<>();
        try
        {
            RenderingRegistry reg = ObfuscationReflectionHelper.getPrivateValue(RenderingRegistry.class, null, "INSTANCE");
            List entityRenderers = ObfuscationReflectionHelper.getPrivateValue(RenderingRegistry.class, reg, "entityRenderers");

            for(Object obj : entityRenderers)
            {
                Field[] fields = obj.getClass().getDeclaredFields();
                Render render = null;
                Class clzz = null;
                for(Field f : fields)
                {
                    f.setAccessible(true);
                    if(f.getType() == Render.class)
                    {
                        render = (Render)f.get(obj);
                    }
                    else if(f.getType() == Class.class)
                    {
                        clzz = (Class)f.get(obj);
                    }
                }
                if(render != null && clzz != null)
                {
                    renders.put(clzz, render);
                }
            }
        }
        catch(Exception e)
        {
        }

        for(int i = compatibleEntities.size() - 1; i >= 0; i--)
        {
            if(renders.containsKey(compatibleEntities.get(i)))
            {
                continue;
            }
            Render rend = Minecraft.getMinecraft().getRenderManager().getEntityClassRenderObject(compatibleEntities.get(i));
            if(rend != null && rend.getClass() == RenderEntity.class)
            {
                rend = renders.get(compatibleEntities.get(i));
            }
            renders.put(compatibleEntities.get(i), rend);
        }

        for(Class clz : compatibleEntities)
        {
            Render rend1 = renders.get(clz);
            if(!(rend1 instanceof RenderLivingBase))
            {
                continue;
            }
            RenderLivingBase rend = (RenderLivingBase)rend1;
            mapModelInfo(clz, rend, null);
            renders.remove(clz);
        }

        mapModelInfo(EntityPlayer.class, Minecraft.getMinecraft().getRenderManager().skinMap.get("default"), new ResourceLocation("textures/entity/steve.png"));
        mapModelInfo(EntityPlayer.class, Minecraft.getMinecraft().getRenderManager().skinMap.get("slim"), new ResourceLocation("textures/entity/alex.png"));

        Collections.sort(ModelList.models);

        for(Map.Entry<Class, Render> e : renders.entrySet())
        {
            try
            {
                ModelBase base = null;
                ResourceLocation loc = null;
                Field[] fields = e.getValue().getClass().getDeclaredFields();
                for(Field f : fields)
                {
                    f.setAccessible(true);
                    if(ModelBase.class.isAssignableFrom(f.getType()))
                    {
                        base = (ModelBase)f.get(e.getValue());
                    }
                    if(loc == null && ResourceLocation.class.isAssignableFrom(f.getType()))
                    {
                        loc = (ResourceLocation)f.get(e.getValue());
                    }
                }
                if(base != null && e.getKey() != null)
                {
                    ModelList.models.add(new ModelInfo(loc, base, e.getKey()));
                }
            }
            catch(Exception e1)
            {
            }
        }

        for(Object o1 : Minecraft.getMinecraft().getRenderManager().entityRenderMap.entrySet())
        {
            Map.Entry e = (Map.Entry)o1;
            Class te = (Class)e.getKey();
            Render rend = (Render)e.getValue();

            if(RenderLivingBase.class.isAssignableFrom(rend.getClass()))
            {
                continue;
            }

            try
            {
                ModelBase base = null;
                ResourceLocation loc = null;
                Field[] fields = rend.getClass().getDeclaredFields();
                for(Field f : fields)
                {
                    f.setAccessible(true);
                    if(ModelBase.class.isAssignableFrom(f.getType()))
                    {
                        base = (ModelBase)f.get(rend);
                    }
                    if(loc == null && ResourceLocation.class.isAssignableFrom(f.getType()))
                    {
                        loc = (ResourceLocation)f.get(e.getValue());
                    }
                }
                if(base != null && te != null)
                {
                    ModelList.models.add(new ModelInfo(loc, base, te));
                }
            }
            catch(Exception e1)
            {
            }
        }

        for(Object o : TileEntityRendererDispatcher.instance.renderers.entrySet())
        {
            Map.Entry e = (Map.Entry)o;
            Class te = (Class)e.getKey();
            TileEntitySpecialRenderer rend = (TileEntitySpecialRenderer)e.getValue();

            try
            {
                ModelBase base = null;
                ResourceLocation loc = null;
                Field[] fields = rend.getClass().getDeclaredFields();
                for(Field f : fields)
                {
                    f.setAccessible(true);
                    if(ModelBase.class.isAssignableFrom(f.getType()))
                    {
                        base = (ModelBase)f.get(rend);
                    }
                    if(loc == null && ResourceLocation.class.isAssignableFrom(f.getType()))
                    {
                        loc = (ResourceLocation)f.get(e.getValue());
                    }
                }
                if(base != null && te != null)
                {
                    ModelList.models.add(new ModelInfo(loc, base, te));
                }
            }
            catch(Exception e1)
            {
            }
        }

        //TODO import block and item models.

        for(int i = ModelList.models.size() - 1; i >= 0 ; i--)
        {
            ModelInfo info = ModelList.models.get(i);
            if(ModelList.modelBlacklist.contains(info.modelParent.getClass()))
            {
                ModelList.models.remove(i);
            }
        }
    }

    @Override
    public void updateProject(boolean fromClient, String ident, boolean isTexture, boolean updateDims)
    {
        if(tickHandlerClient.mainframe == null)
        {
            if(isTexture)
            {
                Tabula.proxy.tickHandlerClient.projectImagesToUpdate.put(ident, ProjectHelper.projectTextures.get(ident));
            }
            else
            {
                Tabula.proxy.tickHandlerClient.projectsToUpdate.put(ident, ProjectHelper.projects.get(ident));
            }
        }
        else if(fromClient && isTexture)
        {
            Tabula.proxy.tickHandlerClient.mainframe.loadTexture(ident, ProjectHelper.projectTextures.get(ident), updateDims);
        }
    }

    public void mapModelInfo(Class<? extends EntityLivingBase> clz, final RenderLivingBase rend, ResourceLocation loc)
    {
        try
        {
            if(rend.mainModel != null && clz != null)
            {
                if(clz != EntityPlayer.class)
                {
                    EntityLivingBase instance;
                    try
                    {
                        instance = clz.getConstructor(World.class).newInstance(new Object[] { null });
                    }
                    catch(InvocationTargetException e)
                    {
                        try
                        {
                            instance = clz.getConstructor(World.class).newInstance(new World(new SaveHandlerMP(), null, new WorldProviderSurface(), null, true)
                            {
                                @Override
                                protected IChunkProvider createChunkProvider()
                                {
                                    return null;
                                }

                                @Override
                                protected boolean isChunkLoaded(int x, int z, boolean allowEmpty)
                                {
                                    return false;
                                }
                            });
                        }
                        catch(Exception e1)
                        {
                            instance = null;
                        }
                    }
                    catch(Exception e)
                    {
                        instance = null;
                    }
                    if(Tabula.config.animateImports == 1)
                    {
                        try
                        {
                            rend.mainModel.setRotationAngles(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0625F, instance);
                        }
                        catch(Exception e)
                        {
                        }
                        try
                        {
                            rend.mainModel.render(instance, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0625F);
                        }
                        catch(Exception e)
                        {
                        }
                        try
                        {
                            rend.mainModel.setLivingAnimations(instance, 0.0F, 0.0F, 0.0F);
                        }
                        catch(Exception e)
                        {
                        }
                    }
                    if(loc == null) //horse gives some kind of error that can't be silenced
                    {
                        try
                        {
                            loc = ObfHelper.getEntityTexture(rend, rend.getClass(), instance);
                        }
                        catch(Exception e)
                        {
                            loc = null;
                        }
                    }
                }
                ModelList.models.add(new ModelInfo(loc, rend.mainModel, clz));

                ArrayList<ModelBase> modelsToCompare = new ArrayList<ModelBase>() {{ add(rend.mainModel); }};
                for(int i = 0; i < rend.layerRenderers.size(); i++)
                {
                    LayerRenderer layer = (LayerRenderer)rend.layerRenderers.get(i);
                    Field[] fields = layer.getClass().getDeclaredFields();
                    ResourceLocation loc1 = null;
                    ModelBase base = null;
                    for(Field f : fields)
                    {
                        f.setAccessible(true);
                        if(ModelBase.class.isAssignableFrom(f.getType()))
                        {
                            base = (ModelBase)f.get(layer);
                        }
                        if(loc1 == null && ResourceLocation.class.isAssignableFrom(f.getType()))
                        {
                            loc1 = (ResourceLocation)f.get(layer);
                        }
                    }
                    if(base != null)
                    {
                        boolean add = true;
                        for(ModelBase model : modelsToCompare)
                        {
                            if(ModelHelper.areModelsEqual(model, base))
                            {
                                add = false;
                                break;
                            }
                        }
                        if(add)
                        {
                            ModelList.models.add(new ModelInfo(loc1 == null ? loc : loc1, base, layer.getClass()));
                            modelsToCompare.add(base);
                        }
                    }
                }
            }
        }
        catch(Exception e)
        {
        }

    }
}
