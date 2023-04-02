package com.desertkun.brainout.menu.ui;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.*;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.NinePatchDrawable;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.ObjectMap;
import com.badlogic.gdx.utils.Scaling;
import com.desertkun.brainout.BrainOutClient;
import com.desertkun.brainout.ClientConstants;
import com.desertkun.brainout.Constants;
import com.desertkun.brainout.L;
import com.desertkun.brainout.common.msg.client.ContentActionMsg;
import com.desertkun.brainout.components.DurabilityComponent;
import com.desertkun.brainout.content.instrument.Instrument;
import com.desertkun.brainout.menu.Menu;

public class GearsButton extends Button
{
    private final Instrument instrument;
    private final Stage stage;
    private final String title;

    private static ObjectMap<Integer, String> conditions = new ObjectMap<>();

    static
    {
        conditions.put(1, "MENU_CONDITION_1");
        conditions.put(2, "MENU_CONDITION_2");
        conditions.put(3, "MENU_CONDITION_3");
    }

    public GearsButton(Instrument instrument, String title, Stage stage)
    {
        super(BrainOutClient.Skin, "button-gears");

        this.instrument = instrument;
        this.stage = stage;
        this.title = title;

        init();
    }

    private void init()
    {
        Image image = new Image(BrainOutClient.getRegion("icon-gears"));
        image.setFillParent(true);
        image.setTouchable(Touchable.disabled);
        image.setScaling(Scaling.none);
        addActor(image);

        addListener(new ClickOverListener()
        {
            @Override
            public void clicked(InputEvent event, float x, float y)
            {
                tryFix();
            }
        });

        DurabilityComponent dc = instrument.getComponentFrom(DurabilityComponent.class);

        if (dc != null)
        {
            Tooltip.RegisterToolTip(this, new Tooltip.TooltipCreator()
            {
                @Override
                public Actor get()
                {
                    Table tooltip = new Table()
                    {
                        @Override
                        public void act(float delta)
                        {
                            super.act(delta);

                            Tooltip.update(this);
                        }
                    };

                    tooltip.align(Align.top | Align.center);

                    float durability = dc.getDurability(BrainOutClient.ClientController.getUserProfile());
                    float durabilityLevel = durability /
                            dc.getDurability();

                    int condition =
                        (durabilityLevel > 0.66) ? 1 :
                            (durabilityLevel > 0.33 ? 2 : 3);

                    Table content = new Table();
                    content.setBackground(new NinePatchDrawable(BrainOutClient.getNinePatch("form-default")));

                    Label titleLabel = new Label(
                        title,
                        BrainOutClient.Skin, "title-level");

                    titleLabel.setAlignment(Align.center);
                    titleLabel.setWrap(true);

                    tooltip.add(new BorderActor(titleLabel, "form-gray")).expandX().fillX().row();
                    tooltip.add(content).expand().fill().row();

                    TextureRegion bigIcon = BrainOutClient.getRegion(
                        "icon-gears-" + condition
                    );

                    Image bigIconImage = new Image(bigIcon);
                    bigIconImage.setScaling(Scaling.none);
                    content.add(bigIconImage).pad(8).expandX().fillX().row();

                    Label conditionTitle = new Label(L.get("MENU_CONDITION"),
                            BrainOutClient.Skin, "title-gray");
                    conditionTitle.setAlignment(Align.center);
                    conditionTitle.setWrap(true);
                    content.add(conditionTitle).width(400).expandX().fillX().row();

                    Label killsText = new Label(L.get(
                            conditions.get(condition)),
                            BrainOutClient.Skin, "title-small");

                    killsText.setAlignment(Align.center);
                    killsText.setWrap(true);
                    content.add(killsText).width(400).expandX().fillX().row();

                    Group progress = new Group();

                    ProgressBar scoreBar = new ProgressBar(0, dc.getDurability(),
                        0.01f, false, BrainOutClient.Skin,
                        "progress-score");

                    scoreBar.setBounds(
                        0,
                        -1,
                        392,
                        ClientConstants.Menu.PlayerInfo.LABEL_HEIGHT
                    );

                    scoreBar.setValue(durability);
                    progress.addActor(scoreBar);

                    Label scoreValue = new Label(String.valueOf((int)(float)Math.ceil(durability)) +
                        " / " + (int)dc.getDurability(),
                        BrainOutClient.Skin, "title-small");

                    scoreValue.setAlignment(Align.center);
                    scoreValue.setFillParent(true);
                    progress.addActor(scoreValue);

                    BorderActor ba = new BorderActor(progress, 392);
                    ba.getCell().height(22);
                    tooltip.add(ba).expandX().fillX().row();

                    tooltip.setSize(416, 240);

                    return tooltip;
                }

            }, stage);
        }
    }

    private void tryFix()
    {
        if (BrainOutClient.ClientController.getUserProfile().getInt(Constants.User.GEARS, 0) > 0)
        {
            Menu.playSound(Menu.MenuSound.select);

            fix();
        }
        else
        {
            Menu.playSound(Menu.MenuSound.denied);
        }
    }

    private void fix()
    {
        BrainOutClient.ClientController.sendTCP(new ContentActionMsg(instrument, ContentActionMsg.Action.repair));
    }
}
