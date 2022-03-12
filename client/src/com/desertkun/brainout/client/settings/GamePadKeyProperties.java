package com.desertkun.brainout.client.settings;

import com.badlogic.gdx.utils.ObjectMap;

public class GamePadKeyProperties extends Properties
{
    public enum Keys
    {
        moveUpDown,
        moveLeftRight,
        aimUpDown,
        aimLeftRight,

        slotPrimary,
        slotSecondary,
        slotSpecial,
        slotKnife,

        reload,
        fire,
        aim,
        run,
        crouch,
        firingMode,
        playerList
    }

    private ObjectMap<Integer, Keys> sticks;
    private ObjectMap<Integer, Keys> buttons;

    public GamePadKeyProperties(String name, String localization)
    {
        super(name, localization);

        sticks = new ObjectMap<>();
        buttons = new ObjectMap<>();
    }

    public GamePadKeyProperties(String name, String localization, Properties parent)
    {
        super(name, localization, parent);

        sticks = new ObjectMap<>();
        buttons = new ObjectMap<>();
    }

    public Keys getStickKey(int keyCode)
    {
        return sticks.get(keyCode);
    }

    public Keys getButtonKey(int keyCode)
    {
        return buttons.get(keyCode);
    }

    public void update()
    {
        sticks.clear();
        buttons.clear();

        for (Property property : getProperties())
        {
            GamePadKeycodeProperty keycodeProperty = (GamePadKeycodeProperty) property;

            if (keycodeProperty.isStickOnly() || keycodeProperty.isStick())
            {
                sticks.put(keycodeProperty.getValue(), keycodeProperty.getKey());
            }
            else
            {
                buttons.put(keycodeProperty.getValue(), keycodeProperty.getKey());
            }
        }
    }
}
