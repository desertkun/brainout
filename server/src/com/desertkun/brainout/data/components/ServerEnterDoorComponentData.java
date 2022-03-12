package com.desertkun.brainout.data.components;

import com.desertkun.brainout.BrainOutServer;
import com.desertkun.brainout.client.Client;
import com.desertkun.brainout.client.PlayerClient;
import com.desertkun.brainout.content.components.ServerFreeplayEnterDoorComponent;
import com.desertkun.brainout.data.active.PlayerData;
import com.desertkun.brainout.data.components.base.Component;
import com.desertkun.brainout.data.components.base.ComponentObject;
import com.desertkun.brainout.events.ActivateActiveEvent;
import com.desertkun.brainout.events.Event;
import com.desertkun.brainout.mode.GameMode;
import com.desertkun.brainout.mode.ServerFreeRealization;
import com.desertkun.brainout.playstate.PlayState;
import com.desertkun.brainout.playstate.ServerPSGame;
import com.desertkun.brainout.reflection.Reflect;
import com.desertkun.brainout.reflection.ReflectAlias;

@Reflect("ServerEnterDoorComponent")
@ReflectAlias("data.components.ServerEnterDoorComponentData")
public class ServerEnterDoorComponentData extends Component<ServerFreeplayEnterDoorComponent>
{
    public ServerEnterDoorComponentData(ComponentObject componentObject,
                                        ServerFreeplayEnterDoorComponent contentComponent)
    {
        super(componentObject, contentComponent);
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
        switch (event.getID())
        {
            case activeActivateData:
            {
                ActivateActiveEvent ev = ((ActivateActiveEvent) event);
                enter(ev.client, ev.playerData);
                break;
            }
        }

        return false;
    }

    private boolean isAllowedToLeave(PlayerData playerData)
    {
        return true;
    }

    private void enter(Client client, PlayerData playerData)
    {
        if (!(client instanceof PlayerClient))
            return;

        PlayerClient playerClient = ((PlayerClient) client);

        ActiveProgressComponentData progress = playerData.getComponent(ActiveProgressComponentData.class);

        if (progress == null)
            return;

        if (!isAllowedToLeave(playerData))
            return;

        PlayState playState = BrainOutServer.Controller.getPlayState();

        if (playState.getID() != PlayState.ID.game)
            return;

        GameMode mode = ((ServerPSGame) playState).getMode();

        if (mode.getID() != GameMode.ID.free)
            return;

        ServerFreeRealization free = ((ServerFreeRealization) mode.getRealization());

        playerClient.enablePlayer(false);

        progress.startCancellable(1.0f, () ->
        {
            free.playerEnter(playerData, playerClient);
            playerClient.enablePlayer(true);
        }, () ->
        {
            playerClient.enablePlayer(true);
        });

    }
}
