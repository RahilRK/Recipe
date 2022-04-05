package com.hksofttronix.khansama.AddAddress;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.WriteBatch;
import com.google.gson.Gson;
import com.hksofttronix.khansama.Models.addressDetail;
import com.hksofttronix.khansama.Models.cityDetail;
import com.hksofttronix.khansama.Models.stateDetail;
import com.hksofttronix.khansama.Models.userDetail;
import com.hksofttronix.khansama.R;
import com.hksofttronix.khansama.Globalclass;
import com.hksofttronix.khansama.Mydatabase;
import com.hksofttronix.khansama.Verification;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class AddAddress extends AppCompatActivity {

    String tag = this.getClass().getSimpleName();
    AppCompatActivity activity = AddAddress.this;

    Globalclass globalclass;
    Mydatabase mydatabase;

    TextInputLayout nametf, mobileNumbertf, addresstf, nearbytf;
    EditText name, mobileNumber, address, nearby;
    RelativeLayout selectCity;
    TextView city, state, pincode;
    ImageView ivrefreshCityState;
    MaterialButton addAddressbt;

    ArrayList<cityDetail> cityDetailList = new ArrayList<>();
    ArrayList<String> cityList = new ArrayList<>();
    int selectedCityPos = -1;

    String selectedCityId;
    String selectedCityName;
    String selectedStateId;
    String selectedStateName;
    String selectedPincode;

    ArrayList<stateDetail> stateDetailList = new ArrayList<>();

    int addressCount = 0;

    int VERIFY_MOBILENUMBER = 111;

    boolean newAddressAdded = false;
    ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_address);

        setToolbar();
        init();
        binding();
        onClick();
        setText();
        fillCityList();
        fillStateList();
    }

    void init() {
        globalclass = Globalclass.getInstance(activity);
        mydatabase = Mydatabase.getInstance(activity);
    }

    void setToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_backarrow);
        toolbar.setTitle(R.string.add_new_address);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                onBackPressed();
            }
        });
    }

    void binding() {
        nametf = findViewById(R.id.nametf);
        mobileNumbertf = findViewById(R.id.mobileNumbertf);
        addresstf = findViewById(R.id.addresstf);
        nearbytf = findViewById(R.id.nearbytf);
        name = findViewById(R.id.name);
        mobileNumber = findViewById(R.id.mobileNumber);
        address = findViewById(R.id.address);
        nearby = findViewById(R.id.nearby);
        selectCity = findViewById(R.id.selectCity);
        city = findViewById(R.id.city);
        state = findViewById(R.id.state);
        pincode = findViewById(R.id.pincode);
        ivrefreshCityState = findViewById(R.id.ivrefreshCityState);
        addAddressbt = findViewById(R.id.addAddressbt);
    }

    void onClick() {

        ivrefreshCityState.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (!globalclass.isInternetPresent()) {
                    globalclass.toast_long(getResources().getString(R.string.noInternetConnection));
                    return;
                }

                getCityList();
                getStateList();
            }
        });

        selectCity.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                showCityListDialogue();
            }
        });

        addAddressbt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (!globalclass.isInternetPresent()) {
                    globalclass.toast_long(getResources().getString(R.string.noInternetConnection));
                    return;
                }

                if (validation()) {
                    showConfirmDialogue();
                }
            }
        });
    }

    void setText() {
        mobileNumber.setText(globalclass.getStringData("mobilenumber"));
    }

    void checkAddressCount() {

        try {
            showprogress("Hold on", "Please wait...");

            CollectionReference userColl = globalclass.firebaseInstance().collection(Globalclass.userColl);
            Query query = userColl.whereEqualTo("id", globalclass.getStringData("id"));
            query.get().addOnCompleteListener(activity, new OnCompleteListener<QuerySnapshot>() {
                @Override
                public void onComplete(@NonNull Task<QuerySnapshot> task) {

                    hideprogress();

                    if (task.isSuccessful()) {
                        if (!task.getResult().isEmpty()) {
                            for (DocumentSnapshot documents : task.getResult()) {
                                userDetail model = documents.toObject(userDetail.class);
                                addressCount = model.getAddressCount();
                                globalclass.log(tag, "AddressCount: " + addressCount);
                            }

                            if (addressCount < globalclass.maxAddressCount()) {

                                if(globalclass.verificationNeeded(mobileNumber.getText().toString().trim())) {
                                    showVerifyMobileNumberDialogue();
                                }
                                else {
//                                    addNewAddress();
                                }
                            } else {
                                globalclass.showDialogue(activity, "Address limit alert",
                                        "You can have maximum " + globalclass.maxAddressCount() + " address at a time, please remove some address to add new!");

                            }
                        } else {
                            String error = "No user data found!";
                            globalclass.log(tag, "checkAddressCount: " + error);
                            globalclass.toast_long("Unable to add new address, please try after sometime!");
                            globalclass.sendErrorLog(tag,"checkAddressCount",error);
                        }
                    } else {
                        hideprogress();
                        String error = Log.getStackTraceString(task.getException());
                        globalclass.log(tag, "checkAddressCount: " + error);
                        globalclass.toast_long("Unable to add new address, please try after sometime!");
                        globalclass.sendErrorLog(tag,"checkAddressCount",error);
                    }
                }
            });
        } catch (Exception e) {

            hideprogress();
            String error = Log.getStackTraceString(e);
            globalclass.log(tag, "checkAddressCount: " + error);
            globalclass.toast_long("Unable to add new address, please try after sometime!");
            globalclass.sendErrorLog(tag,"checkAddressCount",error);
        }

    }

    void showVerifyMobileNumberDialogue() {
        new MaterialAlertDialogBuilder(activity, R.style.RoundShapeTheme)
                .setTitle("Mobile number Verification")
                .setMessage("We will send you otp on "
                        +mobileNumber.getText().toString().trim()+" to verify mobile number")
                .setCancelable(false)
                .setPositiveButton("Send otp", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        dialog.dismiss();
                        startActivityForResult(new Intent(activity, Verification.class)
                                        .putExtra("action",tag)
                                        .putExtra("mobilenumber",mobileNumber.getText().toString().trim()),
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

    void showConfirmDialogue() {

        new MaterialAlertDialogBuilder(activity, R.style.RoundShapeTheme)
                .setTitle("Sure")
                .setMessage("Are you sure you want to add new address ?")
                .setCancelable(false)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        dialog.dismiss();

//                    checkAddressCount();
                        if(mydatabase.getAddressList() != null && !mydatabase.getAddressList().isEmpty()) {
                            if (mydatabase.getAddressList().size() < globalclass.maxAddressCount()) {
                                if(globalclass.verificationNeeded(mobileNumber.getText().toString().trim())) {
                                    showVerifyMobileNumberDialogue();
                                }
                                else {
                                    addNewAddress();
                                }
                            }
                            else {
                                globalclass.showDialogue(activity, "Address limit alert",
                                        "You can have maximum " + globalclass.maxAddressCount() + " address at a time, please remove some address to add new!");
                            }
                        }
                        else {
                            if(globalclass.verificationNeeded(mobileNumber.getText().toString().trim())) {
                                showVerifyMobileNumberDialogue();
                            }
                            else {
                                addNewAddress();
                            }
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

    void addNewAddress() {

        String parameter = "";

        try {
            showprogress("Adding new address", "Please wait...");

            WriteBatch batch = globalclass.firebaseInstance().batch();

            CollectionReference addressColl = globalclass.firebaseInstance().collection(Globalclass.addressColl);
//            CollectionReference userColl = globalclass.firebaseInstance().collection(Globalclass.userColl);

            String addressId = addressColl.document().getId();
            addressDetail model = new addressDetail();
            model.setUserId(globalclass.getStringData("id"));
            model.setName(name.getText().toString().trim());
            model.setMobileNumber(mobileNumber.getText().toString().trim());
            model.setAddressId(addressId);
            model.setAddress(address.getText().toString().trim().replaceAll(System.lineSeparator(), " "));
            model.setNearBy(nearby.getText().toString().trim());
            model.setCityId(selectedCityId);
            model.setCity(selectedCityName.toLowerCase().trim());
            model.setStateId(selectedStateId);
            model.setState(selectedStateName.toLowerCase().trim());
            model.setPincode(selectedPincode);

            DocumentReference addressDocReference = addressColl.document(addressId);
            batch.set(addressDocReference, model);

//            DocumentReference userDocReference = userColl.document(model.getUserId());
//            Map<String, Object> map = new HashMap<>();
//            map.put("addressCount", FieldValue.increment(1));
//            batch.update(userDocReference, map);

            Gson gson = new Gson();
            if(model != null) {
                parameter = gson.toJson(model);
            }
            globalclass.log(tag,"parameter: "+parameter);

            String finalParameter = parameter;

            batch.commit().addOnCompleteListener(activity, new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    hideprogress();
                    if (task.isSuccessful()) {
                        mydatabase.addNewAddress(model);
                        if (getIntent().hasExtra("action") &&
                                getIntent().getStringExtra("action").equalsIgnoreCase("OrderSummary")) {

                            Intent intent = new Intent();
                            intent.putExtra("addressId", model.getAddressId());
                            setResult(RESULT_OK, intent);
                            finish();
                        } else {
                            newAddressAdded = true;
                            globalclass.toast_short("Address added successfully!");
                            onBackPressed();
                        }
                    } else {
                        String error = Log.getStackTraceString(task.getException());
                        globalclass.log(tag, "addNewAddress: " + error);
                        globalclass.toast_long("Unable to add new address, please try after sometime!");
                        globalclass.sendResponseErrorLog(tag,"addNewAddress: ",error, finalParameter);
                    }
                }
            });
        } catch (Exception e) {

            hideprogress();
            String error = Log.getStackTraceString(e);
            globalclass.log(tag, "addNewAddress: " + error);
            globalclass.toast_long("Unable to add new address, please try after sometime!");
            globalclass.sendResponseErrorLog(tag,"addNewAddress: ",error,parameter);
        }
    }

    void fillCityList() {
        cityList.clear();
        cityDetailList = mydatabase.getCityList();
        for (int i = 0; i < cityDetailList.size(); i++) {
            cityList.add(cityDetailList.get(i).getCityName());
        }
    }

    void fillStateList() {
        stateDetailList = mydatabase.getStateList();
    }

    void showCityListDialogue() {

        if (cityDetailList == null ||  cityDetailList.isEmpty()) {
            globalclass.snackit(activity, "No city found, please tap on refresh and try again!");
            return;
        }

        if (stateDetailList == null || stateDetailList.isEmpty()) {
            globalclass.snackit(activity, "No state found, please tap on refresh and try again!");
            return;
        }

        CharSequence[] charSequence = cityList.toArray(new CharSequence[cityList.size()]);
        MaterialAlertDialogBuilder materialAlertDialogBuilder =
                new MaterialAlertDialogBuilder(activity, R.style.RoundShapeTheme)
                        .setTitle("Select City")
                        .setSingleChoiceItems(charSequence, selectedCityPos, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int pos) {
                                selectedCityPos = pos;

                                selectedCityId = cityDetailList.get(selectedCityPos).getCityId();
                                selectedCityName = cityDetailList.get(selectedCityPos).getCityName();
                                selectedPincode = cityDetailList.get(selectedCityPos).getPincode();
                                selectedStateId = cityDetailList.get(selectedCityPos).getStateId();

                                city.setText(selectedCityName);
                                pincode.setText(selectedPincode);

                                selectState(selectedStateId);
                                dialogInterface.dismiss();
                            }
                        });
        materialAlertDialogBuilder.show();
    }

    void selectState(String stateId) {
        for (int i = 0; i < stateDetailList.size(); i++) {
            if (stateDetailList.get(i).getStateId().equalsIgnoreCase(stateId)) {
                selectedStateName = stateDetailList.get(i).getStateName();
                state.setText(selectedStateName);
                break;
            }
        }
    }

    void getCityList() {

        try {
            CollectionReference cityColl = globalclass.firebaseInstance().collection(Globalclass.cityColl);
            cityColl.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                @Override
                public void onComplete(@NonNull Task<QuerySnapshot> task) {

                    if(task.isSuccessful()) {
                        if(!task.getResult().isEmpty()) {
                            mydatabase.truncateTable(mydatabase.city);
                            for(DocumentSnapshot documents : task.getResult()) {
                                cityDetail model = documents.toObject(cityDetail.class);
                                mydatabase.addCity(model);
                            }

                            globalclass.snackit(activity,"Refresh successfully!");
                        }
                        else {
                            mydatabase.truncateTable(mydatabase.city);
                            globalclass.log(tag,"City is empty!");
                            globalclass.toast_long("Unable to get City list, please try after sometime!");
                        }
                    }
                    else {
                        String error = Log.getStackTraceString(task.getException());
                        globalclass.log(tag,"getCityList: "+error);
                        globalclass.toast_long("Unable to get City list, please try after sometime!");
                        globalclass.sendErrorLog(tag,"getCityList",error);
                    }
                }
            });
        }
        catch (Exception e) {

            String error = Log.getStackTraceString(e);
            globalclass.log(tag,"getCityList: "+error);
            globalclass.toast_long("Unable to get City list, please try after sometime!");
            globalclass.sendErrorLog(tag,"getCityList",error);
        }
    }

    void getStateList() {

        try {
            CollectionReference stateColl = globalclass.firebaseInstance().collection(Globalclass.stateColl);
            stateColl.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                @Override
                public void onComplete(@NonNull Task<QuerySnapshot> task) {

                    if(task.isSuccessful()) {
                        if(!task.getResult().isEmpty()) {
                            mydatabase.truncateTable(mydatabase.state);
                            for(DocumentSnapshot documents : task.getResult()) {
                                stateDetail model = documents.toObject(stateDetail.class);
                                mydatabase.addState(model);
                            }

                            globalclass.snackit(activity,"Refresh successfully!");
                        }
                        else {
                            mydatabase.truncateTable(mydatabase.state);
                            globalclass.log(tag,"State is empty!");
                            globalclass.toast_long("Unable to get State list, please try after sometime!");
                        }
                    }
                    else {
                        String error = Log.getStackTraceString(task.getException());
                        globalclass.log(tag,"getStateList: "+error);
                        globalclass.toast_long("Unable to get State list, please try after sometime!");
                        globalclass.sendErrorLog(tag,"getStateList",error);
                    }
                }
            });
        }
        catch (Exception e) {

            String error = Log.getStackTraceString(e);
            globalclass.log(tag,"getStateList: "+error);
            globalclass.toast_long("Unable to get State list, please try after sometime!");
            globalclass.sendErrorLog(tag,"getStateList",error);
        }
    }

    void clearAll() {

        selectedCityPos = -1;
        selectedCityId = "";
        selectedCityName = "";
        selectedStateId = "";
        selectedStateName = "";
        selectedPincode = "";

        name.setText("");
        mobileNumber.setText("");
        address.setText("");
        nearby.setText("");
        city.setText(getString(R.string.tap_here_to_select_city));
        state.setText(getString(R.string.state));
        pincode.setText(getString(R.string.pincode));
    }

    boolean validation() {

        String firstchar = "";
        if (mobileNumber.getText().length() > 0) {
            String getmobileno = mobileNumber.getText().toString();
            firstchar = getmobileno.substring(0, 1);
        }

        if (name.getText().length() < 3) {
            nametf.setError("Should contain atleast 3 characters");
            return false;
        } else if (!name.getText().toString().matches(globalclass.alphaNumericRegexOne())) {
            nametf.setError("Invalid name");
            return false;
        } else if (mobileNumber.getText().length() != 10) {
            mobileNumbertf.setError("Invalid mobile number");
            return false;
        } else if (!firstchar.equalsIgnoreCase("6") && !firstchar.equalsIgnoreCase("7") &&
                !firstchar.equalsIgnoreCase("8") && !firstchar.equalsIgnoreCase("9")) {
            mobileNumbertf.setError("Invalid mobile number");
            return false;
        } else if (address.getText().length() < 30) {
            addresstf.setError("Should contain atleast 30 characters");
            return false;
        } else if (!address.getText().toString().matches(globalclass.addressRegex())) {
            addresstf.setError("Invalid address");
            return false;
        }
        if (nearby.getText().length() < 10) {
            nearbytf.setError("Should contain atleast 10 characters");
            return false;
        } else if (!nearby.getText().toString().matches(globalclass.addressRegex())) {
            nearbytf.setError("Invalid Near by");
            return false;
        } else if (selectedCityPos == -1) {
            globalclass.snackit(activity, "Please select the City!");
            return false;
        }

        nametf.setErrorEnabled(false);
        mobileNumbertf.setErrorEnabled(false);
        addresstf.setErrorEnabled(false);
        nearbytf.setErrorEnabled(false);
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == VERIFY_MOBILENUMBER) {
            if (resultCode == Activity.RESULT_OK && data != null) {
                globalclass.toast_short("Verified successfully!");
                addNewAddress();
            }
        }
    }

    @Override
    public void onBackPressed() {
        if (newAddressAdded) {
            Intent intent = new Intent();
            setResult(RESULT_OK, intent);
            finish();
        } else {
            super.onBackPressed();
        }
    }
}