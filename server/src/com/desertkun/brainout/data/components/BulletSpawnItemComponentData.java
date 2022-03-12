package com.desertkun.brainout.data.components;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.desertkun.brainout.content.components.BulletSpawnItemComponent;
import com.desertkun.brainout.content.consumable.impl.InstrumentConsumableItem;
import com.desertkun.brainout.data.Map;
import com.desertkun.brainout.data.ServerMap;
import com.desertkun.brainout.data.bullet.BulletData;
import com.desertkun.brainout.data.components.base.Component;
import com.desertkun.brainout.data.components.base.ComponentObject;
import com.desertkun.brainout.data.consumable.ConsumableRecord;
import com.desertkun.brainout.data.containers.ChunkData;
import com.desertkun.brainout.data.instrument.ChipData;
import com.desertkun.brainout.data.interfaces.LaunchData;
import com.desertkun.brainout.events.Event;
import com.desertkun.brainout.events.LaunchEffectEvent;

public class BulletSpawnItemComponentData extends Component<BulletSpawnItemComponent>
{
    private boolean triggered;

    public BulletSpawnItemComponentData(ComponentObject componentObject,
                                        BulletSpawnItemComponent contentComponent)
    {
        super(componentObject, contentComponent);

        triggered = false;
    }

    @Override
    public boolean hasRender()
    {
        return false;
    }

    @Override
    public boolean hasUpdate()
    {
        return false;
    }

    @Override
    public void release()
    {
        super.release();

        triggered = false;
    }

    @Override
    public boolean onEvent(Event event)
    {
        switch (event.getID())
        {
            case launchEffect:
            {
                LaunchEffectEvent launchEffectEvent = (LaunchEffectEvent) event;

                switch (launchEffectEvent.kind)
                {
                    case hit:
                    {
                        hit(launchEffectEvent.launchData);

                        break;
                    }
                }
            }
        }

        return false;
    }

    private void hit(LaunchData launchData)
    {
        if (triggered)
            return;

        BulletData bulletData = ((BulletData) getComponentObject());
        Map map = getMap();

        if (bulletData == null || map == null)
            return;

        float x = launchData.getX() + MathUtils.cosDeg(launchData.getAngle()) * 0.5f,
              y = launchData.getY() + MathUtils.sinDeg(launchData.getAngle()) * 0.5f;

        ChunkData chunk = map.getChunkAt((int)x, (int)y);
        if (chunk == null || !chunk.hasFlag(ChunkData.ChunkFlag.hideOthers))
        {
            Array<ConsumableRecord> records = new Array<>();

            records.add(new ConsumableRecord(bulletData.getBullet().acquireConsumableItem(), 1, 0));

            ServerMap.dropItem(map.getDimension(),
                    getContentComponent().getItem(), records, -1, x, y, 0, 0);
        }

        triggered = true;
    }
}
