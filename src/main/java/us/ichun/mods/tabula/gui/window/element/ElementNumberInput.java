package us.ichun.mods.tabula.gui.window.element;

import ichun.client.render.RendererHelper;
import net.minecraft.client.gui.GuiTextField;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;
import us.ichun.mods.tabula.gui.Theme;
import us.ichun.mods.tabula.gui.window.Window;

import java.util.ArrayList;

public class ElementNumberInput extends Element
{
    public ArrayList<GuiTextField> textFields = new ArrayList<GuiTextField>();
    public int selectedTextField = -1;

    public boolean allowDecimal;
    public String tooltip;
    public int spaceL;
    public int spaceR;

    public ElementNumberInput(Window window, int x, int y, int w, int h, int ID, String tip, int fieldCount, boolean allowDec)
    {
        super(window, x, y, w, h, ID, false); //12 for height?

        for(int i = 0; i < fieldCount; i++)
        {
            GuiTextField textField = new GuiTextField(parent.workspace.getFontRenderer(), parent.posX + posX + 2 + ((width / fieldCount) * i), parent.posY + posY + 2, (width / fieldCount) - 22, parent.workspace.getFontRenderer().FONT_HEIGHT);
            textField.setMaxStringLength(20);
            textField.setEnableBackgroundDrawing(false);
            textField.setTextColor(Theme.getAsHex(Theme.font));
            textField.setCanLoseFocus(false);

            textFields.add(textField);
        }
        allowDecimal = allowDec;
        tooltip = tip;
        spaceL = posX;
        spaceR = parent.width - posX - width;
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
    public boolean onClick(int mouseX, int mouseY, int id)
    {
        if(selectedTextField != -1)
        {
            if(textFields.get(selectedTextField).isFocused())
            {
                textFields.get(selectedTextField).mouseClicked(getPosX() + mouseX - 18, getPosY() + mouseY, id);
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
                        String val = String.format("%.3f", Double.parseDouble(text) + 0.1D);
                        if(val.contains(".") && val.length() > val.indexOf(".") + 4)
                        {
                            val = val.substring(0, val.indexOf(".") + 4);
                        }
                        textFields.get(selectedTextField).setText(val);
                    }
                    else
                    {
                        textFields.get(selectedTextField).setText(Integer.toString(Integer.parseInt(text) + 1));
                    }
                }
                //TODO actually do something about the input
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
                        String val = String.format("%.3f", Double.parseDouble(text) - 0.1D);
                        if(val.contains(".") && val.length() > val.indexOf(".") + 4)
                        {
                            val = val.substring(0, val.indexOf(".") + 4);
                        }
                        textFields.get(selectedTextField).setText(val);
                    }
                    else
                    {
                        textFields.get(selectedTextField).setText(Integer.toString(Integer.parseInt(text) - 1));
                    }
                }
                //TODO actually do something about the input
                break;
            }
        }
        return true;
    }

    @Override
    public void draw(int mouseX, int mouseY, boolean hover)
    {
        RendererHelper.drawColourOnScreen(Theme.elementInputBorder[0], Theme.elementInputBorder[1], Theme.elementInputBorder[2], 255, getPosX(), getPosY(), width - 2, height, 0);
        RendererHelper.drawColourOnScreen(Theme.elementInputBackgroundInactive[0], Theme.elementInputBackgroundInactive[1], Theme.elementInputBackgroundInactive[2], 255, getPosX() + 1, getPosY() + 1, width - 2 - 2, height - 2, 0);

        for(int i = 0; i < textFields.size(); i++)
        {
            int x1 = posX + (width / textFields.size()) * (i);
            int x2 = posX + (width / textFields.size()) * (i + 1) - 11;
            int y1 = posY;
            int y2 = posY + height;

            RendererHelper.drawColourOnScreen(Theme.elementInputBorder[0], Theme.elementInputBorder[1], Theme.elementInputBorder[2], 255, getPosX() + ((width / textFields.size()) * (i)), getPosY() + 1, 1, height - 2, 0);

            if(mouseX >= x1 && mouseX < x2 && mouseY >= y1 && mouseY < y2)
            {
                RendererHelper.drawColourOnScreen(Theme.elementInputBackgroundHover[0], Theme.elementInputBackgroundHover[1], Theme.elementInputBackgroundHover[2], 255, getPosX() + ((width / textFields.size()) * (i)) + 1, getPosY() + 1, (x2 - x1) - 2, height - 2, 0);
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
                    RendererHelper.drawColourOnScreen(Theme.elementInputUpDownClick[0], Theme.elementInputUpDownClick[1], Theme.elementInputUpDownClick[2], 255, getPosX() + ((width / textFields.size()) * (i + 1)) - 12, getPosY(), 12, 6, 0);
                }
                else
                {
                    RendererHelper.drawColourOnScreen(Theme.elementInputUpDownHover[0], Theme.elementInputUpDownHover[1], Theme.elementInputUpDownHover[2], 255, getPosX() + ((width / textFields.size()) * (i + 1)) - 12, getPosY(), 12, 6, 0);
                }
            }
            else
            {
                RendererHelper.drawColourOnScreen(Theme.elementInputBorder[0], Theme.elementInputBorder[1], Theme.elementInputBorder[2], 255, getPosX() + ((width / textFields.size()) * (i + 1)) - 12, getPosY(), 12, 6, 0);
            }

            if(mouseX >= x1 && mouseX < x2 && mouseY >= y1 + 7 && mouseY <= y1 + 12)
            {
                if(Mouse.isButtonDown(0))
                {
                    RendererHelper.drawColourOnScreen(Theme.elementInputUpDownClick[0], Theme.elementInputUpDownClick[1], Theme.elementInputUpDownClick[2], 255, getPosX() + ((width / textFields.size()) * (i + 1)) - 12, getPosY() + 6, 12, 6, 0);
                }
                else
                {
                    RendererHelper.drawColourOnScreen(Theme.elementInputUpDownHover[0], Theme.elementInputUpDownHover[1], Theme.elementInputUpDownHover[2], 255, getPosX() + ((width / textFields.size()) * (i + 1)) - 12, getPosY() + 6, 12, 6, 0);
                }
            }
            else
            {
                RendererHelper.drawColourOnScreen(Theme.elementInputBorder[0], Theme.elementInputBorder[1], Theme.elementInputBorder[2], 255, getPosX() + ((width / textFields.size()) * (i + 1)) - 12, getPosY() + 6, 12, 6, 0);
            }

            GL11.glPushMatrix();
            float scale = 1F;
            GL11.glScalef(scale, scale, scale);
            parent.workspace.getFontRenderer().drawString("\u25B2", (int)((float)(getPosX() + ((width / textFields.size()) * (i + 1)) - 8) / scale), (int)((float)(getPosY() - 1) / scale), Theme.getAsHex(Theme.font), false);//up
            parent.workspace.getFontRenderer().drawString("\u25BC", (int)((float)(getPosX() + ((width / textFields.size()) * (i + 1)) - 8) / scale), (int)((float)(getPosY() + 5) / scale), Theme.getAsHex(Theme.font), false);//down
            GL11.glPopMatrix();
        }
    }

    @Override
    public void keyInput(char c, int key)
    {
        if(selectedTextField != -1)
        {
            if(Keyboard.KEY_RETURN == key)
            {
                //TODO return input
            }
            if(Keyboard.KEY_D == key) // prevents users from hitting the D key
            {
                return;
            }
            String oldText = textFields.get(selectedTextField).getText();
            textFields.get(selectedTextField).textboxKeyTyped(c, key);
            if(!textFields.get(selectedTextField).getText().isEmpty())
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
    }

    @Override
    public void resized()
    {
        width = parent.width - spaceL - spaceR;
        for(int i = 0; i < textFields.size(); i++)
        {
            textFields.get(i).xPosition = parent.posX + posX + 2 + ((width / textFields.size()) * i);
            textFields.get(i).yPosition = parent.posY + posY + 2;
            textFields.get(i).width = (width / textFields.size()) - 22;
        }
        //TODO adjust width then adjust the text fields.
    }

    @Override
    public String tooltip()
    {
        return tooltip; //return null for no tooltip. This is localized.
    }
}
