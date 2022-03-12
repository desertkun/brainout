package com.desertkun.brainout.common.msg;

public class ErrorMsg
{
    public enum Code
    {
        errorSpawning,
        notEnoughtGold,
        notEnoughtWeight,
        wrongTeam,
        cantSpawn,
        notInitialized,
        codeIsNotValid
    }

    public Code code;

    public ErrorMsg()
    {

    }

    public ErrorMsg(Code code)
    {
        this.code = code;
    }
}
