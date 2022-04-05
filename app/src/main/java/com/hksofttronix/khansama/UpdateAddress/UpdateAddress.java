package com.hksofttronix.khansama.UpdateAddress;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

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
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.WriteBatch;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.hksofttronix.khansama.Models.addressDetail;
import com.hksofttronix.khansama.Models.cityDetail;
import com.hksofttronix.khansama.Models.stateDetail;
import com.hksofttronix.khansama.R;
import com.hksofttronix.khansama.Globalclass;
import com.hksofttronix.khansama.Mydatabase;
import com.hksofttronix.khansama.Verification;

import java.util.ArrayList;
import java.util.Map;

public class UpdateAddress extends AppCompatActivity {

    String tag = this.getClass().getSimpleName();
    AppCompatActivity activity = UpdateAddress.this;

    Globalclass globalclass;
    Mydatabase mydatabase;

    SwipeRefreshLayout swipeRefresh;
    TextInputLayout nametf, mobileNumbertf, addresstf, nearbytf;
    EditText name, mobileNumber, address, nearby;
    RelativeLayout selectCity;
    TextView city,state,pincode;
    ImageView ivrefreshCityState;
    MaterialButton updateAddressbt;

    String addressId = "";
    addressDetail model;

    ArrayList<cityDetail> cityDetailList = new ArrayList<>();
    ArrayList<String> cityList = new ArrayList<>();
    int selectedCityPos = -1;

    String selectedCityId;
    String selectedCityName;
    String selectedStateId;
    String selectedStateName;
    String selectedPincode;

    ArrayList<stateDetail> stateDetailList = new ArrayList<>();

    int VERIFY_MOBILENUMBER = 111;

    ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update_address);

        setToolbar();
        init();
        binding();
        fillCityList();
        fillStateList();
        setText();
        onClick();
    }

    void init() {
        globalclass = Globalclass.getInstance(activity);
        mydatabase = Mydatabase.getInstance(activity);
    }

    void binding() {
        swipeRefresh = findViewById(R.id.swipeRefresh);
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
        updateAddressbt = findViewById(R.id.updateAddressbt);
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

                getAddressDetail();
            }
        });

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

        mobileNumber.addTextChangedListener(new TextWatcher() {
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

        address.addTextChangedListener(new TextWatcher() {
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

        nearby.addTextChangedListener(new TextWatcher() {
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

        selectCity.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                showCityListDialogue();
            }
        });

        updateAddressbt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (!globalclass.isInternetPresent()) {
                    globalclass.toast_long(getResources().getString(R.string.noInternetConnection));
                    return;
                }

                globalclass.hideKeyboard(activity);

                if(validation()) {
                    showConfirmDialogue();
                }
            }
        });
    }

    void setText() {

        addressId = getIntent().getStringExtra("addressId");
        model = mydatabase.getParticularAddressDetail(addressId);
        if(model != null) {
            name.setText(model.getName());
            name.setSelection(model.getName().length());
            mobileNumber.setText(model.getMobileNumber());
            address.setText(model.getAddress());
            nearby.setText(model.getNearBy());
            autoSelectCity(model);

            enableUpdateButton();
        }
        else {
            globalclass.snackit(activity,"No address detail found!");
        }
    }

    void setToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_backarrow);
        toolbar.setTitle(R.string.change_detail);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                onBackPressed();
            }
        });
    }

    void getAddressDetail() {

        String parameter = "";

        try {
            swipeRefresh.setRefreshing(true);

            CollectionReference addressColl = globalclass.firebaseInstance().collection(Globalclass.addressColl);
            Query checkAddressExist = addressColl.whereEqualTo("addressId", model.getAddressId());

            Gson gson = new Gson();
            if(model != null) {
                parameter = gson.toJson(model);
            }
            globalclass.log(tag,"parameter: "+parameter);

            String finalParameter = parameter;

            checkAddressExist.get().addOnCompleteListener(activity, new OnCompleteListener<QuerySnapshot>() {
                @Override
                public void onComplete(@NonNull Task<QuerySnapshot> task) {

                    swipeRefresh.setRefreshing(false);
                    globalclass.snackit(activity,"Refresh successfully!");

                    if(task.isSuccessful()) {
                        if(!task.getResult().isEmpty()) {
                            for(DocumentSnapshot documents : task.getResult()) {
                                model = documents.toObject(addressDetail.class);
                                globalclass.log(tag,"Address Person name: "+ model.getName());
                            }

                            setText();
                        }
                        else {
                            mydatabase.deleteData(mydatabase.address,"addressId",model.getAddressId());
                            globalclass.snackit(activity,"Address not exist!");
                        }
                    }
                    else {
                        String error = Log.getStackTraceString(task.getException());
                        globalclass.log(tag,"getAddressDetail: "+error);
                        globalclass.toast_long("Unable to get address detail, please try after sometime!");
                        globalclass.sendResponseErrorLog(tag,"checkAddressExist: ",error, finalParameter);
                    }
                }
            });
        }
        catch (Exception e) {

            swipeRefresh.setRefreshing(false);
            String error = Log.getStackTraceString(e);
            globalclass.log(tag,"getAddressDetail: "+error);
            globalclass.toast_long("Unable to get address detail, please try after sometime!");
            globalclass.sendResponseErrorLog(tag,"checkAddressExist: ",error, parameter);
        }
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
                        checkAddressExist();
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

    void checkAddressExist() {

        String parameter = "";

        try {
            showprogress("Hold on","Please wait...");

            CollectionReference addressColl = globalclass.firebaseInstance().collection(Globalclass.addressColl);
            Query query = addressColl.whereEqualTo("addressId",model.getAddressId());

            Gson gson = new Gson();
            if(model != null) {
                parameter = gson.toJson(model);
            }
            globalclass.log(tag,"parameter: "+parameter);

            String finalParameter = parameter;

            query.get().addOnCompleteListener(activity, new OnCompleteListener<QuerySnapshot>() {
                @Override
                public void onComplete(@NonNull Task<QuerySnapshot> task) {

                    hideprogress();

                    if(task.isSuccessful()) {
                        if(!task.getResult().isEmpty()) {

                            if(model.getMobileNumber().equalsIgnoreCase(mobileNumber.getText().toString().trim())) {
                                updateAddress();
                            }
                            else {
                                if(globalclass.verificationNeeded(mobileNumber.getText().toString().trim())) {
                                    showVerifyMobileNumberDialogue();
                                }
                                else {
                                    updateAddress();
                                }
                            }
                        }
                        else {
                            mydatabase.deleteData(mydatabase.address,"addressId",model.getAddressId());
                            String error = "No address found!";
                            globalclass.log(tag,"checkAddressExist: "+error);
                            globalclass.toast_long("Unable to change address detail, please try after sometime!");
                            globalclass.sendResponseErrorLog(tag,"checkAddressExist: ",error, finalParameter);
                        }
                    }
                    else {
                        String error = Log.getStackTraceString(task.getException());
                        globalclass.log(tag,"checkAddressExist: "+error);
                        globalclass.toast_long("Unable to change address detail, please try after sometime!");
                        globalclass.sendResponseErrorLog(tag,"checkAddressExist: ",error, finalParameter);
                    }
                }
            });
        }
        catch (Exception e) {

            hideprogress();
            String error = Log.getStackTraceString(e);
            globalclass.log(tag,"checkAddressExist: "+error);
            globalclass.toast_long("Unable to change address detail, please try after sometime!");
            globalclass.sendResponseErrorLog(tag,"checkAddressExist: ",error, parameter);
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

    void updateAddress() {

        String parameter = "";

        try {
            showprogress("Changing address details","Please wait...");

            WriteBatch batch = globalclass.firebaseInstance().batch();

            CollectionReference addressColl = globalclass.firebaseInstance().collection(Globalclass.addressColl);

            String addressId = model.getAddressId();
            addressDetail model = new addressDetail();
            model.setUserId(globalclass.getStringData("id"));
            model.setName(name.getText().toString().trim().toLowerCase());
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

            Gson gson = new GsonBuilder().create();
            String json = gson.toJson(model);
            Map<String,Object> map = new Gson().fromJson(json, Map.class);

            batch.update(addressDocReference,map);

            if(model != null) {
                parameter = gson.toJson(model);
            }
            globalclass.log(tag,"parameter: "+parameter);

            String finalParameter = parameter;

            batch.commit().addOnCompleteListener(activity, new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    hideprogress();
                    if(task.isSuccessful()) {
                        globalclass.toast_short("Address details changed successfully!");
                        mydatabase.addNewAddress(model);

                        Intent intent = new Intent();
                        intent.putExtra("position",getIntent().getStringExtra("position"));
                        intent.putExtra("addressId",model.getAddressId());
                        setResult(RESULT_OK, intent);
                        finish();
                    }
                    else {
                        String error = Log.getStackTraceString(task.getException());
                        globalclass.log(tag,"updateAddress: "+error);
                        globalclass.toast_long("Unable to change address detail, please try after sometime!");
                        globalclass.sendResponseErrorLog(tag,"updateAddress: ",error, finalParameter);
                    }
                }
            });
        }
        catch (Exception e) {

            hideprogress();
            String error = Log.getStackTraceString(e);
            globalclass.log(tag,"updateAddress: "+error);
            globalclass.toast_long("Unable to change address detail, please try after sometime!");
            globalclass.sendResponseErrorLog(tag,"updateAddress: ",error, parameter);
        }
    }

    void fillCityList() {
        cityList.clear();
        cityDetailList = mydatabase.getCityList();
        for(int i=0;i<cityDetailList.size();i++) {
            cityList.add(cityDetailList.get(i).getCityName());
        }
    }

    void fillStateList() {
        stateDetailList = mydatabase.getStateList();
    }

    void autoSelectCity(addressDetail model) {
        for(int i=0;i<cityDetailList.size();i++) {
            if(cityDetailList.get(i).getCityId().equalsIgnoreCase(model.getCityId())) {

                selectedCityPos = i;

                selectedCityId = cityDetailList.get(selectedCityPos).getCityId();
                selectedCityName = cityDetailList.get(selectedCityPos).getCityName();
                selectedPincode = cityDetailList.get(selectedCityPos).getPincode();
                selectedStateId = cityDetailList.get(selectedCityPos).getStateId();

                city.setText(selectedCityName);
                pincode.setText(selectedPincode);

                selectState(selectedStateId);
                break;
            }
        }
    }

    void showCityListDialogue() {

        if(cityDetailList.isEmpty()) {
            globalclass.snackit(activity,"No city found, please refresh and try again!");
            return;
        }

        if(stateDetailList.isEmpty()) {
            globalclass.snackit(activity,"No state found, please refresh and try again!");
            return;
        }

        CharSequence[] charSequence = cityList.toArray(new CharSequence[cityList.size()]);
        MaterialAlertDialogBuilder materialAlertDialogBuilder =
                new MaterialAlertDialogBuilder(activity,R.style.RoundShapeTheme)
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

                                enableUpdateButton();
                                dialogInterface.dismiss();
                            }
                        });
        materialAlertDialogBuilder.show();
    }

    void selectState(String stateId) {
        for(int i=0;i<stateDetailList.size();i++) {
            if(stateDetailList.get(i).getStateId().equalsIgnoreCase(stateId)) {
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
                        globalclass.sendErrorLog(tag, "getCityList", error);
                    }
                }
            });
        }
        catch (Exception e) {

            String error = Log.getStackTraceString(e);
            globalclass.log(tag,"getCityList: "+error);
            globalclass.toast_long("Unable to get City list, please try after sometime!");
            globalclass.sendErrorLog(tag, "getCityList", error);
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
                            autoSelectCity(model);
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
                        globalclass.sendErrorLog(tag, "getStateList", error);
                    }
                }
            });
        }
        catch (Exception e) {

            String error = Log.getStackTraceString(e);
            globalclass.log(tag,"getStateList: "+error);
            globalclass.toast_long("Unable to get State list, please try after sometime!");
            globalclass.sendErrorLog(tag, "getStateList", error);
        }
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
        } else if(address.getText().length() < 30) {
            addresstf.setError("Should contain atleast 30 characters");
            return false;
        }
        else if(!address.getText().toString().matches(globalclass.addressRegex())) {
            addresstf.setError("Invalid address");
            return false;
        }
        if(nearby.getText().length() < 10) {
            nearbytf.setError("Should contain atleast 10 characters");
            return false;
        }
        else if(!nearby.getText().toString().matches(globalclass.addressRegex())) {
            nearbytf.setError("Invalid Near by");
            return false;
        }
        else if(selectedCityPos == -1) {
            globalclass.snackit(activity,"Please select the City!");
            return false;
        }

        addresstf.setErrorEnabled(false);
        nearbytf.setErrorEnabled(false);
        return true;
    }

    void enableUpdateButton() {
        addressDetail addressDetailModel = new addressDetail();
        addressDetailModel.setUserId(globalclass.getStringData("id"));
        addressDetailModel.setName((name.getText().toString().trim()));
        addressDetailModel.setMobileNumber((mobileNumber.getText().toString().trim()));
        addressDetailModel.setAddressId(addressId);
        addressDetailModel.setAddress(address.getText().toString().trim().replaceAll(System.lineSeparator(), " "));
        addressDetailModel.setNearBy(nearby.getText().toString().trim());
        addressDetailModel.setCityId(selectedCityId);
        addressDetailModel.setCity(selectedCityName.toLowerCase().trim());
        addressDetailModel.setStateId(selectedStateId);
        addressDetailModel.setState(selectedStateName.toLowerCase().trim());
        addressDetailModel.setPincode(selectedPincode);

        if(equals2(addressDetailModel)) {
            updateAddressbt.setEnabled(true);
        }
        else {
            updateAddressbt.setEnabled(false);
        }
    }

    boolean equals2(Object object2) {  // equals2 method
        if(model.equals(object2)) { // if equals() method returns true
            return false; // return true
        }
        else return true; // if equals() method returns false, also return false
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
        if (requestCode == VERIFY_MOBILENUMBER) {
            if (resultCode == Activity.RESULT_OK && data != null) {
                globalclass.toast_short("Verified successfully!");
                updateAddress();
            }
        }
    }

}