package com.desertkun.brainout.menu.impl;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.actions.RepeatAction;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.Align;
import com.desertkun.brainout.BrainOutClient;
import com.desertkun.brainout.ClientConstants;
import com.desertkun.brainout.L;
import com.desertkun.brainout.controllers.GameController;
import com.desertkun.brainout.menu.Menu;

public class WaitForDuelistMenu extends Menu
{
    private float dt;

    @Override
    public Table createUI()
    {
        Table data = new Table();

        Group notice = new Group();

        Image label = new Image(BrainOutClient.getRegion("label-search"));
        label.setFillParent(true);

        Image zoom = new Image(BrainOutClient.getRegion("label-search-zoom"));
        zoom.setSize(62, 64);
        zoom.setPosition(30, 40);
        zoom.setZIndex(10);
        zoom.addAction(Actions.repeat(RepeatAction.FOREVER, Actions.sequence(
            Actions.run(() -> zoom.setPosition(
                (int)(30 + MathUtils.cosDeg(dt) * 10),
                (int)(34 + MathUtils.sinDeg(dt) * 10)
            )), Actions.delay(0.01f)
        )));

        notice.addActor(label);
        notice.addActor(zoom);

        data.add(notice).size(118, 124).row();

        Label title = new Label(L.get("MENU_LOOKING_FOR_DUELIST"),
                BrainOutClient.Skin, "title-small");
        title.setAlignment(Align.left);
        data.add(title).padTop(16).row();

        return data;
    }

    @Override
    public void act(float delta)
    {
        super.act(delta);

        dt += delta * 180f;
    }

    @Override
    protected MenuAlign getMenuAlign()
    {
        return MenuAlign.fillTop;
    }

    @Override
    public void onFocusIn()
    {
        super.onFocusIn();

        BrainOutClient.Env.getGameController().setControllerMode(GameController.ControllerMode.disabled);

        if (ClientConstants.Client.MOUSE_LOCK)
        {
            Gdx.input.setCursorCatched(false);
        }
    }

    @Override
    public boolean escape()
    {
        pushMenu(new ExitMenu(false));

        return true;
    }
}
