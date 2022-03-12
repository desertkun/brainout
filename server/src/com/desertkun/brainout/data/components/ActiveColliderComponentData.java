package com.desertkun.brainout.data.components;

import com.badlogic.gdx.utils.Array;
import com.desertkun.brainout.BrainOutServer;
import com.desertkun.brainout.Constants;
import com.desertkun.brainout.client.Client;
import com.desertkun.brainout.client.PlayerClient;
import com.desertkun.brainout.common.msg.server.HitConfirmMsg;
import com.desertkun.brainout.content.Team;
import com.desertkun.brainout.content.components.ActiveColliderComponent;
import com.desertkun.brainout.data.Map;
import com.desertkun.brainout.data.active.ActiveData;
import com.desertkun.brainout.data.bullet.BulletData;
import com.desertkun.brainout.data.components.base.Component;
import com.desertkun.brainout.data.interfaces.LaunchData;
import com.desertkun.brainout.events.CollideEvent;
import com.desertkun.brainout.events.DamageEvent;
import com.desertkun.brainout.events.Event;
import com.desertkun.brainout.mode.GameMode;
import com.desertkun.brainout.mode.ServerRealization;
import com.desertkun.brainout.server.ServerSettings;

import com.desertkun.brainout.reflection.Reflect;
import com.desertkun.brainout.reflection.ReflectAlias;

import java.lang.ref.WeakReference;

@Reflect("ActiveColliderComponent")
@ReflectAlias("data.components.ActiveColliderComponentData")
public class ActiveColliderComponentData<T extends ActiveColliderComponent> extends Component<T>
{
    protected final ActiveData activeData;
    protected ColliderComponentData coll;
    protected HealthComponentData health;
    protected Array<BulletData> toRemove;

    public ActiveColliderComponentData(ActiveData activeData, T colliderComponent)
    {
        super(activeData, colliderComponent);

        this.activeData = activeData;
        this.toRemove = new Array<>();
    }

    @Override
    public void init()
    {
        super.init();

        this.coll = getComponentObject().getComponent(ColliderComponentData.class);
        this.health = getComponentObject().getComponent(HealthComponentData.class);
    }

    protected ActiveData.LastHitInfo damage(CollideEvent collide, BulletData bullet, float dmg)
    {
        if (health.isImmortal() || dmg <= 0)
        {
            return activeData.getLastHitInfo();
        }

        ActiveData activeData = ((ActiveData) getComponentObject());

        GameMode gameMode = BrainOutServer.Controller.getGameMode();

        if (gameMode != null)
        {
            ServerRealization serverRealization = ((ServerRealization) gameMode.getRealization());

            if (serverRealization != null)
            {
                dmg = serverRealization.calculateDamage(activeData.getTeam(), bullet.getTeam(), activeData.getOwnerId(), bullet.getOwnerId(), dmg);
            }
        }

        LaunchData l = bullet.getLaunchData();

        health.damage((DamageEvent)DamageEvent.obtain(dmg, bullet.getOwnerId(), bullet.getInstrumentInfo(),
            bullet, l.getX(), l.getY(), l.getAngle(), Constants.Damage.DAMAGE_HIT));

        Client client = BrainOutServer.Controller.getClients().getByActive(activeData);

        if (client != null)
        {
            client.setLastHitInfo(activeData.getLastHitInfo());
        }

        ActiveData.LastHitInfo lastHitInfo = activeData.getLastHitInfo();

        lastHitInfo.hitterId = bullet.getOwnerId();
        lastHitInfo.instrument = bullet.getInstrumentInfo();
        lastHitInfo.bullet = bullet.getBullet();
        lastHitInfo.silent = bullet.isSilent();

        Client ownerClient = BrainOutServer.Controller.getClients().get(bullet.getOwnerId());

        if (ownerClient instanceof PlayerClient)
        {
            ((PlayerClient) ownerClient).sendUDP(new HitConfirmMsg(collide.colliderName,
                activeData.getDimensionId(), activeData.getId(), bullet.getX(), bullet.getY(), (int)dmg));
        }

        return lastHitInfo;
    }

    @Override
    public boolean onEvent(Event event)
    {
        switch (event.getID())
        {
            case collide:
            {
                CollideEvent collideEvent = ((CollideEvent) event);

                if (activeData.isAlive())
                {
                    if (!collideEvent.bulletData.isCollided())
                    {
                        collide(collideEvent, collideEvent.bulletData.calculateDamage());
                    }
                }

                break;
            }
        }

        return false;
    }

    public boolean validateBullet(BulletData bullet)
    {
        if (!getContentComponent().isNeedsValidateion())
            return true;

        if (!bullet.getBullet().isNeedValidation())
            return true;

        Client ownerClient = BrainOutServer.Controller.getClients().get(bullet.getOwnerId());
        Client ownerColl = BrainOutServer.Controller.getClients().get(coll.getActiveData().getOwnerId());

        ServerSettings serverSettings = BrainOutServer.Settings;

        boolean myself = (bullet.getBullet().isDamageMyself());

        if (ownerClient == null || ownerColl == null)
        {
            // bots please stop
            if (!myself && bullet.getPlayerData() == coll.getActiveData())
                return false;

            return serverSettings.checkTeamFire(bullet.getPlayerData(), coll.getActiveData());
        }

        Team team = ownerClient.getTeam();

        boolean sameOwner = bullet.getOwnerId() == coll.getActiveData().getOwnerId();
        boolean teamFire =
            serverSettings.checkTeamFire(team, coll.getActiveData().getTeam()) ||
            serverSettings.checkTeamFire(ownerClient, ownerColl);

        return myself ? (teamFire || sameOwner) : teamFire && !sameOwner;
    }

    protected ActiveData.LastHitInfo collide(CollideEvent collideEvent, float damage)
    {
        BulletData bullet = collideEvent.bulletData;

        if (validateBullet(bullet))
        {
            bullet.setCollided(true);

            float dmg = damage * collideEvent.collider.content.damageCoefficient;

            return damage(collideEvent, bullet, dmg);
        }

        return activeData.getLastHitInfo();
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
}
