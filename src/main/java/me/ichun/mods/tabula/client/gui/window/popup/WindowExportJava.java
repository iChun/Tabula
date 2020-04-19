package me.ichun.mods.tabula.client.gui.window.popup;

import me.ichun.mods.ichunutil.client.gui.bns.window.Window;
import me.ichun.mods.ichunutil.client.gui.bns.window.WindowPopup;
import me.ichun.mods.ichunutil.client.gui.bns.window.constraint.Constraint;
import me.ichun.mods.ichunutil.client.gui.bns.window.view.View;
import me.ichun.mods.ichunutil.client.gui.bns.window.view.element.ElementButton;
import me.ichun.mods.ichunutil.client.gui.bns.window.view.element.ElementTextField;
import me.ichun.mods.ichunutil.client.gui.bns.window.view.element.ElementTextWrapper;
import me.ichun.mods.ichunutil.common.module.tabula.project.Project;
import me.ichun.mods.tabula.client.export.ExportList;
import me.ichun.mods.tabula.client.gui.WorkspaceTabula;
import net.minecraft.client.resources.I18n;

import javax.annotation.Nonnull;
import java.util.function.Consumer;

public class WindowExportJava extends Window<WorkspaceTabula>
{
    public WindowExportJava(WorkspaceTabula parent, Project project)
    {
        super(parent);

        setView(new ViewExportJava(this, project));
        disableDockingEntirely();
    }

    public static class ViewExportJava extends View<WindowExportJava>
    {
        public ViewExportJava(@Nonnull WindowExportJava parent, Project project)
        {
            super(parent, "export.javaClass.title");

            Consumer<String> enterResponder = s -> {
                if(!s.isEmpty())
                {
                    submit(project);
                }
            };

            ElementTextWrapper text = new ElementTextWrapper(this);
            text.setNoWrap().setText(I18n.format("export.javaClass.package"));
            text.setConstraint(new Constraint(text).left(this, Constraint.Property.Type.LEFT, 20).right(this, Constraint.Property.Type.RIGHT, 20).top(this, Constraint.Property.Type.TOP, 20));
            elements.add(text);

            ElementTextField textField = new ElementTextField(this);
            textField.setId("packageName");
            textField.setDefaultText("my.first.mod.model").setEnterResponder(enterResponder);
            textField.setConstraint(new Constraint(textField).left(this, Constraint.Property.Type.LEFT, 20).right(this, Constraint.Property.Type.RIGHT, 20).top(text, Constraint.Property.Type.BOTTOM, 3));
            elements.add(textField);

            text = new ElementTextWrapper(this);
            text.setNoWrap().setText(I18n.format("export.javaClass.className"));
            text.setConstraint(new Constraint(text).left(this, Constraint.Property.Type.LEFT, 20).right(this, Constraint.Property.Type.RIGHT, 20).top(textField, Constraint.Property.Type.BOTTOM, 20));
            elements.add(text);

            textField = new ElementTextField(this);
            textField.setId("className");
            textField.setDefaultText(project.name).setValidator(ElementTextField.FILE_SAFE.and(s-> !s.contains(" "))).setEnterResponder(enterResponder);
            textField.setConstraint(new Constraint(textField).left(this, Constraint.Property.Type.LEFT, 20).right(this, Constraint.Property.Type.RIGHT, 20).top(text, Constraint.Property.Type.BOTTOM, 3));
            elements.add(textField);

            ElementButton<?> button = new ElementButton<>(this, I18n.format("gui.cancel"), elementClickable ->
            {
                getWorkspace().removeWindow(parent);
            });
            button.setSize(60, 20);
            button.setConstraint(new Constraint(button).bottom(this, Constraint.Property.Type.BOTTOM, 10).right(this, Constraint.Property.Type.RIGHT, 10));
            elements.add(button);

            ElementButton<?> button1 = new ElementButton<>(this, I18n.format("gui.ok"), elementClickable -> submit(project));
            button1.setSize(60, 20);
            button1.setConstraint(new Constraint(button1).right(button, Constraint.Property.Type.LEFT, 10));
            elements.add(button1);
        }

        public void submit(Project project)
        {
            getWorkspace().removeWindow(parentFragment);

            String packageName;
            String className;
            packageName = ((ElementTextField)getById("packageName")).getText();
            if(packageName.isEmpty())
            {
                packageName = "there.was.supposed.to.be.a.package";
            }
            className = ((ElementTextField)getById("className")).getText();
            if(className.isEmpty())
            {
                className = "ThereWasSupposedToBeAClassName";
            }

            if(!ExportList.EXPORTERS.get("javaClass").export(project, packageName, className))
            {
                WindowPopup.popup(parentFragment.parent, 0.4D, 0.3D, null, I18n.format("export.failed"));
            }
        }
    }
}
