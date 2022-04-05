package com.hksofttronix.khansama.Log;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.CursorWindow;
import android.database.sqlite.SQLiteCursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.hksofttronix.khansama.BuildConfig;
import com.hksofttronix.khansama.Globalclass;
import com.hksofttronix.khansama.Models.logModel;
import com.hksofttronix.khansama.Mydatabase;
import com.hksofttronix.khansama.R;
import com.hksofttronix.khansama.VolleyUtil.MySingleton;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class sendLogService extends Service {

    String tag = this.getClass().getSimpleName();
    Context context = sendLogService.this;

    Globalclass globalclass;
    Mydatabase mydatabase;
    NotificationManagerCompat notificationManager;
    NotificationCompat.Builder notification;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        init();
        createAndupdateNotification();

        if (!globalclass.isInternetPresent()) {
            globalclass.log(tag, "No internet connection found, service stop!");
            stopSelf();
            return;
        }

        getAllLogs();

        globalclass.log(tag, "Started...");
    }

    void init() {
        globalclass = Globalclass.getInstance(context);
        mydatabase = Mydatabase.getInstance(context);
        notificationManager = NotificationManagerCompat.from(context);
    }

    public void getAllLogs() {

        if(BuildConfig.DEBUG) {

            globalclass.log(tag, "Not sending Logs for DEBUG!");
            stopSelf();
            return;
        }

        Cursor cursor = null;
        try {
            String query = "SELECT  * FROM " + mydatabase.log + " ";

            SQLiteDatabase db = mydatabase.getReadableDatabase();
            cursor = db.rawQuery(query, null);

            if (cursor instanceof SQLiteCursor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                ((SQLiteCursor) cursor).setWindow(new CursorWindow(null, 1024 * 1024 * 10));
            }

            if (cursor.getCount() == 0) {
                globalclass.log(tag, "No more logs are available!");
                stopSelf();
            } else {
                // looping through all rows and adding to list
                if (cursor.moveToFirst()) {
                    do {

                        logModel model = new logModel();
                        model.setLocalid(String.valueOf(cursor.getInt(cursor.getColumnIndex("localid"))));
                        model.setLogType(cursor.getString(cursor.getColumnIndex("logType")));
                        model.setUserLogin(cursor.getString(cursor.getColumnIndex("userLogin")));
                        model.setMobileNumber(cursor.getString(cursor.getColumnIndex("mobileNumber")));
                        model.setDetail(cursor.getString(cursor.getColumnIndex("detail")));

                        globalclass.log(tag, "getAllLogs: " + "logType:" + model.getLogType()
                                + ", userLogin:" + model.getUserLogin()
                                + ", mobileNumber:" + model.getMobileNumber());

                        sendLog(model);

                    } while (cursor.moveToNext());
                }
            }
        } catch (Exception e) {

            String error = Log.getStackTraceString(e);
            globalclass.log(tag, "getAllLogs: " + error);
            globalclass.sendErrorLog(tag, "getAllLogs", error);
            stopSelf();

        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    void sendLog(logModel model) {

        final String url = "http://"+Globalclass.localIP+"/Khansama/index.php/Globalclass/" + "logThis";

        final Map<String, String> params = new HashMap<String, String>();
        params.put("logType", model.getLogType());
        params.put("mobileNumber", model.getMobileNumber());
        params.put("userLogin", model.getUserLogin());
        params.put("id", model.getLocalid());
        params.put("detail", model.getDetail());

        globalclass.log(tag,"url: "+url);

        StringRequest stringRequest = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try {
                    JSONObject jsonObject = new JSONObject(response);
                    String status = jsonObject.getString("status");

                    globalclass.log(tag,response);

                    if (status.equalsIgnoreCase("success")) {
                        mydatabase.deleteData(mydatabase.log, "localid", jsonObject.getString("id"));
                    } else {
                        globalclass.log(tag,jsonObject.getString("message"));
                    }

                    stopSelf();

                } catch (JSONException e) {

                    String error = Log.getStackTraceString(e);
                    globalclass.log(tag, "JSONException: " + error);
                    stopSelf();
                }
            }

        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError e) {

                String error = Log.getStackTraceString(e);
                globalclass.log(tag, "onErrorResponse: " + error);
                stopSelf();
            }
        }) {
            @Override
            protected Map<String, String> getParams() {

                globalclass.log(tag,"params: "+params);
                return params;
            }
        };

        stringRequest.setRetryPolicy(new DefaultRetryPolicy(
                30000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        MySingleton.getInstance(context).addToRequestQueue(stringRequest);
    }

    void createAndupdateNotification() {
        notification = new NotificationCompat.Builder(this, Globalclass.logNotificationChannelId)
                .setSmallIcon(R.drawable.ic_appicon)
                .setContentTitle(context.getResources().getString(R.string.app_name))
                .setContentText("Checking error...")
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .setOnlyAlertOnce(true)
                .setOngoing(true)
                .setProgress(0, 0, true);


        startForeground(Globalclass.logNotificationId, notification.build());
        notificationManager.notify(Globalclass.logNotificationId, notification.build());
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        globalclass.setAlarm(context, Globalclass.log_alarmMin, Globalclass.log_ACTION, Globalclass.log_requestID);
        globalclass.log(tag, "Destroy...");
    }
}
