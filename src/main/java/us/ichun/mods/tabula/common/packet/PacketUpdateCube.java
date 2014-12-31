package us.ichun.mods.tabula.common.packet;

import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.network.ByteBufUtils;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import ichun.common.core.network.AbstractPacket;
import ichun.common.core.network.PacketHandler;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import us.ichun.mods.tabula.common.Tabula;

public class PacketUpdateCube extends AbstractPacket
{
    public String host;
    public String projIdent;
    public String cubeJson;
    public String animIdent;
    public String tlIdent;
    public int curPos;

    public PacketUpdateCube(){}

    public PacketUpdateCube(String host, String projIdent, String cubeJson, String animIdent, String tlIdent, int curPos)
    {
        this.host = host;
        this.projIdent = projIdent;
        this.cubeJson = cubeJson;
        this.animIdent = animIdent;
        this.tlIdent = tlIdent;
        this.curPos = curPos;
    }

    @Override
    public void writeTo(ByteBuf buffer, Side side)
    {
        ByteBufUtils.writeUTF8String(buffer, host);
        ByteBufUtils.writeUTF8String(buffer, projIdent);
        ByteBufUtils.writeUTF8String(buffer, cubeJson);
        ByteBufUtils.writeUTF8String(buffer, animIdent);
        ByteBufUtils.writeUTF8String(buffer, tlIdent);
        buffer.writeInt(curPos);
    }

    @Override
    public void readFrom(ByteBuf buffer, Side side)
    {
        host = ByteBufUtils.readUTF8String(buffer);
        projIdent = ByteBufUtils.readUTF8String(buffer);
        cubeJson = ByteBufUtils.readUTF8String(buffer);
        animIdent = ByteBufUtils.readUTF8String(buffer);
        tlIdent = ByteBufUtils.readUTF8String(buffer);
        curPos = buffer.readInt();
    }

    @Override
    public void execute(Side side, EntityPlayer player)
    {
        if(side.isServer())
        {
            EntityPlayerMP hoster = FMLCommonHandler.instance().getMinecraftServerInstance().getConfigurationManager().func_152612_a(host);
            if(hoster != null)
            {
                PacketHandler.sendToPlayer(Tabula.channels, this, hoster);
            }
        }
        else
        {
            handleClient();
        }
    }

    @SideOnly(Side.CLIENT)
    public void handleClient()
    {
        if(Tabula.proxy.tickHandlerClient.mainframe != null && Minecraft.getMinecraft().getSession().getUsername().equals(host))
        {
            Tabula.proxy.tickHandlerClient.mainframe.updateCube(projIdent, cubeJson, animIdent, tlIdent, curPos);
        }
    }
}
