package com.hksofttronix.khansama;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.hksofttronix.khansama.Admin.AddRecipe.AddRecipeService;
import com.hksofttronix.khansama.Admin.UpdateRecipe.UpdateRecipeService;
import com.hksofttronix.khansama.Log.sendLogService;

public class GlobalReceiver extends BroadcastReceiver {

    String tag = this.getClass().getSimpleName();
    Globalclass globalclass;

    @Override
    public void onReceive(Context context, Intent intent) {

        globalclass = Globalclass.getInstance(context);

        if (intent.getAction() != null && intent.getAction().equalsIgnoreCase(Intent.ACTION_BOOT_COMPLETED)) {
            globalclass.log(tag, "ACTION_BOOT_COMPLETED");

        }
        else if (intent.getAction() != null && intent.getAction().equalsIgnoreCase("android.intent.action.DOWNLOAD_COMPLETE")) {
            globalclass.log(tag, "DOWNLOAD_COMPLETE");
            Intent downloadCompleteIntent = new Intent(Globalclass.DownloadCompleteReceiver);
            LocalBroadcastManager.getInstance(context).sendBroadcast(downloadCompleteIntent);
        }
        else if (intent.getAction() != null && intent.getAction().equalsIgnoreCase(Globalclass.addRecipe_ACTION)) {
            globalclass.log(tag, intent.getAction());
            globalclass.startService(context, AddRecipeService.class);
        }
        else if (intent.getAction() != null && intent.getAction().equalsIgnoreCase(Globalclass.updateRecipe_ACTION)) {
            globalclass.log(tag, intent.getAction());
            globalclass.startService(context, UpdateRecipeService.class);
        }
        else if (intent.getAction() != null && intent.getAction().equalsIgnoreCase(Globalclass.SyncData_ACTION)) {
            globalclass.log(tag, intent.getAction());

            if(!globalclass.isAppIsInBackground(context) && globalclass.isInternetPresent()) {
                checkClientDataNeedToSync(context);
            }
        }
        else if (intent.getAction() != null && intent.getAction().equalsIgnoreCase(Globalclass.log_ACTION)) {
            globalclass.log(tag, intent.getAction());

            if(globalclass.isInternetPresent()) {
                globalclass.startService(context, sendLogService.class);
            }
        }
    }

    void checkClientDataNeedToSync(Context context) {

        int lastSyncDataDays = Integer.parseInt(globalclass.getTimeDifference(
                globalclass.getLongData("lastSyncDataMilliSecond"),
                globalclass.getMilliSecond()).get(0));
        globalclass.log(tag,"lastSyncDataDays: "+lastSyncDataDays);
        if(lastSyncDataDays >= globalclass.syncDataDays()) {
            globalclass.startService(context, SyncData.class);
        }
    }
}
