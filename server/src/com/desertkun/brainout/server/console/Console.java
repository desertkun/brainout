package com.desertkun.brainout.server.console;

import com.desertkun.brainout.client.Client;

import java.lang.reflect.Constructor;
import java.util.HashMap;

public class Console
{
    private static HashMap<String, Class<? extends ConsoleCommand>> commands = new HashMap<String, Class<? extends ConsoleCommand>>();

    static
    {
        registerCommand("echo", Echo.class);
        registerCommand("ping", Ping.class);
        registerCommand("kill", Kill.class);
        registerCommand("killteam", KillTeam.class);
        registerCommand("addscore", AddScore.class);
        registerCommand("endgame", EndGame.class);
        registerCommand("wingame", WinGame.class);
        registerCommand("endtime", EndTime.class);
        registerCommand("god", God.class);
        registerCommand("unlock", UnlockOwnable.class);
        registerCommand("reset", ResetProfile.class);
        registerCommand("kick", KickPlayer.class);
        registerCommand("for", ForUser.class);
        registerCommand("each", ForEach.class);
        registerCommand("name", UserName.class);
        registerCommand("mod", MakeMod.class);
        registerCommand("admin", MakeAdmin.class);
        registerCommand("addstat", AddStat.class);
        registerCommand("nonce", ReNonce.class);
        registerCommand("addev", AddBattlePassEventStat.class);
        registerCommand("bot", SpawnBot.class);
        registerCommand("speed", GameSpeed.class);
        registerCommand("editor", MakeEditor.class);
        registerCommand("tp", Teleport.class);
        registerCommand("slowmo", Slowmo.class);
        registerCommand("setstat", SetStat.class);
        registerCommand("map", SwitchMap.class);
        registerCommand("popup", Popup.class);
        registerCommand("give", Give.class);
        registerCommand("promo", UsePromo.class);
        registerCommand("autobalance", Autobalance.class);
        registerCommand("netstat", NetworkStatistics.class);
        registerCommand("nstart", NetworkStatisticsPerClassStart.class);
        registerCommand("nshow", NetworkStatisticsPerClassShow.class);
        registerCommand("shutdown", Shutdown.class);
        registerCommand("warmup", WarmUp.class);
        registerCommand("shuffle", Shuffle.class);
        registerCommand("custom", Custom.class);
        registerCommand("exitfreeplay", ExitFreeplay.class);
        registerCommand("enterfreeplay", EnterFreeplay.class);
        registerCommand("resize", ResizeMap.class);
        registerCommand("movechunks", MoveChunks.class);
        registerCommand("completequest", CompleteQuest.class);
        registerCommand("skipwarmup", SkipWarmUp.class);
        registerCommand("boom", Boom.class);
        registerCommand("migrate", Migrate.class);
        registerCommand("renamedimension", RenameDimension.class);
        registerCommand("follow", FollowPlayer.class);
        registerCommand("mebot", SpawnBotPlayer.class);
        registerCommand("impulse101", Impulse101.class);
        registerCommand("day", TimeOfTheDay.class);
        registerCommand("rain", Rain.class);
        registerCommand("code", Code.class);
        registerCommand("maxqueue", MaxQueueSize.class);
        registerCommand("flush", Flush.class);
        registerCommand("custom", CustomCommand.class);
        registerCommand("fullheal", FullHealCommand.class);
        registerCommand("extractextensions", ExtractExtensions.class);
    }

    public Console()
    {
    }

    private static void registerCommand(String name, Class<? extends ConsoleCommand> commandClass)
    {
        commands.put(name, commandClass);
    }

    public static String[] parseCommand( String cmd )
    {
        if( cmd == null || cmd.length() == 0 || cmd.length() > 128 )
        {
            return new String[] {};
        }

        try
        {
            cmd = cmd.trim();
            String[] parsedCommand = cmd.split(" +(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)");
            for (int i = 0; i < parsedCommand.length; i++)
            {
                String cmd_ = parsedCommand[i];
                if (cmd_.startsWith("\""))
                    parsedCommand[i] = cmd_.substring(1, cmd_.length()-1);
                cmd_ = parsedCommand[i];
                if (cmd_.endsWith("\""))
                    parsedCommand[i] = cmd_.substring(0, cmd_.length()-2);
            }

            return parsedCommand;
        }
        catch (Exception e)
        {
            return new String[] {};
        }
    }

    public String execute(Client client, String command)
    {
        String[] parameters = parseCommand(command);

        return executeParams(client, parameters, client);
    }

    public String executeParams(Client client, String[] parameters, Client asker)
    {
        if (parameters.length == 0)
        {
            return "error: command name required";
        }

        String commandName = parameters[0];

        Class<? extends ConsoleCommand> clazz = commands.get(commandName);

        if (clazz == null)
        {
            return "error: command '" + commandName + "' was not found.";
        }

        ConsoleCommand consoleCommand;

        try
        {
            Constructor<? extends ConsoleCommand> constructor = clazz.getConstructor();
            consoleCommand = constructor.newInstance();
        }
        catch (Exception e)
        {
            e.printStackTrace();
            return e.getMessage();
        }

        if (!consoleCommand.isRightsValid(asker, client, asker.getRights()))
        {
            return "error: forbidden";
        }

        if (consoleCommand.requiredArgs() >= parameters.length)
        {
            return "error: not enough parameters passed (" + (parameters.length - 1) + ", requied " + consoleCommand.requiredArgs();
        }

        return consoleCommand.execute(parameters, client);
    }
}
