package me.ichun.mods.tabula.client.gui.window.element;

import me.ichun.mods.ichunutil.client.gui.bns.Theme;
import me.ichun.mods.ichunutil.client.gui.bns.window.Fragment;
import me.ichun.mods.ichunutil.client.gui.bns.window.view.element.ElementClickable;
import me.ichun.mods.tabula.client.gui.WorkspaceTabula;
import me.ichun.mods.tabula.client.tabula.Mainframe;
import net.jodah.typetools.TypeResolver;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.EntityType;
import org.lwjgl.glfw.GLFW;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.lang.reflect.Type;
import java.util.Map;
import java.util.function.Consumer;

public class ElementProjectButton<T extends ElementProjectButton> extends ElementClickable<T>
{
    @Nonnull
    public final Mainframe.ProjectInfo projectInfo;

    public ElementProjectButton(@Nonnull Fragment parent, @Nonnull Mainframe.ProjectInfo projectInfo, Consumer<T> callback)
    {
        super(parent, callback);
        this.projectInfo = projectInfo;
    }

    @Override
    public void render(int mouseX, int mouseY, float partialTick)
    {
        hover = isMouseOver(mouseX, mouseY) || parentFragment.getFocused() == this;

        if(renderMinecraftStyle())
        {
            renderMinecraftStyleButton(getLeft(), getTop(), width, height, parentFragment.isDragging() && parentFragment.getFocused() == this ? ButtonState.CLICK : hover ? ButtonState.HOVER : ButtonState.IDLE);
        }
        else
        {
            int[] colour = parentFragment.isDragging() && parentFragment.getFocused() == this ? getTheme().elementButtonClick : hover ? getTheme().elementProjectTabHover : getTheme().elementTreeItemBg;
            fill(colour, 0);
        }
        int[] fontClr = getTheme().fontDim;
        Mainframe.ProjectInfo info = ((WorkspaceTabula)getWorkspace()).mainframe.getActiveProject();
        if(info != null)
        {
            if(info == projectInfo) //this is active
            {
                fontClr = getTheme().font;
            }
            else if(projectInfo.project.isDirty)
            {
                fontClr = getTheme().fontChat;
            }
        }
        if(!projectInfo.project.name.isEmpty())
        {
            String s = reString(projectInfo.project.name, width - 14);
            if(projectInfo.project.isDirty)
            {
                s = s + "*";
            }

            drawString(s, getLeft() + 2, getTop() + (height - getFontRenderer().FONT_HEIGHT) / 2F + 1, Theme.getAsHex(fontClr));
        }
        drawString("X", getRight() - 7, getTop() + (height - getFontRenderer().FONT_HEIGHT) / 2F + 1, Theme.getAsHex(isOverX(mouseX, mouseY) ? getTheme().font : getTheme().fontDim));
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button)
    {
        boolean flag = super.mouseReleased(mouseX, mouseY, button); // unsets dragging;
        parentFragment.setFocused(null); //we're a one time click, stop focusing on us
        if(isMouseOver(mouseX, mouseY)) //lmb //TODO close project
        {
            trigger(); //Switch to this project anyway

            if(isOverX(mouseX, mouseY) || button == GLFW.GLFW_MOUSE_BUTTON_MIDDLE)
            {
                ((WorkspaceTabula)getWorkspace()).closeProject(projectInfo);
            }
        }
        return flag;
    }

    public boolean isOverX(double mouseX, double mouseY)
    {
        return parentFragment.isMouseOver(mouseX, mouseY) && isMouseBetween(mouseX, getLeft() + width - 10, getLeft() + width) && isMouseBetween(mouseY, getTop(), getTop() + height);
    }

    @Nullable
    @Override
    public String tooltip(double mouseX, double mouseY)
    {
        return projectInfo.project.name + " - " + projectInfo.project.author;
    }

    @Override
    public void onClickRelease()
    {
        if(projectInfo != ((WorkspaceTabula)getWorkspace()).mainframe.getActiveProject())
        {
            ((WorkspaceTabula)getWorkspace()).mainframe.setActiveProject(projectInfo);
        }
    }

    @Override
    public int getMinWidth()
    {
        return 15;
    }

    @Override
    public int getMinHeight()
    {
        return 10;
    }
}
