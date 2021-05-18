package me.ichun.mods.tabula.client.export.types.headInfo;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import me.ichun.mods.ichunutil.api.common.head.HeadInfo;
import me.ichun.mods.ichunutil.client.model.tabula.ModelTabula;
import me.ichun.mods.ichunutil.common.module.tabula.project.Project;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.model.ModelRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.vector.Vector3f;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class PreviewRenderHandler
{
    private static final ResourceLocation TEX_GOOGLY_EYE = new ResourceLocation("tabula","textures/model/modelgooglyeye.png");
    private static final RenderType RENDER_TYPE = RenderType.getEntityCutout(TEX_GOOGLY_EYE);
    private static final RenderType RENDER_TYPE_EYES = RenderType.getEyes(TEX_GOOGLY_EYE);

    private static final ModelGooglyEye MODEL_GOOGLY_EYE = new ModelGooglyEye();
    private static final ItemStack ITEMSTACK_SLAB = new ItemStack(Blocks.SPRUCE_SLAB);

    public static void renderHeadInfoPreview(@Nonnull MatrixStack stack, @Nonnull HeadInfo helper, @Nonnull ModelTabula model, @Nonnull Project.Part selectedPart)
    {
        if(helper.noFaceInfo && helper.noTopInfo) //but why tho
        {
            return;
        }

        List<String> names = HeadInfo.DOT_SPLITTER.splitToList(helper.modelFieldName);
        ModelRenderer head = null;
        ArrayList<ModelRenderer> childs = new ArrayList<>();

        if(!names.isEmpty())
        {
            for(int i = 0; i < names.size(); i++)
            {
                String name = names.get(i);

                for(Map.Entry<Project.Part, ModelTabula.ModelRendererTabula> entry : model.partMap.entrySet())
                {
                    if(entry.getKey().name.equals(name))
                    {
                        //we found it;
                        if(i == 0)
                        {
                            head = entry.getValue();
                        }
                        else
                        {
                            childs.add(entry.getValue());
                        }
                    }
                }
            }
        }
        else
        {
            return;
        }

        if(!(head != null && childs.size() + 1 == names.size())) //we didn't find it all.
        {
            return;
        }

        helper.headModel = head;
        helper.childTranslates = childs.toArray(new ModelRenderer[0]);

        stack.push();

        LivingEntity living = null;
        float partialTicks = 1F;
        int packedLightIn = 0xf000f0;
        IRenderTypeBuffer.Impl bufferIn = Minecraft.getInstance().getRenderTypeBuffers().getBufferSource();

        if(!helper.noFaceInfo)
        {
            int eyeCount = Math.min(helper.getEyeCount(living), 2);

            for(int i = 0; i < eyeCount; i++)
            {
                float eyeScale = helper.getEyeScale(living, stack, partialTicks, i);

                if(eyeScale <= 0F)
                {
                    continue;
                }

                stack.push();

                float[] joint = helper.getHeadJointOffset(living, stack, partialTicks, -1);
                stack.translate(-joint[0], -joint[1], -joint[2]);

                stack.rotate(Vector3f.ZP.rotationDegrees(helper.getHeadRoll(living, stack, partialTicks, -1, i)));
                stack.rotate(Vector3f.YP.rotationDegrees(helper.getHeadYaw(living, stack, partialTicks, -1, i)));
                stack.rotate(Vector3f.XP.rotationDegrees(helper.getHeadPitch(living, stack, partialTicks, -1, i)));

                helper.postHeadTranslation(living, stack, partialTicks);

                float[] eyes = helper.getEyeOffsetFromJoint(living, stack, partialTicks, i);
                stack.translate(-(eyes[0] + helper.getEyeSideOffset(living, stack, partialTicks, i)), -eyes[1], -eyes[2]);

                stack.rotate(Vector3f.YP.rotationDegrees(helper.getEyeRotation(living, stack, partialTicks, i)));
                stack.rotate(Vector3f.XP.rotationDegrees(helper.getEyeTopRotation(living, stack, partialTicks, i)));

                stack.scale(eyeScale, eyeScale, eyeScale * 0.4F);

                //rendering the eyes
                IVertexBuilder buffer = bufferIn.getBuffer(RENDER_TYPE);

                int overlay = OverlayTexture.NO_OVERLAY;

                float[] irisColours = helper.getCorneaColours(living, stack, partialTicks, i);
                MODEL_GOOGLY_EYE.renderCornea(stack, buffer, packedLightIn, overlay, irisColours[0], irisColours[1], irisColours[2], 1F);

                float[] pupilColours = helper.getIrisColours(living, stack, partialTicks, i);

                float pupilScale = 1F;
                stack.push();
                stack.scale(pupilScale, pupilScale, 1F);
                MODEL_GOOGLY_EYE.renderIris(stack, buffer, packedLightIn, overlay, pupilColours[0], pupilColours[1], pupilColours[2], 1F);
                stack.pop();

                if(helper.doesEyeGlow(living, i))
                {
                    buffer = bufferIn.getBuffer(RENDER_TYPE_EYES);
                    MODEL_GOOGLY_EYE.renderCornea(stack, buffer, packedLightIn, overlay, irisColours[0], irisColours[1], irisColours[2], 1F);

                    stack.push();
                    stack.scale(pupilScale, pupilScale, 1F);
                    MODEL_GOOGLY_EYE.renderIris(stack, buffer, packedLightIn, overlay, pupilColours[0], pupilColours[1], pupilColours[2], 1F);
                    stack.pop();
                }
                //end rendering the eyes

                bufferIn.finish();

                stack.pop();
            }
        }

        if(!helper.noTopInfo)
        {
            int headCount = 1;

            for(int i = 0; i < headCount; i++)
            {
                float hatScale = helper.getHatScale(living, stack, partialTicks, i);

                if(hatScale <= 0F)
                {
                    continue;
                }
                hatScale *= 1.005F; //to reduce Z-fighting

                stack.push();

                float[] joint = helper.getHeadJointOffset(living, stack, partialTicks, i);
                stack.translate(-joint[0], -joint[1], -joint[2]); //to fight Z-fighting

                stack.rotate(Vector3f.ZP.rotationDegrees(helper.getHeadRoll(living, stack, partialTicks, i, -1)));
                stack.rotate(Vector3f.YP.rotationDegrees(helper.getHeadYaw(living, stack, partialTicks, i, -1)));
                stack.rotate(Vector3f.XP.rotationDegrees(helper.getHeadPitch(living, stack, partialTicks, i, -1)));

                helper.postHeadTranslation(living, stack, partialTicks);

                float[] headPoint = helper.getHatOffsetFromJoint(living, stack, partialTicks, i);
                stack.translate(-headPoint[0], -headPoint[1] - 0.00225F, -headPoint[2]);

                stack.rotate(Vector3f.YP.rotationDegrees(helper.getHatYaw(living, stack, partialTicks, i)));
                stack.rotate(Vector3f.XP.rotationDegrees(helper.getHatPitch(living, stack, partialTicks, i)));

                stack.scale(hatScale, hatScale, hatScale);

                stack.translate(-0.25D, -0.25D, -0.25D);
                stack.scale(0.5F, 0.5F, 0.5F);

                Block block = Blocks.SPRUCE_SLAB;
                Minecraft.getInstance().getItemRenderer().renderModel(Minecraft.getInstance().getBlockRendererDispatcher().getBlockModelShapes().getModel(block.getDefaultState()), ITEMSTACK_SLAB, packedLightIn, OverlayTexture.NO_OVERLAY, stack, bufferIn.getBuffer(RenderType.getSolid()));

                bufferIn.finish();

                stack.pop();
            }
        }

        stack.pop();
    }
}
