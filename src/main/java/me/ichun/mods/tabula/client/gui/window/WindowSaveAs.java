package me.ichun.mods.tabula.client.gui.window;

import me.ichun.mods.ichunutil.client.gui.window.IWorkspace;
import me.ichun.mods.ichunutil.client.gui.window.Window;
import me.ichun.mods.ichunutil.client.gui.window.WindowPopup;
import me.ichun.mods.ichunutil.client.gui.window.element.Element;
import me.ichun.mods.ichunutil.client.gui.window.element.ElementButton;
import me.ichun.mods.ichunutil.client.gui.window.element.ElementTextInput;
import me.ichun.mods.ichunutil.client.gui.window.element.ElementTextInputSaveAs;
import me.ichun.mods.ichunutil.common.core.util.IOUtil;
import me.ichun.mods.ichunutil.common.module.tabula.project.ProjectInfo;
import me.ichun.mods.tabula.client.core.ResourceHelper;
import me.ichun.mods.tabula.client.gui.GuiWorkspace;
import me.ichun.mods.tabula.client.gui.Theme;
import net.minecraft.util.text.translation.I18n;

import java.io.File;

public class WindowSaveAs extends Window
{
    private static final String[] invalidChars = new String[] { "\\\\", "/", ":", "\\*", "\\?", "\"", "<", ">", "|" };

    public boolean shouldClose;
    public boolean closeProject;

    public WindowSaveAs(IWorkspace parent, int x, int y, int w, int h, int minW, int minH, boolean close)
    {
        super(parent, x, y, w, h, minW, minH, "window.saveAs.title", true);

        ProjectInfo project = ((GuiWorkspace)workspace).projectManager.projects.get(((GuiWorkspace)workspace).projectManager.selectedProject);

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
            if(closeProject && !((GuiWorkspace)workspace).projectManager.projects.isEmpty())
            {
                ((GuiWorkspace)workspace).closeProject(((GuiWorkspace)workspace).projectManager.projects.get(((GuiWorkspace)workspace).projectManager.selectedProject));
            }
        }
    }

    @Override
    public void draw(int mouseX, int mouseY)
    {
        super.draw(mouseX, mouseY);
        if(!minimized)
        {
            workspace.getFontRenderer().drawString(I18n.translateToLocal("window.saveAs.fileName"), posX + 11, posY + 20, Theme.getAsHex(workspace.currentTheme.font), false);
        }
    }

    @Override
    public void elementTriggered(Element element)
    {
        if(element.id == 0)
        {
            workspace.removeWindow(this, true);
            ((GuiWorkspace)workspace).wantToExit = false;
        }
        if(element.id == 3)
        {
            String projName = "";
            for(Element element1 : elements)
            {
                if(element1 instanceof ElementTextInput)
                {
                    ElementTextInput text = (ElementTextInput)element1;
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
                workspace.addWindowOnTop(new WindowOverwrite(workspace, this, ((GuiWorkspace)workspace).projectManager.projects.get(((GuiWorkspace)workspace).projectManager.selectedProject), file).putInMiddleOfScreen());
            }
            else
            {
                if(ProjectInfo.saveProject(((GuiWorkspace)workspace).projectManager.projects.get(((GuiWorkspace)workspace).projectManager.selectedProject), file))
                {
                    ((GuiWorkspace)workspace).projectManager.projects.get(((GuiWorkspace)workspace).projectManager.selectedProject).saveFile = file;
                    ((GuiWorkspace)workspace).projectManager.projects.get(((GuiWorkspace)workspace).projectManager.selectedProject).saveFileMd5 = IOUtil.getMD5Checksum(file);
                    workspace.removeWindow(this, true);

                    if(closeProject && !((GuiWorkspace)workspace).projectManager.projects.isEmpty())
                    {
                        ((GuiWorkspace)workspace).closeProject(((GuiWorkspace)workspace).projectManager.projects.get(((GuiWorkspace)workspace).projectManager.selectedProject));
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
