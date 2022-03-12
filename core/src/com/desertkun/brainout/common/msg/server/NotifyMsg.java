package com.desertkun.brainout.common.msg.server;

import com.desertkun.brainout.common.enums.data.NotifyData;
import com.desertkun.brainout.common.enums.NotifyMethod;
import com.desertkun.brainout.common.enums.NotifyReason;
import com.desertkun.brainout.common.enums.NotifyAward;

public class NotifyMsg
{
    public NotifyAward notifyAward;
    public float amount;
    public NotifyReason reason;
    public NotifyMethod method;
    public NotifyData data;

    public NotifyMsg() {}
    public NotifyMsg(NotifyAward notifyAward, float amount, NotifyReason reason, NotifyMethod method, NotifyData data)
    {
        this.notifyAward = notifyAward;
        this.amount = amount;
        this.reason = reason;
        this.method = method;
        this.data = data;
    }
}
