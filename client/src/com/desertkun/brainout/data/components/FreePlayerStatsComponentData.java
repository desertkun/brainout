package com.desertkun.brainout.data.components;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.Align;
import com.desertkun.brainout.BrainOutClient;
import com.desertkun.brainout.ClientConstants;
import com.desertkun.brainout.client.ClientController;
import com.desertkun.brainout.client.RemoteClient;
import com.desertkun.brainout.client.states.CSGame;
import com.desertkun.brainout.content.active.Player;
import com.desertkun.brainout.content.components.FreePlayerStatsComponent;
import com.desertkun.brainout.content.quest.DailyQuest;
import com.desertkun.brainout.data.active.FreePlayPlayerData;
import com.desertkun.brainout.data.active.PlayerData;
import com.desertkun.brainout.data.interfaces.Animable;
import com.desertkun.brainout.data.interfaces.RenderContext;
import com.desertkun.brainout.events.Event;
import com.desertkun.brainout.events.GameControllerEvent;
import com.desertkun.brainout.events.NewRemoteClientEvent;

import com.desertkun.brainout.events.RemoteClientUpdatedEvent;
import com.desertkun.brainout.menu.ui.ActiveProgressBar;
import com.desertkun.brainout.mode.ClientRealization;
import com.desertkun.brainout.mode.GameMode;
import com.desertkun.brainout.reflection.Reflect;
import com.desertkun.brainout.reflection.ReflectAlias;

@Reflect("FreePlayerStatsComponent")
@ReflectAlias("data.components.FreePlayerStatsComponentData")
public class FreePlayerStatsComponentData extends ActiveStatsComponentData<FreePlayerStatsComponent>
{
    private PlayerData playerData;
    private RemoteClient remoteClient;
    private Label nickName;
    private ActiveProgressBar progressBar;
    private boolean progress;

    private StatIcon teamIcon;
    private StatIcon voiceIcon;

    private ActiveProgressVisualComponentData visualProgress;

    public FreePlayerStatsComponentData(FreePlayPlayerData playerData, FreePlayerStatsComponent contentComponent)
    {
        super(playerData, contentComponent);
    }

    @Override
    public void init()
    {
        this.playerData = ((PlayerData) getComponentObject());
        this.remoteClient = BrainOutClient.ClientController.getRemoteClients().get(playerData.getOwnerId());
        this.visualProgress = playerData.getComponent(ActiveProgressVisualComponentData.class);

        if (visualProgress != null)
        {
            progressBar = new ActiveProgressBar(new Animable()
            {
                @Override
                public float getX()
                {
                    return playerData.getX();
                }

                @Override
                public float getY()
                {
                    return playerData.getY();
                }

                @Override
                public float getAngle()
                {
                    return 0;
                }

                @Override
                public boolean getFlipX()
                {
                    return false;
                }
            });

            progressBar.setBackgroundColor(Color.BLACK);
            progressBar.setForegroundColor(Color.YELLOW);
        }

        super.init();

        BrainOutClient.EventMgr.subscribe(Event.ID.newRemoteClient, this);
        BrainOutClient.EventMgr.subscribe(Event.ID.remoteClientUpdated, this);
        BrainOutClient.EventMgr.subscribe(Event.ID.gameController, this);
    }

    @Override
    public void release()
    {
        super.release();

        BrainOutClient.EventMgr.unsubscribe(Event.ID.newRemoteClient, this);
        BrainOutClient.EventMgr.unsubscribe(Event.ID.remoteClientUpdated, this);
        BrainOutClient.EventMgr.unsubscribe(Event.ID.gameController, this);
    }

    @Override
    public boolean onEvent(Event event)
    {
        switch (event.getID())
        {
            case newRemoteClient:
            {
                NewRemoteClientEvent e = ((NewRemoteClientEvent) event);

                newRemoteClient(e.remoteClient);

                break;
            }

            case remoteClientUpdated:
            {
                RemoteClientUpdatedEvent e = ((RemoteClientUpdatedEvent) event);

                if (e.remoteClient != null && e.remoteClient.getId() == playerData.getOwnerId())
                {
                    updateTeam();
                }

                break;
            }

            case voice:
            {
                voice();

                break;
            }

            case componentUpdated:
            {
                componentUpdated();

                break;
            }

            case gameController:
            {

                GameControllerEvent gcEvent = ((GameControllerEvent) event);

                switch (gcEvent.action)
                {
                    case hideInterface:
                    {
                        if (nickName != null)
                            nickName.setVisible(!gcEvent.flag);

                        if (teamIcon != null)
                            teamIcon.setVisible(!gcEvent.flag);

                        if (voiceIcon != null)
                            voiceIcon.setVisible(!gcEvent.flag);
                    }
                }
                break;
            }
        }

        return super.onEvent(event);
    }

    private void voice()
    {
        if (voiceIcon == null)
        {
            voiceIcon = addIcon("icon-voice-chat");
        }
        else
        {
            voiceIcon.clearActions();
        }

        if (voiceIcon == null)
            return;

        voiceIcon.addAction(Actions.sequence(
            Actions.delay(0.5f),
            Actions.run(() -> voiceIcon = null),
            Actions.removeActor()
        ));
    }

    private void componentUpdated()
    {
        if (visualProgress != null)
        {
            if (visualProgress.isActive() != progress)
            {
                progress = visualProgress.isActive();
                progressBar.setForegroundColor(visualProgress.isCancellable() ? Color.GREEN : Color.YELLOW);
            }
        }
    }

    private void newRemoteClient(RemoteClient remoteClient)
    {
        if (playerData == null)
            return;

        if (this.remoteClient == null && remoteClient != null && remoteClient.getId() == playerData.getOwnerId())
        {
            this.remoteClient = remoteClient;
            initStats();
        }
    }

    @SuppressWarnings("ConstantConditions")
    @Override
    protected void initUI(Table ui, float width, float height)
    {
        super.initUI(ui, width, height);

        if (remoteClient != null)
        {
            initStats();
        }
    }

    @Override
    public void render(Batch batch, RenderContext context)
    {
        if (playerData == null)
            return;

        PlayerComponentData pc = playerData.getComponent(PlayerComponentData.class);

        GameMode gameMode = BrainOutClient.ClientController.getGameMode();
        if (gameMode != null)
        {
            if (pc != null && pc.getState() == Player.State.sit)
            {
                if (gameMode.isEnemies(BrainOutClient.ClientController.getMyId(), playerData.getOwnerId()))
                {
                    return;
                }
            }

            ClientRealization clientRealization = ((ClientRealization) gameMode.getRealization());

            if (!clientRealization.doDisplayPlayerBadge(playerData))
                return;
        }

        super.render(batch, context);

        if (progress)
        {
            long startTime = visualProgress.getStartTime();
            long endTime = visualProgress.getEndTime();

            float total = (float)(endTime - startTime) / 1000.0f;

            if (total != 0)
            {
                float passed = (float) (System.currentTimeMillis() - startTime) / 1000.0f;
                float v = MathUtils.clamp(passed / total, 0.0f, 1.0f);

                progressBar.setValue(Interpolation.pow2.apply(v));
                progressBar.render(batch, context);
            }
        }
    }

    protected void initStats()
    {
        super.initStats();

        CSGame CC = BrainOutClient.ClientController.getState(CSGame.class);

        if (CC != null && playerData != CC.getPlayerData())
        {
            String name = remoteClient.getName();

            if (name.length() > 20)
            {
                name = name.substring(0, 20) + "...";
            }

            this.nickName = new Label(name, BrainOutClient.Skin, "title-ingame-nickname");
            nickName.setEllipsis(true);
            nickName.setWrap(false);

            getUi().add(nickName).height(1.5f).expandX().fillX().row();
            nickName.setFontScale(1.0f / ClientConstants.Graphics.RES_SIZE);
            nickName.setAlignment(Align.center);

            updateTeam();
        }

        updateTeamIcon();
    }

    private void updateTeamIcon()
    {
        RemoteClient o = BrainOutClient.ClientController.getRemoteClients().get(playerData.getOwnerId());

        if (isKarmaQuestTarget())
        {
            setTopIcon("fp-player-quest-target");
        }
        else if (o != null && o.getInfoBoolean("friendly", false))
        {
            setTopIcon("fp-player-icon-friendly");
        }
        else
        {
            if (teamIcon != null)
            {
                removeIcon(teamIcon);
                teamIcon = null;
            }
        }

    }

    private boolean isKarmaQuestTarget()
    {
        RemoteClient o = BrainOutClient.ClientController.getRemoteClients().get(playerData.getOwnerId());
        RemoteClient me = BrainOutClient.ClientController.getMyRemoteClient();

        if (o == null || me == null)
            return false;

        if (o.getInfoBoolean("bot", false))
            return false;

        DailyQuest c = BrainOutClient.ContentMgr.get("quest-daily-kill", DailyQuest.class);
        if (c == null)
            return false;

        if (c.isComplete(BrainOutClient.ClientController.getUserProfile(), BrainOutClient.ClientController.getMyAccount()))
        {
            return false;
        }

        int karma = o.getInfoInt("karma", 0);
        int myKarma = me.getInfoInt("karma", 0);

        if (karma > 1 && myKarma < -1)
        {
            return true;
        }

        return karma < -1 && myKarma > 1;
    }

    private void setTopIcon(String icon)
    {
        if (teamIcon == null)
        {
            teamIcon = addIcon(icon);
        }
        else
        {
            teamIcon.setDrawable(BrainOutClient.Skin, icon);
        }
    }

    @Override
    protected void updateTeam()
    {
        super.updateTeam();

        ClientController CC = BrainOutClient.ClientController;

        if (nickName != null)
        {
            nickName.setColor(CC.getColorOf(playerData));
        }

        updateTeamIcon();
    }

    @Override
    public boolean hasUpdate()
    {
        return true;
    }

    @Override
    public boolean hasRender()
    {
        return true;
    }
}
