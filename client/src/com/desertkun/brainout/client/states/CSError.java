package com.desertkun.brainout.client.states;

public class CSError extends ControllerState
{
    private final String message;
    private final Runnable ok;

    public CSError(String message, Runnable ok)
    {
        this.message = message;
        this.ok = ok;
    }

    public CSError(String message)
    {
        this(message, null);
    }

    @Override
    public void init()
    {

    }

    @Override
    public void release()
    {

    }

    @Override
    public ID getID()
    {
        return ID.error;
    }

    public String getMessage()
    {
        return message;
    }

    public Runnable getOk()
    {
        return ok;
    }
}
