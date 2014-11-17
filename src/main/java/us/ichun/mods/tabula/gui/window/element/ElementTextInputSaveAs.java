package us.ichun.mods.tabula.gui.window.element;

import us.ichun.mods.tabula.gui.window.Window;

public class ElementTextInputSaveAs extends ElementTextInput
{
    private static final String[] invalidChars = new String[] { "\\", "/", ":", "*", "?", "\"", "<", ">", "|" };

    public ElementTextInputSaveAs(Window window, int x, int y, int w, int h, int ID, String tip)
    {
        super(window, x, y, w, h, ID, tip, 60);
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
        for(String c : invalidChars)
        {
            if(newString.contains(c))
            {
                reject = true;
                break;
            }
        }
        if(reject)
        {
            int pos = textField.getCursorPosition();
            textField.setText(oldText);
            textField.setCursorPosition(pos - 1);
        }
    }
}
