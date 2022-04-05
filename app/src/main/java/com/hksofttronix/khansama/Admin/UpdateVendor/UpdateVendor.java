package com.hksofttronix.khansama.Admin.UpdateVendor;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.hksofttronix.khansama.Models.inventoryDetail;
import com.hksofttronix.khansama.Models.vendorDetail;
import com.hksofttronix.khansama.R;
import com.hksofttronix.khansama.Globalclass;
import com.hksofttronix.khansama.Mydatabase;

import java.util.Map;

public class UpdateVendor extends AppCompatActivity {

    String tag = this.getClass().getSimpleName();
    AppCompatActivity activity = UpdateVendor.this;

    Globalclass globalclass;
    Mydatabase mydatabase;

    SwipeRefreshLayout swipeRefresh;
    TextInputLayout vendorNametf,vendorMobileNumbertf,vendorShopAddresstf;
    EditText vendorName,vendorMobileNumber,vendorShopAddress;
    MaterialButton updatebt;

    ProgressDialog progressDialog;

    vendorDetail model;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update_vendor);

        setToolbar();
        init();
        binding();
        setText();
        onClick();
    }

    void init() {
        globalclass = Globalclass.getInstance(activity);
        mydatabase = Mydatabase.getInstance(activity);
    }

    void binding() {
        swipeRefresh = findViewById(R.id.swipeRefresh);
        vendorNametf = findViewById(R.id.vendorNametf);
        vendorMobileNumbertf = findViewById(R.id.vendorMobileNumbertf);
        vendorShopAddresstf = findViewById(R.id.vendorShopAddresstf);
        vendorName = findViewById(R.id.vendorName);
        vendorMobileNumber = findViewById(R.id.vendorMobileNumber);
        vendorShopAddress = findViewById(R.id.vendorShopAddress);
        updatebt = findViewById(R.id.updatebt);
    }

    void setText() {
        model = getIntent().getParcelableExtra("vendorDetail");

        vendorName.setText(model.getVendorName());
        vendorMobileNumber.setText(model.getVendorMobileNumber());
        vendorShopAddress.setText(globalclass.checknull(model.getVendorShopAddress()));
    }

    void onClick() {

        swipeRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {

                if (!globalclass.isInternetPresent()) {
                    swipeRefresh.setRefreshing(false);
                    globalclass.toast_long(getResources().getString(R.string.noInternetConnection));
                    return;
                }

                getVendorDetail();
            }
        });

        vendorName.addTextChangedListener(new TextWatcher() {
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

        vendorName.addTextChangedListener(new TextWatcher() {
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

        vendorShopAddress.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {

                enableUpdateButton();
            }
        });

        updatebt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (!globalclass.isInternetPresent()) {
                    globalclass.toast_long(getResources().getString(R.string.noInternetConnection));
                    return;
                }

                if(validation()) {
                    showConfirmDialogue();
                }
            }
        });

        vendorMobileNumber.addTextChangedListener(new TextWatcher() {
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
    }

    void setToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_backarrow);
        toolbar.setTitle(getString(R.string.change_vendor_detail));
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                onBackPressed();
            }
        });
    }

    void getVendorDetail() {

        String parameter = "";

        try {
            swipeRefresh.setRefreshing(true);

            CollectionReference vendorColl = globalclass.firebaseInstance().collection(Globalclass.vendorColl);
            Query checkVendorExist = vendorColl.whereEqualTo("vendorId", model.getVendorId());

            Gson gson = new Gson();
            if(model != null) {
                parameter = gson.toJson(model);
            }
            globalclass.log(tag,"parameter: "+parameter);

            String finalParameter = parameter;

            checkVendorExist.get().addOnCompleteListener(activity, new OnCompleteListener<QuerySnapshot>() {
                @Override
                public void onComplete(@NonNull Task<QuerySnapshot> task) {

                    swipeRefresh.setRefreshing(false);
                    globalclass.snackit(activity,"Vendor detail refresh successfully!");

                    if(task.isSuccessful()) {
                        if(!task.getResult().isEmpty()) {
                            for(DocumentSnapshot documents : task.getResult()) {
                                model = documents.toObject(vendorDetail.class);
                                globalclass.log(tag,"Vendor name: "+ model.getVendorName());
                            }

                            setText();
                        }
                        else {
                            mydatabase.deleteData(mydatabase.vendor,"vendorId",model.getVendorId());
                            globalclass.snackit(activity,"Vendor not exist!");
                        }
                    }
                    else {
                        String error = Log.getStackTraceString(task.getException());
                        globalclass.log(tag,"getVendorDetail: "+error);
                        globalclass.sendResponseErrorLog(tag,"getVendorDetail: ",error, finalParameter);
                        globalclass.toast_long("Unable to get vendor detail, please try after sometime!");
                    }
                }
            });
        }
        catch (Exception e) {

            swipeRefresh.setRefreshing(false);
            String error = Log.getStackTraceString(e);
            globalclass.log(tag,"getVendorDetail: "+error);
            globalclass.sendResponseErrorLog(tag,"getVendorDetail: ",error, parameter);
            globalclass.toast_long("Unable to get vendor detail, please try after sometime!");
        }
    }

    void showConfirmDialogue() {

        new MaterialAlertDialogBuilder(activity, R.style.RoundShapeTheme)
                .setTitle("Sure")
                .setMessage("Are you sure you want to change vendor detail ?")
                .setCancelable(false)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        checkVendorExist();
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

    void checkVendorExist() {

        String parameter = "";

        try {
            showprogress("Changing Vendor details","Please wait...");

            CollectionReference vendorColl = globalclass.firebaseInstance().collection(Globalclass.vendorColl);
            Query checkInventoryExist = vendorColl.whereEqualTo("vendorMobileNumber",vendorMobileNumber.getText().toString().trim().toLowerCase());

            Gson gson = new Gson();
            if(model != null) {
                parameter = gson.toJson(model);
            }
            globalclass.log(tag,"parameter: "+parameter);

            String finalParameter = parameter;

            checkInventoryExist.get().addOnCompleteListener(activity, new OnCompleteListener<QuerySnapshot>() {
                @Override
                public void onComplete(@NonNull Task<QuerySnapshot> task) {

                    String source = task.getResult().getMetadata().isFromCache() ?
                            "local cache" : "server";
                    globalclass.log(tag,"checkVendorExist - Data fetched from "+source);

                    if(task.isSuccessful()) {
                        if(!task.getResult().isEmpty()) {
                            for(DocumentSnapshot documents : task.getResult()) {
                                vendorDetail model = documents.toObject(vendorDetail.class);
                                globalclass.log(tag,"Vendor name: "+model.getVendorName());
                            }

                            updateVendor();
                        }
                        else {
                            hideprogress();
                            globalclass.log(tag,"Vendor Mobile Number not exist!");
                            globalclass.snackit(activity,"No Vendor details found!");
                        }
                    }
                    else {
                        hideprogress();
                        String error = Log.getStackTraceString(task.getException());
                        globalclass.log(tag,"checkVendorExist: "+error);
                        globalclass.sendResponseErrorLog(tag,"checkVendorExist: ",error, finalParameter);
                        globalclass.toast_long("Unable to add vendor, please try after sometime!");
                    }
                }
            });
        }
        catch (Exception e) {

            hideprogress();
            String error = Log.getStackTraceString(e);
            globalclass.log(tag,"checkVendorExist: "+error);
            globalclass.sendResponseErrorLog(tag,"checkVendorExist: ",error, parameter);
            globalclass.toast_long("Unable to add vendor, please try after sometime!");
        }
    }

    void updateVendor() {

        String parameter = "";

        try {
            CollectionReference vendorColl = globalclass.firebaseInstance().collection(Globalclass.vendorColl);

            String vendorId = model.getVendorId();
            vendorDetail model = new vendorDetail();
            model.setVendorId(vendorId);
            model.setVendorName(vendorName.getText().toString().trim().toLowerCase());
            model.setVendorMobileNumber(vendorMobileNumber.getText().toString().trim().toLowerCase());
            model.setVendorShopAddress(vendorShopAddress.getText().toString().trim().replaceAll(System.lineSeparator(), " "));

            Gson gson = new GsonBuilder().create();
            String json = gson.toJson(model);
            Map<String,Object> map = new Gson().fromJson(json, Map.class);

            if(model != null) {
                parameter = gson.toJson(model);
            }
            globalclass.log(tag,"parameter: "+parameter);

            String finalParameter = parameter;

            DocumentReference vendorDocReference = vendorColl.document(vendorId);
            vendorDocReference.update(map)
                    .addOnCompleteListener(activity, new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {

                            hideprogress();
                            if(task.isSuccessful()) {
                                globalclass.toast_short("Vendor details changed successfully!");
                                mydatabase.addVendor(model);

                                Intent intent = new Intent();
                                intent.putExtra("position",getIntent().getStringExtra("position"));
                                intent.putExtra("vendorDetail",model);
                                setResult(RESULT_OK, intent);
                                finish();
                            }
                            else {
                                String error = Log.getStackTraceString(task.getException());
                                globalclass.log(tag,"updateVendor: "+error);
                                globalclass.sendResponseErrorLog(tag,"updateVendor: ",error, finalParameter);
                                globalclass.toast_long("Unable to change vendor details, please try after sometime!");
                            }
                        }
                    });
        }
        catch (Exception e) {

            String error = Log.getStackTraceString(e);
            globalclass.log(tag,"updateVendor: "+error);
            globalclass.sendResponseErrorLog(tag,"updateVendor: ",error, parameter);
            globalclass.toast_long("Unable to change vendor details, please try after sometime!");
        }
    }

    void enableUpdateButton() {
        vendorDetail vendorDetailModel = new vendorDetail();
        vendorDetailModel.setVendorName(vendorName.getText().toString().trim().toLowerCase());
        vendorDetailModel.setVendorMobileNumber(vendorMobileNumber.getText().toString().trim().toLowerCase());
        vendorDetailModel.setVendorShopAddress(vendorShopAddress.getText().toString().trim().replaceAll(System.lineSeparator(), " "));

        if(equals2(vendorDetailModel)) {
            updatebt.setEnabled(true);
        }
        else {
            updatebt.setEnabled(false);
        }
    }

    boolean equals2(Object object2) {  // equals2 method
        if(model.equals(object2)) { // if equals() method returns true
            return false; // return true
        }
        else return true; // if equals() method returns false, also return false
    }

    boolean validation()
    {
        String firstchar = "";
        if(vendorMobileNumber.getText().length() > 0)
        {
            String getmobileno = vendorMobileNumber.getText().toString();
            firstchar=getmobileno.substring(0,1);
        }

        if(vendorName.getText().length() < 3) {
            vendorNametf.setError("Should contain atleast 3 characters!");
            return false;
        }
        else if(!vendorName.getText().toString().trim().matches(globalclass.alphaNumericRegexOne())) {
            vendorNametf.setError("Invalid Vendor name!");
            return false;
        }
        else if(vendorMobileNumber.getText().length()!=10)
        {
            vendorMobileNumbertf.setError("Invalid mobile number");
            return false;
        }
        else if(!firstchar.equalsIgnoreCase("6") && !firstchar.equalsIgnoreCase("7") &&
                !firstchar.equalsIgnoreCase("8") && !firstchar.equalsIgnoreCase("9"))
        {
            vendorMobileNumbertf.setError("Invalid mobile number");
            return false;
        }
        else if(vendorShopAddress.getText().length() > 0)
        {
            if(vendorShopAddress.getText().length() < 10) {
                vendorShopAddresstf.setError("Should contain atleast 10 characters!");
                return false;
            }
            else {
                vendorNametf.setErrorEnabled(false);
                vendorMobileNumbertf.setErrorEnabled(false);
                vendorShopAddresstf.setErrorEnabled(false);
                return true;
            }
        }

        vendorNametf.setErrorEnabled(false);
        vendorMobileNumbertf.setErrorEnabled(false);
        vendorShopAddresstf.setErrorEnabled(false);
        return true;
    }

    void clearAll() {
        vendorName.setText("");
        vendorMobileNumber.setText("");
        vendorShopAddress.setText("");
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
}