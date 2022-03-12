package com.desertkun.brainout.menu.ui;

import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Scaling;
import com.desertkun.brainout.*;
import com.desertkun.brainout.common.enums.NotifyAward;
import com.desertkun.brainout.common.enums.NotifyReason;
import com.desertkun.brainout.common.enums.data.*;
import com.desertkun.brainout.content.*;
import com.desertkun.brainout.content.components.AnimatedIconComponent;
import com.desertkun.brainout.content.components.AnimationComponent;
import com.desertkun.brainout.content.components.IconComponent;
import com.desertkun.brainout.content.components.UnlockSoundComponent;
import com.desertkun.brainout.content.consumable.impl.InstrumentConsumableItem;
import com.desertkun.brainout.content.quest.Quest;
import com.desertkun.brainout.content.shop.InstrumentSlotItem;
import com.desertkun.brainout.data.ClientMap;
import com.desertkun.brainout.data.Map;
import com.desertkun.brainout.data.battlepass.BattlePassTaskData;
import com.desertkun.brainout.data.consumable.ConsumableRecord;
import com.desertkun.brainout.data.instrument.InstrumentInfo;
import com.desertkun.brainout.data.interfaces.PointLaunchData;
import com.desertkun.brainout.events.NotifyEvent;
import com.desertkun.brainout.gs.GameState;
import com.desertkun.brainout.gs.actions.MenuAction;
import com.desertkun.brainout.gs.actions.WaitAction;
import com.desertkun.brainout.menu.Menu;
import com.desertkun.brainout.menu.popups.AlertPopup;
import com.desertkun.brainout.online.ClientBattlePassEvent;
import com.desertkun.brainout.online.ClientEvent;
import com.desertkun.brainout.utils.ContentImage;

public class Notifications
{
    private static void processMessageNotification(int eventAmount, String notice, NotifyReason eventReason,
        NotifyAward notifyAward, NotifyData eventData, String title, boolean finalPositive, Actor defaultAdditional)
    {
        int amount = eventAmount;

        if (notice != null)
        {
            switch (eventReason)
            {
                case duelLost:
                {
                    Menu.playSound(Menu.MenuSound.chipFail);
                    break;
                }
                case duelWon:
                {
                    Menu.playSound(Menu.MenuSound.chipSuccess);
                    break;
                }
            }

            switch (notifyAward)
            {
                case trophy:
                {
                    Menu.playSound(Menu.MenuSound.trophy);
                    break;
                }
                case candies:
                {
                    Menu.playSound(Menu.MenuSound.trophy);
                    break;
                }
                case ch:
                {
                    TextureAtlas.AtlasRegion chRegion =
                            BrainOutClient.getRegion("icon-big-gold");

                    if (chRegion == null)
                        return;

                    Image image  = new Image(chRegion);
                    image.setScaling(Scaling.none);

                    Table additional = new Table();
                    additional.setFillParent(true);
                    additional.align(Align.top);

                    additional.add(image).padTop(48).expandX().fillX().row();

                    addLabelNotice(notice, additional);

                    Menu.playSound(Menu.MenuSound.skillpointsEarned);

                    break;
                }
                case skillpoints:
                {
                    TextureAtlas.AtlasRegion skillptsRegion =
                            BrainOutClient.getRegion("skillpoints-big");

                    if (skillptsRegion == null)
                        return;

                    Image image  = new Image(skillptsRegion);
                    image.setScaling(Scaling.none);

                    Table additional = new Table();
                    additional.setFillParent(true);
                    additional.align(Align.top);

                    additional.add(image).padTop(48).expandX().fillX().row();

                    addLabelNotice(notice, additional);

                    Menu.playSound(Menu.MenuSound.skillpointsEarned);

                    break;
                }
                case ru:
                {
                    Table additional = new Table();
                    additional.setFillParent(true);
                    additional.align(Align.top);

                    {
                        Table image = new Table();

                        if (eventData instanceof ConsumableND)
                        {
                            ConsumableRecord record = ((ConsumableND)eventData).item;

                            if (record.getItem() instanceof InstrumentConsumableItem)
                            {
                                InstrumentConsumableItem ici = ((InstrumentConsumableItem) record.getItem());
                                if (ici.getInstrumentData().getInstrument().getComponent(IconComponent.class) != null)
                                {
                                    ContentImage.RenderImage(ici.getInstrumentData().getInstrument(), image, 1);
                                }
                                else
                                {
                                    ContentImage.RenderInstrument(image, ici.getInstrumentData().getInfo());
                                }
                            }
                            else
                            {
                                ContentImage.RenderImage(record.getItem().getContent(), image, eventAmount);
                            }
                        }
                        else
                        {
                            ContentImage.RenderImage(
                                    BrainOutClient.ContentMgr.get("freeplay-ru"), image, eventAmount);
                        }

                        additional.add(image).padTop(48).expandX().fillX().row();
                    }

                    addLabelNotice(notice, additional);

                    switch (eventReason)
                    {
                        case marketOrderFulfilled:
                        {
                            Menu.playSound(Menu.MenuSound.itemSold);
                            break;
                        }
                        default:
                        {
                            Menu.playSound(Menu.MenuSound.skillpointsEarned);
                            break;
                        }
                    }


                    break;
                }
                case clanScore:
                {
                    TextureAtlas.AtlasRegion dogTags =
                            BrainOutClient.getRegion("dog-tags");

                    if (dogTags == null)
                        return;

                    Image image  = new Image(dogTags);
                    image.setScaling(Scaling.none);

                    Table additional = new Table();
                    additional.setFillParent(true);
                    additional.align(Align.top);

                    additional.add(image).padTop(48).expandX().fillX().row();

                    addLabelNotice(notice, additional);

                    break;
                }
                case gears:
                {
                    TextureAtlas.AtlasRegion gearsRegion =
                            BrainOutClient.getRegion("icon-gears-big");

                    if (gearsRegion == null)
                        return;

                    Image image  = new Image(gearsRegion);
                    image.setScaling(Scaling.none);

                    Table additional = new Table();
                    additional.setFillParent(true);
                    additional.align(Align.top);

                    additional.add(image).padTop(48).expandX().fillX().row();

                    addLabelNotice(notice, additional);

                    Menu.playSound(Menu.MenuSound.repair);

                    break;
                }
                case nuclearMaterial:
                {
                    TextureAtlas.AtlasRegion gearsRegion =
                            BrainOutClient.getRegion("icon-nuclear-material-big-3");

                    if (gearsRegion == null)
                        return;

                    Image image  = new Image(gearsRegion);
                    image.setScaling(Scaling.none);

                    Table additional = new Table();
                    additional.setFillParent(true);
                    additional.align(Align.top);

                    additional.add(image).padTop(48).expandX().fillX().row();

                    addLabelNotice(notice, additional);

                    Menu.playSound(Menu.MenuSound.geigerCard);

                    break;
                }
                case ownable:
                {
                    String ownedId = ((ContentND) eventData).id;
                    OwnableContent ownableContent = ((OwnableContent) BrainOutClient.ContentMgr.get(ownedId));

                    Actor ac = null;

                    if (ownableContent != null)
                    {

                        if (ownableContent.hasComponent(AnimationComponent.class))
                        {
                            AnimationComponent anim = ownableContent.getComponent(AnimationComponent.class);
                            ac = new ProfileBadgeAnimation(anim.getAnimation());
                        } else if (ownableContent.hasComponent(IconComponent.class))
                        {
                            ac = new Image(ownableContent.getComponent(IconComponent.class).getIcon("big-icon"));
                            ((Image) ac).setScaling(Scaling.none);
                        } else if (ownableContent.hasComponent(AnimatedIconComponent.class))
                        {
                            ac = new Image();
                            ownableContent.getComponent(AnimatedIconComponent.class).setupImage(((Image) ac));
                            ((Image) ac).setScaling(Scaling.none);
                        } else if (ownableContent instanceof InstrumentSlotItem)
                        {
                            InstrumentSlotItem item = ((InstrumentSlotItem) ownableContent);

                            InstrumentInfo info = new InstrumentInfo();
                            info.instrument = item.getInstrument();
                            info.skin = item.getDefaultSkin();

                            InstrumentIcon icon = new InstrumentIcon(info, 1.0f, false);
                            icon.init();

                            icon.setSize(192, 64);

                            ac = icon;
                        }

                        Table additional = new Table();
                        additional.setFillParent(true);
                        additional.align(Align.top);

                        if (ac != null)
                        {
                            additional.add(ac).padTop(48).expandX().fillX().row();
                        }

                        if (ownableContent.getTitle().isValid())
                        {
                            final Label contentTitle = new Label(ownableContent.getTitle().get(),
                                    BrainOutClient.Skin, "title-messages-white");
                            contentTitle.setAlignment(Align.center);
                            contentTitle.setWrap(true);

                            additional.add(contentTitle).expandX().fillX().row();
                        }

                        addLabelNotice(notice, additional);
                        amount = 0;

                        if (ownableContent.hasComponent(UnlockSoundComponent.class))
                        {
                            Sound sound = ownableContent.getComponent(UnlockSoundComponent.class).getSound();
                            sound.play();
                        } else
                        {
                            Menu.playSound(Menu.MenuSound.contentOwned);
                        }
                    }

                    break;
                }
                case consumable:
                {
                    ConsumableRecord record = ((ConsumableND) eventData).item;

                    Actor image = null;
                    String contentTitle;

                    if (record.getItem() instanceof InstrumentConsumableItem)
                    {
                        InstrumentConsumableItem ici = ((InstrumentConsumableItem) record.getItem());

                        InstrumentIcon icon = new InstrumentIcon(
                                ici.getInstrumentData().getInfo(), 1.0f, false);

                        contentTitle = ici.getInstrumentData().getInfo().skin != null ?
                                ici.getInstrumentData().getInfo().skin.getTitle().get() :
                                ici.getInstrumentData().getInfo().instrument.getTitle().get();

                        icon.init();

                        icon.setSize(192, 64);

                        image = icon;
                    }
                    else
                    {
                        Content content = record.getItem().getContent();
                        contentTitle = content.getTitle().get();

                        if (content.hasComponent(IconComponent.class))
                        {
                            image = new Image(content.getComponent(IconComponent.class).getIcon("big-icon"));
                            ((Image) image).setScaling(Scaling.none);
                        } else if (content.hasComponent(AnimatedIconComponent.class))
                        {
                            image = new Image();
                            content.getComponent(AnimatedIconComponent.class).setupImage(((Image) image));
                            ((Image) image).setScaling(Scaling.none);
                        }
                    }

                    Table additional = new Table();
                    additional.setFillParent(true);
                    additional.align(Align.top);

                    if (image != null)
                    {
                        additional.add(image).padTop(48).expandX().fillX().row();
                    }

                    {
                        final Label contentTitle_= new Label(contentTitle,
                                BrainOutClient.Skin, "title-messages-white");
                        contentTitle_.setAlignment(Align.center);
                        contentTitle_.setWrap(true);

                        additional.add(contentTitle_).expandX().fillX().row();
                    }

                    addLabelNotice(notice, additional);
                    amount = 0;

                    switch (eventReason)
                    {
                        case marketItemPurchased:
                        {
                            Menu.playSound(Menu.MenuSound.itemSold);
                            break;
                        }
                        case marketOrderPosted:
                        case marketOrderCancelled:
                        {
                            Menu.playSound(Menu.MenuSound.chipFail);
                            break;
                        }
                        default:
                        {
                            Menu.playSound(Menu.MenuSound.contentOwned);
                            break;
                        }
                    }
                    break;
                }
                case weaponSkills:
                {
                    Skin weapon = ((Skin) BrainOut.ContentMgr.get(((SkillsND) eventData).weaponSkin));

                    if (weapon == null)
                        return;

                    Table additional = new Table();
                    additional.setFillParent(true);
                    additional.align(Align.top);

                    String levelIcon = "weapon-skill-" + eventAmount + "-big";

                    TextureRegion levelIconRegion = BrainOutClient.getRegion(levelIcon);
                    if (levelIconRegion != null)
                    {
                        Image image = new Image(levelIconRegion);
                        image.setScaling(Scaling.none);

                        additional.add(image).padTop(22).expandX().fillX().row();
                    }

                    final Label contentTitle = new Label(weapon.getTitle().get(),
                            BrainOutClient.Skin, "title-messages-white");
                    contentTitle.setAlignment(Align.center);
                    contentTitle.setWrap(true);

                    additional.add(contentTitle).padTop(10).expandX().fillX().row();

                    addLabelNotice(notice, additional);
                    amount = 0;

                    Menu.playSound(Menu.MenuSound.newWeaponSkill);

                    break;
                }
                case level:
                {
                    String kind = ((LevelND) eventData).kind;

                    Levels levels = BrainOutClient.ClientController.getLevels(kind);
                    Levels.Level newLevel = levels.getLevel(eventAmount);

                    final Label newLevelTitle = new Label(newLevel.toString(),
                            BrainOutClient.Skin, "title-messages-white");
                    newLevelTitle.setAlignment(Align.center);
                    newLevelTitle.setWrap(true);

                    Table additional = new Table();
                    additional.setFillParent(true);
                    additional.align(Align.top);

                    additional.add(newLevelTitle).padTop(10).expandX().fillX().row();


                    String levelIcon = newLevel.icon;
                    TextureRegion levelIconRegion = BrainOutClient.getRegion(levelIcon);
                    if (levelIconRegion != null)
                    {
                        Image image = new Image(levelIconRegion);
                        image.setScaling(Scaling.none);

                        additional.add(image).padTop(22).expandX().fillX().row();
                    }


                    final Label contentTitle = new Label(newLevel.name.get(),
                            BrainOutClient.Skin, "title-messages-white");
                    contentTitle.setAlignment(Align.bottom, Align.center);
                    contentTitle.setWrap(true);

                    additional.add(contentTitle).expand().fill().padBottom(48).row();

                    addLabelNotice(notice, additional);
                    amount = 0;

                    if (kind.equals(Constants.User.LEVEL))
                    {
                        Menu.playSound(Menu.MenuSound.levelUp);
                    }
                    else
                    {
                        Menu.playSound(Menu.MenuSound.techLevelUp);
                    }

                    break;
                }
                case rankUp:
                {
                    Menu.playSound(Menu.MenuSound.rankUp);

                    Table additional = new Table();
                    additional.setFillParent(true);
                    additional.align(Align.top);

                    final Label contentTitle = new Label(L.get("MENU_GUNGAME_RANK_UP"),
                            BrainOutClient.Skin, "title-messages-white");

                    contentTitle.setAlignment(Align.bottom, Align.center);
                    contentTitle.setWrap(true);

                    additional.add(contentTitle).expand().fill().padBottom(48).row();

                    addLabelNotice(notice, additional);

                    break;
                }
                default:
                {
                    addLabelNotice(notice, defaultAdditional);
                }
            }

        }

        final Table r = new Table();

        r.setFillParent(true);
        r.align(Align.center | Align.top);

        String postFix;

        switch (notifyAward)
        {
            case ru:
            {
                postFix = " RU";
                break;
            }
            default:
            {
                postFix = "";
                break;
            }
        }

        Table d = new Notify(title, finalPositive, amount, postFix);

        r.add(d).pad(100).maxSize(400).row();

        if (!BrainOutClient.getInstance().topState().hasTopMenu())
            return;

        Menu menu = BrainOutClient.getInstance().topState().topMenu();
        menu.addActor(r);

        r.addAction(Actions.sequence(
                Actions.delay(ClientConstants.Menu.Notify.APPEARANCE),
                Actions.alpha(0, 0.25f),
                Actions.run(r::remove)
        ));
    }

    public static void AddNotification(NotifyEvent event)
    {
        final String title;
        final String notice;

        boolean positive = true;
        Actor defaultAdditional = null;

        final NotifyAward notifyAward = event.notifyAward;
        int eventAmount = (int)event.amount;
        final NotifyData eventData = event.data;

        switch (event.reason)
        {
            case trophyEarned:
            {
                Menu.playSound(Menu.MenuSound.trophy);
                return;
            }
            case doubleKill:
            {
                notice = "label-doublekill";
                title = null;
                break;
            }

            case tripleKill:
            {
                notice = "label-triplekill";
                title = null;
                break;
            }

            case enemyHeadToHead:
            {
                notice = "label-headtohead";
                title = null;

                break;
            }

            case knifeKill:
            {
                notice = "label-killer";
                title = null;

                break;
            }
            case zombieWaveCompleted:
            {
                notice = "label-tech-levelup";
                title = L.get("WAVE_COMPLETED");

                break;
            }

            case enemyLongShot:
            {
                notice = "label-longshot";
                title = null;

                break;
            }

            case unlockedOwnable:
            case gotOwnable:
            {
                notice = "label-ownable";
                title = L.get("MENU_UNLOCKED");

                break;
            }

            case newSkillLevel:
            {
                notice = "label-ownable";
                title = L.get("MENU_NEW_SKILL_LEVEL");

                break;
            }

            case levelUp:
            {
                String skin = ((LevelND) eventData).kind;

                notice = skin.equals(Constants.User.LEVEL) ? "label-levelup" : "label-tech-levelup";
                title = L.get("MENU_LEVEL_UP");

                break;
            }
            case skillPointsEarned:
            {
                notice = "label-ownable";
                title = L.get("MENU_SKILLPOINTS_EARNED");

                break;
            }
            case battlePointsEarned:
            {
                notice = "label-ownable";
                title = L.get("MENU_BATTLEPOINTS_EARNED");

                break;
            }
            case chEarned:
            {
                notice = "label-ownable";
                title = L.get("MENU_GOLD_BARS_EARNED");

                break;
            }
            case duelBegin:
            {
                RankND rank = ((RankND) eventData);

                notice = "label-duel";
                title = L.get("MENU_DUEL_STARTED", String.valueOf(rank.amount));

                Menu.playSound(Menu.MenuSound.gameStarted);

                break;
            }
            case ruEarned:
            {
                notice = "label-ownable";
                title = "RU";

                break;
            }
            case gearsEarned:
            {
                notice = "label-ownable";
                title = L.get("MENU_GEARS_EARNED");

                break;
            }
            case questComplete:
            {
                notice = null;
                title = L.get("MENU_QUEST_COMPLETE");

                if (event.data instanceof ContentND)
                {
                    ContentND cp = ((ContentND) event.data);

                    Quest quest = BrainOut.ContentMgr.get(cp.id, Quest.class);

                    if (quest != null)
                    {
                        BrainOutClient.Actions.addAction(new WaitAction(ClientConstants.Menu.Notify.APPEARANCE + 0.25f)
                        {
                            @Override
                            public void run()
                            {
                                Table additional = new Table();
                                additional.setFillParent(true);
                                additional.align(Align.top);

                                Menu.playSound(Menu.MenuSound.contentOwnedEx);

                                IconComponent iconComponent = quest.getComponent(IconComponent.class);

                                if (quest.getTitle().isValid())
                                {
                                    Label name = new Label(quest.getTitle().get(), BrainOutClient.Skin, "title-small");
                                    additional.add(name).padTop(16).row();
                                }

                                if (iconComponent != null && iconComponent.getIcon("bg", null) != null)
                                {
                                    Image img = new Image(iconComponent.getIcon("bg", null));
                                    img.setScaling(Scaling.none);
                                    additional.add(img).padTop(8).row();
                                }

                                addLabelNotice("label-levelup", additional);
                            }
                        });
                    }
                }

                break;
            }
            case nuclearMaterialReceived:
            {
                notice = "label-nuclear";
                title = L.get("CARD_NUCLEAR_MATERIAL");

                break;
            }
            case shootingRangeCompleted:
            {
                notice = "label-shooting-range";
                title = L.get("MENU_TRAINING_RANGE_COMPLETE");

                break;
            }
            case gunGameLevelUpgrade:
            {
                notice = "label-gungame-rankup";
                RankND rankND = ((RankND) eventData);
                title = String.valueOf(rankND.amount + 1) + " / " + (rankND.of + 1);

                break;
            }

            case warmupComplete:
            {
                Menu.playSound(Menu.MenuSound.gameStarted);
                return;
            }
            case clanEnemyKilled:
            {
                Effect effect = BrainOutClient.ContentMgr.get("clan-kill-effect", Effect.class);

                if (effect != null)
                {
                    ClientMap clientMap = Map.GetWatcherMap(ClientMap.class);

                    if (clientMap != null && Map.GetWatcher() != null)
                    {
                        effect.getSet().launchEffects(new PointLaunchData(
                            Map.GetWatcher().getWatchX(),
                            Map.GetWatcher().getWatchY(), 0,
                            Map.GetWatcher().getDimension()
                        ));

                        notice = "label-ownable";
                        title = L.get("MENU_CLAN_KILL");

                        break;
                    }
                }
            }

            case enemyHeadShot:
            case enemyKilled:
            {
                switch (event.reason)
                {
                    case enemyHeadShot:
                    {
                        notice = "label-headshot";
                        break;
                    }
                    default:
                    {
                        notice = null;
                        break;
                    }
                }

                title = null;

                break;
            }
            case eventRewardUnlocked:
            {
                EventRewardND nd = ((EventRewardND) eventData);

                title = L.get("MENU_EVENT_REWARD_UNLOCKED") + ": " +
                        String.valueOf(nd.unlocked) + " / " + nd.of;
                notice = null;

                Menu.playSound(Menu.MenuSound.levelUp);

                break;
            }
            case battlePassStageComplete:
            {
                LevelND nd = ((LevelND) eventData);

                title = L.get("MENU_BATTLE_PASS_STAGE_COMPLETE");
                notice = "label-tier";

                Table additional = new Table();
                additional.setFillParent(true);
                additional.align(Align.top);

                final Label contentTitle = new Label(String.valueOf(nd.kind), BrainOutClient.Skin, "title-messages-white");

                contentTitle.setAlignment(Align.bottom, Align.center);
                contentTitle.setWrap(true);

                additional.add(contentTitle).expand().fill().padBottom(48).row();
                defaultAdditional = additional;

                Menu.playSound(Menu.MenuSound.levelUp);

                break;
            }
            case battlePassTaskCompleted:
            {
                BattlePassEventRewardND nd = ((BattlePassEventRewardND) eventData);

                ClientBattlePassEvent ev = null;
                for (ClientEvent clientEvent : BrainOutClient.ClientController.getOnlineEvents())
                {
                    if (!(clientEvent instanceof ClientBattlePassEvent))
                    {
                        continue;
                    }

                    if (clientEvent.getEvent().id == nd.eventId)
                    {
                        ev = ((ClientBattlePassEvent) clientEvent);
                        break;
                    }
                }

                if (ev == null)
                {
                    return;
                }

                BattlePassTaskData d = ev.getData().getTasks().get(nd.idx);
                title = L.get("MENU_EVENT_REWARD_UNLOCKED");
                notice = "label-tier";

                Table additional = new Table();
                additional.setFillParent(true);
                additional.align(Align.top);

                final Label contentTitle = new Label(d.getTaskTitle(), BrainOutClient.Skin, "title-messages-white");

                contentTitle.setAlignment(Align.bottom, Align.center);
                contentTitle.setWrap(true);

                additional.add(contentTitle).expand().fill().padBottom(48).row();
                defaultAdditional = additional;

                Menu.playSound(Menu.MenuSound.levelUp);

                break;
            }
            case roundDraw:
            {
                title = L.get("MENU_ROUND_DRAW");
                notice = null;
                Menu.playSound(Menu.MenuSound.chipFail);
                positive = false;
                break;
            }
            case duelLost:
            {
                title = L.get("MENU_DUEL_LOST");
                notice = "label-duel";
                positive = false;
                break;
            }
            case marketOrderCancelled:
            {
                title = L.get("MENU_MARKET_ORDER_CANCELLED");
                positive = false;
                notice = "label-nuclear";
                break;
            }
            case marketOrderFulfilled:
            {
                title = L.get("MENU_MARKET_ITEM_SOLD");
                notice = "label-nuclear";
                break;
            }
            case marketItemPurchased:
            {
                title = L.get("MENU_MARKET_ITEM_PURCHASED");
                notice = "label-nuclear";
                break;
            }
            case marketOrderPosted:
            {
                title = L.get("MENU_MARKET_POSTED");
                notice = "label-nuclear";
                break;
            }
            case duelWon:
            {
                title = L.get("MENU_DUEL_WON");
                notice = "label-duel";
                break;
            }
            case roundYourTeamLost:
            {
                title = L.get("MENU_YOU_LOST");
                notice = null;
                Menu.playSound(Menu.MenuSound.chipFail);
                positive = false;
                break;
            }
            case roundYourTeamWon:
            {
                title = L.get("MENU_YOU_WON");
                notice = null;
                Menu.playSound(Menu.MenuSound.chipSuccess);
                break;
            }
            case chipLost:
            {
                title = L.get("REASON_CHIP_DELIVERED");
                notice = null;
                Menu.playSound(Menu.MenuSound.chipFail);
                positive = false;
                break;
            }
            case chipTaken:
            {
                title = L.get("REASON_CHIP_DELIVERED");
                notice = null;
                Menu.playSound(Menu.MenuSound.chipSuccess);
                break;
            }
            case chipWeStolen:
            {
                title = L.get("REASON_CHIP_TAKEN");
                notice = null;
                Menu.playSound(Menu.MenuSound.chipSuccess);
                break;
            }
            case chipEnemyStolen:
            {
                title = L.get("REASON_CHIP_LOST");
                notice = null;
                positive = false;
                Menu.playSound(Menu.MenuSound.chipFail);
                break;
            }
            case flagTaken:
            {
                title = L.get("REASON_FLAG_TAKEN");
                notice = null;
                Menu.playSound(Menu.MenuSound.chipSuccess);
                break;
            }
            case flagLost:
            {
                title = L.get("REASON_FLAG_LOST");
                notice = null;
                positive = false;
                break;
            }
            case autoBalanced:
            {
                title = L.get("REASON_BALANCED");
                notice = null;

                break;
            }
            default:
            {
                title = null;
                notice = null;

                break;
            }
        }

        switch (event.method)
        {
            case message:
            {
                final boolean finalPositive = positive;

                final NotifyReason eventReason = event.reason;

                Actor finalDefaultAdditional = defaultAdditional;

                BrainOutClient.Actions.addAction(new WaitAction(ClientConstants.Menu.Notify.APPEARANCE + 0.25f)
                {
                    @Override
                    public void run()
                    {
                        processMessageNotification(eventAmount, notice, eventReason, notifyAward, eventData,
                            title, finalPositive, finalDefaultAdditional);
                    }
                });

                break;
            }

            case popup:
            {
                BrainOutClient.Actions.addAction(new MenuAction()
                {
                    @Override
                    public void run()
                    {
                        BrainOutClient.getInstance().topState().pushMenu(new AlertPopup(title)
                        {
                            @Override
                            public void ok()
                            {
                                done();
                            }

                            @Override
                            protected void initContent(Table data)
                            {
                                super.initContent(data);

                                if (eventAmount != 0)
                                {
                                    Table reward = new Table();

                                    Label l = new Label(String.valueOf(eventAmount), BrainOutClient.Skin, "title-medium");
                                    l.setAlignment(Align.center);
                                    reward.add(l).expandX();
                                    reward.row();

                                    data.add(reward).expandX().fillX().pad(4).row();
                                }
                            }
                        });
                    }
                });

                break;
            }
        }
    }

    private static void addLabelNotice(String s, Actor additional)
    {
        GameState topState = BrainOutClient.getInstance().topState();

        if (topState == null)
            return;

        Menu menu = topState.topMenu();

        if (menu == null)
            return;

        TextureRegion region = BrainOutClient.getRegion(s);

        if (region == null) return;

        final Group root = new Group();

        root.setSize(region.getRegionWidth(), region.getRegionHeight());
        root.setPosition(BrainOutClient.getWidth() / 2f - region.getRegionWidth() / 2f,
                BrainOutClient.getHeight());

        root.addAction(Actions.sequence(
            Actions.moveBy(0, -region.getRegionHeight(), 0.25f, Interpolation.sineOut),
            Actions.delay(3.0f),
            Actions.alpha(0, 0.25f),
            Actions.run(root::remove)
        ));

        Image image = new Image(region);
        image.setZIndex(0);

        root.addActor(image);

        if (additional != null)
        {
            root.addActor(additional);
        }

        root.setTouchable(Touchable.disabled);
        menu.addActor(root);
    }
}
