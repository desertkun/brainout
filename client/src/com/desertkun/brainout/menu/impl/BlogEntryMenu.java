package com.desertkun.brainout.menu.impl;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Scaling;
import com.desertkun.brainout.BrainOutClient;
import com.desertkun.brainout.L;
import com.desertkun.brainout.Version;
import com.desertkun.brainout.gs.GameState;
import com.desertkun.brainout.managers.LocalizationManager;
import com.desertkun.brainout.menu.Menu;
import com.desertkun.brainout.menu.popups.AlertPopup;
import com.desertkun.brainout.menu.ui.Avatars;
import com.desertkun.brainout.menu.ui.ClickOverListener;
import com.desertkun.brainout.menu.ui.MenuHelper;
import com.desertkun.brainout.online.BattlePassEvent;
import com.desertkun.brainout.online.ClientBattlePassEvent;
import com.desertkun.brainout.online.ClientEvent;
import com.desertkun.brainout.utils.VersionCompare;
import org.anthillplatform.runtime.services.BlogService;
import org.json.JSONObject;

import java.text.DateFormat;

public class BlogEntryMenu extends Menu
{
    private BlogService.BlogEntry entry;

    public BlogEntryMenu(BlogService.BlogEntry entry)
    {
        this.entry = entry;
    }

    @Override
    public void onInit()
    {
        super.onInit();

        MenuHelper.AddCloseButton(this, this::pop);
    }

    @Override
    public void onRelease()
    {
        super.onRelease();

        entry = null;
    }

    @Override
    protected MenuAlign getMenuAlign()
    {
        return MenuAlign.fillCenter;
    }

    @Override
    public Table createUI()
    {
        Table data = new Table();

        Table contents = new Table();
        contents.align(Align.top);
        renderContents(contents);

        ScrollPane pane = new ScrollPane(contents, BrainOutClient.Skin, "scroll-default");
        data.add(pane).expand().fill().row();
        setScrollFocus(pane);

        return data;
    }

    @Override
    public boolean escape()
    {
        pop();
        return true;
    }

    private void renderContents(Table contents)
    {
        JSONObject image = entry.data.optJSONObject("image");
        JSONObject titleValue = entry.data.optJSONObject("title");
        JSONObject descriptionValue = entry.data.optJSONObject("description");

        if (image == null || titleValue == null || descriptionValue == null)
            return;

        String version = titleValue.optString("version", null);
        if (version != null && !version.isEmpty())
        {
            VersionCompare v = new VersionCompare(version);
            VersionCompare current = new VersionCompare(Version.VERSION);

            if (current.compareTo(v) < 0)
            {
                return;
            }
        }

        String titleText = titleValue.optString(BrainOutClient.LocalizationMgr.getCurrentLanguage().toLowerCase(),
                titleValue.optString(LocalizationManager.GetDefaultLanguage().toLowerCase(), null));

        String descriptionText = descriptionValue.optString(BrainOutClient.LocalizationMgr.getCurrentLanguage().toLowerCase(),
                descriptionValue.optString(LocalizationManager.GetDefaultLanguage().toLowerCase(), null));

        if (titleText == null || descriptionText == null)
            return;

        String imageUrl = image.optString(BrainOutClient.LocalizationMgr.getCurrentLanguage().toLowerCase(),
                image.optString(LocalizationManager.GetDefaultLanguage().toLowerCase(), null));

        if (imageUrl == null)
            return;

        Image background = new Image();
        background.setScaling(Scaling.fit);

        Avatars.GetAndCache(imageUrl, (has, avatar) ->
        {
            if (has)
            {
                background.setDrawable(new TextureRegionDrawable(avatar));
            }
        });

        contents.add(background).pad(16).expandX().fillX().row();

        {
            Label title = new Label(DateFormat.getDateTimeInstance().format(entry.dateCreate),
                    BrainOutClient.Skin, "title-small");
            contents.add(title).expandX().fillX().pad(8).padTop(16).row();
        }

        {
            Label title = new Label(titleText, BrainOutClient.Skin, "title-yellow");
            contents.add(title).expandX().fillX().pad(8).row();
        }

        {
            Label description = new Label(descriptionText, BrainOutClient.Skin, "title-small");
            description.setWrap(true);
            contents.add(description).expandX().fillX().pad(8).row();
        }

        JSONObject action = entry.data.optJSONObject("action");

        if (action != null)
        {
            renderAction(action, contents);
        }

    }

    private void renderAction(JSONObject action, Table contents)
    {
        String behaviour = action.optString("behaviour", "store");
        String follow = action.optString("follow", null);

        if (behaviour == null)
            return;

        switch (behaviour)
        {
            case "event":
            {
                if (BrainOutClient.ClientController.getOnlineEvents().size > 0)
                {
                    TextButton btn = new TextButton(L.get("MENU_EVENTS"), BrainOutClient.Skin, "button-green");

                    btn.addListener(new ClickOverListener()
                    {
                        @Override
                        public void clicked(InputEvent event, float x, float y)
                        {
                            Menu.playSound(MenuSound.select);
                            popMeAndPushMenu(new OnlineEventMenu(
                                    BrainOutClient.ClientController.getOnlineEvents().get(0), false));
                        }
                    });

                    contents.add(btn).size(192, 64).padTop(16).expandX().center().row();
                }

                break;
            }
            case "bpevent":
            {
                for (ClientEvent event : BrainOutClient.ClientController.getOnlineEvents())
                {
                    if (event instanceof ClientBattlePassEvent)
                    {
                        TextButton btn = new TextButton(L.get("MENU_BATTLE_PASS"), BrainOutClient.Skin, "button-green");

                        btn.addListener(new ClickOverListener()
                        {
                            @Override
                            public void clicked(InputEvent e, float x, float y)
                            {
                                Menu.playSound(MenuSound.select);
                                popMeAndPushMenu(new BattlePassMenu(((ClientBattlePassEvent) event)));
                            }
                        });

                        contents.add(btn).size(192, 64).padTop(16).expandX().center().row();

                        break;
                    }
                }

                break;
            }
            case "freeplay":
            {
                TextButton btn = new TextButton(L.get("MENU_FREE_PLAY"), BrainOutClient.Skin, "button-green");

                btn.addListener(new ClickOverListener()
                {
                    @Override
                    public void clicked(InputEvent event, float x, float y)
                    {
                        Menu.playSound(MenuSound.select);
                        popMeAndPushMenu(new FreePlayQuestsMenu());
                    }
                });

                contents.add(btn).size(192, 64).padTop(16).expandX().center().row();

                break;
            }
            case "url":
            {
                if (follow != null && !follow.isEmpty())
                {
                    TextButton btn = new TextButton(L.get("MENU_VOTE"), BrainOutClient.Skin, "button-green");

                    btn.addListener(new ClickOverListener()
                    {
                        @Override
                        public void clicked(InputEvent event, float x, float y)
                        {
                            Menu.playSound(MenuSound.select);
                            GameState gs = getGameState();
                            pop();

                            if (Gdx.app.getNet().openURI(follow))
                            {
                                Gdx.app.postRunnable(() ->
                                        gs.pushMenu(new AlertPopup(L.get("MENU_BROWSER_TAB"))));
                            }
                        }
                    });

                    contents.add(btn).size(192, 64).padTop(16).expandX().center().row();
                }

                break;
            }
            case "store":
            {
                if (BrainOutClient.Env.storeEnabled())
                {
                    TextButton btn = new TextButton(L.get("MENU_STORE"), BrainOutClient.Skin, "button-green");

                    btn.addListener(new ClickOverListener()
                    {
                        @Override
                        public void clicked(InputEvent event, float x, float y)
                        {
                            Menu.playSound(MenuSound.select);
                            popMeAndPushMenu(new StoreMenu());
                        }
                    });

                    contents.add(btn).size(192, 64).padTop(16).expandX().center().row();
                }

                break;
            }
        }
    }

    @Override
    protected TextureRegion getBackground()
    {
        return BrainOutClient.getRegion("bg-ingame");
    }
}
