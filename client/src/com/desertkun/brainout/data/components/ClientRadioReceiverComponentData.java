package com.desertkun.brainout.data.components;

import com.desertkun.brainout.BrainOutClient;
import com.desertkun.brainout.content.components.ClientRadioReceiverComponent;
import com.desertkun.brainout.content.effect.Effect;
import com.desertkun.brainout.data.ClientMap;
import com.desertkun.brainout.data.active.ActiveData;
import com.desertkun.brainout.data.components.base.Component;
import com.desertkun.brainout.data.effect.EffectData;
import com.desertkun.brainout.data.interfaces.LaunchData;
import com.desertkun.brainout.data.interfaces.PointLaunchData;
import com.desertkun.brainout.events.Event;
import com.desertkun.brainout.events.FreePlayRadioEvent;
import com.desertkun.brainout.reflection.Reflect;
import com.desertkun.brainout.reflection.ReflectAlias;

@Reflect("ClientRadioReceiverComponent")
@ReflectAlias("data.components.ClientRadioReceiverComponentData")
public class ClientRadioReceiverComponentData extends Component<ClientRadioReceiverComponent>
{
    private final ActiveData a;
    private LaunchData launch;
    private EffectData ef;
    private Mode mode;
    private float timer;
    private String read;
    private int index;
    private int repeat;

    private enum Mode
    {
        background,
        delay,
        characters
    }

    public ClientRadioReceiverComponentData(ActiveData componentObject,
                                            ClientRadioReceiverComponent contentComponent)
    {
        super(componentObject, contentComponent);

        this.a = componentObject;
        this.mode = Mode.background;
    }

    @Override
    public void update(float dt)
    {
        super.update(dt);

        switch (mode)
        {
            case delay:
            {
                timer -= dt;
                if (timer < 0)
                {
                    timer = 1.0f;
                    index = 0;
                    mode = Mode.characters;
                }

                break;
            }
            case characters:
            {
                timer -= dt;
                if (timer < 0)
                {
                    char c = read.charAt(index++);
                    Effect play = getContentComponent().getCharacters().get(String.valueOf(c));
                    if (play != null)
                    {
                        getMap(ClientMap.class).addEffect(play, launch);
                    }

                    if (index >= read.length())
                    {
                        index = 0;
                        if (repeat-- >= 0)
                        {
                            timer = 8.0f;
                            mode = Mode.delay;
                        }
                        else
                        {
                            timer = 1.0f;
                            mode = Mode.background;
                            ef.release();
                            playBackground();
                        }
                    }
                    else
                    {
                        timer = 1.8f;
                    }
                }

                break;
            }
        }
    }

    private void playBackground()
    {
        if (getContentComponent().getBackground() != null)
        {
            ef = getMap(ClientMap.class).addEffect(getContentComponent().getBackground(), launch);
        }
    }

    private boolean playActive()
    {
        if (getContentComponent().getActive() != null)
        {
            ef = getMap(ClientMap.class).addEffect(getContentComponent().getActive(), launch);
            return true;
        }

        return false;
    }

    @Override
    public void init()
    {
        super.init();

        this.launch = new LaunchData()
        {

            @Override
            public float getX()
            {
                return a.getX();
            }

            @Override
            public float getY()
            {
                return a.getY();
            }

            @Override
            public float getAngle()
            {
                return 0;
            }

            @Override
            public boolean getFlipX()
            {
                return false;
            }

            @Override
            public String getDimension()
            {
                return a.getDimension();
            }
        };

        playBackground();

        BrainOutClient.EventMgr.subscribe(Event.ID.freePlayRadio, this);
    }

    @Override
    public void release()
    {
        super.release();

        mode = Mode.background;
        index = 0;
        read = "";
        timer = 0;

        if (ef != null)
        {
            ef.release();
        }

        BrainOutClient.EventMgr.unsubscribe(Event.ID.freePlayRadio, this);
    }

    @Override
    public boolean hasRender()
    {
        return false;
    }

    @Override
    public boolean hasUpdate()
    {
        return true;
    }

    @Override
    public boolean onEvent(Event event)
    {
        switch (event.getID())
        {
            case freePlayRadio:
            {
                FreePlayRadioEvent ev = ((FreePlayRadioEvent) event);
                playRadioMessage(ev.message, ev.repeat);

                break;
            }
        }

        return false;
    }

    private void playRadioMessage(String message, int repeat)
    {
        if (ef != null)
        {
            ef.release();
        }

        if (playActive())
        {
            index = 0;
            read = message;
            mode = Mode.delay;
            timer = 5.0f;
            this.repeat = repeat;
        }
    }
}
