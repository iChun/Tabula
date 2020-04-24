package me.ichun.mods.tabula.client.gui.window.popup;

import me.ichun.mods.ichunutil.client.gui.bns.window.Window;
import me.ichun.mods.ichunutil.client.gui.bns.window.WindowPopup;
import me.ichun.mods.ichunutil.client.gui.bns.window.constraint.Constraint;
import me.ichun.mods.ichunutil.client.gui.bns.window.view.View;
import me.ichun.mods.ichunutil.client.gui.bns.window.view.element.ElementButton;
import me.ichun.mods.ichunutil.client.gui.bns.window.view.element.ElementButtonTextured;
import me.ichun.mods.ichunutil.client.gui.bns.window.view.element.ElementList;
import me.ichun.mods.ichunutil.client.gui.bns.window.view.element.ElementScrollBar;
import me.ichun.mods.ichunutil.common.module.tabula.formats.types.Exporter;
import me.ichun.mods.tabula.client.core.ResourceHelper;
import me.ichun.mods.tabula.client.export.ExportList;
import me.ichun.mods.tabula.client.gui.WorkspaceTabula;
import me.ichun.mods.tabula.client.tabula.Mainframe;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Util;

import javax.annotation.Nonnull;

public class WindowExport extends Window<WorkspaceTabula>
{
    public WindowExport(WorkspaceTabula parent, Mainframe.ProjectInfo info)
    {
        super(parent);

        setView(new ViewExport(this, info));
        disableDockingEntirely();
    }

    public static class ViewExport extends View<WindowExport>
    {
        public ViewExport(@Nonnull WindowExport parent, Mainframe.ProjectInfo info)
        {
            super(parent, "export.title");

            ElementScrollBar<?> sv = new ElementScrollBar<>(this, ElementScrollBar.Orientation.VERTICAL, 0.6F);
            sv.setConstraint(new Constraint(sv).top(this, Constraint.Property.Type.TOP, 0)
                    .bottom(this, Constraint.Property.Type.BOTTOM, 40) // 10 + 20 + 10, bottom + button height + padding
                    .right(this, Constraint.Property.Type.RIGHT, 0)
            );
            elements.add(sv);

            ElementList<?> list = new ElementList<>(this).setScrollVertical(sv);
            list.setConstraint(new Constraint(list).bottom(this, Constraint.Property.Type.BOTTOM, 40)
                    .left(this, Constraint.Property.Type.LEFT, 0).right(sv, Constraint.Property.Type.LEFT, 0)
                    .top(this, Constraint.Property.Type.TOP, 0));
            elements.add(list);

            ExportList.EXPORTERS.forEach((s, e) -> {
                list.addItem(e).addTextWrapper(e.name).setDoubleClickHandler(item -> {
                    if(item.selected)
                    {
                        loadFile(item.getObject(), info);
                    }
                });
            });

            ElementButtonTextured<?> openDir = new ElementButtonTextured<>(this, new ResourceLocation("tabula", "textures/icon/info.png"), btn -> {
                Util.getOSType().openFile(ResourceHelper.getExportsDir().toFile());
            });
            openDir.setTooltip(I18n.format("topdock.openWorkingDir")).setSize(20, 20);
            openDir.setConstraint(new Constraint(openDir).bottom(this, Constraint.Property.Type.BOTTOM, 10).left(this, Constraint.Property.Type.LEFT, 10));
            elements.add(openDir);

            ElementButton<?> button = new ElementButton<>(this, I18n.format("gui.cancel"), btn ->
            {
                getWorkspace().removeWindow(parent);
            });
            button.setSize(60, 20);
            button.setConstraint(new Constraint(button).bottom(this, Constraint.Property.Type.BOTTOM, 10).right(this, Constraint.Property.Type.RIGHT, 10));
            elements.add(button);

            ElementButton<?> button1 = new ElementButton<>(this, I18n.format("gui.ok"), btn ->
            {
                for(ElementList.Item<?> item : list.items)
                {
                    if(item.selected)
                    {
                        loadFile((Exporter)item.getObject(), info);
                        return;
                    }
                }
            });
            button1.setSize(60, 20);
            button1.setConstraint(new Constraint(button1).right(button, Constraint.Property.Type.LEFT, 10));
            elements.add(button1);
        }

        public void loadFile(Exporter exporter, Mainframe.ProjectInfo info)
        {
            parentFragment.parent.removeWindow(parentFragment);

            if(info != null && !exporter.override(parentFragment.parent, info.project) && !exporter.export(info.project))
            {
                WindowPopup.popup(parentFragment.parent, 0.4D, 140, w -> {}, I18n.format("export.failed"));
            }
        }
    }
}
