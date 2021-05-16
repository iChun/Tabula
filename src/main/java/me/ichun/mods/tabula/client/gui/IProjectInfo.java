package me.ichun.mods.tabula.client.gui;

import me.ichun.mods.tabula.client.tabula.Mainframe;
import net.minecraft.client.gui.INestedGuiEventHandler;

import javax.annotation.Nullable;

public interface IProjectInfo extends INestedGuiEventHandler
{
    /**
     * Called before projectChanged(PROJECT) (ideally)
     * @param info
     */
    default void setCurrentProject(@Nullable Mainframe.ProjectInfo info)
    {
        getEventListeners().stream().filter(child -> child instanceof IProjectInfo).forEach(child -> ((IProjectInfo)child).setCurrentProject(info));
    }

    default void projectChanged(ChangeType type)
    {
        getEventListeners().stream().filter(child -> child instanceof IProjectInfo).forEach(child -> ((IProjectInfo)child).projectChanged(type));
    }

    public enum ChangeType
    {
        PROJECTS, // When the number of opened projects change
        PROJECT, // When the current project changes
        PARTS,
        TEXTURE
    }
}
