package me.ichun.mods.tabula.client.gui.window.popup;

import me.ichun.mods.ichunutil.client.gui.bns.window.Fragment;
import me.ichun.mods.ichunutil.client.gui.bns.window.Window;
import me.ichun.mods.ichunutil.client.gui.bns.window.WindowEditList;
import me.ichun.mods.ichunutil.client.gui.bns.window.WindowPopup;
import me.ichun.mods.ichunutil.client.gui.bns.window.constraint.Constraint;
import me.ichun.mods.ichunutil.client.gui.bns.window.view.View;
import me.ichun.mods.ichunutil.client.gui.bns.window.view.element.ElementButton;
import me.ichun.mods.ichunutil.client.gui.bns.window.view.element.ElementTextField;
import me.ichun.mods.ichunutil.client.gui.bns.window.view.element.ElementTextWrapper;
import me.ichun.mods.ichunutil.common.module.tabula.project.Project;
import me.ichun.mods.tabula.client.core.ResourceHelper;
import me.ichun.mods.tabula.client.gui.WorkspaceTabula;
import net.minecraft.client.resources.I18n;

import javax.annotation.Nonnull;
import java.io.File;

public class WindowSaveOverwrite extends Window<WorkspaceTabula>
{
    public WindowSaveOverwrite(WorkspaceTabula parent, Project project, File saveFile)
    {
        super(parent);

        setView(new ViewSaveAs(this, project, saveFile));
        disableDockingEntirely();
    }

    public static class ViewSaveAs extends View<WindowSaveOverwrite>
    {
        @Nonnull
        public final Project project;
        public final File saveFile;

        public ViewSaveAs(@Nonnull WindowSaveOverwrite parent, Project project, File file)
        {
            super(parent, "window.saveAs.overwrite");
            this.project = project;
            this.saveFile = file;

            ElementTextWrapper text = new ElementTextWrapper(this);
            text.setNoWrap().setText(I18n.format("window.saveAs.confirmOverwrite"));
            text.setConstraint(new Constraint(text).left(this, Constraint.Property.Type.LEFT, 20).right(this, Constraint.Property.Type.RIGHT, 20).top(this, Constraint.Property.Type.TOP, 20));
            elements.add(text);

            ElementButton<?> button = new ElementButton<>(this, I18n.format("gui.cancel"), elementClickable ->
            {
                parent.parent.removeWindow(parent);
            });
            button.setSize(60, 20);
            button.setConstraint(new Constraint(button).bottom(this, Constraint.Property.Type.BOTTOM, 10).right(this, Constraint.Property.Type.RIGHT, 10));
            elements.add(button);

            ElementButton<?> button1 = new ElementButton<>(this, I18n.format("gui.ok"), button2 ->
            {
                parent.parent.removeWindow(parent);

                WindowSaveAs window = parent.parent.getWindowType(WindowSaveAs.class);
                if(window != null)
                {
                    getWorkspace().removeWindow(window);
                }

                if(!project.save(file))
                {
                    WindowPopup.popup(parentFragment.parent, 0.4D, 0.3D, I18n.format("window.saveAs.failed"), null);
                }
            });
            button1.setSize(60, 20);
            button1.setConstraint(new Constraint(button1).right(button, Constraint.Property.Type.LEFT, 10));
            elements.add(button1);
        }
    }

}
