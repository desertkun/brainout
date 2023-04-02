package com.desertkun.brainout.menu.impl;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.actions.RepeatAction;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Align;
import com.desertkun.brainout.BrainOutClient;
import com.desertkun.brainout.L;
import com.desertkun.brainout.client.states.CSGetRegions;
import com.desertkun.brainout.client.states.CSWaitForUser;
import com.desertkun.brainout.content.Animation;
import com.desertkun.brainout.content.effect.ParticleEffect;
import com.desertkun.brainout.data.effect.EffectData;
import com.desertkun.brainout.data.interfaces.PointLaunchData;
import com.desertkun.brainout.events.ClientControllerEvent;
import com.desertkun.brainout.events.EventReceiver;
import com.desertkun.brainout.menu.Menu;
import com.desertkun.brainout.menu.popups.AlertPopup;
import com.desertkun.brainout.menu.ui.LoadingBlock;
import com.desertkun.brainout.menu.ui.UIAnimation;
import com.desertkun.brainout.utils.HashedUrl;
import com.esotericsoftware.spine.AnimationState;
import com.esotericsoftware.spine.Event;

public class IntroMenu extends Menu implements EventReceiver
{
    private EffectData sparksEffect;

    private boolean canSwitch = false;
    private Table status;
    private Music introMusic;
    private Runnable autoContinue;

    @Override
    public Table createUI()
    {
        Table data = new Table();

        UIAnimation intro = new UIAnimation(BrainOutClient.ContentMgr.get("anim-intro", Animation.class));
        intro.getState().setAnimation(0, "start", false);
        intro.setScale(
            Math.max(BrainOutClient.getHeight() * 12.0f / 768.0f,
                    BrainOutClient.getWidth() * 9.0f / 1024.0f)
        );

        intro.getState().addListener(new AnimationState.AnimationStateListener()
        {
            @Override
            public void start(AnimationState.TrackEntry entry)
            {

            }

            @Override
            public void interrupt(AnimationState.TrackEntry entry)
            {

            }

            @Override
            public void end(AnimationState.TrackEntry entry)
            {

            }

            @Override
            public void dispose(AnimationState.TrackEntry entry)
            {

            }

            @Override
            public void complete(AnimationState.TrackEntry entry)
            {
                if (intro.getState().getCurrent(0).getAnimation().getName().equals("start"))
                {
                    intro.getState().setAnimation(0, "idle", true);
                }
            }

            @Override
            public void event(AnimationState.TrackEntry entry, Event event)
            {

            }
        });

        data.add(intro).expand().fill().row();

        createAdditionalUI(data);

        ParticleEffect sparks = BrainOutClient.ContentMgr.get("sparks-intro", ParticleEffect.class);

        if (sparks.isEnabled())
        {
            sparksEffect = sparks.getEffect(new PointLaunchData(BrainOutClient.getWidth() / 2.0f, 0, 0, "default"));
            sparksEffect.init();
        }

        status = new Table();
        status.align(Align.bottom);
        status.setFillParent(true);

        addActor(status);

        status.add(new LoadingBlock()).pad(32).expandX().right().row();

        return data;
    }

    protected void createAdditionalUI(Table data)
    {

    }

    @Override
    public boolean keyDown(int keyCode)
    {
        continueToNextMenu();

        return true;
    }

    @Override
    public boolean touchDown(int screenX, int screenY, int pointer, int button)
    {
        continueToNextMenu();

        return true;
    }

    private void continueToNextMenu()
    {
        if (!canSwitch)
            return;

        canSwitch = false;

        CSWaitForUser waitInto = BrainOutClient.ClientController.getState(CSWaitForUser.class);

        if (waitInto == null)
        {
            BrainOutClient.ClientController.setState(new CSGetRegions());
            return;
        }

        pushMenu(new FadeInMenu(0.5f, () ->
        {
            pop();
            waitInto.proceed();
        }));
    }

    @Override
    public void act(float delta)
    {
        super.act(delta);

        if (sparksEffect != null)
            sparksEffect.update(delta);
    }

    @Override
    public void render()
    {
        super.render();

        if (sparksEffect != null)
        {
            getBatch().begin();
            sparksEffect.render(getBatch(), null);
            getBatch().end();
        }
    }

    @Override
    public void onRelease()
    {
        super.onRelease();

        BrainOutClient.EventMgr.unsubscribe(com.desertkun.brainout.events.Event.ID.controller, this);

        //introMusic.stop();
        //introMusic.dispose();

        if (sparksEffect != null)
            sparksEffect.release();
    }

    @Override
    protected MenuAlign getMenuAlign()
    {
        return MenuAlign.fill;
    }

    @Override
    public void onInit()
    {
        super.onInit();

        BrainOutClient.EventMgr.subscribe(com.desertkun.brainout.events.Event.ID.controller, this);

        com.desertkun.brainout.content.Music intro =
            BrainOutClient.ContentMgr.get("music-intro", com.desertkun.brainout.content.Music.class);

        //introMusic = Gdx.audio.newMusic(BrainOut.PackageMgr.getFile(intro.getMusicName()));
        //introMusic.play();
        //introMusic.setVolume(BrainOutClient.ClientSett.getMusicVolume().getFloatValue());

        BrainOutClient.MusicMng.playMusic("music-mainmenu", true);
    }

    @Override
    public boolean onEvent(com.desertkun.brainout.events.Event event)
    {
        switch (event.getID())
        {
            case controller:
            {
                final ClientControllerEvent clientControllerEvent = ((ClientControllerEvent) event);

                if (clientControllerEvent.state == null)
                    return false;

                switch (clientControllerEvent.state.getID())
                {
                    case waitForUser:
                    {
                        weCanContinue();

                        break;
                    }

                    case maintenance:
                    {
                        pop();
                        pushMenu(new MaintenanceMenu(L.get("MENU_MAINTENANCE")));

                        break;
                    }

                    case gameOutdated:
                    {
                        pop();
                        pushMenu(new MaintenanceMenu(L.get("MENU_GAME_OUTDATED")));

                        break;
                    }
                }

                break;
            }
        }

        return false;
    }

    public boolean isCanSwitch()
    {
        return canSwitch;
    }

    private void weCanContinue()
    {
        if (canSwitch)
            return;

        if (BrainOutClient.ConnectToLocation != null)
        {
            HashedUrl hashedUrl = new HashedUrl();

            if (hashedUrl.unhash(BrainOutClient.ConnectToLocation))
            {
                Gdx.app.postRunnable(() ->
                {
                    BrainOutClient.ClientController.connect(
                        hashedUrl.getLocation(),
                        hashedUrl.getTcp(),
                        hashedUrl.getUdp(),
                        hashedUrl.getHttp(),
                        null,
                        false,
                        -1, () -> pushMenu(new AlertPopup("MENU_CONNECTION_ERROR")));
                });

            }
            else
            {
                pushMenu(new AlertPopup("MENU_WRONG_LINK")
                {
                    @Override
                    public void ok()
                    {
                        pushMenu(BrainOutClient.Env.createIntroMenu());
                    }
                });
            }

            BrainOutClient.ConnectToLocation = null;
        }

        Label clickToContinue = new Label(L.get("MENU_CLICK_TO_CONTINUE"),
                BrainOutClient.Skin, "title-small");

        clickToContinue.getColor().a = 0;

        clickToContinue.addAction(Actions.repeat(RepeatAction.FOREVER, Actions.sequence(
            Actions.alpha(1, 1f),
            Actions.alpha(0, 1f)
        )));

        clickToContinue.addListener(new ClickListener()
        {
            @Override
            public void clicked(InputEvent event, float x, float y)
            {
                continueToNextMenu();
            }
        });

        status.clear();

        status.add(clickToContinue).pad(128).row();

        canSwitch = true;

        if (autoContinue != null)
        {
            pop();
            autoContinue.run();
        }
    }

    public void setAutoContinue(Runnable autoContinue)
    {
        this.autoContinue = autoContinue;
    }
}
