package com.desertkun.brainout.menu.impl;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ActorGestureListener;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Scaling;
import com.desertkun.brainout.BrainOut;
import com.desertkun.brainout.BrainOutClient;
import com.desertkun.brainout.Constants;
import com.desertkun.brainout.L;
import com.desertkun.brainout.client.SocialController;
import com.desertkun.brainout.content.ContentLockTree;
import com.desertkun.brainout.content.ProfileBadge;
import com.desertkun.brainout.content.Shader;
import com.desertkun.brainout.content.components.AnimationComponent;
import com.desertkun.brainout.content.components.IconComponent;
import com.desertkun.brainout.menu.Menu;
import com.desertkun.brainout.menu.ui.*;
import com.desertkun.brainout.online.UserProfile;
import org.json.JSONObject;

import java.util.Comparator;


public class SwitchProfileBadgeMenu extends Menu
{
    private final PlayerProfileMenu playerProfileMenu;

    public SwitchProfileBadgeMenu(PlayerProfileMenu playerProfileMenu)
    {
        this.playerProfileMenu = playerProfileMenu;
    }

    @Override
    public Table createUI()
    {
        Table data = new Table();

        {
            Table badges = new Table();

            {
                Label title = new Label(L.get("MENU_SELECT_PROFILE_BADGE"), BrainOutClient.Skin, "title-small");
                badges.add(title).expandX().center().colspan(2).pad(32).row();
            }

            ScrollPane pane = new ScrollPane(badges, BrainOutClient.Skin, "scroll-default");
            renderBadges(badges);

            data.add(pane).expand().pad(20).fill();

            setScrollFocus(pane);
        }

        return data;
    }

    private int getProfileBadgeCompletionLevel(ProfileBadge profileBadge)
    {
        ContentLockTree.LockItem lockItem = profileBadge.getLockItem();

        if (lockItem == null)
            return -Math.abs(profileBadge.getID().hashCode());

        if (lockItem.isUnlocked(BrainOutClient.ClientController.getUserProfile()))
        {
            return -Math.abs(profileBadge.getID().hashCode());
        }

        int need = lockItem.getParam();
        int have = lockItem.getUnlockValue(BrainOutClient.ClientController.getUserProfile(), 0);

        if (need == 0)
            return 0;

        return (int)(1.0f - ((float)have / (float)need) * 1000.0f);
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

        MenuHelper.AddCloseButton(this, this::pop);
    }

    private ProfileBadge getDefaultBadge()
    {
        UserProfile profile = BrainOutClient.ClientController.getUserProfile();
        String badgeName = profile.getSelection(Constants.User.PROFILE_BADGE);
        ProfileBadge badge = BrainOutClient.ContentMgr.get(badgeName, ProfileBadge.class);

        if (badge == null)
        {
            badge = BrainOutClient.ContentMgr.get(Constants.User.PROFILE_BADGE_DEFAULT, ProfileBadge.class);
        }

        return badge;
    }

    private void renderBadges(Table contents)
    {
        ButtonGroup<Button> buttons = new ButtonGroup<>();
        buttons.setMinCheckCount(1);
        buttons.setMaxCheckCount(1);

        UserProfile profile = BrainOutClient.ClientController.getUserProfile();
        ProfileBadge currentBadge = getDefaultBadge();

        int i = 0;

        Array<ProfileBadge> badges = BrainOutClient.ContentMgr.queryContent(ProfileBadge.class);

        badges.sort(Comparator.comparingInt(this::getProfileBadgeCompletionLevel));

        Shader grayscaledShader = ((Shader) BrainOut.ContentMgr.get("shader-grayscaled-ui"));

        if (grayscaledShader == null)
            return;

        for (ProfileBadge badge : badges)
        {
            if (badge.getLockItem() != null &&
                badge.getLockItem().getVisibility() == ContentLockTree.Visibility.hidden &&
                !badge.hasItem(profile))
            {
                continue;
            }

            if (badge.hasItem(profile))
            {
                AnimationComponent badgeAnimation = badge.getComponent(AnimationComponent.class);

                Actor background;

                if (badgeAnimation != null)
                {
                    background = new ProfileBadgeAnimation(badgeAnimation.getAnimation());
                }
                else
                {
                    IconComponent badgeIcon = badge.getComponent(IconComponent.class);

                    if (badgeIcon == null)
                        continue;

                    Image image = new Image(BrainOutClient.Skin, badgeIcon.getIconName());
                    image.setScaling(Scaling.none);
                    image.setTouchable(Touchable.disabled);
                    background = image;
                }

                Button button = new Button(BrainOutClient.Skin, "button-checkable-clear");
                button.setTransform(true);
                button.add(background).size(384, 112).expand().fill();

                button.addListener(new ClickOverListener()
                {
                    @Override
                    public void clicked(InputEvent event, float x, float y)
                    {
                        if (!badge.hasItem(profile))
                            return;

                        switchBadge(badge);
                    }
                });

                buttons.add(button);
                contents.add(button).expandX().align(i % 2 == 0 ? Align.right : Align.left).pad(4);

                if (badge == currentBadge)
                {
                    button.setChecked(true);
                }

                if (badge.getLockItem() != null)
                {
                    BadgeUnlockTooltip.show(button, badge, profile, this);
                }
            }
            else
            {
                IconComponent badgeIcon = badge.getComponent(IconComponent.class);

                if (badgeIcon == null)
                    continue;

                String iconName = badgeIcon.getIconName("icon", null);
                if (iconName == null)
                    continue;

                Button group = new Button(BrainOutClient.Skin, "button-hoverable-clear");

                ShaderedImage background = new ShaderedImage(BrainOutClient.Skin, iconName, grayscaledShader);

                background.setScaling(Scaling.none);
                background.setTouchable(Touchable.disabled);
                group.add(background).size(384, 112).row();
                contents.add(group).expandX().align(i % 2 == 0 ? Align.right : Align.left).pad(4);

                if (badge.getLockItem() != null)
                {
                    ContentLockTree.LockItem lockItem = badge.getLockItem();

                    float have = lockItem.getUnlockValue(profile, 0);
                    if (have > 0)
                    {
                        float need = lockItem.getParam();

                        float value = 100.0f * (have / need);

                        ButtonProgressBar progressBar = new ButtonProgressBar(
                                (int)value, 100, BrainOutClient.Skin, "progress-parts");
                        group.add(progressBar).expandX().fillX().padTop(-2).row();
                    }

                    BadgeUnlockTooltip.show(group, badge, profile, this);
                }

                if (!BrainOut.OnlineEnabled())
                {
                    group.addListener(new ActorGestureListener()
                    {
                        @Override
                        public void tap(InputEvent event, float x, float y, int count, int button)
                        {
                            if (count == 2)
                            {
                                JSONObject args = new JSONObject();
                                args.put("content", badge.getID());

                                BrainOutClient.SocialController.sendRequest("offline_force_unlock", args,
                                        new SocialController.RequestCallback()
                                        {
                                            @Override
                                            public void success(JSONObject response)
                                            {
                                                SwitchProfileBadgeMenu.this.reset();
                                            }

                                            @Override
                                            public void error(String reason)
                                            {
                                                System.out.println(reason);
                                            }
                                        });
                            }
                        }
                    });
                }
            }

            i++;

            if (i % 2 == 0)
            {
                contents.row();
            }
        }
    }

    private void switchBadge(ProfileBadge badge)
    {
        WaitLoadingMenu waitLoadingMenu = new WaitLoadingMenu("");

        BrainOutClient.getInstance().topState().pushMenu(waitLoadingMenu);

        JSONObject args = new JSONObject();
        args.put("badge", badge.getID());

        BrainOutClient.SocialController.sendRequest("switch_profile_badge", args,
            new SocialController.RequestCallback()
        {
            @Override
            public void success(JSONObject response)
            {
                waitLoadingMenu.pop();
                pop();
                playerProfileMenu.forceProfileBadge(badge);
            }

            @Override
            public void error(String reason)
            {
                waitLoadingMenu.pop();
                pop();
            }
        });
    }

    @Override
    protected TextureRegion getBackground()
    {
        return BrainOutClient.getRegion("bg-ingame");
    }

    @Override
    public boolean lockInput()
    {
        return true;
    }

    @Override
    public boolean escape()
    {
        pop();

        return true;
    }
}
