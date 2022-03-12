package com.desertkun.brainout.menu.impl;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ActorGestureListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ObjectMap;
import com.badlogic.gdx.utils.Scaling;
import com.desertkun.brainout.BrainOut;
import com.desertkun.brainout.BrainOutClient;
import com.desertkun.brainout.Constants;
import com.desertkun.brainout.L;
import com.desertkun.brainout.client.RemoteClient;
import com.desertkun.brainout.client.SocialController;
import com.desertkun.brainout.client.states.CSGame;
import com.desertkun.brainout.content.*;
import com.desertkun.brainout.content.Skin;
import com.desertkun.brainout.content.components.AnimatedIconComponent;
import com.desertkun.brainout.content.components.IconComponent;
import com.desertkun.brainout.content.components.ItemLimitsComponent;
import com.desertkun.brainout.content.instrument.Instrument;
import com.desertkun.brainout.content.instrument.Weapon;
import com.desertkun.brainout.content.shop.InstrumentSlotItem;
import com.desertkun.brainout.content.shop.Slot;
import com.desertkun.brainout.content.shop.SlotItem;
import com.desertkun.brainout.content.upgrades.Upgrade;
import com.desertkun.brainout.data.instrument.InstrumentInfo;
import com.desertkun.brainout.gs.GameState;
import com.desertkun.brainout.managers.ContentManager;
import com.desertkun.brainout.menu.ForceTopMenu;
import com.desertkun.brainout.menu.Menu;
import com.desertkun.brainout.menu.popups.AlertPopup;
import com.desertkun.brainout.menu.popups.RichAlertPopup;
import com.desertkun.brainout.menu.ui.*;
import com.desertkun.brainout.menu.ui.Tooltip;
import com.desertkun.brainout.online.Clan;
import com.desertkun.brainout.online.PlayerRights;
import com.desertkun.brainout.online.UserProfile;
import org.anthillplatform.runtime.services.LoginService;
import org.anthillplatform.runtime.services.SocialService;
import org.json.JSONObject;

import java.util.Comparator;
import java.util.TimerTask;

public abstract class       PlayerProfileMenu extends Menu
{
    private final boolean limited;

    private Table data;
    private UserProfile profile;
    private boolean even;
    private Table stats;
    private int topSkills;
    private int topSkillsMax;

    private int achievementsDone;
    private int achievementsTotal;
    private int cntBig, cntSmall;
    private ProfileBadge forcedProfileBadge;

    public PlayerProfileMenu(boolean limited)
    {
        this.limited = limited;
    }

    public PlayerProfileMenu()
    {
        this(false);
    }

    public interface ProfileCallback
    {
        void received(boolean success, UserProfile userProfile);
    }

    private void renderButtons()
    {
        if (BrainOut.OnlineEnabled())
        {
            Table leftButtons = MenuHelper.AddLeftButtonsContainers(this);

            String steamId = getSteamID();

            if (steamId != null && !isLimited())
            {
                Button btn = new Button(BrainOutClient.Skin, "button-notext");

                btn.addListener(new ClickOverListener()
                {
                    @Override
                    public void clicked(InputEvent event, float x, float y)
                    {
                        Menu.playSound(MenuSound.select);

                        BrainOutClient.Env.openURI("http://steamcommunity.com/profiles/" + steamId);
                    }
                });

                Image image = new Image(BrainOutClient.getRegion("steam-icon"));
                image.setScaling(Scaling.none);
                btn.add(image).expand().fill();

                Label title = new Label(L.get("MENU_ACCOUNT_STEAM"), BrainOutClient.Skin, "title-small");
                title.setAlignment(Align.center);

                leftButtons.add(btn).size(64, 64).padRight(10);
                leftButtons.add(title).left().row();
            }

            if (hasComplaintsButton() && !isLimited())
            {
                Button btn = new Button(BrainOutClient.Skin, "button-notext");

                btn.addListener(new ClickOverListener()
                {
                    @Override
                    public void clicked(InputEvent event, float x, float y)
                    {
                        Menu.playSound(MenuSound.select);

                        reportPlayer();
                    }
                });

                Image image = new Image(BrainOutClient.getRegion("report-icon"));
                image.setScaling(Scaling.none);
                btn.add(image).expand().fill();

                Label title = new Label(L.get("MENU_REPORT"), BrainOutClient.Skin, "title-small");
                title.setAlignment(Align.center);

                leftButtons.add(btn).size(64, 64).padRight(10);
                leftButtons.add(title).left().row();
            }

            RemoteClient r = BrainOutClient.ClientController.getMyRemoteClient();
            if (r != null && (r.getRights() == PlayerRights.mod || r.getRights() == PlayerRights.admin))
            {
                Button btn = new Button(BrainOutClient.Skin, "button-notext");

                btn.addListener(new ClickOverListener()
                {
                    @Override
                    public void clicked(InputEvent event, float x, float y)
                    {
                        Menu.playSound(MenuSound.select);

                        pushMenu(new BanPlayerMenu(getAccountID()));
                    }
                });

                Image image = new Image(BrainOutClient.getRegion("report-icon"));
                image.setScaling(Scaling.none);
                btn.add(image).expand().fill();

                Label title = new Label("Ban", BrainOutClient.Skin, "title-medium-red");
                title.setAlignment(Align.center);

                leftButtons.add(btn).size(64, 64).padRight(10);
                leftButtons.add(title).left().row();
            }

            if (canInvite() && !isLimited())
            {
                Button btn = new Button(BrainOutClient.Skin, "button-notext");

                btn.addListener(new ClickOverListener()
                {
                    @Override
                    public void clicked(InputEvent event, float x, float y)
                    {
                        Menu.playSound(MenuSound.select);

                        invitePlayer();
                    }
                });

                Image image = new Image(BrainOutClient.getRegion("icon-invite"));
                image.setScaling(Scaling.none);
                btn.add(image).expand().fill();

                Label title = new Label(L.get("MENU_CLAN_INVITE"), BrainOutClient.Skin, "title-small");
                title.setAlignment(Align.center);

                leftButtons.add(btn).size(64, 64).padRight(10);
                leftButtons.add(title).left().row();

            }
        }


        Table rightButtons = MenuHelper.AddButtonsContainers(this);

        {
            TextButton btn = new TextButton(L.get("MENU_CLOSE"), BrainOutClient.Skin, "button-default");

            btn.addListener(new ClickOverListener()
            {
                @Override
                public void clicked(InputEvent event, float x, float y)
                {
                    Menu.playSound(MenuSound.select);

                    close();
                }
            });

            rightButtons.add(btn).expandX().fillX().height(64).row();
        }

        if (BrainOut.OnlineEnabled())
        {
            if (!isLimited())
            {
                TextButton btn = new TextButton(L.get("MENU_TOP_100"), BrainOutClient.Skin, "button-default");

                btn.addListener(new ClickOverListener()
                {
                    @Override
                    public void clicked(InputEvent event, float x, float y)
                    {
                        Menu.playSound(MenuSound.select);

                        GameState gs = getGameState();

                        if (gs == null)
                            return;

                        close();

                        gs.pushMenu(new Top100Menu());
                    }
                });

                rightButtons.add(btn).expandX().fillX().height(64).row();
            }
        }
    }

    private void invitePlayer()
    {
        if (!canInvite())
            return;

        WaitLoadingMenu waitLoadingMenu = new WaitLoadingMenu("");
        pushMenu(waitLoadingMenu);

        JSONObject args = new JSONObject();

        args.put("account_id", getAccountID());

        BrainOutClient.SocialController.sendRequest("invite_to_clan", args,
            new SocialController.RequestCallback()
        {
            @Override
            public void success(JSONObject response)
            {
                GameState gs = getGameState();

                if (gs == null)
                    return;

                waitLoadingMenu.pop();
                PlayerProfileMenu.this.reset();

                BrainOutClient.SocialController.addOutgoingInviteRequest(getAccountID());
                gs.pushMenu(new RichAlertPopup(L.get("MENU_CLAN_INVITE"), L.get("MENU_CLAN_INVITE_SENT")));
            }

            @Override
            public void error(String reason)
            {
                GameState gs = getGameState();

                if (gs == null)
                    return;

                waitLoadingMenu.pop();
                gs.pushMenu(new AlertPopup(L.get(reason)));
            }
        });
    }

    private boolean canInvite()
    {
        if (BrainOutClient.SocialController.hasOutgoingInviteRequest(getAccountID()))
            return false;

        Clan myClan = BrainOutClient.SocialController.getMyClan();

        if (profile.isParticipatingClan() && myClan != null && profile.getClanId().equals(myClan.getId()))
            return false;

        String myAccount = BrainOutClient.ClientController.getMyAccount();

        if (!myAccount.equals(getAccountID()) &&
                myClan != null && myClan.getJoinMethod() == SocialService.Group.JoinMethod.invite &&
                !myClan.getMembers().containsKey(getAccountID()))
        {
            Clan.ClanMember myMember = myClan.getMembers().get(myAccount);

            if (myMember != null && myMember.hasPermission("send_invite"))
            {
                return true;
            }
        }

        return false;
    }

    private void reportPlayer()
    {
        close();

        Gdx.app.postRunnable(() -> pushMenu(new ReportPlayerPopup(getAccountID(), profile.getName())));
    }

    private void close()
    {
        pop();
    }

    @Override
    protected TextureRegion getBackground()
    {
        return BrainOutClient.getRegion("bg-ingame");
    }

    @Override
    protected MenuAlign getMenuAlign()
    {
        return MenuAlign.fill;
    }

    protected abstract void receive(ProfileCallback callback);

    protected abstract String getSteamID();
    protected abstract String getAccountID();
    protected abstract boolean hasComplaintsButton();

    @Override
    public Table createUI()
    {
        data = new Table();

        data.add(new LoadingBlock()).pad(32);

        return data;
    }

    private void received(boolean success, UserProfile userProfile)
    {
        Gdx.app.postRunnable(() ->
        {
            if (success)
            {
                PlayerProfileMenu.this.profile = userProfile;

                renderButtons();
                renderProfile();
            }
            else
            {
                MenuHelper.AddCloseButton(PlayerProfileMenu.this, PlayerProfileMenu.this::close);
            }
        });
    }

    private void renderProfile()
    {
        data.clear();

        Table contents = new Table();
        contents.align(Align.top);

        renderContents(contents);

        ScrollPane scrollPane = new ScrollPane(contents, BrainOutClient.Skin, "scroll-default");
        scrollPane.setScrollingDisabled(true, false);
        scrollPane.setFadeScrollBars(false);

        setScrollFocus(scrollPane);

        data.add(scrollPane).expand().fill().pad(20).row();
    }

    private void renderContents(Table contents)
    {
        // badge
        {
            String myAccount = BrainOutClient.ClientController.getMyAccount();

            String accountId = getAccountID();

            Slot primarySlot = BrainOutClient.ContentMgr.get("slot-primary", Slot.class);

            if (accountId != null && accountId.equals(myAccount))
            {
                Button button = new Button(BrainOutClient.Skin, "button-hoverable-clear");

                InstrumentInfo instrumentInfo = null;

                if (primarySlot != null &&  BrainOutClient.ClientController.getState(CSGame.class) != null)
                {
                    SlotItem.Selection selection = BrainOutClient.ClientController.getState(CSGame.class).getShopCart().
                        getItem(primarySlot);

                    if (selection instanceof InstrumentSlotItem.InstrumentSelection)
                    {
                        InstrumentSlotItem.InstrumentSelection isi =
                            ((InstrumentSlotItem.InstrumentSelection) selection);

                        instrumentInfo = isi.getInfo();
                    }
                }

                ProfileBadgeWidget avatarInfo = new ProfileBadgeWidget(profile, forcedProfileBadge, instrumentInfo);

                button.add(avatarInfo).size(384, 112).row();

                button.addListener(new ClickOverListener()
                {
                    @Override
                    public void clicked(InputEvent event, float x, float y)
                    {
                        Menu.playSound(MenuSound.select);

                        pushMenu(new SwitchProfileBadgeMenu(PlayerProfileMenu.this));
                    }
                });

                contents.add(button).pad(10).row();
            }
            else
            {
                InstrumentInfo instrumentInfo = null;

                Layout layout = profile.getLayout();

                if (primarySlot != null)
                {
                    String key = (layout != null && !layout.getKey().isEmpty()) ? "-" + layout.getKey() : "";
                    String slotId = profile.getSelection(primarySlot.getID() + key);
                    InstrumentSlotItem slotItem = BrainOutClient.ContentMgr.get(slotId, InstrumentSlotItem.class);

                    if (slotItem != null)
                    {
                        InstrumentSlotItem.InstrumentSelection isi = slotItem.getSelection();
                        isi.init(profile);

                        instrumentInfo = isi.getInfo();
                    }
                }

                ProfileBadgeWidget avatarInfo = new ProfileBadgeWidget(profile, null, instrumentInfo);
                contents.add(avatarInfo).size(384, 112).pad(10).row();
            }
        }

        // account id
        {
            String accountId = getAccountID();

            if (accountId != null)
            {
                Label nickname = new Label("id " + String.valueOf(accountId), BrainOutClient.Skin, "title-gray");
                contents.add(nickname).pad(10).padTop(0).row();
            }
        }

        // clan
        {
            if (profile.isParticipatingClan() && !isLimited())
            {
                Table clanInfo = new Table();

                Label skinTitle = new Label(L.get("MENU_CLAN"),
                        BrainOutClient.Skin, "title-small");
                skinTitle.setAlignment(Align.center);
                clanInfo.add(new BorderActor(skinTitle, "form-gray")).height(32).expandX().fillX().row();

                Button button = new Button(BrainOutClient.Skin, "button-notext");
                button.setBackground("form-default");

                Image avatarImage;

                if (!profile.getClanAvatar().isEmpty())
                {
                    avatarImage = new Image();

                    Avatars.Get(profile.getClanAvatar(),
                        (has, avatar) ->
                    {
                        if (has)
                        {
                            avatarImage.setDrawable(new TextureRegionDrawable(new TextureRegion(avatar)));
                        }
                        else
                        {
                            avatarImage.setDrawable(BrainOutClient.Skin, "default-avatar");
                        }
                    });
                }
                else
                {
                    avatarImage = new Image(BrainOutClient.Skin, "default-avatar");
                }

                button.add(avatarImage).size(60, 60);

                button.addListener(new ClickOverListener()
                {
                    @Override
                    public void clicked(InputEvent event, float x, float y)
                    {
                        Menu.playSound(MenuSound.select);

                        popMeAndPushMenu(new ClanMenu(profile.getClanId()));
                    }
                });

                clanInfo.add(button).expandX().fillX().row();

                contents.add(clanInfo).size(192, 96).pad(32).row();
            }
        }

        // favorite weapon
        {
            renderFavoriteWeapons(contents);
        }

        // medals
        {
            // 832 x 128

             Array<Medal> medals = BrainOutClient.ContentMgr.queryContent(Medal.class,
                 check ->
             {
                 if (BrainOut.OnlineEnabled() && !check.hasItem(profile))
                     return false;

                 ItemLimitsComponent cmp = check.getComponent(ItemLimitsComponent.class);

                 if (cmp != null)
                 {
                     return cmp.getLimits().passes(profile);
                 }

                 return true;
             });

             if (medals.size > 0)
             {
                 Table medalsPad = new Table();

                 {
                     Label header = new Label(L.get("MENU_MEDALS"), BrainOutClient.Skin, "title-yellow");
                     header.setAlignment(Align.center);
                     medalsPad.add(new BorderActor(header, "form-dark-blue")).expandX().fillX().row();
                 }

                 {
                     Table medalItems = new Table();

                     for (Medal medal : medals)
                     {
                         IconComponent medalIcon = medal.getComponent(IconComponent.class);

                         if (medalIcon != null)
                         {
                             TextureRegion icon = medalIcon.getIcon();

                             if (icon != null)
                             {
                                 Image image = new Image(icon);

                                 Tooltip.RegisterStandardToolTip(image,
                                     medal.getTitle().get(), medal.getDescription().get(), this);

                                 medalItems.add(image).padLeft(8).padRight(8);
                             }

                             continue;
                         }

                         AnimatedIconComponent animatedIconComponent = medal.getComponent(AnimatedIconComponent.class);

                         if (animatedIconComponent != null)
                         {
                             Image image = new Image();

                             animatedIconComponent.setupImage(image);

                             Tooltip.RegisterStandardToolTip(image,
                                     medal.getTitle().get(), medal.getDescription().get(), this);

                             medalItems.add(image).padLeft(8).padRight(8);
                         }
                     }

                     ScrollPane medalPane = new ScrollPane(medalItems, BrainOutClient.Skin, "scroll-default");
                     BorderActor borderActor = new BorderActor(medalPane, "border-dark-blue");
                     medalsPad.add(borderActor).height(128).expandX().fillX().row();

                 }

                 contents.add(medalsPad).width(832).pad(10).row();
             }
        }

        // stats itself
        {
            Table stats = new Table();
            renderStats(stats);
            contents.add(stats).width(832).fillX().pad(10).padLeft(32).padRight(32).row();
        }

        // achievements
        {
            Table achievementsBig = new Table();
            Table achievementsSmall = new Table();
            renderAchievements(achievementsBig, achievementsSmall);

            {
                Table title = new Table();

                Label header = new Label(L.get("MENU_ACHIEVEMENTS"), BrainOutClient.Skin, "title-yellow");
                Label value = new Label(
                    String.valueOf(achievementsDone) + " / " + achievementsTotal,
                    BrainOutClient.Skin, "title-small");

                title.add(header).padRight(10);
                title.add(value);

                contents.add(title).padTop(10).row();
            }

            contents.add(achievementsBig).expandX().fillX().pad(10).padLeft(32).padRight(32).row();
            contents.add(achievementsSmall).expandX().fillX().pad(10).padLeft(32).padRight(32).row();
        }
    }

    private int getAchievementCompletionLevel(Achievement achievement)
    {
        ContentLockTree.LockItem lockItem = achievement.getLockItem();

        if (lockItem == null)
            return 0;

        if (lockItem.isUnlocked(profile))
        {
            return -5000;
        }

        int need = lockItem.getParam();
        int have = lockItem.getUnlockValue(profile, 0);

        if (need == 0)
            return 0;

        return (int)((1.0f - (float)have / (float)need) * 1000.0f);
    }

    private void renderAchievements(Table achievementsBig, Table achievementsSmall)
    {
        achievementsBig.clear();
        achievementsSmall.clear();

        achievementsTotal = 0;
        achievementsDone = 0;
        cntBig = 0; cntSmall = 0;

        TextureAtlas.AtlasRegion empty = BrainOutClient.getRegion("achievement-empty");

        Array<Achievement> achievements = BrainOutClient.ContentMgr.queryContent(Achievement.class, check ->
            !check.isHide());

        achievements.sort(Comparator.comparingInt(this::getAchievementCompletionLevel));

        for (Achievement achievement : achievements)
        {
            if (achievement.isHide())
                return;

            IconComponent iconComponent = achievement.getComponent(IconComponent.class);

            if (iconComponent == null)
                return;

            boolean locked = achievement.isLocked(profile) && !profile.hasItem(achievement);

            TextureRegion textureRegion;

            if (locked)
            {
                textureRegion = iconComponent.getIcon("icon-locked", empty);
            }
            else
            {
                textureRegion = iconComponent.getIcon();
            }

            if (textureRegion == null)
                return;

            boolean big = textureRegion.getRegionWidth() > 64;

            int cnt, rowCnt;
            Table cntTable;

            if (big)
            {
                cntBig++;
                cnt = cntBig;
                rowCnt = 4;
                cntTable = achievementsBig;
            }
            else
            {
                cntSmall++;
                cnt = cntSmall;
                rowCnt = 10;
                cntTable = achievementsSmall;
            }

            achievementsTotal++;

            Table entry = new Table();

            Image achievementImage = new Image(textureRegion);
            entry.add(achievementImage).row();

            if (!locked)
            {
                achievementsDone++;
            }

            if (locked)
            {
                UnlockTooltip.show(entry, achievement, profile, this);

                if (big)
                {
                    ContentLockTree.LockItem lockItem = achievement.getLockItem();

                    float have = lockItem.getUnlockValue(profile, 0);
                    if (have > 0)
                    {
                        float need = lockItem.getParam();
                        float value = 100.0f * (have / need);
                        ButtonProgressBar progressBar = new ButtonProgressBar(
                                (int) value, 100, BrainOutClient.Skin, "progress-parts");
                        entry.add(progressBar).expandX().fillX().padTop(-2).row();
                    }

                }

                entry.addListener(new ActorGestureListener()
                {
                    @Override
                    public void tap(InputEvent event, float x, float y, int count, int button)
                    {
                        if (count == 2)
                        {
                            JSONObject args = new JSONObject();
                            args.put("content", achievement.getID());

                            BrainOutClient.SocialController.sendRequest("offline_force_unlock", args,
                                new SocialController.RequestCallback()
                            {
                                @Override
                                public void success(JSONObject response)
                                {
                                    BrainOutClient.Timer.schedule(new TimerTask()
                                    {
                                        @Override
                                        public void run()
                                        {
                                            Gdx.app.postRunnable(PlayerProfileMenu.this::reset);
                                        }
                                    }, 150);
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
            else
            {
                Tooltip.RegisterStandardToolTip(entry,
                    achievement.getTitle().get(),
                    achievement.getLockItem().getUnlockTitle(achievement.getLockItem().getParam()), this);
            }

            if (cnt % rowCnt == 0)
            {
                cntTable.add(entry).pad(4).padLeft(10).padRight(10).row();
            }
            else
            {
                cntTable.add(entry).pad(4).padLeft(10).padRight(10);
            }
        }
    }

    private void renderStats(Table stats)
    {
        this.stats = stats;

        int timeSpent = (int)(float) profile.getStats().get(Constants.Stats.TIME_SPENT, 0.0f);
        float efficiency = profile.getStats().get(Constants.Stats.EFFICIENCY, 0.0f);

        topSkills = 0;
        topSkillsMax = 0;

        BrainOutClient.ContentMgr.queryContentGen(InstrumentSlotItem.class, slotItem ->
        {
            Instrument instrument = slotItem.getInstrument();

            if (!(instrument instanceof Weapon))
                return;

            if (!slotItem.hasItem(profile))
                return;

            Weapon weapon = ((Weapon) instrument);

            float skills = profile.getStats().get(weapon.getSkillStat(), 0.0f);

            if (skills >= 3)
            {
                topSkills++;
            }

            topSkillsMax++;
        });

        addStat(L.get("MENU_STATS_TIME_SPENT"), "stats-time-spent", String.valueOf(timeSpent) + " min");

        if (efficiency > 0)
        {
            addStat(L.get("MENU_STATS_EFFICIENCY"), "stats-kpd", efficiency);
        }

        addStat(L.get("MENU_STATS_KILLS"), "stats-kills",
            (int)(float) profile.getStats().get(Constants.Stats.KILLS, 0.0f));
        addStat(L.get("MENU_STATS_DEATHS"), "stats-deaths",
            (int)(float) profile.getStats().get(Constants.Stats.DEATHS, 0.0f));
        addStat(L.get("MENU_STATS_DOUBLE_KILLS"), "stats-double-kills",
            (int)(float) profile.getStats().get("double-kills", 0.0f));
        addStat(L.get("MENU_STATS_TRIPLE_KILLS"), "stats-triple-kills",
            (int)(float) profile.getStats().get("triple-kills", 0.0f));
        addStat(L.get("MENU_STATS_HEADSHOTS"), "stats-headshots",
            (int)(float) profile.getStats().get("headshots", 0.0f));
        addStat(L.get("MENU_STATS_KNIFE_KILLS"), "stats-kills-from-weapon-knife",
            (int)(float) profile.getStats().get("kills-from-weapon-knife", 0.0f));
        addStat(L.get("MENU_STATS_GAMES_WON"), "stats-games-won",
            (int)(float) profile.getStats().get(Constants.Stats.GAMES_WON, 0.0f));
        addStat(L.get("MENU_STATS_GAMES_LOST"), "stats-games-lost",
            (int)(float) profile.getStats().get(Constants.Stats.GAMES_LOST, 0.0f));
        addStat(L.get("MENU_STATS_DESERTED_TIMES"), "stats-icon-desert",
            (int)(float) profile.getStats().get("total-deserts", 0.0f));
        addStat(L.get("MENU_STATS_TOTAL_ROUNDS_WASTED"), "stats-icon-shoots",
            (int)(float) profile.getStats().get("shots", 0.0f));
        addStat(L.get("MENU_STATS_TROPHIES_COLLECTED"), "stats-icon-trophy",
            (int)(float) profile.getStats().get("trophies-picked", 0.0f));
        addStat(L.get("MENU_STATS_POINTS_CAPTURED"), "stats-icon-flags",
            (int)(float) profile.getStats().get("capture-flags", 0.0f));
        addStat(L.get("MENU_STATS_WEAPONS_SKILL_3"), "stats-icon-profy",
            String.valueOf(topSkills) + " / " + topSkillsMax);
        addStat(L.get("MENU_STATS_CHALLENGES_COMPLETED"), "stats-icon-challenges",
            (int)(float) profile.getStats().get("events-completed", 0.0f));
        addStat(L.get("MENU_STATS_CONTAINERS_OPENED"), "stats-icon-containers",
            (int)(float) profile.getStats().get("cases-opened", 0.0f));
    }

    private void addStat(String title, String icon, int value)
    {
        addStat(title, icon, String.valueOf(value));
    }

    private void addStat(String title, String icon, float value)
    {
        addStat(title, icon, String.valueOf(value));
    }

    private void addStat(String title, String icon, String value)
    {
        even = !even;

        Table row = new Table(BrainOutClient.Skin);
        row.setBackground(even ? "form-dark-blue" : "border-dark-blue");

        TextureRegion iconRegion = BrainOutClient.getRegion(icon);
        if (iconRegion != null)
        {
            Image iconImage = new Image(iconRegion);
            iconImage.setScaling(Scaling.none);
            row.add(iconImage).height(28).padRight(10).padTop(-2).padBottom(-2);
        }
        else
        {
            row.add().height(28).padRight(10).padTop(-2).padBottom(-2);
        }

        Label titleLabel = new Label(title, BrainOutClient.Skin, "title-yellow");
        row.add(titleLabel).expandX().padTop(-2).padBottom(-2).left();

        Label valueLabel = new Label(value, BrainOutClient.Skin, "title-small");
        row.add(valueLabel).expandX().right().padRight(10).padTop(-2).padBottom(-2).row();

        stats.add(row).expandX().fillX().row();
    }

    private void renderFavoriteWeapons(Table contents)
    {
        Array<InstrumentSlotItem> primaryWeapons = BrainOutClient.ContentMgr.queryContentTpl(
            InstrumentSlotItem.class, check ->
        {
            if (!profile.hasItem(check))
                return false;

            if (check.getSlot() == null || !check.getSlot().getID().equals("slot-primary"))
                return false;

            Instrument instrument = check.getInstrument();

            if (!(instrument instanceof Weapon))
                return false;

            Weapon weapon = ((Weapon) instrument);

            return profile.getStats().get(weapon.getKillsStat(), 0.f) > 0.f;
        });

        Array<InstrumentSlotItem> secondaryWeapons = BrainOutClient.ContentMgr.queryContentTpl(
            InstrumentSlotItem.class, check ->
        {
            if (!profile.hasItem(check))
                return false;

            if (check.getSlot() == null || !check.getSlot().getID().equals("slot-secondary"))
                return false;

            Instrument instrument = check.getInstrument();

            if (!(instrument instanceof Weapon))
                return false;

            Weapon weapon = ((Weapon) instrument);

            return profile.getStats().get(weapon.getKillsStat(), 0.f) > 0.f;
        });

        if (primaryWeapons.size > 0 && secondaryWeapons.size > 0)
        {

            primaryWeapons.sort((o1, o2) ->
            {
                Weapon i1 = ((Weapon) o1.getInstrument());
                Weapon i2 = ((Weapon) o2.getInstrument());

                float st1 = profile.getStats().get(i1.getKillsStat(), 0.f);
                float st2 = profile.getStats().get(i2.getKillsStat(), 0.f);

                return (int)(Math.signum(st2 - st1));
            });

            secondaryWeapons.sort((o1, o2) ->
            {
                Weapon i1 = ((Weapon) o1.getInstrument());
                Weapon i2 = ((Weapon) o2.getInstrument());

                float st1 = profile.getStats().get(i1.getKillsStat(), 0.f);
                float st2 = profile.getStats().get(i2.getKillsStat(), 0.f);

                return (int)(Math.signum(st2 - st1));
            });

            InstrumentSlotItem primary = primaryWeapons.get(0);
            InstrumentSlotItem secondary = secondaryWeapons.get(0);

            Table favoriteWeapons = new Table();

            {
                Label header = new Label(L.get("MENU_FAVORITE_WEAPON"), BrainOutClient.Skin, "title-yellow");
                header.setAlignment(Align.center);
                favoriteWeapons.add(new BorderActor(header, "form-dark-blue")).colspan(2).expandX().fillX().row();
            }

            {
                Weapon weapon = ((Weapon) primary.getInstrument());

                InstrumentInfo info = new InstrumentInfo();
                info.instrument = weapon;
                info.skin = primary.getDefaultSkin();

                String skinSelection = primary.getSkinSelection(profile);
                if (skinSelection != null)
                {
                    Skin skin = BrainOut.ContentMgr.get(skinSelection, Skin.class);

                    if (primary.getSkins().contains(skin, true))
                    {
                        info.skin = skin;
                    }
                }

                for (ObjectMap.Entry<String, Array<Upgrade>> entry : primary.getUpgrades())
                {
                    String slot = entry.key;
                    String upgradeSelection = primary.getUpgradeSelection(profile, slot);

                    if (upgradeSelection != null)
                    {
                        Upgrade upgrade = BrainOutClient.ContentMgr.get(upgradeSelection, Upgrade.class);

                        if (upgrade == null)
                            continue;

                        if (entry.value.contains(upgrade, true))
                        {
                            info.upgrades.put(slot, upgrade);
                        }
                    }
                }

                InstrumentIcon instrumentIcon = new InstrumentIcon(info, 2.0f, false);
                instrumentIcon.init();

                BorderActor borderActor = new BorderActor(instrumentIcon, "border-dark-blue");

                int kills = profile.getStats().get(info.instrument.getKillsStat(), 0.0f).intValue();

                if (kills > 0)
                {
                    WidgetGroup killCountGroup = new WidgetGroup();

                    Image killBg = new Image(BrainOutClient.getRegion("label-kills"));
                    killBg.setScaling(Scaling.none);
                    killBg.setFillParent(true);
                    killCountGroup.addActor(killBg);

                    Label killCount = new Label(String.valueOf(
                            kills),
                            BrainOutClient.Skin,
                            "title-small"
                    );

                    killCount.setAlignment(Align.center);
                    killCount.setFillParent(true);
                    killCountGroup.addActor(killCount);

                    Table killCountParent = new Table();
                    killCountParent.align(Align.left | Align.top);
                    killCountParent.setTouchable(Touchable.childrenOnly);
                    killCountParent.setFillParent(true);
                    killCountParent.add(killCountGroup).size(64, 32).pad(10).padLeft(0).row();

                    borderActor.addActor(killCountParent);
                }

                favoriteWeapons.add(borderActor).expandY().size(384, 128).fill();
            }

            {
                Weapon weapon = ((Weapon) secondary.getInstrument());

                InstrumentInfo info = new InstrumentInfo();
                info.instrument = weapon;
                info.skin = secondary.getDefaultSkin();

                String skinSelection = secondary.getSkinSelection(profile);
                if (skinSelection != null)
                {
                    Skin skin = BrainOut.ContentMgr.get(skinSelection, Skin.class);

                    if (secondary.getSkins().contains(skin, true))
                    {
                        info.skin = skin;
                    }
                }

                for (ObjectMap.Entry<String, Array<Upgrade>> entry : secondary.getUpgrades())
                {
                    String slot = entry.key;
                    String upgradeSelection = secondary.getUpgradeSelection(profile, slot);

                    if (upgradeSelection != null)
                    {
                        Upgrade upgrade = BrainOutClient.ContentMgr.get(upgradeSelection, Upgrade.class);

                        if (upgrade == null)
                            continue;

                        if (entry.value.contains(upgrade, true))
                        {
                            info.upgrades.put(slot, upgrade);
                        }
                    }
                }

                InstrumentIcon instrumentIcon = new InstrumentIcon(info, 2.0f, false);
                instrumentIcon.init();

                BorderActor borderActor = new BorderActor(instrumentIcon, "border-dark-blue");

                int kills = profile.getStats().get(info.instrument.getKillsStat(), 0.0f).intValue();

                if (kills > 0)
                {
                    WidgetGroup killCountGroup = new WidgetGroup();

                    Image killBg = new Image(BrainOutClient.getRegion("label-kills"));
                    killBg.setScaling(Scaling.none);
                    killBg.setFillParent(true);
                    killCountGroup.addActor(killBg);

                    Label killCount = new Label(String.valueOf(
                            kills),
                            BrainOutClient.Skin,
                            "title-small"
                    );

                    killCount.setAlignment(Align.center);
                    killCount.setFillParent(true);
                    killCountGroup.addActor(killCount);

                    Table killCountParent = new Table();
                    killCountParent.align(Align.left | Align.top);
                    killCountParent.setTouchable(Touchable.childrenOnly);
                    killCountParent.setFillParent(true);
                    killCountParent.add(killCountGroup).size(64, 32).pad(10).padLeft(0).row();

                    borderActor.addActor(killCountParent);
                }

                favoriteWeapons.add(borderActor).expand().fill().height(128).row();
            }

            contents.add(favoriteWeapons).width(580).pad(10).row();
        }
    }

    private Levels.Level getLevel()
    {
        Levels levels = BrainOutClient.ClientController.getLevels(Constants.User.LEVEL);

        if (levels == null)
        {
            return null;
        }

        return levels.getLevel(profile.getLevel(
            Constants.User.LEVEL, 1
        ));
    }

    @Override
    public void onInit()
    {
        super.onInit();

        receive(this::received);
    }

    @Override
    public void reset()
    {
        super.reset();

        receive(this::received);
    }

    @Override
    public boolean escape()
    {
        pop();

        return true;
    }

    @Override
    public boolean lockInput()
    {
        return true;
    }

    public boolean isLimited()
    {
        return limited;
    }

    public void forceProfileBadge(ProfileBadge badge)
    {
        forcedProfileBadge = badge;
        reset();
    }
}
