package com.hksofttronix.khansama.Address;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.RelativeLayout;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.Transaction;
import com.google.firebase.firestore.WriteBatch;
import com.google.gson.Gson;
import com.hksofttronix.khansama.AddAddress.AddAddress;
import com.hksofttronix.khansama.Models.addressDetail;
import com.hksofttronix.khansama.Models.userDetail;
import com.hksofttronix.khansama.R;
import com.hksofttronix.khansama.UpdateAddress.UpdateAddress;
import com.hksofttronix.khansama.Globalclass;
import com.hksofttronix.khansama.Mydatabase;

import java.util.ArrayList;

public class Address extends AppCompatActivity {

    String tag = this.getClass().getSimpleName();
    AppCompatActivity activity = Address.this;

    Globalclass globalclass;
    Mydatabase mydatabase;

    SwipeRefreshLayout swipeRefresh;
    ExtendedFloatingActionButton addNewAddressFab;
    RecyclerView recyclerView;
    RelativeLayout nodatafoundlo;

    ArrayList<addressDetail> arrayList = new ArrayList<>();
    AddressAdapter adapter;
    LinearLayoutManager linearLayoutManager;

    int ADD_NEW_ADDRESS = 111;
    int UPDATE_ADDRESS = 222;
    int ORDERSUMMARY_ADDRESS = 333;

    ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_address);

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
        toolbar.setTitle(R.string.your_address_book);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                onBackPressed();
            }
        });
    }

    void binding() {
        swipeRefresh = findViewById(R.id.swipeRefresh);
        addNewAddressFab = findViewById(R.id.addNewAddressFab);
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

                getAddressList();
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
                    addNewAddressFab.hide();
                }
                else if (dy <0) {

                    // Scroll Up
                    addNewAddressFab.show();
                }
            }
        });

        addNewAddressFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(mydatabase.getAddressList() != null && !mydatabase.getAddressList().isEmpty()) {
                    if(mydatabase.getAddressList().size() < globalclass.maxAddressCount()) {

                        if(getIntent().hasExtra("action") &&
                                getIntent().getStringExtra("action").equalsIgnoreCase("OrderSummary")) {

                            startActivityForResult(new Intent(activity, AddAddress.class)
                                            .putExtra("action", getIntent().getStringExtra("action"))
                                    ,ORDERSUMMARY_ADDRESS);
                        }
                        else {
                            startActivityForResult(new Intent(activity, AddAddress.class),ADD_NEW_ADDRESS);
                        }
                    }
                    else {
                        globalclass.showDialogue(activity,"Address limit alert",
                                "You can have maximum "+globalclass.maxAddressCount()+" address at a time, please remove some address to add new!");
                    }
                }
                else {
                    if(getIntent().hasExtra("action") &&
                            getIntent().getStringExtra("action").equalsIgnoreCase("OrderSummary")) {

                        startActivityForResult(new Intent(activity, AddAddress.class)
                                        .putExtra("action", getIntent().getStringExtra("action"))
                                ,ORDERSUMMARY_ADDRESS);
                    }
                    else {
                        startActivityForResult(new Intent(activity, AddAddress.class),ADD_NEW_ADDRESS);
                    }
                }
            }
        });
    }

    void getData() {
        arrayList.clear();
        arrayList = mydatabase.getAddressList();
        if(!arrayList.isEmpty()) {
            setAdapter();
        }
        else {
            getAddressList();
        }
    }

    void getAddressList() {

        try {
            CollectionReference addressColl = globalclass.firebaseInstance().collection(Globalclass.addressColl);
            Query query = addressColl
                    .whereEqualTo("userId",globalclass.getStringData("id"));
            query.get().addOnCompleteListener(activity, new OnCompleteListener<QuerySnapshot>() {
                @Override
                public void onComplete(@NonNull Task<QuerySnapshot> task) {

                    swipeRefresh.setRefreshing(false);
                    if(task.isSuccessful()) {
                        if(!task.getResult().isEmpty()) {

                            mydatabase.truncateTable(mydatabase.address);
                            for(DocumentSnapshot documents : task.getResult()) {
                                addressDetail model = documents.toObject(addressDetail.class);
                                mydatabase.addNewAddress(model);
                            }

                            getData();
                        }
                        else {
                            mydatabase.truncateTable(mydatabase.address);
                            recyclerView.setVisibility(View.GONE);
                            nodatafoundlo.setVisibility(View.VISIBLE);
                        }
                    }
                    else {

                        recyclerView.setVisibility(View.GONE);
                        nodatafoundlo.setVisibility(View.VISIBLE);

                        String error = Log.getStackTraceString(task.getException());
                        globalclass.log(tag,"getAddressList: "+error);
                        globalclass.toast_long("Unable to get address list, please try after sometime!");
                        globalclass.sendErrorLog(tag,"getAddressList",error);
                    }
                }
            });
        }
        catch (Exception e) {

            recyclerView.setVisibility(View.GONE);
            nodatafoundlo.setVisibility(View.VISIBLE);

            swipeRefresh.setRefreshing(false);
            String error = Log.getStackTraceString(e);
            globalclass.log(tag,"getAddressList: "+error);
            globalclass.toast_long("Unable to get address list, please try after sometime!");
            globalclass.sendErrorLog(tag,"getAddressList",error);
        }
    }

    void setAdapter() {
        nodatafoundlo.setVisibility(View.GONE);
        recyclerView.setVisibility(View.VISIBLE);

        adapter = new AddressAdapter(activity, arrayList, new AdddressOnClick() {
            @Override
            public void onClick(int position, addressDetail model) {

                if(getIntent().hasExtra("action") &&
                        getIntent().getStringExtra("action").equalsIgnoreCase("OrderSummary")) {

                    Intent intent = new Intent();
                    intent.putExtra("addressId",model.getAddressId());
                    intent.putExtra("action", getIntent().getStringExtra("action"));
                    setResult(RESULT_OK, intent);
                    finish();
                }
            }

            @Override
            public void onDelete(int position, addressDetail model) {

                if(!globalclass.isInternetPresent()) {
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
                                removeAddress(position,model);
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

            @Override
            public void onUpdate(int position, addressDetail model) {

                Intent intent = new Intent(activity, UpdateAddress.class);
                intent.putExtra("position", String.valueOf(position));
                intent.putExtra("addressId", model.getAddressId());
                startActivityForResult(intent, UPDATE_ADDRESS);
            }
        });

        linearLayoutManager = new LinearLayoutManager(activity);
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setAdapter(adapter);
    }

    void removeAddress(int position, addressDetail model) {

        String parameter = "";

        try {
            showprogress("Removing address","Please wait...");

            final DocumentReference addressDocRef = globalclass.firebaseInstance()
                    .collection(Globalclass.addressColl)
                    .document(model.getAddressId());

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

                    DocumentSnapshot addressDoc = transaction.get(addressDocRef);
                    if(addressDoc.exists()) {

                        addressDetail addressDetailModel = addressDoc.toObject(addressDetail.class);
                        if(addressDetailModel.getAddressId().equalsIgnoreCase(model.getAddressId())) {

                            transaction.delete(addressDocRef);
                            return 0;
                        }
                    }
                    else {

                        return 0;
                    }

                    return -1;
                }
            }).addOnSuccessListener(activity, new OnSuccessListener<Integer>() {
                @Override
                public void onSuccess(Integer integer) {

                    hideprogress();
                    if(integer == 0) {

                        mydatabase.deleteData(mydatabase.address,"addressId",model.getAddressId());
                        adapter.deleteData(position);
                        globalclass.snackitForFab(addNewAddressFab,"Removed successfully!");
                    }
                    else if(integer == -1) {

                        String error = "return -1";
                        globalclass.log(tag,"removeAddress onSuccess: "+error);
                        globalclass.toast_long("Unable to remove address, please try after sometime!");
                        globalclass.sendResponseErrorLog(tag,"removeAddress onFailure: ",error, finalParameter);
                    }
                }
            }).addOnFailureListener(activity, new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {

                    hideprogress();
                    String error = Log.getStackTraceString(e);
                    globalclass.log(tag,"removeAddress onFailure: "+error);
                    globalclass.toast_long("Unable to remove address, please try after sometime!");
                    globalclass.sendResponseErrorLog(tag,"removeAddress onFailure: ",error, finalParameter);

                }
            });
        }
        catch (Exception e) {

            hideprogress();
            String error = Log.getStackTraceString(e);
            globalclass.log(tag,"removeAddress: "+error);
            globalclass.toast_long("Unable to remove address, please try after sometime!");
            globalclass.sendResponseErrorLog(tag,"removeAddress: ",error, parameter);
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
        if(progressDialog != null && progressDialog.isShowing()) {
            progressDialog.dismiss();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == ADD_NEW_ADDRESS) {
            if (resultCode == Activity.RESULT_OK && data != null) {
                getData();
            }
        }
        else if (requestCode == UPDATE_ADDRESS) {
            if (resultCode == Activity.RESULT_OK && data != null) {

                int position = Integer.parseInt(data.getStringExtra("position"));
                addressDetail model = mydatabase.getParticularAddressDetail(data.getStringExtra("addressId"));
                adapter.updateData(position,model);
            }
        }
        else if (requestCode == ORDERSUMMARY_ADDRESS) {
            if (resultCode == Activity.RESULT_OK && data != null) {

                Intent intent = new Intent();
                intent.putExtra("addressId",data.getStringExtra("addressId"));
                setResult(RESULT_OK, intent);
                finish();
            }
        }
    }
}