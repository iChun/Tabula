package me.ichun.mods.tabula.client.gui.window;

import me.ichun.mods.ichunutil.client.core.EventHandlerClient;
import me.ichun.mods.ichunutil.client.gui.bns.window.Window;
import me.ichun.mods.ichunutil.client.gui.bns.window.WindowPopup;
import me.ichun.mods.ichunutil.client.gui.bns.window.constraint.Constraint;
import me.ichun.mods.ichunutil.client.gui.bns.window.view.View;
import me.ichun.mods.ichunutil.client.gui.bns.window.view.element.ElementButtonTextured;
import me.ichun.mods.ichunutil.client.gui.bns.window.view.element.ElementToggle;
import me.ichun.mods.ichunutil.common.module.tabula.project.Identifiable;
import me.ichun.mods.ichunutil.common.module.tabula.project.Project;
import me.ichun.mods.tabula.client.gui.IProjectInfo;
import me.ichun.mods.tabula.client.gui.WorkspaceTabula;
import me.ichun.mods.tabula.client.gui.window.popup.*;
import me.ichun.mods.tabula.client.tabula.Mainframe;
import me.ichun.mods.tabula.common.Tabula;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.ResourceLocation;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public class WindowToolbar extends Window<WorkspaceTabula>
        implements IProjectInfo
{
    @Nullable
    public Mainframe.ProjectInfo currentInfo = null;
    public final Mainframe mainframe;

    public final ViewToolbar toolbar;
    public WindowToolbar(WorkspaceTabula parent)
    {
        super(parent);
        mainframe = parent.mainframe;

        borderSize = () -> 0;
        disableDocking();
        disableDockStacking();
        disableUndocking();
        disableBringToFront();
        disableDrag();
        disableDragResize();
        disableTitle();

        setView(toolbar = new ViewToolbar(this));
        setId("windowToolbar");
    }

    @Override
    public void setCurrentProject(Mainframe.ProjectInfo info)
    {
        currentInfo = info;
        children().stream().filter(child -> child instanceof IProjectInfo).forEach(child -> ((IProjectInfo)child).setCurrentProject(info));
    }

    public ViewToolbar getCurrentView()
    {
        return (ViewToolbar)currentView;
    }

    public static class ViewToolbar extends View<WindowToolbar>
            implements IProjectInfo
    {
        public ViewToolbar(@Nonnull WindowToolbar parent)
        {
            super(parent, "");

            populate(parent.currentInfo);
        }

        @Override
        public void setCurrentProject(Mainframe.ProjectInfo info)
        {
            populate(info);
        }

        public void populate(Mainframe.ProjectInfo info)
        {
            elements.clear();

            ElementButtonTextured<?> last;
            ElementButtonTextured<?> btn;
            //new project
            btn = new ElementButtonTextured<>(this, new ResourceLocation("tabula", "textures/icon/new.png"), button -> openNewProject());
            btn.setSize(20,20).setTooltip(I18n.format("topdock.new"));
            btn.setConstraint(new Constraint(btn).left(this, Constraint.Property.Type.LEFT, 0));
            elements.add(last = btn);

            //open project
            btn = new ElementButtonTextured<>(this, new ResourceLocation("tabula", "textures/icon/open.png"), button -> openOpenProject());
            btn.setSize(20,20).setTooltip(I18n.format("topdock.open"));
            btn.setConstraint(new Constraint(btn).left(last, Constraint.Property.Type.RIGHT, 0));
            elements.add(last = btn);

            if(info != null)
            {
                //edit project
                btn = new ElementButtonTextured<>(this, new ResourceLocation("tabula", "textures/icon/edit.png"), button -> {
                    Mainframe.ProjectInfo info1 = parentFragment.parent.mainframe.getActiveProject();
                    if(info1 != null)
                    {
                        getWorkspace().openWindowInCenter(new WindowEditProject(getWorkspace(), info1.project), 0.6D, 0.6D);
                    }
                });
                btn.setSize(20,20).setTooltip(I18n.format("topdock.edit"));
                btn.setConstraint(new Constraint(btn).left(last, Constraint.Property.Type.RIGHT, 0));
                elements.add(last = btn);

                //save project
                btn = new ElementButtonTextured<>(this, new ResourceLocation("tabula", "textures/icon/save.png"), button -> saveProject());
                btn.setSize(20,20).setTooltip(I18n.format("topdock.save"));
                btn.setConstraint(new Constraint(btn).left(last, Constraint.Property.Type.RIGHT, 0));
                elements.add(last = btn);

                //saveAs project
                btn = new ElementButtonTextured<>(this, new ResourceLocation("tabula", "textures/icon/saveas.png"), button -> saveAsProject());
                btn.setSize(20,20).setTooltip(I18n.format("topdock.saveAs"));
                btn.setConstraint(new Constraint(btn).left(last, Constraint.Property.Type.RIGHT, 0));
                elements.add(last = btn);

                //import to project
                btn = new ElementButtonTextured<>(this, new ResourceLocation("tabula", "textures/icon/import.png"), button -> openImportProject());
                btn.setSize(20,20).setTooltip(I18n.format("topdock.import"));
                btn.setConstraint(new Constraint(btn).left(last, Constraint.Property.Type.RIGHT, 0));
                elements.add(last = btn);

                //importMC to project
                btn = new ElementButtonTextured<>(this, new ResourceLocation("tabula", "textures/icon/importmc.png"), button -> openImportMCProject());
                btn.setSize(20,20).setTooltip(I18n.format("topdock.importMC"));
                btn.setConstraint(new Constraint(btn).left(last, Constraint.Property.Type.RIGHT, 0));
                elements.add(last = btn);

                //export project
                btn = new ElementButtonTextured<>(this, new ResourceLocation("tabula", "textures/icon/export.png"), button -> openExportProject());
                btn.setSize(20,20).setTooltip(I18n.format("topdock.export"));
                btn.setConstraint(new Constraint(btn).left(last, Constraint.Property.Type.RIGHT, 0));
                elements.add(last = btn);

                //ghostModel
                btn = new ElementButtonTextured<>(this, new ResourceLocation("tabula", "textures/icon/ghostmodel.png"), button -> openGhostModel());
                btn.setSize(20,20).setTooltip(I18n.format("topdock.ghostModel"));
                btn.setConstraint(new Constraint(btn).left(last, Constraint.Property.Type.RIGHT, 0));
                elements.add(last = btn);

                //cut
                btn = new ElementButtonTextured<>(this, new ResourceLocation("tabula", "textures/icon/cut.png"), button -> cut());
                btn.setSize(20,20).setTooltip(I18n.format("topdock.cut"));
                btn.setConstraint(new Constraint(btn).left(last, Constraint.Property.Type.RIGHT, 0));
                elements.add(last = btn);

                //copy
                btn = new ElementButtonTextured<>(this, new ResourceLocation("tabula", "textures/icon/copy.png"), button -> copy());
                btn.setSize(20,20).setTooltip(I18n.format("topdock.copy"));
                btn.setConstraint(new Constraint(btn).left(last, Constraint.Property.Type.RIGHT, 0));
                elements.add(last = btn);

                //paste
                btn = new ElementButtonTextured<>(this, new ResourceLocation("tabula", "textures/icon/paste.png"), button -> paste(false, false));
                btn.setSize(20,20).setTooltip(I18n.format("topdock.paste"));
                btn.setConstraint(new Constraint(btn).left(last, Constraint.Property.Type.RIGHT, 0));
                elements.add(last = btn);

                //pasteinplace
                btn = new ElementButtonTextured<>(this, new ResourceLocation("tabula", "textures/icon/pasteinplace.png"), button -> paste(false, true));
                btn.setSize(20,20).setTooltip(I18n.format("topdock.pasteInPlace"));
                btn.setConstraint(new Constraint(btn).left(last, Constraint.Property.Type.RIGHT, 0));
                elements.add(last = btn);

                //pasteWithoutChildren
                btn = new ElementButtonTextured<>(this, new ResourceLocation("tabula", "textures/icon/pastewithoutchildren.png"), button -> paste(true, false));
                btn.setSize(20,20).setTooltip(I18n.format("topdock.pasteWithoutChildren"));
                btn.setConstraint(new Constraint(btn).left(last, Constraint.Property.Type.RIGHT, 0));
                elements.add(last = btn);

                //undo
                btn = new ElementButtonTextured<>(this, new ResourceLocation("tabula", "textures/icon/undo.png"), button -> undo());
                btn.setSize(20,20).setTooltip(I18n.format("topdock.undo"));
                btn.setConstraint(new Constraint(btn).left(last, Constraint.Property.Type.RIGHT, 0));
                elements.add(last = btn);

                //redo
                btn = new ElementButtonTextured<>(this, new ResourceLocation("tabula", "textures/icon/redo.png"), button -> redo());
                btn.setSize(20,20).setTooltip(I18n.format("topdock.redo"));
                btn.setConstraint(new Constraint(btn).left(last, Constraint.Property.Type.RIGHT, 0));
                elements.add(last = btn);
            }

            if(parentFragment.parent.mainframe.origin != null) // it's on a server
            {
                //chat
                btn = new ElementButtonTextured<>(this, new ResourceLocation("tabula", "textures/icon/chat.png"), button -> {
                    WindowChat chat = parentFragment.parent.getByWindowType(WindowChat.class);
                    if(chat != null)
                    {
                        if(!chat.parent.isDocked(chat) && chat.getRight() < 0) //we're not docked, we're off screen
                        {
                            chat.parent.addToDock(chat, Constraint.Property.Type.BOTTOM);
                            chat.setHeight(95);
                            chat.constraint.apply();
                            chat.resize(Minecraft.getInstance(), chat.getParentWidth(), chat.getParentHeight());
                            chat.updateChat();
                        }
                        else //hide chat?
                        {
                            if(chat.parent.isDocked(chat))
                            {
                                chat.parent.removeFromDock(chat);
                            }

                            chat.setPosX(-10000);
                        }
                    }
                });
                btn.setSize(20, 20).setTooltip(I18n.format("topdock.chat"));
                btn.setConstraint(new Constraint(btn).left(last, Constraint.Property.Type.RIGHT, 0));
                elements.add(last = btn);
            }


            //settings
            btn = new ElementButtonTextured<>(this, new ResourceLocation("tabula", "textures/icon/settings.png"), button -> {
                for(Mainframe.ProjectInfo project : parentFragment.mainframe.projects)
                {
                    if(project.project.isDirty)
                    {
                        WindowPopup.popup(parentFragment.parent, 0.4D, 0.4D, w->{}, I18n.format("tabula.warning.unsavedProjects"));
                        return;
                    }
                }

                parentFragment.parent.getMinecraft().displayGuiScreen(EventHandlerClient.getConfigGui(parentFragment.parent.getMinecraft(), parentFragment.parent));
            });
            btn.setSize(20,20).setTooltip(I18n.format("topdock.settings"));
            btn.setConstraint(new Constraint(btn).left(last, Constraint.Property.Type.RIGHT, 0));
            elements.add(last = btn);

            //info
            btn = new ElementButtonTextured<>(this, new ResourceLocation("tabula", "textures/icon/info.png"), button -> {
                String[] creds = new String[]{
                        Tabula.VERSION, "iChun", "mr_hazard", "heldplayer, Vswe, bombmask, FraserKillip", "Kihira, Dizkonnekted, Dunkleosteus, Zorn_Taov, OndraSter, K-4U, Horfius, GlitchPulse"
                };
                List<String> lines = new ArrayList<>();

                lines.add(I18n.format("window.about.powered"));
                for(int i = 0; i <= 6; i++)
                {
                    String text = I18n.format("window.about.line" + i);
                    if(i < creds.length)
                    {
                        if(text.endsWith(" "))
                        {
                            text = text + creds[i];
                        }
                        else
                        {
                            text = text + " " + creds[i];
                        }
                    }
                    lines.add(text);
                }
                lines.add("");
                lines.add(I18n.format("window.about.os1"));
                lines.add(I18n.format("window.about.os2"));
                lines.add("https://github.com/iChun/Tabula");

                WindowPopup.popup(parentFragment.parent, 0.6D, 0.6D, "window.about.title", workspace -> {}, lines.toArray(new String[lines.size()]));

            });
            btn.setSize(20,20).setTooltip(I18n.format("topdock.info"));
            btn.setConstraint(new Constraint(btn).left(last, Constraint.Property.Type.RIGHT, 0));
            elements.add(last = btn);


            //Add exit button. last button
            btn = new ElementButtonTextured<>(this, new ResourceLocation("tabula", "textures/icon/exittabula.png"), button -> getWorkspace().onClose());
            btn.setSize(20,20).setTooltip(I18n.format("topdock.exitTabula"));
            btn.setConstraint(new Constraint(btn).right(this, Constraint.Property.Type.RIGHT, 0));
            elements.add(btn);

            ElementToggle<?> toggle = new ElementToggle<>(this, "B", elementClickable -> {}).setToggled(Tabula.configClient.renderWorkspaceBlock);
            toggle.setSize(20,20).setTooltip(I18n.format("topdock.woodFull"));
            toggle.setId("buttonBlockToggle");
            toggle.setConstraint(new Constraint(toggle).right(btn, Constraint.Property.Type.LEFT, 0));
            elements.add(toggle);

            ElementToggle<?> toggle1 = new ElementToggle<>(this, "G", elementClickable -> {}).setToggled(Tabula.configClient.renderWorkspaceGrid);
            toggle1.setSize(20,20).setTooltip(I18n.format("tabula.config.prop.renderGrid.name"));
            toggle1.setId("buttonGridToggle");
            toggle1.setConstraint(new Constraint(toggle1).right(toggle, Constraint.Property.Type.LEFT, 0));
            elements.add(toggle1);

            init();
        }

        public void openSaveAsWindow(Mainframe.ProjectInfo info)
        {
            getWorkspace().openWindowInCenter(new WindowSaveAs(parentFragment.parent, info.project), 0.4D, 0.4D);
        }

        public void openNewProject()
        {
            getWorkspace().openWindowInCenter(new WindowNewProject(getWorkspace()), 0.6D, 0.6D);
        }

        public void openOpenProject()
        {
            Window<?> window = new WindowOpenProject(getWorkspace());
            getWorkspace().openWindowInCenter(window, 0.4D, 0.6D);
            window.init();
        }

        public void saveProject()
        {
            Mainframe.ProjectInfo info1 = parentFragment.parent.mainframe.getActiveProject();
            if(info1 != null)
            {
                if(info1.project.saveFile == null) //we've not saved before. no savefile
                {
                    openSaveAsWindow(info1);
                }
                else if(!info1.project.save(info1.project.saveFile))
                {
                    WindowPopup.popup(parentFragment.parent, 0.4D, 0.3D, null, I18n.format("window.saveAs.failed"));
                }
            }
        }

        public void saveAsProject()
        {
            Mainframe.ProjectInfo info1 = parentFragment.parent.mainframe.getActiveProject();
            if(info1 != null)
            {
                openSaveAsWindow(info1);
            }
        }

        public void openImportProject()
        {
            Mainframe.ProjectInfo info1 = parentFragment.parent.mainframe.getActiveProject();
            if(info1 != null)
            {
                Window<?> window = new WindowImportProject(getWorkspace());
                getWorkspace().openWindowInCenter(window, 0.4D, 0.6D);
                window.init();
            }
        }

        public void openImportMCProject()
        {
            Mainframe.ProjectInfo info1 = parentFragment.parent.mainframe.getActiveProject();
            if(info1 != null)
            {
                Window<?> window = new WindowImportMCProject(getWorkspace());
                getWorkspace().openWindowInCenter(window, 0.4D, 0.8D);
                window.init();
            }
        }

        public void openExportProject()
        {
            Mainframe.ProjectInfo info1 = parentFragment.parent.mainframe.getActiveProject();
            if(info1 != null)
            {
                WindowExport window = new WindowExport(getWorkspace(), info1);
                getWorkspace().openWindowInCenter(window, 0.6D, 0.6D);
                window.init();
            }
        }

        public void openGhostModel()
        {
            Mainframe.ProjectInfo info1 = parentFragment.parent.mainframe.getActiveProject();
            if(info1 != null)
            {
                Window<?> window = new WindowGhostProject(getWorkspace());
                getWorkspace().openWindowInCenter(window, 0.4D, 0.6D);
                window.init();
            }
        }

        public void undo()
        {
            Mainframe.ProjectInfo info1 = parentFragment.parent.mainframe.getActiveProject();
            if(info1 != null)
            {
                parentFragment.parent.mainframe.changeState(info1, false);
            }
        }

        public void redo()
        {
            Mainframe.ProjectInfo info1 = parentFragment.parent.mainframe.getActiveProject();
            if(info1 != null)
            {
                parentFragment.parent.mainframe.changeState(info1, true);
            }
        }

        public void cut()
        {
            Mainframe.ProjectInfo info = parentFragment.parent.mainframe.getActiveProject();
            if(info != null)
            {
                Identifiable<?> selected = info.getSelectedPart() != null ? info.getSelectedPart() : info.getSelectedBox();
                if(selected != null)
                {
                    info.createState();
                    info.delete(selected);
                    selected.parent = null; //orphan this child
                    selected.witnessProtectionProgramme(); //and give it a new identity
                    parentFragment.parent.clipboard = selected;
                    info.createState();
                }
            }
        }

        public void copy()
        {
            Mainframe.ProjectInfo info = parentFragment.parent.mainframe.getActiveProject();
            if(info != null)
            {
                Identifiable<?> selected = info.getSelectedPart() != null ? info.getSelectedPart() : info.getSelectedBox();
                if(selected != null)
                {
                    Identifiable<?> clone = (Identifiable<?>)selected.clone();
                    clone.witnessProtectionProgramme();
                    parentFragment.parent.clipboard = clone;
                }
            }
        }

        public void paste(boolean sterile, boolean inPlace)
        {
            Mainframe.ProjectInfo info = parentFragment.parent.mainframe.getActiveProject();
            if(info != null)
            {
                Identifiable<?> clone = (Identifiable<?>)parentFragment.parent.clipboard.clone();
                clone.witnessProtectionProgramme();
                if(clone instanceof Project.Part)
                {
                    if(!inPlace)
                    {
                        ((Project.Part)clone).resetPositions();
                    }
                    if(sterile)
                    {
                        ((Project.Part)clone).children.clear();
                    }
                    info.addPart(info.getSelectedPart() != null ? info.getSelectedPart() : info.getSelectedBox() != null ? info.getSelectedBox() : info.project, (Project.Part)clone);
                }
                else if(clone instanceof Project.Part.Box)
                {
                    info.addBox(info.getSelectedPart() != null ? info.getSelectedPart() : info.getSelectedBox() != null ? info.getSelectedBox() : info.project, (Project.Part.Box)clone);
                }
            }
        }
    }

    @Override
    public int getMinHeight()
    {
        return 20;
    }

    @Override
    public int getMaxHeight()
    {
        return 20;
    }
}
