package com.desertkun.brainout.mode;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;
import com.desertkun.brainout.BrainOutClient;
import com.desertkun.brainout.Constants;
import com.desertkun.brainout.L;
import com.desertkun.brainout.client.states.CSDuel;
import com.desertkun.brainout.common.enums.DisconnectReason;
import com.desertkun.brainout.common.enums.data.DuelResultsND;
import com.desertkun.brainout.common.msg.server.DuelCompletedMsg;
import com.desertkun.brainout.data.Map;
import com.desertkun.brainout.data.active.ActiveData;
import com.desertkun.brainout.data.instrument.InstrumentInfo;
import com.desertkun.brainout.data.interfaces.Watcher;
import com.desertkun.brainout.events.Event;
import com.desertkun.brainout.gs.ActionPhaseState;
import com.desertkun.brainout.gs.EndGameState;
import com.desertkun.brainout.gs.actions.MenuAction;
import com.desertkun.brainout.menu.impl.*;
import com.desertkun.brainout.menu.ui.BorderActor;

public class ClientDuelRealization extends ClientGameRealization<GameModeDuel>
{
    private GameModeDuel.DuelState state;
    private Array<Integer> deaths;
    private int deathsRequired;
    private int enemy;
    private int round;

    public ClientDuelRealization(GameModeDuel gameMode)
    {
        super(gameMode);

        state = GameModeDuel.DuelState.waiting;
        deaths = new Array<>();
    }

    @Override
    public void read(Json json, JsonValue jsonData)
    {
        super.read(json, jsonData);

        if (jsonData.has("state"))
        {
            state = GameModeDuel.DuelState.valueOf(jsonData.getString("state"));
        }

        deaths.clear();

        if (jsonData.has("dl"))
        {
            for (JsonValue value : jsonData.get("dl"))
            {
                deaths.add(value.asInt());
            }
        }

        deathsRequired = jsonData.getInt("deaths", 3);
        enemy = jsonData.getInt("enemy", -1);
    }

    private int getRound()
    {
        return deaths.size + 1;
    }

    private int getMyDeaths()
    {
        int d = 0;

        for (Integer death : deaths)
        {
            if (death == BrainOutClient.ClientController.getMyId())
            {
                d++;
            }
        }

        return d;
    }

    private int getEnemyDeaths()
    {
        int d = 0;

        for (Integer death : deaths)
        {
            if (death == enemy)
            {
                d++;
            }
        }

        return d;
    }

    @Override
    protected void updateStats()
    {
        super.updateStats();

        if (getGameMode().isGameActive(false, false))
        {
            updateCurrentState();
        }
    }

    @Override
    protected void updated()
    {
        super.updated();

        updateStats();
    }

    private void updateCurrentState()
    {
        if (topStats != null)
        {
            switch (state)
            {
                case waiting:
                {
                    setWatcher();

                    if (!(BrainOutClient.getInstance().topState().topMenu() instanceof WaitForDuelistMenu))
                    {
                        BrainOutClient.getInstance().topState().pushMenu(new WaitForDuelistMenu());
                    }

                    break;
                }
                case steady:
                {
                    if (BrainOutClient.getInstance().topState().topMenu() instanceof WaitForDuelistMenu)
                    {
                        BrainOutClient.getInstance().topState().popMenu(WaitForDuelistMenu.class);
                    }

                    Table counter = new Table();

                    Image notice = new Image(BrainOutClient.getRegion("label-warmup"));
                    counter.add(notice).padBottom(-72).row();

                    Label title = new Label(L.get("MENU_STEADY"),
                            BrainOutClient.Skin, "title-small");

                    title.setAlignment(Align.left);
                    counter.add(title).row();

                    topStats.add(counter).padBottom(-106).row();

                    renderDeathsStats(false);

                    break;
                }
                case active:
                {
                    renderDeathsStats(true);

                    break;
                }

                case await:
                {
                    renderDeathsStats(false);

                    break;
                }
            }
        }
    }

    private void renderDeathsStats(boolean round)
    {
        if (enemy < 0)
            return;

        Table root = new Table();

        root.add(renderDeaths(getMyDeaths(), "assault-icon-friend"));
        if (round)
        {
            Table counter = new Table();
            Label roundTitle = new Label(L.get("MENU_ROUND",
                    String.valueOf(getRound())), BrainOutClient.Skin, "title-small");
            counter.add(roundTitle).row();

            root.add(counter).width(128).center();
        }
        else
        {
            root.add().width(128).center();
        }
        root.add(renderDeaths(getEnemyDeaths(), "assault-icon-enemy"));

        topStats.add(root).padTop(16).center().row();
    }

    private Table renderDeaths(int deaths, String style)
    {
        Table icons = new Table();

        int alive = deathsRequired - deaths;

        for (int i = 0; i < alive; i++)
        {
            icons.add(new BorderActor(new Image(BrainOutClient.Skin, style)));
        }
        for (int i = 0; i < deaths; i++)
        {
            icons.add(new BorderActor(new Image(BrainOutClient.Skin, "assault-icon-dead")));
        }

        return icons;
    }

    private void setWatcher()
    {
        for (Map map : Map.All())
        {
            for (ActiveData data : map.getActivesForTag(Constants.ActiveTags.SPAWNABLE, false))
            {
                Map.SetWatcher(new Watcher()
                {
                    @Override
                    public float getWatchX()
                    {
                        return data.getX();
                    }

                    @Override
                    public float getWatchY()
                    {
                        return data.getY();
                    }

                    @Override
                    public boolean allowZoom()
                    {
                        return false;
                    }

                    @Override
                    public float getScale()
                    {
                        return 1;
                    }

                    @Override
                    public String getDimension()
                    {
                        return data.getDimension();
                    }
                });

                return;
            }
        }
    }

    public boolean received(final DuelCompletedMsg msg)
    {
        Gdx.app.postRunnable(new Runnable()
        {
            @Override
            public void run()
            {
                ActionPhaseState ps = ((ActionPhaseState) BrainOutClient.getInstance().topState());

                ps.addAction(new MenuAction()
                {
                    @Override
                    public void run()
                    {
                        final Runnable doneF = this::done;

                        ps.pushMenu(new FadeInMenu(0.5f, () ->
                        {
                            ps.popAll();
                            ps.pushMenu(new DuelCompleteMenu(msg, getMyDeaths(), getEnemyDeaths(),
                                deathsRequired, () ->
                                BrainOutClient.ClientController.disconnect(DisconnectReason.leave, () ->
                                {
                                    doneF.run();
                                    BrainOutClient.Env.gameCompleted();
                                    BrainOutClient.getInstance().popState();
                                    BrainOutClient.getInstance().initMainMenu().loadPackages();
                                }), () -> {
                                    doneF.run();
                                    BrainOutClient.Env.gameCompleted();

                                    BrainOutClient.ClientController.setState(new CSDuel());
                                }));
                        }));
                    }
                });
            }
        });

        return true;
    }

    /*
    @Override
    public void onKilledBy(ActionPhaseState ps, Map map, ActiveData killer, InstrumentInfo info)
    {
        // nothing
    }
    */

    @Override
    public void update(float dt)
    {

    }

    @Override
    public boolean onEvent(Event event)
    {
        return false;
    }
}
