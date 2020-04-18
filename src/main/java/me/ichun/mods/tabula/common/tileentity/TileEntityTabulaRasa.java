package me.ichun.mods.tabula.common.tileentity;

import me.ichun.mods.tabula.common.Tabula;
import net.minecraft.block.BlockState;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SUpdateTileEntityPacket;
import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;

public class TileEntityTabulaRasa extends TileEntity
        implements ITickableTileEntity
{
    public Direction facing;
    public String host;

    public int age;

    public TileEntityTabulaRasa()
    {
        super(Tabula.TileEntityTypes.TABULA_RASA.get());
        host = "";
    }

    @Override
    public void tick()
    {
        age++;
    }

    @Override
    public void onDataPacket(NetworkManager net, SUpdateTileEntityPacket pkt)
    {
        read(pkt.getNbtCompound());

        BlockState state = world.getBlockState(getPos());
        world.notifyBlockUpdate(getPos(), state, state, 3);
    }

    @Override
    public CompoundNBT getUpdateTag()
    {
        return this.write(new CompoundNBT());
    }

    @Override
    public SUpdateTileEntityPacket getUpdatePacket()
    {
        return new SUpdateTileEntityPacket(getPos(), 0, getUpdateTag());
    }

    @Override
    public CompoundNBT write(CompoundNBT tag)
    {
        super.write(tag);
        tag.putByte("facing", (byte)facing.getHorizontalIndex());
        tag.putString("host", host);
        return tag;
    }

    @Override
    public void read(CompoundNBT tag)
    {
        super.read(tag);
        facing = Direction.byHorizontalIndex(tag.getByte("facing"));
        host = tag.getString("host");
    }
}
