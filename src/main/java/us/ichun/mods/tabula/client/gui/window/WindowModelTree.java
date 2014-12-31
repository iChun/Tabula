package us.ichun.mods.tabula.client.gui.window;

import ichun.client.render.RendererHelper;
import ichun.common.core.network.PacketHandler;
import net.minecraft.util.ResourceLocation;
import us.ichun.mods.tabula.client.gui.GuiWorkspace;
import us.ichun.mods.tabula.client.gui.Theme;
import us.ichun.mods.tabula.client.gui.window.element.Element;
import us.ichun.mods.tabula.client.gui.window.element.ElementButtonTextured;
import us.ichun.mods.tabula.client.gui.window.element.ElementListTree;
import us.ichun.mods.tabula.common.Tabula;
import us.ichun.mods.tabula.common.packet.PacketGenericMethod;

public class WindowModelTree extends Window
{
    public ElementListTree modelList;

    public WindowModelTree(GuiWorkspace parent, int x, int y, int w, int h, int minW, int minH)
    {
        super(parent, x, y, w, h, minW, minH, "window.modelTree.title", true);

        elements.add(new ElementButtonTextured(this, BORDER_SIZE + 00, height - 20 - BORDER_SIZE, 0, false, 0, 1, "window.modelTree.newCube", new ResourceLocation("tabula", "textures/icon/newCube.png")));
        elements.add(new ElementButtonTextured(this, BORDER_SIZE + 20, height - 20 - BORDER_SIZE, 1, false, 0, 1, "window.modelTree.newGroup", new ResourceLocation("tabula", "textures/icon/newGroup.png")));
        elements.add(new ElementButtonTextured(this, BORDER_SIZE + 40, height - 20 - BORDER_SIZE, 2, false, 0, 1, "window.modelTree.delete", new ResourceLocation("tabula", "textures/icon/delete.png")));
        modelList = new ElementListTree(this, BORDER_SIZE + 1, BORDER_SIZE + 1 + 10, width - (BORDER_SIZE * 2 + 2), height - BORDER_SIZE - 21 - 16, 3, false, true);
        elements.add(modelList);
    }

    @Override
    public void draw(int mouseX, int mouseY) //4 pixel border?
    {
        super.draw(mouseX, mouseY);
        if(!minimized)
        {
            RendererHelper.drawColourOnScreen(Theme.instance.elementButtonBorder[0], Theme.instance.elementButtonBorder[1], Theme.instance.elementButtonBorder[2], 255, posX + BORDER_SIZE, posY + height - 21 - BORDER_SIZE, width - (BORDER_SIZE * 2), 1, 0);
        }
    }

    @Override
    public boolean interactableWhileNoProjects()
    {
        return false;
    }

    @Override
    public void elementTriggered(Element element)
    {
        if(element.id == 0) //newcube
        {
            if(!workspace.remoteSession)
            {
                Tabula.proxy.tickHandlerClient.mainframe.createNewCube(workspace.projectManager.projects.get(workspace.projectManager.selectedProject).identifier);
            }
            else if(!workspace.sessionEnded && workspace.isEditor)
            {
                PacketHandler.sendToServer(Tabula.channels, new PacketGenericMethod(workspace.host, "createNewCube", workspace.projectManager.projects.get(workspace.projectManager.selectedProject).identifier));
            }
        }
        else if(element.id == 1) //newgroup
        {
            if(!workspace.remoteSession)
            {
                Tabula.proxy.tickHandlerClient.mainframe.createNewGroup(workspace.projectManager.projects.get(workspace.projectManager.selectedProject).identifier);
            }
            else if(!workspace.sessionEnded && workspace.isEditor)
            {
                PacketHandler.sendToServer(Tabula.channels, new PacketGenericMethod(workspace.host, "createNewGroup", workspace.projectManager.projects.get(workspace.projectManager.selectedProject).identifier));
            }
        }
        else if(element.id == 2 && !modelList.selectedIdentifier.isEmpty())
        {
            if(!workspace.remoteSession)
            {
                Tabula.proxy.tickHandlerClient.mainframe.deleteObject(workspace.projectManager.projects.get(workspace.projectManager.selectedProject).identifier, modelList.selectedIdentifier);
            }
            else if(!workspace.sessionEnded && workspace.isEditor)
            {
                PacketHandler.sendToServer(Tabula.channels, new PacketGenericMethod(workspace.host, "deleteObject", workspace.projectManager.projects.get(workspace.projectManager.selectedProject).identifier, modelList.selectedIdentifier));
            }
        }
        else
        {
        }
    }
}
