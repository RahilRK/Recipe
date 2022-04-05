package com.hksofttronix.khansama.CartActivity;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import android.os.Bundle;

import com.hksofttronix.khansama.CartFrag.CartFrag;
import com.hksofttronix.khansama.Globalclass;
import com.hksofttronix.khansama.R;

public class CartActivity extends AppCompatActivity {

    String tag = this.getClass().getSimpleName();
    AppCompatActivity activity = CartActivity.this;

    Globalclass globalclass;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cart);

        init();
        loadFragment();
    }

    void init() {
        globalclass = Globalclass.getInstance(activity);
    }

    public void loadFragment() {

        //creating fragment object
        Fragment fragment = null;

        fragment = new CartFrag();
        Bundle args = new Bundle();
        args.putString("action", tag);
        fragment.setArguments(args);

        //replacing the fragment
        if (fragment != null) {
            FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
            ft.replace(R.id.content_frame, fragment);
            ft.commit();
        }
    }
}