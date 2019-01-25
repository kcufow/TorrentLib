package com.ldw.torrentclient;

import android.os.Environment;
import android.text.TextUtils;
import android.util.Log;

/**
 * Created by ldw
 * time :2019/1/23.
 */
public class Request {
    /**
     * 网络torrent路径，或者本地torrent路径暂不支持磁力链接
     */
    public String url;
    public String savePath;
    /**
     * 存储时保存的名字
     */
    public String saveFileName;
    /**
     * 该request的uuid标识，不指定时会以torrent的名称为值
     */
    public String uuid;
    /**
     * 当前request的索引值，默认会自增长 从0开始
     */
    public int index;

    public boolean isNativeTorrent;

    private Request(Builder builder) {
        this.url = builder.url;
        this.savePath = builder.savePath;
        this.saveFileName = builder.saveFileName;
        this.uuid = builder.uuid;
        this.index = builder.index;
        this.isNativeTorrent = builder.isNativeTorrent;
    }

    public static final class Builder {

        public Builder url(String url) {
            this.url = url;
            return this;
        }

        public Builder savePath(String savePath) {
            this.savePath = savePath;
            return this;
        }

        public Builder savedFileName(String saveFileName) {
            this.saveFileName = saveFileName;
            return this;
        }

        public Builder uuid(String uuid) {
            this.uuid = uuid;
            return this;
        }

        public Builder index(int index) {
            this.index = index;

            return this;
        }

        String url;
        String savePath;
        String saveFileName;
        String uuid;
        int index;
        boolean isNativeTorrent;


        public Request build() {
            if (TextUtils.isEmpty(url)) {
                throw new RuntimeException("url null !");
            }
            if (TextUtils.isEmpty(uuid)) {

                if (url.startsWith("http") || url.startsWith("https")) {
                    try {
                        int start = url.lastIndexOf("/") + 1;
                        int end = url.lastIndexOf(".");
                        uuid = url.substring(start, end);
                    } catch (Exception e) {
                        throw new RuntimeException("error torrent url");
                    }

                } else if (url.startsWith("magnet")) {
                    throw new RuntimeException(" magnet not support!!!");
                } else if (url.endsWith("torrent")) {
                    try {
                        int start = url.lastIndexOf("/") + 1;
                        int end = url.lastIndexOf(".");
                        uuid = url.substring(start, end);

                    } catch (Exception e) {
                        throw new RuntimeException("error torrent url");
                    }
                    this.isNativeTorrent = true;

                } else {
                    throw new RuntimeException("error torrent url ");
                }

            }

            if (TextUtils.isEmpty(savePath)){
                //未设置保存路径时 默认放在外置存储中，使用时确保有存储卡的读写权限
                savePath = Environment.getExternalStorageDirectory().getAbsolutePath();
            }


            return new Request(this);
        }


    }

    @Override
    public String toString() {
        return "Request{" +
                "url='" + url + '\'' +
                ", savePath='" + savePath + '\'' +
                ", savedFileName='" + saveFileName + '\'' +
                ", uuid='" + uuid + '\'' +
                ", index=" + index +
                ", isNativeTorrent=" + isNativeTorrent +
                '}';
    }
}
