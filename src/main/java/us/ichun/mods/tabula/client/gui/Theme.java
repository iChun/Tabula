package us.ichun.mods.tabula.client.gui;

import java.lang.reflect.Field;

public class Theme
{
    public static transient Theme instance = new Theme();

    public String name = "Default";

    public int[] workspaceBackground                 = new int[] { 204, 204, 204 };
    public int[] windowBorder                        = new int[] { 150, 150, 150 };
    public int[] windowBackground                    = new int[] { 34, 34, 34 };
    public int[] tabBorder                           = new int[] { 255, 255, 255 };
    public int[] tabSideInactive                     = new int[] { 100, 100, 100 };

    public int[] elementInputBackgroundInactive      = new int[] { 60, 60, 60 };
    public int[] elementInputBackgroundHover         = new int[] { 70, 70, 70 };
    public int[] elementInputBorder                  = new int[] { 140, 140, 140 };
    public int[] elementInputUpDownHover             = new int[] { 170, 170, 170 };
    public int[] elementInputUpDownClick             = new int[] { 100, 100, 100 };

    public int[] elementButtonBackgroundInactive      = new int[] { 60, 60, 60 };
    public int[] elementButtonBackgroundHover         = new int[] { 70, 70, 70 };
    public int[] elementButtonBorder                  = new int[] { 140, 140, 140 };
    public int[] elementButtonClick                   = new int[] { 100, 100, 100 };
    public int[] elementButtonToggle                  = new int[] { 30, 30, 30 };
    public int[] elementButtonToggleHover             = new int[] { 170, 170, 170 };

    public int[] elementProjectTabActive             = new int[] { 60, 60, 60 };
    public int[] elementProjectTabHover              = new int[] { 100, 100, 100 };
    public int[] elementProjectTabFont               = new int[] { 140, 140, 140 };
    public int[] elementProjectTabFontChanges        = new int[] { 255, 255, 255 };

    public int[] elementTreeBorder                   = new int[] { 100, 100, 100 };
    public int[] elementTreeScrollBar                = new int[] { 34, 34, 34 };
    public int[] elementTreeScrollBarBorder          = new int[] { 60, 60, 60 };

    public int[] elementTreeItemBorder               = new int[] { 40, 40, 40 };
    public int[] elementTreeItemBg                   = new int[] { 60, 60, 60 };
    public int[] elementTreeItemBgSelect             = new int[] { 100, 100, 100 };
    public int[] elementTreeItemBgHover              = new int[] { 120, 120, 120 };

    public int[] fontChat                            = new int[] { 220, 220, 220 };
    public int[] font                                = new int[] { 255, 255, 255 };
    public int[] fontDim                             = new int[] { 150, 150, 150 };

    public BlockInfo workspaceBlock = new BlockInfo();

    public class BlockInfo
    {
        public String block = "minecraft:planks";
        public int metadata = 1;
    }

    public static void loadTheme(Theme theme)
    {
        if(theme == null)
        {
            return;
        }

        Field[] fields = Theme.class.getDeclaredFields();
        try
        {
            for(Field f : fields)
            {
                Object obj = f.get(theme);
                if(obj != null)
                {
                    f.set(instance, obj);
                }
            }
        }
        catch(Exception e)
        {
        }
    }

    public static void changeColour(int[] set, int r, int g, int b)
    {
        set[0] = r;
        set[1] = g;
        set[2] = b;
    }

    public static int getAsHex(int[] set)
    {
        return (set[0] << 16) + (set[1] << 8) + (set[2]);
    }
}
