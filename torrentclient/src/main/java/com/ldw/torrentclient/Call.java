package com.ldw.torrentclient;

/**
 * Created by ldw
 * time :2019/1/23.
 */
public interface Call {

    Request request();


    void enqueue(Callback callback);

    void cancel();
    void pause();
    void resume();

    boolean isExecuted();
    boolean isCanceled();


    interface Factory {

        Call newCall(Request request);
    }

}
