package com.desertkun.brainout.menu.impl;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.actions.RepeatAction;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.ObjectMap;
import com.badlogic.gdx.utils.Scaling;
import com.desertkun.brainout.BrainOutClient;
import com.desertkun.brainout.ClientConstants;
import com.desertkun.brainout.Constants;
import com.desertkun.brainout.L;
import com.desertkun.brainout.client.ClientController;
import com.desertkun.brainout.client.RemoteClient;
import com.desertkun.brainout.common.msg.client.ReadyMsg;
import com.desertkun.brainout.content.Levels;
import com.desertkun.brainout.events.*;
import com.desertkun.brainout.menu.Menu;
import com.desertkun.brainout.menu.ui.Avatars;
import com.desertkun.brainout.menu.ui.ClickOverListener;
import com.desertkun.brainout.menu.ui.LoadingBlock;
import com.desertkun.brainout.menu.ui.Tooltip;
import com.desertkun.brainout.mode.GameMode;
import com.desertkun.brainout.mode.GameModeFree;
import com.desertkun.brainout.utils.TimeUtils;

public class FreePlayWarmupMenu extends Menu implements EventReceiver
{
    private final GameModeFree mode;
    private Table playerList;
    private Label playerCount;
    private ObjectMap<Integer, EventReceiver> remoteClientsMap;

    private TextButton readyButton;
    private Label readyLabel;

    public FreePlayWarmupMenu(GameModeFree mode)
    {
        this.mode = mode;
        this.remoteClientsMap = new ObjectMap<>();
    }

    @Override
    public Table createUI()
    {
        Table data = new Table();

        {
            Image icon = new Image(BrainOutClient.Skin, "icon-event-freeplay");

            icon.setScaling(Scaling.none);
            data.add(icon).padTop(48).size(64).padBottom(16).row();
        }

        {
            Label prepare = new Label(L.get("MENU_FREEPLAY_PREPARE"), BrainOutClient.Skin, "title-small");
            data.add(prepare).row();
        }

        {
            Table autoStartRoot = new Table();

            autoStartRoot.add(new Label(L.get("MENU_FREEPLAY_AUTOSTART"),
                    BrainOutClient.Skin, "title-yellow")).padRight(4);

            Label autoStart = new Label(getAutoStartInfo(), BrainOutClient.Skin, "title-small");
            autoStart.addAction(Actions.repeat(RepeatAction.FOREVER,
                    Actions.sequence(
                            Actions.delay(1.0f),
                            Actions.run(()-> autoStart.setText(getAutoStartInfo()))
                    )));

            autoStartRoot.add(autoStart).padLeft(4).row();

            data.add(autoStartRoot).padBottom(32).row();
        }

        {
            Table playersRoot = new Table(BrainOutClient.Skin);
            playersRoot.setBackground("form-gray");

            Label players = new Label(L.get("MENU_PLAYERS"), BrainOutClient.Skin, "title-yellow");
            playersRoot.add(players).padRight(4);

            playerCount = new Label(
                getPlayerStats(),
                BrainOutClient.Skin, "title-small");
            playersRoot.add(playerCount).padLeft(4).row();

            data.add(playersRoot).width(400).expandX().fillY().center().row();
        }

        {
            Table playerListRoot = new Table(BrainOutClient.Skin);
            playerListRoot.setBackground("form-default");

            playerList = new Table();
            playerList.align(Align.top | Align.center);

            ScrollPane pane = new ScrollPane(playerList, BrainOutClient.Skin, "scroll-default");
            pane.setFadeScrollBars(false);

            setScrollFocus(pane);

            playerListRoot.add(pane).expand().fill().row();
            data.add(playerListRoot).expand().width(400).center().fillY().row();
        }

        {
            readyLabel = new Label("", BrainOutClient.Skin, "title-gray-bg");
            readyLabel.setAlignment(Align.center);
            data.add(readyLabel).size(192, 32).padTop(16).center().row();
        }

        {
            readyButton = new TextButton(L.get("MENU_READY"), BrainOutClient.Skin, "button-checkable-green");

            readyButton.addListener(new ClickOverListener()
            {
                @Override
                public void clicked(InputEvent event, float x, float y)
                {
                    Menu.playSound(MenuSound.select);

                    ready(readyButton.isChecked());
                }
            });

            data.add(readyButton).pad(0, 16, 16, 16).center().size(192, 64).row();
        }

        updateClients();

        return data;
    }

    private void ready(boolean ready)
    {
        BrainOutClient.ClientController.sendTCP(new ReadyMsg(ready));
    }

    private String getPlayerStats()
    {
        return String.valueOf(BrainOutClient.ClientController.getRemoteClients().size) + " / " +
            BrainOutClient.ClientController.getMaxPlayers();
    }

    private String getAutoStartInfo()
    {
        return TimeUtils.formatMinutesInterval((long)mode.getTimer() * 1000L);
    }

    @Override
    protected MenuAlign getMenuAlign()
    {
        return MenuAlign.fill;
    }

    private void fetchAvatar(String avatar, Table avatarInfo)
    {
        Avatars.Get(avatar, (has, avatarTexture) ->
        {
            if (has)
            {
                Image avatarImage = new Image(avatarTexture);

                avatarImage.setScaling(Scaling.fit);

                avatarInfo.add(avatarImage).size(40, 40).row();
            }
        });
    }

    private void updateClients()
    {
        Levels levels = BrainOutClient.ClientController.getLevels(Constants.User.LEVEL);

        if (levels == null)
            return;

        playerList.clear();

        float levelScale = 0.25f;

        ClientController CC = BrainOutClient.ClientController;

        remoteClientsMap.clear();

        int required = (int)((float)CC.getMaxPlayers() * 0.75f);
        boolean enough = CC.getRemoteClients().size >= required;
        readyButton.setVisible(enough);
        readyLabel.setVisible(enough);

        int ready = 0;

        for (ObjectMap.Entry<Integer, RemoteClient> entry : CC.getRemoteClients())
        {
            RemoteClient remoteClient = entry.value;

            if (remoteClient == null)
                continue;

            Color color = CC.getColorOf(remoteClient);

            Table item = new Table();
            item.align(Align.left | Align.center);
            item.setTouchable(Touchable.disabled);
            item.setTransform(false);

            if (remoteClient.getClanAvatar() != null && !remoteClient.getClanAvatar().isEmpty())
            {
                Table avatarInfo = new Table();
                fetchAvatar(remoteClient.getClanAvatar(), avatarInfo);
                item.add(avatarInfo).padLeft(-2);
            }

            if (!remoteClient.getAvatar().isEmpty())
            {
                Table avatarInfo = new Table();

                fetchAvatar(remoteClient.getAvatar(), avatarInfo);

                item.add(avatarInfo).padLeft(-2);
            } else
            {
                Image def = new Image(BrainOutClient.Skin, "default-avatar");
                def.setScaling(Scaling.fit);
                item.add(def).size(40, 40).padLeft(-2);
            }

            Levels.Level level = levels.getLevel(remoteClient.getLevel());

            Label levelText = new Label(level.toString(), BrainOutClient.Skin, "player-list");
            levelText.setColor(color);
            levelText.setAlignment(Align.left);

            item.add(levelText).padLeft(4);

            TextureRegion levelImage = BrainOutClient.getRegion(level.icon);

            if (levelImage != null)
            {
                Image image = new Image(levelImage);
                item.add(image).size(levelImage.getRegionWidth() * levelScale,
                        levelImage.getRegionHeight() * levelScale).padLeft(4);
            }

            String showIds = Gdx.input.isButtonPressed(Input.Buttons.RIGHT) ?
                    ("[" + remoteClient.getId() + "] ") : "";

            Label clientName = new Label(showIds + remoteClient.getName(), BrainOutClient.Skin, "player-list");
            clientName.setColor(color);
            clientName.setAlignment(Align.left);
            clientName.setEllipsis(true);

            Tooltip.RegisterToolTip(clientName, "ID: " + remoteClient.getId(), this);

            item.add(clientName).padLeft(4).width(160).expandX().left();

            if (remoteClient.isReady())
            {
                Image readyIcon = new Image(BrainOutClient.Skin, "icon-ready");
                item.add(readyIcon).right();
            }

            {
                Table latency = new Table();
                long ping = remoteClient.getPing();
                String pingImage = ping <= Constants.Core.PING_GOOD ? "ping-good" :
                        ping <= Constants.Core.PING_NORMAL ? "ping-normal" : "ping-bad";
                latency.add(new Image(BrainOutClient.Skin, pingImage)).pad(4, 0, 4, 0);
                Label clientPing = new Label(String.valueOf(ping), BrainOutClient.Skin, "player-list-ping");
                latency.add(clientPing).fillX().padLeft(2).row();
                item.add(latency).right().width(48).row();
            }

            Button itemBorder = new Button(BrainOutClient.Skin, "button-notext");
            itemBorder.setUserObject(remoteClient);

            Image voice = new Image(BrainOutClient.Skin, "icon-voice-chat");
            voice.setScaling(Scaling.none);
            voice.setTouchable(Touchable.disabled);
            voice.setVisible(false);
            voice.setBounds(256, 0, 42, 42);

            item.addActor(voice);

            itemBorder.add(item).expand().fill();

            remoteClientsMap.put(remoteClient.getId(), event ->
            {
                if (event instanceof VoiceEvent)
                {
                    voice.clearActions();
                    voice.addAction(Actions.sequence(
                        Actions.visible(true),
                        Actions.delay(0.5f),
                        Actions.visible(false)
                    ));

                    return false;
                }

                return false;
            });

            playerList.add(itemBorder).expandX().fillX().height(48).row();

            if (remoteClient.isReady())
            {
                ready++;
            }
        }

        readyLabel.setText(String.valueOf(ready) + " / " + required);
    }

    @Override
    public void onInit()
    {
        super.onInit();

        BrainOutClient.EventMgr.subscribe(Event.ID.simple, this);
        BrainOutClient.EventMgr.subscribe(Event.ID.newRemoteClient, this);
        BrainOutClient.EventMgr.subscribe(Event.ID.remoteClientUpdated, this);
        BrainOutClient.EventMgr.subscribe(Event.ID.remoteClientLeft, this);
        BrainOutClient.EventMgr.subscribe(Event.ID.voice, this);

        {
            Table waiting = new Table();
            waiting.align(Align.right);

            if (BrainOutClient.Skin.has("title-small", Label.LabelStyle.class))
            {
                Label statusLabel = new Label(L.get("MENU_FREEPLAY_LOADING"),
                        BrainOutClient.Skin, "title-messages-white");
                waiting.add(statusLabel);
            }

            waiting.add(new LoadingBlock()).padLeft(16);

            waiting.setBounds(getWidth() - 256, 32, 224, 16);
            addActor(waiting);
        }
    }

    @Override
    public void onRelease()
    {
        super.onRelease();

        BrainOutClient.EventMgr.unsubscribe(Event.ID.simple, this);
        BrainOutClient.EventMgr.unsubscribe(Event.ID.newRemoteClient, this);
        BrainOutClient.EventMgr.unsubscribe(Event.ID.remoteClientUpdated, this);
        BrainOutClient.EventMgr.unsubscribe(Event.ID.remoteClientLeft, this);
        BrainOutClient.EventMgr.unsubscribe(Event.ID.voice, this);
    }

    @Override
    public boolean escape()
    {
        pushMenu(new ExitMenu());
        return true;
    }

    @Override
    public boolean onEvent(Event event)
    {
        switch (event.getID())
        {
            case voice:
            {
                VoiceEvent ev = ((VoiceEvent) event);

                if (ev.remoteClient != null)
                {
                    EventReceiver receiver = remoteClientsMap.get(ev.remoteClient.getId());
                    if (receiver != null)
                    {
                        if (receiver.onEvent(event))
                        {
                            return true;
                        }
                    }
                }
                break;
            }
            case newRemoteClient:
            case remoteClientUpdated:
            case remoteClientLeft:
            {
                updateStats();
                updateClients();

                break;
            }

            case simple:
            {
                SimpleEvent ev = ((SimpleEvent) event);

                switch (ev.getAction())
                {
                    case modeUpdated:
                    {
                        modeUpdated();
                        break;
                    }

                    case pingUpdated:
                    {
                        updateClients();
                        break;
                    }
                }

                break;
            }
        }

        return false;
    }

    private void updateStats()
    {
        playerCount.setText(getPlayerStats());
    }

    private void modeUpdated()
    {
        GameMode gameMode = BrainOutClient.ClientController.getGameMode();

        if (gameMode == null)
            return;

        if (gameMode.getPhase() != GameMode.Phase.warmUp)
        {
            pushMenu(new FadeInMenu(0.5f, this::pop));
        }
    }
}
