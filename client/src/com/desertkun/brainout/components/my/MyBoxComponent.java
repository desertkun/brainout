package com.desertkun.brainout.components.my;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.desertkun.brainout.BrainOut;
import com.desertkun.brainout.BrainOutClient;
import com.desertkun.brainout.Constants;
import com.desertkun.brainout.common.msg.client.PlaceBlockMsg;
import com.desertkun.brainout.components.ClientPlayerComponent;
import com.desertkun.brainout.components.PlayerOwnerComponent;
import com.desertkun.brainout.data.active.ActiveData;
import com.desertkun.brainout.data.components.SimplePhysicsComponentData;
import com.desertkun.brainout.data.components.PlaceAnimationComponentData;
import com.desertkun.brainout.data.components.PlayerAnimationComponentData;
import com.desertkun.brainout.data.components.base.Component;
import com.desertkun.brainout.data.consumable.ConsumableRecord;
import com.desertkun.brainout.data.instrument.BoxData;
import com.desertkun.brainout.data.interfaces.LaunchData;
import com.desertkun.brainout.data.interfaces.PointLaunchData;
import com.desertkun.brainout.data.interfaces.RenderContext;
import com.desertkun.brainout.events.Event;
import com.desertkun.brainout.events.GameControllerEvent;
import com.desertkun.brainout.events.LaunchEffectEvent;
import com.desertkun.brainout.graphics.CenterSprite;

public class MyBoxComponent extends Component
{
    private final BoxData boxData;
    private final CenterSprite placeSprite;
    private final PointLaunchData placeLaunchData;
    private final ConsumableRecord record;
    private State state;
    protected boolean launching;
    private float timer;

    private int placeX;
    private int placeY;

    public enum State
    {
        idle,
        done
    }

    public MyBoxComponent(BoxData boxData, ConsumableRecord record)
    {
        super(boxData, null);

        this.boxData = boxData;
        this.state = State.idle;
        this.launching = false;
        this.timer = 0;

        this.placeLaunchData = new PointLaunchData(0, 0, 0, boxData.getDimension());
        this.placeSprite = new CenterSprite(BrainOutClient.getRegion("place-cursor"), placeLaunchData);
        this.record = record;
    }

    @Override
    public void update(float dt)
    {
        switch (state)
        {
            case idle:
            {
                if (isLaunching())
                {
                    doLaunch();
                }

                break;
            }
            case done:
            {
                timer -= dt;
                if (timer <= 0)
                {
                    setState(State.idle);
                }

                break;
            }
        }

        MyPlayerComponent mpc = boxData.getOwner().getComponent(MyPlayerComponent.class);

        this.placeX = mpc.getPosX();
        this.placeY = mpc.getPosY();

        placeLaunchData.setX(placeX + 0.5f);
        placeLaunchData.setY(placeY + 0.5f);
    }

    private void doLaunch()
    {
        PlaceAnimationComponentData crc = getComponentObject().getComponent(PlaceAnimationComponentData.class);

        timer = boxData.getPlaceBlock().getPlaceTime();

        setState(State.done);

        if (!place(placeX, placeY)) return;

        BrainOut.EventMgr.sendDelayedEvent(getComponentObject(), LaunchEffectEvent.obtain(LaunchEffectEvent.Kind.shoot,
                crc.getLaunchPointData()));
    }

    @Override
    public void render(Batch batch, RenderContext context)
    {
        super.render(batch, context);

        placeSprite.draw(batch);
    }

    private LaunchData getPlayerData()
    {
        if (boxData.getOwner() != null)
        {
            return boxData.getOwner().getComponent(PlayerAnimationComponentData.class).getPrimaryLaunchData();
        }

        return null;
    }

    @Override
    public void init()
    {
        super.init();

        PlayerOwnerComponent poc = boxData.getOwner().getComponent(PlayerOwnerComponent.class);
    }

    public boolean place(int x, int y)
    {
        ActiveData playerData = boxData.getOwner();

        MyPlayerComponent mpc = playerData.getComponent(MyPlayerComponent.class);
        SimplePhysicsComponentData phy = playerData.getComponentWithSubclass(SimplePhysicsComponentData.class);
        ClientPlayerComponent cpc = playerData.getComponent(ClientPlayerComponent.class);

        BrainOutClient.ClientController.sendUDP(new PlaceBlockMsg(
                playerData.getX(),
                playerData.getY(),
                mpc.getOriginalDirection(),
                cpc.getMousePosition().x, cpc.getMousePosition().y,
                x, y, Constants.Layers.BLOCK_LAYER_FOREGROUND, null, record));

        return true;
    }

    @Override
    public boolean onEvent(Event event)
    {
        switch (event.getID())
        {
            case gameController:
            {
                GameControllerEvent gcEvent = ((GameControllerEvent) event);

                switch (gcEvent.action)
                {
                    case beginLaunch:
                    {
                        beginLaunching();

                        return true;
                    }

                    case endLaunch:
                    {
                        endLaunching();

                        return true;
                    }
                }

                return false;
            }
        }

        return false;
    }

    public void setState(State state)
    {
        this.state = state;
    }

    public boolean isLaunching()
    {
        return launching;
    }

    private void endLaunching()
    {
        launching = false;
    }

    private void beginLaunching()
    {
        launching = true;
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
