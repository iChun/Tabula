package me.ichun.mods.tabula.client.gui.window;

import com.mojang.blaze3d.systems.RenderSystem;
import me.ichun.mods.ichunutil.client.gui.bns.window.Window;
import me.ichun.mods.ichunutil.client.gui.bns.window.WindowPopup;
import me.ichun.mods.ichunutil.client.gui.bns.window.constraint.Constraint;
import me.ichun.mods.ichunutil.client.gui.bns.window.view.View;
import me.ichun.mods.ichunutil.client.gui.bns.window.view.element.*;
import me.ichun.mods.ichunutil.client.render.RenderHelper;
import me.ichun.mods.ichunutil.common.module.tabula.project.Project;
import me.ichun.mods.ichunutil.common.util.IOUtil;
import me.ichun.mods.tabula.client.gui.IProjectInfo;
import me.ichun.mods.tabula.client.gui.WorkspaceTabula;
import me.ichun.mods.tabula.client.gui.window.popup.WindowNewTexture;
import me.ichun.mods.tabula.client.tabula.Mainframe;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.ResourceLocation;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;

public class WindowTexture extends Window<WorkspaceTabula>
        implements IProjectInfo
{
    public WindowTexture(WorkspaceTabula parent)
    {
        super(parent);

        setView(new ViewTexture(this));
        setId("windowTexture");
        size(140, 122);
    }

    public static class ViewTexture extends View<WindowTexture>
            implements IProjectInfo
    {
        @Nullable
        public Mainframe.ProjectInfo currentInfo = null;

        public int listenTime;

        public ViewTexture(@Nonnull WindowTexture parent)
        {
            super(parent, "window.texture.title");

            ElementButtonTextured<?> btn = new ElementButtonTextured<>(this, new ResourceLocation("tabula", "textures/icon/cleartexture.png"), button -> {
                if(currentInfo != null)
                {
                    currentInfo.textureFile = null;
                    currentInfo.textureFileMd5 = null;
                    parentFragment.parent.mainframe.setImage(currentInfo, null);
                }
            });
            btn.setSize(20, 20).setTooltip(I18n.format("window.texture.clearTexture"));
            btn.setConstraint(new Constraint(btn).bottom(this, Constraint.Property.Type.BOTTOM, 2).right(this, Constraint.Property.Type.RIGHT, 2));
            elements.add(btn);

            ElementButtonTextured<?> btn1 = new ElementButtonTextured<>(this, new ResourceLocation("tabula", "textures/icon/newtexture.png"), button -> {
                if(currentInfo != null)
                {
                    getWorkspace().openWindowInCenter(new WindowNewTexture(getWorkspace(), currentInfo), 0.4D, 0.6D);
                }
            });
            btn1.setSize(20, 20).setTooltip(I18n.format("window.texture.loadTexture"));
            btn1.setConstraint(new Constraint(btn1).bottom(this, Constraint.Property.Type.BOTTOM, 2).right(btn, Constraint.Property.Type.LEFT, 0));
            elements.add(btn1);

            ElementToggleTextured<?> btn2 = new ElementToggleTextured<>(this, I18n.format("window.texture.hideTexture"), new ResourceLocation("tabula", "textures/icon/hidetexture.png"), button -> {
                if(currentInfo != null)
                {
                    currentInfo.hideTexture = button.toggleState;
                }
            });
            btn2.setSize(20, 20).setId("buttonHideTexture");
            btn2.setConstraint(new Constraint(btn2).bottom(this, Constraint.Property.Type.BOTTOM, 2).right(btn1, Constraint.Property.Type.LEFT, 0));
            elements.add(btn2);

            ElementButtonTextured<?> btn3 = new ElementButtonTextured<>(this, new ResourceLocation("tabula", "textures/icon/autolayout.png"), button -> {
                if(currentInfo != null)
                {
                    boolean[][] positions = new boolean[currentInfo.project.texWidth][currentInfo.project.texHeight];

                    ArrayList<Project.Part.Box> cubes = currentInfo.project.getAllBoxes();

                    for(Project.Part.Box cube : cubes) {
                        boolean collide = true;
                        int breakout = 0;
                        cube.texOffX = 0;
                        cube.texOffY = 0;

                        while (collide && cube.texOffX + (int)Math.ceil(cube.dimZ) * 2 + (int)Math.ceil(cube.dimX) * 2 < currentInfo.project.texWidth && cube.texOffY + (int)Math.ceil(cube.dimY) + (int)Math.ceil(cube.dimZ) < currentInfo.project.texHeight && breakout++ < 150000) {
                            collide = false;
                            for (int i = 0; i < (int)Math.ceil(cube.dimX); i++) {
                                for (int j = 0; j < (int)Math.ceil(cube.dimY); j++) {
                                    for (int k = 0; k < (int)Math.ceil(cube.dimZ); k++) {
                                        if (withinBounds(currentInfo.project.texWidth, currentInfo.project.texHeight, cube.texOffX + k, cube.texOffY + (int)Math.ceil(cube.dimZ) + j) && positions[cube.texOffX + k][cube.texOffY + (int)Math.ceil(cube.dimZ) + j]) {
                                            collide = true;
                                        }
                                        if (withinBounds(currentInfo.project.texWidth, currentInfo.project.texHeight, cube.texOffX + (int)Math.ceil(cube.dimZ) + i, cube.texOffY + (int)Math.ceil(cube.dimZ) + j) && positions[cube.texOffX + (int)Math.ceil(cube.dimZ) + i][cube.texOffY + (int)Math.ceil(cube.dimZ) + j]) {
                                            collide = true;
                                        }
                                        if (withinBounds(currentInfo.project.texWidth, currentInfo.project.texHeight, cube.texOffX + (int)Math.ceil(cube.dimZ) + (int)Math.ceil(cube.dimX) + k, cube.texOffY + (int)Math.ceil(cube.dimZ) + j) && positions[cube.texOffX + (int)Math.ceil(cube.dimZ) + (int)Math.ceil(cube.dimX) + k][cube.texOffY + (int)Math.ceil(cube.dimZ) + j]) {
                                            collide = true;
                                        }
                                        if (withinBounds(currentInfo.project.texWidth, currentInfo.project.texHeight, cube.texOffX + (int)Math.ceil(cube.dimZ) + (int)Math.ceil(cube.dimX) + (int)Math.ceil(cube.dimZ) + i, cube.texOffY + (int)Math.ceil(cube.dimZ) + j) && positions[cube.texOffX + (int)Math.ceil(cube.dimZ) + (int)Math.ceil(cube.dimX) + (int)Math.ceil(cube.dimZ) + i][cube.texOffY + (int)Math.ceil(cube.dimZ) + j]) {
                                            collide = true;
                                        }
                                        if (withinBounds(currentInfo.project.texWidth, currentInfo.project.texHeight, cube.texOffX + (int)Math.ceil(cube.dimZ) + i, cube.texOffY + k) && positions[cube.texOffX + (int)Math.ceil(cube.dimZ) + i][cube.texOffY + k]) {
                                            collide = true;
                                        }
                                        if (withinBounds(currentInfo.project.texWidth, currentInfo.project.texHeight, cube.texOffX + (int)Math.ceil(cube.dimZ) + (int)Math.ceil(cube.dimX) + i, cube.texOffY + k) && positions[cube.texOffX + (int)Math.ceil(cube.dimZ) + (int)Math.ceil(cube.dimX) + i][cube.texOffY + k]) {
                                            collide = true;
                                        }
                                    }
                                }
                            }

                            if (!collide) {
                                for (int i = 0; i < (int)Math.ceil(cube.dimX); i++) {
                                    for (int j = 0; j < (int)Math.ceil(cube.dimY); j++) {
                                        for (int k = 0; k < (int)Math.ceil(cube.dimZ); k++) {
                                            if (withinBounds(currentInfo.project.texWidth, currentInfo.project.texHeight, cube.texOffX + k, cube.texOffY + (int)Math.ceil(cube.dimZ) + j)) {
                                                positions[cube.texOffX + k][cube.texOffY + (int)Math.ceil(cube.dimZ) + j] = true;
                                            }
                                            if (withinBounds(currentInfo.project.texWidth, currentInfo.project.texHeight, cube.texOffX + (int)Math.ceil(cube.dimZ) + i, cube.texOffY + (int)Math.ceil(cube.dimZ) + j)) {
                                                positions[cube.texOffX + (int)Math.ceil(cube.dimZ) + i][cube.texOffY + (int)Math.ceil(cube.dimZ) + j] = true;
                                            }
                                            if (withinBounds(currentInfo.project.texWidth, currentInfo.project.texHeight, cube.texOffX + (int)Math.ceil(cube.dimZ) + (int)Math.ceil(cube.dimX) + k, cube.texOffY + (int)Math.ceil(cube.dimZ) + j)) {
                                                positions[cube.texOffX + (int)Math.ceil(cube.dimZ) + (int)Math.ceil(cube.dimX) + k][cube.texOffY + (int)Math.ceil(cube.dimZ) + j] = true;
                                            }
                                            if (withinBounds(currentInfo.project.texWidth, currentInfo.project.texHeight, cube.texOffX + (int)Math.ceil(cube.dimZ) + (int)Math.ceil(cube.dimX) + (int)Math.ceil(cube.dimZ) + i, cube.texOffY + (int)Math.ceil(cube.dimZ) + j)) {
                                                positions[cube.texOffX + (int)Math.ceil(cube.dimZ) + (int)Math.ceil(cube.dimX) + (int)Math.ceil(cube.dimZ) + i][cube.texOffY + (int)Math.ceil(cube.dimZ) + j] = true;
                                            }
                                            if (withinBounds(currentInfo.project.texWidth, currentInfo.project.texHeight, cube.texOffX + (int)Math.ceil(cube.dimZ) + i, cube.texOffY + k)) {
                                                positions[cube.texOffX + (int)Math.ceil(cube.dimZ) + i][cube.texOffY + k] = true;
                                            }
                                            if (withinBounds(currentInfo.project.texWidth, currentInfo.project.texHeight, cube.texOffX + (int)Math.ceil(cube.dimZ) + (int)Math.ceil(cube.dimX) + i, cube.texOffY + k)) {
                                                positions[cube.texOffX + (int)Math.ceil(cube.dimZ) + (int)Math.ceil(cube.dimX) + i][cube.texOffY + k] = true;
                                            }
                                        }
                                    }
                                }

                            }
                            if(collide) {
                                cube.texOffX++;
                                if(cube.texOffX + (int)Math.ceil(cube.dimZ) * 2 + (int)Math.ceil(cube.dimX) * 2 >= currentInfo.project.texWidth) {
                                    cube.texOffX = 0;
                                    cube.texOffY++;
                                }
                            }
                        }

                        if(breakout >= 150000 || collide) {
                            WindowPopup.popup(parent.parent, 0.4D, 0.4D, w->{}, I18n.format("window.autoLayout.failed"));
                        }
                    }

                    ArrayList<Project.Part> parts = currentInfo.project.getAllParts();
                    for(Project.Part part : parts)
                    {
                        int lowTexX = part.texWidth;
                        int lowTexY = part.texHeight;

                        for(Project.Part.Box box : part.boxes)
                        {
                            if(box.texOffX < lowTexX)
                            {
                                lowTexX = box.texOffX;
                            }
                            if(box.texOffY < lowTexY)
                            {
                                lowTexY = box.texOffY;
                            }
                        }

                        part.texOffX = lowTexX;
                        part.texOffY = lowTexY;
                        for(Project.Part.Box box : part.boxes)
                        {
                            box.texOffX -= lowTexX;
                            box.texOffY -= lowTexY;
                        }
                    }
                }
            });
            btn3.setSize(20, 20).setTooltip(I18n.format("topdock.autoLayout"));
            btn3.setConstraint(new Constraint(btn3).bottom(this, Constraint.Property.Type.BOTTOM, 2).right(btn2, Constraint.Property.Type.LEFT, 0));
            elements.add(btn3);

            ElementToggle<?> toggle = new ElementToggle<>(this, "window.texture.listenTexture", elementClickable -> {
                listenTime = 0;
            });
            toggle.setToggled(true).setSize(60, 20).setTooltip(I18n.format("window.texture.listenTextureFull")).setId("elementListenTex");
            toggle.setConstraint(new Constraint(toggle).bottom(this, Constraint.Property.Type.BOTTOM, 2).right(btn3, Constraint.Property.Type.LEFT, 0).left(this, Constraint.Property.Type.LEFT, 2));
            elements.add(toggle);

            ElementTextWrapper text = new ElementTextWrapper(this);
            text.setNoWrap().setText(" ").setConstraint(new Constraint(text).left(this, Constraint.Property.Type.LEFT, 2).bottom(toggle, Constraint.Property.Type.TOP, 1)).setId("elementTextWrapper");
            elements.add(text);

            ElementProjectTexture texture = new ElementProjectTexture(this);
            texture.setConstraint(new Constraint(texture).bottom(text, Constraint.Property.Type.TOP, 1).left(this, Constraint.Property.Type.LEFT, 2).right(this, Constraint.Property.Type.RIGHT, 2).top(this, Constraint.Property.Type.TOP, 2));
            elements.add(texture);
        }

        public boolean withinBounds(int textureWidth, int textureHeight, int x, int y)
        {
            return x >= 0 && x < textureWidth && y >= 0 && y < textureHeight;
        }

        @Override
        public void tick()
        {
            super.tick();

            listenTime++;
            if(listenTime > 20)
            {
                listenTime = 0;
                if(currentInfo != null)
                {
                    if(currentInfo.textureFile != null && currentInfo.textureFile.exists()) // we should listen
                    {
                        if(((ElementToggle)getById("elementListenTex")).toggleState)
                        {
                            //Check the file.
                            String md5 = IOUtil.getMD5Checksum(currentInfo.textureFile);
                            if(md5 != null && !md5.equals(currentInfo.textureFileMd5)) // file changed!
                            {
                                currentInfo.textureFileMd5 = md5;

                                BufferedImage image = null;
                                try
                                {
                                    image = ImageIO.read(currentInfo.textureFile);
                                }
                                catch(IOException ignored){}

                                parentFragment.parent.mainframe.setImage(currentInfo, image);
                            }
                        }
                    }
                }
            }
        }

        @Override
        public void setCurrentProject(Mainframe.ProjectInfo info)
        {
            currentInfo = info;
            listenTime = 0;
        }

        @Override
        public void projectChanged(ChangeType type)
        {
            if(type == ChangeType.TEXTURE || type == ChangeType.PROJECT)
            {
                if(currentInfo != null)
                {
                    listenTime = 0;
                    currentInfo.textureFileMd5 = null;
                    if(currentInfo.textureFile != null && currentInfo.textureFile.exists())
                    {
                        ((ElementTextWrapper)getById("elementTextWrapper")).setText(currentInfo.textureFile.getName());
                    }
                    else
                    {
                        ((ElementTextWrapper)getById("elementTextWrapper")).setText(I18n.format(currentInfo.project.getBufferedTexture() != null ? "window.texture.remoteTexture" : "window.texture.noTexture"));
                    }
                    ((ElementToggleTextured)getById("buttonHideTexture")).setToggled(currentInfo.hideTexture);
                }
            }
        }
    }

    public static class ElementProjectTexture extends Element<ViewTexture>
    {
        public ElementProjectTexture(@Nonnull ViewTexture parent)
        {
            super(parent);
        }

        @Override
        public void render(int mouseX, int mouseY, float partialTick)
        {
            if(parentFragment.currentInfo != null)
            {
                Project project = parentFragment.currentInfo.project;
                double w = width;
                double h = height;
                double rW = w / (double)project.texWidth;
                double rH = h / (double)project.texHeight;

                double max = Math.min(rW, rH);
                double w1 = (project.texWidth * max);
                double h1 = (project.texHeight * max);

                double offX = getLeft() + (width - w1) / 2D;
                double offY = getTop() + (height - h1) / 2D;

                RenderHelper.drawColour(200, 200, 200, 255, offX, offY, w1, h1, 0D);

                RenderSystem.enableBlend();
                RenderSystem.defaultBlendFunc();

                if(project.bufferedImageTexture != null)
                {
                    RenderSystem.enableAlphaTest();
                    RenderHelper.drawTexture(project.bufferedImageTexture.getResourceLocation(), offX, offY, w1, h1, 0D);
                }

                ArrayList<Project.Part.Box> boxes = project.getAllBoxes();

                double pX = offX;
                double pY = offY;

                for(Project.Part.Box box : boxes)
                {
                    Project.Part part = (Project.Part)box.parent;
                    int alpha = parentFragment.currentInfo.getSelectedBox() == box ? 125 : 30;
                    double ratio = (project.texWidth / w1);
                    RenderHelper.drawColour(255, 0, 0, alpha, pX + (part.texOffX + box.texOffX) / ratio                                                          , pY + (part.texOffY + box.texOffY) / ratio + box.dimZ / ratio, box.dimZ / ratio, box.dimY / ratio, 0D);
                    RenderHelper.drawColour(0, 0, 255, alpha, pX + (part.texOffX + box.texOffX) / ratio + box.dimZ / ratio                                       , pY + (part.texOffY + box.texOffY) / ratio + box.dimZ / ratio, box.dimX / ratio, box.dimY / ratio, 0D);
                    RenderHelper.drawColour(170, 0, 0, alpha, pX + (part.texOffX + box.texOffX) / ratio + box.dimZ / ratio + box.dimX / ratio                    , pY + (part.texOffY + box.texOffY) / ratio + box.dimZ / ratio, box.dimZ / ratio, box.dimY / ratio, 0D);
                    RenderHelper.drawColour(0, 0, 170, alpha, pX + (part.texOffX + box.texOffX) / ratio + box.dimZ / ratio + box.dimX / ratio  + box.dimZ / ratio, pY + (part.texOffY + box.texOffY) / ratio + box.dimZ / ratio, box.dimX / ratio, box.dimY / ratio, 0D);
                    RenderHelper.drawColour(0, 255, 0, alpha, pX + (part.texOffX + box.texOffX) / ratio + box.dimZ / ratio                                       , pY + (part.texOffY + box.texOffY) / ratio                   , box.dimX / ratio, box.dimZ / ratio, 0D);
                    RenderHelper.drawColour(0, 170, 0, alpha, pX + (part.texOffX + box.texOffX) / ratio + box.dimZ / ratio + box.dimX / ratio                    , pY + (part.texOffY + box.texOffY) / ratio                   , box.dimX / ratio, box.dimZ / ratio, 0D);

                }

                RenderSystem.disableBlend();
            }
        }
    }
}
