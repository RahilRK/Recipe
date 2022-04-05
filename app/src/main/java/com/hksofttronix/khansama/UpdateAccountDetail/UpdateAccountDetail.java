package com.hksofttronix.khansama.UpdateAccountDetail;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.IntentSender;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.google.android.gms.auth.api.credentials.Credential;
import com.google.android.gms.auth.api.credentials.Credentials;
import com.google.android.gms.auth.api.credentials.CredentialsClient;
import com.google.android.gms.auth.api.credentials.HintRequest;
import com.google.android.gms.auth.api.credentials.IdentityProviders;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.hksofttronix.khansama.Globalclass;
import com.hksofttronix.khansama.Models.inventoryDetail;
import com.hksofttronix.khansama.Models.userDetail;
import com.hksofttronix.khansama.R;

import java.util.Map;

public class UpdateAccountDetail extends AppCompatActivity {

    String tag = this.getClass().getSimpleName();
    AppCompatActivity activity = UpdateAccountDetail.this;

    Globalclass globalclass;
    CredentialsClient mCredentialsApiClient;

    SwipeRefreshLayout swipeRefresh;
    TextView mobileNumber;
    TextInputLayout nametf;
    EditText name, emailid;
    Button updateacbt;

    public static int RESOLVE_HINT = 111;

    userDetail model = new userDetail();

    ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update_account_detail);

        init();
        binding();
        setToolbar();
        setText();
        onClick();
    }

    void init() {
        globalclass = Globalclass.getInstance(activity);
        mCredentialsApiClient = Credentials.getClient(this);
    }

    void binding() {
        swipeRefresh = findViewById(R.id.swipeRefresh);
        mobileNumber = findViewById(R.id.mobileNumber);
        nametf = findViewById(R.id.nametf);
        name = findViewById(R.id.name);
        emailid = findViewById(R.id.emailid);
        updateacbt = findViewById(R.id.updateacbt);
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

                getUserDetail();
            }
        });

        name.addTextChangedListener(new TextWatcher() {
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

        emailid.addTextChangedListener(new TextWatcher() {
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

        emailid.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                requestEmailId();
            }
        });

        updateacbt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                globalclass.hideKeyboard(activity);

                if (!globalclass.isInternetPresent()) {
                    globalclass.toast_long(getResources().getString(R.string.noInternetConnection));
                    return;
                }

                if(validation()) {
                    checkEmailIdExist();
                }
            }
        });
    }

    void setText() {

        model.setId(globalclass.getStringData("id"));
        model.setMobilenumber(globalclass.getStringData("mobilenumber"));
        model.setName(globalclass.getStringData("name"));
        model.setAdmin(globalclass.getBooleanData("admin"));

        mobileNumber.setText(globalclass.getStringData("mobilenumber"));
        name.setText(globalclass.getStringData("name"));
        if(!globalclass.getStringData("emailid").equalsIgnoreCase("Not available")) {
            emailid.setText(globalclass.getStringData("emailid"));
            model.setEmailid(globalclass.getStringData("emailid"));
        }
        else {
            model.setEmailid("");
        }
    }

    void setToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_close);
        toolbar.setTitle("Account details");
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                onBackPressed();
            }
        });
    }

    void getUserDetail() {

        String parameter = "";

        try {
            swipeRefresh.setRefreshing(true);

            CollectionReference userColl = globalclass.firebaseInstance().collection(Globalclass.userColl);
            Query checkUserExist = userColl.whereEqualTo("id", model.getId());

            Gson gson = new Gson();
            if(model != null) {
                parameter = gson.toJson(model);
            }
            globalclass.log(tag,"parameter: "+parameter);

            String finalParameter = parameter;

            checkUserExist.get().addOnCompleteListener(activity, new OnCompleteListener<QuerySnapshot>() {
                @Override
                public void onComplete(@NonNull Task<QuerySnapshot> task) {

                    swipeRefresh.setRefreshing(false);
                    globalclass.snackit(activity,"Refresh successfully!");

                    if(task.isSuccessful()) {
                        if(!task.getResult().isEmpty()) {
                            for(DocumentSnapshot documents : task.getResult()) {
                                model = documents.toObject(userDetail.class);
                                globalclass.log(tag,"User name: "+ model.getName());
                            }

                            globalclass.setStringData("id",model.getId());
                            globalclass.setStringData("name",model.getName());
                            globalclass.setStringData("emailid",model.getEmailid());
                            globalclass.setStringData("mobilenumber",model.getMobilenumber());
                            globalclass.setBooleanData("admin",model.getAdmin());
                            setText();
                        }
                    }
                    else {
                        String error = Log.getStackTraceString(task.getException());
                        globalclass.log(tag,"getUserDetail: "+error);
                        globalclass.toast_long("Unable to get detail, please try after sometime!");
                        globalclass.sendResponseErrorLog(tag,"getUserDetail: ",error, finalParameter);
                    }
                }
            });
        }
        catch (Exception e) {

            swipeRefresh.setRefreshing(false);
            String error = Log.getStackTraceString(e);
            globalclass.log(tag,"getUserDetail: "+error);
            globalclass.toast_long("Unable to get detail, please try after sometime!");
            globalclass.sendResponseErrorLog(tag,"getUserDetail: ",error, parameter);
        }
    }

    void checkEmailIdExist() {

        String parameter = "";

        try {
            showprogress("Changing account details","Please wait...");

            CollectionReference userColl = globalclass.firebaseInstance().collection(Globalclass.userColl);
            Query query = userColl.whereEqualTo("emailid",emailid.getText().toString().trim().toLowerCase())
                    .whereNotEqualTo("id",model.getId());

            Gson gson = new Gson();
            if(model != null) {
                parameter = gson.toJson(model);
            }
            globalclass.log(tag,"parameter: "+parameter);

            String finalParameter = parameter;

            query.get().addOnCompleteListener(activity, new OnCompleteListener<QuerySnapshot>() {
                @Override
                public void onComplete(@NonNull Task<QuerySnapshot> task) {
                    if(task.isSuccessful()) {
                        if(!task.getResult().isEmpty()) {
                            hideprogress();
                            globalclass.snackit(activity,"EmailId already exist!");
                        }
                        else {
                            changeAccountDetails();
                        }
                    }
                    else {
                        hideprogress();
                        String error = Log.getStackTraceString(task.getException());
                        globalclass.log(tag,"checkEmailIdExist: "+error);
                        globalclass.toast_long("Unable to change detail, please try after sometime!");
                        globalclass.sendResponseErrorLog(tag,"checkEmailIdExist: ",error, finalParameter);
                    }
                }
            });
        }
        catch (Exception e) {

            hideprogress();
            String error = Log.getStackTraceString(e);
            globalclass.log(tag,"checkEmailIdExist: "+error);
            globalclass.toast_long("Unable to change detail, please try after sometime!");
            globalclass.sendResponseErrorLog(tag,"checkEmailIdExist: ",error, parameter);
        }
    }

    void changeAccountDetails() {

        try {
            CollectionReference userColl = globalclass.firebaseInstance().collection(Globalclass.userColl);
            String id = model.getId();

            Gson gson = new GsonBuilder().create();
            String json = gson.toJson(model);
            Map<String,Object> map = new Gson().fromJson(json, Map.class);
            if(emailid.getText().length() > 0) {
                map.put("emailid",emailid.getText().toString().trim().toLowerCase());
            }
            else {
                map.put("emailid","Not available");
            }
            map.put("name",name.getText().toString().toLowerCase());
            map.put("admin",model.getAdmin());

            DocumentReference vendorDocReference = userColl.document(id);
            vendorDocReference.update(map)
                    .addOnCompleteListener(activity, new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            hideprogress();
                            if(task.isSuccessful()) {
                                changeDataLocally();
                                globalclass.toast_long("Account details changed successfully!");
                                onBackPressed();
                            }
                            else {
                                String error = Log.getStackTraceString(task.getException());
                                globalclass.log(tag,"changeAccountDetails: "+error);
                                globalclass.toast_long("Unable to change vendor details, please try after sometime!");
                                globalclass.sendErrorLog(tag, "changeAccountDetails", error);
                            }
                        }
                    });
        }
        catch (Exception e) {
            hideprogress();
            String error = Log.getStackTraceString(e);
            globalclass.log(tag,"changeAccountDetails: "+error);
            globalclass.toast_long("Unable to change vendor details, please try after sometime!");
            globalclass.sendErrorLog(tag, "changeAccountDetails", error);
        }
    }

    void changeDataLocally() {
        globalclass.setStringData("name",name.getText().toString().toLowerCase());
        if(globalclass.checknull(globalclass.getStringData("adminId")).equalsIgnoreCase(model.getId())) {
            globalclass.setStringData("adminName",name.getText().toString().toLowerCase());
        }

        if(emailid.getText().length() > 0) {
            globalclass.setStringData("emailid",emailid.getText().toString().trim().toLowerCase());
        }
        else {
            globalclass.setStringData("emailid","Not available");
        }
    }

    void requestEmailId() {
        HintRequest hintRequest = new HintRequest.Builder()
                .setAccountTypes(IdentityProviders.GOOGLE)
                .build();

        PendingIntent intent = mCredentialsApiClient.getHintPickerIntent(hintRequest);
        try {
            startIntentSenderForResult(intent.getIntentSender(),
                    RESOLVE_HINT, null, 0, 0, 0);
        } catch (IntentSender.SendIntentException e) {

            String error = Log.getStackTraceString(e);
            globalclass.log(tag ,"requestEmailId: "+error);
            globalclass.sendErrorLog(tag, "requestEmailId", error);
        }
    }

    void enableUpdateButton() {
        userDetail userDetailModel = new userDetail();
        userDetailModel.setMobilenumber(mobileNumber.getText().toString().trim());
        userDetailModel.setName(name.getText().toString().trim());
        userDetailModel.setEmailid(emailid.getText().toString().trim());

        if(equals2(userDetailModel)) {
            updateacbt.setEnabled(true);
        }
        else {
            updateacbt.setEnabled(false);
        }
    }

    boolean equals2(Object object2) {  // equals2 method
        if(model.equals(object2)) { // if equals() method returns true
            return false; // return true
        }
        else return true; // if equals() method returns false, also return false
    }

    boolean validation() {
        if(name.getText().toString().length() < 3) {
            nametf.setError("Should contain atleast 3 characters!");
            return false;
        }
        else if (!name.getText().toString().matches(globalclass.alphaNumericRegexOne())) {
            nametf.setError("Only alphates and numbers are allow!");
            return false;
        }

        nametf.setErrorEnabled(false);
        return true;
    }

    void showprogress(String title,String message) {
        progressDialog = new ProgressDialog(activity);
        progressDialog.setCancelable(false);
        progressDialog.setTitle(title);
        progressDialog.setMessage(message);
        progressDialog.show();
    }

    void hideprogress() {
        if(progressDialog != null && progressDialog.isShowing()) {
            progressDialog.dismiss();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RESOLVE_HINT) {
            if (resultCode == RESULT_OK) {

                Credential credential = data.getParcelableExtra(Credential.EXTRA_KEY);

                try {
                    String getemailid = credential.getId();
                    emailid.setText(getemailid);
//                    globalclass.log(tag, getemailid);
                } catch (Exception e) {
                    String error = Log.getStackTraceString(e);
                    globalclass.log(tag,"onActivityResult: "+error);
                    globalclass.sendErrorLog(tag, "onActivityResult", error);
                }
            }
        }
    }
}