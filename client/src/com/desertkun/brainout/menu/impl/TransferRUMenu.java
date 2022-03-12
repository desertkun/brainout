package com.desertkun.brainout.menu.impl;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.utils.Align;
import com.desertkun.brainout.BrainOutClient;
import com.desertkun.brainout.L;
import com.desertkun.brainout.client.SocialController;
import com.desertkun.brainout.menu.Menu;
import com.desertkun.brainout.menu.ui.ClickOverListener;
import com.desertkun.brainout.utils.ContentImage;
import com.desertkun.brainout.utils.MarketUtils;
import org.json.JSONObject;

public class TransferRUMenu extends AmountMenu
{
    private boolean in;
    private final int marketRU;
    private final int profileRU;
    private Callback callback;

    public interface Callback
    {
        void approve(int amount);
        void cancel();
    }

    public TransferRUMenu(int proffered, int max, boolean in, int marketRU, int profileRU, Callback callback)
    {
        super(proffered, max);

        this.callback = callback;
        this.marketRU = marketRU;
        this.profileRU = profileRU;
        this.in = in;
    }

    @Override
    public void approve(int amount)
    {
        JSONObject args = new JSONObject();

        args.put("market", "freeplay");
        args.put("amount", amount);

        BrainOutClient.SocialController.sendRequest(
            in ? "put_market_ru" : "withdraw_market_ru", args,
        new SocialController.RequestCallback()
        {
            @Override
            public void success(JSONObject response)
            {
                MarketUtils.GetMarketRU(new MarketUtils.GetRUCallback()
                {
                    @Override
                    public void success(int amount)
                    {
                        Gdx.app.postRunnable(() -> callback.approve(amount));
                    }

                    @Override
                    public void error()
                    {
                        callback.cancel();
                    }
                });
            }

            @Override
            public void error(String reason)
            {
                Menu.playSound(MenuSound.denied);
            }
        });
    }

    @Override
    public void cancel()
    {
        Menu.playSound(MenuSound.back);
    }

    @Override
    protected void renderContentAboveSlider(Table data)
    {
        Table profile = new Table();
        Table market = new Table();

        {
            Table header = new Table(BrainOutClient.Skin);
            header.setBackground("form-gray");

            Label title = new Label(L.get("MENU_PROFILE"), BrainOutClient.Skin, "title-yellow");
            title.setAlignment(Align.center);
            header.add(title).expand().fill();

            profile.add(header).expandX().fillX().row();
        }

        {
            Table header = new Table(BrainOutClient.Skin);
            header.setBackground("form-gray");

            Label title = new Label(L.get("MENU_MARKET"), BrainOutClient.Skin, "title-yellow");
            title.setAlignment(Align.center);
            header.add(title).expand().fill();

            market.add(header).expandX().fillX().row();
        }

        Button profileButton = new Button(BrainOutClient.Skin, "button-checkable");
        Button marketButton = new Button(BrainOutClient.Skin, "button-checkable");

        Image arrow = new Image(BrainOutClient.Skin, in ? "icon-exchange-right" : "icon-exchange-left");

        profileButton.addListener(new ClickOverListener()
        {
            @Override
            public void clicked(InputEvent event, float x, float y)
            {
                if (marketRU == 0)
                {
                    marketButton.setChecked(true);
                    Menu.playSound(MenuSound.denied);
                    return;
                }

                Menu.playSound(MenuSound.select);

                max = marketRU;
                refreshSlider();

                arrow.setDrawable(BrainOutClient.Skin, "icon-exchange-left");
                title.setText(L.get("MENU_MARKET_TRANSFER_RU_FROM_MARKET"));
                in = false;
            }
        });

        marketButton.addListener(new ClickOverListener()
        {
            @Override
            public void clicked(InputEvent event, float x, float y)
            {
                if (profileRU == 0)
                {
                    profileButton.setChecked(true);
                    Menu.playSound(MenuSound.denied);
                    return;
                }

                Menu.playSound(MenuSound.select);

                max = profileRU;
                refreshSlider();

                arrow.setDrawable(BrainOutClient.Skin, "icon-exchange-right");
                title.setText(L.get("MENU_MARKET_TRANSFER_RU_INTO_MARKET"));
                in = true;
            }
        });

        {
            Table icon = new Table();
            ContentImage.RenderStatImage("ru", profileRU, icon);
            profileButton.add(icon).row();

            Label amount = new Label(String.valueOf(profileRU), BrainOutClient.Skin, "title-small");
            profileButton.add(amount).row();
        }

        {
            Table icon = new Table();
            ContentImage.RenderStatImage("ru", marketRU, icon);
            marketButton.add(icon).row();

            Label amount = new Label(String.valueOf(marketRU), BrainOutClient.Skin, "title-small");
            marketButton.add(amount).row();
        }

        ButtonGroup<Button> group = new ButtonGroup<>();
        group.add(profileButton);
        group.add(marketButton);

        if (in)
        {
            marketButton.setChecked(true);
        }
        else
        {
            profileButton.setChecked(true);
        }

        profile.add(profileButton).expand().fill().row();
        market.add(marketButton).expand().fill().row();

        Table row = new Table();

        row.add(profile).size(192, 96);

        row.add(arrow).pad(16).expandY().padBottom(0).bottom();

        row.add(market).size(192, 96);

        data.add(row).pad(32).padBottom(0).row();
    }

    @Override
    protected String getTitle()
    {
        return L.get(in ? "MENU_MARKET_TRANSFER_RU_INTO_MARKET" : "MENU_MARKET_TRANSFER_RU_FROM_MARKET");
    }
}
