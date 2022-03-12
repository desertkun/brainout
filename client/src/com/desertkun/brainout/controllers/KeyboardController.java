package com.desertkun.brainout.controllers;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.math.Vector2;
import com.desertkun.brainout.*;
import com.desertkun.brainout.client.settings.KeyProperties;
import com.desertkun.brainout.data.ClientMap;
import com.desertkun.brainout.events.Event;
import com.desertkun.brainout.events.GameControllerEvent;
import com.desertkun.brainout.events.SelectPreviousSlotEvent;
import com.desertkun.brainout.events.SelectSlotEvent;

public class KeyboardController extends GameController
{

    public enum KeyDirections
    {
        left,
        right,
        up,
        down
    }

    private boolean keyPressed[];
    private int keyCodes[];

    private final Vector2 pointPos, aimPos, tmp;

    public KeyboardController()
    {
        this.keyPressed = new boolean[KeyDirections.values().length];
        this.keyCodes = new int[4];
        this.pointPos = new Vector2();
        this.aimPos = new Vector2();
        this.tmp = new Vector2();
    }

    @Override
    public void init()
    {
        super.init();

        updateKeyCodes();

        BrainOutClient.EventMgr.subscribe(Event.ID.settingsUpdated, event ->
        {
            updateKeyCodes();

            return false;
        });
    }

    private void updateKeyCodes()
    {
        keyCodes = new int[]
                {
                        BrainOutClient.ClientSett.getKeyLeft().getValue(),
                        BrainOutClient.ClientSett.getKeyRight().getValue(),
                        BrainOutClient.ClientSett.getKeyUp().getValue(),
                        BrainOutClient.ClientSett.getKeyDown().getValue()
                };
    }

    private void directionKeyPressed(KeyDirections keyDirections)
    {
        this.keyPressed[keyDirections.ordinal()] = true;

        updateMoves();
    }

    private void directionKeyReleased(KeyDirections keyDirections)
    {
        this.keyPressed[keyDirections.ordinal()] = false;

        updateMoves();
    }

    private boolean keyPressed(int key)
    {
        if (getControllerMode() == ControllerMode.disabledKeys)
            return false;

        for (KeyDirections keyDirections: KeyDirections.values())
        {
            if (keyCodes[keyDirections.ordinal()] == key)
            {
                directionKeyPressed(keyDirections);

                return false;
            }
        }

        KeyProperties.Keys action = BrainOutClient.ClientSett.getControls().getKey(key);

        if (action != null)
        {
            switch (action)
            {
                case sit:
                {
                    sendEvent(GameControllerEvent.obtain(GameControllerEvent.Action.beginSit));
                    updateMoves();

                    return true;
                }
                case squat:
                {
                    sendEvent(GameControllerEvent.obtain(GameControllerEvent.Action.squat));
                    updateMoves();

                    return true;
                }
                case freePlayFriends:
                {
                    sendEvent(GameControllerEvent.obtain(GameControllerEvent.Action.freePlayFriends));

                    return true;
                }
                case voiceChat:
                {
                    sendEvent(GameControllerEvent.obtain(GameControllerEvent.Action.voiceChatBegin));

                    return true;
                }
                case hideInterface:
                {
                    BrainOutClient.ClientController.switchScreenshotMode();

                    return true;
                }
                case run:
                {
                    sendEvent(GameControllerEvent.obtain(GameControllerEvent.Action.beginRun));
                    updateMoves();

                    return true;
                }
                case chat:
                {
                    sendDelayedEvent(GameControllerEvent.obtain(GameControllerEvent.Action.openChat));

                    return true;
                }
                case teamChat:
                {
                    sendDelayedEvent(GameControllerEvent.obtain(GameControllerEvent.Action.openTeamChat));

                    return true;
                }
                case console:
                {
                    sendDelayedEvent(GameControllerEvent.obtain(GameControllerEvent.Action.openConsole));

                    return true;
                }
                case activate:
                {
                    sendEvent(GameControllerEvent.obtain(GameControllerEvent.Action.activate));

                    return true;
                }
                case playerList:
                {
                    sendEvent(GameControllerEvent.obtain(GameControllerEvent.Action.openPlayerList));

                    return true;
                }
                case dropWeapon:
                {
                    sendEvent(GameControllerEvent.obtain(GameControllerEvent.Action.dropInstrument));

                    return true;
                }
                case dropAmmo:
                {
                    sendEvent(GameControllerEvent.obtain(GameControllerEvent.Action.dropAmmo));

                    return true;
                }
                case changeTeam:
                {
                    sendEvent(GameControllerEvent.obtain(GameControllerEvent.Action.changeTeam));

                    return true;
                }
                case reload:
                {
                    sendEvent(GameControllerEvent.obtain(GameControllerEvent.Action.reload));

                    return true;
                }
                case unloadWeapon:
                {
                    sendEvent(GameControllerEvent.obtain(GameControllerEvent.Action.unload));

                    return true;
                }
                case shootMode:
                {
                    sendEvent(GameControllerEvent.obtain(GameControllerEvent.Action.switchShootMode));

                    return true;
                }
                case zoom:
                {
                    sendEvent(GameControllerEvent.obtain(GameControllerEvent.Action.switchZoom));

                    return true;
                }
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
                case slotBinoculars:
                {
                    sendEvent(SelectSlotEvent.obtain(4));

                    return true;
                }
                case slotFlashlight:
                {
                    sendEvent(SelectSlotEvent.obtain(5));

                    return true;
                }
                case previousSlot:
                {
                    sendEvent(SelectPreviousSlotEvent.obtain());

                    return true;
                }
            }
        }

        switch (key)
        {
            case Input.Keys.ESCAPE:
            {
                sendEvent(GameControllerEvent.obtain(GameControllerEvent.Action.back));

                return false;
            }
        }

        return false;
    }

    private void updateMoves()
    {
        updateMoves(false);
    }

    private void updateMoves(boolean delay)
    {
        if (controllerMode == ControllerMode.action || controllerMode == ControllerMode.actionWithNoMouseLocking)
        {
            if (isKeyDown(KeyDirections.left))
            {
                pointPos.x = -1;
            }
            else
            if (isKeyDown(KeyDirections.right))
            {
                pointPos.x = 1;
            }
            else
            {
                pointPos.x = 0;
            }

            if (isKeyDown(KeyDirections.up))
            {
                pointPos.y = 1;
            }
            else
            if (isKeyDown(KeyDirections.down))
            {
                pointPos.y = -1;
            }
            else
            {
                pointPos.y = 0;
            }

            if (delay)
            {
                sendDelayedEvent(GameControllerEvent.obtain(GameControllerEvent.Action.move, pointPos));
            }
            else
            {
                sendEvent(GameControllerEvent.obtain(GameControllerEvent.Action.move, pointPos));
            }
        }
    }

    private boolean keyReleased(int key)
    {
        if (getControllerMode() == ControllerMode.disabledKeys)
            return false;

        for (KeyDirections keyDirections: KeyDirections.values())
        {
            if (keyCodes[keyDirections.ordinal()] == key)
            {
                directionKeyReleased(keyDirections);

                return false;
            }
        }

        KeyProperties.Keys action = BrainOutClient.ClientSett.getControls().getKey(key);

        if (action != null)
        {
            switch (action)
            {
                case sit:
                {
                    sendEvent(GameControllerEvent.obtain(GameControllerEvent.Action.endSit));
                    updateMoves();

                    return true;
                }
                case voiceChat:
                {
                    sendEvent(GameControllerEvent.obtain(GameControllerEvent.Action.voiceChatEnd));

                    return true;
                }
                case run:
                {
                    sendEvent(GameControllerEvent.obtain(GameControllerEvent.Action.endRun));
                    updateMoves();

                    return true;
                }
                case playerList:
                {
                    switch (controllerMode)
                    {
                        case clientList:
                        {
                            sendEvent(GameControllerEvent.obtain(GameControllerEvent.Action.back));
                            return true;
                        }
                    }

                    break;
                }
            }
        }

        return false;
    }

    @Override
    public void update(float dt)
    {
        switch (controllerMode)
        {
            case action:
            case actionWithNoMouseLocking:
            {
                tmp.set(
                        Gdx.input.getX() - (BrainOutClient.getWidth() / 2),
                        Gdx.input.getY() - (BrainOutClient.getHeight() / 2)
                );

                if (controllerMode == ControllerMode.action)
                {
                    if (ClientConstants.Client.MOUSE_LOCK)
                        Gdx.input.setCursorPosition(BrainOutClient.getWidth() / 2, BrainOutClient.getHeight() / 2);
                }

                if (!aimPos.epsilonEquals(tmp, 0.001f))
                {
                    aimPos.set(tmp);

                    if (controllerMode == ControllerMode.action)
                    {
                        BrainOutClient.EventMgr.sendEvent(
                            GameControllerEvent.obtain(GameControllerEvent.Action.aim, aimPos));
                    }
                    else
                    {
                        BrainOutClient.EventMgr.sendEvent(
                            GameControllerEvent.obtain(GameControllerEvent.Action.absoluteAim, aimPos));
                    }
                }


                break;
            }

            case move:
            {
                ClientMap.getMouseScale(
                        Gdx.input.getX() - BrainOutClient.getWidth() / 2,
                        Gdx.input.getY() - BrainOutClient.getHeight() / 2, tmp);

                Gdx.input.setCursorPosition(BrainOutClient.getWidth() / 2, BrainOutClient.getHeight() / 2);

                // invert y
                tmp.y = -tmp.y;

                if (!pointPos.epsilonEquals(tmp, 0.001f))
                {
                    pointPos.set(tmp);

                    sendEvent(GameControllerEvent.obtain(GameControllerEvent.Action.move, pointPos));

                    Gdx.input.setCursorPosition(BrainOutClient.getWidth() / 2, BrainOutClient.getHeight() / 2);
                }

                break;
            }
        }
    }

    @Override
    public boolean keyUp(int keycode)
    {
        return keyReleased(keycode);
    }

    private boolean isDisabled()
    {
        return getControllerMode() == ControllerMode.disabled ||
            getControllerMode() == ControllerMode.disabledKeys;
    }

    @Override
    public boolean keyDown(int keyCode)
    {
        return keyPressed(keyCode);
    }

    @Override
    public boolean touchUp(int screenX, int screenY, int pointer, int button)
    {
        if (isDisabled()) return false;

        switch (controllerMode)
        {
            case action:
            {
                if (ClientEnvironment.isMac())
                {
                    if (Gdx.input.isKeyPressed(Input.Keys.CONTROL_LEFT) && button == Input.Buttons.RIGHT)
                    {
                        button = Input.Buttons.LEFT;
                    }
                }

                if (button == Input.Buttons.LEFT)
                {
                    sendEvent(GameControllerEvent.obtain(GameControllerEvent.Action.endLaunch, button));
                }
                else
                {
                    sendEvent(GameControllerEvent.obtain(GameControllerEvent.Action.endLaunchSecondary, button));
                }

                break;
            }
        }

        return super.touchUp(screenX, screenY, pointer, button);
    }

    private boolean isKeyDown(KeyDirections keyDirections)
    {
        return !isDisabled() && keyPressed[keyDirections.ordinal()];
    }

    @Override
    public boolean touchDown(int screenX, int screenY, int pointer, int button)
    {
        if (isDisabled()) return false;

        switch (controllerMode)
        {
            case action:
            {
                if (ClientEnvironment.isMac())
                {
                    if (Gdx.input.isKeyPressed(Input.Keys.CONTROL_LEFT) && button == Input.Buttons.RIGHT)
                    {
                        button = Input.Buttons.LEFT;
                    }
                }

                if (button == Input.Buttons.LEFT)
                {

                    sendEvent(GameControllerEvent.obtain(GameControllerEvent.Action.beginLaunch, button));
                }
                else
                {
                    sendEvent(GameControllerEvent.obtain(GameControllerEvent.Action.beginLaunchSecondary));
                }

                break;
            }
            case move:
            {
                sendEvent(GameControllerEvent.obtain(GameControllerEvent.Action.select));

                break;
            }
        }

        return super.touchDown(screenX, screenY, pointer, button);
    }

    @Override
    public void setControllerMode(ControllerMode controllerMode)
    {
        super.setControllerMode(controllerMode);

        switch (controllerMode)
        {
            case move:
            {
                Gdx.input.setCursorPosition(BrainOutClient.getWidth() / 2, BrainOutClient.getHeight() / 2);

                break;
            }
        }
    }

    @Override
    public void reset()
    {
        super.reset();

        for (int i = 0; i < keyPressed.length; i++)
        {
            keyPressed[i] = false;
        }

        updateMoves(true);
    }
}
