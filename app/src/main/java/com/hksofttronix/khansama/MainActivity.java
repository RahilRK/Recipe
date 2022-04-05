package com.hksofttronix.khansama;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager.widget.ViewPager;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.google.android.material.badge.BadgeDrawable;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;
import com.hksofttronix.khansama.AccountFrag.AccountFrag;
import com.hksofttronix.khansama.CartFrag.CartFrag;
import com.hksofttronix.khansama.HomeFrag.HomeFrag;

import java.util.Map;

public class MainActivity extends AppCompatActivity {

    String tag = this.getClass().getSimpleName();
    AppCompatActivity activity = MainActivity.this;

    Globalclass globalclass;
    Mydatabase mydatabase;

    private TabLayout tabLayout;
    private ViewPager viewPager;
    private int[] tabIcons = {
            R.drawable.tab_home_selector,
            R.drawable.tab_cart_selector,
            R.drawable.tab_account_selector
    };

    BadgeDrawable badgecount;

    boolean doubleBackToExitPressedOnce = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        init();
        handleIntent();
        binding();
//        getAppInfo();
        checkDataNeedToSync();
        setTAB();
    }

    void handleIntent() {
        Intent intent = getIntent();
        Bundle extras = intent.getExtras();

        if (extras != null) {

            globalclass.snackit(activity, "Welcome " + globalclass.getStringData("name").toUpperCase());
        }
    }

    void init() {
        globalclass = Globalclass.getInstance(activity);
        mydatabase = Mydatabase.getInstance(activity);
    }

    void binding() {
        viewPager = findViewById(R.id.viewpager);
        tabLayout = findViewById(R.id.tabs);
    }

    void setupTabIcons() {
        tabLayout.getTabAt(0).setIcon(tabIcons[0]);
        tabLayout.getTabAt(1).setIcon(tabIcons[1]);
        tabLayout.getTabAt(2).setIcon(tabIcons[2]);
    }

    void setupViewPager(ViewPager viewPager) {
        TabAdp adapter = new TabAdp(getSupportFragmentManager());
        adapter.addFrag(new HomeFrag(), "Home");
        adapter.addFrag(new CartFrag(), "Cart");
        adapter.addFrag(new AccountFrag(), "Account");
        viewPager.setAdapter(adapter);
    }

    void setTAB() {
        setupViewPager(viewPager);
        tabLayout.setupWithViewPager(viewPager);
        setupTabIcons();
    }

    public void setBudgetCount() {
        badgecount = tabLayout.getTabAt(1).getOrCreateBadge();
        int unreadcount = mydatabase.getCartItemCount();
        if (unreadcount > 0) {
            badgecount.setVisible(true);
            badgecount.setNumber(unreadcount);
        } else if (unreadcount == 0) {
            badgecount.setVisible(false);
        }
    }

    public void selectTab(int position) {
        viewPager.setCurrentItem(position);
    }

    void getAppInfo() {

        try {
            CollectionReference appInfoColl = globalclass.firebaseInstance().collection(Globalclass.appInfoColl);
            appInfoColl.addSnapshotListener(activity, new EventListener<QuerySnapshot>() {
                @Override
                public void onEvent(@Nullable QuerySnapshot querySnapshot, @Nullable FirebaseFirestoreException firebaseFirestoreException) {

                    if (firebaseFirestoreException != null) {
                        String error = Log.getStackTraceString(firebaseFirestoreException);
                        globalclass.log(tag, "getAppInfo: " + error.toString());
                        globalclass.sendErrorLog(tag, "getAppInfo", error.toString());
                    }

                    if(querySnapshot == null || querySnapshot.isEmpty()) {

                        globalclass.log(tag, "No AppInfo data found!");
                        return;
                    }

                    String source = querySnapshot.getMetadata().isFromCache() ?
                            "local cache" : "server";
                    globalclass.log(tag,"getAppInfo - Data fetched from "+source);

                    for (DocumentChange documentChange : querySnapshot.getDocumentChanges()) {
                        DocumentSnapshot documentSnapshot = documentChange.getDocument();

                        if (documentChange.getType() == DocumentChange.Type.ADDED) {

                            if(!source.equalsIgnoreCase("local cache")) {
                                globalclass.log(tag,"AppInfo data ADDED from server: ");
                            }

                            Map<String, Object> map = documentSnapshot.getData();
                            for (Map.Entry<String, Object> entry : map.entrySet()) {
//                                globalclass.log(tag,"Key : " + entry.getKey() + ", Value : " + entry.getValue());
                                globalclass.setStringData(entry.getKey(),entry.getValue().toString());
                            }

                        } else if (documentChange.getType() == DocumentChange.Type.MODIFIED) {

                            if(!source.equalsIgnoreCase("local cache")) {
                                globalclass.log(tag,"AppInfo data MODIFIED from server: ");
                            }

                            Map<String, Object> map = documentSnapshot.getData();
                            for (Map.Entry<String, Object> entry : map.entrySet()) {
                                globalclass.log(tag,"Key : " + entry.getKey() + ", Value : " + entry.getValue());
                                globalclass.setStringData(entry.getKey(),entry.getValue().toString());
                            }

                        } else if (documentChange.getType() == DocumentChange.Type.REMOVED) {

                            if(!source.equalsIgnoreCase("local cache")) {
                                globalclass.log(tag,"AppInfo data REMOVED from server: ");
                            }
                        }
                    }
                }
            });
        }
        catch (Exception e) {
            String error = Log.getStackTraceString(e);
            globalclass.log(tag,"getAppInfo: "+error);
            globalclass.sendErrorLog(tag, "getAppInfo", error);
        }
    }

    void checkDataNeedToSync() {

        if(!globalclass.isInternetPresent()) {
            return;
        }

        int lastSyncDataDays = Integer.parseInt(globalclass.getTimeDifference(globalclass.getLongData("lastSyncDataMilliSecond"),
                globalclass.getMilliSecond()).get(0));
        globalclass.log(tag,"lastSyncDataDays: "+lastSyncDataDays);
        if(lastSyncDataDays >= globalclass.syncDataDays()) {
            globalclass.startService(activity,SyncData.class);
        }
    }

    @Override
    public void onBackPressed() {

        if (tabLayout.getSelectedTabPosition() == 0) {
            if(globalclass.checknull(globalclass.getStringData("loginType"))
                    .equalsIgnoreCase("Admin")) {
                super.onBackPressed();
                return;
            }

            if (doubleBackToExitPressedOnce) {
                super.onBackPressed();
                return;
            }

            this.doubleBackToExitPressedOnce = true;
            globalclass.toast_short("Press back again to exit!");

            new Handler(Looper.myLooper()).postDelayed(new Runnable() {

                @Override
                public void run() {
                    doubleBackToExitPressedOnce = false;
                }
            }, 2000);
        } else {
            selectTab(0);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        setBudgetCount();
    }
}