package us.ichun.mods.tabula.client.gui.window.element;

import ichun.client.render.RendererHelper;
import ichun.common.core.util.MD5Checksum;
import net.minecraft.util.MathHelper;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;
import us.ichun.mods.tabula.client.gui.Theme;
import us.ichun.mods.tabula.client.gui.window.Window;
import us.ichun.module.tabula.client.model.ModelInfo;
import us.ichun.module.tabula.common.project.components.CubeInfo;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class ElementListTree extends Element
{
    public int spacerL;
    public int spacerR;
    public int spacerU;
    public int spacerD;

    public double sliderProg = 0.0D;

    public ArrayList<Tree> trees = new ArrayList<Tree>();

    public boolean canDrag;

    public Tree treeDragged;
    public int dragX;
    public int dragY;

    public boolean lmbDown;

    public String selectedIdentifier;

    public ElementListTree(Window window, int x, int y, int w, int h, int ID, boolean igMin, boolean drag)
    {
        super(window, x, y, w, h, ID, igMin);
        spacerL = x;
        spacerR = parent.width - x - width;
        spacerU = y;
        spacerD = parent.height - y - height;
        selectedIdentifier = "";
        canDrag = drag;
    }

    @Override
    public void draw(int mouseX, int mouseY, boolean hover)
    {
        int x1 = getPosX();
        int x2 = getPosX() + width;
        int y1 = getPosY();
        int y2 = getPosY() + height;

        int treeHeight1 = 0;
        for(int i = 0; i < trees.size(); i++)
        {
            Tree tree = trees.get(i);
            treeHeight1 += tree.getHeight();
        }

        RendererHelper.endGlScissor();

        RendererHelper.startGlScissor(getPosX(), getPosY(), width + 2, height + 2);

        if(treeHeight1 > height)
        {
            x2 -= 10;

            RendererHelper.drawColourOnScreen(Theme.elementTreeScrollBarBorder[0], Theme.elementTreeScrollBarBorder[1], Theme.elementTreeScrollBarBorder[2], 255, x2 + 5, getPosY() + (height / 40), 2, height - ((height / 40) * 2), 0);

            RendererHelper.drawColourOnScreen(Theme.elementTreeScrollBarBorder[0], Theme.elementTreeScrollBarBorder[1], Theme.elementTreeScrollBarBorder[2], 255, x2 + 1, getPosY() - 1 + ((height - (height / 11)) * sliderProg), 10, height / 10, 0);
            RendererHelper.drawColourOnScreen(Theme.elementTreeScrollBar[0], Theme.elementTreeScrollBar[1], Theme.elementTreeScrollBar[2], 255, x2 + 2, getPosY() + ((height - (height / 11)) * sliderProg), 8, (height / 10) - 2, 0);

            int sbx1 = x2 + 1 - parent.posX;
            int sbx2 = sbx1 + 10;
            int sby1 = getPosY() - 1 - parent.posY;
            int sby2 = getPosY() + height - parent.posY;

            if(Mouse.isButtonDown(0) && mouseX >= sbx1 && mouseX <= sbx2 && mouseY >= sby1 && mouseY <= sby2)
            {
                sby1 += 10;
                sby2 -= 10;
                sliderProg = 1.0F - MathHelper.clamp_double((double)(sby2 - mouseY) / (double)(sby2 - sby1), 0.0D, 1.0D);
            }
        }

        GL11.glPushMatrix();
        GL11.glTranslated(0D, (double)-((treeHeight1 - height) * sliderProg), 0D);
        int treeHeight = 0;
        for(int i = 0; i < trees.size(); i++)
        {
            Tree tree = trees.get(i);

            tree.draw(mouseX, mouseY, hover, (x2 - x1), treeHeight, treeHeight1 > height, treeHeight1, Mouse.isButtonDown(0) && !lmbDown);

            treeHeight += tree.getHeight();
        }
        GL11.glPopMatrix();

        RendererHelper.endGlScissor();

        if(treeDragged != null && !(dragX == mouseX && dragY == mouseY))
        {
            treeHeight = 0;
            for(int i = 0; i < trees.size(); i++)
            {
                Tree tree = trees.get(i);

                if(tree == treeDragged)
                {
                    break;
                }

                treeHeight += tree.getHeight();
            }
            treeDragged.dragDraw = true;
            treeDragged.draw(mouseX, mouseY, hover, (x2 - x1), treeHeight, treeHeight1 > height, treeHeight1, Mouse.isButtonDown(0) && !lmbDown);
            treeDragged.dragDraw = false;
        }

        if(parent.isTab)
        {
            RendererHelper.startGlScissor(parent.posX + 1, parent.posY + 1 + 12, parent.getWidth() - 2, parent.getHeight() - 2 - 12);
        }
        else
        {
            RendererHelper.startGlScissor(parent.posX + 1, parent.posY + 1, parent.getWidth() - 2, parent.getHeight() - 2);
        }

        if(parent.docked < 0)
        {
            RendererHelper.drawColourOnScreen(Theme.elementTreeBorder[0], Theme.elementTreeBorder[1], Theme.elementTreeBorder[2], 255, x1 - 1, y1 - 1, (x2 - x1) + 1, 1, 0);
            RendererHelper.drawColourOnScreen(Theme.elementTreeBorder[0], Theme.elementTreeBorder[1], Theme.elementTreeBorder[2], 255, x1 - 1, y1 - 1, 1, height + 2, 0);
            RendererHelper.drawColourOnScreen(Theme.elementTreeBorder[0], Theme.elementTreeBorder[1], Theme.elementTreeBorder[2], 255, x1 - 1, y2 + 1, (x2 - x1) + 2, 1, 0);
            RendererHelper.drawColourOnScreen(Theme.elementTreeBorder[0], Theme.elementTreeBorder[1], Theme.elementTreeBorder[2], 255, x2, y1 - 1, 1, height + 2, 0);
        }

        if(!Mouse.isButtonDown(0) && lmbDown)
        {
            treeDragged = null;
        }

        lmbDown = Mouse.isButtonDown(0);
    }

    @Override
    public void resized()
    {
        posX = spacerL;
        width = parent.width - posX - spacerR;
        posY = spacerU;
        height = parent.height - posY - spacerD;
    }

    @Override
    public boolean mouseScroll(int mouseX, int mouseY, int k)
    {
        int treeHeight1 = 0;
        for(int i = 0; i < trees.size(); i++)
        {
            Tree tree = trees.get(i);
            treeHeight1 += tree.getHeight();
        }

        if(treeHeight1 > height)
        {
            sliderProg += 0.05D * -k;
            sliderProg = MathHelper.clamp_double(sliderProg, 0.0D, 1.0D);
        }
        return false;//return true to say you're interacted with
    }

    @Override
    public String tooltip()
    {
        return null; //return null for no tooltip. This is localized.
    }

    public void createTree(ResourceLocation loc, Object obj, int h, int attach, boolean expandable, boolean collapse)
    {
        trees.add(new Tree(loc, obj, h, attach, expandable, collapse));
    }

    public void clickElement(Object obj)
    {
        if(obj instanceof CubeInfo)
        {
            CubeInfo info = (CubeInfo)obj;
            selectedIdentifier = info.identifier;

            parent.workspace.windowControls.selectedObject = info;
            parent.workspace.windowControls.refresh = true;
        }
    }

    public class Tree
    {
        public ResourceLocation txLoc;

        public Object attachedObject;

        private int theHeight;

        public boolean canExpand;
        public boolean collapsed;

        public int attached;// attachment level

        public boolean selected;

        public boolean dragDraw;

        public Tree(ResourceLocation loc, Object obj, int h, int attach, boolean expandable, boolean collapse)
        {
            txLoc = loc;
            attachedObject = obj;
            theHeight = h;
            attached = attach;
            canExpand = expandable;
            collapsed = collapse;
        }

        public int getHeight()
        {
            //TODO get parents and see if they're expanded
            return theHeight;
        }

        public Tree draw(int mouseX, int mouseY, boolean hover, int width, int treeHeight, boolean hasScroll, int totalHeight, boolean clicking)
        {
            if(!(treeDragged == this && !(dragX == mouseX && dragY == mouseY)) || dragDraw)
            {
                double scrollHeight = 0.0D;
                if(hasScroll)
                {
                    scrollHeight = (height - totalHeight) * sliderProg;
                }
                boolean realBorder = mouseX >= posX && mouseX < posX + width && mouseY >= posY + treeHeight + scrollHeight && mouseY < posY + treeHeight + scrollHeight + theHeight;
                int offX = 0;
                int offY = 0;
                if(dragDraw)
                {
                    offX = mouseX - dragX;
                    offY = mouseY - dragY;
                }
                RendererHelper.drawColourOnScreen(Theme.elementTreeItemBorder[0], Theme.elementTreeItemBorder[1], Theme.elementTreeItemBorder[2], 255, getPosX() + offX, getPosY() + offY + treeHeight, width, theHeight, 0);
                if(selected)
                {
                    RendererHelper.drawColourOnScreen(Theme.elementTreeItemBgSelect[0], Theme.elementTreeItemBgSelect[1], Theme.elementTreeItemBgSelect[2], 255, getPosX() + offX + 1, getPosY() + offY + treeHeight + 1, width - 2, theHeight - 2, 0);
                }
                else if(realBorder)
                {
                    RendererHelper.drawColourOnScreen(Theme.elementTreeItemBgHover[0], Theme.elementTreeItemBgHover[1], Theme.elementTreeItemBgHover[2], 255, getPosX() + offX + 1, getPosY() + offY + treeHeight + 1, width - 2, theHeight - 2, 0);
                }
                else
                {
                    RendererHelper.drawColourOnScreen(Theme.elementTreeItemBg[0], Theme.elementTreeItemBg[1], Theme.elementTreeItemBg[2], 255, getPosX() + offX + 1, getPosY() + offY + treeHeight + 1, width - 2, theHeight - 2, 0);
                }

                if(realBorder && hasScroll)
                {
                    if(mouseY > height + 5 - scrollHeight || mouseY <= scrollHeight)
                    {
                        clicking = false;
                    }
                }

                if(attachedObject instanceof CubeInfo)
                {
                    CubeInfo info = (CubeInfo)attachedObject;
                    parent.workspace.getFontRenderer().drawString(info.name, getPosX() + offX + 4, getPosY() + offY + ((theHeight - parent.workspace.getFontRenderer().FONT_HEIGHT) / 2) + treeHeight, Theme.getAsHex(Theme.font), false);
                }
                else if(attachedObject instanceof ModelInfo)
                {
                    ModelInfo info = (ModelInfo)attachedObject;
                    parent.workspace.getFontRenderer().drawString(info.modelParent.getClass().getSimpleName() + " - " + info.clz.getSimpleName(), getPosX() + offX + 4, getPosY() + offY + ((theHeight - parent.workspace.getFontRenderer().FONT_HEIGHT) / 2) + treeHeight, Theme.getAsHex(Theme.font), false);
                }
                else if(attachedObject instanceof File)
                {
                    File info = (File)attachedObject;
                    parent.workspace.getFontRenderer().drawString(info.getName(), getPosX() + offX + 4, getPosY() + offY + 3 + treeHeight, Theme.getAsHex(Theme.font), false);
                    parent.workspace.getFontRenderer().drawString((new SimpleDateFormat()).format(new Date(info.lastModified())), getPosX() + offX + 4, getPosY() + offY + 14 + treeHeight, Theme.getAsHex(Theme.font), false);
                    parent.workspace.getFontRenderer().drawString(MD5Checksum.readableFileSize(info.length()), getPosX() + offX + width - 4 - parent.workspace.getFontRenderer().getStringWidth(MD5Checksum.readableFileSize(info.length())), getPosY() + offY + 3 + treeHeight, Theme.getAsHex(Theme.font), false);
                }

                if(realBorder && clicking)
                {
                    //TODO action when selected
                    selected = true;
                    deselectOthers(trees);
                    clickElement(attachedObject);

                    if(canDrag)
                    {
                        treeDragged = this;
                        dragX = mouseX;
                        dragY = mouseY;
                    }
                }
            }
            return null;
        }

        public void deselectOthers(ArrayList<Tree> trees)
        {
            for(Tree tree : trees)
            {
                if(tree != this && tree.selected)
                {
                    tree.selected = false;
                }
            }
        }
    }
}
