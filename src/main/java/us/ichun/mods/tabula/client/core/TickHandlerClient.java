package us.ichun.mods.tabula.client.core;

import com.mojang.util.UUIDTypeAdapter;
import cpw.mods.fml.client.FMLClientHandler;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiMainMenu;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.resources.I18n;
import net.minecraft.init.Blocks;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;
import us.ichun.mods.tabula.client.mainframe.Mainframe;
import us.ichun.mods.tabula.client.gui.GuiWorkspace;

public class TickHandlerClient
{
    public TickHandlerClient()
    {
        btnDummy = new GuiButton(0, btnX, btnY, 20, 20, "T");
    }

    @SubscribeEvent
    public void onRenderTick(TickEvent.RenderTickEvent event)
    {
        if(event.phase == TickEvent.Phase.END)
        {
            Minecraft mc = Minecraft.getMinecraft();

            if(mc.currentScreen instanceof GuiMainMenu)
            {
                btnX = mc.currentScreen.width / 2 - 124;
                btnY = mc.currentScreen.height / 4 + 48 + 24 * 2;
                btnDummy.xPosition = btnX;
                btnDummy.yPosition = btnY;

                ScaledResolution reso = new ScaledResolution(mc, mc.displayWidth, mc.displayHeight);
                int i = Mouse.getX() * reso.getScaledWidth() / mc.displayWidth;
                int j = reso.getScaledHeight() - Mouse.getY() * reso.getScaledHeight() / mc.displayHeight - 1;
                if(Mouse.isButtonDown(0) && !mouseDown)
                {
                    if(i >= btnX && i <= btnX + 20 && j >= btnY && j <= btnY + 20)
                    {
                        btnDummy.func_146113_a(mc.getSoundHandler());
                        int oriScale = mc.gameSettings.guiScale;
                        mc.gameSettings.guiScale = mc.gameSettings.guiScale == 1 ? 1 : 2;
                        mainframe = new Mainframe();
                        mainframe.addListener(UUIDTypeAdapter.fromString(mc.getSession().getPlayerID()), true);
                        FMLClientHandler.instance().showGuiScreen(new GuiWorkspace(oriScale, false, true));
                    }
                }
                btnDummy.drawButton(mc, i, j);

                mouseDown = Mouse.isButtonDown(0);
            }
        }
    }

    @SubscribeEvent
    public void onClientTick(TickEvent.ClientTickEvent event)
    {
        if(event.phase == TickEvent.Phase.END)
        {
            if(mainframe != null)
            {
                mainframe.tick();
            }
        }
    }

    public Mainframe mainframe;

    private int btnX = 0;
    private int btnY = 0;
    private boolean mouseDown;
    private GuiButton btnDummy;
}
