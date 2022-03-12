package com.desertkun.brainout.common.msg.server;

import com.desertkun.brainout.data.Data;
import com.desertkun.brainout.data.gamecase.CaseData;

public class CaseOpenResultMsg
{
    public Result result;
    public String id;
    public String data;

    public enum Result
    {
        success,
        notApplicable
    }

    public CaseOpenResultMsg(){}
    public CaseOpenResultMsg(Result result, CaseData caseData)
    {
        this.result = result;

        if (caseData != null)
        {
            this.id = caseData.getContent().getID();
            this.data = Data.ComponentSerializer.toJson(caseData, Data.ComponentWriter.TRUE, -1);
        }
        else
        {
            this.id = "";
            this.data = "";
        }
    }
}
