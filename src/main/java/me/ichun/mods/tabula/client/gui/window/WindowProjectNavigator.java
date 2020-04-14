package me.ichun.mods.tabula.client.gui.window;

import me.ichun.mods.ichunutil.client.gui.bns.window.Window;
import me.ichun.mods.ichunutil.client.gui.bns.window.constraint.Constraint;
import me.ichun.mods.ichunutil.client.gui.bns.window.constraint.IConstrainable;
import me.ichun.mods.ichunutil.client.gui.bns.window.view.View;
import me.ichun.mods.ichunutil.client.gui.bns.window.view.element.Element;
import me.ichun.mods.ichunutil.client.render.RenderHelper;
import me.ichun.mods.tabula.client.gui.IProjectInfo;
import me.ichun.mods.tabula.client.gui.WorkspaceTabula;
import me.ichun.mods.tabula.client.gui.window.element.ElementProjectButton;
import me.ichun.mods.tabula.client.tabula.Mainframe;

import javax.annotation.Nonnull;

public class WindowProjectNavigator extends Window<WorkspaceTabula>
        implements IProjectInfo
{
    public WindowProjectNavigator(WorkspaceTabula parent)
    {
        super(parent);
        borderSize = () -> 0;
        disableDocking();
        disableDockStacking();
        disableUndocking();
        disableBringToFront();
        disableDrag();
        disableDragResize();
        disableTitle();

        setView(new ViewProjectNavigator(this));
        setId("windowNav");

        setHeight(10);
    }

    @Override
    public int getMaxHeight()
    {
        return 10;
    }

    @Override
    public int getMinHeight()
    {
        return 10;
    }

    public static class ViewProjectNavigator extends View<WindowProjectNavigator>
            implements IProjectInfo
    {
        public Mainframe.ProjectInfo currentInfo = null;

        public ViewProjectNavigator(@Nonnull WindowProjectNavigator parent)
        {
            super(parent, "");

            populate();
        }

        @Override
        public void setCurrentProject(Mainframe.ProjectInfo info)
        {
            currentInfo = info;
        }

        @Override
        public void projectChanged(ChangeType type)
        {
            if(type == ChangeType.PROJECTS)
            {
                populate();
            }
        }

        public void populate()
        {
            elements.clear();

            Mainframe mainframe = parentFragment.parent.mainframe;
            IConstrainable last = null;
            for(int i = 0; i < mainframe.projects.size(); i++)
            {
                Mainframe.ProjectInfo projectInfo = mainframe.projects.get(i);
                ElementProjectButton<?, ?> btn = new ElementProjectButton<>(this, projectInfo.project.modelName, elementClickable -> {});
                btn.setConstraint(new Constraint(btn).left(last == null ? this : last, last == null ? Constraint.Property.Type.LEFT : Constraint.Property.Type.RIGHT, 0));
                btn.setWidth(getFontRenderer().getStringWidth(projectInfo.project.modelName) + 14);
                elements.add(btn);
                last = btn;
            }

            init();
        }

        @Override
        public void render(int mouseX, int mouseY, float partialTick)
        {
            setScissor();
            //render our background
            if(!renderMinecraftStyle())
            {
                fill(getTheme().windowBackground, 0);

                int[] clr = getTheme().elementTreeBorder;
                RenderHelper.drawColour(clr[0], clr[1], clr[2], 255, getLeft(), getTop(), getWidth(), 1, 0);
            }

            //render attached elements
            for(Element<?> element : elements)
            {
                element.render(mouseX, mouseY, partialTick);
            }

            resetScissorToParent();
        }
    }
}
