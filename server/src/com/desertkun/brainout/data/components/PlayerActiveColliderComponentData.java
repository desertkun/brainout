package com.desertkun.brainout.data.components;

import com.badlogic.gdx.utils.ObjectMap;
import com.desertkun.brainout.BrainOutServer;
import com.desertkun.brainout.Constants;
import com.desertkun.brainout.client.Client;
import com.desertkun.brainout.client.PlayerClient;
import com.desertkun.brainout.common.msg.server.HitConfirmMsg;
import com.desertkun.brainout.components.PlayerOwnerComponent;
import com.desertkun.brainout.content.components.PlayerActiveColliderComponent;
import com.desertkun.brainout.content.consumable.impl.ArmorConsumableItem;
import com.desertkun.brainout.data.active.ActiveData;
import com.desertkun.brainout.data.bullet.BulletData;
import com.desertkun.brainout.data.consumable.ConsumableRecord;
import com.desertkun.brainout.data.interfaces.LaunchData;
import com.desertkun.brainout.events.CollideEvent;

import com.desertkun.brainout.events.DamageEvent;
import com.desertkun.brainout.reflection.Reflect;
import com.desertkun.brainout.reflection.ReflectAlias;

@Reflect("PlayerActiveColliderComponent")
@ReflectAlias("data.components.PlayerActiveColliderComponentData")
public class PlayerActiveColliderComponentData<T extends PlayerActiveColliderComponent>
    extends ActiveColliderComponentData<T>
{
    public PlayerActiveColliderComponentData(ActiveData activeData, T colliderComponent)
    {
        super(activeData, colliderComponent);
    }

    @Override
    protected ActiveData.LastHitInfo collide(CollideEvent collideEvent, float damage)
    {
        PlayerOwnerComponent poc = activeData.getComponent(PlayerOwnerComponent.class);

        if (poc != null)
        {
            ObjectMap<Integer, ConsumableRecord> armor = poc.getConsumableContainer().getCategory("armor");

            if (armor != null)
            {
                for (ObjectMap.Entry<Integer, ConsumableRecord> entry : armor)
                {
                    ConsumableRecord dm = entry.value;

                    if (dm != null)
                    {
                        if (dm.getItem() instanceof ArmorConsumableItem)
                        {
                            ArmorConsumableItem hci = ((ArmorConsumableItem) dm.getItem());

                            if (validateBullet(collideEvent.bulletData))
                            {
                                damage = hci.protect(collideEvent, damage, entry.value.getQuality());

                                if (hci.empty())
                                {
                                    poc.getConsumableContainer().removeRecord(dm, true);

                                    Client client = BrainOutServer.Controller.getClients().get(activeData.getOwnerId());

                                    if (client instanceof PlayerClient && client.isAlive())
                                    {
                                        ServerPlayerControllerComponentData pc =
                                                activeData.getComponentWithSubclass(ServerPlayerControllerComponentData.class);

                                        if (pc != null)
                                        {
                                            pc.updateAttachments();
                                        }

                                        client.sendConsumable();
                                    }
                                }

                                if (damage <= 0)
                                {
                                    // system protected the bullet
                                    BulletData bullet = collideEvent.bulletData;

                                    bullet.setCollided(true);
                                    LaunchData l = bullet.getLaunchData();

                                    health.damage((DamageEvent) DamageEvent.obtain(
                                            0,
                                            coll.getActiveData().getId(),
                                            bullet.getInstrumentInfo(),
                                            bullet,
                                            l.getX(), l.getY(), l.getAngle(),
                                            Constants.Damage.DAMAGE_PROTECT
                                    ));

                                    Client client = BrainOutServer.Controller.getClients().get(bullet.getOwnerId());

                                    if (client instanceof PlayerClient)
                                    {
                                        ((PlayerClient) client).sendUDP(new HitConfirmMsg());
                                    }

                                    return null;
                                }
                            }
                        }
                    }
                }
            }
        }

        ActiveData.LastHitInfo lastHitInfo = super.collide(collideEvent, damage);

        if (collideEvent.colliderName.equals("head"))
        {
            lastHitInfo.kind = ActiveData.LastHitKind.headshot;
        }
        else
        {
            lastHitInfo.kind = ActiveData.LastHitKind.normal;
        }

        return lastHitInfo;
    }
}
