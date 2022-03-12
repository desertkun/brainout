package com.desertkun.brainout.utils;

import com.badlogic.gdx.utils.ObjectMap;
import com.desertkun.brainout.BrainOut;
import com.desertkun.brainout.Constants;
import com.esotericsoftware.kryonet.KryoNetException;
import com.esotericsoftware.minlog.Log;
import org.anthillplatform.runtime.AnthillRuntime;
import org.anthillplatform.runtime.requests.Request;
import org.anthillplatform.runtime.services.LoginService;
import org.anthillplatform.runtime.services.ReportService;
import org.json.JSONObject;


import java.io.*;
import java.text.SimpleDateFormat;

public class ExceptionHandler
{
    private static SimpleDateFormat crashFormal = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss");

    public ExceptionHandler()
    {

    }

    public static void reportCrash(final Throwable e, String name, Runnable done)
    {
        if (!BrainOut.OnlineEnabled())
        {
            StringWriter result = new StringWriter();
            PrintWriter printWriter = new PrintWriter(result);

            printWriter.println("Stack trace:");
            printWriter.println();
            e.printStackTrace(printWriter);

            if (e.getCause() != null)
            {
                printWriter.println();
                printWriter.println("Caused by:");
                printWriter.println();
                e.getCause().printStackTrace(printWriter);
            }

            System.err.println(result.toString());

            return;
        }

        ReportService reportService = ReportService.Get();
        LoginService loginService = LoginService.Get();

        // send the error report
        try
        {
            StringWriter result = new StringWriter();
            PrintWriter printWriter = new PrintWriter(result);

            printWriter.println("Stack trace:");
            printWriter.println();
            e.printStackTrace(printWriter);

            if (e.getCause() != null)
            {
                printWriter.println();
                printWriter.println("Caused by:");
                printWriter.println();
                e.getCause().printStackTrace(printWriter);
            }

            if (loginService == null || reportService == null)
            {
                System.err.println("Failed to send report:");
                System.err.println(result.toString());
                return;
            }

            JSONObject info = new JSONObject();

            for (ObjectMap.Entry<String, String> entry: BrainOut.Env.getEnvironmentValues())
            {
                info.put(entry.key, entry.value);
            }

            reportService.uploadTextReport(name, e.toString(),
                info, result.toString(), loginService.getCurrentAccessToken(),
            (reportId, request, status) ->
            {
                if (status == Request.Result.success)
                {
                    System.err.println("Report uploaded: " + reportId);
                }
                else
                {
                    System.err.println("Failed to upload report: " + status.toString());
                    System.err.println(result.toString());
                }

                done.run();
            });
        }
        catch (Exception e1)
        {
            e1.printStackTrace();
            // failed to send the report.. fine
        }
    }

    public static void handle(final Throwable e, OutputStream outputStream)
    {
        if (Log.ERROR)
        {
            StringWriter result = new StringWriter();
            PrintWriter printWriter = new PrintWriter(result);

            printWriter.println("Stack trace:");
            printWriter.println("----------------------------------");
            e.printStackTrace(printWriter);
            printWriter.println("----------------------------------");

            Log.error("crash:\n" + result.toString());
        }

        if (outputStream != null)
        {
            PrintStream printStream = new PrintStream(outputStream);
            printStream.append("Sorry, the exception happened:");

            try
            {
                e.printStackTrace(printStream);
            }
            catch (Exception e2)
            {
                // failed to print, forget
            }
        }

        BrainOut.StopLoop();

        if (!BrainOut.OnlineEnabled())
            return;

        reportCrash(e, "crashreport", () -> BrainOut.getInstance().crashed(e));
    }

    public static void handle(Throwable e)
    {
        e.printStackTrace();

        if (e instanceof KryoNetException)
            return;

        handle(e, null);
    }
}
