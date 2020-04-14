package me.ichun.mods.tabula.old.client.gui.window.element;

import com.google.gson.Gson;
import me.ichun.mods.ichunutil.client.gui.window.Window;
import me.ichun.mods.ichunutil.common.module.tabula.project.components.CubeGroup;
import me.ichun.mods.ichunutil.common.module.tabula.project.components.CubeInfo;
import me.ichun.mods.tabula.old.client.gui.GuiWorkspace;
import me.ichun.mods.tabula.old.client.gui.Theme;
import me.ichun.mods.tabula.old.common.Tabula;
import me.ichun.mods.tabula.old.common.packet.PacketGenericMethod;

public class ElementListTree extends me.ichun.mods.ichunutil.client.gui.window.element.ElementListTree
{
    public ElementListTree(Window window, int x, int y, int w, int h, int ID, boolean igMin, boolean drag)
    {
        super(window, x, y, w, h, ID, igMin, drag);
    }

    @Override
    public void dragOnto(Object draggedOn, Object dragged)
    {
        if(draggedOn instanceof CubeInfo && dragged instanceof CubeGroup)
        {
            return; //you can't attach a group to a cube.
        }
        String draggedOnIdent = "";
        if(draggedOn instanceof CubeInfo)
        {
            draggedOnIdent = ((CubeInfo)draggedOn).identifier;
        }
        else if(draggedOn instanceof CubeGroup)
        {
            draggedOnIdent = ((CubeGroup)draggedOn).identifier;
        }
        String draggedIdent = "";
        if(dragged instanceof CubeInfo)
        {
            draggedIdent = ((CubeInfo)dragged).identifier;
        }
        else if(dragged instanceof CubeGroup)
        {
            draggedIdent = ((CubeGroup)dragged).identifier;
        }

        if(!((GuiWorkspace)parent.workspace).remoteSession)
        {
            Tabula.proxy.tickHandlerClient.mainframe.dragOnto(((GuiWorkspace)parent.workspace).projectManager.projects.get(((GuiWorkspace)parent.workspace).projectManager.selectedProject).identifier, draggedOnIdent, draggedIdent);
        }
        else if(!((GuiWorkspace)parent.workspace).sessionEnded && ((GuiWorkspace)parent.workspace).isEditor)
        {
            Tabula.channel.sendToServer(new PacketGenericMethod(((GuiWorkspace)parent.workspace).host, "dragOnto", ((GuiWorkspace)parent.workspace).projectManager.projects.get(((GuiWorkspace)parent.workspace).projectManager.selectedProject).identifier, draggedOnIdent, draggedIdent));
        }
        sliderProg = 0.0F;
    }

    @Override
    public void clickElement(Object obj)
    {
        if(obj instanceof CubeGroup)
        {
            CubeGroup info = (CubeGroup)obj;
            selectedIdentifier = info.identifier;

            ((GuiWorkspace)parent.workspace).windowControls.selectedObject = info;
            ((GuiWorkspace)parent.workspace).windowControls.refresh = true;
        }
        else if(obj instanceof CubeInfo)
        {
            CubeInfo info = (CubeInfo)obj;
            selectedIdentifier = info.identifier;

            ((GuiWorkspace)parent.workspace).windowControls.selectedObject = info;
            ((GuiWorkspace)parent.workspace).windowControls.refresh = true;
        }
        else if(obj instanceof Theme)
        {
            Theme theme = (Theme)obj;
            Theme.loadTheme(parent.workspace.currentTheme, theme);
            if(theme.filename != null)
            {
                Tabula.config.favTheme = (theme.filename);
                Tabula.config.save();
            }
        }
        super.clickElement(obj);
    }

    @Override
    public void rightClickElement(Object obj)
    {
        if(obj instanceof CubeInfo)
        {
            CubeInfo info = (CubeInfo)obj;
            info.hidden = !info.hidden;

            Gson gson = new Gson();
            String s = gson.toJson(info);
            if(!((GuiWorkspace)parent.workspace).remoteSession)
            {
                Tabula.proxy.tickHandlerClient.mainframe.updateCube(((GuiWorkspace)parent.workspace).projectManager.projects.get(((GuiWorkspace)parent.workspace).projectManager.selectedProject).identifier, s, "", "", 0);
            }
            else if(!((GuiWorkspace)parent.workspace).sessionEnded && ((GuiWorkspace)parent.workspace).isEditor)
            {
                Tabula.channel.sendToServer(new PacketGenericMethod(((GuiWorkspace)parent.workspace).host, "updateCube", ((GuiWorkspace)parent.workspace).projectManager.projects.get(((GuiWorkspace)parent.workspace).projectManager.selectedProject).identifier, s, "", "", 0));
            }
        }
        else if(obj instanceof CubeGroup)
        {
            CubeGroup info = (CubeGroup)obj;
            info.hidden = !info.hidden;

            if(!((GuiWorkspace)parent.workspace).remoteSession)
            {
                Tabula.proxy.tickHandlerClient.mainframe.setGroupVisibility(((GuiWorkspace)parent.workspace).projectManager.projects.get(((GuiWorkspace)parent.workspace).projectManager.selectedProject).identifier, info.identifier, info.hidden);
            }
            else if(!((GuiWorkspace)parent.workspace).sessionEnded && ((GuiWorkspace)parent.workspace).isEditor)
            {
                Tabula.channel.sendToServer(new PacketGenericMethod(((GuiWorkspace)parent.workspace).host, "setGroupVisibility", ((GuiWorkspace)parent.workspace).projectManager.projects.get(((GuiWorkspace)parent.workspace).projectManager.selectedProject).identifier, info.identifier, info.hidden));
            }
        }
    }
}
