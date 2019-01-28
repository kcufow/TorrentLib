package com.kcufow.torrentlib;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.ldw.torrentclient.Call;
import com.ldw.torrentclient.Callback;
import com.ldw.torrentclient.Request;
import com.ldw.torrentclient.TorrentClient;

import java.io.File;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    private static final String TAG = "MainActivity";
    private static final int CODE_REQUEST_PERMISSION = 100;
    private TorrentClient mClient;
    private Call mCall;
    private float preProgress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();
        initPermission();
//        initData();
    }

    private void initPermission() {
        if (Build.VERSION.SDK_INT<Build.VERSION_CODES.M)return;
        if (ActivityCompat.checkSelfPermission(this,Manifest.permission.WRITE_EXTERNAL_STORAGE)
                !=PackageManager.PERMISSION_GRANTED){

            requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE
                    ,Manifest.permission.READ_EXTERNAL_STORAGE},CODE_REQUEST_PERMISSION);

        }else {
            createFile();
        }

    }

    private void createFile() {
        File file = new File("/mnt/media_rw/sda1/dcp/torrent/test");
        if (!file.exists()){
            if (!file.mkdirs()){
                Log.e(TAG, "file 创建失败 " );
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions
            , @NonNull int[] grantResults) {

        if (requestCode == CODE_REQUEST_PERMISSION){

            if (grantResults.length>0 && grantResults[0]==PackageManager.PERMISSION_GRANTED){
               createFile();
            }
        }


        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    private void initView() {
        Button pause = (Button) findViewById(R.id.btn_pause);
        Button resum = (Button) findViewById(R.id.btn_resume);
        Button stop = (Button) findViewById(R.id.btn_stop);
        Button start = (Button) findViewById(R.id.btn_start);
        pause.setOnClickListener(this);
        resum.setOnClickListener(this);
        stop.setOnClickListener(this);
        start.setOnClickListener(this);

    }

    private void initData() {
        mClient = new TorrentClient();

        Request request = new Request.Builder()
                .url("http://192.168.1.131/video/fengrenjiledui.mp4.torrent")
                .savePath("/mnt/media_rw/sda1/dcp/torrent")
                .savedFileName("gongfuxiongmao3.mp4")
                .build();
        mCall = mClient.newCall(request);
        mCall.enqueue(new Callback() {
            @Override
            public void onFailed(Request request, Exception e) {
                Log.e(TAG, "onFailed: ", e);
            }

            @Override
            public void onTorrentDownload(Request request) {
                Log.e(TAG, "onTorrentDownload: ");

            }

            @Override
            public void onPrepare(Request request) {
                Log.e(TAG, "onPrepare: ");

            }

            @Override
            public void onStart(Request request) {
                Log.e(TAG, "onStart: ");
            }

            @Override
            public void notifyProgress(Request request, float progress, int downloadRate, long totalDownload) {
                if (preProgress ==progress){
                    return;
                }
                Log.i(TAG, "notifyProgress: " + " progress :" + progress + " -- downloadRate :" + downloadRate + "---" +
                        "totalDownload:" + totalDownload);
            }

            @Override
            public void onStop(Request request) {
                Log.e(TAG, "onStop: ");

            }

            @Override
            public void onCompleted(Request request) {
                Log.e(TAG, "onCompleted: ");
            }

            @Override
            public void onPause(Request request) {
                Log.e(TAG, "onPause: ");
            }
        });


    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mCall != null) {
            mCall.cancel();
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){

            case R.id.btn_start:
             initData();
                break;
            case R.id.btn_pause:
                if (mCall != null) {
                    mCall.pause();
                }
                break;
            case R.id.btn_resume:
                if (mCall != null) {
                    mCall.resume();
                }
                break;
            case R.id.btn_stop:
                if (mCall != null) {
                    mCall.cancel();
                }
                break;

        }

    }
}
