package com.desertkun.brainout.data.components;

import com.badlogic.gdx.utils.Array;
import com.desertkun.brainout.BrainOut;
import com.desertkun.brainout.content.components.BulletEffectsComponent;
import com.desertkun.brainout.content.effect.EffectSet;
import com.desertkun.brainout.content.effect.EffectSetGroup;
import com.desertkun.brainout.data.ClientMap;
import com.desertkun.brainout.data.bullet.BulletData;
import com.desertkun.brainout.data.components.base.Component;
import com.desertkun.brainout.data.effect.EffectData;
import com.desertkun.brainout.events.Event;
import com.desertkun.brainout.events.LaunchAttachedEffectEvent;
import com.desertkun.brainout.events.LaunchEffectEvent;

import com.desertkun.brainout.reflection.Reflect;
import com.desertkun.brainout.reflection.ReflectAlias;

@Reflect("BulletEffectsComponent")
@ReflectAlias("data.components.BulletEffectsComponentData")
public class BulletEffectsComponentData extends Component<BulletEffectsComponent>
{
    private final BulletData bulletData;

    private Array<EffectData> flyEffects;

    public BulletEffectsComponentData(BulletData bulletData, BulletEffectsComponent bulletEffectsComponent)
    {
        super(bulletData, bulletEffectsComponent);

        this.bulletData = bulletData;
        this.flyEffects = new Array<>();
    }

    @Override
    public boolean onEvent(Event event)
    {
        switch (event.getID())
        {
            case launchAttachedEffect:
            {
                LaunchAttachedEffectEvent e = (LaunchAttachedEffectEvent)event;

                EffectSet effectSet = getContentComponent().getEffects().get(e.kind);

                if (effectSet != null)
                {
                    effectSet.launchEffects(e.effectAttacher);
                }

                break;
            }

            case launchEffect:
            {
                LaunchEffectEvent launchEffectEvent = (LaunchEffectEvent)event;
                if (launchEffectEvent.launchData == null)
                    break;

                EffectSetGroup effects = getContentComponent().getEffects();

                switch (launchEffectEvent.kind)
                {
                    case hit:
                    {
                        effects.launchEffects("hit", launchEffectEvent.launchData);

                        break;
                    }
                    case custom:
                    {
                        effects.launchEffects(launchEffectEvent.custom, launchEffectEvent.launchData);

                        break;
                    }
                }

                break;
            }
        }

        return false;
    }

    @Override
    public void init()
    {
        super.init();

        EffectSetGroup effects = getContentComponent().getEffects();
        EffectSet fly = effects.get("fly");

        if (fly != null)
        {
            fly.launchEffects(bulletData.getLaunchData(), flyEffects);
        }
    }

    @Override
    public void release()
    {
        super.release();

        ClientMap map = ((ClientMap) getMap());

        if (map == null)
            return;

        map.removeEffect(flyEffects);
    }

    @Override
    public boolean hasUpdate()
    {
        return false;
    }

    @Override
    public boolean hasRender()
    {
        return false;
    }
}
