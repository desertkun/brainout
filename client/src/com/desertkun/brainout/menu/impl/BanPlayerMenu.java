package com.desertkun.brainout.menu.impl;

import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;
import com.desertkun.brainout.BrainOut;
import com.desertkun.brainout.BrainOutClient;
import com.desertkun.brainout.L;
import com.desertkun.brainout.client.SocialController;
import com.desertkun.brainout.gs.GameState;
import com.desertkun.brainout.menu.FormMenu;
import com.desertkun.brainout.menu.Menu;
import com.desertkun.brainout.menu.Popup;
import com.desertkun.brainout.menu.popups.AlertPopup;
import com.desertkun.brainout.menu.ui.ClickOverListener;
import org.anthillplatform.runtime.util.Utils;
import org.json.JSONObject;

import java.text.ParseException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;

public class BanPlayerMenu extends FormMenu
{
    private final String account;
    private TextField reason;
    private TextField expires;

    public BanPlayerMenu(String account)
    {
        this.account = account;
    }

    @Override
    protected String formBorderStyle()
    {
        return "form-border-red";
    }

    @Override
    public Table createUI()
    {
        Table data = super.createUI();

        Table title = new Table(BrainOutClient.Skin);
        title.setBackground("form-red");
        title.add(new Label("Ban a player",  BrainOutClient.Skin, "title-small")).pad(16).row();
        data.add(title).expandX().fillX().row();

        Table fields = new Table();

        {
            fields.add(new Label("Reason (english):", BrainOutClient.Skin, "title-yellow")).pad(2).padTop(16).expandX().center().row();

            reason = new TextField("", BrainOutClient.Skin, "edit-default");
            fields.add(reason).width(500).expandX().fillX().pad(8).row();

            setKeyboardFocus(reason);
        }

        {
            fields.add(new Label("Expiration date:", BrainOutClient.Skin, "title-yellow")).pad(2).padTop(16).expandX().center().row();

            Date now = Date.from(Instant.now().plus(30, ChronoUnit.DAYS));
            expires = new TextField(Utils.DATE_FORMAT.format(now), BrainOutClient.Skin, "edit-default");
            fields.add(expires).width(300).expandX().fillX().pad(8).row();

            setKeyboardFocus(reason);
        }

        data.add(fields).pad(16).expandX().fillX().row();

        Table buttons = new Table();

        {
            TextButton cancel = new TextButton(L.get("MENU_CANCEL"),
                    BrainOutClient.Skin, "button-green");

            cancel.addListener(new ClickOverListener()
            {
                @Override
                public void clicked(InputEvent event, float x, float y)
                {
                    GameState gs = getGameState();

                    if (gs == null)
                        return;

                    pop();
                }
            });

            buttons.add(cancel).width(196).height(64).pad(4);
        }

        {
            TextButton ban = new TextButton("DO IT", BrainOutClient.Skin, "button-danger");

            ban.addListener(new ClickOverListener()
            {
                @Override
                public void clicked(InputEvent event, float x, float y)
                {
                    if (reason.getText().length() == 0 || reason.getText().length() > 200)
                    {
                        Menu.playSound(MenuSound.denied);
                        return;
                    }

                    Date p;

                    try
                    {
                        p = Utils.DATE_FORMAT.parse(expires.getText());
                    }
                    catch (ParseException e)
                    {
                        Menu.playSound(MenuSound.denied);
                        return;
                    }

                    ban(reason.getText(), p);
                }
            });

            buttons.add(ban).width(196).height(64).pad(4);
        }

        data.add(buttons).expandX().center().pad(4).row();

        return data;
    }

    private void ban(String reason, Date expires)
    {
        GameState gs = getGameState();

        WaitLoadingMenu waitLoadingMenu = new WaitLoadingMenu("");

        gs.pushMenu(waitLoadingMenu);

        JSONObject args = new JSONObject();
        args.put("account", this.account);
        args.put("reason", reason);
        args.put("expires", Utils.DATE_FORMAT.format(expires));

        BrainOutClient.SocialController.sendRequest(
            "ban", args, new SocialController.RequestCallback()
        {
            @Override
            public void success(JSONObject response)
            {
                waitLoadingMenu.pop();
                gs.popMenu(BanPlayerMenu.this);
                gs.pushMenu(new AlertPopup("OK"));
            }

            @Override
            public void error(String reason)
            {
                waitLoadingMenu.pop();
                gs.pushMenu(new AlertPopup(reason));
            }
        });
    }
}
