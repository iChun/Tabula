package us.ichun.mods.tabula.gui.window;

import net.minecraft.util.StatCollector;
import us.ichun.mods.tabula.gui.GuiWorkspace;
import us.ichun.mods.tabula.gui.Theme;
import us.ichun.mods.tabula.gui.window.element.Element;
import us.ichun.mods.tabula.gui.window.element.ElementNumberInput;
import us.ichun.mods.tabula.gui.window.element.ElementTextInput;
import us.ichun.mods.tabula.gui.window.element.ElementToggle;

public class WindowControls extends Window
{
    public WindowControls(GuiWorkspace parent, int x, int y, int w, int h, int minW, int minH)
    {
        super(parent, x, y, w, h, minW, minH, "window.controls.title", true);

        elements.add(new ElementTextInput(this, 5, 27, width - 10, 12, 0, "window.controls.cubeName"));
        elements.add(new ElementNumberInput(this, 5, 53, width - 10, 12, 1, "window.controls.dimensions", 3, false, 0, Integer.MAX_VALUE));
        elements.add(new ElementNumberInput(this, 5, 79, width - 10, 12, 2, "window.controls.position", 3, true));
        elements.add(new ElementNumberInput(this, 5, 105, width - 10, 12, 3, "window.controls.offset", 3, true));
        elements.add(new ElementNumberInput(this, 5, 131, width - 10, 12, 4, "window.controls.scale", 3, true));
        elements.add(new ElementNumberInput(this, 5, 157, width - 10 - ((width - 10) / 3), 12, 5, "window.controls.txOffset", 2, false));
        elements.add(new ElementNumberInput(this, 5, 183, width - 10, 12, 6, "window.controls.rotation", 3, true));
        elements.add(new ElementToggle(this, ((width - 10) / 3 * 2) + 7, 157, width - 5 - (((width - 10) / 3 * 2) + 7), 12, 7, false, 1, 0, "window.controls.txMirror", "window.controls.txMirrorFull", false));
    }

    @Override
    public void draw(int mouseX, int mouseY)
    {
        super.draw(mouseX, mouseY);
        if(!minimized)
        {
            workspace.getFontRenderer().drawString(StatCollector.translateToLocal("window.controls.cubeName"), posX + 6, posY + 17, Theme.getAsHex(Theme.font), false);
            workspace.getFontRenderer().drawString(StatCollector.translateToLocal("window.controls.dimensions"), posX + 6, posY + 43, Theme.getAsHex(Theme.font), false);
            workspace.getFontRenderer().drawString(StatCollector.translateToLocal("window.controls.position"), posX + 6, posY + 69, Theme.getAsHex(Theme.font), false);
            workspace.getFontRenderer().drawString(StatCollector.translateToLocal("window.controls.offset"), posX + 6, posY + 95, Theme.getAsHex(Theme.font), false);
            workspace.getFontRenderer().drawString(StatCollector.translateToLocal("window.controls.scale"), posX + 6, posY + 121, Theme.getAsHex(Theme.font), false);
            workspace.getFontRenderer().drawString(StatCollector.translateToLocal("window.controls.txOffset"), posX + 6, posY + 147, Theme.getAsHex(Theme.font), false);
            workspace.getFontRenderer().drawString(StatCollector.translateToLocal("window.controls.rotation"), posX + 6, posY + 173, Theme.getAsHex(Theme.font), false);
        }
    }

    @Override
    public boolean interactableWhileNoProjects()
    {
        return false;
    }

    @Override
    public void elementTriggered(Element element)
    {
        if(element.id == 0)
        {
            workspace.removeWindow(this, true);
        }
        if(element.id >= 0 && element.id != 7)
        {
            //DO STUFF.
//            workspace.removeWindow(this, true);
        }
    }
}
