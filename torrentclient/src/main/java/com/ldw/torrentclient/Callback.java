package com.ldw.torrentclient;

/**
 * Created by ldw
 * time :2019/1/23.
 */
public interface Callback {
    /**
     * 下载失败
     * @param request 下载请求
     * @param e 失败的exception
     */
    void onFailed(Request request,Exception e);
    void onTorrentDownload(Request request);

    /**
     * 种子文件准备解析中
     * @param request 请求的request
     */
    void onPrepare(Request request);

    /**
     * 下载开始
     * @param request 请求的request
     */
    void onStart(Request request);
    void notifyProgress(Request request,float progress , int downloadRate,long totalDownload);
    void onStop(Request request);
    void onCompleted(Request request);


    void onPause(Request request);
}
