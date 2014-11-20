package us.ichun.mods.tabula.client.export.types;

import org.apache.commons.io.FileUtils;
import us.ichun.mods.tabula.client.core.ResourceHelper;
import us.ichun.mods.tabula.client.gui.GuiWorkspace;
import us.ichun.mods.tabula.client.gui.window.WindowExportJava;
import us.ichun.module.tabula.common.project.ProjectInfo;

import java.io.File;
import java.io.IOException;

public class ExportJava extends Exporter
{
    public ExportJava()
    {
        super("export.javaClass.name");
    }

    @Override
    public boolean export(ProjectInfo info, Object... params)
    {
        File file = new File(ResourceHelper.getExportsDir(), params[1] + ".java");

        StringBuilder sb = new StringBuilder();

        try
        {
            FileUtils.writeStringToFile(file, sb.toString());
            return true;
        }
        catch(IOException e)
        {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public boolean override(GuiWorkspace workspace)
    {
        workspace.addWindowOnTop(new WindowExportJava(workspace, workspace.width / 2 - 100, workspace.height / 2 - 80, 200, 160, 200, 160).putInMiddleOfScreen());
        return true;
    }
}
