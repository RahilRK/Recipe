package com.hksofttronix.khansama.Admin.UpdateInventory;

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
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.hksofttronix.khansama.Models.inventoryDetail;
import com.hksofttronix.khansama.Models.unitDetail;
import com.hksofttronix.khansama.R;
import com.hksofttronix.khansama.Globalclass;
import com.hksofttronix.khansama.Mydatabase;

import java.util.ArrayList;
import java.util.Map;

public class UpdateInventory extends AppCompatActivity {

    String tag = this.getClass().getSimpleName();
    AppCompatActivity activity = UpdateInventory.this;

    Globalclass globalclass;
    Mydatabase mydatabase;

    SwipeRefreshLayout swipeRefresh;
    RelativeLayout selectUnitlayout;
    TextView tvUnit;
    TextView costPriceHeader,sellingPriceHeader,minimumQuantityPriceHeader;
    TextInputLayout inventoryNametf,costPricetf,sellingPricetf,minimumQuantitytf;
    EditText inventoryName,costPrice,sellingPrice,minimumQuantity;
    ImageView ivrefreshUnitList;
    MaterialButton updateInventorybt;

    ProgressDialog progressDialog;

    ArrayList<unitDetail> unitDetailList = new ArrayList<>();
    ArrayList<String> unitList = new ArrayList<>();
    int selectedUnitPos = -1;

    String inventoryId = "";
    inventoryDetail model;

    String highUnit = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update_inventory);

        init();
        binding();
        setToolbar();
        getData();
        onClick();
    }

    void init() {
        globalclass = Globalclass.getInstance(activity);
        mydatabase = Mydatabase.getInstance(activity);
    }

    void getData() {
        inventoryId = getIntent().getStringExtra("inventoryId");
        model = mydatabase.getParticularInventory(inventoryId);
        if(model != null) {
            setText();
            getUnitData();
        }
        else {
            getInventoryDetails();
        }
    }

    void binding() {
        swipeRefresh = findViewById(R.id.swipeRefresh);
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
        updateInventorybt = findViewById(R.id.updateInventorybt);
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

                getInventoryDetails();
            }
        });

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
                    enableUpdateButton();
                }
            }
        });

        costPrice.addTextChangedListener(new TextWatcher() {
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

        sellingPrice.addTextChangedListener(new TextWatcher() {
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

        minimumQuantity.addTextChangedListener(new TextWatcher() {
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

                if(unitDetailList.isEmpty()) {
                    globalclass.snackit(activity,"No unit details found!");
                    return;
                }

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
                        showConfirmDialogue();
                    }
                }
                return false;
            }
        });

        updateInventorybt.setOnClickListener(new View.OnClickListener() {
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

    void setText() {
        tvUnit.setText(model.getUnit());
        inventoryName.setText(model.getName());
        if(model.getUnit().equalsIgnoreCase("grams")) {
            highUnit = "kg";
        }
        else if(model.getUnit().equalsIgnoreCase("ml")) {
            highUnit = "liter";
        }
        else {
            highUnit = "piece";
        }

        costPrice.setText(globalclass.checknull(String.valueOf(model.getCostPrice())));
        sellingPrice.setText(globalclass.checknull(String.valueOf(model.getSellingPrice())));
        minimumQuantity.setText(String.valueOf(model.getMinimumQuantity()));

        changeHeaderText();
    }

    void setToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_backarrow);
        toolbar.setTitle(getString(R.string.inventory_detail));
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                onBackPressed();
            }
        });
    }

    void getInventoryDetails() {

        String parameter = "";

        try {
            swipeRefresh.setRefreshing(true);

            CollectionReference inventoryColl = globalclass.firebaseInstance().collection(Globalclass.inventoryColl);
            Query checkInventoryExist = inventoryColl.whereEqualTo("inventoryId", inventoryId);

            Gson gson = new Gson();
            if(model != null) {
                parameter = gson.toJson(model);
            }
            globalclass.log(tag,"parameter: "+parameter);

            String finalParameter = parameter;

            checkInventoryExist.get().addOnCompleteListener(activity, new OnCompleteListener<QuerySnapshot>() {
                @Override
                public void onComplete(@NonNull Task<QuerySnapshot> task) {

                    swipeRefresh.setRefreshing(false);
                    globalclass.snackit(activity,"Inventory detail refresh successfully!");

                    if(task.isSuccessful()) {
                        if(!task.getResult().isEmpty()) {
                            for(DocumentSnapshot documents : task.getResult()) {
                                model = documents.toObject(inventoryDetail.class);
                                globalclass.log(tag,"Inventory name: "+ model.getName());
                            }

                            getData();
                        }
                        else {
                            mydatabase.deleteData(mydatabase.inventory,"inventoryId",model.getInventoryId());
                            globalclass.snackit(activity,"Inventory not exist!");
                        }
                    }
                    else {
                        String error = Log.getStackTraceString(task.getException());
                        globalclass.log(tag,"checkInventoryExist: "+error);
                        globalclass.sendResponseErrorLog(tag,"checkInventoryExist: ",error, finalParameter);
                        globalclass.toast_long("Unable to get inventory detail, please try after sometime!");
                    }
                }
            });
        }
        catch (Exception e) {

            swipeRefresh.setRefreshing(false);
            String error = Log.getStackTraceString(e);
            globalclass.log(tag,"checkInventoryExist: "+error);
            globalclass.sendResponseErrorLog(tag,"checkInventoryExist: ",error, parameter);
            globalclass.toast_long("Unable to get inventory detail, please try after sometime!");
        }
    }

    void getUnitData() {
        unitDetailList.clear();
        unitList.clear();

        unitDetailList = mydatabase.getUnitList();
        if(!unitDetailList.isEmpty()) {

            fillUnitList();
            autoSelectUnit();
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
                            mydatabase.truncateTable(mydatabase.unit);
                            for(DocumentSnapshot documents : task.getResult()) {
                                unitDetail model = documents.toObject(unitDetail.class);
                                globalclass.log(tag,"Unit name: "+model.getName());
                                mydatabase.addUnit(model);
                            }

                            getUnitData();
                        }
                        else {
                            mydatabase.truncateTable(mydatabase.unit);
                            globalclass.snackit(activity,"No unit details found!");
                        }
                    }
                    else {
                        String error = Log.getStackTraceString(task.getException());
                        globalclass.log(tag,"getUnitList: "+error);
                        globalclass.sendErrorLog(tag,"getUnitList: ",error);
                        globalclass.toast_long("Unable to get Unit data, please try after sometime!");
                    }
                }
            });
        }
        catch (Exception e) {

            hideprogress();
            String error = Log.getStackTraceString(e);
            globalclass.log(tag,"getUnitList: "+error);
            globalclass.sendErrorLog(tag,"getUnitList: ",error);
            globalclass.toast_long("Unable to get Unit data, please try after sometime!");
        }
    }

    void fillUnitList() {
        for(int i=0;i<unitDetailList.size();i++) {
            unitList.add(unitDetailList.get(i).getName());
        }
    }

    void autoSelectUnit() {
        if(!unitList.isEmpty()) {
            for(int i=0;i<unitList.size();i++) {
                if(model.getUnit().equalsIgnoreCase(unitList.get(i))) {
                    selectedUnitPos = i;
                    return;
                }
            }
        }
    }

    void showUnitDialogue() {

        CharSequence[] charSequence = unitList.toArray(new CharSequence[unitList.size()]);
        MaterialAlertDialogBuilder materialAlertDialogBuilder =
                new MaterialAlertDialogBuilder(activity,R.style.RoundShapeTheme)
                .setTitle("Select Unit")
                .setCancelable(false)
                .setSingleChoiceItems(charSequence, selectedUnitPos, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int pos) {
                        selectedUnitPos = pos;
                        enableUpdateButton();
                        tvUnit.setText(unitList.get(selectedUnitPos));
                        changeHeaderText();
                        dialogInterface.dismiss();
                    }
                });
        materialAlertDialogBuilder.show();
    }

    void changeHeaderText() {

        if(tvUnit.getText().toString().trim().equalsIgnoreCase("grams")) {
            highUnit = "kg";
        }
        else if(tvUnit.getText().toString().trim().equalsIgnoreCase("ml")) {
            highUnit = "liter";
        }
        else {
            highUnit = "piece";
        }

        costPriceHeader.setText("Purchase price of "+"1 "+highUnit+" "+globalclass.firstLetterCapital(model.getName()));
        sellingPriceHeader.setText("Selling price of "+"1 "+highUnit+" "+globalclass.firstLetterCapital(model.getName()));
        minimumQuantityPriceHeader.setText("Enter Minimum "+tvUnit.getText().toString().trim()+" required");
        minimumQuantitytf.setHint("Minimum "+tvUnit.getText().toString().trim());
    }

    void showConfirmDialogue() {

        new MaterialAlertDialogBuilder(activity, R.style.RoundShapeTheme)
                .setTitle("Sure")
                .setMessage("Are you sure you want to change inventory detail ?")
                .setCancelable(false)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        dialog.dismiss();
                        updateInventory();
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

    void updateInventory() {

        String parameter = "";

        try {
            showprogress("Changing Inventory detail","Please wait...");

            CollectionReference inventoryColl = globalclass.firebaseInstance().collection(Globalclass.inventoryColl);
            String inventoryId = model.getInventoryId();

            model.setName(inventoryName.getText().toString().trim().toLowerCase());
            model.setUnitId(unitDetailList.get(selectedUnitPos).getunitId());
            model.setUnit(unitDetailList.get(selectedUnitPos).getName());
            model.setQuantity(model.getQuantity());
            model.setMinimumQuantity(Integer.parseInt(minimumQuantity.getText().toString().trim()));
            model.setAdminId(globalclass.getStringData("adminId"));
            model.setAdminName(globalclass.getStringData("adminName"));
            model.setCostPrice(Double.parseDouble(costPrice.getText().toString()));
//            model.setSellingPrice(Double.parseDouble(sellingPrice.getText().toString()));

            Gson gson = new GsonBuilder().create();
            String json = gson.toJson(model);
            Map<String,Object> map = new Gson().fromJson(json, Map.class);
            map.put("lastChangeTime", FieldValue.serverTimestamp());

            if(model != null) {
                parameter = gson.toJson(model);
            }
            globalclass.log(tag,"parameter: "+parameter);

            String finalParameter = parameter;

            DocumentReference inventoryReference = inventoryColl.document(inventoryId);
            inventoryReference.update(map)
                    .addOnCompleteListener(activity, new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {

                            hideprogress();
                            if(task.isSuccessful()) {

                                globalclass.toast_short("Inventory detail changed successfully!");
                                mydatabase.addInventory(model);

                                Intent intent = new Intent();
                                intent.putExtra("position",getIntent().getStringExtra("position"));
                                intent.putExtra("inventoryId",inventoryId);
                                setResult(RESULT_OK, intent);
                                finish();
                            }
                            else {
                                String error = Log.getStackTraceString(task.getException());
                                globalclass.log(tag,"updateInventory: "+error);
                                globalclass.sendResponseErrorLog(tag,"updateInventory: ",error, finalParameter);
                                globalclass.toast_long("Unable to change inventory detail, please try after sometime!");
                            }
                        }
                    });
        }
        catch (Exception e) {

            hideprogress();
            String error = Log.getStackTraceString(e);
            globalclass.log(tag,"updateInventory: "+error);
            globalclass.sendResponseErrorLog(tag,"updateInventory: ",error, parameter);
            globalclass.toast_long("Unable to change inventory detail, please try after sometime!");
        }
    }

    void enableUpdateButton() {
        inventoryDetail inventoryDetailModel = new inventoryDetail();
        inventoryDetailModel.setName(inventoryName.getText().toString().trim().toLowerCase());
        inventoryDetailModel.setUnit(unitDetailList.get(selectedUnitPos).getName());
        inventoryDetailModel.setCostPrice(Double.parseDouble(costPrice.getText().toString().trim()));
        inventoryDetailModel.setSellingPrice(Double.parseDouble(sellingPrice.getText().toString().trim()));
        inventoryDetailModel.setMinimumQuantity(Integer.parseInt(minimumQuantity.getText().toString().trim()));

        if(equals2(inventoryDetailModel)) {
            updateInventorybt.setEnabled(true);
        }
        else {
            updateInventorybt.setEnabled(false);
        }
    }

    boolean equals2(Object object2) {  // equals2 method
        if(model.equals(object2)) { // if equals() method returns true
            return false; // return true
        }
        else return true; // if equals() method returns false, also return false
    }

    boolean validation () {

        if(selectedUnitPos == -1) {
            globalclass.snackit(activity,"Select Inventory unit!");
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
        else if(costPrice.getText().length() == 0) {
            costPricetf.setError("Invalid Cost price!");
            return false;
        }
        else if(sellingPrice.getText().length() == 0) {
            sellingPricetf.setError("Invalid Selling price!");
            return false;
        }
//        else if(Double.parseDouble(sellingPrice.getText().toString()) <= Double.parseDouble(costPrice.getText().toString())) {
//            sellingPricetf.setError("Selling price must be more then Cost price!");
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
}