package us.ichun.mods.tabula.client.core;

import cpw.mods.fml.client.registry.ClientRegistry;
import cpw.mods.fml.client.registry.RenderingRegistry;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.ObfuscationReflectionHelper;
import ichun.common.core.EntityHelperBase;
import ichun.common.core.util.ObfHelper;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.client.model.ModelBase;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderEntity;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.entity.RendererLivingEntity;
import net.minecraft.client.renderer.texture.TextureUtil;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.passive.EntityHorse;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.client.IItemRenderer;
import net.minecraftforge.client.MinecraftForgeClient;
import us.ichun.mods.tabula.client.mainframe.core.ProjectHelper;
import us.ichun.mods.tabula.client.render.TileRendererTabulaRasa;
import us.ichun.mods.tabula.common.Tabula;
import us.ichun.mods.tabula.common.core.CommonProxy;
import us.ichun.module.tabula.client.model.ModelInfo;
import us.ichun.module.tabula.client.model.ModelList;
import us.ichun.module.tabula.common.project.ProjectInfo;

import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.WritableRaster;
import java.lang.reflect.Field;
import java.util.*;

public class ClientProxy extends CommonProxy
{
    @Override
    public void init()
    {
        super.init();

        ResourceHelper.init();

        tickHandlerClient = new TickHandlerClient();
        FMLCommonHandler.instance().bus().register(tickHandlerClient);
    }

    @Override
    public void postInit()
    {
        super.postInit();

        ArrayList<Class> compatibleEntities = new ArrayList<Class>();

        Iterator ite = EntityList.classToStringMapping.entrySet().iterator();
        while(ite.hasNext())
        {
            Map.Entry e = (Map.Entry)ite.next();
            Class clz = (Class)e.getKey();
            if(EntityLivingBase.class.isAssignableFrom(clz) && !compatibleEntities.contains(clz))
            {
                compatibleEntities.add(clz);
            }
        }
        compatibleEntities.add(EntityPlayer.class);

        HashMap<Class, Render> renders = new HashMap<Class, Render>();
        try
        {
            List entityRenderers = (List)ObfuscationReflectionHelper.getPrivateValue(RenderingRegistry.class, RenderingRegistry.instance(), "entityRenderers");

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
            Render rend = EntityHelperBase.getEntityClassRenderObject(compatibleEntities.get(i));
            if(rend != null && rend.getClass() == RenderEntity.class)
            {
                rend = renders.get(compatibleEntities.get(i));
            }
            renders.put(compatibleEntities.get(i), rend);
        }

        for(Class clz : compatibleEntities)
        {
            try
            {
                Render rend1 = renders.get(clz);
                if(!(rend1 instanceof RendererLivingEntity))
                {
                    continue;
                }
                RendererLivingEntity rend = (RendererLivingEntity)rend1;
                if(clz == EntityPlayer.class)
                {
                    ModelList.models.add(new ModelInfo(AbstractClientPlayer.locationStevePng, rend.mainModel, EntityPlayer.class));
                }
                else if(rend.mainModel != null && clz != null)
                {
                    EntityLivingBase instance;
                    try
                    {
                        instance = (EntityLivingBase)clz.getConstructor(World.class).newInstance(new Object[] { null });
                    }
                    catch(Exception e)
                    {
                        instance = null;
                    }
                    if(Tabula.config.getInt("animateImports") == 1)
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
                    ResourceLocation loc = null;
                    if(clz != EntityHorse.class) //horse gives some kind of error that can't be silenced
                    {
                        try
                        {
                            loc = ObfHelper.invokeGetEntityTexture(rend, rend.getClass(), instance);
                        }
                        catch(Exception e)
                        {
                            loc = null;
                        }
                    }
                    ModelList.models.add(new ModelInfo(loc, rend.mainModel, clz));
                }
            }
            catch(Exception e)
            {
            }
            renders.remove(clz);
        }

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

        Iterator ite2 = RenderManager.instance.entityRenderMap.entrySet().iterator();
        while(ite2.hasNext())
        {
            Map.Entry e = (Map.Entry)ite2.next();
            Class te = (Class)e.getKey();
            Render rend = (Render)e.getValue();

            if(RendererLivingEntity.class.isAssignableFrom(rend.getClass()))
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

        Iterator ite1 = TileEntityRendererDispatcher.instance.mapSpecialRenderers.entrySet().iterator();
        while(ite1.hasNext())
        {
            Map.Entry e = (Map.Entry)ite1.next();
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

        IdentityHashMap<Item, IItemRenderer> customItemRenderers = ObfuscationReflectionHelper.getPrivateValue(MinecraftForgeClient.class, null, "customItemRenderers");//TODO move this string to iChunUtil obfhelper in 1.8
        Iterator ite3 = customItemRenderers.entrySet().iterator();
        while(ite3.hasNext())
        {
            Map.Entry<Item, IItemRenderer> e = (Map.Entry)ite3.next();
            Item item = e.getKey();
            IItemRenderer renderer = e.getValue();

            try
            {
                ModelBase base = null;
                ResourceLocation loc = null;
                Field[] fields = renderer.getClass().getDeclaredFields();
                for(Field f : fields)
                {
                    f.setAccessible(true);
                    if(ModelBase.class.isAssignableFrom(f.getType()))
                    {
                        base = (ModelBase)f.get(renderer);
                    }
                    if(loc == null && ResourceLocation.class.isAssignableFrom(f.getType()))
                    {
                        loc = (ResourceLocation)f.get(e.getValue());
                    }
                }
                if(base != null && item != null)
                {
                    ModelList.models.add(new ModelInfo(loc, base, item.getClass()));
                }
            }
            catch(Exception e1)
            {
            }
        }


        for(int i = ModelList.models.size() - 1; i >= 0 ; i--)
        {
            ModelInfo info = ModelList.models.get(i);
            if(ModelList.modelBlacklist.contains(info.modelParent.getClass()))
            {
                ModelList.modelBlacklist.remove(i);
            }
        }
    }

    @Override
    public void registerTileEntity(Class<? extends TileEntity> clz, String id)
    {
        super.registerTileEntity(clz, id);
        ClientRegistry.bindTileEntitySpecialRenderer(clz, new TileRendererTabulaRasa());
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
}
