package com.desertkun.brainout.data.components;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.World;
import com.desertkun.brainout.BrainOut;
import com.desertkun.brainout.Constants;
import com.desertkun.brainout.content.components.PhysicsCollisionDetectorComponent;
import com.desertkun.brainout.data.Map;
import com.desertkun.brainout.data.active.ActiveData;
import com.desertkun.brainout.data.components.base.Component;
import com.desertkun.brainout.events.DetectedEvent;
import com.desertkun.brainout.events.Event;
import com.desertkun.brainout.reflection.Reflect;
import com.desertkun.brainout.reflection.ReflectAlias;

@Reflect("PhysicsCollisionDetectorComponent")
@ReflectAlias("data.components.PhysicsCollisionDetectorComponentData")
public class PhysicsCollisionDetectorComponentData extends Component<PhysicsCollisionDetectorComponent>
{
    private final ActiveData activeData;
    private boolean detected;

    private static Vector2 tmpEnd = new Vector2();
    private static Vector2 tmpStart = new Vector2();

    public PhysicsCollisionDetectorComponentData(
            ActiveData activeData,
            PhysicsCollisionDetectorComponent detectorComponent)
    {
        super(activeData, detectorComponent);

        this.activeData = activeData;
        this.detected = false;
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

    @Override
    public void update(float dt)
    {
        super.update(dt);

        if (detected)
            return;

        Map map = getMap();

        if (map == null)
            return;

        World world = map.getPhysicWorld();

        if (world == null)
            return;

        SimplePhysicsComponentData phy = activeData.getComponentWithSubclass(SimplePhysicsComponentData.class);

        if (phy == null)
            return;

        tmpStart.set(activeData.getX(), activeData.getY()).scl(Constants.Physics.SCALE);
        tmpEnd.set(phy.getSpeed().x, phy.getSpeed().y).nor().scl(
            getContentComponent().getDistance() * Constants.Physics.SCALE).add(tmpStart);

        if (tmpStart.equals(tmpEnd))
            return;

        world.rayCast(this::collision, tmpStart, tmpEnd);
    }

    /** @return -1 to filter, 0 to terminate, fraction to clip the ray for closest hit, 1 to continue **/
    private float collision(Fixture fixture, Vector2 point, Vector2 normal, float fraction)
    {
        Map map = getMap();

        if (map == null)
            return 0;

        point.scl(Constants.Physics.SCALE_OF);
        normal.scl(Constants.Physics.SCALE_OF);

        tmpEnd.set(normal).scl(-0.1f).add(point);

        Object userData = fixture.getUserData();

        // No user data means it's a static object (say a block)
        if (userData == null)
        {
            return 1;
        }

        detect();

        return 1;
    }

    private void detect()
    {
        if (detected)
            return;

        detected = true;

        BrainOut.EventMgr.sendDelayedEvent(activeData,
            DetectedEvent.obtain(
                "physics",
                activeData,
                DetectedEvent.EventKind.enter));
    }
}
