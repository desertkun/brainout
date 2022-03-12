package com.desertkun.brainout;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.Touchpad;
import com.badlogic.gdx.utils.Align;
import com.desertkun.brainout.controllers.GameController;
import com.desertkun.brainout.data.ClientMap;
import com.desertkun.brainout.events.GameControllerEvent;
import com.desertkun.brainout.menu.ui.ClickOverListener;

public class AndroidGameController extends GameController
{
    private final Vector2 aimPos, touchPos, prevPos, pointPos;

    private boolean touched;

    private Stage ui;
    private Touchpad touchMove;

    public AndroidGameController()
    {
        this.touched = false;
        this.touchPos = new Vector2();
        this.pointPos = new Vector2();
        this.aimPos = new Vector2();
        this.prevPos = new Vector2();
    }

    @Override
    public void init()
    {
        this.ui = new Stage();
    }

    @Override
    public boolean touchDown(int screenX, int screenY, int pointer, int button)
    {
        if (!touched)
        {
            touched = true;
            touchPos.set(screenX, screenY);

            switch (getControllerMode())
            {
                case action:
                {
                    sendAimEvent(screenX, screenY);
                }
            }
        }

        return super.touchDown(screenX, screenY, pointer, button);
    }

    @Override
    public boolean touchUp(int screenX, int screenY, int pointer, int button)
    {
        if (touched)
        {
            touched = false;
        }

        return super.touchUp(screenX, screenY, pointer, button);
    }

    @Override
    public boolean touchDragged(int screenX, int screenY, int pointer)
    {
        if (touched)
        {
            switch (getControllerMode())
            {
                case move:
                {
                    sendMoveEvent(screenX, screenY);
                }
                case action:
                {
                    sendAimEvent(screenX, screenY);
                }
            }
        }

        return super.touchDragged(screenX, screenY, pointer);
    }

    private void sendMoveEvent(int screenX, int screenY)
    {
        ClientMap.getMouseScale(screenX - touchPos.x, screenY - touchPos.y, pointPos);

        // invert y
        pointPos.y = -pointPos.y;

        sendEvent(GameControllerEvent.obtain(GameControllerEvent.Action.move, pointPos));

        touchPos.set(screenX, screenY);
    }

    private void sendAimEvent(int screenX, int screenY)
    {
        aimPos.set(screenX - BrainOutClient.getWidth() / 2f, screenY - BrainOutClient.getHeight() / 2f);

        sendEvent(GameControllerEvent.obtain(GameControllerEvent.Action.aim, aimPos));
    }

    @Override
    public void setControllerMode(ControllerMode controllerMode)
    {
        super.setControllerMode(controllerMode);

        if (ui != null)
        {
            ui.clear();

            switch (controllerMode)
            {
                case move:
                {
                    Table buttons = new Table();
                    buttons.setFillParent(true);
                    buttons.align(Align.center | Align.bottom);

                    ImageButton buttonOkay = new ImageButton(BrainOutClient.Skin, "button-touch-okay");
                    ImageButton buttonCancel = new ImageButton(BrainOutClient.Skin, "button-touch-cancel");

                    buttons.add(buttonOkay).pad(64);
                    buttons.add(buttonCancel).pad(64);

                    buttonOkay.addListener(new ClickOverListener()
                    {
                        @Override
                        public void clicked(InputEvent event, float x, float y)
                        {
                            sendEvent(GameControllerEvent.obtain(GameControllerEvent.Action.select));
                        }
                    });

                    buttonCancel.addListener(new ClickOverListener()
                    {
                        @Override
                        public void clicked(InputEvent event, float x, float y)
                        {
                            sendEvent(GameControllerEvent.obtain(GameControllerEvent.Action.back));
                        }
                    });

                    ui.addActor(buttons);

                    break;
                }

                case action:
                {
                    float size = Math.min(BrainOutClient.getWidth(), BrainOutClient.getHeight()) *
                            AndroidConstants.Touch.TOUCHPAD_SIZE;

                    touchMove = new Touchpad(AndroidConstants.Touch.TOUCH_DEAD_ZONE,
                            BrainOutClient.Skin, "touchpad-move");

                    ImageButton touchLaunch = new ImageButton(BrainOutClient.Skin, "button-touch-launch");

                    touchLaunch.addListener(new ClickOverListener()
                    {
                        @Override
                        public boolean touchDown(InputEvent event, float x, float y, int pointer, int button)
                        {
                            sendEvent(GameControllerEvent.obtain(GameControllerEvent.Action.beginLaunch, button));

                            return super.touchDown(event, x, y, pointer, button);
                        }

                        @Override
                        public void touchUp(InputEvent event, float x, float y, int pointer, int button)
                        {
                            sendEvent(GameControllerEvent.obtain(GameControllerEvent.Action.endLaunch));

                            super.touchUp(event, x, y, pointer, button);
                        }
                    });

                    Table touchPads = new Table();
                    touchPads.setFillParent(true);
                    touchPads.align(Align.center | Align.bottom);

                    touchPads.add(touchMove).pad(AndroidConstants.Touch.TOUCHPAD_PAD).expandX().size(size).left();
                    touchPads.add(touchLaunch).pad(AndroidConstants.Touch.TOUCHPAD_PAD).expandX().right();

                    touchPads.row();

                    ui.addActor(touchPads);

                    break;
                }
            }
        }
    }

    @Override
    public void update(float dt)
    {
        ui.act(dt);

        switch (controllerMode)
        {
            case action:
            {
                if (Math.abs(touchMove.getKnobPercentX()) < AndroidConstants.Touch.TOUCHPAD_MIN_DETECT)
                {
                    pointPos.x = 0;
                }
                else
                {
                    pointPos.x = Math.signum(touchMove.getKnobPercentX());
                }

                if (Math.abs(touchMove.getKnobPercentY()) < AndroidConstants.Touch.TOUCHPAD_MIN_DETECT)
                {
                    pointPos.y = 0;
                }
                else
                {
                    pointPos.y = Math.signum(touchMove.getKnobPercentY());
                }

                if (!prevPos.equals(pointPos))
                {
                    BrainOutClient.EventMgr.sendEvent(getEventReceiver(),
                        GameControllerEvent.obtain(GameControllerEvent.Action.move, pointPos));

                    prevPos.set(pointPos);
                }

                break;
            }
        }
    }

    @Override
    public void render()
    {
        ui.draw();
    }

    @Override
    public boolean keyDown(int keycode)
    {
        return ui.keyDown(keycode) || super.keyDown(keycode);
    }

    @Override
    public boolean keyUp(int keycode)
    {
        return ui.keyUp(keycode) || super.keyUp(keycode);
    }

    @Override
    public boolean keyTyped(char character)
    {
        return ui.keyTyped(character) || super.keyTyped(character);
    }

    @Override
    public boolean mouseMoved(int screenX, int screenY)
    {
        return ui.mouseMoved(screenX, screenY) || super.mouseMoved(screenX, screenY);
    }

    @Override
    public boolean scrolled(int amount)
    {
        return ui.scrolled(amount) || super.scrolled(amount);
    }
}
