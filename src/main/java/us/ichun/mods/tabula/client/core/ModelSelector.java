package us.ichun.mods.tabula.client.core;

import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.client.renderer.GlStateManager;
import org.lwjgl.BufferUtils;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;
import us.ichun.mods.ichunutil.client.gui.window.Window;
import us.ichun.mods.ichunutil.common.module.tabula.client.model.ModelBaseDummy;
import us.ichun.mods.ichunutil.common.module.tabula.common.project.ProjectInfo;
import us.ichun.mods.ichunutil.common.module.tabula.common.project.components.CubeInfo;
import us.ichun.mods.tabula.client.gui.GuiWorkspace;
import us.ichun.mods.ichunutil.client.gui.window.element.ElementListTree;
import us.ichun.mods.tabula.common.Tabula;

import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.List;


/**
 * A class which renders different parts of a model in different colors to figure out which part that was clicked on.
 *
 * Call onClick and the part of the model the user clicked on will get selected.
 *
 * @author Vswe
 * @version 1.1
 */
public class ModelSelector {

    private final GuiWorkspace workspace;
    private final int colors;
    private final float color_precision;

    /**
     * Create a new model selector
     * @param workspace The workspace interface this model selector will operator on
     * @param colors The amount of colors the model selector is allowed to use when figuring out what was clicked. The
     *               pro of having a high number is that the max number of parts a model can have is increased. The max
     *               amount of parts the model can have is (colors^3 - 1). However, if the number is too high floating
     *               point errors might cause problems.
     */
    public ModelSelector(GuiWorkspace workspace, int colors) {
        this.workspace = workspace;
        this.colors = colors - 1;
        this.color_precision = 1F / this.colors;
    }

    /**
     * Call this to selected the part of the model that is moused over.
     *
     * If the mouse is not in the model viewer when the click occurs, nothing is updated.
     * If the mouse is not on the model, the current selection is removed.
     * If the mouse is on the current selection, the selection is removed.
     *
     * @param mouseX The x coordinate as it is used in minecraft interfaces
     * @param mouseY The y coordinate as it is used in minecraft interfaces
     */
    public void onClick(int mouseX, int mouseY) {
        if(!isOnWindow(mouseX, mouseY) && workspace.projectManager.selectedProject != -1) {

            ArrayList<ElementListTree.Tree> trees = workspace.windowModelTree.modelList.trees;

            if(Tabula.config.renderModelControls == 1)
            {
                for(ElementListTree.Tree tree : trees)
                {
                    if(tree.selected && tree.attachedObject instanceof CubeInfo)
                    {
                        CubeInfo cube = (CubeInfo)tree.attachedObject;

                        fakeRenderSelectedCube(cube);

                        break;
                    }
                }
            }

            int control = getSelectedId();

            if(control != -1)
            {
                workspace.controlDrag = GuiScreen.isShiftKeyDown() ? control + 3 : control;
                workspace.controlDragX = mouseX;
                workspace.controlDragY = mouseY;
            }
            else
            {
                GlStateManager.clear(GL11.GL_COLOR_BUFFER_BIT);
                //render the model so the result can be analyzed
                fakeRender();

                //once the model is rendered we can fetch the id of the part that contributed to the pixel the mouse is hovering
                int id = getSelectedId();

                //update selection
                workspace.windowModelTree.modelList.selectedIdentifier = "";
                workspace.windowControls.selectedObject = null;
                int treeId = 0;
                for(ElementListTree.Tree tree : trees)
                {
                    if(tree.attachedObject instanceof CubeInfo)
                    {
                        tree.selected = !tree.selected && id == treeId;
                        if(tree.selected)
                        {
                            workspace.windowControls.selectedObject = tree.attachedObject;
                            workspace.windowModelTree.modelList.selectedIdentifier = ((CubeInfo)tree.attachedObject).identifier;
                        }
                        treeId++;
                    }
                    else
                    {
                        tree.selected = false;
                    }
                }
                workspace.windowControls.refresh = true;

                if(workspace.windowControls.selectedObject == null && id == -1)
                {
                    for(ElementListTree.Tree tree : workspace.windowAnimate.animList.trees)
                    {
                        tree.selected = false;
                    }
                    workspace.windowAnimate.animList.selectedIdentifier = "";
                    workspace.windowAnimate.timeline.selectedIdentifier = "";
                    workspace.windowAnimate.timeline.setCurrentPos(0);
                }
            }
        }
    }

    private boolean isOnWindow(int mouseX, int mouseY) {
        for(int i = 0; i < workspace.levels.size(); i++) {
            for(int j = 0; j < workspace.levels.get(i).size(); j++) {
                Window window = workspace.levels.get(i).get(j);
                if(mouseX >= window.posX && mouseX <= window.posX + window.getWidth() && mouseY >= window.posY && mouseY <= window.posY + window.getHeight()) {
                    return true;
                }
            }
        }

        return false;
    }

    private int getSelectedId() {
        //retrieve the color where the mouse is pointing
        FloatBuffer buffer = BufferUtils.createFloatBuffer(3);
        GL11.glReadPixels(Mouse.getX(), Mouse.getY(), 1, 1, GL11.GL_RGB, GL11.GL_FLOAT, buffer);
        int R = Math.round(buffer.get() * colors);
        int G = Math.round(buffer.get() * colors);
        int B = Math.round(buffer.get() * colors);

        //return the id based on the color, the color was generated from the id in the first place
        return R + G * colors + B * colors * colors - 1;
    }

    public void fakeRender() {
        GlStateManager.pushMatrix();

        //the background is black since that's teh color retrieved for sending in id = 0 in the color generator
        workspace.applyCamera();
        workspace.applyModelTranslation();

        ProjectInfo info = workspace.projectManager.projects.get(workspace.projectManager.selectedProject);

        GlStateManager.disableTexture2D();
        GlStateManager.disableLighting();
        GlStateManager.disableNormalize();

        GlStateManager.scale(1D / info.scale[0], 1D / info.scale[1], 1D / info.scale[2]);
        fakeRenderModel(info, 0.0625F);

        GlStateManager.enableNormalize();
        GlStateManager.enableTexture2D();

        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        GlStateManager.popMatrix();


    }

    public void fakeRenderSelectedCube(CubeInfo info)
    {
        List<CubeInfo> hidden = workspace.getHiddenElements();

        if(!(info.hidden || hidden.contains(info)))
        {
            GlStateManager.pushMatrix();

            //the background is black since that's teh color retrieved for sending in id = 0 in the color generator
            workspace.applyCamera();
            workspace.applyModelTranslation();

            ProjectInfo proj = workspace.projectManager.projects.get(workspace.projectManager.selectedProject);

            GlStateManager.disableTexture2D();
            GlStateManager.disableLighting();
            GlStateManager.disableNormalize();

            GlStateManager.scale(1D / proj.scale[0], 1D / proj.scale[1], 1D / proj.scale[2]);

            float f5 = 0.0625F;
            workspace.applyModelAnimations();

            if (!info.getChildren().isEmpty()) {
                info.modelCube.childModels.clear();
            }

            GlStateManager.pushMatrix();
            applyScale(info, f5);
            applyParentTransformations(proj.model.getParents(info), f5);
            int id = 0;
            applyColorAndFakeRender(info.modelCube, id++, f5);

            GlStateManager.pushMatrix();

            GlStateManager.translate(info.modelCube.offsetX, info.modelCube.offsetY, info.modelCube.offsetZ);
            GlStateManager.translate(info.modelCube.rotationPointX * f5, info.modelCube.rotationPointY * f5, info.modelCube.rotationPointZ * f5);

            if(info.modelCube.rotateAngleZ != 0.0F)
            {
                GlStateManager.rotate(info.modelCube.rotateAngleZ * (180F / (float)Math.PI), 0.0F, 0.0F, 1.0F);
            }

            if(info.modelCube.rotateAngleY != 0.0F)
            {
                GlStateManager.rotate(info.modelCube.rotateAngleY * (180F / (float)Math.PI), 0.0F, 1.0F, 0.0F);
            }

            if(info.modelCube.rotateAngleX != 0.0F)
            {
                GlStateManager.rotate(info.modelCube.rotateAngleX * (180F / (float)Math.PI), 1.0F, 0.0F, 0.0F);
            }

            if(!GuiScreen.isShiftKeyDown())
            {
                GlStateManager.disableDepth();
                GlStateManager.pushMatrix();
                float scale = 0.75F;
                GlStateManager.scale(scale, scale, scale);
                applyColor(id++);
                GlStateManager.rotate(90F, 1.0F, 0.0F, 0.0F);
                GlStateManager.rotate(90F, 0.0F, 0.0F, 1.0F);
                proj.model.rotationControls.render(f5);
                GlStateManager.rotate(90F, 0.0F, 0.0F, -1.0F);
                applyColor(id++);
                proj.model.rotationControls.render(f5);
                GlStateManager.rotate(90F, -1.0F, 0.0F, 0.0F);
                applyColor(id++);
                proj.model.rotationControls.render(f5);
                GlStateManager.popMatrix();
                GlStateManager.enableDepth();
            }
            else
            {
                GlStateManager.pushMatrix();
                float scale1 = 0.5F;
                GlStateManager.scale(scale1, scale1, scale1);

                GlStateManager.pushMatrix();
                GlStateManager.translate((0.125F * (info.dimensions[0] / 2D + info.offset[0])), (0.125D * (1D + (info.dimensions[1] + info.offset[1]) + info.mcScale)), (0.125F * (info.dimensions[2] / 2D + info.offset[2])));
                applyColor(id++);
                proj.model.sizeControls.render(f5);
                GlStateManager.popMatrix();

                GlStateManager.pushMatrix();
                GlStateManager.translate((0.125F * (info.dimensions[0] / 2D + info.offset[0])), -(0.125D * (1D - (info.offset[1]) + info.mcScale)), (0.125F * (info.dimensions[2] / 2D + info.offset[2])));
                GlStateManager.rotate(180F, 0F, 0F, -1F);
                applyColor(id++);
                proj.model.sizeControls.render(f5);
                GlStateManager.popMatrix();

                GlStateManager.pushMatrix();
                GlStateManager.translate((0.125F * (1D + info.dimensions[0] + info.offset[0] + info.mcScale)), (0.125D * ((info.dimensions[1] / 2D + info.offset[1]))), (0.125F * (info.dimensions[2] / 2D + info.offset[2])));
                GlStateManager.rotate(90F, 0F, 0F, -1F);
                applyColor(id++);
                proj.model.sizeControls.render(f5);
                GlStateManager.popMatrix();

                GlStateManager.pushMatrix();
                GlStateManager.translate(-(0.125F * (1D - info.offset[0] + info.mcScale)), (0.125D * ((info.dimensions[1] / 2D + info.offset[1]))), (0.125F * (info.dimensions[2] / 2D + info.offset[2])));
                GlStateManager.rotate(90F, 0F, 0F, 1F);
                applyColor(id++);
                proj.model.sizeControls.render(f5);
                GlStateManager.popMatrix();

                GlStateManager.pushMatrix();
                GlStateManager.translate((0.125F * (info.dimensions[0] / 2D + info.offset[0])), (0.125D * ((info.dimensions[1] / 2D + info.offset[1]))), (0.125F * (1D + info.dimensions[2] + info.offset[2] + info.mcScale)));
                GlStateManager.rotate(90F, 1F, 0F, 0F);
                applyColor(id++);
                proj.model.sizeControls.render(f5);
                GlStateManager.popMatrix();

                GlStateManager.pushMatrix();
                GlStateManager.translate((0.125F * (info.dimensions[0] / 2D + info.offset[0])), (0.125D * ((info.dimensions[1] / 2D + info.offset[1]))), -(0.125F * (1D - info.offset[2] + info.mcScale)));
                GlStateManager.rotate(90F, -1F, 0F, 0F);
                applyColor(id++);
                proj.model.sizeControls.render(f5);
                GlStateManager.popMatrix();

                GlStateManager.popMatrix();
            }

            GlStateManager.popMatrix();

            GlStateManager.popMatrix();

            workspace.resetModelAnimations();

            GlStateManager.enableNormalize();
            GlStateManager.enableTexture2D();

            GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
            GlStateManager.popMatrix();

            return;
        }
    }

    private void fakeRenderModel(ProjectInfo info, float f5) {
        List<CubeInfo> hidden = workspace.getHiddenElements();

        workspace.applyModelAnimations();

        int id = 1; //leave one id for the background
        for (int i = 0; i < info.model.cubes.size(); i++) {
            id = fakeRenderModelPart(info.model, info.model.cubes.get(i), hidden, id, f5, true);
        }

        workspace.resetModelAnimations();
    }

    private int fakeRenderModelPart(ModelBaseDummy model, CubeInfo info, List<CubeInfo> hidden, int id, float f5, boolean top) {
        if (info.hidden || hidden.contains(info)) {
            id = consumeIds(info, id);
        }else{

            //to be able to render child models in their own color, temporarily remove them
            if (!info.getChildren().isEmpty()) {
                info.modelCube.childModels.clear();
            }

            GlStateManager.pushMatrix();
            if (top) {
                applyScale(info, f5);
            }else{
                applyParentTransformations(model.getParents(info), f5);
            }
            applyColorAndFakeRender(info.modelCube, id++, f5);
            GlStateManager.popMatrix();

            for (CubeInfo cubeInfo : info.getChildren()) {
                //noinspection unchecked
                info.modelCube.childModels.add(cubeInfo.modelCube);
                id = fakeRenderModelPart(model, cubeInfo, hidden, id, f5, false);
            }
        }

        return id;
    }

    private int consumeIds(CubeInfo info, int id) {
        id++;
        for (CubeInfo cubeInfo : info.getChildren()) {
            id = consumeIds(cubeInfo, id);
        }
        return id;
    }

    private void applyColor(int id) {
        //calculate the color based on the id, the color can then be used to retrieve the id
        int R = id % colors;
        id /= colors;
        int G = id % colors;
        id /= colors;
        int B = id % colors;

        GlStateManager.color(R * color_precision, G * color_precision, B * color_precision, 1.0F);
    }

    private void applyColorAndFakeRender(ModelRenderer model, int id, float f5) {
        applyColor(id);
        model.render(f5);
    }

    private void applyScale(CubeInfo info, float f5) {
        GlStateManager.translate(info.modelCube.offsetX, info.modelCube.offsetY, info.modelCube.offsetZ);
        GlStateManager.translate(info.modelCube.rotationPointX * f5, info.modelCube.rotationPointY * f5, info.modelCube.rotationPointZ * f5);
        GlStateManager.scale(info.scale[0], info.scale[1], info.scale[2]);
        GlStateManager.translate(-info.modelCube.offsetX, -info.modelCube.offsetY, -info.modelCube.offsetZ);
        GlStateManager.translate(-info.modelCube.rotationPointX * f5, -info.modelCube.rotationPointY * f5, -info.modelCube.rotationPointZ * f5);
    }

    //Extracted from the ModelBaseDummy class
    private void applyParentTransformations(List<CubeInfo> parents, float f5) {
        for(int i = parents.size() - 1; i >= 0; i--) {
            CubeInfo parent = parents.get(i);

            if(i == parents.size() - 1) {
                GlStateManager.translate(parent.modelCube.offsetX, parent.modelCube.offsetY, parent.modelCube.offsetZ);
                GlStateManager.translate(parent.modelCube.rotationPointX * f5, parent.modelCube.rotationPointY * f5, parent.modelCube.rotationPointZ * f5);
                GlStateManager.scale(parent.scale[0], parent.scale[1], parent.scale[2]);
                GlStateManager.translate(-parent.modelCube.offsetX, -parent.modelCube.offsetY, -parent.modelCube.offsetZ);
                GlStateManager.translate(-parent.modelCube.rotationPointX * f5, -parent.modelCube.rotationPointY * f5, -parent.modelCube.rotationPointZ * f5);
            }

            GlStateManager.translate(parent.modelCube.offsetX, parent.modelCube.offsetY, parent.modelCube.offsetZ);
            GlStateManager.translate(parent.modelCube.rotationPointX * f5, parent.modelCube.rotationPointY * f5, parent.modelCube.rotationPointZ * f5);

            if(parent.modelCube.rotateAngleZ != 0.0F) {
                GlStateManager.rotate(parent.modelCube.rotateAngleZ * (180F / (float)Math.PI), 0.0F, 0.0F, 1.0F);
            }

            if(parent.modelCube.rotateAngleY != 0.0F) {
                GlStateManager.rotate(parent.modelCube.rotateAngleY * (180F / (float)Math.PI), 0.0F, 1.0F, 0.0F);
            }

            if(parent.modelCube.rotateAngleX != 0.0F){
                GlStateManager.rotate(parent.modelCube.rotateAngleX * (180F / (float)Math.PI), 1.0F, 0.0F, 0.0F);
            }

        }
    }
}
