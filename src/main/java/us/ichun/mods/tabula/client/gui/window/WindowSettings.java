package us.ichun.mods.tabula.client.gui.window;

import net.minecraft.util.StatCollector;
import us.ichun.mods.ichunutil.client.gui.window.IWorkspace;
import us.ichun.mods.ichunutil.client.gui.window.Window;
import us.ichun.mods.ichunutil.client.gui.window.element.Element;
import us.ichun.mods.ichunutil.client.gui.window.element.ElementButton;
import us.ichun.mods.ichunutil.client.gui.window.element.ElementCheckBox;
import us.ichun.mods.ichunutil.client.gui.window.element.ElementNumberInput;
import us.ichun.mods.tabula.client.gui.GuiWorkspace;
import us.ichun.mods.tabula.client.gui.Theme;
import us.ichun.mods.tabula.common.Tabula;

public class WindowSettings extends Window
{
    public WindowSettings(IWorkspace parent, int x, int y, int w, int h, int minW, int minH)
    {
        super(parent, x, y, w, h, minW, minH, "window.settings.title", true);

        elements.add(new ElementButton(this, width / 2 - 30, height - 25, 60, 16, -1, false, 2, 1, "element.button.ok"));

        int offset = 0;
        elements.add(new ElementCheckBox(this, 11, 20 + (15 * offset++), 0, false, 0, 0, "window.settings.renderRotationPoint", Tabula.config.renderRotationPoint == 1));
        elements.add(new ElementCheckBox(this, 11, 20 + (15 * offset++), 2, false, 0, 0, "tabula.config.prop.renderGrid.comment", Tabula.config.renderGrid == 1));
        elements.add(new ElementCheckBox(this, 11, 20 + (15 * offset++), 3, false, 0, 0, "tabula.config.prop.renderModelControls.comment", Tabula.config.renderModelControls == 1));// ID 3
        elements.add(new ElementCheckBox(this, 11, 20 + (15 * offset++), 1, false, 0, 0, "tabula.config.prop.chatSound.comment", Tabula.config.chatSound == 1));

        elements.add(new ElementNumberInput(this, 10, 19 + (15 * offset++), 40, 12, 4, "tabula.config.prop.tooltipTime.comment", 1, false, 0, Integer.MAX_VALUE, Tabula.config.tooltipTime));
    }

    @Override
    public void draw(int mouseX, int mouseY)
    {
        super.draw(mouseX, mouseY);
        if(!minimized)
        {
            int offset = 0;
            workspace.getFontRenderer().drawString(StatCollector.translateToLocal("window.settings.renderRotationPoint"), posX + 25, posY + 21 + (15 * offset++), Theme.getAsHex(workspace.currentTheme.font), false);
            workspace.getFontRenderer().drawString(StatCollector.translateToLocal("tabula.config.prop.renderGrid.name"), posX + 25, posY + 21 + (15 * offset++), Theme.getAsHex(workspace.currentTheme.font), false);
            workspace.getFontRenderer().drawString(StatCollector.translateToLocal("tabula.config.prop.renderModelControls.name"), posX + 25, posY + 21 + (15 * offset++), Theme.getAsHex(workspace.currentTheme.font), false);
            workspace.getFontRenderer().drawString(StatCollector.translateToLocal("tabula.config.prop.chatSound.comment"), posX + 25, posY + 21 + (15 * offset++), Theme.getAsHex(workspace.currentTheme.font), false);

            workspace.getFontRenderer().drawString(StatCollector.translateToLocal("tabula.config.prop.tooltipTime.comment"), posX + 55, posY + 21 + (15 * offset++), Theme.getAsHex(workspace.currentTheme.font), false);
        }
    }

    @Override
    public void elementTriggered(Element element)
    {
        if(element.id == 0)
        {
            Tabula.config.renderRotationPoint = (((ElementCheckBox)element).toggledState ? 1 : 0);
            Tabula.config.save();
        }
        else if(element.id == 1)
        {
            Tabula.config.chatSound = (((ElementCheckBox)element).toggledState ? 1 : 0);
            Tabula.config.save();
        }
        else if(element.id == 2)
        {
            Tabula.config.renderGrid = (((ElementCheckBox)element).toggledState ? 1 : 0);
            Tabula.config.save();
        }
        else if(element.id == 3)
        {
            Tabula.config.renderModelControls = (((ElementCheckBox)element).toggledState ? 1 : 0);
            Tabula.config.save();
        }
        else if(element.id == 4)
        {
            ((GuiWorkspace)workspace).tooltipTime = Tabula.config.tooltipTime = Integer.parseInt(((ElementNumberInput)element).textFields.get(0).getText());
            Tabula.config.save();
        }
        if(element.id == -1)
        {
            workspace.removeWindow(this, true);
        }
    }
}
