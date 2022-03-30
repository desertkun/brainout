package com.desertkun.brainout.server.console;

import com.badlogic.gdx.math.MathUtils;
import com.desertkun.brainout.BrainOutServer;
import com.desertkun.brainout.client.Client;
import com.desertkun.brainout.content.active.ThrowableActive;
import com.desertkun.brainout.data.Map;
import com.desertkun.brainout.data.active.ActiveData;
import com.desertkun.brainout.data.active.PlayerData;
import com.desertkun.brainout.data.components.SimplePhysicsComponentData;
import com.desertkun.brainout.data.interfaces.LaunchData;
import com.desertkun.brainout.mode.GameMode;
import com.desertkun.brainout.online.PlayerRights;
import com.desertkun.brainout.playstate.PlayState;
import com.desertkun.brainout.playstate.ServerPSGame;

/**
 * A replication of boom command but on the client location.
 * Used to test chunks with shooting disable.
 * @author OcZi
 */
public class MyBoom extends ConsoleCommand {

    @Override
    public int requiredArgs() {
        return 0;
    }

    @Override
    public String execute(String[] args, Client client) {
        PlayState playState = BrainOutServer.Controller.getPlayState();

        if (playState.getID() != PlayState.ID.game) {
            return "Not in game";
        }

        GameMode<?> mode = ((ServerPSGame) playState).getMode();

        if (mode.getID() != GameMode.ID.free) {
            return "Not in freeplay";
        }

        ThrowableActive active = BrainOutServer.ContentMgr.get("explosive-freeplay-active", ThrowableActive.class);
        PlayerData playerData = client.getPlayerData();

        LaunchData launchData = playerData.getLaunchData();
        ActiveData activeData = active.getData(launchData.getDimension());

        activeData.setPosition(playerData.getX(), playerData.getY() + 5);
        activeData.setAngle(MathUtils.random(240, 270));

        SimplePhysicsComponentData phy = activeData.getComponentWithSubclass(SimplePhysicsComponentData.class);
        if (phy == null) {
            return "No physics?";
        }

        phy.getSpeed().set(
            -MathUtils.random(60, 90),
            -MathUtils.random(400, 500)
        );

        Map map = phy.getMap();
        map.addActive(map.generateServerId(), activeData, true);

        return "Done";
    }

    @Override
    public boolean isRightsValid(Client asker, Client forClient, PlayerRights rights) {
        return rights == PlayerRights.mod || rights == PlayerRights.admin;
    }
}
