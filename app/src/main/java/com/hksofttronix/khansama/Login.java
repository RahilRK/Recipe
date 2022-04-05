package com.hksofttronix.khansama;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.IntentSender;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;

import com.google.android.gms.auth.api.credentials.Credential;
import com.google.android.gms.auth.api.credentials.Credentials;
import com.google.android.gms.auth.api.credentials.CredentialsClient;
import com.google.android.gms.auth.api.credentials.HintRequest;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.hksofttronix.khansama.Models.userDetail;

public class Login extends AppCompatActivity {

    String tag = this.getClass().getSimpleName();
    AppCompatActivity activity = Login.this;

    Globalclass globalclass;
    Mydatabase mydatabase;
    CredentialsClient mCredentialsApiClient;

    TextInputLayout mobilenotf;
    EditText mobileno;
    MaterialButton sendotpbt;

    public static int RESOLVE_HINT = 111;

    ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        init();
        binding();
        onClick();
//        setToolbar();
        requestMobilenumber();
    }

    void init() {
        globalclass = Globalclass.getInstance(activity);
        mydatabase = Mydatabase.getInstance(activity);
        mCredentialsApiClient = Credentials.getClient(this);
    }

    void binding() {
        mobilenotf = findViewById(R.id.mobilenotf);
        mobileno = findViewById(R.id.mobileno);
        sendotpbt = findViewById(R.id.sendotpbt);
    }

    void onClick() {

        mobileno.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {

                if(actionId == EditorInfo.IME_ACTION_DONE) {

                    globalclass.hideKeyboard(activity);

                    if(!globalclass.isInternetPresent()) {
                        globalclass.toast_long(getResources().getString(R.string.noInternetConnection));
                        return false;
                    }

                    if(validation()) {
                        checkUserExist();
                    }

                }
                return false;
            }
        });
        sendotpbt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                globalclass.hideKeyboard(activity);

                if(!globalclass.isInternetPresent()) {
                    globalclass.toast_long(getResources().getString(R.string.noInternetConnection));
                    return;
                }

                if(validation()) {
                    showprogress("Hold on","Please wait...");
                    checkUserExist();
                }
            }
        });
    }

    void setToolbar() {
        Toolbar toolbar =  findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_close);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                onBackPressed();
            }
        });
    }

    void checkUserExist() {
        try {
            showprogress("Hold on","Please wait...");

            CollectionReference userColl = globalclass.firebaseInstance().collection(Globalclass.userColl);
            Query checkInUserColl = userColl.whereEqualTo("mobilenumber",mobileno.getText().toString().trim());
            checkInUserColl.get().addOnSuccessListener(activity,new OnSuccessListener<QuerySnapshot>() {
                @Override
                public void onSuccess(QuerySnapshot queryDocumentSnapshots) {

                    hideprogress();
                    globalclass.log(tag,""+queryDocumentSnapshots.getDocuments().size());
                    if(queryDocumentSnapshots.getDocuments().size() > 0) {

                        for(DocumentSnapshot documents : queryDocumentSnapshots) {
                            userDetail model = documents.toObject(userDetail.class);
                            if(!model.getAdmin()) {
                                globalclass.log(tag,"User: "+model.getName());
                                startActivity(new Intent(activity, Verification.class)
                                        .putExtra("action",tag)
                                        .putExtra("mobilenumber",mobileno.getText().toString().trim())
                                        .putExtra("model",model)
                                        .putExtra("loginType","User"));
                            }
                            else {
                                globalclass.log(tag,"Admin: "+model.getName());
                                startActivity(new Intent(activity, Verification.class)
                                        .putExtra("action",tag)
                                        .putExtra("mobilenumber",mobileno.getText().toString().trim())
                                        .putExtra("model",model)
                                        .putExtra("loginType","Admin"));
                            }
                        }
                    }
                    else {
                        globalclass.log(tag,"Please create account to login!");
                        userDetail model = new userDetail();
                        model.setAdmin(false);
                        model.setMobilenumber(mobileno.getText().toString().trim());
                        model.setName("");
                        model.setEmailid("");

                        startActivity(new Intent(activity, Verification.class)
                                .putExtra("action","CreateAccount")
                                .putExtra("mobilenumber",mobileno.getText().toString().trim())
                                .putExtra("model",model)
                                .putExtra("loginType","User"));
                    }
                }
            }).addOnFailureListener(activity,new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {

                    hideprogress();
                    String error = Log.getStackTraceString(e);
                    globalclass.log(tag,error);
                    globalclass.toast_long("Unable to login, please try after sometime!");
                    globalclass.sendErrorLog(tag, "checkUserExist", error);
                }
            });
        }
        catch (Exception e) {

            hideprogress();
            String error = Log.getStackTraceString(e);
            globalclass.log(tag,error);
            globalclass.toast_long("Unable to login, please try after sometime!");
            globalclass.sendErrorLog(tag, "checkUserExist", error);
        }
    }

    boolean validation()
    {
        if(mobileno.getText().length()>0)
        {
            String getmobileno = mobileno.getText().toString();
            String firstchar=getmobileno.substring(0,1);

            if(mobileno.getText().length()!=10)
            {
                mobilenotf.setError("Invalid mobile number");
                return false;
            }
            else if(!firstchar.equalsIgnoreCase("6") && !firstchar.equalsIgnoreCase("7") &&
                    !firstchar.equalsIgnoreCase("8") && !firstchar.equalsIgnoreCase("9"))
            {
                mobilenotf.setError("Invalid mobile number");
                return false;
            }
            else
            {
                mobilenotf.setErrorEnabled(false);
                return true;
            }
        }
        else
        {
            mobilenotf.setError("Invalid mobile number");
            return false;
        }
    }

    void requestMobilenumber() {
        HintRequest hintRequest = new HintRequest.Builder()
                .setPhoneNumberIdentifierSupported(true)
                .build();

        PendingIntent intent =mCredentialsApiClient.getHintPickerIntent(hintRequest);
        try {
            startIntentSenderForResult(intent.getIntentSender(),
                    RESOLVE_HINT, null, 0, 0, 0);
        } catch (IntentSender.SendIntentException e) {

            String error = Log.getStackTraceString(e);
            globalclass.log(tag,"checkUserExist"+error);
            globalclass.sendErrorLog(tag, "checkUserExist", error);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RESOLVE_HINT) {
            if (resultCode == RESULT_OK) {

                Credential credential = data.getParcelableExtra(Credential.EXTRA_KEY);

                try {
                    String number = credential.getId();
                    String final_number = number;
                    if (number.length() > 10) {
                        final_number = number.substring(number.length() - 10);
                    }
                    mobileno.setText(final_number);
                }
                catch (Exception e) {
                    String error = Log.getStackTraceString(e);
                    globalclass.log(tag,"onActivityResult: "+error);
                    globalclass.sendErrorLog(tag, "onActivityResult", error);
                }
            }
        }
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