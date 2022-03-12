package com.desertkun.brainout.menu.ui;

import com.badlogic.gdx.scenes.scene2d.*;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.WidgetGroup;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Scaling;
import com.desertkun.brainout.BrainOutClient;
import com.desertkun.brainout.common.msg.client.BadgeReadMsg;
import com.desertkun.brainout.content.OwnableContent;
import com.desertkun.brainout.data.interfaces.WithBadge;
import com.desertkun.brainout.events.BadgeReadEvent;
import com.desertkun.brainout.events.Event;
import com.desertkun.brainout.events.EventReceiver;
import com.desertkun.brainout.online.UserProfile;

public class MenuBadge extends WidgetGroup implements EventReceiver
{
    private final WithBadge badge;
    private final UserProfile profile;
    private final WidgetGroup to;
    private Actor applied;
    private WithBadge.Involve involve;
    private Mode mode;

    public interface BadgeCreator
    {
        MenuBadge get(WidgetGroup to, WithBadge badge, UserProfile profile, Mode mode);
        WithBadge.Involve involveChild();
    }

    public static class DefaultBadgeCreator implements BadgeCreator
    {
        @Override
        public MenuBadge get(WidgetGroup to, WithBadge badge, UserProfile profile, Mode mode)
        {
            return new MenuBadge(to, badge, profile, mode);
        }

        @Override
        public WithBadge.Involve involveChild()
        {
            return WithBadge.Involve.withChild;
        }
    };

    public static BadgeCreator DEFAULT_CREATOR = new DefaultBadgeCreator();

    public WidgetGroup getTo()
    {
        return to;
    }

    public enum Mode
    {
        hover,
        click
    }

    public static void apply(WidgetGroup to, WithBadge badge, Mode mode, BadgeCreator badgeCreator)
    {
        apply(to, badge, BrainOutClient.ClientController.getUserProfile(), mode, badgeCreator);
    }

    public static void apply(WidgetGroup to, WithBadge badge, BadgeCreator badgeCreator)
    {
        apply(to, badge, BrainOutClient.ClientController.getUserProfile(), Mode.hover, badgeCreator);
    }

    public static void apply(WidgetGroup to, WithBadge badge)
    {
        apply(to, badge, BrainOutClient.ClientController.getUserProfile(), Mode.hover, DEFAULT_CREATOR);
    }

    public static void apply(WidgetGroup to, WithBadge badge, Mode mode)
    {
        apply(to, badge, BrainOutClient.ClientController.getUserProfile(), mode, DEFAULT_CREATOR);
    }

    public static void apply(WidgetGroup to, WithBadge badge, UserProfile profile,
         Mode mode, BadgeCreator badgeCreator)
    {
        WithBadge.Involve involve = badgeCreator.involveChild();

        if (badge.hasBadge(profile, badgeCreator.involveChild()))
        {
            MenuBadge menuBadge = badgeCreator.get(to, badge, profile, mode);
            menuBadge.setInvolve(involve);
        }
    }

    protected MenuBadge(WidgetGroup to, WithBadge badge)
    {
        this(to, badge, BrainOutClient.ClientController.getUserProfile(), Mode.hover);
    }

    protected MenuBadge(WidgetGroup to, WithBadge badge, Mode mode)
    {
        this(to, badge, BrainOutClient.ClientController.getUserProfile(), mode);
    }

    public MenuBadge(WidgetGroup to, WithBadge badge, UserProfile profile, Mode mode)
    {
        this.badge = badge;
        this.profile = profile;
        this.to = to;

        this.applied = applyBadge(to);

        applied.addListener(new ClickListener()
        {
            @Override
            public void enter(InputEvent event, float x, float y, int pointer, Actor fromActor)
            {
                if (mode == Mode.hover)
                {
                    MarkBadge(badge.getBadgeId(), profile);
                }
            }

            @Override
            public void clicked(InputEvent event, float x, float y)
            {
                if (mode == Mode.click)
                {
                    MarkBadge(badge.getBadgeId(), profile);
                }
            }
        });

        to.addActor(this);
    }

    protected Actor applyBadge(WidgetGroup to)
    {
        Image image = new Image(BrainOutClient.getRegion("badge"));

        image.setScaling(Scaling.none);
        image.setAlign(Align.left | Align.top);
        image.setFillParent(true);

        setFillParent(true);
        addActor(image);

        return image;
    }

    protected void removeBadge(Actor applied)
    {
        applied.setVisible(false);
    }

    public static void MarkBadge(String id, UserProfile profile)
    {
        if (id != null)
        {
            if (profile.hasBadge(id))
            {
                BrainOutClient.ClientController.sendTCP(new BadgeReadMsg(id));
                profile.removeBadge(id);
                BrainOutClient.EventMgr.sendDelayedEvent(BadgeReadEvent.obtain());
            }
        }
    }

    private void checkBadge(UserProfile profile)
    {
        if (!badge.hasBadge(profile, involve))
        {
            if (applied != null)
            {
                removeBadge(applied);
                applied = null;
            }
        }
    }

    @Override
    public boolean onEvent(Event event)
    {
        switch (event.getID())
        {
            case badgeRead:
            {
                checkBadge(BrainOutClient.ClientController.getUserProfile());

                break;
            }
        }
        return false;
    }

    public void init()
    {
        BrainOutClient.EventMgr.subscribe(Event.ID.badgeRead, this);
    }

    public void release()
    {
        BrainOutClient.EventMgr.unsubscribe(Event.ID.badgeRead, this);
    }

    private void setInvolve(WithBadge.Involve involve)
    {
        this.involve = involve;
    }
}
