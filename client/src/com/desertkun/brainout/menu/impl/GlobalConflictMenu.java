package com.desertkun.brainout.menu.impl;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Group;
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
import com.desertkun.brainout.content.GlobalConflict;
import com.desertkun.brainout.menu.FormMenu;
import com.desertkun.brainout.menu.Menu;
import com.desertkun.brainout.menu.popups.RichAlertPopup;
import com.desertkun.brainout.menu.ui.ClickOverListener;
import com.desertkun.brainout.menu.ui.Tooltip;
import com.desertkun.brainout.online.Matchmaking;
import com.desertkun.brainout.online.RoomSettings;
import com.desertkun.brainout.utils.ArrayUtils;
import com.desertkun.brainout.utils.Ping;
import org.anthillplatform.runtime.requests.Request;
import org.anthillplatform.runtime.services.EnvironmentService;
import org.anthillplatform.runtime.services.GameService;
import org.anthillplatform.runtime.services.LoginService;
import org.anthillplatform.runtime.services.ProfileService;
import org.json.JSONObject;

import java.util.List;

public class GlobalConflictMenu extends FormMenu
{
    private final Callback callback;
    private Table content;
    private GlobalConflict.Owner myOwner;
    private GlobalConflict globalConflict;
    private long conflictStart;

    private RoomSettings filter;
    private ClientController.RegionWrapper anyRegion;

    private ButtonGroup<Button> roomButtons;

    public interface Callback
    {
        void selected(GameService.Room room);
        void cancelled();
        void newOne(String zoneKey);
    }

    @Override
    public Table createUI()
    {
        Table data = new Table();

        globalConflict = BrainOutClient.ContentMgr.get("global-conflict", GlobalConflict.class);

        Table header = new Table();
        this.content = super.createUI();

        renderLoading();

        Table buttons = new Table();
        buttons.setBackground(new NinePatchDrawable(BrainOutClient.getNinePatch("form-default")));
        renderButtons(buttons);

        data.add(header).expandX().fillX().row();
        data.add(content).minSize(600, 600).maxHeight(600).expandX().fillX().row();
        data.add(buttons).padTop(-2).expandX().fillX().row();

        return data;
    }

    private String getServerName()
    {
        return "main";
    }

    @Override
    public void onInit()
    {
        super.onInit();

        EnvironmentService env = EnvironmentService.Get();
        ProfileService profileService = ProfileService.Get();
        LoginService loginService = LoginService.Get();

        if (env == null || profileService == null || loginService == null)
            return;

        String conflictAccount = (String)env.getEnvironmentVariables().get("conflict-account");

        if (conflictAccount == null)
        {
            conflictAccount = defaultConflictAccount();
        }

        profileService.getAccountProfile(loginService.getCurrentAccessToken(), conflictAccount,
            (profileService1, request, result, profile) -> Gdx.app.postRunnable(() ->
        {
            if (result == Request.Result.success)
            {
                Matchmaking.ListGames(getServerName(), filter,
                    (service, request1, result2, rooms) -> Gdx.app.postRunnable(() ->
                {
                    switch (result2)
                    {
                        case success:
                        {
                            renderRoomsPrepare(rooms, profile);

                            break;
                        }
                        default:
                        {
                            renderError(result2);
                            break;
                        }
                    }
                }));
            }
            else
            {
                renderError(result);
            }
        }));
    }

    private void renderRoomsPrepare(List<GameService.Room> rooms, JSONObject profile)
    {
        JSONObject status = profile.optJSONObject("conflict");
        if (status == null)
        {
            status = new JSONObject();
        }

        renderRooms(rooms, status);
    }

    private void renderRooms(List<GameService.Room> rooms, JSONObject status)
    {
        content.clear();

        Group group = new Group();

        float w = 115;
        float h = 94;

        float globalWidth = (globalConflict.getWidth() + 0.5f) * (w * 0.75f + 2);
        float globalHeight = (globalConflict.getHeight() + 0.5f) * (h + 2);

        group.setSize(1000, 600);

        float offsetX = (group.getWidth() - globalWidth) / 2;
        float offsetY = (group.getHeight() - globalHeight) / 2;

        ObjectMap<String, Button> buttons = new ObjectMap<>();

        GlobalConflict.ConflictData data = globalConflict.getData(status, conflictStart);

        for (GlobalConflict.ConflictData.ZoneData zoneData : data.getZones())
        {
            GlobalConflict.Zone zone = zoneData.getZone();

            Button b = new Button(BrainOutClient.Skin,
                zoneData.getOwner() == GlobalConflict.Owner.neutral ? "hex-neutral" :
                    (zoneData.getOwner() == myOwner ? "hex-blue" : "hex-orange"));

            buttons.put(zone.getKey(), b);

            if (zoneData.getOwner() == GlobalConflict.Owner.neutral)
            {
                b.addListener(new ClickOverListener()
                {
                    @Override
                    public void clicked(InputEvent event, float x, float y)
                    {
                        pop();

                        if (zoneData.getStatus() == GlobalConflict.ZoneStatus.ongoing)
                        {
                            callback.selected(zoneData.getRoom());
                        }
                        else
                        {
                            callback.newOne(zoneData.getKey());
                        }
                    }
                });
            }

            b.setSize(w, h);

            float x = zone.getPhysicalX() * (w * 0.75f + 2);
            float y = zone.getPhysicalY() * (h + 2);
            b.setPosition(x + offsetX, y + offsetY);
            group.addActor(b);
        }

        for (GameService.Room room : rooms)
        {
            String zone = room.settings.optString("zone");
            if (zone == null)
                continue;

            GlobalConflict.ConflictData.ZoneData zoneData = data.getByKey(zone);
            if (zoneData == null)
                continue;

            int myPlayers = room.settings.optInt("players-" + myOwner.toString(), 0);
            if (myPlayers >= (room.maxPlayers / 2))
            {
                zoneData.setStatus(GlobalConflict.ZoneStatus.full);
                continue;
            }

            if (zoneData.getStatus() == GlobalConflict.ZoneStatus.ongoing)
                continue;

            zoneData.setStatus(GlobalConflict.ZoneStatus.ongoing);
            zoneData.setRoomId(room);
        }

        for (GlobalConflict.ConflictData.ZoneData zone : data.getZones())
        {
            Button b = buttons.get(zone.getKey());

            if (zone.getOwner() != GlobalConflict.Owner.neutral)
            {
                Image im = new Image(BrainOutClient.Skin,
                    zone.getOwner() == myOwner ? "assault-icon-friend" : "assault-icon-enemy");
                im.setScaling(Scaling.none);
                im.setTouchable(Touchable.disabled);
                b.add(im).size(64, 64);

                continue;
            }

            if (zone.getStatus() == GlobalConflict.ZoneStatus.ongoing)
            {
                GameService.Room room = zone.getRoom();
                if (room == null)
                    continue;
                String statusText = room.players + " / " + room.maxPlayers;
                Label s = new Label(statusText, BrainOutClient.Skin, "title-small");
                b.add(s).expandX().fill();
            }
            else
            {
                Image im = new Image(BrainOutClient.Skin, "icon-shop-special-healthbox");
                im.setScaling(Scaling.none);
                im.setTouchable(Touchable.disabled);
                b.add(im).size(32, 32);
            }
        }

        content.add(group).size(1000, 600).row();

        /*
        Table items = new Table();
        items.align(Align.top);

        ScrollPane pane = new ScrollPane(items, BrainOutClient.Skin, "scroll-default");
        setScrollFocus(pane);

        renderItems(items, rooms, workshop);

        content.add(pane).expand().fill().row();
         */
    }

    private void clearSelection()
    {
        roomButtons = null;
    }

    private void renderItems(Table items, List<GameService.Room> rooms, ObjectMap<String,
        GameUser.WorkshopItem> workshop)
    {
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

            verified = false;
            mapName = roomSettings.getMap().getValue();

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
                int myPlayers = room.settings.optInt("players-" + myOwner.toString(), 0);
                String num = String.valueOf(myPlayers) + "/" + (room.maxPlayers / 2);

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

            int level = roomSettings.getLevel();
            int a = Math.max(level - filter.getLevelGap(), 1);
            int b = Math.min(level + filter.getLevelGap(), 68);

            Label levels = new Label(String.valueOf(a) + " - " + b,
                    BrainOutClient.Skin, "title-small");
            levels.setAlignment(Align.center);

            row.add(levels).uniformX().fillX().expandX();

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

    }

    private void refreshRooms()
    {
        renderLoading();

        EnvironmentService env = EnvironmentService.Get();
        ProfileService profileService = ProfileService.Get();
        LoginService loginService = LoginService.Get();

        if (env == null || profileService == null || loginService == null)
            return;

        String conflictAccount = (String)env.getEnvironmentVariables().get("conflict-account");

        if (conflictAccount == null)
        {
            conflictAccount = defaultConflictAccount();
        }

        profileService.getAccountProfile(loginService.getCurrentAccessToken(), conflictAccount,
            (profileService1, request, result, profile) -> Gdx.app.postRunnable(() ->
        {
            if (result == Request.Result.success)
            {
                Matchmaking.ListGames(getServerName(), filter, (service, request2, result2, rooms) ->
                    Gdx.app.postRunnable(() ->
                {
                    switch (result2)
                    {
                        case success:
                        {
                            renderRoomsPrepare(rooms, profile);

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
            else
            {
                renderError(result);
            }
        }));
    }

    private String defaultConflictAccount()
    {
        return "4";
    }

    public GlobalConflictMenu(Callback callback, RoomSettings filter, long conflictStart, GlobalConflict.Owner myOwner)
    {
        this.conflictStart = conflictStart;
        this.myOwner = myOwner;
        this.callback = callback;
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
