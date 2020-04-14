package me.ichun.mods.tabula.client.gui.window;

import me.ichun.mods.ichunutil.client.gui.bns.window.Fragment;
import me.ichun.mods.ichunutil.client.gui.bns.window.Window;
import me.ichun.mods.ichunutil.client.gui.bns.window.constraint.Constraint;
import me.ichun.mods.ichunutil.client.gui.bns.window.view.View;
import me.ichun.mods.ichunutil.client.gui.bns.window.view.element.Element;
import me.ichun.mods.ichunutil.client.gui.bns.window.view.element.ElementButtonTextured;
import me.ichun.mods.ichunutil.client.gui.bns.window.view.element.ElementTextWrapper;
import me.ichun.mods.ichunutil.client.gui.bns.window.view.element.ElementToggle;
import me.ichun.mods.tabula.client.gui.IProjectInfo;
import me.ichun.mods.tabula.client.gui.WorkspaceTabula;
import me.ichun.mods.tabula.client.tabula.Mainframe;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.ResourceLocation;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class WindowTexture extends Window<WorkspaceTabula>
        implements IProjectInfo
{
    public @Nullable
    Mainframe.ProjectInfo currentInfo = null;

    public WindowTexture(WorkspaceTabula parent)
    {
        super(parent);

        setView(new ViewTexture(this));
        setId("windowTexture");
        size(104, 100);
    }

    @Override
    public void setCurrentProject(Mainframe.ProjectInfo info)
    {
        currentInfo = info;
        children().stream().filter(child -> child instanceof IProjectInfo).forEach(child -> ((IProjectInfo)child).setCurrentProject(info));
    }

    public static class ViewTexture extends View<WindowTexture>
    {
        public ViewTexture(@Nonnull WindowTexture parent)
        {
            super(parent, "window.texture.title");

            ElementButtonTextured<?, ?> btn = new ElementButtonTextured<>(this, new ResourceLocation("tabula", "textures/icon/cleartexture.png"), elementClickable -> {
                //TODO this
            }).setSize(20, 20).setTooltip(I18n.format("window.texture.clearTexture"));
            btn.setConstraint(new Constraint(btn).bottom(this, Constraint.Property.Type.BOTTOM, 2).right(this, Constraint.Property.Type.RIGHT, 2));
            elements.add(btn);

            ElementButtonTextured<?, ?> btn1 = new ElementButtonTextured<>(this, new ResourceLocation("tabula", "textures/icon/newtexture.png"), elementClickable -> {
                //TODO this
            }).setSize(20, 20).setTooltip(I18n.format("window.texture.loadTexture"));
            btn1.setConstraint(new Constraint(btn1).bottom(this, Constraint.Property.Type.BOTTOM, 2).right(btn, Constraint.Property.Type.LEFT, 0));
            elements.add(btn1);

            ElementToggle<?, ?> toggle = new ElementToggle<>(this, "window.texture.listenTexture", elementClickable -> {

            }).setToggled(true).setSize(60, 20).setTooltip(I18n.format("window.texture.listenTextureFull"));
            toggle.setConstraint(new Constraint(toggle).bottom(this, Constraint.Property.Type.BOTTOM, 2).right(btn1, Constraint.Property.Type.LEFT, 0).left(this, Constraint.Property.Type.LEFT, 2));
            elements.add(toggle);

            ElementTextWrapper<?> text = new ElementTextWrapper<>(this);
            text.setNoWrap().setText("akjshdaskjhd").setConstraint(new Constraint(text).left(this, Constraint.Property.Type.LEFT, 2).bottom(toggle, Constraint.Property.Type.TOP, 1));
            elements.add(text);

            ElementProjectTexture texture = new ElementProjectTexture(this);
            texture.setConstraint(new Constraint(texture).bottom(text, Constraint.Property.Type.TOP, 1).left(this, Constraint.Property.Type.LEFT, 2).right(this, Constraint.Property.Type.RIGHT, 2).top(this, Constraint.Property.Type.TOP, 2));
            elements.add(texture);
        }
    }

    public static class ElementProjectTexture extends Element<Fragment<?>>
    {
        public int[] background = new int[] { 200, 200, 200 };
        public ElementProjectTexture(@Nonnull Fragment<?> parent)
        {
            super(parent);
        }

        @Override
        public void render(int mouseX, int mouseY, float partialTick)
        {
            fill(background, 0);
        }
    }
}
