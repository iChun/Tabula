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
import me.ichun.mods.tabula.common.Tabula;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.I18n;

import javax.annotation.Nonnull;
import java.util.function.Consumer;

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

            Consumer<String> enterResponder = s -> {
                if(!s.isEmpty())
                {
                    submit();
                }
            };

            ElementTextWrapper text = new ElementTextWrapper(this);
            text.setNoWrap().setText(I18n.format("window.newProject.projIdent"));
            text.setConstraint(new Constraint(text).left(this, Constraint.Property.Type.LEFT, 20).right(this, Constraint.Property.Type.RIGHT, 20).top(this, Constraint.Property.Type.TOP, 20));
            elements.add(text);

            ElementTextField textField = new ElementTextField(this);
            textField.setId("modelName");
            textField.setDefaultText("MyFirstModel").setEnterResponder(enterResponder);
            textField.setConstraint(new Constraint(textField).left(this, Constraint.Property.Type.LEFT, 20).right(this, Constraint.Property.Type.RIGHT, 20).top(text, Constraint.Property.Type.BOTTOM, 3));
            elements.add(textField);

            text = new ElementTextWrapper(this);
            text.setNoWrap().setText(I18n.format("window.newProject.animName"));
            text.setConstraint(new Constraint(text).left(this, Constraint.Property.Type.LEFT, 20).right(this, Constraint.Property.Type.RIGHT, 20).top(textField, Constraint.Property.Type.BOTTOM, 20));
            elements.add(text);

            textField = new ElementTextField(this);
            textField.setId("author");
            textField.setDefaultText(Minecraft.getInstance().getSession().getUsername()).setEnterResponder(enterResponder);
            textField.setConstraint(new Constraint(textField).left(this, Constraint.Property.Type.LEFT, 20).right(this, Constraint.Property.Type.RIGHT, 20).top(text, Constraint.Property.Type.BOTTOM, 3));
            elements.add(textField);

            text = new ElementTextWrapper(this);
            text.setNoWrap().setText(I18n.format("window.controls.scale"));
            text.setConstraint(new Constraint(text).left(this, Constraint.Property.Type.LEFT, 20).right(this, Constraint.Property.Type.RIGHT, 20).top(textField, Constraint.Property.Type.BOTTOM, 20));
            elements.add(text);

            ElementNumberInput numberInput = new ElementNumberInput(this, true);
            numberInput.setId("scaleX");
            numberInput.setMin(0).setMaxDec(Tabula.configClient.guiMaxDecimals).setDefaultText("1");
            numberInput.setWidth(80);
            numberInput.setConstraint(new Constraint(numberInput).left(this, Constraint.Property.Type.LEFT, 20).top(text, Constraint.Property.Type.BOTTOM, 3));
            elements.add(numberInput);

            ElementNumberInput numberInput1 = new ElementNumberInput(this, true);
            numberInput1.setId("scaleY");
            numberInput1.setMin(0).setMaxDec(Tabula.configClient.guiMaxDecimals).setDefaultText("1");
            numberInput1.setWidth(80);
            numberInput1.setConstraint(new Constraint(numberInput1).left(numberInput, Constraint.Property.Type.RIGHT, 3));
            elements.add(numberInput1);

            ElementNumberInput numberInput2 = new ElementNumberInput(this, true);
            numberInput2.setId("scaleZ");
            numberInput2.setMin(0).setMaxDec(Tabula.configClient.guiMaxDecimals).setDefaultText("1");
            numberInput2.setWidth(80);
            numberInput2.setConstraint(new Constraint(numberInput2).left(numberInput1, Constraint.Property.Type.RIGHT, 3));
            elements.add(numberInput2);

            text = new ElementTextWrapper(this);
            text.setNoWrap().setText(I18n.format("window.newProject.txDimensions"));
            text.setConstraint(new Constraint(text).left(this, Constraint.Property.Type.LEFT, 20).right(this, Constraint.Property.Type.RIGHT, 20).top(numberInput, Constraint.Property.Type.BOTTOM, 20));
            elements.add(text);

            numberInput = new ElementNumberInput(this, false);
            numberInput.setTooltip(I18n.format("tabula.project.width")).setId("texWidth");
            numberInput.setMin(1).setDefaultText("64");
            numberInput.setWidth(80);
            numberInput.setConstraint(new Constraint(numberInput).left(this, Constraint.Property.Type.LEFT, 20).top(text, Constraint.Property.Type.BOTTOM, 3));
            elements.add(numberInput);

            numberInput1 = new ElementNumberInput(this, false);
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

            ElementButton<?> button1 = new ElementButton<>(this, I18n.format("gui.ok"), elementClickable -> submit());
            button1.setSize(60, 20);
            button1.setConstraint(new Constraint(button1).right(button, Constraint.Property.Type.LEFT, 10));
            elements.add(button1);
        }

        public void submit()
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
            project.scaleX = (float)((ElementNumberInput)getById("scaleX")).getDouble();
            project.scaleY = (float)((ElementNumberInput)getById("scaleY")).getDouble();
            project.scaleZ = (float)((ElementNumberInput)getById("scaleZ")).getDouble();

            project.texWidth = ((ElementNumberInput)getById("texWidth")).getInt();
            project.texHeight = ((ElementNumberInput)getById("texHeight")).getInt();
            project.markDirty();
            parentFragment.parent.mainframe.openProject(project);
            getWorkspace().removeWindow(parentFragment);
        }
    }
}
