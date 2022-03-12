package com.desertkun.brainout.managers;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.controllers.*;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.ObjectMap;
import com.desertkun.brainout.BrainOutClient;
import com.desertkun.brainout.Constants;
import com.desertkun.brainout.client.settings.GamePadKeyProperties;
import com.desertkun.brainout.client.settings.GamePadKeycodeProperty;
import com.desertkun.brainout.client.settings.Property;
import com.desertkun.brainout.client.states.CSGame;
import com.desertkun.brainout.client.states.ControllerState;
import com.desertkun.brainout.components.my.MyPlayerComponent;
import com.desertkun.brainout.controllers.GameController;
import com.desertkun.brainout.data.active.PlayerData;
import com.desertkun.brainout.events.Event;
import com.desertkun.brainout.events.GameControllerEvent;
import com.desertkun.brainout.events.SelectSlotEvent;
import com.esotericsoftware.minlog.Log;


public class GamePadManager implements ControllerListener
{
    public static final float AXIS_GAP = 0.02f;

    public interface ConfigurationCallback
    {
        void step(int step);
        void complete();
    }

    private ObjectMap<Integer, Float> axis;

    private Vector2 moveAxis;
    private Vector2 aimAxis, prevAim, target, resultAim;
    private AimMode aimMode;
    private float trigger;
    private boolean enabled;
    private boolean aimLock;
    private Controller controller;
    private ConfigurationCallback configuration;
    private int configurationStep;
    private boolean buttonLocked;
    private float dirty;

    public GamePadKeyProperties getCurrentProperties()
    {
        if (!isEnabled())
            return null;

        return BrainOutClient.ClientSett.getGamePadControls(getDetectedControllerName());
    }

    public String getControllerKind()
    {
        String name = getDetectedController().getName().toLowerCase();

        if (name.contains("sony"))
            return "sony";

        if (name.contains("ps4"))
            return "sony";

        if (name.contains("xbox"))
            return "xbox";

        return "generic";
    }

    private enum AimMode
    {
        update,
        accuracy
    }

    public GamePadManager()
    {
        axis = new ObjectMap<>();
        moveAxis = new Vector2();
        target = new Vector2();
        resultAim = new Vector2();
        aimAxis = new Vector2();
        prevAim = new Vector2();

        aimMode = AimMode.update;
        trigger = 0;
        enabled = false;
        aimLock = false;
        buttonLocked = false;
        dirty = 0;
    }

    public void configure(ConfigurationCallback callback)
    {
        for (Property property : getCurrentProperties().getProperties())
        {
            ((GamePadKeycodeProperty) property).setValue(-1);
        }

        configuration = callback;
        configurationStep = 1;

        configuration.step(configurationStep);
    }

    public boolean isEnabled()
    {
        return enabled;
    }

    public void init()
    {
        Controllers.addListener(this);

        for (Controller controller : Controllers.getControllers())
        {
            if (Log.INFO) Log.info("Controller detected: " + controller.getName());

            this.controller = controller;
            this.enabled = true;

            break;
        }
    }

    public Controller getDetectedController()
    {
        return controller;
    }

    public void release()
    {
        //
    }

    public void update(float dt)
    {
        if (BrainOutClient.Env.getGameController().getControllerMode() != GameController.ControllerMode.action)
            return;

        if (dirty <= 0 && aimAxis.isZero(0.02f))
            return;

        //if (prevAim.dst2(aimAxis) < 0.001f)
        //    return;

        //prevAim.set(aimAxis);

        ControllerState cs = BrainOutClient.ClientController.getState();

        if (cs.getID() != ControllerState.ID.game)
            return;

        CSGame game = ((CSGame) cs);

        PlayerData myPlayer = game.getPlayerData();

        if (myPlayer == null)
            return;

        MyPlayerComponent myPlayerComponent = myPlayer.getComponent(MyPlayerComponent.class);

        if (myPlayerComponent == null)
            return;

        target.set(myPlayerComponent.getPosX(), myPlayerComponent.getPosY());
        target.sub(myPlayer.getX(), myPlayer.getY());
        target.y = -target.y;

        float minAimDistance =
            (Math.min(BrainOutClient.getWidth(),
                BrainOutClient.getHeight()) / Constants.Graphics.RES_SIZE) * 0.4f * 0.75f;

        switch (aimMode)
        {
            case update:
            {
                float len = aimAxis.len();

                if (len < 0.5f)
                {
                    if (dirty > 0)
                    {
                        aimAxis.set(target).nor();
                    }
                    else
                    {
                        return;
                    }
                }

                resultAim.set(aimAxis).nor().scl(minAimDistance);
                resultAim.sub(target).scl(500.0f * dt);

                if (len > 0.95f && aimLock)
                {
                    trigger = 0;
                    aimMode = AimMode.accuracy;
                }

                break;
            }
            case accuracy:
            {
                float targetLen = target.len();

                float dtmt;

                if (targetLen < 40)
                {
                    dtmt = 8000.0f;
                }
                else
                {
                    float coef = 1.0f - ((Math.min(targetLen, 60) - 40.0f) / 20.0f);
                    dtmt = 3000.0f + Interpolation.circleIn.apply(coef) * 5000.f;
                }

                dtmt *= BrainOutClient.ClientSett.getGamepadSensitivity();

                target.nor();

                float dot = 1.0f - aimAxis.dot(target) / 2.0f;

                resultAim.set(aimAxis).scl(dtmt * dt * dot);

                if (!aimLock)
                {
                    trigger = 0;
                    aimMode = AimMode.update;
                }

                break;
            }
        }

        dirty -= dt;

        BrainOutClient.EventMgr.sendEvent(
            GameControllerEvent.obtain(GameControllerEvent.Action.aim, resultAim, true));
    }

    @Override
    public void connected(Controller controller)
    {
        if (Log.INFO) Log.info("New controller connected: " + controller.getName());
    }

    @Override
    public void disconnected(Controller controller)
    {
        if (Log.INFO) Log.info("Controller disconnected: " + controller.getName());
    }

    @Override
    public boolean buttonDown(Controller controller, int buttonCode)
    {
        if (controller != getDetectedController())
            return false;

        GamePadKeyProperties props = getCurrentProperties();

        buttonLocked = true;

        if (configuration != null && configurationStep > 0)
        {
            if (configurationStep > 1)
            {
                for (int i = 0; i < configurationStep - 1; i++)
                {
                    GamePadKeycodeProperty prevProperty =
                            ((GamePadKeycodeProperty) props.getProperties().get(i));

                    if (!prevProperty.isStick() && prevProperty.getValue() == buttonCode)
                    {
                        return true;
                    }
                }
            }

            GamePadKeycodeProperty property =
                ((GamePadKeycodeProperty) props.getProperties().get(configurationStep - 1));

            if (property.isStickOnly())
                return false;

            property.setStick(false);
            property.setKeyValue(buttonCode);

            nextConfigurationStep();
            return true;
        }
        else
        {
            GamePadKeyProperties.Keys key = props.getButtonKey(buttonCode);

            return key != null && processButtonDown(key);

        }
    }

    private boolean processButtonDown(GamePadKeyProperties.Keys key)
    {
        switch (key)
        {
            case slotPrimary:
            {
                sendEvent(SelectSlotEvent.obtain(0, Constants.Properties.SLOT_PRIMARY));

                return true;
            }
            case slotSecondary:
            {
                sendEvent(SelectSlotEvent.obtain(1));

                return true;
            }
            case slotSpecial:
            {
                // try to select special
                if (!sendEvent(SelectSlotEvent.obtain(2)))
                {
                    // if nothing to select, try to select secondary slot of primary weapon
                    sendEvent(SelectSlotEvent.obtain(0, Constants.Properties.SLOT_SECONDARY));
                }

                return true;
            }
            case slotKnife:
            {
                sendEvent(SelectSlotEvent.obtain(3));

                return true;
            }
            case reload:
            {
                sendEvent(GameControllerEvent.obtain(GameControllerEvent.Action.reload));

                return true;
            }
            case fire:
            {
                sendEvent(GameControllerEvent.obtain(GameControllerEvent.Action.beginLaunch));

                return true;
            }
            case aim:
            {
                aimLock = true;
                dirty = 0.2f;

                return true;
            }
            case run:
            {
                sendEvent(GameControllerEvent.obtain(GameControllerEvent.Action.beginRun));

                return true;
            }
            case crouch:
            {
                sendEvent(GameControllerEvent.obtain(GameControllerEvent.Action.beginSit));

                return true;
            }
            case firingMode:
            {
                sendEvent(GameControllerEvent.obtain(GameControllerEvent.Action.switchShootMode));

                return true;
            }
            case playerList:
            {
                sendEvent(GameControllerEvent.obtain(GameControllerEvent.Action.openPlayerList));

                return true;
            }
        }
        return false;
    }

    private void nextConfigurationStep()
    {
        if (configuration == null)
            return;

        configurationStep ++;

        if (configurationStep > GamePadKeyProperties.Keys.values().length)
        {
            configuration.complete();
            configuration = null;
            configurationStep = 0;

            return;
        }

        configuration.step(configurationStep);
    }

    @Override
    public boolean buttonUp(Controller controller, int buttonCode)
    {
        if (controller != getDetectedController())
            return false;

        buttonLocked = false;

        if (configuration != null && configurationStep > 0)
        {
            return true;
        }
        else
        {
            GamePadKeyProperties props = getCurrentProperties();

            GamePadKeyProperties.Keys key = props.getButtonKey(buttonCode);

            return key != null && processButtonUp(key);

        }
    }

    private boolean processButtonUp(GamePadKeyProperties.Keys key)
    {
        switch (key)
        {
            case fire:
            {
                sendEvent(GameControllerEvent.obtain(GameControllerEvent.Action.endLaunch));

                return true;
            }
            case aim:
            {
                aimLock = false;
                dirty = 0.2f;

                return true;
            }
            case run:
            {
                sendEvent(GameControllerEvent.obtain(GameControllerEvent.Action.endRun));

                return true;
            }
            case crouch:
            {
                sendEvent(GameControllerEvent.obtain(GameControllerEvent.Action.endSit));

                return true;
            }
            case playerList:
            {
                Gdx.app.postRunnable(() -> sendEvent(GameControllerEvent.obtain(GameControllerEvent.Action.closePlayerList)));

                return true;
            }
        }

        return false;
    }

    @Override
    public boolean axisMoved(Controller controller, int axisCode, float value)
    {
        Float oldValue = axis.get(axisCode, null);
        float old = value;

        if (oldValue != null)
        {
            old = oldValue;

            if (Math.abs(value - oldValue) < AXIS_GAP)
                return true;
        }

        axis.put(axisCode, value);

        if (controller != getDetectedController())
            return false;

        GamePadKeyProperties props = getCurrentProperties();

        if (configuration != null && configurationStep > 0)
        {
            if (buttonLocked)
            {
                return false;
            }

            GamePadKeycodeProperty property =
                    ((GamePadKeycodeProperty) props.getProperties().get(configurationStep - 1));

            boolean triggered;

            if (property.isStickOnly())
            {
                triggered = Math.abs(old) < 0.5f && Math.abs(value) >= 0.5f;
            }
            else
            {
                triggered = old < 0.5f && value >= 0.5f;
            }

            if (triggered)
            {
                if (configurationStep > 1)
                {
                    for (int i = 0; i < configurationStep - 1; i++)
                    {
                        GamePadKeycodeProperty prevProperty =
                                ((GamePadKeycodeProperty) props.getProperties().get(i));

                        if (prevProperty.isStick() && prevProperty.getValue() == axisCode)
                        {
                            return true;
                        }
                    }
                }

                property.setStick(true);
                property.setKeyValue(axisCode);

                nextConfigurationStep();
            }

            return true;
        }
        else
        {
            GamePadKeyProperties.Keys key = props.getStickKey(axisCode);

            if (key == null)
                return false;

            switch (key)
            {
                case aimLeftRight:
                {
                    aimAxis.x = value;

                    return true;
                }
                case moveLeftRight:
                {
                    if (Math.abs(value) > 0.5)
                    {
                        moveAxis.x = value;
                    }
                    else
                    {
                        moveAxis.x = 0;
                    }
                    updateMoveAxis();

                    return true;
                }
                case aimUpDown:
                {
                    aimAxis.y = value;

                    return true;
                }
                case moveUpDown:
                {
                    if (Math.abs(value) > 0.5)
                    {
                        moveAxis.y = -value;
                    }
                    else
                    {
                        moveAxis.y = 0;
                    }
                    updateMoveAxis();

                    return true;
                }
                default:
                {
                    if (old > 0.5f && value <= 0.5f)
                    {
                        return processButtonUp(key);
                    }
                    else if (old < 0.5f && value >= 0.5f)
                    {
                        return processButtonDown(key);
                    }

                    break;
                }
            }
        }

        return true;
    }

    private void updateMoveAxis()
    {
        if (moveAxis.isZero(0.02f))
        {
            moveAxis.set(0, 0);
        }

        sendEvent(GameControllerEvent.obtain(GameControllerEvent.Action.move, moveAxis));
    }

    private boolean sendEvent(Event event)
    {
        return BrainOutClient.EventMgr.sendEvent(event);
    }

    public String getDetectedControllerName()
    {
        return getDetectedController().getName();
    }

    public boolean isConfigured()
    {
        for (Property property : getCurrentProperties().getProperties())
        {
            if (((GamePadKeycodeProperty) property).getValue() == -1)
            {
                return false;
            }
        }

        return true;
    }
}
