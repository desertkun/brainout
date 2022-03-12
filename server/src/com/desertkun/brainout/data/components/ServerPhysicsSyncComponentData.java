package com.desertkun.brainout.data.components;

import com.badlogic.gdx.math.Vector2;
import com.desertkun.brainout.BrainOutServer;
import com.desertkun.brainout.Constants;
import com.desertkun.brainout.content.components.ServerPhysicsSyncComponent;
import com.desertkun.brainout.data.active.ActiveData;
import com.desertkun.brainout.data.components.base.Component;
import com.desertkun.brainout.data.components.base.ComponentObject;
import com.desertkun.brainout.events.Event;
import com.desertkun.brainout.reflection.Reflect;
import com.desertkun.brainout.reflection.ReflectAlias;

@Reflect("ServerPhysicsSyncComponent")
@ReflectAlias("data.components.ServerPhysicsSyncComponentData")
public class ServerPhysicsSyncComponentData extends Component<ServerPhysicsSyncComponent>
{
    private Vector2 correctedPosition;
    private Vector2 correctPosition;
    private Vector2 targetPosition;
    private Vector2 correction;
    private boolean correctionEnabled;
    private float correctionDistance;

    private SimplePhysicsComponentData phy;
    private ServerPlayerControllerComponentData ctl;

    public ServerPhysicsSyncComponentData(ComponentObject componentObject,
                                          ServerPhysicsSyncComponent content)
    {
        super(componentObject, content);

        correctedPosition = new Vector2();
        correctPosition = new Vector2();
        correction = new Vector2();
        targetPosition = new Vector2();

        correctionDistance = Constants.Physics.CORRECTION_DISTANCE;
    }

    @Override
    public void init()
    {
        super.init();

        ctl = getComponentObject().getComponentWithSubclass(ServerPlayerControllerComponentData.class);
        phy = getComponentObject().getComponentWithSubclass(SimplePhysicsComponentData.class);

        if (BrainOutServer.Controller.isLobby())
        {
            correctionDistance = Constants.Physics.CORRECTION_DISTANCE * 2.0f;
        }
    }

    @Override
    public boolean hasRender()
    {
        return false;
    }

    @Override
    public boolean hasUpdate()
    {
        return true;
    }

    @Override
    public boolean onEvent(Event event)
    {
        return false;
    }

    private void correctPosition(float dt)
    {
        if (phy == null || !correctionEnabled)
            return;

        if (correctPosition.isZero(0.01f))
        {
            correctionEnabled = false;
            return;
        }

        correction.set(0, 0);

        if (correctedPosition.len() < correctPosition.len())
        {
            correction.set(correctPosition.x - correctedPosition.x, correctPosition.y - correctedPosition.y);
            correction.scl(16.0f);
            correctedPosition.add(correction.x * dt, correction.y * dt);

            Vector2 maxSpeed = ctl.getMaximumSpeed();

            float diffX = Math.abs(phy.getSpeed().x);
            float diffY = Math.abs(phy.getSpeed().y);

            if (diffX >= maxSpeed.x * 1.5f && (ctl.hasBottomContact() || phy.hasFixture()))
            {
                correction.x = 0;
            }

            if (diffY >= maxSpeed.y * 2f)
            {
                correction.y = 0;
            }

            if (!correction.isZero())
            {
                phy.applyForce(correction);
            }
        }
        else
        {
            correctionEnabled = false;
        }
    }

    @Override
    public void update(float dt)
    {
        correctPosition(dt);
    }

    public void syncTo(Vector2 position)
    {
        ActiveData activeData = ((ActiveData) getComponentObject());

        this.correctionEnabled = true;
        correctedPosition.set(0, 0);
        correctPosition.set(position).sub(activeData.getX(), activeData.getY());
        targetPosition.set(position);
    }

    public boolean sync(float x, float y, float angle)
    {
        if (phy == null || ctl == null)
            return true;

        ActiveData activeData = ((ActiveData) getComponentObject());

        if (activeData == null)
            return false;

        if (Vector2.dst(x, y, activeData.getX(), activeData.getY()) >= correctionDistance)
        {
            return false;
        }

        activeData.setAngle(angle);

        this.correctionEnabled = true;
        correctedPosition.set(0, 0);
        correctPosition.set(x, y).sub(activeData.getX(), activeData.getY());
        targetPosition.set(x, y);

        return true;
    }

    public Vector2 getTargetPosition()
    {
        return targetPosition;
    }
}
