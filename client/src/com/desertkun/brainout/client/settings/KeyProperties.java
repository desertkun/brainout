package com.desertkun.brainout.client.settings;

import com.badlogic.gdx.utils.ObjectMap;
import com.desertkun.brainout.L;

public class KeyProperties extends Properties
{
    public enum Keys
    {
        up,
        down,
        left,
        right,
        sit,
        run,
        reload,

        playerList,
        dropWeapon,
        dropAmmo,
        changeTeam,
        shootMode,
        unloadWeapon,
        zoom,
        chat,
        teamChat,
        console,
        voiceChat,
        hideInterface,

        slotPrimary,
        slotSecondary,
        slotSpecial,
        slotKnife,
        slotBinoculars,
        slotFlashlight,
        previousSlot,

        activate,
        squat,
        freePlayFriends
    }

    private ObjectMap<Integer, Keys> keys;
    private ObjectMap<Keys, Integer> codes;

    public KeyProperties(String name, String localization)
    {
        super(name, localization);

        keys = new ObjectMap<>();
        codes = new ObjectMap<>();
    }

    public KeyProperties(String name, String localization, Properties parent)
    {
        super(name, localization, parent);

        keys = new ObjectMap<>();
        codes = new ObjectMap<>();
    }

    public Keys getKey(int keyCode)
    {
        return keys.get(keyCode);
    }

    public int getKeyCode(Keys keys, int def)
    {
        return this.codes.get(keys, def);
    }

    public void update()
    {
        keys.clear();

        for (Property property : getProperties())
        {
            if (!(property instanceof KeycodeProperty))
                continue;

            KeycodeProperty keycodeProperty = (KeycodeProperty) property;

            keys.put(keycodeProperty.getValue(), keycodeProperty.getKey());
            codes.put(keycodeProperty.getKey(), keycodeProperty.getValue());
        }
    }
}
