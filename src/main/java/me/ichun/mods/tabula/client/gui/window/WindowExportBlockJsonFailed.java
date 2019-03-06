package me.ichun.mods.tabula.client.gui.window;

import me.ichun.mods.ichunutil.client.gui.Theme;
import me.ichun.mods.ichunutil.client.gui.window.IWorkspace;
import me.ichun.mods.ichunutil.client.gui.window.WindowPopup;

public class WindowExportBlockJsonFailed extends WindowPopup {
    public String[] infoText;

    public WindowExportBlockJsonFailed(IWorkspace parent, int x, int y, int w, int h, int minW, int minH) {
        super(parent, x, y, w, h, minW, minH, "");
        this.titleLocale = "export.failed";
    }

    public void setInfoText(String text) {
        this.infoText = text.split("\n");
        int maxLength = 0;
        for(String s : this.infoText) {
            int size = s.length();
            if(size > maxLength) maxLength = size;
        }
        this.width = maxLength * 8;
        this.height = this.infoText.length * 10 + 40;
    }

    @Override
    public void draw(int mouseX, int mouseY) {
        super.draw(mouseX, mouseY);
        if (!this.minimized) {
            for (int i = 0; i < this.infoText.length; ++i) {
                String infoString = this.infoText[i];
                this.workspace.getFontRenderer().drawString(infoString, (float) (this.posX + 11), (float) (this.posY + 20) + 8.0F * i, Theme.getAsHex(this.workspace.currentTheme.font), false);
            }
        }
    }
}
