package com.hksofttronix.khansama.Admin.AllOrders;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.gson.Gson;
import com.hksofttronix.khansama.Admin.AdminHome;
import com.hksofttronix.khansama.Admin.AdminOrderDetail.AdminOrderDetail;
import com.hksofttronix.khansama.Globalclass;
import com.hksofttronix.khansama.Models.allOrderItemDetail;
import com.hksofttronix.khansama.Models.orderItemDetail;
import com.hksofttronix.khansama.Models.orderSummaryDetail;
import com.hksofttronix.khansama.Mydatabase;
import com.hksofttronix.khansama.R;

import java.util.ArrayList;

public class AllOrdersFrag extends Fragment {

    String tag = this.getClass().getSimpleName();
    AppCompatActivity activity;

    Globalclass globalclass;
    Mydatabase mydatabase;

    Toolbar toolbar;
    SwipeRefreshLayout swipeRefresh;
    RecyclerView recyclerView;
//    ExtendedFloatingActionButton extendedFloatingActionButton;

    ArrayList<allOrderItemDetail> arrayList = new ArrayList<>();
    AllOrderAdapter adapter;
    LinearLayoutManager linearLayoutManager;

    int ORDER_STATUS_CHANGED = 111;

    public AllOrdersFrag() {
        // Required empty public constructor
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        if (context instanceof AppCompatActivity) {
            activity = (AppCompatActivity) context;
        }
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        try {
            ((AdminHome) getActivity()).setToolbar(toolbar);
        } catch (Exception e) {

            String error = Log.getStackTraceString(e);
            globalclass.log(tag,"onActivityCreated: "+error);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_all_orders, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        getActivity().setTitle(getResources().getString(R.string.allorders));

        init();
        binding(view);
        onClick();
        getData();
    }

    void init() {
        globalclass = Globalclass.getInstance(activity);
        mydatabase = Mydatabase.getInstance(activity);
    }

    void binding(View view) {
        toolbar = view.findViewById(R.id.toolbar);
        swipeRefresh = view.findViewById(R.id.swipeRefresh);
        recyclerView = view.findViewById(R.id.recyclerView);
    }

    void onClick() {

        swipeRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {

                if (!globalclass.isInternetPresent()) {
                    swipeRefresh.setRefreshing(false);
                    globalclass.toast_short(getString(R.string.noInternetConnection));
                    return;
                }

                getAllOrderList();
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

                if (dy > 0) {
                    // Scroll Down
//                    extendedFloatingActionButton.hide();
                } else if (dy < 0) {

                    // Scroll Up
//                    extendedFloatingActionButton.show();
                }
            }
        });
    }

    void getData() {
        arrayList.clear();
        arrayList = mydatabase.getAllOrderList();
        if (!arrayList.isEmpty()) {
            setAdapter();
        } else {
            getAllOrderList();
        }
    }

    void getAllOrderList() {

        try {
            swipeRefresh.setRefreshing(true);

            CollectionReference placeOrderColl = globalclass.firebaseInstance().collection(Globalclass.placeOrderColl);
            placeOrderColl.get().addOnCompleteListener(activity, new OnCompleteListener<QuerySnapshot>() {
                @Override
                public void onComplete(@NonNull Task<QuerySnapshot> task) {

                    swipeRefresh.setRefreshing(false);

                    if (task.isSuccessful()) {
                        if (!task.getResult().isEmpty()) {

                            mydatabase.truncateTable(mydatabase.allOrder);
                            for (DocumentSnapshot documents : task.getResult()) {
                                orderSummaryDetail model = documents.toObject(orderSummaryDetail.class);
                                getOrderItemList(model);
                            }
                        } else {
                            mydatabase.truncateTable(mydatabase.allOrder);
                            globalclass.snackit(activity, "No order found!");
                        }
                    } else {

                        swipeRefresh.setRefreshing(false);
                        String error = Log.getStackTraceString(task.getException());
                        globalclass.log(tag, "getPlaceOrderList: " + error);
                        globalclass.sendErrorLog(tag,"getPlaceOrderList: ",error);
                        globalclass.toast_long("Unable to get order list, please try after sometime!");
                    }
                }
            });
        } catch (Exception e) {

            swipeRefresh.setRefreshing(false);
            String error = Log.getStackTraceString(e);
            globalclass.log(tag, "getPlaceOrderList: " + error);
            globalclass.sendErrorLog(tag,"getPlaceOrderList: ",error);
            globalclass.toast_long("Unable to get order list, please try after sometime!");
        }
    }

    void getOrderItemList(orderSummaryDetail model) {

        String parameter = "";

        try {
            swipeRefresh.setRefreshing(true);

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

                    swipeRefresh.setRefreshing(false);

                    if (task.isSuccessful()) {
                        if (!task.getResult().isEmpty()) {

                            for (DocumentSnapshot documents : task.getResult()) {
                                orderItemDetail orderItemModel = documents.toObject(orderItemDetail.class);

                                allOrderItemDetail allOrderDetailModel = new allOrderItemDetail();
                                allOrderDetailModel.setOrderId(model.getOrderId());
                                allOrderDetailModel.setUserId(model.getUserId());
                                allOrderDetailModel.setUserMobileNumber(model.getUserMobileNumber());
                                allOrderDetailModel.setCancelReason(model.getCancelReason());
                                allOrderDetailModel.setOrderStatus(model.getOrderStatus());
                                allOrderDetailModel.setOrderStep(model.getOrderStep());
                                allOrderDetailModel.setTotal(model.getTotal());
                                allOrderDetailModel.setItem(model.getItem());
                                allOrderDetailModel.setOrderDateTime(model.getOrderDateTime());
                                allOrderDetailModel.setDeliveryDateTime(model.getDeliveryDateTime());
                                allOrderDetailModel.setAddressId(model.getAddressId());
                                allOrderDetailModel.setName(model.getName());
                                allOrderDetailModel.setMobileNumber(model.getMobileNumber());
                                allOrderDetailModel.setAddress(model.getAddress());
                                allOrderDetailModel.setNearBy(model.getNearBy());
                                allOrderDetailModel.setCityId(model.getCityId());
                                allOrderDetailModel.setCity(model.getCity());
                                allOrderDetailModel.setStateId(model.getStateId());
                                allOrderDetailModel.setState(model.getState());
                                allOrderDetailModel.setPincode(model.getPincode());
                                allOrderDetailModel.setOrderItemId(orderItemModel.getOrderItemId());
                                allOrderDetailModel.setRecipeId(orderItemModel.getRecipeId());
                                allOrderDetailModel.setRecipeName(orderItemModel.getRecipeName());
                                allOrderDetailModel.setRecipeImageId(orderItemModel.getRecipeImageId());
                                allOrderDetailModel.setRecipeImageUrl(orderItemModel.getRecipeImageUrl());
                                allOrderDetailModel.setPrice(orderItemModel.getPrice());
                                allOrderDetailModel.setQuantity(orderItemModel.getQuantity());

                                mydatabase.addAllOrder(allOrderDetailModel);
                            }

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
            globalclass.sendResponseErrorLog(tag,"getOrderItemList: ",error, parameter);
            globalclass.toast_long("Unable to get order list, please try after sometime!");
        }
    }

    void setAdapter() {
        adapter = new AllOrderAdapter(activity, arrayList, new AllOrderOnClick() {
            @Override
            public void onClick(int position, allOrderItemDetail model) {
                startActivityForResult(new Intent(activity, AdminOrderDetail.class)
                        .putExtra("orderId", model.getOrderId()), ORDER_STATUS_CHANGED);
            }
        });

        linearLayoutManager = new LinearLayoutManager(activity);
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setAdapter(adapter);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
//        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == ORDER_STATUS_CHANGED) {
            if (resultCode == Activity.RESULT_OK && data != null) {
                getData();
            }
        }
    }

    public BroadcastReceiver OrderReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            try {
                globalclass.log(tag, "OrderReceiver: onReceive");
                getData();
            } catch (Exception e) {
                String error = Log.getStackTraceString(e);
                globalclass.log(tag, "OrderReceiver: " + error);
                globalclass.sendErrorLog(tag,"OrderReceiver: ",error);
                globalclass.toast_long("Error in refreshing data, please swipe down to refresh data!");
            }
        }
    };

    @Override
    public void onResume() {
        super.onResume();
        LocalBroadcastManager.getInstance(activity).registerReceiver(
                OrderReceiver, new IntentFilter(Globalclass.OrderReceiver));
    }

    @Override
    public void onPause() {
        super.onPause();

        LocalBroadcastManager.getInstance(activity).unregisterReceiver(OrderReceiver);
    }
}