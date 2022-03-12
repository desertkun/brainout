package com.desertkun.brainout.common.msg.server;

import com.desertkun.brainout.common.msg.InstrumentInfoPart;
import com.desertkun.brainout.data.active.ActiveData;
import com.desertkun.brainout.data.instrument.InstrumentInfo;

public class KilledByMsg
{
    public int activeId;
    public int d;
    public InstrumentInfoPart infoPart;

    public KilledByMsg() {}
    public KilledByMsg(ActiveData activeData, InstrumentInfo info)
    {
        this.infoPart = new InstrumentInfoPart(info);
        this.activeId = activeData != null ? activeData.getId() : -1;
        this.d = activeData != null ? activeData.getDimensionId() : -1;
    }
}
