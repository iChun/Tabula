package me.ichun.mods.tabula.common.packet;

import me.ichun.mods.ichunutil.common.module.tabula.project.Identifiable;
import me.ichun.mods.ichunutil.common.module.tabula.project.Project;
import me.ichun.mods.ichunutil.common.network.PacketDataFragment;
import me.ichun.mods.ichunutil.common.util.IOUtil;
import me.ichun.mods.tabula.client.gui.IProjectInfo;
import me.ichun.mods.tabula.client.gui.WorkspaceTabula;
import me.ichun.mods.tabula.client.tabula.Mainframe;
import me.ichun.mods.tabula.common.Tabula;
import me.ichun.mods.tabula.common.tileentity.TileEntityTabulaRasa;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.fml.network.NetworkEvent;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

public class PacketProjectFragment extends PacketDataFragment
{
    public BlockPos pos;
    public String directed;
    public String projIdent;
    public String secondaryIdent;
    public byte type;
    public byte func;

    public PacketProjectFragment(){}

    public PacketProjectFragment(String fileName, int packetTotal, int packetNumber, byte[] data, BlockPos pos, String directed, String projIdent, String secondaryIdent, byte type, byte func)
    {
        super(fileName, packetTotal, packetNumber, data);
        this.pos = pos;
        this.directed = directed;
        this.projIdent = projIdent;
        this.secondaryIdent = secondaryIdent;
        this.type = type;
        this.func = func;
    }

    @Override
    public void writeTo(PacketBuffer buf)
    {
        buf.writeBlockPos(pos);
        buf.writeString(directed);
        buf.writeString(projIdent);
        buf.writeString(secondaryIdent);
        buf.writeByte(type);
        buf.writeByte(func);
        super.writeTo(buf);
    }

    @Override
    public void readFrom(PacketBuffer buf)
    {
        pos = buf.readBlockPos();
        directed = buf.readString(32767);
        projIdent = buf.readString(32767);
        secondaryIdent = buf.readString(32767);
        type = buf.readByte();
        func = buf.readByte();
        super.readFrom(buf);
    }

    @Override
    public void process(NetworkEvent.Context context)
    {
        context.enqueueWork(() -> {
            if(context.getDirection().getReceptionSide().isServer())
            {
                //Send it off you lil bitch, servers are just proxies for these.
                ServerPlayerEntity player = context.getSender();
                if(directed.isEmpty())
                {
                    World world = player.world;
                    TileEntity tileEntity = world.getTileEntity(pos);
                    if(tileEntity instanceof TileEntityTabulaRasa)
                    {
                        TileEntityTabulaRasa tabulaRasa = (TileEntityTabulaRasa)tileEntity;

                        world.getPlayers().stream().filter(player1 -> (player1 != player && tabulaRasa.listeners.contains(player1.getName().getUnformattedComponentText())))
                                .forEach(player1 -> Tabula.channel.sendTo(new PacketProjectFragment(this.fileName, this.packetTotal, this.packetNumber, this.data, this.pos, this.directed, this.projIdent, this.secondaryIdent, this.type, this.func), (ServerPlayerEntity)player1));
                    }
                }
                else
                {
                    for(PlayerEntity worldPlayer : player.world.getPlayers())
                    {
                        if(worldPlayer.getName().getUnformattedComponentText().equals(directed))
                        {
                            Tabula.channel.sendTo(new PacketProjectFragment(this.fileName, this.packetTotal, this.packetNumber, this.data, this.pos, this.directed, this.projIdent, this.secondaryIdent, this.type, this.func), (ServerPlayerEntity)worldPlayer);
                        }
                    }
                }
            }
            else
            {
                byte[] data = process(LogicalSide.CLIENT);
                if(data != null) //we have all the fragments
                {
                    Screen screen = Minecraft.getInstance().currentScreen;
                    if(screen instanceof WorkspaceTabula)
                    {
                        WorkspaceTabula workspace = (WorkspaceTabula)screen;

                        if(type == 0)
                        {
                            try
                            {
                                InputStream is = new ByteArrayInputStream(data);
                                BufferedImage img = ImageIO.read(is);
                                workspace.mainframe.setImage(workspace.mainframe.getProjectInfoByProjectIdentifier(projIdent), img, false);
                            }
                            catch(IOException ignored){}
                        }
                        else
                        {
                            try
                            {
                                String s = IOUtil.decompress(data);

                                if(func == 1)
                                {
                                    Project project = Project.SIMPLE_GSON.fromJson(s, Project.class);
                                    project.adoptChildren();
                                    workspace.mainframe.openProject(project, false);
                                }
                                else if(func == 2) //replace old project with this.
                                {
                                    Mainframe.ProjectInfo info = workspace.mainframe.getProjectInfoByProjectIdentifier(projIdent);
                                    Project project = Project.SIMPLE_GSON.fromJson(s, Project.class);
                                    if(info != null && project != null)
                                    {
                                        info.project.transferTransients(project);

                                        //DO NOT CALL DESTROY.
                                        info.project = project;
                                        info.project.adoptChildren();

                                        workspace.projectChanged(IProjectInfo.ChangeType.PROJECT);
                                        workspace.projectChanged(IProjectInfo.ChangeType.PARTS);
                                        workspace.projectChanged(IProjectInfo.ChangeType.TEXTURE);
                                    }
                                }
                                else if(func == 3)
                                {
                                    Project project = Project.SIMPLE_GSON.fromJson(s, Project.class);
                                    project.adoptChildren();
                                    workspace.mainframe.importProject(project, false, false);
                                }
                                else if(func == 4)
                                {
                                    workspace.mainframe.closeProject(workspace.mainframe.getProjectInfoByProjectIdentifier(projIdent), false);
                                }
                                else if(func == 5)
                                {
                                    Mainframe.ProjectInfo info = workspace.mainframe.getProjectInfoByProjectIdentifier(projIdent);
                                    if(info != null)
                                    {
                                        Identifiable<?> parent = info.project.getById(secondaryIdent);

                                        Project.Part part = Project.SIMPLE_GSON.fromJson(s, Project.Part.class);
                                        part.adoptChildren();

                                        workspace.mainframe.addPart(info, parent, part, false);
                                    }
                                }
                                else if(func == 6)
                                {
                                    Mainframe.ProjectInfo info = workspace.mainframe.getProjectInfoByProjectIdentifier(projIdent);
                                    if(info != null)
                                    {
                                        Identifiable<?> parent = info.project.getById(secondaryIdent);

                                        Project.Part.Box box = Project.SIMPLE_GSON.fromJson(s, Project.Part.Box.class);
                                        box.adoptChildren();

                                        workspace.mainframe.addBox(info, parent, box, false);
                                    }
                                }
                                else if(func == 7)
                                {
                                    Mainframe.ProjectInfo info = workspace.mainframe.getProjectInfoByProjectIdentifier(projIdent);
                                    if(info != null)
                                    {
                                        Identifiable<?> child = info.project.getById(secondaryIdent);

                                        workspace.mainframe.delete(info, child, false);
                                    }
                                }
                                else if(func == 8)
                                {
                                    Mainframe.ProjectInfo info = workspace.mainframe.getProjectInfoByProjectIdentifier(projIdent);
                                    if(info != null)
                                    {
                                        Identifiable<?> parent = info.project.getById(secondaryIdent);

                                        if(parent != null)
                                        {
                                            Project.Part part = Project.SIMPLE_GSON.fromJson(s, Project.Part.class);
                                            part.adoptChildren();

                                            Identifiable<?> clone = parent.getById(part.identifier);
                                            parent.disown(clone);
                                            parent.adopt(part);

                                            workspace.mainframe.updatePart(part, false);
                                        }
                                    }
                                }
                                else if(func == 9)
                                {
                                    Mainframe.ProjectInfo info = workspace.mainframe.getProjectInfoByProjectIdentifier(projIdent);
                                    if(info != null)
                                    {
                                        Identifiable<?> parent = info.project.getById(secondaryIdent);

                                        if(parent != null)
                                        {
                                            Project.Part.Box box = Project.SIMPLE_GSON.fromJson(s, Project.Part.Box.class);
                                            box.adoptChildren();

                                            Identifiable<?> clone = parent.getById(box.identifier);
                                            parent.disown(clone);
                                            parent.adopt(box);

                                            workspace.mainframe.updateBox(box, false);
                                        }
                                    }
                                }
                            }
                            catch(IOException ignored){}
                        }
                    }

                }
            }
        });
    }

}
