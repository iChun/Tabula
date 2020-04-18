package me.ichun.mods.tabula.old.client.gui.window;

import me.ichun.mods.ichunutil.client.gui.window.IWorkspace;
import me.ichun.mods.ichunutil.client.gui.window.WindowTopDockBase;
import me.ichun.mods.ichunutil.client.gui.window.element.Element;
import me.ichun.mods.ichunutil.client.gui.window.element.ElementButton;
import me.ichun.mods.ichunutil.client.gui.window.element.ElementButtonTextured;
import me.ichun.mods.ichunutil.client.gui.window.element.ElementToggle;
import me.ichun.mods.tabula.old.client.core.ResourceHelper;
import me.ichun.mods.tabula.old.client.gui.GuiWorkspace;
import me.ichun.mods.tabula.old.common.Tabula;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.util.ResourceLocation;

public class WindowTopDock extends WindowTopDockBase
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
    public static final int ID_ADD_EDITOR = 20;
    public static final int ID_REMOVE_EDITOR = 21;
    public static final int ID_AUTO_LAYOUT = 22;
    public static final int ID_GHOST_MODEL = 23;

    public static final int ID_WOOD = -1;
    public static final int ID_OPEN_WORKING_DIR = -2;

    public WindowTopDock(IWorkspace parent, int w, int h)
    {
        super(parent, w, h);

        int button = 0;
        if(((GuiWorkspace)workspace).host != null)
        {
            elements.add(new ElementButtonTextured(this, 20 * button++, 0, ID_CHAT, true, 0, 0, "topdock.chat", new ResourceLocation("tabula", "textures/icon/chat.png")));
            if(!((GuiWorkspace)workspace).remoteSession)
            {
                elements.add(new ElementButtonTextured(this, 20 * button++, 0, ID_ADD_EDITOR, true, 0, 0, "topdock.addEditor", new ResourceLocation("tabula", "textures/icon/addeditor.png")));
                elements.add(new ElementButtonTextured(this, 20 * button++, 0, ID_REMOVE_EDITOR, true, 0, 0, "topdock.removeEditor", new ResourceLocation("tabula", "textures/icon/removeeditor.png")));
            }
        }
        //elements.add(new ElementButtonTextured(this, width - 20, 0, ID_EXIT_TABULA, true, 1, 0, "topdock.exitTabula", new ResourceLocation("tabula", "textures/icon/exittabula.png")));
    }

    @Override
    public void elementTriggered(Element element)
    {
        else if(element.id == ID_CUT)
        {
            ((GuiWorkspace)workspace).cut();
        }
        else if(element.id == ID_COPY)
        {
            ((GuiWorkspace)workspace).copy();
        }
        else if(element.id == ID_PASTE)
        {
            ((GuiWorkspace)workspace).paste(GuiScreen.isShiftKeyDown(), true);
        }
        else if(element.id == ID_PASTE_IN_PLACE)
        {
            ((GuiWorkspace)workspace).paste(true, true);
        }
        else if(element.id == ID_PASTE_WITHOUT_CHILDREN)
        {
            ((GuiWorkspace)workspace).paste(GuiScreen.isShiftKeyDown(), false);
        }
        else if(element.id == ID_UNDO)
        {
            ((GuiWorkspace)workspace).switchState(true);
        }
        else if(element.id == ID_REDO)
        {
            ((GuiWorkspace)workspace).switchState(false);
        }
        else if(element.id == ID_CHAT)
        {
            ((GuiWorkspace)workspace).windowChat.toggleVisibility();
        }
        else if(element.id == ID_ADD_EDITOR)
        {
            workspace.addWindowOnTop(new WindowAddEditor(workspace, workspace.width / 2 - 130, workspace.height / 2 - 160, 260, 160, 240, 160).putInMiddleOfScreen());
        }
        else if(element.id == ID_REMOVE_EDITOR)
        {
            workspace.addWindowOnTop(new WindowRemoveEditor(workspace, workspace.width / 2 - 130, workspace.height / 2 - 160, 260, 160, 240, 160).putInMiddleOfScreen());
        }
        else if(element.id == ID_THEMES)
        {
            workspace.addWindowOnTop(new WindowThemeSelect(workspace, workspace.width / 2 - 130, workspace.height / 2 - 160, 260, 160, 240, 160).putInMiddleOfScreen());
        }
        else if(element.id == ID_SETTINGS)
        {
            workspace.addWindowOnTop(new WindowSettings(workspace, workspace.width / 2 - 130, workspace.height / 2 - 160, 500, 160, 400, 160).putInMiddleOfScreen());
        }
        else if(element.id == ID_CREDITS)
        {
            workspace.addWindowOnTop(new WindowCredits(workspace, 0, 0, 300, 200, 300, 200).putInMiddleOfScreen());
        }
        else if(element.id == ID_EXIT_TABULA)
        {
            ((GuiWorkspace)workspace).wantToExit = true;
        }
        else if(element.id == ID_WOOD)
        {
            Tabula.config.renderWorkspaceBlock = (((ElementToggle)element).toggledState ? 1 : 0);
            Tabula.config.save();
        }
        else if(element.id == ID_OPEN_WORKING_DIR)
        {
            OpenGlHelper.openFile(ResourceHelper.getWorkRoot());
        }
    }
}
