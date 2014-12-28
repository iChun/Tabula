package us.ichun.mods.tabula.client.gui.window;

import com.google.gson.Gson;
import net.minecraft.util.StatCollector;
import us.ichun.mods.tabula.client.gui.GuiWorkspace;
import us.ichun.mods.tabula.client.gui.Theme;
import us.ichun.mods.tabula.client.gui.window.element.*;
import us.ichun.mods.tabula.common.Tabula;
import us.ichun.module.tabula.common.project.components.CubeGroup;
import us.ichun.module.tabula.common.project.components.CubeInfo;

import java.util.Locale;

public class WindowControls extends Window
{
    public Object selectedObject;
    public boolean refresh;

    public WindowControls(GuiWorkspace parent, int x, int y, int w, int h, int minW, int minH)
    {
        super(parent, x, y, w, h, minW, minH, "window.controls.title", true);

        int offset = 26;
        int count = 0;
        elements.add(new ElementTextInput(this, 5, 27 + (offset * count++), width - 10, 12, 0, "window.controls.cubeName"));
        elements.add(new ElementNumberInput(this, 5, 27 + (offset * count++), width - 10, 12, 1, "window.controls.dimensions", 3, false, 0, Integer.MAX_VALUE));
        elements.add(new ElementNumberInput(this, 5, 27 + (offset * count++), width - 10, 12, 2, "window.controls.position", 3, true));
        elements.add(new ElementNumberInput(this, 5, 27 + (offset * count++), width - 10, 12, 3, "window.controls.offset", 3, true));
        elements.add(new ElementNumberInput(this, 5, 27 + (offset * count++), width - 12, 12, 9, "window.controls.opacity", 1, true, 0, 100));// ID 9
        elements.add(new ElementNumberInput(this, 5, 27 + (offset * count++), width - 12, 12, 8, "window.controls.mcScale", 1, true));// ID 8
        elements.add(new ElementNumberInput(this, 5, 27 + (offset * count++), width - 10, 12, 4, "window.controls.scale", 3, true));
        elements.add(new ElementToggle(this, ((width - 10) / 3 * 2) + 7, 27 + (offset * count), width - 5 - (((width - 10) / 3 * 2) + 7) - 2, 12, 7, false, 1, 0, "window.controls.txMirror", "window.controls.txMirrorFull", false));
        elements.add(new ElementNumberInput(this, 5, 27 + (offset * count++), width - 10 - ((width - 10) / 3) - 2, 12, 5, "window.controls.txOffset", 2, false));
        elements.add(new ElementNumberInput(this, 5, 27 + (offset * count++), width - 10, 12, 6, "window.controls.rotation", 3, true));
        elements.add(new ElementHoriSlider(this, 5, 27 + (offset * count++) - 12, width - 12, 50, false, "window.controls.rotation"));
        elements.add(new ElementHoriSlider(this, 5, 27 + (offset * count++) - 24, width - 12, 51, false, "window.controls.rotation"));
        elements.add(new ElementHoriSlider(this, 5, 27 + (offset * count++) - 36, width - 12, 52, false, "window.controls.rotation"));
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
                            ((ElementNumberInput)e).textFields.get(l).setText(String.format(Locale.ENGLISH, "%.2f", info.position[l]));
                        }
                    }
                    else if(e.id == 3)
                    {
                        for(int l = 0; l < ((ElementNumberInput)e).textFields.size(); l++)
                        {
                            ((ElementNumberInput)e).textFields.get(l).setText(String.format(Locale.ENGLISH, "%.2f", info.offset[l]));
                        }
                    }
                    else if(e.id == 4)
                    {
                        for(int l = 0; l < ((ElementNumberInput)e).textFields.size(); l++)
                        {
                            ((ElementNumberInput)e).textFields.get(l).setText(String.format(Locale.ENGLISH, "%.2f", info.parentIdentifier == null ? info.scale[l] : 1D));
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
                            ((ElementNumberInput)e).textFields.get(l).setSelectionPos(0);
                            ((ElementNumberInput)e).textFields.get(l).setText(String.format(Locale.ENGLISH, "%.2f", info.rotation[l]));
                            ((ElementNumberInput)e).textFields.get(l).setCursorPositionZero();
                        }
                    }
                    else if(e.id == 7)
                    {
                        ((ElementToggle)e).toggledState = info.txMirror;
                    }
                    else if(e.id == 8)
                    {
                        ((ElementNumberInput)e).textFields.get(0).setText(String.format(Locale.ENGLISH, "%.2f", info.parentIdentifier == null ? info.mcScale : 0D));
                    }
                    else if(e.id == 9)
                    {
                        ((ElementNumberInput)e).textFields.get(0).setText(String.format(Locale.ENGLISH, "%.2f", info.parentIdentifier == null ? info.opacity : 100D));
                    }
                    else if(e.id >= 50 && e.id <= 52)
                    {
                        for(int i = 0; i < 3; i++)
                        {
                            if(e.id - 50 - i == 0)
                            {
                                ((ElementHoriSlider)e).sliderProg = (info.rotation[i] + 180F) / 360F;
                            }
                        }
                    }
                }
            }
            else if(selectedObject instanceof CubeGroup)
            {
                CubeGroup info = (CubeGroup)selectedObject;
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
                            ((ElementNumberInput)e).textFields.get(l).setText("");
                        }
                    }
                    else if(e.id == 2)
                    {
                        for(int l = 0; l < ((ElementNumberInput)e).textFields.size(); l++)
                        {
                            ((ElementNumberInput)e).textFields.get(l).setText(String.format(Locale.ENGLISH, "%.2f", 0F));
                        }
                    }
                    else if(e.id == 3)
                    {
                        for(int l = 0; l < ((ElementNumberInput)e).textFields.size(); l++)
                        {
                            ((ElementNumberInput)e).textFields.get(l).setText(String.format(Locale.ENGLISH, "%.2f", 0F));
                        }
                    }
                    else if(e.id == 4)
                    {
                        for(int l = 0; l < ((ElementNumberInput)e).textFields.size(); l++)
                        {
                            ((ElementNumberInput)e).textFields.get(l).setText(String.format(Locale.ENGLISH, "%.2f", 1F));
                        }
                    }
                    else if(e.id == 5)
                    {
                        for(int l = 0; l < ((ElementNumberInput)e).textFields.size(); l++)
                        {
                            ((ElementNumberInput)e).textFields.get(l).setText(Integer.toString(0));
                        }
                    }
                    else if(e.id == 6)
                    {
                        for(int l = 0; l < ((ElementNumberInput)e).textFields.size(); l++)
                        {
                            ((ElementNumberInput)e).textFields.get(l).setText(String.format(Locale.ENGLISH, "%.2f", 0F));
                        }
                    }
                    else if(e.id == 7)
                    {
                        ((ElementToggle)e).toggledState = info.txMirror;
                    }
                    else if(e.id == 8)
                    {
                        ((ElementNumberInput)e).textFields.get(0).setText(String.format(Locale.ENGLISH, "%.2f", 0D));
                    }
                    else if(e.id == 9)
                    {
                        ((ElementNumberInput)e).textFields.get(0).setText(String.format(Locale.ENGLISH, "%.2f", 100D));
                    }
                    else if(e.id >= 50 && e.id <= 52)
                    {
                        for(int i = 0; i < 3; i++)
                        {
                            if(e.id - 50 - i == 0)
                            {
                                ((ElementHoriSlider)e).sliderProg = 0.5F;
                            }
                        }
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
                    else if(e.id >= 1 && e.id <= 6 || e.id == 8 || e.id == 9)
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
                    else if(e.id >= 50 && e.id <= 52)
                    {
                        for(int i = 0; i < 3; i++)
                        {
                            if(e.id - 50 - i == 0)
                            {
                                ((ElementHoriSlider)e).sliderProg = 0.5F;
                            }
                        }
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
            int offset = 26;
            int count = 0;
            workspace.getFontRenderer().drawString(StatCollector.translateToLocal("window.controls.cubeName"), posX + 6, posY + 17 + offset * count++, Theme.getAsHex(Theme.instance.font), false);
            workspace.getFontRenderer().drawString(StatCollector.translateToLocal("window.controls.dimensions"), posX + 6, posY + 17 + offset * count++, Theme.getAsHex(Theme.instance.font), false);
            workspace.getFontRenderer().drawString(StatCollector.translateToLocal("window.controls.position"), posX + 6, posY + 17 + offset * count++, Theme.getAsHex(Theme.instance.font), false);
            workspace.getFontRenderer().drawString(StatCollector.translateToLocal("window.controls.offset"), posX + 6, posY + 17 + offset * count++, Theme.getAsHex(Theme.instance.font), false);
            workspace.getFontRenderer().drawString(StatCollector.translateToLocal("window.controls.opacity"), posX + 6, posY + 17 + offset * count++, Theme.getAsHex(Theme.instance.font), false);
            workspace.getFontRenderer().drawString(StatCollector.translateToLocal("window.controls.mcScale"), posX + 6, posY + 17 + offset * count++, Theme.getAsHex(Theme.instance.font), false);
            workspace.getFontRenderer().drawString(StatCollector.translateToLocal("window.controls.scale"), posX + 6, posY + 17 + offset * count++, Theme.getAsHex(Theme.instance.font), false);
            workspace.getFontRenderer().drawString(StatCollector.translateToLocal("window.controls.txOffset"), posX + 6, posY + 17 + offset * count++, Theme.getAsHex(Theme.instance.font), false);
            workspace.getFontRenderer().drawString(StatCollector.translateToLocal("window.controls.rotation"), posX + 6, posY + 17 + offset * count++, Theme.getAsHex(Theme.instance.font), false);
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
        if(element.id >= 0 && element.id <= 9)
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
                            info.scale[l] = info.parentIdentifier == null ? Double.parseDouble(((ElementNumberInput)e).textFields.get(l).getText()) : 1D;
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
                            while(info.rotation[l] < -180D)
                            {
                                info.rotation[l] += 360D;
                            }
                            while(info.rotation[l] > 180D)
                            {
                                info.rotation[l] -= 360D;
                            }
                        }
                    }
                    else if(e.id == 7)
                    {
                        info.txMirror = ((ElementToggle)e).toggledState;
                    }
                    else if(e.id == 8)
                    {
                        info.mcScale = info.parentIdentifier == null ? Double.parseDouble(((ElementNumberInput)e).textFields.get(0).getText()) : 0D;
                    }
                    else if(e.id == 9)
                    {
                        info.opacity = info.parentIdentifier == null ? Double.parseDouble(((ElementNumberInput)e).textFields.get(0).getText()) : 100D;
                    }
                }

                Gson gson = new Gson();
                String s = gson.toJson(selectedObject);
                if(workspace.remoteSession)
                {
                    //TODO this
                }
                else
                {
                    Tabula.proxy.tickHandlerClient.mainframe.updateCube(workspace.projectManager.projects.get(workspace.projectManager.selectedProject).identifier, s, workspace.windowAnimate.animList.selectedIdentifier, workspace.windowAnimate.timeline.currentPos);
                }
            }
            else if(selectedObject instanceof CubeGroup)
            {
                CubeGroup info = (CubeGroup)selectedObject;
                String name = "";
                double[] pos = new double[3];
                double[] offset = new double[3];
                double[] scale = new double[3];
                int[] txOffset = new int[2];
                double[] rot = new double[3];
                boolean mirror = false;
                double mcScale = 0.0D;
                double opacity = 100D;

                for(int k = 0; k < elements.size(); k++)
                {
                    Element e = elements.get(k);
                    if(e.id == 0)
                    {
                        name = ((ElementTextInput)e).textField.getText();
                    }
                    else if(e.id == 2)
                    {
                        for(int l = 0; l < ((ElementNumberInput)e).textFields.size(); l++)
                        {
                            pos[l] = Double.parseDouble(((ElementNumberInput)e).textFields.get(l).getText());
                        }
                    }
                    else if(e.id == 3)
                    {
                        for(int l = 0; l < ((ElementNumberInput)e).textFields.size(); l++)
                        {
                            offset[l] = Double.parseDouble(((ElementNumberInput)e).textFields.get(l).getText());
                        }
                    }
                    else if(e.id == 4)
                    {
                        for(int l = 0; l < ((ElementNumberInput)e).textFields.size(); l++)
                        {
                            scale[l] = Double.parseDouble(((ElementNumberInput)e).textFields.get(l).getText());
                        }
                    }
                    else if(e.id == 5)
                    {
                        for(int l = 0; l < ((ElementNumberInput)e).textFields.size(); l++)
                        {
                            txOffset[l] = Integer.parseInt(((ElementNumberInput)e).textFields.get(l).getText());
                        }
                    }
                    else if(e.id == 6)
                    {
                        for(int l = 0; l < ((ElementNumberInput)e).textFields.size(); l++)
                        {
                            rot[l] = Double.parseDouble(((ElementNumberInput)e).textFields.get(l).getText());
                        }
                    }
                    else if(e.id == 7)
                    {
                        mirror = ((ElementToggle)e).toggledState;
                    }
                    else if(e.id == 8)
                    {
                        mcScale = Double.parseDouble(((ElementNumberInput)e).textFields.get(0).getText());
                    }
                    else if(e.id == 9)
                    {
                        opacity = Double.parseDouble(((ElementNumberInput)e).textFields.get(0).getText());
                    }
                }

                if(workspace.remoteSession)
                {

                }
                else
                {
                    Tabula.proxy.tickHandlerClient.mainframe.updateGroup(workspace.projectManager.projects.get(workspace.projectManager.selectedProject).identifier, info.identifier, name, pos, offset, scale, txOffset, rot, mirror, mcScale, opacity);
                }
            }
        }
        else if(element.id >= 50 && element.id <= 52)
        {
            for(int k = 0; k < elements.size(); k++)
            {
                Element e = elements.get(k);
                if(e.id == 6)
                {
                    ((ElementNumberInput)e).textFields.get(element.id - 50).setSelectionPos(0);
                    ((ElementNumberInput)e).textFields.get(element.id - 50).setText(String.format(Locale.ENGLISH, "%.2f", (((ElementHoriSlider)element).sliderProg * 360F) - 180F));
                    ((ElementNumberInput)e).textFields.get(element.id - 50).setFocused(true);
                    ((ElementNumberInput)e).textFields.get(element.id - 50).setCursorPositionZero();
                    ((ElementNumberInput)e).textFields.get(element.id - 50).setSelectionPos(0);
                    ((ElementNumberInput)e).textFields.get(element.id - 50).setFocused(false);
                    elementTriggered(e);
                    break;
                }
            }
        }
    }
}
