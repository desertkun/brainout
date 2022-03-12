package com.desertkun.brainout.content.components;

import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.Align;
import com.desertkun.brainout.BrainOutClient;
import com.desertkun.brainout.ClientConstants;
import com.desertkun.brainout.data.gamecase.gen.CardData;
import com.desertkun.brainout.data.gamecase.gen.StatCardData;
import com.desertkun.brainout.menu.ui.Notify;

import com.desertkun.brainout.reflection.Reflect;
import com.desertkun.brainout.reflection.ReflectAlias;

@Reflect("content.components.ClientScoreCardComponent")
public class ClientScoreCardComponent extends ClientStatCardComponent
{
    @Override
    public void flip(CardData cardData, Group cardContainer)
    {
        StatCardData card = ((StatCardData) cardData);
        int amount = card.getAmount();

        Table container = new Table();
        container.setFillParent(true);
        container.align(Align.top);

        container.add(new Notify(null, true, amount)).pad(100).maxSize(400).row();

        cardContainer.getStage().addActor(container);

        container.addAction(Actions.sequence(
            Actions.delay(ClientConstants.Menu.Notify.APPEARANCE),
            Actions.alpha(0, 0.25f),
            Actions.run(container::remove)
        ));
    }
}
