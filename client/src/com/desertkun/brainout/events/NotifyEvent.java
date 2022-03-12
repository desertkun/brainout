package com.desertkun.brainout.events;

import com.desertkun.brainout.common.enums.NotifyMethod;
import com.desertkun.brainout.common.enums.NotifyReason;
import com.desertkun.brainout.common.enums.NotifyAward;
import com.desertkun.brainout.common.enums.data.NotifyData;

public class NotifyEvent extends Event
{
    public float amount;
    public NotifyReason reason;
    public NotifyAward notifyAward;
    public NotifyMethod method;
    public NotifyData data;

    @Override
    public ID getID()
    {
        return ID.notify;
    }

    private Event init(NotifyAward notifyAward, float amount,
                       NotifyReason reason, NotifyMethod method, NotifyData data)
    {
        this.amount = amount;
        this.reason = reason;
        this.notifyAward = notifyAward;
        this.method = method;
        this.data = data;

        return this;
    }

    public static Event obtain(NotifyAward notifyAward, float amount,
                               NotifyReason reason, NotifyMethod method, NotifyData data)
    {
        NotifyEvent e = obtain(NotifyEvent.class);
        if (e == null) return null;
        return e.init(notifyAward, amount, reason, method, data);
    }

    @Override
    public void reset()
    {
        this.amount = 0;
        this.reason = null;
        this.notifyAward = null;
        this.method = null;
        this.data = null;
    }
}
