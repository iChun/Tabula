package us.ichun.mods.tabula.client.core;

import com.google.common.io.ByteStreams;
import com.google.gson.Gson;
import com.mojang.util.UUIDTypeAdapter;
import cpw.mods.fml.client.FMLClientHandler;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.PositionedSoundRecord;
import net.minecraft.client.gui.GuiMainMenu;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.input.Keyboard;
import us.ichun.mods.tabula.client.gui.GuiWorkspace;
import us.ichun.mods.tabula.client.gui.Theme;
import us.ichun.mods.tabula.client.mainframe.Mainframe;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
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

        File defaultTheme = new File(ResourceHelper.getThemesDir(), "default.json");

        try
        {
            InputStream con = new FileInputStream(defaultTheme);
            String data = new String(ByteStreams.toByteArray(con));
            con.close();

            Theme.loadTheme((new Gson()).fromJson(data, Theme.class));
        }
        catch(Exception e)
        {
        }

        FMLClientHandler.instance().showGuiScreen(new GuiWorkspace(oriScale, false, true));
    }

    public Mainframe mainframe;

    private boolean keyTDown;
}
