package com.desertkun.brainout.menu.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.badlogic.gdx.utils.Scaling;
import com.desertkun.brainout.BrainOutClient;
import com.desertkun.brainout.ClientConstants;
import com.desertkun.brainout.L;
import com.desertkun.brainout.content.Medal;
import com.desertkun.brainout.content.components.IconComponent;

import java.util.TimerTask;

public class Tooltip extends InputListener
{
    private final Actor actor;
    private final Stage stage;

    private Actor tooltipActor;
    private TimerTask timer;
    private TooltipCreator creator;

    private static Tooltip Last = null;

    public interface TooltipCreator
    {
        Actor get();
    }

    public static void Hide()
    {
        if (Last != null)
        {
            Last.hide();
            Last = null;
        }
    }

    public static class TooltipTable extends Table
    {
        @Override
        public void act(float delta)
        {
            super.act(delta);

            Tooltip.update(this);
        }
    }

    public static void update(Actor actor)
    {
        float x;
        float y;

        float my = BrainOutClient.getHeight() - Gdx.input.getY() + 16;
        float mx = Gdx.input.getX() + 16;

        if (mx > BrainOutClient.getWidth() - actor.getWidth() - 32)
        {
            x = Math.max(mx - actor.getWidth() - 32, 32);
        }
        else
        {
            x = mx;
        }

        if (my > BrainOutClient.getHeight() - actor.getHeight() - 32)
        {
            y = Math.max(my - actor.getHeight() - 32, 32);
        }
        else
        {
            y = my;
        }

        actor.setPosition(x, y);
    }

    public Tooltip(Actor actor, final String text, Stage stage)
    {
        this.actor = actor;
        this.creator = new TooltipCreator()
        {
            @Override
            public Actor get()
            {
                return new Label(text, BrainOutClient.Skin, "tooltip-medium")
                {
                    float time = ClientConstants.Menu.Tooltip.HIDE_TIME;

                    @Override
                    public void act(float delta)
                    {
                        super.act(delta);

                        update(this);

                        time -= delta;

                        if (time < 0)
                        {
                            hide();
                            remove();
                        }
                    }
                };
            }
        };

        this.stage = stage;
    }

    public Tooltip(Actor actor, TooltipCreator creator, Stage stage)
    {
        this.actor = actor;
        this.creator = creator;
        this.stage = stage;
    }

    public static void RegisterToolTip(final Actor actor, final String text, final Stage stage)
    {
        actor.addListener(new Tooltip(actor, text, stage));
    }

    public static void RegisterStandardToolTip(
        final Actor actor, final String title, final String text, final Stage stage)
    {
        actor.addListener(new Tooltip(actor, new TooltipCreator()
        {
            @Override
            public Actor get()
            {
                Table actor = new Table()
                {
                    @Override
                    public void act(float delta)
                    {
                        super.act(delta);

                        Tooltip.update(this);
                    }
                };

                {
                    Label header = new Label(title, BrainOutClient.Skin, "title-yellow");
                    header.setAlignment(Align.center);
                    actor.add(new BorderActor(header, "form-dark-blue")).expandX().fillX().row();
                }

                {
                    Label description = new Label(text, BrainOutClient.Skin, "title-small");
                    description.setAlignment(Align.center);
                    description.setWrap(true);

                    BorderActor borderActor = new BorderActor(description, "border-dark-blue");
                    borderActor.getCell().width(560);
                    actor.add(borderActor).height(128).expandX().fillX().row();
                }

                actor.setSize(580, 128);

                return actor;
            }
        }, stage));
    }

    public static void RegisterImageToolTip(
            final Actor actor, final String title, final Drawable drawable, final Stage stage)
    {
        actor.addListener(new Tooltip(actor, new TooltipCreator()
        {
            @Override
            public Actor get()
            {
                Table actor = new Table()
                {
                    @Override
                    public void act(float delta)
                    {
                        super.act(delta);

                        Tooltip.update(this);
                    }
                };

                {
                    Label header = new Label(title, BrainOutClient.Skin, "title-yellow");
                    header.setAlignment(Align.center);
                    actor.add(new BorderActor(header, "form-dark-blue")).expandX().fillX().row();
                }

                {
                    Image background = new Image(drawable);
                    background.setScaling(Scaling.fit);

                    BorderActor borderActor = new BorderActor(background, "border-dark-blue");
                    borderActor.getCell().size(560, 120);
                    actor.add(borderActor).height(128).expandX().fillX().row();
                }

                actor.setSize(580, 128);

                return actor;
            }
        }, stage));
    }

    public static void RegisterToolTip(final Actor actor, final TooltipCreator creator, final Stage stage)
    {
        actor.addListener(new Tooltip(actor, creator, stage));
    }

    @Override
    public void enter(InputEvent event, float x, float y, int pointer, Actor fromActor)
    {
        if (fromActor != null && fromActor.isDescendantOf(actor))
        {
            return;
        }

        if (pointer == -1 && !Gdx.input.isButtonPressed(Input.Buttons.LEFT))
        {
            show();
        }

        super.enter(event, x, y, pointer, fromActor);
    }

    @Override
    public void exit(InputEvent event, float x, float y, int pointer, Actor toActor)
    {
        if (toActor != null && toActor.isDescendantOf(actor))
        {
            return;
        }

        remove();
        hide();

        super.exit(event, x, y, pointer, toActor);
    }

    @Override
    public boolean touchDown(InputEvent event, float x, float y, int pointer, int button)
    {
        remove();
        hide();

        return super.touchDown(event, x, y, pointer, button);
    }

    private void show()
    {
        Last = this;

        this.timer = new TimerTask()
        {
            @Override
            public void run()
            {
                Gdx.app.postRunnable(() ->
                {
                    timer = null;

                    try
                    {
                        tooltipActor = creator.get();
                        if (stage == null)
                            return;
                        stage.addActor(tooltipActor);
                    }
                    catch (GdxRuntimeException ignored)
                    {

                    }
                });

            }
        };

        BrainOutClient.Timer.schedule(timer, (long)(1000 * ClientConstants.Menu.Tooltip.TOOLTIP_TIME));
    }

    public void hide()
    {
        if (timer != null)
        {
            timer.cancel();
            timer = null;
        }
    }

    public void remove()
    {
        if (tooltipActor != null)
        {
            tooltipActor.remove();

            tooltipActor = null;
        }
    }
}
