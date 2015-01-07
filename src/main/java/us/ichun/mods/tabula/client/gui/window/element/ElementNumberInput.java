package us.ichun.mods.tabula.client.gui.window.element;

import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.renderer.GlStateManager;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import us.ichun.mods.ichunutil.client.render.RendererHelper;
import us.ichun.mods.tabula.client.gui.Theme;
import us.ichun.mods.tabula.client.gui.window.Window;

import java.util.ArrayList;
import java.util.Locale;

public class ElementNumberInput extends Element
        implements ITextInput
{
    public ArrayList<GuiTextField> textFields = new ArrayList<GuiTextField>();
    public int selectedTextField = -1;

    public boolean allowDecimal;
    public String tooltip;
    public int spaceL;
    public int spaceR;

    public int min;
    public int max;

    public ElementNumberInput(Window window, int x, int y, int w, int h, int ID, String tip, int fieldCount, boolean allowDec, int minn, int maxx, double...args)
    {
        super(window, x, y, w, h, ID, false); //12 for height?

        for(int i = 0; i < fieldCount; i++)
        {
            GuiTextField textField = new GuiTextField(0, parent.workspace.getFontRenderer(), parent.posX + posX + 2 + ((width / fieldCount) * i), parent.posY + posY + 2, (width / fieldCount) - 18, parent.workspace.getFontRenderer().FONT_HEIGHT);
            textField.setMaxStringLength(20);
            textField.setEnableBackgroundDrawing(false);
            textField.setTextColor(Theme.getAsHex(Theme.instance.font));
            textField.setCanLoseFocus(false);
            if(i < args.length)
            {
                textField.setText(allowDec ? Double.toString(args[i]) : Integer.toString((int)args[i]));
            }

            textFields.add(textField);
        }
        allowDecimal = allowDec;
        tooltip = tip;
        spaceL = posX;
        spaceR = parent.width - posX - width;

        min = minn;
        max = maxx;
    }

    public ElementNumberInput(Window window, int x, int y, int w, int h, int ID, String tip, int fieldCount, boolean allowDec, int minn, int maxx)
    {
        this(window, x, y, w, h, ID, tip, fieldCount, allowDec, minn, maxx, new double[0]); //12 for height?
    }

    public ElementNumberInput(Window window, int x, int y, int w, int h, int ID, String tip, int fieldCount, boolean allowDec)
    {
        this(window, x, y, w, h, ID, tip, fieldCount, allowDec, Integer.MIN_VALUE, Integer.MAX_VALUE);
    }

    @Override
    public void update()
    {
        if(selectedTextField != -1)
        {
            textFields.get(selectedTextField).updateCursorCounter();
        }
    }

    @Override
    public boolean mouseScroll(int mouseX, int mouseY, int k)
    {
        for(int i = 0; i < textFields.size(); i++)
        {
            int x1 = posX + (width / textFields.size()) * (i);
            int x2 = posX + (width / textFields.size()) * (i + 1);
            int y1 = posY;
            int y2 = posY + height;

            if(mouseX >= x1 && mouseX < x2 && mouseY >= y1 && mouseY < y2)
            {
                if(selectedTextField != -1)
                {
                    textFields.get(selectedTextField).setFocused(false);
                }
                selectedTextField = i;
                textFields.get(selectedTextField).setFocused(true);

                String text = textFields.get(selectedTextField).getText();
                if(!text.isEmpty())
                {
                    if(allowDecimal)
                    {
                        String val = String.format(Locale.ENGLISH, "%.2f", Double.parseDouble(text) + (k > 0 ? (GuiScreen.isShiftKeyDown() ? 1D : GuiScreen.isCtrlKeyDown() ? 0.01D : 0.1D) : -(GuiScreen.isShiftKeyDown() ? 1D : GuiScreen.isCtrlKeyDown() ? 0.01D : 0.1D)));
                        if(val.contains(".") && val.length() > val.indexOf(".") + 4)
                        {
                            val = val.substring(0, val.indexOf(".") + 4);
                        }
                        textFields.get(selectedTextField).setText(val);

                        if(Double.parseDouble(textFields.get(selectedTextField).getText()) < min)
                        {
                            textFields.get(selectedTextField).setText(Double.toString(min));
                        }
                        if(Double.parseDouble(textFields.get(selectedTextField).getText()) > max)
                        {
                            textFields.get(selectedTextField).setText(Double.toString(max));
                        }
                    }
                    else
                    {
                        textFields.get(selectedTextField).setText(Integer.toString(Integer.parseInt(text) + (k > 0 ? 1 : -1)));
                        if(Integer.parseInt(textFields.get(selectedTextField).getText()) < min)
                        {
                            textFields.get(selectedTextField).setText(Integer.toString(min));
                        }
                        if(Integer.parseInt(textFields.get(selectedTextField).getText()) > max)
                        {
                            textFields.get(selectedTextField).setText(Integer.toString(max));
                        }
                    }
                }
                else
                {
                    textFields.get(selectedTextField).setText("0");
                }
                parent.elementTriggered(this);
                break;
            }
        }
        return true;//return true to say you're interacted with
    }

    @Override
    public boolean onClick(int mouseX, int mouseY, int id)
    {
        if(selectedTextField != -1)
        {
            if(textFields.get(selectedTextField).isFocused())
            {
                textFields.get(selectedTextField).mouseClicked(getPosX() + mouseX, getPosY() + mouseY, id);
            }
        }

        for(int i = 0; i < textFields.size(); i++)
        {
            int x1 = posX + (width / textFields.size()) * (i);
            int x2 = posX + (width / textFields.size()) * (i + 1) - 11;
            int y1 = posY;
            int y2 = posY + height;

            if(mouseX >= x1 && mouseX < x2 && mouseY >= y1 && mouseY < y2)
            {
                if(selectedTextField != -1)
                {
                    textFields.get(selectedTextField).setFocused(false);
                }
                selectedTextField = i;
                textFields.get(selectedTextField).setFocused(true);
                break;
            }

            x1 = posX + ((width / textFields.size()) * (i + 1)) - 12;
            x2 = posX + (width / textFields.size()) * (i + 1);

            if(mouseX >= x1 && mouseX < x2 && mouseY >= y1 && mouseY <= y1 + 6 && id == 0)
            {
                if(selectedTextField != -1)
                {
                    textFields.get(selectedTextField).setFocused(false);
                }
                selectedTextField = i;
                textFields.get(selectedTextField).setFocused(true);

                String text = textFields.get(selectedTextField).getText();
                if(!text.isEmpty())
                {
                    if(allowDecimal)
                    {
                        String val = String.format(Locale.ENGLISH, "%.2f", Double.parseDouble(text) + (GuiScreen.isShiftKeyDown() ? 1D : GuiScreen.isCtrlKeyDown() ? 0.01D : 0.1D));
                        if(val.contains(".") && val.length() > val.indexOf(".") + 4)
                        {
                            val = val.substring(0, val.indexOf(".") + 4);
                        }
                        textFields.get(selectedTextField).setText(val);

                        if(Double.parseDouble(textFields.get(selectedTextField).getText()) < min)
                        {
                            textFields.get(selectedTextField).setText(Double.toString(min));
                        }
                        if(Double.parseDouble(textFields.get(selectedTextField).getText()) > max)
                        {
                            textFields.get(selectedTextField).setText(Double.toString(max));
                        }
                    }
                    else
                    {
                        textFields.get(selectedTextField).setText(Integer.toString(Integer.parseInt(text) + 1));
                        if(Integer.parseInt(textFields.get(selectedTextField).getText()) < min)
                        {
                            textFields.get(selectedTextField).setText(Integer.toString(min));
                        }
                        if(Integer.parseInt(textFields.get(selectedTextField).getText()) > max)
                        {
                            textFields.get(selectedTextField).setText(Integer.toString(max));
                        }
                    }
                }
                parent.elementTriggered(this);
                break;
            }
            if(mouseX >= x1 && mouseX < x2 && mouseY >= y1 + 7 && mouseY <= y1 + 12 && id == 0)
            {
                if(selectedTextField != -1)
                {
                    textFields.get(selectedTextField).setFocused(false);
                }
                selectedTextField = i;
                textFields.get(selectedTextField).setFocused(true);

                String text = textFields.get(selectedTextField).getText();
                if(!text.isEmpty())
                {
                    if(allowDecimal)
                    {
                        String val = String.format(Locale.ENGLISH, "%.2f", Double.parseDouble(text) - (GuiScreen.isShiftKeyDown() ? 1D : GuiScreen.isCtrlKeyDown() ? 0.01D : 0.1D));
                        if(val.contains(".") && val.length() > val.indexOf(".") + 4)
                        {
                            val = val.substring(0, val.indexOf(".") + 4);
                        }
                        textFields.get(selectedTextField).setText(val);

                        if(Double.parseDouble(textFields.get(selectedTextField).getText()) < min)
                        {
                            textFields.get(selectedTextField).setText(Double.toString(min));
                        }
                        if(Double.parseDouble(textFields.get(selectedTextField).getText()) > max)
                        {
                            textFields.get(selectedTextField).setText(Double.toString(max));
                        }
                    }
                    else
                    {
                        textFields.get(selectedTextField).setText(Integer.toString(Integer.parseInt(text) - 1));
                        if(Integer.parseInt(textFields.get(selectedTextField).getText()) < min)
                        {
                            textFields.get(selectedTextField).setText(Integer.toString(min));
                        }
                        if(Integer.parseInt(textFields.get(selectedTextField).getText()) > max)
                        {
                            textFields.get(selectedTextField).setText(Integer.toString(max));
                        }
                    }
                }
                parent.elementTriggered(this);
                break;
            }
        }
        return true;
    }

    @Override
    public void draw(int mouseX, int mouseY, boolean hover)
    {
        RendererHelper.drawColourOnScreen(Theme.instance.elementInputBorder[0], Theme.instance.elementInputBorder[1], Theme.instance.elementInputBorder[2], 255, getPosX(), getPosY(), width - 2, height, 0);
        RendererHelper.drawColourOnScreen(Theme.instance.elementInputBackgroundInactive[0], Theme.instance.elementInputBackgroundInactive[1], Theme.instance.elementInputBackgroundInactive[2], 255, getPosX() + 1, getPosY() + 1, width - 2 - 2, height - 2, 0);

        for(int i = 0; i < textFields.size(); i++)
        {
            int x1 = posX + (width / textFields.size()) * (i);
            int x2 = posX + (width / textFields.size()) * (i + 1) - 11;
            int y1 = posY;
            int y2 = posY + height;

            RendererHelper.drawColourOnScreen(Theme.instance.elementInputBorder[0], Theme.instance.elementInputBorder[1], Theme.instance.elementInputBorder[2], 255, getPosX() + ((width / textFields.size()) * (i)), getPosY() + 1, 1, height - 2, 0);

            if(mouseX >= x1 && mouseX < x2 && mouseY >= y1 && mouseY < y2)
            {
                RendererHelper.drawColourOnScreen(Theme.instance.elementInputBackgroundHover[0], Theme.instance.elementInputBackgroundHover[1], Theme.instance.elementInputBackgroundHover[2], 255, getPosX() + ((width / textFields.size()) * (i)) + 1, getPosY() + 1, (x2 - x1) - 2, height - 2, 0);
            }

            if(textFields.get(i).getVisible())
            {
                textFields.get(i).drawTextBox();
            }

            x1 = posX + ((width / textFields.size()) * (i + 1)) - 12;
            x2 = posX + (width / textFields.size()) * (i + 1);

            if(mouseX >= x1 && mouseX < x2 && mouseY >= y1 && mouseY <= y1 + 6)
            {
                if(Mouse.isButtonDown(0))
                {
                    RendererHelper.drawColourOnScreen(Theme.instance.elementInputUpDownClick[0], Theme.instance.elementInputUpDownClick[1], Theme.instance.elementInputUpDownClick[2], 255, getPosX() + ((width / textFields.size()) * (i + 1)) - 12, getPosY(), 12, 6, 0);
                }
                else
                {
                    RendererHelper.drawColourOnScreen(Theme.instance.elementInputUpDownHover[0], Theme.instance.elementInputUpDownHover[1], Theme.instance.elementInputUpDownHover[2], 255, getPosX() + ((width / textFields.size()) * (i + 1)) - 12, getPosY(), 12, 6, 0);
                }
            }
            else
            {
                RendererHelper.drawColourOnScreen(Theme.instance.elementInputBorder[0], Theme.instance.elementInputBorder[1], Theme.instance.elementInputBorder[2], 255, getPosX() + ((width / textFields.size()) * (i + 1)) - 12, getPosY(), 12, 6, 0);
            }

            if(mouseX >= x1 && mouseX < x2 && mouseY >= y1 + 7 && mouseY <= y1 + 12)
            {
                if(Mouse.isButtonDown(0))
                {
                    RendererHelper.drawColourOnScreen(Theme.instance.elementInputUpDownClick[0], Theme.instance.elementInputUpDownClick[1], Theme.instance.elementInputUpDownClick[2], 255, getPosX() + ((width / textFields.size()) * (i + 1)) - 12, getPosY() + 6, 12, 6, 0);
                }
                else
                {
                    RendererHelper.drawColourOnScreen(Theme.instance.elementInputUpDownHover[0], Theme.instance.elementInputUpDownHover[1], Theme.instance.elementInputUpDownHover[2], 255, getPosX() + ((width / textFields.size()) * (i + 1)) - 12, getPosY() + 6, 12, 6, 0);
                }
            }
            else
            {
                RendererHelper.drawColourOnScreen(Theme.instance.elementInputBorder[0], Theme.instance.elementInputBorder[1], Theme.instance.elementInputBorder[2], 255, getPosX() + ((width / textFields.size()) * (i + 1)) - 12, getPosY() + 6, 12, 6, 0);
            }

            GlStateManager.pushMatrix();
            float scale = 1F;
            GlStateManager.scale(scale, scale, scale);
            parent.workspace.getFontRenderer().drawString("\u25B2", (int)((float)(getPosX() + ((width / textFields.size()) * (i + 1)) - 8) / scale), (int)((float)(getPosY() - 1) / scale), Theme.getAsHex(Theme.instance.font), false);//up
            parent.workspace.getFontRenderer().drawString("\u25BC", (int)((float)(getPosX() + ((width / textFields.size()) * (i + 1)) - 8) / scale), (int)((float)(getPosY() + 5) / scale), Theme.getAsHex(Theme.instance.font), false);//down
            GlStateManager.popMatrix();
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
        if(selectedTextField != -1)
        {
            if(Keyboard.KEY_D == key) // prevents users from hitting the D key
            {
                return;
            }
            else if(key == Keyboard.KEY_RETURN)
            {
                if(textFields.get(selectedTextField).getText().isEmpty())
                {
                    textFields.get(selectedTextField).setText("0");
                }
                parent.elementTriggered(this);
            }
            String oldText = textFields.get(selectedTextField).getText();
            textFields.get(selectedTextField).textboxKeyTyped(c, key);
            if(!textFields.get(selectedTextField).getText().isEmpty() && !(textFields.get(selectedTextField).getText().startsWith("-") & textFields.get(selectedTextField).getText().length() == 1))
            {
                try
                {
                    if(allowDecimal)
                    {
                        Double.parseDouble(textFields.get(selectedTextField).getText());
                    }
                    else
                    {
                        Integer.parseInt(textFields.get(selectedTextField).getText());
                    }
                }
                catch(NumberFormatException e)
                {
                    int pos = textFields.get(selectedTextField).getCursorPosition();
                    textFields.get(selectedTextField).setText(oldText);
                    textFields.get(selectedTextField).setCursorPosition(pos - 1);
                    return;
                }
            }
        }
    }

    @Override
    public void selected()
    {
    }

    @Override
    public void deselected()
    {
        if(selectedTextField != -1)
        {
            textFields.get(selectedTextField).setFocused(false);
        }
        selectedTextField = -1;
        resized();
    }

    @Override
    public void resized()
    {
        width = parent.width - spaceL - spaceR;
        for(int i = 0; i < textFields.size(); i++)
        {
            textFields.get(i).xPosition = parent.posX + posX + 2 + ((width / textFields.size()) * i);
            textFields.get(i).yPosition = parent.posY + posY + 2;
            textFields.get(i).width = (width / textFields.size()) - 18;
            textFields.get(i).setCursorPositionZero();
        }
    }

    @Override
    public String tooltip()
    {
        return tooltip; //return null for no tooltip. This is localized.
    }

    @Override
    public void tabHit()
    {
        if(selectedTextField < textFields.size() - 1)
        {
            if(selectedTextField != -1)
            {
                textFields.get(selectedTextField).setFocused(false);
            }
            selectedTextField++;
            textFields.get(selectedTextField).setFocused(true);
        }
        else
        {
            if(selectedTextField != -1)
            {
                textFields.get(selectedTextField).setFocused(false);
            }
            selectedTextField = -1;
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
    }

    @Override
    public void cycledTo()
    {
        parent.workspace.elementSelected = this;
        if(selectedTextField != -1)
        {
            textFields.get(selectedTextField).setFocused(false);
        }
        selectedTextField = 0;
        textFields.get(selectedTextField).setFocused(true);
    }
}
