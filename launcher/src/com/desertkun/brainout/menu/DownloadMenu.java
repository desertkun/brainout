package com.desertkun.brainout.menu;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.ProgressBar;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.Align;
import com.desertkun.brainout.Launcher;
import com.desertkun.brainout.ui.BorderActor;
import com.desertkun.brainout.ui.LoadingBlock;
import com.desertkun.brainout.utils.ContentDownloader;

import java.io.File;

public class DownloadMenu extends Menu
{
    private final Launcher.DownloadFile file;
    private final DownloadResult downloadResult;
    private ContentDownloader contentDownloader;

    public DownloadMenu(Launcher.DownloadFile file, DownloadResult downloadResult)
    {
        this.file = file;
        this.downloadResult = downloadResult;
        this.contentDownloader = new ContentDownloader();
    }

    public interface DownloadResult
    {
        void success();
        void failed(String text);
    }

    @Override
    public void init()
    {
        attachBackground();

        super.init();

        LoadingBlock loadingBlock = new LoadingBlock();
        loadingBlock.setBounds(Launcher.getWidth() - 32, 16, 16, 16);

        addActor(loadingBlock);
    }

    @Override
    public void initUI(Table content)
    {
        Label status = new Label("Downloading " + file.name, Launcher.SKIN, "title-default");
        status.setWrap(true);
        status.setAlignment(Align.center);

        content.add(status).width(300).pad(20).row();

        final ProgressBar progressBar = new ProgressBar(0, 100.0f, 1.0f, false, Launcher.SKIN, "progress-default");

        content.add(new BorderActor(progressBar, 256)).pad(20).row();

        try
        {
            FileHandle fileHandle = Gdx.files.local(file.name).parent();
            fileHandle.mkdirs();

            contentDownloader.download(file.url, file.name, new ContentDownloader.DownloadStatus()
            {
                @Override
                public void progress(final float value)
                {
                    progressBar.setValue(value * 100.0f);
                }

                @Override
                public void complete(final DownloadCode code)
                {
                    Gdx.app.postRunnable(new Runnable()
                    {
                        @Override
                        public void run()
                        {
                            if (code == DownloadCode.success)
                            {
                                downloadResult.success();
                            }
                            else
                            {
                                downloadResult.failed("Code:" + code.ordinal());
                            }
                        }
                    });
                }
            });
        }
        catch (Exception e)
        {
            downloadResult.failed(e.getMessage());
        }
    }
}
