package us.ichun.mods.tabula.client.gui.window.element;

import ichun.client.render.RendererHelper;
import net.minecraft.client.gui.GuiTextField;
import org.lwjgl.input.Keyboard;
import us.ichun.mods.tabula.client.gui.window.Window;
import us.ichun.mods.tabula.client.gui.Theme;

public class ElementTextInput extends Element
        implements ITextInput
{
    public GuiTextField textField;
    public String tooltip;
    public int spacing;

    public ElementTextInput(Window window, int x, int y, int w, int h, int ID, String tip, int maxLength)
    {
        super(window, x, y, w, 12, ID, false); //12 for height?
        textField = new GuiTextField(parent.workspace.getFontRenderer(), parent.posX + posX + 2, parent.posY + posY + 2, width - 9, parent.workspace.getFontRenderer().FONT_HEIGHT);
        textField.setMaxStringLength(maxLength);
        textField.setEnableBackgroundDrawing(false);
        textField.setTextColor(Theme.getAsHex(Theme.font));
        textField.setCanLoseFocus(false);
        tooltip = tip;
        spacing = parent.width - width - x;
    }

    public ElementTextInput(Window window, int x, int y, int w, int h, int ID, String tip)
    {
        this(window, x, y, w, h, ID, tip, 80);
    }

    @Override
    public void update()
    {
        textField.updateCursorCounter();
        textField.setTextColor(Theme.getAsHex(Theme.font));
    }

    @Override
    public boolean onClick(int mouseX, int mouseY, int id)
    {
        if(textField.isFocused())
        {
            textField.mouseClicked(getPosX() + mouseX, getPosY() + mouseY, id);
        }
        return true;
    }

    @Override
    public void draw(int mouseX, int mouseY, boolean hover)
    {
        RendererHelper.drawColourOnScreen(Theme.elementInputBorder[0], Theme.elementInputBorder[1], Theme.elementInputBorder[2], 255, getPosX(), getPosY(), width, height, 0);
        if(hover)
        {
            RendererHelper.drawColourOnScreen(Theme.elementInputBackgroundHover[0], Theme.elementInputBackgroundHover[1], Theme.elementInputBackgroundHover[2], 255, getPosX() + 1, getPosY() + 1, width - 2, height - 2, 0);
        }
        else
        {
            RendererHelper.drawColourOnScreen(Theme.elementInputBackgroundInactive[0], Theme.elementInputBackgroundInactive[1], Theme.elementInputBackgroundInactive[2], 255, getPosX() + 1, getPosY() + 1, width - 2, height - 2, 0);
        }

        if(textField.getVisible())
        {
            textField.drawTextBox();
        }
    }

    @Override
    public void keyInput(char c, int key)
    {
        if(key == Keyboard.KEY_TAB)
        {
            tabHit();
            return;
        }
        else if(key == Keyboard.KEY_RETURN)
        {
            parent.elementTriggered(this);
        }
        String prevText = textField.getText();
        textField.textboxKeyTyped(c, key);
        checkAndCorrectText(prevText);
    }

    public void checkAndCorrectText(String oldText)
    {
    }

    @Override
    public void selected()
    {
        textField.setFocused(true);
    }

    @Override
    public void deselected()
    {
        textField.setFocused(false);
        resized();
    }

    @Override
    public void resized()
    {
        textField.xPosition = parent.posX + posX + 2;
        textField.yPosition = parent.posY + posY + 2;
        textField.width = width - 9;
        textField.setCursorPositionZero();
        width = parent.width - posX - spacing;
    }

    @Override
    public String tooltip()
    {
        return tooltip; //return null for no tooltip. This is localized.
    }

    @Override
    public void tabHit()
    {
        deselected();
        boolean found = false;
        boolean foundSelf = false;
        for(int i = 0; i < parent.elements.size(); i++)
        {
            Element e = parent.elements.get(i);
            if(e == this)
            {
                foundSelf = true;
                continue;
            }
            if(e instanceof ITextInput && foundSelf)
            {
                found = true;
                ((ITextInput)e).cycledTo();
                break;
            }
        }
        if(!found)
        {
            for(int i = 0; i < parent.elements.size(); i++)
            {
                Element e = parent.elements.get(i);
                if(e instanceof ITextInput)
                {
                    ((ITextInput)e).cycledTo();
                    break;
                }
            }
        }
    }

    @Override
    public void cycledTo()
    {
        parent.workspace.elementSelected = this;
        selected();
    }
}
