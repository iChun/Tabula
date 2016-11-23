package us.ichun.mods.tabula.common.core;

import me.ichun.mods.ichunutil.common.core.network.PacketChannel;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.fml.common.registry.GameRegistry;
import us.ichun.mods.ichunutil.common.core.network.ChannelHandler;
import us.ichun.mods.tabula.client.core.TickHandlerClient;
import us.ichun.mods.tabula.common.Tabula;
import us.ichun.mods.tabula.common.block.BlockTabulaRasa;
import us.ichun.mods.tabula.common.packet.*;
import us.ichun.mods.tabula.common.tileentity.TileEntityTabulaRasa;

public class CommonProxy
{
    public TickHandlerClient tickHandlerClient;

    public void preInit()
    {
        Tabula.blockTabulaRasa = (new BlockTabulaRasa(Material.CIRCUITS)).setHardness(0.0F).setCreativeTab(CreativeTabs.DECORATIONS).setStepSound(Block.soundTypeWood).setUnlocalizedName("tabula.block.tabularasa");

        GameRegistry.registerBlock(Tabula.blockTabulaRasa, "TabulaRasa");

        GameRegistry.addRecipe(new ItemStack(Tabula.blockTabulaRasa, 1),"#", "S", '#', Items.GHAST_TEAR, 'S', Blocks.WOODEN_PRESSURE_PLATE);

        registerTileEntity(TileEntityTabulaRasa.class, "Tabula_TabulaRasa");

        Tabula.channel = new PacketChannel(Tabula.MOD_NAME, PacketRequestSession.class, PacketBeginSession.class, PacketEndSession.class, PacketAddListener.class, PacketRemoveListener.class,
                PacketChat.class, PacketChatMessage.class, PacketPingAlive.class, PacketIsEditor.class, PacketRequestHeartbeat.class,
                PacketHeartbeat.class, PacketProjectFragment.class, PacketCloseProject.class, PacketRequestProject.class, PacketSetCurrentProject.class,
                PacketGenericMethod.class, PacketProjectFragmentFromClient.class, PacketClearTexture.class, PacketListenersList.class, PacketSetProjectMetadata.class);
    }

    public void init(){}

    public void postInit(){}

    public void registerTileEntity(Class<? extends TileEntity> clz, String id)
    {
        GameRegistry.registerTileEntity(clz, id);
    }

    public void updateProject(boolean fromClient, String ident, boolean isTexture, boolean updateDims){}
}
