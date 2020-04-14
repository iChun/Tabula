package me.ichun.mods.tabula.client.gui;

import me.ichun.mods.tabula.client.tabula.Mainframe;
import net.minecraft.client.gui.IGuiEventListener;

import java.util.List;

public interface IProjectInfo
{
    //because I'm lazy
    List<? extends IGuiEventListener> children();

    default void setCurrentProject(Mainframe.ProjectInfo info)
    {
        children().stream().filter(child -> child instanceof IProjectInfo).forEach(child -> ((IProjectInfo)child).setCurrentProject(info));
    }

    default void projectChanged(ChangeType type)
    {
        children().stream().filter(child -> child instanceof IProjectInfo).forEach(child -> ((IProjectInfo)child).projectChanged(type));
    }

    public enum ChangeType
    {
        PROJECTS,
        PROJECT,
        PARTS,
        TEXTURE,
        MODEL
    }
}
