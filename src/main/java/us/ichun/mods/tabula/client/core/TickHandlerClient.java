package us.ichun.mods.tabula.client.core;

import com.mojang.util.UUIDTypeAdapter;
import cpw.mods.fml.client.FMLClientHandler;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.PositionedSoundRecord;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiMainMenu;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.resources.I18n;
import net.minecraft.init.Blocks;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;
import us.ichun.mods.tabula.client.mainframe.Mainframe;
import us.ichun.mods.tabula.client.gui.GuiWorkspace;

import java.util.UUID;

public class TickHandlerClient
{
    @SubscribeEvent
    public void onRenderTick(TickEvent.RenderTickEvent event)
    {
        if(event.phase == TickEvent.Phase.END)
        {
            Minecraft mc = Minecraft.getMinecraft();

            if(mc.currentScreen instanceof GuiMainMenu)
            {
                if(Keyboard.isKeyDown(Keyboard.KEY_T) && !keyTDown)
                {
                    initializeMainframe();
                }
                keyTDown = Keyboard.isKeyDown(Keyboard.KEY_T);
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

    public void initializeMainframe()
    {
        Minecraft mc = Minecraft.getMinecraft();
        mc.getSoundHandler().playSound(PositionedSoundRecord.func_147674_a(new ResourceLocation("gui.button.press"), 1.0F));

        int oriScale = mc.gameSettings.guiScale;
        mc.gameSettings.guiScale = mc.gameSettings.guiScale == 1 ? 1 : 2;
        mainframe = new Mainframe();
        UUID uuid;
        try
        {
            uuid = UUIDTypeAdapter.fromString(mc.getSession().getPlayerID());
        }
        catch(IllegalArgumentException e)
        {
            uuid = UUIDTypeAdapter.fromString("deadbeef-dead-beef-dead-beefdeadbeef");
        }
        mainframe.addListener(uuid, true);
        FMLClientHandler.instance().showGuiScreen(new GuiWorkspace(oriScale, false, true));
    }

    public Mainframe mainframe;

    private boolean keyTDown;
}
