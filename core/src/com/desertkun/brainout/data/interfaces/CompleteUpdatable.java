package com.desertkun.brainout.data.interfaces;

public interface CompleteUpdatable extends Updatable
{
    boolean done();

    void init();
    void release();
}
