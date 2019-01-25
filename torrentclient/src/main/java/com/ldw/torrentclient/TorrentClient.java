package com.ldw.torrentclient;

/**
 * Created by ldw
 * time :2019/1/22.
 */
public class TorrentClient implements Call.Factory {

      Dispatcher mDispatcher;

    public  int maxDownloadRate;
    public  int maxUploadRate;

    public boolean saveTorrent;
    public boolean deleteFileOnStop;
    public  boolean needsRename;
    public boolean autoDownloadNext;

    public Dispatcher dispatcher() {
        return mDispatcher;
    }



    public TorrentClient() {
            this(new Builder());
    }
     TorrentClient(Builder builder){

         mDispatcher =builder.mDispatcher;
         maxDownloadRate = builder.maxDownloadRate;
         maxUploadRate = builder.maxUploadRate;
         saveTorrent = builder.saveTorrent;
         deleteFileOnStop = builder.deleteFileOnStop;
         needsRename = builder.needsRename;
         autoDownloadNext = builder.autoDownloadNext;


    }

    @Override
    public Call newCall(Request request) {
        return new RealCall(this,request);
    }

    public static final class Builder{

        private boolean autoDownloadNext;
        private  Dispatcher mDispatcher;

        private int maxDownloadRate;
        private int maxUploadRate;

        public Builder autoDownloadNext(boolean autoDownloadNext) {
            this.autoDownloadNext = autoDownloadNext;
            mDispatcher.setAutoDownloadNext(autoDownloadNext);
            return this;
        }

        public Builder maxDownloadRate(int maxDownloadRate) {
            this.maxDownloadRate = maxDownloadRate;
            return this;
        }

        public Builder maxUploadRate(int maxUploadRate) {
            this.maxUploadRate = maxUploadRate;
            return this;
        }

        public Builder saveTorrent(boolean saveTorrent) {
            this.saveTorrent = saveTorrent;
            return this;
        }

        public Builder deleteFileOnStop(boolean deleteFileOnStop) {
            this.deleteFileOnStop = deleteFileOnStop;
            return this;
        }

        public Builder needsRename(boolean needsRename) {
            this.needsRename = needsRename;
            return this;
        }

        private boolean saveTorrent;
        private boolean deleteFileOnStop;
        private boolean needsRename;

        public Builder(){
            mDispatcher = new Dispatcher();
            maxDownloadRate = 0;
            maxUploadRate = 0;
            saveTorrent = false;
            deleteFileOnStop = true;
            needsRename = true;

        }


        public TorrentClient build(){
            return new TorrentClient(this);
        }





    }






}
