package com.desertkun.brainout.playstate;

import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;
import com.desertkun.brainout.BrainOut;
import com.desertkun.brainout.common.ReflectionReceiver;
import com.desertkun.brainout.common.msg.ModeMessage;
import com.desertkun.brainout.data.Data;
import com.desertkun.brainout.data.interfaces.ComponentWritable;
import com.desertkun.brainout.events.Event;
import com.desertkun.brainout.events.EventReceiver;
import com.desertkun.brainout.events.PlayStateUpdatedEvent;

public abstract class PlayState implements ComponentWritable, EventReceiver
{
    private ReflectionReceiver receiver;

    public abstract ID getID();

    public enum ID
    {
        DEPRECATED,
        game,
        endgame,
        empty
    }

    public PlayState()
    {
        receiver = new ReflectionReceiver();
    }

    public interface InitCallback
    {
        void done(boolean success);
    }

    public void init(InitCallback done) {}
    public void release() {}
    public void update(float dt) {}

    public void updated()
    {
        BrainOut.EventMgr.sendDelayedEvent(PlayStateUpdatedEvent.obtain());
    }

    public void write(Json json, Data.ComponentWriter componentWriter, int owner) {}
    public void read(Json json, JsonValue jsonData) {}

    @Override
    public boolean onEvent(Event event)
    {
        return false;
    }

    public boolean received(Object from, ModeMessage o)
    {
        return receiver.received(o, this);
    }
}
