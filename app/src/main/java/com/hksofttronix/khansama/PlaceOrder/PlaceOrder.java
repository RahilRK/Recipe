package com.hksofttronix.khansama.PlaceOrder;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.RelativeLayout;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.hksofttronix.khansama.Globalclass;
import com.hksofttronix.khansama.Models.orderItemDetail;
import com.hksofttronix.khansama.Models.orderSummaryDetail;
import com.hksofttronix.khansama.Models.placeOrderDetail;
import com.hksofttronix.khansama.Mydatabase;
import com.hksofttronix.khansama.OrderDetail.OrderDetail;
import com.hksofttronix.khansama.R;

import java.util.ArrayList;

public class PlaceOrder extends AppCompatActivity {

    String tag = this.getClass().getSimpleName();
    AppCompatActivity activity = PlaceOrder.this;

    Globalclass globalclass;
    Mydatabase mydatabase;
//    ListenerRegistration orderListListener;
//    ListenerRegistration orderItemListListener;

    SwipeRefreshLayout swipeRefresh;
    RecyclerView recyclerView;
    RelativeLayout nodatafoundlo;

    ArrayList<placeOrderDetail> arrayList = new ArrayList<>();
    PlaceOrderAdapter adapter;
    LinearLayoutManager linearLayoutManager;

    int ORDER_STATUS_CHANGED = 111;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_place_order);

        setToolbar();
        init();
        binding();
        onClick();
        getData();
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
        toolbar.setTitle(R.string.your_orders);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                onBackPressed();
            }
        });
    }

    void binding() {
        swipeRefresh = findViewById(R.id.swipeRefresh);
        recyclerView = findViewById(R.id.recyclerView);
        nodatafoundlo = findViewById(R.id.nodatafoundlo);
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

                getPlaceOrderList();
            }
        });

        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
            }

            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

                int visibleItemCount = linearLayoutManager.getChildCount();
                int totalItemCount = linearLayoutManager.getItemCount();
                int pastVisibleItems = linearLayoutManager.findFirstVisibleItemPosition();

                if (dy >0) {
                    // Scroll Down
                }
                else if (dy <0) {

                    // Scroll Up
                }
            }
        });
    }

    void getData() {
        arrayList.clear();
        arrayList = mydatabase.getPlaceOrderList();
        if(!arrayList.isEmpty()) {
            setAdapter();
        }
        else {
            getPlaceOrderList();
        }
    }

    void getPlaceOrderList() {

        try {
            CollectionReference placeOrderColl = globalclass.firebaseInstance().collection(Globalclass.placeOrderColl);
            Query query = placeOrderColl
                    .whereEqualTo("userId",globalclass.getStringData("id"));
            query.get().addOnCompleteListener(activity, new OnCompleteListener<QuerySnapshot>() {
                @Override
                public void onComplete(@NonNull Task<QuerySnapshot> task) {

                    swipeRefresh.setRefreshing(false);

                    if(task.isSuccessful()) {
                        if(!task.getResult().isEmpty()) {

                            mydatabase.truncateTable(mydatabase.placeOrder);
                            for(DocumentSnapshot documents : task.getResult()) {
                                orderSummaryDetail model = documents.toObject(orderSummaryDetail.class);

                                getOrderItemList(model);
                            }
                        }
                        else {
                            mydatabase.truncateTable(mydatabase.placeOrder);
                            recyclerView.setVisibility(View.GONE);
                            nodatafoundlo.setVisibility(View.VISIBLE);
                        }
                    }
                    else {

                        recyclerView.setVisibility(View.GONE);
                        nodatafoundlo.setVisibility(View.VISIBLE);

                        swipeRefresh.setRefreshing(false);
                        String error = Log.getStackTraceString(task.getException());
                        globalclass.log(tag,"getPlaceOrderList: "+error);
                        globalclass.toast_long("Unable to get order list, please try after sometime!");
                        globalclass.sendErrorLog(tag,"getPlaceOrderList",error);
                    }
                }
            });
        }
        catch (Exception e) {

            recyclerView.setVisibility(View.GONE);
            nodatafoundlo.setVisibility(View.VISIBLE);

            swipeRefresh.setRefreshing(false);
            String error = Log.getStackTraceString(e);
            globalclass.log(tag,"getPlaceOrderList: "+error);
            globalclass.toast_long("Unable to get order list, please try after sometime!");
            globalclass.sendErrorLog(tag,"getPlaceOrderList",error);
        }
    }

    void getOrderItemList(orderSummaryDetail model) {

        try {
            CollectionReference orderItemColl = globalclass.firebaseInstance().collection(Globalclass.orderItemColl);
            Query query = orderItemColl
                    .whereEqualTo("orderId",model.getOrderId());
            query.get().addOnCompleteListener(activity, new OnCompleteListener<QuerySnapshot>() {
                @Override
                public void onComplete(@NonNull Task<QuerySnapshot> task) {

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

                            getData();
                        }
                    }
                    else {

                        recyclerView.setVisibility(View.GONE);
                        nodatafoundlo.setVisibility(View.VISIBLE);

                        swipeRefresh.setRefreshing(false);
                        String error = Log.getStackTraceString(task.getException());
                        globalclass.log(tag,"getOrderItemList: "+error);
                        globalclass.toast_long("Unable to get order list, please try after sometime!");
                        globalclass.sendErrorLog(tag,"getOrderItemList",error);
                    }
                }
            });
        }
        catch (Exception e) {

            recyclerView.setVisibility(View.GONE);
            nodatafoundlo.setVisibility(View.VISIBLE);

            swipeRefresh.setRefreshing(false);
            String error = Log.getStackTraceString(e);
            globalclass.log(tag,"getOrderItemList: "+error);
            globalclass.toast_long("Unable to get order list, please try after sometime!");
            globalclass.sendErrorLog(tag,"getOrderItemList",error);
        }
    }

    void setAdapter() {

        nodatafoundlo.setVisibility(View.GONE);
        recyclerView.setVisibility(View.VISIBLE);

        adapter = new PlaceOrderAdapter(activity, arrayList, new PlaceOrderOnClick() {
            @Override
            public void onClick(int position, placeOrderDetail model) {

                startActivityForResult(new Intent(activity, OrderDetail.class)
                        .putExtra("orderId",model.getOrderId()),
                        ORDER_STATUS_CHANGED);
            }
        });

        linearLayoutManager = new LinearLayoutManager(activity);
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setAdapter(adapter);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == ORDER_STATUS_CHANGED) {
            if (resultCode == Activity.RESULT_OK && data != null) {
                getData();
            }
        }
    }

    public BroadcastReceiver OrderReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            try
            {
                globalclass.log(tag,"OrderReceiver: onReceive");
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

//    void removeListener() {
//
//        if(orderListListener != null) {
//            orderListListener.remove();
//        }
//
//        if(orderItemListListener != null) {
//            orderItemListListener.remove();
//        }
//    }

    @Override
    protected void onPause() {
        super.onPause();

//        removeListener();
        LocalBroadcastManager.getInstance(activity).unregisterReceiver(OrderReceiver);
    }
}