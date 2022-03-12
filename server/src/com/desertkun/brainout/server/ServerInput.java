package com.desertkun.brainout.server;

import com.desertkun.brainout.BrainOutServer;
import com.desertkun.brainout.client.Client;
import com.desertkun.brainout.online.PlayerRights;

import java.io.BufferedInputStream;
import java.io.InputStream;
import java.util.Scanner;

public class ServerInput implements Runnable
{
    private Client root;

    public ServerInput()
    {
        root = new Client(-1, BrainOutServer.Controller);
        root.setRights(PlayerRights.admin);
    }


    @Override
    public void run()
    {
        InputStream stream = new BufferedInputStream(System.in);
        final Scanner in = new Scanner(stream);

        while (in.hasNext())
        {
            final String input = in.nextLine();

            BrainOutServer.PostRunnable(() ->
            {
                String output = BrainOutServer.Controller.getConsole().execute(root, input);
                System.out.println("> " + output);
            });
        }
    }
}
