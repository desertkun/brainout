package com.desertkun.brainout.menu.impl;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.utils.Align;
import com.desertkun.brainout.BrainOutClient;
import com.desertkun.brainout.ClientConstants;
import com.desertkun.brainout.L;
import com.desertkun.brainout.client.SocialController;
import com.desertkun.brainout.menu.Menu;
import com.desertkun.brainout.menu.popups.AlertPopup;
import com.desertkun.brainout.menu.ui.BorderActor;
import com.desertkun.brainout.menu.ui.ClickOverListener;
import org.json.JSONObject;

public class AccountDeactivatedMenu extends Menu
{
    private Runnable reactivated;

    public AccountDeactivatedMenu(Runnable reactivated)
    {
        this.reactivated = reactivated;
    }

    @Override
    public Table createUI()
    {
        Table root = new Table();

        {
            Label label = new Label(
                L.get("MENU_ACCOUNT_DEACTIVATED"),
                BrainOutClient.Skin, "title-yellow"
            );

            label.setAlignment(Align.center);

            root.add(new BorderActor(label, "form-red")).padBottom(0).expandX().fillX().row();
        }

        Table data = new Table();
        data.setSkin(BrainOutClient.Skin);
        data.setBackground("form-border-red");
        data.align(Align.center);

        root.add(data);

        Image maintenance = new Image(BrainOutClient.getRegion("maintenance"));

        data.add(maintenance).expandX().pad(8).row();

        {
            Label label = new Label(
                L.get("MENU_DELETE_ACCOUNT_HINT"),
                BrainOutClient.Skin, "title-yellow"
            );

            label.setWrap(true);
            label.setAlignment(Align.center);

            data.add(label).pad(16).expandX().fillX().row();
        }

        {
            Table buttons = new Table();

            {
                TextButton close = new TextButton(L.get("MENU_EXIT"),
                        BrainOutClient.Skin, "button-default");

                close.addListener(new ClickOverListener()
                {
                    @Override
                    public void clicked(InputEvent event, float x, float y)
                    {
                        Gdx.app.exit();
                    }
                });

                buttons.add(close).size(192, 64).pad(8);
            }

            {
                TextButton reactivate = new TextButton(L.get("MENU_REACTIVATE"),
                    BrainOutClient.Skin, "button-green");

                reactivate.addListener(new ClickOverListener()
                {
                    @Override
                    public void clicked(InputEvent event, float x, float y)
                    {
                        Menu.playSound(MenuSound.select);

                        reactivate();
                    }
                });

                buttons.add(reactivate).size(192, 64).pad(8);
            }

            data.add(buttons).expandX().fillX().pad(8).row();
        }

        return root;
    }

    private void reactivate()
    {
        WaitLoadingMenu waitLoadingMenu = new WaitLoadingMenu("");
        pushMenu(waitLoadingMenu);

        JSONObject args = new JSONObject();

        BrainOutClient.SocialController.sendRequest("reactivate", args,
            new SocialController.RequestCallback()
        {
            @Override
            public void success(JSONObject response)
            {
                waitLoadingMenu.pop();
                pop();
                reactivated.run();
            }

            @Override
            public void error(String reason)
            {
                waitLoadingMenu.pop();
            }
        });
    }

    @Override
    public void onFocusIn()
    {
        super.onFocusIn();

        if (ClientConstants.Client.MOUSE_LOCK)
        {
            Gdx.input.setCursorCatched(false);
        }
    }

    @Override
    public boolean lockRender()
    {
        return true;
    }

    @Override
    public boolean lockInput()
    {
        return true;
    }

    @Override
    protected TextureRegion getBackground()
    {
        return BrainOutClient.getRegion("bg-ingame");
    }
}
