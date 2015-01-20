package us.ichun.mods.tabula.client.gui.window;

import com.google.common.io.ByteStreams;
import com.google.gson.Gson;
import org.apache.commons.io.FilenameUtils;
import us.ichun.mods.ichunutil.client.gui.window.IWorkspace;
import us.ichun.mods.ichunutil.client.gui.window.Window;
import us.ichun.mods.ichunutil.client.gui.window.element.Element;
import us.ichun.mods.ichunutil.client.gui.window.element.ElementButton;
import us.ichun.mods.tabula.client.core.ResourceHelper;
import us.ichun.mods.tabula.client.gui.Theme;
import us.ichun.mods.tabula.client.gui.window.element.ElementListTree;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;

public class WindowThemeSelect extends Window
{
    public ElementListTree modelList;
    public Theme currentTheme;

    public WindowThemeSelect(IWorkspace parent, int x, int y, int w, int h, int minW, int minH)
    {
        super(parent, x, y, w, h, minW, minH, "window.selectTheme.title", true);

        currentTheme = (Theme)Theme.copyTheme(parent.currentTheme);

        elements.add(new ElementButton(this, width - 140, height - 22, 60, 16, 1, false, 1, 1, "element.button.ok"));
        elements.add(new ElementButton(this, width - 70, height - 22, 60, 16, 0, false, 1, 1, "element.button.cancel"));
        modelList = new ElementListTree(this, BORDER_SIZE + 1, BORDER_SIZE + 1 + 10, width - (BORDER_SIZE * 2 + 2), height - BORDER_SIZE - 22 - 16, 3, false, false);
        elements.add(modelList);

        File def = new File(ResourceHelper.getThemesDir(), "default.json");

        if(def.exists())
        {
            try
            {
                InputStream con = new FileInputStream(def);
                String data = new String(ByteStreams.toByteArray(con));
                con.close();

                Theme theme = (new Gson()).fromJson(data, Theme.class);

                theme.filename = "default";

                modelList.createTree(null, theme, 13, 0, false, false);
            }
            catch(Exception ignored){}
        }

        File[] themes = ResourceHelper.getThemesDir().listFiles();

        for(File file : themes)
        {
            if(!file.isDirectory() && FilenameUtils.getExtension(file.getName()).equals("json") && !file.getName().equalsIgnoreCase("default.json"))
            {
                try
                {
                    InputStream con = new FileInputStream(file);
                    String data = new String(ByteStreams.toByteArray(con));
                    con.close();

                    Theme theme = (new Gson()).fromJson(data, Theme.class);

                    theme.filename = file.getName().substring(0, file.getName().length() - 5); //remove ".json"

                    modelList.createTree(null, theme, 13, 0, false, false);
                }
                catch(Exception ignored){}
            }
        }
    }

    @Override
    public void draw(int mouseX, int mouseY)
    {
        super.draw(mouseX, mouseY);
    }

    @Override
    public void elementTriggered(Element element)
    {
        if(element.id == 0)
        {
            Theme.loadTheme(workspace.currentTheme, currentTheme);
            workspace.removeWindow(this, true);
        }
        if(element.id == 1 || element.id == 3)
        {
            if(workspace.windowDragged == this)
            {
                workspace.windowDragged = null;
            }
            workspace.removeWindow(this, true);
        }
    }
}
