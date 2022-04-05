package com.hksofttronix.khansama.Admin.AdminOrderDetail;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
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
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.Transaction;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.hksofttronix.khansama.Admin.OrderStatusLog.OrderStatusLog;
import com.hksofttronix.khansama.Admin.UpdateRecipe.UpdateRecipe;
import com.hksofttronix.khansama.Globalclass;
import com.hksofttronix.khansama.Models.allOrderItemDetail;
import com.hksofttronix.khansama.Models.inventoryDetail;
import com.hksofttronix.khansama.Models.orderItemDetail;
import com.hksofttronix.khansama.Models.orderRecipeIngredientDetail;
import com.hksofttronix.khansama.Models.orderStatusDetail;
import com.hksofttronix.khansama.Models.orderStatusLogDetail;
import com.hksofttronix.khansama.Models.orderSummaryDetail;
import com.hksofttronix.khansama.Models.stockDetail;
import com.hksofttronix.khansama.Mydatabase;
import com.hksofttronix.khansama.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class AdminOrderDetail extends AppCompatActivity {

    String tag = this.getClass().getSimpleName();
    AppCompatActivity activity = AdminOrderDetail.this;

    Globalclass globalclass;
    Mydatabase mydatabase;

    SwipeRefreshLayout swipeRefresh;
    RecyclerView recyclerView;
    TextView userMobileNumber,orderId, orderDateTime, deliveryDateTime;
    RelativeLayout deliveryLayout;
    TextView name, mobileNumber, address, nearby, city, state, pincode;
    TextView item,recipeCharges,packagingCharges,total;
    LinearLayout changeOrderStatusLayout,selectOrderStatusLayout;
    TextView tvOrderStatus;
    LinearLayout cancelReasonLayout;
    TextView cancelReason;
    MaterialButton orderStatus, changeStatusbt;

    String getOrderId = "";
    allOrderItemDetail model = null;

    ArrayList<allOrderItemDetail> arrayList = new ArrayList<>();
    AdminOrderItemAdapter adapter;

    ArrayList<orderStatusDetail> orderStatusList = new ArrayList<>();
    ArrayList<String> statusList = new ArrayList<>();
    int selectedStatusPos = -1;
    int orderStepNumber = -1;

    ArrayList<orderRecipeIngredientDetail> orderRecipeIngredientList = new ArrayList<>();

    boolean orderStatusChanged = false;

    ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_order_detail);

        setToolbar();
        init();
        binding();
        onClick();
        setText();
        getData();
    }

    void init() {
        globalclass = Globalclass.getInstance(activity);
        mydatabase = Mydatabase.getInstance(activity);
    }

    void binding() {
        swipeRefresh = findViewById(R.id.swipeRefresh);
        userMobileNumber = findViewById(R.id.userMobileNumber);
        orderId = findViewById(R.id.orderId);
        orderDateTime = findViewById(R.id.orderDateTime);
        deliveryDateTime = findViewById(R.id.deliveryDateTime);
        deliveryLayout = findViewById(R.id.deliveryLayout);
        recyclerView = findViewById(R.id.recyclerView);
        name = findViewById(R.id.name);
        mobileNumber = findViewById(R.id.mobileNumber);
        address = findViewById(R.id.address);
        nearby = findViewById(R.id.nearby);
        city = findViewById(R.id.city);
        state = findViewById(R.id.state);
        pincode = findViewById(R.id.pincode);
        item = findViewById(R.id.item);
        recipeCharges = findViewById(R.id.recipeCharges);
        packagingCharges = findViewById(R.id.packagingCharges);
        total = findViewById(R.id.total);
        cancelReasonLayout = findViewById(R.id.cancelReasonLayout);
        cancelReason = findViewById(R.id.cancelReason);
        orderStatus = findViewById(R.id.orderStatus);
        changeOrderStatusLayout = findViewById(R.id.changeOrderStatusLayout);
        selectOrderStatusLayout = findViewById(R.id.selectOrderStatusLayout);
        tvOrderStatus = findViewById(R.id.tvOrderStatus);
        changeStatusbt = findViewById(R.id.changeStatusbt);
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

                getPlaceOrderDetail();
            }
        });

        selectOrderStatusLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                showOrderStatusDialogue();
            }
        });

        changeStatusbt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                globalclass.hideKeyboard(activity);
                if (!globalclass.isInternetPresent()) {
                    swipeRefresh.setRefreshing(false);
                    globalclass.toast_long(getResources().getString(R.string.noInternetConnection));
                    return;
                }

                if(selectedStatusPos < 0) {
                    globalclass.snackit(activity,"Please select order status!");
                    return;
                }

                if(orderStepNumber == 5) {
                    showCancelOrderDialogue();
                }
                else {
                    new MaterialAlertDialogBuilder(activity, R.style.RoundShapeTheme)
                            .setTitle("Sure")
                            .setMessage("Are you sure you want to change order status?")
                            .setCancelable(false)
                            .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                    changeOrderStatus();
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
            }
        });
    }

    void setToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_backarrow);
        toolbar.setTitle(R.string.orderDetail);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                onBackPressed();
            }
        });
    }

    void setText() {
        getOrderId = getIntent().getStringExtra("orderId");
        model = mydatabase.getAdminParticularOrderDetail(getOrderId);

        if (model != null) {
            orderStatus.setText(globalclass.firstLetterCapital(model.getOrderStatus()));
            userMobileNumber.setText(model.getUserMobileNumber());
            orderId.setText(model.getOrderId());
            orderDateTime.setText(globalclass.dateToString(model.getOrderDateTime(),"dd MMM yyyy hh:mm aa"));
            if(model.getOrderStep() == 4) {
                deliveryLayout.setVisibility(View.VISIBLE);
                deliveryDateTime.setText(globalclass.dateToString(model.getDeliveryDateTime(),"dd MMM yyyy hh:mm aa"));
            }
            else {
                deliveryLayout.setVisibility(View.GONE);
            }

            if(model.getOrderStep() == 4 || model.getOrderStep() == 5) {
                changeOrderStatusLayout.setVisibility(View.GONE);
            }
            else {
                changeOrderStatusLayout.setVisibility(View.VISIBLE);
            }

            if(model.getOrderStep() == 5) {
                cancelReasonLayout.setVisibility(View.VISIBLE);
                cancelReason.setText(model.getCancelReason());
            }
            else {
                cancelReasonLayout.setVisibility(View.GONE);
            }

            name.setText(model.getName());
            mobileNumber.setText("+91" + model.getMobileNumber());
            address.setText(model.getAddress());
            nearby.setText(model.getNearBy());
            city.setText(globalclass.firstLetterCapital(model.getCity()));
            state.setText(globalclass.firstLetterCapital(model.getState()));
            pincode.setText(model.getPincode());

            item.setText("" + model.getItem());
            recipeCharges.setText("₹ " + model.getRecipeCharges());
            packagingCharges.setText("₹ " + model.getPackagingCharges());
            total.setText("₹ " + model.getTotal());

            fillOrderStatusList();
        } else {
//            globalclass.toast_long("Unable to show order detail, please try after sometime!");
            globalclass.log(tag,"Unable to show order detail, please try after sometime!");
        }
    }

    void getData() {
        arrayList.clear();
        arrayList = mydatabase.getAllOrderItemList(getOrderId);
        if (arrayList != null && !arrayList.isEmpty()) {
            setAdapter();
        } else {
            getPlaceOrderDetail();
        }
    }

    void getPlaceOrderDetail() {

        String parameter = "";

        try {
            CollectionReference placeOrderColl = globalclass.firebaseInstance().collection(Globalclass.placeOrderColl);
            Query query = placeOrderColl
                    .whereEqualTo("orderId", getOrderId);

            Gson gson = new Gson();
            if(model != null) {
                parameter = gson.toJson(model);
            }
            globalclass.log(tag,"parameter: "+parameter);

            String finalParameter = parameter;

            query.get().addOnCompleteListener(activity, new OnCompleteListener<QuerySnapshot>() {
                @Override
                public void onComplete(@NonNull Task<QuerySnapshot> task) {

                    swipeRefresh.setRefreshing(false);

                    if (task.isSuccessful()) {
                        if (!task.getResult().isEmpty()) {

                            mydatabase.deleteData(mydatabase.allOrder, "orderId", getOrderId);
                            for (DocumentSnapshot documents : task.getResult()) {
                                orderSummaryDetail model = documents.toObject(orderSummaryDetail.class);
                                getOrderItemList(model);
                            }
                        }
                    } else {

                        swipeRefresh.setRefreshing(false);
                        String error = Log.getStackTraceString(task.getException());
                        globalclass.log(tag, "getPlaceOrderDetail: " + error);
                        globalclass.sendResponseErrorLog(tag,"getPlaceOrderDetail: ",error, finalParameter);
                        globalclass.toast_long("Unable to get order list, please try after sometime!");
                    }
                }
            });
        } catch (Exception e) {

            swipeRefresh.setRefreshing(false);
            String error = Log.getStackTraceString(e);
            globalclass.log(tag, "getPlaceOrderDetail: " + error);
            globalclass.sendResponseErrorLog(tag,"getPlaceOrderDetail: ",error,parameter);
            globalclass.toast_long("Unable to get order list, please try after sometime!");
        }
    }

    void getOrderItemList(orderSummaryDetail model) {

        String parameter = "";

        try {
            CollectionReference orderItemColl = globalclass.firebaseInstance().collection(Globalclass.orderItemColl);
            Query query = orderItemColl
                    .whereEqualTo("orderId", model.getOrderId());

            Gson gson = new Gson();
            if(model != null) {
                parameter = gson.toJson(model);
            }
            globalclass.log(tag,"parameter: "+parameter);

            String finalParameter = parameter;

            query.get().addOnCompleteListener(activity, new OnCompleteListener<QuerySnapshot>() {
                @Override
                public void onComplete(@NonNull Task<QuerySnapshot> task) {

                    if (task.isSuccessful()) {
                        if (!task.getResult().isEmpty()) {

                            for (DocumentSnapshot documents : task.getResult()) {
                                orderItemDetail orderItemModel = documents.toObject(orderItemDetail.class);

                                allOrderItemDetail allOrderItemModel = new allOrderItemDetail();
                                allOrderItemModel.setOrderId(model.getOrderId());
                                allOrderItemModel.setUserId(model.getUserId());
                                allOrderItemModel.setUserMobileNumber(model.getUserMobileNumber());
                                allOrderItemModel.setCancelReason(model.getCancelReason());
                                allOrderItemModel.setOrderStatus(model.getOrderStatus());
                                allOrderItemModel.setOrderStep(model.getOrderStep());
                                allOrderItemModel.setRecipeCharges(model.getRecipeCharges());
                                allOrderItemModel.setPackagingCharges(model.getPackagingCharges());
                                allOrderItemModel.setTotal(model.getTotal());
                                allOrderItemModel.setItem(model.getItem());
                                allOrderItemModel.setOrderDateTime(model.getOrderDateTime());
                                allOrderItemModel.setDeliveryDateTime(model.getDeliveryDateTime());
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
                                allOrderItemModel.setOrderItemId(orderItemModel.getOrderItemId());
                                allOrderItemModel.setRecipeId(orderItemModel.getRecipeId());
                                allOrderItemModel.setRecipeName(orderItemModel.getRecipeName());
                                allOrderItemModel.setRecipeImageId(orderItemModel.getRecipeImageId());
                                allOrderItemModel.setRecipeImageUrl(orderItemModel.getRecipeImageUrl());
                                allOrderItemModel.setPrice(orderItemModel.getPrice());
                                allOrderItemModel.setQuantity(orderItemModel.getQuantity());

                                mydatabase.addAllOrder(allOrderItemModel);
                            }

                            setText();
                            getData();
                        }
                    } else {

                        swipeRefresh.setRefreshing(false);
                        String error = Log.getStackTraceString(task.getException());
                        globalclass.log(tag, "getOrderItemList: " + error);
                        globalclass.sendResponseErrorLog(tag,"getOrderItemList: ",error, finalParameter);
                        globalclass.toast_long("Unable to get order list, please try after sometime!");
                    }
                }
            });
        } catch (Exception e) {

            swipeRefresh.setRefreshing(false);
            String error = Log.getStackTraceString(e);
            globalclass.log(tag, "getOrderItemList: " + error);
            globalclass.sendResponseErrorLog(tag,"getOrderItemList: ",error,parameter);
            globalclass.toast_long("Unable to get order list, please try after sometime!");
        }
    }

    void setAdapter() {

        adapter = new AdminOrderItemAdapter(activity, arrayList, new AdminOrderItemOnClick() {
            @Override
            public void viewDetail(int position, allOrderItemDetail model) {
                Intent intent = new Intent(activity, UpdateRecipe.class);
                intent.putExtra("recipeId", model.getRecipeId());
                startActivity(intent);
            }
        });

        recyclerView.setLayoutManager(new LinearLayoutManager(activity));
        recyclerView.setAdapter(adapter);
    }

    void fillOrderStatusList() {

        orderStatusList.clear();
        statusList.clear();
        orderStatusList = mydatabase.getOrderStatusList();
        if (orderStatusList!=null && !orderStatusList.isEmpty()) {

            showOnlyNextOrderStatus();

            for (int i = model.getOrderStep(); i < orderStatusList.size(); i++) {
                statusList.add(orderStatusList.get(i).getOrderStatus().toUpperCase());

                if (orderStatusList.get(i).getOrderStatus().equalsIgnoreCase(model.getOrderStatus())) {
                    orderStepNumber = orderStatusList.get(i).getStepNumber();
                }
            }
        } else {
            globalclass.snackit(activity, "No order status found!");
        }
    }

    void showOnlyNextOrderStatus() {

        //todo removing middle orders
        for (int i = model.getOrderStep()+1; i < orderStatusList.size()-1; i++) {
            orderStatusList.remove(i);
            i--;
        }
    }

    void getSelectedOrderStepNumber() {
        for(int i=0;i<orderStatusList.size();i++) {
            if(tvOrderStatus.getText().toString().trim().toLowerCase().
                    equalsIgnoreCase(orderStatusList.get(i).getOrderStatus())) {

                orderStepNumber = orderStatusList.get(i).getStepNumber();
                return;
            }
        }
    }

    void showOrderStatusDialogue() {

        if (orderStatusList.isEmpty()) {
            globalclass.snackit(activity, "No order status found!");
            return;
        }

        CharSequence[] charSequence = statusList.toArray(new CharSequence[statusList.size()]);
        MaterialAlertDialogBuilder materialAlertDialogBuilder =
                new MaterialAlertDialogBuilder(activity, R.style.RoundShapeTheme)
                        .setTitle("Select Order Status")
                        .setSingleChoiceItems(charSequence, selectedStatusPos, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int pos) {
                                selectedStatusPos = pos;
                                tvOrderStatus.setText(statusList.get(selectedStatusPos));
                                getSelectedOrderStepNumber();
                                changeStatusbt.setEnabled(true);
                                dialogInterface.dismiss();
                            }
                        });
        materialAlertDialogBuilder.show();
    }

    void changeOrderStatus() {
        try {
            showprogress("Changing order status", "Please wait...");

            final DocumentReference placeOrderDocRef = globalclass.firebaseInstance()
                    .collection(Globalclass.placeOrderColl)
                    .document(getOrderId);

            globalclass.firebaseInstance().runTransaction(new Transaction.Function<Integer>() {
                public Integer apply(Transaction transaction) throws FirebaseFirestoreException {

                    DocumentSnapshot placeOrderDocReference = transaction.get(placeOrderDocRef);
                    if(placeOrderDocReference.exists()) {

                        orderSummaryDetail model = placeOrderDocReference.toObject(orderSummaryDetail.class);

                        if(model.getOrderStep() == 5) {

                            return model.getOrderStep();
                        }

                        if(model.getOrderStep() == orderStepNumber) {
                            //todo same order status is selected
                            globalclass.log(tag,"Same order status is selected!");
                            return -1;
                        }

                        Gson gson = new GsonBuilder().create();
                        String json = gson.toJson(model);
                        Map<String, Object> map = new Gson().fromJson(json, Map.class);
                        map.put("orderDateTime", model.getOrderDateTime());
                        if(orderStepNumber == 4) {
                            map.put("deliveryDateTime", FieldValue.serverTimestamp());
                        }
                        else {
                            map.put("deliveryDateTime", model.getDeliveryDateTime());
                        }
                        map.put("cancelReason","");
                        map.put("orderStatus",mydatabase.getParticularOrderStatus(orderStepNumber).getOrderStatus());
                        map.put("orderStep", mydatabase.getParticularOrderStatus(orderStepNumber).getStepNumber());
                        transaction.update(placeOrderDocRef,map);

                        CollectionReference orderStatusLogColl = globalclass.firebaseInstance().collection(Globalclass.orderStatusLogColl);
                        String logId = orderStatusLogColl.document().getId();
                        orderStatusLogDetail orderStatusLogModel = new orderStatusLogDetail();
                        orderStatusLogModel.setLogId(logId);
                        orderStatusLogModel.setOrderId(getOrderId);
                        orderStatusLogModel.setEntryBy("ADMIN");
                        orderStatusLogModel.setUserId(globalclass.getStringData("adminId"));
                        orderStatusLogModel.setUserName(globalclass.getStringData("adminName"));
                        orderStatusLogModel.setOrderStep(mydatabase.getParticularOrderStatus(orderStepNumber).getStepNumber());
                        orderStatusLogModel.setOrderStatus(mydatabase.getParticularOrderStatus(orderStepNumber).getOrderStatus());
                        DocumentReference orderItemDocReference = orderStatusLogColl.document(logId);
                        transaction.set(orderItemDocReference, orderStatusLogModel);

                        return orderStepNumber;
                    }
                    else {

                        return -1;
                    }
                }
            }).addOnSuccessListener(activity, new OnSuccessListener<Integer>() {
                @Override
                public void onSuccess(Integer integer) {

                    hideprogress();
                    orderStatusChanged = true;
                    if(integer == 5) {

                        globalclass.snackit(activity,"Order has been already "+
                                mydatabase.getParticularOrderStatus(integer).getOrderStatus().toUpperCase());
                        getPlaceOrderDetail();
                    }
                    else if(integer < 0) {
                        globalclass.snackit(activity,"Order has been already "+
                                mydatabase.getParticularOrderStatus(orderStepNumber).getOrderStatus().toUpperCase()+", please change the status! ");
                    }
                    else {

                        changeOrderDetailLocally(mydatabase.getParticularOrderStatus(integer).getStepNumber(), "");
                        setText();
                        getData();

                        globalclass.snackit(activity,
                                mydatabase.getParticularOrderStatus(integer).getOrderStatus().toUpperCase()+
                                        " successfully!");
                    }

                    selectedStatusPos = -1;
                }
            }).addOnFailureListener(activity, new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {

                    hideprogress();
                    String error = Log.getStackTraceString(e);
                    globalclass.log(tag, "changeOrderStatus: " + error);
                    globalclass.sendErrorLog(tag,"changeOrderStatus: ",error);
                    globalclass.toast_long("Unable to change order status, please try after sometime!");
                }
            });
        }
        catch (Exception e) {

            hideprogress();
            String error = Log.getStackTraceString(e);
            globalclass.log(tag, "changeOrderStatus: " + error);
            globalclass.sendErrorLog(tag,"changeOrderStatus: ",error);
            globalclass.toast_long("Unable to change order status, please try after sometime!");
        }
    }

    void changeOrderDetailLocally(int stepNumber, String cancelReason) {

        try {
            ArrayList<allOrderItemDetail> localList = mydatabase.getAllOrderItemList(getOrderId);
            if(localList !=null && !localList.isEmpty()) {
                for(int i=0;i<localList.size();i++) {
                    allOrderItemDetail model = localList.get(i);
                    model.setDeliveryDateTime(globalclass.getDate());
                    model.setOrderStatus(mydatabase.getParticularOrderStatus(orderStepNumber).getOrderStatus());
                    model.setOrderStep(orderStepNumber);
                    if(stepNumber == 5 && !cancelReason.equalsIgnoreCase("")) {
                        model.setCancelReason(cancelReason);
                    }
                    else {
                        model.setCancelReason("");
                    }
                    mydatabase.addAllOrder(model);
                }
            }
        }
        catch (Exception e) {
            String error = Log.getStackTraceString(e);
            globalclass.log(tag, "changeOrderDetailLocally: " + error);
            globalclass.sendErrorLog(tag,"changeOrderDetailLocally",error);
            globalclass.toast_long("Unable to change order detail locally, please refresh date to see changes!");
        }
    }

    void changeInventoryStockLocally(ArrayList<stockDetail> stockList) {
        try {
            for(int i=0;i<stockList.size();i++) {
                stockDetail model = stockList.get(i);
                inventoryDetail inventoryModel = mydatabase.getParticularInventory(model.getInventoryId());
                double addQuantity = inventoryModel.getQuantity() + model.getQuantity();
                inventoryModel.setQuantity(addQuantity);
                inventoryModel.setAdminId(globalclass.getStringData("adminId"));
                inventoryModel.setAdminName(globalclass.getStringData("adminName"));
                mydatabase.addInventory(inventoryModel);
            }
        }
        catch (Exception e) {
            String error = Log.getStackTraceString(e);
            globalclass.log(tag, "changeInventoryStockLocally: " + error);
            globalclass.sendErrorLog(tag,"changeInventoryStockLocally",error);
            globalclass.toast_long("Unable to change inventory stock locally, please refresh date to see changes!");
        }
    }

    void showCancelOrderDialogue() {

        AlertDialog dialog = new AlertDialog.Builder(activity,R.style.CustomAlertDialog)
                .setTitle("Add cancel reason")
                .setPositiveButton("Cancel Order",null)
                .setNeutralButton("Cancel",null)
                .setView(R.layout.cancelorderbs)
                .setCancelable(false)
                .show();

        final TextInputLayout cancelReasontf = dialog.findViewById(R.id.cancelReasontf);
        final EditText cancelReason = dialog.findViewById(R.id.cancelReason);

        Button positiveButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
        positiveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                globalclass.hidekeyboard_dialogue(dialog);
                if (cancelReason.getText().toString().length() == 0 ||
                        !cancelReason.getText().toString().matches(globalclass.alphaNumericRegexOne())) {
                    cancelReasontf.setError("Invalid cancel reason");
                    cancelReason.requestFocus();
                } else {

                    new MaterialAlertDialogBuilder(activity, R.style.RoundShapeTheme)
                            .setTitle("Sure")
                            .setMessage("Are you sure you want to cancel order?")
                            .setCancelable(false)
                            .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                    getStockList(cancelReason.getText().toString().trim()
                                            .replaceAll(System.lineSeparator(), " "));
                                }
                            })
                            .setNeutralButton("Cancel", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    dialogInterface.dismiss();
                                }
                            })
                            .show();

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
    }

    void getStockList(String cancelReason) {

        String parameter = "";

        try {
            showprogress("Hold on","Please wait...");

            ArrayList<stockDetail> stockList = new ArrayList<>();

            CollectionReference stockColl = globalclass.firebaseInstance().collection(Globalclass.stockColl);
            Query query = stockColl
                    .whereEqualTo("inOutId",getOrderId);

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
                            for(DocumentSnapshot documents : task.getResult()) {
                                stockDetail model = documents.toObject(stockDetail.class);
                                stockList.add(model);
                            }

                            if(stockList !=null && !stockList.isEmpty()) {
                                cancelOrder(stockList,cancelReason);
                            }
                        }
                        else {
                            String error = "inOutId/orderId not available in stockColl!";
                            globalclass.log(tag,"getStockList: "+error);
                            globalclass.sendResponseErrorLog(tag,"getStockList: ",error, finalParameter);
                            globalclass.toast_long("Unable to cancel order, please try after sometime!");
                        }
                    }
                    else {
                        String error = Log.getStackTraceString(task.getException());
                        globalclass.log(tag,"getStockList: "+error);
                        globalclass.sendResponseErrorLog(tag,"getStockList: ",error,finalParameter);
                        globalclass.toast_long("Unable to cancel order, please try after sometime!");
                    }
                }
            });
        }
        catch (Exception e) {

            hideprogress();
            String error = Log.getStackTraceString(e);
            globalclass.log(tag,"getStockList: "+error);
            globalclass.sendResponseErrorLog(tag,"getStockList: ",error,parameter);
            globalclass.toast_long("Unable to cancel order, please try after sometime!");
        }
    }

    void cancelOrder(ArrayList<stockDetail> stockList, String cancelReason) {

        String parameter = "";

        try {
            showprogress("Cancelling order","Please wait...");

            final DocumentReference placeOrderDocRef = globalclass.firebaseInstance()
                    .collection(Globalclass.placeOrderColl)
                    .document(getOrderId);

            Gson gson = new Gson();
            if(model != null) {
                parameter = gson.toJson(model);
            }
            globalclass.log(tag,"parameter: "+parameter);

            String finalParameter = parameter;

            globalclass.firebaseInstance().runTransaction(new Transaction.Function<Integer>() {
                public Integer apply(Transaction transaction) throws FirebaseFirestoreException {

                    DocumentSnapshot placeOrderDocReference = transaction.get(placeOrderDocRef);
                    if(placeOrderDocReference.exists()) {

                        orderSummaryDetail model = placeOrderDocReference.toObject(orderSummaryDetail.class);

                        if(model.getOrderStep() == orderStepNumber) {
                            //todo same order status is selected
                            globalclass.log(tag,"Same order status is selected!");
                            return -1;
                        }

                        if(model.getOrderStep() == 5) {
                            return 0;
                        }

                        Gson gson = new GsonBuilder().create();
                        String json = gson.toJson(model);
                        Map<String, Object> map = new Gson().fromJson(json, Map.class);
                        map.put("orderDateTime", model.getOrderDateTime());
                        map.put("deliveryDateTime", model.getDeliveryDateTime());
                        map.put("cancelReason",cancelReason);
                        map.put("orderStatus",mydatabase.getParticularOrderStatus(orderStepNumber).getOrderStatus());
                        map.put("orderStep", mydatabase.getParticularOrderStatus(orderStepNumber).getStepNumber());
                        transaction.update(placeOrderDocRef,map);

                        for(int i=0;i<stockList.size();i++) {
                            String stockId = stockList.get(i).getStockId();
                            String inventoryId = stockList.get(i).getInventoryId();
                            double quantity = stockList.get(i).getQuantity();

                            DocumentReference stockDocRef = globalclass.firebaseInstance()
                                    .collection(Globalclass.stockColl).document(stockId);
                            transaction.delete(stockDocRef);

                            DocumentReference inventoryReference = globalclass.firebaseInstance()
                                    .collection(Globalclass.inventoryColl).document(inventoryId);
                            Map<String,Object> invntoryMap = new HashMap<>();
                            invntoryMap.put("quantity", FieldValue.increment(+quantity));
                            transaction.update(inventoryReference, invntoryMap);
                        }

                        CollectionReference orderStatusLogColl = globalclass.firebaseInstance().collection(Globalclass.orderStatusLogColl);
                        String logId = orderStatusLogColl.document().getId();
                        orderStatusLogDetail orderStatusLogModel = new orderStatusLogDetail();
                        orderStatusLogModel.setLogId(logId);
                        orderStatusLogModel.setOrderId(getOrderId);
                        orderStatusLogModel.setEntryBy("ADMIN");
                        orderStatusLogModel.setUserId(globalclass.getStringData("adminId"));
                        orderStatusLogModel.setUserName(globalclass.getStringData("adminName"));
                        orderStatusLogModel.setOrderStep(mydatabase.getParticularOrderStatus(orderStepNumber).getStepNumber());
                        orderStatusLogModel.setOrderStatus(mydatabase.getParticularOrderStatus(orderStepNumber).getOrderStatus());
                        DocumentReference orderItemDocReference = orderStatusLogColl.document(logId);
                        transaction.set(orderItemDocReference, orderStatusLogModel);

                        return mydatabase.getParticularOrderStatus(orderStepNumber).getStepNumber();
                    }
                    else {

                        return -1;
                    }
                }
            }).addOnSuccessListener(activity, new OnSuccessListener<Integer>() {
                @Override
                public void onSuccess(Integer integer) {

                    hideprogress();
                    orderStatusChanged = true;
                    if(integer < 0) {
                        globalclass.snackit(activity,"Order has been already "+
                                mydatabase.getParticularOrderStatus(orderStepNumber).getOrderStatus().toUpperCase()+", please change the status! ");
                    }
                    else if(integer == 5) {
                        changeOrderDetailLocally(mydatabase.getParticularOrderStatus(integer).getStepNumber(),cancelReason);
                        changeInventoryStockLocally(stockList);

                        setText();
                        getData();

                        globalclass.snackit(activity,
                                mydatabase.getParticularOrderStatus(integer).getOrderStatus().toUpperCase()+
                                        " successfully!");
                    }
                    else {

                        globalclass.snackit(activity,"Order has been already "+
                                mydatabase.getParticularOrderStatus(integer).getOrderStatus().toUpperCase());
                        getPlaceOrderDetail();
                    }

                    selectedStatusPos = -1;
                }
            }).addOnFailureListener(activity, new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {

                    hideprogress();
                    String error = Log.getStackTraceString(e);
                    globalclass.log(tag, "cancelOrder: " + error);
                    globalclass.sendResponseErrorLog(tag,"cancelOrder: ",error, finalParameter);
                    globalclass.toast_long("Unable to cancel order, please try after sometime!");
                }
            });
        }
        catch (Exception e) {

            String error = Log.getStackTraceString(e);
            globalclass.log(tag, "cancelOrder: " + error);
            globalclass.sendResponseErrorLog(tag,"cancelOrder: ",error, parameter);
            globalclass.toast_long("Unable to cancel order, please try after sometime!");
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_adminorderdetail, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id){
            case R.id.menuorderstatuslog:

                if (!globalclass.isInternetPresent()) {
                    globalclass.toast_long(getResources().getString(R.string.noInternetConnection));
                    return true;
                }

                startActivity(new Intent(activity, OrderStatusLog.class)
                .putExtra("orderId",getOrderId));

                return true;
            default:
                return super.onOptionsItemSelected(item);
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

    public BroadcastReceiver OrderReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            try
            {
                globalclass.log(tag,"OrderReceiver: onReceive");
                setText();
                getData();
            }
            catch (Exception e)
            {
                String error = Log.getStackTraceString(e);
                globalclass.log(tag,"OrderReceiver: "+error);
                globalclass.sendErrorLog(tag,"OrderReceiver: ",error);
                globalclass.toast_long("Error in refreshing data, please swipe down to refresh data!");
            }
        }
    };

    @Override
    protected void onResume() {
        super.onResume();

        LocalBroadcastManager.getInstance(activity).registerReceiver(
                OrderReceiver, new IntentFilter(Globalclass.OrderReceiver));

    }

    @Override
    protected void onPause() {
        super.onPause();

        LocalBroadcastManager.getInstance(activity).unregisterReceiver(OrderReceiver);
    }

    @Override
    public void onBackPressed() {
        if(orderStatusChanged) {
            Intent intent = new Intent();
            setResult(RESULT_OK, intent);
            finish();
        }
        else {
            super.onBackPressed();
        }
    }
}