package com.desertkun.brainout.data.components;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.actions.RepeatAction;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.VerticalGroup;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Scaling;
import com.desertkun.brainout.BrainOutClient;
import com.desertkun.brainout.ClientConstants;
import com.desertkun.brainout.content.components.ActiveStatsComponent;
import com.desertkun.brainout.data.active.ActiveData;
import com.desertkun.brainout.data.components.base.Component;
import com.desertkun.brainout.data.interfaces.RenderContext;
import com.desertkun.brainout.events.Event;
import com.desertkun.brainout.events.SimpleEvent;

import com.desertkun.brainout.reflection.Reflect;
import com.desertkun.brainout.reflection.ReflectAlias;

@Reflect("ActiveStatsComponent")
@ReflectAlias("data.components.ActiveStatsComponentData")
public class ActiveStatsComponentData<T extends ActiveStatsComponent> extends Component<T>
{
    private final Table ui;
    private VerticalGroup icons;

    protected HealthComponentData health;
    protected final ActiveData activeData;

    public class StatIcon extends Image
    {
        public StatIcon(String textureRegion)
        {
            super(BrainOutClient.getRegion(textureRegion));

            setScaling(Scaling.none);
            setScale(1.0f / ClientConstants.Graphics.RES_SIZE);
            setSize(1.0f, 1.0f);
        }

        @Override
        public float getPrefWidth()
        {
            return 1.0f;
        }

        @Override
        public float getPrefHeight()
        {
            return 1.0f;
        }

        public void flash()
        {
            addAction(Actions.repeat(RepeatAction.FOREVER, Actions.sequence(
                Actions.visible(true),
                Actions.delay(0.25f),
                Actions.visible(false),
                Actions.delay(0.25f)
            )));
        }
    }

    public ActiveStatsComponentData(ActiveData activeData, T activeStatsComponent)
    {
        super(activeData, activeStatsComponent);

        this.activeData = activeData;
        this.ui = new Table(BrainOutClient.Skin);

        ui.setRound(false);
        ui.setSize(ClientConstants.Menu.PlayerStats.WIDTH,
                ClientConstants.Menu.PlayerStats.HEIGHT);
        ui.align(Align.bottom);
    }

    protected void initUI(Table ui, float width, float height)
    {
    }

    @Override
    public void init()
    {
        super.init();

        this.health = getComponentObject().getComponent(HealthComponentData.class);

        initUI(ui, ClientConstants.Menu.PlayerStats.WIDTH,
                ClientConstants.Menu.PlayerStats.HEIGHT);
        updateTeam();

        BrainOutClient.EventMgr.subscribe(Event.ID.simple, this);
    }

    @Override
    public void release()
    {
        super.release();

        BrainOutClient.EventMgr.unsubscribe(Event.ID.simple, this);
    }

    @Override
    public boolean onEvent(Event event)
    {
        switch (event.getID())
        {
            case simple:
            {
                if (((SimpleEvent) event).getAction() == null)
                    return false;

                switch (((SimpleEvent) event).getAction())
                {
                    case teamUpdated:
                    {
                        updateTeam();

                        break;
                    }
                }

                break;
            }
        }

        return false;
    }

    protected void updateTeam()
    {
    }

    @Override
    public void update(float dt)
    {
        super.update(dt);

        ui.act(dt);
    }

    @Override
    public void render(Batch batch, RenderContext context)
    {
        super.render(batch, context);

        ui.setPosition(
            activeData.getX() - ui.getWidth() / 2.0f,
            activeData.getY() + 3f);
        ui.draw(batch, 1);
    }

    public float getHealthValue()
    {
        return health.getHealth() / health.getInitHealth();
    }

    @Override
    public boolean hasRender()
    {
        return true;
    }

    @Override
    public boolean hasUpdate()
    {
        return true;
    }

    public Table getUi()
    {
        return ui;
    }

    protected void initStats()
    {
        this.icons = new VerticalGroup();
        icons.center();
        icons.setRound(false);
        icons.reverse(true);

        getUi().add(icons).width(4.0f).expandY().fillY().right().row(); //TODO: figure out why right alignment aligns to the center
    }

    public void removeIcon(StatIcon icon)
    {
        if (icons == null) return;
        icons.removeActor(icon);
    }

    public StatIcon addIcon(String textureRegion)
    {
        if (icons == null) return null;

        StatIcon icon = new StatIcon(textureRegion);
        icons.addActor(icon);
        return icon;
    }
}
