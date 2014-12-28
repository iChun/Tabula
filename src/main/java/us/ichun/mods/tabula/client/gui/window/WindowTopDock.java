package us.ichun.mods.tabula.client.gui.window;

import net.minecraft.client.gui.GuiScreen;
import net.minecraft.util.ResourceLocation;
import us.ichun.mods.tabula.client.gui.GuiWorkspace;
import us.ichun.mods.tabula.client.gui.window.element.Element;
import us.ichun.mods.tabula.client.gui.window.element.ElementButtonTextured;
import us.ichun.mods.tabula.client.gui.window.element.ElementCheckBox;
import us.ichun.mods.tabula.client.gui.window.element.ElementToggle;
import us.ichun.mods.tabula.common.Tabula;

public class WindowTopDock extends Window
{
    public static final int ID_NEW = 0;
    public static final int ID_OPEN = 1;
    public static final int ID_SAVE = 2;
    public static final int ID_SAVE_AS = 3;
    public static final int ID_IMPORT = 4;
    public static final int ID_IMPORT_MC = 5;
    public static final int ID_EXPORT = 6;
    public static final int ID_CHAT = 7;
    public static final int ID_CREDITS = 8;
    public static final int ID_EDIT = 9;
    public static final int ID_CUT = 10;
    public static final int ID_COPY = 11;
    public static final int ID_PASTE = 12;
    public static final int ID_PASTE_IN_PLACE = 13;
    public static final int ID_EXIT_TABULA = 14;
    public static final int ID_UNDO = 15;
    public static final int ID_REDO = 16;
    public static final int ID_PASTE_WITHOUT_CHILDREN = 17;
    public static final int ID_THEMES = 18;
    public static final int ID_SETTINGS = 19;

    public static final int ID_WOOD = -1;

    public WindowTopDock(GuiWorkspace parent, int x, int y, int w, int h, int minW, int minH)
    {
        super(parent, x, y, w, h, minW, minH, "", false);

        //ADD Elements
        elements.add(new ElementToggle(this, width - 44 - 20, 4, 40, 12, ID_WOOD, true, 1, 0, "topdock.wood", "topdock.woodFull", Tabula.config.getInt("renderWorkspaceBlock") == 1));

        int button = 0;
        elements.add(new ElementButtonTextured(this, 20 * button++, 0, ID_NEW, true, 0, 0, "topdock.new", new ResourceLocation("tabula", "textures/icon/new.png")));
        elements.add(new ElementButtonTextured(this, 20 * button++, 0, ID_EDIT, true, 0, 0, "topdock.edit", new ResourceLocation("tabula", "textures/icon/edit.png")));
        elements.add(new ElementButtonTextured(this, 20 * button++, 0, ID_OPEN, true, 0, 0, "topdock.open", new ResourceLocation("tabula", "textures/icon/open.png")));
        elements.add(new ElementButtonTextured(this, 20 * button++, 0, ID_SAVE, true, 0, 0, "topdock.save", new ResourceLocation("tabula", "textures/icon/save.png")));
        elements.add(new ElementButtonTextured(this, 20 * button++, 0, ID_SAVE_AS, true, 0, 0, "topdock.saveAs", new ResourceLocation("tabula", "textures/icon/saveAs.png")));
        elements.add(new ElementButtonTextured(this, 20 * button++, 0, ID_IMPORT, true, 0, 0, "topdock.import", new ResourceLocation("tabula", "textures/icon/import.png")));
        elements.add(new ElementButtonTextured(this, 20 * button++, 0, ID_IMPORT_MC, true, 0, 0, "topdock.importMC", new ResourceLocation("tabula", "textures/icon/importMC.png")));
        elements.add(new ElementButtonTextured(this, 20 * button++, 0, ID_EXPORT, true, 0, 0, "topdock.export", new ResourceLocation("tabula", "textures/icon/export.png")));
        elements.add(new ElementButtonTextured(this, 20 * button++, 0, ID_CUT, true, 0, 0, "topdock.cut", new ResourceLocation("tabula", "textures/icon/cut.png")));
        elements.add(new ElementButtonTextured(this, 20 * button++, 0, ID_COPY, true, 0, 0, "topdock.copy", new ResourceLocation("tabula", "textures/icon/copy.png")));
        elements.add(new ElementButtonTextured(this, 20 * button++, 0, ID_PASTE, true, 0, 0, "topdock.paste", new ResourceLocation("tabula", "textures/icon/paste.png")));
        elements.add(new ElementButtonTextured(this, 20 * button++, 0, ID_PASTE_IN_PLACE, true, 0, 0, "topdock.pasteInPlace", new ResourceLocation("tabula", "textures/icon/pasteInPlace.png")));
        elements.add(new ElementButtonTextured(this, 20 * button++, 0, ID_PASTE_WITHOUT_CHILDREN, true, 0, 0, "topdock.pasteWithoutChildren", new ResourceLocation("tabula", "textures/icon/pasteWithoutChildren.png")));
        elements.add(new ElementButtonTextured(this, 20 * button++, 0, ID_UNDO, true, 0, 0, "topdock.undo", new ResourceLocation("tabula", "textures/icon/undo.png")));
        elements.add(new ElementButtonTextured(this, 20 * button++, 0, ID_REDO, true, 0, 0, "topdock.redo", new ResourceLocation("tabula", "textures/icon/redo.png")));
        elements.add(new ElementButtonTextured(this, 20 * button++, 0, ID_CHAT, true, 0, 0, "topdock.chat", new ResourceLocation("tabula", "textures/icon/chat.png")));
        elements.add(new ElementButtonTextured(this, 20 * button++, 0, ID_THEMES, true, 0, 0, "topdock.themes", new ResourceLocation("tabula", "textures/icon/themes.png")));
        elements.add(new ElementButtonTextured(this, 20 * button++, 0, ID_SETTINGS, true, 0, 0, "topdock.settings", new ResourceLocation("tabula", "textures/icon/settings.png")));
        elements.add(new ElementButtonTextured(this, 20 * button++, 0, ID_CREDITS, true, 0, 0, "topdock.info", new ResourceLocation("tabula", "textures/icon/info.png")));
        elements.add(new ElementButtonTextured(this, width - 20, 0, ID_EXIT_TABULA, true, 1, 0, "topdock.exitTabula", new ResourceLocation("tabula", "textures/icon/exitTabula.png")));
    }

    @Override
    public int clickedOnBorder(int mouseX, int mouseY, int id)//only left clicks
    {
        return 0;
    }

    @Override
    public boolean clickedOnTitle(int mouseX, int mouseY, int id)
    {
        return false;
    }

    @Override
    public void elementTriggered(Element element)
    {
        if(element.id == ID_NEW)
        {
            workspace.addWindowOnTop(new WindowNewProject(workspace, workspace.width / 2 - 100, workspace.height / 2 - 100, 200, 200, 200, 160).putInMiddleOfScreen());
        }
        else if(element.id == ID_EDIT)
        {
            if(!workspace.projectManager.projects.isEmpty())
            {
                workspace.addWindowOnTop(new WindowEditProject(workspace, workspace.width / 2 - 100, workspace.height / 2 - 100, 200, 200, 200, 160).putInMiddleOfScreen());
            }
        }
        else if(element.id == ID_OPEN)
        {
            workspace.addWindowOnTop(new WindowOpenProject(workspace, workspace.width / 2 - 130, workspace.height / 2 - 160, 260, 320, 240, 160).putInMiddleOfScreen());
        }
        else if(element.id == ID_SAVE)
        {
            workspace.save(false);
        }
        else if(element.id == ID_SAVE_AS)
        {
            if(!workspace.projectManager.projects.isEmpty())
            {
                workspace.addWindowOnTop(new WindowSaveAs(workspace, workspace.width / 2 - 100, workspace.height / 2 - 80, 200, 100, 200, 100, false).putInMiddleOfScreen());
            }
        }
        else if(element.id == ID_IMPORT)
        {
            if(!workspace.projectManager.projects.isEmpty())
            {
                workspace.addWindowOnTop(new WindowImportProject(workspace, workspace.width / 2 - 130, workspace.height / 2 - 160, 260, 320, 240, 160).putInMiddleOfScreen());
            }
        }
        else if(element.id == ID_IMPORT_MC)
        {
            workspace.addWindowOnTop(new WindowImport(workspace, workspace.width / 2 - 150, workspace.height / 2 - 200, 300, workspace.height < 400 ? workspace.height - 30 < 160 ? 160 : workspace.height - 30 : 400, 280, 160).putInMiddleOfScreen());
        }
        else if(element.id == ID_EXPORT)
        {
            if(!workspace.projectManager.projects.isEmpty())
            {
                workspace.addWindowOnTop(new WindowExport(workspace, workspace.width / 2 - 130, workspace.height / 2 - 160, 260, 160, 240, 160).putInMiddleOfScreen());
            }
        }
        else if(element.id == ID_CUT)
        {
            workspace.cut();
        }
        else if(element.id == ID_COPY)
        {
            workspace.copy();
        }
        else if(element.id == ID_PASTE)
        {
            workspace.paste(GuiScreen.isShiftKeyDown(), true);
        }
        else if(element.id == ID_PASTE_IN_PLACE)
        {
            workspace.paste(true, true);
        }
        else if(element.id == ID_PASTE_WITHOUT_CHILDREN)
        {
            workspace.paste(GuiScreen.isShiftKeyDown(), false);
        }
        else if(element.id == ID_UNDO)
        {
            workspace.switchState(true);
        }
        else if(element.id == ID_REDO)
        {
            workspace.switchState(false);
        }
        else if(element.id == ID_CHAT)
        {
            workspace.windowChat.toggleVisibility();
        }
        else if(element.id == ID_THEMES)
        {
            workspace.addWindowOnTop(new WindowThemeSelect(workspace, workspace.width / 2 - 130, workspace.height / 2 - 160, 260, 160, 240, 160).putInMiddleOfScreen());
        }
        else if(element.id == ID_SETTINGS)
        {
            workspace.addWindowOnTop(new WindowSettings(workspace, workspace.width / 2 - 130, workspace.height / 2 - 160, 260, 160, 240, 160).putInMiddleOfScreen());
        }
        else if(element.id == ID_CREDITS)
        {
            workspace.addWindowOnTop(new WindowCredits(workspace, 0, 0, 300, 200, 300, 200).putInMiddleOfScreen());
        }
        else if(element.id == ID_EXIT_TABULA)
        {
            workspace.wantToExit = true;
        }
        else if(element.id == ID_WOOD)
        {
            Tabula.config.get("renderWorkspaceBlock").set(((ElementToggle)element).toggledState ? 1 : 0);
            Tabula.config.save();
        }
    }

    @Override
    public void resized()
    {
        for(Element element : elements)
        {
            element.resized();
        }
        posX = 0;
        posY = 0;
        width = workspace.width;
        height = 20;
    }

    @Override
    public void toggleMinimize()
    {
    }

    @Override
    public int getHeight()
    {
        return 20;
    }

}
