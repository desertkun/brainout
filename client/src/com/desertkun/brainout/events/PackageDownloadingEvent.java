package com.desertkun.brainout.events;

public class PackageDownloadingEvent extends Event
{
    public enum Status
    {
        loading,
        completed,
        error
    }

    public Status code;

    @Override
    public ID getID()
    {
        return ID.packageDownload;
    }

    private Event init(Status code)
    {
        this.code = code;

        return this;
    }

    public static Event obtain(Status code)
    {
        PackageDownloadingEvent e = obtain(PackageDownloadingEvent.class);
        if (e == null) return null;
        return e.init(code);
    }

    @Override
    public void reset()
    {
        this.code = null;
    }
}
