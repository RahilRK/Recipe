package com.hksofttronix.khansama.Admin.OrderStatusLog;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.hksofttronix.khansama.Globalclass;
import com.hksofttronix.khansama.Models.orderStatusLogDetail;
import com.hksofttronix.khansama.Models.purchaseDetail;
import com.hksofttronix.khansama.Mydatabase;
import com.hksofttronix.khansama.R;

import java.util.ArrayList;

public class OrderStatusLog extends AppCompatActivity {

    String tag = this.getClass().getSimpleName();
    AppCompatActivity activity = OrderStatusLog.this;

    Globalclass globalclass;
    Mydatabase mydatabase;
    ListenerRegistration orderStatusLogListener;

    SwipeRefreshLayout swipeRefresh;
    RecyclerView recyclerView;

    ArrayList<orderStatusLogDetail> arrayList = new ArrayList<>();
    OrderStatusLogAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_status_log);

        setToolbar();
        init();
        binding();
        onClick();
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
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_close);
        toolbar.setTitle("Order Status History");
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
    }

    void onClick() {

        swipeRefresh.setRefreshing(false);
        swipeRefresh.setEnabled(false);
        swipeRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {

                if (!globalclass.isInternetPresent()) {
                    swipeRefresh.setRefreshing(false);
                    globalclass.toast_long(getResources().getString(R.string.noInternetConnection));
                    return;
                }
            }
        });
    }

    @Override
    public void onStart() {
        super.onStart();
        getOrderStatusLog();
    }

    void getOrderStatusLog() {

        try {
            setAdapter();

            CollectionReference orderStatusLogColl = globalclass.firebaseInstance().collection(Globalclass.orderStatusLogColl);
            Query query = orderStatusLogColl
                    .whereEqualTo("orderId",getIntent().getStringExtra("orderId"))
                    .orderBy("logDateTime",Query.Direction.ASCENDING);
            orderStatusLogListener = query.addSnapshotListener(new EventListener<QuerySnapshot>() {
                @Override
                public void onEvent(@Nullable QuerySnapshot querySnapshot, @Nullable FirebaseFirestoreException firebaseFirestoreException) {

                    if (firebaseFirestoreException != null) {
                        String error = Log.getStackTraceString(firebaseFirestoreException);
                        globalclass.log(tag, "getOrderStatusLog: " + error);
                        globalclass.sendErrorLog(tag,"getOrderStatusLog: ",error);
                        globalclass.toast_long("Unable to get order status details, please try after sometime!");
                    }

                    if (querySnapshot == null || querySnapshot.isEmpty()) {

                        globalclass.snackit(activity,"No order history found!");
                        return;
                    }

                    String source = querySnapshot.getMetadata().isFromCache() ?
                            "local cache" : "server";
                    globalclass.log(tag,"getOrderStatusLog - Data fetched from "+source);

                    for (DocumentChange documentChange : querySnapshot.getDocumentChanges()) {
                        DocumentSnapshot documentSnapshot = documentChange.getDocument();

                        int oldIndex = documentChange.getOldIndex();
                        int newIndex = documentChange.getNewIndex();
//                                globalclass.log(tag,"oldIndex: "+oldIndex);
//                                globalclass.log(tag,"newIndex: "+newIndex);

                        if (documentChange.getType() == DocumentChange.Type.ADDED) {

                            orderStatusLogDetail model = documentSnapshot.toObject(orderStatusLogDetail.class);
                            if(!source.equalsIgnoreCase("local cache")) {
                                globalclass.log(tag,"OrderStatusLog ADDED from server: "+model.getOrderStatus().toUpperCase());
                            }
                            adapter.addData(newIndex,model);

                        } else if (documentChange.getType() == DocumentChange.Type.MODIFIED) {

                            orderStatusLogDetail model = documentSnapshot.toObject(orderStatusLogDetail.class);
                            if(!source.equalsIgnoreCase("local cache")) {
                                globalclass.log(tag,"OrderStatusLog MODIFIED from server: "+model.getOrderStatus().toUpperCase());
                            }
                            adapter.updateData(newIndex,model);
                        }
                    }
                }
            });
        }
        catch (Exception e) {

            String error = Log.getStackTraceString(e);
            globalclass.log(tag, "getOrderStatusLog: " + error);
            globalclass.sendErrorLog(tag,"getOrderStatusLog: ",error);
            globalclass.toast_long("Unable to get order status details, please try after sometime!");
        }
    }

    void setAdapter() {
        adapter = new OrderStatusLogAdapter(activity,arrayList);
        recyclerView.setLayoutManager(new LinearLayoutManager(activity));
        recyclerView.setAdapter(adapter);
    }

    @Override
    protected void onPause() {
        super.onPause();

        if(orderStatusLogListener != null) {
            orderStatusLogListener.remove();
        }
    }
}