package us.ichun.mods.tabula.client.gui.window;

import net.minecraft.util.StatCollector;
import scala.Int;
import us.ichun.mods.tabula.Tabula;
import us.ichun.mods.tabula.client.gui.GuiWorkspace;
import us.ichun.mods.tabula.client.gui.window.element.Element;
import us.ichun.mods.tabula.client.gui.Theme;
import us.ichun.mods.tabula.client.gui.window.element.ElementButton;
import us.ichun.mods.tabula.client.gui.window.element.ElementNumberInput;
import us.ichun.mods.tabula.client.gui.window.element.ElementTextInput;

public class WindowNewProject extends Window
{
    public WindowNewProject(GuiWorkspace parent, int x, int y, int w, int h, int minW, int minH)
    {
        super(parent, x, y, w, h, minW, minH, "window.newProject.title", true);

        elements.add(new ElementTextInput(this, 10, 30, width - 20, 12, 1, "window.newProject.projName"));
        elements.add(new ElementTextInput(this, 10, 65, width - 20, 12, 2, "window.newProject.authName"));
        ElementNumberInput nums = new ElementNumberInput(this, 10, 100, 80, 12, 3, "window.newProject.txDimensions", 2, false, 0, (int)Short.MAX_VALUE);
        nums.textFields.get(0).setText("64");
        nums.textFields.get(1).setText("32");
        elements.add(nums);
        elements.add(new ElementButton(this, width - 140, height - 30, 60, 16, 100, false, 1, 1, "element.button.ok"));
        elements.add(new ElementButton(this, width - 70, height - 30, 60, 16, 0, false, 1, 1, "element.button.cancel"));
    }

    @Override
    public void draw(int mouseX, int mouseY)
    {
        super.draw(mouseX, mouseY);
        if(!minimized)
        {
            workspace.getFontRenderer().drawString(StatCollector.translateToLocal("window.newProject.projName"), posX + 11, posY + 20, Theme.getAsHex(Theme.font), false);
            workspace.getFontRenderer().drawString(StatCollector.translateToLocal("window.newProject.authName"), posX + 11, posY + 55, Theme.getAsHex(Theme.font), false);
            workspace.getFontRenderer().drawString(StatCollector.translateToLocal("window.newProject.txDimensions"), posX + 11, posY + 90, Theme.getAsHex(Theme.font), false);
        }
    }

    @Override
    public void elementTriggered(Element element)
    {
        if(element.id == 0)
        {
            workspace.removeWindow(this, true);
        }
        if(element.id > 0 && element.id != 3)
        {
            String projName = "";
            String authName = "";
            int dimW = 0;
            int dimH = 0;
            for(int i = 0; i < elements.size(); i++)
            {
                if(elements.get(i) instanceof ElementTextInput)
                {
                    ElementTextInput text = (ElementTextInput)elements.get(i);
                    if(text.id == 1)
                    {
                        projName = text.textField.getText();
                    }
                    else if(text.id == 2)
                    {
                        authName = text.textField.getText();
                    }
                }
                if(elements.get(i) instanceof ElementNumberInput)
                {
                    ElementNumberInput nums = (ElementNumberInput)elements.get(i);
                    dimW = Integer.parseInt(nums.textFields.get(0).getText());
                    dimH = Integer.parseInt(nums.textFields.get(1).getText());
                }
            }
            if(projName.isEmpty())
            {
                return;
            }
            if(workspace.remoteSession)
            {
                //TODO remote session
            }
            else
            {
                Tabula.proxy.tickHandlerClient.mainframe.loadEmptyProject(projName, authName, dimW, dimH);
            }
            workspace.removeWindow(this, true);
        }
    }
}
