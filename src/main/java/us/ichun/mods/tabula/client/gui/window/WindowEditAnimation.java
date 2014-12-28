package us.ichun.mods.tabula.client.gui.window;

import net.minecraft.util.StatCollector;
import us.ichun.mods.tabula.client.gui.GuiWorkspace;
import us.ichun.mods.tabula.client.gui.Theme;
import us.ichun.mods.tabula.client.gui.window.element.Element;
import us.ichun.mods.tabula.client.gui.window.element.ElementButton;
import us.ichun.mods.tabula.client.gui.window.element.ElementCheckBox;
import us.ichun.mods.tabula.client.gui.window.element.ElementTextInput;
import us.ichun.mods.tabula.common.Tabula;
import us.ichun.module.tabula.common.project.components.Animation;

public class WindowEditAnimation extends Window
{
    public String ident;

    public WindowEditAnimation(GuiWorkspace parent, int x, int y, int w, int h, int minW, int minH, Animation anim)
    {
        super(parent, x, y, w, h, minW, minH, "window.newAnim.title", true);

        ident = anim.identifier;
        ElementTextInput text = new ElementTextInput(this, 10, 30, width - 20, 12, 1, "window.newAnim.animName");
        text.textField.setText(anim.name);
        elements.add(text);
        elements.add(new ElementCheckBox(this, 11, 54, -1, false, 0, 0, "window.newAnim.animLoop", anim.loops));
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
                if(workspace.remoteSession)
                {
                    //TODO remote session
                }
                else
                {
                    Tabula.proxy.tickHandlerClient.mainframe.editAnimation(workspace.projectManager.projects.get(workspace.projectManager.selectedProject).identifier, ident, animName, loop);
                }
                workspace.removeWindow(this, true);
            }
        }
    }
}
