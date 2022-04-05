package com.hksofttronix.khansama.Admin.AddNewAdmin;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.gson.Gson;
import com.hksofttronix.khansama.Globalclass;
import com.hksofttronix.khansama.Models.navMenuDetail;
import com.hksofttronix.khansama.Models.userDetail;
import com.hksofttronix.khansama.R;
import com.hksofttronix.khansama.Verification;

import java.util.ArrayList;

public class AddNewAdmin extends AppCompatActivity {

    String tag = this.getClass().getSimpleName();
    AppCompatActivity activity = AddNewAdmin.this;

    Globalclass globalclass;

    TextInputLayout adminNametf,adminMobileNumbertf;
    RecyclerView recyclerView;
    EditText adminName,adminMobileNumber;
    MaterialButton addbt;

    int VERIFY_MOBILENUMBER = 111;

    ArrayList<navMenuDetail> arrayList = new ArrayList<>();
    adminRightsAdapter adapter;

    boolean verified = false;
    ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_new_admin);

        setToolbar();
        init();
        binding();
        onClick();
        setAdapter();
    }

    void init() {
        globalclass = Globalclass.getInstance(activity);
    }

    void binding() {
        adminNametf = findViewById(R.id.adminNametf);
        adminMobileNumbertf = findViewById(R.id.adminMobileNumbertf);
        adminName = findViewById(R.id.adminName);
        adminMobileNumber = findViewById(R.id.adminMobileNumber);
        recyclerView = findViewById(R.id.recyclerView);
        addbt = findViewById(R.id.addbt);
    }

    void onClick() {

        adminMobileNumber.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {

                if(actionId == EditorInfo.IME_ACTION_DONE) {

                    globalclass.hideKeyboard(activity);

                    if (!globalclass.isInternetPresent()) {
                        globalclass.toast_long(getResources().getString(R.string.noInternetConnection));
                        return false;
                    }

                    if(validation()) {

                        showConfirmDialogue();
                    }
                }
                return false;
            }
        });

        addbt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                globalclass.hideKeyboard(activity);

                if (!globalclass.isInternetPresent()) {
                    globalclass.toast_long(getResources().getString(R.string.noInternetConnection));
                    return;
                }

                if(validation()) {

                    showConfirmDialogue();
                }
            }
        });
    }

    void setToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_backarrow);
        toolbar.setTitle(getString(R.string.add_new_admin));
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                onBackPressed();
            }
        });
    }

    void setAdapter() {

        navMenuDetail model = new navMenuDetail();
        model.setAdminId(globalclass.getStringData("adminId"));
        model.setNavMenuId("nav_allorders");
        model.setNavMenuName("Manage Order");
        model.setNavMenuDescription("Will able to view all orders & also can change order status");
        model.setAccessStatus(false);
        arrayList.add(model);

        model = new navMenuDetail();
        model.setAdminId(globalclass.getStringData("adminId"));
        model.setNavMenuId("nav_purchase");
        model.setNavMenuName("Manage Purchase");
        model.setNavMenuDescription("Will able to see all purchased inventories & also can add or remove purchase entry");
        model.setAccessStatus(false);
        arrayList.add(model);

        model = new navMenuDetail();
        model.setAdminId(globalclass.getStringData("adminId"));
        model.setNavMenuId("nav_recipe");
        model.setNavMenuName("Manage Recipe");
        model.setNavMenuDescription("Will able to see all recipes, can change racipe status, can add 'New Recipe' & also can make changes in recipe detail");
        model.setAccessStatus(false);
        arrayList.add(model);

        model = new navMenuDetail();
        model.setAdminId(globalclass.getStringData("adminId"));
        model.setNavMenuId("nav_inventory");
        model.setNavMenuName("Manage Inventory");
        model.setNavMenuDescription("Will able to see all inventories and it's available stock, can change inventory detail & also can add new inventory");
        model.setAccessStatus(false);
        arrayList.add(model);

        model = new navMenuDetail();
        model.setAdminId(globalclass.getStringData("adminId"));
        model.setNavMenuId("nav_recipecategory");
        model.setNavMenuName("Manage Recipe category");
        model.setNavMenuDescription("Will able to see all recipe categories, can change recipe categories detail & also can add or remove recipe category");
        model.setAccessStatus(false);
        arrayList.add(model);

        model = new navMenuDetail();
        model.setAdminId(globalclass.getStringData("adminId"));
        model.setNavMenuId("nav_vendor");
        model.setNavMenuName("Manage Vendor");
        model.setNavMenuDescription("Will able to see all vendor, can change vendor details & also can add or remove vendor");
        model.setAccessStatus(false);
        arrayList.add(model);

        model = new navMenuDetail();
        model.setAdminId(globalclass.getStringData("adminId"));
        model.setNavMenuId("nav_adminlist");
        model.setNavMenuName("Manage Admin");
        model.setNavMenuDescription("Will able to see all admin, can change admin details, rights & also can add or remove admin");
        model.setAccessStatus(false);
        arrayList.add(model);

        adapter = new adminRightsAdapter(activity, arrayList, new adminRightsOnClick() {
            @Override
            public void onCheckStatus(int position, navMenuDetail model, adminRightsAdapter.RecyclerViewHolders holder) {
                if(model.getAccessStatus()) {
                    model.setAccessStatus(false);
                }
                else {
                    model.setAccessStatus(true);
                }
                holder.accessStatus.setChecked(model.getAccessStatus());
                adapter.updateData(position,model);
            }
        });

        recyclerView.setLayoutManager(new LinearLayoutManager(activity));
        recyclerView.setAdapter(adapter);
    }

    void showConfirmDialogue() {

        new MaterialAlertDialogBuilder(activity, R.style.RoundShapeTheme)
                .setTitle("Sure")
                .setMessage("Are you sure you want to add new admin ?")
                .setCancelable(false)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();

                        if(!verified) {
                            checkUserExist();
                        }
                        else {
                            createAdminAccount();
                        }
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

    void checkUserExist() {

        try {
            showprogress("Hold on","Please wait...");

            CollectionReference userColl = globalclass.firebaseInstance().collection(Globalclass.userColl);
            Query query = userColl.whereEqualTo("mobilenumber",adminMobileNumber.getText().toString().trim().toLowerCase());
            query.get().addOnCompleteListener(activity, new OnCompleteListener<QuerySnapshot>() {
                @Override
                public void onComplete(@NonNull Task<QuerySnapshot> task) {

                    hideprogress();
                    if(task.isSuccessful()) {
                        if(!task.getResult().isEmpty()) {
                            globalclass.snackit(activity,"Mobile Number already exist!");
                        }
                        else {

                            new MaterialAlertDialogBuilder(activity, R.style.RoundShapeTheme)
                                    .setTitle("Mobile number Verification")
                                    .setMessage("We will send you otp on "
                                            +adminMobileNumber.getText().toString().trim()+" to verify mobile number")
                                    .setCancelable(false)
                                    .setPositiveButton("Send otp", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {

                                            dialog.dismiss();
                                            startActivityForResult(new Intent(activity, Verification.class)
                                                            .putExtra("action",tag)
                                                            .putExtra("mobilenumber",adminMobileNumber.getText().toString().trim()),
                                                    VERIFY_MOBILENUMBER);
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
                    }
                    else {
                        String error = Log.getStackTraceString(task.getException());
                        globalclass.log(tag,"checkAdminExist: "+error);
                        globalclass.toast_long("Unable to add new admin, please try after sometime!");
                        globalclass.sendErrorLog(tag,"checkAdminExist: ",error);
                    }
                }
            });
        }
        catch (Exception e) {

            hideprogress();
            String error = Log.getStackTraceString(e);
            globalclass.log(tag,"checkAdminExist: "+error);
            globalclass.toast_long("Unable to add new admin, please try after sometime!");
            globalclass.sendErrorLog(tag,"checkAdminExist: ",error);
        }
    }

    void createAdminAccount() {

        String parameter = "";

        try {
            showprogress("Creating account","Please wait...");

            CollectionReference userColl = globalclass.firebaseInstance().collection(Globalclass.userColl);
            userDetail model = new userDetail();
            model.setName(adminName.getText().toString().trim().toLowerCase());
            model.setEmailid(getString(R.string.notavailable));
            model.setMobilenumber(adminMobileNumber.getText().toString().trim());
            model.setAdmin(true);
            model.setRightsList(arrayList);

            String id = userColl.document().getId();
            model.setId(id);

            Gson gson = new Gson();
            if(model != null) {
                parameter = gson.toJson(model);
            }
            globalclass.log(tag,"parameter: "+parameter);

            String finalParameter = parameter;

            DocumentReference documentReference = userColl.document(id);
            documentReference.set(model).addOnCompleteListener(activity, new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {

                    hideprogress();
                    if(task.isSuccessful()) {

                        globalclass.toast_long("Admin account created successfully!");
                        onBackPressed();
                    }
                    else {
                        String error = Log.getStackTraceString(task.getException());
                        globalclass.log(tag,"createAdminAccount: "+error);
                        globalclass.toast_long("Unable to create new admin account, please try after sometime!");
                        globalclass.sendResponseErrorLog(tag,"createAdminAccount",error, finalParameter);
                    }
                }
            });
        }
        catch (Exception e) {

            hideprogress();
            String error = Log.getStackTraceString(e);
            globalclass.log(tag,"createAdminAccount: "+error);
            globalclass.toast_long("Unable to create new admin account, please try after sometime!");
            globalclass.sendResponseErrorLog(tag,"createAdminAccount",error,parameter);
        }
    }

    boolean validation()
    {
        String firstchar = "";
        if(adminMobileNumber.getText().length() > 0)
        {
            String getmobileno = adminMobileNumber.getText().toString();
            firstchar=getmobileno.substring(0,1);
        }

        if(adminName.getText().length() < 3) {
            adminNametf.setError("Should contain atleast 3 characters!");
            return false;
        }
        else if(!adminName.getText().toString().trim().matches(globalclass.alphaNumericRegexOne())) {
            adminNametf.setError("Invalid Admin name!");
            return false;
        }
        else if(adminMobileNumber.getText().length()!=10)
        {
            adminMobileNumbertf.setError("Invalid mobile number");
            return false;
        }
        else if(!firstchar.equalsIgnoreCase("6") && !firstchar.equalsIgnoreCase("7") &&
                !firstchar.equalsIgnoreCase("8") && !firstchar.equalsIgnoreCase("9"))
        {
            adminMobileNumbertf.setError("Invalid mobile number");
            return false;
        }

        adminNametf.setErrorEnabled(false);
        adminMobileNumbertf.setErrorEnabled(false);
        return true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == VERIFY_MOBILENUMBER) {
            if (resultCode == Activity.RESULT_OK && data != null) {
                verified = true;
                globalclass.toast_short("Verified successfully!");
                createAdminAccount();
            }
        }
    }

    void showprogress(String title, String message) {
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
}