package me.ichun.mods.tabula.client.gui.window;

import me.ichun.mods.ichunutil.client.gui.bns.window.Window;
import me.ichun.mods.ichunutil.client.gui.bns.window.constraint.Constraint;
import me.ichun.mods.ichunutil.client.gui.bns.window.view.View;
import me.ichun.mods.ichunutil.client.gui.bns.window.view.element.ElementButtonTextured;
import me.ichun.mods.ichunutil.client.gui.bns.window.view.element.ElementToggle;
import me.ichun.mods.tabula.client.gui.IProjectInfo;
import me.ichun.mods.tabula.client.gui.WorkspaceTabula;
import me.ichun.mods.tabula.client.gui.window.popup.WindowNewProject;
import me.ichun.mods.tabula.client.tabula.Mainframe;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.ResourceLocation;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class WindowToolbar extends Window<WorkspaceTabula>
    implements IProjectInfo
{
    @Nullable
    public Mainframe.ProjectInfo currentInfo = null;
    public final Mainframe mainframe;

    public final ViewToolbar toolbar;
    public WindowToolbar(WorkspaceTabula parent)
    {
        super(parent);
        mainframe = parent.mainframe;

        borderSize = () -> 0;
        disableDocking();
        disableDockStacking();
        disableUndocking();
        disableBringToFront();
        disableDrag();
        disableDragResize();
        disableTitle();

        setView(toolbar = new ViewToolbar(this));
        setId("windowToolbar");
    }

    @Override
    public void setCurrentProject(Mainframe.ProjectInfo info)
    {
        currentInfo = info;
        children().stream().filter(child -> child instanceof IProjectInfo).forEach(child -> ((IProjectInfo)child).setCurrentProject(info));
    }

    public static class ViewToolbar extends View<WindowToolbar>
            implements IProjectInfo
    {
        public ViewToolbar(@Nonnull WindowToolbar parent)
        {
            super(parent, "");

            populate(parent.currentInfo);
        }

        @Override
        public void setCurrentProject(Mainframe.ProjectInfo info)
        {
            populate(info);
        }

        public void populate(Mainframe.ProjectInfo info)
        {
            elements.clear();

            ElementButtonTextured<?> last;
            ElementButtonTextured<?> btn;
            //new project
            btn = new ElementButtonTextured<>(this, new ResourceLocation("tabula", "textures/icon/new.png"), button -> {
                WindowNewProject window = new WindowNewProject(getWorkspace());
                window.setWidth((int)(window.getParentWidth() * 0.6D));
                window.setHeight((int)(window.getParentHeight() * 0.6D));
                getWorkspace().addWindow(window);
                getWorkspace().putInCenter(window);
                getWorkspace().setFocused(window);
                window.init();
            });
            btn.setSize(20,20).setTooltip(I18n.format("topdock.new"));
            btn.setConstraint(new Constraint(btn).left(this, Constraint.Property.Type.LEFT, 0));
            elements.add(last = btn);

            //open project
            btn = new ElementButtonTextured<>(this, new ResourceLocation("tabula", "textures/icon/open.png"), button -> {
                //TODO this
            });
            btn.setSize(20,20).setTooltip(I18n.format("topdock.open"));
            btn.setConstraint(new Constraint(btn).left(last, Constraint.Property.Type.RIGHT, 0));
            elements.add(last = btn);

            if(info != null)
            {
                //edit project
                btn = new ElementButtonTextured<>(this, new ResourceLocation("tabula", "textures/icon/edit.png"), button -> {
                    //TODO this
                });
                btn.setSize(20,20).setTooltip(I18n.format("topdock.edit"));
                btn.setConstraint(new Constraint(btn).left(last, Constraint.Property.Type.RIGHT, 0));
                elements.add(last = btn);

                //save project
                btn = new ElementButtonTextured<>(this, new ResourceLocation("tabula", "textures/icon/save.png"), button -> {
                    //TODO this
                });
                btn.setSize(20,20).setTooltip(I18n.format("topdock.save"));
                btn.setConstraint(new Constraint(btn).left(last, Constraint.Property.Type.RIGHT, 0));
                elements.add(last = btn);

                //saveAs project
                btn = new ElementButtonTextured<>(this, new ResourceLocation("tabula", "textures/icon/saveas.png"), button -> {
                    //TODO this
                });
                btn.setSize(20,20).setTooltip(I18n.format("topdock.saveAs"));
                btn.setConstraint(new Constraint(btn).left(last, Constraint.Property.Type.RIGHT, 0));
                elements.add(last = btn);

                //import to project
                btn = new ElementButtonTextured<>(this, new ResourceLocation("tabula", "textures/icon/import.png"), button -> {
                    //TODO this
                });
                btn.setSize(20,20).setTooltip(I18n.format("topdock.import"));
                btn.setConstraint(new Constraint(btn).left(last, Constraint.Property.Type.RIGHT, 0));
                elements.add(last = btn);

                //importMC to project
                btn = new ElementButtonTextured<>(this, new ResourceLocation("tabula", "textures/icon/importmc.png"), button -> {
                    //TODO this
                });
                btn.setSize(20,20).setTooltip(I18n.format("topdock.importMC"));
                btn.setConstraint(new Constraint(btn).left(last, Constraint.Property.Type.RIGHT, 0));
                elements.add(last = btn);

                //export project
                btn = new ElementButtonTextured<>(this, new ResourceLocation("tabula", "textures/icon/export.png"), button -> {
                    //TODO this
                });
                btn.setSize(20,20).setTooltip(I18n.format("topdock.export"));
                btn.setConstraint(new Constraint(btn).left(last, Constraint.Property.Type.RIGHT, 0));
                elements.add(last = btn);

                //ghostModel
                btn = new ElementButtonTextured<>(this, new ResourceLocation("tabula", "textures/icon/ghostmodel.png"), button -> {
                    //TODO this
                });
                btn.setSize(20,20).setTooltip(I18n.format("topdock.ghostModel"));
                btn.setConstraint(new Constraint(btn).left(last, Constraint.Property.Type.RIGHT, 0));
                elements.add(last = btn);

                //cut
                btn = new ElementButtonTextured<>(this, new ResourceLocation("tabula", "textures/icon/cut.png"), button -> {
                    //TODO this
                });
                btn.setSize(20,20).setTooltip(I18n.format("topdock.cut"));
                btn.setConstraint(new Constraint(btn).left(last, Constraint.Property.Type.RIGHT, 0));
                elements.add(last = btn);

                //copy
                btn = new ElementButtonTextured<>(this, new ResourceLocation("tabula", "textures/icon/copy.png"), button -> {
                    //TODO this
                });
                btn.setSize(20,20).setTooltip(I18n.format("topdock.copy"));
                btn.setConstraint(new Constraint(btn).left(last, Constraint.Property.Type.RIGHT, 0));
                elements.add(last = btn);

                //paste
                btn = new ElementButtonTextured<>(this, new ResourceLocation("tabula", "textures/icon/paste.png"), button -> {
                    //TODO this
                });
                btn.setSize(20,20).setTooltip(I18n.format("topdock.paste"));
                btn.setConstraint(new Constraint(btn).left(last, Constraint.Property.Type.RIGHT, 0));
                elements.add(last = btn);

                //pasteinplace
                btn = new ElementButtonTextured<>(this, new ResourceLocation("tabula", "textures/icon/pasteinplace.png"), button -> {
                    //TODO this
                });
                btn.setSize(20,20).setTooltip(I18n.format("topdock.pasteInPlace"));
                btn.setConstraint(new Constraint(btn).left(last, Constraint.Property.Type.RIGHT, 0));
                elements.add(last = btn);

                //pasteWithoutChildren
                btn = new ElementButtonTextured<>(this, new ResourceLocation("tabula", "textures/icon/pastewithoutchildren.png"), button -> {
                    //TODO this
                });
                btn.setSize(20,20).setTooltip(I18n.format("topdock.pasteWithoutChildren"));
                btn.setConstraint(new Constraint(btn).left(last, Constraint.Property.Type.RIGHT, 0));
                elements.add(last = btn);

                //autolayout
                btn = new ElementButtonTextured<>(this, new ResourceLocation("tabula", "textures/icon/autolayout.png"), button -> {
                    //TODO this
                });
                btn.setSize(20,20).setTooltip(I18n.format("topdock.autoLayout"));
                btn.setConstraint(new Constraint(btn).left(last, Constraint.Property.Type.RIGHT, 0));
                elements.add(last = btn);

                //undo
                btn = new ElementButtonTextured<>(this, new ResourceLocation("tabula", "textures/icon/undo.png"), button -> {
                    //TODO this
                });
                btn.setSize(20,20).setTooltip(I18n.format("topdock.undo"));
                btn.setConstraint(new Constraint(btn).left(last, Constraint.Property.Type.RIGHT, 0));
                elements.add(last = btn);

                //redo
                btn = new ElementButtonTextured<>(this, new ResourceLocation("tabula", "textures/icon/redo.png"), button -> {
                    //TODO this
                });
                btn.setSize(20,20).setTooltip(I18n.format("topdock.redo"));
                btn.setConstraint(new Constraint(btn).left(last, Constraint.Property.Type.RIGHT, 0));
                elements.add(last = btn);
            }

            //Add exit button. last button
            btn = new ElementButtonTextured<>(this, new ResourceLocation("tabula", "textures/icon/exittabula.png"), button -> {
                getWorkspace().onClose();
            });
            btn.setSize(20,20).setTooltip(I18n.format("topdock.exitTabula"));
            btn.setConstraint(new Constraint(btn).right(this, Constraint.Property.Type.RIGHT, 0));
            elements.add(btn);

            ElementToggle<?> toggle = new ElementToggle<>(this, "B", elementClickable -> {}).setToggled(true);
            toggle.setSize(20,20).setTooltip(I18n.format("topdock.woodFull"));
            toggle.setId("buttonBlockToggle");
            toggle.setConstraint(new Constraint(toggle).right(btn, Constraint.Property.Type.LEFT, 0));
            elements.add(toggle);

            ElementToggle<?> toggle1 = new ElementToggle<>(this, "G", elementClickable -> {}).setToggled(true);
            toggle1.setSize(20,20).setTooltip(I18n.format("tabula.config.prop.renderGrid.name"));
            toggle1.setId("buttonGridToggle");
            toggle1.setConstraint(new Constraint(toggle1).right(toggle, Constraint.Property.Type.LEFT, 0));
            elements.add(toggle1);

            init();
        }
    }

    @Override
    public int getMinHeight()
    {
        return 20;
    }

    @Override
    public int getMaxHeight()
    {
        return 20;
    }
}
