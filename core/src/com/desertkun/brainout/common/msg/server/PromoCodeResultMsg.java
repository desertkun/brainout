package com.desertkun.brainout.common.msg.server;

import com.desertkun.brainout.data.Data;
import com.desertkun.brainout.data.gamecase.CaseData;

public class PromoCodeResultMsg
{
    public Result result;
    public String id;
    public String data;

    public enum Result
    {
        success,
        codeIsNotValid,
        error
    }

    public PromoCodeResultMsg(){}
    public PromoCodeResultMsg(Result result)
    {
        this.result = result;
    }
}
