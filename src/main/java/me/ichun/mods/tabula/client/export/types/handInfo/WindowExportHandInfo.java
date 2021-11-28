package me.ichun.mods.tabula.client.export.types.handInfo;

import me.ichun.mods.ichunutil.api.client.hand.HandInfo;
import me.ichun.mods.ichunutil.api.common.PlacementCorrector;
import me.ichun.mods.ichunutil.client.gui.bns.window.Window;
import me.ichun.mods.ichunutil.client.gui.bns.window.WindowPopup;
import me.ichun.mods.ichunutil.client.gui.bns.window.constraint.Constraint;
import me.ichun.mods.ichunutil.client.gui.bns.window.view.View;
import me.ichun.mods.ichunutil.client.gui.bns.window.view.element.*;
import me.ichun.mods.ichunutil.common.module.tabula.formats.ImportList;
import me.ichun.mods.ichunutil.common.module.tabula.project.Project;
import me.ichun.mods.tabula.client.export.ExportList;
import me.ichun.mods.tabula.client.gui.WorkspaceTabula;
import me.ichun.mods.tabula.client.tabula.Mainframe;
import me.ichun.mods.tabula.common.Tabula;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.HandSide;

import javax.annotation.Nonnull;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.function.Consumer;

public class WindowExportHandInfo extends Window<WorkspaceTabula>
{
    public final Mainframe.ProjectInfo projectInfo;
    public LinkedHashSet<Project.Part> leftParts;
    public LinkedHashSet<Project.Part> rightParts;
    public ArrayList<Project.Part> alignedLeft;
    public ArrayList<Project.Part> alignedRight;

    public WindowExportHandInfo(WorkspaceTabula parent, Mainframe.ProjectInfo project)
    {
        super(parent);

        this.projectInfo = project;

        disableDockingEntirely();
    }

    public static WindowExportHandInfo open(WorkspaceTabula parent, Mainframe.ProjectInfo project)
    {
        project.selectPart(null); //Deselect any currently selected parts.

        WindowExportHandInfo window = new WindowExportHandInfo(parent, project);
        window.setView(new ViewSelectParts(window, HandSide.LEFT));
        window.pos(0, 20);
        window.size(180, parent.getHeight() - 20);
        parent.addWindow(window);
        parent.setListener(window);
        window.init();
        return window;
    }

    public void nextStep()
    {
        if(currentView instanceof ViewSelectParts)
        {
            ViewSelectParts view = (ViewSelectParts)currentView;
            if(view.handSide == HandSide.LEFT)
            {
                leftParts = view.parts;
                projectInfo.selectPart(null);
                setView(new ViewSelectParts(this, HandSide.RIGHT));
                getCurrentView().init();
                setListener(getCurrentView());
            }
            else
            {
                rightParts = view.parts;
                //load player ghost
                InputStream in = Tabula.class.getResourceAsStream("/PlayerGhost.tbl");
                if(in != null)
                {
                    Project ghost = ImportList.IMPORTER_TABULA.createProject(in);
                    if(ghost != null)
                    {
                        ghost.projVersion = ImportList.IMPORTER_TABULA.getProjectVersion();
                        ghost.load();

                        projectInfo.setGhostProject(ghost, 0.2F);
                    }
                }

                //Delete all the non-relevant parts
                ArrayList<Project.Part> parts = new ArrayList<>(projectInfo.project.parts);
                for(Project.Part part : parts)
                {
                    if(!(leftParts.contains(part) || rightParts.contains(part)))
                    {
                        projectInfo.delete(part);
                    }
                }

                for(Project.Part leftPart : leftParts)
                {
                    leftPart.rotAX = 0F;
                    parent.mainframe.updatePart(leftPart, true);
                }

                for(Project.Part rightPart : rightParts)
                {
                    rightPart.rotAX = 0F;

                    if(!leftParts.contains(rightPart))
                    {
                        rightPart.showModel = false;
                    }

                    parent.mainframe.updatePart(rightPart, true);
                }

                alignedLeft = new ArrayList<>(leftParts);
                alignedRight = new ArrayList<>(rightParts);

                setView(new ViewAdjustParts(this, HandSide.LEFT, alignedLeft));
                getCurrentView().init();
                setListener(getCurrentView());

                //make a copy of the parts
                LinkedHashSet<Project.Part> copyParts = new LinkedHashSet<>();
                for(Project.Part part : leftParts)
                {
                    copyParts.add(part.clone());
                }
                leftParts = copyParts;

                copyParts = new LinkedHashSet<>();
                for(Project.Part part : rightParts)
                {
                    copyParts.add(part.clone());
                }
                rightParts = copyParts;
            }
        }
        else if(currentView instanceof ViewAdjustParts)
        {
            ViewAdjustParts view = (ViewAdjustParts)currentView;
            if(view.handSide == HandSide.LEFT)
            {
                for(Project.Part leftPart : alignedLeft)
                {
                    leftPart.showModel = false;
                    parent.mainframe.updatePart(leftPart, true);
                }

                for(Project.Part rightPart : alignedRight)
                {
                    rightPart.showModel = true;
                    parent.mainframe.updatePart(rightPart, true);
                }

                setView(new ViewAdjustParts(this, HandSide.RIGHT, alignedRight));
                getCurrentView().init();
                setListener(getCurrentView());

                //make a copy of the parts
                LinkedHashSet<Project.Part> copyParts = new LinkedHashSet<>();
                for(Project.Part part : rightParts)
                {
                    copyParts.add(part.clone());
                }

                rightParts = copyParts;
            }
            else
            {
                for(Project.Part leftPart : alignedLeft)
                {
                    leftPart.showModel = false;
                    parent.mainframe.updatePart(leftPart, true);
                }

                if(!ExportList.EXPORTERS.get("handInfo").export(projectInfo.project, export()))
                {
                    WindowPopup.popup(parent, 0.4D, 140, null, I18n.format("export.failed"));
                }
                else
                {
                    WindowPopup.popup(parent, 0.3D, 140, null, I18n.format("gui.done"));
                }
                cancel();
            }
        }
    }

    public void cancel()
    {
        projectInfo.selectPart(null);
        projectInfo.setGhostProject(null, 0.2F);
        parent.removeWindow(this);
    }

    public HandInfo export()
    {
        HandInfo info = new HandInfo();

        String forClass = null;

        for(String note : projectInfo.project.notes)
        {
            if(note.startsWith("model-class:"))
            {
                forClass = note.substring("model-class:".length());
                break;
            }
        }

        if(forClass == null)
        {
            forClass = "Model Class Goes Here";
        }

        info.forClass = forClass;

        info.leftHandParts = new HandInfo.ModelRendererMarker[alignedLeft.size()];
        info.rightHandParts = new HandInfo.ModelRendererMarker[alignedRight.size()];

        addMarkers(info.leftHandParts, alignedLeft, leftParts);
        addMarkers(info.rightHandParts, alignedRight, rightParts);

        return info;
    }

    public void addMarkers(HandInfo.ModelRendererMarker[] markers, ArrayList<Project.Part> alignedParts, LinkedHashSet<Project.Part> oriParts)
    {
        for(int i = 0; i < markers.length; i++)
        {
            HandInfo.ModelRendererMarker marker = new HandInfo.ModelRendererMarker();
            Project.Part aligned = alignedParts.get(i);

            marker.fieldName = aligned.name;

            Project.Part ori = null;

            for(Project.Part oriPart : oriParts)
            {
                if(aligned.name.equals(oriPart.name))
                {
                    ori = oriPart;
                    break;
                }
            }

            ArrayList<PlacementCorrector> placementCorrectors = new ArrayList<>();

            //Reverse the rotationZYX by the model first, in XYZ order.
            if(ori.rotAX != 0F)
            {
                PlacementCorrector corrector = new PlacementCorrector();
                corrector.type = "rotate";
                corrector.axis = "x";
                corrector.amount = (double)(-ori.rotAX);
                placementCorrectors.add(corrector);
            }

            if(ori.rotAY != 0F)
            {
                PlacementCorrector corrector = new PlacementCorrector();
                corrector.type = "rotate";
                corrector.axis = "y";
                corrector.amount = (double)(-ori.rotAY);
                placementCorrectors.add(corrector);
            }

            if(ori.rotAZ != 0F)
            {
                PlacementCorrector corrector = new PlacementCorrector();
                corrector.type = "rotate";
                corrector.axis = "z";
                corrector.amount = (double)(-ori.rotAZ);
                placementCorrectors.add(corrector);
            }

            //Translate to the right position
            if(ori.rotPX != aligned.rotPX)
            {
                PlacementCorrector corrector = new PlacementCorrector();
                corrector.type = "translate";
                corrector.axis = "x";
                corrector.amount = (aligned.rotPX - ori.rotPX) / 16D;
                placementCorrectors.add(corrector);
            }

            if(ori.rotPY != aligned.rotPY)
            {
                PlacementCorrector corrector = new PlacementCorrector();
                corrector.type = "translate";
                corrector.axis = "y";
                corrector.amount = (aligned.rotPY - ori.rotPY) / 16D;
                placementCorrectors.add(corrector);
            }

            if(ori.rotPZ != aligned.rotPZ)
            {
                PlacementCorrector corrector = new PlacementCorrector();
                corrector.type = "translate";
                corrector.axis = "z";
                corrector.amount = (aligned.rotPZ - ori.rotPZ) / 16D;
                placementCorrectors.add(corrector);
            }

            if(aligned.rotAZ != 0D)
            {
                PlacementCorrector corrector = new PlacementCorrector();
                corrector.type = "rotate";
                corrector.axis = "z";
                corrector.amount = (double)(aligned.rotAZ);
                placementCorrectors.add(corrector);
            }

            if(aligned.rotAY != 0D)
            {
                PlacementCorrector corrector = new PlacementCorrector();
                corrector.type = "rotate";
                corrector.axis = "y";
                corrector.amount = (double)(aligned.rotAY);
                placementCorrectors.add(corrector);
            }

            if(aligned.rotAX != 0D)
            {
                PlacementCorrector corrector = new PlacementCorrector();
                corrector.type = "rotate";
                corrector.axis = "x";
                corrector.amount = (double)(aligned.rotAX);
                placementCorrectors.add(corrector);
            }

            placementCorrectors.removeIf(pc -> Math.abs(pc.amount) == 0.0D); //Remove -0.0s

            if(!placementCorrectors.isEmpty())
            {
                marker.placementCorrectors = placementCorrectors.toArray(new PlacementCorrector[0]);
            }

            markers[i] = marker;
        }
    }

    public static class ViewSelectParts extends View<WindowExportHandInfo>
    {
        public final HandSide handSide;
        public final LinkedHashSet<Project.Part> parts = new LinkedHashSet<>();

        public ViewSelectParts(WindowExportHandInfo parent, HandSide side)
        {
            super(parent, "");

            handSide = side;

            ElementButton<?> button = new ElementButton<>(this, "gui.cancel", elementClickable ->
            {
                parent.cancel();
            });
            button.setId("btnCancel");
            button.setSize(60, 20);
            button.setConstraint(new Constraint(button).bottom(this, Constraint.Property.Type.BOTTOM, 10).right(this, Constraint.Property.Type.RIGHT, 10));
            elements.add(button);

            ElementButton<?> button1 = new ElementButton<>(this, "gui.ok", btn -> {
                parent.nextStep();
            });
            button1.setId("btnOk");
            button1.setSize(60, 20);
            button1.setConstraint(new Constraint(button1).right(button, Constraint.Property.Type.LEFT, 10));
            elements.add(button1);

            ElementTextWrapper text = new ElementTextWrapper(this);
            text.setText(I18n.format(handSide == HandSide.LEFT ? "tabula.export.handInfo.pick.leftParts" : "tabula.export.handInfo.pick.rightParts"));
            text.setId("text");
            text.constraints().left(this, Constraint.Property.Type.LEFT, 10).right(this, Constraint.Property.Type.RIGHT, 10).top(this, Constraint.Property.Type.TOP, 10);
            elements.add(text);
        }

        public void populateButtons()
        {
            elements.removeIf(element -> element.id == null);

            Element<?> lastElement = getById("text");

            for(Project.Part part : parts)
            {
                ElementButton<?> btn = new ElementButton<>(this, "X", b -> {
                    if(parts.remove(part))
                    {
                        parentFragment.projectInfo.selectPart(null);
                        populateButtons();
                    }
                });
                btn.setSize(16, 16);
                btn.constraints().left(lastElement, Constraint.Property.Type.LEFT, 0).top(lastElement, Constraint.Property.Type.BOTTOM, 5);
                elements.add(btn);

                ElementTextWrapper text = new ElementTextWrapper(this);
                text.setText(part.name);
                text.constraints().left(btn, Constraint.Property.Type.RIGHT, 6).top(btn, Constraint.Property.Type.TOP, 1).right(this, Constraint.Property.Type.RIGHT, 10);
                elements.add(text);

                lastElement = btn;
            }

            this.resize(getWorkspace().getMinecraft(), getWidth(), getHeight());
        }

        @Override
        public void tick()
        {
            super.tick();

            //Add selected parts
            if(parentFragment.projectInfo.getSelectedPart() != null)
            {
                if(parts.add(parentFragment.projectInfo.getSelectedPart())) //successfully added
                {
                    populateButtons();
                }
            }
        }
    }

    public static class ViewAdjustParts extends View<WindowExportHandInfo>
    {
        public final HandSide handSide;
        public final ArrayList<Project.Part> parts;

        public Project.Part currentPart;
        public int currentIndex = -1;

        public ViewAdjustParts(@Nonnull WindowExportHandInfo parent, HandSide side, ArrayList<Project.Part> parts)
        {
            super(parent, "");
            this.handSide = side;
            this.parts = parts;

            ElementButton<?> button = new ElementButton<>(this, "gui.cancel", elementClickable ->
            {
                parent.cancel();
            });
            button.setId("btnCancel");
            button.setSize(60, 20);
            button.setConstraint(new Constraint(button).bottom(this, Constraint.Property.Type.BOTTOM, 10).right(this, Constraint.Property.Type.RIGHT, 10));
            elements.add(button);

            ElementButton<?> button1 = new ElementButton<>(this, "gui.ok", btn -> {
                currentIndex++;
                if(currentIndex < parts.size())
                {
                    populatePart();
                }
                else
                {
                    parent.nextStep();
                }
            });
            button1.setId("btnOk");
            button1.setSize(60, 20);
            button1.setConstraint(new Constraint(button1).right(button, Constraint.Property.Type.LEFT, 10));
            elements.add(button1);

            ElementTextWrapper text = new ElementTextWrapper(this);
            text.setText(I18n.format(handSide == HandSide.LEFT ? "tabula.export.handInfo.set.leftParts" : "tabula.export.handInfo.set.rightParts"));
            text.setId("text");
            text.constraints().left(this, Constraint.Property.Type.LEFT, 10).right(this, Constraint.Property.Type.RIGHT, 10).top(this, Constraint.Property.Type.TOP, 10);
            elements.add(text);

            //Elements
            int numberInputWidth = 45;
            Consumer<String> responder = s -> {
                if(!s.isEmpty())
                {
                    updatePart();
                }
            };
            Consumer<String> enterResponder = s -> updatePart(); //bypass the empty check

            ElementTextWrapper text1 = new ElementTextWrapper(this);
            text1.setNoWrap().setText(I18n.format("window.controls.position"));
            text1.constraints().left(text, Constraint.Property.Type.LEFT, 0).top(text, Constraint.Property.Type.BOTTOM, 10);
            elements.add(text1);

            ElementSharedSpace space = new ElementSharedSpace(this, ElementScrollBar.Orientation.HORIZONTAL);
            space.setSize(14, 14).constraints().left(text1, Constraint.Property.Type.LEFT, 0).top(text1, Constraint.Property.Type.BOTTOM, 2).right(text, Constraint.Property.Type.RIGHT, 0);
            elements.add(space);

            ElementNumberInput num = new ElementNumberInput(space, true);
            num.setMaxDec(Tabula.configClient.guiMaxDecimals).setSize(numberInputWidth, 14).setId("posX");
            num.setResponder(responder).setEnterResponder(enterResponder);
            num.constraints().left(space, Constraint.Property.Type.LEFT, 0).top(space, Constraint.Property.Type.TOP, 0);
            space.addElement(num);

            ElementNumberInput num1 = new ElementNumberInput(space, true);
            num1.setMaxDec(Tabula.configClient.guiMaxDecimals).setSize(numberInputWidth, 14).setId("posY");
            num1.setResponder(responder).setEnterResponder(enterResponder);
            num1.constraints().left(num, Constraint.Property.Type.RIGHT, 0).top(space, Constraint.Property.Type.TOP, 0);
            space.addElement(num1);

            ElementNumberInput num2 = new ElementNumberInput(space, true);
            num2.setMaxDec(Tabula.configClient.guiMaxDecimals).setSize(numberInputWidth, 14).setId("posZ");
            num2.setResponder(responder).setEnterResponder(enterResponder);
            num2.constraints().left(num1, Constraint.Property.Type.RIGHT, 0).top(space, Constraint.Property.Type.TOP, 0).right(space, Constraint.Property.Type.RIGHT, 0);
            space.addElement(num2);

            ElementTextWrapper text2 = new ElementTextWrapper(this);
            text2.setNoWrap().setText(I18n.format("window.controls.rotation"));
            text2.constraints().left(text, Constraint.Property.Type.LEFT, 0).top(num, Constraint.Property.Type.BOTTOM, 5);
            elements.add(text2);

            space = new ElementSharedSpace(this, ElementScrollBar.Orientation.HORIZONTAL);
            space.setSize(14, 14).constraints().left(text2, Constraint.Property.Type.LEFT, 0).top(text2, Constraint.Property.Type.BOTTOM, 2).right(text, Constraint.Property.Type.RIGHT, 0);
            elements.add(space);

            num = new ElementNumberInput(space, true);
            num.setMaxDec(Tabula.configClient.guiMaxDecimals).setMin(-180).setMax(180).setSize(numberInputWidth, 14).setId("rotX");
            num.setResponder(responder).setEnterResponder(enterResponder);
            num.setConstraint(new Constraint(num).left(space, Constraint.Property.Type.LEFT, 0).top(space, Constraint.Property.Type.TOP, 0));
            space.addElement(num);

            num1 = new ElementNumberInput(space, true);
            num1.setMaxDec(Tabula.configClient.guiMaxDecimals).setMin(-180).setMax(180).setSize(numberInputWidth, 14).setId("rotY");
            num1.setResponder(responder).setEnterResponder(enterResponder);
            num1.setConstraint(new Constraint(num1).left(num, Constraint.Property.Type.RIGHT, 0).top(space, Constraint.Property.Type.TOP, 0));
            space.addElement(num1);

            num2 = new ElementNumberInput(space, true);
            num2.setMaxDec(Tabula.configClient.guiMaxDecimals).setMin(-180).setMax(180).setSize(numberInputWidth, 14).setId("rotZ");
            num2.setResponder(responder).setEnterResponder(enterResponder);
            num2.setConstraint(new Constraint(num2).left(num1, Constraint.Property.Type.RIGHT, 0).top(space, Constraint.Property.Type.TOP, 0).right(space, Constraint.Property.Type.RIGHT, 0));
            space.addElement(num2);
        }

        private void updatePart()
        {
            if(currentPart != null)
            {
                String preUpdate = currentPart.getJsonWithoutChildren();

                currentPart.rotPX = (float)((ElementNumberInput)getById("posX")).getDouble();
                currentPart.rotPY = (float)((ElementNumberInput)getById("posY")).getDouble();
                currentPart.rotPZ = (float)((ElementNumberInput)getById("posZ")).getDouble();

                currentPart.rotAX = (float)((ElementNumberInput)getById("rotX")).getDouble();
                currentPart.rotAY = (float)((ElementNumberInput)getById("rotY")).getDouble();
                currentPart.rotAZ = (float)((ElementNumberInput)getById("rotZ")).getDouble();

                String postUpdate = currentPart.getJsonWithoutChildren();
                if(!postUpdate.equals(preUpdate))
                {
                    parentFragment.parent.mainframe.updatePart(currentPart, true);
                }
            }
        }

        public void populatePart()
        {
            if(currentIndex < parts.size())
            {
                currentPart = null;

                Project.Part part = parts.get(currentIndex);

                ((ElementNumberInput)getById("posX")).setText(String.format(Locale.ENGLISH, "%." + Tabula.configClient.guiMaxDecimals + "f", part.rotPX));
                ((ElementNumberInput)getById("posY")).setText(String.format(Locale.ENGLISH, "%." + Tabula.configClient.guiMaxDecimals + "f", part.rotPY));
                ((ElementNumberInput)getById("posZ")).setText(String.format(Locale.ENGLISH, "%." + Tabula.configClient.guiMaxDecimals + "f", part.rotPZ));

                ((ElementNumberInput)getById("rotX")).setText(String.format(Locale.ENGLISH, "%." + Tabula.configClient.guiMaxDecimals + "f", part.rotAX));
                ((ElementNumberInput)getById("rotY")).setText(String.format(Locale.ENGLISH, "%." + Tabula.configClient.guiMaxDecimals + "f", part.rotAY));
                ((ElementNumberInput)getById("rotZ")).setText(String.format(Locale.ENGLISH, "%." + Tabula.configClient.guiMaxDecimals + "f", part.rotAZ));

                currentPart = part;

                parentFragment.projectInfo.selectPart(currentPart);
            }
        }

        @Override
        public void tick()
        {
            super.tick();

            if(currentIndex == -1)
            {
                currentIndex++;
                populatePart();
            }
        }
    }
}
