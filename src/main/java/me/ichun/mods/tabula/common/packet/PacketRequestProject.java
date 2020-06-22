package me.ichun.mods.tabula.common.packet;

import me.ichun.mods.ichunutil.common.module.tabula.project.Project;
import me.ichun.mods.ichunutil.common.network.AbstractPacket;
import me.ichun.mods.ichunutil.common.util.IOUtil;
import me.ichun.mods.tabula.common.Tabula;
import me.ichun.mods.tabula.common.tileentity.TileEntityTabulaRasa;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.network.NetworkEvent;
import org.apache.commons.lang3.RandomStringUtils;

import java.io.IOException;

public class PacketRequestProject extends AbstractPacket
{
    public BlockPos pos;

    public PacketRequestProject(){}

    public PacketRequestProject(BlockPos pos)
    {
        this.pos = pos;
    }

    @Override
    public void writeTo(PacketBuffer buf)
    {
        buf.writeBlockPos(pos);
    }

    @Override
    public void readFrom(PacketBuffer buf)
    {
        pos = buf.readBlockPos();
    }

    @Override
    public void process(NetworkEvent.Context context)
    {
        context.enqueueWork(() -> {
            ServerPlayerEntity player = context.getSender();
            World world = player.world;
            TileEntity tileEntity = world.getTileEntity(pos);
            if(tileEntity instanceof TileEntityTabulaRasa)
            {
                TileEntityTabulaRasa tabulaRasa = (TileEntityTabulaRasa)tileEntity;
                if(tabulaRasa.projectString != null)
                {
                    sendContent("project", tabulaRasa.projectString, player);
                    if(tabulaRasa.projectImage != null)
                    {
                        sendContent("image", tabulaRasa.projectImage, player);
                    }
                }
            }
        });
    }

    public void sendContent(String projIdent, Object o, ServerPlayerEntity requester) //empty string for directed to send to all
    {
        byte[] data;
        if(projIdent.equals("image")) //sending out an image
        {
            data = (byte[])o;
        }
        else
        {
            try
            {
                data = IOUtil.compress(o.toString());
            }
            catch(IOException e){return;}
        }

        final int maxFile = 31000; //smaller packet cause I'm worried about too much info carried over from the bloat vs hat info.

        String fileName = RandomStringUtils.randomAscii(Project.IDENTIFIER_LENGTH);
        int fileSize = data.length;

        int packetsToSend = (int)Math.ceil((float)fileSize / (float)maxFile);

        int packetCount = 0;
        int offset = 0;
        while(fileSize > 0)
        {
            byte[] fileBytes = new byte[Math.min(fileSize, maxFile)];
            int index = 0;
            while(index < fileBytes.length) //from index 0 to 31999
            {
                fileBytes[index] = data[index + offset];
                index++;
            }

            Tabula.channel.sendTo(new PacketProjectFragment(fileName, packetsToSend, packetCount, fileBytes, pos, "", projIdent, "", (byte)0, (byte)-1), requester);

            packetCount++;
            fileSize -= 32000;
            offset += index;
        }
    }
}
