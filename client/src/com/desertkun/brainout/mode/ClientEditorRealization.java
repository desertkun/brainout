package com.desertkun.brainout.mode;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.JsonValue;
import com.desertkun.brainout.BrainOutClient;
import com.desertkun.brainout.common.editor.props.set.EditorSetActivePropertiesMsg;
import com.desertkun.brainout.common.editor.props.set.EditorSetMapPropertiesMsg;
import com.desertkun.brainout.common.msg.server.editor.MapSettingsUpdatedMsg;
import com.desertkun.brainout.data.ClientMap;
import com.desertkun.brainout.data.EditorMap;
import com.desertkun.brainout.data.Map;
import com.desertkun.brainout.data.active.ActiveData;
import com.desertkun.brainout.data.block.BlockData;
import com.desertkun.brainout.editor.EditorComponent;
import com.desertkun.brainout.editor.data.EditorWatcher;
import com.desertkun.brainout.editor.menu.EditorMenu;
import com.desertkun.brainout.events.Event;
import com.desertkun.brainout.playstate.PlayState;

public class ClientEditorRealization extends ClientRealization<GameModeEditor>
{
    private EditorMenu editorMenu;

    public ClientEditorRealization(GameModeEditor gameMode)
    {
        super(gameMode);

        BrainOutClient.MusicMng.stopMusic();
    }

    @Override
    public void init(PlayState.InitCallback callback)
    {
        for (EditorMap map : Map.All(EditorMap.class))
        {
            // register editor watcher
            EditorWatcher editorWatcher = new EditorWatcher(map.getDimension());
            map.addActive(map.generateClientId(), editorWatcher, true);

            // attach the editor component
            EditorComponent editorComponent = new EditorComponent(map.getDimension());
            map.getComponents().addComponent(editorComponent);
            editorComponent.init();

            map.setEditorWatcher(editorWatcher);

            // disable shadow
            map.setShadowEnabled(false);
        }

        // attach the editor menu
        this.editorMenu = new EditorMenu();
        BrainOutClient.getInstance().topState().pushMenu(editorMenu);

        if (callback != null)
        {
            callback.done(true);
        }
    }

    @Override
    public void update(float dt)
    {
        //
    }

    @Override
    public Class<? extends ClientMap> getMapClass()
    {
        return EditorMap.class;
    }

    @Override
    public boolean onEvent(Event event)
    {
        return false;
    }

    @SuppressWarnings("unused")
    public boolean received(final EditorSetActivePropertiesMsg msg)
    {
        EditorMap editorMap = Map.Get(msg.d, EditorMap.class);

        if (editorMap == null)
            return true;

        Gdx.app.postRunnable(() ->
        {
            ActiveData activeData = editorMap.getActiveData(msg.activeId);

            if (activeData != null)
            {
                editorMenu.showActiveProperties(activeData, new Array<>(msg.properties));
            }
        });

        return true;
    }

    @SuppressWarnings("unused")
    public boolean received(final EditorSetMapPropertiesMsg msg)
    {
        EditorMap editorMap = Map.Get(msg.d, EditorMap.class);

        if (editorMap == null)
            return true;

        Gdx.app.postRunnable(() -> editorMenu.showMapProperties(editorMap, new Array<>(msg.properties)));

        return true;
    }

    @SuppressWarnings("unused")
    public boolean received(final MapSettingsUpdatedMsg msg)
    {
        EditorMap editorMap = Map.Get(msg.d, EditorMap.class);

        if (editorMap == null)
            return true;

        Gdx.app.postRunnable(() ->
        {
            JsonValue jsonValue = new JsonReader().parse(msg.data);
            editorMap.readCustom(json, jsonValue);
        });

        return true;
    }
}
