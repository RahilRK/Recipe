package com.hksofttronix.khansama.Admin.UpdateAdmin;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Transaction;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.hksofttronix.khansama.Admin.AddNewAdmin.adminRightsAdapter;
import com.hksofttronix.khansama.Admin.AddNewAdmin.adminRightsOnClick;
import com.hksofttronix.khansama.Globalclass;
import com.hksofttronix.khansama.Models.navMenuDetail;
import com.hksofttronix.khansama.Models.userDetail;
import com.hksofttronix.khansama.R;

import java.util.ArrayList;
import java.util.Map;

public class UpdateAdmin extends AppCompatActivity {

    String tag = this.getClass().getSimpleName();
    AppCompatActivity activity = UpdateAdmin.this;

    Globalclass globalclass;

    TextView adminMobileNumber;
    TextInputLayout adminNametf;
    LinearLayout rightsLayout;
    RecyclerView recyclerView;
    EditText adminName;
    MaterialButton updateAdminDetailbt;

    ArrayList<navMenuDetail> arrayList;
    ArrayList<navMenuDetail> oldArrayList;
    adminRightsAdapter adapter;

    userDetail model;

    ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update_admin);

        setToolbar();
        init();
        binding();
        offlineList();
        setText();
        onClick();
    }

    void init() {
        globalclass = Globalclass.getInstance(activity);
    }

    void binding() {
        adminNametf = findViewById(R.id.adminNametf);
        rightsLayout = findViewById(R.id.rightsLayout);
        adminName = findViewById(R.id.adminName);
        adminMobileNumber = findViewById(R.id.adminMobileNumber);
        recyclerView = findViewById(R.id.recyclerView);
        updateAdminDetailbt = findViewById(R.id.updateAdminDetailbt);
    }

    void setText() {

        model = getIntent().getParcelableExtra("userDetail");
        if(model != null) {

            if(model.getId().equalsIgnoreCase(globalclass.getStringData("adminId"))) {
                adminName.setEnabled(true);
            }

            adminName.setText(model.getName());
            adminMobileNumber.setText(model.getMobilenumber());

            arrayList = getIntent().getParcelableArrayListExtra("rightsList");
            oldArrayList = getIntent().getParcelableArrayListExtra("oldRightsList");

            if(arrayList == null || arrayList.isEmpty()) {
                arrayList = offlineList();
            }
            arrayList = arraylist();

            if(arrayList != null && !arrayList.isEmpty()) {
                setAdapter();
            }
            else {
                String error = "admin rights list is null!";
                globalclass.log(tag,error);
                globalclass.sendErrorLog(tag,"setText: ",error);
                globalclass.toast_long("Unable to show admin details, please try after sometime!");
            }
        }
        else {
            String error = "userDetail model is null!";
            globalclass.log(tag,error);
            globalclass.sendErrorLog(tag,"setText: ",error);
            globalclass.toast_long("Unable to show admin details, please try after sometime!");
        }
    }

    void onClick() {

        adminName.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {

                if(editable.length() > 0) {
                    enableUpdateButton();
                }
            }
        });

        adminName.setOnEditorActionListener(new TextView.OnEditorActionListener() {
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

        updateAdminDetailbt.setOnClickListener(new View.OnClickListener() {
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
        toolbar.setTitle(getString(R.string.change_detail));
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                onBackPressed();
            }
        });
    }

    ArrayList<navMenuDetail> offlineList() {

        ArrayList<navMenuDetail> offlineList = new ArrayList<>();
        navMenuDetail model = new navMenuDetail();
        model.setAdminId(globalclass.getStringData("adminId"));
        model.setNavMenuId("nav_allorders");
        model.setNavMenuName("Manage Order");
        model.setNavMenuDescription("Will able to view all orders & also can change order status");
        model.setAccessStatus(false);
        offlineList.add(model);

        model = new navMenuDetail();
        model.setAdminId(globalclass.getStringData("adminId"));
        model.setNavMenuId("nav_purchase");
        model.setNavMenuName("Manage Purchase");
        model.setNavMenuDescription("Will able to see all purchased inventories & also can add or remove purchase entry");
        model.setAccessStatus(false);
        offlineList.add(model);

        model = new navMenuDetail();
        model.setAdminId(globalclass.getStringData("adminId"));
        model.setNavMenuId("nav_recipe");
        model.setNavMenuName("Manage Recipe");
        model.setNavMenuDescription("Will able to see all recipes, can change racipe status, can add 'New Recipe' & also can make changes in recipe detail");
        model.setAccessStatus(false);
        offlineList.add(model);

        model = new navMenuDetail();
        model.setAdminId(globalclass.getStringData("adminId"));
        model.setNavMenuId("nav_inventory");
        model.setNavMenuName("Manage Inventory");
        model.setNavMenuDescription("Will able to see all inventories and it's available stock, can change inventory detail & also can add new inventory");
        model.setAccessStatus(false);
        offlineList.add(model);

        model = new navMenuDetail();
        model.setAdminId(globalclass.getStringData("adminId"));
        model.setNavMenuId("nav_recipecategory");
        model.setNavMenuName("Manage Recipe category");
        model.setNavMenuDescription("Will able to see all recipe categories, can change recipe categories detail & also can add or remove recipe category");
        model.setAccessStatus(false);
        offlineList.add(model);

        model = new navMenuDetail();
        model.setAdminId(globalclass.getStringData("adminId"));
        model.setNavMenuId("nav_vendor");
        model.setNavMenuName("Manage Vendor");
        model.setNavMenuDescription("Will able to see all vendor, can change vendor details & also can add or remove vendor");
        model.setAccessStatus(false);
        offlineList.add(model);

        model = new navMenuDetail();
        model.setAdminId(globalclass.getStringData("adminId"));
        model.setNavMenuId("nav_adminlist");
        model.setNavMenuName("Manage Admin");
        model.setNavMenuDescription("Will able to see all admin, can change admin details, rights & also can add or remove admin");
        model.setAccessStatus(false);
        offlineList.add(model);

        return offlineList;
    }

    ArrayList<navMenuDetail> arraylist() {
        ArrayList<navMenuDetail> list = new ArrayList<>();
        for(int i=0;i<offlineList().size();i++) {
            navMenuDetail navMenuModelOffline = offlineList().get(i);
            list.add(getAccessStatusFromArrayList(navMenuModelOffline));
        }

        return list;
    }

    navMenuDetail getAccessStatusFromArrayList(navMenuDetail navMenuModelOffline) {
        if(arrayList != null && !arrayList.isEmpty()) {
            for(int j=0;j<arrayList.size();j++) {
                navMenuDetail navMenuModelLive = arrayList.get(j);
                if(navMenuModelOffline.getNavMenuId().equalsIgnoreCase(navMenuModelLive.getNavMenuId())) {
                    navMenuDetail navMenuModel = navMenuModelOffline;
                    navMenuModel.setAccessStatus(navMenuModelLive.getAccessStatus());
                    return navMenuModel;
                }
            }
        }

        return null;
    }

    void setAdapter() {

        if(model.getId().equalsIgnoreCase(globalclass.getStringData("adminId"))) {

            rightsLayout.setVisibility(View.GONE);
            return;
        }

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
                enableUpdateButton();
            }
        });

        recyclerView.setLayoutManager(new LinearLayoutManager(activity));
        recyclerView.setAdapter(adapter);
    }

    void showConfirmDialogue() {

        new MaterialAlertDialogBuilder(activity, R.style.RoundShapeTheme)
                .setTitle("Sure")
                .setMessage("Are you sure you want to change admin detail ?")
                .setCancelable(false)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        dialog.dismiss();
                        updateAdminDetails();
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

    void updateAdminDetails() {

        String parameter = "";

        try {
            showprogress("Changing details","Please wait...");

            final DocumentReference userDocRef = globalclass.firebaseInstance()
                    .collection(Globalclass.userColl)
                    .document(model.getId());

            Gson gson = new Gson();
            if(model != null) {
                parameter = gson.toJson(model);
            }
            globalclass.log(tag,"parameter: "+parameter);

            String finalParameter = parameter;

            globalclass.firebaseInstance().runTransaction(new Transaction.Function<Integer>() {

                @Nullable
                @Override
                public Integer apply(@NonNull Transaction transaction) throws FirebaseFirestoreException {

                    DocumentSnapshot userDoc = transaction.get(userDocRef);
                    if(userDoc.exists()) {

                        userDetail userDetailModel = userDoc.toObject(userDetail.class);
                        Gson gson = new GsonBuilder().create();
                        String json = gson.toJson(userDetailModel);
                        Map<String,Object> map = new Gson().fromJson(json, Map.class);
                        map.put("name", adminName.getText().toString().trim().toLowerCase());
                        map.put("mobilenumber", adminMobileNumber.getText().toString().trim().toLowerCase());
                        map.put("rightsList", arrayList);

                        transaction.update(userDocRef,map);
                        return 0;
                    }
                    else {

                        return -1;
                    }
                }
            }).addOnSuccessListener(activity, new OnSuccessListener<Integer>() {
                @Override
                public void onSuccess(Integer integer) {

                    hideprogress();
                    if(integer == 0) {

                        changeDataLocally(); //todo if personal detail has been changed!
                        globalclass.toast_long("Details changed successfully!");
                        onBackPressed();
                    }
                    else if(integer == -1) {

                        String error = "return -1";
                        globalclass.log(tag,"updateAdminDetails onSuccess: "+error);
                        globalclass.sendResponseErrorLog(tag,"updateAdminDetails onSuccess: ",error, finalParameter);
                        globalclass.toast_long("Unable to change admin detail, please try after sometime!");
                    }
                }
            }).addOnFailureListener(activity, new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {

                    hideprogress();
                    String error = Log.getStackTraceString(e);
                    globalclass.log(tag,"updateAdminDetails onFailure: "+error);
                    globalclass.sendResponseErrorLog(tag,"updateAdminDetails onFailure: ",error, finalParameter);
                    globalclass.toast_long("Unable to change admin detail, please try after sometime!");
                }
            });

        }
        catch (Exception e) {

            hideprogress();
            String error = Log.getStackTraceString(e);
            globalclass.log(tag,"updateAdminDetails: "+error);
            globalclass.sendResponseErrorLog(tag,"updateAdminDetails: ",error, parameter);
            globalclass.toast_long("Unable to change admin detail, please try after sometime!");
        }
    }

    void changeDataLocally() {
        if(model.getId().equalsIgnoreCase(globalclass.getStringData("adminId"))) {
            globalclass.setStringData("name",adminName.getText().toString().toLowerCase());
            globalclass.setStringData("adminName",adminName.getText().toString().toLowerCase());
        }
    }

    void enableUpdateButton() {

        userDetail userDetailModel = new userDetail();
        userDetailModel.setName(adminName.getText().toString().trim().toLowerCase());
        if(globalclass.checknull(model.getEmailid()).equalsIgnoreCase("")) {
            userDetailModel.setEmailid(getString(R.string.notavailable));
        }
        else {
            userDetailModel.setEmailid(model.getEmailid());
        }
        userDetailModel.setMobilenumber(adminMobileNumber.getText().toString().trim().toLowerCase());

        if(userDetailequals2(userDetailModel) || !checkRightequal2()) {
            updateAdminDetailbt.setEnabled(true);
        }
        else {
            updateAdminDetailbt.setEnabled(false);
        }

        globalclass.log(tag,"userDetailequals2: "+userDetailequals2(userDetailModel));
        globalclass.log(tag,"checkRightequal2: "+checkRightequal2());
    }

    boolean userDetailequals2(Object object2) {  // equals2 method
        if(model.equals(object2)) { // if equals() method returns true
            return false; // return true
        }
        else return true; // if equals() method returns false, also return false
    }

    boolean checkRightequal2() {

        return arrayList.equals(oldArrayList);
    }

    boolean validation()
    {
        if(adminName.getText().length() < 3) {
            adminNametf.setError("Should contain atleast 3 characters!");
            return false;
        }
        else if(!adminName.getText().toString().trim().matches(globalclass.alphaNumericRegexOne())) {
            adminNametf.setError("Invalid Admin name!");
            return false;
        }

        adminNametf.setErrorEnabled(false);
        return true;
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