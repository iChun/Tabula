package us.ichun.mods.tabula.client.gui.window;

import net.minecraft.util.StatCollector;
import us.ichun.mods.ichunutil.client.gui.window.IWorkspace;
import us.ichun.mods.ichunutil.client.gui.window.Window;
import us.ichun.mods.ichunutil.client.gui.window.WindowPopup;
import us.ichun.mods.ichunutil.client.gui.window.element.Element;
import us.ichun.mods.ichunutil.client.gui.window.element.ElementButton;
import us.ichun.mods.ichunutil.client.gui.window.element.ElementTextInput;
import us.ichun.mods.ichunutil.common.module.tabula.common.project.ProjectInfo;
import us.ichun.mods.tabula.client.export.ExportList;
import us.ichun.mods.tabula.client.gui.GuiWorkspace;
import us.ichun.mods.tabula.client.gui.Theme;

public class WindowExportJava extends Window
{
    public WindowExportJava(IWorkspace parent, int x, int y, int w, int h, int minW, int minH)
    {
        super(parent, x, y, w, h, minW, minH, "export.javaClass.title", true);

        elements.add(new ElementTextInput(this, 10, 30, width - 20, 12, 1, "export.javaClass.package"));
        ElementTextInput clzName = new ElementTextInput(this, 10, 65, width - 20, 12, 2, "export.javaClass.className");
        clzName.textField.setText(((GuiWorkspace)workspace).projectManager.projects.get(((GuiWorkspace)workspace).projectManager.selectedProject).modelName.replaceAll(" ", ""));
        elements.add(clzName);
        elements.add(new ElementButton(this, width - 140, height - 30, 60, 16, 100, false, 1, 1, "element.button.ok"));
        elements.add(new ElementButton(this, width - 70, height - 30, 60, 16, 0, false, 1, 1, "element.button.cancel"));
    }

    @Override
    public void draw(int mouseX, int mouseY)
    {
        super.draw(mouseX, mouseY);
        if(!minimized)
        {
            workspace.getFontRenderer().drawString(StatCollector.translateToLocal("export.javaClass.package"), posX + 11, posY + 20, Theme.getAsHex(workspace.currentTheme.font), false);
            workspace.getFontRenderer().drawString(StatCollector.translateToLocal("export.javaClass.name"), posX + 11, posY + 55, Theme.getAsHex(workspace.currentTheme.font), false);
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
            if(!((GuiWorkspace)workspace).projectManager.projects.isEmpty())
            {
                if(workspace.windowDragged == this)
                {
                    workspace.windowDragged = null;
                }

                ProjectInfo info = ((GuiWorkspace)workspace).projectManager.projects.get(((GuiWorkspace)workspace).projectManager.selectedProject);
                String projName = "";
                String authName = "";
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
                }
                if(projName.isEmpty() || authName.isEmpty())
                {
                    return;
                }
                if(!ExportList.exportTypes.get(1).export(info, projName, authName))
                {
                    workspace.addWindowOnTop(new WindowPopup(workspace, 0, 0, 180, 80, 180, 80, "export.failed").putInMiddleOfScreen());
                }
                workspace.removeWindow(this, true);
            }
        }
    }
}
