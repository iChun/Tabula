package us.ichun.mods.tabula.client.core;

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
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.passive.EntityHorse;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import us.ichun.mods.tabula.client.model.ModelInfo;
import us.ichun.mods.tabula.client.model.ModelList;
import us.ichun.mods.tabula.common.core.CommonProxy;

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

        HashMap<Class, RendererLivingEntity> renders = new HashMap<Class, RendererLivingEntity>();
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
                if(render instanceof RendererLivingEntity && clzz != null)
                {
                    renders.put(clzz, (RendererLivingEntity)render);
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
            if(!(rend instanceof RendererLivingEntity))
            {
                compatibleEntities.remove(i);
                continue;
            }
            renders.put(compatibleEntities.get(i), (RendererLivingEntity)rend);
        }

        for(Class clz : compatibleEntities)
        {
            try
            {
                RendererLivingEntity rend = (RendererLivingEntity)renders.get(clz);
                if(clz == EntityPlayer.class)
                {
                    ModelList.models.add(new ModelInfo(AbstractClientPlayer.locationStevePng, rend.mainModel, EntityPlayer.class));
                }
                else if(rend.mainModel != null)
                {
                    EntityLivingBase instance;
                    try { instance = (EntityLivingBase)clz.getConstructor(World.class).newInstance(new Object[] { null }); } catch(Exception e){ instance = null; }
                    try { rend.mainModel.setRotationAngles(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0625F, instance); } catch(Exception e){}
                    try { rend.mainModel.render(instance, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0625F); } catch(Exception e){}
                    try { rend.mainModel.setLivingAnimations(instance, 0.0F, 0.0F, 0.0F); } catch(Exception e){}
                    ResourceLocation loc = null;
                    if(clz != EntityHorse.class)
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
                Field[] fields = rend.getClass().getDeclaredFields();
                for(Field f : fields)
                {
                    f.setAccessible(true);
                    if(ModelBase.class.isAssignableFrom(f.getType()))
                    {
                        ModelBase base = (ModelBase)f.get(rend);
                        if(base != null)
                        {
                            ModelList.models.add(new ModelInfo(null, base, te));
                        }
                    }
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
                Field[] fields = rend.getClass().getDeclaredFields();
                for(Field f : fields)
                {
                    f.setAccessible(true);
                    if(ModelBase.class.isAssignableFrom(f.getType()))
                    {
                        ModelBase base = (ModelBase)f.get(rend);
                        if(base != null)
                        {
                            ModelList.models.add(new ModelInfo(null, base, te));
                        }
                    }
                }
            }
            catch(Exception e1)
            {
            }
        }
    }
}
