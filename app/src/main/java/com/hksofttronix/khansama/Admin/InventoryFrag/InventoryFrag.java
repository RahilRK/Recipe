package com.hksofttronix.khansama.Admin.InventoryFrag;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
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
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.QuerySnapshot;
import com.hksofttronix.khansama.Admin.AddInventory.AddInventory;
import com.hksofttronix.khansama.Admin.AdminHome;
import com.hksofttronix.khansama.Admin.SelectInventory.SelectInventory;
import com.hksofttronix.khansama.Admin.UpdateInventory.UpdateInventory;
import com.hksofttronix.khansama.Models.inventoryDetail;
import com.hksofttronix.khansama.R;
import com.hksofttronix.khansama.Globalclass;
import com.hksofttronix.khansama.Mydatabase;

import java.util.ArrayList;

public class InventoryFrag extends Fragment {

    String tag = this.getClass().getSimpleName();
    AppCompatActivity activity;

    Globalclass globalclass;
    Mydatabase mydatabase;
    ListenerRegistration inventoryListListener;

    Toolbar toolbar;
    SwipeRefreshLayout swipeRefresh;
    RecyclerView recyclerView;
    ExtendedFloatingActionButton extendedFloatingActionButton;

    ArrayList<inventoryDetail> arrayList = new ArrayList<>();
    InventoryAdapter adapter;
    LinearLayoutManager linearLayoutManager;

//    int ADD_INVENTORY = 111;
//    int UPDATE_INVENTORY = 222;
//    int SEARCH_INVENTORY = 333;

    ProgressDialog progressDialog;

    public InventoryFrag() {
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
            globalclass.log(tag, "onActivityCreated: " + error);
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_inventory, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        getActivity().setTitle(getResources().getString(R.string.inventory));

        init();
        binding(view);
        onClick();
//        getData();
    }

    void init() {
        globalclass = Globalclass.getInstance(activity);
        mydatabase = Mydatabase.getInstance(activity);
    }

    void binding(View view) {
        toolbar = view.findViewById(R.id.toolbar);
        swipeRefresh = view.findViewById(R.id.swipeRefresh);
        recyclerView = view.findViewById(R.id.recyclerView);
        extendedFloatingActionButton = view.findViewById(R.id.extendedFloatingActionButton);
    }

    void onClick() {

        swipeRefresh.setRefreshing(false);
        swipeRefresh.setEnabled(false);
        swipeRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {

                if (!globalclass.isInternetPresent()) {
                    swipeRefresh.setRefreshing(false);
                    globalclass.toast_short(getString(R.string.noInternetConnection));
                    return;
                }

                getInventoryListOld();
            }
        });

        extendedFloatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                startActivity(new Intent(activity, AddInventory.class));
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
                    extendedFloatingActionButton.hide();
                } else if (dy < 0) {

                    // Scroll Up
                    extendedFloatingActionButton.show();
                }
            }
        });
    }

    void getData() {
        arrayList.clear();
        arrayList = mydatabase.getInventoryList();
        if (!arrayList.isEmpty()) {
            setAdapter();
        } else {
            getInventoryListOld();
        }
    }

    @Override
    public void onStart() {
        super.onStart();

        getInventoryList();
    }

    void getInventoryList() {
        try {
            setAdapter();

            arrayList.clear();
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

                        int oldIndex = documentChange.getOldIndex();
                        int newIndex = documentChange.getNewIndex();

                        if (documentChange.getType() == DocumentChange.Type.ADDED) {

                            inventoryDetail model = documentSnapshot.toObject(inventoryDetail.class);
                            if(!source.equalsIgnoreCase("local cache")) {
                                globalclass.log(tag,"Inventory ADDED from server: "+model.getName());
                            }

                            adapter.addData(newIndex,model);
                            mydatabase.addInventory(model);

                        } else if (documentChange.getType() == DocumentChange.Type.MODIFIED) {

                            inventoryDetail model = documentSnapshot.toObject(inventoryDetail.class);
                            if(!source.equalsIgnoreCase("local cache")) {
                                globalclass.log(tag,"Inventory MODIFIED from server: "+model.getName());
                            }

                            adapter.updateData(newIndex, model);
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

    void getInventoryListOld() {

        try {
            swipeRefresh.setRefreshing(true);
            CollectionReference inventoryColl = globalclass.firebaseInstance().collection(Globalclass.inventoryColl);
            inventoryColl.get().addOnCompleteListener(activity, new OnCompleteListener<QuerySnapshot>() {
                @Override
                public void onComplete(@NonNull Task<QuerySnapshot> task) {

                    swipeRefresh.setRefreshing(false);
                    if (task.isSuccessful()) {
                        if (!task.getResult().isEmpty()) {

                            mydatabase.truncateTable(mydatabase.inventory);
                            for (DocumentSnapshot documentSnapshot : task.getResult()) {

                                inventoryDetail model = documentSnapshot.toObject(inventoryDetail.class);
                                model.setisSelected(false);
                                mydatabase.addInventory(model);
                            }

                            getData();
                        } else {
                            globalclass.snackitForFab(extendedFloatingActionButton, "No inventory found!");
                        }
                    } else {
                        String error = task.getException().toString();
                        globalclass.log(tag, "getInventoryList: " + error);
                        globalclass.snackit(activity, "Unable to get inventory, please try again after sometime!");
                    }
                }
            });
        } catch (Exception e) {

            swipeRefresh.setRefreshing(false);
            String error = Log.getStackTraceString(e);
            globalclass.log(tag, "getInventoryList: " + error);
            globalclass.toast_long("Unable to get inventory list, please try after sometime!");
        }
    }

    void setAdapter() {

        adapter = new InventoryAdapter(activity, arrayList, new InventoryOnClick() {
            @Override
            public void edit(int position, inventoryDetail model) {

                startActivity(new Intent(activity, UpdateInventory.class)
                                .putExtra("position", "" + position)
                                .putExtra("inventoryId", model.getInventoryId())
                );
            }

            @Override
            public void delete(inventoryDetail model) {

                if (!globalclass.isInternetPresent()) {
                    swipeRefresh.setRefreshing(false);
                    globalclass.toast_short(getString(R.string.noInternetConnection));
                    return;
                }

                new MaterialAlertDialogBuilder(activity, R.style.RoundShapeTheme)
                        .setTitle("Sure")
                        .setMessage("Are you sure you want to remove?")
                        .setCancelable(false)
                        .setPositiveButton("Remove", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                                dialog.dismiss();
                            }
                        })
                        .setNeutralButton("No", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                                dialog.dismiss();
                            }
                        })
                        .show();
            }
        });

        linearLayoutManager = new LinearLayoutManager(activity);
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setAdapter(adapter);
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        activity.getMenuInflater().inflate(R.menu.menu_inventoryfrag, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        if (item.getItemId() == R.id.menu_search) {

            mydatabase.unCheckAllSelectedInventory();

            Intent intent = new Intent(activity, SelectInventory.class);
            intent.putExtra("action", tag);
            startActivity(intent);
        }

        return super.onOptionsItemSelected(item);
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

    void removeListener() {

        if(inventoryListListener != null) {
            inventoryListListener.remove();
        }
    }

    @Override
    public void onPause() {
        super.onPause();

        removeListener();
    }
}