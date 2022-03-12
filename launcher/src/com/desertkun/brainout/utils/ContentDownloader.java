package com.desertkun.brainout.utils;

import java.io.*;
import java.net.MalformedURLException;
import java.net.SocketException;
import java.net.URL;
import java.net.URLConnection;

public class ContentDownloader implements Runnable
{
    private DownloadStatus resultCallback;
    private OutputStream outputStream;
    private Thread download;
    private URL url;

    public interface DownloadStatus
    {
        enum DownloadCode
        {
            loading,
            success,
            notFound,
            failed
        }

        void progress(float value);
        void complete(DownloadCode code);
    }

    public interface DownloadString
    {
        void complete(DownloadStatus.DownloadCode code, String content);
    }

    void downloadFile(URL url, OutputStream outputStream) throws IOException
    {
        InputStream is = null;

        try
        {
            URLConnection urlConn = url.openConnection();
            urlConn.setConnectTimeout(10000);

            is = urlConn.getInputStream();

            long contentSize = urlConn.getContentLength();
            long pregress = 0;

            byte[] buffer = new byte[4096 * 50];
            int len;

            while ((len = is.read(buffer)) > 0)
            {
                outputStream.write(buffer, 0, len);
                pregress += len;

                resultCallback.progress((float)pregress / (float)contentSize);
            }

            resultCallback.complete(DownloadStatus.DownloadCode.success);
        }

        catch (SocketException e)
        {
            e.printStackTrace();

            resultCallback.complete(DownloadStatus.DownloadCode.failed);
        }
        finally
        {
            try
            {
                if (is != null)
                {
                    is.close();
                }
            }
            finally
            {
                if (outputStream != null)
                {
                    outputStream.close();
                }
            }
        }
    }

    public void download(String file, String saveTo, DownloadStatus result) throws FileNotFoundException
    {
        download(file, new FileOutputStream(saveTo), result);
    }

    public void download(String file, final DownloadString downloadString) throws FileNotFoundException
    {
        final ByteArrayOutputStream bo = new ByteArrayOutputStream();

        download(file, bo, new DownloadStatus()
        {
            @Override
            public void progress(float value)
            {
                //
            }

            @Override
            public void complete(DownloadCode code)
            {
                try
                {
                    downloadString.complete(code, bo.toString("UTF-8"));
                }
                catch (UnsupportedEncodingException e)
                {
                    e.printStackTrace();

                    downloadString.complete(DownloadCode.failed, "");
                }
            }
        });
    }

    public void download(String file, OutputStream saveTo, DownloadStatus result)
    {
        try
        {
            url = new URL(file);
        }
        catch (MalformedURLException e)
        {
            e.printStackTrace();
            return;
        }

        resultCallback = result;
        outputStream = saveTo;

        download = new Thread(this);
        download.start();
    }

    @Override
    public void run()
    {
        try
        {
            downloadFile(url, outputStream);
        }
        catch (FileNotFoundException e)
        {
            resultCallback.complete(DownloadStatus.DownloadCode.notFound);
        }
        catch (IOException e)
        {
            e.printStackTrace();
            resultCallback.complete(DownloadStatus.DownloadCode.failed);
        }
    }
}
