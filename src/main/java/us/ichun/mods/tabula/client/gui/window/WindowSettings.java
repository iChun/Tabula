package us.ichun.mods.tabula.client.gui.window;

import net.minecraft.util.StatCollector;
import us.ichun.mods.tabula.client.gui.GuiWorkspace;
import us.ichun.mods.tabula.client.gui.Theme;
import us.ichun.mods.tabula.client.gui.window.element.*;
import us.ichun.mods.tabula.common.Tabula;

public class WindowSettings extends Window
{
    public WindowSettings(GuiWorkspace parent, int x, int y, int w, int h, int minW, int minH)
    {
        super(parent, x, y, w, h, minW, minH, "window.settings.title", true);

        elements.add(new ElementButton(this, width / 2 - 30, height - 25, 60, 16, -1, false, 2, 1, "element.button.ok"));

        elements.add(new ElementCheckBox(this, 11, 20, 0, false, 0, 0, "window.settings.renderRotationPoint", Tabula.config.getInt("renderRotationPoint") == 1));
        elements.add(new ElementCheckBox(this, 11, 35, 2, false, 0, 0, "tabula.config.prop.renderGrid.comment", Tabula.config.getInt("renderGrid") == 1));
        elements.add(new ElementCheckBox(this, 11, 50, 1, false, 0, 0, "tabula.config.prop.chatSound.comment", Tabula.config.getInt("chatSound") == 1));
    }

    @Override
    public void draw(int mouseX, int mouseY)
    {
        super.draw(mouseX, mouseY);
        if(!minimized)
        {
            workspace.getFontRenderer().drawString(StatCollector.translateToLocal("window.settings.renderRotationPoint"), posX + 25, posY + 21, Theme.getAsHex(Theme.instance.font), false);
            workspace.getFontRenderer().drawString(StatCollector.translateToLocal("tabula.config.prop.renderGrid.comment"), posX + 25, posY + 36, Theme.getAsHex(Theme.instance.font), false);
            workspace.getFontRenderer().drawString(StatCollector.translateToLocal("tabula.config.prop.chatSound.comment"), posX + 25, posY + 51, Theme.getAsHex(Theme.instance.font), false);
        }
    }

    @Override
    public void elementTriggered(Element element)
    {
        if(element.id == 0)
        {
            Tabula.config.get("renderRotationPoint").set(((ElementCheckBox)element).toggledState ? 1 : 0);
            Tabula.config.save();
        }
        else if(element.id == 1)
        {
            Tabula.config.get("chatSound").set(((ElementCheckBox)element).toggledState ? 1 : 0);
            Tabula.config.save();
        }
        else if(element.id == 2)
        {
            Tabula.config.get("renderGrid").set(((ElementCheckBox)element).toggledState ? 1 : 0);
            Tabula.config.save();
        }
        if(element.id == -1)
        {
            workspace.removeWindow(this, true);
        }
    }
}
