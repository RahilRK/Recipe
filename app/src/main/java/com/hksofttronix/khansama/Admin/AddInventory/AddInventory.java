package com.hksofttronix.khansama.Admin.AddInventory;

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
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
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
import com.google.gson.Gson;
import com.hksofttronix.khansama.Models.inventoryDetail;
import com.hksofttronix.khansama.Models.unitDetail;
import com.hksofttronix.khansama.R;
import com.hksofttronix.khansama.Globalclass;
import com.hksofttronix.khansama.Mydatabase;

import java.util.ArrayList;

public class AddInventory extends AppCompatActivity {

    String tag = this.getClass().getSimpleName();
    AppCompatActivity activity = AddInventory.this;

    Globalclass globalclass;
    Mydatabase mydatabase;

    RelativeLayout selectUnitlayout;
    TextView tvUnit;
    TextView costPriceHeader,sellingPriceHeader,minimumQuantityPriceHeader;
    TextInputLayout inventoryNametf,costPricetf,sellingPricetf,minimumQuantitytf;
    EditText inventoryName,costPrice,sellingPrice,minimumQuantity;
    ImageView ivrefreshUnitList;
    MaterialButton addInventorybt;

    boolean inventoryAdded = false;
    ProgressDialog progressDialog;

    ArrayList<unitDetail> unitDetailList = new ArrayList<>();
    ArrayList<String> unitList = new ArrayList<>();
    int selectedUnitPos = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_inverntory);

        init();
        binding();
        onClick();
        setToolbar();
        getUnitData();
    }

    void init() {
        globalclass = Globalclass.getInstance(activity);
        mydatabase = Mydatabase.getInstance(activity);
    }

    void binding() {
        selectUnitlayout = findViewById(R.id.selectUnitlayout);
        tvUnit = findViewById(R.id.tvUnit);
        costPriceHeader = findViewById(R.id.costPriceHeader);
        sellingPriceHeader = findViewById(R.id.sellingPriceHeader);
        inventoryNametf = findViewById(R.id.inventoryNametf);
        costPricetf = findViewById(R.id.costPricetf);
        sellingPricetf = findViewById(R.id.sellingPricetf);
        minimumQuantitytf = findViewById(R.id.minimumQuantitytf);
        inventoryName = findViewById(R.id.inventoryName);
        costPrice = findViewById(R.id.costPrice);
        sellingPrice = findViewById(R.id.sellingPrice);
        minimumQuantityPriceHeader = findViewById(R.id.minimumQuantityPriceHeader);
        minimumQuantity = findViewById(R.id.minimumQuantity);
        ivrefreshUnitList = findViewById(R.id.ivrefreshUnitList);
        addInventorybt = findViewById(R.id.addInventorybt);
    }

    void onClick() {

        inventoryName.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {

                if(editable.length() > 0) {
                    if(mydatabase.checkInventoryExist(inventoryName.getText().toString().trim()) > 0) {
                        inventoryNametf.setError(inventoryName.getText().toString().trim()+" already exist");
                    }
                    else {
                        inventoryNametf.setErrorEnabled(false);
                    }
                }
            }
        });

        ivrefreshUnitList.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (!globalclass.isInternetPresent()) {
                    globalclass.toast_long(getResources().getString(R.string.noInternetConnection));
                    return;
                }

                getUnitList();
            }
        });

        selectUnitlayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                showUnitDialogue();
            }
        });

        minimumQuantity.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {

                if(actionId == EditorInfo.IME_ACTION_DONE) {

                    globalclass.hideKeyboard(activity);

                    if (!globalclass.isInternetPresent()) {
                        globalclass.toast_long(getResources().getString(R.string.noInternetConnection));
                        return false;
                    }

                    if(validation()) {
//                        globalclass.toast_short("Done");
                        showConfirmDialogue();
                    }
                }
                return false;
            }
        });

        addInventorybt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                globalclass.hideKeyboard(activity);

                if (!globalclass.isInternetPresent()) {
                    globalclass.toast_long(getResources().getString(R.string.noInternetConnection));
                    return;
                }

                if(validation()) {
//                    globalclass.toast_short("Done");
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
        toolbar.setTitle(getString(R.string.add_inventory));
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                onBackPressed();
            }
        });
    }

    void getUnitData() {
        unitDetailList.clear();
        unitList.clear();

        unitDetailList = mydatabase.getUnitList();
        if(!unitDetailList.isEmpty()) {

            fillUnitList();
        }
        else {
            getUnitList();
        }
    }

    void getUnitList() {
        try {
            unitDetailList.clear();
            unitList.clear();

            showprogress("Refreshing unit list","Please wait...");

            CollectionReference collectionReference = globalclass.firebaseInstance().collection(Globalclass.unitColl);
            collectionReference.get().addOnCompleteListener(activity, new OnCompleteListener<QuerySnapshot>() {
                @Override
                public void onComplete(@NonNull Task<QuerySnapshot> task) {

                    hideprogress();
                    globalclass.snackit(activity,"Refresh successfully!");

                    if(task.isSuccessful()) {
                        if(!task.getResult().isEmpty()) {
                            for(DocumentSnapshot documents : task.getResult()) {
                                unitDetail model = documents.toObject(unitDetail.class);
                                globalclass.log(tag,"Unit name: "+model.getName());
                                mydatabase.addUnit(model);
                            }

                            getUnitData();
                        }
                        else {
                            globalclass.snackit(activity,"No unit details found!");
                        }
                    }
                    else {
                        String error = Log.getStackTraceString(task.getException());
                        globalclass.log(tag,"getUnitList: "+error);
                        globalclass.toast_long("Unable to get Unit data, please try after sometime!");
                        globalclass.sendErrorLog(tag,"getUnitList: ",error);
                    }
                }
            });
        }
        catch (Exception e) {

            hideprogress();
            String error = Log.getStackTraceString(e);
            globalclass.log(tag,"getUnitList: "+error);
            globalclass.toast_long("Unable to get Unit data, please try after sometime!");
            globalclass.sendErrorLog(tag,"getUnitList: ",error);
        }
    }

    void fillUnitList() {

        if(!unitDetailList.isEmpty()) {
            for(int i=0;i<unitDetailList.size();i++) {
                unitList.add(unitDetailList.get(i).getName());
            }
        }
    }

    void showUnitDialogue() {

        if(unitDetailList.isEmpty()) {
            globalclass.snackit(activity,"No unit details found, please refresh and try again!");
            return;
        }

        CharSequence[] charSequence = unitList.toArray(new CharSequence[unitList.size()]);
        MaterialAlertDialogBuilder materialAlertDialogBuilder =
                new MaterialAlertDialogBuilder(activity,R.style.RoundShapeTheme)
                .setTitle("Select Unit")
                .setCancelable(false)
                .setSingleChoiceItems(charSequence, selectedUnitPos, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int pos) {
                        selectedUnitPos = pos;
                        tvUnit.setText(unitList.get(selectedUnitPos));
                        changeHeaderText();
                        dialogInterface.dismiss();
                    }
                });
        materialAlertDialogBuilder.show();
    }

    void changeHeaderText() {

        String highUnit = "";
        if(unitList.get(selectedUnitPos).equalsIgnoreCase("grams")) {
            highUnit = "kg";
        }
        else if(unitList.get(selectedUnitPos).equalsIgnoreCase("ml")) {
            highUnit = "liter";
        }
        else {
            highUnit = "piece";
        }

        costPriceHeader.setText("Enter Purchase Price of 1 "+highUnit);
        sellingPriceHeader.setText("Enter Selling Price of 1 "+highUnit);
        minimumQuantityPriceHeader.setText("Enter Minimum "+unitList.get(selectedUnitPos)+" required");
        minimumQuantitytf.setHint("Minimum "+unitList.get(selectedUnitPos));
    }

    void showConfirmDialogue() {

        new MaterialAlertDialogBuilder(activity, R.style.RoundShapeTheme)
                .setTitle("Sure")
                .setMessage("Are you sure you want to add new inventory ?")
                .setCancelable(false)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        checkInventoryExist();
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

    void checkInventoryExist() {

        try {
            showprogress("Adding Inventory","Please wait...");

            CollectionReference collectionReference = globalclass.firebaseInstance().collection(Globalclass.inventoryColl);
            Query checkInventoryExist = collectionReference.whereEqualTo("name",inventoryName.getText().toString().trim().toLowerCase());
            checkInventoryExist.get().addOnCompleteListener(activity, new OnCompleteListener<QuerySnapshot>() {
                @Override
                public void onComplete(@NonNull Task<QuerySnapshot> task) {

                    if(task.isSuccessful()) {
                        if(!task.getResult().isEmpty()) {
                            for(DocumentSnapshot documents : task.getResult()) {
                                inventoryDetail model = documents.toObject(inventoryDetail.class);
                                globalclass.log(tag,"Inventory name: "+model.getName());
                            }

                            hideprogress();
                            globalclass.snackit(activity,"Inventory already exist!");
                        }
                        else {
                            globalclass.log(tag,"Inventory not exist!");
                            addInventory();
                        }
                    }
                    else {
                        hideprogress();
                        String error = Log.getStackTraceString(task.getException());
                        globalclass.log(tag,"checkInventoryExist: "+error);
                        globalclass.toast_long("Unable to add inventory, please try after sometime!");
                        globalclass.sendErrorLog(tag,"checkInventoryExist: ",error);
                    }
                }
            });
        }
        catch (Exception e) {

            hideprogress();
            String error = Log.getStackTraceString(e);
            globalclass.log(tag,"checkInventoryExist: "+error);
            globalclass.toast_long("Unable to add inventory, please try after sometime!");
            globalclass.sendErrorLog(tag,"checkInventoryExist: ",error);
        }
    }

    void addInventory() {

        String parameter = "";

        try {
            double getCostPrice = 0;
            if(costPrice.getText().length() > 0) {
                getCostPrice = Double.parseDouble(costPrice.getText().toString());
            }

//        double getSellingPrice = 0;
//        if(sellingPrice.getText().length() > 0) {
//            getSellingPrice = Double.parseDouble(sellingPrice.getText().toString());
//        }

            CollectionReference inventoryColl = globalclass.firebaseInstance().collection(Globalclass.inventoryColl);
            String inventoryId = inventoryColl.document().getId();

            inventoryDetail model = new inventoryDetail();
            model.setInventoryId(inventoryId);
            model.setName(inventoryName.getText().toString().trim().toLowerCase());
            model.setUnitId(unitDetailList.get(selectedUnitPos).getunitId());
            model.setUnit(unitDetailList.get(selectedUnitPos).getName());
            model.setQuantity(0);
            model.setMinimumQuantity(Integer.parseInt(minimumQuantity.getText().toString().trim()));
            model.setAdminId(globalclass.getStringData("adminId"));
            model.setAdminName(globalclass.getStringData("adminName"));
            model.setCostPrice(getCostPrice);
//        model.setSellingPrice(getSellingPrice);

            Gson gson = new Gson();
            if(model != null) {
                parameter = gson.toJson(model);
            }
            globalclass.log(tag,"parameter: "+parameter);

            String finalParameter = parameter;

            DocumentReference inventoryReference = inventoryColl.document(inventoryId);
            inventoryReference.set(model)
                    .addOnCompleteListener(activity, new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            hideprogress();
                            if(task.isSuccessful()) {
                                inventoryAdded = true;
                                globalclass.snackit(activity,"Added successfully!");
                                mydatabase.addInventory(model);
                                clearAll();
                            }
                            else {
                                String error = Log.getStackTraceString(task.getException());
                                globalclass.log(tag,"addInventory: "+error);
                                globalclass.toast_long("Unable to add inventory, please try after sometime!");
                                globalclass.sendResponseErrorLog(tag,"addInventory: ",error, finalParameter);
                            }
                        }
                    });
        }
        catch (Exception e) {

            hideprogress();
            String error = Log.getStackTraceString(e);
            globalclass.log(tag, "addInventory: " + error);
            globalclass.toast_long("Unable to add inventory, please try after sometime!");
            globalclass.sendResponseErrorLog(tag,"addInventory",error,parameter);
        }
    }

    void clearAll() {
        selectedUnitPos = 0;
        tvUnit.setText(getString(R.string.tap_here_to_select_unit));
        inventoryName.setText("");
        costPrice.setText("");
        sellingPrice.setText("");
        minimumQuantity.setText("");
    }

    boolean validation () {

        if(selectedUnitPos < 0) {
            globalclass.snackit(activity,"Please select inventory unit");
            return false;
        }
        else if(inventoryName.getText().length() < 3) {
            inventoryNametf.setError("Should contain atleast 3 characters!");
            return false;
        }
        else if(!inventoryName.getText().toString().trim().matches(globalclass.alphaNumericRegexOne())) {
            inventoryNametf.setError("Invalid Inventory name!");
            return false;
        }
        else if(mydatabase.checkInventoryExist(inventoryName.getText().toString().trim()) > 0) {
            inventoryNametf.setError(inventoryName.getText().toString().trim()+" already exist");
            return false;
        }
        else if(costPrice.getText().length() > 0 && Double.parseDouble(costPrice.getText().toString()) <= 0) {
            costPricetf.setError("Invalid Cost price!");
            return false;
        }
//        else if(sellingPrice.getText().length() > 0 && Double.parseDouble(sellingPrice.getText().toString()) <= 0) {
//            sellingPricetf.setError("Invalid Selling price!");
//            return false;
//        }
        else if(minimumQuantity.getText().length() == 0 || Integer.parseInt(minimumQuantity.getText().toString()) == 0) {
            minimumQuantitytf.setError("Invalid Minimum quantity!");
            return false;
        }

        inventoryNametf.setErrorEnabled(false);
        costPricetf.setErrorEnabled(false);
//        sellingPricetf.setErrorEnabled(false);
        minimumQuantitytf.setErrorEnabled(false);
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
        if (progressDialog != null && progressDialog.isShowing()) {
            progressDialog.dismiss();
        }
    }

    @Override
    public void onBackPressed() {
        if(inventoryAdded) {
            Intent intent = new Intent();
            setResult(RESULT_OK, intent);
            finish();
        }
        else {
            super.onBackPressed();
        }
    }
}