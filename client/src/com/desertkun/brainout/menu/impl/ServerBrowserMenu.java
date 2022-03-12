package com.desertkun.brainout.menu.impl;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.NinePatchDrawable;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.*;
import com.desertkun.brainout.BrainOutClient;
import com.desertkun.brainout.ClientConstants;
import com.desertkun.brainout.GameUser;
import com.desertkun.brainout.L;
import com.desertkun.brainout.client.ClientController;
import com.desertkun.brainout.menu.FormMenu;
import com.desertkun.brainout.menu.Menu;
import com.desertkun.brainout.menu.popups.RichAlertPopup;
import com.desertkun.brainout.menu.ui.Avatars;
import com.desertkun.brainout.menu.ui.BorderActor;
import com.desertkun.brainout.menu.ui.ClickOverListener;
import com.desertkun.brainout.menu.ui.Tooltip;
import com.desertkun.brainout.online.Matchmaking;
import com.desertkun.brainout.online.RoomSettings;
import com.desertkun.brainout.utils.ArrayUtils;
import com.desertkun.brainout.utils.Ping;
import org.anthillplatform.runtime.requests.Request;
import org.anthillplatform.runtime.services.GameService;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class ServerBrowserMenu extends FormMenu
{
    private final Callback callback;
    private Table content;

    private RoomSettings filter;
    private ClientController.RegionWrapper anyRegion;

    private ButtonGroup<Button> roomButtons;
    private TextButton connectButton;
    private Mode mode;

    public enum Mode
    {
        standard,
        workshop
    }

    public interface Callback
    {
        void selected(GameService.Room room);
        void cancelled();
        void newOne();
    }

    public Mode getMode()
    {
        return mode;
    }

    @Override
    public Table createUI()
    {
        Table data = new Table();

        if (BrainOutClient.Env.getGameUser().hasWorkshop())
        {
            renderTabs(data);
        }

        Table header = new Table();
        this.content = super.createUI();

        Table headers = new Table();
        headers.setBackground(new NinePatchDrawable(BrainOutClient.getNinePatch("form-gray")));

        {
            Label map = new Label(L.get("MENU_MAP"),
                    BrainOutClient.Skin, "title-yellow");
            map.setAlignment(Align.center);

            headers.add(map).uniformX().fillX().expandX();
        }
        {
            Label players = new Label(L.get("MENU_PLAYERS"),
                    BrainOutClient.Skin, "title-yellow");
            players.setAlignment(Align.center);

            headers.add(players).uniformX().fillX().expandX();
        }
        {
            Label mode = new Label(L.get("MENU_MODE"),
                    BrainOutClient.Skin, "title-yellow");
            mode.setAlignment(Align.center);

            headers.add(mode).uniformX().fillX().expandX();
        }

        if (mode == Mode.standard)
        {
            Label levels = new Label(L.get("MENU_PLAYER_LEVELS"),
                    BrainOutClient.Skin, "title-yellow");
            levels.setAlignment(Align.center);

            headers.add(levels).uniformX().fillX().expandX();
        }

        {
            Label ping = new Label(L.get("MENU_PRESET"),
                    BrainOutClient.Skin, "title-yellow");
            ping.setAlignment(Align.center);

            headers.add(ping).minWidth(200);
        }
        {
            Label ping = new Label(L.get("MENU_PLAYER_PING"),
                    BrainOutClient.Skin, "title-yellow");
            ping.setAlignment(Align.center);

            headers.add(ping).uniformX().fillX().expandX();
        }


        header.add(headers).minWidth(740).expandX().fillX().row();

        renderLoading();

        Table filters = new Table();
        filters.setBackground(new NinePatchDrawable(BrainOutClient.getNinePatch("form-default")));
        renderFilters(filters);

        Table buttons = new Table();
        buttons.setBackground(new NinePatchDrawable(BrainOutClient.getNinePatch("form-default")));
        renderButtons(buttons);

        data.add(header).expandX().fillX().row();
        data.add(content).minSize(600, 412).maxHeight(412).expandX().fillX().row();
        data.add(filters).padTop(-2).expandX().fillX().row();
        data.add(buttons).padTop(-2).expandX().fillX().row();

        return data;
    }

    private void renderTabs(Table data)
    {
        {
            Table tabs = new Table();

            ButtonGroup<TextButton> group = new ButtonGroup<>();

            group.setMinCheckCount(1);
            group.setMaxCheckCount(1);

            {
                TextButton standard = new TextButton(L.get("MENU_STANDARD_MAPS"), BrainOutClient.Skin, "button-tab");
                standard.setChecked(getMode() == Mode.standard);

                standard.addListener(new ClickOverListener()
                {
                    @Override
                    public void clicked(InputEvent event, float x, float y)
                    {
                        Menu.playSound(MenuSound.select);
                        setMode(Mode.standard);
                    }
                });

                tabs.add(standard).expandX().uniformX().fillX();
                group.add(standard);
            }

            {
                TextButton workshopMaps = new TextButton(L.get("MENU_WORKSHOP_MAPS"), BrainOutClient.Skin, "button-tab");
                workshopMaps.setChecked(getMode() == Mode.workshop);

                workshopMaps.addListener(new ClickOverListener()
                {
                    @Override
                    public void clicked(InputEvent event, float x, float y)
                    {
                        Menu.playSound(MenuSound.select);
                        setMode(Mode.workshop);
                    }
                });

                tabs.add(workshopMaps).expandX().uniformX().fillX();
                group.add(workshopMaps);
            }

            data.add(tabs).expandX().fillX().row();
        }
    }

    private void setMode(Mode mode)
    {
        if (mode == this.mode)
            return;

        popMeAndPushMenu(new ServerBrowserMenu(callback, filter, mode));
    }

    private void renderFilters(Table filters)
    {
        {
            SelectBox<ClientController.RegionWrapper> checkBox =
                new SelectBox<>(BrainOutClient.Skin, "select-badged");

            ClientController.RegionWrapper select = anyRegion;

            Array<ClientController.RegionWrapper> items = new Array<>();

            items.add(anyRegion);

            for (ClientController.RegionWrapper region : BrainOutClient.ClientController.getRegions())
            {
                items.add(region);
            }

            if (filter.getRegion() != null)
            {
                for (ClientController.RegionWrapper region : BrainOutClient.ClientController.getRegions())
                {
                    if (region.region.name.equals(filter.getRegion()))
                    {
                        select = region;
                        break;
                    }
                }
            }

            checkBox.setItems(items);
            checkBox.setSelected(select);

            filters.add(checkBox).height(32).expandX().fillX().pad(8).padRight(0);

            Image regionIcon = new Image();
            regionIcon.setScaling(Scaling.none);
            regionIcon.setTouchable(Touchable.disabled);
            filters.add(regionIcon).padLeft(-40).padRight(0).size(24, 16);

            if (select != anyRegion)
            {
                String flag = select.region.settings != null ? select.region.settings.optString("flag", null) : null;
                if (flag != null)
                {
                    TextureAtlas.AtlasRegion flagRegion = BrainOutClient.getRegion(flag);
                    if (flagRegion != null)
                    {
                        regionIcon.setDrawable(new TextureRegionDrawable(flagRegion));
                    }
                }
            }

            checkBox.addListener(new ChangeListener()
            {
                @Override
                public void changed(ChangeEvent event, Actor actor)
                {
                    Menu.playSound(MenuSound.select);

                    if (checkBox.getSelected() != anyRegion)
                    {
                        filter.setRegion(checkBox.getSelected().region.name);
                        String flag = checkBox.getSelected().region.settings != null ?
                            checkBox.getSelected().region.settings.optString("flag", null) : null;
                        if (flag != null)
                        {
                            regionIcon.setDrawable(new TextureRegionDrawable(BrainOutClient.getRegion(flag)));
                        }
                    }
                    else
                    {
                        filter.setRegion(null);
                        regionIcon.setDrawable(null);
                    }


                    refreshRooms();
                }
            });
        }

        if (mode == Mode.standard)
        {
            CheckBox checkBox = new CheckBox(L.get("MENU_MY_LEVEL_ONLY"), BrainOutClient.Skin, "checkbox-default");

            checkBox.setChecked(filter.isMyLevelOnly());

            checkBox.addListener(new ClickOverListener()
            {
                @Override
                public void clicked(InputEvent event, float x, float y)
                {
                    Menu.playSound(MenuSound.select);

                    filter.setMyLevelOnly(checkBox.isChecked());

                    refreshRooms();
                }
            });

            filters.add(checkBox).pad(8);
        }

        filter.setIgnoreMyLevelOnly(mode == Mode.workshop);

        {
            CheckBox checkBox = new CheckBox(L.get("MENU_SHOW_FULL"), BrainOutClient.Skin, "checkbox-default");

            checkBox.setChecked(filter.isShowFull());

            checkBox.addListener(new ClickOverListener()
            {
                @Override
                public void clicked(InputEvent event, float x, float y)
                {
                    Menu.playSound(MenuSound.select);

                    filter.setShowFull(checkBox.isChecked());

                    refreshRooms();
                }
            });

            filters.add(checkBox).pad(8);
        }

        /*
        {
            ImageButton otherFilters = new ImageButton(BrainOutClient.Skin, "button-filters");

            otherFilters.addListener(new ClickOverListener()
            {
                @Override
                public void clicked(InputEvent event, float x, float y)
                {
                    Menu.playSound(MenuSound.select);

                    pushMenu(new ServerBrowserFiltersMenu(
                        settings -> refreshRooms(), filter));
                }
            });

            filters.add(otherFilters).expandX().right().size(32).pad(8).row();
        }
        */
    }

    private String getServerName()
    {
        switch (getMode())
        {
            case workshop:
            {
                return "custom";
            }
            case standard:
            default:
            {
                return "main";
            }
        }
    }

    @Override
    public void onInit()
    {
        super.onInit();

        Matchmaking.ListGames(getServerName(), filter,
            (service, request, result, rooms) -> Gdx.app.postRunnable(() ->
        {
            switch (result)
            {
                case success:
                {
                    renderRoomsPrepare(rooms);

                    break;
                }
                default:
                {
                    renderError(result);
                    break;
                }
            }
        }));
    }

    private void renderRoomsPrepare(List<GameService.Room> rooms)
    {
        switch (getMode())
        {
            case standard:
            {
                renderRooms(rooms, null);
                break;
            }
            case workshop:
            {
                Queue<String> roomIds = new Queue<>();

                for (GameService.Room room : rooms)
                {
                    RoomSettings roomSettings = new RoomSettings();
                    roomSettings.init(BrainOutClient.ClientController.getUserProfile(), false);
                    roomSettings.read(room.settings);

                    if (!roomSettings.getMap().isDefined())
                        continue;

                    roomIds.addLast(roomSettings.getMap().getValue());
                }

                if (roomIds.size == 0)
                {
                    renderRooms(rooms, new ObjectMap<>());
                    return;
                }

                BrainOutClient.Env.getGameUser().queryWorkshopItems(
                    roomIds, new GameUser.WorkshopItemsQueryCallback()
                {
                    @Override
                    public void success(Queue<GameUser.WorkshopItem> items, int results, int totalResults)
                    {
                        ObjectMap<String, GameUser.WorkshopItem> workshop = new ObjectMap<>();
                        for (GameUser.WorkshopItem item : items)
                        {
                            workshop.put(item.getID(), item);
                        }

                        renderRooms(rooms, workshop);
                    }

                    @Override
                    public void failed(String reason)
                    {
                        renderError(Request.Result.failed);
                    }
                });

                break;
            }
        }
    }

    private void renderRooms(List<GameService.Room> rooms, ObjectMap<String, GameUser.WorkshopItem> workshop)
    {
        if (rooms.size() == 0)
        {
            clearSelection();

            renderEmpty();
            return;
        }

        content.clear();

        Table items = new Table();
        items.align(Align.top);

        ScrollPane pane = new ScrollPane(items, BrainOutClient.Skin, "scroll-default");
        setScrollFocus(pane);

        renderItems(items, rooms, workshop);

        content.add(pane).expand().fill().row();
    }

    private void clearSelection()
    {
        roomButtons = null;

        connectButton.setDisabled(true);
    }

    private void renderItems(Table items, List<GameService.Room> rooms, ObjectMap<String,
        GameUser.WorkshopItem> workshop)
    {
        connectButton.setText(L.get("MENU_CONNECT"));
        connectButton.setDisabled(false);

        roomButtons = new ButtonGroup<>();
        roomButtons.setMaxCheckCount(1);
        roomButtons.setMinCheckCount(1);

        for (GameService.Room room: rooms)
        {

            RoomSettings roomSettings = new RoomSettings();
            roomSettings.init(BrainOutClient.ClientController.getUserProfile(), true);
            roomSettings.read(room.settings);

            GameUser.WorkshopItem workshopItem = workshop != null && roomSettings.getMap().isDefined() ?
                    workshop.get(roomSettings.getMap().getValue()) : null;

            String mapName;
            boolean verified;

            switch (getMode())
            {
                case workshop:
                {
                    if (workshopItem == null)
                        continue;

                    if (roomSettings.getSubscribers() < ClientConstants.Workshop.WORKSHOP_MINIMUM_SUBSCRIPTIONS)
                        continue;

                    verified = ArrayUtils.Contains(workshopItem.getTags(), "verified");

                    mapName = workshopItem.getTitle();
                    break;
                }
                case standard:
                default:
                {
                    verified = false;
                    mapName = roomSettings.getMap().getValue();
                    break;
                }
            }

            Button row = new Button(BrainOutClient.Skin, "button-notext-checkable");
            row.setUserObject(room);

            roomButtons.add(row);

            {
                Table title = new Table();

                Label map = new Label(mapName,
                        BrainOutClient.Skin, "title-small");
                map.setAlignment(Align.center);

                title.add(map);

                if (verified)
                {
                    Image verified_ = new Image(BrainOutClient.Skin, "icon-verified");
                    verified_.setScaling(Scaling.none);
                    title.add(verified_).size(24).padRight(4);
                }

                row.add(title).uniformX().fillX().expandX();
            }
            {
                String num = String.valueOf(room.players) + "/" + room.maxPlayers;

                Label players = new Label(num,
                        BrainOutClient.Skin, room.players > 0 ? "title-green" : "title-small");
                players.setAlignment(Align.center);

                row.add(players).uniformX().fillX().expandX();
            }
            {
                Label mode = new Label(roomSettings.getMode().getValue(),
                        BrainOutClient.Skin, "title-small");
                mode.setAlignment(Align.center);

                row.add(mode).uniformX().fillX().expandX();
            }

            if (mode == Mode.standard)
            {
                int level = roomSettings.getLevel();
                int a = Math.max(level - filter.getLevelGap(), 1);
                int b = Math.min(level + filter.getLevelGap(), 68);

                Label levels = new Label(String.valueOf(a) + " - " + b,
                        BrainOutClient.Skin, "title-small");
                levels.setAlignment(Align.center);

                row.add(levels).uniformX().fillX().expandX();
            }
            {
                String presetTitle = null;

                if (roomSettings.getPreset() != null)
                {
                    presetTitle = ClientConstants.Presets.PRESETS.get(roomSettings.getPreset());
                }

                if (presetTitle == null)
                {
                    presetTitle = "";
                }

                Label setting = new Label(L.get(presetTitle), BrainOutClient.Skin, "title-small");
                setting.setAlignment(Align.center);
                setting.setEllipsis(true);

                row.add(setting).width(200);
            }
            {
                Label ping = new Label("...",
                        BrainOutClient.Skin, "title-small");
                ping.setAlignment(Align.center);

                Ping.GetLatency(room.host, (success, time) ->
                {
                    if (success)
                    {
                        ping.setText(String.valueOf(time));

                        if (time < 100)
                        {
                            ping.setStyle(BrainOutClient.Skin.get("title-green", Label.LabelStyle.class));
                        }
                    }
                    else
                    {
                        ping.setText("!");
                        ping.setStyle(BrainOutClient.Skin.get("title-red", Label.LabelStyle.class));
                    }
                });

                row.add(ping).uniformX().fillX().expandX();
            }

            items.add(row).expandX().fillX().row();
        }
    }

    private void renderError(Request.Result status)
    {
        content.clear();

        Label loading = new Label(L.get("MENU_ONLINE_ERROR", status.toString()),
                BrainOutClient.Skin, "title-messages-red");
        loading.setAlignment(Align.center);
        content.add(loading).pad(8).expand().fill().row();
    }

    private void renderEmpty()
    {
        content.clear();

        connectButton.setText(L.get("MENU_QUICK_PLAY"));

        Label loading = new Label(L.get("MENU_NO_SERVERS"),
                BrainOutClient.Skin, "title-gray");
        loading.setAlignment(Align.center);
        content.add(loading).pad(8).expand().fill().row();
    }

    private void renderLoading()
    {
        content.clear();

        Label loading = new Label(L.get("MENU_LOADING"),
                BrainOutClient.Skin, "title-gray");
        loading.setAlignment(Align.center);
        content.add(loading).pad(8).expand().fill().row();
    }

    private void renderButtons(Table buttons)
    {
        TextButton cancel = new TextButton(
            L.get("MENU_CANCEL"),
            BrainOutClient.Skin, "button-default");

        cancel.addListener(new ClickOverListener()
        {
            @Override
            public void clicked(InputEvent event, float x, float y)
            {
                Menu.playSound(MenuSound.back);
                pop();
                callback.cancelled();
            }
        });

        buttons.add(cancel).expandX().left().size(192, 32).pad(8);

        TextButton joinRoomById = new TextButton("ID", BrainOutClient.Skin, "button-gray");
        Tooltip.RegisterToolTip(joinRoomById, L.get("MENU_JOIN_BY_ROOM_ID"), this);

        joinRoomById.addListener(new ClickOverListener()
        {
            @Override
            public void clicked(InputEvent event, float x, float y)
            {
                Menu.playSound(MenuSound.select);

                pushMenu(new RoomIDMenu()
                {
                    @Override
                    void join(String roomId)
                    {
                        Matchmaking.JoinGame(roomId, new Matchmaking.JoinGameResult()
                        {
                            @Override
                            public void complete(String roomId)
                            {
                                BrainOutClient.Env.setCurrentRoom(roomId);
                            }

                            @Override
                            public void failed(Request.Result status, Request request)
                            {
                                pushMenu(new RichAlertPopup(L.get("MENU_JOIN_BY_ROOM_ID"), L.get("MENU_PROMO_ERROR")));
                            }

                            @Override
                            public void connectionFailed()
                            {
                                pushMenu(new RichAlertPopup(L.get("MENU_JOIN_BY_ROOM_ID"), L.get("MENU_PROMO_ERROR")));
                            }
                        });
                    }

                    @Override
                    void error()
                    {
                        pushMenu(new RichAlertPopup(L.get("MENU_JOIN_BY_ROOM_ID"), L.get("MENU_PROMO_ERROR")));
                    }
                });
            }
        });

        buttons.add(joinRoomById).right().size(48, 32).pad(8);

        ImageButton refresh = new ImageButton(BrainOutClient.Skin, "button-refresh");

        refresh.addListener(new ClickOverListener()
        {
            @Override
            public void clicked(InputEvent event, float x, float y)
            {
                Menu.playSound(MenuSound.select);

                refreshRooms();
            }
        });

        buttons.add(refresh).right().size(32, 32).pad(8);

        connectButton = new TextButton(
                L.get("MENU_CONNECT"),
                BrainOutClient.Skin, "button-yellow");

        connectButton.addListener(new ClickOverListener()
        {
            @Override
            public void clicked(InputEvent event, float x, float y)
            {
                Menu.playSound(MenuSound.select);

                if (roomButtons != null && roomButtons.getChecked() != null)
                {
                    pop();

                    GameService.Room room = ((GameService.Room) roomButtons.getChecked().getUserObject());

                    if (!room.isFull())
                    {
                        callback.selected(room);
                    }
                }
                else
                {
                    pop();
                    callback.newOne();
                }
            }
        });

        buttons.add(connectButton).right().size(192, 32).pad(8);

    }

    private void refreshRooms()
    {
        renderLoading();

        Matchmaking.ListGames(getServerName(), filter, (service, request, result, rooms) -> Gdx.app.postRunnable(() ->
        {
            switch (result)
            {
                case success:
                {
                    renderRoomsPrepare(rooms);

                    break;
                }
                default:
                {
                    renderError(result);

                    break;
                }
            }
        }));
    }

    public ServerBrowserMenu(Callback callback, RoomSettings filter, Mode mode)
    {
        this.callback = callback;
        this.mode = mode;
        this.filter = filter;
        this.anyRegion = new ClientController.RegionWrapper(new GameService.Region(L.get("MENU_ANY_REGION"), null));
    }

    @Override
    public boolean escape()
    {
        pop();

        return true;
    }

    @Override
    public boolean lockInput()
    {
        return true;
    }
}
