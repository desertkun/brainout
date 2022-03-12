package com.desertkun.brainout.data.components;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.ObjectMap;
import com.desertkun.brainout.BrainOut;
import com.desertkun.brainout.Constants;
import com.desertkun.brainout.content.active.Player;
import com.desertkun.brainout.content.components.ColliderComponent;
import com.desertkun.brainout.data.active.ActiveData;
import com.desertkun.brainout.data.bullet.BulletData;
import com.desertkun.brainout.data.components.base.Component;
import com.desertkun.brainout.data.interfaces.WithTag;
import com.desertkun.brainout.events.CollideEvent;
import com.desertkun.brainout.events.Event;

import com.desertkun.brainout.reflection.Reflect;
import com.desertkun.brainout.reflection.ReflectAlias;

@Reflect("ColliderComponent")
@ReflectAlias("data.components.ColliderComponentData")
public class ColliderComponentData extends Component<ColliderComponent> implements WithTag
{
    public class Collider
    {
        public float x1;
        public float y1;
        public float x2;
        public float y2;

        public ColliderComponent.Collider content;

        public Collider(float x, float y, float width, float height)
        {
            this.x1 = x;
            this.y1 = y;
            this.x2 = x + width;
            this.y2 = y + height;
        }

        public Collider(ColliderComponent.Collider content)
        {
            this(content.x, content.y, content.width, content.height);

            this.content = content;
        }

        public void update(Player.PlayerState.ColliderState colliderState)
        {
            this.x1 = colliderState.x1;
            this.y1 = colliderState.y1;
            this.x2 = colliderState.x2;
            this.y2 = colliderState.y2;
        }
    }

    private final ActiveData activeData;
    private ObjectMap<String, Collider> colliders;
    private Vector2 position;

    public ColliderComponentData(ActiveData activeData, ColliderComponent colliderComponent)
    {
        super(activeData, colliderComponent);

        this.activeData = activeData;
        this.position = new Vector2();
        this.colliders = new ObjectMap<>();

        for (ObjectMap.Entry<String, ColliderComponent.Collider> entry : colliderComponent.getColliders())
        {
            if (entry.value == null)
                continue;

            colliders.put(entry.key, new Collider(entry.value));
        }
    }

    @Override
    public void init()
    {
        super.init();

        update(0);
    }

    @Override
    public void update(float dt)
    {
        super.update(dt);

        SimplePhysicsComponentData phy = activeData.getComponentWithSubclass(SimplePhysicsComponentData.class);

        if (phy != null)
        {
            this.position.set(activeData.getX(), activeData.getY());
        }
    }

    @Override
    public boolean onEvent(Event event)
    {
        return false;
    }

    @Override
    public boolean hasRender()
    {
        return false;
    }

    @Override
    public boolean hasUpdate()
    {
        return activeData.getComponentWithSubclass(SimplePhysicsComponentData.class) != null;
    }

    public boolean isCollide(float x, float y, Collider collider)
    {
        return (x >= position.x + collider.x1) && (y >= position.y + collider.y1) &&
            (x < position.x + collider.x2) && (y < position.y + collider.y2);
    }

    public void collide(float x, float y, BulletData bulletData)
    {
        for (ObjectMap.Entry<String, Collider> entry : colliders)
        {
            if (bulletData.isCollided())
                return;
            
            Collider collider = entry.value;

            if (isCollide(x, y, collider))
            {
                BrainOut.EventMgr.sendEvent(getComponentObject(),
                        CollideEvent.obtain(bulletData, x, y, entry.value, entry.key));
            }
        }
    }

    public ActiveData getActiveData()
    {
        return activeData;
    }

    public Vector2 getPosition()
    {
        return position;
    }

    public ObjectMap<String, Collider> getColliders()
    {
        return colliders;
    }

    @Override
    public int getTags()
    {
        return WithTag.TAG(Constants.ActiveTags.COLLIDER);
    }
}
