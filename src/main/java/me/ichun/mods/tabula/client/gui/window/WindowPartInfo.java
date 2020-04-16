package me.ichun.mods.tabula.client.gui.window;

import me.ichun.mods.ichunutil.client.gui.bns.window.Window;
import me.ichun.mods.ichunutil.client.gui.bns.window.constraint.Constraint;
import me.ichun.mods.ichunutil.client.gui.bns.window.view.View;
import me.ichun.mods.ichunutil.client.gui.bns.window.view.element.*;
import me.ichun.mods.ichunutil.common.module.tabula.project.Project;
import me.ichun.mods.tabula.client.gui.IProjectInfo;
import me.ichun.mods.tabula.client.gui.WorkspaceTabula;
import me.ichun.mods.tabula.client.tabula.Mainframe;
import net.minecraft.client.resources.I18n;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Locale;
import java.util.function.Consumer;

public class WindowPartInfo extends Window<WorkspaceTabula>
        implements IProjectInfo
{
    public final Mainframe mainframe;
    public final ViewPartInfo viewPartInfo;

    public WindowPartInfo(WorkspaceTabula parent)
    {
        super(parent);
        mainframe = parent.mainframe;

        setView(viewPartInfo = new ViewPartInfo(this));
        setId("windowPartInfo");
        size(180, 225);
    }

    public static class ViewPartInfo extends View<WindowPartInfo>
            implements IProjectInfo
    {
        public @Nullable Mainframe.ProjectInfo currentInfo = null;
        @Nullable
        private Project.Part currentPart = null;

        public ViewPartInfo(@Nonnull WindowPartInfo parent)
        {
            super(parent, "window.partInfo.title");
            setId("viewPartInfo");

            int numberInputWidth = 45;
            int spacing = 2;
            int padding = 0;

            Consumer<String> responder = s -> {
                if(!s.isEmpty())
                {
                    updatePart();
                }
            };

            ElementTextWrapper text1 = new ElementTextWrapper(this);
            text1.setNoWrap().setText(I18n.format("window.partInfo.partName"));
            text1.setConstraint(new Constraint(text1).left(this, Constraint.Property.Type.LEFT, 2).top(this, Constraint.Property.Type.TOP, 2));
            elements.add(text1);

            ElementTextField input = new ElementTextField(this);
            input.setDefaultText("").setSize(numberInputWidth * 3, 14).setId("partName");
            input.setResponder(responder);
            input.setConstraint(new Constraint(input).left(text1, Constraint.Property.Type.LEFT, 2).top(text1, Constraint.Property.Type.BOTTOM, padding).right(this, Constraint.Property.Type.RIGHT, 4));
            elements.add(input);

            ElementTextWrapper text = new ElementTextWrapper(this);
            text.setNoWrap().setText(I18n.format("window.partInfo.texDims"));
            text.setConstraint(new Constraint(text).left(text1, Constraint.Property.Type.LEFT, 0).top(input, Constraint.Property.Type.BOTTOM, spacing));
            elements.add(text);

            ElementSharedSpace space = new ElementSharedSpace(this, ElementScrollBar.Orientation.HORIZONTAL);
            space.setSize(14, 14).setConstraint(new Constraint(space).left(this, Constraint.Property.Type.LEFT, 4).top(text, Constraint.Property.Type.BOTTOM, padding).right(this, Constraint.Property.Type.RIGHT, 4));
            elements.add(space);

            ElementNumberInput num = new ElementNumberInput(this, false);
            num.setMin(0).setSize(numberInputWidth, 14).setId("txWidth");
            num.setResponder(responder);
            num.setConstraint(new Constraint(num).left(space, Constraint.Property.Type.LEFT, 0).top(space, Constraint.Property.Type.TOP, 0));
            space.addElement(num);

            ElementNumberInput num1 = new ElementNumberInput(this, false);
            num1.setMin(0).setSize(numberInputWidth, 14).setId("txHeight");
            num1.setResponder(responder);
            num1.setConstraint(new Constraint(num1).left(num, Constraint.Property.Type.RIGHT, 0).top(num, Constraint.Property.Type.TOP, 0));
            space.addElement(num1);

            ElementToggle<?> btn = new ElementToggle<>(this, "window.partInfo.matchProject", elementClickable -> {
                updatePart();
            });
            btn.setSize(numberInputWidth - 2, 14).setTooltip(I18n.format("window.partInfo.matchProjectFull"));
            btn.setId("txMatch");
            btn.setConstraint(new Constraint(btn).left(num1, Constraint.Property.Type.RIGHT, 2).top(num1, Constraint.Property.Type.TOP, 0).right(space, Constraint.Property.Type.RIGHT, 0));
            space.addElement(btn);

            text = new ElementTextWrapper(this);
            text.setNoWrap().setText(I18n.format("window.controls.txOffset"));
            text.setConstraint(new Constraint(text).left(text1, Constraint.Property.Type.LEFT, 0).top(num, Constraint.Property.Type.BOTTOM, spacing));
            elements.add(text);

            space = new ElementSharedSpace(this, ElementScrollBar.Orientation.HORIZONTAL);
            space.setSize(14, 14).setConstraint(new Constraint(space).left(this, Constraint.Property.Type.LEFT, 4).top(text, Constraint.Property.Type.BOTTOM, padding).right(this, Constraint.Property.Type.RIGHT, 4));
            elements.add(space);

            num = new ElementNumberInput(this, false);
            num.setMin(0).setSize(numberInputWidth, 14).setId("txOffX");
            num.setResponder(responder);
            num.setConstraint(new Constraint(num).left(space, Constraint.Property.Type.LEFT, 0).top(space, Constraint.Property.Type.TOP, 0));
            space.addElement(num);

            num1 = new ElementNumberInput(this, false);
            num1.setMin(0).setSize(numberInputWidth, 14).setId("txOffY");
            num1.setResponder(responder);
            num1.setConstraint(new Constraint(num1).left(num, Constraint.Property.Type.RIGHT, 0).top(num, Constraint.Property.Type.TOP, 0));
            space.addElement(num1);

            btn = new ElementToggle<>(this, "window.controls.txMirror", elementClickable -> {
                updatePart();
            });
            btn.setSize(numberInputWidth - 2, 14).setTooltip(I18n.format("window.controls.txMirrorFull"));
            btn.setId("txMirror");
            btn.setConstraint(new Constraint(btn).left(num1, Constraint.Property.Type.RIGHT, 2).top(num1, Constraint.Property.Type.TOP, 0).right(space, Constraint.Property.Type.RIGHT, 0));
            space.addElement(btn);

            text = new ElementTextWrapper(this);
            text.setNoWrap().setText(I18n.format("window.controls.position"));
            text.setConstraint(new Constraint(text).left(text1, Constraint.Property.Type.LEFT, 0).top(num, Constraint.Property.Type.BOTTOM, spacing));
            elements.add(text);

            space = new ElementSharedSpace(this, ElementScrollBar.Orientation.HORIZONTAL);
            space.setSize(14, 14).setConstraint(new Constraint(space).left(this, Constraint.Property.Type.LEFT, 4).top(text, Constraint.Property.Type.BOTTOM, padding).right(this, Constraint.Property.Type.RIGHT, 4));
            elements.add(space);

            num = new ElementNumberInput(this, true);
            num.setMaxDec(2).setSize(numberInputWidth, 14).setId("posX");
            num.setResponder(responder);
            num.setConstraint(new Constraint(num).left(space, Constraint.Property.Type.LEFT, 0).top(space, Constraint.Property.Type.TOP, 0));
            space.addElement(num);

            num1 = new ElementNumberInput(this, true);
            num1.setMaxDec(2).setSize(numberInputWidth, 14).setId("posY");
            num1.setResponder(responder);
            num1.setConstraint(new Constraint(num1).left(num, Constraint.Property.Type.RIGHT, 0).top(num, Constraint.Property.Type.TOP, 0));
            space.addElement(num1);

            ElementNumberInput num2 = new ElementNumberInput(this, true);
            num2.setMaxDec(2).setSize(numberInputWidth, 14).setId("posZ");
            num2.setResponder(responder);
            num2.setConstraint(new Constraint(num2).left(num1, Constraint.Property.Type.RIGHT, 0).top(num, Constraint.Property.Type.TOP, 0).right(space, Constraint.Property.Type.RIGHT, 0));
            space.addElement(num2);

            text = new ElementTextWrapper(this);
            text.setNoWrap().setText(I18n.format("window.controls.rotation"));
            text.setConstraint(new Constraint(text).left(text1, Constraint.Property.Type.LEFT, 0).top(num, Constraint.Property.Type.BOTTOM, spacing));
            elements.add(text);

            space = new ElementSharedSpace(this, ElementScrollBar.Orientation.HORIZONTAL);
            space.setSize(14, 14).setConstraint(new Constraint(space).left(this, Constraint.Property.Type.LEFT, 4).top(text, Constraint.Property.Type.BOTTOM, padding).right(this, Constraint.Property.Type.RIGHT, 4));
            elements.add(space);

            num = new ElementNumberInput(this, true);
            num.setMaxDec(2).setMin(-180).setMax(180).setSize(numberInputWidth, 14).setId("rotX");
            num.setResponder(responder);
            num.setConstraint(new Constraint(num).left(space, Constraint.Property.Type.LEFT, 0).top(space, Constraint.Property.Type.TOP, 0));
            space.addElement(num);

            num1 = new ElementNumberInput(this, true);
            num1.setMaxDec(2).setMin(-180).setMax(180).setSize(numberInputWidth, 14).setId("rotY");
            num1.setResponder(responder);
            num1.setConstraint(new Constraint(num1).left(num, Constraint.Property.Type.RIGHT, 0).top(num, Constraint.Property.Type.TOP, 0));
            space.addElement(num1);

            num2 = new ElementNumberInput(this, true);
            num2.setMaxDec(2).setMin(-180).setMax(180).setSize(numberInputWidth, 14).setId("rotZ");
            num2.setResponder(responder);
            num2.setConstraint(new Constraint(num2).left(num1, Constraint.Property.Type.RIGHT, 0).top(num, Constraint.Property.Type.TOP, 0).right(space, Constraint.Property.Type.RIGHT, 0));
            space.addElement(num2);

            ElementScrollBar<?> scrollBar = new ElementScrollBar<>(this, ElementScrollBar.Orientation.HORIZONTAL, 0.05F);
            scrollBar.setScrollProg(0.5F);
            scrollBar.setCallback(scroll -> {
                ((ElementNumberInput)getById("rotX")).setText(String.format(Locale.ENGLISH, "%.2f", (scroll.scrollProg * 360F) - 180F));
            }).setId("scrollX");
            scrollBar.setConstraint(new Constraint(scrollBar).left(num, Constraint.Property.Type.LEFT, 0).right(num2, Constraint.Property.Type.RIGHT, 0).top(num, Constraint.Property.Type.BOTTOM, spacing * 2).right(this, Constraint.Property.Type.RIGHT, 4));
            elements.add(scrollBar);

            ElementScrollBar<?> scrollBar1 = new ElementScrollBar<>(this, ElementScrollBar.Orientation.HORIZONTAL, 0.05F);
            scrollBar1.setScrollProg(0.5F);
            scrollBar1.setCallback(scroll -> {
                ((ElementNumberInput)getById("rotY")).setText(String.format(Locale.ENGLISH, "%.2f", (scroll.scrollProg * 360F) - 180F));
            }).setId("scrollY");
            scrollBar1.setConstraint(new Constraint(scrollBar1).left(num, Constraint.Property.Type.LEFT, 0).right(num2, Constraint.Property.Type.RIGHT, 0).top(scrollBar, Constraint.Property.Type.BOTTOM, spacing).right(this, Constraint.Property.Type.RIGHT, 4));
            elements.add(scrollBar1);

            ElementScrollBar<?> scrollBar2 = new ElementScrollBar<>(this, ElementScrollBar.Orientation.HORIZONTAL, 0.05F);
            scrollBar2.setScrollProg(0.5F);
            scrollBar2.setCallback(scroll -> {
                ((ElementNumberInput)getById("rotZ")).setText(String.format(Locale.ENGLISH, "%.2f", (scroll.scrollProg * 360F) - 180F));
            }).setId("scrollZ");
            scrollBar2.setConstraint(new Constraint(scrollBar2).left(num, Constraint.Property.Type.LEFT, 0).right(num2, Constraint.Property.Type.RIGHT, 0).top(scrollBar1, Constraint.Property.Type.BOTTOM, spacing).right(this, Constraint.Property.Type.RIGHT, 4));
            elements.add(scrollBar2);

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
                    selectPart(currentInfo.getSelectedPart());
                }
                else
                {
                    selectPart(null);
                }
            }
        }

        private void updatePart()
        {
            if(currentPart != null)
            {
                String preUpdate = currentPart.getJsonWithoutChildren();

                currentPart.name = ((ElementTextField)getById("partName")).getText();

                if(((ElementToggle<?>)getById("txMatch")).toggleState)
                {
                    currentPart.matchProject = true;
                }
                else
                {
                    currentPart.matchProject = false;
                    currentPart.texWidth = ((ElementNumberInput)getById("txWidth")).getInt();
                    currentPart.texHeight = ((ElementNumberInput)getById("txHeight")).getInt();
                }

                currentPart.texOffX = ((ElementNumberInput)getById("txOffX")).getInt();
                currentPart.texOffY = ((ElementNumberInput)getById("txOffY")).getInt();
                currentPart.mirror = ((ElementToggle<?>)getById("txMirror")).toggleState;

                currentPart.rotPX = (float)((ElementNumberInput)getById("posX")).getDouble();
                currentPart.rotPY = (float)((ElementNumberInput)getById("posY")).getDouble();
                currentPart.rotPZ = (float)((ElementNumberInput)getById("posZ")).getDouble();

                currentPart.rotAX = (float)((ElementNumberInput)getById("rotX")).getDouble();
                currentPart.rotAY = (float)((ElementNumberInput)getById("rotY")).getDouble();
                currentPart.rotAZ = (float)((ElementNumberInput)getById("rotZ")).getDouble();

                String postUpdate = currentPart.getJsonWithoutChildren();
                if(!postUpdate.equals(preUpdate))
                {
                    parentFragment.mainframe.updatePart(currentPart);
                }
            }
        }

        private boolean selectPart(Project.Part part)
        {
            currentPart = null;
            if(part != null)
            {
                ((ElementTextField)getById("partName")).setText(part.name);
                if(part.matchProject)
                {
                    ((ElementNumberInput)getById("txWidth")).setText("");
                    ((ElementNumberInput)getById("txHeight")).setText("");
                    ((ElementToggle<?>)getById("txMatch")).setToggled(true);
                }
                else
                {
                    ((ElementNumberInput)getById("txWidth")).setText(Integer.toString(part.texWidth));
                    ((ElementNumberInput)getById("txHeight")).setText(Integer.toString(part.texHeight));
                    ((ElementToggle<?>)getById("txMatch")).setToggled(false);
                }
                ((ElementNumberInput)getById("txOffX")).setText(Integer.toString(part.texOffX));
                ((ElementNumberInput)getById("txOffY")).setText(Integer.toString(part.texOffY));
                ((ElementToggle<?>)getById("txMirror")).setToggled(part.mirror);

                ((ElementNumberInput)getById("posX")).setText(String.format(Locale.ENGLISH, "%.2f", part.rotPX));
                ((ElementNumberInput)getById("posY")).setText(String.format(Locale.ENGLISH, "%.2f", part.rotPY));
                ((ElementNumberInput)getById("posZ")).setText(String.format(Locale.ENGLISH, "%.2f", part.rotPZ));

                ((ElementNumberInput)getById("rotX")).setText(String.format(Locale.ENGLISH, "%.2f", part.rotAX));
                ((ElementNumberInput)getById("rotY")).setText(String.format(Locale.ENGLISH, "%.2f", part.rotAY));
                ((ElementNumberInput)getById("rotZ")).setText(String.format(Locale.ENGLISH, "%.2f", part.rotAZ));

                ((ElementScrollBar<?>)getById("scrollX")).setScrollProg((part.rotAX + 180F) / 360F);
                ((ElementScrollBar<?>)getById("scrollY")).setScrollProg((part.rotAY + 180F) / 360F);
                ((ElementScrollBar<?>)getById("scrollZ")).setScrollProg((part.rotAZ + 180F) / 360F);
                currentPart = part;
            }
            else
            {
                ((ElementTextField)getById("partName")).setText("");

                    ((ElementNumberInput)getById("txWidth")).setText("");
                    ((ElementNumberInput)getById("txHeight")).setText("");
                    ((ElementToggle<?>)getById("txMatch")).setToggled(false);

                ((ElementNumberInput)getById("txOffX")).setText("");
                ((ElementNumberInput)getById("txOffY")).setText("");
                ((ElementToggle<?>)getById("txMirror")).setToggled(false);

                ((ElementNumberInput)getById("posX")).setText("");
                ((ElementNumberInput)getById("posY")).setText("");
                ((ElementNumberInput)getById("posZ")).setText("");

                ((ElementNumberInput)getById("rotX")).setText("");
                ((ElementNumberInput)getById("rotY")).setText("");
                ((ElementNumberInput)getById("rotZ")).setText("");

                ((ElementScrollBar<?>)getById("scrollX")).setScrollProg(0.5F);
                ((ElementScrollBar<?>)getById("scrollY")).setScrollProg(0.5F);
                ((ElementScrollBar<?>)getById("scrollZ")).setScrollProg(0.5F);
            }
            return true;
        }
    }
}
