package com.desertkun.brainout.menu.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;
import com.desertkun.brainout.BrainOutClient;
import com.desertkun.brainout.Constants;
import com.desertkun.brainout.L;
import com.desertkun.brainout.menu.popups.AlertPopup;
import com.desertkun.brainout.menu.popups.RichConfirmationPopup;
import com.desertkun.brainout.online.UserProfile;

import java.util.TimerTask;

public class Greenlight
{
    public static boolean show()
    {
        if (!checkGreenlight())
            return false;

        BrainOutClient.Timer.schedule(new TimerTask()
        {
            @Override
            public void run()
            {
                Gdx.app.postRunnable(() ->
                {
                    BrainOutClient.getInstance().topState().pushMenu(new RichConfirmationPopup(
                            "<img expand=\"true\" fill=\"true\" pad=\"20\">greenlight</img>" +
                                    "<br/><br/><loc expand=\"true\" fill=\"true\" align=\"center\">MENU_GREENLIGHT</loc><br/>")
                    {
                        @Override
                        public String buttonYes()
                        {
                            return L.get("MENU_LATER");
                        }

                        @Override
                        public String buttonNo()
                        {
                            return L.get("MENU_VOTE");
                        }

                        @Override
                        public void no()
                        {
                            openGreenlight();
                        }

                        @Override
                        public String buttonStyleNo()
                        {
                            return "button-yellow";
                        }

                        @Override
                        public void yes()
                        {
                            Preferences prefs = Gdx.app.getPreferences("steam-store");
                            long current = System.currentTimeMillis() / 1000L;
                            prefs.putLong("later", current + 86400);
                            prefs.flush();
                        }

                        @Override
                        protected float getFade()
                        {
                            return 0.75f;
                        }
                    });
                });
            }
        }, 500);

        return true;
    }

    private static boolean checkGreenlight()
    {
        if (!BrainOutClient.Env.greenlightEnabled())
            return false;

        Preferences prefs = Gdx.app.getPreferences("steam-store");

        if (prefs.contains("opened"))
        {
            return false;
        }

        if (prefs.contains("later"))
        {
            long tm = prefs.getLong("later");
            long current = System.currentTimeMillis() / 1000L;

            return current > tm;
        }
        else
        {
            return true;
        }
    }

    private static void openGreenlight()
    {
        Preferences prefs = Gdx.app.getPreferences("steam-store");
        prefs.putBoolean("opened", true);
        prefs.flush();

        if (Gdx.app.getNet().openURI("http://store.steampowered.com/app/578310"))
        {
            Gdx.app.postRunnable(() ->
                BrainOutClient.getInstance().topState().pushMenu(new AlertPopup(L.get("MENU_BROWSER_TAB"))));
        }
    }
}
