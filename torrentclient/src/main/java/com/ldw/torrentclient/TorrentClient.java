package com.ldw.torrentclient;

/**
 * Created by ldw
 * time :2019/1/22.
 */
public class TorrentClient implements Call.Factory {

      Dispatcher mDispatcher;

     int maxDownloadRate;
     int maxUploadRate;

     boolean saveTorrent;
     boolean deleteFileOnStop;
     boolean needsRename;

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


    }

    @Override
    public Call newCall(Request request) {
        return new RealCall(this,request);
    }

    public static final class Builder{

        private  Dispatcher mDispatcher;

        private int maxDownloadRate;
        private int maxUploadRate;

        public Builder setDispatcher(Dispatcher dispatcher) {
            mDispatcher = dispatcher;
            return this;
        }

        public Builder setMaxDownloadRate(int maxDownloadRate) {
            this.maxDownloadRate = maxDownloadRate;
            return this;
        }

        public Builder setMaxUploadRate(int maxUploadRate) {
            this.maxUploadRate = maxUploadRate;
            return this;
        }

        public Builder setSaveTorrent(boolean saveTorrent) {
            this.saveTorrent = saveTorrent;
            return this;
        }

        public Builder setDeleteFileOnStop(boolean deleteFileOnStop) {
            this.deleteFileOnStop = deleteFileOnStop;
            return this;
        }

        public Builder setNeedsRename(boolean needsRename) {
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
