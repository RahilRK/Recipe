package com.hksofttronix.khansama;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.google.android.material.card.MaterialCardView;

public class ContactUs extends AppCompatActivity {

    String tag = this.getClass().getSimpleName();
    AppCompatActivity activity = ContactUs.this;

    Globalclass globalclass;

    MaterialCardView callUsCardView,emailUsCardView,whatAppUsCardView;
    TextView contactUsMobileNumber,contactUsEmailId,contactUsWhatsAppNumber;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contact_us);

        setToolbar();
        init();
        binding();
        setText();
        onClick();
    }

    void init() {
        globalclass = Globalclass.getInstance(activity);
    }

    void setToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_close);
        toolbar.setTitle(R.string.contact_us);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                onBackPressed();
            }
        });
    }

    void binding() {
        callUsCardView = findViewById(R.id.callUsCardView);
        emailUsCardView = findViewById(R.id.emailUsCardView);
        whatAppUsCardView = findViewById(R.id.whatAppUsCardView);
        contactUsMobileNumber = findViewById(R.id.contactUsMobileNumber);
        contactUsEmailId = findViewById(R.id.contactUsEmailId);
        contactUsWhatsAppNumber = findViewById(R.id.contactUsWhatsAppNumber);
    }

    void setText() {
        contactUsMobileNumber.setText("+91 "+globalclass.contactUsNumber());
        contactUsEmailId.setText(globalclass.contactUsEmailId());
        contactUsWhatsAppNumber.setText("+91 "+globalclass.contactUsNumber());
    }

    void onClick() {

        callUsCardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                openDialer(globalclass.contactUsNumber());
            }
        });

        emailUsCardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                openEmail(globalclass.contactUsEmailId());
            }
        });

        whatAppUsCardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                openWhatsApp(globalclass.contactUsNumber());
            }
        });
    }

    public void openDialer(String phone)
    {
        Intent intent = new Intent(Intent.ACTION_DIAL);
        intent.setData(Uri.parse("tel:"+phone));
        startActivity(intent);
    }

    public void openEmail(String email)
    {
        String mailto = "mailto:" +email+

                "?subject=" + Uri.encode("Regarding KHANSAMA APP: \nuserid: "+globalclass.getStringData("id")) +
                "&body=" + Uri.encode("");

        Intent emailIntent = new Intent(Intent.ACTION_SENDTO);
        emailIntent.setData(Uri.parse(mailto));

        try {
            startActivity(emailIntent);
        } catch (ActivityNotFoundException e) {
            globalclass.toast_long("No email app found!");
        }
    }

    void openWhatsApp(String number)
    {
        try
        {
            String text;
            text = "Regarding KHANSAMA APP: \n";

            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(Uri.parse("http://api.whatsapp.com/send?phone="+"+91"+number +"&text="+text));
            activity.startActivity(intent);
        }
        catch (Exception e)
        {
            String error = Log.getStackTraceString(e);
            globalclass.log("openWhatsApp", error);
            globalclass.toast_long("Unable to proceed, please try after sometime!");
            globalclass.sendErrorLog(tag, "openWhatsApp", error);
        }
    }
}