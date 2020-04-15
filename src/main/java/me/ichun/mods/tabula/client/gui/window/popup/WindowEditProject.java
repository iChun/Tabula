package me.ichun.mods.tabula.client.gui.window.popup;

import me.ichun.mods.ichunutil.client.gui.bns.window.Fragment;
import me.ichun.mods.ichunutil.client.gui.bns.window.Window;
import me.ichun.mods.ichunutil.client.gui.bns.window.WindowEditList;
import me.ichun.mods.ichunutil.client.gui.bns.window.constraint.Constraint;
import me.ichun.mods.ichunutil.client.gui.bns.window.view.View;
import me.ichun.mods.ichunutil.client.gui.bns.window.view.element.*;
import me.ichun.mods.ichunutil.common.module.tabula.project.Project;
import me.ichun.mods.tabula.client.gui.IProjectInfo;
import me.ichun.mods.tabula.client.gui.WorkspaceTabula;
import net.minecraft.client.resources.I18n;

import javax.annotation.Nonnull;

public class WindowEditProject extends Window<WorkspaceTabula>
{
    public WindowEditProject(WorkspaceTabula parent, Project project)
    {
        super(parent);

        setView(new ViewEditProject(this, project));
        disableDockingEntirely();
    }

    public static class ViewEditProject extends View<WindowEditProject>
    {
        @Nonnull
        public final Project project;

        public ViewEditProject(@Nonnull WindowEditProject parent, Project project)
        {
            super(parent, "window.editProject.title");
            this.project = project;

            ElementTextWrapper text = new ElementTextWrapper(this);
            text.setNoWrap().setText(I18n.format("window.newProject.projIdent"));
            text.setConstraint(new Constraint(text).left(this, Constraint.Property.Type.LEFT, 20).right(this, Constraint.Property.Type.RIGHT, 20).top(this, Constraint.Property.Type.TOP, 20));
            elements.add(text);

            ElementTextField textField = new ElementTextField(this);
            textField.setId("modelName");
            textField.setDefaultText(project.name).setHeight(14);
            textField.setConstraint(new Constraint(textField).left(this, Constraint.Property.Type.LEFT, 20).right(this, Constraint.Property.Type.RIGHT, 20).top(text, Constraint.Property.Type.BOTTOM, 3));
            elements.add(textField);

            text = new ElementTextWrapper(this);
            text.setNoWrap().setText(I18n.format("window.newProject.animName"));
            text.setConstraint(new Constraint(text).left(this, Constraint.Property.Type.LEFT, 20).right(this, Constraint.Property.Type.RIGHT, 20).top(textField, Constraint.Property.Type.BOTTOM, 20));
            elements.add(text);

            textField = new ElementTextField(this);
            textField.setId("author");
            textField.setDefaultText(project.author).setHeight(14);
            textField.setConstraint(new Constraint(textField).left(this, Constraint.Property.Type.LEFT, 20).right(this, Constraint.Property.Type.RIGHT, 20).top(text, Constraint.Property.Type.BOTTOM, 3));
            elements.add(textField);

            text = new ElementTextWrapper(this);
            text.setNoWrap().setText(I18n.format("window.newProject.txDimensions"));
            text.setConstraint(new Constraint(text).left(this, Constraint.Property.Type.LEFT, 20).right(this, Constraint.Property.Type.RIGHT, 20).top(textField, Constraint.Property.Type.BOTTOM, 20));
            elements.add(text);

            ElementNumberInput numberInput = new ElementNumberInput(this, false);
            numberInput.setTooltip(I18n.format("tabula.project.width")).setId("texWidth");
            numberInput.setMin(1).setDefaultText(Integer.toString(project.texWidth)).setHeight(14);
            numberInput.setWidth(80);
            numberInput.setConstraint(new Constraint(numberInput).left(this, Constraint.Property.Type.LEFT, 20).top(text, Constraint.Property.Type.BOTTOM, 3));
            elements.add(numberInput);

            ElementNumberInput numberInput1 = new ElementNumberInput(this, false);
            numberInput1.setTooltip(I18n.format("tabula.project.height")).setId("texHeight");
            numberInput1.setMin(1).setDefaultText(Integer.toString(project.texHeight)).setHeight(14);
            numberInput1.setWidth(80);
            numberInput1.setConstraint(new Constraint(numberInput1).left(numberInput, Constraint.Property.Type.RIGHT, 3));
            elements.add(numberInput1);

            ElementButton<?> editMeta = new ElementButton<>(this, I18n.format("selectWorld.edit"), btn -> {
                WindowEditList<?> window = new WindowEditList<>(getWorkspace(), "window.modelTree.editMeta", project.notes, s -> true, list1 -> {
                    project.notes.clear();
                    for(ElementList.Item<?> item1 : list1.items)
                    {
                        ElementTextField oriText = (ElementTextField)item1.elements.get(0);
                        if(!oriText.getText().isEmpty())
                        {
                            project.notes.add(oriText.getText());
                        }
                    }
                    parent.parent.mainframe.editProject(project);
                    parent.parent.projectChanged(IProjectInfo.ChangeType.PROJECT);
                });
                window.setId("windowEditProjectMeta");
                getWorkspace().openWindowInCenter(window, 0.6D, 0.8D);
                window.init();
            });
            editMeta.setSize(80, 14).setConstraint(new Constraint(editMeta).left(numberInput1, Constraint.Property.Type.RIGHT, 30).top(numberInput1, Constraint.Property.Type.TOP, 0));
            elements.add(editMeta);

            text = new ElementTextWrapper(this);
            text.setNoWrap().setText(I18n.format("window.modelTree.editMeta"));
            text.setConstraint(new Constraint(text).left(editMeta, Constraint.Property.Type.LEFT, 0).bottom(editMeta, Constraint.Property.Type.TOP, 3));
            elements.add(text);

            ElementButton<?> button = new ElementButton<>(this, I18n.format("gui.cancel"), elementClickable ->
            {
                parent.parent.removeWindow(parent);
            });
            button.setSize(60, 20);
            button.setConstraint(new Constraint(button).bottom(this, Constraint.Property.Type.BOTTOM, 10).right(this, Constraint.Property.Type.RIGHT, 10));
            elements.add(button);

            ElementButton<?> button1 = new ElementButton<>(this, I18n.format("gui.done"), button2 ->
            {
                project.name = ((ElementTextField)getById("modelName")).getText();
                project.author = ((ElementTextField)getById("author")).getText();
                project.texWidth = ((ElementNumberInput)getById("texWidth")).getInt();
                project.texHeight = ((ElementNumberInput)getById("texHeight")).getInt();
                parent.parent.mainframe.editProject(project);
                parent.parent.projectChanged(IProjectInfo.ChangeType.PROJECT);
                parent.parent.removeWindow(parent);
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
        Fragment<?> window = parent.getById("windowEditProjectMeta");
        if(window instanceof WindowEditList)
        {
            getWorkspace().removeWindow((Window<?>)window);
        }
    }
}
