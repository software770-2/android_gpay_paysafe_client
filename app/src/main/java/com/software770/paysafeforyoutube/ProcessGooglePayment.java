package com.software770.paysafeforyoutube;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.software770.paysafeforyoutube.otherClasses.GoogleReqInfos;
import com.software770.paysafeforyoutube.otherClasses.ThreadSingleUseToken;
import com.software770.utils.Constants;
import com.software770.utils.Utils;

public class ProcessGooglePayment extends AppCompatActivity {

    private TextView txtAmount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_process_google_payment);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        txtAmount = (TextView)findViewById(R.id.txtAmount);

        txtAmount.setText(getIntent().getStringExtra("amount"));

        Button btnProcess = (Button)findViewById(R.id.process);

        btnProcess.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fncProcess();
            }
        });
    }


    private void fncProcess()
    {
        // first check if there is internet:
        boolean isNetworkAvailable = Utils.isNetworkAvailable(getApplication());
        boolean isOnline = Utils.isOnline(getApplication());
        if (isNetworkAvailable == false && isOnline == false) {
            Toast.makeText(getApplicationContext(),	Constants.PLEASE_TURN_ON_YOUR_INTERNET, Toast.LENGTH_LONG).show();
        }
        else
        {
            GoogleReqInfos model = new GoogleReqInfos();
            // Get data from Intent
            Intent intentPayWithGoogle = getIntent();

            model.mSignature = intentPayWithGoogle.getStringExtra("Signature");
            model.mProtocolVersion = intentPayWithGoogle.getStringExtra("ProtocolVersion");
            model.mSignedMessage = intentPayWithGoogle.getStringExtra("SignedMessage");


            new ThreadSingleUseToken(this, this.getString(R.string.loading_text), model, getIntent().getStringExtra("amount")).execute();
        }
    }

}
