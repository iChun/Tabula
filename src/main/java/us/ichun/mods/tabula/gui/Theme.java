package us.ichun.mods.tabula.gui;

public class Theme
{
    public static int[] workspaceBackground                 = new int[] { 204, 204, 204 };
    public static int[] windowBorder                        = new int[] { 150, 150, 150 };
    public static int[] windowBackground                    = new int[] { 34, 34, 34 };
    public static int[] tabBorder                           = new int[] { 255, 255, 255 };
    public static int[] tabSideInactive                     = new int[] { 100, 100, 100 };

    public static int[] elementInputBackgroundInactive      = new int[] { 60, 60, 60 };
    public static int[] elementInputBackgroundHover         = new int[] { 70, 70, 70 };
    public static int[] elementInputBorder                  = new int[] { 140, 140, 140 };
    public static int[] elementInputUpDownHover             = new int[] { 170, 170, 170 };
    public static int[] elementInputUpDownClick             = new int[] { 100, 100, 100 };

    public static int[] elementButtonBackgroundInactive      = new int[] { 60, 60, 60 };
    public static int[] elementButtonBackgroundHover         = new int[] { 70, 70, 70 };
    public static int[] elementButtonBorder                  = new int[] { 140, 140, 140 };
    public static int[] elementButtonClick                   = new int[] { 100, 100, 100 };

    public static int[] font                                = new int[] { 255, 255, 255 };
    
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
