package com.mazsoft.app.update;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.util.Log;

import com.getcapacitor.JSObject;
import com.getcapacitor.NativePlugin;
import com.getcapacitor.Plugin;
import com.getcapacitor.PluginCall;
import com.getcapacitor.PluginMethod;

import java.io.File;
import androidx.core.content.FileProvider;

@NativePlugin(
        permissionRequestCode = AppUpdate.REQUEST_APP_UPDATE_CODE,
        permissions = {
                Manifest.permission.ACCESS_NETWORK_STATE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.READ_EXTERNAL_STORAGE
        }
)
public class AppUpdate extends Plugin implements DownloadFileInformer {
    static final  int REQUEST_APP_UPDATE_CODE = 2801;

    // --------------------------------------------------------------
    //                          Permissions
    // --------------------------------------------------------------

    @Override
    protected void handleRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.handleRequestPermissionsResult(requestCode, permissions, grantResults);

        Log.i("PermissionsResult","handling request perms result");

        PluginCall savedCall = getSavedCall();

        if (savedCall == null) {
            Log.e("PermissionsResult","No stored plugin call for permissions request result");
            return;
        }

        for(int result : grantResults) {
            if (result == PackageManager.PERMISSION_DENIED) {
                savedCall.error("User denied permission");
                return;
            }
        }

        if (requestCode == REQUEST_APP_UPDATE_CODE) {
            // We got the permission
            Log.i("PermissionsResult", "Request Code: " +  requestCode);

            try {
                Log.i("DownloadFileFromURL", "verifyRequestParams");

                String[] requestParameters = verifyRequestParams(savedCall);
                if(requestParameters == null) {
                    Log.i("DownloadFileFromURL", "verifyRequestParams => params null");
                    return;
                }

                Log.i("DownloadFileFromURL", "execute");
                DownloadFileFromURL downloadFileTask = new DownloadFileFromURL(getContext(), this);

                downloadFileTask.execute(requestParameters);

            } catch (Exception e) {
                Log.v("DownloadFileFromURL", e.toString());
                savedCall.error(e.getMessage());
            }
        }
    }

    // --------------------------------------------------------------
    //                          Methods
    // --------------------------------------------------------------

    @PluginMethod
    public void echo(PluginCall call) {
        String value = call.getString("value");

        JSObject ret = new JSObject();
        ret.put("value", value);
        call.success(ret);
    }

    @PluginMethod
    public void runAutoUpdate(PluginCall call) {
        saveCall(call);

        pluginRequestAllPermissions();
    }

    private String[] verifyRequestParams(PluginCall call) {
        String[] parameters = new String[4];
        String serverFileUrl = call.getString("serverUrl");
        if(serverFileUrl == null || serverFileUrl.length() == 0) {
            call.error("Must provide a valid SERVER URL (Parameter 'serverUrl')");
            return null;
        }
        parameters[0] = serverFileUrl;

        String fileName = call.getString("fileName");
        if(fileName == null || fileName.length() == 0) {
            call.error("Must provide a valid FILE NAME (Parameter 'fileName')");
            return null;
        }
        parameters[1] = fileName;

         //Optional Parameter
        String folderName = call.getString("folderName");
        if(folderName != null && folderName.length() > 0) {
            parameters[2] = folderName;
        }

        JSObject authenticationOptions = call.getObject("authenticationOptions");
        if(authenticationOptions != null) {
            parameters[3] = authenticationOptions.toString();
        }

        return parameters;
    }

    @Override
    public void onDownloadSuccess(DownLoadFileResult downLoadFileResult) {
        Log.i("onDownloadSuccess", "DownloadFileInformer => onDownloadSuccess " + downLoadFileResult);

        if(downLoadFileResult.DownloadSuccess) {
            File apkFile = new File(downLoadFileResult.DownloadedFileUrl);

            // IF SDK IS EQUALS OR GREATER THAN NOUGAT
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                Log.d("AppUpdate", "Build SDK Greater than or equal to Nougat");

                Uri apkUri = FileProvider.getUriForFile(getContext(), getAppId() + ".fileprovider", apkFile );

                Intent intent = new Intent(Intent.ACTION_INSTALL_PACKAGE);
                intent.setData(apkUri);
                intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                getBridge().startActivityForPluginWithResult(getSavedCall(), intent, AppUpdate.REQUEST_APP_UPDATE_CODE);
            }
            else {
                Log.d("AppUpdate", "Build SDK less than Nougat");
                Intent i = new Intent(Intent.ACTION_VIEW);
                i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                i.setDataAndType(Uri.parse("file://" + apkFile.toString()), "application/vnd.android.package-archive");

                getContext().startActivity(i);
            }

            getSavedCall().resolve(downLoadFileResult.toJSON());
        } else {
            getSavedCall().error(downLoadFileResult.DownloadError);
        }
    }
}
