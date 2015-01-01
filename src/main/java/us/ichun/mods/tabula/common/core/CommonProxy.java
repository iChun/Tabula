package us.ichun.mods.tabula.common.core;

import cpw.mods.fml.common.registry.GameRegistry;
import ichun.common.core.network.ChannelHandler;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import us.ichun.mods.tabula.client.core.TickHandlerClient;
import us.ichun.mods.tabula.common.Tabula;
import us.ichun.mods.tabula.common.block.BlockTabulaRasa;
import us.ichun.mods.tabula.common.packet.*;
import us.ichun.mods.tabula.common.tileentity.TileEntityTabulaRasa;
import us.ichun.module.tabula.common.project.ProjectInfo;

public class CommonProxy
{
    public TickHandlerClient tickHandlerClient;

    public void init()
    {
        Tabula.blockTabulaRasa = (new BlockTabulaRasa(Material.wood)).setHardness(0.0F).setCreativeTab(CreativeTabs.tabDecorations).setStepSound(Block.soundTypeWood).setBlockName("tabula.block.tabularasa");

        GameRegistry.registerBlock(Tabula.blockTabulaRasa, "Tabula_TabulaRasa");

        GameRegistry.addRecipe(new ItemStack(Tabula.blockTabulaRasa, 1),"#", "S", Character.valueOf('#'), Items.ghast_tear, Character.valueOf('S'), Blocks.wooden_pressure_plate);

        registerTileEntity(TileEntityTabulaRasa.class, "Tabula_TabulaRasa");

        Tabula.channels = ChannelHandler.getChannelHandlers("Tabula", PacketRequestSession.class, PacketBeginSession.class, PacketEndSession.class, PacketAddListener.class, PacketRemoveListener.class,
                PacketChat.class, PacketChatMessage.class, PacketPingAlive.class, PacketIsEditor.class, PacketRequestHeartbeat.class,
                PacketHeartbeat.class, PacketProjectFragment.class, PacketCloseProject.class, PacketRequestProject.class, PacketSetCurrentProject.class,
                PacketGenericMethod.class, PacketProjectFragmentFromClient.class, PacketClearTexture.class);
    }

    public void postInit(){}

    public void registerTileEntity(Class<? extends TileEntity> clz, String id)
    {
        GameRegistry.registerTileEntity(clz, id);
    }

    public void updateProject(boolean fromClient, String ident, boolean isTexture, boolean updateDims){}
}
