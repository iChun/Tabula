package me.ichun.mods.tabula.old.client.core;

import me.ichun.mods.ichunutil.client.model.util.ModelHelper;
import me.ichun.mods.ichunutil.client.module.tabula.model.ModelInfo;
import me.ichun.mods.ichunutil.client.module.tabula.model.ModelList;
import me.ichun.mods.ichunutil.common.core.util.ObfHelper;
import me.ichun.mods.tabula.old.client.mainframe.core.ProjectHelper;
import me.ichun.mods.tabula.old.client.render.TileRendererTabulaRasa;
import me.ichun.mods.tabula.old.common.Tabula;
import me.ichun.mods.tabula.old.common.core.ProxyCommon;
import me.ichun.mods.tabula.old.common.tileentity.TileEntityTabulaRasa;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.ModelBase;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderEntity;
import net.minecraft.client.renderer.entity.RenderLivingBase;
import net.minecraft.client.renderer.entity.layers.LayerRenderer;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
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
