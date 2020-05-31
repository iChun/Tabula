package me.ichun.mods.tabula.client.gui.window.popup;

import com.google.common.collect.Ordering;
import me.ichun.mods.ichunutil.client.gui.bns.window.Window;
import me.ichun.mods.ichunutil.client.gui.bns.window.constraint.Constraint;
import me.ichun.mods.ichunutil.client.gui.bns.window.view.View;
import me.ichun.mods.ichunutil.client.gui.bns.window.view.element.*;
import me.ichun.mods.ichunutil.common.util.IOUtil;
import me.ichun.mods.tabula.client.core.ResourceHelper;
import me.ichun.mods.tabula.client.gui.IProjectInfo;
import me.ichun.mods.tabula.client.gui.WorkspaceTabula;
import me.ichun.mods.tabula.client.tabula.Mainframe;
import net.minecraft.client.renderer.texture.NativeImage;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Util;

import javax.annotation.Nonnull;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.TreeSet;

public class WindowNewTexture extends Window<WorkspaceTabula>
{
    public Mainframe.ProjectInfo projectInfo;

    public WindowNewTexture(WorkspaceTabula parent, Mainframe.ProjectInfo projectInfo)
    {
        super(parent);
        this.projectInfo = projectInfo;

        setView(new ViewNewTexture(this));
        disableDockingEntirely();
    }

    public static class ViewNewTexture extends View<WindowNewTexture>
    {
        public ViewNewTexture(@Nonnull WindowNewTexture parent)
        {
            super(parent, "window.loadTexture.title");

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

            TreeSet<File> files = new TreeSet<>(Ordering.natural());
            File[] textures = ResourceHelper.getTexturesDir().toFile().listFiles();
            if(textures != null)
            {
                for(File file : textures)
                {
                    if(!file.isDirectory() && file.getName().endsWith(".png"))
                    {
                        files.add(file);
                    }
                }
            }

            for(File file : files)
            {
                list.addItem(file).setDefaultAppearance().setDoubleClickHandler(item -> {
                    if(item.selected)
                    {
                        loadFile(item.getObject());
                    }
                });
            }

            ElementButtonTextured<?> openDir = new ElementButtonTextured<>(this, new ResourceLocation("tabula", "textures/icon/info.png"), btn -> {
                Util.getOSType().openFile(ResourceHelper.getTexturesDir().toFile());
            });
            openDir.setTooltip(I18n.format("topdock.openWorkingDir")).setSize(20, 20);
            openDir.setConstraint(new Constraint(openDir).bottom(this, Constraint.Property.Type.BOTTOM, 10).left(this, Constraint.Property.Type.LEFT, 10));
            elements.add(openDir);

            ElementToggle<?> toggle = new ElementToggle<>(this, "window.loadTexture.updateTextureDimensions", btn -> {});
            toggle.setToggled(true).setTooltip(I18n.format("window.loadTexture.updateTextureDimensionsFull")).setSize(60, 20).setId("buttonTexture");
            toggle.setConstraint(new Constraint(toggle).bottom(this, Constraint.Property.Type.BOTTOM, 10).left(openDir, Constraint.Property.Type.RIGHT, 10));
            elements.add(toggle);

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
                        loadFile((File)item.getObject());
                        return;
                    }
                }
            });
            button1.setSize(60, 20);
            button1.setConstraint(new Constraint(button1).right(button, Constraint.Property.Type.LEFT, 10));
            elements.add(button1);
        }

        public void loadFile(File file)
        {
            parentFragment.parent.removeWindow(parentFragment);

            Mainframe.ProjectInfo info = parentFragment.parent.mainframe.getActiveProject();
            if(info != null)
            {
                parentFragment.projectInfo.textureFile = file;
                parentFragment.projectInfo.project.textureFile = file.getName();
                parentFragment.projectInfo.project.textureFileMd5 = IOUtil.getMD5Checksum(file);

                byte[] image = null;
                try (NativeImage img = NativeImage.read(new FileInputStream(file)))
                {
                    image = img.getBytes();

                    if(!(parentFragment.projectInfo.project.texWidth == img.getWidth() && parentFragment.projectInfo.project.texHeight == img.getHeight()) && ((ElementToggle)getById("buttonTexture")).toggleState)
                    {
                        parentFragment.projectInfo.project.texWidth = img.getWidth();
                        parentFragment.projectInfo.project.texHeight = img.getHeight();
                        parentFragment.parent.projectChanged(IProjectInfo.ChangeType.PROJECT);
                    }
                }
                catch(IOException ignored){}

                parentFragment.parent.mainframe.setImage(info, image, true);
            }
        }
    }
}
