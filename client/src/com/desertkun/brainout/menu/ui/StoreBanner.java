package com.desertkun.brainout.menu.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.utils.Array;
import com.desertkun.brainout.BrainOutClient;
import com.desertkun.brainout.L;
import com.desertkun.brainout.managers.LocalizationManager;
import com.desertkun.brainout.menu.Menu;
import com.desertkun.brainout.menu.impl.FreePlayQuestsMenu;
import com.desertkun.brainout.menu.impl.OnlineEventMenu;
import com.desertkun.brainout.menu.impl.StoreMenu;
import com.desertkun.brainout.menu.impl.WaitLoadingMenu;
import com.desertkun.brainout.menu.popups.AlertPopup;
import com.desertkun.brainout.menu.popups.BannerPopup;
import com.desertkun.brainout.online.ClientEvent;
import com.desertkun.brainout.utils.EarlyAccess;
import org.anthillplatform.runtime.services.EnvironmentService;
import org.json.JSONObject;

public class StoreBanner
{
    public static boolean show()
    {
        EnvironmentService environmentService = EnvironmentService.Get();

        Object banner = environmentService.variable("banner", null, Object.class);

        if (!(banner instanceof JSONObject))
            return false;

        JSONObject info = ((JSONObject) banner);

        String id = info.optString("id", null);
        JSONObject image = info.optJSONObject("image");
        String behaviour = info.optString("behaviour", "store");
        String follow = info.optString("follow", null);
        boolean earlyAccess = info.optBoolean("early", false);

        if (earlyAccess && !EarlyAccess.Have())
        {
            return false;
        }

        String button;

        switch (behaviour)
        {
            case "event":
            {
                button = L.get("MENU_EVENTS");

                if (BrainOutClient.ClientController.getOnlineEvents().size == 0)
                    return false;
                break;
            }
            case "freeplay":
            {
                button = L.get("MENU_FREE_PLAY");

                break;
            }
            case "url":
            {
                button = L.get("MENU_VOTE");

                if (follow == null || follow.isEmpty())
                    return false;

                break;
            }
            default:
            case "store":
            {
                button = L.get("MENU_STORE");

                if (!BrainOutClient.Env.storeEnabled())
                    return false;
                break;
            }
        }

        if (id == null || image == null || id.isEmpty())
            return false;

        String url = image.optString(BrainOutClient.LocalizationMgr.getCurrentLanguage(), null);

        if (url == null || url.isEmpty())
        {
            url = image.optString(LocalizationManager.GetDefaultLanguage(), null);
        }

        if (url == null || url.isEmpty())
        {
            return false;
        }

        Preferences prefs = Gdx.app.getPreferences("b");

        if (prefs.contains(id))
        {
            return false;
        }

        Menu loading = new WaitLoadingMenu("");
        BrainOutClient.getInstance().topState().pushMenu(loading);

        new BannerPopup(url,
                (success, self) ->
                {
                    loading.pop();
                    if (success)
                    {
                        BrainOutClient.getInstance().topState().pushMenu(self);
                    }
                })
        {
            @Override
            public String buttonYes()
            {
                return button;
            }

            @Override
            public String buttonNo()
            {
                return L.get("MENU_CLOSE");
            }

            @Override
            public String getTitle()
            {
                return L.get(L.get("MENU_ATTENTION"));
            }

            @Override
            protected boolean reverseOrder()
            {
                return true;
            }

            @Override
            protected float getButtonHeight()
            {
                return 64;
            }

            @Override
            public void no()
            {
                prefs.putBoolean(id, true);
                prefs.flush();
            }

            @Override
            public String buttonStyleYes()
            {
                return "button-green";
            }

            @Override
            public void yes()
            {
                prefs.putBoolean(id, true);
                prefs.flush();

                Gdx.app.postRunnable(() -> {
                    followBanner(behaviour, follow);
                });
            }

            @Override
            protected float getFade()
            {
                return 0.75f;
            }
        };

        return true;
    }

    private static void followBanner(String behaviour, String follow)
    {
        switch (behaviour)
        {
            case "url":
            {
                if (Gdx.app.getNet().openURI(follow))
                {
                    Gdx.app.postRunnable(() ->
                        BrainOutClient.getInstance().topState().pushMenu(new AlertPopup(L.get("MENU_BROWSER_TAB"))));
                }

                break;
            }
            case "store":
            {
                BrainOutClient.getInstance().topState().pushMenu(new StoreMenu());

                break;
            }
            case "freeplay":
            {
                BrainOutClient.getInstance().topState().pushMenu(new FreePlayQuestsMenu());

                break;
            }
            case "event":
            {
                Array<ClientEvent> clientEvents = BrainOutClient.ClientController.getOnlineEvents();
                if (clientEvents.size > 0)
                {
                    BrainOutClient.getInstance().topState().pushMenu(new OnlineEventMenu(clientEvents.get(0), false));
                }

                break;
            }
        }
    }
}
