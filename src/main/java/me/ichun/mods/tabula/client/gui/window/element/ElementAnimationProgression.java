package me.ichun.mods.tabula.client.gui.window.element;

import me.ichun.mods.ichunutil.client.gui.window.Window;
import me.ichun.mods.ichunutil.client.gui.window.element.Element;
import me.ichun.mods.ichunutil.client.render.RendererHelper;
import me.ichun.mods.ichunutil.common.module.tabula.math.PolynomialFunctionLagrangeForm;
import me.ichun.mods.ichunutil.common.module.tabula.project.ProjectInfo;
import me.ichun.mods.ichunutil.common.module.tabula.project.components.Animation;
import me.ichun.mods.ichunutil.common.module.tabula.project.components.AnimationComponent;
import me.ichun.mods.tabula.client.gui.GuiWorkspace;
import me.ichun.mods.tabula.client.gui.window.WindowEditAnimComponentProgression;
import me.ichun.mods.tabula.common.Tabula;
import me.ichun.mods.tabula.common.packet.PacketGenericMethod;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.math.MathHelper;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;

import java.util.ArrayList;

public class ElementAnimationProgression extends Element
{
    public Coords coordDragged = null;
    public boolean lmbDown;

    public ElementAnimationProgression(Window window, int x, int y, int ID)
    {
        super(window, x, y, 250, 250, ID, false);
    }

    @Override
    public void draw(int mouseX, int mouseY, boolean hover)
    {
        RendererHelper.drawColourOnScreen(parent.workspace.currentTheme.elementTreeItemBorder[0], parent.workspace.currentTheme.elementTreeItemBorder[1], parent.workspace.currentTheme.elementTreeItemBorder[2], 255, getPosX(), getPosY(), width, height, 0);

        WindowEditAnimComponentProgression window = (WindowEditAnimComponentProgression)parent;

        ProjectInfo info = null;
        Animation anim = null;
        AnimationComponent comp = null;

        if(!((GuiWorkspace)parent.workspace).projectManager.projects.isEmpty())
        {
            info = ((GuiWorkspace)parent.workspace).projectManager.projects.get(((GuiWorkspace)parent.workspace).projectManager.selectedProject);
            for(Animation animation : info.anims)
            {
                if(animation.identifier.equals(window.parentAnim))
                {
                    anim = animation;
                    for(AnimationComponent comp1 : anim.sets.get(window.modelIdent))
                    {
                        if(comp1.identifier.equals(window.comp))
                        {
                            comp = comp1;
                            break;
                        }
                    }
                    break;
                }
            }
        }

        if(comp == null)
        {
            return;
        }

        ArrayList<double[]> coords = new ArrayList<>();
        coords.add(new double[] { 0, 0 });
        coords.add(new double[] { 1, 1 });

        if(comp.progressionCoords != null)
        {
            coords.addAll(comp.progressionCoords);
        }

        PolynomialFunctionLagrangeForm curve = comp.getProgressionCurve();

        GlStateManager.disableTexture2D();
        GlStateManager.color(parent.workspace.currentTheme.elementInputUpDownClick[0] / 255F, parent.workspace.currentTheme.elementInputUpDownClick[1] / 255F, parent.workspace.currentTheme.elementInputUpDownClick[2] / 255F, 1.0F);
        GL11.glLineWidth(2F);
        GL11.glBegin(GL11.GL_LINE_STRIP);
        if(curve == null)
        {
            GL11.glVertex2f(getPosX() + width, getPosY());
            GL11.glVertex2f(getPosX(), getPosY() + height);
        }
        else
        {
            for(int i = 0; i <= width; i++)
            {
                GL11.glVertex2f(getPosX() + MathHelper.clamp_float((width * i / (float)width), 0.0F, width), getPosY() + MathHelper.clamp_float(height - (height * (float)curve.value(i / (double)height)), 0.0F, height));
            }
        }
        GL11.glEnd();
        GlStateManager.enableTexture2D();

        for(double[] coord : coords)
        {
            RendererHelper.drawColourOnScreen(parent.workspace.currentTheme.elementInputUpDownHover[0], parent.workspace.currentTheme.elementInputUpDownHover[1], parent.workspace.currentTheme.elementInputUpDownHover[2], 255, getPosX() + width * coord[0] - 2, getPosY() + height - height * coord[1] - 2, 4, 4, 0);
        }

        int clickPosX = width - (width - (mouseX - posX));
        int clickPosY = height - (mouseY - posY);
        if(Mouse.isButtonDown(0) && !lmbDown)
        {
            int leeway = 8;
            boolean clickedOnLine = false;

            int curveY = clickPosX;
            if(curve == null)
            {
                clickedOnLine = Math.abs(clickPosX - clickPosY) < leeway / 2;
            }
            else
            {
                curveY = (int)(curve.value(clickPosX / (double)width) * height);
                clickedOnLine = Math.abs(curveY - clickPosY) < leeway / 2;
            }
            if(clickedOnLine && !(clickPosX < 0 || clickPosY < 0 || clickPosX > width || clickPosY > height))
            {
                Coords oldCoord = coordDragged;

                if(oldCoord == null && comp.progressionCoords != null)
                {
                    for(double[] coord : comp.progressionCoords)
                    {
                        if(Math.abs((coord[0] * width) - clickPosX) < leeway / 2 && Math.abs((coord[1] * height) - clickPosY) < leeway / 2)
                        {
                            oldCoord = new Coords((coord[0] * width), (coord[1] * height));
                            break;
                        }
                    }
                }

                coordDragged = new Coords(MathHelper.clamp_double(clickPosX, 0D, width), MathHelper.clamp_double(clickPosY, 0D, height));
                if(!((GuiWorkspace)parent.workspace).remoteSession)
                {
                    Tabula.proxy.tickHandlerClient.mainframe.moveAnimCompProgCoord(info.identifier, anim.identifier, comp.identifier, oldCoord == null ? -1 : oldCoord.x / (double)width, oldCoord == null ? -1 : oldCoord.y / (double)height, coordDragged.x / (double)width, coordDragged.y / (double)height);
                }
                else if(!((GuiWorkspace)parent.workspace).sessionEnded && ((GuiWorkspace)parent.workspace).isEditor)
                {
                    Tabula.channel.sendToServer(new PacketGenericMethod(((GuiWorkspace)parent.workspace).host, "moveAnimCompProgCoord", info.identifier, anim.identifier, comp.identifier, oldCoord == null ? -1 : oldCoord.x / (double)width, oldCoord == null ? -1 : oldCoord.y / (double)height, coordDragged.x / (double)width, coordDragged.y / (double)height));
                }
            }
        }
        if(coordDragged != null)
        {
            //Check if boundaries are out of bounds and if it is >-10, remove the coord.
            if(clickPosX < -10 || clickPosY < -10 || clickPosX > width + 10 || clickPosY > height + 10)
            {
                //remove coord
                if(!((GuiWorkspace)parent.workspace).remoteSession)
                {
                    Tabula.proxy.tickHandlerClient.mainframe.moveAnimCompProgCoord(info.identifier, anim.identifier, comp.identifier, coordDragged.x / (double)width, coordDragged.y / (double)height, -1, -1);
                }
                else if(!((GuiWorkspace)parent.workspace).sessionEnded && ((GuiWorkspace)parent.workspace).isEditor)
                {
                    Tabula.channel.sendToServer(new PacketGenericMethod(((GuiWorkspace)parent.workspace).host, "moveAnimCompProgCoord", info.identifier, anim.identifier, comp.identifier, coordDragged.x / (double)width, coordDragged.y / (double)height, -1, -1));
                }
                coordDragged = null;
            }
            else if(clickPosX != coordDragged.x || clickPosY != coordDragged.y)
            {
                Coords oldCoord = coordDragged;
                coordDragged = new Coords(MathHelper.clamp_double(clickPosX, 0D, width), MathHelper.clamp_double(clickPosY, 0D, height));

                if(!((GuiWorkspace)parent.workspace).remoteSession)
                {
                    Tabula.proxy.tickHandlerClient.mainframe.moveAnimCompProgCoord(info.identifier, anim.identifier, comp.identifier, oldCoord.x / (double)width, oldCoord.y / (double)height, coordDragged.x / (double)width, coordDragged.y / (double)height);
                }
                else if(!((GuiWorkspace)parent.workspace).sessionEnded && ((GuiWorkspace)parent.workspace).isEditor)
                {
                    Tabula.channel.sendToServer(new PacketGenericMethod(((GuiWorkspace)parent.workspace).host, "moveAnimCompProgCoord", info.identifier, anim.identifier, comp.identifier, oldCoord.x / (double)width, oldCoord.y / (double)height, coordDragged.x / (double)width, coordDragged.y / (double)height));
                }
            }
        }
        if(!Mouse.isButtonDown(0) && lmbDown)
        {
            coordDragged = null;
        }

        lmbDown = Mouse.isButtonDown(0);
    }

    @Override
    public void resized()
    {
        posX = (parent.width - width) / 2;
        posY = 25 + 10;
    }

    public class Coords
    {
        public double x;
        public double y;

        public Coords(double x, double y)
        {
            this.x = x;
            this.y = y;
        }
    }
}
