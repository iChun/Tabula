package me.ichun.mods.tabula.client.gui.window;

import me.ichun.mods.ichunutil.client.gui.bns.window.Window;
import me.ichun.mods.ichunutil.client.gui.bns.window.constraint.Constraint;
import me.ichun.mods.ichunutil.client.gui.bns.window.view.View;
import me.ichun.mods.ichunutil.client.gui.bns.window.view.element.*;
import me.ichun.mods.tabula.client.gui.WorkspaceTabula;
import me.ichun.mods.tabula.client.tabula.Mainframe;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.I18n;

import javax.annotation.Nonnull;
import java.util.ArrayList;

public class WindowChat extends Window<WorkspaceTabula>
{
    public final Mainframe mainframe;
    public boolean firstUpdate = true;

    public WindowChat(WorkspaceTabula parent)
    {
        super(parent);
        mainframe = parent.mainframe;

        setView(new ViewChat(this));
        setId("windowChat");
        size(300, 200);

        ViewChat chat = (ViewChat)currentView;
        chat.chatMessages.setText(parent.mainframe.chatMessages);
        chat.chatMessages.init();
    }

    public void updateChat()
    {
        if(firstUpdate)
        {
            firstUpdate = false;

            if(!parent.isDocked(this) && getRight() < 0) //we're not docked, we're off screen
            {
                parent.addToDock(this, Constraint.Property.Type.BOTTOM);
            }
        }

        ViewChat chat = (ViewChat)currentView;

        float oldSize = chat.scroll.getScrollbarSize();

        boolean maintainScroll = chat.scroll.scrollProg == 1.0F;

        chat.chatMessages.setText(parent.mainframe.chatMessages);
        chat.chatMessages.init();

        if(maintainScroll || oldSize == 1.01F && chat.scroll.getScrollbarSize() < oldSize)
        {
            chat.scroll.setScrollProg(1F);

            chat.chatMessages.init();
        }
    }

    public static class ViewChat extends View<WindowChat>
    {
        public ElementTextWrapper chatMessages;
        public ElementScrollBar<?> scroll;

        public ViewChat(@Nonnull WindowChat parent)
        {
            super(parent, "window.chat.title");

            int spaceBottom = 4;

            ElementButton<?> button = new ElementButton<>(this, "window.chat.tabula", btn -> {
                if(I18n.format("window.chat.tabula").equals(btn.text))
                {
                    btn.text = I18n.format("window.chat.global");
                }
                else
                {
                    btn.text = I18n.format("window.chat.tabula");
                }
            });
            button.setWidth(60);
            button.setConstraint(new Constraint(button).left(this, Constraint.Property.Type.LEFT, spaceBottom).bottom(this, Constraint.Property.Type.BOTTOM, spaceBottom));
            elements.add(button);

            scroll = new ElementScrollBar<>(this, ElementScrollBar.Orientation.VERTICAL, 0.6F);
            scroll.setConstraint(new Constraint(scroll).top(this, Constraint.Property.Type.TOP, spaceBottom)
                    .bottom(button, Constraint.Property.Type.TOP, spaceBottom)
                    .right(this, Constraint.Property.Type.RIGHT, spaceBottom)
            );
            elements.add(scroll);

            ElementButton<?> users = null;
            if(parentFragment.parent.mainframe.getIsMaster())
            {
                users = new ElementButton<>(this, "<", btn -> {
                    if(btn.text.equalsIgnoreCase("<"))
                    {
                        //spawn users list
                        ElementList userList = new ElementList(this);
                        userList.setId("userList");
                        userList.setWidth(80);
                        userList.setConstraint(new Constraint(userList).top(this, Constraint.Property.Type.TOP, spaceBottom).right(this, Constraint.Property.Type.RIGHT, spaceBottom).bottom(button, Constraint.Property.Type.TOP, spaceBottom));
                        elements.add(userList);

                        parentFragment.parent.mainframe.listeners.forEach(name -> {
                            boolean isHost = name.equals(parentFragment.parent.mainframe.master);
                            boolean isEditor = parentFragment.parent.mainframe.editors.contains(name);
                            String tooltip = isHost ? I18n.format("system.host") : isEditor ? I18n.format("system.editor") : null;
                            ElementList.Item item = userList.addItem(name);
                            if(!isHost)
                            {
                                ArrayList<String> objects = new ArrayList<>();
                                objects.add(isEditor ? I18n.format("topdock.removeEditor") : I18n.format("topdock.addEditor"));
                                ElementContextMenu menu = new ElementContextMenu(item, objects, ((iContextMenu, item1) -> {
                                    if(item1.selected)
                                    {
                                        parentFragment.parent.mainframe.editorChange(name, !parentFragment.parent.mainframe.editors.contains(name));
                                        objects.clear();
                                        objects.add(parentFragment.parent.mainframe.editors.contains(name) ? I18n.format("topdock.removeEditor") : I18n.format("topdock.addEditor"));
                                    }
                                }));
                                item.addElement(menu);
                                item.addTextWrapper(name).setTooltip(tooltip);
                                menu.setConstraint(Constraint.matchParent(menu, item, item.getBorderSize()).bottom(((Element<?>)item.elements.get(1)), Constraint.Property.Type.BOTTOM, 0));
                                ((Element<?>)item.elements.get(1)).setTooltip(tooltip);
                            }
                            else
                            {
                                item.addTextWrapper(name).setTooltip(tooltip);
                                ((Element<?>)item.elements.get(0)).setTooltip(tooltip);
                            }
                        });

                        userList.init();
                        userList.init();

                        scroll.constraint.right(userList, Constraint.Property.Type.LEFT, spaceBottom);
                        scroll.constraint.apply();
                        this.resize(Minecraft.getInstance(), this.getParentWidth(), this.getParentHeight());

                        userList.init();

                        btn.text = ">";
                    }
                    else
                    {
                        //destroy list
                        elements.remove(getById("userList"));

                        scroll.constraint.right(this, Constraint.Property.Type.RIGHT, spaceBottom);
                        scroll.constraint.apply();
                        this.resize(Minecraft.getInstance(), this.getParentWidth(), this.getParentHeight());

                        btn.text = "<";
                    }
                });
                users.setSize(14, 14);
                users.setConstraint(new Constraint(users).right(this, Constraint.Property.Type.RIGHT, spaceBottom).bottom(this, Constraint.Property.Type.BOTTOM, spaceBottom));
                elements.add(users);
            }

            ElementTextField textField = new ElementTextField(this){
                @Override
                public boolean changeFocus(boolean direction)
                {
                    if(I18n.format("window.chat.tabula").equals(button.text))
                    {
                        button.text = I18n.format("window.chat.global");
                    }
                    else
                    {
                        button.text = I18n.format("window.chat.tabula");
                    }
                    return true; //we're not focused anyway, so, nah
                }
            };
            textField.setMaxStringLength(256);//match chat's
            textField.setTooltip(I18n.format("window.chat.textbox"));
            textField.setHeight(14);
            textField.setEnterResponder(s -> {
                if(!s.isEmpty())
                {
                    parent.mainframe.sendChat(s, s.startsWith("/") || button.text.equals(I18n.format("window.chat.global")));
                    textField.setText("");
                }
            });
            textField.setConstraint(new Constraint(textField).left(button, Constraint.Property.Type.RIGHT, spaceBottom).bottom(this, Constraint.Property.Type.BOTTOM, spaceBottom).right(users != null ? users : this, users != null ? Constraint.Property.Type.LEFT : Constraint.Property.Type.RIGHT, spaceBottom));
            elements.add(textField);

            ElementScrollView list = new ElementScrollView(this).setScrollVertical(scroll);
            list.setConstraint(new Constraint(list).bottom(textField, Constraint.Property.Type.TOP, spaceBottom).left(this, Constraint.Property.Type.LEFT, spaceBottom).right(scroll, Constraint.Property.Type.LEFT, 0).top(this, Constraint.Property.Type.TOP, spaceBottom));
            elements.add(list);

            chatMessages = new ElementTextWrapper(list);
            chatMessages.setConstraint(Constraint.sizeOnly(chatMessages));
            list.addElement(chatMessages);
        }
    }
}
