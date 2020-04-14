package me.ichun.mods.tabula.old.client.gui.window;

import me.ichun.mods.ichunutil.client.gui.window.IWorkspace;
import me.ichun.mods.ichunutil.client.gui.window.Window;
import me.ichun.mods.ichunutil.client.gui.window.element.Element;
import me.ichun.mods.ichunutil.client.gui.window.element.ElementButton;
import me.ichun.mods.ichunutil.client.gui.window.element.ElementTextInput;
import me.ichun.mods.ichunutil.common.module.tabula.project.ProjectInfo;
import me.ichun.mods.tabula.old.client.export.ExportList;
import me.ichun.mods.tabula.old.client.export.types.ExportBlockJson;
import me.ichun.mods.tabula.old.client.gui.GuiWorkspace;
import me.ichun.mods.tabula.old.client.gui.Theme;
import net.minecraft.util.text.translation.I18n;

public class WindowExportBlockJson extends Window
{
    /**
     * Toggles cornerAtZero
     */
    private ElementButton toggleCornerAtZeroButton;

    /**
     * Toggles relativeBlock
     */
    private ElementButton toggleRelativeToBlockButton;
    /**
     * Starts true.
     * If true: the corner of the block is at (0, 0) (centre of block is at (8, 8)).
     * If false: the centre of the block is at (0, 0).
     */
    private boolean cornerAtZero;

    /**
     * Starts true.
     * If true: the coordinates are relative to the block (y=23).
     * If false: the block is built using the Tabula coordinates (y=0).
     */
    private boolean relativeToBlock;

    public WindowExportBlockJson(IWorkspace parent, int x, int y, int w, int h, int minW, int minH)
    {
        super(parent, x, y, w, h, minW, minH, "export.blockjson.title", true);

        elements.add(new ElementTextInput(this, 10, 30, width - 20, 12, 1, "Mod ID"));
        String suggestedName = ((GuiWorkspace)workspace).projectManager.projects.get(((GuiWorkspace)workspace).projectManager.selectedProject).modelName.toLowerCase().replaceAll(" ", "_");
        ElementTextInput fileName = new ElementTextInput(this, 10, 65, width - 50, 12, 2, "export.blockjson.filename");
        fileName.textField.setText(suggestedName);
        elements.add(fileName);
        ElementTextInput textureName = new ElementTextInput(this, 10, 100, width - 50, 12, 3, "export.blockjson.texturename");
        textureName.textField.setText(suggestedName);
        elements.add(textureName);
        this.relativeToBlock = true;
        this.cornerAtZero = true;
        this.toggleRelativeToBlockButton = new ElementButton(this, 10, height - 100, width - 50, 16, 4, false, 1, 1, "export.blockjson.relative");
        elements.add(this.toggleRelativeToBlockButton);
        this.toggleCornerAtZeroButton = new ElementButton(this, 10, height - 65, width - 50, 16, 5, false, 1, 1, "export.blockjson.unused");
        elements.add(this.toggleCornerAtZeroButton);
        elements.add(new ElementButton(this, width - 140, height - 30, 60, 16, 100, false, 1, 1, "element.button.ok"));
        elements.add(new ElementButton(this, width - 70, height - 30, 60, 16, 0, false, 1, 1, "element.button.cancel"));
    }

    @Override
    public void draw(int mouseX, int mouseY)
    {
        super.draw(mouseX, mouseY);
        if(!minimized)
        {
            workspace.getFontRenderer().drawString("Mod ID", posX + 11, posY + 20, Theme.getAsHex(workspace.currentTheme.font), false);
            workspace.getFontRenderer().drawString(I18n.translateToLocal("export.blockjson.filename"), posX + 11, posY + 55, Theme.getAsHex(workspace.currentTheme.font), false);
            workspace.getFontRenderer().drawString(".json", posX + width - 38, posY + 67, Theme.getAsHex(workspace.currentTheme.font), false);
            workspace.getFontRenderer().drawString(I18n.translateToLocal("export.projTexture.name"), posX + 11, posY + 90, Theme.getAsHex(workspace.currentTheme.font), false);
            workspace.getFontRenderer().drawString(".png", posX + width - 38, posY + 102, Theme.getAsHex(workspace.currentTheme.font), false);
        }
    }

    @Override
    public void elementTriggered(Element element)
    {
        if(element.id == 0)
        {
            workspace.removeWindow(this, true);
        }
        else if(element.id == 4)
        {
            //Toggle whether the model is relative to the block or absolute
            if(this.relativeToBlock)
            {
                this.relativeToBlock = false;
                this.toggleRelativeToBlockButton.text = I18n.translateToLocal("export.blockjson.absolute");
                this.toggleCornerAtZeroButton.posY = height - 65;
                this.toggleCornerAtZeroButton.text = this.cornerAtZero ? I18n.translateToLocal("export.blockjson.corner") : I18n.translateToLocal("export.blockjson.centre");
            }
            else
            {
                this.relativeToBlock = true;
                this.toggleRelativeToBlockButton.text = I18n.translateToLocal("export.blockjson.relative");
                this.toggleCornerAtZeroButton.posY = -1000;
                this.cornerAtZero = true;
            }
        }
        else if(element.id == 5)
        {
            if(!this.relativeToBlock)
            {
                //Toggle whether the block corner or centre is at (0, 0)
                this.cornerAtZero = !this.cornerAtZero;
                this.toggleCornerAtZeroButton.text = this.cornerAtZero ? I18n.translateToLocal("export.blockjson.corner") : I18n.translateToLocal("export.blockjson.centre");
            }
        }
        else if(element.id > 0)
        {
            if(!((GuiWorkspace)workspace).projectManager.projects.isEmpty())
            {
                if(workspace.windowDragged == this)
                {
                    workspace.windowDragged = null;
                }

                ProjectInfo info = ((GuiWorkspace)workspace).projectManager.projects.get(((GuiWorkspace)workspace).projectManager.selectedProject);
                String modid = "";
                String fileName = "";
                String textureName = "";
                for(Element element1 : elements)
                {
                    if(element1 instanceof ElementTextInput)
                    {
                        ElementTextInput text = (ElementTextInput)element1;
                        if(text.id == 1)
                        {
                            modid = text.textField.getText();
                        }
                        else if(text.id == 2)
                        {
                            fileName = text.textField.getText();
                        }
                        else if(text.id == 3)
                        {
                            textureName = text.textField.getText();
                        }
                    }
                }
                if(modid.isEmpty() || fileName.isEmpty() || textureName.isEmpty())
                {
                    return;
                }
                ExportBlockJson exporter = (ExportBlockJson) ExportList.exportTypes.get(3);
                if(!exporter.export(info, modid, fileName, textureName, this.cornerAtZero, this.relativeToBlock))
                {
                    WindowExportBlockJsonFailed errorDialog = new WindowExportBlockJsonFailed(workspace, 0, 0, 180, 80, 180, 80);
                    errorDialog.setInfoText(exporter.errors.toString());
                    errorDialog.putInMiddleOfScreen();
                    workspace.addWindowOnTop(errorDialog);
                }
                workspace.removeWindow(this, true);
            }
        }
    }
}
