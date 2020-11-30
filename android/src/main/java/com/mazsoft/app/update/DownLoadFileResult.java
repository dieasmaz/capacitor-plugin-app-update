package com.mazsoft.app.update;
import com.getcapacitor.JSObject;

public class DownLoadFileResult {
    Boolean DownloadSuccess;
    String DownloadedFileUrl;
    String DownloadError;

    DownLoadFileResult() {
        this.DownloadSuccess = false;
        this.DownloadedFileUrl = null;
        this.DownloadError = null;
    }

    public JSObject toJSON() {
        JSObject jsonObject = new JSObject();
        jsonObject.put("downloadSuccess", this.DownloadSuccess);
        jsonObject.put("downloadedFileUrl", this.DownloadedFileUrl);
        jsonObject.put("downloadError", this.DownloadError);

        return jsonObject;
    }
}
