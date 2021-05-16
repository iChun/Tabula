package me.ichun.mods.tabula.client.gui.window.popup;

import com.mojang.blaze3d.matrix.MatrixStack;
import me.ichun.mods.ichunutil.client.gui.bns.Workspace;
import me.ichun.mods.ichunutil.client.gui.bns.window.Window;
import me.ichun.mods.ichunutil.client.gui.bns.window.constraint.Constraint;
import me.ichun.mods.ichunutil.client.gui.bns.window.view.View;
import me.ichun.mods.ichunutil.client.gui.bns.window.view.element.*;
import me.ichun.mods.ichunutil.client.model.util.ModelHelper;
import me.ichun.mods.ichunutil.client.render.RenderHelper;
import me.ichun.mods.ichunutil.common.module.tabula.project.Project;
import me.ichun.mods.tabula.client.gui.WorkspaceTabula;
import me.ichun.mods.tabula.common.Tabula;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.HorseRenderer;
import net.minecraft.client.renderer.entity.LivingRenderer;
import net.minecraft.client.renderer.entity.layers.LayerRenderer;
import net.minecraft.client.renderer.entity.model.EntityModel;
import net.minecraft.client.renderer.entity.model.PlayerModel;
import net.minecraft.client.renderer.model.Model;
import net.minecraft.client.renderer.model.ModelRenderer;
import net.minecraft.client.renderer.model.RenderMaterial;
import net.minecraft.client.renderer.texture.NativeImage;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.tileentity.TileEntityRenderer;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.Fluid;
import net.minecraft.item.crafting.RecipeManager;
import net.minecraft.profiler.EmptyProfiler;
import net.minecraft.resources.IResource;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.tags.ITagCollectionSupplier;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.DynamicRegistries;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.*;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.Biomes;
import net.minecraft.world.chunk.AbstractChunkProvider;
import net.minecraft.world.storage.ISpawnWorldInfo;
import net.minecraft.world.storage.MapData;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class WindowImportMCProject extends Window<WorkspaceTabula>
{
    public static final ArrayList<ModelInfo> MODELS = new ArrayList<>();
    public static class ModelInfo
            implements Comparable<ModelInfo>
    {
        @Nullable
        public final ResourceLocation texture;
        @Nonnull
        public final Object model; //doesn't extend Model because some TileEntityRenderers may be the models.
        @Nullable
        public final Object source;

        @Nullable
        public Class<? extends Entity> originEntity;
        @Nullable
        public String origin = null;

        public ModelInfo(@Nullable ResourceLocation texture, @Nonnull Object model, @Nullable Object source)
        {
            this.texture = texture;
            this.model = model;
            this.source = source;
        }

        public String getSourceTypeName()
        {
            if(source instanceof EntityRenderer)
            {
                return "EntityRenderer";
            }
            else if(source instanceof LayerRenderer)
            {
                return "LayerRenderer";
            }
            else if(source instanceof TileEntityRenderer)
            {
                return "TileEntityRenderer";
            }
            else if(source != null)
            {
                return source.getClass().getSimpleName();
            }
            return "Unknown";
        }

        @Override
        public int compareTo(ModelInfo o)
        {
            return getSourceTypeName().equals(o.getSourceTypeName()) ? model.getClass().getSimpleName().compareTo(o.model.getClass().getSimpleName()) : getSourceTypeName().compareTo(o.getSourceTypeName());
        }

        @Override
        public boolean equals(Object obj)
        {
            return obj instanceof ModelInfo && (texture == null && ((ModelInfo)obj).texture == null || texture != null && texture.equals(((ModelInfo)obj).texture)) && model.equals(((ModelInfo)obj).model) && (source == null && ((ModelInfo)obj).source == null || source != null && source.equals(((ModelInfo)obj).source));
        }

        public void originEntity(Class<? extends Entity> aClass)
        {
            this.originEntity = aClass;
        }
    }

    private static boolean hasInit;
    public static void initModels()
    {
        if(!hasInit)
        {
            hasInit = true;
            //Hackery
            World worldInstance = new World(new ISpawnWorldInfo()
            {
                @Override
                public int getSpawnX()
                {
                    return 0;
                }

                @Override
                public int getSpawnY()
                {
                    return 0;
                }

                @Override
                public int getSpawnZ()
                {
                    return 0;
                }

                @Override
                public float getSpawnAngle()
                {
                    return 0;
                }

                @Override
                public long getGameTime()
                {
                    return 0;
                }

                @Override
                public long getDayTime()
                {
                    return 0;
                }

                @Override
                public boolean isThundering()
                {
                    return false;
                }

                @Override
                public boolean isRaining()
                {
                    return false;
                }

                @Override
                public void setRaining(boolean isRaining)
                {

                }

                @Override
                public boolean isHardcore()
                {
                    return false;
                }

                @Override
                public GameRules getGameRulesInstance()
                {
                    return new GameRules();
                }

                @Override
                public Difficulty getDifficulty()
                {
                    return Difficulty.HARD;
                }

                @Override
                public boolean isDifficultyLocked()
                {
                    return true;
                }

                @Override
                public void setSpawnX(int x)
                {

                }

                @Override
                public void setSpawnY(int y)
                {

                }

                @Override
                public void setSpawnZ(int z)
                {

                }

                @Override
                public void setSpawnAngle(float angle)
                {

                }
            }, World.OVERWORLD, DimensionType.OVERWORLD_TYPE, () -> EmptyProfiler.INSTANCE, false, false, 0L) {
                @Override
                public DynamicRegistries func_241828_r()
                {
                    return DynamicRegistries.func_239770_b_();
                }

                @Override
                public float func_230487_a_(Direction p_230487_1_, boolean p_230487_2_)
                {
                    return 0;
                }

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
                public Scoreboard getScoreboard() { return new Scoreboard();}

                @Override
                public RecipeManager getRecipeManager() { return new RecipeManager();}

                @Override
                public ITagCollectionSupplier getTags() { return ITagCollectionSupplier.TAG_COLLECTION_SUPPLIER;}

                @Override
                public ITickList<Block> getPendingBlockTicks() { return EmptyTickList.get();}

                @Override
                public ITickList<Fluid> getPendingFluidTicks() { return EmptyTickList.get();}

                @Override
                public AbstractChunkProvider getChunkProvider() { return null;} //this might crash things?

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
                    return this.func_241828_r().getRegistry(Registry.BIOME_KEY).getOrThrow(Biomes.PLAINS);
                }
            };

            Map<EntityType<?>, EntityRenderer<?>> renderers = Minecraft.getInstance().getRenderManager().renderers;
            renderers.forEach((entityType, entityRenderer) -> {
                try
                {
                    Entity instance = null;
                    try
                    {
                        instance = entityType.create(worldInstance);
                    }
                    catch(Throwable ignored){}

                    ResourceLocation texLoc = null;
                    if(entityRenderer instanceof HorseRenderer)
                    {
                        texLoc = new ResourceLocation("textures/entity/horse/horse_gray.png");
                    }
                    else
                    {
                        if(instance != null)
                        {
                            try
                            {
                                texLoc = RenderHelper.getEntityTexture(entityRenderer, instance);
                            }
                            catch(Throwable ignored)
                            {
                            }
                        }


                        if(texLoc == null)
                        {
                            try
                            {
                                Class<?> clz = entityRenderer.getClass();
                                while(clz != EntityRenderer.class && texLoc == null)
                                {
                                    Field[] fields = clz.getDeclaredFields();
                                    for(Field f : fields)
                                    {
                                        f.setAccessible(true);
                                        if(ResourceLocation.class.isAssignableFrom(f.getType()))
                                        {
                                            texLoc = (ResourceLocation)f.get(entityRenderer);
                                            break;
                                        }
                                    }

                                    clz = clz.getSuperclass();
                                }
                            }
                            catch(Throwable ignored)
                            {
                            }
                        }
                    }

                    //we have the texture. let's get all the models now.
                    try
                    {
                        Class<?> clz = entityRenderer.getClass();
                        while(clz != EntityRenderer.class)
                        {
                            Field[] fields = clz.getDeclaredFields();
                            for(Field f : fields)
                            {
                                f.setAccessible(true);
                                if(Model.class.isAssignableFrom(f.getType()))
                                {
                                    //we found a model.
                                    Model model = (Model)f.get(entityRenderer);

                                    if(model != null)
                                    {
                                        if(model instanceof EntityModel && Tabula.configClient.animateImports)
                                        {
                                            EntityModel entityModel = (EntityModel)model;
                                            try
                                            {
                                                entityModel.setLivingAnimations(instance, 0F, 0F, 0F);
                                            }
                                            catch(Throwable ignored)
                                            {
                                            }
                                            try
                                            {
                                                entityModel.setRotationAngles(instance, 0F, 0F, 0F, 0F, 0F);
                                            }
                                            catch(Throwable ignored)
                                            {
                                            }
                                            try
                                            {
                                                MatrixStack stack = new MatrixStack();
                                                stack.translate(0D, -500D, 0D);
                                                entityModel.render(stack, Minecraft.getInstance().getRenderTypeBuffers().getBufferSource().getBuffer(RenderType.getCutout()), 15728880, OverlayTexture.NO_OVERLAY, 1F, 1F, 1F, 0F);
                                            }
                                            catch(Throwable ignored)
                                            {
                                            }
                                        }

                                        ModelInfo info = new ModelInfo(texLoc, model, entityRenderer);
                                        info.originEntity(instance.getClass());
                                        info.origin = entityType.getRegistryName().getNamespace();
                                        if(!MODELS.contains(info))
                                        {
                                            MODELS.add(info);
                                        }
                                    }
                                }
                            }

                            clz = clz.getSuperclass();
                        }
                    }
                    catch(Throwable ignored){}

                    if(entityRenderer instanceof LivingRenderer)
                    {
                        LivingRenderer renderer = (LivingRenderer)entityRenderer;
                        for(Object o : renderer.layerRenderers)
                        {
                            LayerRenderer layer = (LayerRenderer)o;
                            ResourceLocation texLoc1 = null;
                            try
                            {
                                Class<?> clz = layer.getClass();
                                while(clz != LayerRenderer.class && texLoc1 == null)
                                {
                                    Field[] fields = clz.getDeclaredFields();
                                    for(Field f : fields)
                                    {
                                        f.setAccessible(true);
                                        if(ResourceLocation.class.isAssignableFrom(f.getType()))
                                        {
                                            texLoc1 = (ResourceLocation)f.get(layer);
                                            break;
                                        }
                                    }

                                    clz = clz.getSuperclass();
                                }
                            }
                            catch(Throwable ignored){}

                            //we have the texture. let's get all the models now.
                            try
                            {
                                Class<?> clz = layer.getClass();
                                while(clz != LayerRenderer.class)
                                {
                                    Field[] fields = clz.getDeclaredFields();
                                    for(Field f : fields)
                                    {
                                        f.setAccessible(true);
                                        if(Model.class.isAssignableFrom(f.getType()))
                                        {
                                            //we found a model.
                                            Model model = (Model)f.get(layer);

                                            if(model != null)
                                            {
                                                ModelInfo info = new ModelInfo(texLoc1, model, layer);
                                                info.origin = texLoc1.getNamespace();
                                                if(!MODELS.contains(info))
                                                {
                                                    MODELS.add(info);
                                                }
                                            }
                                        }
                                    }

                                    clz = clz.getSuperclass();
                                }
                            }
                            catch(Throwable ignored){}
                        }
                    }
                }
                catch(Throwable ignored){}
            });

            Map<TileEntityType<?>, TileEntityRenderer<?>> tileRenderers = TileEntityRendererDispatcher.instance.renderers;
            tileRenderers.forEach((tileEntityType, tileEntityRenderer) -> {
                try
                {
                    ResourceLocation texLoc = null;
                    try
                    {
                        Class<?> clz = tileEntityRenderer.getClass();
                        while(clz != TileEntityRenderer.class && texLoc == null)
                        {
                            Field[] fields = clz.getDeclaredFields();
                            for(Field f : fields)
                            {
                                f.setAccessible(true);
                                if(ResourceLocation.class.isAssignableFrom(f.getType()))
                                {
                                    texLoc = (ResourceLocation)f.get(tileEntityRenderer);
                                    break;
                                }
                                else if(RenderMaterial.class.isAssignableFrom(f.getType()))
                                {
                                    ResourceLocation loc = ((RenderMaterial)f.get(tileEntityRenderer)).getTextureLocation();
                                    texLoc = new ResourceLocation(loc.getNamespace(), "textures/" + loc.getPath() + ".png");
                                    break;
                                }
                            }

                            clz = clz.getSuperclass();
                        }
                    }
                    catch(Throwable ignored)
                    {
                        ignored.printStackTrace();
                    }


                    //we have the texture. let's get all the models now.
                    boolean rendererIsModel = false;
                    try
                    {
                        Class<?> clz = tileEntityRenderer.getClass();
                        while(clz != TileEntityRenderer.class)
                        {
                            Field[] fields = clz.getDeclaredFields();
                            for(Field f : fields)
                            {
                                f.setAccessible(true);
                                if(Model.class.isAssignableFrom(f.getType()))
                                {
                                    //we found a model.
                                    Model model = (Model)f.get(tileEntityRenderer);

                                    if(model != null)
                                    {
                                        ModelInfo info = new ModelInfo(texLoc, model, tileEntityRenderer);
                                        info.origin = texLoc.getNamespace();
                                        if(!MODELS.contains(info))
                                        {
                                            MODELS.add(info);
                                        }
                                    }
                                }
                                else if(ModelRenderer.class.isAssignableFrom(f.getType()))
                                {
                                    rendererIsModel = true;
                                }
                            }

                            clz = clz.getSuperclass();
                        }
                    }
                    catch(Throwable ignored){}

                    if(rendererIsModel)
                    {
                        ModelInfo info = new ModelInfo(texLoc, tileEntityRenderer, tileEntityRenderer);
                        info.origin = texLoc.getNamespace();
                        if(!MODELS.contains(info))
                        {
                            MODELS.add(info);
                        }
                    }
                }
                catch(Throwable ignored){}
            });

            ModelInfo playerInfo = new ModelInfo(new ResourceLocation("textures/entity/steve.png"), new PlayerModel<>(0F, false), Minecraft.getInstance().getRenderManager().skinMap.get("default"));
            playerInfo.originEntity = PlayerEntity.class;
            playerInfo.origin = "minecraft";
            MODELS.add(playerInfo);

            playerInfo = new ModelInfo(new ResourceLocation("textures/entity/alex.png"), new PlayerModel<>(0F, true), Minecraft.getInstance().getRenderManager().skinMap.get("slim"));
            playerInfo.originEntity = PlayerEntity.class;
            playerInfo.origin = "minecraft";
            MODELS.add(playerInfo);

            MODELS.removeIf(info -> {
                for(Class<? extends Model> aClass : Tabula.modelBlacklist)
                {
                    if(aClass.isAssignableFrom(info.model.getClass()))
                    {
                        return true;
                    }
                }
                return false;
            });

            Collections.sort(MODELS);

            Workspace.registerObjectInterpreter(ModelInfo.class, o -> {
                ModelInfo info = (ModelInfo)o;
                ArrayList<String> strings = new ArrayList<>();
                strings.add(info.model.getClass().getSimpleName() + " - " + info.source.getClass().getSimpleName() + (info.origin != null ? " - " + info.origin : ""));
                return strings;
            });
        }
    }

    public WindowImportMCProject(WorkspaceTabula parent)
    {
        super(parent);

        setView(new ViewImportMCProject(this));
        disableDockingEntirely();
    }

    public static class ViewImportMCProject extends View<WindowImportMCProject>
    {
        public ElementList<?> list;
        public boolean populatedList;
        public boolean renderedOnce;

        public ViewImportMCProject(@Nonnull WindowImportMCProject parent)
        {
            super(parent, "window.importMC.title");

            ElementScrollBar<?> sv = new ElementScrollBar<>(this, ElementScrollBar.Orientation.VERTICAL, 0.6F);
            sv.setConstraint(new Constraint(sv).top(this, Constraint.Property.Type.TOP, 0)
                    .bottom(this, Constraint.Property.Type.BOTTOM, 40) // 10 + 20 + 10, bottom + button height + padding
                    .right(this, Constraint.Property.Type.RIGHT, 0)
            );
            elements.add(sv);

            list = new ElementList<>(this).setScrollVertical(sv);
            list.setConstraint(new Constraint(list).bottom(this, Constraint.Property.Type.BOTTOM, 40)
                    .left(this, Constraint.Property.Type.LEFT, 0).right(sv, Constraint.Property.Type.LEFT, 0)
                    .top(this, Constraint.Property.Type.TOP, 0));
            elements.add(list);

            ElementToggle<?> toggle = new ElementToggle<>(this, "window.importMC.newProject", btn -> {});
            toggle.setToggled(true).setTooltip(I18n.format("window.importMC.newProjectFull")).setSize(60, 20).setId("buttonProject");
            toggle.setConstraint(new Constraint(toggle).bottom(this, Constraint.Property.Type.BOTTOM, 10).left(this, Constraint.Property.Type.LEFT, 10));
            elements.add(toggle);

            ElementToggle<?> toggle1 = new ElementToggle<>(this, "window.import.texture", btn -> {});
            toggle1.setToggled(true).setTooltip(I18n.format("window.import.textureFull")).setSize(60, 20).setId("buttonTexture");
            toggle1.setConstraint(new Constraint(toggle1).bottom(this, Constraint.Property.Type.BOTTOM, 10).left(toggle, Constraint.Property.Type.RIGHT, 10));
            elements.add(toggle1);

            ElementButton<?> button = new ElementButton<>(this, I18n.format("gui.cancel"), btn ->
            {
                getWorkspace().removeWindow(parent);
            });
            button.setSize(60, 20);
            button.setConstraint(new Constraint(button).bottom(this, Constraint.Property.Type.BOTTOM, 10).right(this, Constraint.Property.Type.RIGHT, 10));
            elements.add(button);

            ElementButton<?> button1 = new ElementButton<>(this, I18n.format("gui.ok"), btn ->
            {
                for(ElementList.Item<?> item : list.items)
                {
                    if(item.selected)
                    {
                        loadModel((ModelInfo)item.getObject());
                        return;
                    }
                }
            });
            button1.setSize(60, 20);
            button1.setConstraint(new Constraint(button1).right(button, Constraint.Property.Type.LEFT, 10));
            elements.add(button1);

            ElementTextField textField = new ElementTextField(this);
            textField.setId("search");
            textField.setResponder(s -> {
                this.list.items.clear();
                this.populatedList = false;
            }).setHeight(14);
            textField.setConstraint(new Constraint(textField).left(toggle1, Constraint.Property.Type.RIGHT, 10).right(button1, Constraint.Property.Type.LEFT, 10).bottom(button1, Constraint.Property.Type.BOTTOM, 3));
            elements.add(textField);

            ElementTextWrapper text = new ElementTextWrapper(this);
            text.setText("Populating list...").setConstraint(new Constraint(text).left(this, Constraint.Property.Type.LEFT, 2).right(this, Constraint.Property.Type.RIGHT, 2)).setId("textPopulating");
            elements.add(text);
        }

        @Override
        public void render(MatrixStack stack, int mouseX, int mouseY, float partialTick)
        {
            super.render(stack, mouseX, mouseY, partialTick);

            if(!hasInit)
            {
                if(!renderedOnce)
                {
                    renderedOnce = true;
                    return;
                }
                initModels();
            }

            if(!populatedList)
            {
                populatedList = true;

                MODELS.forEach(modelInfo -> {
                    String search = ((ElementTextField)getWorkspace().getById("search")).getText();
                    String modelName = modelInfo.model.getClass().getSimpleName() + " - " + modelInfo.source.getClass().getSimpleName();
                    if(search.isEmpty() || modelName.toLowerCase().contains(search.toLowerCase()))
                    {
                        list.addItem(modelInfo).setDefaultAppearance().setDoubleClickHandler(item -> {
                            if(item.selected)
                            {
                                loadModel(item.getObject());
                            }
                        });
                    }
                });

                ((ElementTextWrapper)getById("textPopulating")).setText("");
                getById("textPopulating").init();

                list.init();
                list.init();
            }
        }

        public void loadModel(ModelInfo model)
        {
            parentFragment.parent.removeWindow(parentFragment);

            boolean texture = ((ElementToggle)getById("buttonTexture")).toggleState;

            Project project = ModelHelper.convertModelToProject(model.model);
            if(model.originEntity != null)
            {
                project.notes.add("suspected-origin-entity:" + model.originEntity.getName());
            }
            if(texture && model.texture != null)
            {
                try
                {
                    IResource iresource = Minecraft.getInstance().getResourceManager().getResource(model.texture);
                    NativeImage img = NativeImage.read(iresource.getInputStream());
                    project.setImageBytes(img.getBytes());
                    project.texWidth = img.getWidth();
                    project.texHeight = img.getHeight();
                    img.close();
                }
                catch(IOException ignored){}
            }

            if(((ElementToggle)getById("buttonProject")).toggleState || parentFragment.parent.mainframe.getActiveProject() == null)
            {
                parentFragment.parent.mainframe.openProject(project, true);
            }
            else
            {
                parentFragment.parent.mainframe.importProject(project, texture, true);
            }
        }
    }
}
