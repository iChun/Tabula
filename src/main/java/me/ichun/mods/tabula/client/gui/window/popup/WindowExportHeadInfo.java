package me.ichun.mods.tabula.client.gui.window.popup;

import me.ichun.mods.ichunutil.client.gui.bns.window.Window;
import me.ichun.mods.ichunutil.client.gui.bns.window.WindowPopup;
import me.ichun.mods.ichunutil.client.gui.bns.window.constraint.Constraint;
import me.ichun.mods.ichunutil.client.gui.bns.window.view.View;
import me.ichun.mods.ichunutil.client.gui.bns.window.view.element.*;
import me.ichun.mods.ichunutil.common.head.HeadHandler;
import me.ichun.mods.ichunutil.common.head.HeadInfo;
import me.ichun.mods.ichunutil.common.module.tabula.project.Project;
import me.ichun.mods.tabula.client.export.ExportList;
import me.ichun.mods.tabula.client.gui.WorkspaceTabula;
import me.ichun.mods.tabula.client.tabula.Mainframe;
import me.ichun.mods.tabula.common.Tabula;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.LivingEntity;

import javax.annotation.Nonnull;
import java.util.Locale;
import java.util.function.Consumer;

public class WindowExportHeadInfo extends Window<WorkspaceTabula>
{
    public WindowExportHeadInfo(WorkspaceTabula parent, Project project)
    {
        super(parent);

        disableDockingEntirely();

        setView(new ViewExportHeadInfo(this, project));
    }

    @Override
    public ViewExportHeadInfo getCurrentView()
    {
        return super.getCurrentView();
    }

    public static class ViewExportHeadInfo extends View<WindowExportHeadInfo>
    {
        public HeadInfo<?> infoInstance;

        public ViewExportHeadInfo(@Nonnull WindowExportHeadInfo parent, Project project)
        {
            super(parent, "tabula.export.headInfo.name");

            String forClass = null;
            for(String note : project.notes)
            {
                if(note.startsWith("suspected-origin-entity:"))
                {
                    forClass = note.substring("suspected-origin-entity:".length());
                    break;
                }
            }

            if(forClass != null)
            {
                try
                {
                    Class<?> clz = Class.forName(forClass);
                    if(LivingEntity.class.isAssignableFrom(clz))
                    {
                        HeadInfo<?> info = HeadHandler.getHelper((Class<? extends LivingEntity>)clz);
                        if(info != null)
                        {
                            HeadInfo<?> clone = HeadHandler.GSON.fromJson(HeadHandler.GSON.toJson(info), HeadInfo.class);
                            if(clone.customClass != null)
                            {
                                clone.customClass = null;
                                WindowPopup.popup(parent.parent, 0.6D, 0.6D, w ->{}, I18n.format("tabula.export.headInfo.customClassWarning"));
                            }

                            infoInstance = HeadHandler.GSON.fromJson(HeadHandler.GSON.toJson(clone), HeadInfo.class);
                        }
                    }
                }
                catch(ClassNotFoundException ignored){}
            }
            else
            {
                forClass = "Entity Class Goes Here";
            }

            if(infoInstance == null)
            {
                infoInstance = new HeadInfo<>();
                infoInstance.modelFieldName = "Model Name Goes Here";
                infoInstance.forClass = forClass;
            }

            Mainframe.ProjectInfo activeProject = parent.parent.mainframe.getActiveProject();
            if(activeProject != null)
            {
                Project.Part selectedPart = activeProject.getSelectedPart();
                if(selectedPart != null)
                {
                    infoInstance.modelFieldName = selectedPart.name;
                }
            }

            int spacing = 3;
            Element<?> last;

            ElementTextWrapper text = new ElementTextWrapper(this);
            text.setNoWrap().setText(I18n.format("tabula.export.headInfo.forClass"));
            text.constraints().left(this, Constraint.Property.Type.LEFT, 10).top(this, Constraint.Property.Type.TOP, 10);
            elements.add(text);
            last = text;

            ElementTextField textField = new ElementTextField(this);
            textField.setTooltip(I18n.format("tabula.export.headInfo.forClass.tooltip")).setId("forClass");
            textField.setDefaultText(infoInstance.forClass);
            textField.constraints().left(text, Constraint.Property.Type.RIGHT, 5).right(this, Constraint.Property.Type.RIGHT, 10).top(text, Constraint.Property.Type.TOP, 2);
            elements.add(textField);

            text = new ElementTextWrapper(this);
            text.setNoWrap().setText(I18n.format("tabula.export.headInfo.modelFieldName"));
            text.constraints().left(last, Constraint.Property.Type.LEFT, 0).top(last, Constraint.Property.Type.BOTTOM, spacing);
            elements.add(text);
            last = text;

            textField = new ElementTextField(this);
            textField.setTooltip(I18n.format("tabula.export.headInfo.modelFieldName.tooltip")).setId("modelFieldName");
            textField.setDefaultText(infoInstance.modelFieldName);
            textField.constraints().left(text, Constraint.Property.Type.RIGHT, 5).right(this, Constraint.Property.Type.RIGHT, 10).top(text, Constraint.Property.Type.TOP, 2);
            elements.add(textField);

            ElementToggle<?> toggle = new ElementToggle<>(this, "tabula.export.headInfo.isBoss", btn -> { updateHeadInfo(); });
            toggle.setSize(120, 14).setTooltip(I18n.format("tabula.export.headInfo.isBoss.tooltip")).setId("isBoss");
            toggle.setToggled(infoInstance.isBoss);
            toggle.constraints().left(last, Constraint.Property.Type.LEFT, 0).top(last, Constraint.Property.Type.BOTTOM, spacing);
            elements.add(toggle);

            ElementToggle<?> toggle1 = new ElementToggle<>(this, "tabula.export.headInfo.affectedByInvisibility", btn -> { updateHeadInfo(); });
            toggle1.setSize(140, 14).setTooltip(I18n.format("tabula.export.headInfo.affectedByInvisibility.tooltip")).setId("affectedByInvisibility");
            toggle1.setToggled(infoInstance.affectedByInvisibility);
            toggle1.constraints().left(toggle, Constraint.Property.Type.RIGHT, 8).top(last, Constraint.Property.Type.BOTTOM, spacing);
            elements.add(toggle1);
            last = toggle;

            toggle = new ElementToggle<>(this, "tabula.export.headInfo.noFaceInfo", btn -> { updateHeadInfo(); });
            toggle.setSize(120, 14).setTooltip(I18n.format("tabula.export.headInfo.noFaceInfo.tooltip")).setId("noFaceInfo");
            toggle.setToggled(infoInstance.noFaceInfo);
            toggle.constraints().left(last, Constraint.Property.Type.LEFT, 0).top(last, Constraint.Property.Type.BOTTOM, spacing);
            elements.add(toggle);
            last = toggle;

            Consumer<String> responder = s -> {
                updateHeadInfo();
            };

            text = new ElementTextWrapper(this);
            text.setNoWrap().setText(I18n.format("tabula.export.headInfo.eyeOffset"));
            text.constraints().left(last, Constraint.Property.Type.LEFT, 0).top(last, Constraint.Property.Type.BOTTOM, spacing);
            elements.add(text);

            ElementNumberInput numberInput = new ElementNumberInput(this, true);
            numberInput.setTooltip(I18n.format("tabula.export.headInfo.eyeOffset.tooltip")).setId("eyeOffset0");
            numberInput.setMaxDec(Tabula.configClient.guiMaxDecimals).setDefaultText(String.format(Locale.ENGLISH, "%." + Tabula.configClient.guiMaxDecimals + "f", infoInstance.eyeOffset[0] * 16F));
            numberInput.setResponder(responder).setEnterResponder(responder);
            numberInput.setWidth(50);
            numberInput.constraints().left(text, Constraint.Property.Type.RIGHT, 5).top(text, Constraint.Property.Type.TOP, 2);
            elements.add(numberInput);

            ElementNumberInput numberInput1 = new ElementNumberInput(this, true);
            numberInput1.setTooltip(I18n.format("tabula.export.headInfo.eyeOffset.tooltip")).setId("eyeOffset1");
            numberInput1.setMaxDec(Tabula.configClient.guiMaxDecimals).setDefaultText(String.format(Locale.ENGLISH, "%." + Tabula.configClient.guiMaxDecimals + "f", infoInstance.eyeOffset[1] * 16F));
            numberInput1.setResponder(responder).setEnterResponder(responder);
            numberInput1.setWidth(50);
            numberInput1.constraints().left(numberInput, Constraint.Property.Type.RIGHT, 5);
            elements.add(numberInput1);

            ElementNumberInput numberInput2 = new ElementNumberInput(this, true);
            numberInput2.setTooltip(I18n.format("tabula.export.headInfo.eyeOffset.tooltip")).setId("eyeOffset2");
            numberInput2.setMaxDec(Tabula.configClient.guiMaxDecimals).setDefaultText(String.format(Locale.ENGLISH, "%." + Tabula.configClient.guiMaxDecimals + "f", infoInstance.eyeOffset[2] * 16F));
            numberInput2.setResponder(responder).setEnterResponder(responder);
            numberInput2.setWidth(50);
            numberInput2.constraints().left(numberInput1, Constraint.Property.Type.RIGHT, 5);
            elements.add(numberInput2);
            last = text;

            text = new ElementTextWrapper(this);
            text.setNoWrap().setText(I18n.format("tabula.export.headInfo.irisColour"));
            text.constraints().left(last, Constraint.Property.Type.LEFT, 0).top(last, Constraint.Property.Type.BOTTOM, spacing);
            elements.add(text);

            numberInput = new ElementNumberInput(this, true);
            numberInput.setTooltip(I18n.format("tabula.export.headInfo.irisColour.tooltip")).setId("irisColour0");
            numberInput.setMin(0).setMax(1).setMaxDec(Tabula.configClient.guiMaxDecimals).setDefaultText(String.format(Locale.ENGLISH, "%." + Tabula.configClient.guiMaxDecimals + "f", infoInstance.irisColour[0]));
            numberInput.setResponder(responder).setEnterResponder(responder);
            numberInput.setWidth(50);
            numberInput.constraints().left(text, Constraint.Property.Type.RIGHT, 5).top(text, Constraint.Property.Type.TOP, 2);
            elements.add(numberInput);

            numberInput1 = new ElementNumberInput(this, true);
            numberInput1.setTooltip(I18n.format("tabula.export.headInfo.irisColour.tooltip")).setId("irisColour1");
            numberInput1.setMin(0).setMax(1).setMaxDec(Tabula.configClient.guiMaxDecimals).setDefaultText(String.format(Locale.ENGLISH, "%." + Tabula.configClient.guiMaxDecimals + "f", infoInstance.irisColour[1]));
            numberInput1.setResponder(responder).setEnterResponder(responder);
            numberInput1.setWidth(50);
            numberInput1.constraints().left(numberInput, Constraint.Property.Type.RIGHT, 5);
            elements.add(numberInput1);

            numberInput2 = new ElementNumberInput(this, true);
            numberInput2.setTooltip(I18n.format("tabula.export.headInfo.irisColour.tooltip")).setId("irisColour2");
            numberInput2.setMin(0).setMax(1).setMaxDec(Tabula.configClient.guiMaxDecimals).setDefaultText(String.format(Locale.ENGLISH, "%." + Tabula.configClient.guiMaxDecimals + "f", infoInstance.irisColour[2]));
            numberInput2.setResponder(responder).setEnterResponder(responder);
            numberInput2.setWidth(50);
            numberInput2.constraints().left(numberInput1, Constraint.Property.Type.RIGHT, 5);
            elements.add(numberInput2);
            last = text;

            text = new ElementTextWrapper(this);
            text.setNoWrap().setText(I18n.format("tabula.export.headInfo.pupilColour"));
            text.constraints().left(last, Constraint.Property.Type.LEFT, 0).top(last, Constraint.Property.Type.BOTTOM, spacing);
            elements.add(text);

            numberInput = new ElementNumberInput(this, true);
            numberInput.setTooltip(I18n.format("tabula.export.headInfo.pupilColour.tooltip")).setId("pupilColour0");
            numberInput.setMin(0).setMax(1).setMaxDec(Tabula.configClient.guiMaxDecimals).setDefaultText(String.format(Locale.ENGLISH, "%." + Tabula.configClient.guiMaxDecimals + "f", infoInstance.pupilColour[0]));
            numberInput.setResponder(responder).setEnterResponder(responder);
            numberInput.setWidth(50);
            numberInput.constraints().left(text, Constraint.Property.Type.RIGHT, 5).top(text, Constraint.Property.Type.TOP, 2);
            elements.add(numberInput);

            numberInput1 = new ElementNumberInput(this, true);
            numberInput1.setTooltip(I18n.format("tabula.export.headInfo.pupilColour.tooltip")).setId("pupilColour1");
            numberInput1.setMin(0).setMax(1).setMaxDec(Tabula.configClient.guiMaxDecimals).setDefaultText(String.format(Locale.ENGLISH, "%." + Tabula.configClient.guiMaxDecimals + "f", infoInstance.pupilColour[1]));
            numberInput1.setResponder(responder).setEnterResponder(responder);
            numberInput1.setWidth(50);
            numberInput1.constraints().left(numberInput, Constraint.Property.Type.RIGHT, 5);
            elements.add(numberInput1);

            numberInput2 = new ElementNumberInput(this, true);
            numberInput2.setTooltip(I18n.format("tabula.export.headInfo.pupilColour.tooltip")).setId("pupilColour2");
            numberInput2.setMin(0).setMax(1).setMaxDec(Tabula.configClient.guiMaxDecimals).setDefaultText(String.format(Locale.ENGLISH, "%." + Tabula.configClient.guiMaxDecimals + "f", infoInstance.pupilColour[2]));
            numberInput2.setResponder(responder).setEnterResponder(responder);
            numberInput2.setWidth(50);
            numberInput2.constraints().left(numberInput1, Constraint.Property.Type.RIGHT, 5);
            elements.add(numberInput2);
            last = text;

            text = new ElementTextWrapper(this);
            text.setNoWrap().setText(I18n.format("tabula.export.headInfo.halfInterpupillaryDistance"));
            text.constraints().left(last, Constraint.Property.Type.LEFT, 0).top(last, Constraint.Property.Type.BOTTOM, spacing);
            elements.add(text);

            numberInput = new ElementNumberInput(this, true);
            numberInput.setTooltip(I18n.format("tabula.export.headInfo.halfInterpupillaryDistance.tooltip")).setId("halfInterpupillaryDistance");
            numberInput.setMin(0).setMaxDec(Tabula.configClient.guiMaxDecimals).setDefaultText(String.format(Locale.ENGLISH, "%." + Tabula.configClient.guiMaxDecimals + "f", infoInstance.halfInterpupillaryDistance * 16F));
            numberInput.setResponder(responder).setEnterResponder(responder);
            numberInput.setWidth(50);
            numberInput.constraints().left(text, Constraint.Property.Type.RIGHT, 5).top(text, Constraint.Property.Type.TOP, 2);
            elements.add(numberInput);
            last = text;

            text = new ElementTextWrapper(this);
            text.setNoWrap().setText(I18n.format("tabula.export.headInfo.eyeScale"));
            text.constraints().left(last, Constraint.Property.Type.LEFT, 0).top(last, Constraint.Property.Type.BOTTOM, spacing);
            elements.add(text);

            numberInput = new ElementNumberInput(this, true);
            numberInput.setTooltip(I18n.format("tabula.export.headInfo.eyeScale.tooltip")).setId("eyeScale");
            numberInput.setMin(0).setMaxDec(Tabula.configClient.guiMaxDecimals).setDefaultText(String.format(Locale.ENGLISH, "%." + Tabula.configClient.guiMaxDecimals + "f", infoInstance.eyeScale));
            numberInput.setResponder(responder).setEnterResponder(responder);
            numberInput.setWidth(50);
            numberInput.constraints().left(text, Constraint.Property.Type.RIGHT, 5).top(text, Constraint.Property.Type.TOP, 2);
            elements.add(numberInput);

            toggle = new ElementToggle<>(this, "tabula.export.headInfo.doesEyeGlow", btn -> { updateHeadInfo(); });
            toggle.setSize(80, 14).setTooltip(I18n.format("tabula.export.headInfo.doesEyeGlow.tooltip")).setId("doesEyeGlow");
            toggle.setToggled(infoInstance.doesEyeGlow);
            toggle.constraints().left(numberInput, Constraint.Property.Type.RIGHT, 5).top(last, Constraint.Property.Type.BOTTOM, spacing);
            elements.add(toggle);
            last = text;

            toggle = new ElementToggle<>(this, "tabula.export.headInfo.sideEyed", btn -> { updateHeadInfo(); });
            toggle.setSize(80, 14).setTooltip(I18n.format("tabula.export.headInfo.sideEyed.tooltip")).setId("sideEyed");
            toggle.setToggled(infoInstance.sideEyed);
            toggle.constraints().left(last, Constraint.Property.Type.LEFT, 0).top(last, Constraint.Property.Type.BOTTOM, spacing);
            elements.add(toggle);

            toggle1 = new ElementToggle<>(this, "tabula.export.headInfo.topEyed", btn -> { updateHeadInfo(); });
            toggle1.setSize(80, 14).setTooltip(I18n.format("tabula.export.headInfo.topEyed.tooltip")).setId("topEyed");
            toggle1.setToggled(infoInstance.topEyed);
            toggle1.constraints().left(toggle, Constraint.Property.Type.RIGHT, 8).top(last, Constraint.Property.Type.BOTTOM, spacing);
            elements.add(toggle1);
            last = toggle;

            text = new ElementTextWrapper(this);
            text.setNoWrap().setText(I18n.format("tabula.export.headInfo.eyeCount"));
            text.constraints().left(last, Constraint.Property.Type.LEFT, 0).top(last, Constraint.Property.Type.BOTTOM, spacing);
            elements.add(text);

            numberInput = new ElementNumberInput(this, false);
            numberInput.setTooltip(I18n.format("tabula.export.headInfo.eyeCount.tooltip")).setId("eyeCount");
            numberInput.setMin(0).setMaxDec(0).setDefaultText(Integer.toString(infoInstance.eyeCount));
            numberInput.setResponder(responder).setEnterResponder(responder);
            numberInput.setWidth(50);
            numberInput.constraints().left(text, Constraint.Property.Type.RIGHT, 5).top(text, Constraint.Property.Type.TOP, 2);
            elements.add(numberInput);
            last = text;

            toggle = new ElementToggle<>(this, "tabula.export.headInfo.noTopInfo", btn -> { updateHeadInfo(); });
            toggle.setSize(120, 14).setTooltip(I18n.format("tabula.export.headInfo.noTopInfo.tooltip")).setId("noTopInfo");
            toggle.setToggled(infoInstance.noTopInfo);
            toggle.constraints().left(last, Constraint.Property.Type.LEFT, 0).top(last, Constraint.Property.Type.BOTTOM, spacing);
            elements.add(toggle);
            last = toggle;

            text = new ElementTextWrapper(this);
            text.setNoWrap().setText(I18n.format("tabula.export.headInfo.headTopCenter"));
            text.constraints().left(last, Constraint.Property.Type.LEFT, 0).top(last, Constraint.Property.Type.BOTTOM, spacing);
            elements.add(text);

            numberInput = new ElementNumberInput(this, true);
            numberInput.setTooltip(I18n.format("tabula.export.headInfo.headTopCenter.tooltip")).setId("headTopCenter0");
            numberInput.setMaxDec(Tabula.configClient.guiMaxDecimals).setDefaultText(String.format(Locale.ENGLISH, "%." + Tabula.configClient.guiMaxDecimals + "f", infoInstance.headTopCenter[0] * 16F));
            numberInput.setResponder(responder).setEnterResponder(responder);
            numberInput.setWidth(50);
            numberInput.constraints().left(text, Constraint.Property.Type.RIGHT, 5).top(text, Constraint.Property.Type.TOP, 2);
            elements.add(numberInput);

            numberInput1 = new ElementNumberInput(this, true);
            numberInput1.setTooltip(I18n.format("tabula.export.headInfo.headTopCenter.tooltip")).setId("headTopCenter1");
            numberInput1.setMaxDec(Tabula.configClient.guiMaxDecimals).setDefaultText(String.format(Locale.ENGLISH, "%." + Tabula.configClient.guiMaxDecimals + "f", infoInstance.headTopCenter[1] * 16F));
            numberInput1.setResponder(responder).setEnterResponder(responder);
            numberInput1.setWidth(50);
            numberInput1.constraints().left(numberInput, Constraint.Property.Type.RIGHT, 5);
            elements.add(numberInput1);

            numberInput2 = new ElementNumberInput(this, true);
            numberInput2.setTooltip(I18n.format("tabula.export.headInfo.headTopCenter.tooltip")).setId("headTopCenter2");
            numberInput2.setMaxDec(Tabula.configClient.guiMaxDecimals).setDefaultText(String.format(Locale.ENGLISH, "%." + Tabula.configClient.guiMaxDecimals + "f", infoInstance.headTopCenter[2] * 16F));
            numberInput2.setResponder(responder).setEnterResponder(responder);
            numberInput2.setWidth(50);
            numberInput2.constraints().left(numberInput1, Constraint.Property.Type.RIGHT, 5);
            elements.add(numberInput2);
            last = text;

            text = new ElementTextWrapper(this);
            text.setNoWrap().setText(I18n.format("tabula.export.headInfo.headScale"));
            text.constraints().left(last, Constraint.Property.Type.LEFT, 0).top(last, Constraint.Property.Type.BOTTOM, spacing);
            elements.add(text);

            numberInput = new ElementNumberInput(this, true);
            numberInput.setTooltip(I18n.format("tabula.export.headInfo.headScale.tooltip")).setId("headScale");
            numberInput.setMin(0).setMaxDec(Tabula.configClient.guiMaxDecimals).setDefaultText(String.format(Locale.ENGLISH, "%." + Tabula.configClient.guiMaxDecimals + "f", infoInstance.headScale));
            numberInput.setResponder(responder).setEnterResponder(responder);
            numberInput.setWidth(50);
            numberInput.constraints().left(text, Constraint.Property.Type.RIGHT, 5).top(text, Constraint.Property.Type.TOP, 2);
            elements.add(numberInput);
            last = text;

            text = new ElementTextWrapper(this);
            text.setNoWrap().setText(I18n.format("tabula.export.headInfo.headCount"));
            text.constraints().left(last, Constraint.Property.Type.LEFT, 0).top(last, Constraint.Property.Type.BOTTOM, spacing);
            elements.add(text);

            numberInput = new ElementNumberInput(this, false);
            numberInput.setTooltip(I18n.format("tabula.export.headInfo.headCount.tooltip")).setId("headCount");
            numberInput.setMin(0).setMaxDec(0).setDefaultText(Integer.toString(infoInstance.headCount));
            numberInput.setResponder(responder).setEnterResponder(responder);
            numberInput.setWidth(50);
            numberInput.constraints().left(text, Constraint.Property.Type.RIGHT, 5).top(text, Constraint.Property.Type.TOP, 2);
            elements.add(numberInput);
            last = text;

            text = new ElementTextWrapper(this);
            text.setNoWrap().setText(I18n.format("tabula.export.headInfo.hatTiltPitchYaw"));
            text.constraints().left(last, Constraint.Property.Type.LEFT, 0).top(last, Constraint.Property.Type.BOTTOM, spacing);
            elements.add(text);

            numberInput = new ElementNumberInput(this, true);
            numberInput.setTooltip(I18n.format("tabula.export.headInfo.hatTiltPitch.tooltip")).setId("hatTiltPitch");
            numberInput.setMaxDec(Tabula.configClient.guiMaxDecimals).setDefaultText(String.format(Locale.ENGLISH, "%." + Tabula.configClient.guiMaxDecimals + "f", infoInstance.hatTiltPitch));
            numberInput.setResponder(responder).setEnterResponder(responder);
            numberInput.setWidth(50);
            numberInput.constraints().left(text, Constraint.Property.Type.RIGHT, 5).top(text, Constraint.Property.Type.TOP, 2);
            elements.add(numberInput);

            numberInput1 = new ElementNumberInput(this, true);
            numberInput1.setTooltip(I18n.format("tabula.export.headInfo.hatTiltYaw.tooltip")).setId("hatTiltYaw");
            numberInput1.setMaxDec(Tabula.configClient.guiMaxDecimals).setDefaultText(String.format(Locale.ENGLISH, "%." + Tabula.configClient.guiMaxDecimals + "f", infoInstance.hatTiltYaw));
            numberInput1.setResponder(responder).setEnterResponder(responder);
            numberInput1.setWidth(50);
            numberInput1.constraints().left(numberInput, Constraint.Property.Type.RIGHT, 5);
            elements.add(numberInput1);
            last = text;

            text = new ElementTextWrapper(this);
            text.setNoWrap().setText(I18n.format("tabula.export.headInfo.headArmorOffset"));
            text.constraints().left(last, Constraint.Property.Type.LEFT, 0).top(last, Constraint.Property.Type.BOTTOM, spacing);
            elements.add(text);

            numberInput = new ElementNumberInput(this, true);
            numberInput.setTooltip(I18n.format("tabula.export.headInfo.headArmorOffset.tooltip")).setId("headArmorOffset0");
            numberInput.setMaxDec(Tabula.configClient.guiMaxDecimals).setDefaultText(String.format(Locale.ENGLISH, "%." + Tabula.configClient.guiMaxDecimals + "f", infoInstance.headArmorOffset[0] * 16F));
            numberInput.setResponder(responder).setEnterResponder(responder);
            numberInput.setWidth(50);
            numberInput.constraints().left(text, Constraint.Property.Type.RIGHT, 5).top(text, Constraint.Property.Type.TOP, 2);
            elements.add(numberInput);

            numberInput1 = new ElementNumberInput(this, true);
            numberInput1.setTooltip(I18n.format("tabula.export.headInfo.headArmorOffset.tooltip")).setId("headArmorOffset1");
            numberInput1.setMaxDec(Tabula.configClient.guiMaxDecimals).setDefaultText(String.format(Locale.ENGLISH, "%." + Tabula.configClient.guiMaxDecimals + "f", infoInstance.headArmorOffset[1] * 16F));
            numberInput1.setResponder(responder).setEnterResponder(responder);
            numberInput1.setWidth(50);
            numberInput1.constraints().left(numberInput, Constraint.Property.Type.RIGHT, 5);
            elements.add(numberInput1);

            numberInput2 = new ElementNumberInput(this, true);
            numberInput2.setTooltip(I18n.format("tabula.export.headInfo.headArmorOffset.tooltip")).setId("headArmorOffset2");
            numberInput2.setMaxDec(Tabula.configClient.guiMaxDecimals).setDefaultText(String.format(Locale.ENGLISH, "%." + Tabula.configClient.guiMaxDecimals + "f", infoInstance.headArmorOffset[2] * 16F));
            numberInput2.setResponder(responder).setEnterResponder(responder);
            numberInput2.setWidth(50);
            numberInput2.constraints().left(numberInput1, Constraint.Property.Type.RIGHT, 5);
            elements.add(numberInput2);
            last = text;

            text = new ElementTextWrapper(this);
            text.setNoWrap().setText(I18n.format("tabula.export.headInfo.headArmorScale"));
            text.constraints().left(last, Constraint.Property.Type.LEFT, 0).top(last, Constraint.Property.Type.BOTTOM, spacing);
            elements.add(text);

            numberInput = new ElementNumberInput(this, true);
            numberInput.setTooltip(I18n.format("tabula.export.headInfo.headArmorScale.tooltip")).setId("headArmorScale");
            numberInput.setMin(0).setMaxDec(Tabula.configClient.guiMaxDecimals).setDefaultText(String.format(Locale.ENGLISH, "%." + Tabula.configClient.guiMaxDecimals + "f", infoInstance.headArmorScale));
            numberInput.setResponder(responder).setEnterResponder(responder);
            numberInput.setWidth(50);
            numberInput.constraints().left(text, Constraint.Property.Type.RIGHT, 5).top(text, Constraint.Property.Type.TOP, 2);
            elements.add(numberInput);
            last = text;


            ElementButton<?> button = new ElementButton<>(this, "gui.cancel", elementClickable ->
            {
                getWorkspace().removeWindow(parent);
            });
            button.setSize(60, 20);
            button.setConstraint(new Constraint(button).bottom(this, Constraint.Property.Type.BOTTOM, 10).right(this, Constraint.Property.Type.RIGHT, 10));
            elements.add(button);

            ElementButton<?> button1 = new ElementButton<>(this, "gui.ok", btn -> {
                getWorkspace().removeWindow(parent);

                export(project);
            });
            button1.setSize(60, 20);
            button1.setConstraint(new Constraint(button1).right(button, Constraint.Property.Type.LEFT, 10));
            elements.add(button1);

            ElementToggle<?> previewToggle = new ElementToggle<>(this, "tabula.export.headInfo.preview", btn -> {
                if(btn.toggleState)
                {
                    parent.parent.headInfoRender = infoInstance;
                }
                else
                {
                    parent.parent.headInfoRender = null;
                }
            });
            previewToggle.setSize(60, 20);
            previewToggle.setToggled(true);
            previewToggle.constraints().left(this, Constraint.Property.Type.LEFT, 10).bottom(button, Constraint.Property.Type.BOTTOM, 0);
            elements.add(previewToggle);

            parent.parent.headInfoRender = infoInstance;
        }

        @Override
        public void onClose()
        {
            super.onClose();

            parentFragment.parent.headInfoRender = null;
        }

        public void updateHeadInfo()
        {
            infoInstance.forClass = ((ElementTextField)getById("forClass")).getText();
            infoInstance.modelFieldName = ((ElementTextField)getById("modelFieldName")).getText();
            infoInstance.isBoss = ((ElementToggle)getById("isBoss")).toggleState;
            infoInstance.affectedByInvisibility = ((ElementToggle)getById("affectedByInvisibility")).toggleState;
            infoInstance.noFaceInfo = ((ElementToggle)getById("noFaceInfo")).toggleState;
            infoInstance.eyeOffset[0] = Float.parseFloat(String.format(Locale.ENGLISH, "%.7f", ((ElementNumberInput)getById("eyeOffset0")).getDouble() / 16D));
            infoInstance.eyeOffset[1] = Float.parseFloat(String.format(Locale.ENGLISH, "%.7f", ((ElementNumberInput)getById("eyeOffset1")).getDouble() / 16D));
            infoInstance.eyeOffset[2] = Float.parseFloat(String.format(Locale.ENGLISH, "%.7f", ((ElementNumberInput)getById("eyeOffset2")).getDouble() / 16D));
            infoInstance.irisColour[0] = Float.parseFloat(String.format(Locale.ENGLISH, "%.7f", ((ElementNumberInput)getById("irisColour0")).getDouble()));
            infoInstance.irisColour[1] = Float.parseFloat(String.format(Locale.ENGLISH, "%.7f", ((ElementNumberInput)getById("irisColour1")).getDouble()));
            infoInstance.irisColour[2] = Float.parseFloat(String.format(Locale.ENGLISH, "%.7f", ((ElementNumberInput)getById("irisColour2")).getDouble()));
            infoInstance.pupilColour[0] = Float.parseFloat(String.format(Locale.ENGLISH, "%.7f", ((ElementNumberInput)getById("pupilColour0")).getDouble()));
            infoInstance.pupilColour[1] = Float.parseFloat(String.format(Locale.ENGLISH, "%.7f", ((ElementNumberInput)getById("pupilColour1")).getDouble()));
            infoInstance.pupilColour[2] = Float.parseFloat(String.format(Locale.ENGLISH, "%.7f", ((ElementNumberInput)getById("pupilColour2")).getDouble()));
            infoInstance.halfInterpupillaryDistance = Float.parseFloat(String.format(Locale.ENGLISH, "%.7f", ((ElementNumberInput)getById("halfInterpupillaryDistance")).getDouble() / 16D));
            infoInstance.eyeScale = Float.parseFloat(String.format(Locale.ENGLISH, "%.7f", ((ElementNumberInput)getById("eyeScale")).getDouble()));
            infoInstance.sideEyed = ((ElementToggle)getById("sideEyed")).toggleState;
            infoInstance.topEyed = ((ElementToggle)getById("topEyed")).toggleState;
            infoInstance.eyeCount = ((ElementNumberInput)getById("eyeCount")).getInt();
            infoInstance.doesEyeGlow = ((ElementToggle)getById("doesEyeGlow")).toggleState;

            infoInstance.noTopInfo = ((ElementToggle)getById("noTopInfo")).toggleState;
            infoInstance.headTopCenter[0] = Float.parseFloat(String.format(Locale.ENGLISH, "%.7f", ((ElementNumberInput)getById("headTopCenter0")).getDouble() / 16D));
            infoInstance.headTopCenter[1] = Float.parseFloat(String.format(Locale.ENGLISH, "%.7f", ((ElementNumberInput)getById("headTopCenter1")).getDouble() / 16D));
            infoInstance.headTopCenter[2] = Float.parseFloat(String.format(Locale.ENGLISH, "%.7f", ((ElementNumberInput)getById("headTopCenter2")).getDouble() / 16D));
            infoInstance.headScale = Float.parseFloat(String.format(Locale.ENGLISH, "%.7f", ((ElementNumberInput)getById("headScale")).getDouble()));
            infoInstance.headCount = ((ElementNumberInput)getById("headCount")).getInt();
            infoInstance.hatTiltPitch = Float.parseFloat(String.format(Locale.ENGLISH, "%.7f", ((ElementNumberInput)getById("hatTiltPitch")).getDouble() / 16D));
            infoInstance.hatTiltYaw = Float.parseFloat(String.format(Locale.ENGLISH, "%.7f", ((ElementNumberInput)getById("hatTiltYaw")).getDouble() / 16D));
            infoInstance.headArmorOffset[0] = Float.parseFloat(String.format(Locale.ENGLISH, "%.7f", ((ElementNumberInput)getById("headArmorOffset0")).getDouble() / 16D));
            infoInstance.headArmorOffset[1] = Float.parseFloat(String.format(Locale.ENGLISH, "%.7f", ((ElementNumberInput)getById("headArmorOffset1")).getDouble() / 16D));
            infoInstance.headArmorOffset[2] = Float.parseFloat(String.format(Locale.ENGLISH, "%.7f", ((ElementNumberInput)getById("headArmorOffset2")).getDouble() / 16D));
            infoInstance.headArmorScale = Float.parseFloat(String.format(Locale.ENGLISH, "%.7f", ((ElementNumberInput)getById("headArmorScale")).getDouble()));
        }

        public void export(Project project)
        {
            if(!ExportList.EXPORTERS.get("headInfo").export(project, infoInstance))
            {
                WindowPopup.popup(parentFragment.parent, 0.4D, 140, null, I18n.format("export.failed"));
            }
        }
    }
}
