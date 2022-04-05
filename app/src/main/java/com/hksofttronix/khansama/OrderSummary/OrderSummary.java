package com.hksofttronix.khansama.OrderSummary;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.WriteBatch;
import com.google.gson.Gson;
import com.hksofttronix.khansama.Address.Address;
import com.hksofttronix.khansama.MainActivity;
import com.hksofttronix.khansama.Models.addressDetail;
import com.hksofttronix.khansama.Models.allOrderItemDetail;
import com.hksofttronix.khansama.Models.cartDetail;
import com.hksofttronix.khansama.Models.checkStockExistDetail;
import com.hksofttronix.khansama.Models.inventoryDetail;
import com.hksofttronix.khansama.Models.orderItemDetail;
import com.hksofttronix.khansama.Models.orderStatusLogDetail;
import com.hksofttronix.khansama.Models.orderSummaryDetail;
import com.hksofttronix.khansama.Models.placeOrderDetail;
import com.hksofttronix.khansama.Models.stockDetail;
import com.hksofttronix.khansama.R;
import com.hksofttronix.khansama.Globalclass;
import com.hksofttronix.khansama.Mydatabase;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class OrderSummary extends AppCompatActivity {

    String tag = this.getClass().getSimpleName();
    AppCompatActivity activity = OrderSummary.this;

    Globalclass globalclass;
    Mydatabase mydatabase;

    RecyclerView recyclerView;
    TextView name, mobileNumber, address, nearby, city, state, pincode;
    TextView changeAddress, addAddress;
    MaterialCardView addressLayout;
    TextView item, recipeCharges,packagingCharges,total;
    MaterialButton placeOrderbt;

    ArrayList<orderItemDetail> orderItemList = new ArrayList<>();
    OrderSummaryAdapter adapter;

    orderSummaryDetail model = new orderSummaryDetail();

    addressDetail addressDetailModel = null;
    int CHANGE_ADDRESS = 111;

    int recipeTotalCharges;
    int additionalCharges;
    int Total;

    boolean shownCancelDialogue = true;

    ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_summary);

        setToolbar();
        init();
        binding();
        onClick();
        setText();
    }

    void init() {
        globalclass = Globalclass.getInstance(activity);
        mydatabase = Mydatabase.getInstance(activity);
    }

    void binding() {
        recyclerView = findViewById(R.id.recyclerView);
        name = findViewById(R.id.name);
        mobileNumber = findViewById(R.id.mobileNumber);
        address = findViewById(R.id.address);
        nearby = findViewById(R.id.nearby);
        city = findViewById(R.id.city);
        state = findViewById(R.id.state);
        pincode = findViewById(R.id.pincode);
        changeAddress = findViewById(R.id.changeAddress);
        addAddress = findViewById(R.id.addAddress);
        addressLayout = findViewById(R.id.addressLayout);
        item = findViewById(R.id.item);
        recipeCharges = findViewById(R.id.recipeCharges);
        packagingCharges = findViewById(R.id.packagingCharges);
        total = findViewById(R.id.total);
        placeOrderbt = findViewById(R.id.placeOrderbt);
    }

    void onClick() {
        changeAddress.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent intent = new Intent(activity, Address.class);
                intent.putExtra("action", tag);
                startActivityForResult(intent, CHANGE_ADDRESS);
            }
        });

        addAddress.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(activity, Address.class);
                intent.putExtra("action", tag);
                startActivityForResult(intent, CHANGE_ADDRESS);
            }
        });

        placeOrderbt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (!globalclass.isInternetPresent()) {
                    globalclass.toast_long(getResources().getString(R.string.noInternetConnection));
                    return;
                }

                new MaterialAlertDialogBuilder(activity, R.style.RoundShapeTheme)
                        .setTitle("Sure")
                        .setMessage("Are you sure you want to place order?")
                        .setCancelable(false)
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                                placeOrder();
                            }
                        })
                        .setNeutralButton("Cancel", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                dialogInterface.dismiss();
                            }
                        })
                        .show();
            }
        });
    }

    void setToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_close);
        toolbar.setTitle(R.string.order_summary);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                onBackPressed();
            }
        });
    }

    void setText() {
        model.setItem(mydatabase.getCartItemCount());

        item.setText("" + model.getItem());
        recipeCharges.setText("₹ " + mydatabase.getCartSum());

        additionalCharges = Integer.parseInt(getIntent().getStringExtra("additionalCharges"));
        packagingCharges.setText("₹ " + additionalCharges);
        recipeTotalCharges = mydatabase.getCartSum();

        Total = recipeTotalCharges + additionalCharges;
        total.setText("₹ " + Total);

        model.setRecipeCharges(recipeTotalCharges);
        model.setPackagingCharges(additionalCharges);
        model.setTotal(Total);

        fillOrderItemList();
    }

    void setTextDeliveryAddress() {

        addAddress.setVisibility(View.GONE);
        addressLayout.setVisibility(View.VISIBLE);
        changeAddress.setVisibility(View.VISIBLE);
        placeOrderbt.setEnabled(true);

        model.setAddressId(addressDetailModel.getAddressId());
        model.setName(addressDetailModel.getName());
        model.setMobileNumber(addressDetailModel.getMobileNumber());
        model.setAddress(addressDetailModel.getAddress());
        model.setNearBy(addressDetailModel.getNearBy());
        model.setCityId(addressDetailModel.getCityId());
        model.setCity(addressDetailModel.getCity());
        model.setStateId(addressDetailModel.getStateId());
        model.setState(addressDetailModel.getState());
        model.setPincode(addressDetailModel.getPincode());

        name.setText(model.getName());
        mobileNumber.setText("+91" + model.getMobileNumber());
        address.setText(model.getAddress());
        nearby.setText(model.getNearBy());
        city.setText(globalclass.firstLetterCapital(model.getCity()));
        state.setText(globalclass.firstLetterCapital(model.getState()));
        pincode.setText(model.getPincode());
    }

    void getFirstAddress() {

        if (addressDetailModel != null) {
            //todo check selected address is available locally
            if (mydatabase.getParticularAddressDetail(addressDetailModel.getAddressId()) != null) {
                setTextDeliveryAddress();
            } else {
                addressLayout.setVisibility(View.GONE);
                changeAddress.setVisibility(View.GONE);
                addAddress.setVisibility(View.VISIBLE);
                placeOrderbt.setEnabled(false);
            }
        } else {
            ArrayList<addressDetail> addressList = mydatabase.getAddressList();
            if (addressList != null && !addressList.isEmpty()) {
                for (int i = 0; i < 1; i++) {
                    addressDetailModel = addressList.get(i);
                }

                setTextDeliveryAddress();

            } else {
                addressLayout.setVisibility(View.GONE);
                changeAddress.setVisibility(View.GONE);
                addAddress.setVisibility(View.VISIBLE);
                placeOrderbt.setEnabled(false);
            }
        }
    }

    void fillOrderItemList() {
        try {

            ArrayList<cartDetail> cartList;
            cartList = mydatabase.getCartList();
            if (cartList != null && !cartList.isEmpty()) {
                for (int i = 0; i < cartList.size(); i++) {
                    orderItemDetail orderItemModel = new orderItemDetail();
                    orderItemModel.setRecipeId(cartList.get(i).getRecipeId());
                    orderItemModel.setRecipeName(cartList.get(i).getRecipeName());
                    orderItemModel.setRecipeImageId(cartList.get(i).getRecipeImageId());
                    orderItemModel.setRecipeImageUrl(cartList.get(i).getRecipeImageUrl());
                    orderItemModel.setQuantity(cartList.get(i).getQuantity());
                    orderItemModel.setPrice(cartList.get(i).getPrice());

                    orderItemList.add(orderItemModel);
                }

                setAdapter();
            } else {

                String error = "cartList is null or empty";
                globalclass.log(tag, "fillOrderList: " + error);
                globalclass.toast_long("Unable to get order recipe list, please try again later!");
                globalclass.sendErrorLog(tag,"fillOrderItemList",error);

            }
        } catch (Exception e) {

            String error = Log.getStackTraceString(e);
            globalclass.log(tag, "fillOrderList: " + error);
            globalclass.toast_long("Unable to get order recipe list, please try again later!");
            globalclass.sendErrorLog(tag,"fillOrderItemList",error);
        }
    }

    void setAdapter() {
        adapter = new OrderSummaryAdapter(activity, orderItemList, new OrderSummaryOnClick() {
            @Override
            public void viewDetail(int position, orderItemDetail model) {

            }
        });

        recyclerView.setLayoutManager(new LinearLayoutManager(activity));
        recyclerView.setAdapter(adapter);
    }

    void placeOrder() {

        String parameter = "";

        try {
            showprogress("Placing order", "Please wait...");

            WriteBatch batch = globalclass.firebaseInstance().batch();

            CollectionReference placeOrderColl = globalclass.firebaseInstance().collection(Globalclass.placeOrderColl);
            String orderId = placeOrderColl.document().getId();

            model.setOrderId(orderId);
            model.setCancelReason("");
            model.setUserId(globalclass.getStringData("id"));
            model.setUserMobileNumber(globalclass.getStringData("mobilenumber"));
            model.setOrderStatus(mydatabase.getParticularOrderStatus(1).getOrderStatus());
            model.setOrderStep(mydatabase.getParticularOrderStatus(1).getStepNumber());

            DocumentReference placeOrderDocReference = placeOrderColl.document(orderId);
            batch.set(placeOrderDocReference, model);

            CollectionReference orderedItemColl = globalclass.firebaseInstance().collection(Globalclass.orderItemColl);
            for (int i = 0; i < orderItemList.size(); i++) {
                String orderItemId = orderedItemColl.document().getId();

                orderItemList.get(i).setOrderItemId(orderItemId);
                orderItemList.get(i).setOrderId(orderId);
                orderItemDetail orderItemModel = orderItemList.get(i);
                orderItemModel.setUserId(globalclass.getStringData("id"));

                DocumentReference orderItemDocReference = orderedItemColl.document(orderItemId);
                batch.set(orderItemDocReference, orderItemModel);
            }

            CollectionReference stockColl = globalclass.firebaseInstance().collection(Globalclass.stockColl);
            CollectionReference inventoryColl = globalclass.firebaseInstance().collection(Globalclass.inventoryColl);
            ArrayList<checkStockExistDetail> cartRecipeIngredientsList = mydatabase.getCartRecipeIngredients();
            for(int i=0;i<cartRecipeIngredientsList.size();i++) {
                checkStockExistDetail model = cartRecipeIngredientsList.get(i);
                model.setOrderId(orderId);

                String stockId = stockColl.document().getId();

                stockDetail stockDetailModel = new stockDetail();
                stockDetailModel.setStockId(stockId);
                stockDetailModel.setInventoryId(model.getInventoryId());
                stockDetailModel.setName(model.getName());
                stockDetailModel.setUnitId(model.getUnitId());
                stockDetailModel.setUnit(model.getUnit());
                stockDetailModel.setSelectedUnit(model.getUnit());
                stockDetailModel.setQuantity(model.getSumOfQuantity());
//                stockDetailModel.setPrice(model.getSumOfPrice());
                stockDetailModel.setPrice(0);
                stockDetailModel.setBillno("");
                stockDetailModel.setInOutId(model.getOrderId());
                stockDetailModel.setInOutType("OUT");

                DocumentReference stockDocReference = stockColl.document(stockId);
                batch.set(stockDocReference,stockDetailModel);

                DocumentReference inventoryReference = inventoryColl.document(model.getInventoryId());
                Map<String,Object> invntoryMap = new HashMap<>();
                invntoryMap.put("quantity", FieldValue.increment(-model.getSumOfQuantity()));
                batch.update(inventoryReference, invntoryMap);
            }

            CollectionReference orderStatusLogColl = globalclass.firebaseInstance().collection(Globalclass.orderStatusLogColl);
            String logId = orderStatusLogColl.document().getId();
            orderStatusLogDetail orderStatusLogModel = new orderStatusLogDetail();
            orderStatusLogModel.setLogId(logId);
            orderStatusLogModel.setOrderId(orderId);
            orderStatusLogModel.setEntryBy("USER");
            orderStatusLogModel.setUserId(globalclass.getStringData("id"));
            orderStatusLogModel.setUserName(globalclass.getStringData("name"));
            orderStatusLogModel.setOrderStep(mydatabase.getParticularOrderStatus(1).getStepNumber());
            orderStatusLogModel.setOrderStatus(mydatabase.getParticularOrderStatus(1).getOrderStatus());

            DocumentReference orderItemDocReference = orderStatusLogColl.document(logId);
            batch.set(orderItemDocReference, orderStatusLogModel);

            CollectionReference cartColl = globalclass.firebaseInstance().collection(Globalclass.cartColl);
            ArrayList<cartDetail> cartList;
            cartList = mydatabase.getCartList();
            if (cartList != null && !cartList.isEmpty()) {
                for (int i = 0; i < cartList.size(); i++) {
                    cartDetail cartModel = cartList.get(i);

                    String cartId = cartModel.getCartId();

                    DocumentReference cartDocReference = cartColl.document(cartId);
                    batch.delete(cartDocReference);
                }
            }

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
                        addDataToPlaceOrder();

                        if (globalclass.getStringData("loginType").equalsIgnoreCase("Admin")) {
                            addDataToAllOrder();
                            changeInventoryStockLocally();
                        }

                        mydatabase.truncateTable(mydatabase.cart);
                        new MaterialAlertDialogBuilder(activity, R.style.RoundShapeTheme)
                                .setTitle("Ordered successfully")
                                .setMessage("Your order has been placed successfully, you will be notify for your order status!")
                                .setCancelable(false)
                                .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        dialog.dismiss();
                                        startActivity(new Intent(activity, MainActivity.class)
                                                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK));
                                        finish();
                                    }
                                })
                                .show();
                    } else {

                        String error = Log.getStackTraceString(task.getException());
                        globalclass.log(tag, "placeOrder: " + error);
                        globalclass.toast_long("Unable to place order, please try after sometime!");
                        globalclass.sendResponseErrorLog(tag,"placeOrder",error, finalParameter);
                    }
                }
            });
        } catch (Exception e) {

            hideprogress();
            String error = Log.getStackTraceString(e);
            globalclass.log(tag, "placeOrder: " + error);
            globalclass.toast_long("Unable to place order, please try after sometime!");
            globalclass.sendResponseErrorLog(tag,"placeOrder",error, parameter);
        }
    }

    void addDataToPlaceOrder() {

        try {
            for (int i = 0; i < orderItemList.size(); i++) {

                placeOrderDetail placeOrderModel = new placeOrderDetail();
                placeOrderModel.setOrderId(model.getOrderId());
                placeOrderModel.setUserId(model.getUserId());
                placeOrderModel.setUserMobileNumber(model.getUserMobileNumber());
                placeOrderModel.setOrderStatus(model.getOrderStatus());
                placeOrderModel.setOrderStep(model.getOrderStep());
                placeOrderModel.setRecipeCharges(model.getRecipeCharges());
                placeOrderModel.setPackagingCharges(model.getPackagingCharges());
                placeOrderModel.setTotal(model.getTotal());
                placeOrderModel.setItem(model.getItem());
                placeOrderModel.setOrderDateTime(globalclass.getDate());
                placeOrderModel.setDeliveryDateTime(globalclass.getDate());
                placeOrderModel.setAddressId(model.getAddressId());
                placeOrderModel.setName(model.getName());
                placeOrderModel.setMobileNumber(model.getMobileNumber());
                placeOrderModel.setAddress(model.getAddress());
                placeOrderModel.setNearBy(model.getNearBy());
                placeOrderModel.setCityId(model.getCityId());
                placeOrderModel.setCity(model.getCity());
                placeOrderModel.setStateId(model.getStateId());
                placeOrderModel.setState(model.getState());
                placeOrderModel.setPincode(model.getPincode());
                placeOrderModel.setOrderItemId(orderItemList.get(i).getOrderItemId());
                placeOrderModel.setRecipeId(orderItemList.get(i).getRecipeId());
                placeOrderModel.setRecipeName(orderItemList.get(i).getRecipeName());
                placeOrderModel.setRecipeImageId(orderItemList.get(i).getRecipeImageId());
                placeOrderModel.setRecipeImageUrl(orderItemList.get(i).getRecipeImageUrl());
                placeOrderModel.setPrice(orderItemList.get(i).getPrice());
                placeOrderModel.setQuantity(orderItemList.get(i).getQuantity());

                mydatabase.addPlaceOrder(placeOrderModel);
            }
        } catch (Exception e) {

            String error = Log.getStackTraceString(e);
            globalclass.log(tag, "addDataToPlaceOrder: " + error);
            globalclass.toast_long("Unable to add data in place order!");
            globalclass.sendErrorLog(tag,"addDataToPlaceOrder",error);
        }
    }

    void addDataToAllOrder() {

        try {
            for (int i = 0; i < orderItemList.size(); i++) {

                allOrderItemDetail allOrderItemModel = new allOrderItemDetail();
                allOrderItemModel.setOrderId(model.getOrderId());
                allOrderItemModel.setUserId(model.getUserId());
                allOrderItemModel.setUserMobileNumber(model.getUserMobileNumber());
                allOrderItemModel.setOrderStatus(model.getOrderStatus());
                allOrderItemModel.setOrderStep(model.getOrderStep());
                allOrderItemModel.setRecipeCharges(model.getRecipeCharges());
                allOrderItemModel.setPackagingCharges(model.getPackagingCharges());
                allOrderItemModel.setTotal(model.getTotal());
                allOrderItemModel.setItem(model.getItem());
                allOrderItemModel.setOrderDateTime(globalclass.getDate());
                allOrderItemModel.setDeliveryDateTime(globalclass.getDate());
                allOrderItemModel.setAddressId(model.getAddressId());
                allOrderItemModel.setName(model.getName());
                allOrderItemModel.setMobileNumber(model.getMobileNumber());
                allOrderItemModel.setAddress(model.getAddress());
                allOrderItemModel.setNearBy(model.getNearBy());
                allOrderItemModel.setCityId(model.getCityId());
                allOrderItemModel.setCity(model.getCity());
                allOrderItemModel.setStateId(model.getStateId());
                allOrderItemModel.setState(model.getState());
                allOrderItemModel.setPincode(model.getPincode());
                allOrderItemModel.setOrderItemId(orderItemList.get(i).getOrderItemId());
                allOrderItemModel.setRecipeId(orderItemList.get(i).getRecipeId());
                allOrderItemModel.setRecipeName(orderItemList.get(i).getRecipeName());
                allOrderItemModel.setRecipeImageUrl(orderItemList.get(i).getRecipeImageUrl());
                allOrderItemModel.setPrice(orderItemList.get(i).getPrice());
                allOrderItemModel.setQuantity(orderItemList.get(i).getQuantity());

                mydatabase.addAllOrder(allOrderItemModel);
            }
        } catch (Exception e) {

            String error = Log.getStackTraceString(e);
            globalclass.log(tag, "addDataToAllOrder: " + error);
            globalclass.toast_long("Unable to add data in all order!");
            globalclass.sendErrorLog(tag,"addDataToAllOrder",error);
        }
    }

    void changeInventoryStockLocally() {
        try {
            ArrayList<checkStockExistDetail> cartRecipeIngredientsList = mydatabase.getCartRecipeIngredients();
            for(int i=0;i<cartRecipeIngredientsList.size();i++) {
                checkStockExistDetail model = cartRecipeIngredientsList.get(i);
                inventoryDetail inventoryModel = mydatabase.getParticularInventory(model.getInventoryId());
                double minusQuantity = inventoryModel.getQuantity() - model.getSumOfQuantity();
                inventoryModel.setQuantity(minusQuantity);
                inventoryModel.setAdminId(globalclass.getStringData("adminId"));
                inventoryModel.setAdminName(globalclass.getStringData("adminName"));
                mydatabase.addInventory(inventoryModel);
            }
        }
        catch (Exception e) {

            String error = Log.getStackTraceString(e);
            globalclass.log(tag, "changeInventoryStockLocally: " + error);
            globalclass.toast_long("Unable to change inventory stock locally, please refresh date to see changes!");
            globalclass.sendErrorLog(tag,"changeInventoryStockLocally",error);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == CHANGE_ADDRESS) {
            if (resultCode == Activity.RESULT_OK && data != null) {

                addressDetailModel = mydatabase.getParticularAddressDetail(data.getStringExtra("addressId"));
                if (addressDetailModel != null) {
                    setTextDeliveryAddress();
                } else {
                    addressLayout.setVisibility(View.GONE);
                    changeAddress.setVisibility(View.GONE);
                    addAddress.setVisibility(View.VISIBLE);
                    placeOrderbt.setEnabled(false);
                }
            }
        }
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
    protected void onResume() {
        super.onResume();

        getFirstAddress();
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
                        shownCancelDialogue = false;
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

        if(shownCancelDialogue) {
            showCancelDialogue();
        }
        else {
//            super.onBackPressed();
            startActivity(new Intent(activity, MainActivity.class)
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK));
            finish();
        }
    }
}