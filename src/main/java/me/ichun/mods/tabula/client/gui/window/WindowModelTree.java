package me.ichun.mods.tabula.client.gui.window;

import me.ichun.mods.ichunutil.client.gui.bns.window.Window;
import me.ichun.mods.ichunutil.client.gui.bns.window.constraint.Constraint;
import me.ichun.mods.ichunutil.client.gui.bns.window.view.View;
import me.ichun.mods.ichunutil.client.gui.bns.window.view.element.ElementButtonTextured;
import me.ichun.mods.ichunutil.client.gui.bns.window.view.element.ElementList;
import me.ichun.mods.ichunutil.common.module.tabula.project.Project;
import me.ichun.mods.tabula.client.gui.IProjectInfo;
import me.ichun.mods.tabula.client.gui.WorkspaceTabula;
import me.ichun.mods.tabula.client.tabula.Mainframe;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.ResourceLocation;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class WindowModelTree extends Window<WorkspaceTabula>
        implements IProjectInfo
{
    public final Mainframe mainframe;

    public WindowModelTree(WorkspaceTabula parent)
    {
        super(parent);
        mainframe = parent.mainframe;

        setView(new ViewModelTree(this));
        setId("windowModelTree");
        size(104, 200);
    }

    public static class ViewModelTree extends View<WindowModelTree>
            implements IProjectInfo
    {
        public @Nullable Mainframe.ProjectInfo currentInfo = null;
        public ElementList list;

        public ViewModelTree(@Nonnull WindowModelTree parent)
        {
            super(parent, "window.modelTree.title"); //TODO update buttons based on the project

            populate(null);
        }

        @Override
        public void setCurrentProject(Mainframe.ProjectInfo info)
        {
            currentInfo = info;
            populate(info);
        }

        @Override
        public void projectChanged(ChangeType type)
        {
            if(type == ChangeType.PARTS)
            {
                updateList();
            }
        }

        public void updateList()
        {
            list.items.clear(); //TODO automatically select box.

            for(Project.Part part : currentInfo.project.parts)
            {
                list.addItem(part).addTextWrapper(part.name).setSelectionHandler(item -> {
                    ((WorkspaceTabula)getWorkspace()).selectPart((Project.Part)item.getObject());
                });

                for(Project.Part.Box box : part.boxes)
                {
                    list.addItem(box).addTextWrapper(box.name).setSelectionHandler(item -> {
                        ((WorkspaceTabula)getWorkspace()).selectBox((Project.Part.Box)item.getObject());
                    });
                }
            }

            list.init();
            list.init();
        }

        public void populate(Mainframe.ProjectInfo info)
        {
            elements.clear();

            if(info != null)
            {
                int spaceBottom = 2;
                if(list == null)
                {
                    list = new ElementList(this);
                    list.setConstraint(new Constraint(list).bottom(this, Constraint.Property.Type.BOTTOM, 2 + spaceBottom + 20).left(this, Constraint.Property.Type.LEFT, spaceBottom).right(this, Constraint.Property.Type.RIGHT, spaceBottom).top(this, Constraint.Property.Type.TOP, spaceBottom));
                }
                elements.add(list);

                boolean hasParts = false;
                for(Project.Part part : info.project.parts) //TODO Update this
                {
                    hasParts = true;
                    list.addItem(part).addTextWrapper(part.name);
                    //TODO add boxes
                }

                ElementButtonTextured last;
                ElementButtonTextured button;

                button = new ElementButtonTextured(this, new ResourceLocation("tabula", "textures/icon/newgroup.png"), elementClickable -> {
                    info.addPart();
                }).setSize(20, 20).setTooltip(I18n.format("window.modelTree.newGroup"));
                button.setConstraint(new Constraint(button).left(this, Constraint.Property.Type.LEFT, 2).bottom(this, Constraint.Property.Type.BOTTOM, spaceBottom));
                elements.add(last = button);

                button = new ElementButtonTextured(this, new ResourceLocation("tabula", "textures/icon/newcube.png"), elementClickable -> {

                }).setSize(20, 20).setTooltip(I18n.format("window.modelTree.newCube"));
                button.setConstraint(new Constraint(button).left(last, Constraint.Property.Type.RIGHT, 0).bottom(this, Constraint.Property.Type.BOTTOM, spaceBottom));
                elements.add(last = button);

                button = new ElementButtonTextured(this, new ResourceLocation("tabula", "textures/icon/delete.png"), elementClickable -> {

                }).setSize(20, 20).setTooltip(I18n.format("window.modelTree.delete"));
                button.setConstraint(new Constraint(button).left(last, Constraint.Property.Type.RIGHT, 0).bottom(this, Constraint.Property.Type.BOTTOM, spaceBottom));
                elements.add(last = button);

                //
                //                button = new ElementButtonTextured(this, new ResourceLocation("tabula", "textures/icon/editmeta.png"), elementClickable -> {
                //
                //                }).setSize(20, 20).setTooltip(I18n.format("window.modelTree.editMeta"));
                //                button.setConstraint(new Constraint(button).left(last, Constraint.Property.Type.RIGHT, 0).bottom(this, Constraint.Property.Type.BOTTOM, spaceBottom));
                //                elements.add(last = button);
            }

            init();
        }
    }
}
