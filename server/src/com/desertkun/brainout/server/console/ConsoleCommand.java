package com.desertkun.brainout.server.console;

import com.desertkun.brainout.client.Client;
import com.desertkun.brainout.online.PlayerRights;

public abstract class ConsoleCommand
{
    public abstract int requiredArgs();
    public abstract String execute(String[] args, Client client);
    public abstract boolean isRightsValid(Client asker, Client forClient, PlayerRights rights);
}
