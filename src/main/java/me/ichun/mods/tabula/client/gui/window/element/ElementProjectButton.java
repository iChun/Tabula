package me.ichun.mods.tabula.client.gui.window.element;

import me.ichun.mods.ichunutil.client.gui.bns.window.Fragment;
import me.ichun.mods.ichunutil.client.gui.bns.window.view.element.ElementClickable;
import net.minecraft.client.resources.I18n;

import javax.annotation.Nonnull;
import java.util.function.Consumer;

public class ElementProjectButton<T extends ElementProjectButton> extends ElementClickable<T>
{
    public @Nonnull String text;

    public ElementProjectButton(@Nonnull Fragment parent, String s, Consumer<T> callback)
    {
        super(parent, callback);
        text = I18n.format(s);
    }

    @Override
    public void render(int mouseX, int mouseY, float partialTick)
    {
        hover = isMouseOver(mouseX, mouseY) || parentFragment.getFocused() == this;

        if(renderMinecraftStyle())
        {
            renderMinecraftStyleButton(getLeft(), getTop(), width, height, parentFragment.isDragging() && parentFragment.getFocused() == this ? ButtonState.CLICK : hover ? ButtonState.HOVER : ButtonState.IDLE);
        }
        else
        {
            int[] colour = parentFragment.isDragging() && parentFragment.getFocused() == this ? getTheme().elementButtonClick : hover ? getTheme().elementProjectTabHover : getTheme().elementTreeItemBg;
            fill(colour, 0);
        }
        if(!text.isEmpty())
        {
            String s = reString(text, width - 12);
            if(s.equals(text))
            {
                setTooltip(null);
            }
            else
            {
                setTooltip(text);
            }
            //TODO different font colours
            drawString(s, getLeft() + 2, getTop() + (height - getFontRenderer().FONT_HEIGHT) / 2F + 1);

            drawString("X", getRight() - 7, getTop() + (height - getFontRenderer().FONT_HEIGHT) / 2F + 1);
        }
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button)
    {
        boolean flag = super.mouseReleased(mouseX, mouseY, button); // unsets dragging;
        parentFragment.setFocused(null); //we're a one time click, stop focusing on us
        if(isMouseOver(mouseX, mouseY) && button == 0) //lmb //TODO close project
        {
            trigger();
        }
        return flag;
    }

    public boolean isOverX(double mouseX, double mouseY)
    {
        return parentFragment.isMouseOver(mouseX, mouseY) && isMouseBetween(mouseX, getLeft() + width - 5, getLeft() + width) && isMouseBetween(mouseY, getTop(), getTop() + height);
    }

    @Override
    public void onClickRelease() {} //we don't do anything, we're a static button

    @Override
    public int getMinWidth()
    {
        return 15;
    }

    @Override
    public int getMinHeight()
    {
        return 10;
    }
}
