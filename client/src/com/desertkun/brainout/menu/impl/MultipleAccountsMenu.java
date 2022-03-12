package com.desertkun.brainout.menu.impl;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.NinePatchDrawable;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Scaling;
import com.desertkun.brainout.BrainOutClient;
import com.desertkun.brainout.L;
import com.desertkun.brainout.client.states.CSMultipleAccounts;
import com.desertkun.brainout.menu.FormMenu;
import com.desertkun.brainout.menu.ui.ClickOverListener;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class MultipleAccountsMenu extends FormMenu
{
    private final CSMultipleAccounts accounts;

    public MultipleAccountsMenu(CSMultipleAccounts accounts)
    {
        this.accounts = accounts;
    }

    @Override
    protected TextureRegion getBackground()
    {
        return BrainOutClient.getRegion("bg-loading");
    }

    @Override
    public Table createUI()
    {
        Table data = super.createUI();

        Label title = new Label(L.get("MENU_CHOOSE_ACCOUNT_TITLE"),
                BrainOutClient.Skin, "title-yellow");
        title.setAlignment(Align.center);

        data.add(title).pad(16).expandX().row();

        Label description = new Label(L.get("MENU_CHOOSE_ACCOUNT_DESC"),
                BrainOutClient.Skin, "title-small");
        description.setAlignment(Align.center);

        data.add(description).pad(16).expandX().row();

        Table accountsData = new Table();

        for (CSMultipleAccounts.Profile profile : accounts.getProfiles())
        {
            renderAccount(profile, accountsData);
        }

        data.add(accountsData).row();

        return data;
    }

    private void renderAccount(CSMultipleAccounts.Profile profile, Table data)
    {
        Table account = new Table();
        account.align(Align.center);

        Table stats = new Table();
        stats.setBackground(new NinePatchDrawable(BrainOutClient.getNinePatch("form-gray")));

        TextButton select = new TextButton(L.get("MENU_THIS"),
                BrainOutClient.Skin, "button-default");

        select.addListener(new ClickOverListener()
        {
            @Override
            public void clicked(InputEvent event, float x, float y)
            {
                playSound(MenuSound.select);

                CSMultipleAccounts cs = BrainOutClient.ClientController.getState(CSMultipleAccounts.class);

                cs.resolve(profile.account);
            }
        });

        SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy\nHH:mm:ss", Locale.US);

        String name = profile.data.optString("name", "Anonymous");
        String level = profile.data.optString("level", "1");

        Date timeCreated = null, timeUpdated = null;

        try {
            timeCreated = new Date(
                Long.parseLong(profile.data.optString("@time_created")) * 1000
            );
        } catch (NumberFormatException ignored) {}

        try {
            timeUpdated = new Date(
                Long.parseLong(profile.data.optString("@time_updated")) * 1000
            );
        } catch (NumberFormatException ignored) {}

        Label nameTitle = new Label(name, BrainOutClient.Skin, "title-small");
        nameTitle.setAlignment(Align.center);
        stats.add(nameTitle).pad(8).expandX().row();

        Label levelTitle = new Label(L.get("MENU_LEVEL") + ": " + level,
                BrainOutClient.Skin, "title-yellow");
        levelTitle.setAlignment(Align.center);
        stats.add(levelTitle).pad(8).expandX().row();

        if (timeCreated != null)
        {
            Label title = new Label(L.get("MENU_TIME_CREATED",
                    format.format(timeCreated)),
                    BrainOutClient.Skin, "title-small");
            title.setAlignment(Align.center);
            stats.add(title).pad(8).padBottom(16).expandX().row();
        }

        if (timeUpdated != null)
        {
            Label title = new Label(L.get("MENU_TIME_UPDATED",
                    format.format(timeUpdated)),
                    BrainOutClient.Skin, "title-small");
            title.setAlignment(Align.center);
            stats.add(title).pad(8).padBottom(16).expandX().row();
        }

        account.add(stats).width(192).pad(8).row();
        account.add(select).size(192, 40).row();

        data.add(account).pad(8);
    }
}
