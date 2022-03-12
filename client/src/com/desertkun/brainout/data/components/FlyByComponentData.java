package com.desertkun.brainout.data.components;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Pool;
import com.desertkun.brainout.BrainOut;
import com.desertkun.brainout.components.ClientPlayerComponent;
import com.desertkun.brainout.components.my.MyPlayerComponent;
import com.desertkun.brainout.content.components.FlyByComponent;
import com.desertkun.brainout.data.ClientMap;
import com.desertkun.brainout.data.Map;
import com.desertkun.brainout.data.active.PlayerData;
import com.desertkun.brainout.data.bullet.BulletData;
import com.desertkun.brainout.data.components.base.Component;
import com.desertkun.brainout.data.effect.EffectData;
import com.desertkun.brainout.data.interfaces.Watcher;
import com.desertkun.brainout.events.Event;

import com.desertkun.brainout.reflection.Reflect;
import com.desertkun.brainout.reflection.ReflectAlias;

@Reflect("FlyByComponent")
@ReflectAlias("data.components.FlyByComponentData")
public class FlyByComponentData extends Component<FlyByComponent> implements Pool.Poolable
{
    private final BulletData bulletData;
    private boolean done;
    private float prevDist;
    private Array<EffectData> effects;

    public FlyByComponentData(BulletData bulletData, FlyByComponent flyByComponent)
    {
        super(bulletData, flyByComponent);

        this.bulletData = bulletData;
        this.done = false;
        this.prevDist = 99999f;
        this.effects = new Array<>();
    }

    @Override
    public void update(float dt)
    {
        super.update(dt);

        if (done) return;

        ClientMap map = ((ClientMap) getMap());

        if (map == null)
            return;

        Watcher watcher = Map.GetWatcher();

        if (watcher != null && bulletData.getPlayerData() != null
            && watcher.getDimension().equals(bulletData.getDimension())
            && watcher != bulletData.getPlayerData().getComponent(ClientPlayerComponent.class))
        {
            float dist = Vector2.dst(bulletData.getX(), bulletData.getY(), watcher.getWatchX(), watcher.getWatchY());

            if (dist > getContentComponent().getDistance() &&
                prevDist <= getContentComponent().getDistance())
            {
                done = true;

                launchEffect();

                /*

                float slowdown = getContentComponent().getSlowdown();

                if (slowdown != 0)
                {
                    float slowdownTime = getContentComponent().getSlowdownTime();
                    PlayerData playerData = bulletData.getPlayerData();
                    MyPlayerComponent myPlayerComponent = playerData.getComponent(MyPlayerComponent.class);

                    if (myPlayerComponent != null)
                    {
                        myPlayerComponent.applySlowdown(slowdownTime, slowdown);
                    }
                }

                */
            }

            prevDist = dist;
        }
    }

    @Override
    public void release()
    {
        super.release();

        ClientMap map = ((ClientMap) getMap());

        if (map == null)
            return;

        map.removeEffect(effects);
    }

    private void launchEffect()
    {
        getContentComponent().getEffect().launchEffects(bulletData.getLaunchData(), effects);
    }

    @Override
    public boolean onEvent(Event event)
    {
        return false;
    }

    @Override
    public boolean hasUpdate()
    {
        return true;
    }

    @Override
    public boolean hasRender()
    {
        return false;
    }

    @Override
    public void reset()
    {
        done = false;
    }
}
