package us.ichun.mods.tabula.client.gui.window.element;

import ichun.client.render.RendererHelper;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;
import us.ichun.mods.tabula.client.gui.Theme;
import us.ichun.mods.tabula.client.gui.window.Window;
import us.ichun.mods.tabula.common.Tabula;
import us.ichun.module.tabula.common.project.components.Animation;
import us.ichun.module.tabula.common.project.components.AnimationComponent;
import us.ichun.module.tabula.common.project.components.CubeInfo;

import java.util.ArrayList;
import java.util.Map;

public class ElementAnimationTimeline extends Element
{

    public int tickWidth = 5;

    public int currentPos;

    public String selectedIdentifier;

    public ElementAnimationTimeline(Window window, int x, int y, int w, int h, int ID)
    {
        super(window, x, y, w, h, ID, false);

        selectedIdentifier = "";
    }

    @Override
    public void draw(int mouseX, int mouseY, boolean hover)
    {
        //        RendererHelper.drawColourOnScreen(Theme.instance.elementTreeBorder[0], Theme.instance.elementTreeBorder[1], Theme.instance.elementTreeBorder[2], 255, getPosX() + 1, getPosY() + 1, width - 2, height - 2, 0);
        RendererHelper.drawColourOnScreen(Theme.instance.elementTreeBorder[0], Theme.instance.elementTreeBorder[1], Theme.instance.elementTreeBorder[2], 255, getPosX() + 100, getPosY(), 1, height, 0);

        for(ElementListTree.Tree tree : parent.workspace.windowAnimate.animList.trees)
        {
            if(tree.selected)
            {
                Animation anim = (Animation)tree.attachedObject;

                final int spacingY = 13;

                int idClicked = -1;
                if(mouseX < posX + 100 && mouseX >= posX && mouseY >= posY && mouseY < posY + height)
                {
                    idClicked = (mouseY - posY) / 13; //spacing = 13
                }

                int idHovered = 0;
                int offY = 0;
                for(Map.Entry<String, ArrayList<AnimationComponent>> e : anim.sets.entrySet())
                {
                    String cubeName = "<Unknown (not good)>";
                    for(ElementListTree.Tree model : parent.workspace.windowModelTree.modelList.trees)
                    {
                        if(model.attachedObject instanceof CubeInfo && ((CubeInfo)model.attachedObject).identifier.equals(e.getKey()))
                        {
                            cubeName = ((CubeInfo)model.attachedObject).name;
                        }
                    }

                    drawCompElement(cubeName, offY, e.getKey().equals(selectedIdentifier), false, idClicked == idHovered); //TODO hover code with scroll support

                    idHovered++;
                    offY += spacingY;

                    for(AnimationComponent comp : e.getValue())
                    {
                        drawCompElement(" - " + comp.name, offY, comp.identifier.equals(selectedIdentifier), comp.hidden, idClicked == idHovered); //TODO hover!

                        idHovered++;
                        offY += spacingY;
                    }
                }

                break;
            }
        }

        //Timeline
        RendererHelper.startGlScissor(getPosX() + 101, getPosY() - 1, width - 101, height + 3);

        if(Mouse.isButtonDown(0) && mouseInBoundary(mouseX, mouseY))
        {
            if(mouseX > posX + 100 && mouseY < posY + height - 10)
            {
                int tickPos = mouseX - (posX + 100 - 1);
                currentPos = Math.max(0, tickPos / 5);
            }
        }

        RendererHelper.drawColourOnScreen(Theme.instance.elementTreeItemBorder[0], Theme.instance.elementTreeItemBorder[1], Theme.instance.elementTreeItemBorder[2], 255, getPosX() + 101, getPosY() + height - 19, width - 101, 20, 0); //timeline bg

        int tick = 0;
        int timeOffX = 0;
        while(getPosX() + 100 + timeOffX < parent.posX + parent.width) //TODO scroll stuff
        {
            if(tick % 5 == 0)
            {
                RendererHelper.drawColourOnScreen(Theme.instance.elementTreeScrollBarBorder[0], Theme.instance.elementTreeScrollBarBorder[1], Theme.instance.elementTreeScrollBarBorder[2], 255, getPosX() + 100 + timeOffX, getPosY() + height - 19, 2, 7, 0);
                GL11.glPushMatrix();
                float scale = 0.5F;
                GL11.glScalef(scale, scale, scale);
                parent.workspace.getFontRenderer().drawString(Integer.toString(tick), (int)((getPosX() + 103 + timeOffX) / scale), (int)((getPosY() + height - 16) / scale), Theme.getAsHex(tick == currentPos ? Theme.instance.font : Theme.instance.fontDim), false);
                GL11.glPopMatrix();
            }
            else
            {
                RendererHelper.drawColourOnScreen(Theme.instance.elementTreeScrollBarBorder[0], Theme.instance.elementTreeScrollBarBorder[1], Theme.instance.elementTreeScrollBarBorder[2], 255, getPosX() + 100 + timeOffX, getPosY() + height - 19, 2, 2, 0);
            }
            tick++;
            timeOffX += tickWidth;
        }

        //Animation Component areas
        for(ElementListTree.Tree tree : parent.workspace.windowAnimate.animList.trees)
        {
            if(tree.selected)
            {
                Animation anim = (Animation)tree.attachedObject;

                final int spacingY = 13;

                int idClicked = -1;
                if(mouseX < posX + 100 && mouseX >= posX && mouseY >= posY && mouseY < posY + height)
                {
                    idClicked = (mouseY - posY) / 13; //spacing = 13
                }

                int idHovered = 0;
                int offY = 0;
                for(Map.Entry<String, ArrayList<AnimationComponent>> e : anim.sets.entrySet())
                {
                    idHovered++;
                    offY += spacingY;
                    for(AnimationComponent comp : e.getValue())
                    {
                        //draw stuff
                        int[] lineClr = Theme.instance.elementTreeItemBg;
                        if(comp.identifier.equals(selectedIdentifier))
                        {
                            lineClr = Theme.instance.elementTreeItemBgSelect;
                        }
                        else if(idClicked == idHovered)
                        {
                            lineClr = Theme.instance.elementTreeItemBgHover;
                        }

                        RendererHelper.drawColourOnScreen(lineClr[0], lineClr[1], lineClr[2], 255, getPosX() + 99D + (comp.startKey * tickWidth), getPosY() + offY + 4.5D, 4, 4, 0);
                        RendererHelper.drawColourOnScreen(lineClr[0], lineClr[1], lineClr[2], 255, getPosX() + 99D + ((comp.startKey + comp.length) * tickWidth), getPosY() + offY + 4.5D, 4, 4, 0);

                        RendererHelper.drawColourOnScreen(lineClr[0], lineClr[1], lineClr[2], 255, getPosX() + 99D + (comp.startKey * tickWidth), getPosY() + offY + 5.5D, ((comp.length) * tickWidth), 2, 0);

                        idHovered++;
                        offY += spacingY;
                    }
                }

                break;
            }
        }

        //Timeline cursor
        RendererHelper.drawColourOnScreen(Theme.instance.tabBorder[0], Theme.instance.tabBorder[1], Theme.instance.tabBorder[2], 255, getPosX() + 100.5D + (currentPos * tickWidth), getPosY(), 1, height - 19, 0);
        RendererHelper.drawColourOnScreen(Theme.instance.tabBorder[0], Theme.instance.tabBorder[1], Theme.instance.tabBorder[2], 255, getPosX() + 99 + (currentPos * tickWidth), getPosY() + height - 19, 4, 1.5D, 0);

        if(currentPos % 5 != 0)
        {
            RendererHelper.drawColourOnScreen(Theme.instance.elementTreeItemBorder[0], Theme.instance.elementTreeItemBorder[1], Theme.instance.elementTreeItemBorder[2], 255, (getPosX() + 100 + (currentPos * tickWidth)) - (parent.workspace.getFontRenderer().getStringWidth(Integer.toString(currentPos)) / 2) * 0.5F, getPosY() + height - 17, parent.workspace.getFontRenderer().getStringWidth(Integer.toString(currentPos)) / 2 + 2, 5, 0);//blocks underlying number
            GL11.glPushMatrix();
            float scale = 0.5F;
            GL11.glScalef(scale, scale, scale);
            parent.workspace.getFontRenderer().drawString(Integer.toString(currentPos), (int)((getPosX() + 101 + (currentPos * tickWidth)) / scale) - parent.workspace.getFontRenderer().getStringWidth(Integer.toString(currentPos)) / 2, (int)((getPosY() + height - 16) / scale), Theme.getAsHex(Theme.instance.font), false);
            GL11.glPopMatrix();
        }


        if(parent.isTab)
        {
            RendererHelper.startGlScissor(parent.posX + 1, parent.posY + 1 + 12, parent.getWidth() - 2, parent.getHeight() - 2 - 12);
        }
        else
        {
            RendererHelper.startGlScissor(parent.posX + 1, parent.posY + 1, parent.getWidth() - 2, parent.getHeight() - 2);
        }
    }

    public void drawCompElement(String name, int offY, boolean isSelected, boolean isHidden, boolean hover)
    {
        RendererHelper.drawColourOnScreen(Theme.instance.elementTreeItemBorder[0], Theme.instance.elementTreeItemBorder[1], Theme.instance.elementTreeItemBorder[2], 255, getPosX(), getPosY() + offY, 100, 13, 0);

        if(isSelected)
        {
            RendererHelper.drawColourOnScreen(Theme.instance.elementTreeItemBgSelect[0], Theme.instance.elementTreeItemBgSelect[1], Theme.instance.elementTreeItemBgSelect[2], 255, getPosX()+ 1, getPosY() + offY + 1, 100 - 2, 13 - 2, 0);
        }
        else if(hover)
        {
            RendererHelper.drawColourOnScreen(Theme.instance.elementTreeItemBgHover[0], Theme.instance.elementTreeItemBgHover[1], Theme.instance.elementTreeItemBgHover[2], 255, getPosX() + 1, getPosY() + offY + 1, 100 - 2, 13 - 2, 0);
        }
        else
        {
            RendererHelper.drawColourOnScreen(Theme.instance.elementTreeItemBg[0], Theme.instance.elementTreeItemBg[1], Theme.instance.elementTreeItemBg[2], 255, getPosX() + 1, getPosY() + offY + 1, 100 - 2, 13 - 2, 0);
        }
        if(isHidden)
        {
            parent.workspace.getFontRenderer().drawString(reString(name, 100), getPosX() + 4, getPosY() + offY + 2, Theme.getAsHex(Theme.instance.fontDim), false);
        }
        else
        {
            parent.workspace.getFontRenderer().drawString(reString(name, 100), getPosX() + 4, getPosY() + offY + 2, Theme.getAsHex(Theme.instance.font), false);
        }
    }

    @Override
    public boolean onClick(int mouseX, int mouseY, int id)
    {
        if(mouseX < posX + 100)
        {
            int idClicked = (mouseY - posY) / 13; //spacing = 13

            for(ElementListTree.Tree tree : parent.workspace.windowAnimate.animList.trees)
            {
                if(tree.selected)
                {
                    Animation anim = (Animation)tree.attachedObject;

                    int offY = 0;
                    for(Map.Entry<String, ArrayList<AnimationComponent>> e : anim.sets.entrySet())
                    {
                        if(idClicked == offY)
                        {
                            selectedIdentifier = e.getKey();
                        }
                        offY++;

                        for(AnimationComponent comp : e.getValue())
                        {
                            if(idClicked == offY)
                            {
                                if(id == 0)
                                {
                                    selectedIdentifier = comp.identifier;
                                }
                                else if(id == 1)
                                {
                                    if(parent.workspace.remoteSession)
                                    {
                                        //TODO this
                                    }
                                    else
                                    {
                                        Tabula.proxy.tickHandlerClient.mainframe.toggleAnimComponentVisibility(parent.workspace.projectManager.projects.get(parent.workspace.projectManager.selectedProject).identifier, anim.identifier, comp.identifier);
                                    }
                                }
                            }
                            offY++;
                        }
                    }

                    break;
                }
            }

        }
        return false;//return true for elements that has input eg typing
    }

    @Override
    public boolean mouseInBoundary(int mouseX, int mouseY)
    {
        return mouseX >= this.posX && mouseX <= this.posX + this.width && mouseY >= this.posY && mouseY <= this.posY + this.height && !(mouseX < this.posX + 100 && mouseY > this.posY + this.height - 20);
    }

    @Override
    public boolean mouseScroll(int mouseX, int mouseY, int k)
    {
        return false;
    }

    public String reString(String s, int width)
    {
        while(s.length() > 1 && parent.workspace.getFontRenderer().getStringWidth(s) > width - 3)
        {
            if(s.startsWith("..."))
            {
                break;
            }
            if(s.endsWith("..."))
            {
                s = s.substring(0, s.length() - 5) + "...";
            }
            else
            {
                s = s.substring(0, s.length() - 1) + "...";
            }
        }
        return s;
    }

    @Override
    public void resized()
    {
        posX = 101;
        width = parent.width - posX - 1;
        posY = parent.BORDER_SIZE + 10;
        height = parent.height - posY - 1;
    }
}
