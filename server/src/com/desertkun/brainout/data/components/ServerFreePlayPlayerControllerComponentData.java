package com.desertkun.brainout.data.components;

import com.desertkun.brainout.client.Client;
import com.desertkun.brainout.client.PlayerClient;
import com.desertkun.brainout.content.bullet.Bullet;
import com.desertkun.brainout.data.active.PlayerData;
import com.desertkun.brainout.mode.payload.FreePayload;
import com.desertkun.brainout.mode.payload.ModePayload;
import com.desertkun.brainout.reflection.Reflect;
import com.desertkun.brainout.reflection.ReflectAlias;

import java.util.Objects;

@Reflect("ServerFreePlayPlayerControllerComponent")
@ReflectAlias("data.components.ServerFreePlayPlayerControllerComponentData")
public class ServerFreePlayPlayerControllerComponentData extends ServerPlayerControllerComponentData
{
    public ServerFreePlayPlayerControllerComponentData(PlayerData playerData)
    {
        super(playerData);
    }

    @Override
    public boolean launchBullet(PlayerData pd, Bullet bullet, Bullet.BulletSlot slot,
                                float x, float y, float[] angles, int bulletsAmount, int random)
    {
        boolean ok = super.launchBullet(pd, bullet, slot, x, y, angles, bulletsAmount, random);

        if (ok && getClient() != null)
        {
            FreePayload pay = (FreePayload) getClient().getModePayload();
            if (pay != null)
            {
                pay.nonFriendAction();
            }
        }

        return ok;
    }

    @Override
    protected boolean isValidForDeadPlayer(PlayerClient client)
    {
        Client c = getClient();
        if (c == null)
            return false;
        
        return Objects.equals(client.getPartyId(), c.getPartyId());
    }
}
