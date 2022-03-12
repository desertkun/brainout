package com.desertkun.brainout.menu.ui;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.NinePatchDrawable;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Scaling;
import com.desertkun.brainout.BrainOutClient;
import com.desertkun.brainout.ClientConstants;
import com.desertkun.brainout.L;
import com.desertkun.brainout.content.instrument.Weapon;

public class SkillsButton extends Button
{
    private final int skillLevel;
    private final Weapon weapon;
    private final String title;
    private final Stage stage;

    public SkillsButton(int skillLevel, Weapon weapon, String title, Stage stage)
    {
        super(BrainOutClient.Skin, "button-skills");

        this.skillLevel = skillLevel;
        this.weapon = weapon;
        this.title = title;
        this.stage = stage;

        init();
    }

    private void init()
    {
        TextureRegion region = BrainOutClient.getRegion("weapon-skill-" + skillLevel + "-small");

        if (region != null)
        {
            Image image = new Image(region);
            image.setFillParent(true);
            image.setTouchable(Touchable.disabled);
            image.setScaling(Scaling.none);
            addActor(image);
        }

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

                Table content = new Table();
                content.setBackground(new NinePatchDrawable(BrainOutClient.getNinePatch("form-default")));

                Label title = new Label(
                        L.get("MENU_SKILL_LEVEL_" + skillLevel),
                        BrainOutClient.Skin, "title-level");

                title.setAlignment(Align.center);
                title.setWrap(true);

                tooltip.add(new BorderActor(title, "form-gray")).expandX().fillX().row();
                tooltip.add(content).expand().fill().row();

                if (skillLevel < weapon.getSkills().size)
                {
                    Weapon.Skill nextSkill = weapon.getSkills().get(skillLevel);

                    TextureRegion bigIcon = BrainOutClient.getRegion(
                            "weapon-skill-" + skillLevel + "-big"
                    );

                    if (bigIcon != null)
                    {
                        Image bigIconImage = new Image(bigIcon);
                        bigIconImage.setScaling(Scaling.none);
                        content.add(bigIconImage).pad(8).expandX().fillX().row();
                    }

                    Label lockedText = new Label(L.get("MENU_TASK"),
                            BrainOutClient.Skin, "title-gray");
                    lockedText.setAlignment(Align.center);
                    lockedText.setWrap(true);
                    content.add(lockedText).expandX().fillX().row();

                    Label killsText = new Label(L.get(
                        "MENU_NEW_SKILL_LEVEL_TASK", String.valueOf(nextSkill.getKills()),
                            SkillsButton.this.title),
                        BrainOutClient.Skin, "title-small");

                    killsText.setAlignment(Align.center);
                    killsText.setWrap(true);
                    content.add(killsText).expandX().fillX().row();

                    int need = nextSkill.getKills();
                    int have = BrainOutClient.ClientController.getUserProfile().getInt(
                        weapon.getKillsStat(), 0
                    );

                    Group progress = new Group();

                    ProgressBar scoreBar = new ProgressBar(0, need,
                            1, false, BrainOutClient.Skin,
                            "progress-score");

                    scoreBar.setBounds(
                            0,
                            -1,
                            392,
                            ClientConstants.Menu.PlayerInfo.LABEL_HEIGHT
                    );

                    scoreBar.setValue(have);
                    progress.addActor(scoreBar);

                    Label scoreValue = new Label(String.valueOf(have) + " / " + need,
                            BrainOutClient.Skin, "title-small");

                    scoreValue.setAlignment(Align.center);
                    scoreValue.setFillParent(true);
                    progress.addActor(scoreValue);

                    BorderActor ba = new BorderActor(progress, 392);
                    ba.getCell().height(22);
                    tooltip.add(ba).expandX().fillX().row();
                }
                else
                {
                    TextureRegion bigIcon = BrainOutClient.getRegion(
                            "weapon-skill-" + skillLevel + "-big"
                    );

                    Image bigIconImage = new Image(bigIcon);
                    bigIconImage.setScaling(Scaling.none);
                    content.add(bigIconImage).expandX().fillX().row();

                    Label lockedText = new Label(L.get("MENU_TASK"),
                            BrainOutClient.Skin, "title-gray");
                    lockedText.setAlignment(Align.center);
                    lockedText.setWrap(true);
                    content.add(lockedText).expandX().fillX().row();

                    Label killsText = new Label(L.get(
                            "MENU_SKILL_LEVEL_MAX"),
                            BrainOutClient.Skin, "title-small");

                    killsText.setAlignment(Align.center);
                    killsText.setWrap(true);
                    content.add(killsText).padTop(8).expandX().fillX().row();
                }

                tooltip.setSize(416, 240);

                return tooltip;
            }

        }, stage);
    }
}
