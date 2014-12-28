package us.ichun.mods.tabula.client.gui.window;

import net.minecraft.util.StatCollector;
import us.ichun.mods.tabula.client.gui.GuiWorkspace;
import us.ichun.mods.tabula.client.gui.Theme;
import us.ichun.mods.tabula.client.gui.window.element.*;
import us.ichun.mods.tabula.common.Tabula;
import us.ichun.module.tabula.common.project.components.CubeInfo;

public class WindowNewAnimComponent extends Window
{
    public String cubeIdent;

    public WindowNewAnimComponent(GuiWorkspace parent, int x, int y, int w, int h, int minW, int minH, CubeInfo info)
    {
        super(parent, x, y, w, h, minW, minH, "window.newAnimComp.title", true);

        cubeIdent = info.identifier;
        elements.add(new ElementTextInput(this, 10, 30, width - 20, 12, 4, "window.newAnimComp.name"));
        elements.add(new ElementNumberInput(this, 10, 65, 40, 12, 1, "window.newAnimComp.length", 1, false, 1, (int)Short.MAX_VALUE, 1D));

        elements.add(new ElementButton(this, width - 140, height - 30, 60, 16, 100, false, 1, 1, "element.button.ok"));
        elements.add(new ElementButton(this, width - 70, height - 30, 60, 16, 0, false, 1, 1, "element.button.cancel"));
    }

    @Override
    public void draw(int mouseX, int mouseY)
    {
        super.draw(mouseX, mouseY);
        if(!minimized)
        {
            workspace.getFontRenderer().drawString(StatCollector.translateToLocal("window.newAnimComp.name"), posX + 11, posY + 20, Theme.getAsHex(Theme.instance.font), false);
            workspace.getFontRenderer().drawString(StatCollector.translateToLocal("window.newAnimComp.length"), posX + 11, posY + 55, Theme.getAsHex(Theme.instance.font), false);
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
            if(!workspace.projectManager.projects.isEmpty() && !workspace.windowAnimate.animList.selectedIdentifier.isEmpty())
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
                if(workspace.remoteSession)
                {
                    //TODO remote session
                }
                else
                {
                    Tabula.proxy.tickHandlerClient.mainframe.createNewAnimComponent(workspace.projectManager.projects.get(workspace.projectManager.selectedProject).identifier, workspace.windowAnimate.animList.selectedIdentifier, cubeIdent, animName, length, workspace.windowAnimate.timeline.currentPos);
                }
                workspace.removeWindow(this, true);
            }
        }
    }
}
