package us.ichun.mods.tabula.client.gui.window;

import net.minecraft.client.Minecraft;
import us.ichun.mods.tabula.client.gui.GuiWorkspace;
import us.ichun.mods.tabula.client.gui.window.element.Element;
import us.ichun.mods.tabula.client.gui.window.element.ElementButton;
import us.ichun.mods.tabula.client.gui.window.element.ElementListTree;
import us.ichun.mods.tabula.common.Tabula;

public class WindowRemoveEditor extends Window
{
    public ElementListTree modelList;

    public WindowRemoveEditor(GuiWorkspace parent, int x, int y, int w, int h, int minW, int minH)
    {
        super(parent, x, y, w, h, minW, minH, "topdock.removeEditor", true);

        elements.add(new ElementButton(this, width - 140, height - 22, 60, 16, 1, false, 1, 1, "element.button.ok"));
        elements.add(new ElementButton(this, width - 70, height - 22, 60, 16, 0, false, 1, 1, "element.button.cancel"));
        modelList = new ElementListTree(this, BORDER_SIZE + 1, BORDER_SIZE + 1 + 10, width - (BORDER_SIZE * 2 + 2), height - BORDER_SIZE - 22 - 16, 3, false, false);
        elements.add(modelList);

        for(String model : Tabula.proxy.tickHandlerClient.mainframe.editors)
        {
            if(!model.equals(Minecraft.getMinecraft().getSession().getUsername()))
            {
                modelList.createTree(null, model, 13, 0, false, false);
            }
        }
    }

    @Override
    public void draw(int mouseX, int mouseY)
    {
        super.draw(mouseX, mouseY);
    }

    @Override
    public void elementTriggered(Element element)
    {
        if(element.id == 0)
        {
            workspace.removeWindow(this, true);
        }
        if(element.id == 1 || element.id == 3)
        {
            boolean found = false;

            if(workspace.windowDragged == this)
            {
                workspace.windowDragged = null;
            }
            for(int i = 0; i < modelList.trees.size(); i++)
            {
                ElementListTree.Tree tree = modelList.trees.get(i);
                if(tree.selected)
                {
                    Tabula.proxy.tickHandlerClient.mainframe.removeEditor((String)tree.attachedObject);
                    found = true;
                    break;
                }
            }

            if(found)
            {
                workspace.removeWindow(this, true);
            }
        }
    }
}
