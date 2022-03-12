package com.desertkun.brainout.data.components;

import com.badlogic.gdx.utils.Array;
import com.desertkun.brainout.BrainOut;
import com.desertkun.brainout.client.PlayerClient;
import com.desertkun.brainout.content.components.CardComponent;
import com.desertkun.brainout.content.components.ItemLimitsComponent;
import com.desertkun.brainout.content.components.ServerCardComponent;
import com.desertkun.brainout.content.components.ServerCaseComponent;
import com.desertkun.brainout.content.gamecase.CardGroup;
import com.desertkun.brainout.content.gamecase.Case;
import com.desertkun.brainout.content.gamecase.gen.Card;
import com.desertkun.brainout.data.components.base.Component;
import com.desertkun.brainout.data.gamecase.CaseData;
import com.desertkun.brainout.data.gamecase.gen.CardData;
import com.desertkun.brainout.events.Event;
import com.desertkun.brainout.online.UserProfile;
import com.desertkun.brainout.utils.WeightedRandom;

import com.desertkun.brainout.reflection.Reflect;
import com.desertkun.brainout.reflection.ReflectAlias;

@Reflect("ServerCaseComponent")
@ReflectAlias("data.components.ServerCaseComponentData")
public class ServerCaseComponentData extends Component<ServerCaseComponent>
{
    private final CaseData caseData;

    public ServerCaseComponentData(CaseData caseData,
                                   ServerCaseComponent caseComponent)
    {
        super(caseData, caseComponent);

        this.caseData = caseData;
    }


    public void generate(PlayerClient playerClient, UserProfile profile)
    {
        Case gamecase = ((Case) caseData.getContent());

        for (Case.Card card : gamecase.getCards())
        {
            Array<Card> generators = new Array<>();
            // select the applicable generators of the groups in this cardData
            for (CardGroup group : card.getGroups())
            {
                generators.addAll(BrainOut.ContentMgr.queryContentTpl(Card.class,
                    (content) ->
                    {
                        if (!content.getGroup().equals(group))
                        {
                            return false;
                        }

                        ItemLimitsComponent limits = content.getComponent(ItemLimitsComponent.class);

                        if (limits != null && !limits.getLimits().passes(profile))
                            return false;

                        CardComponent scc = content.getComponentFrom(CardComponent.class);

                        return scc != null && scc.applicable(profile);
                    }));
            }

            if (generators.size > 0)
            {
                Card reward = WeightedRandom.random(Card.class, generators);

                if (reward != null)
                {
                    CardData result = reward.getCard(caseData, "default");

                    ServerCardComponent scc = result.getCard().getComponentFrom(ServerCardComponent.class);

                    if (scc != null)
                    {
                        scc.generate(result);
                        scc.apply(playerClient, profile, result);
                    }

                    caseData.getCards().add(new CaseData.CardResult(result, caseData));
                }
            }
        }
    }

    @Override
    public boolean hasRender()
    {
        return false;
    }

    @Override
    public boolean hasUpdate()
    {
        return false;
    }

    @Override
    public boolean onEvent(Event event)
    {
        return false;
    }
}
