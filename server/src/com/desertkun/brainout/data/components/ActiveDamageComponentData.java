package com.desertkun.brainout.data.components;

import com.badlogic.gdx.math.Vector2;
import com.desertkun.brainout.BrainOut;
import com.desertkun.brainout.BrainOutServer;
import com.desertkun.brainout.Constants;
import com.desertkun.brainout.content.components.ActiveDamageComponent;
import com.desertkun.brainout.data.Map;
import com.desertkun.brainout.data.active.ActiveData;
import com.desertkun.brainout.data.components.base.Component;
import com.desertkun.brainout.data.instrument.InstrumentInfo;
import com.desertkun.brainout.events.DamageEvent;
import com.desertkun.brainout.events.Event;

import com.desertkun.brainout.reflection.Reflect;
import com.desertkun.brainout.reflection.ReflectAlias;

@Reflect("ActiveDamageComponent")
@ReflectAlias("data.components.ActiveDamageComponentData")
public class ActiveDamageComponentData extends Component<ActiveDamageComponent>
{
    private final ActiveData activeData;
    private float timer;
    private Vector2 tmp;

    public ActiveDamageComponentData(ActiveData activeData,
                                     ActiveDamageComponent activeDamageComponent)
    {
        super(activeData, activeDamageComponent);

        this.activeData = activeData;
        this.timer = 0;
        this.tmp = new Vector2();
    }

    @Override
    public void update(float dt)
    {
        super.update(dt);

        timer -= dt;

        if (timer <= 0)
        {
            timer = getContentComponent().getPeriod();

            damage();
        }
    }

    private void damage()
    {
        Map map = getMap();

        float posX1 = activeData.getX() + getContentComponent().getX();
        float posY1 = activeData.getY() + getContentComponent().getY();

        float posX2 = posX1 + getContentComponent().getWidth();
        float posY2 = posY1 + getContentComponent().getHeight();

        InstrumentInfo instrumentInfo;

        PassInstrumentInfoComponentData ii = activeData.getComponent(PassInstrumentInfoComponentData.class);

        if (ii != null)
        {
            instrumentInfo = ii.getInstrumentInfo();
        }
        else
        {
            instrumentInfo = null;
        }

        for (ActiveData activeData: map.getActivesForTag(Constants.ActiveTags.WITH_HEALTH, false))
        {
            if (activeData.getOwnerId() != this.activeData.getOwnerId() &&
                !BrainOutServer.Controller.isEnemies(activeData.getOwnerId(), this.activeData.getOwnerId()))
            {
                continue;
            }

            HealthComponentData pcd = activeData.getComponent(HealthComponentData.class);

            if (pcd != null)
            {
                float pX = activeData.getX(), pY = activeData.getY();

                if (pX >= posX1 && pY >= posY1 && pX <= posX2 && pY <= posY2)
                {
                    tmp.set(pX, pY);
                    tmp.sub(this.activeData.getX(), this.activeData.getY());

                    pcd.damage((DamageEvent)DamageEvent.obtain(getContentComponent().getDamage(),
                        this.activeData.getOwnerId(), instrumentInfo, null, pX, pY,
                            tmp.angleDeg(), getContentComponent().getKind()));

                    ActiveData.LastHitInfo lastHitInfo = activeData.getLastHitInfo();

                    if (lastHitInfo != null)
                    {
                        lastHitInfo.hitterId = this.activeData.getOwnerId();
                        lastHitInfo.instrument = instrumentInfo;
                        lastHitInfo.bullet = null;
                        lastHitInfo.silent = false;
                    }


                    pcd.updated(activeData);
                }
            }
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
        return true;
    }
}
