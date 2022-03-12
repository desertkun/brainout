package com.desertkun.brainout.menu.widgets;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Disposable;
import com.desertkun.brainout.BrainOutClient;
import com.desertkun.brainout.data.ClientMap;
import com.desertkun.brainout.data.Map;
import com.desertkun.brainout.menu.Menu;
import com.desertkun.brainout.menu.ui.StatisticsChart;
import com.desertkun.brainout.online.KryoNetworkConnection;
import com.esotericsoftware.kryonet.Connection;

public class ProfilingWidget extends Widget implements Disposable
{
    private StatisticsChart netRead;
    private StatisticsChart netWrite;
    private StatisticsChart physics;
    private StatisticsChart freeMemory, totalMemory;
    private StatisticsChart map;
    private StatisticsChart ping;
    private StatisticsChart fps;
    private StatisticsChart _1, _3, _5, _6;
    private float timer;
    private float statsRead, statsWrite;

    public ProfilingWidget(float x, float y, float w, float h)
    {
        super(x, y, w, h);

        align(Align.bottom);

        if (BrainOutClient.ClientController.getServerConnection() == null)
            return;

        Connection conn = ((KryoNetworkConnection)
            BrainOutClient.ClientController.getServerConnection()).getConnection();


        this.netRead = new StatisticsChart(128, 4096, "net read: kb",
            () -> statsRead);
        this.netWrite = new StatisticsChart(128, 4096, "net write: kb",
            () -> statsWrite);

        this.ping = new StatisticsChart(64, 100, "ping ms",
            () -> (conn.getReturnTripTime()));
        this.fps = new StatisticsChart(64, 17, "dt",
            () -> Gdx.graphics.getDeltaTime() * 1000.0f);

        this._1 = new StatisticsChart(64, 10, "update",
                () -> (BrainOutClient._update.getTook()));
        this._3 = new StatisticsChart(64, 10, "evt",
                () -> (BrainOutClient._events.getTook()));
        this._5 = new StatisticsChart(64, 10, "controller",
                () -> (BrainOutClient._controller.getTook()));
        this._6 = new StatisticsChart(64, 10, "render",
                () -> BrainOutClient._render.getTook());

        Runtime runtime = Runtime.getRuntime();

        this.freeMemory = new StatisticsChart(64, 256, "free: mb",
                () -> runtime.freeMemory() / 1024 / 1024);
        this.totalMemory = new StatisticsChart(64, 256, "total: mb",
                () -> runtime.totalMemory() / 1024 / 1024);

        add(netRead).size(192, 32).colspan(2).pad(4).row();
        add(netWrite).size(192, 32).colspan(2).pad(4).row();

        add(ping).size(96, 32).pad(2);
        add(fps).size(96, 32).pad(2).row();

        add(_1).size(96, 32).pad(2);
        add(_3).size(96, 32).pad(2).row();

        add(_5).size(96, 32).pad(2);
        add(_6).size(96, 32).pad(2).row();

        ClientMap map_ = Map.GetWatcherMap(ClientMap.class);

        if (map_ != null)
        {
            this.physics = new StatisticsChart(64, 10, "phy: ms",
                    () -> map_.getPhysicsCalculation().getTook());
            this.map = new StatisticsChart(64, 10, "map: ms",
                    () -> map_.getMapUpdate().getTook());

            add(physics).size(96, 32).pad(2);
            add(map).size(96, 32).pad(2).row();
        }
        else
        {
            this.physics = null;
            this.map = null;
        }

        add(freeMemory).size(96, 32).pad(2);
        add(totalMemory).size(96, 32).pad(2).row();

        setVisible(false);
    }

    @Override
    public void act(float delta)
    {
        super.act(delta);

        timer += delta;

        if (timer >= 1.0f)
        {
            timer = 0;

            if (BrainOutClient.ClientController.getServerConnection() != null)
            {
                Connection conn = ((KryoNetworkConnection)
                        BrainOutClient.ClientController.getServerConnection()).getConnection();

                statsRead = conn.getTcpConnection().queryBytesReceived() / 1024.0f +
                        conn.getUdpConnection().queryBytesReceived() / 1024.0f;
                statsWrite = conn.getTcpConnection().queryBytesSent() / 1024.0f +
                        conn.getUdpConnection().queryBytesSent() / 1024.0f;
            }
        }

        if (Gdx.input.isKeyPressed(Input.Keys.ALT_LEFT) &&
                Gdx.input.isKeyJustPressed(Input.Keys.NUM_0))
        {
            setVisible(!isVisible());
        }
    }

    @Override
    public void dispose()
    {
        netRead.dispose();
        netWrite.dispose();
        ping.dispose();
        fps.dispose();

        if (physics != null)
            physics.dispose();

        if (map != null)
            map.dispose();

        freeMemory.dispose();
        totalMemory.dispose();

        _1.dispose();
        _3.dispose();
        _5.dispose();
        _6.dispose();

        netRead = null;
        netWrite = null;
        ping = null;
        fps = null;
        physics = null;
        map = null;
        freeMemory = null;
        totalMemory = null;
        _1 = null;
        _3 = null;
        _5 = null;
        _6 = null;
    }
}
