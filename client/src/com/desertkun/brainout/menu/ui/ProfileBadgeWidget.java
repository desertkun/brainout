package com.desertkun.brainout.menu.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Scaling;
import com.desertkun.brainout.BrainOut;
import com.desertkun.brainout.BrainOutClient;
import com.desertkun.brainout.ClientEnvironment;
import com.desertkun.brainout.Constants;
import com.desertkun.brainout.content.Levels;
import com.desertkun.brainout.content.ProfileBadge;
import com.desertkun.brainout.content.Shader;
import com.desertkun.brainout.content.components.AnimationComponent;
import com.desertkun.brainout.content.components.IconComponent;
import com.desertkun.brainout.content.components.InstrumentAnimationComponent;
import com.desertkun.brainout.data.instrument.InstrumentInfo;
import com.desertkun.brainout.online.UserProfile;

public class ProfileBadgeWidget extends Group
{
    private final Levels.Level level;
    private final String avatar;
    private final Color nameColor;
    private String badgeName;
    private String name;
    private int health;
    private InstrumentInfo instrumentInfo;

    public ProfileBadgeWidget(UserProfile userProfile)
    {
        this(
            userProfile.getName(),
            null,
            userProfile.getSelection(Constants.User.PROFILE_BADGE),
            userProfile.getAvatar(),
            userProfile.getLevel(Constants.User.LEVEL, 1),
            -1,
            null);
    }

    public ProfileBadgeWidget(UserProfile userProfile, ProfileBadge badge, InstrumentInfo instrumentInfo)
    {
        this( userProfile.getName(),
                null,
                badge != null ? badge.getID() : userProfile.getSelection(Constants.User.PROFILE_BADGE),
                userProfile.getAvatar(),
                userProfile.getLevel(Constants.User.LEVEL, 1),
                -1,
                instrumentInfo);
    }

    public ProfileBadgeWidget(UserProfile userProfile, ProfileBadge badge)
    {
        this( userProfile.getName(),
            null,
            badge != null ? badge.getID() : userProfile.getSelection(Constants.User.PROFILE_BADGE),
            userProfile.getAvatar(),
            userProfile.getLevel(Constants.User.LEVEL, 1),
            -1,
            null);
    }

    public ProfileBadgeWidget(String name, Color nameColor, String badgeName, String avatar,
                              int level, int health, InstrumentInfo instrumentInfo)
    {
        this.name = name;
        this.nameColor = nameColor;
        this.badgeName = badgeName;
        this.avatar = avatar;
        this.level = getLevel(level);
        this.health = health;
        this.instrumentInfo = instrumentInfo;

        Gdx.app.postRunnable(this::render);
    }

    private void render()
    {
        renderBackground();

        final int w = 384, h = 112;

        // nickname
        {
            if (nameColor != null)
            {
                try
                {
                    Image background = new Image(BrainOutClient.Skin, "profile-badge-name");
                    background.setScaling(Scaling.none);
                    background.setTouchable(Touchable.disabled);
                    background.setFillParent(true);
                    background.setColor(nameColor);
                    addActor(background);
                }
                catch (IllegalArgumentException ignored)
                {
                    //
                }
            }

            Label nickname = new Label(name, BrainOutClient.Skin, "title-small");
            nickname.setAlignment(Align.center | Align.left);
            nickname.setBounds(52, h - 40 - 6, w - 100, 40);
            nickname.setEllipsis(true);
            addActor(nickname);
        }

        // avatar
        {
            Table avatarInfo = new Table();
            fetchAvatar(avatarInfo);
            avatarInfo.setBounds(6, h - 40 - 6, 40, 40);
            addActor(avatarInfo);
        }

        if (health >= 0)
        {
            {
                Image background = new Image(BrainOutClient.Skin, "profile-badge-health");
                background.setScaling(Scaling.none);
                background.setTouchable(Touchable.disabled);
                background.setFillParent(true);
                addActor(background);
            }

            Label healthLabel = new Label(String.valueOf(health), BrainOutClient.Skin, "title-small");
            healthLabel.setAlignment(Align.center | Align.right);
            healthLabel.setBounds(w - 64, h - 40 - 6, 40, 40);
            addActor(healthLabel);
        }

        // level
        if (level != null)
        {
            Table contents = new Table();
            contents.align(Align.center);

            TextureAtlas.AtlasRegion levelRegion = BrainOutClient.getRegion(level.icon);
            if (levelRegion != null)
            {
                Image levelIcon = new Image(levelRegion);
                levelIcon.setScaling(Scaling.none);
                levelIcon.setScale(0.5f);
                levelIcon.setOrigin(35, 24);
                contents.add(levelIcon).size(70, 48).padBottom(-8 ).row();
            }

            Label levelNumber = new Label(level.toString(), BrainOutClient.Skin, "title-level");

            contents.add(levelNumber).row();

            contents.setBounds((w >> 1) - 35, 8, 70, 60);
            addActor(contents);
        }

        if (instrumentInfo != null && instrumentInfo.instrument != null)
        {

            float scale;

            InstrumentAnimationComponent iac =
                instrumentInfo.instrument.getComponentFrom(InstrumentAnimationComponent.class);

            if (iac != null)
            {
                scale = iac.getIconScale();
            }
            else
            {
                scale = 1.0f;
            }

            Shader blackShader = ((Shader) BrainOut.ContentMgr.get("shader-black"));

            InstrumentIcon instrumentIcon = new InstrumentIcon(instrumentInfo, scale, true);
            ShaderedInstrumentIcon shaderedInstrumentIcon =
                    new ShaderedInstrumentIcon(blackShader, instrumentInfo, scale, true);

            instrumentIcon.setBounds(0, 0, 192, 64);
            instrumentIcon.init();

            shaderedInstrumentIcon.setBounds(-4, -4, 192, 64);
            shaderedInstrumentIcon.init();

            Group instrument = new Group();
            instrument.setBounds(-8, 0, 192, 64);

            instrument.addActor(shaderedInstrumentIcon);
            instrument.addActor(instrumentIcon);

            addActor(instrument);
        }

        setSize(w, h);
    }

    private Levels.Level getLevel(int level)
    {
        Levels levels = BrainOutClient.ClientController.getLevels(Constants.User.LEVEL);

        if (levels == null)
        {
            return null;
        }

        return levels.getLevel(level);
    }

    private void renderBackground()
    {
        if (badgeName == null)
            badgeName = Constants.User.PROFILE_BADGE_DEFAULT;

        ProfileBadge badge = BrainOutClient.ContentMgr.get(badgeName, ProfileBadge.class);

        if (badge == null)
        {
            badge = BrainOutClient.ContentMgr.get(Constants.User.PROFILE_BADGE_DEFAULT, ProfileBadge.class);

            if (badge == null)
            {
                return;
            }
        }

        AnimationComponent anim = badge.getComponent(AnimationComponent.class);

        Actor background;

        if (anim != null)
        {
            background = new ProfileBadgeAnimation(anim.getAnimation());
        }
        else
        {
            IconComponent badgeIcon = badge.getComponent(IconComponent.class);

            if (badgeIcon == null)
                return;

            Image image = new Image(BrainOutClient.Skin, badgeIcon.getIconName());
            image.setScaling(Scaling.none);
            image.setTouchable(Touchable.disabled);
            image.setFillParent(true);

            background = image;
        }

        addActor(background);
    }

    private void fetchAvatar(Table avatarInfo)
    {
        if (avatar == null || avatar.isEmpty())
        {
            renderDefaultAvatar(avatarInfo);
            return;
        }

        Avatars.Get(avatar, (has, avatar) ->
        {
            if (has)
            {
                Image avatarImage = new Image(avatar);

                avatarImage.setScaling(Scaling.fit);

                avatarInfo.add(avatarImage).size(40, 40).row();
            }
            else
            {
                renderDefaultAvatar(avatarInfo);
            }
        });
    }

    private void renderDefaultAvatar(Table avatarInfo)
    {
        TextureRegion defaultAvatar = BrainOutClient.getRegion("default-avatar");

        if (defaultAvatar == null)
            return;

        Image image = new Image(defaultAvatar);

        avatarInfo.add(image).size(40, 40).row();
    }
}
