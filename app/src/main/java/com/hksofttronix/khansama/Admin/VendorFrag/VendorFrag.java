package com.hksofttronix.khansama.Admin.VendorFrag;

import android.app.Activity;
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
import com.google.android.gms.tasks.Task;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.gson.Gson;
import com.hksofttronix.khansama.Admin.AddVendor.AddVendor;
import com.hksofttronix.khansama.Admin.AdminHome;
import com.hksofttronix.khansama.Admin.UpdateVendor.UpdateVendor;
import com.hksofttronix.khansama.Models.vendorDetail;
import com.hksofttronix.khansama.R;
import com.hksofttronix.khansama.Globalclass;
import com.hksofttronix.khansama.Mydatabase;

import java.util.ArrayList;

public class VendorFrag extends Fragment {

    String tag = this.getClass().getSimpleName();
    AppCompatActivity activity;

    Globalclass globalclass;
    Mydatabase mydatabase;

    Toolbar toolbar;
    SwipeRefreshLayout swipeRefresh;
    RecyclerView recyclerView;
    ExtendedFloatingActionButton extendedFloatingActionButton;

    ArrayList<vendorDetail> arrayList = new ArrayList<>();
    VendorAdapter adapter;
    LinearLayoutManager linearLayoutManager;

    int ADD_VENDOR = 111;
    int UPDATE_VENDOR_DETAILS = 222;

    ProgressDialog progressDialog;

    public VendorFrag() {
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
        return inflater.inflate(R.layout.fragment_vendor, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        getActivity().setTitle(getResources().getString(R.string.vendor));

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
        extendedFloatingActionButton = view.findViewById(R.id.extendedFloatingActionButton);
    }

    void onClick() {

        swipeRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {

                if(!globalclass.isInternetPresent()) {
                    swipeRefresh.setRefreshing(false);
                    globalclass.toast_short(getString(R.string.noInternetConnection));
                    return;
                }

                getVendorList();
            }
        });

        extendedFloatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                startActivityForResult(new Intent(activity, AddVendor.class),ADD_VENDOR);
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
    }

    void getData() {
        arrayList.clear();
        arrayList = mydatabase.getVendorList();
        if(!arrayList.isEmpty()) {
            setAdapter();
        }
        else {
            getVendorList();
        }
    }

    void getVendorList() {

        try {
            swipeRefresh.setRefreshing(true);
            arrayList.clear();
            CollectionReference inventoryColl = globalclass.firebaseInstance().collection(Globalclass.vendorColl);
            inventoryColl.get().addOnCompleteListener(activity, new OnCompleteListener<QuerySnapshot>() {
                @Override
                public void onComplete(@NonNull Task<QuerySnapshot> task) {

                    swipeRefresh.setRefreshing(false);

                    if(task.isSuccessful()) {
                        if(!task.getResult().isEmpty()) {

                            mydatabase.truncateTable(mydatabase.vendor);
                            for (DocumentSnapshot documentSnapshot : task.getResult()) {

                                vendorDetail model = documentSnapshot.toObject(vendorDetail.class);
                                mydatabase.addVendor(model);
                            }

                            getData();
                        }
                        else {
                            mydatabase.truncateTable(mydatabase.vendor);
                            globalclass.snackit(activity,"No vendor found!");
                        }
                    }
                    else {
                        String error = task.getException().toString();
                        globalclass.log(tag, "getVendorList: " + error);
                        globalclass.sendErrorLog(tag,"getVendorList: ",error);
                        globalclass.snackit(activity, "Unable to get vendor list, please try again after sometime!");
                    }
                }
            });

        } catch (Exception e) {

            swipeRefresh.setRefreshing(false);
            String error = Log.getStackTraceString(e);
            globalclass.log(tag, "getVendorList: " + error);
            globalclass.sendErrorLog(tag,"getVendorList: ",error);
            globalclass.toast_long("Unable to get vendor list, please try again after sometime!");
        }
    }

    void setAdapter() {
        adapter = new VendorAdapter(activity, arrayList, new VendorOnClick() {
            @Override
            public void onClick(int position, vendorDetail model) {

                Intent intent = new Intent(activity, UpdateVendor.class);
                intent.putExtra("position", String.valueOf(position));
                intent.putExtra("vendorDetail", model);
                startActivityForResult(intent, UPDATE_VENDOR_DETAILS);
            }

            @Override
            public void onDelete(int position, vendorDetail model) {

                if(!globalclass.isInternetPresent()) {
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
                                checkVendorExist(position,model);
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

    void checkVendorExist(int position, vendorDetail model) {

        String parameter = "";

        try {
            showprogress("Removing Vendor details","Please wait...");

            CollectionReference purchaseColl = globalclass.firebaseInstance().collection(Globalclass.purchaseColl);
            Query checkInventoryExist = purchaseColl.whereEqualTo("vendorId",model.getVendorId()).limit(1);

            Gson gson = new Gson();
            if(model != null) {
                parameter = gson.toJson(model);
            }
            globalclass.log(tag,"parameter: "+parameter);

            String finalParameter = parameter;

            checkInventoryExist.get().addOnCompleteListener(activity, new OnCompleteListener<QuerySnapshot>() {
                @Override
                public void onComplete(@NonNull Task<QuerySnapshot> task) {

                    String source = task.getResult().getMetadata().isFromCache() ?
                            "local cache" : "server";
                    globalclass.log(tag,"checkVendorExist - Data fetched from "+source);

                    if(task.isSuccessful()) {
                        if(!task.getResult().isEmpty()) {
                            for(DocumentSnapshot documents : task.getResult()) {
                                vendorDetail model = documents.toObject(vendorDetail.class);
                                globalclass.log(tag,"Vendor name: "+model.getVendorName());
                            }

                            hideprogress();
                            globalclass.showDialogue(activity,
                                    "Alert",
                                    "Product is still available with vendor name: "+model.getVendorName()+" ,please remove all it's product first!");
                        }
                        else {
                            removeVendor(position,model);
                        }
                    }
                    else {
                        hideprogress();
                        String error = Log.getStackTraceString(task.getException());
                        globalclass.log(tag,"checkVendorExist: "+error);
                        globalclass.sendResponseErrorLog(tag,"checkVendorExist: ",error, finalParameter);
                        globalclass.toast_long("Unable to remove vendor, please try after sometime!");
                    }
                }
            });
        }
        catch (Exception e) {

            hideprogress();
            String error = Log.getStackTraceString(e);
            globalclass.log(tag,"checkVendorExist: "+error);
            globalclass.sendResponseErrorLog(tag,"checkVendorExist: ",error, parameter);
            globalclass.toast_long("Unable to remove vendor, please try after sometime!");
        }
    }

    void removeVendor(int position, vendorDetail model) {

        String parameter = "";

        try {
            CollectionReference vendorColl = globalclass.firebaseInstance().collection(Globalclass.vendorColl);
            DocumentReference documentReference = vendorColl.document(model.getVendorId());

            Gson gson = new Gson();
            if(model != null) {
                parameter = gson.toJson(model);
            }
            globalclass.log(tag,"parameter: "+parameter);

            String finalParameter = parameter;

            documentReference.delete().addOnCompleteListener(activity, new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {

                    hideprogress();
                    if(task.isSuccessful()) {

                        mydatabase.deleteData(mydatabase.vendor,"vendorId",model.getVendorId());
                        adapter.deleteData(position);
                        globalclass.snackitForFab(extendedFloatingActionButton,"Removed successfully!");
                    }
                    else {
                        String error = task.getException().toString();
                        globalclass.log(tag,"removeVendor: "+error);
                        globalclass.sendResponseErrorLog(tag,"removeVendor: ",error, finalParameter);
                        globalclass.toast_long("Unable to remove vendor, please try again later!");
                    }
                }
            });
        }
        catch (Exception e) {

            String error = Log.getStackTraceString(e);
            globalclass.log(tag,"removeVendor: "+error);
            globalclass.sendResponseErrorLog(tag,"removeVendor: ",error, parameter);
            globalclass.toast_long("Unable to remove vendor, please try again later!");
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
//        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == ADD_VENDOR) {
            if (resultCode == Activity.RESULT_OK && data != null) {
                getData();
            }
        }
        else if (requestCode == UPDATE_VENDOR_DETAILS) {
            if (resultCode == Activity.RESULT_OK && data != null) {

                int position = Integer.parseInt(data.getStringExtra("position"));
                vendorDetail model = data.getParcelableExtra("vendorDetail");
                adapter.updateData(position,model);
            }
        }
    }

    void showprogress(String title,String message) {
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
}