package me.ichun.mods.tabula.client.gui.window.element;

import me.ichun.mods.ichunutil.client.gui.window.Window;
import me.ichun.mods.ichunutil.client.gui.window.element.ElementTextInput;

public class ElementTextInputCubeName extends ElementTextInput
{
    public ElementTextInputCubeName(Window window, int x, int y, int w, int h)
    {
        super(window, x, y, w, h, 0, "window.controls.cubeName");
    }

    @Override
    public void checkAndCorrectText(String oldText)//Text needs to be corrected cause this becomes a field name in the future
    {
        String newString = textField.getText();
        if(newString.isEmpty())
        {
            return;
        }
        boolean reject = false;
        try
        {
            Integer.parseInt(newString.substring(0, 1));
            reject = true;
        }
        catch(NumberFormatException e)
        {
        }
        if(newString.contains(" "))
        {
            reject = true;
        }
        if(reject)
        {
            int pos = textField.getCursorPosition();
            textField.setText(oldText);
            textField.setCursorPosition(pos - 1);
        }
    }
}
