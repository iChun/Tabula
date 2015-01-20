package us.ichun.mods.tabula.client.gui.window;

import net.minecraft.util.StatCollector;
import us.ichun.mods.ichunutil.client.gui.window.IWorkspace;
import us.ichun.mods.ichunutil.client.gui.window.Window;
import us.ichun.mods.ichunutil.client.gui.window.element.Element;
import us.ichun.mods.ichunutil.client.gui.window.element.ElementButton;
import us.ichun.mods.ichunutil.client.gui.window.element.ElementNumberInput;
import us.ichun.mods.ichunutil.client.gui.window.element.ElementTextInput;
import us.ichun.mods.ichunutil.common.module.tabula.common.project.components.CubeInfo;
import us.ichun.mods.tabula.client.gui.GuiWorkspace;
import us.ichun.mods.tabula.client.gui.Theme;
import us.ichun.mods.tabula.common.Tabula;
import us.ichun.mods.tabula.common.packet.PacketGenericMethod;

public class WindowNewAnimComponent extends Window
{
    public String cubeIdent;

    public WindowNewAnimComponent(IWorkspace parent, int x, int y, int w, int h, int minW, int minH, CubeInfo info)
    {
        super(parent, x, y, w, h, minW, minH, "window.newAnimComp.title", true);

        cubeIdent = info.identifier;
        elements.add(new ElementTextInput(this, 10, 30, width - 20, 12, 4, "window.newAnimComp.name"));
        elements.add(new ElementNumberInput(this, 10, 65, 40, 12, 1, "window.newAnimComp.length", 1, false, 1, (int)Short.MAX_VALUE, 10D));

        elements.add(new ElementButton(this, width - 140, height - 30, 60, 16, 100, false, 1, 1, "element.button.ok"));
        elements.add(new ElementButton(this, width - 70, height - 30, 60, 16, 0, false, 1, 1, "element.button.cancel"));
    }

    @Override
    public void draw(int mouseX, int mouseY)
    {
        super.draw(mouseX, mouseY);
        if(!minimized)
        {
            workspace.getFontRenderer().drawString(StatCollector.translateToLocal("window.newAnimComp.name"), posX + 11, posY + 20, Theme.getAsHex(workspace.currentTheme.font), false);
            workspace.getFontRenderer().drawString(StatCollector.translateToLocal("window.newAnimComp.length"), posX + 11, posY + 55, Theme.getAsHex(workspace.currentTheme.font), false);
        }
    }

    @Override
    public void elementTriggered(Element element)
    {
        if(element.id == 0)
        {
            workspace.removeWindow(this, true);
        }
        if(element.id > 1)
        {
            if(!((GuiWorkspace)workspace).projectManager.projects.isEmpty() && !((GuiWorkspace)workspace).windowAnimate.animList.selectedIdentifier.isEmpty())
            {
                String animName = "";
                int length = 1;
                for(int i = 0; i < elements.size(); i++)
                {
                    if(elements.get(i) instanceof ElementTextInput)
                    {
                        ElementTextInput text = (ElementTextInput)elements.get(i);
                        if(text.id == 4)
                        {
                            animName = text.textField.getText();
                        }
                    }
                    if(elements.get(i) instanceof ElementNumberInput)
                    {
                        ElementNumberInput text = (ElementNumberInput)elements.get(i);
                        length = Integer.parseInt(text.textFields.get(0).getText());
                    }
                }
                if(animName.isEmpty())
                {
                    animName = "NewComponent";
                }
                if(!((GuiWorkspace)workspace).remoteSession)
                {
                    Tabula.proxy.tickHandlerClient.mainframe.createNewAnimComponent(((GuiWorkspace)workspace).projectManager.projects.get(((GuiWorkspace)workspace).projectManager.selectedProject).identifier, ((GuiWorkspace)workspace).windowAnimate.animList.selectedIdentifier, cubeIdent, animName, length, ((GuiWorkspace)workspace).windowAnimate.timeline.getCurrentPos());
                }
                else if(!((GuiWorkspace)workspace).sessionEnded && ((GuiWorkspace)workspace).isEditor)
                {
                    Tabula.channel.sendToServer(new PacketGenericMethod(((GuiWorkspace)workspace).host, "createNewAnimComponent", ((GuiWorkspace)workspace).projectManager.projects.get(((GuiWorkspace)workspace).projectManager.selectedProject).identifier, ((GuiWorkspace)workspace).windowAnimate.animList.selectedIdentifier, cubeIdent, animName, length, ((GuiWorkspace)workspace).windowAnimate.timeline.getCurrentPos()));
                }
                workspace.removeWindow(this, true);
            }
        }
    }
}
