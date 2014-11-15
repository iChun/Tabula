package us.ichun.mods.tabula.gui.window.element;

import ichun.client.render.RendererHelper;
import net.minecraft.util.MathHelper;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;
import us.ichun.mods.tabula.gui.Theme;
import us.ichun.mods.tabula.gui.window.Window;

import java.util.ArrayList;

public class ElementListTree extends Element
{
    public int spacerL;
    public int spacerR;
    public int spacerU;
    public int spacerD;

    public double sliderProg = 0.0D;

    public ArrayList<Tree> trees = new ArrayList<Tree>();

    public ElementListTree(Window window, int x, int y, int w, int h, int ID, boolean igMin)
    {
        super(window, x, y, w, h, ID, igMin);
        spacerL = x;
        spacerR = parent.width - x - width;
        spacerU = y;
        spacerD = parent.height - y - height;
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

            tree.draw(mouseX, mouseY, hover, (x2 - x1), treeHeight, 0);

            treeHeight += tree.getHeight();
        }
        GL11.glPopMatrix();

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

    public void createTree(Tree parent, ResourceLocation loc, Object obj, int h, boolean canexp)
    {
        trees.add(new Tree(parent, loc, obj, h, canexp));
    }

    public class Tree
    {
        public ArrayList<Tree> branches = new ArrayList<Tree>();

        public Tree root;

        public ResourceLocation txLoc;

        public Object attachedObject;

        private int height;

        public boolean canExpand;
        public boolean expanded;

        public boolean selected;

        public Tree(Tree parent, ResourceLocation loc, Object obj, int h, boolean canexp)
        {
            if(parent != null)
            {
                root = parent;
                parent.branches.add(this);
            }
            txLoc = loc;
            attachedObject = obj;
            height = h;
            canExpand = canexp;
        }

        public int getHeight()
        {
            int branchHeight = 0;
            for(int i = 0; i < branches.size(); i++)
            {
                branchHeight += branches.get(i).getHeight();
            }
            return height + branchHeight;
        }

        public void draw(int mouseX, int mouseY, boolean hover, int width, int treeHeight, int prevHeight)
        {
            RendererHelper.drawColourOnScreen(Theme.elementTreeItemBorder[0], Theme.elementTreeItemBorder[1], Theme.elementTreeItemBorder[2], 255, getPosX(), getPosY() + treeHeight + prevHeight, width, height, 0);
            //TODO hover, select, nonselected
            RendererHelper.drawColourOnScreen(Theme.elementTreeItemBg[0], Theme.elementTreeItemBg[1], Theme.elementTreeItemBg[2], 255, getPosX() + 1, getPosY() + treeHeight + prevHeight + 1, width - 2, height - 2, 0);

            for(int i = 0; i < branches.size(); i++)
            {
                branches.get(i).draw(mouseX, mouseY, hover, width, treeHeight, prevHeight + height);
            }
        }
    }
}
