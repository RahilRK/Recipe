package com.hksofttronix.khansama.CartFrag;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.CursorWindow;
import android.database.sqlite.SQLiteCursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.Transaction;
import com.google.firebase.firestore.WriteBatch;
import com.google.gson.Gson;
import com.hksofttronix.khansama.Login;
import com.hksofttronix.khansama.MainActivity;
import com.hksofttronix.khansama.Models.cartDetail;
import com.hksofttronix.khansama.Models.checkStockExistDetail;
import com.hksofttronix.khansama.Models.inventoryDetail;
import com.hksofttronix.khansama.Models.recipeDetail;
import com.hksofttronix.khansama.OrderSummary.OrderSummary;
import com.hksofttronix.khansama.R;
import com.hksofttronix.khansama.RecipeDetail.RecipeDetail;
import com.hksofttronix.khansama.Globalclass;
import com.hksofttronix.khansama.Mydatabase;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class CartFrag extends Fragment {

    String tag = this.getClass().getSimpleName();
    AppCompatActivity activity;

    Globalclass globalclass;
    Mydatabase mydatabase;

    RelativeLayout loginLayout;
    MaterialButton loginbt;
    RelativeLayout mainLayout;
    RelativeLayout cartSummaryLayout;
    TextView summary,orderSummary;
    SwipeRefreshLayout swipeRefresh;
    RecyclerView recyclerView;
    RelativeLayout nodatafoundlo;

    ArrayList<cartDetail> arrayList = new ArrayList<>();
    CartAdapter adapter;

    ArrayList<recipeDetail> recipeDetailsList = new ArrayList<>();
    ArrayList<String> notAvailableRecipeList = new ArrayList<>();
    ArrayList<String> limitedStockRecipeList = new ArrayList<>();

    ArrayList<checkStockExistDetail> cartRecipeIngredientsList = new ArrayList<>();

    String action = "";

    ProgressDialog progressDialog;

    public CartFrag() {
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
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if(getArguments() != null) {
            action = getArguments().getString("action");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_cart, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        init();
        binding(view);
        onClick();
    }

    void init() {
        globalclass = Globalclass.getInstance(activity);
        mydatabase = Mydatabase.getInstance(activity);
    }

    void binding(View view) {
        mainLayout = view.findViewById(R.id.mainLayout);
        loginLayout = view.findViewById(R.id.loginLayout);
        loginbt = view.findViewById(R.id.loginbt);
        cartSummaryLayout = view.findViewById(R.id.cartSummaryLayout);
        summary = view.findViewById(R.id.summary);
        orderSummary = view.findViewById(R.id.orderSummary);
        swipeRefresh = view.findViewById(R.id.swipeRefresh);
        recyclerView = view.findViewById(R.id.recyclerView);
        nodatafoundlo = view.findViewById(R.id.nodatafoundlo);
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

                getCartList();
            }
        });

        orderSummary.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if(!globalclass.isInternetPresent()) {
                    globalclass.toast_long(getResources().getString(R.string.noInternetConnection));
                    return;
                }

                checkRecipeDetail();
            }
        });

        loginbt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(activity, Login.class));
            }
        });
    }

    void checkRecipeDetail() {

        //todo Checking recipeStatus and price
        ArrayList<String> recipeIds = new ArrayList<>();
        recipeIds.clear();
        for(int i=0;i<arrayList.size();i++) {
            recipeIds.add(arrayList.get(i).getRecipeId());
        }

        try {
            showprogress("Hold on","Please wait...");

            CollectionReference recipeColl = globalclass.firebaseInstance().collection(Globalclass.recipeColl);
            Query query = recipeColl
                    .whereIn("recipeId",recipeIds);
            query.get().addOnCompleteListener(activity, new OnCompleteListener<QuerySnapshot>() {
                @Override
                public void onComplete(@NonNull Task<QuerySnapshot> task) {

                    if(task.isSuccessful()) {
                        if(!task.getResult().isEmpty()) {
                            recipeDetailsList.clear();
                            for(DocumentSnapshot documents : task.getResult()) {

                                recipeDetail recipeDetailModel = documents.toObject(recipeDetail.class);
                                recipeDetailsList.add(recipeDetailModel);
                                mydatabase.addRecipe(recipeDetailModel);
                            }

                            if(checkingRecipeStatus()) {
                                mydatabase.truncateTable(mydatabase.cartRecipeIngredients);

                                //todo merging all recipe ingredients to calculate stock
                                for(int i=0;i<arrayList.size();i++) {
                                    cartDetail model = arrayList.get(i);
                                    mydatabase.getIngredientsListWithAddedQuantity(model.getRecipeId(),model.getQuantity());
                                }

                                getInventoryList();
                            }
                            else {
                                hideprogress();
                                globalclass.showDialogue(
                                        activity,
                                        "Alert",
                                        TextUtils.join(", ", notAvailableRecipeList)+" is not currently available, it will be avaible soon. You can order & enjoy other recipe!"
                                );
                            }
                        }
                        else {
                            hideprogress();
                            String error = "Recipe not found!";
                            globalclass.log(tag,"Recipe not found!");
                            globalclass.toast_long("Unable to proceed, please try after sometime!");
                            globalclass.sendErrorLog(tag,"checkRecipeDetail",error);
                        }
                    }
                    else {
                        hideprogress();
                        String error = Log.getStackTraceString(task.getException());
                        globalclass.log(tag,"checkRecipeDetail: "+error);
                        globalclass.toast_long("Unable to proceed, please try after sometime!");
                        globalclass.sendErrorLog(tag,"checkRecipeDetail",error);
                    }
                }
            });
        }
        catch (Exception e) {

            hideprogress();
            String error = Log.getStackTraceString(e);
            globalclass.log(tag,"checkRecipeDetail: "+error);
            globalclass.toast_long("Unable to proceed, please try after sometime!");
            globalclass.sendErrorLog(tag,"checkRecipeDetail",error);
        }
    }

    boolean checkingRecipeStatus() {

        notAvailableRecipeList.clear();

        if(recipeDetailsList !=null && !recipeDetailsList.isEmpty()) {
            for(int i=0;i<recipeDetailsList.size();i++) {
                recipeDetail recipeDetailModel = recipeDetailsList.get(i);
                String recipeId = recipeDetailsList.get(i).getRecipeId();
                String recipeName = recipeDetailsList.get(i).getRecipeName();
                int price = recipeDetailsList.get(i).getPrice();
                boolean recipeStatus = recipeDetailsList.get(i).getStatus();

                if(!recipeStatus) {
                    notAvailableRecipeList.add(globalclass.firstLetterCapital(recipeName));
                }
                updateCartTable(recipeDetailModel);
            }
        }

        return notAvailableRecipeList.isEmpty();
    }

    void updateCartTable(recipeDetail recipeDetailModel) {
        for(int i=0;i<arrayList.size();i++) {
            if(arrayList.get(i).getRecipeId().equalsIgnoreCase(recipeDetailModel.getRecipeId())) {
                cartDetail cartDetailModel = mydatabase.getParticularCartDetail(recipeDetailModel.getRecipeId());

                cartDetailModel.setRecipeName(recipeDetailModel.getRecipeName());
                cartDetailModel.setPrice(recipeDetailModel.getPrice());
                cartDetailModel.setRecipeStatus(recipeDetailModel.getStatus());
                int mquantity = cartDetailModel.getQuantity();
                int sum = cartDetailModel.getPrice() * mquantity;
                cartDetailModel.setQuantity(mquantity);
                cartDetailModel.setSum(sum);

                adapter.updateData(i,cartDetailModel);
                mydatabase.addToCart(cartDetailModel);
            }
        }

        changeUI();
    }

    //todo checking Ingredient Stock
    void getInventoryList() {

        cartRecipeIngredientsList.clear();
        cartRecipeIngredientsList = mydatabase.getCartRecipeIngredients();
        ArrayList<inventoryDetail> liveIngredientsList = new ArrayList<>();
        ArrayList<String> inventoryIds = new ArrayList<>();

        liveIngredientsList.clear();
        inventoryIds.clear();

        for(int i=0;i<cartRecipeIngredientsList.size();i++) {
            inventoryIds.add(cartRecipeIngredientsList.get(i).getInventoryId());
        }

        try {
            CollectionReference inventoryColl = globalclass.firebaseInstance().collection(Globalclass.inventoryColl);
            Query query = inventoryColl
                    .whereIn("inventoryId",inventoryIds);
            query.get().addOnCompleteListener(activity, new OnCompleteListener<QuerySnapshot>() {
                @Override
                public void onComplete(@NonNull Task<QuerySnapshot> task) {

                    hideprogress();

                    if(task.isSuccessful()) {
                        if(!task.getResult().isEmpty()) {
                            for(DocumentSnapshot documents : task.getResult()) {
                                inventoryDetail model = documents.toObject(inventoryDetail.class);
                                globalclass.log("Inventory Name",model.getName());
                                liveIngredientsList.add(model);
                            }

                            if(checkStockExist(cartRecipeIngredientsList,liveIngredientsList)) {

                                recipeAdditionalCharges();
                                startActivity(new Intent(activity, OrderSummary.class)
                                .putExtra("additionalCharges",""+recipeAdditionalCharges()));
                            }
                        }
                        else {
                            String error = "Inventory not found!";
                            globalclass.log(tag,"getInventoryList: "+error);
                            globalclass.toast_long("Unable to proceed, please try after sometime!");
                            globalclass.sendErrorLog(tag,"getInventoryList",error);
                        }
                    }
                    else {
                        String error = Log.getStackTraceString(task.getException());
                        globalclass.log(tag,"getInventoryList: "+error);
                        globalclass.toast_long("Unable to proceed, please try after sometime!");
                        globalclass.sendErrorLog(tag,"getInventoryList",error);
                    }
                }
            });
        }
        catch (Exception e) {

            hideprogress();
            String error = Log.getStackTraceString(e);
            globalclass.log(tag,"getInventoryList: "+error);
            globalclass.toast_long("Unable to proceed, please try after sometime!");
            globalclass.sendErrorLog(tag,"getInventoryList",error);
        }
    }

    boolean checkStockExist(ArrayList<checkStockExistDetail> cartRecipeIngredientsList, ArrayList<inventoryDetail> liveIngredientsList) {

        try {
            for(int i=0;i<cartRecipeIngredientsList.size();i++) {
                String inventoryId = cartRecipeIngredientsList.get(i).getInventoryId();
                String inventoryName = cartRecipeIngredientsList.get(i).getName();
                String recipeName = cartRecipeIngredientsList.get(i).getRecipeName();
                double sum = cartRecipeIngredientsList.get(i).getSumOfQuantity();

                for(int j=0;j<liveIngredientsList.size();j++) {
                    String liveInventoryId = liveIngredientsList.get(j).getInventoryId();
                    double liveQuantity = liveIngredientsList.get(j).getQuantity();

                    if(inventoryId.equalsIgnoreCase(liveInventoryId)) {
                        if(sum > liveQuantity) {
                            getRecipeId(inventoryId);
                            globalclass.log(tag,"inventoryId: "+inventoryId);
                            globalclass.log("checkStockAvailable","Not enough stock of "+inventoryName+" for "+recipeName+"!");
                            return false;
                        }
                    }
                }
            }
        }
        catch (Exception e) {
            String error = Log.getStackTraceString(e);
            globalclass.log(tag,"checkStockExist: "+error);
            globalclass.toast_long("Unable to proceed, please try after sometime!");
            globalclass.sendErrorLog(tag,"checkStockExist",error);
            return false;
        }

        return true;
    }

    public void getRecipeId(String inventoryId) {

        ArrayList<String> recipeIdList = new ArrayList<>();
        recipeIdList.clear();

        Cursor cursor = null;
        try {
            String selectQuery = "SELECT * FROM " + mydatabase.recipeIngredients + " WHERE inventoryId = '"+inventoryId+"'";

            SQLiteDatabase db = mydatabase.getReadableDatabase();
            cursor = db.rawQuery(selectQuery, null);

            if (cursor instanceof SQLiteCursor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                ((SQLiteCursor) cursor).setWindow(new CursorWindow(null, 1024 * 1024 * 10));
            }

            // looping through all rows and adding to list
            if (cursor.moveToFirst()) {
                do {
                    String recipeId = cursor.getString(cursor.getColumnIndex("recipeId"));

                    if(checkRecipeExistInCart(recipeId)) {
                        recipeIdList.add(recipeId);
                    }
                }
                while (cursor.moveToNext());

                if(recipeIdList != null && !recipeIdList.isEmpty()) {
                    getRecipeName(recipeIdList);
                }
                else {
                    String error = "recipeIdList is null!";
                    globalclass.log(tag, "getRecipeId: " + error);
                    globalclass.toast_long("Something went wrong, please try after sometime!");
                    globalclass.sendErrorLog(tag,"getRecipeId",error);
                }
            }
        }
        catch (Exception e) {
            String error = Log.getStackTraceString(e);
            globalclass.log(tag, "getRecipeId: " + error);
            globalclass.toast_long("Something went wrong, please try after sometime!");
            globalclass.sendErrorLog(tag,"getRecipeId",error);
        }
        finally {
            if (cursor != null && !cursor.isClosed()) {
                cursor.close();
            }
        }
    }

    boolean checkRecipeExistInCart(String recipeId) {
        for(int i=0;i<arrayList.size();i++) {
            if(arrayList.get(i).getRecipeId().equalsIgnoreCase(recipeId)) {
                return true;
            }
        }

        return false;
    }

    void getRecipeName(ArrayList<String> recipeIdList) {
        try {
            limitedStockRecipeList.clear();
            for(int i=0;i<recipeIdList.size();i++) {
                limitedStockRecipeList.add(globalclass.firstLetterCapital(mydatabase.getParticularRecipeDetail(recipeIdList.get(i)).getRecipeName()));
            }

            if(limitedStockRecipeList != null && !limitedStockRecipeList.isEmpty()) {
                globalclass.showDialogue(activity,
                        "Alert",
                        "Please remove some "+TextUtils.join(" or ", limitedStockRecipeList)+" quantity, as it's quantity is currently limited");
            }
            else {
                String error = "limitedStockRecipeList is null!";
                globalclass.log(tag, "getRecipeName: " + error);
                globalclass.toast_long("Something went wrong, please try after sometime!");
                globalclass.sendErrorLog(tag,"getRecipeName",error);
            }
        }
        catch (Exception e) {
            String error = Log.getStackTraceString(e);
            globalclass.log(tag,"getRecipeName: "+error);
            globalclass.toast_long("Unable to proceed, please try after sometime!");
            globalclass.sendErrorLog(tag,"getRecipeName",error);
        }
    }

    int recipeAdditionalCharges() {

        int sum = 0;
        for(int i=0;i<arrayList.size();i++) {
            cartDetail cartModel = arrayList.get(i);
            recipeDetail recipeModel = mydatabase.getParticularRecipeDetail(cartModel.getRecipeId());
            int perRecipeCharge = recipeModel.getAdditionalCharges() * cartModel.getQuantity();
            sum += perRecipeCharge;
        }

        globalclass.log(tag,"recipeAdditionalCharges: "+sum);
        return sum;
    }

    void getData() {
        arrayList.clear();
        arrayList = mydatabase.getCartList();
        if(!arrayList.isEmpty()) {
            setAdapter();
        }
        else {
            getCartList();
        }

        changeUI();
    }

    void getCartList() {

        try {
            CollectionReference cartColl = globalclass.firebaseInstance().collection(Globalclass.cartColl);
            Query query = cartColl
                    .whereEqualTo("userId",globalclass.getStringData("id"));

            query.get().addOnCompleteListener(activity, new OnCompleteListener<QuerySnapshot>() {
                @Override
                public void onComplete(@NonNull Task<QuerySnapshot> task) {

                    swipeRefresh.setRefreshing(false);
                    if(task.isSuccessful()) {
                        if(!task.getResult().isEmpty()) {

                            mydatabase.truncateTable(mydatabase.cart);
                            for(DocumentSnapshot documents : task.getResult()) {
                                cartDetail model = documents.toObject(cartDetail.class);
                                model.setQuantity(1);
                                model.setSum(model.getPrice());
                                mydatabase.addToCart(model);
                            }

                            getData();
                        }
                        else {

                            mydatabase.truncateTable(mydatabase.cart);
                            changeUI();
                        }
                    }
                    else {
                        String error = Log.getStackTraceString(task.getException());
                        globalclass.log(tag,"getCartList: "+error);
                        globalclass.toast_long("Unable to get cart list, please try after sometime!");
                        globalclass.sendErrorLog(tag,"getCartList",error);

                        cartSummaryLayout.setVisibility(View.GONE);
                        recyclerView.setVisibility(View.GONE);
                        nodatafoundlo.setVisibility(View.VISIBLE);
                    }
                }
            });
        }
        catch (Exception e) {

            swipeRefresh.setRefreshing(false);
            String error = Log.getStackTraceString(e);
            globalclass.log(tag,"getCartList: "+error);
            globalclass.toast_long("Unable to get cart list, please try after sometime!");
            globalclass.sendErrorLog(tag,"getCartList",error);

            cartSummaryLayout.setVisibility(View.GONE);
            recyclerView.setVisibility(View.GONE);
            nodatafoundlo.setVisibility(View.VISIBLE);
        }
    }

    void setAdapter() {

        nodatafoundlo.setVisibility(View.GONE);
        cartSummaryLayout.setVisibility(View.VISIBLE);
        recyclerView.setVisibility(View.VISIBLE);

        adapter = new CartAdapter(activity, arrayList, new CartOnClick() {
            @Override
            public void onRemove(int position, cartDetail model) {

                showRemoveDialogue(position,model);
            }

            @Override
            public void viewDetail(int position, cartDetail model) {

                startActivity(new Intent(activity, RecipeDetail.class)
                                .putExtra("recipeId",model.getRecipeId()));
            }

            @Override
            public void minus(int position, cartDetail model, CartAdapter.RecyclerViewHolders holder) {



                if(model.getQuantity() >= 2) {
                    int quantity = model.getQuantity() - 1;
                    int sum = model.getPrice() * quantity;
                    model.setQuantity(quantity);
                    model.setSum(sum);
                    mydatabase.addToCart(model);
                    holder.quantity.setText(""+quantity);
                    changeUI();
                }
                else if(model.getQuantity() <= 1){

                    showRemoveDialogue(position,model);
                }
            }

            @Override
            public void add(int position, cartDetail model, CartAdapter.RecyclerViewHolders holder) {

                int maxQuantity = 0;
                maxQuantity = globalclass.maxCartQuantity();
//                if(BuildConfig.DEBUG) {
//                    maxQuantity = 50;
//                }

                if(model.getQuantity() <= maxQuantity-1) {
                    int quantity = model.getQuantity() + 1;
                    int sum = model.getPrice() * quantity;
                    model.setQuantity(quantity);
                    model.setSum(sum);
                    mydatabase.addToCart(model);
                    holder.quantity.setText(""+quantity);
                    changeUI();
                }
                else {
                    globalclass.toast_short("You can add maximum "+globalclass.maxCartQuantity()+" quantities for every recipe!");
                }
            }
        });

        recyclerView.setLayoutManager(new LinearLayoutManager(activity));
        recyclerView.setAdapter(adapter);
    }

    void showRemoveDialogue(int position, cartDetail cartDetailModel) {

        if(!globalclass.isInternetPresent()) {
            globalclass.toast_long(getResources().getString(R.string.noInternetConnection));
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
                        removeFromCart(position,cartDetailModel);
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

    void removeFromCart(int position, cartDetail model) {

        String parameter = "";

        try {
            showprogress("Removing from cart", "Please wait...");

            final DocumentReference cartDocRef = globalclass.firebaseInstance()
                    .collection(Globalclass.cartColl)
                    .document(model.getCartId());

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

                    DocumentSnapshot addressDoc = transaction.get(cartDocRef);
                    if(addressDoc.exists()) {

                        cartDetail cartDetailModel = addressDoc.toObject(cartDetail.class);
                        if(cartDetailModel.getCartId().equalsIgnoreCase(model.getCartId())) {

                            transaction.delete(cartDocRef);
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

                        adapter.deleteData(position);
                        mydatabase.deleteData(mydatabase.cart,"cartId",model.getCartId());
                        globalclass.snackit(activity, "Removed successfully!");
                        changeUI();
                    }
                    else if(integer == -1) {

                        String error = "return -1";
                        globalclass.log(tag,"removeFromCart onSuccess: "+error);
                        globalclass.toast_long("Unable to remove recipe from cart, please try after sometime!");
                        globalclass.sendResponseErrorLog(tag,"removeFromCart",error, finalParameter);
                    }
                }
            }).addOnFailureListener(activity, new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {

                    hideprogress();
                    String error = Log.getStackTraceString(e);
                    globalclass.log(tag, "removeFromCart onFailure: " + error);
                    globalclass.toast_long("Unable to remove recipe from cart, please try after sometime!");
                    globalclass.sendResponseErrorLog(tag,"removeFromCart",error, finalParameter);
                }
            });
        }
        catch (Exception e) {

            hideprogress();
            String error = Log.getStackTraceString(e);
            globalclass.log(tag, "removeFromCart: " + error);
            globalclass.toast_long("Unable to remove recipe from cart, please try after sometime!");
            globalclass.sendResponseErrorLog(tag,"removeFromCart",error, parameter);
        }
    }

    void changeUI() {
        if(mydatabase.getCartItemCount() > 0) {

            nodatafoundlo.setVisibility(View.GONE);
            cartSummaryLayout.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.VISIBLE);

            summary.setText(""+mydatabase.getCartItemCount()+" Item | "+" ₹ "+mydatabase.getCartSum());
        }
        else {
            cartSummaryLayout.setVisibility(View.GONE);
            recyclerView.setVisibility(View.GONE);
            nodatafoundlo.setVisibility(View.VISIBLE);
        }

        if(action.equalsIgnoreCase("")) {
            ((MainActivity)getActivity()).setBudgetCount();
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
        if (progressDialog !=null && progressDialog.isShowing()) {
            progressDialog.dismiss();
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        if(globalclass.checkUserIsLoggedIn()) {
            loginLayout.setVisibility(View.GONE);
            mainLayout.setVisibility(View.VISIBLE);
            getData();
        }
        else {
            mainLayout.setVisibility(View.GONE);
            loginLayout.setVisibility(View.VISIBLE);
        }
    }
}