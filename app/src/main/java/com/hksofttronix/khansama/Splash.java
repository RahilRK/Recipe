package com.hksofttronix.khansama;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;

import com.hksofttronix.khansama.Admin.AddRecipe.AddRecipeService;
import com.hksofttronix.khansama.Admin.AdminHome;
import com.hksofttronix.khansama.Admin.UpdateRecipe.UpdateRecipeService;

public class Splash extends AppCompatActivity {

    String tag = this.getClass().getSimpleName();
    Globalclass globalclass;

    AppCompatActivity activity = Splash.this;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        init();
        globalclass.createNotificationChannel();

        checkAppOpenFirstTime();
    }

    void init() {
        globalclass = Globalclass.getInstance(activity);
    }

    void checkAppOpenFirstTime() {
        if(globalclass.checknull(String.valueOf(globalclass.getBooleanData("firstTime")))
                .equalsIgnoreCase("")
            || !globalclass.getBooleanData("firstTime")) {
            if (!globalclass.isInternetPresent()) {
                globalclass.toast_long(getResources().getString(R.string.noInternetConnection));
            }
            else {
//                globalclass.startService(activity,SyncData.class);
                globalclass.setBooleanData("firstTime",true);
                checkLoginType(1500);
            }
        }
        else {
            checkLoginType(1500);
        }
    }

    void checkLoginType(long delay) {

        new Handler(Looper.myLooper()).postDelayed(new Runnable() {
            @Override
            public void run() {
                if(globalclass.getStringData("loginType") == null ||
                        globalclass.getStringData("loginType").equalsIgnoreCase("")) {
                    startActivity(new Intent(activity, MainActivity.class));
                    finish();
                    return;
                }

                if(globalclass.getStringData("loginType").equalsIgnoreCase("Admin")) {

                    globalclass.startAllAdminServices(activity);
                    startActivity(new Intent(activity, AdminHome.class));
                    finish();
                }
                else if(globalclass.getStringData("loginType").equalsIgnoreCase("User")){
                    startActivity(new Intent(activity, MainActivity.class));
                    finish();
                }
            }
        },delay);
    }
}