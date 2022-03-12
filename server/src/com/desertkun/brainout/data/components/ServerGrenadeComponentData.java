package com.desertkun.brainout.data.components;

import com.badlogic.gdx.math.MathUtils;
import com.desertkun.brainout.BrainOutServer;
import com.desertkun.brainout.components.PlayerOwnerComponent;
import com.desertkun.brainout.content.active.ThrowableActive;
import com.desertkun.brainout.content.components.ServerGrenadeComponent;
import com.desertkun.brainout.data.Map;
import com.desertkun.brainout.data.active.ActiveData;
import com.desertkun.brainout.data.active.PlayerData;
import com.desertkun.brainout.data.active.ThrowableActiveData;
import com.desertkun.brainout.data.components.base.Component;
import com.desertkun.brainout.data.components.base.ComponentObject;
import com.desertkun.brainout.data.consumable.ConsumableRecord;
import com.desertkun.brainout.data.containers.ChunkData;
import com.desertkun.brainout.data.instrument.GrenadeData;
import com.desertkun.brainout.data.interfaces.BonePointData;
import com.desertkun.brainout.events.Event;

import com.desertkun.brainout.reflection.Reflect;
import com.desertkun.brainout.reflection.ReflectAlias;
import com.desertkun.brainout.utils.GrenadeUtils;

import java.util.TimerTask;

@Reflect("ServerGrenadeComponent")
@ReflectAlias("data.components.ServerGrenadeComponentData")
public class ServerGrenadeComponentData extends Component<ServerGrenadeComponent>
{
    public enum State
    {
        ready,
        cooked,
        cancelled
    }

    private State state;
    private float timer;

    public ServerGrenadeComponentData(ComponentObject componentObject,
                                      ServerGrenadeComponent throwableComponent)
    {
        super(componentObject, throwableComponent);

        this.state = State.ready;
        this.timer = 0;
    }

    public boolean isCooked()
    {
        return state == State.cooked;
    }

    public float getTimeLeft()
    {
        return timer;
    }

    public void cancel()
    {
        state = State.ready;
    }

    public boolean cook(float timer)
    {
        if (state == State.ready)
        {
            this.state = State.cooked;
            this.timer = timer;

            return true;
        }

        return false;
    }

    @Override
    public void update(float dt)
    {
        super.update(dt);

        switch (state)
        {
            case cooked:
            {
                timer -= dt;

                if (timer < 0)
                {
                    waste();
                }

                break;
            }
        }
    }

    private void waste()
    {
        final float t_ = timer;

        BrainOutServer.PostRunnable(() -> wasted(t_));
    }

    private void wasted(float t)
    {
        if (state != State.cooked)
            return;

        state = State.ready;

        Map map = getMap();

        if (map == null)
            return;

        GrenadeData grenadeData = ((GrenadeData) getComponentObject());
        ThrowableActive thr = grenadeData.getThrowActive();

        ActiveData owner = grenadeData.getOwner();
        if (owner == null)
            return;

        PlayerOwnerComponent poc = owner.getComponent(PlayerOwnerComponent.class);
        if (poc != null)
        {
            ConsumableRecord record = poc.findRecord(grenadeData);
            if (record != null)
            {
                poc.getConsumableContainer().getConsumable(1, record);

                ServerPlayerControllerComponentData pcc =
                    owner.getComponentWithSubclass(ServerPlayerControllerComponentData.class);

                pcc.consumablesUpdated();

                if (record.getAmount() == 0)
                {
                    BrainOutServer.Timer.schedule(new TimerTask()
                    {
                        @Override
                        public void run()
                        {
                            BrainOutServer.PostRunnable(() ->
                            {
                                pcc.selectFirstInstrument(poc);
                            });
                        }
                    }, 200);
                }
            }
        }

        float x, y;

        InstrumentAnimationComponentData imc = grenadeData.getComponentWithSubclass(InstrumentAnimationComponentData.class);

        if (imc != null)
        {
            BonePointData lp = imc.getLaunchPointData();
            x = lp.getX();
            y = lp.getY();
        }
        else
        {
            x = owner.getX();
            y = owner.getY();
        }

        ChunkData chunk = map.getChunkAt((int)x, (int)y);

        if (chunk != null && chunk.hasFlag(ChunkData.ChunkFlag.shootingDisabled))
            return;

        boolean syncToOthers = chunk == null || !chunk.hasFlag(ChunkData.ChunkFlag.hideOthers);

        ThrowableActiveData activeData = thr.getData(map.getDimension());
        activeData.setPosition(x, y);
        GrenadeUtils.getGrenadeOutOfWall(map, x, y, owner, activeData);
        activeData.setOwnerId(owner.getOwnerId());
        activeData.setLaunchedBy(grenadeData.getInfo());
        activeData.setTeam(activeData.getTeam());

        TimeToLiveComponentData ttld = activeData.getComponent(TimeToLiveComponentData.class);
        if (ttld != null)
        {
            ttld.setTime(t);
        }

        if (!syncToOthers)
        {
            // if the chunk has the hideOthers flag, sync the active data ONLY to the player who has
            // launched it

            ActiveFilterComponentData acf = new ActiveFilterComponentData(owner_ -> owner_ == owner.getOwnerId());
            activeData.addComponent(acf);
        }

        map.addActive(map.generateServerId(), activeData, true);
    }

    @Override
    public void release()
    {
        if (isCooked())
        {
            wasted(timer);
        }

        super.release();
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
