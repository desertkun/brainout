package com.desertkun.brainout.menu.ui;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.ProgressBar;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.NinePatchDrawable;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Scaling;
import com.desertkun.brainout.BrainOutClient;
import com.desertkun.brainout.ClientConstants;
import com.desertkun.brainout.Constants;
import com.desertkun.brainout.L;
import com.desertkun.brainout.content.ContentLockTree;
import com.desertkun.brainout.content.Levels;
import com.desertkun.brainout.content.OwnableContent;
import com.desertkun.brainout.content.components.ContentProgressComponent;
import com.desertkun.brainout.content.instrument.Weapon;
import com.desertkun.brainout.content.shop.InstrumentSlotItem;
import com.desertkun.brainout.online.UserProfile;

public class UnlockTooltip
{
    public static class UnlockTooltipCreator implements Tooltip.TooltipCreator
    {
        protected final OwnableContent item;
        protected final UserProfile userProfile;

        public UnlockTooltipCreator(OwnableContent item, UserProfile userProfile)
        {
            this.item = item;
            this.userProfile = userProfile;
        }

        protected Actor renderBottomLine()
        {
            if (userProfile == null || item == null)
                return null;

            int need = item.getLockItem().getParam() + item.getLockItem().getDisplayOffset();
            if (need == 1) return null;

            int have = Math.max(item.getLockItem().getUnlockValue(userProfile, 0) + item.getLockItem().getDisplayOffset(), 0);

            Group progress = new Group();

            ProgressBar scoreBar = new ProgressBar(0, need,
                    1, false, BrainOutClient.Skin,
                    "progress-score");

            scoreBar.setBounds(
                    0,
                    -1,
                    512,
                    ClientConstants.Menu.PlayerInfo.LABEL_HEIGHT
            );

            scoreBar.setValue(have);
            progress.addActor(scoreBar);

            Label scoreValue = new Label(String.valueOf(have) + " / " + need,
                    BrainOutClient.Skin, "title-small");

            scoreValue.setAlignment(Align.center);
            scoreValue.setFillParent(true);
            progress.addActor(scoreValue);

            return progress;
        }

        protected void renderItem(Table content, OwnableContent item)
        {
            if (item instanceof InstrumentSlotItem)
            {
                InstrumentSlotItem slotItem = ((InstrumentSlotItem) item);

                if (slotItem.getInstrument() instanceof Weapon)
                {
                    Weapon weapon = ((Weapon) slotItem.getInstrument());

                    if (weapon.getPrimaryProperties().isVisible())
                    {

                        InstrumentSlotItem.InstrumentSelection selection =
                                ((InstrumentSlotItem.InstrumentSelection) slotItem.getStaticSelection());

                        InstrumentCharacteristics chars = new InstrumentCharacteristics(selection.getInfo(), -1);

                        content.add(chars).pad(16).expandX().fillX().row();
                    }

                    return;
                }
            }

            Label description = new Label(getDescription(),
                    BrainOutClient.Skin, "title-small");
            description.setAlignment(Align.center);
            description.setWrap(true);

            content.add(description).expandX().fillX().row();
        }

        protected boolean forceBottomLine()
        {
            return false;
        }

        protected boolean removeBottomLine()
        {
            return false;
        }

        @Override
        public Actor get()
        {
            Table tooltip = new Tooltip.TooltipTable();
            tooltip.align(Align.top | Align.center);

            Table content = new Table();
            content.setBackground(new NinePatchDrawable(BrainOutClient.getNinePatch("form-default")));

            Label title = new Label(getTitle(), BrainOutClient.Skin, "title-level");
            title.setAlignment(Align.center);
            title.setWrap(true);

            tooltip.add(new BorderActor(title, 540, "form-gray")).expandX().fillX().row();
            tooltip.add(content).expand().fill().row();

            boolean addBottomLine = false;

            if (item == null || userProfile == null || item.hasItem(userProfile) || !item.isLocked(userProfile))
            {
                renderItem(content, item);
            }
            else
            {
                ContentLockTree.LockItem lockItem = item.getLockItem();

                TextureRegion locked = BrainOutClient.getRegion(getLockedIcon(lockItem));
                Image lockedImage = new Image(locked);
                lockedImage.setScaling(Scaling.none);
                content.add(lockedImage).padBottom(8).expandX().fillX().row();

                if (!lockItem.isValid())
                {
                    Label lockedText = new Label(
                        lockItem.isUnlockTitleValid() ?
                        lockItem.getUnlockTitle() :
                        L.get("MENU_LOCKED"),
                        BrainOutClient.Skin, "title-small");
                    lockedText.setAlignment(Align.center);
                    lockedText.setWrap(true);
                    content.add(lockedText).expandX().fillX().row();

                    addBottomLine = false;
                }
                else
                {
                    String f = lockItem.getUnlockFor();

                    Label taskText = new Label(L.get("MENU_TASK"),
                            BrainOutClient.Skin, "title-gray");
                    taskText.setAlignment(Align.center);
                    taskText.setWrap(true);
                    content.add(taskText).expandX().fillX().row();

                    String text;

                    if (f.equals(Constants.User.LEVEL))
                    {
                        Levels levels = BrainOutClient.ClientController.getLevels(Constants.User.LEVEL);
                        if (levels != null)
                        {
                            Levels.Level level = levels.getLevel(lockItem.getParam());

                            text = L.get("MENU_TASK_REACH_LEVEL",
                                    level.toShortString());
                        }
                        else
                        {
                            text = "???";
                        }
                    }
                    else if (f.equals(Constants.User.TECH_LEVEL))
                    {
                        Levels levels = BrainOutClient.ClientController.getLevels(Constants.User.TECH_LEVEL);
                        Levels.Level level = levels.getLevel(lockItem.getParam());

                        text = L.get("MENU_TASK_REACH_TECH_LEVEL",
                            level.toShortString());
                    }
                    else
                    {
                        text = lockItem.isUnlockTitleValid() ?
                            lockItem.getUnlockTitle(lockItem.getParam()) :
                            L.get("MENU_LOCKED");
                    }

                    addBottomLine = true;

                    Label reachLevel = new Label(text,
                            BrainOutClient.Skin, "title-small");

                    reachLevel.setAlignment(Align.center);
                    reachLevel.setWrap(true);
                    content.add(reachLevel).expandX().fillX().row();
                }
            }

            if (!removeBottomLine() && (addBottomLine || forceBottomLine()))
            {
                Actor progress = renderBottomLine();

                if (progress != null)
                {
                    BorderActor ba = new BorderActor(progress, 512);
                    ba.getCell().height(20);
                    tooltip.add(ba).expandX().fillX().row();
                }
            }

            tooltip.setSize(544, 290);

            return tooltip;
        }

        protected String getTitle()
        {
            return item.getTitle().get();
        }

        protected String getDescription()
        {
            return item.getDescription().get();
        }

        protected String getLockedIcon(ContentLockTree.LockItem lockItem)
        {
            return lockItem.getLockIcon() != null ? lockItem.getLockIcon() : "icon-locked";
        }
    }

    public static void show(Actor actor,
        OwnableContent item,
        UserProfile userProfile,
        Stage stage,
        UnlockTooltipCreator tooltipCreator)
    {
        Tooltip.RegisterToolTip(actor, tooltipCreator, stage);
    }

    public static void show(Actor actor,
        OwnableContent item,
        UserProfile userProfile,
        Stage stage)
    {
        show(actor, item, userProfile, stage, new UnlockTooltipCreator(item, userProfile));
    }
}
