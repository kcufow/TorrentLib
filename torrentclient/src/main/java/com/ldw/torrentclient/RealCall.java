package com.ldw.torrentclient;


import android.text.TextUtils;
import android.util.Log;

import com.frostwire.jlibtorrent.AlertListener;
import com.frostwire.jlibtorrent.SessionManager;
import com.frostwire.jlibtorrent.TorrentInfo;
import com.frostwire.jlibtorrent.alerts.AddTorrentAlert;
import com.frostwire.jlibtorrent.alerts.Alert;
import com.frostwire.jlibtorrent.alerts.AlertType;
import com.frostwire.jlibtorrent.alerts.BlockFinishedAlert;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.CountDownLatch;

/**
 * Created by ldw
 * time :2019/1/23.
 */
public class RealCall implements Call {

    private TorrentClient client;
    private Request originalRequest;

    private boolean executed;
    private DownloadCall downloadCall;

    RealCall(TorrentClient client, Request request) {
        this.client = client;
        this.originalRequest = request;
    }

    @Override
    public Request request() {
        return originalRequest;
    }

    @Override
    public void enqueue(Callback callback) {
        synchronized (this) {
            if (executed) throw new IllegalStateException("Already Executed");
            executed = true;
        }
        downloadCall = new DownloadCall(callback,originalRequest);
        client.dispatcher().enqueue(downloadCall);
    }

    @Override
    public void cancel() {
        if (downloadCall != null) {
            downloadCall.cancel();
        }

    }

    @Override
    public void pause() {
        if (downloadCall != null) {
            downloadCall.pause();
        }
    }

    public void resume() {
        if (downloadCall != null) {
            downloadCall.resume();
        }
    }

    @Override
    public synchronized boolean isExecuted() {
        return executed;
    }

    @Override
    public boolean isCanceled() {
        return downloadCall.isCanceled();
    }


    final class DownloadCall extends NamedRunnable {
        private static final int STATUS_CANCELED = 1;
        private static final int STATUS_PAUSED = 2;
        private static final int STATUS_ERROR = 3;
        private static final int STATUS_DOWNLOADING = 4;
        private static final int STATUS_FINISH = 5;


        private SessionManager mS;
        private CountDownLatch mSignal;
        private Callback callback;
        private Request mRequest;
        private int mCurrentStatus;
        private boolean firstStart;

        private AlertListener mAlertListener = new AlertListener() {
            @Override
            public int[] types() {
                return null;
            }

            @Override
            public void alert(Alert<?> alert) {
                AlertType type = alert.type();
                L.v("torrentClient","alert "+type);

                switch (type) {
                    case ADD_TORRENT:
                        ((AddTorrentAlert) alert).handle().resume();


                        if (callback != null) {
                            ThreadUtils.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    callback.onPrepare(request());
                                }
                            });
                        }
                        break;
                    case BLOCK_FINISHED:
                        if (mCurrentStatus == STATUS_PAUSED || mCurrentStatus == STATUS_CANCELED){
                            return;
                        }
                        BlockFinishedAlert a = (BlockFinishedAlert) alert;
                        final float p = a.handle().status().progress() * 100;

                        final int i = a.handle().status().downloadRate();
                        final long totalDownload = mS.stats().totalDownload();
                        if (callback != null) {
                            ThreadUtils.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    callback.notifyProgress(request(), p, i, totalDownload);
                                }
                            });


                        }

                        break;
                    case STATS:

                        if (firstStart){
                            return;
                        }
                        if (callback != null ) {
                            ThreadUtils.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    callback.onStart(mRequest);
                                }
                            });

                        }
                            firstStart = false;
                        break;
                    case TORRENT_PAUSED:
                        if (callback != null) {
                            ThreadUtils.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    callback.onPause(mRequest);
                                }
                            });

                        }

                        break;

                    case DHT_ERROR:
//                        onError(new Exception("dht error"));
                        break;
                    case TORRENT_ERROR:
                        onError(new Exception("torrent error"));
                        break;
                    case SESSION_ERROR:
                        onError(new Exception("session error"));
                        break;
                    case TORRENT_FINISHED:
                        System.out.println("Torrent finished");
                        onFinish();
                        break;

                }
            }
        };
        private boolean isCanceled;

        private void onFinish() {
            mCurrentStatus = STATUS_FINISH;
            mSignal.countDown();
            if (callback != null) {
                callback.onCompleted(mRequest);
            }
        }

        public Callback getCallback() {
            return callback;
        }



        DownloadCall(Callback callback,Request originalRequest) {
            super("TorrentClient %s", originalRequest.uuid);
            this.callback = callback;
            mRequest = originalRequest;
        }

        @Override
        protected void execute() {

            try {
                if (mRequest.isNativeTorrent) {

                    TorrentInfo torrentInfo = new TorrentInfo(new File(mRequest.url));
                    if (!TextUtils.isEmpty(mRequest.saveFileName)){
                        torrentInfo.renameFile(0,mRequest.saveFileName);
                    }

                    torrentDownload(torrentInfo, mRequest.savePath);

                } else {
                    downloadTorrent(mRequest.url);

                }


            } catch (Exception e) {
                e.printStackTrace();
            } finally {

                client.dispatcher().finished(this);
            }


        }

        private void downloadTorrent(String torrentUrl) {
            try {
                ThreadUtils.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (callback != null) {
                            callback.onTorrentDownload(mRequest);
                        }
                    }
                });

                L.v("torrentClient", "downloadTorrent: ");
                URL url = new URL(torrentUrl);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();

                connection.setRequestMethod("GET");
                connection.setInstanceFollowRedirects(true);
                connection.connect();

                InputStream inputStream = connection.getInputStream();

                byte[] responseByteArray = new byte[0];

                if (connection.getResponseCode() == 200) {
                    responseByteArray = getBytesFromInputStream(inputStream);
                }

                inputStream.close();
                connection.disconnect();

                if (responseByteArray.length > 0) {

                    TorrentInfo torrentInfo = TorrentInfo.bdecode(responseByteArray);
                    if (!TextUtils.isEmpty(mRequest.saveFileName)){
                        torrentInfo.renameFile(0,mRequest.saveFileName);
                    }
                    torrentDownload(torrentInfo, mRequest.savePath);
                }
            } catch (IOException | IllegalArgumentException e) {

                L.v("torrentClient", "downloadTorrent: error ");
                onError(new Exception("torrent download error"));
            }


        }

        private byte[] getBytesFromInputStream(InputStream inputStream) throws IOException {
            ByteArrayOutputStream byteBuffer = new ByteArrayOutputStream();

            int bufferSize = 1024;
            byte[] buffer = new byte[bufferSize];

            int len = 0;
            while ((len = inputStream.read(buffer)) != -1) {
                byteBuffer.write(buffer, 0, len);
            }

            return byteBuffer.toByteArray();
        }

        private void torrentDownload(TorrentInfo torrentInfo, String savePath) {


            mS = new SessionManager();
            mSignal = new CountDownLatch(1);
            isCanceled = false;
            firstStart =true;
            mS.addListener(mAlertListener);
            mS.start();
            mCurrentStatus = STATUS_DOWNLOADING;
            if (client.maxDownloadRate!=0){
                mS.downloadRateLimit(client.maxDownloadRate);
            }
            mS.download(torrentInfo, new File(savePath));

            try {
                mSignal.await();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            mS.stop();
            mS.removeListener(mAlertListener);
            if (isCanceled){
                FileUtils.recursiveDelete(new File(savePath,torrentInfo.name()));
                FileUtils.recursiveDelete(new File(savePath,mRequest.saveFileName));
            }
        }

        private void onError(final Exception e) {
            if (callback != null) {
                ThreadUtils.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        callback.onFailed(mRequest, e);
                    }
                });
            }
            mCurrentStatus = STATUS_ERROR;
            mSignal.countDown();
        }

        void cancel() {
            if (mCurrentStatus == STATUS_CANCELED){
                return;
            }

            if (mS==null||!(mS.isRunning() || mS.isDhtRunning())){
                return ;
            }
            if (callback!=null){
                ThreadUtils.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        callback.onStop(mRequest);
                    }
                });
            }
            if (mS != null && (mS.isRunning() || mS.isDhtRunning())) {
                mSignal.countDown();

            }
            isCanceled = true;


        }

        synchronized void pause() {
            if (mCurrentStatus!=STATUS_DOWNLOADING){
                return;
            }
            if (mS != null && (!mS.isRunning() || mS.isDhtRunning())) {
                mS.pause();
                mCurrentStatus = STATUS_PAUSED;
            }

        }

        synchronized void resume() {

            if (mCurrentStatus!=STATUS_PAUSED){

                return;
            }
            if (mS != null && mS.isPaused()) {
                mS.resume();
                mCurrentStatus = STATUS_DOWNLOADING;
            }

        }

        public boolean isCanceled() {
            return isCanceled;
        }

        public Call get(){
            return RealCall.this;
        }
    }


}
