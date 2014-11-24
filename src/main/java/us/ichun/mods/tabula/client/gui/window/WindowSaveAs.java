package us.ichun.mods.tabula.client.gui.window;

import ichun.common.core.util.MD5Checksum;
import net.minecraft.util.StatCollector;
import us.ichun.mods.tabula.client.core.ResourceHelper;
import us.ichun.mods.tabula.client.gui.GuiWorkspace;
import us.ichun.module.tabula.common.project.ProjectInfo;
import us.ichun.mods.tabula.client.gui.Theme;
import us.ichun.mods.tabula.client.gui.window.element.Element;
import us.ichun.mods.tabula.client.gui.window.element.ElementButton;
import us.ichun.mods.tabula.client.gui.window.element.ElementTextInput;
import us.ichun.mods.tabula.client.gui.window.element.ElementTextInputSaveAs;

import java.io.File;

public class WindowSaveAs extends Window
{
    private static final String[] invalidChars = new String[] { "\\\\", "/", ":", "\\*", "\\?", "\"", "<", ">", "|" };

    public boolean shouldClose;
    public boolean closeProject;

    public WindowSaveAs(GuiWorkspace parent, int x, int y, int w, int h, int minW, int minH, boolean close)
    {
        super(parent, x, y, w, h, minW, minH, "window.saveAs.title", true);

        ProjectInfo project = workspace.projectManager.projects.get(workspace.projectManager.selectedProject);

        ElementTextInputSaveAs text = new ElementTextInputSaveAs(this, 10, 30, width - 20, 12, 1, "window.saveAs.fileName");
        String name = project.modelName;

        name = name.replaceAll("[^A-Za-z0-9()\\[\\]]", "");

        text.textField.setText(name);
        elements.add(text);
        elements.add(new ElementButton(this, width - 140, height - 30, 60, 16, 3, false, 1, 1, "element.button.ok"));
        elements.add(new ElementButton(this, width - 70, height - 30, 60, 16, 0, false, 1, 1, "element.button.cancel"));

        closeProject = close;
    }

    @Override
    public void update()
    {
        if(shouldClose)
        {
            workspace.removeWindow(this, true);
            if(closeProject && !workspace.projectManager.projects.isEmpty())
            {
                workspace.closeProject(workspace.projectManager.projects.get(workspace.projectManager.selectedProject));
            }
        }
    }

    @Override
    public void draw(int mouseX, int mouseY)
    {
        super.draw(mouseX, mouseY);
        if(!minimized)
        {
            workspace.getFontRenderer().drawString(StatCollector.translateToLocal("window.saveAs.fileName"), posX + 11, posY + 20, Theme.getAsHex(Theme.font), false);
        }
    }

    @Override
    public void elementTriggered(Element element)
    {
        if(element.id == 0)
        {
            workspace.removeWindow(this, true);
            workspace.wantToExit = false;
        }
        if(element.id == 3)
        {
            String projName = "";
            for(int i = 0; i < elements.size(); i++)
            {
                if(elements.get(i) instanceof ElementTextInput)
                {
                    ElementTextInput text = (ElementTextInput)elements.get(i);
                    if(text.id == 1)
                    {
                        projName = text.textField.getText();
                    }
                }
            }
            if(projName.isEmpty())
            {
                return;
            }

            if(!projName.endsWith(".tbl"))
            {
                projName = projName + ".tbl";
            }

            File file = new File(ResourceHelper.getSaveDir(), projName);

            if(workspace.windowDragged == this)
            {
                workspace.windowDragged = null;
            }
            if(file.exists())
            {
                workspace.addWindowOnTop(new WindowOverwrite(workspace, this, workspace.projectManager.projects.get(workspace.projectManager.selectedProject), file).putInMiddleOfScreen());
            }
            else
            {
                if(ProjectInfo.saveProject(workspace.projectManager.projects.get(workspace.projectManager.selectedProject), file))
                {
                    workspace.projectManager.projects.get(workspace.projectManager.selectedProject).saveFile = file;
                    workspace.projectManager.projects.get(workspace.projectManager.selectedProject).saveFileMd5 = MD5Checksum.getMD5Checksum(file);
                    workspace.removeWindow(this, true);

                    if(closeProject && !workspace.projectManager.projects.isEmpty())
                    {
                        workspace.closeProject(workspace.projectManager.projects.get(workspace.projectManager.selectedProject));
                    }
                }
                else
                {
                    workspace.addWindowOnTop(new WindowPopup(workspace, 0, 0, 180, 80, 180, 80, "window.saveAs.failed").putInMiddleOfScreen());
                }
            }
        }
    }
}
