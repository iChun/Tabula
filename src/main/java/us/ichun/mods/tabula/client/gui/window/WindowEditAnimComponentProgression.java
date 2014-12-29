package us.ichun.mods.tabula.client.gui.window;

import net.minecraft.util.StatCollector;
import sun.org.mozilla.javascript.internal.ast.ArrayLiteral;
import us.ichun.mods.tabula.client.gui.GuiWorkspace;
import us.ichun.mods.tabula.client.gui.Theme;
import us.ichun.mods.tabula.client.gui.window.element.*;
import us.ichun.mods.tabula.common.Tabula;
import us.ichun.module.tabula.common.project.components.Animation;
import us.ichun.module.tabula.common.project.components.AnimationComponent;

import java.util.ArrayList;
import java.util.Map;

public class WindowEditAnimComponentProgression extends Window
{
    public String parentAnim;
    public String modelIdent;
    public String comp;

    public boolean playAnim = true;

    public WindowEditAnimComponentProgression(GuiWorkspace parent, int x, int y, int w, int h, int minW, int minH, String anim, String model, String animComp)
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
            workspace.getFontRenderer().drawString(StatCollector.translateToLocal("window.editAnimCompProg.prog"), posX + (width / 2) - workspace.getFontRenderer().getStringWidth(StatCollector.translateToLocal("window.editAnimCompProg.prog")) / 2, posY + 40 + 250, Theme.getAsHex(Theme.instance.font), false);
//            workspace.getFontRenderer().drawString(StatCollector.translateToLocal("window.newAnimComp.length"), posX + 11, posY + 55, Theme.getAsHex(Theme.instance.font), false);
//            workspace.getFontRenderer().drawString(StatCollector.translateToLocal("window.editAnimComp.startPos"), posX + 11, posY + 90, Theme.getAsHex(Theme.instance.font), false);
        }
    }

    @Override
    public void update()
    {
        if(playAnim)
        {
            for(ElementListTree.Tree tree : workspace.windowAnimate.animList.trees)
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
            for(ElementListTree.Tree tree : workspace.windowAnimate.animList.trees)
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
            if(!workspace.projectManager.projects.isEmpty())
            {
                Tabula.proxy.tickHandlerClient.mainframe.resetAnimCompProgCoord(workspace.projectManager.projects.get(workspace.projectManager.selectedProject).identifier, parentAnim, comp);
            }
        }
        if(element.id == 1)
        {
            playAnim = ((ElementToggle)element).toggledState;
        }
        if(element.id > 2)
        {
            if(!workspace.projectManager.projects.isEmpty() && !workspace.windowAnimate.animList.selectedIdentifier.isEmpty())
            {
                for(ElementListTree.Tree tree : workspace.windowAnimate.animList.trees)
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

                for(ElementListTree.Tree tree : workspace.windowAnimate.animList.trees)
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
