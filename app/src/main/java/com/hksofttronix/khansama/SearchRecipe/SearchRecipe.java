package com.hksofttronix.khansama.SearchRecipe;

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
import android.widget.RelativeLayout;

import com.hksofttronix.khansama.Globalclass;
import com.hksofttronix.khansama.Models.recipeDetail;
import com.hksofttronix.khansama.Mydatabase;
import com.hksofttronix.khansama.R;
import com.hksofttronix.khansama.RecipeDetail.RecipeDetail;

import java.util.ArrayList;

public class SearchRecipe extends AppCompatActivity {

    String tag = this.getClass().getSimpleName();
    AppCompatActivity activity = SearchRecipe.this;

    Globalclass globalclass;
    Mydatabase mydatabase;

    SwipeRefreshLayout swipeRefresh;
    RecyclerView recyclerView;
    RelativeLayout noSearchResultLayout;

    Toolbar toolbar;
    MenuItem menu_search = null;
    SearchView searchView;

    ArrayList<recipeDetail> arrayList = new ArrayList<>();
    SearchRecipeAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_recipe);

        init();
        binding();
        onClick();
        setToolbar();
        getData();
    }

    void init() {
        globalclass = Globalclass.getInstance(activity);
        mydatabase = Mydatabase.getInstance(activity);
    }

    void binding() {
        swipeRefresh = findViewById(R.id.swipeRefresh);
        recyclerView = findViewById(R.id.recyclerView);
        noSearchResultLayout = findViewById(R.id.noSearchResultLayout);
    }

    void onClick() {

        swipeRefresh.setRefreshing(false);
        swipeRefresh.setEnabled(false);
        swipeRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                if (!globalclass.isInternetPresent()) {
                    swipeRefresh.setRefreshing(false);
                    globalclass.toast_long(getString(R.string.noInternetConnection));
                    return;
                }

//                getAllRecipeList();
//                getAllIngredientsList();
//                getAllRecipeImageList();
            }
        });
    }

    void setToolbar() {
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_backarrow);
        toolbar.setTitle(getString(R.string.searchRecipe));
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                onBackPressed();
            }
        });
    }

    void getData() {

        try {
            arrayList.clear();
            arrayList = mydatabase.getRecipeList();
            if (!arrayList.isEmpty()) {
                setAdapter();
            }
        } catch (Exception e) {

            String error = Log.getStackTraceString(e);
            globalclass.log(tag, "getData: " + error);
            globalclass.toast_long("Unable to get recipe list, please try after sometime!");
            globalclass.sendErrorLog(tag, "getData", error);
        }
    }

    void setAdapter() {
        noSearchResultLayout.setVisibility(View.GONE);
        recyclerView.setVisibility(View.VISIBLE);

        adapter = new SearchRecipeAdapter(activity, arrayList, new SearchRecipeOnClick() {
            @Override
            public void viewDetail(int position, recipeDetail model) {

                startActivity(new Intent(activity, RecipeDetail.class)
                        .putExtra("recipeId", model.getRecipeId()));
            }
        });
        recyclerView.setLayoutManager(new LinearLayoutManager(activity));
        recyclerView.setAdapter(adapter);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        activity.getMenuInflater().inflate(R.menu.menu_searchrecipe, menu);
        menu_search = menu.findItem(R.id.menu_searchrecipe);
        menu_search.expandActionView(); // Expand the search menu item in order to show by default the query
        searchView = (SearchView) menu_search.getActionView();

        ImageView iconClose = (ImageView) searchView.findViewById(androidx.appcompat.R.id.search_close_btn);
        iconClose.setColorFilter(getResources().getColor(R.color.black));

        EditText searchEditText = (EditText) searchView.findViewById(androidx.appcompat.R.id.search_src_text);
        searchEditText.setTextColor(getResources().getColor(R.color.black));
        searchEditText.setHintTextColor(getResources().getColor(R.color.mgray));

        searchView.setQueryHint("Enter recipe name");
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {

                globalclass.hideKeyboard(activity);
                if (s.length() > 0) {
                    arrayList.clear();
                    arrayList = mydatabase.getSearchRecipeList(s);
                    if (!arrayList.isEmpty()) {
                        setAdapter();
                    } else {
                        recyclerView.setVisibility(View.GONE);
                        noSearchResultLayout.setVisibility(View.VISIBLE);
                    }
                } else {
                    globalclass.toast_short("Enter recipe name!");
                }
                return false;
            }

            @Override
            public boolean onQueryTextChange(String s) {

                if (s.length() > 0) {
                    arrayList.clear();
                    arrayList = mydatabase.getSearchRecipeList(s);
                    if (!arrayList.isEmpty()) {
                        setAdapter();
                    } else {
                        recyclerView.setVisibility(View.GONE);
                        noSearchResultLayout.setVisibility(View.VISIBLE);
                    }
                } else {
                    getData();
                }

                return false;
            }
        });

        return super.onCreateOptionsMenu(menu);
    }
}