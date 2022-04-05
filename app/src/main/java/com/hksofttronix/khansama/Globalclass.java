package com.hksofttronix.khansama;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.AlarmManager;
import android.app.Dialog;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.messaging.FirebaseMessaging;
import com.hksofttronix.khansama.Admin.AddRecipe.AddRecipeService;
import com.hksofttronix.khansama.Admin.UpdateRecipe.UpdateRecipeService;
import com.hksofttronix.khansama.Models.logModel;
import com.hksofttronix.khansama.Models.userDetail;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.TimeZone;

public class Globalclass {

    String tag = this.getClass().getSimpleName();
    private static volatile Globalclass globalClass;
    private Context context;

    SharedPreferences preferences, otpVerificationPreferences;
    SharedPreferences.Editor editor, otpVerificationEditor;
    private static final String PreferanceName = "Recipe";
    private static final String OtpVerificationPrefName = "OtpVerificationPref";

    FirebaseFirestore firebaseFirestore;
    public static final String appInfoColl = "appInfoColl";
    public static final String userColl = "userColl";
    public static final String unitColl = "unitColl";
    public static final String inventoryColl = "inventoryColl";
    public static final String stockColl = "stockColl";
    public static final String recipeColl = "recipeColl";
    public static final String ingredientsColl = "ingredientsColl";
    public static final String recipeImagesColl = "recipeImagesColl";
    public static final String purchaseColl = "purchaseColl";
    public static final String vendorColl = "vendorColl";
    public static final String recipeCategoryColl = "recipeCategoryColl";
    public static final String cartColl = "cartColl";
    public static final String favouriteColl = "favouriteColl";
    public static final String cityColl = "cityColl";
    public static final String stateColl = "stateColl";
    public static final String addressColl = "addressColl";
    public static final String placeOrderColl = "placeOrderColl";
    public static final String orderItemColl = "orderItemColl";
    public static final String orderStatusLogColl = "orderStatusLogColl";

    //todo channel & notifications
    public static final String appNotificationChannelId = "Recipe";
    public static final String appNotificationChannelName = "Recipe";
    public static final int appNotificationId = 101;

    public static final String addRecipeChannelId = "Add Recipe";
    public static final String addRecipeChannelName = "Add Recipe";
    public static final int addRecipeNotificationId = 102;

    public static final String updateRecipeChannelId = "Update Purchase Item";
    public static final String updateRecipeChannelName = "Update Purchase Item";
    public static final int updateRecipeNotificationId = 104;

    public static final String SyncDataChannelId = "Sync data";
    public static final String SyncDataChannelName = "Sync data";
    public static final int SyncDataNotificationId = 105;

    public static final String appAdminNotificationChannelId = "Admin Recipe";
    public static final String appAdminNotificationChannelName = "Admin Recipe";
    public static final int appAdminNotificationId = 106;

    public static final String logNotificationChannelId = "Check error";
    public static final String logNotificationChannelName = "Check error";
    public static final int logNotificationId = 107;

    //todo alarm Actions
    public static final String addRecipe_ACTION = "addRecipe_ACTION";
    public static final int addRecipe_requestID = 201;
    public static final int addRecipe_alarmMin = 5;

    public static final String updateRecipe_ACTION = "updatePurchaseItem_ACTION";
    public static final int updateRecipe_requestID = 203;
    public static final int updateRecipe_alarmMin = 5;

    public static final String SyncData_ACTION = "SyncData_ACTION";
    public static final int SyncData_requestID = 204;
    public static final int SyncData_alarmMin = 5;

    public static final String log_ACTION = "log_ACTION";
    public static final int log_requestID = 204;
    public static final int log_alarmMin = 30;

    //todo failed Notifications ID
    public static final int addRecipeFailedNotifiID = 301;
    public static final int updateRecipeFailedNotifiID = 303;
    public static final int SyncDataFailedNotifiID = 304;

    //todo local Broadcast Receiver
    public static final String SyncDataReceiver = "SyncDataReceiver";
    public static final String AddRecipeReceiver = "AddRecipeReceiver";
    public static final String OrderReceiver = "OrderReceiver";
    public static final String DownloadCompleteReceiver = "DownloadCompleteReceiver";

    //todo local IP and PORT
    public static final String localIP = "192.168.0.69";
    public static final int localAuthPORT = 1010;
    public static final int localFireStorePORT = 3030;

    public static final String appFolderPath = "/KhanSama/";

    //todo log type
    String ErrorLog = "ErrorLog";
    String ResponseErrorLog = "ResponseErrorLog";
    String NotificationErrorLog = "NotificationErrorLog";

    public Globalclass(Context context) {

        this.context = context;

        preferences = context.getSharedPreferences(PreferanceName, 0);
        editor = preferences.edit();

        otpVerificationPreferences = context.getSharedPreferences(OtpVerificationPrefName, 0);
        otpVerificationEditor = otpVerificationPreferences.edit();
    }

    public static Globalclass getInstance(Context context) {

        if (globalClass == null) { //Check for the first time

            synchronized (Globalclass.class) {   //Check for the second time.
                //if there is no instance available... create new one
                if (globalClass == null) globalClass = new Globalclass(context);
            }
        }

        return globalClass;
    }

    // todo PrefStringData
    public void setStringData(String setkey, String setvalue) {
        editor.putString(setkey, setvalue).commit();
    }

    public String getStringData(String getkey) {
        return preferences.getString(getkey, "");
    }

    public String getStringDataWithD(String getkey, String defValue) {
        return preferences.getString(getkey, defValue);
    }

    //todo PrefIntData
    public void setIntData(String setkey, int setvalue) {
        editor.putInt(setkey, setvalue).commit();
    }

    public int getIntData(String getkey) {
        return preferences.getInt(getkey, 0);
    }

    //todo PrefBooleanData
    public void setBooleanData(String setkey, Boolean setvalue) {
        editor.putBoolean(setkey, setvalue).commit();
    }

    public boolean getBooleanData(String getkey) {
        return preferences.getBoolean(getkey, false);
    }

    //todo PrefLongData
    public void setLongData(String setkey, Long setvalue) {
        editor.putLong(setkey, setvalue).commit();
    }

    public long getLongData(String getkey) {
        return preferences.getLong(getkey, 0);
    }

    public void clearPref() {
        SharedPreferences settings = context.getSharedPreferences(PreferanceName, Context.MODE_PRIVATE);
        settings.edit().clear().apply();

        //todo to clear notification settings pref
        PreferenceManager.getDefaultSharedPreferences(context).edit().clear().apply();
    }

    //todo Otp Verification Preference
    // todo PrefStringData
    public void setStringDataForOtp(String setkey, String setvalue) {
        otpVerificationEditor.putString(setkey, setvalue).commit();
    }

    public String getStringDataForOtp(String getkey) {
        return otpVerificationPreferences.getString(getkey, "");
    }

    //todo PrefLongData
    public void setLongDataForOtp(String setkey, Long setvalue) {
        otpVerificationEditor.putLong(setkey, setvalue).commit();
    }

    public long getLongDataForOtp(String getkey) {
        return otpVerificationPreferences.getLong(getkey, 0);
    }

    //todo appInfo start...

    public int syncDataDays() {
        return Integer.parseInt(getStringDataWithD("syncDataDays", "1"));
    }

    public int verifyMobileNumberDays() {
        return Integer.parseInt(getStringDataWithD("verifyMobileNumberDays", "3"));
    }

    public int maxFavouriteCount() {
        return Integer.parseInt(getStringDataWithD("maxFavouriteCount", "5"));
    }

    public int maxCartCount() {
        return Integer.parseInt(getStringDataWithD("maxCartCount", "3"));
    }

    public int maxCartQuantity() {
        return Integer.parseInt(getStringDataWithD("maxCartQuantity", "5"));
    }

    public int maxAddressCount() {
        return Integer.parseInt(getStringDataWithD("maxAddressCount", "5"));
    }

    public int maxRecipeImages() {
        return Integer.parseInt(getStringDataWithD("maxRecipeImages", "6"));
    }

    public int deletePurchaseDays() {
        return Integer.parseInt(getStringDataWithD("deletePurchaseDays", "0"));
    }

    public String contactUsEmailId() {
        return getStringDataWithD("contactUsEmailId", "info@hkworld.com");
    }

    public String contactUsNumber() {
        return getStringDataWithD("contactUsNumber", "9687149131");
    }

    public String appShareImageUrl() {
        return getStringDataWithD("appShareImageUrl", "https://firebasestorage.googleapis.com/v0/b/khansama-hk.appspot.com/o/ic_appicon.png?alt=media&token=d6fd684c-2804-4a0b-8fb0-83a234b6c11d");
    }

    //todo appInfo end...

    public void log(String tag, String msg) {
        Log.e(tag, msg);
    }

    public void toast_short(String text) {
        Toast.makeText(context, text, Toast.LENGTH_SHORT).show();
    }

    public void toast_long(String text) {
        Toast.makeText(context, text, Toast.LENGTH_LONG).show();
    }

    public void snackit(Activity activity, String text) {
        try {
            Snackbar.make(activity.findViewById(android.R.id.content),
                    "" + text, Snackbar.LENGTH_LONG).show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void snackitForFab(View fab, String text) {
        try {
            Snackbar.make(fab,
                    "" + text, Snackbar.LENGTH_LONG).show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void hideKeyboard(Activity activity) {
        View view = activity.findViewById(android.R.id.content);
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    public void hidekeyboard_dialogue(Dialog dialog) {
        if (dialog.getCurrentFocus() != null) {
            InputMethodManager inputManager = (InputMethodManager) Objects.requireNonNull(context).getSystemService(Context.INPUT_METHOD_SERVICE);
            inputManager.hideSoftInputFromWindow(dialog.getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
        }
    }

    public String checknull(String value) {
        if (value == null) {
            return "";
        }

        String rvalue = value;
        if (TextUtils.isEmpty(value.toString().trim()) || value.toString().trim().equals("null")) {
            rvalue = "";
        }
        return rvalue;
    }

    public void showDialogue(Activity activity, String title, String message) {
        new MaterialAlertDialogBuilder(activity, R.style.RoundShapeTheme)
                .setTitle(title)
                .setMessage(message)
                .setCancelable(false)
                .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
//                .setNeutralButton("Cancel", new DialogInterface.OnClickListener() {
//                    @Override
//                    public void onClick(DialogInterface dialog, int which) {
//
//                    }
//                })
                .show();

    }

    public boolean isInternetPresent() {
        ConnectivityManager cm =
                (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        boolean isConnected = activeNetwork != null &&
                activeNetwork.isConnectedOrConnecting();

        return isConnected;
    }

    public String alphaNumericRegexOne() {
        return "^[a-zA-Z0-9\\d-._\n ]+$";
    }

    public String alphaRegexTwo() {
        //only A to Z and dash
        return "^[a-zA-Z- ]+$";
    }

    public String addressRegex() {
        return "^[a-zA-Z0-9\\d-.,\n ]+$";
    }

    public static Calendar getCalendar() {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeZone(TimeZone.getTimeZone("Asia/Calcutta"));
        return calendar;
    }

    public Date getDate() {
        return getCalendar().getTime();
    }

    public long getMilliSecond() {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeZone(TimeZone.getTimeZone("Asia/Calcutta"));
        return calendar.getTimeInMillis();
    }

    public String milliSecondToDateFormat(long milliSeconds, String dateFormat) {
        // Create a DateFormatter object for displaying date in specified format.
        SimpleDateFormat formatter = new SimpleDateFormat(dateFormat);

        // Create a calendar object that will convert the date and time value in milliseconds to date.
        Calendar calendar = getCalendar();
        calendar.setTimeInMillis(milliSeconds);
        return formatter.format(calendar.getTime());
    }

    public String changeDateFormat(String defaultPattern, String neededPattern, String input) {

//        String inputPattern = "yyyy-mm-dd";
//        String outputPattern = "dd/mm/yyyy";

        String inputPattern = defaultPattern;
        String outputPattern = neededPattern;
        SimpleDateFormat inputFormat = new SimpleDateFormat(inputPattern);
        SimpleDateFormat outputFormat = new SimpleDateFormat(outputPattern);

        Date date = null;
        String str = null;

        try {
            date = inputFormat.parse(input);
            str = outputFormat.format(date);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return str;
    }

    public void createNotificationChannel() {

        createAppNotificationChannel();
        createAddRecipeChannel();
        createUpdateRecipeChannel();
        createSyncDataChannel();
        createAppAdminNotificationChannel();
        createLogNotificationChannel();
    }

    void createAppNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel serviceChannel = new NotificationChannel(
                    appNotificationChannelId,
                    appNotificationChannelName,
                    NotificationManager.IMPORTANCE_DEFAULT
            );

            NotificationManager manager = context.getSystemService(NotificationManager.class);
            manager.createNotificationChannel(serviceChannel);
        }
    }

    void createAddRecipeChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel serviceChannel = new NotificationChannel(
                    addRecipeChannelId,
                    addRecipeChannelName,
                    NotificationManager.IMPORTANCE_DEFAULT
            );

            NotificationManager manager = context.getSystemService(NotificationManager.class);
            manager.createNotificationChannel(serviceChannel);
        }
    }

    void createUpdateRecipeChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel serviceChannel = new NotificationChannel(
                    updateRecipeChannelId,
                    updateRecipeChannelName,
                    NotificationManager.IMPORTANCE_DEFAULT
            );

            NotificationManager manager = context.getSystemService(NotificationManager.class);
            manager.createNotificationChannel(serviceChannel);
        }
    }

    void createSyncDataChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel serviceChannel = new NotificationChannel(
                    SyncDataChannelId,
                    SyncDataChannelName,
                    NotificationManager.IMPORTANCE_LOW
            );

            NotificationManager manager = context.getSystemService(NotificationManager.class);
            manager.createNotificationChannel(serviceChannel);
        }
    }

    void createAppAdminNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel serviceChannel = new NotificationChannel(
                    appAdminNotificationChannelId,
                    appAdminNotificationChannelName,
                    NotificationManager.IMPORTANCE_DEFAULT
            );

            NotificationManager manager = context.getSystemService(NotificationManager.class);
            manager.createNotificationChannel(serviceChannel);
        }
    }

    void createLogNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel serviceChannel = new NotificationChannel(
                    logNotificationChannelId,
                    logNotificationChannelName,
                    NotificationManager.IMPORTANCE_LOW
            );

            NotificationManager manager = context.getSystemService(NotificationManager.class);
            manager.createNotificationChannel(serviceChannel);
        }
    }

    public void cancelAlarm(Context context, String ACTION, int requestID) {
        Intent intent = new Intent(context, GlobalReceiver.class);
        intent.setAction(ACTION);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context,
                requestID, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        alarmManager.cancel(pendingIntent);
    }

    public void setAlarm(Context context, int time, String ACTION, int requestID) {
        cancelAlarm(context, ACTION, requestID);

        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Calendar calendar = getCalendar();

//        if(BuildConfig.DEBUG)
//        {
//            calendar.add(Calendar.SECOND,10);
//        }
//        else
//        {
//            calendar.add(Calendar.MINUTE,time);
//        }

        calendar.add(Calendar.MINUTE, time);


        Intent alarmIntent = new Intent(context, GlobalReceiver.class);
        alarmIntent.setAction(ACTION);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, requestID, alarmIntent, 0);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            alarmManager.setExact(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);
        } else {
            alarmManager.set(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);
        }

        log(tag + "_setAlarm", "Of " + ACTION + " ON - " + milliSecondToDateFormat(calendar.getTimeInMillis(), "dd-MM-yyyy hh:mm:ss aa"));
    }

    public boolean isMyServiceRunning(Context context, Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

    public void startService(Context context, Class aClass) {
        if (!isMyServiceRunning(context, aClass)) {
            Intent intent = new Intent(context, aClass);
            ContextCompat.startForegroundService(context, intent);
        }
    }

    public void stopService(Context context, Class aClass) {
        Intent intent = new Intent(context, aClass);
        context.stopService(intent);
    }

    public FirebaseFirestore firebaseInstance() {
        firebaseFirestore = FirebaseFirestore.getInstance();
        return firebaseFirestore;
    }

    public String firstLetterCapital(String text) {
        return text.substring(0, 1).toUpperCase() +
                text.substring(1);
    }

    public Date stringToDate(String text, String outPutFormat) {
        SimpleDateFormat format = new SimpleDateFormat(outPutFormat);
        try {
            Date date = format.parse(text);
            return date;
        } catch (ParseException e) {
            e.printStackTrace();
        }

        return null;
    }

    public String dateToString(Date date, String outPutFormat) {
        SimpleDateFormat dateFormat = new SimpleDateFormat(outPutFormat);
        String dateTime = dateFormat.format(date);
        return dateTime;
    }

    public String getDatetime(long milliSeconds, String dateFormat) {
        // Create a DateFormatter object for displaying date in specified format.
        SimpleDateFormat formatter = new SimpleDateFormat(dateFormat);

        // Create a calendar object that will convert the date and time value in milliseconds to date.
        Calendar calendar = Globalclass.getCalendar();
        calendar.setTimeInMillis(milliSeconds);
        return formatter.format(calendar.getTime());
    }

    public ArrayList<String> getTimeDifference(long time1, long time2) {
//        globalclass.log(tag,"Day diff: "+globalclass.getTimeDifference(
//                globalclass.millisecondsFromDate("28 Mar 2021 08:00 PM","dd MMM yyyy hh:mm aa"),
//                globalclass.getMilliSecond()).get(0));

        ArrayList<String> arrayList = new ArrayList<>();
        arrayList.clear();

        Long last_time = time1;
        Long current_time = time2;
        Long different = current_time - last_time;

        long secondsInMilli = 1000;
        long minutesInMilli = secondsInMilli * 60;
        long hoursInMilli = minutesInMilli * 60;
        long daysInMilli = hoursInMilli * 24;

        long elapsedDays = different / daysInMilli;
        different = different % daysInMilli;

        long elapsedHours = different / hoursInMilli;
        different = different % hoursInMilli;

        long elapsedMinutes = different / minutesInMilli;
        different = different % minutesInMilli;

        long elapsedSeconds = different / secondsInMilli;

        arrayList.add(String.valueOf(elapsedDays));
        arrayList.add(String.valueOf(elapsedHours));
        arrayList.add(String.valueOf(elapsedMinutes));
        arrayList.add(String.valueOf(elapsedSeconds));

        String finalvalue = elapsedDays + "day " + elapsedHours + "hr " + elapsedMinutes + "min " + elapsedSeconds + "sec";
        log("getTimeDifference", finalvalue);
        arrayList.add(finalvalue);

        return arrayList;
    }

    public long millisecondsFromDate(String date, String format) {
        //String date_ = date;
        SimpleDateFormat sdf = new SimpleDateFormat(format);
        try {
            Date mDate = sdf.parse(date);
            long timeInMilliseconds = mDate.getTime();
            return timeInMilliseconds;
        } catch (ParseException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        return 0;
    }

    public boolean isAppIsInBackground(Context context) {
        boolean isInBackground = true;
        ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.KITKAT_WATCH) {
            List<ActivityManager.RunningAppProcessInfo> runningProcesses;
            if (am != null) {
                runningProcesses = am.getRunningAppProcesses();
                for (ActivityManager.RunningAppProcessInfo processInfo : runningProcesses) {
                    if (processInfo.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND) {
                        for (String activeProcess : processInfo.pkgList) {
                            if (activeProcess.equals(context.getPackageName())) {
                                isInBackground = false;
                            }
                        }
                    }
                }
            }

        } else {
            List<ActivityManager.RunningTaskInfo> taskInfo;
            if (am != null) {
                taskInfo = am.getRunningTasks(1);
                ComponentName componentInfo = taskInfo.get(0).topActivity;
                if (componentInfo.getPackageName().equals(context.getPackageName())) {
                    isInBackground = false;
                }
            }

        }

        return isInBackground;
    }

    public void subscribeToTopic(String topic) {
        FirebaseMessaging.getInstance().subscribeToTopic(topic)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (!task.isSuccessful()) {
                            String error = task.getException().toString();
                            log(tag, "subscribeToTopic: " + error);
                            sendErrorLog(tag, "subscribeToTopic", error);
                        } else {
                            log(tag, "subscribeToTopic: " + topic);
                        }
                    }
                });
    }

    public void unsubscribeToTopic(final String topic) {
        FirebaseMessaging.getInstance().unsubscribeFromTopic(topic)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {

                        if (!task.isSuccessful()) {
                            String error = task.getException().toString();
                            log(tag, "unsubscribeToTopic: " + error);
                            sendErrorLog(tag, "unsubscribeToTopic", error);
                        } else {
                            log(tag, "unsubscribeToTopic: " + topic);
                        }
                    }
                });
    }

    public boolean checkUserIsLoggedIn() {
        if (checknull(getStringData("id")).equalsIgnoreCase("")) {
            return false;
        }

        return true;
    }

    public void showLoginDialogue(Activity activity) {
        new MaterialAlertDialogBuilder(activity, R.style.RoundShapeTheme)
                .setTitle("Alert")
                .setMessage("Please login to your account to continue...")
                .setCancelable(false)
                .setPositiveButton("Login now", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        activity.startActivity(new Intent(activity, Login.class));
                    }
                })
                .setNeutralButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .show();
    }

    public void setLastSyncDataTime() {

        setLongData("lastSyncDataMilliSecond", getMilliSecond());
        setStringData("lastSyncDataDateTime",
                getDatetime(getMilliSecond(), "dd MMM yyyy hh:mm aa"));
    }

    public void setVerifiedMobileNumber(String mobileNumber) {

        if (!checknull(mobileNumber).equalsIgnoreCase("")) {
            setLongDataForOtp(mobileNumber + "_verifiedMilliSecond", getMilliSecond());
            setStringDataForOtp(mobileNumber + "_verifiedDateTime",
                    getDatetime(getMilliSecond(), "dd MMM yyyy hh:mm aa"));
            log(tag, "New setVerifiedMobileNumber is set for " + mobileNumber);
        } else {
            String error = "setVerifiedMobileNumber: mobileNumber is null";
            log(tag, error);
            sendErrorLog(tag, "setVerifiedMobileNumber", error);
        }
    }

    public boolean verificationNeeded(String mobileNumber) {

        if (checknull(String.valueOf(getLongDataForOtp(mobileNumber + "_verifiedMilliSecond")))
                .equalsIgnoreCase("")) {
            return true;
        }

        int diff = Integer.parseInt(
                getTimeDifference(getLongDataForOtp(mobileNumber + "_verifiedMilliSecond")
                        , getMilliSecond()).get(0));
        if (diff <= verifyMobileNumberDays()) {
            log(tag, mobileNumber + " verified before " + diff + " days!");
            return false;
        } else {
            log(tag, mobileNumber + " verified before " + diff + " days!");
            return true;
        }
    }

    public File appFolderPath() {
        File file = new File(Environment.getExternalStorageDirectory().getAbsolutePath() +
                Globalclass.appFolderPath);
        if (!file.exists()) {
            file.mkdirs();
        }

        return file;
    }

    public void startAllAdminServices(Activity activity) {

        if (Mydatabase.getInstance(context).recipePhotoPendingToUpload() > 0) {
            startService(activity, AddRecipeService.class);
        }

        if (Mydatabase.getInstance(context).recipeDetailPendingToUpdate() > 0) {
            startService(activity, UpdateRecipeService.class);
        }
    }

    //todo sendLog
    public String getDeviceInfo() {
        JSONObject deviceInfoObj = new JSONObject();
        try {

            deviceInfoObj.put("Device Name", "" + Build.MANUFACTURER);
            deviceInfoObj.put("Device Model", "" + Build.MODEL);
            deviceInfoObj.put("Android Version", "" + Build.VERSION.RELEASE);
            deviceInfoObj.put("App Version", "" + BuildConfig.VERSION_NAME);

        } catch (Exception e) {
            String error = Log.getStackTraceString(e);
            log(tag, "getDeviceInfo: " + error);
            sendErrorLog(tag, "getDeviceInfo", error);
        }

        return deviceInfoObj.toString();
    }

    public String getUserDetail() {

        if(checkUserIsLoggedIn()) {

            JSONObject userDetailObj = new JSONObject();
            try {

                userDetailObj.put("userMobileNumber", globalClass.getStringData("mobilenumber"));
                userDetailObj.put("userName", globalClass.getStringData("name"));
                userDetailObj.put("userId", globalClass.getStringData("id"));

            } catch (Exception e) {
                String error = Log.getStackTraceString(e);
                log(tag, "getUserDetail: " + error);
            }

            return userDetailObj.toString();
        }
        else {
            return "";
        }
    }

    public void sendErrorLog(String errorLocation, String errorFunction, String errorDetail) {

        JSONObject logObj = new JSONObject();

        try {
            JSONObject deviceInfoObj = new JSONObject(getDeviceInfo());

            logObj.put("deviceInfo", deviceInfoObj);
            logObj.put("logTime", milliSecondToDateFormat(getMilliSecond(), "dd MMM yyyy hh:mm:ss aa"));
            logObj.put("userLogin",false);

            //todo adding userDetail...
            if(!checknull(getUserDetail()).equalsIgnoreCase("")) {

                JSONObject userDetailObj = new JSONObject(getUserDetail());
                logObj.put("mobileNumber", userDetailObj.get("userMobileNumber"));
                logObj.put("userLogin",true);
                logObj.put("userDetail", userDetailObj);
            }

            logObj.put("errorLocation", errorLocation);
            logObj.put("errorFunction", errorFunction);
            logObj.put("errorDetail", errorDetail);

            logModel model = new logModel();
            model.setLogType(ErrorLog);
            model.setUserLogin(logObj.getString("userLogin"));

            if(model.getUserLogin().equalsIgnoreCase("true")) {
                model.setMobileNumber(logObj.getString("mobileNumber"));
            }
            else {
                model.setMobileNumber("");
            }

            model.setDetail(logObj.toString());
            new Mydatabase(context).addLog(model);

            log(tag,logObj.toString());

        } catch (JSONException e) {

            String error = Log.getStackTraceString(e);
            log(tag, "sendErrorLog: " + error);
            sendErrorLog(tag, "sendErrorLog", error);
        }
    }

    public void sendResponseErrorLog(String errorLocation, String errorFunction, String errorDetail, final String parameter) {

        JSONObject logObj = new JSONObject();

        try {
            //todo adding Device Info...
            JSONObject deviceInfoObj = new JSONObject(getDeviceInfo());
            logObj.put("deviceInfo", deviceInfoObj);
            logObj.put("logTime", milliSecondToDateFormat(getMilliSecond(), "dd MMM yyyy hh:mm:ss aa"));

            //todo adding Parameter...
            if(!checknull(parameter).equalsIgnoreCase("")) {
                JSONObject parameterObj = new JSONObject(parameter);
                logObj.put("parameter", parameterObj);
            }
            else {
                logObj.put("parameter", "");
            }

            logObj.put("userLogin",false);

            //todo adding userDetail...
            if(!checknull(getUserDetail()).equalsIgnoreCase("")) {

                JSONObject userDetailObj = new JSONObject(getUserDetail());
                logObj.put("mobileNumber", userDetailObj.get("userMobileNumber"));
                logObj.put("userLogin",true);
                logObj.put("userDetail", userDetailObj);
            }

            logObj.put("errorLocation", errorLocation);
            logObj.put("errorFunction", errorFunction);
            logObj.put("errorDetail", errorDetail);

            logModel model = new logModel();
            model.setLogType(ResponseErrorLog);
            model.setUserLogin(logObj.getString("userLogin"));

            if(model.getUserLogin().equalsIgnoreCase("true")) {
                model.setMobileNumber(logObj.getString("mobileNumber"));
            }
            else {
                model.setMobileNumber("");
            }

            model.setDetail(logObj.toString());
            new Mydatabase(context).addLog(model);

            log(tag,logObj.toString());

        } catch (JSONException e) {

            String error = Log.getStackTraceString(e);
            log(tag, "sendResponseErrorLog: " + error);
            sendErrorLog(tag, "sendResponseErrorLog", error);
        }
    }

    public void sendNotificationErrorLog(String errorLocation, String errorFunction, String errorDetail, final String parameter) {

        JSONObject logObj = new JSONObject();

        try {
            //todo adding Device Info...
            JSONObject deviceInfoObj = new JSONObject(getDeviceInfo());
            logObj.put("deviceInfo", deviceInfoObj);
            logObj.put("logTime", milliSecondToDateFormat(getMilliSecond(), "dd MMM yyyy hh:mm:ss aa"));

            //todo adding Parameter...
            if(!checknull(parameter).equalsIgnoreCase("")) {
                JSONObject parameterObj = new JSONObject(parameter);
                logObj.put("parameter", parameterObj);
            }
            else {
                logObj.put("parameter", "");
            }

            logObj.put("userLogin",false);

            //todo adding userDetail...
            if(!checknull(getUserDetail()).equalsIgnoreCase("")) {

                JSONObject userDetailObj = new JSONObject(getUserDetail());
                logObj.put("mobileNumber", userDetailObj.get("userMobileNumber"));
                logObj.put("userLogin",true);
                logObj.put("userDetail", userDetailObj);
            }

            logObj.put("errorLocation", errorLocation);
            logObj.put("errorFunction", errorFunction);
            logObj.put("errorDetail", errorDetail);

            logModel model = new logModel();
            model.setLogType(NotificationErrorLog);
            model.setUserLogin(logObj.getString("userLogin"));

            if(model.getUserLogin().equalsIgnoreCase("true")) {
                model.setMobileNumber(logObj.getString("mobileNumber"));
            }
            else {
                model.setMobileNumber("");
            }

            model.setDetail(logObj.toString());
            new Mydatabase(context).addLog(model);

            log(tag,logObj.toString());

        } catch (JSONException e) {

            String error = Log.getStackTraceString(e);
            log(tag, "sendNotificationErrorLog: " + error);
            sendErrorLog(tag, "sendNotificationErrorLog", error);
        }
    }
}
