package com.desertkun.brainout.menu.tutorial;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;

public class Tutorials
{
    private static Preferences Prefs;

    public static void Init()
    {
        Prefs = Gdx.app.getPreferences("tutorial");
    }

    public static boolean IsFinished(String name)
    {
        return Prefs.getBoolean(name, false);
    }

    public static void Done(String name)
    {
        Prefs.putBoolean(name, true);
        Prefs.flush();
    }
}
