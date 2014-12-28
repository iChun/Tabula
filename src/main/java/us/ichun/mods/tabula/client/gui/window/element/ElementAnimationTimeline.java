package us.ichun.mods.tabula.client.gui.window.element;

import ichun.client.render.RendererHelper;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.util.MathHelper;
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

    public double sliderProgVert;
    public double sliderProgHori;

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

        RendererHelper.drawColourOnScreen(Theme.instance.elementTreeItemBorder[0], Theme.instance.elementTreeItemBorder[1], Theme.instance.elementTreeItemBorder[2], 255, getPosX() + 101, getPosY() + height - 19, width - 101, 20, 0); //timeline bg

        RendererHelper.startGlScissor(getPosX(), getPosY(), 100, height - 20);

        //get total animation element height
        int size = 0;
        boolean hasScrollVert = false;

        int timeWidth = 0;

        for(ElementListTree.Tree tree : parent.workspace.windowAnimate.animList.trees)
        {
            if(tree.selected)
            {
                Animation anim = (Animation)tree.attachedObject;

                final int spacingY = 13;
                int offY = 0;
                for(Map.Entry<String, ArrayList<AnimationComponent>> e : anim.sets.entrySet())
                {
                    offY += spacingY;

                    for(AnimationComponent comp : e.getValue())
                    {
                        if(comp.startKey + comp.length > timeWidth)
                        {
                            timeWidth = comp.startKey + comp.length;
                        }
                        offY += spacingY;
                    }
                }

                size = offY;
                hasScrollVert = size > height - 20;
                break;
            }
        }
        boolean hasScrollHori = timeWidth + 20 > Math.floor((float)(width - 101) / (float)tickWidth);

        GL11.glPushMatrix();
        GL11.glTranslated(0D, (double)-((size - (height - 20)) * sliderProgVert), 0D);

        //draw animation elements
        for(ElementListTree.Tree tree : parent.workspace.windowAnimate.animList.trees)
        {
            if(tree.selected)
            {
                Animation anim = (Animation)tree.attachedObject;

                final int spacingY = 13;

                int idClicked = -1;
                if(mouseX < posX + 100 && mouseX >= posX && mouseY >= posY && mouseY < posY + height)
                {
                    idClicked = (int)(mouseY - posY + ((size - (height - 20)) * sliderProgVert)) / 13; //spacing = 13
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

                    drawCompElement(cubeName, offY, e.getKey().equals(selectedIdentifier), false, idClicked == idHovered);

                    idHovered++;
                    offY += spacingY;

                    for(AnimationComponent comp : e.getValue())
                    {
                        drawCompElement(" - " + comp.name, offY, comp.identifier.equals(selectedIdentifier), comp.hidden, idClicked == idHovered);

                        idHovered++;
                        offY += spacingY;
                    }
                }
                break;
            }
        }
        GL11.glPopMatrix();

        //Timeline
        RendererHelper.startGlScissor(getPosX() + 101, getPosY() - 1, width - (hasScrollVert ? 111 : 101), height + 3);

        if(Mouse.isButtonDown(0) && mouseInBoundary(mouseX, mouseY))
        {
            if(mouseX > posX + 100 && mouseX < posX + (hasScrollVert ? width - 10 : width) && mouseY < posY + height - 10)
            {
                double tickPos = (int)(mouseX - (posX + 100 - 1) + (hasScrollHori ? ((double)(((timeWidth + 20) * tickWidth) - (width - (hasScrollVert ? 111 : 101))) * sliderProgHori) : 0));
                currentPos = (int)Math.max(0, tickPos / (double)tickWidth);
            }
        }

        if(hasScrollHori)
        {
            int x2 = getPosY() + height - 10;

            int timelineWidth = hasScrollVert ? (width - 10) : width;

            RendererHelper.drawColourOnScreen(Theme.instance.elementTreeScrollBarBorder[0], Theme.instance.elementTreeScrollBarBorder[1], Theme.instance.elementTreeScrollBarBorder[2], 255, getPosX() + 101 + ((timelineWidth - 101) / 40), x2 + 4, (timelineWidth - 101) - (((timelineWidth - 101) / 40) * 2), 2, 0);

            RendererHelper.drawColourOnScreen(Theme.instance.elementTreeScrollBarBorder[0], Theme.instance.elementTreeScrollBarBorder[1], Theme.instance.elementTreeScrollBarBorder[2], 255, getPosX() + 101 + (((timelineWidth - 101) - ((timelineWidth - 101) / 11)) * sliderProgHori), x2, Math.floor((float)(timelineWidth - 101) / 11D), 10, 0);
            RendererHelper.drawColourOnScreen(Theme.instance.elementTreeScrollBar[0], Theme.instance.elementTreeScrollBar[1], Theme.instance.elementTreeScrollBar[2], 255, getPosX() + 101 + 1 + (((timelineWidth - 101) - ((timelineWidth - 101) / 11)) * sliderProgHori), x2 + 1, Math.floor(((float)(timelineWidth - 101) / 11D) - 2), 8, 0);

            int sbx1 = getPosX() + 101 - parent.posX;
            int sbx2 = getPosX() + 1 + timelineWidth - parent.posX;
            int sby1 = x2 + 1 - parent.posY; ;
            int sby2 = sby1 + 10;

            if(Mouse.isButtonDown(0) && mouseX >= sbx1 && mouseX <= sbx2 && mouseY >= sby1 && mouseY <= sby2)
            {
                sbx1 += 10;
                sbx2 -= 10;
                sliderProgHori = 1.0F - MathHelper.clamp_double((double) (sbx2 - mouseX) / (double) (sbx2 - sbx1), 0.0D, 1.0D);
            }
        }
        else
        {
            sliderProgHori = 0.0D;
        }

        GL11.glPushMatrix();
        GL11.glTranslated(-((((timeWidth + 20) * tickWidth) - (hasScrollVert ? width - 111 : width - 101)) * sliderProgHori), 0D, 0D);

        int tick = 0;
        int timeOffX = 0;
        while(getPosX() + 100 + timeOffX < parent.posX + parent.width || timeOffX < (timeWidth + 20) * tickWidth) //TODO scroll stuff
        {
            if(tick % 5 == 0)
            {
                RendererHelper.drawColourOnScreen(Theme.instance.elementTreeItemBorder[0], Theme.instance.elementTreeItemBorder[1], Theme.instance.elementTreeItemBorder[2], 255, getPosX() + 100.5D + timeOffX, getPosY(), 1, height - 19, 0);
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

        RendererHelper.startGlScissor(getPosX() + 101, getPosY() - 1, width - (hasScrollVert ? 111 : 101), height - 19);

        GL11.glPushMatrix();
        GL11.glTranslated(0D, (double)-((size - (height - 20)) * sliderProgVert), 0D);

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
                    idClicked = (int)(mouseY - posY + ((size - (height - 20)) * sliderProgVert)) / 13; //spacing = 13
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
        GL11.glPopMatrix();

        RendererHelper.startGlScissor(getPosX() + 101, getPosY() - 1, width - (hasScrollVert ? 111 : 101), height + 3);

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

        GL11.glPopMatrix();

        RendererHelper.startGlScissor(getPosX() + 101, getPosY() - 1, width - 101, height + 3); //vert scroll bar

        if(hasScrollVert)
        {
            int x2 = getPosX() + width - 10;

            RendererHelper.drawColourOnScreen(Theme.instance.elementTreeItemBorder[0], Theme.instance.elementTreeItemBorder[1], Theme.instance.elementTreeItemBorder[2], 255, x2, getPosY(), 10, height - 19, 0);

            RendererHelper.drawColourOnScreen(Theme.instance.elementTreeScrollBarBorder[0], Theme.instance.elementTreeScrollBarBorder[1], Theme.instance.elementTreeScrollBarBorder[2], 255, x2 + 4, getPosY() + ((height - 20) / 40), 2, (height - 20) - (((height - 20) / 40) * 2), 0);

            RendererHelper.drawColourOnScreen(Theme.instance.elementTreeScrollBarBorder[0], Theme.instance.elementTreeScrollBarBorder[1], Theme.instance.elementTreeScrollBarBorder[2], 255, x2, getPosY() + (((height - 20) - ((height - 20) / 11)) * sliderProgVert), 10, Math.ceil((float)(height - 20) / 10D), 0);
            RendererHelper.drawColourOnScreen(Theme.instance.elementTreeScrollBar[0], Theme.instance.elementTreeScrollBar[1], Theme.instance.elementTreeScrollBar[2], 255, x2 + 1, getPosY() + 1 + (((height - 20) - ((height - 20) / 11)) * sliderProgVert), 8, Math.ceil(((float)(height - 20) / 10D) - 2), 0);

            int sbx1 = x2 + 1 - parent.posX;
            int sbx2 = sbx1 + 10;
            int sby1 = getPosY() - parent.posY;
            int sby2 = getPosY() + 1 + (height - 20) - parent.posY;

            if(Mouse.isButtonDown(0) && mouseX >= sbx1 && mouseX <= sbx2 && mouseY >= sby1 && mouseY <= sby2)
            {
                sby1 += 10;
                sby2 -= 10;
                sliderProgVert = 1.0F - MathHelper.clamp_double((double) (sby2 - mouseY) / (double) (sby2 - sby1), 0.0D, 1.0D);
            }
        }
        else
        {
            sliderProgVert = 0.0D;
        }

        //reset current scissor
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
            //get total animation element height
            int size = 0;

            for(ElementListTree.Tree tree : parent.workspace.windowAnimate.animList.trees)
            {
                if(tree.selected)
                {
                    Animation anim = (Animation)tree.attachedObject;

                    final int spacingY = 13;
                    int offY = 0;
                    for(Map.Entry<String, ArrayList<AnimationComponent>> e : anim.sets.entrySet())
                    {
                        offY += spacingY;

                        for(AnimationComponent comp : e.getValue())
                        {
                            offY += spacingY;
                        }
                    }

                    size = offY;
                    break;
                }
            }
            int idClicked = (int)(mouseY - posY + ((size - (height - 20)) * sliderProgVert)) / 13; //spacing = 13

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
        //get total animation element height
        int size = 0;
        int timeWidth = 0;
        for(ElementListTree.Tree tree : parent.workspace.windowAnimate.animList.trees)
        {
            if(tree.selected)
            {
                Animation anim = (Animation)tree.attachedObject;

                final int spacingY = 13;
                int offY = 0;
                for(Map.Entry<String, ArrayList<AnimationComponent>> e : anim.sets.entrySet())
                {
                    offY += spacingY;

                    for(AnimationComponent comp : e.getValue())
                    {
                        if(comp.startKey + comp.length > timeWidth)
                        {
                            timeWidth = comp.startKey + comp.length;
                        }
                        offY += spacingY;
                    }
                }

                size = offY;
                break;
            }
        }
        if(GuiScreen.isShiftKeyDown())
        {
            if(timeWidth + 20 > Math.floor((float)(width - 101) / (float)tickWidth))
            {
                sliderProgHori += 0.05D * -k;
                sliderProgHori = MathHelper.clamp_double(sliderProgHori, 0.0D, 1.0D);
            }
        }
        else
        {
            if(size > height - 20)
            {
                sliderProgVert += 0.05D * -k;
                sliderProgVert = MathHelper.clamp_double(sliderProgVert, 0.0D, 1.0D);
            }
        }

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
