package us.ichun.mods.tabula.client.gui.window;

import ichun.client.render.RendererHelper;
import net.minecraft.util.ResourceLocation;
import us.ichun.mods.tabula.client.gui.GuiWorkspace;
import us.ichun.mods.tabula.client.gui.Theme;
import us.ichun.mods.tabula.client.gui.window.element.Element;
import us.ichun.mods.tabula.client.gui.window.element.ElementAnimationTimeline;
import us.ichun.mods.tabula.client.gui.window.element.ElementButtonTextured;
import us.ichun.mods.tabula.client.gui.window.element.ElementListTree;
import us.ichun.mods.tabula.common.Tabula;
import us.ichun.module.tabula.common.project.components.Animation;
import us.ichun.module.tabula.common.project.components.AnimationComponent;
import us.ichun.module.tabula.common.project.components.CubeInfo;

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

    public ElementListTree animList;
    public ElementAnimationTimeline timeline;

    public WindowAnimate(GuiWorkspace parent, int x, int y, int w, int h, int minW, int minH)
    {
        super(parent, x, y, w, h, minW, minH, "window.animate.title", true);

        timeline = new ElementAnimationTimeline(this, 101, BORDER_SIZE + 1 + 10, width - 102, height - (BORDER_SIZE + 1 + 10), -2);
        elements.add(timeline);

        int button = 0;
        elements.add(new ElementButtonTextured(this, 20 * button++, 80, ID_NEW_ANIM, true, 0, 1, "window.animate.newAnim", new ResourceLocation("tabula", "textures/icon/newAnim.png")));
        elements.add(new ElementButtonTextured(this, 20 * button++, 80, ID_EDIT_ANIM, true, 0, 1, "window.animate.editAnim", new ResourceLocation("tabula", "textures/icon/editAnim.png")));
        elements.add(new ElementButtonTextured(this, 20 * button++, 80, ID_DEL_ANIM, true, 0, 1, "window.animate.delAnim", new ResourceLocation("tabula", "textures/icon/delAnim.png")));
        elements.add(new ElementButtonTextured(this, 20 * button++, 80, ID_PLAY_ANIM, true, 0, 1, "window.animate.playAnim", new ResourceLocation("tabula", "textures/icon/playAnim.png")));
        elements.add(new ElementButtonTextured(this, 20 * button++, 80, ID_STOP_ANIM, true, 0, 1, "window.animate.stopAnim", new ResourceLocation("tabula", "textures/icon/stopAnim.png")));

        elements.add(new ElementButtonTextured(this, 20 * button++ + 1, 80, ID_NEW_COMP, true, 0, 1, "window.animate.newComponent", new ResourceLocation("tabula", "textures/icon/newComponent.png")));
        elements.add(new ElementButtonTextured(this, 20 * button++ + 1, 80, ID_EDIT_COMP, true, 0, 1, "window.animate.editComponent", new ResourceLocation("tabula", "textures/icon/editComponent.png")));
        elements.add(new ElementButtonTextured(this, 20 * button++ + 1, 80, ID_DEL_COMP, true, 0, 1, "window.animate.delComponent", new ResourceLocation("tabula", "textures/icon/delComponent.png")));

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
        RendererHelper.drawColourOnScreen(Theme.instance.elementButtonBorder[0], Theme.instance.elementButtonBorder[1], Theme.instance.elementButtonBorder[2], 255, posX + 100 + 1, posY + height - 20, Math.min(100, width - 101), 1, 0);
        RendererHelper.drawColourOnScreen(Theme.instance.elementTreeBorder[0], Theme.instance.elementTreeBorder[1], Theme.instance.elementTreeBorder[2], 255, posX + 100, posY + 13, 1, height - 13, 0);
    }

    @Override
    public void elementTriggered(Element element)
    {
        if(!workspace.projectManager.projects.isEmpty())
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
                if(workspace.remoteSession)
                {
                    //TODO this, clearly
                }
                else
                {
                    Tabula.proxy.tickHandlerClient.mainframe.deleteAnimation(workspace.projectManager.projects.get(workspace.projectManager.selectedProject).identifier, animList.selectedIdentifier);
                }
            }
            else if(element.id == ID_NEW_COMP)
            {
                if(!animList.selectedIdentifier.isEmpty())
                {
                    Object obj = workspace.windowControls.selectedObject;
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
                    if(workspace.remoteSession)
                    {
                        //TODO remote session
                    }
                    else
                    {
                        Tabula.proxy.tickHandlerClient.mainframe.deleteAnimComponent(workspace.projectManager.projects.get(workspace.projectManager.selectedProject).identifier, animList.selectedIdentifier, timeline.selectedIdentifier);
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
        }
    }

    @Override
    public void resized()
    {
        super.resized();
        animList.width = 97;
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
    public boolean interactableWhileNoProjects()
    {
        return false;
    }
}
