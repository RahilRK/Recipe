package com.hksofttronix.khansama;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthOptions;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.gson.Gson;
import com.hksofttronix.khansama.Models.userDetail;
import com.mukesh.OtpView;

import java.util.concurrent.TimeUnit;

public class Verification extends AppCompatActivity {

    String tag = this.getClass().getSimpleName();
    AppCompatActivity activity = Verification.this;

    Globalclass globalclass;
    FirebaseAuth mAuth;
    CountDownTimer cTimer = null;

    String getmVerificationId;
    String mobilenumber;
    int resendOtpCount = 0;

    OtpView edcode;
    TextView tvverificationcodemsg,tvtimer;
    MaterialButton verifyotpbt, resendotpbt;

    ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_verification);

        init();
        binding();
        onclick();
        setToolbar();
        mobilenumber = getIntent().getStringExtra("mobilenumber");

        if(BuildConfig.DEBUG) {
            handleIntent();
        }
        else {
            sentOtp(mobilenumber);
        }

//        if(globalclass.verificationNeeded(mobilenumber)) {
//            sentOtp(mobilenumber);
//        }
//        else {
//            handleIntent();
//        }
    }

    void init() {
        globalclass = Globalclass.getInstance(activity);
        mAuth = FirebaseAuth.getInstance();
    }

    void binding() {
        tvverificationcodemsg = findViewById(R.id.tvverificationcodemsg);
        tvtimer = findViewById(R.id.tvtimer);
        verifyotpbt = findViewById(R.id.verifyotpbt);
        resendotpbt = findViewById(R.id.resendotpbt);
        edcode = findViewById(R.id.code);
    }

    void onclick() {
        verifyotpbt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (!globalclass.isInternetPresent()) {
                    globalclass.toast_long(getResources().getString(R.string.noInternetConnection));
                    return;
                }

                globalclass.hideKeyboard(activity);
                if (edcode.getText().length() != 6) {
                    globalclass.snackit(activity, "Please enter the otp");
                } else if (edcode.getText().length() == 6) {
                    showprogress("Please wait...","Verifying otp");
                    verifyCode(edcode.getText().toString());
                }
            }
        });

        resendotpbt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (!globalclass.isInternetPresent()) {
                    globalclass.toast_long(getResources().getString(R.string.noInternetConnection));
                    return;
                }

                globalclass.hideKeyboard(activity);

                resendOtpCount++;
                if (resendOtpCount >= 3) {
                    globalclass.showDialogue(activity, "Unable to proceed", "Your maximum attempt has been done, please try after sometime");
                } else {
//                    startTimer();
                    sentOtp(mobilenumber);
                }
            }
        });
    }

    void setToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
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

    //TODO #1 sentOtp
    void sentOtp(String getMobileNo) {
        try {
            if(BuildConfig.DEBUG) {
//                edcode.setText(getString(R.string.code));
                mAuth.useEmulator(Globalclass.localIP,Globalclass.localAuthPORT);
            }
            showprogress("Please wait...","Sending otp");

            PhoneAuthOptions options =
                    PhoneAuthOptions.newBuilder(mAuth)
                            .setPhoneNumber("+91" + getMobileNo)       // Phone number to verify
                            .setTimeout(30L, TimeUnit.SECONDS) // Timeout and unit
                            .setActivity(activity)                 // Activity (for callback binding)
                            .setCallbacks(mCallBack)          // OnVerificationStateChangedCallbacks
                            .build();
            PhoneAuthProvider.verifyPhoneNumber(options);
        } catch (Exception e) {
            hideprogress();
            String error = Log.getStackTraceString(e);
            globalclass.log(tag ,"sentOtp:"+error);
            globalclass.toast_long("Unable to send Otp!");
            globalclass.sendErrorLog(tag, "sentOtp", error);
        }
    }

    //TODO #2
    PhoneAuthProvider.OnVerificationStateChangedCallbacks
            mCallBack = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

        @Override
        public void onCodeSent(String mVerificationId, PhoneAuthProvider.ForceResendingToken forceResendingToken) {
            super.onCodeSent(mVerificationId, forceResendingToken);

            getmVerificationId = mVerificationId;
            globalclass.log(tag + "_onCodeSent", getmVerificationId);
            tvverificationcodemsg.setText(
                    Html.fromHtml(getString(R.string.please_type_the_verification_code_sent_to, "+91"+mobilenumber)));
            hideprogress();
            startTimer();

            if(BuildConfig.DEBUG) {
                if (edcode.getText().length() == 6) {
                    verifyCode(edcode.getText().toString()); //todo add this to autoverify otp
                }
            }
        }

        @Override
        public void onVerificationCompleted(PhoneAuthCredential phoneAuthCredential) {
            String code = phoneAuthCredential.getSmsCode();

            if (code != null) {
                edcode.setText(code);
                verifyCode(code);
                globalclass.log(tag + "_onVerificationCompleted", code);
            } else {
                String error = "Code is Null";
                globalclass.log(tag,"onVerificationCompleted: "+error);
                globalclass.sendErrorLog(tag, "onVerificationCompleted", error);
            }
        }

        @Override
        public void onVerificationFailed(FirebaseException e) {

            hideprogress();
            globalclass.toast_long("Unable to send code due to bad network connection");
            String error = Log.getStackTraceString(e);
            globalclass.log(tag,"onVerificationFailed: "+error);
            globalclass.sendErrorLog(tag, "onVerificationFailed", error);
        }
    };

    //TODO #3 verify code
    void verifyCode(String code) {
        PhoneAuthCredential credential = PhoneAuthProvider.getCredential(getmVerificationId, code);
        signInWithCredential(credential);
    }

    void signInWithCredential(PhoneAuthCredential credential) {
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {

                            FirebaseUser userdetail = task.getResult().getUser();
                            String userid = userdetail.getUid();
                            globalclass.log(tag, "Verified successfully auth_userid: " + userid);

                            hideprogress();
                            handleIntent();
                        } else {

                            hideprogress();
                            globalclass.toast_short("Invalid Otp");
                            globalclass.log(tag + "_signInWithCredential", task.getException().getMessage());
                        }
                    }
                });
    }

    void handleIntent() {

        //todo update/set new verification datetime of mobile number!
        globalclass.setVerifiedMobileNumber(mobilenumber);

        Intent intent = getIntent();
        if(intent.getStringExtra("action").equalsIgnoreCase("Login")) {
            String loginType = intent.getStringExtra("loginType");
            userDetail model = intent.getParcelableExtra("model");
            globalclass.setStringData("loginType",loginType);
            startActivity(new Intent(activity, SetUpPage.class).putExtra("model",model)
                    .putExtra("loginType",loginType)
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK));
            finish();
        }
        else if(intent.getStringExtra("action").equalsIgnoreCase("CreateAccount")) {

            userDetail model = intent.getParcelableExtra("model");
            createAccount(model);
        }
        else if(intent.getStringExtra("action").equalsIgnoreCase("AddNewAdmin")) {
            intent = new Intent();
            setResult(RESULT_OK, intent);
            finish();
        }
        else if(intent.getStringExtra("action").equalsIgnoreCase("AddAddress")) {
            intent = new Intent();
            setResult(RESULT_OK, intent);
            finish();
        }
        else if(intent.getStringExtra("action").equalsIgnoreCase("UpdateAddress")) {
            intent = new Intent();
            setResult(RESULT_OK, intent);
            finish();
        }
    }

    void createAccount(userDetail model) {

        String parameter = "";

        try {
            showprogress("Creating account","Please wait...");

            CollectionReference userColl = globalclass.firebaseInstance().collection(Globalclass.userColl);
            String id = userColl.document().getId();
            model.setId(id);

            DocumentReference documentReference = userColl.document(id);

            Gson gson = new Gson();
            if(model != null) {
                parameter = gson.toJson(model);
            }
            globalclass.log(tag,"parameter: "+parameter);

            String finalParameter = parameter;

            documentReference.set(model).addOnCompleteListener(activity, new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {

                    hideprogress();
                    if(task.isSuccessful()) {

                        globalclass.setStringData("loginType",getIntent().getStringExtra("loginType"));
                        startActivity(new Intent(activity, SetUpPage.class).putExtra("model",model)
                                .putExtra("loginType",getIntent().getStringExtra("loginType"))
                                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK));
                        finish();
                    }
                    else {
                        String error = Log.getStackTraceString(task.getException());
                        globalclass.log(tag,"createAccount: "+error);
                        globalclass.toast_long("Unable to create account, please try after sometime!");
                        globalclass.sendResponseErrorLog(tag,"createAccount: ",error, finalParameter);
                    }
                }
            });
        }
        catch (Exception e) {

            hideprogress();
            String error = Log.getStackTraceString(e);
            globalclass.log(tag,"createAccount: "+error);
            globalclass.toast_long("Unable to create account, please try after sometime!");
            globalclass.sendResponseErrorLog(tag,"createAccount: ",error, parameter);
        }
    }

    //start timer function
    void startTimer() {

        tvtimer.setVisibility(View.VISIBLE);

        cTimer = new CountDownTimer(30000, 1000) {
            public void onTick(long millisUntilFinished) {

                String getSecond = "" + millisUntilFinished / 1000;
//                globalclass.log(TAG+"_onTick","seconds remaining: " + getSecond);
                if (Integer.parseInt(getSecond) == 0 || Integer.parseInt(getSecond) == 1) {
                    tvtimer.setText(getSecond + " second to go!");
                } else {
                    tvtimer.setText(getSecond + " seconds to go!");
                }
                tvtimer.setTextColor(getResources().getColor(R.color.secondaryColor));
                resendotpbt.setVisibility(View.GONE);
                verifyotpbt.setVisibility(View.VISIBLE);
            }

            public void onFinish() {

                cTimer = null;
                tvtimer.setText("Otp has been expired!");
                tvtimer.setTextColor(getResources().getColor(R.color.mgray));
                verifyotpbt.setVisibility(View.GONE);
                resendotpbt.setVisibility(View.VISIBLE);
            }
        };
        cTimer.start();
    }

    //cancel timer
    void cancelTimer() {
        if (cTimer != null)
            cTimer.cancel();
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

    void showStopVerificationDialogue(Activity activity, String message) {
        new MaterialAlertDialogBuilder(activity, R.style.RoundShapeTheme)
                .setTitle("Warning")
                .setMessage(message)
                .setCancelable(false)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        dialog.dismiss();
                        cancelTimer();
                        finishAffinity();
                        finish();
                    }
                })
                .setNeutralButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        dialog.dismiss();
                    }
                })
                .show();
    }

    @Override
    public void onBackPressed() {

        if (cTimer == null) {
            globalclass.log(tag, "timer is not running");
            super.onBackPressed();
            cancelTimer();
        } else {
            globalclass.log(tag, "timer is running");
            showStopVerificationDialogue(activity, "Verification is pending, do you still want to cancel verification and exit?");
        }
    }
}