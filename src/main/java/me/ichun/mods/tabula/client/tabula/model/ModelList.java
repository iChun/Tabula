package me.ichun.mods.tabula.client.tabula.model;

import com.mojang.blaze3d.matrix.MatrixStack;
import me.ichun.mods.ichunutil.common.util.ObfHelper;
import me.ichun.mods.tabula.common.Tabula;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.LivingRenderer;
import net.minecraft.client.renderer.entity.layers.LayerRenderer;
import net.minecraft.client.renderer.model.Model;
import net.minecraft.client.renderer.model.ModelRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.Fluid;
import net.minecraft.item.crafting.RecipeManager;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.tags.NetworkTagManager;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.*;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.Biomes;
import net.minecraft.world.dimension.DimensionType;
import net.minecraft.world.storage.MapData;
import net.minecraft.world.storage.WorldInfo;
import net.minecraftforge.fml.client.registry.RenderingRegistry;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;
import net.minecraftforge.registries.ForgeRegistries;

import javax.annotation.Nullable;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.*;

public class ModelList
{
    public static boolean hasInit;

    //TODO reevaluate the necessity of these
//    public static ArrayList<ModelInfo> models = new ArrayList<>();
//    public static ArrayList<Class<? extends Model>> modelBlacklist = new ArrayList<>();

    public static void init()
    {
        if(!hasInit)
        {
            hasInit = true;

/*
            ArrayList<Class<? extends Entity>> compatibleEntities = new ArrayList<>();

            for(EntityType entry : ForgeRegistries.ENTITIES.getValues())
            {
                Class clz = entry.getEntityClass();
                if(LivingEntity.class.isAssignableFrom(clz) && !compatibleEntities.contains(clz))
                {
                    compatibleEntities.add(clz);
                }
            }

            HashMap<Class, EntityRenderer> renders = new HashMap<>();
            try
            {
                RenderingRegistry reg = ObfuscationReflectionHelper.getPrivateValue(RenderingRegistry.class, null, "INSTANCE");
                List entityRenderers = ObfuscationReflectionHelper.getPrivateValue(RenderingRegistry.class, reg, "entityRenderers");

                for(Object obj : entityRenderers)
                {
                    Field[] fields = obj.getClass().getDeclaredFields();
                    EntityRenderer render = null;
                    Class clzz = null;
                    for(Field f : fields)
                    {
                        f.setAccessible(true);
                        if(f.getType() == EntityRenderer.class)
                        {
                            render = (EntityRenderer)f.get(obj);
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
                Render rend = Minecraft.getInstance().getRenderManager().getEntityClassRenderObject(compatibleEntities.get(i));
                if(rend != null && rend.getClass() == RenderEntity.class)
                {
                    rend = renders.get(compatibleEntities.get(i));
                }
                renders.put(compatibleEntities.get(i), rend);
            }

            for(Class clz : compatibleEntities)
            {
                EntityRenderer rend1 = renders.get(clz);
                if(!(rend1 instanceof LivingRenderer))
                {
                    continue;
                }
                LivingRenderer rend = (LivingRenderer)rend1;
                mapModelInfo(clz, rend, null);
                renders.remove(clz);
            }

            mapModelInfo(PlayerEntity.class, Minecraft.getInstance().getRenderManager().skinMap.get("default"), new ResourceLocation("textures/entity/steve.png"));
            mapModelInfo(PlayerEntity.class, Minecraft.getInstance().getRenderManager().skinMap.get("slim"), new ResourceLocation("textures/entity/alex.png"));

            Collections.sort(ModelList.models);

            for(Map.Entry<Class, EntityRenderer> e : renders.entrySet())
            {
                try
                {
                    Model base = null;
                    ResourceLocation loc = null;
                    Field[] fields = e.getValue().getClass().getDeclaredFields();
                    for(Field f : fields)
                    {
                        f.setAccessible(true);
                        if(Model.class.isAssignableFrom(f.getType()))
                        {
                            base = (Model)f.get(e.getValue());
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

            for(Object o1 : Minecraft.getInstance().getRenderManager().renderers.entrySet())
            {
                Map.Entry e = (Map.Entry)o1;
                Class te = (Class)e.getKey();
                EntityRenderer rend = (EntityRenderer)e.getValue();

                if(LivingRenderer.class.isAssignableFrom(rend.getClass()))
                {
                    continue;
                }

                try
                {
                    Model base = null;
                    ResourceLocation loc = null;
                    Field[] fields = rend.getClass().getDeclaredFields();
                    for(Field f : fields)
                    {
                        f.setAccessible(true);
                        if(Model.class.isAssignableFrom(f.getType()))
                        {
                            base = (Model)f.get(rend);
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
                    Model base = null;
                    ResourceLocation loc = null;
                    Field[] fields = rend.getClass().getDeclaredFields();
                    for(Field f : fields)
                    {
                        f.setAccessible(true);
                        if(Model.class.isAssignableFrom(f.getType()))
                        {
                            base = (Model)f.get(rend);
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

            //TODO import block and item models???

            for(int i = ModelList.models.size() - 1; i >= 0 ; i--)
            {
                ModelInfo info = ModelList.models.get(i);
                if(ModelList.modelBlacklist.contains(info.modelParent.getClass()))
                {
                    ModelList.models.remove(i);
                }
            }*/

        }
    }

    //TODO IEntityRenderer?
    public static void mapModelInfo(Class<? extends LivingEntity> clz, final LivingRenderer rend, ResourceLocation loc)
    {
        /*try
        {
            if(rend.entityModel != null && clz != null)
            {
                if(clz != PlayerEntity.class)
                {
                    LivingEntity instance;
                    try
                    {
                        instance = clz.getConstructor(World.class).newInstance(new Object[] { null });
                    }
                    catch(InvocationTargetException e)
                    {
                        try
                        {
                            instance = clz.getConstructor(World.class).newInstance(new World(new WorldInfo(new WorldSettings(0L, GameType.CREATIVE, false, false, WorldType.CUSTOMIZED), "Tabula"), DimensionType.OVERWORLD, ((world, dimension) -> null), null, false) {
                                @Override
                                public void notifyBlockUpdate(BlockPos pos, BlockState oldState, BlockState newState, int flags){}

                                @Override
                                public void playSound(@Nullable PlayerEntity player, double x, double y, double z, SoundEvent soundIn, SoundCategory category, float volume, float pitch){}

                                @Override
                                public void playMovingSound(@Nullable PlayerEntity playerIn, Entity entityIn, SoundEvent eventIn, SoundCategory categoryIn, float volume, float pitch){}

                                @Nullable
                                @Override
                                public Entity getEntityByID(int id) { return null;}

                                @Nullable
                                @Override
                                public MapData getMapData(String mapName){ return null;}

                                @Override
                                public void registerMapData(MapData mapDataIn){}

                                @Override
                                public int getNextMapId() {return 0;}

                                @Override
                                public void sendBlockBreakProgress(int breakerId, BlockPos pos, int progress){}

                                @Override
                                public Scoreboard getScoreboard() { return null;}

                                @Override
                                public RecipeManager getRecipeManager() { return null;}

                                @Override
                                public NetworkTagManager getTags() { return null;}

                                @Override
                                public ITickList<Block> getPendingBlockTicks() { return EmptyTickList.get();}

                                @Override
                                public ITickList<Fluid> getPendingFluidTicks() { return EmptyTickList.get();}

                                @Override
                                public void playEvent(@Nullable PlayerEntity player, int type, BlockPos pos, int data){}

                                @Override
                                public List<? extends PlayerEntity> getPlayers()
                                {
                                    return new ArrayList<>();
                                }

                                @Override
                                public Biome getNoiseBiomeRaw(int x, int y, int z)
                                {
                                    return Biomes.OCEAN;
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
                    if(Tabula.configClient.animateImports)
                    {
                        try
                        {
                            rend.entityModel.setLivingAnimations(instance, 0.0F, 0.0F, 0.0F);
                        }
                        catch(Exception ignored){}
                        try
                        {
                            rend.entityModel.setRotationAngles(instance, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F);
                        }
                        catch(Exception ignored){}
                        try
                        {
                            rend.entityModel.render(new MatrixStack(), Minecraft.getInstance().getRenderTypeBuffers().getBufferSource().getBuffer(RenderType.getCutout()), 15728880, OverlayTexture.NO_OVERLAY, 1F, 1F, 1F, 1F);
                        }
                        catch(Exception ignored){}
                    }
                    if(loc == null) //horse gives some kind of error that can't be silenced
                    {
                        try
                        {
                            loc = ObfHelper.getEntityTexture(rend, rend.getClass(), instance);
                        }
                        catch(Exception ignored){}
                    }
                }
                ModelList.models.add(new ModelInfo(loc, rend.entityModel, clz));

                ArrayList<Model> modelsToCompare = new ArrayList<Model>() {{ add(rend.entityModel); }};
                for(int i = 0; i < rend.layerRenderers.size(); i++)
                {
                    LayerRenderer layer = (LayerRenderer)rend.layerRenderers.get(i);
                    Field[] fields = layer.getClass().getDeclaredFields();
                    ResourceLocation loc1 = null;
                    Model base = null;
                    for(Field f : fields)
                    {
                        f.setAccessible(true);
                        if(Model.class.isAssignableFrom(f.getType()))
                        {
                            base = (Model)f.get(layer);
                        }
                        if(loc1 == null && ResourceLocation.class.isAssignableFrom(f.getType()))
                        {
                            loc1 = (ResourceLocation)f.get(layer);
                        }
                    }
                    if(base != null)
                    {
                        boolean add = true;
                        for(Model model : modelsToCompare)
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
        }*/
    }
/*

    public static class ModelInfo
            implements Comparable<ModelInfo>
    {
        public final ResourceLocation texture;
        public final Model modelParent;
        public final HashMap<String, ModelRenderer> modelList;
        public final Class clz;

        public ModelInfo(ResourceLocation texture, Model modelParent, Class clz)
        {
            this.texture = texture;
            this.modelParent = modelParent;
            this.modelList = ModelHelper.getModelCubesWithNames(modelParent);
            this.clz = clz;
        }

        @Override
        public int compareTo(ModelInfo info)
        {
            return modelParent.getClass().getSimpleName().compareTo(info.modelParent.getClass().getSimpleName());
        }
    }
*/

}
