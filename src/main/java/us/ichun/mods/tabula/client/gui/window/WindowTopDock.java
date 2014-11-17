package us.ichun.mods.tabula.client.gui.window;

import ichun.common.core.util.MD5Checksum;
import net.minecraft.util.ResourceLocation;
import us.ichun.mods.tabula.client.gui.GuiWorkspace;
import us.ichun.mods.tabula.client.gui.window.element.ElementToggle;
import us.ichun.module.tabula.common.project.ProjectInfo;
import us.ichun.mods.tabula.client.gui.window.element.Element;
import us.ichun.mods.tabula.client.gui.window.element.ElementButtonTextured;

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
    public static final int ID_WOOD = -1;

    public WindowTopDock(GuiWorkspace parent, int x, int y, int w, int h, int minW, int minH)
    {
        super(parent, x, y, w, h, minW, minH, "", false);

        //ADD Elements
        elements.add(new ElementToggle(this, width - 44, 4, 40, 12, ID_WOOD, true, 1, 0, "topdock.wood", "topdock.woodFull", true));

        int button = 0;
        elements.add(new ElementButtonTextured(this, 20 * button++, 0, ID_NEW, true, 0, 0, "topdock.new", new ResourceLocation("tabula", "textures/icon/new.png")));
        elements.add(new ElementButtonTextured(this, 20 * button++, 0, ID_OPEN, true, 0, 0, "topdock.open", new ResourceLocation("tabula", "textures/icon/open.png")));
        elements.add(new ElementButtonTextured(this, 20 * button++, 0, ID_SAVE, true, 0, 0, "topdock.save", new ResourceLocation("tabula", "textures/icon/save.png")));
        elements.add(new ElementButtonTextured(this, 20 * button++, 0, ID_SAVE_AS, true, 0, 0, "topdock.saveAs", new ResourceLocation("tabula", "textures/icon/saveAs.png")));
        elements.add(new ElementButtonTextured(this, 20 * button++, 0, ID_IMPORT, true, 0, 0, "topdock.import", new ResourceLocation("tabula", "textures/icon/import.png")));
        elements.add(new ElementButtonTextured(this, 20 * button++, 0, ID_IMPORT_MC, true, 0, 0, "topdock.importMC", new ResourceLocation("tabula", "textures/icon/importMC.png")));
        elements.add(new ElementButtonTextured(this, 20 * button++, 0, ID_EXPORT, true, 0, 0, "topdock.export", new ResourceLocation("tabula", "textures/icon/export.png")));
        elements.add(new ElementButtonTextured(this, 20 * button++, 0, ID_CHAT, true, 0, 0, "topdock.chat", new ResourceLocation("tabula", "textures/icon/chat.png")));
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
            workspace.addWindowOnTop(new WindowNewProject(workspace, workspace.width / 2 - 100, workspace.height / 2 - 80, 200, 160, 200, 160));
        }
        else if(element.id == ID_OPEN)
        {
            workspace.addWindowOnTop(new WindowOpenProject(workspace, workspace.width / 2 - 130, workspace.height / 2 - 160, 260, 320, 240, 160).putInMiddleOfScreen());
        }
        else if(element.id == ID_SAVE)
        {
            if(!workspace.projectManager.projects.isEmpty())
            {
                ProjectInfo proj = workspace.projectManager.projects.get(workspace.projectManager.selectedProject);
                boolean saveAs = true;
                boolean error = false;
                if(proj.saveFile != null && proj.saveFile.exists() && MD5Checksum.getMD5Checksum(proj.saveFile).equals(proj.saveFileMd5))
                {
                    if(ProjectInfo.saveProject(proj, proj.saveFile))
                    {
                        proj.saveFileMd5 = MD5Checksum.getMD5Checksum(proj.saveFile);
                        saveAs = false;
                    }
                    else
                    {
                        error = true;
                    }
                }
                if(saveAs)
                {
                    workspace.addWindowOnTop(new WindowSaveAs(workspace, workspace.width / 2 - 100, workspace.height / 2 - 80, 200, 100, 200, 100).putInMiddleOfScreen());
                }
                if(error)
                {
                    workspace.addWindowOnTop(new WindowPopup(workspace, 0, 0, 180, 80, 180, 80, "window.saveAs.failed").putInMiddleOfScreen());
                }
            }
        }
        else if(element.id == ID_SAVE_AS)
        {
            if(!workspace.projectManager.projects.isEmpty())
            {
                workspace.addWindowOnTop(new WindowSaveAs(workspace, workspace.width / 2 - 100, workspace.height / 2 - 80, 200, 100, 200, 100).putInMiddleOfScreen());
            }
        }
        else if(element.id == ID_IMPORT_MC)
        {
            workspace.addWindowOnTop(new WindowImport(workspace, workspace.width / 2 - 150, workspace.height / 2 - 200, 300, workspace.height < 400 ? workspace.height - 30 < 160 ? 160 : workspace.height - 30 : 400, 280, 160).putInMiddleOfScreen());
        }
        else if(element.id == ID_CHAT)
        {
            workspace.windowChat.toggleVisibility();
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
