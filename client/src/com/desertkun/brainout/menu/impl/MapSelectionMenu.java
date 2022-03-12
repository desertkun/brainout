package com.desertkun.brainout.menu.impl;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Queue;
import com.badlogic.gdx.utils.Scaling;
import com.desertkun.brainout.BrainOutClient;
import com.desertkun.brainout.GameUser;
import com.desertkun.brainout.L;
import com.desertkun.brainout.menu.Menu;
import com.desertkun.brainout.menu.ui.Avatars;
import com.desertkun.brainout.menu.ui.ClickOverListener;

public class MapSelectionMenu extends Menu
{
    private final String[] standardMaps;
    private SelectionCallback callback;

    public interface SelectionCallback
    {
        void selectedAny();
        void selectedStandard(String name);
        void selectedWorkshop(String id, String title);
    }

    public MapSelectionMenu(SelectionCallback callback, String[] standardMaps)
    {
        this.callback = callback;
        this.standardMaps = standardMaps;
    }

    @Override
    public Table createUI()
    {
        Table data = new Table();

        Table contents = new Table();
        renderContents(contents);

        ScrollPane scrollPane = new ScrollPane(contents, BrainOutClient.Skin, "scroll-default");
        setScrollFocus(scrollPane);

        data.add(scrollPane).expand().fill().row();

        return data;
    }

    @Override
    protected MenuAlign getMenuAlign()
    {
        return MenuAlign.fill;
    }

    private void renderContents(Table data)
    {
        {
            Table header = new Table();
            Label title = new Label(L.get("MENU_ANY_MAP"), BrainOutClient.Skin, "title-small");

            header.add(title).row();
            header.add(new Image(BrainOutClient.Skin, "stroke-ui")).padBottom(8).padTop(8).row();


            data.add(header).padTop(32).row();
        }

        {
            TextButton any = new TextButton(L.get("MENU_ANY_MAP"), BrainOutClient.Skin, "button-default");

            any.addListener(new ClickOverListener()
            {
                @Override
                public void clicked(InputEvent event, float x, float y)
                {
                    Menu.playSound(MenuSound.select);

                    pop();
                    callback.selectedAny();
                }
            });

            data.add(any).size(192, 64).padBottom(32).row();
        }

        {
            Table header = new Table();
            Label title = new Label(L.get("MENU_STANDARD_MAPS"), BrainOutClient.Skin, "title-small");

            header.add(title).row();
            header.add(new Image(BrainOutClient.Skin, "stroke-ui")).padBottom(8).padTop(8).row();


            data.add(header).row();
        }

        {
            Table maps = new Table();

            int i = 0;

            for (String map : standardMaps)
            {
                Button btn = new Button(BrainOutClient.Skin, "button-notext");

                {
                    Image image = new Image(BrainOutClient.Skin, "map-" + map);
                    image.setScaling(Scaling.none);
                    image.setFillParent(true);
                    image.setTouchable(Touchable.disabled);

                    btn.addActor(image);
                }

                {
                    Label title = new Label(L.get("MAP_" + map.toUpperCase()),
                            BrainOutClient.Skin, "title-yellow");
                    title.setAlignment(Align.bottom | Align.center);
                    title.setFillParent(true);
                    title.setTouchable(Touchable.disabled);

                    btn.addActor(title);
                }

                btn.addListener(new ClickOverListener()
                {
                    @Override
                    public void clicked(InputEvent event, float x, float y)
                    {
                        Menu.playSound(MenuSound.select);

                        pop();

                        callback.selectedStandard(map);
                    }
                });

                maps.add(btn).size(140, 140).pad(8);

                i++;
                if (i % 6 == 0)
                {
                    maps.row();
                }
            }

            data.add(maps).padBottom(32).row();
        }

        if (BrainOutClient.Env.getGameUser().hasWorkshop())
        {
            // created
            {
                Table publishedMaps = new Table();

                GameUser.WorkshopItemsQuery query = BrainOutClient.Env.getGameUser().queryMyPublishedWorkshopItems();

                query.addRequiredTag("map");
                query.sendQuery(new GameUser.WorkshopItemsQueryCallback()
                {
                    @Override
                    public void success(Queue<GameUser.WorkshopItem> items, int results, int totalResults)
                    {
                        {
                            Table header = new Table();
                            Label title = new Label(L.get("MENU_MY_WORKSHOP_MAPS"), BrainOutClient.Skin, "title-small");

                            header.add(title).row();
                            header.add(new Image(BrainOutClient.Skin, "stroke-ui")).padBottom(8).padTop(8).row();

                            publishedMaps.add(header).row();
                        }

                        Table body = new Table();
                        renderWorkshopMaps(body, items);
                        publishedMaps.add(body).padBottom(32).row();
                    }

                    @Override
                    public void failed(String reason)
                    {
                    }
                });

                data.add(publishedMaps).row();
            }

            // subscribed
            {
                Table subscribedMaps = new Table();

                GameUser.WorkshopItemsQuery query = BrainOutClient.Env.getGameUser().queryMySubscribedWorkshopItems();

                query.addRequiredTag("map");
                query.sendQuery(new GameUser.WorkshopItemsQueryCallback()
                {
                    @Override
                    public void success(Queue<GameUser.WorkshopItem> items, int results, int totalResults)
                    {
                        {
                            Table header = new Table();
                            Label title = new Label(L.get("MENU_WORKSHOP_MAPS"), BrainOutClient.Skin, "title-small");

                            header.add(title).row();
                            header.add(new Image(BrainOutClient.Skin, "stroke-ui")).padBottom(8).padTop(8).row();

                            subscribedMaps.add(header).row();
                        }

                        Table body = new Table();
                        renderWorkshopMaps(body, items);
                        subscribedMaps.add(body).padBottom(32).row();
                    }

                    @Override
                    public void failed(String reason)
                    {
                    }
                });

                data.add(subscribedMaps).row();
            }
        }
    }

    private void renderWorkshopMaps(Table maps, Queue<GameUser.WorkshopItem> items)
    {
        int i = 0;

        for (GameUser.WorkshopItem item : items)
        {
            Button btn = new Button(BrainOutClient.Skin, "button-notext");

            {
                Image image = new Image();

                if (item.getPreviewURL() != null)
                {
                    Avatars.Get(item.getPreviewURL(), (has, avatar) ->
                    {
                        if (has)
                        {
                            image.setDrawable(new TextureRegionDrawable(avatar));
                        }
                    });
                }

                image.setScaling(Scaling.fit);
                image.setBounds(6, 6, 128, 128);
                image.setTouchable(Touchable.disabled);

                btn.addActor(image);
            }

            {
                Label title = new Label(item.getTitle(), BrainOutClient.Skin, "title-yellow");
                title.setAlignment(Align.bottom | Align.center);
                title.setFillParent(true);
                title.setWrap(true);
                title.setTouchable(Touchable.disabled);

                btn.addActor(title);
            }

            btn.addListener(new ClickOverListener()
            {
                @Override
                public void clicked(InputEvent event, float x, float y)
                {
                    Menu.playSound(MenuSound.select);

                    pop();

                    callback.selectedWorkshop(item.getID(), item.getTitle());
                }
            });

            maps.add(btn).size(140, 140).pad(8);

            i++;
            if (i % 6 == 0)
            {
                maps.row();
            }
        }
    }

    @Override
    public boolean escape()
    {
        pop();

        return true;
    }

    @Override
    protected TextureRegion getBackground()
    {
        return BrainOutClient.getRegion("bg-ingame");
    }

    @Override
    public boolean lockInput()
    {
        return super.lockInput();
    }
}
