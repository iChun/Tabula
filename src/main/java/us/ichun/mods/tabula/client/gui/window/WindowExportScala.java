package us.ichun.mods.tabula.client.gui.window;

import net.minecraft.util.StatCollector;
import us.ichun.mods.tabula.client.export.ExportList;
import us.ichun.mods.tabula.client.gui.GuiWorkspace;
import us.ichun.mods.tabula.client.gui.Theme;
import us.ichun.mods.tabula.client.gui.window.element.*;
import us.ichun.module.tabula.common.project.ProjectInfo;

/**
 * Just WindowExportJava class with some modified strings and one integer
 *
 * @author anti344
 * @version 1.0
 */

public class WindowExportScala extends Window
{
    public WindowExportScala(GuiWorkspace parent, int x, int y, int w, int h, int minW, int minH)
    {
        super(parent, x, y, w, h, minW, minH, "export.scalaObject.title", true);

        elements.add(new ElementTextInput(this, 10, 30, width - 20, 12, 1, "export.scalaObject.package"));
        ElementTextInput clzName = new ElementTextInput(this, 10, 65, width - 20, 12, 2, "export.scalaObject.objectName");
        clzName.textField.setText(workspace.projectManager.projects.get(workspace.projectManager.selectedProject).modelName.replaceAll(" ", ""));
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
            workspace.getFontRenderer().drawString(StatCollector.translateToLocal("export.scalaObject.package"), posX + 11, posY + 20, Theme.getAsHex(Theme.font), false);
            workspace.getFontRenderer().drawString(StatCollector.translateToLocal("export.scalaObject.name"), posX + 11, posY + 55, Theme.getAsHex(Theme.font), false);
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
            if(!workspace.projectManager.projects.isEmpty())
            {
                if(workspace.windowDragged == this)
                {
                    workspace.windowDragged = null;
                }

                ProjectInfo info = workspace.projectManager.projects.get(workspace.projectManager.selectedProject);
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
                if(!ExportList.exportTypes.get(2).export(info, projName, authName))
                {
                    workspace.addWindowOnTop(new WindowPopup(workspace, 0, 0, 180, 80, 180, 80, "export.failed").putInMiddleOfScreen());
                }
                workspace.removeWindow(this, true);
            }
        }
    }
}
