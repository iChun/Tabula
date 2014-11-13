package us.ichun.mods.tabula.client.core;

import cpw.mods.fml.common.FMLCommonHandler;
import us.ichun.mods.tabula.common.core.CommonProxy;

public class ClientProxy extends CommonProxy
{
    @Override
    public void init()
    {
        super.init();

        tickHandlerClient = new TickHandlerClient();
        FMLCommonHandler.instance().bus().register(tickHandlerClient);
    }
}
