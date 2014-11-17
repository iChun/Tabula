package us.ichun.mods.tabula.client.gui.window;

import com.google.gson.Gson;
import net.minecraft.util.StatCollector;
import us.ichun.mods.tabula.Tabula;
import us.ichun.mods.tabula.client.gui.GuiWorkspace;
import us.ichun.mods.tabula.client.gui.Theme;
import us.ichun.mods.tabula.client.gui.window.element.Element;
import us.ichun.mods.tabula.client.gui.window.element.ElementNumberInput;
import us.ichun.mods.tabula.client.gui.window.element.ElementTextInput;
import us.ichun.mods.tabula.client.gui.window.element.ElementToggle;
import us.ichun.module.tabula.common.project.components.CubeInfo;

public class WindowControls extends Window
{
    public Object selectedObject;
    public boolean refresh;

    public WindowControls(GuiWorkspace parent, int x, int y, int w, int h, int minW, int minH)
    {
        super(parent, x, y, w, h, minW, minH, "window.controls.title", true);

        elements.add(new ElementTextInput(this, 5, 27, width - 10, 12, 0, "window.controls.cubeName"));
        elements.add(new ElementNumberInput(this, 5, 53, width - 10, 12, 1, "window.controls.dimensions", 3, false, 0, Integer.MAX_VALUE));
        elements.add(new ElementNumberInput(this, 5, 79, width - 10, 12, 2, "window.controls.position", 3, true));
        elements.add(new ElementNumberInput(this, 5, 105, width - 10, 12, 3, "window.controls.offset", 3, true));
        elements.add(new ElementNumberInput(this, 5, 131, width - 10, 12, 4, "window.controls.scale", 3, true));
        elements.add(new ElementNumberInput(this, 5, 157, width - 10 - ((width - 10) / 3) - 2, 12, 5, "window.controls.txOffset", 2, false));
        elements.add(new ElementNumberInput(this, 5, 183, width - 10, 12, 6, "window.controls.rotation", 3, true));
        elements.add(new ElementToggle(this, ((width - 10) / 3 * 2) + 7, 157, width - 5 - (((width - 10) / 3 * 2) + 7) - 2, 12, 7, false, 1, 0, "window.controls.txMirror", "window.controls.txMirrorFull", false));
    }

    @Override
    public void update()
    {
        super.update();
        if(refresh)
        {
            refresh = false;
            if(selectedObject instanceof CubeInfo)
            {
                CubeInfo info = (CubeInfo)selectedObject;
                for(int k = 0; k < elements.size(); k++)
                {
                    Element e = elements.get(k);
                    if(e.id == 0)
                    {
                        ((ElementTextInput)e).textField.setText(info.name);
                    }
                    else if(e.id == 1)
                    {
                        for(int l = 0; l < ((ElementNumberInput)e).textFields.size(); l++)
                        {
                            ((ElementNumberInput)e).textFields.get(l).setText(Integer.toString(info.dimensions[l]));
                        }
                    }
                    else if(e.id == 2)
                    {
                        for(int l = 0; l < ((ElementNumberInput)e).textFields.size(); l++)
                        {
                            ((ElementNumberInput)e).textFields.get(l).setText(String.format("%.2f", info.position[l]));
                        }
                    }
                    else if(e.id == 3)
                    {
                        for(int l = 0; l < ((ElementNumberInput)e).textFields.size(); l++)
                        {
                            ((ElementNumberInput)e).textFields.get(l).setText(String.format("%.2f", info.offset[l]));
                        }
                    }
                    else if(e.id == 4)
                    {
                        for(int l = 0; l < ((ElementNumberInput)e).textFields.size(); l++)
                        {
                            ((ElementNumberInput)e).textFields.get(l).setText(String.format("%.2f", info.scale[l]));
                        }
                    }
                    else if(e.id == 5)
                    {
                        for(int l = 0; l < ((ElementNumberInput)e).textFields.size(); l++)
                        {
                            ((ElementNumberInput)e).textFields.get(l).setText(Integer.toString(info.txOffset[l]));
                        }
                    }
                    else if(e.id == 6)
                    {
                        for(int l = 0; l < ((ElementNumberInput)e).textFields.size(); l++)
                        {
                            ((ElementNumberInput)e).textFields.get(l).setText(String.format("%.2f", info.rotation[l]));
                        }
                    }
                    else if(e.id == 7)
                    {
                        ((ElementToggle)e).toggledState = info.txMirror;
                    }
                }
            }
            else
            {
                for(int k = 0; k < elements.size(); k++)
                {
                    Element e = elements.get(k);
                    if(e.id == 0)
                    {
                        ((ElementTextInput)e).textField.setText("");
                    }
                    else if(e.id >= 1 && e.id <= 6)
                    {
                        for(int l = 0; l < ((ElementNumberInput)e).textFields.size(); l++)
                        {
                            ((ElementNumberInput)e).textFields.get(l).setText("");
                        }
                    }
                    else if(e.id == 7)
                    {
                        ((ElementToggle)e).toggledState = false;
                    }
                }
            }
        }
    }

    @Override
    public void draw(int mouseX, int mouseY)
    {
        super.draw(mouseX, mouseY);
        if(!minimized)
        {
            workspace.getFontRenderer().drawString(StatCollector.translateToLocal("window.controls.cubeName"), posX + 6, posY + 17, Theme.getAsHex(Theme.font), false);
            workspace.getFontRenderer().drawString(StatCollector.translateToLocal("window.controls.dimensions"), posX + 6, posY + 43, Theme.getAsHex(Theme.font), false);
            workspace.getFontRenderer().drawString(StatCollector.translateToLocal("window.controls.position"), posX + 6, posY + 69, Theme.getAsHex(Theme.font), false);
            workspace.getFontRenderer().drawString(StatCollector.translateToLocal("window.controls.offset"), posX + 6, posY + 95, Theme.getAsHex(Theme.font), false);
            workspace.getFontRenderer().drawString(StatCollector.translateToLocal("window.controls.scale"), posX + 6, posY + 121, Theme.getAsHex(Theme.font), false);
            workspace.getFontRenderer().drawString(StatCollector.translateToLocal("window.controls.txOffset"), posX + 6, posY + 147, Theme.getAsHex(Theme.font), false);
            workspace.getFontRenderer().drawString(StatCollector.translateToLocal("window.controls.rotation"), posX + 6, posY + 173, Theme.getAsHex(Theme.font), false);
        }
    }

    @Override
    public boolean interactableWhileNoProjects()
    {
        return false;
    }

    @Override
    public void elementTriggered(Element element)
    {
        if(element.id >= 0 && element.id <= 7)
        {
            if(selectedObject instanceof CubeInfo)
            {
                CubeInfo info = (CubeInfo)selectedObject;
                for(int k = 0; k < elements.size(); k++)
                {
                    Element e = elements.get(k);
                    if(e.id == 0)
                    {
                        info.name = ((ElementTextInput)e).textField.getText();
                    }
                    else if(e.id == 1)
                    {
                        for(int l = 0; l < ((ElementNumberInput)e).textFields.size(); l++)
                        {
                            info.dimensions[l] = Integer.parseInt(((ElementNumberInput)e).textFields.get(l).getText());
                        }
                    }
                    else if(e.id == 2)
                    {
                        for(int l = 0; l < ((ElementNumberInput)e).textFields.size(); l++)
                        {
                            info.position[l] = Double.parseDouble(((ElementNumberInput)e).textFields.get(l).getText());
                        }
                    }
                    else if(e.id == 3)
                    {
                        for(int l = 0; l < ((ElementNumberInput)e).textFields.size(); l++)
                        {
                            info.offset[l] = Double.parseDouble(((ElementNumberInput)e).textFields.get(l).getText());
                        }
                    }
                    else if(e.id == 4)
                    {
                        for(int l = 0; l < ((ElementNumberInput)e).textFields.size(); l++)
                        {
                            info.scale[l] = Double.parseDouble(((ElementNumberInput)e).textFields.get(l).getText());
                        }
                    }
                    else if(e.id == 5)
                    {
                        for(int l = 0; l < ((ElementNumberInput)e).textFields.size(); l++)
                        {
                            info.txOffset[l] = Integer.parseInt(((ElementNumberInput)e).textFields.get(l).getText());
                        }
                    }
                    else if(e.id == 6)
                    {
                        for(int l = 0; l < ((ElementNumberInput)e).textFields.size(); l++)
                        {
                            info.rotation[l] = Double.parseDouble(((ElementNumberInput)e).textFields.get(l).getText());
                        }
                    }
                    else if(e.id == 7)
                    {
                        info.txMirror = ((ElementToggle)e).toggledState;
                    }
                }

                Gson gson = new Gson();
                String s = gson.toJson(selectedObject);
                if(workspace.remoteSession)
                {

                }
                else
                {
                    Tabula.proxy.tickHandlerClient.mainframe.updateCube(workspace.projectManager.projects.get(workspace.projectManager.selectedProject).identifier, s);
                }
            }
        }
    }
}
