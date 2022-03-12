package com.desertkun.brainout.events;

import com.desertkun.brainout.common.msg.server.CaseOpenResultMsg;
import com.desertkun.brainout.data.gamecase.CaseData;

public class CaseOpenResultEvent extends Event
{
    public CaseOpenResultMsg.Result result;
    public CaseData caseData;

    @Override
    public ID getID()
    {
        return ID.caseOpenResult;
    }

    private Event init(CaseOpenResultMsg.Result result, CaseData caseData)
    {
        this.result = result;
        this.caseData = caseData;

        return this;
    }

    public static Event obtain(CaseOpenResultMsg.Result result, CaseData caseData)
    {
        CaseOpenResultEvent e = obtain(CaseOpenResultEvent.class);
        if (e == null) return null;
        return e.init(result, caseData);
    }

    @Override
    public void reset()
    {
        this.result = null;
        this.caseData = null;
    }
}
