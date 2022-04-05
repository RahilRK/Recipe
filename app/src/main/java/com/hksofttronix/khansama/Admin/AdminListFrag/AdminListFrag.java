package com.hksofttronix.khansama.Admin.AdminListFrag;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.hksofttronix.khansama.Admin.AddNewAdmin.AddNewAdmin;
import com.hksofttronix.khansama.Admin.AdminHome;
import com.hksofttronix.khansama.Admin.UpdateAdmin.UpdateAdmin;
import com.hksofttronix.khansama.Globalclass;
import com.hksofttronix.khansama.Models.navMenuDetail;
import com.hksofttronix.khansama.Models.userDetail;
import com.hksofttronix.khansama.Mydatabase;
import com.hksofttronix.khansama.R;

import java.util.ArrayList;

public class AdminListFrag extends Fragment {

    String tag = this.getClass().getSimpleName();
    AppCompatActivity activity;

    Globalclass globalclass;
    Mydatabase mydatabase;
    ListenerRegistration adminListListener;

    Toolbar toolbar;
    SwipeRefreshLayout swipeRefresh;
    RecyclerView recyclerView;
    ExtendedFloatingActionButton extendedFloatingActionButton;

    ArrayList<userDetail> arrayList = new ArrayList<>();
    AdminListAdapter adapter;

    ProgressDialog progressDialog;

    public AdminListFrag() {
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
        return inflater.inflate(R.layout.fragment_adminlist, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        getActivity().setTitle(getResources().getString(R.string.all_admin));

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

                getAdminList();
            }
        });

        extendedFloatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                startActivity(new Intent(activity, AddNewAdmin.class));
            }
        });
    }

    @Override
    public void onStart() {
        super.onStart();
        getAdminList();
    }

    void getAdminList() {
        try {

            setAdapter();

            arrayList.clear();
            CollectionReference userColl = globalclass.firebaseInstance().collection(Globalclass.userColl);
            Query query = userColl.whereEqualTo("admin",true);
            adminListListener = query
                    .addSnapshotListener(new EventListener<QuerySnapshot>() {
                        @Override
                        public void onEvent(@Nullable QuerySnapshot querySnapshot, @Nullable FirebaseFirestoreException firebaseFirestoreException) {

                            swipeRefresh.setRefreshing(false);
                            if (firebaseFirestoreException != null) {
                                String error = Log.getStackTraceString(firebaseFirestoreException);
                                globalclass.log(tag, "getAdminList: " + error);
                                globalclass.sendErrorLog(tag,"getAdminList: ",error);
                                globalclass.toast_long("Unable to get admin list, please try after sometime!");
                                return;
                            }

                            String source = querySnapshot.getMetadata().isFromCache() ?
                                    "local cache" : "server";
                            globalclass.log(tag,"getAdminList - Data fetched from "+source);

                            mydatabase.truncateTable(mydatabase.navMenu);
                            for (DocumentChange documentChange : querySnapshot.getDocumentChanges()) {
                                DocumentSnapshot documentSnapshot = documentChange.getDocument();

                                int oldIndex = documentChange.getOldIndex();
                                int newIndex = documentChange.getNewIndex();

                                if (documentChange.getType() == DocumentChange.Type.ADDED) {

                                    userDetail model = documentSnapshot.toObject(userDetail.class);
                                    if(!source.equalsIgnoreCase("local cache")) {
                                        globalclass.log(tag,"Admin ADDED from server: "+model.getName());
                                    }

                                    if(model.getRightsList() !=null && !model.getRightsList().isEmpty()) {
                                        if(model.getId().equalsIgnoreCase(globalclass.checknull(globalclass.getStringData("adminId")))) {
                                            for(int i=0;i<model.getRightsList().size();i++) {
                                                navMenuDetail navMenuModel = model.getRightsList().get(i);
                                                mydatabase.addNavMenu(navMenuModel);
                                            }
                                        }
                                    }

                                    adapter.addData(newIndex,model);

                                } else if (documentChange.getType() == DocumentChange.Type.MODIFIED) {

                                    userDetail model = documentSnapshot.toObject(userDetail.class);
                                    if(!source.equalsIgnoreCase("local cache")) {
                                        globalclass.log(tag,"Admin MODIFIED from server: "+model.getName());
                                    }

                                    if(model.getRightsList() !=null && !model.getRightsList().isEmpty()) {
                                        if(model.getId().equalsIgnoreCase(globalclass.checknull(globalclass.getStringData("adminId")))) {
                                            for(int i=0;i<model.getRightsList().size();i++) {
                                                navMenuDetail navMenuModel = model.getRightsList().get(i);
                                                mydatabase.addNavMenu(navMenuModel);
                                            }
                                        }
                                    }

                                    adapter.updateData(newIndex,model);

                                } else if (documentChange.getType() == DocumentChange.Type.REMOVED) {

                                    adapter.deleteData(oldIndex);
                                }
                            }

                            if(querySnapshot.isEmpty()) {
                                globalclass.snackitForFab(extendedFloatingActionButton,"No admin found!");
                            }
                        }
                    });

        } catch (Exception e) {

            swipeRefresh.setRefreshing(false);
            String error = Log.getStackTraceString(e);
            globalclass.log(tag, "getAdminList: " + error);
            globalclass.sendErrorLog(tag,"getAdminList: ",error);
            globalclass.toast_long("Unable to get admin list, please try after sometime!");
        }
    }

    void setAdapter() {
        adapter = new AdminListAdapter(activity, arrayList, new AdminListOnClick() {
            @Override
            public void viewDetail(userDetail model) {

                if(!globalclass.isInternetPresent()) {
                    swipeRefresh.setRefreshing(false);
                    globalclass.toast_short(getString(R.string.noInternetConnection));
                    return;
                }

                startActivity(new Intent(activity, UpdateAdmin.class)
                .putExtra("userDetail",model)
                .putExtra("rightsList",model.getRightsList())
                .putExtra("oldRightsList",model.getRightsList())
                );
                globalclass.log(tag,"done");
            }
        });

        recyclerView.setLayoutManager(new LinearLayoutManager(activity));
        recyclerView.setAdapter(adapter);
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

        if(adminListListener != null) {
            adminListListener.remove();
        }
    }
}