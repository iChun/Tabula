package us.ichun.mods.tabula.client.gui.window;

import net.minecraft.util.StatCollector;
import us.ichun.mods.tabula.Tabula;
import us.ichun.mods.tabula.client.gui.GuiWorkspace;
import us.ichun.mods.tabula.client.gui.window.element.Element;
import us.ichun.mods.tabula.client.gui.window.element.ElementButton;
import us.ichun.mods.tabula.client.gui.window.element.ElementTextWrapper;
import us.ichun.mods.tabula.client.gui.window.element.ElementTextWrapperChat;

public class WindowCredits extends Window
{
    public ElementTextWrapper textHolder;

    public WindowCredits(GuiWorkspace parent, int x, int y, int w, int h, int minW, int minH)
    {
        super(parent, x, y, w, h, minW, minH, "window.about.title", true);

        textHolder = new ElementTextWrapper(this, 5 + 5, BORDER_SIZE + 1 + 12 + 5, width - 10 - 10, (height - BORDER_SIZE - 14) - (BORDER_SIZE + 1 + 12) - 2 - 10, 2, false, false);
        elements.add(textHolder);

        for(int i = 0; i <= 5; i++)
        {
            String text = StatCollector.translateToLocal("window.about.line" + i);
            if(i == 0)
            {
                text = text + Tabula.version;
            }
            textHolder.text.add(text);
        }
        textHolder.text.add("");
        textHolder.text.add(StatCollector.translateToLocal("window.about.os1"));
        textHolder.text.add(StatCollector.translateToLocal("window.about.os2"));
        textHolder.text.add("https://github.com/iChun/Tabula");

        height = textHolder.getLineCount() * 15 + 30;
        minHeight = height;

        elements.add(new ElementButton(this, width / 2 - 30, height - 25, 60, 16, 3, false, 2, 1, "element.button.ok"));

    }

    @Override
    public void elementTriggered(Element element)
    {
        workspace.removeWindow(this, true);
    }
}
