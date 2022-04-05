package com.hksofttronix.khansama.Admin.AddVendor;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.EditText;

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
import com.hksofttronix.khansama.Models.vendorDetail;
import com.hksofttronix.khansama.R;
import com.hksofttronix.khansama.Globalclass;
import com.hksofttronix.khansama.Mydatabase;

public class AddVendor extends AppCompatActivity {

    String tag = this.getClass().getSimpleName();
    AppCompatActivity activity = AddVendor.this;

    Globalclass globalclass;
    Mydatabase mydatabase;

    TextInputLayout vendorNametf,vendorMobileNumbertf,vendorShopAddresstf;
    EditText vendorName,vendorMobileNumber,vendorShopAddress;
    MaterialButton addbt;

    boolean vendorAdded = false;
    ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_vendor);

        setToolbar();
        init();
        binding();
        onClick();
    }

    void init() {
        globalclass = Globalclass.getInstance(activity);
        mydatabase = Mydatabase.getInstance(activity);
    }

    void binding() {
        vendorNametf = findViewById(R.id.vendorNametf);
        vendorMobileNumbertf = findViewById(R.id.vendorMobileNumbertf);
        vendorShopAddresstf = findViewById(R.id.vendorShopAddresstf);
        vendorName = findViewById(R.id.vendorName);
        vendorMobileNumber = findViewById(R.id.vendorMobileNumber);
        vendorShopAddress = findViewById(R.id.vendorShopAddress);
        addbt = findViewById(R.id.addbt);
    }

    void onClick() {

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

                    if(mydatabase.checkVendorNumberExist(editable.toString()) > 0) {
                        vendorMobileNumbertf.setError("Vendor details already exist");
                    }
                    else {
                        vendorMobileNumbertf.setErrorEnabled(false);
                    }
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
        toolbar.setTitle(getString(R.string.add_vendor));
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                onBackPressed();
            }
        });
    }

    void showConfirmDialogue() {

        new MaterialAlertDialogBuilder(activity, R.style.RoundShapeTheme)
                .setTitle("Sure")
                .setMessage("Are you sure you want to add new vendor ?")
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

        try {
            showprogress("Adding Vendor details","Please wait...");

            CollectionReference vendorColl = globalclass.firebaseInstance().collection(Globalclass.vendorColl);
            Query checkInventoryExist = vendorColl.whereEqualTo("vendorMobileNumber",vendorMobileNumber.getText().toString().trim().toLowerCase());
            checkInventoryExist.get().addOnCompleteListener(activity, new OnCompleteListener<QuerySnapshot>() {
                @Override
                public void onComplete(@NonNull Task<QuerySnapshot> task) {

                    if(task.isSuccessful()) {
                        if(!task.getResult().isEmpty()) {
                            for(DocumentSnapshot documents : task.getResult()) {
                                vendorDetail model = documents.toObject(vendorDetail.class);
                                globalclass.log(tag,"Vendor name: "+model.getVendorName());
                            }

                            hideprogress();
                            globalclass.snackit(activity,"Vendor details already exist!");
                        }
                        else {
                            globalclass.log(tag,"Vendor Mobile Number not exist!");
                            addVendor();
                        }
                    }
                    else {
                        hideprogress();
                        String error = Log.getStackTraceString(task.getException());
                        globalclass.log(tag,"checkVendorExist: "+error);
                        globalclass.sendErrorLog(tag,"checkVendorExist: ",error);
                        globalclass.toast_long("Unable to add vendor, please try after sometime!");
                    }
                }
            });
        }
        catch (Exception e) {

            hideprogress();
            String error = Log.getStackTraceString(e);
            globalclass.log(tag,"checkVendorExist: "+error);
            globalclass.sendErrorLog(tag,"checkVendorExist: ",error);
            globalclass.toast_long("Unable to add vendor, please try after sometime!");
        }

    }

    void addVendor() {

        String parameter = "";

        try {
            CollectionReference vendorColl = globalclass.firebaseInstance().collection(Globalclass.vendorColl);

            String vendorId = vendorColl.document().getId();
            vendorDetail model = new vendorDetail();
            model.setVendorId(vendorId);
            model.setVendorName(vendorName.getText().toString().trim().toLowerCase());
            model.setVendorMobileNumber(vendorMobileNumber.getText().toString().trim().toLowerCase());
            model.setVendorShopAddress(vendorShopAddress.getText().toString().trim().replaceAll(System.lineSeparator(), " "));

            Gson gson = new Gson();
            if(model != null) {
                parameter = gson.toJson(model);
            }
            globalclass.log(tag,"parameter: "+parameter);

            String finalParameter = parameter;

            DocumentReference vendorDocReference = vendorColl.document(vendorId);
            vendorDocReference.set(model)
                    .addOnCompleteListener(activity, new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            hideprogress();
                            if(task.isSuccessful()) {
                                vendorAdded = true;
                                globalclass.snackit(activity,"Added successfully!");
                                mydatabase.addVendor(model);
                                clearAll();
                            }
                            else {
                                String error = Log.getStackTraceString(task.getException());
                                globalclass.log(tag,"addVendor: "+error);
                                globalclass.toast_long("Unable to add vendor, please try after sometime!");
                                globalclass.sendResponseErrorLog(tag,"addVendor: ",error, finalParameter);
                            }
                        }
                    });
        }
        catch (Exception e) {

            hideprogress();
            String error = Log.getStackTraceString(e);
            globalclass.log(tag, "addVendor: " + error);
            globalclass.toast_long("Unable to add vendor, please try after sometime!");
            globalclass.sendResponseErrorLog(tag,"addVendor: ",error,parameter);
        }
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
        else if(mydatabase.checkVendorNumberExist(vendorMobileNumber.getText().toString().trim()) > 0) {
            vendorMobileNumbertf.setError("Vendor details already exist");
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

    @Override
    public void onBackPressed() {
        if(vendorAdded) {
            Intent intent = new Intent();
            setResult(RESULT_OK, intent);
            finish();
        }
        else {
            super.onBackPressed();
        }
    }
}