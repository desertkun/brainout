package com.desertkun.brainout.menu.impl;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.desertkun.brainout.*;
import com.desertkun.brainout.common.enums.DisconnectReason;
import com.desertkun.brainout.data.Map;
import com.desertkun.brainout.gs.GameState;
import com.desertkun.brainout.menu.Menu;
import com.desertkun.brainout.menu.popups.RichConfirmationPopup;
import com.desertkun.brainout.menu.ui.ClickOverListener;
import com.desertkun.brainout.mode.ClientGameRealization;
import com.desertkun.brainout.mode.GameModeRealization;
import com.desertkun.brainout.playstate.ClientPSGame;
import com.desertkun.brainout.playstate.PlayState;
import com.desertkun.brainout.utils.RoomIDEncryption;

public class ExitMenu extends Menu
{
    private final boolean enableActiveButtons;
    private Table data;
    private boolean onTop;

    public ExitMenu(boolean enableActiveButtons)
    {
        onTop = true;
        this.enableActiveButtons = BrainOut.OnlineEnabled() && enableActiveButtons;
    }

    public ExitMenu()
    {
        this(true);
    }

    @Override
    public Table createUI()
    {
        this.data = new Table();

        addButton(L.get("MENU_CONTINUE"), this::close);
        if (enableActiveButtons)
        {
            if (canInviteFriend())
            {
                addButton(L.get("MENU_INVITE"), this::invite);
            }
        }
        //addButton(L.get("MENU_VOTE"), this::vote);
        addButton(L.get("MENU_SETTINGS"), this::settings);
        if (enableActiveButtons)
        {
            addButton(L.get("MENU_ENTER_PROMO_BTN"), this::promo);
        }
        addButton(L.get("MENU_EXIT"), this::exit);

        String currentRoom = BrainOutClient.Env.getCurrentRoom();

        if (currentRoom != null && !currentRoom.isEmpty())
        {
            String encrypted = RoomIDEncryption.EncryptHumanReadable(currentRoom);

            if (encrypted != null)
            {
                Table currentRoomTable = new Table();

                TextButton roomId = new TextButton(encrypted, BrainOutClient.Skin, "button-text-clear");

                roomId.addListener(new ClickOverListener()
                {
                    @Override
                    public void clicked(InputEvent event, float x, float y)
                    {
                        Menu.playSound(MenuSound.select);

                        Gdx.app.getClipboard().setContents(encrypted);

                        roomId.setText(L.get("MENU_COPIED"));
                        roomId.addAction(Actions.sequence(
                            Actions.delay(1.0f),
                            Actions.run(() -> roomId.setText(encrypted))
                        ));
                    }
                });

                currentRoomTable.add(new Label(L.get("MENU_ROOM_ID"), BrainOutClient.Skin, "title-gray")).row();
                currentRoomTable.add(roomId).height(32).expandX().fillX().row();

                data.add(currentRoomTable).width(240).padTop(32).row();
            }
        }

        {
            Map map = Map.GetDefault();

            if (map != null && BrainOutClient.Env.getGameUser().hasWorkshop())
            {
                String workshopId = map.getCustom("workshop-id");

                if (workshopId != null)
                {
                    Table workshopButtonTable = new Table();

                    TextButton seeMap = new TextButton(
                        L.get("MENU_SEE_MAP_ON_WORKSHOP"), BrainOutClient.Skin, "button-text-clear");

                    seeMap.addListener(new ClickOverListener()
                    {
                        @Override
                        public void clicked(InputEvent event, float x, float y)
                        {
                            Menu.playSound(MenuSound.select);

                            BrainOutClient.Env.openURI(
                                "https://steamcommunity.com/sharedfiles/filedetails/?id=" + workshopId);
                        }
                    });

                    workshopButtonTable.add(seeMap).height(32).expandX().fillX().row();

                    data.add(workshopButtonTable).width(240).padTop(32).row();
                }
            }
        }

        return data;
    }

    private boolean canInviteFriend()
    {
        PlayState playState = BrainOutClient.ClientController.getPlayState();

        if (playState == null)
            return false;

        if (!(playState instanceof ClientPSGame))
            return false;

        ClientPSGame clientPSGame = ((ClientPSGame) playState);

        return clientPSGame.getMode().canInviteFriend();
    }

    @Override
    public boolean stayOnTop()
    {
        return onTop;
    }

    @Override
    public boolean escape()
    {
        close();
        return true;
    }

    private void close()
    {
        onTop = false;
        pop();
    }

    private void settings()
    {
        onTop = false;
        popMeAndPushMenu(new SettingsMenu());
    }

    @Override
    protected MenuAlign getMenuAlign()
    {
        return MenuAlign.center;
    }

    private void promo()
    {
        GameState gs = getGameState();

        if (gs == null)
            return;

        close();
        gs.pushMenu(new PromoCodeMenu());
    }

    private void invite()
    {
        GameState gs = getGameState();

        if (gs == null)
            return;

        close();

        gs.pushMenu(new InviteMenu(L.get("MENU_INVITE_FRIENDS"),
            friend -> BrainOutClient.Env.getGameUser().inviteFriend(friend)));
    }

    private void vote()
    {
        GameState gs = getGameState();

        if (gs == null)
            return;

        close();
        gs.pushMenu(new NewVoteMenu());
    }

    private void exit()
    {
        GameState gs = getGameState();

        if (gs == null)
            return;

        close();

        if (BrainOutClient.ClientController.getRemoteClients().size > 3)
        {
            PlayState playState = BrainOutClient.ClientController.getPlayState();

            if (playState instanceof ClientPSGame)
            {
                ClientPSGame psGame = ((ClientPSGame) playState);

                switch (psGame.getPhase())
                {
                    case game:
                    case aboutToEnd:
                    {
                        GameModeRealization realization = psGame.getMode().getRealization();

                        if (realization instanceof ClientGameRealization)
                        {
                            ClientGameRealization cgr = ((ClientGameRealization) realization);

                            if (cgr.isTookPartInWarmUp() && !BrainOutClient.ClientController.isFreePlay())
                            {
                                tryDesert(gs);
                                return;
                            }
                        }

                        break;
                    }
                }
            }
        }

        leave();
    }

    private void tryDesert(GameState gs)
    {
        gs.pushMenu(new RichConfirmationPopup(L.get("MENU_LEAVE_DESCRIPTION"))
        {
            @Override
            public String buttonYes()
            {
                return L.get("MENU_LEAVE_CONTINUE");
            }

            @Override
            public String buttonNo()
            {
                return L.get("MENU_LEAVE_DESERT");
            }

            @Override
            public String buttonStyleYes()
            {
                return "button-small";
            }

            @Override
            protected float getButtonHeight()
            {
                return 64;
            }

            @Override
            public String buttonStyleNo()
            {
                return "button-small";
            }

            @Override
            public String getTitle()
            {
                return L.get("MENU_LEAVE_TITLE");
            }

            @Override
            protected boolean reverseOrder()
            {
                return true;
            }

            @Override
            protected float getFade()
            {
                return Constants.Menu.MENU_BACKGROUND_FADE_DOUBLE;
            }

            @Override
            public void no()
            {
                leave();
            }
        });
    }

    private void leave()
    {
        if (!BrainOut.OnlineEnabled())
        {
            Gdx.app.exit();
            return;
        }

        BrainOutClient.ClientController.disconnect(DisconnectReason.leave, () ->
        {
            BrainOutClient.Env.gameCompleted();

            BrainOutClient.getInstance().popState();
            BrainOutClient.getInstance().initMainMenu().loadPackages();
        });
    }

    private void addButton(String title, Runnable action)
    {
        TextButton button = new TextButton(title,
                BrainOutClient.Skin, "button-default");

        button.addListener(new ClickOverListener()
        {
            @Override
            public void clicked(InputEvent event, float x, float y)
            {
                Menu.playSound(MenuSound.select);

                action.run();
            }
        });

        data.add(button).size(192, 48).pad(4).row();
    }

    @Override
    public void render()
    {
        BrainOutClient.drawFade(Constants.Menu.MENU_BACKGROUND_FADE_DOUBLE, getBatch());

        super.render();
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
    public boolean lockUpdate()
    {
        return true;
    }
}
