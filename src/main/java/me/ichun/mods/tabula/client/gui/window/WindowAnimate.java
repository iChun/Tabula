package me.ichun.mods.tabula.client.gui.window;

import me.ichun.mods.ichunutil.client.gui.window.IWorkspace;
import me.ichun.mods.ichunutil.client.gui.window.Window;
import me.ichun.mods.ichunutil.client.gui.window.WindowPopup;
import me.ichun.mods.ichunutil.client.gui.window.element.Element;
import me.ichun.mods.ichunutil.client.gui.window.element.ElementButtonTextured;
import me.ichun.mods.ichunutil.client.render.RendererHelper;
import me.ichun.mods.ichunutil.common.module.tabula.project.components.Animation;
import me.ichun.mods.ichunutil.common.module.tabula.project.components.AnimationComponent;
import me.ichun.mods.ichunutil.common.module.tabula.project.components.CubeInfo;
import me.ichun.mods.tabula.client.gui.GuiWorkspace;
import me.ichun.mods.tabula.client.gui.window.element.ElementAnimationTimeline;
import me.ichun.mods.tabula.client.gui.window.element.ElementListTree;
import me.ichun.mods.tabula.common.Tabula;
import me.ichun.mods.tabula.common.packet.PacketGenericMethod;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.translation.I18n;

import java.util.ArrayList;
import java.util.Map;

public class WindowAnimate extends Window
{
    public static final int ID_NEW_ANIM = 0;
    public static final int ID_EDIT_ANIM = 1;
    public static final int ID_DEL_ANIM = 2;
    public static final int ID_PLAY_ANIM = 3;
    public static final int ID_STOP_ANIM = 4;

    public static final int ID_NEW_COMP = 5;
    public static final int ID_EDIT_COMP = 6;
    public static final int ID_DEL_COMP = 7;

    public static final int ID_SPLIT_COMP = 8;
    public static final int ID_EDIT_PROGRESSION_COMP = 9;

    public ElementListTree animList;
    public ElementAnimationTimeline timeline;

    public WindowAnimate(IWorkspace parent, int x, int y, int w, int h, int minW, int minH)
    {
        super(parent, x, y, w, h, minW, minH, "window.animate.title", true);

        timeline = new ElementAnimationTimeline(this, 101, BORDER_SIZE + 1 + 10, width - 102, height - (BORDER_SIZE + 1 + 10), -2);
        elements.add(timeline);

        int button = 0;
        elements.add(new ElementButtonTextured(this, 20 * button++, 80, ID_NEW_ANIM, true, 0, 1, "window.animate.newAnim", new ResourceLocation("tabula", "textures/icon/newanim.png")));
        elements.add(new ElementButtonTextured(this, 20 * button++, 80, ID_EDIT_ANIM, true, 0, 1, "window.animate.editAnim", new ResourceLocation("tabula", "textures/icon/editanim.png")));
        elements.add(new ElementButtonTextured(this, 20 * button++, 80, ID_DEL_ANIM, true, 0, 1, "window.animate.delAnim", new ResourceLocation("tabula", "textures/icon/delanim.png")));
        elements.add(new ElementButtonTextured(this, 20 * button++, 80, ID_PLAY_ANIM, true, 0, 1, "window.animate.playAnim", new ResourceLocation("tabula", "textures/icon/playanim.png")));
        elements.add(new ElementButtonTextured(this, 20 * button++, 80, ID_STOP_ANIM, true, 0, 1, "window.animate.stopAnim", new ResourceLocation("tabula", "textures/icon/stopanim.png")));

        elements.add(new ElementButtonTextured(this, 20 * button++ + 1, 80, ID_NEW_COMP, true, 0, 1, "window.animate.newComponent", new ResourceLocation("tabula", "textures/icon/newcomponent.png")));
        elements.add(new ElementButtonTextured(this, 20 * button++ + 1, 80, ID_EDIT_COMP, true, 0, 1, "window.animate.editComponent", new ResourceLocation("tabula", "textures/icon/editcomponent.png")));
        elements.add(new ElementButtonTextured(this, 20 * button++ + 1, 80, ID_DEL_COMP, true, 0, 1, "window.animate.delComponent", new ResourceLocation("tabula", "textures/icon/delcomponent.png")));
        elements.add(new ElementButtonTextured(this, 20 * button++ + 1, 80, ID_SPLIT_COMP, true, 0, 1, "window.animate.splitComponent", new ResourceLocation("tabula", "textures/icon/splitcomponent.png")));
        elements.add(new ElementButtonTextured(this, 20 * button++ + 1, 80, ID_EDIT_PROGRESSION_COMP, true, 0, 1, "window.animate.editProgComponent", new ResourceLocation("tabula", "textures/icon/editprogcomponent.png")));

        animList = new ElementListTree(this, BORDER_SIZE - 1, BORDER_SIZE + 1 + 10, 100 - (BORDER_SIZE * 2 + 2), height - BORDER_SIZE - 22 - 12, -1, false, false);
        elements.add(animList);
    }

    @Override
    public void draw(int mouseX, int mouseY) //4 pixel border?
    {
        if(width <= 0 || height <= 0)
        {
            return;
        }
        super.draw(mouseX, mouseY);
        RendererHelper.drawColourOnScreen(workspace.currentTheme.elementButtonBorder[0], workspace.currentTheme.elementButtonBorder[1], workspace.currentTheme.elementButtonBorder[2], 255, posX + 100 + 1, posY + height - 20, Math.min(100, width - 101), 1, 0);
        RendererHelper.drawColourOnScreen(workspace.currentTheme.elementTreeBorder[0], workspace.currentTheme.elementTreeBorder[1], workspace.currentTheme.elementTreeBorder[2], 255, posX + 100, posY + 13, 1, height - 13, 0);
    }

    @Override
    public void elementTriggered(Element element)
    {
        if(!((GuiWorkspace)workspace).projectManager.projects.isEmpty())
        {
            if(element.id == ID_NEW_ANIM)
            {
                workspace.addWindowOnTop(new WindowNewAnimation(workspace, workspace.width / 2 - 100, workspace.height / 2 - 80, 200, 160, 200, 160).putInMiddleOfScreen());
            }
            else if(element.id == ID_EDIT_ANIM)
            {
                for(ElementListTree.Tree tree : animList.trees)
                {
                    if(tree.selected)
                    {
                        workspace.addWindowOnTop(new WindowEditAnimation(workspace, workspace.width / 2 - 100, workspace.height / 2 - 80, 200, 160, 200, 160, (Animation)tree.attachedObject).putInMiddleOfScreen());
                        break;
                    }
                }
            }
            else if(element.id == ID_DEL_ANIM)
            {
                if(!((GuiWorkspace)workspace).remoteSession)
                {
                    Tabula.proxy.tickHandlerClient.mainframe.deleteAnimation(((GuiWorkspace)workspace).projectManager.projects.get(((GuiWorkspace)workspace).projectManager.selectedProject).identifier, animList.selectedIdentifier);
                }
                else if(!((GuiWorkspace)workspace).sessionEnded && ((GuiWorkspace)workspace).isEditor)
                {
                    Tabula.channel.sendToServer(new PacketGenericMethod(((GuiWorkspace)workspace).host, "deleteAnimation", ((GuiWorkspace)workspace).projectManager.projects.get(((GuiWorkspace)workspace).projectManager.selectedProject).identifier, animList.selectedIdentifier));
                }
            }
            else if(element.id == ID_NEW_COMP)
            {
                if(!animList.selectedIdentifier.isEmpty())
                {
                    Object obj = ((GuiWorkspace)workspace).windowControls.selectedObject;
                    if(obj instanceof CubeInfo)
                    {
                        CubeInfo info = (CubeInfo)obj;
                        workspace.addWindowOnTop(new WindowNewAnimComponent(workspace, workspace.width / 2 - 100, workspace.height / 2 - 80, 200, 160, 200, 160, info).putInMiddleOfScreen());
                    }
                    else
                    {
                        workspace.addWindowOnTop(new WindowPopup(workspace, 0, 0, 180, 80, 180, 80, "window.animate.selectCube").putInMiddleOfScreen());
                    }
                }
                else
                {
                    workspace.addWindowOnTop(new WindowPopup(workspace, 0, 0, 180, 80, 180, 80, "window.animate.selectAnim").putInMiddleOfScreen());
                }
            }
            else if(element.id == ID_EDIT_COMP)
            {
                if(!animList.selectedIdentifier.isEmpty())
                {
                    for(ElementListTree.Tree tree : animList.trees)
                    {
                        if(tree.selected)
                        {
                            Animation anim = (Animation)tree.attachedObject;

                            for(Map.Entry<String, ArrayList<AnimationComponent>> e : anim.sets.entrySet())
                            {
                                for(AnimationComponent comp : e.getValue())
                                {
                                    if(comp.identifier.equalsIgnoreCase(timeline.selectedIdentifier))
                                    {
                                        workspace.addWindowOnTop(new WindowEditAnimComponent(workspace, workspace.width / 2 - 100, workspace.height / 2 - 80, 200, 160, 200, 160, comp).putInMiddleOfScreen());
                                        break;
                                    }
                                }
                            }
                            break;
                        }
                    }
                }
            }
            else if(element.id == ID_DEL_COMP)
            {
                if(!animList.selectedIdentifier.isEmpty() && !timeline.selectedIdentifier.isEmpty())
                {
                    if(!((GuiWorkspace)workspace).remoteSession)
                    {
                        Tabula.proxy.tickHandlerClient.mainframe.deleteAnimComponent(((GuiWorkspace)workspace).projectManager.projects.get(((GuiWorkspace)workspace).projectManager.selectedProject).identifier, animList.selectedIdentifier, timeline.selectedIdentifier);
                    }
                    else if(!((GuiWorkspace)workspace).sessionEnded && ((GuiWorkspace)workspace).isEditor)
                    {
                        Tabula.channel.sendToServer(new PacketGenericMethod(((GuiWorkspace)workspace).host, "deleteAnimComponent", ((GuiWorkspace)workspace).projectManager.projects.get(((GuiWorkspace)workspace).projectManager.selectedProject).identifier, animList.selectedIdentifier, timeline.selectedIdentifier));
                    }
                }
            }
            else if(element.id == ID_PLAY_ANIM)
            {
                if(!animList.selectedIdentifier.isEmpty())
                {
                    for(ElementListTree.Tree tree : animList.trees)
                    {
                        if(tree.selected)
                        {
                            Animation anim = (Animation)tree.attachedObject;

                            anim.play();
                            break;
                        }
                    }
                }
            }
            else if(element.id == ID_STOP_ANIM)
            {
                if(!animList.selectedIdentifier.isEmpty())
                {
                    for(ElementListTree.Tree tree : animList.trees)
                    {
                        if(tree.selected)
                        {
                            Animation anim = (Animation)tree.attachedObject;

                            anim.stop();
                            break;
                        }
                    }
                }
            }
            else if(element.id == ID_SPLIT_COMP)
            {
                if(!animList.selectedIdentifier.isEmpty() && !timeline.selectedIdentifier.isEmpty())
                {
                    for(ElementListTree.Tree tree : animList.trees)
                    {
                        if(tree.selected)
                        {
                            Animation anim = (Animation)tree.attachedObject;

                            for(Map.Entry<String, ArrayList<AnimationComponent>> e : anim.sets.entrySet())
                            {
                                for(AnimationComponent comp : e.getValue())
                                {
                                    if(comp.identifier.equalsIgnoreCase(timeline.selectedIdentifier))
                                    {
                                        if(!((GuiWorkspace)workspace).remoteSession)
                                        {
                                            Tabula.proxy.tickHandlerClient.mainframe.splitAnimComponent(((GuiWorkspace)workspace).projectManager.projects.get(((GuiWorkspace)workspace).projectManager.selectedProject).identifier, animList.selectedIdentifier, timeline.selectedIdentifier, timeline.getCurrentPos());
                                        }
                                        else if(!((GuiWorkspace)workspace).sessionEnded && ((GuiWorkspace)workspace).isEditor)
                                        {
                                            Tabula.channel.sendToServer(new PacketGenericMethod(((GuiWorkspace)workspace).host, "splitAnimComponent", ((GuiWorkspace)workspace).projectManager.projects.get(((GuiWorkspace)workspace).projectManager.selectedProject).identifier, animList.selectedIdentifier, timeline.selectedIdentifier, timeline.getCurrentPos()));
                                        }
                                        break;
                                    }
                                }
                            }
                            break;
                        }
                    }
                }
            }
            else if(element.id == ID_EDIT_PROGRESSION_COMP)
            {
                if(!animList.selectedIdentifier.isEmpty() && !timeline.selectedIdentifier.isEmpty())
                {
                    for(ElementListTree.Tree tree : animList.trees)
                    {
                        if(tree.selected)
                        {
                            Animation anim = (Animation)tree.attachedObject;

                            for(Map.Entry<String, ArrayList<AnimationComponent>> e : anim.sets.entrySet())
                            {
                                for(AnimationComponent comp : e.getValue())
                                {
                                    if(comp.identifier.equalsIgnoreCase(timeline.selectedIdentifier))
                                    {
                                        int w1 = workspace.width - 300;
                                        if(!workspace.levels.get(1).isEmpty())
                                        {
                                            w1 = workspace.levels.get(1).get(0).posX - 300 + 2;
                                        }
                                        workspace.addWindowOnTop(new WindowEditAnimComponentProgression(workspace, w1, 31, 300, 340, 300, 340, animList.selectedIdentifier, e.getKey(), timeline.selectedIdentifier));
                                        break;
                                    }
                                }
                            }
                            break;
                        }
                    }
                }
            }
        }
    }

    @Override
    public void resized()
    {
        super.resized();
        animList.width = 97;
    }

    @Override
    public boolean canBeDragged()
    {
        return false;
    }

    @Override
    public int clickedOnBorder(int mouseX, int mouseY, int id)//only left clicks
    {
        if(id == 0 && !minimized)
        {
            return ((mouseY <= BORDER_SIZE + 1) ? 1 : 0) + 1; //you can only drag the top
        }
        return 0;
    }

    @Override
    public void toggleMinimize()
    {
        super.toggleMinimize();
        if(!minimized && Tabula.config.animationWarning != 1)
        {
            workspace.addWindowOnTop(new WindowPopup(workspace, 0, 0, 300, 80, 300, 80, "You can find the link from the About button -iChun").putInMiddleOfScreen());
            workspace.addWindowOnTop(new WindowPopup(workspace, 0, 0, 300, 80, 300, 80, "Let me know how anims work/suggestions on the GitHub.").putInMiddleOfScreen());
            workspace.addWindowOnTop(new WindowPopup(workspace, 0, 0, 300, 80, 300, 80, "Anims are saved and do work however.").putInMiddleOfScreen());
            workspace.addWindowOnTop(new WindowPopup(workspace, 0, 0, 300, 80, 300, 80, "The exporter does not currently export animations").putInMiddleOfScreen());
            workspace.addWindowOnTop(new WindowPopup(workspace, 0, 0, 300, 80, 300, 80, "I'm currently looking for feedback on the animation UI").putInMiddleOfScreen());
            Tabula.config.animationWarning = 1;
            Tabula.config.save();
        }
    }
    
    @Override
    public void setScissor()
    {
        RendererHelper.startGlScissor(posX, posY + 1, getWidth(), getHeight());
    }
    
    @Override
    public void drawBackground()
    {
        if(!minimized)
        {
            if(docked >= 0)
            {
                RendererHelper.drawColourOnScreen(workspace.currentTheme.windowBorder[0], workspace.currentTheme.windowBorder[1], workspace.currentTheme.windowBorder[2], 255, posX, posY + 1, getWidth(), getHeight() - 2, 0);
                RendererHelper.drawColourOnScreen(workspace.currentTheme.windowBackground[0], workspace.currentTheme.windowBackground[1], workspace.currentTheme.windowBackground[2], 255, posX + 1, posY + 1, getWidth() - 2, getHeight() - 2, 0);
            }
            else
            {
                RendererHelper.drawColourOnScreen(workspace.currentTheme.windowBorder[0], workspace.currentTheme.windowBorder[1], workspace.currentTheme.windowBorder[2], 255, posX + 1, posY + 1, getWidth() - 2, getHeight() - 2, 0);
                RendererHelper.drawColourOnScreen(workspace.currentTheme.windowBackground[0], workspace.currentTheme.windowBackground[1], workspace.currentTheme.windowBackground[2], 255, posX + BORDER_SIZE, posY + BORDER_SIZE, getWidth() - (BORDER_SIZE * 2), getHeight() - (BORDER_SIZE * 2), 0);
            }
        }
    }
    
    @Override
    public void drawTitle()
    {
        if(hasTitle)
        {
                RendererHelper.drawColourOnScreen(workspace.currentTheme.windowBorder[0], workspace.currentTheme.windowBorder[1], workspace.currentTheme.windowBorder[2], 255, posX, posY + 1, getWidth(), 12, 0);
            String titleToRender = I18n.translateToLocal(titleLocale);
            while(titleToRender.length() > 1 && workspace.getFontRenderer().getStringWidth(titleToRender) > getWidth() - (BORDER_SIZE * 2) - workspace.getFontRenderer().getStringWidth("  _"))
            {
                if(titleToRender.startsWith("..."))
                {
                    break;
                }
                if(titleToRender.endsWith("..."))
                {
                    titleToRender = titleToRender.substring(0, titleToRender.length() - 4) + "...";
                }
                else
                {
                    titleToRender = titleToRender.substring(0, titleToRender.length() - 1) + "...";
                }
            }
            workspace.getFontRenderer().drawString(titleToRender, posX + 4, posY + 3, workspace.currentTheme.getAsHex(workspace.currentTheme.font), false);
        }
    }

    @Override
    public boolean invertMinimizeSymbol()
    {
        return true;
    }

    @Override
    public boolean interactableWhileNoProjects()
    {
        return false;
    }
}
