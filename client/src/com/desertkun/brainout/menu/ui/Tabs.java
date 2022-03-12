package com.desertkun.brainout.menu.ui;

import com.badlogic.gdx.scenes.scene2d.*;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.scenes.scene2d.utils.NinePatchDrawable;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ObjectMap;
import com.badlogic.gdx.utils.Scaling;
import com.desertkun.brainout.BrainOutClient;
import com.desertkun.brainout.menu.Menu;

public class Tabs extends Table
{
    private final Table buttons;
    private final Table content;

    private Array<Tab> tabs;
    private ObjectMap<Object, Tab> tabKeys;
    private ButtonGroup<Button> buttonGroup;
    private Tab currentTab;

    public abstract static class TabSelectedListener implements EventListener
    {
        public abstract boolean selected();

        @Override
        public boolean handle(Event event)
        {
            if (!(event instanceof TabSelectedEvent)) return false;
            return selected();
        }

        public static class TabSelectedEvent extends Event
        {

        }
    }


    public Tabs(Skin skin)
    {
        setSkin(skin);

        align(Align.top);

        tabs = new Array<>();
        tabKeys = new ObjectMap<>();
        buttonGroup = new ButtonGroup<>();

        buttons = new Table();
        buttons.align(Align.left);

        content = new Table(BrainOutClient.Skin);
        content.setBackground(getContentBackground());

        add(buttons).expandX().fillX().row();
        add(content).expand().fill().row();
    }

    protected String getContentBackground()
    {
        return "edit-default";
    }

    protected String getTabButtonStyle()
    {
        return "button-checkable";
    }

    public static class Tab extends Table
    {
        private Cell<Button> tabCell;
        private Object key;
        private Button tabButton;

        public Tab()
        {
        }

        public void setKey(Object key)
        {
            this.key = key;
        }

        public void setTabButton(Button tabButton)
        {
            this.tabButton = tabButton;
        }

        public void setTabCell(Cell<Button> tabCell)
        {
            this.tabCell = tabCell;
        }

        public Button getTabButton()
        {
            return tabButton;
        }

        public Cell<Button> getTabCell()
        {
            return tabCell;
        }

        protected void selected()
        {
            fire(new TabSelectedListener.TabSelectedEvent());
        }

        public Tab buttonRight()
        {
            tabCell.expandX().right();
            return this;
        }

        public Tab size(int w, int h)
        {
            tabCell.size(w, h);
            return this;
        }

        public Tab defSize()
        {
            tabCell.size(126, 32);
            return this;
        }

        public Tab fillButton()
        {
            tabCell.expandX().fillX();
            return this;
        }

        public Tab padRight(int pad)
        {
            tabCell.padRight(pad);
            return this;
        }

        public Object getKey()
        {
            return key;
        }
    }

    public Tab addTab(String title)
    {
        return addTab(title, null);
    }
    public Tab addTabIcon(String icon)
    {
        return addTabIcon(icon, null);
    }

    public Tab addTab(String title, Object key)
    {
        return addTab(title, key, new Tab());
    }

    public Tab addTabIcon(String icon, Object key)
    {
        return addTabIcon(icon, key, new Tab());
    }

    public <T extends Actor> Cell<T> addFakeActor(T actor)
    {
        return buttons.add(actor);
    }

    public Tab addTab(String title, Object key, Tab tab)
    {
        TextButton tabButton = new TextButton(title, getSkin(), getTabButtonStyle());
        buttonGroup.add(tabButton);
        Cell<Button> tabCell = buttons.add(tabButton);

        tab.setKey(key);
        tab.setTabButton(tabButton);
        tab.setTabCell(tabCell);
        tabs.add(tab);

        if (key != null)
        {
            tabKeys.put(key, tab);
        }

        tabButton.addListener(new ClickOverListener()
        {
            @Override
            public void clicked(InputEvent event, float x, float y)
            {
                if (currentTab != tab)
                {
                    Menu.playSound(Menu.MenuSound.select);
                    selectTab(tab);
                    TabSelectedListener.TabSelectedEvent ev = new TabSelectedListener.TabSelectedEvent();
                    fire(ev);
                }
            }
        });

        if (currentTab == null)
        {
            selectTab(tab, false);
        }

        return tab;
    }

    public Tab addTabIcon(String icon, Object key, Tab tab)
    {
        Button tabButton = new Button(getSkin(), getTabButtonStyle());

        Image img = new Image(getSkin(), icon);
        img.setTouchable(Touchable.disabled);
        img.setScaling(Scaling.none);
        tabButton.add(img);

        buttonGroup.add(tabButton);
        Cell<Button> tabCell = buttons.add(tabButton);

        tab.setKey(key);
        tab.setTabButton(tabButton);
        tab.setTabCell(tabCell);
        tabs.add(tab);

        if (key != null)
        {
            tabKeys.put(key, tab);
        }

        tabButton.addListener(new ClickOverListener()
        {
            @Override
            public void clicked(InputEvent event, float x, float y)
            {
                if (currentTab != tab)
                {
                    Menu.playSound(Menu.MenuSound.select);
                    selectTab(tab);
                    fire(new TabSelectedListener.TabSelectedEvent());
                }
            }
        });

        if (currentTab == null)
        {
            selectTab(tab, false);
        }

        return tab;
    }

    public void selectTab(Tab tab, boolean sendEvent)
    {
        content.clear();
        content.add(tab).expand().fill().row();

        currentTab = tab;

        if (tab != null && tab.getTabButton() != null)
        {
            tab.getTabButton().setChecked(true);

            if (sendEvent)
            {
                tab.selected();
            }
        }
    }

    public void selectTab(Tab tab)
    {
        selectTab(tab, true);
    }

    public void selectTab(Object tab)
    {
        selectTab(findTab(tab));
    }

    public void selectTab(Object tab, boolean sendEvent)
    {
        selectTab(findTab(tab), sendEvent);
    }

    @Override
    public void clear()
    {
        buttonGroup.clear();
        buttons.clear();
        tabs.clear();
        content.clear();
    }

    public Tab findTab(Object key)
    {
        return tabKeys.get(key);
    }

    public Array<Tab> getTabs()
    {
        return tabs;
    }

    public Tab getCurrentTab()
    {
        return currentTab;
    }
}
