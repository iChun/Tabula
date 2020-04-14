package me.ichun.mods.tabula.old.client.gui.window;

import me.ichun.mods.ichunutil.client.gui.window.IWorkspace;
import me.ichun.mods.ichunutil.client.gui.window.Window;
import me.ichun.mods.ichunutil.client.gui.window.element.Element;
import me.ichun.mods.ichunutil.client.gui.window.element.ElementButton;
import me.ichun.mods.ichunutil.client.gui.window.element.ElementNumberInput;
import me.ichun.mods.ichunutil.client.gui.window.element.ElementTextInput;
import me.ichun.mods.tabula.old.client.gui.GuiWorkspace;
import me.ichun.mods.tabula.old.client.gui.Theme;
import me.ichun.mods.tabula.old.common.Tabula;
import me.ichun.mods.tabula.old.common.packet.PacketGenericMethod;
import net.minecraft.util.text.translation.I18n;

public class WindowNewProject extends Window
{
    public WindowNewProject(IWorkspace parent, int x, int y, int w, int h, int minW, int minH)
    {
        super(parent, x, y, w, h, minW, minH, "window.newProject.title", true);

        elements.add(new ElementTextInput(this, 10, 30, width - 20, 12, 1, "window.newProject.projIdent"));
        elements.add(new ElementTextInput(this, 10, 65, width - 20, 12, 2, "window.newProject.animName"));
        ElementNumberInput nums = new ElementNumberInput(this, 10, 100, 80, 12, 3, "window.newProject.txDimensions", 2, false, 0, (int)Short.MAX_VALUE, 64D, 32D);
        elements.add(nums);
        ElementNumberInput scale = new ElementNumberInput(this, 10, 135, 120, 12, 4, "window.newProject.projectScale", 3, true, (int)Short.MIN_VALUE, (int)Short.MAX_VALUE, 1D, 1D, 1D);
        elements.add(scale);
        elements.add(new ElementButton(this, width - 140, height - 30, 60, 16, 100, false, 1, 1, "element.button.ok"));
        elements.add(new ElementButton(this, width - 70, height - 30, 60, 16, 0, false, 1, 1, "element.button.cancel"));
    }

    @Override
    public void draw(int mouseX, int mouseY)
    {
        super.draw(mouseX, mouseY);
        if(!minimized)
        {
            workspace.getFontRenderer().drawString(I18n.translateToLocal("window.newProject.projIdent"), posX + 11, posY + 20, Theme.getAsHex(workspace.currentTheme.font), false);
            workspace.getFontRenderer().drawString(I18n.translateToLocal("window.newProject.animName"), posX + 11, posY + 55, Theme.getAsHex(workspace.currentTheme.font), false);
            workspace.getFontRenderer().drawString(I18n.translateToLocal("window.newProject.txDimensions"), posX + 11, posY + 90, Theme.getAsHex(workspace.currentTheme.font), false);
            workspace.getFontRenderer().drawString(I18n.translateToLocal("window.newProject.projectScale"), posX + 11, posY + 125, Theme.getAsHex(workspace.currentTheme.font), false);
        }
    }

    @Override
    public void elementTriggered(Element element)
    {
        if(element.id == 0)
        {
            workspace.removeWindow(this, true);
        }
        if(element.id > 0 && element.id != 3 && element.id != 4)
        {
            String projName = "";
            String authName = "";
            int dimW = 0;
            int dimH = 0;
            double scaleX = 1.0F;
            double scaleY = 1.0F;
            double scaleZ = 1.0F;
            for(Element element1 : elements)
            {
                if(element1 instanceof ElementTextInput)
                {
                    ElementTextInput text = (ElementTextInput)element1;
                    if(text.id == 1)
                    {
                        projName = text.textField.getText();
                    }
                    else if(text.id == 2)
                    {
                        authName = text.textField.getText();
                    }
                }
                if(element1 instanceof ElementNumberInput)
                {
                    ElementNumberInput nums = (ElementNumberInput)element1;
                    if(nums.id == 3)
                    {
                        dimW = Integer.parseInt(nums.textFields.get(0).getText());
                        dimH = Integer.parseInt(nums.textFields.get(1).getText());
                    }
                    else if(nums.id == 4)
                    {
                        scaleX = Double.parseDouble(nums.textFields.get(0).getText());
                        scaleY = Double.parseDouble(nums.textFields.get(1).getText());
                        scaleZ = Double.parseDouble(nums.textFields.get(2).getText());
                    }
                }
            }
            if(projName.isEmpty())
            {
                projName = "NewProject";
            }
            if(authName.isEmpty())
            {
                authName = "Undefined";
            }
            ((GuiWorkspace)workspace).openNextNewProject = true;
            if(!((GuiWorkspace)workspace).remoteSession)
            {
                Tabula.proxy.tickHandlerClient.mainframe.loadEmptyProject(projName, authName, dimW, dimH, scaleX, scaleY, scaleZ);
            }
            else if(!((GuiWorkspace)workspace).sessionEnded && ((GuiWorkspace)workspace).isEditor)
            {
                Tabula.channel.sendToServer(new PacketGenericMethod(((GuiWorkspace)workspace).host, "loadEmptyProject", projName, authName, dimW, dimH, scaleX, scaleY, scaleZ));
            }
            workspace.removeWindow(this, true);
        }
    }
}
