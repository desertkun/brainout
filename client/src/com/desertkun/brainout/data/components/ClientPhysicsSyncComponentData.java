package com.desertkun.brainout.data.components;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.desertkun.brainout.Constants;
import com.desertkun.brainout.components.my.MyPlayerComponent;
import com.desertkun.brainout.content.components.ClientPhysicsSyncComponent;
import com.desertkun.brainout.data.active.ActiveData;
import com.desertkun.brainout.data.components.base.Component;
import com.desertkun.brainout.data.components.base.ComponentObject;
import com.desertkun.brainout.events.Event;

public class ClientPhysicsSyncComponentData extends Component<ClientPhysicsSyncComponent>
{
    private Vector2 correctedPosition;
    private Vector2 correctPosition;
    private Vector2 speed;
    private Vector2 targetPosition;
    private Vector2 correction;
    private boolean correctionEnabled;

    private SimplePhysicsComponentData phy;
    private float targetAngle;
    private float targetAngleCorrection;

    public ClientPhysicsSyncComponentData(ComponentObject componentObject,
                                          ClientPhysicsSyncComponent content)
    {
        super(componentObject, content);

        correctedPosition = new Vector2();
        correctPosition = new Vector2();
        speed = new Vector2();
        correction = new Vector2();
        targetPosition = new Vector2();
    }

    @Override
    public void init()
    {
        super.init();

        phy = getComponentObject().getComponentWithSubclass(SimplePhysicsComponentData.class);
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

    private boolean isMe()
    {
        return getComponentObject().getComponent(MyPlayerComponent.class) != null;
    }

    @Override
    public void update(float dt)
    {
        if (phy == null)
            return;

        float force = isMe() ? 4.0f : 16.0f;

        if (targetAngleCorrection > 0)
        {
            targetAngleCorrection -= dt;

            ActiveData activeData = ((ActiveData) getComponentObject());

            if (activeData != null)
            {
                activeData.setAngle(MathUtils.lerpAngleDeg(activeData.getAngle(), targetAngle, dt * 10.0f));
            }
        }

        if (!correctionEnabled)
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
            correction.scl(force);
            correctedPosition.add(correction.x * dt, correction.y * dt);

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

    public void sync(float x, float y)
    {
        if (phy == null)
            return;

        ActiveData activeData = ((ActiveData) getComponentObject());

        if (Vector2.dst(x, y, activeData.getX(), activeData.getY()) >= Constants.Physics.CORRECTION_DISTANCE)
        {
            activeData.setPosition(x, y);
        }

        correctedPosition.set(0, 0);
        correctPosition.set(x, y).sub(activeData.getX(), activeData.getY());
    }

    public void forceSync(float x, float y, float angle, float speedX, float speedY)
    {
        if (phy == null)
            return;

        ActiveData activeData = ((ActiveData) getComponentObject());
        activeData.setPosition(x, y);
        activeData.setAngle(angle);

        phy.getSpeed().set(speedX, speedY);

        correctionEnabled = false;
        correctedPosition.set(0, 0);
        correctPosition.set(x, y).sub(activeData.getX(), activeData.getY());
    }

    public void sync(float x, float y, float angle, float speedX, float speedY)
    {
        if (phy == null)
            return;

        speed.set(speedX, speedY);
        phy.getSpeed().set(speedX, speedY);

        ActiveData activeData = ((ActiveData) getComponentObject());

        float dst2 = Vector2.dst2(x, y, activeData.getX(), activeData.getY());
        float repositionDistance = isMe() ? Constants.Physics.CORRECTION_DISTANCE : 3.0f;

        if (dst2 >= repositionDistance * repositionDistance)
        {
            activeData.setPosition(x, y);
        }

        if (isMe())
        {
            return;
        }

        targetAngle = angle;
        targetAngleCorrection = 0.5f;

        this.correctionEnabled = true;
        correctedPosition.set(0, 0);
        correctPosition.set(x, y).sub(activeData.getX(), activeData.getY());
        targetPosition.set(x, y);
    }
}
