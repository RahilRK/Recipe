package com.hksofttronix.khansama.Admin;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.hksofttronix.khansama.Admin.AdminListFrag.AdminListFrag;
import com.hksofttronix.khansama.Admin.AllOrders.AllOrdersFrag;
import com.hksofttronix.khansama.Admin.RecipeCategoryFrag.RecipeCategoryFrag;
import com.hksofttronix.khansama.Admin.AdminHomeFrag.AdminHomeFrag;
import com.hksofttronix.khansama.Admin.InventoryFrag.InventoryFrag;
import com.hksofttronix.khansama.Admin.PurchaseFrag.PurchaseFrag;
import com.hksofttronix.khansama.Admin.RecipesFrag.RecipesFrag;
import com.hksofttronix.khansama.Admin.VendorFrag.VendorFrag;
import com.hksofttronix.khansama.Login;
import com.hksofttronix.khansama.MainActivity;
import com.hksofttronix.khansama.Models.inventoryDetail;
import com.hksofttronix.khansama.Models.navMenuDetail;
import com.hksofttronix.khansama.Models.userDetail;
import com.hksofttronix.khansama.R;
import com.hksofttronix.khansama.Globalclass;
import com.hksofttronix.khansama.Mydatabase;
import com.hksofttronix.khansama.SyncData;

import java.util.ArrayList;

public class AdminHome extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener{

    String tag = this.getClass().getSimpleName();
    AppCompatActivity activity = AdminHome.this;

    Globalclass globalclass;
    Mydatabase mydatabase;

    DrawerLayout drawerLayout;
    ActionBarDrawerToggle toggle;
    NavigationView navigation_view;
    TextView navHeaderName;

    boolean doubleBackToExitPressedOnce = false;
    boolean shouldExecuteOnResume;

    ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_home);

        shouldExecuteOnResume = false;
        init();
        binding();
        onClick();
        setNavigation_view();
        checkDataNeedToSync();
        getLiveData();
        showHideNavigationMenu();
        handleIntent();
        setText();
    }

    void handleIntent() {
        Intent intent = getIntent();
        Bundle extras = intent.getExtras();

        if(extras != null) {

            globalclass.snackit(activity,"Welcome "+globalclass.getStringData("adminName").toUpperCase());
        }
    }

    void init() {
        globalclass = Globalclass.getInstance(activity);
        mydatabase = Mydatabase.getInstance(activity);
    }

    void binding() {
        drawerLayout = findViewById(R.id.drawerLayout);
        navigation_view = findViewById(R.id.navigation_view);
    }

    void onClick() {
    }

    void setNavigation_view() {
        navigation_view.setNavigationItemSelectedListener(this);
        displaySelectedScreen(R.id.nav_adminhome);
    }

    public Toolbar setToolbar(Toolbar toolbar) {
        if(toolbar != null) {
            setSupportActionBar(toolbar);
            toggle = new ActionBarDrawerToggle(
                    this, drawerLayout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
            drawerLayout.setDrawerListener(toggle);
            toggle.syncState();
        }

        return toolbar;
    }

    void showHideNavigationMenu() {

        try {
            Menu nav_Menu = navigation_view.getMenu();
            ArrayList<navMenuDetail> navMenuList = mydatabase.getNavMenu();

            if(navMenuList != null && !navMenuList.isEmpty()) {
                for(int i=0;i<navMenuList.size();i++) {
                    navMenuDetail navMenuModel = navMenuList.get(i);
                    if(navMenuModel.getNavMenuId().equalsIgnoreCase("nav_allorders")) {
                        nav_Menu.findItem(R.id.nav_allorders).setVisible(navMenuModel.getAccessStatus());
                    }
                    else if(navMenuModel.getNavMenuId().equalsIgnoreCase("nav_purchase")) {
                        nav_Menu.findItem(R.id.nav_purchase).setVisible(navMenuModel.getAccessStatus());
                    }
                    else if(navMenuModel.getNavMenuId().equalsIgnoreCase("nav_recipe")) {
                        nav_Menu.findItem(R.id.nav_recipe).setVisible(navMenuModel.getAccessStatus());
                    }
                    else if(navMenuModel.getNavMenuId().equalsIgnoreCase("nav_inventory")) {
                        nav_Menu.findItem(R.id.nav_inventory).setVisible(navMenuModel.getAccessStatus());
                    }
                    else if(navMenuModel.getNavMenuId().equalsIgnoreCase("nav_recipecategory")) {
                        nav_Menu.findItem(R.id.nav_recipecategory).setVisible(navMenuModel.getAccessStatus());
                    }
                    else if(navMenuModel.getNavMenuId().equalsIgnoreCase("nav_vendor")) {
                        nav_Menu.findItem(R.id.nav_vendor).setVisible(navMenuModel.getAccessStatus());
                    }
                    else if(navMenuModel.getNavMenuId().equalsIgnoreCase("nav_adminlist")) {
                        nav_Menu.findItem(R.id.nav_adminlist).setVisible(navMenuModel.getAccessStatus());
                    }
                }
            }
        }
        catch (Exception e) {

            String error = Log.getStackTraceString(e);
            globalclass.log(tag,"showHideNavigationMenu: "+error);
            globalclass.sendErrorLog(tag,"showHideNavigationMenu: ",error);
        }
    }

    void setText() {
        navHeaderName = navigation_view.getHeaderView(0).findViewById(R.id.navHeaderName);
        navHeaderName.setText(globalclass.firstLetterCapital(globalclass.getStringData("adminName")));
    }

    void getLiveData() {
        getAdminDetail();
    }

    void getAdminDetail() {
        try {
            CollectionReference userColl = globalclass.firebaseInstance().collection(Globalclass.userColl);
            Query query = userColl.whereEqualTo("id",globalclass.getStringData("adminId"));
            query.addSnapshotListener(activity, new EventListener<QuerySnapshot>() {
                        @Override
                        public void onEvent(@Nullable QuerySnapshot querySnapshot, @Nullable FirebaseFirestoreException firebaseFirestoreException) {

                            if (firebaseFirestoreException != null) {
                                String error = Log.getStackTraceString(firebaseFirestoreException);
                                globalclass.log(tag, "getAdminDetail: " + error);
                                globalclass.sendErrorLog(tag,"getAdminDetail: ",error);
                                globalclass.toast_long("Unable to get admin data!");
                                return;
                            }

                            String source = querySnapshot.getMetadata().isFromCache() ?
                                    "local cache" : "server";
                            globalclass.log(tag,"getAdminDetail - Data fetched from "+source);
                            if(source.equalsIgnoreCase("server")) {
                                showHideNavigationMenu();
                            }

                            mydatabase.truncateTable(mydatabase.navMenu);
                            for (DocumentChange documentChange : querySnapshot.getDocumentChanges()) {
                                DocumentSnapshot documentSnapshot = documentChange.getDocument();

                                int oldIndex = documentChange.getOldIndex();
                                int newIndex = documentChange.getNewIndex();
//                                globalclass.log(tag,"oldIndex: "+oldIndex);
//                                globalclass.log(tag,"newIndex: "+newIndex);

                                if (documentChange.getType() == DocumentChange.Type.ADDED) {

                                    userDetail model = documentSnapshot.toObject(userDetail.class);
                                    if(!source.equalsIgnoreCase("local cache")) {
                                        globalclass.log(tag,"User Detail ADDED from server: "+model.getName());
                                    }

                                    if(model.getRightsList() !=null && !model.getRightsList().isEmpty()) {
                                        if(model.getId().equalsIgnoreCase(globalclass.checknull(globalclass.getStringData("adminId")))) {
                                            for(int i=0;i<model.getRightsList().size();i++) {
                                                navMenuDetail navMenuModel = model.getRightsList().get(i);
                                                mydatabase.addNavMenu(navMenuModel);
                                            }
                                        }
                                    }

                                } else if (documentChange.getType() == DocumentChange.Type.MODIFIED) {

                                    userDetail model = documentSnapshot.toObject(userDetail.class);
                                    if(!source.equalsIgnoreCase("local cache")) {
                                        globalclass.log(tag,"User Detail MODIFIED from server: "+model.getName());
                                    }

                                    if(model.getRightsList() !=null && !model.getRightsList().isEmpty()) {
                                        if(model.getId().equalsIgnoreCase(globalclass.checknull(globalclass.getStringData("adminId")))) {
                                            for(int i=0;i<model.getRightsList().size();i++) {
                                                navMenuDetail navMenuModel = model.getRightsList().get(i);
                                                mydatabase.addNavMenu(navMenuModel);
                                            }
                                        }
                                    }

                                } else if (documentChange.getType() == DocumentChange.Type.REMOVED) {

//                                    adapter.deleteData(oldIndex);
                                }
                            }
                        }
                    });

        } catch (Exception e) {

            String error = Log.getStackTraceString(e);
            globalclass.log(tag, "getAdminDetail: " + error);
            globalclass.sendErrorLog(tag,"getAdminDetail: ",error);
            globalclass.toast_long("Unable to get admin data!");
        }
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        displaySelectedScreen(item.getItemId());
        return true;
    }

    public void displaySelectedScreen(int itemId) {

        //creating fragment object
        Fragment fragment = null;

        //initializing the fragment object which is selected
        switch (itemId) {
            case R.id.nav_adminhome:
                fragment = new AdminHomeFrag();
                break;
            case R.id.nav_allorders:
                fragment = new AllOrdersFrag();
                break;
            case R.id.nav_purchase:
                fragment = new PurchaseFrag();
                break;
            case R.id.nav_recipe:
                fragment = new RecipesFrag();
                break;
            case R.id.nav_inventory:
                fragment = new InventoryFrag();
                break;
            case R.id.nav_recipecategory:
                fragment = new RecipeCategoryFrag();
                break;
            case R.id.nav_vendor:
                fragment = new VendorFrag();
                break;
            case R.id.nav_adminlist:
                fragment = new AdminListFrag();
                break;
            case R.id.nav_clienthome:

                startActivity(new Intent(activity, MainActivity.class));

                break;
            case R.id.nav_adminlogout:

                new MaterialAlertDialogBuilder(activity, R.style.RoundShapeTheme)
                        .setTitle("Sure")
                        .setMessage("Are you sure you want to logout?")
                        .setCancelable(false)
                        .setPositiveButton("Logout", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                                dialog.dismiss();

                                globalclass.unsubscribeToTopic("allUser");
                                globalclass.unsubscribeToTopic(globalclass.getStringData("mobilenumber"));
                                globalclass.unsubscribeToTopic("allAdmin");

                                globalclass.clearPref();
                                mydatabase.clearDatabase();

                                startActivity(new Intent(activity, Login.class).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK));                            }
                        })
                        .setNeutralButton("No", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                                dialog.dismiss();
                            }
                        })
                        .show();
                break;
        }

        //replacing the fragment
        if (fragment != null) {
            FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
            ft.replace(R.id.content_frame, fragment);
            ft.commit();
        }

        drawerLayout.closeDrawer(GravityCompat.START);
        navigation_view.setCheckedItem(itemId);
    }

    void showprogress(String title,String message) {
        progressDialog = new ProgressDialog(activity);
        progressDialog.setCancelable(false);
        progressDialog.setTitle(title);
        progressDialog.setMessage(message);
        progressDialog.show();
    }

    void hideprogress() {
        if (progressDialog != null && progressDialog.isShowing()) {
            progressDialog.dismiss();
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
            globalclass.startService(activity, SyncData.class);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (shouldExecuteOnResume) {
            // Your onResume Code Here
//            displaySelectedScreen(R.id.nav_adminhome);
        } else {
            shouldExecuteOnResume = true;
        }
    }

    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            if(navigation_view.getCheckedItem().toString().equalsIgnoreCase("Home")) {
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
            }
            else {
                displaySelectedScreen(R.id.nav_adminhome);
            }
        }
    }
}