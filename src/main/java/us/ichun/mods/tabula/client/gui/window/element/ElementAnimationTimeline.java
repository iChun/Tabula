package us.ichun.mods.tabula.client.gui.window.element;

import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.MathHelper;
import net.minecraft.util.StatCollector;
import org.lwjgl.input.Mouse;
import us.ichun.mods.ichunutil.client.gui.window.Window;
import us.ichun.mods.ichunutil.client.gui.window.element.Element;
import us.ichun.mods.ichunutil.client.render.RendererHelper;
import us.ichun.mods.ichunutil.common.module.tabula.common.project.components.Animation;
import us.ichun.mods.ichunutil.common.module.tabula.common.project.components.AnimationComponent;
import us.ichun.mods.ichunutil.common.module.tabula.common.project.components.CubeInfo;
import us.ichun.mods.tabula.client.gui.GuiWorkspace;
import us.ichun.mods.tabula.client.gui.Theme;
import us.ichun.mods.tabula.common.Tabula;
import us.ichun.mods.tabula.common.packet.PacketGenericMethod;

import java.util.ArrayList;
import java.util.Locale;
import java.util.Map;

public class ElementAnimationTimeline extends Element
{

    public int mX;
    public int mY;

    public int tickWidth = 5;

    private int currentPos;

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
        this.mX = mouseX;
        this.mY = mouseY;
        //        RendererHelper.drawColourOnScreen(parent.workspace.currentTheme.elementTreeBorder[0], parent.workspace.currentTheme.elementTreeBorder[1], parent.workspace.currentTheme.elementTreeBorder[2], 255, getPosX() + 1, getPosY() + 1, width - 2, height - 2, 0);
        RendererHelper.drawColourOnScreen(parent.workspace.currentTheme.elementTreeBorder[0], parent.workspace.currentTheme.elementTreeBorder[1], parent.workspace.currentTheme.elementTreeBorder[2], 255, getPosX() + 100, getPosY(), 1, height, 0);

        RendererHelper.drawColourOnScreen(parent.workspace.currentTheme.elementTreeItemBorder[0], parent.workspace.currentTheme.elementTreeItemBorder[1], parent.workspace.currentTheme.elementTreeItemBorder[2], 255, getPosX() + 101, getPosY() + height - 19, width - 101, 20, 0); //timeline bg

        RendererHelper.startGlScissor(getPosX(), getPosY(), 100, height - 20);

        //get total animation element height
        int size = 0;
        boolean hasScrollVert = false;

        int timeWidth = 0;

        Animation currentAnim = null;

        for(ElementListTree.Tree tree : ((GuiWorkspace)parent.workspace).windowAnimate.animList.trees)
        {
            if(tree.selected)
            {
                currentAnim = (Animation)tree.attachedObject;

                break;
            }
        }

        if(currentAnim != null)
        {
            final int spacingY = 13;
            int offY = 0;
            for(Map.Entry<String, ArrayList<AnimationComponent>> e : currentAnim.sets.entrySet())
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
        }

        boolean hasScrollHori = timeWidth + 20 > Math.floor((float)(width - 101) / (float)tickWidth);

        GlStateManager.pushMatrix();
        GlStateManager.translate(0D, (double)-((size - (height - 20)) * sliderProgVert), 0D);

        //draw animation elements
        if(currentAnim != null)
        {
            final int spacingY = 13;

            int idClicked = -1;
            if(mouseX < posX + 100 && mouseX >= posX && mouseY >= posY && mouseY < posY + height)
            {
                idClicked = (int)(mouseY - posY + ((size - (height - 20)) * sliderProgVert)) / 13; //spacing = 13
            }

            int idHovered = 0;
            int offY = 0;
            for(Map.Entry<String, ArrayList<AnimationComponent>> e : currentAnim.sets.entrySet())
            {
                String cubeName = "<Unknown (not good)>";
                for(ElementListTree.Tree model : ((GuiWorkspace)parent.workspace).windowModelTree.modelList.trees)
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
        }
        GlStateManager.popMatrix();

        //Timeline
        RendererHelper.startGlScissor(getPosX() + 101, getPosY() - 1, width - (hasScrollVert ? 111 : 101), height + 3);

        if(Mouse.isButtonDown(0) && mouseInBoundary(mouseX, mouseY))
        {
            if(mouseX > posX + 100 && mouseX < posX + (hasScrollVert ? width - 10 : width) && mouseY < posY + height - 10)
            {
                double tickPos = (int)(mouseX - (posX + 100 - 1) + (hasScrollHori ? ((double)(((timeWidth + 20) * tickWidth) - (width - (hasScrollVert ? 111 : 101))) * sliderProgHori) : 0));
                setCurrentPos((int)Math.max(0, tickPos / (double)tickWidth));
                if(currentAnim != null && currentAnim.playing)
                {
                    currentAnim.playTime = currentPos;
                }
            }
        }

        if(hasScrollHori)
        {
            int x2 = getPosY() + height - 10;

            int timelineWidth = hasScrollVert ? (width - 10) : width;

            RendererHelper.drawColourOnScreen(parent.workspace.currentTheme.elementTreeScrollBarBorder[0], parent.workspace.currentTheme.elementTreeScrollBarBorder[1], parent.workspace.currentTheme.elementTreeScrollBarBorder[2], 255, getPosX() + 101 + ((timelineWidth - 101) / 40), x2 + 4, (timelineWidth - 101) - (((timelineWidth - 101) / 40) * 2), 2, 0);

            RendererHelper.drawColourOnScreen(parent.workspace.currentTheme.elementTreeScrollBarBorder[0], parent.workspace.currentTheme.elementTreeScrollBarBorder[1], parent.workspace.currentTheme.elementTreeScrollBarBorder[2], 255, getPosX() + 101 + (((timelineWidth - 101) - ((timelineWidth - 101) / 11)) * sliderProgHori), x2, Math.floor((float)(timelineWidth - 101) / 11D), 10, 0);
            RendererHelper.drawColourOnScreen(parent.workspace.currentTheme.elementTreeScrollBar[0], parent.workspace.currentTheme.elementTreeScrollBar[1], parent.workspace.currentTheme.elementTreeScrollBar[2], 255, getPosX() + 101 + 1 + (((timelineWidth - 101) - ((timelineWidth - 101) / 11)) * sliderProgHori), x2 + 1, Math.floor(((float)(timelineWidth - 101) / 11D) - 2), 8, 0);

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

        GlStateManager.pushMatrix();
        GlStateManager.translate(-((((timeWidth + 20) * tickWidth) - (hasScrollVert ? width - 111 : width - 101)) * sliderProgHori), 0D, 0D);

        int tick = 0;
        int timeOffX = 0;
        while(getPosX() + 100 + timeOffX < parent.posX + parent.width || timeOffX < (timeWidth + 20) * tickWidth)
        {
            if(tick % 5 == 0)
            {
                RendererHelper.drawColourOnScreen(parent.workspace.currentTheme.elementTreeItemBorder[0], parent.workspace.currentTheme.elementTreeItemBorder[1], parent.workspace.currentTheme.elementTreeItemBorder[2], 255, getPosX() + 100.5D + timeOffX, getPosY(), 1, height - 19, 0);
                RendererHelper.drawColourOnScreen(parent.workspace.currentTheme.elementTreeScrollBarBorder[0], parent.workspace.currentTheme.elementTreeScrollBarBorder[1], parent.workspace.currentTheme.elementTreeScrollBarBorder[2], 255, getPosX() + 100 + timeOffX, getPosY() + height - 19, 2, 7, 0);
                GlStateManager.pushMatrix();
                float scale = 0.5F;
                GlStateManager.scale(scale, scale, scale);
                parent.workspace.getFontRenderer().drawString(Integer.toString(tick), (int)((getPosX() + 103 + timeOffX) / scale), (int)((getPosY() + height - 16) / scale), Theme.getAsHex(tick == currentPos ? parent.workspace.currentTheme.font : parent.workspace.currentTheme.fontDim), false);
                GlStateManager.popMatrix();
            }
            else
            {
                RendererHelper.drawColourOnScreen(parent.workspace.currentTheme.elementTreeScrollBarBorder[0], parent.workspace.currentTheme.elementTreeScrollBarBorder[1], parent.workspace.currentTheme.elementTreeScrollBarBorder[2], 255, getPosX() + 100 + timeOffX, getPosY() + height - 19, 2, 2, 0);
            }
            tick++;
            timeOffX += tickWidth;
        }

        RendererHelper.startGlScissor(getPosX() + 101, getPosY(), width - (hasScrollVert ? 111 : 101), height - 20);

        GlStateManager.pushMatrix();
        GlStateManager.translate(0D, (double)-((size - (height - 20)) * sliderProgVert), 0D);

        //Animation Component areas
        if(currentAnim != null)
        {
            final int spacingY = 13;

            int idClicked = -1;
            if(mouseX < posX + 100 && mouseX >= posX && mouseY >= posY && mouseY < posY + height)
            {
                idClicked = (int)(mouseY - posY + ((size - (height - 20)) * sliderProgVert)) / 13; //spacing = 13
            }

            int idHovered = 0;
            int offY = 0;
            for(Map.Entry<String, ArrayList<AnimationComponent>> e : currentAnim.sets.entrySet())
            {
                idHovered++;
                offY += spacingY;
                for(AnimationComponent comp : e.getValue())
                {
                    //draw stuff
                    int[] lineClr = parent.workspace.currentTheme.elementTreeItemBg;
                    if(comp.identifier.equals(selectedIdentifier))
                    {
                        lineClr = parent.workspace.currentTheme.elementTreeItemBgSelect;
                    }
                    else if(idClicked == idHovered)
                    {
                        lineClr = parent.workspace.currentTheme.elementTreeItemBgHover;
                    }

                    RendererHelper.drawColourOnScreen(lineClr[0], lineClr[1], lineClr[2], 255, getPosX() + 99D + (comp.startKey * tickWidth), getPosY() + offY + 4.5D, 4, 4, 0);
                    RendererHelper.drawColourOnScreen(lineClr[0], lineClr[1], lineClr[2], 255, getPosX() + 99D + ((comp.startKey + comp.length) * tickWidth), getPosY() + offY + 4.5D, 4, 4, 0);

                    RendererHelper.drawColourOnScreen(lineClr[0], lineClr[1], lineClr[2], 255, getPosX() + 99D + (comp.startKey * tickWidth), getPosY() + offY + 5.5D, ((comp.length) * tickWidth), 2, 0);

                    idHovered++;
                    offY += spacingY;
                }
            }
        }
        GlStateManager.popMatrix();

        RendererHelper.startGlScissor(getPosX() + 101, getPosY() - 1, width - (hasScrollVert ? 111 : 101), height + 3);

        GlStateManager.pushMatrix();
        if(currentAnim != null)
        {
            if(currentAnim.playing)
            {
                setCurrentPos(currentAnim.playTime);
                focusOnTicker();
                if(currentPos < currentAnim.getLength())
                {
                    GlStateManager.translate(tickWidth + ((GuiWorkspace)parent.workspace).renderTick, 0F, 0F);
                }
            }
            else
            {
                currentAnim.playTime = currentPos;
            }
        }
        //Timeline cursor
        RendererHelper.drawColourOnScreen(parent.workspace.currentTheme.tabBorder[0], parent.workspace.currentTheme.tabBorder[1], parent.workspace.currentTheme.tabBorder[2], 255, getPosX() + 100.5D + (currentPos * tickWidth), getPosY(), 1, height - 19, 0);
        RendererHelper.drawColourOnScreen(parent.workspace.currentTheme.tabBorder[0], parent.workspace.currentTheme.tabBorder[1], parent.workspace.currentTheme.tabBorder[2], 255, getPosX() + 99 + (currentPos * tickWidth), getPosY() + height - 19, 4, 1.5D, 0);

        if(currentPos % 5 != 0)
        {
            RendererHelper.drawColourOnScreen(parent.workspace.currentTheme.elementTreeItemBorder[0], parent.workspace.currentTheme.elementTreeItemBorder[1], parent.workspace.currentTheme.elementTreeItemBorder[2], 255, (getPosX() + 100 + (currentPos * tickWidth)) - (parent.workspace.getFontRenderer().getStringWidth(Integer.toString(currentPos)) / 2) * 0.5F, getPosY() + height - 17, parent.workspace.getFontRenderer().getStringWidth(Integer.toString(currentPos)) / 2 + 2, 5, 0);//blocks underlying number
            GlStateManager.pushMatrix();
            float scale = 0.5F;
            GlStateManager.scale(scale, scale, scale);
            parent.workspace.getFontRenderer().drawString(Integer.toString(currentPos), (int)((getPosX() + 101 + (currentPos * tickWidth)) / scale) - parent.workspace.getFontRenderer().getStringWidth(Integer.toString(currentPos)) / 2, (int)((getPosY() + height - 16) / scale), Theme.getAsHex(parent.workspace.currentTheme.font), false);
            GlStateManager.popMatrix();
        }

        GlStateManager.popMatrix();

        GlStateManager.popMatrix();

        RendererHelper.startGlScissor(getPosX() + 101, getPosY() - 1, width - 101, height + 3); //vert scroll bar

        if(hasScrollVert)
        {
            int x2 = getPosX() + width - 10;

            RendererHelper.drawColourOnScreen(parent.workspace.currentTheme.elementTreeItemBorder[0], parent.workspace.currentTheme.elementTreeItemBorder[1], parent.workspace.currentTheme.elementTreeItemBorder[2], 255, x2, getPosY(), 10, height - 19, 0);

            RendererHelper.drawColourOnScreen(parent.workspace.currentTheme.elementTreeScrollBarBorder[0], parent.workspace.currentTheme.elementTreeScrollBarBorder[1], parent.workspace.currentTheme.elementTreeScrollBarBorder[2], 255, x2 + 4, getPosY() + ((height - 20) / 40), 2, (height - 20) - (((height - 20) / 40) * 2), 0);

            RendererHelper.drawColourOnScreen(parent.workspace.currentTheme.elementTreeScrollBarBorder[0], parent.workspace.currentTheme.elementTreeScrollBarBorder[1], parent.workspace.currentTheme.elementTreeScrollBarBorder[2], 255, x2, getPosY() + (((height - 20) - ((height - 20) / 11)) * sliderProgVert), 10, Math.ceil((float)(height - 20) / 10D), 0);
            RendererHelper.drawColourOnScreen(parent.workspace.currentTheme.elementTreeScrollBar[0], parent.workspace.currentTheme.elementTreeScrollBar[1], parent.workspace.currentTheme.elementTreeScrollBar[2], 255, x2 + 1, getPosY() + 1 + (((height - 20) - ((height - 20) / 11)) * sliderProgVert), 8, Math.ceil(((float)(height - 20) / 10D) - 2), 0);

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
        RendererHelper.drawColourOnScreen(parent.workspace.currentTheme.elementTreeItemBorder[0], parent.workspace.currentTheme.elementTreeItemBorder[1], parent.workspace.currentTheme.elementTreeItemBorder[2], 255, getPosX(), getPosY() + offY, 100, 13, 0);

        if(isSelected)
        {
            RendererHelper.drawColourOnScreen(parent.workspace.currentTheme.elementTreeItemBgSelect[0], parent.workspace.currentTheme.elementTreeItemBgSelect[1], parent.workspace.currentTheme.elementTreeItemBgSelect[2], 255, getPosX()+ 1, getPosY() + offY + 1, 100 - 2, 13 - 2, 0);
        }
        else if(hover)
        {
            RendererHelper.drawColourOnScreen(parent.workspace.currentTheme.elementTreeItemBgHover[0], parent.workspace.currentTheme.elementTreeItemBgHover[1], parent.workspace.currentTheme.elementTreeItemBgHover[2], 255, getPosX() + 1, getPosY() + offY + 1, 100 - 2, 13 - 2, 0);
        }
        else
        {
            RendererHelper.drawColourOnScreen(parent.workspace.currentTheme.elementTreeItemBg[0], parent.workspace.currentTheme.elementTreeItemBg[1], parent.workspace.currentTheme.elementTreeItemBg[2], 255, getPosX() + 1, getPosY() + offY + 1, 100 - 2, 13 - 2, 0);
        }
        if(isHidden)
        {
            parent.workspace.getFontRenderer().drawString(reString(name, 100), getPosX() + 4, getPosY() + offY + 2, Theme.getAsHex(parent.workspace.currentTheme.fontDim), false);
        }
        else
        {
            parent.workspace.getFontRenderer().drawString(reString(name, 100), getPosX() + 4, getPosY() + offY + 2, Theme.getAsHex(parent.workspace.currentTheme.font), false);
        }
    }

    public void focusOnTicker()
    {
        int totalWidth = 0;
        boolean hasScrollVert = false;
        for(ElementListTree.Tree tree : ((GuiWorkspace)parent.workspace).windowAnimate.animList.trees)
        {
            if(tree.selected)
            {
                Animation currentAnim = (Animation)tree.attachedObject;

                final int spacingY = 13;
                int offY = 0;
                for(Map.Entry<String, ArrayList<AnimationComponent>> e : currentAnim.sets.entrySet())
                {
                    offY += spacingY;

                    for(AnimationComponent comp : e.getValue())
                    {
                        offY += spacingY;
                    }
                }

                totalWidth = (currentAnim.getLength() + 20) * tickWidth;
                hasScrollVert = offY > height - 20;

                break;
            }
        }

        int elementWidth = width - (hasScrollVert ? 111 : 101);
        int tickerPos = currentPos * tickWidth;

        if(tickerPos < elementWidth)
        {
            sliderProgHori = 0.0D;
        }
        else
        {
            int hiddenWidth = totalWidth - elementWidth;
            if(tickerPos > elementWidth + sliderProgHori * hiddenWidth || tickerPos < hiddenWidth * sliderProgHori)
            {
                sliderProgHori = MathHelper.clamp_double((tickerPos - (elementWidth / 3D)) / (double)hiddenWidth, 0.0D, 1.0D);
            }
        }
    }

    @Override
    public boolean onClick(int mouseX, int mouseY, int id)
    {
        if(mouseX < posX + 100)
        {
            //get total animation element height
            int size = 0;

            for(ElementListTree.Tree tree : ((GuiWorkspace)parent.workspace).windowAnimate.animList.trees)
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

            for(ElementListTree.Tree tree : ((GuiWorkspace)parent.workspace).windowAnimate.animList.trees)
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
                                    for(ElementListTree.Tree tree1 : ((GuiWorkspace)parent.workspace).windowModelTree.modelList.trees)
                                    {
                                        if(tree1.attachedObject instanceof CubeInfo && ((CubeInfo)tree1.attachedObject).identifier.equals(e.getKey()))
                                        {
                                            ((GuiWorkspace)parent.workspace).windowModelTree.modelList.selectedIdentifier = e.getKey();

                                            ((GuiWorkspace)parent.workspace).windowControls.selectedObject = tree1.attachedObject;
                                            ((GuiWorkspace)parent.workspace).windowControls.refresh = true;

                                            tree1.selected = true;
                                        }
                                        else
                                        {
                                            tree1.selected = false;
                                        }
                                    }

                                    if(!anim.playing)
                                    {
                                        setCurrentPos(comp.startKey);
                                        focusOnTicker();
                                    }
                                }
                                else if(id == 1)
                                {
                                    if(!((GuiWorkspace)parent.workspace).remoteSession)
                                    {
                                        Tabula.proxy.tickHandlerClient.mainframe.toggleAnimComponentVisibility(((GuiWorkspace)parent.workspace).projectManager.projects.get(((GuiWorkspace)parent.workspace).projectManager.selectedProject).identifier, anim.identifier, comp.identifier);
                                    }
                                    else if(!((GuiWorkspace)parent.workspace).sessionEnded && ((GuiWorkspace)parent.workspace).isEditor)
                                    {
                                        Tabula.channel.sendToServer(new PacketGenericMethod(((GuiWorkspace)parent.workspace).host, "toggleAnimComponentVisibility", ((GuiWorkspace)parent.workspace).projectManager.projects.get(((GuiWorkspace)parent.workspace).projectManager.selectedProject).identifier, anim.identifier, comp.identifier));
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
        for(ElementListTree.Tree tree : ((GuiWorkspace)parent.workspace).windowAnimate.animList.trees)
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

    public void setCurrentPos(int i)
    {
        currentPos = i;
        ((GuiWorkspace)parent.workspace).windowControls.refresh = true;
    }

    public int getCurrentPos()
    {
        return currentPos;
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
    public String tooltip()
    {
        int timeWidth = 0;


        for(ElementListTree.Tree tree : ((GuiWorkspace)parent.workspace).windowAnimate.animList.trees)
        {
            if(tree.selected)
            {
                Animation currentAnim = (Animation)tree.attachedObject;

                final int spacingY = 13;
                int offY = 0;
                for(Map.Entry<String, ArrayList<AnimationComponent>> e : currentAnim.sets.entrySet())
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

                boolean hasScrollVert = offY > height - 20;
                boolean hasScrollHori = timeWidth + 20 > Math.floor((float)(width - 101) / (float)tickWidth);

                if(mX > posX + 100 && mX < posX + (hasScrollVert ? width - 10 : width) && mY < posY + height - 10)
                {
                    double tickPos = (int)(mX - (posX + 100 - 1) + (hasScrollHori ? ((double)(((timeWidth + 20) * tickWidth) - (width - (hasScrollVert ? 111 : 101))) * sliderProgHori) : 0));
                    int mousePos = (int)Math.max(0, tickPos / (double)tickWidth);

                    int idClicked = (int)(mY - posY + ((offY - (height - 20)) * sliderProgVert)) / 13;

                    int idHovered = 0;

                    for(Map.Entry<String, ArrayList<AnimationComponent>> e : currentAnim.sets.entrySet())
                    {
                        idHovered++;

                        for(AnimationComponent comp : e.getValue())
                        {
                            if(idClicked == idHovered)
                            {
                                if(comp.startKey == mousePos)
                                {
                                    StringBuilder sb = new StringBuilder();
                                    sb.append(StatCollector.translateToLocal("window.editAnimCompProg.animInfo") + "\n");
                                    sb.append(StatCollector.translateToLocal("window.controls.position") + ": ");
                                    sb.append(String.format(Locale.ENGLISH, "%.2f", comp.posOffset[0]) + ", " + String.format(Locale.ENGLISH, "%.2f", comp.posOffset[1]) + ", " + String.format(Locale.ENGLISH, "%.2f", comp.posOffset[2]) + "\n");
                                    sb.append(StatCollector.translateToLocal("window.controls.rotation") + ": ");
                                    sb.append(String.format(Locale.ENGLISH, "%.2f", comp.rotOffset[0]) + ", " + String.format(Locale.ENGLISH, "%.2f", comp.rotOffset[1]) + ", " + String.format(Locale.ENGLISH, "%.2f", comp.rotOffset[2]) + "\n");
                                    sb.append(StatCollector.translateToLocal("window.controls.scale") + ": ");
                                    sb.append(String.format(Locale.ENGLISH, "%.2f", comp.scaleOffset[0]) + ", " + String.format(Locale.ENGLISH, "%.2f", comp.scaleOffset[1]) + ", " + String.format(Locale.ENGLISH, "%.2f", comp.scaleOffset[2]) + "\n");
                                    sb.append(StatCollector.translateToLocal("window.controls.opacity") + ": ");
                                    sb.append(String.format(Locale.ENGLISH, "%.2f", comp.opacityOffset));
                                    return sb.toString();
                                }
                                else if(comp.startKey + comp.length == mousePos)
                                {
                                    StringBuilder sb = new StringBuilder();
                                    sb.append(StatCollector.translateToLocal("window.editAnimCompProg.animInfo") + "\n");
                                    sb.append(StatCollector.translateToLocal("window.controls.position") + ": ");
                                    sb.append(String.format(Locale.ENGLISH, "%.2f", comp.posChange[0]) + ", " + String.format(Locale.ENGLISH, "%.2f", comp.posChange[1]) + ", " + String.format(Locale.ENGLISH, "%.2f", comp.posChange[2]) + "\n");
                                    sb.append(StatCollector.translateToLocal("window.controls.rotation") + ": ");
                                    sb.append(String.format(Locale.ENGLISH, "%.2f", comp.rotChange[0]) + ", " + String.format(Locale.ENGLISH, "%.2f", comp.rotChange[1]) + ", " + String.format(Locale.ENGLISH, "%.2f", comp.rotChange[2]) + "\n");
                                    sb.append(StatCollector.translateToLocal("window.controls.scale") + ": ");
                                    sb.append(String.format(Locale.ENGLISH, "%.2f", comp.scaleChange[0]) + ", " + String.format(Locale.ENGLISH, "%.2f", comp.scaleChange[1]) + ", " + String.format(Locale.ENGLISH, "%.2f", comp.scaleChange[2]) + "\n");
                                    sb.append(StatCollector.translateToLocal("window.controls.opacity") + ": ");
                                    sb.append(String.format(Locale.ENGLISH, "%.2f", comp.opacityChange));
                                    return sb.toString();
                                }
                            }
                            idHovered++;
                        }
                    }

                }

                break;
            }
        }

        return null;
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
