package com.desertkun.brainout.data.components;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.actions.FloatAction;
import com.badlogic.gdx.scenes.scene2d.ui.Container;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.VerticalGroup;
import com.badlogic.gdx.utils.Align;
import com.desertkun.brainout.BrainOutClient;
import com.desertkun.brainout.ClientConstants;
import com.desertkun.brainout.content.components.PlayerChatComponent;
import com.desertkun.brainout.data.active.ActiveData;
import com.desertkun.brainout.data.components.base.Component;
import com.desertkun.brainout.data.interfaces.RenderContext;
import com.desertkun.brainout.events.ChatEvent;
import com.desertkun.brainout.events.Event;
import com.desertkun.brainout.reflection.Reflect;
import com.desertkun.brainout.reflection.ReflectAlias;

@Reflect("PlayerChatComponent")
@ReflectAlias("data.components.PlayerChatComponentData")
public class PlayerChatComponentData extends Component<PlayerChatComponent>
{
    private final VerticalGroup ui;
    protected final ActiveData activeData;
    protected ShapeRenderer rectangleBatch;

    private static float SCALE = 1.0f / ClientConstants.Graphics.RES_SIZE;

    public PlayerChatComponentData(ActiveData activeData, PlayerChatComponent activeStatsComponent)
    {
        super(activeData, activeStatsComponent);

        this.activeData = activeData;
        this.ui = new VerticalGroup();

        ui.setSize(ClientConstants.Menu.PlayerChat.WIDTH, ClientConstants.Menu.PlayerChat.HEIGHT);
        ui.align(Align.bottom);
        ui.space(0f);
        ui.wrap(false);
        ui.setRound(false);
        ui.setTransform(true);
        ui.setScale(SCALE);
    }

    @Override
    public void init()
    {
        super.init();

        this.rectangleBatch = new ShapeRenderer();

        BrainOutClient.EventMgr.subscribe(Event.ID.chat, this);
    }

    @Override
    public void release()
    {
        super.release();

        BrainOutClient.EventMgr.unsubscribe(Event.ID.chat, this);
    }

    @Override
    public boolean onEvent(Event event)
    {
        switch (event.getID())
        {
            case chat:
            {
                ChatEvent ev = ((ChatEvent) event);

                if (ev.message == null)
                    return false;

                Label line = new Label(ev.message.text, BrainOutClient.Skin, "title-ingame-chat");
                line.setWrap(true);
                line.setFontScale(2.0f);
                line.setAlignment(Align.center);

                line.getColor().a = 0;

                Container<Label> labelContainer = new Container<>(line);
                labelContainer.prefWidth(ClientConstants.Menu.PlayerChat.WIDTH / SCALE);
                labelContainer.pack();
                labelContainer.setRound(false);

                float targetHeight = labelContainer.getPrefHeight();
                labelContainer.padTop(-targetHeight);

                FloatAction fl = new FloatAction(-targetHeight, 4.0f)
                {
                    @Override
                    protected void update(float percent)
                    {
                        super.update(percent);

                        labelContainer.padTop(getValue());
                        labelContainer.invalidateHierarchy();
                    }
                };

                fl.setDuration(0.25f);
                fl.setInterpolation(Interpolation.circleOut);
                labelContainer.addAction(fl);

                line.addAction(Actions.sequence(
                    Actions.delay(0.1f),
                    Actions.alpha(1.0f, 0.25f),
                    Actions.delay(5.0f),
                    Actions.alpha(0.0f, 0.25f),
                    Actions.run(() -> ui.removeActor(labelContainer))
                ));

                ui.addActor(labelContainer);

                return true;
            }
        }

        return false;
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
            activeData.getX() - ui.getWidth() / (2.0f / SCALE),
            activeData.getY() + 6f);
        ui.draw(batch, 1);
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

}
