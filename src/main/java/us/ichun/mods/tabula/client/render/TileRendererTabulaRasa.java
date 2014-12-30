package us.ichun.mods.tabula.client.render;

import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;
import us.ichun.mods.tabula.client.model.ModelWaxTablet;
import us.ichun.mods.tabula.common.tileentity.TileEntityTabulaRasa;

public class TileRendererTabulaRasa extends TileEntitySpecialRenderer
{
    public ModelWaxTablet model;
    public final ResourceLocation txModel = new ResourceLocation("tabula", "textures/model/tabulaRasa.png");

    public TileRendererTabulaRasa()
    {
        model = new ModelWaxTablet();
    }

    public void renderTabulaRasa(TileEntityTabulaRasa tr, double d, double d1, double d2, float f)
    {
        GL11.glPushMatrix();

        GL11.glTranslated(d + 0.5D, d1 + 1/16D, d2 + 0.5D);
        GL11.glScalef(-1F, -1F, 1F);

        GL11.glRotatef((tr.side * 90F), 0.0F, 1.0F, 0.0F);

        bindTexture(txModel);
        model.render(0.0625F);

        GL11.glPopMatrix();
    }

    @Override
    public void renderTileEntityAt(TileEntity tileentity, double d0, double d1, double d2, float f)
    {
        renderTabulaRasa((TileEntityTabulaRasa)tileentity, d0, d1, d2, f);
    }
}
