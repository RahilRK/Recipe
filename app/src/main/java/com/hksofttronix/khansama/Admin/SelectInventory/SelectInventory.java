package com.hksofttronix.khansama.Admin.SelectInventory;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.hksofttronix.khansama.Admin.UpdateInventory.UpdateInventory;
import com.hksofttronix.khansama.Models.ingredientsDetail;
import com.hksofttronix.khansama.Models.inventoryDetail;
import com.hksofttronix.khansama.Models.purchaseDetail;
import com.hksofttronix.khansama.R;
import com.hksofttronix.khansama.Globalclass;
import com.hksofttronix.khansama.Mydatabase;

import java.util.ArrayList;

public class SelectInventory extends AppCompatActivity {

    String tag = this.getClass().getSimpleName();
    AppCompatActivity activity = SelectInventory.this;

    Globalclass globalclass;
    Mydatabase mydatabase;

    Toolbar toolbar;
    ImageView iv_select_all;
    SwipeRefreshLayout swipeRefresh;
    RecyclerView recyclerView;
    ExtendedFloatingActionButton extendedFloatingActionButton;

    ArrayList<inventoryDetail> arrayList = new ArrayList<>();
    SelectInventoryAdapter adapter;
    LinearLayoutManager linearLayoutManager;

    ArrayList<ingredientsDetail> ingredientsArrayList;
    ArrayList<purchaseDetail> purchaseDetailArrayList;

    MenuItem menu_search = null;
    SearchView searchView;

    String action = "";
    boolean autoCheck = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_inventory);

        setToolbar();
        init();
        binding();
        onClick();
        handleIntent();
        getData();
    }

    void init() {
        globalclass = Globalclass.getInstance(activity);
        mydatabase = Mydatabase.getInstance(activity);
    }

    void handleIntent() {
        action = getIntent().getStringExtra("action");
        if(getIntent().hasExtra("autoCheck")) {
            autoCheck = getIntent().getBooleanExtra("autoCheck",false);
        }

        globalclass.log(tag,"autoCheck: "+autoCheck);

        if(action.equalsIgnoreCase("AddRecipe")) {
            ingredientsArrayList = getIntent().getExtras().getParcelableArrayList("arrayList");
            extendedFloatingActionButton.show();
        }
        else if(action.equalsIgnoreCase("AddPurchase")) {
            purchaseDetailArrayList = getIntent().getExtras().getParcelableArrayList("arrayList");
            extendedFloatingActionButton.show();
        }
        else if(action.equalsIgnoreCase("InventoryFrag")) {
            extendedFloatingActionButton.hide();
        }
    }

    void binding() {
        iv_select_all = findViewById(R.id.iv_select_all);
        swipeRefresh = findViewById(R.id.swipeRefresh);
        recyclerView = findViewById(R.id.recyclerView);
        extendedFloatingActionButton = findViewById(R.id.extendedFloatingActionButton);
    }

    void onClick() {

        iv_select_all.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(adapter.selectedArraySize() == arrayList.size())
                {
                    if(action.equalsIgnoreCase("AddRecipe")) {
                        ingredientsArrayList.clear();
                    }
                    else if(action.equalsIgnoreCase("AddPurchase")) {
                        purchaseDetailArrayList.clear();
                    }

                    adapter.UnSelectAll();
                }
                else
                {
                    if(action.equalsIgnoreCase("AddRecipe")) {
                        selectAllIngredients();
                    }
                    else if(action.equalsIgnoreCase("AddPurchase")) {
                        selectAllPurchase();
                    }
                    adapter.SelectAll();
                }
                updateUI();
            }
        });

        swipeRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {

                if (!searchView.isIconified()) {
                    toolbar.collapseActionView();
                }

                if(!globalclass.isInternetPresent()) {
                    swipeRefresh.setRefreshing(false);
                    globalclass.toast_short(getString(R.string.noInternetConnection));
                    return;
                }

                getInventoryList();
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
                    if(!action.equalsIgnoreCase("InventoryFrag")) {
                        extendedFloatingActionButton.show();
                    }
                }
            }
        });

        extendedFloatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if(action.equalsIgnoreCase("AddRecipe")) {
                    Intent intent = new Intent();
                    Bundle bundle = new Bundle();
                    bundle.putParcelableArrayList("arrayList", ingredientsArrayList);
                    intent.putExtras(bundle);
                    setResult(RESULT_OK, intent);
                    finish();
                }
                else if(action.equalsIgnoreCase("AddPurchase")) {
//                    startActivity(new Intent(activity, AddInventory.class));
                    Intent intent = new Intent();
                    Bundle bundle = new Bundle();
                    bundle.putParcelableArrayList("arrayList", purchaseDetailArrayList);
                    intent.putExtras(bundle);
                    setResult(RESULT_OK, intent);
                    finish();
                }
            }
        });
    }

    void setToolbar() {
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_backarrow);
        toolbar.setTitle(getString(R.string.select_inventory));
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                onBackPressed();
            }
        });
    }

    void getData() {
        arrayList.clear();
        arrayList = mydatabase.getInventoryList();
        if(!arrayList.isEmpty()) {
            setAdapter();
            updateUI();
        }
        else {
            getInventoryList();
        }
    }

    void getInventoryList() {

        swipeRefresh.setRefreshing(true);

        try {
            arrayList.clear();
            CollectionReference inventoryColl = globalclass.firebaseInstance().collection(Globalclass.inventoryColl);
            inventoryColl.get().addOnCompleteListener(activity, new OnCompleteListener<QuerySnapshot>() {
                @Override
                public void onComplete(@NonNull Task<QuerySnapshot> task) {

                    swipeRefresh.setRefreshing(false);

                    if(task.isSuccessful()) {
                        if(!task.getResult().isEmpty()) {

                            mydatabase.truncateTable(mydatabase.inventory);

                            if(action.equalsIgnoreCase("AddRecipe")) {
                                ingredientsArrayList.clear();
                            }
                            else if(action.equalsIgnoreCase("AddPurchase")) {
                                purchaseDetailArrayList.clear();
                            }

                            for (DocumentSnapshot documentSnapshot : task.getResult()) {

                                inventoryDetail model = documentSnapshot.toObject(inventoryDetail.class);
                                model.setisSelected(false);
                                mydatabase.addInventory(model);
                            }

                            getData();
                        }
                        else {
                            mydatabase.truncateTable(mydatabase.inventory);
                            globalclass.snackit(activity,"No inventory found!");
                        }
                    }
                    else {
                        String error = task.getException().toString();
                        globalclass.log(tag, "getInventoryList: " + error);
                        globalclass.sendErrorLog(tag,"getInventoryList: ",error);
                        globalclass.snackit(activity, "Unable to get inventory, please try again after sometime!");
                    }
                }
            });

        } catch (Exception e) {

            swipeRefresh.setRefreshing(false);
            String error = Log.getStackTraceString(e);
            globalclass.log(tag, "getInventoryList: " + error);
            globalclass.sendErrorLog(tag,"getInventoryList: ",error);
            globalclass.toast_long("Unable to get inventory list, please try after sometime!");
        }
    }

    void autoCheckInventory() {
        if(action.equalsIgnoreCase("AddRecipe")) {
            if(!ingredientsArrayList.isEmpty() && autoCheck) {
                for(int i=0;i<ingredientsArrayList.size();i++) {
                    for(int j=0;j<arrayList.size();j++) {
                        if(ingredientsArrayList.get(i).getInventoryId().equalsIgnoreCase(arrayList.get(j).getInventoryId())) {
                            globalclass.log(tag,"autoCheckInventory: "+arrayList.get(j).getName());

                            inventoryDetail model = arrayList.get(j);
                            model.setisSelected(true);
                            adapter.updateData(j,model);
                            mydatabase.addInventory(model);
                        }
                    }
                }

                hideExistingIngredients();
            }
        }
    }

    void hideExistingIngredients() {
        for(int i=0;i<ingredientsArrayList.size();i++) {
            for(int j=0;j<arrayList.size();j++) {
                if(!globalclass.checknull(ingredientsArrayList.get(i).getIngredientId()).equalsIgnoreCase("") &&
                        ingredientsArrayList.get(i).getInventoryId().equalsIgnoreCase(arrayList.get(j).getInventoryId())) {
                    adapter.deleteData(j);
                }
            }
        }
    }

    void setAdapter() {
        adapter = new SelectInventoryAdapter(activity,action,arrayList, new SelectInventoryOnClick() {
            @Override
            public void onCardViewClick(int position, inventoryDetail model) {
//                startActivity(new Intent(activity, AddPurchase.class).putExtra("inventoryDetail",model));
                if(action.equalsIgnoreCase("InventoryFrag")) {

                    startActivity(new Intent(activity, UpdateInventory.class)
                                    .putExtra("position", "" + 0)
                                    .putExtra("inventoryId", model.getInventoryId())
                    );
                    finish();
                }
            }

            @Override
            public void onCheckBoxClick(int position, inventoryDetail model) {
                toggleSelection(position, model);
                getData();
                recyclerView.scrollToPosition(position);
            }
        });

        linearLayoutManager = new LinearLayoutManager(activity);
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setAdapter(adapter);

        autoCheckInventory();
    }

    void updateUI()
    {
        int size = adapter.selectedArraySize();

        if(size > 0)
        {
            iv_select_all.setVisibility(View.VISIBLE);
            toolbar.setTitle(""+size);
        }
        else
        {
            toolbar.setTitle(getString(R.string.select_inventory));
            iv_select_all.setVisibility(View.GONE);
        }
    }

    void toggleSelection(int position, inventoryDetail model) {

        if(model.getisSelected()) //unselect
        {
            model.setisSelected(false);

            if(action.equalsIgnoreCase("AddRecipe")) {
                deleteIngredients(model);
            }
            else if(action.equalsIgnoreCase("AddPurchase")) {
                deletePurchase(model);
            }
        }
        else   //select
        {
            model.setisSelected(true);

            if(action.equalsIgnoreCase("AddRecipe")) {
                ingredientsDetail ingredientsModel = new ingredientsDetail();
                ingredientsModel.setInventoryId(model.getInventoryId());
                ingredientsModel.setName(model.getName());
                ingredientsModel.setQuantity(0);
                ingredientsModel.setUnitId(model.getUnitId());
                ingredientsModel.setUnit(model.getUnit());
                ingredientsArrayList.add(ingredientsModel);
            }
            else if(action.equalsIgnoreCase("AddPurchase")) {
                purchaseDetail purchaseModel = new purchaseDetail();
                purchaseModel.setInventoryId(model.getInventoryId());
                purchaseModel.setName(model.getName());
                purchaseModel.setQuantity(0);
                purchaseModel.setUnitId(model.getUnitId());
                purchaseModel.setUnit(model.getUnit());
                purchaseModel.setSelectedUnit(model.getUnit());
                purchaseDetailArrayList.add(purchaseModel);
            }
        }

        adapter.updateData(position,model);
        mydatabase.addInventory(model);

        int count = adapter.selectedArraySize();
        if(count>0)
        {
            iv_select_all.setVisibility(View.VISIBLE);
            toolbar.setTitle(""+count);
        }
        else
        {
            toolbar.setTitle(getString(R.string.select_inventory));
            iv_select_all.setVisibility(View.GONE);
        }
    }

    void selectAllIngredients() {
        for(int i=0;i<arrayList.size();i++) {
            inventoryDetail model = arrayList.get(i);
            if(!checkIngredientsExist(model)) {
                ingredientsDetail ingredientsDetail = new ingredientsDetail();
                ingredientsDetail.setInventoryId(model.getInventoryId());
                ingredientsDetail.setName(model.getName());
                ingredientsDetail.setQuantity(0);
                ingredientsDetail.setUnitId(model.getUnitId());
                ingredientsDetail.setUnit(model.getUnit());
                ingredientsArrayList.add(ingredientsDetail);
            }
        }
    }

    void selectAllPurchase() {
        for(int i=0;i<arrayList.size();i++) {
            inventoryDetail model = arrayList.get(i);
            if(!checkPurchaseExist(model)) {
                purchaseDetail purchaseModel = new purchaseDetail();
                purchaseModel.setInventoryId(model.getInventoryId());
                purchaseModel.setName(model.getName());
                purchaseModel.setQuantity(0);
                purchaseModel.setUnitId(model.getUnitId());
                purchaseModel.setUnit(model.getUnit());
                purchaseModel.setSelectedUnit(model.getUnit());
                purchaseDetailArrayList.add(purchaseModel);
            }
        }
    }

    boolean checkIngredientsExist(inventoryDetail model) {
        for(int i=0;i<ingredientsArrayList.size();i++) {
            if(ingredientsArrayList.get(i).getInventoryId().equalsIgnoreCase(model.getInventoryId())){
                return true;
            }
        }

        return false;
    }

    boolean checkPurchaseExist(inventoryDetail model) {
        for(int i=0;i<purchaseDetailArrayList.size();i++) {
            if(purchaseDetailArrayList.get(i).getInventoryId().equalsIgnoreCase(model.getInventoryId())){
                return true;
            }
        }

        return false;
    }

    void deleteIngredients(inventoryDetail model) {
        for(int i=0;i<ingredientsArrayList.size();i++) {
            if(ingredientsArrayList.get(i).getInventoryId().equalsIgnoreCase(model.getInventoryId())){
                ingredientsArrayList.remove(i);
                return;
            }
        }
    }

    void deletePurchase(inventoryDetail model) {
        for(int i=0;i<purchaseDetailArrayList.size();i++) {
            if(purchaseDetailArrayList.get(i).getInventoryId().equalsIgnoreCase(model.getInventoryId())){
                purchaseDetailArrayList.remove(i);
                return;
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        activity.getMenuInflater().inflate(R.menu.menu_selectinventory, menu);
        menu_search = menu.findItem(R.id.menu_searchinventory);
        if(action.equalsIgnoreCase("InventoryFrag")) {
            menu_search.expandActionView(); // Expand the search menu item in order to show by default the query
        }
        searchView = (SearchView) menu_search.getActionView();

        ImageView iconClose = (ImageView) searchView.findViewById(androidx.appcompat.R.id.search_close_btn);
        iconClose.setColorFilter(getResources().getColor(R.color.black));

        EditText searchEditText = (EditText) searchView.findViewById(androidx.appcompat.R.id.search_src_text);
        searchEditText.setTextColor(getResources().getColor(R.color.black));
        searchEditText.setHintTextColor(getResources().getColor(R.color.mgray));

        searchView.setQueryHint("Enter inventory name");
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {

                if(s.length() > 0)
                {
                    arrayList.clear();
                    arrayList = mydatabase.searchInventory(s);
                    if(!arrayList.isEmpty())
                    {
                        setAdapter();
                    }
                    else
                    {
                        globalclass.log(tag,"onQueryTextSubmit: No item found!");
                        globalclass.toast_short("No item found!");
                    }
                }
                else
                {
                    globalclass.snackit(activity,"Enter inventory name!");
                }
                return false;
            }

            @Override
            public boolean onQueryTextChange(String s) {

                if(s.length() > 0)
                {
                    arrayList.clear();
                    arrayList = mydatabase.searchInventory(s);
                    if(!arrayList.isEmpty())
                    {
                        setAdapter();
                    }
                    else
                    {
                        globalclass.log(tag,"onQueryTextChange: No item found!");
                    }
                }
                else
                {
                    getData();
                }

                return false;
            }
        });

        return super.onCreateOptionsMenu(menu);
    }
}