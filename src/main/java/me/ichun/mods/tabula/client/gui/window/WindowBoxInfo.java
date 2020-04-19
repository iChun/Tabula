package me.ichun.mods.tabula.client.gui.window;

import me.ichun.mods.ichunutil.client.gui.bns.window.Window;
import me.ichun.mods.ichunutil.client.gui.bns.window.constraint.Constraint;
import me.ichun.mods.ichunutil.client.gui.bns.window.view.View;
import me.ichun.mods.ichunutil.client.gui.bns.window.view.element.*;
import me.ichun.mods.ichunutil.common.module.tabula.project.Project;
import me.ichun.mods.tabula.client.gui.IProjectInfo;
import me.ichun.mods.tabula.client.gui.WorkspaceTabula;
import me.ichun.mods.tabula.client.tabula.Mainframe;
import me.ichun.mods.tabula.common.Tabula;
import net.minecraft.client.resources.I18n;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Locale;
import java.util.function.Consumer;

public class WindowBoxInfo extends Window<WorkspaceTabula>
        implements IProjectInfo
{
    public final Mainframe mainframe;
    public final ViewBoxInfo viewBoxInfo;

    public WindowBoxInfo(WorkspaceTabula parent)
    {
        super(parent);
        mainframe = parent.mainframe;

        setView(viewBoxInfo = new ViewBoxInfo(this));
        setId("windowBoxInfo");
        size(180 + borderSize.get(), 180);
    }

    public static class ViewBoxInfo extends View<WindowBoxInfo>
            implements IProjectInfo
    {
        public @Nullable Mainframe.ProjectInfo currentInfo = null;
        private @Nullable
        Project.Part.Box currentBox = null;

        public ViewBoxInfo(@Nonnull WindowBoxInfo parent)
        {
            super(parent, "window.boxInfo.title");
            setId("viewBoxInfo");

            int numberInputWidth = 45;
            int spacing = 2;
            int padding = 0;

            Consumer<String> responder = s -> {
                if(!s.isEmpty())
                {
                    updateBox();
                }
            };

            ElementTextWrapper text1 = new ElementTextWrapper(this);
            text1.setNoWrap().setText(I18n.format("window.boxInfo.partName"));
            text1.setConstraint(new Constraint(text1).left(this, Constraint.Property.Type.LEFT, 2).top(this, Constraint.Property.Type.TOP, 2));
            elements.add(text1);

            ElementTextField input = new ElementTextField(this);
            input.setDefaultText("").setMaxStringLength(256).setSize(numberInputWidth * 3, 14).setId("boxName");
            input.setResponder(responder);
            input.setConstraint(new Constraint(input).left(text1, Constraint.Property.Type.LEFT, 2).top(text1, Constraint.Property.Type.BOTTOM, padding).right(this, Constraint.Property.Type.RIGHT, 4));
            elements.add(input);

            ElementTextWrapper text = new ElementTextWrapper(this);
            text.setNoWrap().setText(I18n.format("window.controls.dimensions"));
            text.setConstraint(new Constraint(text).left(text1, Constraint.Property.Type.LEFT, 0).top(input, Constraint.Property.Type.BOTTOM, spacing));
            elements.add(text);

            ElementSharedSpace space = new ElementSharedSpace(this, ElementScrollBar.Orientation.HORIZONTAL);
            space.setSize(14, 14).setConstraint(new Constraint(space).left(this, Constraint.Property.Type.LEFT, 4).top(text, Constraint.Property.Type.BOTTOM, padding).right(this, Constraint.Property.Type.RIGHT, 4));
            elements.add(space);

            ElementNumberInput num = new ElementNumberInput(space, true);
            num.setMaxDec(Tabula.configClient.guiMaxDecimals).setMin(0).setSize(numberInputWidth, 14).setId("dimX");
            num.setResponder(responder);
            num.setConstraint(new Constraint(num).left(space, Constraint.Property.Type.LEFT, 0).top(space, Constraint.Property.Type.TOP, 0));
            space.addElement(num);

            ElementNumberInput num1 = new ElementNumberInput(space, true);
            num1.setMaxDec(Tabula.configClient.guiMaxDecimals).setMin(0).setSize(numberInputWidth, 14).setId("dimY");
            num1.setResponder(responder);
            num1.setConstraint(new Constraint(num1).left(num, Constraint.Property.Type.RIGHT, 0).top(space, Constraint.Property.Type.TOP, 0));
            space.addElement(num1);

            ElementNumberInput num2 = new ElementNumberInput(space, true);
            num2.setMaxDec(Tabula.configClient.guiMaxDecimals).setMin(0).setSize(numberInputWidth, 14).setId("dimZ");
            num2.setResponder(responder);
            num2.setConstraint(new Constraint(num2).left(num1, Constraint.Property.Type.RIGHT, 0).top(space, Constraint.Property.Type.TOP, 0).right(space, Constraint.Property.Type.RIGHT, 0));
            space.addElement(num2);

            text = new ElementTextWrapper(this);
            text.setNoWrap().setText(I18n.format("window.controls.offset"));
            text.setConstraint(new Constraint(text).left(text1, Constraint.Property.Type.LEFT, 0).top(num, Constraint.Property.Type.BOTTOM, spacing));
            elements.add(text);

            space = new ElementSharedSpace(this, ElementScrollBar.Orientation.HORIZONTAL);
            space.setSize(14, 14).setConstraint(new Constraint(space).left(this, Constraint.Property.Type.LEFT, 4).top(text, Constraint.Property.Type.BOTTOM, padding).right(this, Constraint.Property.Type.RIGHT, 4));
            elements.add(space);

            num = new ElementNumberInput(space, true);
            num.setMaxDec(Tabula.configClient.guiMaxDecimals).setSize(numberInputWidth, 14).setId("offX");
            num.setResponder(responder);
            num.setConstraint(new Constraint(num).left(space, Constraint.Property.Type.LEFT, 0).top(space, Constraint.Property.Type.TOP, 0));
            space.addElement(num);

            num1 = new ElementNumberInput(space, true);
            num1.setMaxDec(Tabula.configClient.guiMaxDecimals).setSize(numberInputWidth, 14).setId("offY");
            num1.setResponder(responder);
            num1.setConstraint(new Constraint(num1).left(num, Constraint.Property.Type.RIGHT, 0).top(space, Constraint.Property.Type.TOP, 0));
            space.addElement(num1);

            num2 = new ElementNumberInput(space, true);
            num2.setMaxDec(Tabula.configClient.guiMaxDecimals).setSize(numberInputWidth, 14).setId("offZ");
            num2.setResponder(responder);
            num2.setConstraint(new Constraint(num2).left(num1, Constraint.Property.Type.RIGHT, 0).top(space, Constraint.Property.Type.TOP, 0).right(space, Constraint.Property.Type.RIGHT, 0));
            space.addElement(num2);

            text = new ElementTextWrapper(this);
            text.setNoWrap().setText(I18n.format("window.boxInfo.expand"));
            text.setConstraint(new Constraint(text).left(text1, Constraint.Property.Type.LEFT, 0).top(num, Constraint.Property.Type.BOTTOM, spacing));
            elements.add(text);

            space = new ElementSharedSpace(this, ElementScrollBar.Orientation.HORIZONTAL);
            space.setSize(14, 14).setConstraint(new Constraint(space).left(this, Constraint.Property.Type.LEFT, 4).top(text, Constraint.Property.Type.BOTTOM, padding).right(this, Constraint.Property.Type.RIGHT, 4));
            elements.add(space);

            num = new ElementNumberInput(space, true);
            num.setMaxDec(Tabula.configClient.guiMaxDecimals).setSize(numberInputWidth, 14).setId("expX");
            num.setResponder(responder);
            num.setConstraint(new Constraint(num).left(space, Constraint.Property.Type.LEFT, 0).top(space, Constraint.Property.Type.TOP, 0));
            space.addElement(num);

            num1 = new ElementNumberInput(space, true);
            num1.setMaxDec(Tabula.configClient.guiMaxDecimals).setSize(numberInputWidth, 14).setId("expY");
            num1.setResponder(responder);
            num1.setConstraint(new Constraint(num1).left(num, Constraint.Property.Type.RIGHT, 0).top(space, Constraint.Property.Type.TOP, 0));
            space.addElement(num1);

            num2 = new ElementNumberInput(space, true);
            num2.setMaxDec(Tabula.configClient.guiMaxDecimals).setSize(numberInputWidth, 14).setId("expZ");
            num2.setResponder(responder);
            num2.setConstraint(new Constraint(num2).left(num1, Constraint.Property.Type.RIGHT, 0).top(space, Constraint.Property.Type.TOP, 0).right(space, Constraint.Property.Type.RIGHT, 0));
            space.addElement(num2);

            text = new ElementTextWrapper(this);
            text.setNoWrap().setText(I18n.format("window.controls.txOffset"));
            text.setConstraint(new Constraint(text).left(text1, Constraint.Property.Type.LEFT, 0).top(num, Constraint.Property.Type.BOTTOM, spacing));
            elements.add(text);

            space = new ElementSharedSpace(this, ElementScrollBar.Orientation.HORIZONTAL);
            space.setSize(14, 14).setConstraint(new Constraint(space).left(this, Constraint.Property.Type.LEFT, 4).top(text, Constraint.Property.Type.BOTTOM, padding).right(this, Constraint.Property.Type.RIGHT, 4));
            elements.add(space);

            num = new ElementNumberInput(space, false);
            num.setSize(numberInputWidth, 14).setId("texX");
            num.setResponder(responder);
            num.setConstraint(new Constraint(num).left(space, Constraint.Property.Type.LEFT, 0).top(space, Constraint.Property.Type.TOP, 0));
            space.addElement(num);

            num1 = new ElementNumberInput(space, false);
            num1.setSize(numberInputWidth, 14).setId("texY");
            num1.setResponder(responder);
            num1.setConstraint(new Constraint(num1).left(num, Constraint.Property.Type.RIGHT, 0).top(space, Constraint.Property.Type.TOP, 0));
            space.addElement(num1);

            space.addElement(new ElementPadding(this, 0, 14));

            currentInfo = parent.parent.mainframe.getActiveProject();
        }

        @Override
        public void setCurrentProject(Mainframe.ProjectInfo info)
        {
            currentInfo = info;
        }

        @Override
        public void projectChanged(ChangeType type)
        {
            if(type == ChangeType.PROJECT || type == ChangeType.PARTS)
            {
                if(currentInfo != null)
                {
                    selectBox(currentInfo.getSelectedBox());
                }
                else
                {
                    selectBox(null);
                }
            }
        }

        private void updateBox()
        {
            if(currentBox != null)
            {
                String preUpdate = currentBox.getJsonWithoutChildren();
                currentBox.name = ((ElementTextField)getById("boxName")).getText();

                currentBox.dimX = (float)((ElementNumberInput)getById("dimX")).getDouble();
                currentBox.dimY = (float)((ElementNumberInput)getById("dimY")).getDouble();
                currentBox.dimZ = (float)((ElementNumberInput)getById("dimZ")).getDouble();

                currentBox.posX = (float)((ElementNumberInput)getById("offX")).getDouble();
                currentBox.posY = (float)((ElementNumberInput)getById("offY")).getDouble();
                currentBox.posZ = (float)((ElementNumberInput)getById("offZ")).getDouble();

                currentBox.expandX = (float)((ElementNumberInput)getById("expX")).getDouble();
                currentBox.expandY = (float)((ElementNumberInput)getById("expY")).getDouble();
                currentBox.expandZ = (float)((ElementNumberInput)getById("expZ")).getDouble();

                currentBox.texOffX = ((ElementNumberInput)getById("texX")).getInt();
                currentBox.texOffY = ((ElementNumberInput)getById("texY")).getInt();

                String postUpdate = currentBox.getJsonWithoutChildren();
                if(!postUpdate.equals(preUpdate))
                {
                    parentFragment.mainframe.updateBox(currentBox);
                }
            }
        }

        private void selectBox(Project.Part.Box box)
        {
            if(box == currentBox)
            {
                return;
            }

            currentBox = null; //set as null first so we don't send updates from updating the text
            if(box != null)
            {
                ((ElementTextField)getById("boxName")).setText(box.name);

                ((ElementNumberInput)getById("dimX")).setText(String.format(Locale.ENGLISH, "%." + Tabula.configClient.guiMaxDecimals + "f", box.dimX));
                ((ElementNumberInput)getById("dimY")).setText(String.format(Locale.ENGLISH, "%." + Tabula.configClient.guiMaxDecimals + "f", box.dimY));
                ((ElementNumberInput)getById("dimZ")).setText(String.format(Locale.ENGLISH, "%." + Tabula.configClient.guiMaxDecimals + "f", box.dimZ));

                ((ElementNumberInput)getById("offX")).setText(String.format(Locale.ENGLISH, "%." + Tabula.configClient.guiMaxDecimals + "f", box.posX));
                ((ElementNumberInput)getById("offY")).setText(String.format(Locale.ENGLISH, "%." + Tabula.configClient.guiMaxDecimals + "f", box.posY));
                ((ElementNumberInput)getById("offZ")).setText(String.format(Locale.ENGLISH, "%." + Tabula.configClient.guiMaxDecimals + "f", box.posZ));

                ((ElementNumberInput)getById("expX")).setText(String.format(Locale.ENGLISH, "%." + Tabula.configClient.guiMaxDecimals + "f", box.expandX));
                ((ElementNumberInput)getById("expY")).setText(String.format(Locale.ENGLISH, "%." + Tabula.configClient.guiMaxDecimals + "f", box.expandY));
                ((ElementNumberInput)getById("expZ")).setText(String.format(Locale.ENGLISH, "%." + Tabula.configClient.guiMaxDecimals + "f", box.expandZ));

                ((ElementNumberInput)getById("texX")).setText(Integer.toString(box.texOffX));
                ((ElementNumberInput)getById("texY")).setText(Integer.toString(box.texOffY));
                currentBox = box;
            }
            else
            {
                ((ElementTextField)getById("boxName")).setText("");

                ((ElementNumberInput)getById("dimX")).setText("");
                ((ElementNumberInput)getById("dimY")).setText("");
                ((ElementNumberInput)getById("dimZ")).setText("");

                ((ElementNumberInput)getById("offX")).setText("");
                ((ElementNumberInput)getById("offY")).setText("");
                ((ElementNumberInput)getById("offZ")).setText("");

                ((ElementNumberInput)getById("expX")).setText("");
                ((ElementNumberInput)getById("expY")).setText("");
                ((ElementNumberInput)getById("expZ")).setText("");

                ((ElementNumberInput)getById("texX")).setText("");
                ((ElementNumberInput)getById("texY")).setText("");
            }
        }
    }
}
