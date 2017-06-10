package me.ichun.mods.tabula.client.gui.window;

import me.ichun.mods.ichunutil.client.gui.window.IWorkspace;
import me.ichun.mods.ichunutil.client.gui.window.Window;
import me.ichun.mods.ichunutil.client.gui.window.element.Element;
import me.ichun.mods.ichunutil.client.gui.window.element.ElementButton;
import me.ichun.mods.ichunutil.client.gui.window.element.ElementCheckBox;
import me.ichun.mods.ichunutil.client.gui.window.element.ElementTextInput;
import me.ichun.mods.ichunutil.common.module.tabula.project.components.Animation;
import me.ichun.mods.tabula.client.gui.GuiWorkspace;
import me.ichun.mods.tabula.client.gui.Theme;
import me.ichun.mods.tabula.common.Tabula;
import me.ichun.mods.tabula.common.packet.PacketGenericMethod;
import net.minecraft.util.text.translation.I18n;

public class WindowEditAnimation extends Window
{
    public String ident;

    public WindowEditAnimation(IWorkspace parent, int x, int y, int w, int h, int minW, int minH, Animation anim)
    {
        super(parent, x, y, w, h, minW, minH, "window.editAnim.title", true);

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
            workspace.getFontRenderer().drawString(I18n.translateToLocal("window.newAnim.animName"), posX + 11, posY + 20, Theme.getAsHex(workspace.currentTheme.font), false);
            workspace.getFontRenderer().drawString(I18n.translateToLocal("window.newAnim.animLoop"), posX + 25, posY + 55, Theme.getAsHex(workspace.currentTheme.font), false);
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
            if(!((GuiWorkspace)workspace).projectManager.projects.isEmpty())
            {
                String animName = "";
                boolean loop = false;
                for(Element element1 : elements)
                {
                    if(element1 instanceof ElementTextInput)
                    {
                        ElementTextInput text = (ElementTextInput)element1;
                        if(text.id == 1)
                        {
                            animName = text.textField.getText();
                        }
                    }
                    else if(element1 instanceof ElementCheckBox)
                    {
                        loop = ((ElementCheckBox)element1).toggledState;
                    }
                }
                if(animName.isEmpty())
                {
                    animName = "NewAnimation";
                }
                if(!((GuiWorkspace)workspace).remoteSession)
                {
                    Tabula.proxy.tickHandlerClient.mainframe.editAnimation(((GuiWorkspace)workspace).projectManager.projects.get(((GuiWorkspace)workspace).projectManager.selectedProject).identifier, ident, animName, loop);
                }
                else if(!((GuiWorkspace)workspace).sessionEnded && ((GuiWorkspace)workspace).isEditor)
                {
                    Tabula.channel.sendToServer(new PacketGenericMethod(((GuiWorkspace)workspace).host, "editAnimation", ((GuiWorkspace)workspace).projectManager.projects.get(((GuiWorkspace)workspace).projectManager.selectedProject).identifier, ident, animName, loop));
                }
                workspace.removeWindow(this, true);
            }
        }
    }
}
