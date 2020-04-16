package me.ichun.mods.tabula.client.gui.window;

import me.ichun.mods.ichunutil.client.gui.bns.window.Window;
import me.ichun.mods.ichunutil.client.gui.bns.window.constraint.Constraint;
import me.ichun.mods.ichunutil.client.gui.bns.window.view.View;
import me.ichun.mods.ichunutil.client.gui.bns.window.view.element.*;
import me.ichun.mods.ichunutil.common.module.tabula.project.Identifiable;
import me.ichun.mods.ichunutil.common.module.tabula.project.Project;
import me.ichun.mods.tabula.client.gui.IProjectInfo;
import me.ichun.mods.tabula.client.gui.WorkspaceTabula;
import me.ichun.mods.tabula.client.tabula.Mainframe;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.ResourceLocation;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;

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
        public static ResourceLocation TEX_PART = new ResourceLocation("tabula", "textures/icon/group.png");
        public static ResourceLocation TEX_BOX = new ResourceLocation("tabula", "textures/icon/model.png");

        public @Nullable Mainframe.ProjectInfo currentInfo = null;
        public ElementList<?> list;

        public ViewModelTree(@Nonnull WindowModelTree parent)
        {
            super(parent, "window.modelTree.title"); //TODO update buttons based on the project

            int spaceBottom = 2;
            int bottomSpace = 2 + spaceBottom + 20;

            ElementScrollBar<?> sv = new ElementScrollBar<>(this, ElementScrollBar.Orientation.VERTICAL, 0.6F);
            sv.setConstraint(new Constraint(sv).top(this, Constraint.Property.Type.TOP, 0)
                    .bottom(this, Constraint.Property.Type.BOTTOM, bottomSpace)
                    .right(this, Constraint.Property.Type.RIGHT, 0)
            );
            elements.add(sv);

            ElementScrollBar<?> sh = new ElementScrollBar<>(this, ElementScrollBar.Orientation.HORIZONTAL, 0.6F);
            sh.setConstraint(new Constraint(sh).left(this, Constraint.Property.Type.LEFT, spaceBottom)
                    .bottom(this, Constraint.Property.Type.BOTTOM, bottomSpace)
                    .right(sv, Constraint.Property.Type.LEFT, 0)
            );
            elements.add(sh);

            list = new ElementList<>(this).setScrollHorizontal(sh).setScrollVertical(sv);
            list.setConstraint(new Constraint(list).bottom(sh, Constraint.Property.Type.TOP, 0).left(this, Constraint.Property.Type.LEFT, spaceBottom).right(sv, Constraint.Property.Type.LEFT, 0).top(this, Constraint.Property.Type.TOP, spaceBottom));
            elements.add(list);

            ElementButtonTextured<?> last;
            ElementButtonTextured<?> button;

            button = new ElementButtonTextured<>(this, new ResourceLocation("tabula", "textures/icon/newgroup.png"), elementClickable -> {
                if(currentInfo != null) currentInfo.addPart(getSelectedElement());
            });
            button.setSize(20, 20).setTooltip(I18n.format("window.modelTree.newGroup"));
            button.setConstraint(new Constraint(button).left(this, Constraint.Property.Type.LEFT, 2).bottom(this, Constraint.Property.Type.BOTTOM, spaceBottom));
            elements.add(last = button);

            button = new ElementButtonTextured<>(this, new ResourceLocation("tabula", "textures/icon/newcube.png"), elementClickable -> {
                if(currentInfo != null) currentInfo.addBox(getSelectedElement());
            });
            button.setSize(20, 20).setTooltip(I18n.format("window.modelTree.newCube"));
            button.setConstraint(new Constraint(button).left(last, Constraint.Property.Type.RIGHT, 0).bottom(this, Constraint.Property.Type.BOTTOM, spaceBottom));
            elements.add(last = button);

            button = new ElementButtonTextured<>(this, new ResourceLocation("tabula", "textures/icon/delete.png"), elementClickable -> {
                if(currentInfo != null)
                {
                    Identifiable<?> selected = getSelectedElement();
                    if(selected != null)
                    {
                        if(selected instanceof Project.Part)
                        {
                            Project.Part part = (Project.Part)selected;
                            if(part.boxes.size() == 1)
                            {
                                currentInfo.delete(part.boxes.get(0));
                                return;
                            }
                        }
                        currentInfo.delete(selected);
                    }
                }
            });
            button.setSize(20, 20).setTooltip(I18n.format("window.modelTree.delete"));
            button.setConstraint(new Constraint(button).left(last, Constraint.Property.Type.RIGHT, 0).bottom(this, Constraint.Property.Type.BOTTOM, spaceBottom));
            elements.add(last = button);

            //
            //                button = new ElementButtonTextured(this, new ResourceLocation("tabula", "textures/icon/editmeta.png"), elementClickable -> {
            //
            //                }).setSize(20, 20).setTooltip(I18n.format("window.modelTree.editMeta"));
            //                button.setConstraint(new Constraint(button).left(last, Constraint.Property.Type.RIGHT, 0).bottom(this, Constraint.Property.Type.BOTTOM, spaceBottom));
            //                elements.add(last = button);
        }

        @Override
        public void setCurrentProject(Mainframe.ProjectInfo info)
        {
            currentInfo = info;
            updateList();
        }

        @Override
        public void projectChanged(ChangeType type)
        {
            if(type == ChangeType.PARTS || type == ChangeType.PROJECTS  || type == ChangeType.PROJECT)
            {
                updateList();
            }
        }

        public void updateList()
        {
            Identifiable<?> selected = getSelectedElement();

            list.setFocused(null);
            list.items.clear(); //TODO automatically select box.

            if(currentInfo != null)
            {
                populateList(list, currentInfo.project.parts, 0, selected);
            }

            selected = getSelectedElement();
            if(selected == null && currentInfo != null) //we haven't selected something... or it was deleted
            {
                parentFragment.parent.selectPart(null);
                parentFragment.parent.selectBox(null);
            }

            list.init();
            list.init();
        }

        public void populateList(ElementList<?> list, ArrayList<? extends Identifiable<?>> identifiables, int indent, Identifiable<?> selected)
        {
            for(Identifiable<?> identifiable : identifiables)
            {
                if(identifiable instanceof Project.Part)
                {
                    Project.Part part = (Project.Part)identifiable;
                    ElementList.Item<Project.Part> item = new ElementList.Item<>(list, part);;
                    list.items.add(item);
                    item.constraint = Constraint.sizeOnly(item);
                    if(selected != null && selected.identifier.equals(part.identifier))
                    {
                        item.selected = true;
                        list.setFocused(item);
                        parentFragment.parent.selectPart(part);
                        if(part.boxes.size() == 1)
                        {
                            parentFragment.parent.selectBox(part.boxes.get(0));
                        }
                    }

                    ElementTexture texture = new ElementTexture(item, part.boxes.size() == 1 ? TEX_BOX : TEX_PART);
                    texture.setConstraint(new Constraint(texture).left(item, Constraint.Property.Type.LEFT, indent).top(item, Constraint.Property.Type.TOP, item.getBorderSize()).bottom(item, Constraint.Property.Type.BOTTOM, item.getBorderSize()));
                    texture.setSize(14, 14);
                    item.addElement(texture);

                    ElementTextWrapper wrapper = new ElementTextWrapper(item).setText(part.name);
                    wrapper.setNoWrap().setConstraint(Constraint.matchParent(wrapper, item, item.getBorderSize()).left(item, Constraint.Property.Type.LEFT, indent + texture.getWidth()).bottom(null, Constraint.Property.Type.BOTTOM, 0));
                    item.addElement(wrapper);

                    item.setSelectionHandler(itemPart -> {
                        if(itemPart.selected)
                        {
                            Project.Part part1 = itemPart.getObject();
                            parentFragment.parent.selectPart(part1);
                            if(part1.boxes.size() == 1)
                            {
                                parentFragment.parent.selectBox(part1.boxes.get(0));
                            }
                        }
                        else
                        {
                            parentFragment.parent.selectPart(null);
                        }
                    });

                    int newIndent = indent + 10;
                    if(part.boxes.size() > 1)
                    {
                        populateList(list, part.boxes, newIndent, selected);
                    }
                    if(!part.children.isEmpty())
                    {
                        populateList(list, part.children, newIndent, selected);
                    }
                }
                else if(identifiable instanceof Project.Part.Box)
                {
                    //just add the name
                    Project.Part.Box box = (Project.Part.Box)identifiable;
                    ElementList.Item<Project.Part.Box> item = new ElementList.Item<>(list, box);
                    list.items.add(item);
                    item.constraint = Constraint.sizeOnly(item);
                    if(selected != null && selected.identifier.equals(box.identifier))
                    {
                        item.selected = true;
                        list.setFocused(item);
                        parentFragment.parent.selectBox(box);
                    }

                    ElementTexture texture = new ElementTexture(item, TEX_BOX);
                    texture.setConstraint(new Constraint(texture).left(item, Constraint.Property.Type.LEFT, indent).top(item, Constraint.Property.Type.TOP, item.getBorderSize()).bottom(item, Constraint.Property.Type.BOTTOM, item.getBorderSize()));
                    texture.setSize(14, 14);
                    item.addElement(texture);

                    ElementTextWrapper wrapper = new ElementTextWrapper(item).setText(box.name);
                    wrapper.setNoWrap().setConstraint(Constraint.matchParent(wrapper, item, item.getBorderSize()).left(item, Constraint.Property.Type.LEFT, indent + texture.getWidth()).bottom(null, Constraint.Property.Type.BOTTOM, 0));
                    item.addElement(wrapper);

                    item.setSelectionHandler(itemBox -> {
                        if(itemBox.selected)
                        {
                            parentFragment.parent.selectBox(itemBox.getObject());
                        }
                        else
                        {
                            parentFragment.parent.selectBox(null);
                        }
                    });
                }
            }
        }

        public Identifiable<?> getSelectedElement()
        {
            for(ElementList.Item<?> item : list.items)
            {
                if(item.selected && item.getObject() instanceof Identifiable)
                {
                    return ((Identifiable<?>)item.getObject());
                }
            }
            return null;
        }
    }
}
