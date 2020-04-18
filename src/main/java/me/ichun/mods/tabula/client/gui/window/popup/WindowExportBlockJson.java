package me.ichun.mods.tabula.client.gui.window.popup;

import com.google.common.base.Splitter;
import me.ichun.mods.ichunutil.client.gui.bns.window.Window;
import me.ichun.mods.ichunutil.client.gui.bns.window.WindowPopup;
import me.ichun.mods.ichunutil.client.gui.bns.window.constraint.Constraint;
import me.ichun.mods.ichunutil.client.gui.bns.window.view.View;
import me.ichun.mods.ichunutil.client.gui.bns.window.view.element.ElementButton;
import me.ichun.mods.ichunutil.client.gui.bns.window.view.element.ElementTextField;
import me.ichun.mods.ichunutil.client.gui.bns.window.view.element.ElementTextWrapper;
import me.ichun.mods.ichunutil.common.module.tabula.project.Project;
import me.ichun.mods.tabula.client.export.ExportList;
import me.ichun.mods.tabula.client.export.types.ExportBlockJson;
import me.ichun.mods.tabula.client.gui.WorkspaceTabula;
import net.minecraft.client.resources.I18n;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.function.Consumer;

public class WindowExportBlockJson extends Window<WorkspaceTabula>
{
    public WindowExportBlockJson(WorkspaceTabula parent, Project project)
    {
        super(parent);

        setView(new ViewNewProject(this, project));
        disableDockingEntirely();
    }

    public static class ViewNewProject extends View<WindowExportBlockJson>
    {
        /**
         * Toggles cornerAtZero
         */
        private ElementButton toggleCornerAtZeroButton;

        /**
         * Toggles relativeBlock
         */
        private ElementButton toggleRelativeToBlockButton;
        /**
         * Starts true.
         * If true: the corner of the block is at (0, 0) (centre of block is at (8, 8)).
         * If false: the centre of the block is at (0, 0).
         */
        private boolean cornerAtZero;

        /**
         * Starts true.
         * If true: the coordinates are relative to the block (y=23).
         * If false: the block is built using the Tabula coordinates (y=0).
         */
        private boolean relativeToBlock;


        public ViewNewProject(@Nonnull WindowExportBlockJson parent, Project project)
        {
            super(parent, "export.blockjson.title");

            Consumer<String> enterResponder = s -> {
                if(!s.isEmpty())
                {
                    submit(project);
                }
            };

            ElementTextWrapper text = new ElementTextWrapper(this);
            text.setNoWrap().setText("Mod ID");
            text.setConstraint(new Constraint(text).left(this, Constraint.Property.Type.LEFT, 20).right(this, Constraint.Property.Type.RIGHT, 20).top(this, Constraint.Property.Type.TOP, 20));
            elements.add(text);

            ElementTextField textField = new ElementTextField(this);
            textField.setId("modid");
            textField.setDefaultText("").setEnterResponder(enterResponder);
            textField.setConstraint(new Constraint(textField).left(this, Constraint.Property.Type.LEFT, 20).right(this, Constraint.Property.Type.RIGHT, 20).top(text, Constraint.Property.Type.BOTTOM, 3));
            elements.add(textField);

            String suggestedName = project.name.toLowerCase().replaceAll(" ", "_");

            text = new ElementTextWrapper(this);
            text.setNoWrap().setText(I18n.format("export.blockjson.filename"));
            text.setConstraint(new Constraint(text).left(this, Constraint.Property.Type.LEFT, 20).right(this, Constraint.Property.Type.RIGHT, 20).top(textField, Constraint.Property.Type.BOTTOM, 20));
            elements.add(text);

            ElementTextWrapper fileExt = new ElementTextWrapper(this);
            fileExt.setNoWrap().setText(".json");
            fileExt.setConstraint(new Constraint(fileExt).left(this, Constraint.Property.Type.RIGHT, -60).top(text, Constraint.Property.Type.BOTTOM, 3));
            elements.add(fileExt);

            textField = new ElementTextField(this);
            textField.setId("filename");
            textField.setDefaultText(suggestedName).setEnterResponder(enterResponder);
            textField.setConstraint(new Constraint(textField).left(this, Constraint.Property.Type.LEFT, 20).right(this, Constraint.Property.Type.RIGHT, 60).top(text, Constraint.Property.Type.BOTTOM, 3));
            elements.add(textField);

            text = new ElementTextWrapper(this);
            text.setNoWrap().setText(I18n.format("export.blockjson.texturename"));
            text.setConstraint(new Constraint(text).left(this, Constraint.Property.Type.LEFT, 20).right(this, Constraint.Property.Type.RIGHT, 20).top(textField, Constraint.Property.Type.BOTTOM, 20));
            elements.add(text);

            fileExt = new ElementTextWrapper(this);
            fileExt.setNoWrap().setText(".png");
            fileExt.setConstraint(new Constraint(fileExt).left(this, Constraint.Property.Type.RIGHT, -60).top(text, Constraint.Property.Type.BOTTOM, 3));
            elements.add(fileExt);

            textField = new ElementTextField(this);
            textField.setId("texturename");
            textField.setDefaultText(suggestedName).setEnterResponder(enterResponder);
            textField.setConstraint(new Constraint(textField).left(this, Constraint.Property.Type.LEFT, 20).right(this, Constraint.Property.Type.RIGHT, 60).top(text, Constraint.Property.Type.BOTTOM, 3));
            elements.add(textField);

            this.relativeToBlock = true;
            this.cornerAtZero = true;

            toggleRelativeToBlockButton = new ElementButton(this, "export.blockjson.relative", btn -> {
                //Toggle whether the model is relative to the block or absolute
                if(this.relativeToBlock)
                {
                    this.relativeToBlock = false;
                    this.toggleRelativeToBlockButton.text = I18n.format("export.blockjson.absolute");
                    this.toggleCornerAtZeroButton.posY = toggleRelativeToBlockButton.posY + toggleRelativeToBlockButton.height + 20;
                    this.toggleCornerAtZeroButton.text = this.cornerAtZero ? I18n.format("export.blockjson.corner") : I18n.format("export.blockjson.centre");
                }
                else
                {
                    this.relativeToBlock = true;
                    this.toggleRelativeToBlockButton.text = I18n.format("export.blockjson.relative");
                    this.toggleCornerAtZeroButton.posY = -1000;
                    this.cornerAtZero = true;
                }
            });
            toggleRelativeToBlockButton.setConstraint(new Constraint(toggleRelativeToBlockButton).top(textField, Constraint.Property.Type.BOTTOM, 20).left(this, Constraint.Property.Type.LEFT, 20).right(this, Constraint.Property.Type.RIGHT, 20));
            elements.add(toggleRelativeToBlockButton);

            toggleCornerAtZeroButton = new ElementButton(this, "export.blockjson.unused", btn -> {
                if(!this.relativeToBlock)
                {
                    //Toggle whether the block corner or centre is at (0, 0)
                    this.cornerAtZero = !this.cornerAtZero;
                    this.toggleCornerAtZeroButton.text = this.cornerAtZero ? I18n.format("export.blockjson.corner") : I18n.format("export.blockjson.centre");
                }
            });
            toggleCornerAtZeroButton.setConstraint(new Constraint(toggleCornerAtZeroButton).top(toggleRelativeToBlockButton, Constraint.Property.Type.BOTTOM, 20).left(this, Constraint.Property.Type.LEFT, 20).right(this, Constraint.Property.Type.RIGHT, 20));
            elements.add(toggleCornerAtZeroButton);


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
            String modid = ((ElementTextField)getById("modid")).getText();
            String filename = ((ElementTextField)getById("filename")).getText();
            String texturename = ((ElementTextField)getById("texturename")).getText();

            if(modid.isEmpty() || filename.isEmpty() || texturename.isEmpty())
            {
                return;
            }

            getWorkspace().removeWindow(parentFragment);

            if(!ExportList.EXPORTERS.get("blockJson").export(project, modid, filename, texturename, this.cornerAtZero, this.relativeToBlock))
            {
                StringBuilder blockJson = ((ExportBlockJson)ExportList.EXPORTERS.get("blockJson")).errors;
                List<String> strings = Splitter.on("\n").splitToList(blockJson.toString());
                WindowPopup.popup(parentFragment.parent, 0.4D, 0.3D, I18n.format("export.failed"), null, strings.toArray(new String[strings.size()]));
            }
        }
    }
}
