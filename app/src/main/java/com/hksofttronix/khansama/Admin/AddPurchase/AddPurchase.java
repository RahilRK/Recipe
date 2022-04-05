package com.hksofttronix.khansama.Admin.AddPurchase;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.WriteBatch;
import com.hksofttronix.khansama.Admin.SelectInventory.SelectInventory;
import com.hksofttronix.khansama.Admin.UpdateInventory.UpdateInventory;
import com.hksofttronix.khansama.Models.inventoryDetail;
import com.hksofttronix.khansama.Models.purchaseDetail;
import com.hksofttronix.khansama.Models.stockDetail;
import com.hksofttronix.khansama.Models.vendorDetail;
import com.hksofttronix.khansama.R;
import com.hksofttronix.khansama.Globalclass;
import com.hksofttronix.khansama.Mydatabase;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class AddPurchase extends AppCompatActivity {

    String tag = this.getClass().getSimpleName();
    AppCompatActivity activity = AddPurchase.this;

    Globalclass globalclass;
    Mydatabase mydatabase;

    RelativeLayout selectVendorlayout;
    TextView vendorName;
    ImageView ivrefreshVendorList;
    LinearLayout addInventorylo;
    RecyclerView inventoryRecyclerView;
    TextInputLayout billNotf;
    EditText billNo;
    MaterialButton addbt;

    ArrayList<purchaseDetail> purchaseDetailArrayList = new ArrayList<>();
    addPurchaseInventoryAdapter addPurchaseInventoryAdapter;

    int PURCHASE_LIST = 111;
    int CHANGE_SELLING_PRICE = 222;

    ArrayList<vendorDetail> vendorDetailArrayList = new ArrayList<>();
    ArrayList<String> vendorList = new ArrayList<>();
    int selectedVendorPos = -1;

    boolean checkDetailsAdded = false;

    ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_purchase);

        init();
        setToolbar();
        binding();
        onClick();
        getVendorData();
        mydatabase.unCheckAllSelectedInventory();
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
        toolbar.setTitle(getString(R.string.add_purchase_item));
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                onBackPressed();
            }
        });
    }

    void binding() {
        selectVendorlayout = findViewById(R.id.selectVendorlayout);
        vendorName = findViewById(R.id.vendorName);
        ivrefreshVendorList = findViewById(R.id.ivrefreshVendorList);
        addInventorylo = findViewById(R.id.addInventorylo);
        inventoryRecyclerView = findViewById(R.id.inventoryRecyclerView);
        billNotf = findViewById(R.id.billNotf);
        billNo = findViewById(R.id.billNo);
        addbt = findViewById(R.id.addbt);
    }

    void onClick() {

        selectVendorlayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showVendorDialogue();
            }
        });

        ivrefreshVendorList.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (!globalclass.isInternetPresent()) {
                    globalclass.toast_long(getResources().getString(R.string.noInternetConnection));
                    return;
                }

                getVendorList();
            }
        });

        addInventorylo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(activity, SelectInventory.class);
                intent.putExtra("action", tag);
                Bundle bundle = new Bundle();
                bundle.putParcelableArrayList("arrayList", purchaseDetailArrayList);
                intent.putExtras(bundle);
                startActivityForResult(intent, PURCHASE_LIST);
            }
        });

        billNo.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {

                if (actionId == EditorInfo.IME_ACTION_DONE) {

                    globalclass.hideKeyboard(activity);

                    if (!globalclass.isInternetPresent()) {
                        globalclass.toast_long(getResources().getString(R.string.noInternetConnection));
                        return false;
                    }

                    if (validation()) {

//                        if (checkPrice()) {
//                            showConfirmDialogue();
//                        }

                        showConfirmDialogue();
                    }
                }
                return false;
            }
        });

        addbt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                globalclass.hideKeyboard(activity);

                if (!globalclass.isInternetPresent()) {
                    globalclass.toast_long(getResources().getString(R.string.noInternetConnection));
                    return;
                }

                if (validation()) {

//                    if (checkPrice()) {
//                        showConfirmDialogue();
//                    }

                    showConfirmDialogue();
                }
            }
        });
    }

    void showConfirmDialogue() {

        new MaterialAlertDialogBuilder(activity, R.style.RoundShapeTheme)
                .setTitle("Sure")
                .setMessage("Are you sure you want to add in purchase?")
                .setCancelable(false)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        addPurchaseItem();
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

    void addPurchaseItem() {

        try {
            showprogress("Adding item to purchase","Please wait...");

            WriteBatch batch = globalclass.firebaseInstance().batch();

            for (int i = 0; i < purchaseDetailArrayList.size(); i++) {
                CollectionReference purchaseColl = globalclass.firebaseInstance().collection(Globalclass.purchaseColl);
                String purchaseId = purchaseColl.document().getId();

                purchaseDetail purchaseDetailModel = purchaseDetailArrayList.get(i);
                purchaseDetailModel.setPurchaseId(purchaseId);
                purchaseDetailModel.setVendorId(vendorDetailArrayList.get(selectedVendorPos).getVendorId());
                purchaseDetailModel.setVendorName(vendorDetailArrayList.get(selectedVendorPos).getVendorName().toLowerCase());
                purchaseDetailModel.setAdminId(globalclass.getStringData("adminId"));
                purchaseDetailModel.setAdminName(globalclass.getStringData("adminName"));
                purchaseDetailModel.setBillno(globalclass.checknull(billNo.getText().toString().trim().toLowerCase()));
                //todo convert quantity to kgs and liters
                if (purchaseDetailModel.getSelectedUnit().equalsIgnoreCase("kgs") ||
                        purchaseDetailModel.getSelectedUnit().equalsIgnoreCase("liters")) {

                    double quantity = purchaseDetailModel.getQuantity();
                    purchaseDetailModel.setQuantity(quantity * 1000);
                }

                DocumentReference purchaseReference = purchaseColl.document(purchaseId);
                batch.set(purchaseReference, purchaseDetailModel);

//                CollectionReference stockColl = globalclass.firebaseInstance().collection(Globalclass.stockColl);
//                String stockId = stockColl.document().getId();
//
//                stockDetail stockDetailModel = new stockDetail();
//                stockDetailModel.setInventoryId(purchaseDetailModel.getInventoryId());
//                stockDetailModel.setName(purchaseDetailModel.getName());
//                stockDetailModel.setUnitId(purchaseDetailModel.getUnitId());
//                stockDetailModel.setUnit(purchaseDetailModel.getUnit());
//                stockDetailModel.setSelectedUnit(purchaseDetailModel.getSelectedUnit());
//                stockDetailModel.setQuantity(purchaseDetailModel.getQuantity());
//                stockDetailModel.setPrice(purchaseDetailModel.getPurchasePrice());
//                stockDetailModel.setBillno(purchaseDetailModel.getBillno());
//                stockDetailModel.setInOutType("IN");
//                stockDetailModel.setStockId(stockId);
//                stockDetailModel.setInOutId(purchaseId);
//
//                DocumentReference stockReference = stockColl.document(stockId);
//                batch.set(stockReference, stockDetailModel);

                CollectionReference inventoryColl = globalclass.firebaseInstance().collection(Globalclass.inventoryColl);
                DocumentReference inventoryReference = inventoryColl.document(purchaseDetailModel.getInventoryId());
                Map<String, Object> map = new HashMap<>();
                map.put("costPrice", purchaseDetailModel.getPerPrice());
                map.put("quantity", FieldValue.increment(purchaseDetailModel.getQuantity()));
                batch.update(inventoryReference, map);
            }

            batch.commit().addOnSuccessListener(activity, new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void aVoid) {

                    hideprogress();
                    changeInventoryStockLocally();
                }
            }).addOnFailureListener(activity, new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {

                    hideprogress();
                    String error = Log.getStackTraceString(e);
                    globalclass.log(tag, "addPurchaseItem: " + error);
                    globalclass.toast_long("Unable to add purchase item, please try after sometime!");
                    globalclass.sendErrorLog(tag,"addPurchaseItem: onFailure",error);
                }
            });
        } catch (Exception e) {

            hideprogress();
            String error = Log.getStackTraceString(e);
            globalclass.log(tag, "addPurchaseItem: " + error);
            globalclass.toast_long("Unable to add purchase item, please try after sometime!");
            globalclass.sendErrorLog(tag,"addPurchaseItem: ",error);
        }
    }

    void changeInventoryStockLocally() {
        try {
            for (int i = 0; i < purchaseDetailArrayList.size(); i++) {
                purchaseDetail model = purchaseDetailArrayList.get(i);

                inventoryDetail inventoryModel = mydatabase.getParticularInventory(model.getInventoryId());
                double addQuantity = inventoryModel.getQuantity() + model.getQuantity();
                inventoryModel.setQuantity(addQuantity);
                inventoryModel.setCostPrice(model.getPerPrice());
                inventoryModel.setAdminId(globalclass.getStringData("adminId"));
                inventoryModel.setAdminName(globalclass.getStringData("adminName"));
                mydatabase.addInventory(inventoryModel);

                globalclass.toast_short("Purchase Item added successfully!");

                checkDetailsAdded = false;
                onBackPressed();
            }
        } catch (Exception e) {
            String error = Log.getStackTraceString(e);
            globalclass.log(tag, "changeInventoryStockLocally: " + error);
            globalclass.toast_long("Unable to update inventory stock, please refresh inventory stock!");
            globalclass.sendErrorLog(tag,"changeInventoryStockLocally: ",error);
            onBackPressed();
        }
    }

    void setAddPurchaseInventoryAdapter() {
        addPurchaseInventoryAdapter = new addPurchaseInventoryAdapter(activity, purchaseDetailArrayList, new addPurchaseInventoryOnClick() {
            @Override
            public void onAddQuantity(int position, purchaseDetail model) {
                showAddQuantityPriceDialogue(position, model);
            }

            @Override
            public void onDelete(int position, purchaseDetail model) {
                addPurchaseInventoryAdapter.deleteData(position);
                mydatabase.unCheckPerInventory(model.getInventoryId());
            }

        });

        inventoryRecyclerView.setLayoutManager(new LinearLayoutManager(activity));
        inventoryRecyclerView.setAdapter(addPurchaseInventoryAdapter);
    }

    void showHideInventoryRV() {
        if (!purchaseDetailArrayList.isEmpty()) {
            inventoryRecyclerView.setVisibility(View.VISIBLE);
        } else {
            inventoryRecyclerView.setVisibility(View.GONE);
        }
    }

    void getVendorData() {
        vendorDetailArrayList.clear();
        vendorList.clear();

        vendorDetailArrayList = mydatabase.getVendorList();
        if (!vendorDetailArrayList.isEmpty()) {

            fillVendorList();
        } else {
            getVendorList();
        }
    }

    void getVendorList() {

        try {

            vendorDetailArrayList.clear();
            vendorList.clear();

            showprogress("Refreshing vendor list", "Please wait...");

            CollectionReference inventoryColl = globalclass.firebaseInstance().collection(Globalclass.vendorColl);
            inventoryColl.get().addOnCompleteListener(activity, new OnCompleteListener<QuerySnapshot>() {
                @Override
                public void onComplete(@NonNull Task<QuerySnapshot> task) {

                    hideprogress();
                    globalclass.snackit(activity, "Refresh successfully!");

                    if (task.isSuccessful()) {
                        if (!task.getResult().isEmpty()) {
                            mydatabase.truncateTable(mydatabase.vendor);
                            for (DocumentSnapshot documentSnapshot : task.getResult()) {

                                vendorDetail model = documentSnapshot.toObject(vendorDetail.class);
                                mydatabase.addVendor(model);
                            }

                            getVendorData();
                        } else {
                            mydatabase.truncateTable(mydatabase.vendor);
                            globalclass.snackit(activity, "No vendor found!");
                        }
                    } else {
                        hideprogress();
                        String error = task.getException().toString();
                        globalclass.log(tag, "getVendorList: " + error);
                        globalclass.snackit(activity, "Unable to get vendor list, please try again after sometime!");
                        globalclass.sendErrorLog(tag,"getVendorList: ",error);
                    }
                }
            });

        } catch (Exception e) {

            hideprogress();
            String error = Log.getStackTraceString(e);
            globalclass.log(tag, "getVendorList: " + error);
            globalclass.toast_long("Unable to get vendor list, please try again after sometime!");
            globalclass.sendErrorLog(tag,"getVendorList: ",error);
        }
    }

    void fillVendorList() {

        if (!vendorDetailArrayList.isEmpty()) {
            for (int i = 0; i < vendorDetailArrayList.size(); i++) {
                vendorList.add(vendorDetailArrayList.get(i).getVendorName());
            }
        } else {
            globalclass.snackit(activity, "No vendor found!");
        }
    }

    void showVendorDialogue() {

        if (vendorDetailArrayList.isEmpty()) {
            globalclass.snackit(activity, "No vendor found!");
            return;
        }

        CharSequence[] charSequence = vendorList.toArray(new CharSequence[vendorList.size()]);
        MaterialAlertDialogBuilder materialAlertDialogBuilder =
                new MaterialAlertDialogBuilder(activity, R.style.RoundShapeTheme)
                        .setTitle("Select Vendor")
                        .setCancelable(false)
                        .setSingleChoiceItems(charSequence, selectedVendorPos, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int pos) {
                                selectedVendorPos = pos;
                                vendorName.setText(vendorList.get(selectedVendorPos));
                                dialogInterface.dismiss();
                            }
                        });
        materialAlertDialogBuilder.show();
    }

    void showAddQuantityPriceDialogue(int position, purchaseDetail model) {

        AlertDialog dialog = new AlertDialog.Builder(activity, R.style.CustomAlertDialog)
                .setTitle("Add " + model.getName() + " " + model.getSelectedUnit() + " & price")
                .setPositiveButton("Add", null)
                .setNeutralButton("Cancel", null)
                .setView(R.layout.addquatityandpricebs)
                .setCancelable(false)
                .show();

        String kgs = "kgs";
        String liters = "liters";

        final TextInputLayout quantitytf = dialog.findViewById(R.id.quantitytf);
        final TextInputLayout pricetf = dialog.findViewById(R.id.pricetf);
        final EditText quantity = dialog.findViewById(R.id.quantity);
        final EditText price = dialog.findViewById(R.id.price);
        final CheckBox unitTypeCheckBox = dialog.findViewById(R.id.unitTypeCheckBox);

        if(model.getUnit().equalsIgnoreCase("pieces")) {
            quantity.setInputType(InputType.TYPE_CLASS_NUMBER);
        }

        if (model.getQuantity() > 0) {
            quantity.setText(globalclass.checknull(String.valueOf(model.getQuantity())));
        }

        if (model.getPurchasePrice() > 0) {
            price.setText(globalclass.checknull(String.valueOf(model.getPurchasePrice())));
        }

        quantity.setSelection(quantity.getText().length());

        if (model.getUnit().equalsIgnoreCase("ml")) {
            unitTypeCheckBox.setText("In liters");
        }
        else if (model.getUnit().equalsIgnoreCase("grams")) {
            unitTypeCheckBox.setText("In kgs");
        }
        else {
            unitTypeCheckBox.setVisibility(View.GONE);
        }

        if(model.getSelectedUnit().equalsIgnoreCase(kgs) || model.getSelectedUnit().equalsIgnoreCase(liters)) {
            unitTypeCheckBox.setChecked(true);
        }

        unitTypeCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean check) {
                if(model.getUnit().equalsIgnoreCase("grams")) {
                    if(check) {
                        model.setSelectedUnit(kgs);
                    }
                    else {
                        model.setSelectedUnit(model.getUnit());
                    }
                }
                else if(model.getUnit().equalsIgnoreCase("ml")) {
                    if(check) {
                        model.setSelectedUnit(liters);
                    }
                    else {
                        model.setSelectedUnit(model.getUnit());
                    }
                }
            }
        });

        Button positiveButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
        positiveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                globalclass.hidekeyboard_dialogue(dialog);
                if (quantity.getText().toString().length() == 0 ||
                        Double.parseDouble(quantity.getText().toString()) == 0) {
                    quantitytf.setError("Invalid quantity");
                    quantity.requestFocus();
                } else if (price.getText().toString().length() == 0 ||
                        Double.parseDouble(price.getText().toString()) == 0) {
                    pricetf.setError("Invalid price");
                    price.requestFocus();
                } else {

                    model.setQuantity(Double.parseDouble(quantity.getText().toString().trim()));
                    model.setPurchasePrice(Double.parseDouble(price.getText().toString().trim()));
                    model.setPerPrice(calculatePerPrice(model));
                    model.setShowDialogueToChangePrice(true);
                    addPurchaseInventoryAdapter.updateData(position, model);

                    dialog.dismiss();
                }
            }
        });

        Button neutralButton = dialog.getButton(AlertDialog.BUTTON_NEUTRAL);
        neutralButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                globalclass.hidekeyboard_dialogue(dialog);
                dialog.dismiss();
            }
        });

        price.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {

                    globalclass.hidekeyboard_dialogue(dialog);
                    if (quantity.getText().toString().length() == 0 ||
                            Double.parseDouble(quantity.getText().toString()) == 0) {
                        quantitytf.setError("Invalid quantity");
                        quantity.requestFocus();
                    } else if (price.getText().toString().length() == 0 ||
                            Double.parseDouble(price.getText().toString()) == 0) {
                        pricetf.setError("Invalid price");
                        price.requestFocus();
                    } else {

                        model.setQuantity(Double.parseDouble(quantity.getText().toString().trim()));
                        model.setPurchasePrice(Double.parseDouble(price.getText().toString().trim()));
                        model.setPerPrice(calculatePerPrice(model));
                        model.setShowDialogueToChangePrice(true);
                        addPurchaseInventoryAdapter.updateData(position, model);

                        dialog.dismiss();
                    }
                }

                return false;
            }
        });
    }

    boolean checkAllItemQuantityIsEntered() {
        for (int i = 0; i < purchaseDetailArrayList.size(); i++) {
            if (purchaseDetailArrayList.get(i).getQuantity() < 1) {
                return false;
            }
        }

        return true;
    }

    //todo checking purchase price less then selling price
    boolean checkPrice() {

        double quantity = 0;

        for (int i = 0; i < purchaseDetailArrayList.size(); i++) {
            purchaseDetail model = purchaseDetailArrayList.get(i);
            if (model.getSelectedUnit().equalsIgnoreCase("kgs") ||
                    model.getSelectedUnit().equalsIgnoreCase("liters")) {

//                double quantity = model.getQuantity();
//                model.setQuantity(quantity * 1000);
//                model.setSelectedUnit(model.getUnit());

                quantity = model.getQuantity() * 1000;
            }
            else {
                quantity = model.getQuantity();
            }

            inventoryDetail inventoryDetailModel = mydatabase.getParticularInventory(model.getInventoryId());

            //todo checking Selling Price
            if (!model.getUnit().equalsIgnoreCase("pieces") &&
                    inventoryDetailModel.getSellingPrice() <= getPerKgLiterPrice(model.getPurchasePrice(), quantity) &&
                    model.getShowDialogueToChangePrice()) {

                showChangeSellingPriceDialogue(
                        "Change the Selling Price of " + model.getName().toUpperCase() + " ,as " + model.getName().toUpperCase() + " has been purchase at equal or more then it's Selling Price."
                        , model.getInventoryId());
                return false;

            } else if (model.getUnit().equalsIgnoreCase("pieces") &&
                    inventoryDetailModel.getSellingPrice() <= getPerPiecePrice(model.getPurchasePrice(), quantity) &&
                    model.getShowDialogueToChangePrice()) {

                showChangeSellingPriceDialogue(
                        "Change the Selling Price of " + model.getName().toUpperCase() + " ,as " + model.getName().toUpperCase() + " has been purchase at equal or more then it's Selling Price."
                        , model.getInventoryId());
                return false;
            }

            //todo checking Purchase Price
            if (!model.getUnit().equalsIgnoreCase("pieces")) {

                if(getPerKgLiterPrice(model.getPurchasePrice(), quantity)
                        < inventoryDetailModel.getCostPrice() &&
                        model.getShowDialogueToChangePrice()) {

                    int finalI = i;
                    new MaterialAlertDialogBuilder(activity, R.style.RoundShapeTheme)
                            .setTitle("Alert")
                            .setMessage("Purchase Price of "+model.getName().toUpperCase()+" is LESS then Last Purchase Price, do you want to change the Selling Price of "+model.getName().toUpperCase()+" ?")
                            .setCancelable(false)
                            .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {

                                    dialogueShown(finalI);
                                    startActivityForResult(new Intent(activity, UpdateInventory.class)
                                                    .putExtra("position", "" + 0)
                                                    .putExtra("inventoryId", inventoryDetailModel.getInventoryId()),
                                            CHANGE_SELLING_PRICE);
                                    dialog.dismiss();

                                }
                            })
                            .setNeutralButton("No", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {

                                    dialogueShown(finalI);
                                    dialogInterface.dismiss();
                                }
                            })
                            .show();

                    return false;
                }
                else if(getPerKgLiterPrice(model.getPurchasePrice(), quantity)
                        > inventoryDetailModel.getCostPrice() &&
                        model.getShowDialogueToChangePrice()) {

                    int finalI1 = i;
                    new MaterialAlertDialogBuilder(activity, R.style.RoundShapeTheme)
                            .setTitle("Alert")
                            .setMessage("Purchase Price of "+model.getName().toUpperCase()+" is MORE then Last Purchase Price, do you want to change the Selling Price of "+model.getName().toUpperCase()+" ?")
                            .setCancelable(false)
                            .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {

                                    dialogueShown(finalI1);

                                    startActivityForResult(new Intent(activity, UpdateInventory.class)
                                                    .putExtra("position", "" + 0)
                                                    .putExtra("inventoryId", inventoryDetailModel.getInventoryId()),
                                            CHANGE_SELLING_PRICE);
                                    dialog.dismiss();

                                }
                            })
                            .setNeutralButton("No", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {

                                    dialogueShown(finalI1);
                                    dialogInterface.dismiss();
                                }
                            })
                            .show();

                    return false;
                }
            }
            else if (model.getUnit().equalsIgnoreCase("pieces")) {

                if(getPerPiecePrice(model.getPurchasePrice(), model.getQuantity())
                        < inventoryDetailModel.getCostPrice() &&
                        model.getShowDialogueToChangePrice()) {

                    int finalI = i;
                    new MaterialAlertDialogBuilder(activity, R.style.RoundShapeTheme)
                            .setTitle("Alert")
                            .setMessage("Purchase Price of "+model.getName().toUpperCase()+" is LESS then Last Purchase Price, do you want to change the Selling Price of "+model.getName().toUpperCase()+" ?")
                            .setCancelable(false)
                            .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {

                                    dialogueShown(finalI);
                                    startActivityForResult(new Intent(activity, UpdateInventory.class)
                                                    .putExtra("position", "" + 0)
                                                    .putExtra("inventoryId", inventoryDetailModel.getInventoryId()),
                                            CHANGE_SELLING_PRICE);
                                    dialog.dismiss();
                                }
                            })
                            .setNeutralButton("No", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {

                                    dialogueShown(finalI);
                                    dialogInterface.dismiss();
                                }
                            })
                            .show();

                    return false;
                }
                else if(getPerPiecePrice(model.getPurchasePrice(), model.getQuantity())
                        > inventoryDetailModel.getCostPrice() &&
                        model.getShowDialogueToChangePrice()) {

                    int finalI = i;
                    new MaterialAlertDialogBuilder(activity, R.style.RoundShapeTheme)
                            .setTitle("Alert")
                            .setMessage("Purchase Price of "+model.getName().toUpperCase()+" is MORE then Last Purchase Price, do you want to change the Selling Price of "+model.getName().toUpperCase()+" ?")
                            .setCancelable(false)
                            .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {

                                    dialogueShown(finalI);
                                    startActivityForResult(new Intent(activity, UpdateInventory.class)
                                                    .putExtra("position", "" + 0)
                                                    .putExtra("inventoryId", inventoryDetailModel.getInventoryId()),
                                            CHANGE_SELLING_PRICE);
                                    dialog.dismiss();

                                }
                            })
                            .setNeutralButton("No", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {

                                    dialogueShown(finalI);
                                    dialogInterface.dismiss();
                                }
                            })
                            .show();

                    return false;
                }
            }
        }

        return true;
    }

    void dialogueShown(int showmDialoguePos) {
        for (int i = 0; i < purchaseDetailArrayList.size(); i++) {
            purchaseDetail model = purchaseDetailArrayList.get(i);
            if(i == showmDialoguePos) {
                model.setShowDialogueToChangePrice(false);
                addPurchaseInventoryAdapter.updateData(i,model);
                return;
            }
        }
    }

    void showChangeSellingPriceDialogue(String message, String inventoryId) {
        new MaterialAlertDialogBuilder(activity, R.style.RoundShapeTheme)
                .setTitle("Alert")
                .setMessage(message)
                .setCancelable(false)
                .setPositiveButton("Change Selling Price", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        startActivityForResult(new Intent(activity, UpdateInventory.class)
                                        .putExtra("position",""+ 0)
                                        .putExtra("inventoryId", inventoryId),
                                CHANGE_SELLING_PRICE);
                        dialog.dismiss();

                    }
                })
                .setNeutralButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                    }
                })
                .show();

    }

    void update(String inventoryId) {
        for(int i=0;i<purchaseDetailArrayList.size();i++) {
            purchaseDetail purchaseDetailModel = purchaseDetailArrayList.get(i);
            if(purchaseDetailModel.getInventoryId().equalsIgnoreCase(inventoryId)) {
                purchaseDetailModel.setShowDialogueToChangePrice(false);
                addPurchaseInventoryAdapter.updateData(i,purchaseDetailModel);
                return;
            }
        }
    }

    double getPerKgLiterPrice(double price, double quantity) {

        double perGram = price / quantity;
        double perKgPrice = perGram * 1000;


        globalclass.log(tag,"getPerKgLiterPrice: "+perKgPrice);
        return perKgPrice;
    }

    double getPerPiecePrice(double price, double quantity) {

        double perPiecePrice = price / quantity;

        globalclass.log(tag,"getPerPiecePrice: "+perPiecePrice);
        return perPiecePrice;
    }

    double calculatePerPrice(purchaseDetail model) {

        if(model.getUnit().equalsIgnoreCase("grams")) {
            if(model.getSelectedUnit().equalsIgnoreCase("kgs")) {

                double grams = model.getQuantity()*1000;
                double perGram = model.getPurchasePrice() / grams;
                double perPrice = perGram * 1000;
                return perPrice;
            }
            else if(model.getSelectedUnit().equalsIgnoreCase(model.getUnit())) {

                double perGram = model.getPurchasePrice() / model.getQuantity();
                double perPrice = perGram * 1000;
                return perPrice;
            }
        }
        else if(model.getUnit().equalsIgnoreCase("ml")) {
            if(model.getSelectedUnit().equalsIgnoreCase("liters")) {

                double ml = model.getQuantity()*1000;
                double perML = model.getPurchasePrice() / ml;
                double perPrice = perML * 1000;
                return perPrice;
            }
            else if(model.getSelectedUnit().equalsIgnoreCase(model.getUnit())) {

                double perML = model.getPurchasePrice() / model.getQuantity();
                double perPrice = perML * 1000;
                return perPrice;
            }
        }
        else if(model.getUnit().equalsIgnoreCase("pieces")) {

            double perPiece = model.getPurchasePrice() / model.getQuantity();
            return perPiece;
        }

        return -1;
    }

    boolean validation() {

        if (selectedVendorPos < 0) {
            globalclass.snackit(activity, "Please Select Vendor!");
            return false;
        } else if (purchaseDetailArrayList == null || purchaseDetailArrayList.isEmpty()) {
            globalclass.snackit(activity, "Please add inventory, which are purchased!");
            return false;
        } else if (!checkAllItemQuantityIsEntered()) {
            globalclass.snackit(activity, "Please enter the quantity for all items!");
            return false;
        } else if (billNo.getText().length() > 0) {
            if (billNo.getText().length() < 3) {
                billNotf.setError("Should contain atleast 3 characters!");
                return false;
            } else if (!billNo.getText().toString().trim().matches(globalclass.alphaNumericRegexOne())) {
                billNotf.setError("Invalid Bill no!");
                return false;
            }
        }

        billNotf.setErrorEnabled(false);
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
    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PURCHASE_LIST) {
            if (resultCode == Activity.RESULT_OK) {
                checkDetailsAdded = true;
                purchaseDetailArrayList = data.getExtras().getParcelableArrayList("arrayList");
                showHideInventoryRV();
                setAddPurchaseInventoryAdapter();
            }
        } else if (requestCode == CHANGE_SELLING_PRICE) {
            if (resultCode == Activity.RESULT_OK) {
                checkDetailsAdded = true;
                globalclass.toast_short("Selling price changed successfully!");
                String inventoryId = data.getStringExtra("inventoryId");
                update(inventoryId);
            }
        }
    }

    boolean checkDetailsAdded() {

        if(purchaseDetailArrayList != null && !purchaseDetailArrayList.isEmpty()) {
            return true;
        }

        return false;
    }

    void showCancelDialogue() {

        new MaterialAlertDialogBuilder(activity, R.style.RoundShapeTheme)
                .setTitle("Sure")
                .setMessage("Are you sure you want to cancel ?")
                .setCancelable(false)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        dialog.dismiss();
                        checkDetailsAdded = false;
                        onBackPressed();
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

    @Override
    public void onBackPressed() {

        if(checkDetailsAdded() && checkDetailsAdded) {
            showCancelDialogue();
        }
        else {
            super.onBackPressed();
        }
    }
}