package us.ichun.mods.tabula.client.gui.window;

import net.minecraft.util.StatCollector;
import us.ichun.mods.tabula.client.gui.GuiWorkspace;
import us.ichun.mods.tabula.client.gui.Theme;
import us.ichun.mods.tabula.client.gui.window.element.Element;
import us.ichun.mods.tabula.client.gui.window.element.ElementButton;
import us.ichun.mods.tabula.client.gui.window.element.ElementCheckBox;
import us.ichun.mods.tabula.client.gui.window.element.ElementTextInput;
import us.ichun.mods.tabula.common.Tabula;
import us.ichun.mods.tabula.common.packet.PacketGenericMethod;

public class WindowNewAnimation extends Window
{
    public WindowNewAnimation(GuiWorkspace parent, int x, int y, int w, int h, int minW, int minH)
    {
        super(parent, x, y, w, h, minW, minH, "window.newAnim.title", true);

        elements.add(new ElementTextInput(this, 10, 30, width - 20, 12, 1, "window.newAnim.animName"));
        elements.add(new ElementCheckBox(this, 11, 54, -1, false, 0, 0, "window.newAnim.animLoop", false));
        elements.add(new ElementButton(this, width - 140, height - 30, 60, 16, 100, false, 1, 1, "element.button.ok"));
        elements.add(new ElementButton(this, width - 70, height - 30, 60, 16, 0, false, 1, 1, "element.button.cancel"));
    }

    @Override
    public void draw(int mouseX, int mouseY)
    {
        super.draw(mouseX, mouseY);
        if(!minimized)
        {
            workspace.getFontRenderer().drawString(StatCollector.translateToLocal("window.newAnim.animName"), posX + 11, posY + 20, Theme.getAsHex(Theme.instance.font), false);
            workspace.getFontRenderer().drawString(StatCollector.translateToLocal("window.newAnim.animLoop"), posX + 25, posY + 55, Theme.getAsHex(Theme.instance.font), false);
        }
    }

    @Override
    public void elementTriggered(Element element)
    {
        if(element.id == 0)
        {
            workspace.removeWindow(this, true);
        }
        if(element.id > 0)
        {
            if(!workspace.projectManager.projects.isEmpty())
            {
                String animName = "";
                boolean loop = false;
                for(int i = 0; i < elements.size(); i++)
                {
                    if(elements.get(i) instanceof ElementTextInput)
                    {
                        ElementTextInput text = (ElementTextInput)elements.get(i);
                        if(text.id == 1)
                        {
                            animName = text.textField.getText();
                        }
                    }
                    else if(elements.get(i) instanceof ElementCheckBox)
                    {
                        loop = ((ElementCheckBox)elements.get(i)).toggledState;
                    }
                }
                if(animName.isEmpty())
                {
                    animName = "NewAnimation";
                }
                if(!workspace.remoteSession)
                {
                    Tabula.proxy.tickHandlerClient.mainframe.createNewAnimation(workspace.projectManager.projects.get(workspace.projectManager.selectedProject).identifier, animName, loop);
                }
                else if(!workspace.sessionEnded && workspace.isEditor)
                {
                    Tabula.channel.sendToServer(new PacketGenericMethod(workspace.host, "createNewAnimation", workspace.projectManager.projects.get(workspace.projectManager.selectedProject).identifier, animName, loop));
                }
                workspace.removeWindow(this, true);
            }
        }
    }
}
