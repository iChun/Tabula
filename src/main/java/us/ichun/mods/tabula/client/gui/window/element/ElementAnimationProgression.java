package us.ichun.mods.tabula.client.gui.window.element;

import ichun.client.render.RendererHelper;
import net.minecraft.util.MathHelper;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;
import us.ichun.mods.tabula.client.gui.Theme;
import us.ichun.mods.tabula.client.gui.window.Window;
import us.ichun.mods.tabula.client.gui.window.WindowEditAnimComponentProgression;
import us.ichun.mods.tabula.common.Tabula;
import us.ichun.module.tabula.common.math.PolynomialFunctionLagrangeForm;
import us.ichun.module.tabula.common.project.ProjectInfo;
import us.ichun.module.tabula.common.project.components.Animation;
import us.ichun.module.tabula.common.project.components.AnimationComponent;

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
        RendererHelper.drawColourOnScreen(Theme.instance.elementTreeItemBorder[0], Theme.instance.elementTreeItemBorder[1], Theme.instance.elementTreeItemBorder[2], 255, getPosX(), getPosY(), width, height, 0);

        WindowEditAnimComponentProgression window = (WindowEditAnimComponentProgression)parent;

        ProjectInfo info = null;
        Animation anim = null;
        AnimationComponent comp = null;

        if(!parent.workspace.projectManager.projects.isEmpty())
        {
            info = parent.workspace.projectManager.projects.get(parent.workspace.projectManager.selectedProject);
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

        ArrayList<double[]> coords = new ArrayList<double[]>();
        coords.add(new double[] { 0, 0 });
        coords.add(new double[] { 1, 1 });

        if(comp.progressionCoords != null)
        {
            coords.addAll(comp.progressionCoords);
        }

        PolynomialFunctionLagrangeForm curve = comp.getProgressionCurve();

        GL11.glDisable(GL11.GL_TEXTURE_2D);
        GL11.glColor3f(Theme.instance.elementInputUpDownClick[0] / 255F, Theme.instance.elementInputUpDownClick[1] / 255F, Theme.instance.elementInputUpDownClick[2] / 255F);
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
        GL11.glEnable(GL11.GL_TEXTURE_2D);

        for(double[] coord : coords)
        {
            RendererHelper.drawColourOnScreen(Theme.instance.elementInputUpDownHover[0], Theme.instance.elementInputUpDownHover[1], Theme.instance.elementInputUpDownHover[2], 255, getPosX() + width * coord[0] - 2, getPosY() + height - height * coord[1] - 2, 4, 4, 0);
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
                if(parent.workspace.remoteSession)
                {
                    //TODO this
                }
                else
                {
                    Tabula.proxy.tickHandlerClient.mainframe.moveAnimCompProgCoord(info.identifier, anim.identifier, comp.identifier, oldCoord == null ? -1 : oldCoord.x / (double)width, oldCoord == null ? -1 : oldCoord.y / (double)height, coordDragged.x / (double)width, coordDragged.y / (double)height);
                }
            }
        }
        if(coordDragged != null)
        {
            //Check if boundaries are out of bounds and if it is >-10, remove the coord.
            if(clickPosX < -10 || clickPosY < -10 || clickPosX > width + 10 || clickPosY > height + 10)
            {
                //remove coord
                if(parent.workspace.remoteSession)
                {
                    //TODO this
                }
                else
                {
                    Tabula.proxy.tickHandlerClient.mainframe.moveAnimCompProgCoord(info.identifier, anim.identifier, comp.identifier, coordDragged.x / (double)width, coordDragged.y / (double)height, -1, -1);
                }
                coordDragged = null;
            }
            else if(clickPosX != coordDragged.x || clickPosY != coordDragged.y)
            {
                Coords oldCoord = coordDragged;
                coordDragged = new Coords(MathHelper.clamp_double(clickPosX, 0D, width), MathHelper.clamp_double(clickPosY, 0D, height));

                if(parent.workspace.remoteSession)
                {
                    //TODO this
                }
                else
                {
                    Tabula.proxy.tickHandlerClient.mainframe.moveAnimCompProgCoord(info.identifier, anim.identifier, comp.identifier, oldCoord.x / (double)width, oldCoord.y / (double)height, coordDragged.x / (double)width, coordDragged.y / (double)height);
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
