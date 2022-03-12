package com.desertkun.brainout.data.components;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ObjectSet;
import com.badlogic.gdx.utils.Queue;
import com.desertkun.brainout.BrainOutServer;
import com.desertkun.brainout.Constants;
import com.desertkun.brainout.common.msg.server.LaunchEffectMsg;
import com.desertkun.brainout.components.PlayerOwnerComponent;
import com.desertkun.brainout.components.ServerBotWeaponComponent;
import com.desertkun.brainout.components.WeaponSlotComponent;
import com.desertkun.brainout.content.components.ZombieComponent;
import com.desertkun.brainout.content.consumable.impl.InstrumentConsumableItem;
import com.desertkun.brainout.data.Map;
import com.desertkun.brainout.data.ServerMap;
import com.desertkun.brainout.data.WayPointMap;
import com.desertkun.brainout.data.active.ActiveData;
import com.desertkun.brainout.data.active.PlayerData;
import com.desertkun.brainout.data.consumable.ConsumableRecord;
import com.desertkun.brainout.data.instrument.InstrumentData;
import com.desertkun.brainout.data.instrument.WeaponData;
import com.desertkun.brainout.data.interfaces.BonePointData;
import com.desertkun.brainout.events.Event;
import com.desertkun.brainout.reflection.Reflect;
import com.desertkun.brainout.reflection.ReflectAlias;

@Reflect("ZombieComponent")
@ReflectAlias("data.components.ZombieComponentData")
public class ZombieComponentData extends ServerPlayerControllerComponentData
{
    private final ZombieComponent zombieComponent;
    private Vector2 tmp;
    private ZombieState zombieState;
    private float followTimer;
    private ActiveData followTarget;
    private Runnable targetReached;
    private Queue<Vector2> followPath;
    private boolean followPathSearching;
    private ServerTeamVisibilityComponentData tm;
    private boolean firing;

    private abstract class ZombieState
    {
        public abstract void update(float dt);
    }

    private class AttackAPlayer extends ZombieState
    {
        private float timer, lostSightTimer;
        private float fireTimer, firePauseTimer;
        private ActiveData target;

        public AttackAPlayer(ActiveData target)
        {
            this(target, 0);
        }

        public AttackAPlayer(ActiveData target, float timer)
        {
            this.timer = timer;
            this.target = target;
            resetTimers();
            stopFollowing();
            openFire(false);
            setAim(true);
        }

        @Override
        public void update(float dt)
        {
            timer -= dt;
            if (timer < 0)
            {
                timer = 0.1f;
                dt = 0.1f;

                // player's dead
                if (!target.isAlive())
                {
                    openFire(false);
                    stopThenThink();
                    return;
                }

                // lost the player completely
                if (tm != null && !tm.isVisibleTo(target))
                {
                    openFire(false);
                    stopThenThink();
                    return;
                }

                // can see the player right now
                if (checkVisibility(target))
                {
                    this.lostSightTimer = 0;

                    if (isFollowing(target))
                    {
                        stopFollowing();
                    }

                    if (lerpAngle(target))
                    {
                        fireTimer -= dt;
                        if (fireTimer <= 0)
                        {
                            if (fireTimer < -firePauseTimer)
                            {
                                resetTimers();

                                openFire(true);
                            }
                            else
                            {
                                openFire(false);
                            }
                        }
                        else
                        {
                            openFire(true);
                        }
                    }
                    else
                    {
                        openFire(false);
                    }
                }
                else
                {
                    openFire(false);

                    if (!isFollowing(target))
                    {
                        follow(target, null);
                    }

                    lostSightTimer += dt;
                    if (lostSightTimer > 4.0f)
                    {
                        switchState(new WonderAround());
                    }
                }
            }
        }

        private void resetTimers()
        {
            this.fireTimer = zombieComponent.getFireTime().getValue();
            this.firePauseTimer = zombieComponent.getFirePauseTime().getValue();
        }
    }

    private boolean lerpAngle(ActiveData target)
    {
        tmp.set(target.getX(), target.getY()).
            sub(getPlayerData().getX(), getPlayerData().getY());

        float angleRequired = tmp.angleDeg();
        float angleHave = getPlayerData().getAngle();
        float result = MathUtils.lerpAngleDeg(angleHave, angleRequired, 0.35f);
        getPlayerData().setAngle(result);

        float diff = Math.abs(angleHave - angleRequired);
        return diff < 355.0f || diff >= 355.0f;
    }

    private boolean checkVisibility(ActiveData target)
    {
        Map map = getMap();

        InstrumentData currentWeapon = getPlayerData().getCurrentInstrument();
        if (currentWeapon == null)
            return false;

        WeaponAnimationComponentData anim = currentWeapon.getComponent(WeaponAnimationComponentData.class);
        if (anim == null)
            return false;

        SimplePhysicsComponentData phy = target.getComponentWithSubclass(SimplePhysicsComponentData.class);
        if (phy == null)
            return false;

        BonePointData launch = anim.getLaunchPointData();

        float halfHeight = phy.getHalfSize().y;

        tmp.set(launch.getX(), launch.getY());
        tmp.sub(target.getX(), target.getY() + halfHeight);

        if (!map.trace(target.getX(), target.getY() + halfHeight,
                Constants.Layers.BLOCK_LAYER_FOREGROUND, tmp.angleDeg(), tmp.len(), tmp))
        {
            return true;
        }

        tmp.set(launch.getX(), launch.getY());
        tmp.sub(target.getX(), target.getY() - halfHeight);

        if (!map.trace(target.getX(), target.getY() - halfHeight,
                Constants.Layers.BLOCK_LAYER_FOREGROUND, tmp.angleDeg(), tmp.len(), tmp))
        {
            return true;
        }

        return false;
    }

    private class CloseUpWithAPlayer extends ZombieState
    {
        private float timer;
        private ActiveData target;
        private boolean newTarget;

        public CloseUpWithAPlayer(ActiveData target, boolean newTarget)
        {
            this.newTarget = newTarget;
            this.target = target;
            openFire(false);
            setAim(false);
        }

        @Override
        public void update(float dt)
        {
            lerpAngle(target);
            openFire(false);

            timer -= dt;
            if (timer < 0)
            {
                timer = 0.25f;

                // player's dead
                if (!target.isAlive())
                {
                    stopFollowing();
                    stopThenThink();
                    return;
                }

                // lost the player completely
                if (tm != null && !tm.isVisibleTo(target))
                {
                    stopFollowing();
                    stopThenThink();
                    return;
                }

                // can see the player right now
                if (checkVisibility(target))
                {
                    // too far away
                    if (Vector2.dst(getPlayerData().getX(), getPlayerData().getY(),
                            target.getX(), target.getY()) > 32.0f)
                    {
                        if (!isFollowing(target))
                        {
                            follow(target, null);
                        }
                    }
                    else
                    {
                        if (isFollowing(target))
                        {
                            stopFollowing();
                        }

                        switchState(new AttackAPlayer(target,
                            newTarget ? zombieComponent.getAttackTime().getValue() : 0));
                    }
                }
                else
                {
                    if (!isFollowing(target))
                    {
                        follow(target, null);
                    }
                }
            }
        }
    }

    private class WonderAround extends ZombieState
    {
        private float timer;
        private float moanTimer;
        private ActiveData followTarget;

        public WonderAround()
        {
            this(0);
        }

        public WonderAround(float timer)
        {
            this.timer = timer;
            setAim(false);
        }

        @Override
        public void update(float dt)
        {
            moanTimer -= dt;
            if (moanTimer < 0)
            {
                moanTimer = zombieComponent.getMoanPeriod().getValue();
                moan();
            }

            timer -= dt;
            if (timer < 0)
            {
                timer = 0.25f;

                Array<ActiveData> a = getMap().getActivesForTag(Constants.ActiveTags.PLAYERS,
                    activeData ->
                {
                    if (!(activeData instanceof PlayerData))
                        return false;

                    if (activeData.getTeam() == getPlayerData().getTeam())
                        return false;

                    return checkVisibility(activeData);
                });

                if (a.size > 0)
                {
                    notice();
                    ActiveData target = a.random();
                    stopFollowing();
                    switchState(new CloseUpWithAPlayer(target, true));
                    return;
                }

                if (isFollowing(followTarget))
                    return;

                a = getMap().getActivesForTag(Constants.ActiveTags.SPAWNABLE,
                    activeData -> activeData.getTeam() != getPlayerData().getTeam());

                if (a.size > 0)
                {
                    followTarget = a.random();

                    follow(followTarget, () -> followTarget = null);
                }
            }
        }
    }

    public ZombieComponentData(PlayerData playerData, ZombieComponent contentComponent)
    {
        super(playerData);

        this.tmp = new Vector2();
        this.zombieComponent = contentComponent;
        this.switchState(new WonderAround());
    }

    public void switchState(ZombieState zombieState)
    {
        this.zombieState = zombieState;
    }

    @Override
    public boolean hasUpdate()
    {
        return true;
    }

    public Queue<Vector2> getFollowPath()
    {
        return followPath;
    }

    @Override
    public void update(float dt)
    {
        super.update(dt);

        zombieState.update(dt);

        updateFollow(dt);
    }

    private void stopThenThink()
    {
        switchState(new WonderAround(MathUtils.random(0.25f, 1.0f)));
    }

    private void openFire(boolean fire)
    {
        if (this.firing == fire)
            return;

        PlayerOwnerComponent poc = getPlayerData().getComponent(PlayerOwnerComponent.class);
        if (poc == null)
            return;

        ConsumableRecord record = poc.getCurrentInstrumentRecord();
        if (record == null)
            return;

        if (!(record.getItem() instanceof InstrumentConsumableItem))
            return;

        InstrumentData instrumentData = ((InstrumentConsumableItem) record.getItem()).getInstrumentData();

        if (!(instrumentData instanceof WeaponData))
            return;

        WeaponData weaponData = ((WeaponData) instrumentData);

        ServerBotWeaponComponent sw = instrumentData.getComponent(ServerBotWeaponComponent.class);
        if (sw == null)
        {
            sw = new ServerBotWeaponComponent(weaponData, record);
            sw.init();
            instrumentData.addComponent(sw);
        }

        WeaponSlotComponent slot = sw.getSlot(Constants.Properties.SLOT_PRIMARY);
        if (slot == null)
            return;

        this.firing = fire;
        slot.setLaunching(fire);
    }

    private void follow(ActiveData target, Runnable targetReached)
    {
        this.followTarget = target;
        this.targetReached = targetReached;
    }

    private boolean isFollowing(ActiveData target)
    {
        return followTarget != null && followTarget == target;
    }

    private void stopFollowing()
    {
        setMoveDirection(0, 0);
        followTimer = 0;
        targetReached = null;
        followTarget = null;
    }

    private void updateFollow(float dt)
    {
        if (followTarget == null)
            return;

        if (followPathSearching)
            return;

        if (Vector2.dst2(getPlayerData().getX(), getPlayerData().getY(), followTarget.getX(), followTarget.getY()) <
            2.0f * 2.0f)
        {
            if (targetReached != null)
            {
                targetReached.run();
            }

            stopFollowing();
            return;
        }

        followTimer -= dt;

        if (followTimer < 0 || (followPath != null && followPath.size > 0 &&
            followPath.last().dst(followTarget.getX(), followTarget.getY()) > 1.0f))
        {
            followTimer = 2.0f;

            followPathSearching = true;
            followTarget.getMap(ServerMap.class).getWayPointMap().findPath(
                getPlayerData().getX(), getPlayerData().getY(),
                followTarget.getX(), followTarget.getY(), 8.0f, null, new WayPointMap.PathSearchResult()
                {
                    @Override
                    public void found(
                        Queue<Vector2> path, String dimension,
                        ObjectSet<WayPointMap.BlockCoordinates> blocksInWay,
                        ActiveData portalOfInterest)
                    {
                        followPath = path;
                        followPathSearching = false;
                    }

                    @Override
                    public void notFound()
                    {
                        followPathSearching = false;
                    }
                }
            );
        }

        if (followPath != null)
        {
            while (followPath.size > 0 &&
                    Vector2.dst2(getPlayerData().getX(), getPlayerData().getY(),
                            followPath.first().x, followPath.first().y) < 2.0f * 2.0f)
            {
                followPath.removeFirst();
            }

            if (followPath.size > 0)
            {
                Vector2 goTo = followPath.first();

                tmp.set(goTo).sub(getPlayerData().getX(), getPlayerData().getY());

                getPlayerData().setAngle(tmp.angleDeg());

                tmp.set(goTo).sub(getPlayerData().getX(), getPlayerData().getY() - 1);

                float diffX = goTo.x - getPlayerData().getX(),
                      diffY = goTo.y - getPlayerData().getY() - 1;

                float moveDirectionX = Math.abs(diffX) > 0.5f ? Math.signum(diffX) : 0;
                float moveDirectionY = Math.abs(diffY) > 0.5f ? Math.signum(diffY) : 0;

                setMoveDirection(moveDirectionX, moveDirectionY);
            }
            else
            {
                setMoveDirection(0, 0);
            }
        }
        else
        {
            setMoveDirection(0, 0);
        }
    }

    @Override
    public void init()
    {
        super.init();

        tm = getComponentObject().getComponent(ServerTeamVisibilityComponentData.class);;
    }

    private void notice()
    {
        effect(zombieComponent.getNoticeEffect());
    }

    private void moan()
    {
        effect(zombieComponent.getMoanEffect());
    }

    private void effect(String name)
    {
        ActiveData me = ((ActiveData) getComponentObject());

        BrainOutServer.Controller.getClients().sendUDP(
            new LaunchEffectMsg(me.getDimension(), me.getX(), me.getY(), name));
    }

    @Override
    public boolean hasRender()
    {
        return false;
    }

    @Override
    public boolean onEvent(Event event)
    {
        return false;
    }
}
