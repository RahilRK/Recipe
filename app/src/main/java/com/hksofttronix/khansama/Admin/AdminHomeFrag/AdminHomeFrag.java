package com.hksofttronix.khansama.Admin.AdminHomeFrag;

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
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.material.button.MaterialButton;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.hksofttronix.khansama.Admin.AdminHome;
import com.hksofttronix.khansama.Admin.AdminOrderDetail.AdminOrderDetail;
import com.hksofttronix.khansama.Admin.AllOrders.AllOrderOnClick;
import com.hksofttronix.khansama.Globalclass;
import com.hksofttronix.khansama.Models.allOrderItemDetail;
import com.hksofttronix.khansama.Models.inventoryDetail;
import com.hksofttronix.khansama.Models.navMenuDetail;
import com.hksofttronix.khansama.Models.userDetail;
import com.hksofttronix.khansama.Mydatabase;
import com.hksofttronix.khansama.R;
import com.hksofttronix.khansama.SyncData;

import java.util.ArrayList;

public class AdminHomeFrag extends Fragment {

    String tag = this.getClass().getSimpleName();
    AppCompatActivity activity;

    Globalclass globalclass;
    Mydatabase mydatabase;
    ListenerRegistration inventoryListListener;

    Toolbar toolbar;
    TextView lastSyncTime;
    MaterialButton refreshNowbt;
    SwipeRefreshLayout swipeRefresh;
    TextView tvOrderlistTitle,viewAllOrderlist;
    LinearLayout limitedInventoryLayout,orderListLayout;
    RecyclerView limitedInventoryRecyclerView,orderRecyclerView;

    ArrayList<inventoryDetail> limitInventoryList = new ArrayList<>();
    HomeLimitedInventoryAdapter limitedInventoryAdapter;

    ArrayList<allOrderItemDetail> orderList = new ArrayList<>();
    HomeOrderListAdapter orderListAdapter;
    LinearLayoutManager orderListLinearLayoutManager;

    ArrayList<navMenuDetail> navMenuList = new ArrayList<>();

    int ORDER_STATUS_CHANGED = 111;

    public AdminHomeFrag() {
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
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_admin_home, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        getActivity().setTitle(getResources().getString(R.string.home));

        init();
        binding(view);
        onClick();
        getLiveData();
        setText();
        showHideHomeDetails();
    }

    void init() {
        globalclass = Globalclass.getInstance(activity);
        mydatabase = Mydatabase.getInstance(activity);
    }

    void binding(View view) {
        toolbar = view.findViewById(R.id.toolbar);
        lastSyncTime = view.findViewById(R.id.lastSyncTime);
        refreshNowbt = view.findViewById(R.id.refreshNowbt);
        swipeRefresh = view.findViewById(R.id.swipeRefresh);
        tvOrderlistTitle = view.findViewById(R.id.tvOrderlistTitle);
        viewAllOrderlist = view.findViewById(R.id.viewAllOrderlist);
        limitedInventoryLayout = view.findViewById(R.id.limitedInventoryLayout);
        orderListLayout = view.findViewById(R.id.orderListLayout);
        limitedInventoryRecyclerView = view.findViewById(R.id.limitedInventoryRecyclerView);
        orderRecyclerView = view.findViewById(R.id.orderRecyclerView);
    }

    void onClick() {

        refreshNowbt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (!globalclass.isInternetPresent()) {
                    globalclass.toast_short(getString(R.string.noInternetConnection));
                    return;
                }

                if (!globalclass.isMyServiceRunning(activity, SyncData.class)) {

                    globalclass.toast_long("Syncing data...!");
                    Intent intent = new Intent(activity, SyncData.class);
                    ContextCompat.startForegroundService(activity, intent);
                } else {
                    globalclass.toast_long("Already in progress!");
                }
            }
        });

        swipeRefresh.setRefreshing(false);
        swipeRefresh.setEnabled(false);
        swipeRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {

                if(!globalclass.isInternetPresent()) {
                    swipeRefresh.setRefreshing(false);
                    globalclass.toast_short(getString(R.string.noInternetConnection));
                    return;
                }
            }
        });

        orderRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
            }

            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

                int visibleItemCount = orderListLinearLayoutManager.getChildCount();
                int totalItemCount = orderListLinearLayoutManager.getItemCount();
                int pastVisibleItems = orderListLinearLayoutManager.findFirstVisibleItemPosition();

                if (dy >0) {
                    // Scroll Down
                }
                else if (dy <0) {

                    // Scroll Up
                }
            }
        });

        viewAllOrderlist.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ((AdminHome) getActivity()).displaySelectedScreen(R.id.nav_allorders);
            }
        });
    }

    void setText() {
        lastSyncTime.setText(globalclass.checknull(globalclass.getStringData("lastSyncDataDateTime")));
    }

    void getLiveData() {
        getInventoryList();
    }

    void getInventoryList() {
        try {
            CollectionReference inventoryColl = globalclass.firebaseInstance().collection(Globalclass.inventoryColl);
            inventoryListListener = inventoryColl.addSnapshotListener(new EventListener<QuerySnapshot>() {
                @Override
                public void onEvent(@Nullable QuerySnapshot querySnapshot, @Nullable FirebaseFirestoreException firebaseFirestoreException) {

                    if (firebaseFirestoreException != null) {
                        String error = Log.getStackTraceString(firebaseFirestoreException);
                        globalclass.log(tag, "getInventoryList: " + error);
                        globalclass.sendErrorLog(tag,"getInventoryList: ",error);
                        globalclass.toast_long("Unable to get inventory data!");
                        return;
                    }

                    String source = querySnapshot.getMetadata().isFromCache() ?
                            "local cache" : "server";
                    globalclass.log(tag,"getInventoryList - Data fetched from "+source);

                    for (DocumentChange documentChange : querySnapshot.getDocumentChanges()) {
                        DocumentSnapshot documentSnapshot = documentChange.getDocument();

                        if (documentChange.getType() == DocumentChange.Type.ADDED) {

                            inventoryDetail model = documentSnapshot.toObject(inventoryDetail.class);
                            if(!source.equalsIgnoreCase("local cache")) {
                                globalclass.log(tag,"Inventory ADDED from server: "+model.getName());
                            }
                            mydatabase.addInventory(model);

                        } else if (documentChange.getType() == DocumentChange.Type.MODIFIED) {

                            inventoryDetail model = documentSnapshot.toObject(inventoryDetail.class);
                            if(!source.equalsIgnoreCase("local cache")) {
                                globalclass.log(tag,"Inventory MODIFIED from server: "+model.getName());
                            }

                            mydatabase.addInventory(model);
                        }
                    }
                }
            });

        } catch (Exception e) {

            String error = Log.getStackTraceString(e);
            globalclass.log(tag, "getInventoryList: " + error);
            globalclass.sendErrorLog(tag,"getInventoryList: ",error);
            globalclass.toast_long("Unable to get inventory data!");
        }
    }

    void getLimitedInventoryData() {
        limitInventoryList.clear();
        limitInventoryList = mydatabase.getHomeLimitedInventoryList();
        if (limitInventoryList !=null && !limitInventoryList.isEmpty()) {
            setLimitedInventoryAdapter();
        } else {
            limitedInventoryLayout.setVisibility(View.GONE);
        }
    }

    void setLimitedInventoryAdapter() {
        limitedInventoryLayout.setVisibility(View.VISIBLE);
        limitedInventoryAdapter = new HomeLimitedInventoryAdapter(activity, limitInventoryList);
        limitedInventoryRecyclerView.setLayoutManager(new LinearLayoutManager(activity,LinearLayoutManager.HORIZONTAL,false));
        limitedInventoryRecyclerView.setAdapter(limitedInventoryAdapter);
    }

    void getPendingOrderData() {
        orderList.clear();
        orderList = mydatabase.getAllPendingOrderListForHome();
        if(!orderList.isEmpty()) {

            tvOrderlistTitle.setText("Pending Order");
            setOrderListAdapter();
        }
        else {
            getRecentOrderData();
        }
    }

    void getRecentOrderData() {
        orderList.clear();
        orderList = mydatabase.getAllOrderListForHome();
        if(orderList != null && !orderList.isEmpty()) {

            tvOrderlistTitle.setText("Recent Order");
            setOrderListAdapter();
        }

        showHideOrderLayout();
    }

    void setOrderListAdapter() {
        orderListAdapter = new HomeOrderListAdapter(activity, orderList, new AllOrderOnClick() {
            @Override
            public void onClick(int position, allOrderItemDetail model) {
                startActivityForResult(new Intent(activity, AdminOrderDetail.class)
                        .putExtra("orderId",model.getOrderId()),ORDER_STATUS_CHANGED);
            }
        });

        orderListLinearLayoutManager = new LinearLayoutManager(activity);
        orderRecyclerView.setLayoutManager(orderListLinearLayoutManager);
        orderRecyclerView.setAdapter(orderListAdapter);
    }

    void showHideOrderLayout() {
        if(orderList != null && !orderList.isEmpty()) {
            orderListLayout.setVisibility(View.VISIBLE);
        }
        else {
            orderListLayout.setVisibility(View.GONE);
        }
    }

    void showHideHomeDetails() {
        navMenuList = mydatabase.getNavMenu();
        if(navMenuList != null && !navMenuList.isEmpty()) {
            for(int i=0;i<navMenuList.size();i++) {
                navMenuDetail navMenuModel = navMenuList.get(i);
                if(navMenuModel.getNavMenuId().equalsIgnoreCase("nav_allorders")) {
                    if(navMenuModel.getAccessStatus()) {
                        orderListLayout.setVisibility(View.VISIBLE);
                        getPendingOrderData();
                    }
                    else {
                        orderListLayout.setVisibility(View.GONE);
                    }
                }
                else if(navMenuModel.getNavMenuId().equalsIgnoreCase("nav_purchase")) {
                    if(navMenuModel.getAccessStatus()) {
                        limitedInventoryLayout.setVisibility(View.VISIBLE);
                        getLimitedInventoryData();
                    }
                    else {
                        limitedInventoryLayout.setVisibility(View.GONE);
                    }
                }
            }
        }
        else {
            limitedInventoryLayout.setVisibility(View.GONE);
            orderListLayout.setVisibility(View.GONE);
        }
    }

    void removeListener() {

        if(inventoryListListener != null) {
            inventoryListListener.remove();
        }
    }

    public BroadcastReceiver SyncDataReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            try
            {
                globalclass.log(tag,"SyncDataReceiver: onReceive");
                setText();
                showHideHomeDetails();
            }
            catch (Exception e)
            {
                String error = Log.getStackTraceString(e);
                globalclass.log(tag,"SyncDataReceiver: "+error);
                globalclass.sendErrorLog(tag,"SyncDataReceiver: ",error);
                globalclass.toast_long("Error in refreshing data, please swipe down to refresh data!");
            }
        }
    };

    public BroadcastReceiver OrderReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            try
            {
                globalclass.log(tag,"OrderReceiver: onReceive");
                setText();
                showHideHomeDetails();
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
    public void onResume() {
        super.onResume();

        LocalBroadcastManager.getInstance(activity).registerReceiver(
                SyncDataReceiver, new IntentFilter(Globalclass.SyncDataReceiver));

        LocalBroadcastManager.getInstance(activity).registerReceiver(
                OrderReceiver, new IntentFilter(Globalclass.OrderReceiver));
    }

    @Override
    public void onPause() {
        super.onPause();

        removeListener();
        LocalBroadcastManager.getInstance(activity).unregisterReceiver(SyncDataReceiver);
        LocalBroadcastManager.getInstance(activity).unregisterReceiver(OrderReceiver);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
//        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == ORDER_STATUS_CHANGED) {
            if (resultCode == Activity.RESULT_OK && data != null) {
                showHideHomeDetails();
            }
        }
    }
}