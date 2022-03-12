package com.desertkun.brainout.mode;

import com.desertkun.brainout.BrainOutClient;
import com.desertkun.brainout.content.shop.ShopCart;
import com.desertkun.brainout.data.ClientMap;
import com.desertkun.brainout.data.Editor2Map;
import com.desertkun.brainout.data.interfaces.Spawnable;
import com.desertkun.brainout.editor2.Editor2Menu;
import com.desertkun.brainout.events.Event;
import com.desertkun.brainout.gs.ActionPhaseState;
import com.desertkun.brainout.menu.impl.ActionPhaseMenu;
import com.desertkun.brainout.menu.impl.SpawnMenu;
import com.desertkun.brainout.playstate.PlayState;

public class ClientEditor2Realization extends ClientRealization<GameModeEditor2>
{
    public ClientEditor2Realization(GameModeEditor2 gameMode)
    {
        super(gameMode);

        BrainOutClient.MusicMng.stopMusic();
    }

    @Override
    public void init(PlayState.InitCallback callback)
    {

        /*

        // attach the editor menu
        this.editorMenu = new EditorMenu();
        BrainOutClient.getInstance().topState().pushMenu(editorMenu);

        */

        if (callback != null)
        {
            callback.done(true);
        }
    }

    @Override
    public void init(ActionPhaseMenu menu)
    {
        super.init(menu);

        menu.pushMenu(new Editor2Menu());
    }

    @Override
    public void showSpawnMenu(ActionPhaseState state, ShopCart shopCart, SpawnMenu.Spawn spawn, Spawnable lastSpawnPoint)
    {
        state.pushMenu(new Editor2Menu());
    }

    @Override
    public void update(float dt)
    {
        //
    }

    @Override
    public Class<? extends ClientMap> getMapClass()
    {
        return Editor2Map.class;
    }

    @Override
    public boolean onEvent(Event event)
    {
        return false;
    }
}
