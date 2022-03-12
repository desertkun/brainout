package com.desertkun.brainout.data.components;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.desertkun.brainout.BrainOut;
import com.desertkun.brainout.BrainOutClient;
import com.desertkun.brainout.content.components.RagDollComponent;
import com.desertkun.brainout.data.ClientMap;
import com.desertkun.brainout.data.active.ActiveData;
import com.desertkun.brainout.data.active.RagDollData;
import com.desertkun.brainout.events.ActiveActionEvent;
import com.desertkun.brainout.data.components.base.Component;
import com.desertkun.brainout.events.DamagedEvent;
import com.desertkun.brainout.events.Event;
import com.esotericsoftware.spine.Bone;
import com.esotericsoftware.spine.Skeleton;

import com.desertkun.brainout.reflection.Reflect;
import com.desertkun.brainout.reflection.ReflectAlias;

@Reflect("RagDollComponent")
@ReflectAlias("data.components.RagDollComponentData")
public class RagDollComponentData extends Component<RagDollComponent>
{
    private final ActiveData activeData;
    private DamagedEvent lastDamage;
    private float hitTimer;

    public RagDollComponentData(ActiveData activeData, RagDollComponent ragDollComponent)
    {
        super(activeData, ragDollComponent);

        this.activeData = activeData;
        this.hitTimer = 0;
        this.lastDamage = new DamagedEvent();
    }

    @Override
    public void release()
    {
        super.release();

        BrainOutClient.EventMgr.unsubscribe(Event.ID.activeAction, this);
    }

    @Override
    public void init()
    {
        super.init();

        BrainOutClient.EventMgr.subscribe(Event.ID.activeAction, this);
    }

    private void onDestroy(boolean ragdoll)
    {
        if (!ragdoll)
            return;

        ClientMap map = ((ClientMap) getMap());

        if (map == null)
            return;

        Class componentClass = getContentComponent().getAnimationClass();

        AnimationComponentData animation = null;

        Component it = getComponentObject().getFistComponent();

        while (it != null)
        {
            if (it instanceof AnimationComponentData)
            {
                AnimationComponentData asAnimation = ((AnimationComponentData) it);
                if (it.getContentComponent().getClass() == componentClass)
                {
                    animation = asAnimation;
                }
            }

            it = it.getNext();
        }

        if (animation != null)
        {
            if (!activeData.isVisible())
            {
                activeData.setVisible(true);
                animation.update(0);
            }

            Skeleton skeleton = animation.getSkeleton();
            boolean flipX = skeleton.getFlipX();

            animation.detach();

            RagDollData rd = new RagDollData(animation,
                    getComponentObject().getComponentWithSubclass(SimplePhysicsComponentData.class),
                    getContentComponent().getTimeToLive(), flipX);

            if (hitTimer > 0)
            {
                rd.attachDamage(lastDamage);

                if (MathUtils.random(1.0f) < getContentComponent().getDetachProbability())
                {
                    Vector2 pos = new Vector2(lastDamage.x, lastDamage.y);
                    Bone closestBone = null;
                    float closestDist = 9999;

                    for (Bone bone : skeleton.getBones())
                    {
                        if (!(getContentComponent().getDetachBones().contains(bone.getData().getName(), false)))
                            continue;

                        float d = pos.dst2(bone.getWorldX(), bone.getWorldY());
                        if (d < closestDist)
                        {
                            closestDist = d;
                            closestBone = bone;
                        }
                    }

                    if (closestBone != null)
                    {
                        rd.detachBone(closestBone.getData().getName(), null);
                    }
                }
            }

            map.addActive(map.generateClientId(), rd, true);
        }
    }

    @Override
    public boolean hasRender()
    {
        return false;
    }

    @Override
    public void update(float dt)
    {
        super.update(dt);

        if (hitTimer > 0)
        {
            hitTimer -= dt;
        }
    }

    @Override
    public boolean hasUpdate()
    {
        return true;
    }

    @Override
    public boolean onEvent(Event event)
    {
        switch (event.getID())
        {
            case activeAction:
            {
                ActiveActionEvent activeEvent = ((ActiveActionEvent) event);

                if (activeEvent.activeData == getComponentObject())
                {
                    switch (activeEvent.action)
                    {
                        case removed:
                        {
                            onDestroy(activeEvent.flag);

                            break;
                        }
                    }
                }

                break;
            }

            case damaged:
            {
                DamagedEvent e = ((DamagedEvent) event);

                lastDamage.init(e.data, e.health, e.x, e.y, e.angle, e.content, e.damageKind);
                hitTimer = 0.25f;

                break;
            }
        }

        return false;
    }
}
