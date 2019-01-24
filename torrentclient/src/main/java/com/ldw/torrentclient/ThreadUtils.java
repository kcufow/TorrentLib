package com.ldw.torrentclient;

import android.os.Handler;
import android.os.Looper;

public final class ThreadUtils {

    private ThreadUtils() throws InstantiationException {
        throw new InstantiationException("This class is not created for instantiation");
    }

    /**
     * Execute the given {@link Runnable} on the ui thread.
     *
     * @param runnable The runnable to execute.
     */
    public static void runOnUiThread(Runnable runnable) {
        Thread uiThread = Looper.getMainLooper().getThread();
        if (Thread.currentThread() != uiThread) new Handler(Looper.getMainLooper()).post(runnable);
        else runnable.run();
    }

}