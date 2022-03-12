package com.desertkun.brainout.gs;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.InputProcessor;
import com.desertkun.brainout.BrainOutClient;
import com.desertkun.brainout.gs.actions.MenuAction;
import com.desertkun.brainout.menu.Menu;
import com.desertkun.brainout.menu.ui.ActionList;
import com.desertkun.brainout.menu.widgets.Widgets;

import java.util.Stack;

public class GameState
{
    private Widgets widgets;
    private Stack<Menu> menus;
    private ActionList actionList;
    private InputProcessor menuProcessor;

    public GameState()
    {
        menus = new Stack<>();
        actionList = new ActionList();
        widgets = new Widgets();

        setup();
    }

    private void setup()
    {
        this.menuProcessor = new InputProcessor()
        {
            @Override
            public boolean keyDown(int keycode)
            {
                if (menus.empty())
                    return false;

                for (int i = menus.size() - 1; i >= 0; i--)
                {
                    Menu menu = menus.get(i);

                    if (menu.keyDown(keycode))
                        return true;

                    if (menu.lockInput())
                        return true;
                }

                return false;
            }

            @Override
            public boolean keyUp(int keycode)
            {
                if (menus.empty())
                    return false;

                for (int i = menus.size() - 1; i >= 0; i--)
                {
                    Menu menu = menus.get(i);

                    if (menu.keyUp(keycode))
                        return true;

                    if (menu.lockInput())
                        return true;
                }

                return false;
            }

            @Override
            public boolean keyTyped(char character)
            {
                if (menus.empty())
                    return false;

                for (int i = menus.size() - 1; i >= 0; i--)
                {
                    Menu menu = menus.get(i);

                    if (menu.keyTyped(character))
                        return true;

                    if (menu.lockInput())
                        return true;
                }

                return false;
            }

            @Override
            public boolean touchDown(int screenX, int screenY, int pointer, int button)
            {
                if (menus.empty())
                    return false;

                for (int i = menus.size() - 1; i >= 0; i--)
                {
                    Menu menu = menus.get(i);

                    if (menu.touchDown(screenX, screenY, pointer, button))
                        return true;

                    if (menu.lockInput())
                        return true;
                }

                return false;
            }

            @Override
            public boolean touchUp(int screenX, int screenY, int pointer, int button)
            {
                if (menus.empty())
                    return false;

                for (int i = menus.size() - 1; i >= 0; i--)
                {
                    Menu menu = menus.get(i);

                    if (menu.touchUp(screenX, screenY, pointer, button))
                        return true;

                    if (menu.lockInput())
                        return true;
                }

                return false;
            }

            @Override
            public boolean touchDragged(int screenX, int screenY, int pointer)
            {
                if (menus.empty())
                    return false;

                for (int i = menus.size() - 1; i >= 0; i--)
                {
                    Menu menu = menus.get(i);

                    if (menu.touchDragged(screenX, screenY, pointer))
                        return true;

                    if (menu.lockInput())
                        return true;
                }

                return false;
            }

            @Override
            public boolean mouseMoved(int screenX, int screenY)
            {
                if (menus.empty())
                    return false;

                for (int i = menus.size() - 1; i >= 0; i--)
                {
                    Menu menu = menus.get(i);

                    if (menu.mouseMoved(screenX, screenY))
                        return true;

                    if (menu.lockInput())
                        return true;
                }

                return false;
            }

            @Override
            public boolean scrolled(float amountX, float amountY)
            {
                if (menus.empty())
                    return false;

                for (int i = menus.size() - 1; i >= 0; i--)
                {
                    Menu menu = menus.get(i);

                    if (menu.scrolled(amountX, amountY))
                        return true;

                    if (menu.lockInput())
                        return true;
                }

                return false;
            }
        };

        Gdx.input.setInputProcessor(new InputMultiplexer(
            widgets,
            menuProcessor,
            BrainOutClient.Env.getGameController()
        ));
    }

    public ActionList addAction(MenuAction menuAction)
    {
        return actionList.addAction(menuAction);
    }

    public void onInit() {}
    public void onRelease()
    {
        popAll();

        widgets.release();
    }

    public void onFocusIn()
    {
        if (!menus.empty())
        {
            menus.peek().onFocusIn();
        }
    }

    public void onFocusOut()
    {
        if (!menus.empty())
        {
            menus.peek().onFocusOut(null);
        }
    }

    public void pushMenu(Menu menu)
    {
        menu.setGameState(this);

        if (!menus.empty())
        {
            Menu topMenu = menus.peek();

            if (topMenu.stayOnTop())
            {
                menus.insertElementAt(menu, menus.size() - 1);
                menu.onInit();
                return;
            }

            topMenu.onFocusOut(menu);
        }

        menus.push(menu);

        menu.onInit();
        menu.onFocusIn();
    }

    public void popAll()
    {
        while (!menus.empty())
        {
            popTopMenu();
        }
    }

    public void popAllUntil(Class<? extends Menu> menuClass)
    {
        while (!menus.empty())
        {
            Menu topMenu = menus.peek();

            if (topMenu.getClass() == menuClass)
                return;

            popTopMenu();
        }
    }

    public void popMenu(Class<? extends Menu> menuClass)
    {
        if (menus.empty())
            return;

        if (menus.peek().getClass() == menuClass)
        {
            popTopMenu();
            return;
        }

        for (Menu menu : menus)
        {
            if (menu.getClass() == menuClass)
            {
                if (menus.remove(menu))
                {
                    menu.onRelease();
                    return;
                }
            }
        }
    }

    public void popMenu(Menu menu)
    {
        if (menus.empty())
            return;

        if (menus.peek() == menu)
            popTopMenu();

        if (menus.remove(menu))
        {
            menu.onRelease();
        }
    }

    public void popTopMenu()
    {
        if (!menus.empty())
        {
            Menu topMenu = menus.peek();

            final Menu menu;
            if (topMenu.stayOnTop() && menus.size() >= 2)
            {
                int index = menus.size() - 2;

                menu = menus.elementAt(index);
                menus.remove(index);
            }
            else
            {
                menu = menus.pop();
            }

            menu.onFocusOut(null);
            menu.onRelease();
        }

        if (!menus.empty())
        {
            menus.peek().onFocusIn();
        }
    }

    public void removeMenu(Menu menu)
    {
        menu.onRelease();
        menus.remove(menu);
    }

    public void render()
    {
        boolean includeMenus = true;

        if (!menus.empty())
        {
            Menu top = menus.peek();

            if (top.lockRender())
            {
                top.render();
                includeMenus = false;
            }
        }

        if (includeMenus)
        {
            for (Menu menu : menus)
            {
                menu.render();
            }
        }

        widgets.render();
    }

    public void update(float dt)
    {
        widgets.update(dt);

        actionList.processActions(dt);

        if (!menus.isEmpty())
        {
            for (int i = menus.size() - 1; i >= 0; i--)
            {
                Menu menu = menus.get(i);

                menu.act(dt);
                if (menu.lockUpdate())
                {
                    return;
                }
            }
        }
    }

    public Menu topMenu()
    {
        if (menus.empty())
            return null;

        return menus.peek();
    }

    public boolean hasTopMenu()
    {
        return !menus.isEmpty();
    }

    public void reset()
    {
        for (Menu menu : menus)
        {
            menu.reset();
        }
    }

    public Widgets getWidgets()
    {
        return widgets;
    }

    public ActionList getActionList()
    {
        return actionList;
    }
}
