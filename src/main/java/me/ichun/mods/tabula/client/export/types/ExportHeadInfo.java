package me.ichun.mods.tabula.client.export.types;

import com.google.common.base.Splitter;
import me.ichun.mods.ichunutil.api.common.head.HeadInfo;
import me.ichun.mods.ichunutil.client.gui.bns.Workspace;
import me.ichun.mods.ichunutil.client.gui.bns.window.WindowPopup;
import me.ichun.mods.ichunutil.common.head.HeadHandler;
import me.ichun.mods.ichunutil.common.module.tabula.formats.types.Exporter;
import me.ichun.mods.ichunutil.common.module.tabula.project.Project;
import me.ichun.mods.tabula.client.core.ResourceHelper;
import me.ichun.mods.tabula.client.gui.WorkspaceTabula;
import me.ichun.mods.tabula.client.gui.window.popup.WindowExportHeadInfo;
import me.ichun.mods.tabula.client.tabula.Mainframe;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.I18n;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.util.List;

public class ExportHeadInfo extends Exporter
{
    public ExportHeadInfo()
    {
        super(I18n.format("tabula.export.headInfo.name"));
    }

    @Override
    public String getId()
    {
        return "headInfo";
    }

    @Override
    public boolean override(Workspace workspace, Project project)
    {
        Mainframe.ProjectInfo activeProject = ((WorkspaceTabula)workspace).mainframe.getActiveProject();
        if(activeProject != null)
        {
            Project.Part selectedPart = activeProject.getSelectedPart();
            if(selectedPart != null)
            {
                if(!HeadHandler.hasInit())
                {
                    HeadHandler.init();
                }

                WindowExportHeadInfo.open((WorkspaceTabula)workspace, project, null, false);
            }
            else
            {
                WindowPopup.popup(workspace, 0.5D, 0.5D, w ->{}, I18n.format("tabula.export.headInfo.selectPart"));
            }
        }

        return true;
    }

    @Override
    public boolean export(Project project, Object... params)
    {
        HeadInfo info = (HeadInfo)params[0];

        if(!Minecraft.getInstance().getSession().getUsername().equals("Dev"))
        {
            info.author = Minecraft.getInstance().getSession().getUsername();
        }

        List<String> strings = Splitter.on(".").trimResults().omitEmptyStrings().splitToList(info.forClass);

        if(strings.isEmpty())
        {
            strings.add("NO_CLASS_DEFINED");
        }

        File file = new File(ResourceHelper.getExportsDir().toFile(), strings.get(strings.size() - 1) + ".json");
        try
        {
            String json = HeadHandler.GSON.toJson(info, HeadInfo.class);
            FileUtils.writeStringToFile(file, json, "UTF-8");
            return true;
        }
        catch(Throwable e1)
        {
            e1.printStackTrace();
        }

        return false; //Export is done by the window. We forget about it here.
    }
}
