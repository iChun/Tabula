package me.ichun.mods.tabula.client.gui.window.popup;

import me.ichun.mods.ichunutil.client.gui.bns.window.Fragment;
import me.ichun.mods.ichunutil.client.gui.bns.window.Window;
import me.ichun.mods.ichunutil.client.gui.bns.window.WindowEditList;
import me.ichun.mods.ichunutil.client.gui.bns.window.WindowPopup;
import me.ichun.mods.ichunutil.client.gui.bns.window.constraint.Constraint;
import me.ichun.mods.ichunutil.client.gui.bns.window.view.View;
import me.ichun.mods.ichunutil.client.gui.bns.window.view.element.*;
import me.ichun.mods.ichunutil.common.module.tabula.project.Project;
import me.ichun.mods.tabula.client.core.ResourceHelper;
import me.ichun.mods.tabula.client.gui.IProjectInfo;
import me.ichun.mods.tabula.client.gui.WorkspaceTabula;
import net.minecraft.client.resources.I18n;

import javax.annotation.Nonnull;
import java.io.File;

public class WindowSaveAs extends Window<WorkspaceTabula>
{
    public WindowSaveAs(WorkspaceTabula parent, Project project)
    {
        super(parent);

        setView(new ViewSaveAs(this, project));
        disableDockingEntirely();
    }

    public static class ViewSaveAs extends View<WindowSaveAs>
    {
        @Nonnull
        public final Project project;

        public ViewSaveAs(@Nonnull WindowSaveAs parent, Project project)
        {
            super(parent, "window.saveAs.title");
            this.project = project;

            ElementTextWrapper text = new ElementTextWrapper(this);
            text.setNoWrap().setText(I18n.format("window.saveAs.fileName"));
            text.setConstraint(new Constraint(text).left(this, Constraint.Property.Type.LEFT, 20).right(this, Constraint.Property.Type.RIGHT, 20).top(this, Constraint.Property.Type.TOP, 20));
            elements.add(text);

            ElementTextField textField = new ElementTextField(this);
            textField.setId("fileName");
            textField.setValidator(ElementTextField.FILE_SAFE);
            textField.setDefaultText(project.name).setHeight(14);
            textField.setConstraint(new Constraint(textField).left(this, Constraint.Property.Type.LEFT, 20).right(this, Constraint.Property.Type.RIGHT, 20).top(text, Constraint.Property.Type.BOTTOM, 3));
            elements.add(textField);

            ElementButton<?> button = new ElementButton<>(this, I18n.format("gui.cancel"), elementClickable ->
            {
                parent.parent.setFocused(null);
                parent.parent.removeWindow(parent);
            });
            button.setSize(60, 20);
            button.setConstraint(new Constraint(button).bottom(this, Constraint.Property.Type.BOTTOM, 10).right(this, Constraint.Property.Type.RIGHT, 10));
            elements.add(button);

            ElementButton<?> button1 = new ElementButton<>(this, I18n.format("gui.done"), button2 ->
            {
                String fileName = ((ElementTextField)getById("fileName")).getText();
                if(!fileName.isEmpty())
                {
                    if(!fileName.endsWith(".tbl"))
                    {
                        fileName = fileName + ".tbl";
                    }

                    File file = new File(ResourceHelper.getSavesDir().toFile(), fileName);

                    if(file.exists())
                    {
                        getWorkspace().openWindowInCenter(new WindowSaveOverwrite(getWorkspace(), project, file), 0.6D, 0.8D);
                    }
                    else
                    {
                        parent.parent.removeWindow(parent);

                        if(!project.save(file))
                        {
                            WindowPopup.popup(parentFragment.parent, 0.4D, 0.3D, I18n.format("window.saveAs.failed"), null);
                        }
                    }
                }
            });
            button1.setSize(60, 20);
            button1.setConstraint(new Constraint(button1).right(button, Constraint.Property.Type.LEFT, 10));
            elements.add(button1);
        }
    }

    @Override
    public void onClose()
    {
        super.onClose();
        WindowSaveOverwrite window = parent.getWindowType(WindowSaveOverwrite.class);
        if(window != null)
        {
            getWorkspace().removeWindow(window);
        }
    }
}
