package com.ldw.torrentclient;


import android.util.Log;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Iterator;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import com.ldw.torrentclient.RealCall.DownloadCall;

/**
 * Created by ldw
 * time :2019/1/23.
 */
public final class Dispatcher {
    private  int defaultDownloadRequest = 1;
    private  int maxDownloadRequest = 4;

    private int downloadRequest = defaultDownloadRequest;

    /** Executes calls. Created lazily. */
    private ExecutorService executorService;

    /** Ready async calls in the order they'll be run. */
    private final Deque<DownloadCall> readyAsyncCalls = new ArrayDeque<>();

    /** Running asynchronous calls. Includes canceled calls that haven't finished yet. */
    private final Deque<DownloadCall> runningAsyncCalls = new ArrayDeque<>();

    public void setDownloadRequest(int requestSize){
        if (requestSize<0 || requestSize>maxDownloadRequest){
            Log.i("TorrentClientDispatcher", "request size error ");

            return;
        }
        downloadRequest = requestSize;
    }


    public synchronized ExecutorService executorService() {
        if (executorService == null) {
            executorService = new ThreadPoolExecutor(0, Integer.MAX_VALUE, 60, TimeUnit.SECONDS,
                    new SynchronousQueue<Runnable>(), Util.threadFactory("Torrent Dispatcher", false));
        }
        return executorService;
    }

    synchronized void enqueue(DownloadCall downloadCall) {
        if (runningAsyncCalls.size() < downloadRequest ) {
            runningAsyncCalls.add(downloadCall);
            executorService().execute(downloadCall);
        } else {
            readyAsyncCalls.add(downloadCall);
        }
    }

    public void finished(DownloadCall downloadCall) {
        synchronized (this) {
            if (!runningAsyncCalls.remove(downloadCall)) throw new AssertionError("Call wasn't in-flight!");
             promoteCalls();
        }

    }

    private void promoteCalls() {
        if (runningAsyncCalls.size() >= downloadRequest) return; // Already running max capacity.
        if (readyAsyncCalls.isEmpty()) return; // No ready calls to promote.

        for (Iterator<DownloadCall> i = readyAsyncCalls.iterator(); i.hasNext(); ) {
            DownloadCall call = i.next();


                i.remove();
                runningAsyncCalls.add(call);
                executorService().execute(call);


            if (runningAsyncCalls.size() >= downloadRequest) return; // Reached max capacity.
        }
    }

    public void  cancelAll(){
        for (DownloadCall runningAsyncCall : runningAsyncCalls) {
            runningAsyncCall.cancel();
        }
    }
}
