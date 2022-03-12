package com.desertkun.brainout.client;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.utils.ObjectMap;
import com.desertkun.brainout.BrainOut;
import com.desertkun.brainout.BrainOutClient;
import com.desertkun.brainout.L;
import com.desertkun.brainout.common.enums.NotifyAward;
import com.desertkun.brainout.common.enums.NotifyMethod;
import com.desertkun.brainout.common.enums.NotifyReason;
import com.desertkun.brainout.common.enums.data.ConsumableND;
import com.desertkun.brainout.common.msg.client.RequestMsg;
import com.desertkun.brainout.common.msg.server.RequestErrorMsg;
import com.desertkun.brainout.common.msg.server.RequestSuccessMsg;
import com.desertkun.brainout.content.consumable.impl.InstrumentConsumableItem;
import com.desertkun.brainout.data.consumable.ConsumableRecord;
import com.desertkun.brainout.data.instrument.InstrumentData;
import com.desertkun.brainout.events.Event;
import com.desertkun.brainout.events.EventReceiver;
import com.desertkun.brainout.events.NotifyEvent;
import com.desertkun.brainout.events.SocialMessageEvent;
import com.desertkun.brainout.online.Clan;
import com.desertkun.brainout.online.UserProfile;
import com.desertkun.brainout.utils.MarketUtils;
import com.esotericsoftware.minlog.Log;
import org.anthillplatform.runtime.requests.Request;
import org.anthillplatform.runtime.services.LoginService;
import org.anthillplatform.runtime.services.SocialService;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashSet;

public class SocialController implements EventReceiver
{
    private SocialMessages messages;
    private int nextRequestId = 0;
    private ObjectMap<Integer, RequestCallback> callbackHandlers;
    private HashSet<String> myOutgoingClanRequests;
    private HashSet<String> myOutgoingInviteRequests;
    private HashSet<String> myOutgoingClanEngagements;
    private Clan myClan;
    private boolean myClanRequested;

    public interface RequestCallback
    {
        void success(JSONObject response);
        void error(String reason);
    }

    public SocialController()
    {
        messages = new SocialMessages();
        callbackHandlers = new ObjectMap<>();
        myOutgoingClanRequests = new HashSet<>();
        myOutgoingInviteRequests = new HashSet<>();
        myOutgoingClanEngagements = new HashSet<>();
        myClanRequested = false;
    }

    public SocialMessages getMessages()
    {
        return messages;
    }

    public void init()
    {
        BrainOutClient.EventMgr.subscribe(Event.ID.socialMessage, this);
    }

    public void release()
    {
        BrainOutClient.EventMgr.unsubscribe(Event.ID.socialMessage, this);
    }

    public boolean requestSuccess(RequestSuccessMsg msg)
    {
        Gdx.app.postRunnable(() ->
        {
            RequestCallback callback = callbackHandlers.remove(msg.id);

            if (callback != null)
            {
                JSONObject args;
                try
                {
                    args = new JSONObject(msg.args);
                }
                catch (JSONException ignore)
                {
                    return;
                }

                callback.success(args);
            }
        });

        return true;
    }

    public boolean requestError(RequestErrorMsg msg)
    {
        Gdx.app.postRunnable(() ->
        {
            RequestCallback callback = callbackHandlers.remove(msg.id);
            if (callback != null)
            {
                callback.error(msg.reason);
            }
        });

        return true;
    }

    public void sendRequest(String method, JSONObject args, RequestCallback callback)
    {
        nextRequestId++;
        callbackHandlers.put(nextRequestId, callback);

        BrainOutClient.ClientController.sendTCP(
            new RequestMsg(method, args, nextRequestId));
    }

    public void sendRequest(String method, RequestCallback callback)
    {
        sendRequest(method, new JSONObject(), callback);
    }

    @Override
    public boolean onEvent(Event event)
    {
        switch (event.getID())
        {
            case socialMessage:
            {
                SocialMessageEvent e = ((SocialMessageEvent) event);
                process(e.message);

                break;
            }
        }

        return false;
    }

    private void process(SocialMessages.ClientMessage e)
    {
        switch (e.messageType)
        {
            case "order_completed":
            {
                String give_item = e.payload.optString("give_item", "");
                JSONObject give_payload = e.payload.optJSONObject("give_payload");
                int give_amount = e.payload.optInt("give_amount", 1);

                String take_item = e.payload.optString("take_item", "");
                JSONObject take_payload = e.payload.optJSONObject("take_payload");
                int take_amount = e.payload.optInt("take_amount", 1);

                int amount_completed = e.payload.optInt("amount_completed", 1);
                int amount_left = e.payload.optInt("amount_left", 0);

                if (take_item.equals("ru"))
                {
                    ConsumableRecord record =
                            MarketUtils.MarketObjectToConsumableRecord(give_item, give_payload, give_amount);

                    if (record != null)
                    {
                        BrainOut.EventMgr.sendDelayedEvent(NotifyEvent.obtain(NotifyAward.ru,
                            take_amount * amount_completed, NotifyReason.marketOrderFulfilled,
                            NotifyMethod.message, new ConsumableND(record)));
                    }
                }

                break;
            }
            case "order_cancelled":
            {
                String give_item = e.payload.optString("give_item", "");
                JSONObject give_payload = e.payload.optJSONObject("give_payload");
                int give_amount = e.payload.optInt("give_amount", 1);

                String take_item = e.payload.optString("take_item", "");
                JSONObject take_payload = e.payload.optJSONObject("take_payload");
                int take_amount = e.payload.optInt("take_amount", 1);

                if (take_item.equals("ru"))
                {
                    ConsumableRecord record =
                            MarketUtils.MarketObjectToConsumableRecord(give_item, give_payload, give_amount);

                    if (record != null)
                    {
                        String amount = String.valueOf(give_amount);
                        String type;

                        if (record.getItem() instanceof InstrumentConsumableItem)
                        {
                            InstrumentData instrumentData =
                                ((InstrumentConsumableItem) record.getItem()).getInstrumentData();

                            if (instrumentData.getInfo().skin != null)
                            {
                                type = instrumentData.getInfo().skin.getTitle().get();
                            }
                            else
                            {
                                type = instrumentData.getInstrument().getSlotItem().getTitle().get();
                            }
                        }
                        else
                        {
                            type = record.getItem().getContent().getTitle().get();
                        }

                        BrainOut.EventMgr.sendDelayedEvent(NotifyEvent.obtain(NotifyAward.consumable,
                            0, NotifyReason.marketOrderCancelled,
                            NotifyMethod.message, new ConsumableND(record)));
                    }
                }

                break;
            }
            case "group_request":
            {
                if (e.recipientClass.equals("social-group") &&
                    e.sender.equals(BrainOutClient.ClientController.getMyAccount()))
                {
                    addOutgoingClanRequest(e.recipientKey);
                }

                break;
            }

            case "engage_conflict":
            {
                if (e.recipientClass.equals("social-group") &&
                    e.sender.equals(BrainOutClient.ClientController.getMyAccount()))
                {
                    addOutgoingClanEngagement(e.recipientKey);
                }

                break;
            }
        }
    }

    public void addOutgoingClanRequest(String clanId)
    {
        myOutgoingClanRequests.add(clanId);
    }

    public boolean hasOutgoingClanRequest(String clanId)
    {
        return myOutgoingClanRequests.contains(clanId);
    }

    public void removeOutgoingClanRequest(String clanId)
    {
        myOutgoingClanRequests.remove(clanId);
    }

    public void addOutgoingInviteRequest(String clanId)
    {
        myOutgoingInviteRequests.add(clanId);
    }

    public boolean hasOutgoingInviteRequest(String clanId)
    {
        return myOutgoingInviteRequests.contains(clanId);
    }

    public void removeOutgoingInviteRequest(String clanId)
    {
        myOutgoingInviteRequests.remove(clanId);
    }

    public void addOutgoingClanEngagement(String clanId)
    {
        myOutgoingClanEngagements.add(clanId);
    }

    public boolean hasOutgoingClanEngagement(String clanId)
    {
        return myOutgoingClanEngagements.contains(clanId);
    }

    public void removeOutgoingClanEngagement(String clanId)
    {
        myOutgoingClanEngagements.remove(clanId);
    }

    public Clan getMyClan()
    {
        return myClan;
    }

    public void userProfileUpdated(UserProfile userProfile)
    {
        if (myClanRequested)
            return;

        if (!userProfile.isParticipatingClan())
            return;

        SocialService socialService = SocialService.Get();
        LoginService loginService = LoginService.Get();

        if (socialService == null || loginService == null)
            return;

        socialService.getGroup(loginService.getCurrentAccessToken(), userProfile.getClanId(),
            (service, request, result, group) -> Gdx.app.postRunnable(() ->
        {
            if (result == Request.Result.success)
            {
                myClan = new Clan(group);
            }
            else
            {
                if (Log.ERROR) Log.error("Failed to retrieve my clan: " + result.toString());
                myClanRequested = false;
            }
        }));

        myClanRequested = true;

    }

    public void updateMyClan(Clan clan)
    {
        myClan = clan;
    }
}
