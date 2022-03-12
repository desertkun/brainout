package com.desertkun.brainout.data.components;

import com.badlogic.gdx.math.MathUtils;
import com.desertkun.brainout.BrainOutServer;
import com.desertkun.brainout.Constants;
import com.desertkun.brainout.content.components.ChemicalExposureComponent;
import com.desertkun.brainout.data.Map;
import com.desertkun.brainout.data.active.ActiveData;
import com.desertkun.brainout.data.active.PlayerData;
import com.desertkun.brainout.data.components.base.Component;
import com.desertkun.brainout.events.DamageEvent;
import com.desertkun.brainout.events.Event;
import com.desertkun.brainout.reflection.Reflect;
import com.desertkun.brainout.reflection.ReflectAlias;

@Reflect("ChemicalExposureComponent")
@ReflectAlias("data.components.ChemicalExposureComponentData")
public class ChemicalExposureComponentData extends Component<ChemicalExposureComponent>
{

    private final ActiveData activeData;
    private float update;

    public ChemicalExposureComponentData(ActiveData activeData,
                                         ChemicalExposureComponent contentComponent)
    {
        super(activeData, contentComponent);

        this.activeData = activeData;
    }

    @Override
    public void init()
    {
        update = getContentComponent().getPeriod();
    }

    private void doDamage()
    {
        BoundingBoxComponentData bb = activeData.getComponent(BoundingBoxComponentData.class);

        if (bb == null)
            return;

        Map map = getMap();

        if (map == null)
            return;

        for (ActiveData player : map.getActivesForTag(Constants.ActiveTags.PLAYERS, false))
        {
            PlayerAnimationComponentData anim = player.getComponent(PlayerAnimationComponentData.class);

            if (anim == null)
                continue;

            if (getContentComponent().getPlayerSkins().contains(anim.getSkin(), true))
                continue;

            if (bb.test(player.getX(), player.getY()))
            {
                damagePlayer(((PlayerData) player));
            }
        }
    }

    private void damagePlayer(PlayerData activeData)
    {
        BrainOutServer.EventMgr.sendEvent(activeData, DamageEvent.obtain(
            getContentComponent().getDamage(),
            -1, null, null, activeData.getX(), activeData.getY(),
            MathUtils.random(360), Constants.Damage.DAMAGE_HIT
        ));
    }

    @Override
    public void update(float dt)
    {
        super.update(dt);

        update -= dt;
        if (update < 0)
        {
            update = getContentComponent().getPeriod();
            doDamage();
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
}
