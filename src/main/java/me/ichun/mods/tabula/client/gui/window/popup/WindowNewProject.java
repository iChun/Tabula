package me.ichun.mods.tabula.client.gui.window.popup;

import me.ichun.mods.ichunutil.client.gui.bns.window.Window;
import me.ichun.mods.ichunutil.client.gui.bns.window.constraint.Constraint;
import me.ichun.mods.ichunutil.client.gui.bns.window.view.View;
import me.ichun.mods.ichunutil.client.gui.bns.window.view.element.ElementButton;
import me.ichun.mods.ichunutil.client.gui.bns.window.view.element.ElementNumberInput;
import me.ichun.mods.ichunutil.client.gui.bns.window.view.element.ElementTextField;
import me.ichun.mods.ichunutil.client.gui.bns.window.view.element.ElementTextWrapper;
import me.ichun.mods.ichunutil.common.module.tabula.project.Project;
import me.ichun.mods.tabula.client.gui.WorkspaceTabula;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.I18n;

import javax.annotation.Nonnull;

public class WindowNewProject extends Window<WorkspaceTabula>
{
    public WindowNewProject(WorkspaceTabula parent)
    {
        super(parent);

        setView(new ViewNewProject(this));
        disableDockingEntirely();
    }

    public static class ViewNewProject extends View<WindowNewProject>
    {
        public ViewNewProject(@Nonnull WindowNewProject parent)
        {
            super(parent, "window.newProject.title");

            ElementTextWrapper text = new ElementTextWrapper(this);
            text.setNoWrap().setText(I18n.format("window.newProject.projIdent"));
            text.setConstraint(new Constraint(text).left(this, Constraint.Property.Type.LEFT, 20).right(this, Constraint.Property.Type.RIGHT, 20).top(this, Constraint.Property.Type.TOP, 20));
            elements.add(text);

            ElementTextField textField = new ElementTextField(this);
            textField.setId("modelName");
            textField.setDefaultText("MyFirstModel");
            textField.setConstraint(new Constraint(textField).left(this, Constraint.Property.Type.LEFT, 20).right(this, Constraint.Property.Type.RIGHT, 20).top(text, Constraint.Property.Type.BOTTOM, 3));
            elements.add(textField);

            text = new ElementTextWrapper(this);
            text.setNoWrap().setText(I18n.format("window.newProject.animName"));
            text.setConstraint(new Constraint(text).left(this, Constraint.Property.Type.LEFT, 20).right(this, Constraint.Property.Type.RIGHT, 20).top(textField, Constraint.Property.Type.BOTTOM, 20));
            elements.add(text);

            textField = new ElementTextField(this);
            textField.setId("author");
            textField.setDefaultText(Minecraft.getInstance().getSession().getUsername());
            textField.setConstraint(new Constraint(textField).left(this, Constraint.Property.Type.LEFT, 20).right(this, Constraint.Property.Type.RIGHT, 20).top(text, Constraint.Property.Type.BOTTOM, 3));
            elements.add(textField);

            text = new ElementTextWrapper(this);
            text.setNoWrap().setText(I18n.format("window.newProject.txDimensions"));
            text.setConstraint(new Constraint(text).left(this, Constraint.Property.Type.LEFT, 20).right(this, Constraint.Property.Type.RIGHT, 20).top(textField, Constraint.Property.Type.BOTTOM, 20));
            elements.add(text);

            ElementNumberInput numberInput = new ElementNumberInput(this, false);
            numberInput.setTooltip(I18n.format("tabula.project.width")).setId("texWidth");
            numberInput.setMin(1).setDefaultText("64");
            numberInput.setWidth(80);
            numberInput.setConstraint(new Constraint(numberInput).left(this, Constraint.Property.Type.LEFT, 20).top(text, Constraint.Property.Type.BOTTOM, 3));
            elements.add(numberInput);

            ElementNumberInput numberInput1 = new ElementNumberInput(this, false);
            numberInput1.setTooltip(I18n.format("tabula.project.height")).setId("texHeight");
            numberInput1.setMin(1).setDefaultText("32");
            numberInput1.setWidth(80);
            numberInput1.setConstraint(new Constraint(numberInput1).left(numberInput, Constraint.Property.Type.RIGHT, 3));
            elements.add(numberInput1);

            ElementButton<?> button = new ElementButton<>(this, I18n.format("gui.cancel"), elementClickable ->
            {
                getWorkspace().removeWindow(parent);
            });
            button.setSize(60, 20);
            button.setConstraint(new Constraint(button).bottom(this, Constraint.Property.Type.BOTTOM, 10).right(this, Constraint.Property.Type.RIGHT, 10));
            elements.add(button);

            ElementButton<?> button1 = new ElementButton<>(this, I18n.format("gui.ok"), elementClickable ->
            {
                Project project = new Project();
                project.name = ((ElementTextField)getById("modelName")).getText();
                if(project.name.isEmpty())
                {
                    project.name = "NewProject";
                }
                project.author = ((ElementTextField)getById("author")).getText();
                if(project.author.isEmpty())
                {
                    project.author = "Undefined";
                }
                project.texWidth = ((ElementNumberInput)getById("texWidth")).getInt();
                project.texHeight = ((ElementNumberInput)getById("texHeight")).getInt();
                project.markDirty();
                parent.parent.mainframe.openProject(project);
                getWorkspace().removeWindow(parent);
            });
            button1.setSize(60, 20);
            button1.setConstraint(new Constraint(button1).right(button, Constraint.Property.Type.LEFT, 10));
            elements.add(button1);
        }
    }
}
