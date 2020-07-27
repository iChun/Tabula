package me.ichun.mods.tabula.client.gui.window;

import com.mojang.blaze3d.matrix.MatrixStack;
import me.ichun.mods.ichunutil.client.gui.bns.window.Window;
import me.ichun.mods.ichunutil.client.gui.bns.window.constraint.Constraint;
import me.ichun.mods.tabula.client.gui.WorkspaceTabula;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.Screen;
import org.lwjgl.glfw.GLFW;

public class WindowInputReceiver extends Window<WorkspaceTabula>
{
    public WindowInputReceiver(WorkspaceTabula parent)
    {
        super(parent);
        size(parent.getWidth(), parent.getHeight());
        setConstraint(Constraint.matchParent(this, parent, 0));
        borderSize = () -> 0;
        titleSize = () -> 0;

        disableBringToFront();
        disableDocking();
        disableDockStacking();
        disableUndocking();
        disableDrag();
        disableDragResize();
        disableTitle();
    }

    @Override
    public void init()
    {
        constraint.apply();
    }

    @Override
    public void resize(Minecraft mc, int width, int height)
    {
        constraint.apply();
    }

    @Override
    public void render(MatrixStack stack, int mouseX, int mouseY, float partialTick)
    {
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button)
    {
        if(isMouseOver(mouseX, mouseY))
        {
            if(button == GLFW.GLFW_MOUSE_BUTTON_LEFT)
            {
                parent.selecting = true;
            }
            parent.setDragging(true);
            return true;
        }
        return false;
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button)
    {
        super.mouseReleased(mouseX, mouseY, button);
        return true;
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double distX, double distY)
    {
        if(button == GLFW.GLFW_MOUSE_BUTTON_RIGHT || button == GLFW.GLFW_MOUSE_BUTTON_MIDDLE)
        {
            if(button == GLFW.GLFW_MOUSE_BUTTON_MIDDLE || Screen.hasShiftDown()) // if middle mouse or holding shift
            {
                float mag = 0.0125F;
                parent.mainframe.getCamera().x -= distX * mag;
                parent.mainframe.getCamera().y += distY * mag;
            }
            else if(Screen.hasControlDown())
            {
                parent.mainframe.getCamera().zoom += (distX - distY) * 0.05D;
            }
            else
            {
                float mag = 0.5F;
                parent.mainframe.getCamera().yaw += distX * mag;
                parent.mainframe.getCamera().pitch -= distY * mag;
            }
        }
        return true;
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double amount)
    {
        if(isMouseOver(mouseX, mouseY))
        {
            if(Screen.hasShiftDown())
            {
                parent.mainframe.getCamera().fov -= amount * 1D;
            }
            else
            {
                parent.mainframe.getCamera().zoom += amount * 0.05D;
            }
            return true;
        }
        return false;
    }

    @Override
    public boolean keyPressed(int p_keyPressed_1_, int p_keyPressed_2_, int p_keyPressed_3_)
    {
        return false;
    }

    @Override
    public boolean keyReleased(int keyCode, int scanCode, int modifiers)
    {
        return false;
    }
}
