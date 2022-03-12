package com.desertkun.brainout.common.msg.server;

import com.desertkun.brainout.BrainOut;
import com.desertkun.brainout.common.msg.UdpMessage;
import com.desertkun.brainout.content.bullet.Bullet;
import com.desertkun.brainout.data.consumable.ConsumableRecord;

public class WeaponInfoMsg implements UdpMessage
{
    public int weaponId;
    public int rounds;
    public int roundsQuality;
    public int ch;
    public int chQuality;
    public boolean forceReset;
    public int stuckIn;
    public String slot;
    public MagazineInfo[] magazines;

    public static class MagazineInfo
    {
        public int id;
        public int rounds;
        public int quality;

        public MagazineInfo(int id, int rounds, int quality)
        {
            this.id = id;
            this.rounds = rounds;
            this.quality = quality;
        }

        public MagazineInfo() {}
    }

    public void setMagazinesCount(int count)
    {
        this.magazines = new MagazineInfo[count];
    }

    public WeaponInfoMsg() {}
    public WeaponInfoMsg(ConsumableRecord record, String slot, int rounds, int roundsQuality, int ch, int chQuality,
         boolean forceReset, int stuckIn)
    {
        this.weaponId = record.getId();
        this.rounds = rounds;
        this.roundsQuality = roundsQuality;
        this.ch = ch;
        this.chQuality = chQuality;
        this.stuckIn = stuckIn;
        this.slot = slot;
        this.forceReset = forceReset;
    }
}
