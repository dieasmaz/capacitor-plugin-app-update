package com.mazsoft.app.update;

import android.annotation.SuppressLint;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;
import androidx.core.app.NotificationCompat;
import com.mazsoft.app.update.capacitorpluginappupdate.R;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.ref.WeakReference;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;

public class DownloadFileFromURL extends AsyncTask<String, Integer, DownLoadFileResult> {
    private NotificationManager mNotifyManager;
    private NotificationCompat.Builder build;
    private int id = 2801;
    private OutputStream output;
    @SuppressLint("StaticFieldLeak")
    private Context context;
    private String fileUrl;
    private AuthenticationOptions authentication = null;
    private WeakReference<DownloadFileInformer> downloadFileInformerWeakReference;
    private DownLoadFileResult downLoadFileResult;

    DownloadFileFromURL(Context context, DownloadFileInformer downloadFileInformer) {
        this.context = context;
        this.downloadFileInformerWeakReference = new WeakReference<>(downloadFileInformer);
        this.downLoadFileResult = new DownLoadFileResult();
    }

    protected void onPreExecute() {
        super.onPreExecute();

        mNotifyManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        build = new NotificationCompat.Builder(context, "2801");
        String channelId = "2801";
        build.setContentTitle("Download")
                .setContentText("Download in progress")
                .setChannelId(channelId + "")
                .setAutoCancel(false)
                .setDefaults(0)
                .setSmallIcon(R.drawable.ic_menu_download);

        // Since android Oreo notification channel is needed.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(channelId + "",
                    "App Downloader",
                    NotificationManager.IMPORTANCE_HIGH);
            channel.setDescription("no sound");
            channel.setSound(null, null);
            channel.enableLights(true);
            channel.setLightColor(Color.BLUE);
            channel.enableVibration(false);

            mNotifyManager.createNotificationChannel(channel);

        }
        build.setProgress(100, 0, false);
        mNotifyManager.notify(id, build.build());

        Toast.makeText(context, "Iniciando descarga...", Toast.LENGTH_LONG).show();
    }

    private void prepareOutputStream(String... params) throws FileNotFoundException {
        String fileName = params[1];
        String folderName = null;

        if(params.length > 2) {
            folderName = params[2];
        }

        if(folderName != null && folderName.length() > 0) {
            fileUrl = Environment.getExternalStorageDirectory().toString() + File.separator + folderName;

            File f = new File(fileUrl);
            if(!f.exists()) {
                //Log.i("prepareOutputStream", "folder does not exits, creating folder...");

                if(!f.mkdir()) {
                    throw new FileNotFoundException("The folder '" + folderName +"' was not created...");
                }
            }

            fileUrl = fileUrl + File.separator + fileName;

            output = new FileOutputStream(fileUrl);
        } else {
            fileUrl = Environment.getExternalStorageDirectory().toString() + File.separator + fileName;

            output = new FileOutputStream(fileUrl);
        }
    }

    private void prepareAuthentication(String... params) {
        JSONObject authenticationOptions = null;

        String authOptionsString = params[3];
        if(authOptionsString != null && authOptionsString.length() > 0) {
            try {
                authenticationOptions = new JSONObject(authOptionsString);
            } catch (JSONException jEx) {
                Log.d("authOptionsString", jEx.toString());
            }

            if(authenticationOptions != null) {
                authentication = new AuthenticationOptions(authenticationOptions);

                Log.i("authenticationOptions", authentication.toString());
            }
        }
    }

    @Override
    protected DownLoadFileResult doInBackground(String... params) {
        int count;
        HttpURLConnection connection = null;

        try {
            prepareAuthentication(params);

            String serverUrl = params[0];

            URL url = new URL(serverUrl);
            connection = (HttpURLConnection) url.openConnection();

            connection.setRequestProperty("Access-Control-Allow-Origin", "*");

            if(authentication != null && authentication.hasCredentials()) {
                connection.setRequestProperty("Authorization", this.authentication.getEncodedAuthorization());

                Log.i("Security =>", this.authentication.getEncodedAuthorization());
            }

            connection.connect();

            int lengthOfFile = connection.getContentLength();

            InputStream input = new BufferedInputStream(url.openStream(), lengthOfFile);

            // Output stream
            prepareOutputStream(params);

            byte[] data = new byte[lengthOfFile];

            long total = 0;

            while ((count = input.read(data)) != -1) {
                total += count;
                int cur = (int) ((total * 100) / lengthOfFile);

                publishProgress(Math.min(cur, 100));
                if (Math.min(cur, 100) > 98) {
                    try {
                        Thread.sleep(300);
                    } catch (InterruptedException e) {
                        Log.d("Failure", "sleeping failure");
                    }
                }

                //Log.i("currentProgress", "currentProgress: " + Math.min(cur, 100) + "\n " + cur);

                output.write(data, 0, count);
            }

            output.flush();

            output.close();
            input.close();

            Log.e("doInBackground: ", "SUCCESS!!!");

            downLoadFileResult.DownloadSuccess = true;
            downLoadFileResult.DownloadedFileUrl = fileUrl;
        }
        catch (FileNotFoundException fnfEx) {
            Log.e("doInBackground fnfEx: ", fnfEx.getLocalizedMessage());
            fnfEx.printStackTrace();

            downLoadFileResult.DownloadError = fnfEx.getMessage();
        }
        catch (Exception e) {
            Log.e("doInBackground Error: ", e.getLocalizedMessage());
            e.printStackTrace();

            downLoadFileResult.DownloadError = e.getMessage();
        } finally {
            if(connection != null) {
                connection.disconnect();
            }
        }

        return downLoadFileResult;
    }

    protected void onProgressUpdate(Integer... progress) {
        build.setProgress(100, progress[0], false);
        mNotifyManager.notify(id, build.build());
        super.onProgressUpdate(progress);
    }

    @Override
    protected void onPostExecute(DownLoadFileResult result) {
        Log.i("onPostExecute", "DownloadFileFromURL Execution complete...");

        if(result.DownloadSuccess) {
            build.setContentText("Download complete");
            build.setProgress(100, 100, false);
            mNotifyManager.notify(id, build.build());

            Toast.makeText(context, "Descarga completa...", Toast.LENGTH_LONG).show();

        }
        else {
            build.setContentText("Download fail");
            build.setProgress(100, 100, true);
            mNotifyManager.notify(id, build.build());

            Toast.makeText(context, "Error al realizar la descarga del archivo...", Toast.LENGTH_LONG).show();
        }

        final DownloadFileInformer downloadFileInformer = downloadFileInformerWeakReference.get();
        if(downloadFileInformer != null) {
            downloadFileInformer.onDownloadSuccess(result);
        }

    }

}
