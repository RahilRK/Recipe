package com.hksofttronix.khansama.OrderDetail;

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
import com.hksofttronix.khansama.Globalclass;
import com.hksofttronix.khansama.Instructions.Instructions;
import com.hksofttronix.khansama.Models.allOrderItemDetail;
import com.hksofttronix.khansama.Models.inventoryDetail;
import com.hksofttronix.khansama.Models.orderItemDetail;
import com.hksofttronix.khansama.Models.orderStatusLogDetail;
import com.hksofttronix.khansama.Models.orderSummaryDetail;
import com.hksofttronix.khansama.Models.placeOrderDetail;
import com.hksofttronix.khansama.Models.stockDetail;
import com.hksofttronix.khansama.Mydatabase;
import com.hksofttronix.khansama.R;
import com.hksofttronix.khansama.RecipeDetail.RecipeDetail;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class OrderDetail extends AppCompatActivity {

    String tag = this.getClass().getSimpleName();
    AppCompatActivity activity = OrderDetail.this;

    Globalclass globalclass;
    Mydatabase mydatabase;

    SwipeRefreshLayout swipeRefresh;
    RecyclerView recyclerView;
    TextView orderId,orderDateTime,deliveryDateTime;
    RelativeLayout deliveryLayout;
    TextView name,mobileNumber,address, nearby, city, state, pincode;
    TextView item,recipeCharges,packagingCharges,total;
    LinearLayout cancelReasonLayout;
    TextView cancelReason;
    MaterialButton orderStatus,cancelOrderbt;

    String getOrderId = "";
    placeOrderDetail model = null;

    ArrayList<placeOrderDetail> arrayList = new ArrayList<>();
    OrderItemAdapter adapter;

    boolean orderStatusChanged = false;

    ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_detail);

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
        cancelOrderbt = findViewById(R.id.cancelOrderbt);
    }

    void onClick() {

        swipeRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {

                if(!globalclass.isInternetPresent()) {
                    swipeRefresh.setRefreshing(false);
                    globalclass.toast_long(getResources().getString(R.string.noInternetConnection));
                    return;
                }

                getPlaceOrderDetail();
            }
        });

        cancelOrderbt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (!globalclass.isInternetPresent()) {
                    globalclass.toast_long(getResources().getString(R.string.noInternetConnection));
                    return;
                }

                showCancelOrderDialogue();
            }
        });
    }

    void setToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_close);
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
        model = mydatabase.getParticularOrderDetail(getOrderId);

        if(model != null) {
            orderStatus.setText(globalclass.firstLetterCapital(model.getOrderStatus()));
            orderId.setText(model.getOrderId());
            orderDateTime.setText(globalclass.dateToString(model.getOrderDateTime(),"dd MMM yyyy hh:mm aa"));
            if(model.getOrderStep() == 4) {
                deliveryLayout.setVisibility(View.VISIBLE);
                deliveryDateTime.setText(globalclass.dateToString(model.getDeliveryDateTime(),"dd MMM yyyy hh:mm aa"));
            }
            else {
                deliveryLayout.setVisibility(View.GONE);
            }

            name.setText(model.getName());
            mobileNumber.setText("+91"+model.getMobileNumber());
            address.setText(model.getAddress());
            nearby.setText(model.getNearBy());
            city.setText(globalclass.firstLetterCapital(model.getCity()));
            state.setText(globalclass.firstLetterCapital(model.getState()));
            pincode.setText(model.getPincode());

            item.setText("" + model.getItem());
            recipeCharges.setText("₹ " + model.getRecipeCharges());
            packagingCharges.setText("₹ " + model.getPackagingCharges());
            total.setText("₹ " + model.getTotal());

            if(model.getOrderStep() == 1 || model.getOrderStep() == 2) {
                cancelOrderbt.setVisibility(View.VISIBLE);
            }
            else {
                cancelOrderbt.setVisibility(View.GONE);
            }

            if(model.getOrderStep() == 5) {
                cancelReasonLayout.setVisibility(View.VISIBLE);
                cancelReason.setText(model.getCancelReason());
            }
            else {
                cancelReasonLayout.setVisibility(View.GONE);
            }
        }
        else {
//            globalclass.toast_long("Unable to show order detail, please try after sometime!");
            globalclass.log(tag,"Unable to show order detail, please try after sometime!");
        }
    }

    void getData() {
        arrayList.clear();
        arrayList = mydatabase.getOrderItemList(getOrderId);
        if(arrayList != null && !arrayList.isEmpty()) {
            setAdapter();
        }
        else {
            getPlaceOrderDetail();
        }
    }

    void getPlaceOrderDetail() {

        String parameter = "";

        try {
            CollectionReference placeOrderColl = globalclass.firebaseInstance().collection(Globalclass.placeOrderColl);
            Query query = placeOrderColl
                    .whereEqualTo("userId",globalclass.getStringData("id"))
                    .whereEqualTo("orderId",getOrderId);

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

                            mydatabase.deleteData(mydatabase.placeOrder,"orderId",getOrderId);
                            for(DocumentSnapshot documents : task.getResult()) {
                                orderSummaryDetail model = documents.toObject(orderSummaryDetail.class);
                                getOrderItemList(model);
                            }
                        }
                    }
                    else {

                        mydatabase.deleteData(mydatabase.placeOrder,"orderId",getOrderId);
                        String error = Log.getStackTraceString(task.getException());
                        globalclass.log(tag,"getPlaceOrderDetail: "+error);
                        globalclass.toast_long("Unable to get order list, please try after sometime!");
                        globalclass.sendResponseErrorLog(tag,"getPlaceOrderDetail: ",error, finalParameter);
                    }
                }
            });
        }
        catch (Exception e) {

            String error = Log.getStackTraceString(e);
            globalclass.log(tag,"getPlaceOrderDetail: "+error);
            globalclass.toast_long("Unable to get order list, please try after sometime!");
            globalclass.sendResponseErrorLog(tag,"getPlaceOrderDetail: ",error, parameter);
        }
    }

    void getOrderItemList(orderSummaryDetail model) {

        String parameter = "";

        try {
            swipeRefresh.setRefreshing(true);

            CollectionReference orderItemColl = globalclass.firebaseInstance().collection(Globalclass.orderItemColl);
            Query query = orderItemColl
                    .whereEqualTo("orderId",model.getOrderId());

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

                    if(task.isSuccessful()) {
                        if(!task.getResult().isEmpty()) {

                            for(DocumentSnapshot documents : task.getResult()) {
                                orderItemDetail orderItemModel = documents.toObject(orderItemDetail.class);

                                placeOrderDetail placeOrderModel = new placeOrderDetail();
                                placeOrderModel.setOrderId(model.getOrderId());
                                placeOrderModel.setUserId(model.getUserId());
                                placeOrderModel.setUserMobileNumber(model.getUserMobileNumber());
                                placeOrderModel.setCancelReason(model.getCancelReason());
                                placeOrderModel.setOrderStatus(model.getOrderStatus());
                                placeOrderModel.setOrderStep(model.getOrderStep());
                                placeOrderModel.setRecipeCharges(model.getRecipeCharges());
                                placeOrderModel.setPackagingCharges(model.getPackagingCharges());
                                placeOrderModel.setTotal(model.getTotal());
                                placeOrderModel.setItem(model.getItem());
                                placeOrderModel.setOrderDateTime(model.getOrderDateTime());
                                placeOrderModel.setDeliveryDateTime(model.getDeliveryDateTime());
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
                                placeOrderModel.setOrderItemId(orderItemModel.getOrderItemId());
                                placeOrderModel.setRecipeId(orderItemModel.getRecipeId());
                                placeOrderModel.setRecipeName(orderItemModel.getRecipeName());
                                placeOrderModel.setRecipeImageId(orderItemModel.getRecipeImageId());
                                placeOrderModel.setRecipeImageUrl(orderItemModel.getRecipeImageUrl());
                                placeOrderModel.setPrice(orderItemModel.getPrice());
                                placeOrderModel.setQuantity(orderItemModel.getQuantity());

                                mydatabase.addPlaceOrder(placeOrderModel);
                            }

                            setText();
                            getData();
                        }
                    }
                    else {

                        mydatabase.deleteData(mydatabase.placeOrder,"orderId",getOrderId);
                        swipeRefresh.setRefreshing(false);
                        String error = Log.getStackTraceString(task.getException());
                        globalclass.log(tag,"getOrderItemList: "+error);
                        globalclass.toast_long("Unable to get order list, please try after sometime!");
                        globalclass.sendResponseErrorLog(tag,"getOrderItemList: ",error, finalParameter);
                    }
                }
            });
        }
        catch (Exception e) {

            swipeRefresh.setRefreshing(false);
            String error = Log.getStackTraceString(e);
            globalclass.log(tag,"getOrderItemList: "+error);
            globalclass.toast_long("Unable to get order list, please try after sometime!");
            globalclass.sendResponseErrorLog(tag,"getOrderItemList: ",error, parameter);
        }
    }

    void setAdapter() {

        adapter = new OrderItemAdapter(activity, arrayList, model.getOrderStep(),new OrderItemOnClick() {
            @Override
            public void viewDetail(int position, placeOrderDetail model) {

                startActivity(new Intent(activity, RecipeDetail.class)
                        .putExtra("recipeId",model.getRecipeId()));
            }

            @Override
            public void viewRecipeInstruction(int position, placeOrderDetail model) {
                startActivity(new Intent(activity, Instructions.class)
                        .putExtra("recipeId",model.getRecipeId()));
            }
        });

        recyclerView.setLayoutManager(new LinearLayoutManager(activity));
        recyclerView.setAdapter(adapter);
    }

    void getStockList(String cancelReason) {

        String parameter = "";

        try {
            showprogress("Cancelling order","Please wait...");

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

                    if(task.isSuccessful()) {
                        if(!task.getResult().isEmpty()) {
                            for(DocumentSnapshot documents : task.getResult()) {
                                stockDetail model = documents.toObject(stockDetail.class);
                                stockList.add(model);
                            }

                            if(stockList !=null && !stockList.isEmpty()) {
                                cancelOrder(stockList,cancelReason);
                            }
                            else {
                                hideprogress();
                                String error = "stockList is null!";
                                globalclass.log(tag,"getStockList: "+error);
                                globalclass.toast_long("Unable to cancel order, please try after sometime!");
                                globalclass.sendErrorLog(tag,"getStockList",error);
                            }
                        }
                        else {

                            hideprogress();
                            String error = "inOutId/orderId not available in stockColl!";
                            globalclass.log(tag,"getStockList: "+error);
                            globalclass.toast_long("Unable to cancel order, please try after sometime!");
                            globalclass.sendErrorLog(tag,"getStockList",error);
                        }
                    }
                    else {

                        hideprogress();
                        String error = Log.getStackTraceString(task.getException());
                        globalclass.log(tag,"getStockList: "+error);
                        globalclass.toast_long("Unable to cancel order, please try after sometime!");
                        globalclass.sendResponseErrorLog(tag,"getStockList: ",error, finalParameter);
                    }
                }
            });
        }
        catch (Exception e) {

            hideprogress();
            String error = Log.getStackTraceString(e);
            globalclass.log(tag,"getStockList: "+error);
            globalclass.toast_long("Unable to cancel order, please try after sometime!");
            globalclass.sendResponseErrorLog(tag,"getStockList: ",error, parameter);
        }
    }

    void cancelOrder(ArrayList<stockDetail> stockList, String cancelReasonText) {

        String parameter = "";

        try {
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

                        if(model.getOrderStep() == 3 ||
                                model.getOrderStep() == 4 ||
                                model.getOrderStep() == 5) {

                            return model.getOrderStep();
                        }

                        Gson gson = new GsonBuilder().create();
                        String json = gson.toJson(model);
                        Map<String, Object> map = new Gson().fromJson(json, Map.class);
                        map.put("orderDateTime", model.getOrderDateTime());
                        map.put("deliveryDateTime", model.getDeliveryDateTime());
                        map.put("cancelReason",cancelReasonText);
                        map.put("orderStatus","cancelled");
                        map.put("orderStep", 5);
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
                        orderStatusLogModel.setEntryBy("USER");
                        orderStatusLogModel.setUserId(globalclass.getStringData("id"));
                        orderStatusLogModel.setUserName(globalclass.getStringData("name"));
                        orderStatusLogModel.setOrderStep(mydatabase.getParticularOrderStatus(5).getStepNumber());
                        orderStatusLogModel.setOrderStatus(mydatabase.getParticularOrderStatus(5).getOrderStatus());
                        DocumentReference orderItemDocReference = orderStatusLogColl.document(logId);
                        transaction.set(orderItemDocReference, orderStatusLogModel);

                        return 5;
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
                        addDataToPlaceOrder(cancelReasonText);
                        if (globalclass.getStringData("loginType").equalsIgnoreCase("Admin")) {
                            addDataToAllOrder(cancelReasonText);
                            changeInventoryStockLocally(stockList);
                        }

                        setText();
                        getData();

                        globalclass.snackit(activity,
                                mydatabase.getParticularOrderStatus(5).getOrderStatus().toUpperCase()+
                                        " successfully!");
                    }
                    else {

                        globalclass.snackit(activity,"Unable to cancel order!");
                        getPlaceOrderDetail();
                    }
                }
            }).addOnFailureListener(activity, new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {

                    hideprogress();
                    String error = Log.getStackTraceString(e);
                    globalclass.log(tag, "cancelOrder: " + error);
                    globalclass.toast_long("Unable to cancel order, please try after sometime!");
                    globalclass.sendResponseErrorLog(tag,"cancelOrder: ",error, finalParameter);
                }
            });
        }
        catch (Exception e) {

            String error = Log.getStackTraceString(e);
            globalclass.log(tag, "cancelOrder: " + error);
            globalclass.toast_long("Unable to cancel order, please try after sometime!");
            globalclass.sendResponseErrorLog(tag,"cancelOrder: ",error, parameter);
        }
    }

    void addDataToPlaceOrder(String cancelReasonText) {
        try {
            ArrayList<placeOrderDetail> orderList = mydatabase.getOrderItemList(getOrderId);
            if(orderList !=null && !orderList.isEmpty()) {
                for(int i=0;i<orderList.size();i++) {
                    placeOrderDetail model = orderList.get(i);
                    model.setDeliveryDateTime(globalclass.getDate());
                    model.setOrderStatus(mydatabase.getParticularOrderStatus(5).getOrderStatus());
                    model.setOrderStep(mydatabase.getParticularOrderStatus(5).getStepNumber());
                    model.setCancelReason(cancelReasonText);
                    mydatabase.addPlaceOrder(model);
                }
            }
            else {
                String error = "orderList is null!";
                globalclass.log(tag, "addDataToPlaceOrder: " + error);
                globalclass.toast_long("Unable to change data, please refresh date to see changes!");
                globalclass.sendErrorLog(tag,"changeDateLocally",error);
                onBackPressed();
            }
        }
        catch (Exception e) {
            String error = Log.getStackTraceString(e);
            globalclass.log(tag, "changeDateLocally: " + error);
            globalclass.toast_long("Unable to change data, please refresh date to see changes!");
            globalclass.sendErrorLog(tag,"changeDateLocally",error);
            onBackPressed();
        }
    }

    void addDataToAllOrder(String cancelReasonText) {
        try {
            ArrayList<allOrderItemDetail> allOrderList = mydatabase.getAllOrderItemList(getOrderId);
            if(allOrderList !=null && !allOrderList.isEmpty()) {
                for(int i=0;i<allOrderList.size();i++) {
                    allOrderItemDetail model = allOrderList.get(i);
                    model.setDeliveryDateTime(globalclass.getDate());
                    model.setOrderStatus(mydatabase.getParticularOrderStatus(5).getOrderStatus());
                    model.setOrderStep(mydatabase.getParticularOrderStatus(5).getStepNumber());
                    model.setCancelReason(cancelReasonText);
                    mydatabase.addAllOrder(model);
                }
            }
            else {
                String error = "allOrderList is null!";
                globalclass.log(tag, "addDataToAllOrder: " + error);
                globalclass.toast_long("Unable to change data, please refresh date to see changes!");
                globalclass.sendErrorLog(tag,"addDataToAllOrder",error);
                onBackPressed();
            }
        }
        catch (Exception e) {
            String error = Log.getStackTraceString(e);
            globalclass.log(tag, "changeDateLocally: " + error);
            globalclass.toast_long("Unable to change data, please refresh date to see changes!");
            globalclass.sendErrorLog(tag,"addDataToAllOrder",error);
            onBackPressed();
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
            globalclass.toast_long("Unable to change inventory stock locally, please refresh date to see changes!");
            globalclass.sendErrorLog(tag,"changeInventoryStockLocally",error);
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
                globalclass.toast_long("Error in refreshing data, please swipe down to refresh data!");
                globalclass.sendErrorLog(tag,"OrderReceiver",error);
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