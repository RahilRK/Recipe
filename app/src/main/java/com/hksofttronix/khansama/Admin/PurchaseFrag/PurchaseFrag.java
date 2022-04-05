package com.hksofttronix.khansama.Admin.PurchaseFrag;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.Transaction;
import com.google.firebase.firestore.WriteBatch;
import com.google.gson.Gson;
import com.hksofttronix.khansama.Admin.AddPurchase.AddPurchase;
import com.hksofttronix.khansama.Admin.AdminHome;
import com.hksofttronix.khansama.Models.inventoryDetail;
import com.hksofttronix.khansama.Models.purchaseDetail;
import com.hksofttronix.khansama.R;
import com.hksofttronix.khansama.Globalclass;
import com.hksofttronix.khansama.Mydatabase;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class PurchaseFrag extends Fragment {

    String tag = this.getClass().getSimpleName();
    AppCompatActivity activity;

    Globalclass globalclass;
    Mydatabase mydatabase;
    ListenerRegistration purchaseListListener;

    Toolbar toolbar;
    SwipeRefreshLayout swipeRefresh;
    RecyclerView recyclerView;
    ExtendedFloatingActionButton extendedFloatingActionButton;

    ArrayList<purchaseDetail> arrayList = new ArrayList<>();
    PurchaseAdapter adapter;
    LinearLayoutManager linearLayoutManager;

    ProgressDialog progressDialog;

    public PurchaseFrag() {
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
        return inflater.inflate(R.layout.fragment_purchase, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        getActivity().setTitle(getResources().getString(R.string.purchase));

        init();
        binding(view);
        onClick();
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

                if(!globalclass.isInternetPresent()) {
                    swipeRefresh.setRefreshing(false);
                    globalclass.toast_short(getString(R.string.noInternetConnection));
                    return;
                }

//                getPurchaseList();
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
                    extendedFloatingActionButton.hide();
                }
                else if (dy <0) {

                    // Scroll Up
                    extendedFloatingActionButton.show();
                }
            }
        });

        extendedFloatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                startActivity(new Intent(activity, AddPurchase.class));
            }
        });
    }

    @Override
    public void onStart() {
        super.onStart();
        getPurchaseList();
    }

    void getPurchaseList() {
        try {

            setAdapter();

            arrayList.clear();
            CollectionReference purchaseColl = globalclass.firebaseInstance().collection(Globalclass.purchaseColl);
            Query query = purchaseColl.orderBy("purchaseTime", Query.Direction.DESCENDING);
            purchaseListListener = query
                    .addSnapshotListener(new EventListener<QuerySnapshot>() {
                        @Override
                        public void onEvent(@Nullable QuerySnapshot querySnapshot, @Nullable FirebaseFirestoreException firebaseFirestoreException) {

                            if (firebaseFirestoreException != null) {
                                String error = Log.getStackTraceString(firebaseFirestoreException);
                                globalclass.log(tag, "getPurchaseList: " + error);
                                globalclass.sendErrorLog(tag,"getPurchaseList: ",error);
                                globalclass.toast_long("Unable to get purchase list, please try after sometime!");
                            }

                            if (querySnapshot == null || querySnapshot.isEmpty()) {

                                globalclass.snackitForFab(extendedFloatingActionButton,"No item found!");
                                return;
                            }

                            String source = querySnapshot.getMetadata().isFromCache() ?
                                    "local cache" : "server";
                            globalclass.log(tag,"getPurchaseList - Data fetched from "+source);

                            for (DocumentChange documentChange : querySnapshot.getDocumentChanges()) {
                                DocumentSnapshot documentSnapshot = documentChange.getDocument();

                                int oldIndex = documentChange.getOldIndex();
                                int newIndex = documentChange.getNewIndex();
//                                globalclass.log(tag,"oldIndex: "+oldIndex);
//                                globalclass.log(tag,"newIndex: "+newIndex);

                                if (documentChange.getType() == DocumentChange.Type.ADDED) {

                                    purchaseDetail model = documentSnapshot.toObject(purchaseDetail.class);
                                    if(!source.equalsIgnoreCase("local cache")) {
                                        globalclass.log(tag,"Purchase ADDED from server: "+model.getName().toUpperCase());
                                    }
                                    adapter.addData(newIndex,model);

                                } else if (documentChange.getType() == DocumentChange.Type.MODIFIED) {

                                    purchaseDetail model = documentSnapshot.toObject(purchaseDetail.class);
                                    if(!source.equalsIgnoreCase("local cache")) {
                                        globalclass.log(tag,"Purchase MODIFIED from server: "+model.getName().toUpperCase());
                                    }
                                    adapter.updateData(newIndex,model);

                                } else if (documentChange.getType() == DocumentChange.Type.REMOVED) {

                                    purchaseDetail model = documentSnapshot.toObject(purchaseDetail.class);
                                    if(!source.equalsIgnoreCase("local cache")) {
                                        globalclass.log(tag,"Purchase REMOVED from server: "+model.getName().toUpperCase());
                                    }
                                    if(oldIndex <= arrayList.size()-1 &&
                                            model.getName().equalsIgnoreCase(arrayList.get(oldIndex).getName())) {
                                        adapter.deleteData(oldIndex);
                                        globalclass.snackitForFab(extendedFloatingActionButton,"Removed successfully!");
                                    }
                                }
                            }
                        }
                    });

        } catch (Exception e) {

            swipeRefresh.setRefreshing(false);
            String error = Log.getStackTraceString(e);
            globalclass.log(tag, "getPurchaseList: " + error);
            globalclass.sendErrorLog(tag,"getPurchaseList: ",error);
            globalclass.toast_long("Unable to get purchase list, please try after sometime!");
        }
    }

    void setAdapter() {
        adapter = new PurchaseAdapter(activity, arrayList, new PurchaseOnClick() {
            @Override
            public void delete(purchaseDetail model) {

                if(!globalclass.isInternetPresent()) {
                    swipeRefresh.setRefreshing(false);
                    globalclass.toast_short(getString(R.string.noInternetConnection));
                    return;
                }

                //todo checking Purchase Date Diff is less then deletePurchaseDays()
                int purchaseDaysDiff = Integer.parseInt(
                        globalclass.getTimeDifference(model.getPurchaseTime().getTime()
                                ,globalclass.getMilliSecond()).get(0));

                //todo if purchase entry if of today and only one inventory(for first entry of inventory)
                if(getInventoryCount(model.getInventoryId()) == 1 &&
                    purchaseDaysDiff == 0) {

                    if(checkAnyOrderPlaced(model)) {
                        new MaterialAlertDialogBuilder(activity, R.style.RoundShapeTheme)
                                .setTitle("Sure")
                                .setMessage("Are you sure you want to remove?")
                                .setCancelable(false)
                                .setPositiveButton("Remove", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {

                                        dialog.dismiss();
                                        try {
                                            //todo getting stockId first...
//                                            getStockId(model);
                                            removePurchaseItem(model);
                                        }
                                        catch (Exception e) {

                                            String error = Log.getStackTraceString(e);
                                            globalclass.log(tag, "deleteInventory: " + error);
                                            globalclass.toast_long("Unable to remove inventory, please try after sometime!");
                                        }
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
                    else {
                        globalclass.showDialogue(activity,"Alert","Stock should not be less then minimum stock!");
                    }

                    return;
                }

                //todo normal flow
                if(purchaseDaysDiff > globalclass.deletePurchaseDays()) {
                    globalclass.showDialogue(activity,"Alert","You cannot delete old purchase entry!");
                }
                else {

                    if(checkMinimumStock(model)) {
                        new MaterialAlertDialogBuilder(activity, R.style.RoundShapeTheme)
                                .setTitle("Sure")
                                .setMessage("Are you sure you want to remove?")
                                .setCancelable(false)
                                .setPositiveButton("Remove", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {

                                        dialog.dismiss();
                                        try {
                                            //todo getting stockId first...
//                                            getStockId(model);
                                            removePurchaseItem(model);
                                        }
                                        catch (Exception e) {

                                            String error = Log.getStackTraceString(e);
                                            globalclass.log(tag, "deleteInventory: " + error);
                                            globalclass.sendErrorLog(tag,"deleteInventory: ",error);
                                            globalclass.toast_long("Unable to remove inventory, please try after sometime!");
                                        }
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
                    else {
                        globalclass.showDialogue(activity,"Alert","Stock should not be less then minimum stock!");
                    }
                }
            }
        });

        linearLayoutManager = new LinearLayoutManager(activity);
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setAdapter(adapter);
    }

    int getInventoryCount(String inventoryId) {

        int count = 0;

        for(int i=0;i<arrayList.size();i++) {
            purchaseDetail purchaseModel = arrayList.get(i);
            if(inventoryId.equalsIgnoreCase(purchaseModel.getInventoryId())) {

                count++;
            }
        }

        return count;
    }

    boolean checkAnyOrderPlaced(purchaseDetail model) {

        //todo Minimum stock should be left available
        inventoryDetail inventoryDetailModel = mydatabase.getParticularInventory(model.getInventoryId());
        if(model.getQuantity() == inventoryDetailModel.getQuantity()) {

            return true;
        }

        return false;
    }

    boolean checkMinimumStock(purchaseDetail model) {

        //todo Minimum stock should be left available
        inventoryDetail inventoryDetailModel = mydatabase.getParticularInventory(model.getInventoryId());
        if(model.getQuantity() >= inventoryDetailModel.getQuantity()) {

            double stockLeft = model.getQuantity() - inventoryDetailModel.getQuantity();
            if(stockLeft <= inventoryDetailModel.getMinimumQuantity()) {
                return false;
            }
        }
        else if(inventoryDetailModel.getQuantity() > model.getQuantity()) {

            double stockLeft = inventoryDetailModel.getQuantity() - model.getQuantity();
            if(stockLeft <= inventoryDetailModel.getMinimumQuantity()) {
                return false;
            }
        }

        return true;
    }

    void getStockId(purchaseDetail model) {

        String parameter = "";

        try {
            showprogress("Removing Item","Please wait...");

            Gson gson = new Gson();
            if(model != null) {
                parameter = gson.toJson(model);
            }
            globalclass.log(tag,"parameter: "+parameter);

            String finalParameter = parameter;

            CollectionReference collectionReference = globalclass.firebaseInstance().collection(Globalclass.stockColl);
            collectionReference
                    .whereEqualTo("inOutId", model.getPurchaseId()).get()
                    .addOnCompleteListener(activity, new OnCompleteListener<QuerySnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<QuerySnapshot> task) {

                            if(task.isSuccessful()) {
                                if(!task.getResult().isEmpty()) {
                                    for(DocumentSnapshot documentSnapshot:task.getResult()) {
                                        String stockId = documentSnapshot.getString("stockId");
                                        globalclass.log(tag,"stockId: "+stockId);
//                                        removePurchaseItemOld(model,stockId);
//                                        removePurchaseItem(model,stockId);
                                    }
                                }
                                else {
                                    hideprogress();
                                    globalclass.toast_long("No item found to delete!");
                                }
                            }
                            else {
                                hideprogress();
                                String error = "No stockId found!";
                                globalclass.log(tag,"getStockId: "+error);
                                globalclass.sendResponseErrorLog(tag,"getStockId: ",error, finalParameter);
                                globalclass.toast_long("Unable to remove item, please try again later!");
                            }
                        }
                    });
        }
        catch (Exception e) {

            hideprogress();
            String error = Log.getStackTraceString(e);
            globalclass.log(tag,"getStockId: "+error);
            globalclass.sendResponseErrorLog(tag,"getStockId: ",error, parameter);
            globalclass.toast_long("Unable to remove item, please try again later!");
        }
    }

    void removePurchaseItem(purchaseDetail model) {

        String parameter = "";

        try {
            final DocumentReference inventoryDocRef = globalclass.firebaseInstance()
                    .collection(Globalclass.inventoryColl)
                    .document(model.getInventoryId());

            Gson gson = new Gson();
            if(model != null) {
                parameter = gson.toJson(model);
            }
            globalclass.log(tag,"parameter: "+parameter);

            String finalParameter = parameter;

            globalclass.firebaseInstance().runTransaction(new Transaction.Function<Integer>() {

                @Nullable
                @Override
                public Integer apply(@NonNull Transaction transaction) throws FirebaseFirestoreException {

                    DocumentSnapshot inventoryDoc = transaction.get(inventoryDocRef);
                    if(inventoryDoc.exists()) {

                        CollectionReference purchaseColl = globalclass.firebaseInstance().collection(Globalclass.purchaseColl);
                        String purchaseId = model.getPurchaseId();
                        DocumentReference purchaseReference = purchaseColl.document(purchaseId);
                        transaction.delete(purchaseReference);

                        inventoryDetail inventoryModel = inventoryDoc.toObject(inventoryDetail.class);
                        CollectionReference inventoryColl = globalclass.firebaseInstance().collection(Globalclass.inventoryColl);
                        DocumentReference inventoryReference = inventoryColl.document(inventoryModel.getInventoryId());
                        Map<String,Object> map = new HashMap<>();
                        map.put("quantity", FieldValue.increment(-model.getQuantity()));
                        transaction.update(inventoryReference, map);

                        return 0;
                    }
                    else {

                        return -1;
                    }
                }
            }).addOnSuccessListener(activity, new OnSuccessListener<Integer>() {
                @Override
                public void onSuccess(Integer integer) {

                    hideprogress();
                    if(integer == 0) {

                        changeInventoryStockLocally(model);
                    }
                    else if(integer == -1) {

                        String error = "return -1";
                        globalclass.log(tag,"removePurchaseItem: "+error);
                        globalclass.sendResponseErrorLog(tag,"removePurchaseItem: ",error, finalParameter);
                        globalclass.toast_long("Unable to remove item, please try again later!");
                    }
                }
            }).addOnFailureListener(activity, new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {

                    hideprogress();
                    String error = Log.getStackTraceString(e);
                    globalclass.log(tag,"removePurchaseItem: "+error);
                    globalclass.sendResponseErrorLog(tag,"removePurchaseItem: ",error, finalParameter);
                    globalclass.toast_long("Unable to remove item, please try again later!");
                }
            });
        }
        catch (Exception e) {

            hideprogress();
            String error = Log.getStackTraceString(e);
            globalclass.log(tag,"removePurchaseItem: "+error);
            globalclass.sendResponseErrorLog(tag,"removePurchaseItem: ",error, parameter);
            globalclass.toast_long("Unable to remove item, please try again later!");
        }
    }

    void removePurchaseItemWithStock(purchaseDetail model, String stockId) {

        String parameter = "";

        try {
            final DocumentReference inventoryDocRef = globalclass.firebaseInstance()
                    .collection(Globalclass.inventoryColl)
                    .document(model.getInventoryId());

            Gson gson = new Gson();
            if(model != null) {
                parameter = gson.toJson(model);
            }
            globalclass.log(tag,"parameter: "+parameter);

            String finalParameter = parameter;

            globalclass.firebaseInstance().runTransaction(new Transaction.Function<Integer>() {

                @Nullable
                @Override
                public Integer apply(@NonNull Transaction transaction) throws FirebaseFirestoreException {

                    DocumentSnapshot inventoryDoc = transaction.get(inventoryDocRef);
                    if(inventoryDoc.exists()) {

                        CollectionReference purchaseColl = globalclass.firebaseInstance().collection(Globalclass.purchaseColl);
                        String purchaseId = model.getPurchaseId();
                        DocumentReference purchaseReference = purchaseColl.document(purchaseId);
                        transaction.delete(purchaseReference);

                        CollectionReference stockColl = globalclass.firebaseInstance().collection(Globalclass.stockColl);
                        DocumentReference stockReference = stockColl.document(stockId);
                        transaction.delete(stockReference);

                        inventoryDetail inventoryModel = inventoryDoc.toObject(inventoryDetail.class);
                        CollectionReference inventoryColl = globalclass.firebaseInstance().collection(Globalclass.inventoryColl);
                        DocumentReference inventoryReference = inventoryColl.document(inventoryModel.getInventoryId());
                        Map<String,Object> map = new HashMap<>();
                        map.put("quantity", FieldValue.increment(-model.getQuantity()));
                        transaction.update(inventoryReference, map);

                        return 0;
                    }
                    else {

                        return -1;
                    }
                }
            }).addOnSuccessListener(activity, new OnSuccessListener<Integer>() {
                @Override
                public void onSuccess(Integer integer) {

                    hideprogress();
                    if(integer == 0) {

                        changeInventoryStockLocally(model);
                    }
                    else if(integer == -1) {

                        String error = "return -1";
                        globalclass.log(tag,"removePurchaseItem: "+error);
                        globalclass.sendResponseErrorLog(tag,"removePurchaseItem: ",error, finalParameter);
                        globalclass.toast_long("Unable to remove item, please try again later!");
                    }
                }
            }).addOnFailureListener(activity, new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {

                    hideprogress();
                    String error = Log.getStackTraceString(e);
                    globalclass.log(tag,"removePurchaseItem: "+error);
                    globalclass.sendResponseErrorLog(tag,"removePurchaseItem: ",error, finalParameter);
                    globalclass.toast_long("Unable to remove item, please try again later!");
                }
            });
        }
        catch (Exception e) {

            hideprogress();
            String error = Log.getStackTraceString(e);
            globalclass.log(tag,"removePurchaseItem: "+error);
            globalclass.sendResponseErrorLog(tag,"removePurchaseItem: ",error, parameter);
            globalclass.toast_long("Unable to remove item, please try again later!");
        }
    }

    void removePurchaseItemOld(purchaseDetail model, String stockId) {

        try {
            WriteBatch batch = globalclass.firebaseInstance().batch();
            CollectionReference purchaseColl = globalclass.firebaseInstance().collection(Globalclass.purchaseColl);
            CollectionReference stockColl = globalclass.firebaseInstance().collection(Globalclass.stockColl);
            CollectionReference inventoryColl = globalclass.firebaseInstance().collection(Globalclass.inventoryColl);

            String purchaseId = model.getPurchaseId();

            DocumentReference purchaseReference = purchaseColl.document(purchaseId);
            batch.delete(purchaseReference);

            DocumentReference stockReference = stockColl.document(stockId);
            batch.delete(stockReference);

            DocumentReference inventoryReference = inventoryColl.document(model.getInventoryId());
            Map<String,Object> map = new HashMap<>();
            map.put("quantity", FieldValue.increment(-model.getQuantity()));
            batch.update(inventoryReference, map);

            batch.commit().addOnCompleteListener(activity, new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {

                    hideprogress();
                    if(task.isSuccessful()) {
                        changeInventoryStockLocally(model);
                    }
                    else {
                        String error = task.getException().toString();
                        globalclass.log(tag,"removePurchaseItem: "+error);
                        globalclass.toast_long("Unable to remove item, please try again later!");
                    }
                }
            });
        }
        catch (Exception e) {

            hideprogress();
            String error = Log.getStackTraceString(e);
            globalclass.log(tag,"removePurchaseItem: "+error);
            globalclass.toast_long("Unable to remove item, please try again later!");
        }
    }

    void changeInventoryStockLocally(purchaseDetail model) {
        try {
            inventoryDetail inventoryModel = mydatabase.getParticularInventory(model.getInventoryId());
            double minusQuantity = inventoryModel.getQuantity() - model.getQuantity();
            inventoryModel.setQuantity(minusQuantity);
            inventoryModel.setAdminId(globalclass.getStringData("adminId"));
            inventoryModel.setAdminName(globalclass.getStringData("adminName"));
            mydatabase.addInventory(inventoryModel);
        }
        catch (Exception e) {
            String error = Log.getStackTraceString(e);
            globalclass.log(tag, "changeInventoryStockLocally: " + error);
            globalclass.sendErrorLog(tag,"changeInventoryStockLocally: ",error);
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
    public void onPause() {
        super.onPause();

        if(purchaseListListener != null) {
            purchaseListListener.remove();
        }
    }
}