package me.ichun.mods.tabula.old.common.packet;

import io.netty.buffer.ByteBuf;
import me.ichun.mods.ichunutil.common.core.network.AbstractPacket;
import me.ichun.mods.tabula.old.client.mainframe.Mainframe;
import me.ichun.mods.tabula.old.common.Tabula;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class PacketGenericMethod extends AbstractPacket
{
    public String host;
    public String methodName;
    public Object[] args;

    public PacketGenericMethod(){}

    public PacketGenericMethod(String host, String methodName, Object...args)
    {
        this.host = host;
        this.methodName = methodName;
        this.args = args;
    }

    @Override
    public void writeTo(ByteBuf buffer)
    {
        ByteBufUtils.writeUTF8String(buffer, host);
        ByteBufUtils.writeUTF8String(buffer, methodName);
        buffer.writeInt(args.length);
        for(Object arg : args)
        {
            if(arg instanceof Boolean)
            {
                buffer.writeByte(0);
                buffer.writeBoolean((Boolean)arg);
            }
            else if(arg instanceof Integer)
            {
                buffer.writeByte(1);
                buffer.writeInt((Integer)arg);
            }
            else if(arg instanceof Float)
            {
                buffer.writeByte(2);
                buffer.writeFloat((Float)arg);
            }
            else if(arg instanceof Double)
            {
                buffer.writeByte(3);
                buffer.writeDouble((Double)arg);
            }
            else if(arg instanceof String)
            {
                buffer.writeByte(4);
                ByteBufUtils.writeUTF8String(buffer, (String)arg);
            }
            else
            {
                throw new RuntimeException("Unsupported Arg Type!");
            }
        }
    }

    @Override
    public void readFrom(ByteBuf buffer)
    {
        host = ByteBufUtils.readUTF8String(buffer);
        methodName = ByteBufUtils.readUTF8String(buffer);
        args = new Object[buffer.readInt()];
        for(int i = 0; i < args.length; i++)
        {
            byte type = buffer.readByte();
            if(type == 0)
            {
                args[i] = buffer.readBoolean();
            }
            else if(type == 1)
            {
                args[i] = buffer.readInt();
            }
            else if(type == 2)
            {
                args[i] = buffer.readFloat();
            }
            else if(type == 3)
            {
                args[i] = buffer.readDouble();
            }
            else if(type == 4)
            {
                args[i] = ByteBufUtils.readUTF8String(buffer);
            }
        }
    }

    @Override
    public void execute(Side side, EntityPlayer player)
    {
        if(side.isServer())
        {
            EntityPlayerMP hoster = FMLCommonHandler.instance().getMinecraftServerInstance().getPlayerList().getPlayerByUsername(host);
            if(hoster != null)
            {
                Tabula.channel.sendTo(this, hoster);
            }
        }
        else
        {
            handleClient();
        }
    }

    @Override
    public Side receivingSide()
    {
        return null;
    }

    @SideOnly(Side.CLIENT)
    public void handleClient()
    {
        if(Tabula.proxy.tickHandlerClient.mainframe != null && Minecraft.getMinecraft().getSession().getUsername().equals(host))
        {
            Class[] clzs = new Class[args.length];
            for(int i = 0; i < args.length; i++)
            {
                Object arg = args[i];
                if(arg instanceof Boolean)
                {
                    clzs[i] = boolean.class;
                }
                else if(arg instanceof Integer)
                {
                    clzs[i] = int.class;
                }
                else if(arg instanceof Float)
                {
                    clzs[i] = float.class;
                }
                else if(arg instanceof Double)
                {
                    clzs[i] = double.class;
                }
                else if(arg instanceof String)
                {
                    clzs[i] = String.class;
                }
            }
            try
            {
                Mainframe.class.getDeclaredMethod(methodName, clzs).invoke(Tabula.proxy.tickHandlerClient.mainframe, args);
            }
            catch(Exception e)
            {
                Tabula.LOGGER.warn("This shouldn't be happening. Inform the mod author. Error method: " + methodName);
                e.printStackTrace();
            }
        }
    }
}
