package me.ichun.mods.tabula.client.gui.window;

import me.ichun.mods.ichunutil.client.gui.window.IWorkspace;
import me.ichun.mods.ichunutil.client.gui.window.Window;
import me.ichun.mods.ichunutil.client.gui.window.WindowPopup;
import me.ichun.mods.ichunutil.client.gui.window.element.Element;
import me.ichun.mods.ichunutil.client.gui.window.element.ElementButton;
import me.ichun.mods.ichunutil.client.gui.window.element.ElementListTree;
import me.ichun.mods.ichunutil.client.gui.window.element.ElementToggle;
import me.ichun.mods.ichunutil.common.module.tabula.project.components.Animation;
import me.ichun.mods.ichunutil.common.module.tabula.project.components.AnimationComponent;
import me.ichun.mods.tabula.client.gui.GuiWorkspace;
import me.ichun.mods.tabula.client.gui.Theme;
import me.ichun.mods.tabula.client.gui.window.element.ElementAnimationProgression;
import me.ichun.mods.tabula.common.Tabula;
import net.minecraft.util.text.translation.I18n;

import java.util.ArrayList;
import java.util.Map;

public class WindowEditAnimComponentProgression extends Window
{
    public String parentAnim;
    public String modelIdent;
    public String comp;

    public boolean playAnim = true;

    public WindowEditAnimComponentProgression(IWorkspace parent, int x, int y, int w, int h, int minW, int minH, String anim, String model, String animComp)
    {
        super(parent, x, y, w, h, minW, minH, "window.editAnimCompProg.title", true);

        parentAnim = anim;
        modelIdent = model;
        comp = animComp;

        elements.add(new ElementAnimationProgression(this, 25, 35, 1));

        elements.add(new ElementButton(this, width - 140, height - 30, 60, 16, 100, false, 1, 1, "element.button.ok"));
        elements.add(new ElementButton(this, width - 70, height - 30, 60, 16, 0, false, 1, 1, "window.editAnimCompProg.reset"));
        elements.add(new ElementToggle(this, 10, height - 30, 60, 16, 1, false, 0, 1, "window.editAnimCompProg.play", "window.editAnimCompProg.playDesc", true));
    }

    @Override
    public void draw(int mouseX, int mouseY)
    {
        super.draw(mouseX, mouseY);
        if(!minimized)
        {
            workspace.getFontRenderer().drawString(I18n.translateToLocal("window.editAnimCompProg.prog"), posX + (width / 2) - workspace.getFontRenderer().getStringWidth(I18n.translateToLocal("window.editAnimCompProg.prog")) / 2, posY + 40 + 250, Theme.getAsHex(workspace.currentTheme.font), false);
//            workspace.getFontRenderer().drawString(I18n.translateToLocal("window.newAnimComp.length"), posX + 11, posY + 55, Theme.getAsHex(Theme.instance.font), false);
//            workspace.getFontRenderer().drawString(I18n.translateToLocal("window.editAnimComp.startPos"), posX + 11, posY + 90, Theme.getAsHex(Theme.instance.font), false);
        }
    }

    @Override
    public void update()
    {
        if(playAnim)
        {
            for(ElementListTree.Tree tree : ((GuiWorkspace)workspace).windowAnimate.animList.trees)
            {
                if(tree.selected)
                {
                    Animation anim = (Animation)tree.attachedObject;

                    for(Map.Entry<String, ArrayList<AnimationComponent>> e : anim.sets.entrySet())
                    {
                        for(AnimationComponent comp : e.getValue())
                        {
                            if(comp.identifier.equalsIgnoreCase(this.comp))
                            {
                                if(!anim.playing)
                                {
                                    anim.play();
                                }
                                if(anim.playTime > comp.startKey + comp.length + 5 || anim.playTime < comp.startKey - 5)
                                {
                                    anim.playTime = Math.max(0, comp.startKey - 5);
                                }
                                break;
                            }
                        }
                    }
                    break;
                }
            }
        }
        else
        {
            for(ElementListTree.Tree tree : ((GuiWorkspace)workspace).windowAnimate.animList.trees)
            {
                if(tree.selected)
                {
                    Animation anim = (Animation)tree.attachedObject;

                    for(Map.Entry<String, ArrayList<AnimationComponent>> e : anim.sets.entrySet())
                    {
                        for(AnimationComponent comp : e.getValue())
                        {
                            if(comp.identifier.equalsIgnoreCase(this.comp))
                            {
                                anim.stop();
                                break;
                            }
                        }
                    }
                    break;
                }
            }
        }
    }

    @Override
    public void elementTriggered(Element element)
    {
        if(element.id == 0)
        {
            if(!((GuiWorkspace)workspace).projectManager.projects.isEmpty())
            {
                Tabula.proxy.tickHandlerClient.mainframe.resetAnimCompProgCoord(((GuiWorkspace)workspace).projectManager.projects.get(((GuiWorkspace)workspace).projectManager.selectedProject).identifier, parentAnim, comp);
            }
        }
        if(element.id == 1)
        {
            playAnim = ((ElementToggle)element).toggledState;
        }
        if(element.id > 2)
        {
            if(!((GuiWorkspace)workspace).projectManager.projects.isEmpty() && !((GuiWorkspace)workspace).windowAnimate.animList.selectedIdentifier.isEmpty())
            {
                for(ElementListTree.Tree tree : ((GuiWorkspace)workspace).windowAnimate.animList.trees)
                {
                    if(tree.selected)
                    {
                        Animation anim = (Animation)tree.attachedObject;

                        for(Map.Entry<String, ArrayList<AnimationComponent>> e : anim.sets.entrySet())
                        {
                            for(AnimationComponent comp : e.getValue())
                            {
                                if(comp.identifier.equalsIgnoreCase(this.comp) && comp.progressionCurve != null)
                                {
                                    for(int i = 0; i < 250; i++)
                                    {
                                        double y = comp.progressionCurve.value(i / 250D);
                                        if(y == Double.NaN || y == Double.NEGATIVE_INFINITY || y == Double.POSITIVE_INFINITY)
                                        {
                                            workspace.addWindowOnTop(new WindowPopup(workspace, 0, 0, 180, 80, 180, 80, "window.editAnimCompProg.invalid").putInMiddleOfScreen());
                                            return;
                                        }
                                    }
                                    break;
                                }
                            }
                        }
                        break;
                    }
                }

                for(ElementListTree.Tree tree : ((GuiWorkspace)workspace).windowAnimate.animList.trees)
                {
                    if(tree.selected)
                    {
                        Animation anim = (Animation)tree.attachedObject;

                        anim.stop();

                        break;
                    }
                }
                workspace.removeWindow(this, true);
            }
        }
    }
}
