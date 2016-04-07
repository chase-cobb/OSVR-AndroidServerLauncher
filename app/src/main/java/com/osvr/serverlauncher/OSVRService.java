package com.osvr.serverlauncher;


import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import com.osvr.android.utils.OSVRFileExtractor;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.util.Map;
import java.util.Scanner;

/**
 * Created by busdr on 3/30/2016.
 */
public class OSVRService extends Service {

    private final static String LOGTAG = "OSVRService";

    private Process process;

    final String serverBin = "/data/data/com.osvr.serverlauncher/files/bin/osvr_server";
    final String serverDir = "/data/data/com.osvr.serverlauncher/files/bin";

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        if(null != process) {
            Log.d(LOGTAG, "Process exit status : " + process.exitValue());
            process.destroy();
            Log.d(LOGTAG, "Process exit status : " + process.exitValue());
        }

        Toast.makeText(this, "OSVR Service Stopped", Toast.LENGTH_LONG).show();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        //return super.onStartCommand(intent, flags, startId);

        Log.d(LOGTAG, "OSVRService.onStartCommand begin");
        try {
            OSVRFileExtractor.extractFiles(this);
            doChmod();
            String[] args = {serverBin};
            ProcessBuilder processBuilder = new ProcessBuilder(args)
                    .directory(new File(serverDir));
            processBuilder.redirectErrorStream(true);

            Map<String, String> environment = processBuilder.environment();

            // TODO : iterate environment and print all entries.

            environment.put("LD_LIBRARY_PATH", "/data/data/com.osvr.serverlauncher/files/lib");

            process = processBuilder.start();
            //OSVRFileExtractor.inheritProcessIO(process);

            Log.d(LOGTAG, "Server process started : " + process.toString());

        } catch(Exception ex) {
            Log.e(LOGTAG, "Error when starting process: " + ex.getMessage());
        }finally {
            Toast.makeText(this, "OSVR Service Started", Toast.LENGTH_LONG).show();
            Log.d(LOGTAG, "OSVRService.onStartCommand end");
        }

        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    protected void doChmod() {
        Log.d(LOGTAG, "OSVRService.doChmod begin");
        String[] args = {"chmod", "775", serverBin};
        ProcessBuilder processBuilder = new ProcessBuilder(args)
                .directory(new File(serverDir));
        try {
            Process chmodProcess = processBuilder.start();
            OSVRFileExtractor.inheritProcessIO(chmodProcess);
            chmodProcess.waitFor();

        } catch(IOException ex) {
            Log.e(LOGTAG, "Error when starting chmod: " + ex.getMessage());
        } catch(InterruptedException ex) {
            Log.e(LOGTAG, "Error when starting process: " + ex.getMessage());
        }
        Log.d(LOGTAG, "OSVRService.doChmod end");
    }
}
